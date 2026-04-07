# ─── Stack traces lisibles ───────────────────────────────────────────────────
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ─── Google Play Billing ─────────────────────────────────────────────────────
-keep class com.android.billingclient.** { *; }

# ─── Supabase / Ktor / kotlinx.serialization ─────────────────────────────────
-keep class io.github.jan.supabase.** { *; }
-keep class io.ktor.** { *; }
-keepattributes *Annotation*, Signature, Exception
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}
-keep @kotlinx.serialization.Serializable class * { *; }
-keepclassmembers @kotlinx.serialization.Serializable class * {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}

# ─── Classes JVM absentes sur Android (Ktor) ─────────────────────────────────
-dontwarn java.lang.management.ManagementFactory
-dontwarn java.lang.management.RuntimeMXBean

# ─── Kotlin coroutines ───────────────────────────────────────────────────────
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# ─── Coil (chargement d'images) ──────────────────────────────────────────────
-keep class coil.** { *; }

# ─── Lottie ──────────────────────────────────────────────────────────────────
-keep class com.airbnb.lottie.** { *; }

# ─── Google Sign-In / Credential Manager ─────────────────────────────────────
-keep class com.google.android.gms.** { *; }
-keep class androidx.credentials.** { *; }

# ─── Models Supabase (sérialisés) ────────────────────────────────────────────
-keep class com.valentincolombat.upnews.data.model.** { *; }
-keep class com.valentincolombat.upnews.data.billing.BillingManager$** { *; }
