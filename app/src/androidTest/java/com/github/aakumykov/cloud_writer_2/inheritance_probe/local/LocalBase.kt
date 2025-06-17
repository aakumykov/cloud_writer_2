package com.github.aakumykov.cloud_writer_2.inheritance_probe.local

import android.os.Environment
import com.github.aakumykov.cloud_writer.CloudWriter2
import com.github.aakumykov.cloud_writer_2.inheritance_probe.CloudWriter2Tests
import com.github.aakumykov.local_cloud_writer.LocalCloudWriter2

abstract class LocalBase : CloudWriter2Tests() {

    override val virtualRootPath: String
        get() = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath

    override val cloudWriter2: CloudWriter2
        get() = LocalCloudWriter2(virtualRootPath)
}