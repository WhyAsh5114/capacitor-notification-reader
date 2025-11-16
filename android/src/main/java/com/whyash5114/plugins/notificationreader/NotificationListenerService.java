package com.whyash5114.plugins.notificationreader;

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
}
