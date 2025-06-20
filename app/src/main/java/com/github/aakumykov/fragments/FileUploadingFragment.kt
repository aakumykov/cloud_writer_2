package com.github.aakumykov.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentResultListener
import com.github.aakumykov.cloud_writer_2.R
import com.github.aakumykov.cloud_writer_2.databinding.FragmentDirCreationBinding
import com.github.aakumykov.cloud_writer_2.databinding.FragmentFileUploadingBinding
import com.github.aakumykov.file_lister_navigator_selector.extensions.listenForFragmentResult
import com.github.aakumykov.file_lister_navigator_selector.file_lister.SimpleSortingMode
import com.github.aakumykov.file_lister_navigator_selector.file_selector.FileSelector
import com.github.aakumykov.local_file_lister_navigator_selector.local_file_selector.LocalFileSelector
import com.github.aakumykov.storage_access_helper.StorageAccessHelper

class FileUploadingFragment :
    BasicFragment<FragmentFileUploadingBinding>(R.layout.fragment_file_uploading),
    FragmentResultListener
{
    private lateinit var storageAccessHelper: StorageAccessHelper

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
    }

    private fun selectFile() {
        storageAccessHelper.requestFullAccess { isGranted ->
            if (isGranted) fileSelector.show(childFragmentManager, FileSelector.TAG)
            else showToast(R.string.there_is_no_reading_access)
        }
    }

    override fun onStartButtonClicked() {
        TODO("Not yet implemented")
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
            binding.selectedFileView.text = getString(
                R.string.selection_result, list.joinToString("\n") { it.absolutePath })
        }
    }
}