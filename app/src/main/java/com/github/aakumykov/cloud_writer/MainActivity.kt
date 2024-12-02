package com.github.aakumykov.cloud_writer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.aakumykov.cloud_writer.fragments.ReadingAndDirCreationFragment
import com.github.aakumykov.cloud_writer_2.R
import com.github.aakumykov.cloud_writer_2.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.fragmentContainerView,
                ReadingAndDirCreationFragment.create()
//                Fragment1.create()
            )
            .commit()
    }
}