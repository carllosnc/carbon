package com.carbon.launcher.data

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NotificationBadgeService : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        activeNotifications.forEach { onNotificationPosted(it) }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.isOngoing) return
        val map = badgeNotifications.value.toMutableMap()
        val extras = sbn.notification.extras
        val title = extras.getString(Notification.EXTRA_TITLE)
        val text = extras.getString(Notification.EXTRA_TEXT)
        map[sbn.packageName] = if (!title.isNullOrBlank()) title else text ?: ""
        badgeNotifications.value = map
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        val map = activeNotifications
            .filter { !it.isOngoing }
            .associate { sbn ->
                val extras = sbn.notification.extras
                val title = extras.getString(Notification.EXTRA_TITLE)
                val text = extras.getString(Notification.EXTRA_TEXT)
                sbn.packageName to (if (!title.isNullOrBlank()) title else text ?: "")
            }
        badgeNotifications.value = map
    }

    companion object {
        private val badgeNotifications = MutableStateFlow<Map<String, String>>(emptyMap())
        val badgeNotificationsFlow: StateFlow<Map<String, String>> = badgeNotifications.asStateFlow()
    }
}
