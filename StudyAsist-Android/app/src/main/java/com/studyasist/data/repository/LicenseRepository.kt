package com.studyasist.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages premium/freemium status. Currently always returns premium (free).
 * Will be wired to Google Play Billing when ready to monetize.
 */
@Singleton
class LicenseRepository @Inject constructor() {

    val isPremiumFlow: Flow<Boolean> = flowOf(true)

    fun isPremium(): Boolean = true

    fun maxTimetables(): Int = if (isPremium()) Int.MAX_VALUE else 1
    fun maxGoals(): Int = if (isPremium()) Int.MAX_VALUE else 1
    fun maxQAScansPerMonth(): Int = if (isPremium()) Int.MAX_VALUE else 50
    fun availableThemeCount(): Int = if (isPremium()) Int.MAX_VALUE else 4
    fun isCloudSyncEnabled(): Boolean = isPremium()
    fun isAiTutorEnabled(): Boolean = isPremium()
    fun isAdvancedAnalyticsEnabled(): Boolean = isPremium()
}
