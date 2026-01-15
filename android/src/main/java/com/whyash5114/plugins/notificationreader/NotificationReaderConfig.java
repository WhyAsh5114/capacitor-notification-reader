package com.whyash5114.plugins.notificationreader;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Configuration manager for NotificationReader plugin.
 * Handles reading and writing configuration options like logProgressNotifications and storageLimit.
 */
public class NotificationReaderConfig {

    private static final String PREFS_NAME = "NotificationReaderConfig";
    private static final String PREF_LOG_PROGRESS_NOTIFICATIONS = "log_progress_notifications";
    private static final String PREF_STORAGE_LIMIT = "storage_limit";

    // Default values
    private static final boolean DEFAULT_LOG_PROGRESS_NOTIFICATIONS = true;
    private static final float DEFAULT_STORAGE_LIMIT = -1f; // -1 means unlimited

    private final SharedPreferences prefs;

    public NotificationReaderConfig(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Gets whether progress notifications should be logged.
     * @return true if progress notifications should be logged (default), false otherwise
     */
    public boolean shouldLogProgressNotifications() {
        return prefs.getBoolean(PREF_LOG_PROGRESS_NOTIFICATIONS, DEFAULT_LOG_PROGRESS_NOTIFICATIONS);
    }

    /**
     * Sets whether progress notifications should be logged.
     * @param enabled true to log progress notifications, false to filter them out
     */
    public void setLogProgressNotifications(boolean enabled) {
        prefs.edit().putBoolean(PREF_LOG_PROGRESS_NOTIFICATIONS, enabled).apply();
    }

    /**
     * Gets the storage limit in megabytes.
     * @return storage limit in MB, or -1 if unlimited (default)
     */
    public float getStorageLimit() {
        return prefs.getFloat(PREF_STORAGE_LIMIT, DEFAULT_STORAGE_LIMIT);
    }

    /**
     * Sets the storage limit in megabytes.
     * @param limitMB storage limit in MB, or -1 for unlimited
     */
    public void setStorageLimit(float limitMB) {
        prefs.edit().putFloat(PREF_STORAGE_LIMIT, limitMB).apply();
    }

    /**
     * Gets the storage limit in bytes.
     * @return storage limit in bytes, or -1 if unlimited
     */
    public long getStorageLimitBytes() {
        float limitMB = getStorageLimit();
        if (limitMB < 0) {
            return -1;
        }
        return (long) (limitMB * 1024 * 1024);
    }

    /**
     * Checks if a storage limit is set.
     * @return true if a storage limit is configured, false otherwise
     */
    public boolean hasStorageLimit() {
        return getStorageLimit() > 0;
    }
}
