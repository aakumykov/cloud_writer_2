package com.github.aakumykov.fragments

import android.os.Bundle
import android.os.Environment
import android.view.View
import com.github.aakumykov.other.StorageType
import com.github.aakumykov.cloud_writer.CloudWriter
import com.github.aakumykov.cloud_writer_2.R
import com.github.aakumykov.cloud_writer_2.databinding.FragmentDirCreationBinding
import com.github.aakumykov.extensions.yandexAuthToken
import com.github.aakumykov.local_cloud_writer.LocalCloudWriter
import com.github.aakumykov.storage_access_helper.StorageAccessHelper
import com.github.aakumykov.utils.randomInt3
import com.github.aakumykov.utils.shortUUID
import com.github.aakumykov.yandex_disk_cloud_writer.YandexDiskCloudWriter
import kotlinx.coroutines.Dispatchers

class DirCreationFragment : BasicFragment<FragmentDirCreationBinding>(R.layout.fragment_dir_creation) {

    private var currentStorageType: StorageType = StorageType.LOCAL
    private var currentAuthToken: String? = null
    private lateinit var storageAccessHelper: StorageAccessHelper


    override fun onStartButtonClicked() {
        doWorkWithGuiFeedback (Dispatchers.IO) {
            when(currentStorageType) {
                StorageType.LOCAL -> createLocalDir()
                StorageType.YANDEX_DISK -> createYandexDir()
            }
        }
    }

    private fun createLocalDir(): String {
        storageAccessHelper.requestWriteAccess {
            cloudWriter.createDir(dirPath)
        }
        return dirPath
    }

    private fun createYandexDir(): String {
        return cloudWriter.createDir(dirPath)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDirCreationBinding.bind(view)

        binding.refreshPathButton.setOnClickListener { refreshPath() }
        binding.clearPathButton.setOnClickListener { clearPath() }
        binding.include.startButton.setOnClickListener { onStartButtonClicked() }

//        currentAuthToken = radioButton2authToken(binding.storageTypeRadioGroup.checkedRadioButtonId)

        binding.storageTypeRadioGroup.apply {
            setOnCheckedChangeListener { _, checkedId ->
                currentStorageType = radioButton2storageType(checkedId)
                currentAuthToken = radioButton2authToken(checkedId)
            }
            check(storageTypeToRadioId(currentStorageType))
        }

        storageAccessHelper = StorageAccessHelper.Companion.create(this).apply {
            prepareForWriteAccess()
        }

        refreshPath()
    }


    private fun storageTypeToRadioId(storageType: StorageType): Int {
        return when(storageType) {
            StorageType.LOCAL -> R.id.storageTypeLocal
            StorageType.YANDEX_DISK -> R.id.storageTypeYandexDisk
        }
    }


    private fun radioButton2authToken(checkedId: Int): String? {
        return when(checkedId) {
            R.id.storageTypeLocal -> ""
            R.id.storageTypeYandexDisk -> activity?.yandexAuthToken
            else -> null
        }
    }

    private fun radioButton2storageType(checkedId: Int): StorageType {
        return when(checkedId) {
            R.id.storageTypeLocal -> StorageType.LOCAL
            R.id.storageTypeYandexDisk -> StorageType.YANDEX_DISK
            else -> throw IllegalStateException("Неизвестное значение checkedId: $checkedId")
        }
    }

    private val cloudWriter: CloudWriter get() = when(currentStorageType) {
        StorageType.LOCAL -> LocalCloudWriter(Environment.getExternalStorageDirectory().absolutePath)
        StorageType.YANDEX_DISK -> YandexDiskCloudWriter(currentAuthToken!!)
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


    private val dirPath: String get() = binding.pathInput.text.toString()


    companion object {
        val TAG: String = DirCreationFragment::class.java.simpleName
        fun create(): DirCreationFragment {
            return DirCreationFragment()
        }
    }
}