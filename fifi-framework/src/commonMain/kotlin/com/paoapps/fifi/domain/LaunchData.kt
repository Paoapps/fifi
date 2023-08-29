package com.paoapps.fifi.domain

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class LaunchData(
    val isFirstLaunch: Boolean = true,
    val previousAppVersion: String? = null,
    val currentAppVersion: String
//    val wantsPushNotifications: Boolean? = null,
//    val fcmToken: String? = null,
//    val registeredToken: PushNotificationsToken? = null,
//    val hasAskedForFirstName: Boolean? = null,
//    val firstTimeWeatherSettingsEnabled: Boolean? = null,
) {
    @Serializable
    data class Instants(
        val firstLaunch: Instant = Clock.System.now(),
        val registration: Instant? = null,
        val subscribe: Instant? = null,
        val booked: Instant? = null
    ) {

        // for backwards compatibility on devices on whcih we missed the events or when users login with an account with existing subscriptions, bookings etc
        val registrationOrFirstLaunch: Instant get() = registration ?: firstLaunch
        val subscribeOrFirstLaunch: Instant get() = subscribe ?: firstLaunch
        val bookedOrFirstLaunch: Instant get() = booked ?: firstLaunch
    }

}
