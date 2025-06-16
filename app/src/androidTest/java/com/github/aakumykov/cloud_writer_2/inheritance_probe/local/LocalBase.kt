package com.github.aakumykov.cloud_writer_2.inheritance_probe.local

import com.github.aakumykov.cloud_writer_2.inheritance_probe.CloudWriter2Base
import java.io.File

abstract class LocalBase : CloudWriter2Base() {

    override fun checkFileExistsNative(path: String, isRelative: Boolean): Boolean {
        val fullPath = if(isRelative) aggregateNamesToPath(virtualRootPath, path) else path
        return File(fullPath).exists()
    }
}