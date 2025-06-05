package com.github.aakumykov.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.github.aakumykov.cloud_writer_2.R
import com.github.aakumykov.cloud_writer_2.databinding.FragmentCreateDirBinding
import com.github.aakumykov.utils.randomInt5
import com.github.aakumykov.utils.shortUUID

class CreateDirFragment : Fragment(R.layout.fragment_create_dir) {

    private var _binding: FragmentCreateDirBinding? = null
    private val binding: FragmentCreateDirBinding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCreateDirBinding.bind(view)

        binding.refreshPathButton.setOnClickListener { refreshPath() }
        binding.clearPathButton.setOnClickListener { clearPath() }
        binding.include.startButton.setOnClickListener { onStartButtonClicked() }

        refreshPath()
    }

    private val randomPath: String get() = buildList<String> {
            repeat(randomInt5) {
                add(shortUUID)
            }
        }.joinToString("/")

    private fun refreshPath() {
        binding.pathInput.text = randomPath
    }

    private fun clearPath() {
        binding.pathInput.text = ""
    }

    private fun onStartButtonClicked() {

    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        fun create(): CreateDirFragment {
            return CreateDirFragment()
        }
    }
}