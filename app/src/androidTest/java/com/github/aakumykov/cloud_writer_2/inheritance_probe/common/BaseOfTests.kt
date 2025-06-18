package com.github.aakumykov.cloud_writer_2.inheritance_probe.common

import com.github.aakumykov.cloud_writer.CloudWriter2

abstract class BaseOfTests : StorageAccessTestCase() {

    protected fun aggregateNamesToPath(vararg dirName: String): String
            = dirName
        .filterNot { it.isEmpty() }
        .joinToString(CloudWriter2.DS)
}