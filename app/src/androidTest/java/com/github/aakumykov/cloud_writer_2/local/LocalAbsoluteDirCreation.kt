package com.github.aakumykov.cloud_writer_2.local

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.aakumykov.cloud_writer_2.DirCreationTests
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LocalAbsoluteDirCreation(
    private val lab: Props = LocalAbsoluteBase()
) : DirCreationTests(), Props by lab {

}
