package com.github.aakumykov.cloud_writer_2.inheritance_probe

import com.github.aakumykov.cloud_writer.CloudWriter2
import com.github.aakumykov.cloud_writer_2.common.StorageAccessTestCase

abstract class Base : StorageAccessTestCase() {

    protected fun aggregateNamesToPath(vararg dirName: String): String
            = dirName
        .filterNot { it.isEmpty() }
        .joinToString(CloudWriter2.DS)
}