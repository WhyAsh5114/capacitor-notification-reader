package com.whyash5114.plugins.notificationreader;

import android.app.Notification;
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

    private NotificationReaderConfig config;

    /**
     * Called when the service is connected and ready to receive notification events.
     * Registers this service instance with the NotificationServiceHolder.
     */
    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        NotificationServiceHolder.setService(this);

        final Context context = getApplicationContext();
        config = new NotificationReaderConfig(context);
        final SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        if (!prefs.getBoolean(PREF_INITIAL_NOTIFICATIONS_PROCESSED, false)) {
            // Process existing notifications
            new Thread(() -> {
                for (StatusBarNotification sbn : getActiveNotifications()) {
                    if (shouldLogNotification(sbn)) {
                        final NotificationEntity entity = new NotificationEntity(context, sbn);
                        insertNotificationWithStorageCheck(context, entity);
                    }
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
        if (config == null) {
            config = new NotificationReaderConfig(getApplicationContext());
        }

        if (!shouldLogNotification(sbn)) {
            return;
        }

        final Context context = getApplicationContext();
        final NotificationEntity entity = new NotificationEntity(context, sbn);

        // Run database operations on a background thread
        new Thread(() -> {
            insertNotificationWithStorageCheck(context, entity);
            // Notify the plugin
            NotificationReaderPlugin.onNotificationPosted(entity);
        })
            .start();
    }

    /**
     * Checks if a notification should be logged based on the configuration filters.
     * @param sbn StatusBarNotification to check
     * @return true if the notification should be logged, false otherwise
     */
    private boolean shouldLogNotification(StatusBarNotification sbn) {
        if (config == null) {
            config = new NotificationReaderConfig(getApplicationContext());
        }

        Notification notification = sbn.getNotification();

        // Filter out ongoing notifications if configured
        if (config.shouldFilterOngoing() && notification != null && (notification.flags & Notification.FLAG_ONGOING_EVENT) != 0) {
            return false;
        }

        // Filter out transport category notifications if configured
        if (config.shouldFilterTransport() && notification != null && Notification.CATEGORY_TRANSPORT.equals(notification.category)) {
            return false;
        }

        return true;
    }

    /**
     * Inserts a notification into the database, enforcing storage limits if configured.
     * @param context Application context
     * @param entity NotificationEntity to insert
     */
    private void insertNotificationWithStorageCheck(Context context, NotificationEntity entity) {
        NotificationDatabase db = NotificationDatabase.getDatabase(context);
        
        // Insert the notification
        db.notificationDao().insert(entity);

        // Check storage limit if configured
        if (config.hasStorageLimit()) {
            Long currentSizeBytes = db.notificationDao().getDatabaseSizeBytes();
            long limitBytes = config.getStorageLimitBytes();

            if (currentSizeBytes != null && currentSizeBytes > limitBytes) {
                // Delete oldest notifications until we're under the limit
                // We'll delete in batches of 10 to avoid too many database operations
                while (currentSizeBytes > limitBytes) {
                    db.notificationDao().deleteOldestNotifications(10);
                    currentSizeBytes = db.notificationDao().getDatabaseSizeBytes();
                    
                    // Safety check to avoid infinite loop
                    if (currentSizeBytes == null || db.notificationDao().getTotalCount() == 0) {
                        break;
                    }
                }
            }
        }
    }
}
