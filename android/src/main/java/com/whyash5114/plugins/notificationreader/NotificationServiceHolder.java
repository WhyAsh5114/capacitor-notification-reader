package com.whyash5114.plugins.notificationreader;

/**
 * Holds a reference to the NotificationListenerService instance.
 * This allows the plugin to access the service from outside the service lifecycle.
 */
public class NotificationServiceHolder {

    private static NotificationListenerService service;

    /**
     * Sets the current service instance.
     * Called by NotificationListenerService when connected/disconnected.
     *
     * @param s The service instance, or null to clear
     */
    public static void setService(NotificationListenerService s) {
        service = s;
    }

    /**
     * Gets the current service instance.
     *
     * @return The service instance, or null if not connected
     */
    public static NotificationListenerService getService() {
        return service;
    }
}
