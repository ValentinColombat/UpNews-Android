package com.valentincolombat.upnews.service

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.valentincolombat.upnews.MainActivity
import com.valentincolombat.upnews.R

/**
 * BroadcastReceiver déclenché par AlarmManager pour poster la notification quotidienne.
 * Équivalent du UNCalendarNotificationTrigger iOS.
 *
 * Après avoir posté la notification, reprogramme l'alarme du lendemain
 * (nécessaire car on utilise setExactAndAllowWhileIdle, non répétable).
 */
class DailyNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Reprogramme pour demain EN PREMIER — avant tout guard.
        // Si les notifs sont désactivées puis réactivées, la chaîne reste intacte.
        val savedTime = NotificationManager.getSavedTime(context)
        if (savedTime != null) {
            val parts = savedTime.split(":").mapNotNull { it.toIntOrNull() }
            if (parts.size == 2) {
                NotificationManager.scheduleNextAlarm(context, parts[0], parts[1])
            }
        }

        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return

        val lastOpenDate = context.getSharedPreferences(NotificationManager.PREFS_NAME, android.content.Context.MODE_PRIVATE)
            .getString(NotificationManager.KEY_LAST_OPEN_DATE, null)
        if (lastOpenDate == java.time.LocalDate.now().toString()) return

        val tapIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, NotificationManager.CHANNEL_ID)
            .setSmallIcon(R.drawable.launch)
            .setColor(ContextCompat.getColor(context, R.color.upnews_orange))
            .setContentTitle("Ta bonne nouvelle t'attend ! ☀️")
            .setContentText("Découvre l'article du jour sur UpNews")
            .setContentIntent(tapIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        NotificationManagerCompat.from(context)
            .notify(NotificationManager.NOTIFICATION_ID, notification)
    }
}
