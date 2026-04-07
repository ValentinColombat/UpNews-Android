package com.valentincolombat.upnews

import android.app.Application
import coil.ImageLoader
import coil.SingletonImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.valentincolombat.upnews.data.billing.BillingManager
import com.valentincolombat.upnews.data.remote.SupabaseClientProvider
import com.valentincolombat.upnews.service.NotificationManager
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class UpNewsApplication : Application(), SingletonImageLoader.Factory {

    override fun onCreate() {
        super.onCreate()
        SupabaseClientProvider.init(this)
        BillingManager.init(this)
        NotificationManager.createNotificationChannel(this)
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(50L * 1024 * 1024) // 50 MB
                    .build()
            }
            .okHttpClient {
                OkHttpClient.Builder()
                    .retryOnConnectionFailure(true)
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .build()
            }
            .crossfade(true)
            .build()
    }
}
