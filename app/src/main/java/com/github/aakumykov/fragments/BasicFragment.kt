package com.github.aakumykov.fragments

import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.github.aakumykov.cloud_writer_2.R
import com.github.aakumykov.extensions.errorMsg
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class BasicFragment<T : ViewBinding>(layoutResId: Int) : Fragment(layoutResId) {

    protected var _binding: T? = null
    protected val binding: T get() = _binding!!

    private val errorView: TextView by lazy { binding.root.findViewById(R.id.errorView) }
    private val messageView: TextView by lazy { binding.root.findViewById(R.id.messageView) }
    private val progressBar: ProgressBar by lazy { binding.root.findViewById(R.id.progressBar) }


    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    abstract fun onStartButtonClicked()

    protected fun doWorkWithGuiFeedback(
        coroutineDispatcher: CoroutineDispatcher = Dispatchers.Main,
        block: suspend () -> String
    ) {
        lifecycleScope.launch (coroutineDispatcher) {
            try {
                hideError()
                hideMessage()
                showProgressBar()

                block.invoke().also { message ->
                    if (message.isNotEmpty())
                        showMessage(message)
                }

            } catch (t: Throwable) {
                showError(t)
            } finally {
                hideProgressBar()
            }
        }
    }


    protected fun showToast(stringRes: Int) {
        lifecycleScope.launch (Dispatchers.Main) {
            Toast.makeText(context, getString(stringRes), Toast.LENGTH_SHORT).show()
        }
    }

    protected fun showError(t: Throwable) {
        lifecycleScope.launch (Dispatchers.Main) {
            t.errorMsg.also { errorMsg ->
                Log.e(CreateDirFragment.TAG, errorMsg, t)
                errorView.text = errorMsg
            }
        }
    }

    protected fun hideError() {
        lifecycleScope.launch (Dispatchers.Main) {
            errorView.text = ""
        }
    }

    private fun showMessage(text: String) {
        lifecycleScope.launch (Dispatchers.Main) {
            messageView.text = text
        }
    }

    protected fun hideMessage() {
        lifecycleScope.launch (Dispatchers.Main) {
            messageView.text = ""
        }
    }

    protected fun showProgressBar() {
        lifecycleScope.launch (Dispatchers.Main) {
            progressBar.visibility = View.VISIBLE
        }
    }

    private fun hideProgressBar() {
        lifecycleScope.launch (Dispatchers.Main) {
            progressBar.visibility = View.INVISIBLE
        }
    }
}