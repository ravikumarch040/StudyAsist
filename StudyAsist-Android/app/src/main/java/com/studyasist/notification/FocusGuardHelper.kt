package com.studyasist.notification

import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import com.studyasist.data.repository.SettingsRepositoryEntryPoint
import dagger.hilt.android.EntryPointAccessors
import android.os.Build
import android.os.Process
import android.provider.Settings
import kotlinx.coroutines.runBlocking
import java.util.SortedMap
import java.util.TreeMap

/**
 * Package names we alert on during study time (social/media/games).
 * User is not blocked; we only show a notification.
 */
val FOCUS_GUARD_RESTRICTED_PACKAGES: Set<String> = setOf(
    "com.google.android.youtube",
    "com.google.android.apps.youtube.music",
    "com.instagram.android",
    "com.instagram.lite",
    "com.facebook.katana",
    "com.facebook.orca",
    "com.whatsapp",
    "com.snapchat.android",
    "com.zhiliaoapp.musically", // TikTok
    "com.ss.android.ugc.trill",
    "com.netflix.mediaclient",
    "com.spotify.music",
    "com.mobilelegends", // example game
    "com.supercell.clashofclans",
    "com.activision.callofduty.shooter",
    "com.epicgames.fortnite",
    "com.pubg.krmobile",
    "com.roblox.client",
    "com.miHoYo.Yuanshen", // Genshin
    "com.tencent.ig",
    "com.dts.freefireth",
    "com.king.candycrushsaga",
    "com.ea.gp.fifamobile"
)

/** Display names for notifications (package -> friendly name). */
fun getAppDisplayName(context: Context, packageName: String): String {
    return try {
        val pm = context.packageManager
        val info = pm.getApplicationInfo(packageName, 0)
        pm.getApplicationLabel(info).toString()
    } catch (_: Exception) {
        packageName
    }
}

/**
 * Returns whether the app has been granted Usage access (required for focus guard).
 */
fun hasUsageStatsPermission(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return false
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
    @Suppress("DEPRECATION")
    val mode = appOps.checkOpNoThrow(
        AppOpsManager.OPSTR_GET_USAGE_STATS,
        Process.myUid(),
        context.packageName
    )
    return mode == AppOpsManager.MODE_ALLOWED
}

/**
 * Opens the system screen where the user can grant Usage access to this app.
 */
fun openUsageAccessSettings(context: Context) {
    context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    })
}

/**
 * Gets the current foreground app package name, or null if unknown / no permission.
 * Uses UsageStatsManager with a short time window; the most recently used app is considered foreground.
 */
fun getForegroundPackage(context: Context): String? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return null
    if (!hasUsageStatsPermission(context)) return null
    val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
        ?: return null
    val now = System.currentTimeMillis()
    val stats = usageStatsManager.queryUsageStats(
        UsageStatsManager.INTERVAL_DAILY,
        now - 10_000L,
        now
    ) ?: return null
    if (stats.isEmpty()) return null
    val sorted: SortedMap<Long, UsageStats> = TreeMap()
    for (usageStats in stats) {
        sorted[usageStats.lastTimeUsed] = usageStats
    }
    return sorted[sorted.lastKey()]?.packageName
}

/**
 * Returns the effective set of restricted package names (built-in + user-added).
 * Use when checking if foreground app should trigger alert.
 */
fun getRestrictedPackages(context: Context): Set<String> = runBlocking {
    val entryPoint = EntryPointAccessors.fromApplication(
        context.applicationContext,
        SettingsRepositoryEntryPoint::class.java
    )
    entryPoint.getSettingsRepository().getEffectiveRestrictedPackages()
}
