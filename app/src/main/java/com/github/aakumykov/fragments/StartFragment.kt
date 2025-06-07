package com.github.aakumykov.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import com.github.aakumykov.cloud_writer_2.R
import com.github.aakumykov.cloud_writer_2.databinding.FragmentStartBinding


class StartFragment : Fragment(R.layout.fragment_start) {

    private var _binding: FragmentStartBinding? = null
    private val binding: FragmentStartBinding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentStartBinding.bind(view)
        binding.createDirFragmentButton.setOnClickListener { loadFragment(DirCreationFragment.create()) }
    }

    private fun loadFragment(fragment: DirCreationFragment) {
        parentFragmentManager
            .beginTransaction()
            .addToBackStack(null)
            .replace(R.id.fragmentContainerView, fragment)
            .commit()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        @JvmStatic
        fun create() = StartFragment()
    }
}