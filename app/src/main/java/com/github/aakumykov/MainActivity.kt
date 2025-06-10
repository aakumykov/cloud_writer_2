package com.github.aakumykov

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.github.aakumykov.cloud_writer_2.R
import com.github.aakumykov.cloud_writer_2.databinding.ActivityMainBinding
import com.github.aakumykov.extensions.defaultSharedPreferences
import com.github.aakumykov.extensions.gone
import com.github.aakumykov.extensions.visible
import com.github.aakumykov.extensions.yandexAuthToken
import com.github.aakumykov.fragments.StartFragment
import com.github.aakumykov.yandex_auth_helper.YandexAuthHelper

class MainActivity : AppCompatActivity(), YandexAuthHelper.Callbacks {

    private var currentAuthToken: String? = null

    private lateinit var yandexAuthHelper: YandexAuthHelper

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.yandexAuthButton.setOnClickListener { onAuthButtonClicked() }

        restoreAuthToken()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        yandexAuthHelper = YandexAuthHelper(this, AUTH_REQUEST_CODE, this)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragmentContainerView, StartFragment.create())
            .commit()
    }

    private fun onAuthButtonClicked() {
        hideError()
        if (null == currentAuthToken) yandexAuthHelper.beginAuthorization()
        else hideAuthToken()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AUTH_REQUEST_CODE) yandexAuthHelper.processAuthResult(
            requestCode,
            resultCode,
            data
        )
    }

    companion object {
        const val AUTH_REQUEST_CODE = 10
        const val YANDEX_AUTH_TOKEN = "YANDEX_AUTH_TOKEN"
    }

    override fun onYandexAuthSuccess(authToken: String) {
        storeAuthToken(authToken)
        showAuthToken(authToken)
    }

    override fun onYandexAuthFailed(errorMsg: String) {
        showError(errorMsg)
    }

    private fun storeAuthToken(authToken: String) {
        defaultSharedPreferences
            .edit()
            .putString(YANDEX_AUTH_TOKEN, authToken)
            .apply()
    }

    private fun clearStoredAuthToken() {
        defaultSharedPreferences
            .edit()
            .remove(YANDEX_AUTH_TOKEN)
            .apply()
    }

    private fun restoreAuthToken() {
        yandexAuthToken?.also {
            showAuthToken(it)
        } ?: run {
            hideAuthToken()
        }
    }

    private fun showError(errorMsg: String) {
        binding.errorView.text = errorMsg
    }

    private fun hideError() {
        binding.errorView.text = ""
    }

    private fun showAuthToken(authToken: String) {
        currentAuthToken = authToken
        binding.authTokenView.text = authToken
        binding.yandexAuthButton.setText(R.string.clear_auth)
        binding.loginType.gone()
        binding.errorView.gone()
    }

    private fun hideAuthToken() {
        clearStoredAuthToken()

        currentAuthToken = null
        binding.authTokenView.text = ""
        binding.yandexAuthButton.setText(R.string.auth_in_yandex)
        binding.loginType.visible()
        binding.errorView.visible()
    }


}