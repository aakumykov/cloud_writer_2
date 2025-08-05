package com.github.aakumykov.local_cloud_writer

import com.github.aakumykov.cloud_writer.CloudWriter2
import org.junit.Assert
import org.junit.Test

class LocalCloudWriter_virtualRootPlus_UnitTests {

    companion object {
        const val EMPTY_DIR_NAME = ""
        const val ROOT_DIR_NAME = "/"
        const val SIMPLE_DIR_NAME = "dir1"
        const val DEEP_DIR_NAME = "dir_A/dir_B"
    }

    private val localCloudWriter2: CloudWriter2 = LocalCloudWriter2("/")

    @Test
    fun joins_empty_path_with_virtual_root() {
        Assert.assertEquals(
            localCloudWriter2.virtualRootPath,
            localCloudWriter2.virtualRootPlus(EMPTY_DIR_NAME)
        )
    }

    @Test
    fun joins_root_path_with_virtual_root() {
        Assert.assertEquals(
            localCloudWriter2.virtualRootPath,
            localCloudWriter2.virtualRootPlus(ROOT_DIR_NAME)
        )
    }

    @Test
    fun joins_simple_dir_with_virtual_root() {
        Assert.assertEquals(
            "${localCloudWriter2.virtualRootPath}${SIMPLE_DIR_NAME}",
            localCloudWriter2.virtualRootPlus(SIMPLE_DIR_NAME)
        )
    }

    @Test
    fun joins_deep_dir_with_virtual_root() {
        Assert.assertEquals(
            "${localCloudWriter2.virtualRootPath}${DEEP_DIR_NAME}",
            localCloudWriter2.virtualRootPlus(DEEP_DIR_NAME)
        )
    }
}