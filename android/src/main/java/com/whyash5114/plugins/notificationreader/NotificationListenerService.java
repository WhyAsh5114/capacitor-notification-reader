package com.whyash5114.plugins.notificationreader;

import android.content.Context;
import android.content.SharedPreferences;
import android.service.notification.StatusBarNotification;
import com.whyash5114.plugins.notificationreader.db.NotificationDatabase;
import com.whyash5114.plugins.notificationreader.db.NotificationEntity;

/**
 * Android NotificationListenerService implementation.
 * This service must be enabled by the user in system settings to receive notification events.
 * The service registers itself with NotificationServiceHolder for access from the plugin.
 */
public class NotificationListenerService extends android.service.notification.NotificationListenerService {

    private static final String PREFS_NAME = "NotificationReader";
    private static final String PREF_INITIAL_NOTIFICATIONS_PROCESSED = "initial_notifications_processed";

    /**
     * Called when the service is connected and ready to receive notification events.
     * Registers this service instance with the NotificationServiceHolder.
     */
    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        NotificationServiceHolder.setService(this);

        final Context context = getApplicationContext();
        final SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        if (!prefs.getBoolean(PREF_INITIAL_NOTIFICATIONS_PROCESSED, false)) {
            // Process existing notifications
            new Thread(() -> {
                for (StatusBarNotification sbn : getActiveNotifications()) {
                    final NotificationEntity entity = new NotificationEntity(context, sbn);
                    NotificationDatabase.getDatabase(context).notificationDao().insert(entity);
                }
                // Mark as processed
                prefs.edit().putBoolean(PREF_INITIAL_NOTIFICATIONS_PROCESSED, true).apply();
            })
                .start();
        }
    }

    /**
     * Called when the service is disconnected.
     * Clears the service instance from NotificationServiceHolder.
     */
    @Override
    public void onListenerDisconnected() {
        NotificationServiceHolder.setService(null);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        final Context context = getApplicationContext();
        final NotificationEntity entity = new NotificationEntity(context, sbn);

        // Run database operations on a background thread
        new Thread(() -> {
            NotificationDatabase.getDatabase(context).notificationDao().insert(entity);
            // Notify the plugin
            NotificationReaderPlugin.onNotificationPosted(entity);
        })
            .start();
    }
}
