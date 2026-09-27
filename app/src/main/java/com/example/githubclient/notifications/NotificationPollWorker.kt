package com.example.githubclient.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.githubclient.MainActivity
import com.example.githubclient.R
import com.example.githubclient.data.NetworkModule
import com.example.githubclient.data.TokenManager

/**
 * GitHub has no push channel for third-party, personal-access-token clients
 * (that requires a registered GitHub App with a server to relay webhooks to
 * FCM/APNs). The practical alternative -- and what most token-based GitHub
 * clients do -- is to poll the notifications endpoint periodically in the
 * background and surface anything new as a local notification.
 */
class NotificationPollWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val tokenManager = TokenManager(applicationContext)
        if (!tokenManager.hasToken()) return Result.success()

        return try {
            val clients = NetworkModule.buildClients(tokenManager)
            val notifications = clients.rest.getNotifications(all = false)

            val prefs = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val seen = prefs.getStringSet(KEY_SEEN, emptySet())?.toMutableSet() ?: mutableSetOf()

            val newOnes = notifications.filter { it.id !in seen }
            newOnes.forEach { n ->
                postNotification(
                    title = n.subject.title,
                    text = "${n.reason.replace('_', ' ').replaceFirstChar { c -> c.uppercase() }} \u00b7 ${n.repository.fullName}"
                )
                seen.add(n.id)
            }

            // Cap how many IDs we remember so this doesn't grow forever.
            val trimmed = if (seen.size > 300) seen.toList().takeLast(300).toSet() else seen
            prefs.edit().putStringSet(KEY_SEEN, trimmed).apply()

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun postNotification(title: String, text: String) {
        val context = applicationContext
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.EXTRA_NAVIGATE_TO, MainActivity.NAV_NOTIFICATIONS)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

    companion object {
        const val CHANNEL_ID = "github_notifications"
        private const val PREFS_NAME = "notif_poll_prefs"
        private const val KEY_SEEN = "seen_ids"
        const val UNIQUE_WORK_NAME = "github-notification-poll"

        fun createChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "GitHub notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "New issues, PR activity, and mentions from GitHub"
                }
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.createNotificationChannel(channel)
            }
        }
    }
}
