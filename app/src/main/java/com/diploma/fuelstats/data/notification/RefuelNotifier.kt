package com.diploma.fuelstats.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.diploma.fuelstats.MainActivity
import com.diploma.fuelstats.R

/**
 * Показ локального уведомления о возможной заправке.
 */
class RefuelNotifier(private val context: Context) {

    private val channelId = "refuel_channel"
    private val notificationId = 1001

    init {
        createChannelIfNeeded()
    }

    private fun createChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Напоминания о заправке",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Уведомления при обнаружении заправки автомобиля"
            }
            context.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    fun showRefuelDetected() {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_road_24)
            .setContentTitle("Вы заправились?")
            .setContentText("Не забудьте внести запись о заправке")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context)
                .notify(notificationId, notification)
        } catch (_: SecurityException) {
            // игнорируем
        }
    }
}