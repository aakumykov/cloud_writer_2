package com.github.aakumykov.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SeekBar
import androidx.fragment.app.FragmentResultListener
import androidx.lifecycle.lifecycleScope
import com.github.aakumykov.cloud_writer_2.R
import com.github.aakumykov.cloud_writer_2.databinding.FragmentFileUploadingBinding
import com.github.aakumykov.extensions.yandexAuthToken
import com.github.aakumykov.file_lister_navigator_selector.extensions.listenForFragmentResult
import com.github.aakumykov.file_lister_navigator_selector.file_lister.SimpleSortingMode
import com.github.aakumykov.file_lister_navigator_selector.file_selector.FileSelector
import com.github.aakumykov.local_file_lister_navigator_selector.local_file_selector.LocalFileSelector
import com.github.aakumykov.storage_access_helper.StorageAccessHelper
import com.github.aakumykov.yandex_disk_cloud_writer.YandexDiskCloudWriter2
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.TimeUnit

class FileUploadingFragment :
    BasicFragment<FragmentFileUploadingBinding>(R.layout.fragment_file_uploading),
    FragmentResultListener, SeekBar.OnSeekBarChangeListener {
    private lateinit var storageAccessHelper: StorageAccessHelper
    private var selectedFilePath: String? = null
    private var job: Job? = null
    private var selectedFile: File? = null
    private val selectedFileSize: Long get() = selectedFile!!.length()

    private val fileSelector: FileSelector<SimpleSortingMode> by lazy {
        LocalFileSelector.create(
            fragmentResultKey = LOCAL_SELECTION_REQUEST_KEY,
            isDirSelectionMode = false,
            isMultipleSelectionMode = false,
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFileUploadingBinding.bind(view)

        prepareStorageAccessHelper()
        prepareFragmentResultListener()

        binding.uploadRetardationSeekBar.setOnSeekBarChangeListener(this@FileUploadingFragment)
        displayUploadRetardation(uploadRetardationMs.toInt())

        binding.selectFileButton.setOnClickListener { selectFile() }
        binding.include.startButton.setOnClickListener { onStartButtonClicked() }
        binding.cancellationButton.setOnClickListener { onCancelButtonClicked() }
    }

    private fun onCancelButtonClicked() {
        job?.cancel(CancellationException(getString(R.string.cancelled_by_user)))
            ?: showToast(R.string.nothing_to_cancel)
    }

    private fun selectFile() {
        storageAccessHelper.requestFullAccess { isGranted ->
            if (isGranted) fileSelector.show(childFragmentManager, FileSelector.TAG)
            else showToast(R.string.there_is_no_reading_access)
        }
    }

    override fun onStartButtonClicked() {

        if (null == selectedFile) {
            showToast(R.string.no_file_selected)
            return
        }

        hideError()
        hideProgress()

        val yandexDiskCloudWriter2 = YandexDiskCloudWriter2(
            authToken = activity?.yandexAuthToken ?: ""
        )

        val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
            showError(throwable)
        }

        job = lifecycleScope.launch (Dispatchers.IO + exceptionHandler) {

            withContext(Dispatchers.Main) {
                binding.include.progressBar.apply {
                    isIndeterminate = false
                }
                setProgressVisibility(View.VISIBLE)
            }

            selectedFile!!.inputStream().use { inputStream ->
                yandexDiskCloudWriter2.putStream(
                    inputStream = inputStream,
                    targetPath = "/${selectedFile!!.name}",
                    isRelative = false,
                    overwriteIfExists = true,
                    readingCallback = ::streamReadingCallback,
                    writingCallback = ::streamWritingCallback,
                    finishCallback = { totalReadBytes, totalWrittenBytes ->
                        lifecycleScope.launch {
                            showProgress(0)
                            binding.include.progressBar.apply {
                                isIndeterminate = true
                                setProgressVisibility(View.INVISIBLE)
                            }
                            Log.d(TAG, "всего прочитано: ${totalReadBytes}\n всего записано: $totalWrittenBytes")
                        }
                    }
                )
            }
        }
    }

    private val uploadRetardationMs: Long
        get() = binding.uploadRetardationSeekBar.progress.toLong()

    private fun streamReadingCallback(readBytesCount: Long) {
//        TimeUnit.MILLISECONDS.sleep(uploadRetardationMs)
    }

    private fun streamWritingCallback(writtenBytesCount: Long) {

        Log.d(TAG, "streamWritingCallback($writtenBytesCount)")

        TimeUnit.MILLISECONDS.sleep(uploadRetardationMs)

        lifecycleScope.launch {
                binding.include.progressBar.apply {
                    writtenBytesCount.toInt().also {
                        showProgress(it)
//                        Log.d(TAG, "progress: $it / $selectedFileSize (${Math.round((1f * it / selectedFileSize) * 1000)/10})")
                    }
                }
        }
    }

    private fun prepareStorageAccessHelper() {
        storageAccessHelper = StorageAccessHelper.Companion.create(this).apply {
            prepareForReadAccess()
            prepareForWriteAccess()
            prepareForFullAccess()
        }
    }

    private fun prepareFragmentResultListener() {
        listenForFragmentResult(LOCAL_SELECTION_REQUEST_KEY, this)
    }

    companion object {
        val TAG: String = FileUploadingFragment::class.java.simpleName
        const val LOCAL_SELECTION_REQUEST_KEY = "FILE_SELECTION"
    }

    override fun onFragmentResult(requestKey: String, result: Bundle) {
        onFileSelected(result)
    }

    private fun onFileSelected(result: Bundle) {
        Log.d(TAG, "onFileSelected()")
        FileSelector.extractSelectionResult(result)?.also { list ->

            selectedFile = File(list.first().absolutePath)
            showProgress(0)
            binding.include.progressBar.max = selectedFile!!.length().toInt()
            binding.selectedFileView.text =
                "${selectedFile!!.absolutePath} (${bytesToHumanReadableSize(selectedFile!!.length().toDouble())})"
        }
    }

    private fun bytesToHumanReadableSize(bytes: Double) = when {
        bytes >= 1 shl 30 -> "%.1f ГБ".format(bytes / (1 shl 30))
        bytes >= 1 shl 20 -> "%.1f МБ".format(bytes / (1 shl 20))
        bytes >= 1 shl 10 -> "%.0f КБ".format(bytes / (1 shl 10))
        else -> "$bytes байт"
    }

    fun humanReadableByteCountBin(bytes: Long) = when {
        bytes == Long.MIN_VALUE || bytes < 0 -> "N/A"
        bytes < 1024L -> "$bytes B"
        bytes <= 0xfffccccccccccccL shr 40 -> "%.1f KiB".format(bytes.toDouble() / (0x1 shl 10))
        bytes <= 0xfffccccccccccccL shr 30 -> "%.1f MiB".format(bytes.toDouble() / (0x1 shl 20))
        bytes <= 0xfffccccccccccccL shr 20 -> "%.1f GiB".format(bytes.toDouble() / (0x1 shl 30))
        bytes <= 0xfffccccccccccccL shr 10 -> "%.1f TiB".format(bytes.toDouble() / (0x1 shl 40))
        bytes <= 0xfffccccccccccccL -> "%.1f PiB".format((bytes shr 10).toDouble() / (0x1 shl 40))
        else -> "%.1f EiB".format((bytes shr 20).toDouble() / (0x1 shl 40))
    }

    private fun showProgress(value: Int) {
        binding.include.progressBar.progress = value
    }

    private fun setProgressVisibility(visibility: Int) {
        binding.include.progressBar.visibility = visibility
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        if (fromUser)
            displayUploadRetardation(progress)
    }

    private fun displayUploadRetardation(progress: Int) {
        binding.uploadRetardationView.text = getString(R.string.upload_retardation, progress)
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {}

    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
}