package `in`.binkgo.app.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import `in`.binkgo.app.MainActivity
import `in`.binkgo.app.R

object JobAlertNotifications {
    // New id so importance HIGH applies (Android won't upgrade an existing channel).
    const val CHANNEL_ID = "job_alerts_v2"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        // HIGH so Android shows a heads-up banner (easier to see on the emulator).
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.job_alerts_channel_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = context.getString(R.string.job_alerts_channel_desc)
        }
        manager.createNotificationChannel(channel)
    }

    fun hasPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
    }

    fun show(context: Context, title: String, body: String, notificationId: Int = 1001) {
        if (!hasPermission(context)) return
        ensureChannel(context)
        val openApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            // System icon stays visible in the status bar; launcher mipmaps often look blank.
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }
}
