package com.whyash5114.plugins.notificationreader;

import android.service.notification.StatusBarNotification;

import com.whyash5114.plugins.notificationreader.db.NotificationDatabase;
import com.whyash5114.plugins.notificationreader.db.NotificationEntity;
import com.whyash5114.plugins.notificationreader.parser.NotificationParser;

/**
 * Android NotificationListenerService implementation.
 * This service must be enabled by the user in system settings to receive notification events.
 * The service registers itself with NotificationServiceHolder for access from the plugin.
 */
public class NotificationListenerService extends android.service.notification.NotificationListenerService {

    /**
     * Called when the service is connected and ready to receive notification events.
     * Registers this service instance with the NotificationServiceHolder.
     */
    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        NotificationServiceHolder.setService(this);
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
        final NotificationEntity entity = NotificationParser.parse(getApplicationContext(), sbn);
        if (entity == null) {
            return;
        }

        // Run database operations on a background thread
        new Thread(() -> {
            NotificationDatabase.getDatabase(getApplicationContext()).notificationDao().insert(entity);
            // Notify the plugin
            NotificationReaderPlugin.onNotificationPosted(entity);
        }).start();
    }
}
