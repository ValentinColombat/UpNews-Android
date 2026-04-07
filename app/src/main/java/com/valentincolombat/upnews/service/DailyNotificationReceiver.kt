package com.valentincolombat.upnews.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
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

        val notification = NotificationCompat.Builder(context, NotificationManager.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Ta bonne nouvelle t'attend ! ☀️")
            .setContentText("Découvre l'article du jour sur UpNews")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(context)
            .notify(NotificationManager.NOTIFICATION_ID, notification)
    }
}
