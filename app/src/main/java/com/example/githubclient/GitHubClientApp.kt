package com.example.githubclient

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.githubclient.data.GitHubClients
import com.example.githubclient.data.NetworkModule
import com.example.githubclient.data.TokenManager
import com.example.githubclient.notifications.NotificationPollWorker
import java.util.concurrent.TimeUnit

class GitHubClientApp : Application() {

    lateinit var tokenManager: TokenManager
        private set

    var clients: GitHubClients? = null
        private set

    override fun onCreate() {
        super.onCreate()
        tokenManager = TokenManager(this)
        refreshApi()
        NotificationPollWorker.createChannel(this)
        if (tokenManager.hasToken()) schedulePolling()
    }

    /** Call after the token changes (login/logout) to rebuild the authenticated clients. */
    fun refreshApi() {
        clients = NetworkModule.buildClients(tokenManager)
        if (tokenManager.hasToken()) schedulePolling() else cancelPolling()
    }

    private fun schedulePolling() {
        // 15 minutes is WorkManager's minimum interval for periodic work.
        val request = PeriodicWorkRequestBuilder<NotificationPollWorker>(15, TimeUnit.MINUTES)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            NotificationPollWorker.UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    private fun cancelPolling() {
        WorkManager.getInstance(this).cancelUniqueWork(NotificationPollWorker.UNIQUE_WORK_NAME)
    }
}
