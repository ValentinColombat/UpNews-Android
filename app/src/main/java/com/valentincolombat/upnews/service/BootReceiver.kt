package com.valentincolombat.upnews.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Reprogramme l'alarme après un redémarrage du téléphone.
 * Les alarmes AlarmManager sont effacées à chaque reboot — ce receiver les restaure.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val savedTime = NotificationManager.getSavedTime(context) ?: return
        NotificationManager.scheduleDailyNotification(context, savedTime)
    }
}
