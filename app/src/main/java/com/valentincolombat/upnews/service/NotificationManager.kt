package com.valentincolombat.upnews.service

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager as AndroidNotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import java.util.Calendar

/**
 * Équivalent de NotificationManager.swift (iOS).
 *
 * ⚠️ La demande de permission runtime POST_NOTIFICATIONS (Android 13+)
 * doit être déclenchée depuis la couche UI via rememberLauncherForActivityResult.
 * Ce singleton gère uniquement : vérification du statut, scheduling, annulation.
 */
object NotificationManager {

    const val CHANNEL_ID = "daily_article"
    const val NOTIFICATION_ID = 1001
    private const val ALARM_REQUEST_CODE = 2001
    const val PREFS_NAME = "upnews_notif"
    const val KEY_LAST_OPEN_DATE = "last_open_date"
    private const val KEY_NOTIF_TIME = "notification_time"

    // MARK: - Channel (à appeler au démarrage de l'app)

    fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Article du jour",
            AndroidNotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notification quotidienne pour l'article du jour"
        }
        val manager = context.getSystemService(AndroidNotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    // MARK: - Permission Status

    /** Équivalent de checkAuthorizationStatus() → true si les notifications sont activées */
    fun checkAuthorizationStatus(context: Context): Boolean =
        NotificationManagerCompat.from(context).areNotificationsEnabled()

    // MARK: - Schedule Notification

    /** Programme une notification quotidienne à l'heure indiquée (format "H:mm" ou "HH:mm") */
    fun scheduleDailyNotification(context: Context, time: String) {
        val (hour, minute) = parseTime(time) ?: return

        // Persiste l'heure pour que BootReceiver puisse reprogrammer après un redémarrage
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_NOTIF_TIME, time).apply()

        cancelAllNotifications(context)
        scheduleNextAlarm(context, hour, minute)
    }

    /**
     * Programme la prochaine alarme pour demain à [hour]:[minute].
     * Utilisé par DailyNotificationReceiver pour enchaîner les jours,
     * et par BootReceiver pour reprogrammer après redémarrage.
     */
    fun scheduleNextAlarm(context: Context, hour: Int, minute: Int) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)

        val intent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            Intent(context, DailyNotificationReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            // Si l'heure est déjà passée aujourd'hui, programmer pour demain
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        // setExactAndAllowWhileIdle : bypasse le mode Doze (contrairement à setInexactRepeating)
        // Sur Android 12+ : vérifie canScheduleExactAlarms() ; sinon fallback sur setAndAllowWhileIdle
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, calendar.timeInMillis, intent
                )
            } else {
                // Fallback : pas exacte mais bypasse quand même le Doze
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, calendar.timeInMillis, intent
                )
            }
        } else {
            // Android 6–11 : setExactAndAllowWhileIdle sans permission spéciale
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, calendar.timeInMillis, intent
            )
        }
    }

    // MARK: - Cancel

    /** Annule toutes les notifications programmées */
    fun cancelAllNotifications(context: Context) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val intent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            Intent(context, DailyNotificationReceiver::class.java),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        intent?.let { alarmManager.cancel(it) }
    }

    // MARK: - Helpers

    /** Récupère l'heure sauvegardée (pour BootReceiver) */
    fun getSavedTime(context: Context): String? =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_NOTIF_TIME, null)

    /** Parse "9:00" ou "09:00" → Pair(9, 0) */
    private fun parseTime(time: String): Pair<Int, Int>? {
        val parts = time.split(":").mapNotNull { it.toIntOrNull() }
        if (parts.size != 2) return null
        return Pair(parts[0], parts[1])
    }
}
