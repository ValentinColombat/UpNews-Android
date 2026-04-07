package com.valentincolombat.upnews

import android.app.Application
import com.valentincolombat.upnews.data.billing.BillingManager
import com.valentincolombat.upnews.data.remote.SupabaseClientProvider
import com.valentincolombat.upnews.service.NotificationManager

class UpNewsApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        SupabaseClientProvider.init(this)
        BillingManager.init(this)
        NotificationManager.createNotificationChannel(this)
    }
}
