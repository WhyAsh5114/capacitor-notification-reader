package com.whyash5114.plugins.notificationreader;

import android.app.Notification;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.service.notification.StatusBarNotification;
import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import java.util.Map;

/**
 * Capacitor plugin for reading Android notifications.
 * Provides methods to check permission status, request permission, and read active notifications.
 */
@CapacitorPlugin(name = "NotificationReader")
public class NotificationReaderPlugin extends Plugin {

    private final NotificationReader implementation = new NotificationReader();
    private static final int REQUEST_CODE_SETTINGS = 1001;

    /**
     * Opens the Android system settings page for notification listener access.
     * Users must manually enable notification access for the app from this settings page.
     * The method waits for the user to return and checks if permission was granted.
     *
     * @return Object with "enabled" boolean indicating whether permission was granted
     */
    @PluginMethod
    public void openAccessSettings(PluginCall call) {
        try {
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            startActivityForResult(call, intent, REQUEST_CODE_SETTINGS);
        } catch (Exception e) {
            call.reject("Failed to open settings: " + e.getMessage());
        }
    }

    /**
     * Checks if the app has been granted notification listener access.
     *
     * @return Object with "enabled" boolean indicating permission status
     */
    @PluginMethod
    public void isAccessEnabled(PluginCall call) {
        String enabled = Settings.Secure.getString(getContext().getContentResolver(), "enabled_notification_listeners");

        boolean isEnabled = enabled != null && enabled.contains(getContext().getPackageName());

        JSObject ret = new JSObject();
        ret.put("enabled", isEnabled);
        call.resolve(ret);
    }

    /**
     * Retrieves all currently active notifications from the notification drawer.
     * Requires notification listener permission to be granted.
     *
     * @return Object containing array of notification items
     */
    @PluginMethod
    public void getActiveNotifications(PluginCall call) {
        NotificationListenerService service = NotificationServiceHolder.getService();

        if (service == null) {
            call.reject("Notification listener service not connected");
            return;
        }

        StatusBarNotification[] notifs = service.getActiveNotifications();
        JSArray arr = new JSArray();

        for (StatusBarNotification sbn : notifs) {
            Notification notification = sbn.getNotification();
            Bundle extras = notification.extras;

            JSObject obj = new JSObject();
            obj.put("app", sbn.getPackageName());
            obj.put("title", extras.getString(Notification.EXTRA_TITLE));
            obj.put("text", extras.getCharSequence(Notification.EXTRA_TEXT));
            obj.put("timestamp", sbn.getPostTime());

            arr.put(obj);
        }

        JSObject ret = new JSObject();
        ret.put("notifications", arr);

        call.resolve(ret);
    }

    /**
     * Handles the result from the notification listener settings activity.
     * Checks the permission status when the user returns from settings.
     */
    @Override
    protected void handleOnActivityResult(int requestCode, int resultCode, Intent data) {
        super.handleOnActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SETTINGS) {
            PluginCall savedCall = getSavedCall();
            if (savedCall == null) {
                return;
            }

            // Check if permission was granted
            String enabled = Settings.Secure.getString(
                getContext().getContentResolver(),
                "enabled_notification_listeners"
            );
            boolean isEnabled = enabled != null && enabled.contains(getContext().getPackageName());

            JSObject ret = new JSObject();
            ret.put("enabled", isEnabled);
            savedCall.resolve(ret);
        }
    }
}
