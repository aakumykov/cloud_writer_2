package com.github.aakumykov.cloud_writer.cloud_authenticator

interface CloudAuthenticator {

    fun startAuth()

    interface Callbacks {
        fun onCloudAuthSuccess(authToken: String)
        fun onCloudAuthFailed(throwable: Throwable)
    }
}