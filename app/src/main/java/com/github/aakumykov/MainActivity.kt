package com.github.aakumykov

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.github.aakumykov.cloud_writer_2.R
import com.github.aakumykov.cloud_writer_2.databinding.ActivityMainBinding
import com.github.aakumykov.fragments.AuthFragment
import com.github.aakumykov.yandex_auth_helper.YandexAuthHelper

class MainActivity : AppCompatActivity(), YandexAuthHelper.Callbacks {

    private lateinit var yandexAuthHelper: YandexAuthHelper

    private val sharedPreferences: SharedPreferences get() = PreferenceManager.getDefaultSharedPreferences(this)

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.yandexAuthButton.setOnClickListener { yandexAuthHelper.beginAuthorization() }

        restoreAuthToken()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        yandexAuthHelper = YandexAuthHelper(this, MainActivity.AUTH_REQUEST_CODE, this)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragmentContainerView, AuthFragment.create())
            .commit()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AUTH_REQUEST_CODE) yandexAuthHelper.processAuthResult(
            requestCode,
            resultCode,
            data
        )
    }

    companion object {
        val TAG: String = MainActivity::class.java.simpleName
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
        sharedPreferences
            .edit()
            .putString(YANDEX_AUTH_TOKEN, authToken)
            .apply()
    }

    private fun restoreAuthToken() {
        sharedPreferences.getString(YANDEX_AUTH_TOKEN,null).also { authToken: String? ->
            if (null != authToken) showAuthToken(authToken)
            else hideAuthToken()
        }
    }

    private fun showError(errorMsg: String) {
        binding.errorView.text = errorMsg
    }

    private fun hideError() {
        binding.errorView.text = ""
    }

    private fun showAuthToken(authToken: String) {
        binding.authTokenView.text = authToken
    }

    private fun hideAuthToken() {
        binding.authTokenView.text = ""
    }


}