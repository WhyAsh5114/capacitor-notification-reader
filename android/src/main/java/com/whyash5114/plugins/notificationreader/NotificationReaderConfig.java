package com.whyash5114.plugins.notificationreader;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Configuration manager for NotificationReader plugin.
 * Handles reading and writing configuration options like filterOngoing, filterTransport and storageLimit.
 */
public class NotificationReaderConfig {

    private static final String PREFS_NAME = "NotificationReaderConfig";
    private static final String PREF_FILTER_ONGOING = "filter_ongoing";
    private static final String PREF_FILTER_TRANSPORT = "filter_transport";
    private static final String PREF_STORAGE_LIMIT = "storage_limit";

    // Default values
    private static final boolean DEFAULT_FILTER_ONGOING = true; // Filter out ongoing notifications by default
    private static final boolean DEFAULT_FILTER_TRANSPORT = true; // Filter out transport category by default
    private static final float DEFAULT_STORAGE_LIMIT = -1f; // -1 means unlimited

    private final SharedPreferences prefs;

    public NotificationReaderConfig(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Gets whether ongoing (non-dismissible) notifications should be filtered out.
     * @return true if ongoing notifications should be filtered out (default), false otherwise
     */
    public boolean shouldFilterOngoing() {
        return prefs.getBoolean(PREF_FILTER_ONGOING, DEFAULT_FILTER_ONGOING);
    }

    /**
     * Sets whether ongoing (non-dismissible) notifications should be filtered out.
     * @param enabled true to filter out ongoing notifications, false to log them
     */
    public void setFilterOngoing(boolean enabled) {
        prefs.edit().putBoolean(PREF_FILTER_ONGOING, enabled).apply();
    }

    /**
     * Gets whether transport category notifications should be filtered out.
     * @return true if transport notifications should be filtered out (default), false otherwise
     */
    public boolean shouldFilterTransport() {
        return prefs.getBoolean(PREF_FILTER_TRANSPORT, DEFAULT_FILTER_TRANSPORT);
    }

    /**
     * Sets whether transport category notifications should be filtered out.
     * @param enabled true to filter out transport notifications, false to log them
     */
    public void setFilterTransport(boolean enabled) {
        prefs.edit().putBoolean(PREF_FILTER_TRANSPORT, enabled).apply();
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
