package com.github.aakumykov.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentResultListener
import androidx.lifecycle.lifecycleScope
import com.github.aakumykov.cloud_writer_2.R
import com.github.aakumykov.cloud_writer_2.databinding.FragmentDirCreationBinding
import com.github.aakumykov.cloud_writer_2.databinding.FragmentFileUploadingBinding
import com.github.aakumykov.extensions.yandexAuthToken
import com.github.aakumykov.file_lister_navigator_selector.extensions.listenForFragmentResult
import com.github.aakumykov.file_lister_navigator_selector.file_lister.SimpleSortingMode
import com.github.aakumykov.file_lister_navigator_selector.file_selector.FileSelector
import com.github.aakumykov.local_file_lister_navigator_selector.local_file_selector.LocalFileSelector
import com.github.aakumykov.storage_access_helper.StorageAccessHelper
import com.github.aakumykov.yandex_disk_cloud_writer.YandexDiskCloudWriter2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class FileUploadingFragment :
    BasicFragment<FragmentFileUploadingBinding>(R.layout.fragment_file_uploading),
    FragmentResultListener
{
    private lateinit var storageAccessHelper: StorageAccessHelper
    private var selectedFilePath: String? = null

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

        binding.selectFileButton.setOnClickListener { selectFile() }
        binding.include.startButton.setOnClickListener { onStartButtonClicked() }
    }

    private fun selectFile() {
        storageAccessHelper.requestFullAccess { isGranted ->
            if (isGranted) fileSelector.show(childFragmentManager, FileSelector.TAG)
            else showToast(R.string.there_is_no_reading_access)
        }
    }

    override fun onStartButtonClicked() {

        if (null == selectedFilePath) {
            showToast(R.string.no_file_selected)
            return
        }

        val yandexDiskCloudWriter2 = YandexDiskCloudWriter2(
            authToken = activity?.yandexAuthToken ?: ""
        )

        lifecycleScope.launch (Dispatchers.IO) {

            val filePath = selectedFilePath!!
            val file = File(filePath)

            withContext(Dispatchers.Main) {
                binding.include.progressBar.apply {
                    isIndeterminate = false
                    visibility = View.VISIBLE
                    max = file.length().toInt()
                }
            }

            File(filePath).inputStream().use { inputStream ->
                yandexDiskCloudWriter2.putStream(
                    inputStream = inputStream,
                    targetPath = "/${file.name}",
                    isRelative = false,
                    overwriteIfExists = true,
                    writingCallback = { count ->
                        lifecycleScope.launch {
                            binding.include.progressBar.apply {
                                progress = count.toInt()

                            }
                        }
                    },
                    finishCallback = { a,b ->
                        lifecycleScope.launch {
                            binding.include.progressBar.apply {
                                isIndeterminate = true
                                visibility = View.INVISIBLE
                            }
                        }
                    }
                )
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
        const val LOCAL_SELECTION_REQUEST_KEY = "FILE_SELECTION"
    }

    override fun onFragmentResult(requestKey: String, result: Bundle) {
        FileSelector.extractSelectionResult(result)?.also { list ->
            selectedFilePath = list.first().absolutePath
            binding.selectedFileView.text = getString(
                R.string.selection_result, list.joinToString("\n") { it.absolutePath })
        }
    }
}