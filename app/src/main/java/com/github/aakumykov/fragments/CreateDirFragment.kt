package com.github.aakumykov.fragments

import android.os.Bundle
import android.os.Environment
import android.view.View
import com.github.aakumykov.cloud_writer.CloudWriter
import com.github.aakumykov.cloud_writer_2.R
import com.github.aakumykov.cloud_writer_2.databinding.FragmentCreateDirBinding
import com.github.aakumykov.extensions.yandexAuthToken
import com.github.aakumykov.local_cloud_writer.LocalCloudWriter
import com.github.aakumykov.utils.randomInt3
import com.github.aakumykov.utils.shortUUID
import com.github.aakumykov.yandex_disk_cloud_writer.YandexDiskCloudWriter
import kotlinx.coroutines.Dispatchers
import java.io.File

class CreateDirFragment : BasicFragment<FragmentCreateDirBinding>(R.layout.fragment_create_dir) {

    private var currentStorageType: StorageType? = null
    private var currentAuthToken: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCreateDirBinding.bind(view)

        binding.refreshPathButton.setOnClickListener { refreshPath() }
        binding.clearPathButton.setOnClickListener { clearPath() }
        binding.include.startButton.setOnClickListener { onStartButtonClicked() }

        currentStorageType = radioButton2storageType(binding.storageTypeRadioGroup.checkedRadioButtonId)
        currentAuthToken = radioButton2authToken(binding.storageTypeRadioGroup.checkedRadioButtonId)

        binding.storageTypeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            currentStorageType = radioButton2storageType(checkedId)
            currentAuthToken = radioButton2authToken(checkedId)
        }

        refreshPath()
    }

    private fun radioButton2authToken(checkedId: Int): String? {
        return when(checkedId) {
            R.id.storageTypeLocal -> ""
            R.id.storageTypeYandexDisk -> activity?.yandexAuthToken
            else -> null
        }
    }

    private fun radioButton2storageType(checkedId: Int): StorageType? {
        return when(checkedId) {
            R.id.storageTypeLocal -> StorageType.LOCAL
            R.id.storageTypeYandexDisk -> StorageType.YANDEX_DISK
            else -> null
        }
    }

    private val cloudWriter: CloudWriter? get() {
        return try {
            when(currentStorageType) {
                StorageType.LOCAL -> LocalCloudWriter(virtualRootDir = Environment.getExternalStorageDirectory().absolutePath)
                StorageType.YANDEX_DISK -> YandexDiskCloudWriter(currentAuthToken!!)
                else -> {
                    throw Exception("currentStorageType == null")
                }
            }
        } catch (e: Exception) {
            showError(e)
            null
        }
    }


    private val randomPath: String get() = buildList<String> {
            repeat(randomInt3) {
                add(shortUUID)
            }
    }.joinToString(CloudWriter.DS)


    private fun refreshPath() {
        binding.pathInput.setText(randomPath)
    }


    private fun clearPath() {
        binding.pathInput.setText("")
    }


    override fun onStartButtonClicked() {
        doWorkWithGuiFeedback (Dispatchers.IO) {
            cloudWriter?.createDeepDirIfNotExists(dirPath).let {
                getString(R.string.dir_created, dirPath)
            }
        }
    }



    private val dirPath: String get() = binding.pathInput.text.toString()


    companion object {
        val TAG: String = CreateDirFragment::class.java.simpleName
        fun create(): CreateDirFragment {
            return CreateDirFragment()
        }
    }
}