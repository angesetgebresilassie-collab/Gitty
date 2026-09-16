package com.example.githubclient

import android.app.Application
import com.example.githubclient.data.GitHubClients
import com.example.githubclient.data.NetworkModule
import com.example.githubclient.data.TokenManager

class GitHubClientApp : Application() {

    lateinit var tokenManager: TokenManager
        private set

    var clients: GitHubClients? = null
        private set

    override fun onCreate() {
        super.onCreate()
        tokenManager = TokenManager(this)
        refreshApi()
    }

    /** Call after the token changes (login/logout) to rebuild the authenticated clients. */
    fun refreshApi() {
        clients = NetworkModule.buildClients(tokenManager)
    }
}
