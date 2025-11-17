
package com.whyash5114.plugins.notificationreader;

import android.content.Intent;
import android.provider.Settings;
import android.service.notification.StatusBarNotification;

import androidx.activity.result.ActivityResult;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.ActivityCallback;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.whyash5114.plugins.notificationreader.db.NotificationDatabase;
import com.whyash5114.plugins.notificationreader.db.NotificationEntity;

import org.json.JSONException;

import java.util.List;

/**
 * Capacitor plugin for reading Android notifications.
 * Provides methods to check permission status, request permission, and read active notifications.
 */
@CapacitorPlugin(name = "NotificationReader")
public class NotificationReaderPlugin extends Plugin {

    private static NotificationReaderPlugin instance;

    @Override
    public void load() {
        super.load();
        instance = this;
    }

    public static void onNotificationPosted(NotificationEntity entity) {
        if (instance != null) {
            JSObject notificationData = instance.notificationEntityToJSObject(entity);
            instance.notifyListeners("notificationPosted", notificationData);
        }
    }


    /**
     * Opens the Android system settings page for notification listener access.
     * Users must manually enable notification access for the app from this settings page.
     * The method waits for the user to return and checks if permission was granted.
     *
     * @return Object with "enabled" boolean indicating whether permission was granted
     */
    @PluginMethod
    public void openAccessSettings(PluginCall call) {
        Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
        startActivityForResult(call, intent, "settingsResult");
    }

    @ActivityCallback
    protected void settingsResult(PluginCall call, ActivityResult result) {
        JSObject ret = new JSObject();
        ret.put("enabled", isNotificationAccessEnabled());
        call.resolve(ret);
    }

    /**
     * Checks if the app has been granted notification listener access.
     *
     * @return Object with "enabled" boolean indicating permission status
     */
    @PluginMethod
    public void isAccessEnabled(PluginCall call) {
        JSObject ret = new JSObject();
        ret.put("enabled", isNotificationAccessEnabled());
        call.resolve(ret);
    }

    /**
     * Retrieves all currently active notifications from the notification drawer.
     * Requires notification listener permission to be granted.
     *
     * @return Object containing array of notification items with comprehensive metadata
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
            NotificationEntity entity = com.whyash5114.plugins.notificationreader.parser.NotificationParser.parse(getContext(), sbn);
            if (entity != null) {
                arr.put(notificationEntityToJSObject(entity));
            }
        }

        JSObject ret = new JSObject();
        ret.put("notifications", arr);

        call.resolve(ret);
    }

    @PluginMethod
    public void getNotifications(PluginCall call) {
        Integer afterId = call.getInt("afterId", 0);
        Integer limit = call.getInt("limit", 10);

        new Thread(() -> {
            List<NotificationEntity> entities = NotificationDatabase.getDatabase(getContext()).notificationDao().getNotifications(afterId, limit);
            JSArray notificationArray = new JSArray();
            for (NotificationEntity entity : entities) {
                notificationArray.put(notificationEntityToJSObject(entity));
            }
            JSObject result = new JSObject();
            result.put("notifications", notificationArray);
            call.resolve(result);
        }).start();
    }

    public JSObject notificationEntityToJSObject(NotificationEntity entity) {
        JSObject obj = new JSObject();
        obj.put("id", entity.id);
        obj.put("app", entity.packageName);
        obj.put("title", entity.title);
        obj.put("text", entity.text);
        obj.put("timestamp", entity.postTime);
        obj.put("smallIcon", entity.smallIcon);
        obj.put("largeIcon", entity.largeIcon);
        obj.put("appIcon", entity.appIcon);
        obj.put("category", entity.category);
        obj.put("style", entity.style);
        obj.put("subText", entity.subText);
        obj.put("infoText", entity.infoText);
        obj.put("summaryText", entity.summaryText);
        obj.put("group", entity.groupKey);
        obj.put("isGroupSummary", entity.isGroupSummary);
        obj.put("channelId", entity.channelId);
        try {
            obj.put("actions", entity.actionsJson != null ? new JSArray(entity.actionsJson) : new JSArray());
        } catch (JSONException e) {
            obj.put("actions", new JSArray());
        }
        obj.put("isOngoing", entity.isOngoing);
        obj.put("autoCancel", entity.autoCancel);
        obj.put("isLocalOnly", entity.isLocalOnly);
        obj.put("priority", entity.priority);
        obj.put("number", entity.number);

        // Style-specific fields
        obj.put("bigText", entity.bigText);
        obj.put("bigPicture", entity.bigPicture);
        obj.put("pictureContentDescription", entity.pictureContentDescription);
        try {
            obj.put("inboxLines", entity.inboxLinesJson != null ? new JSArray(entity.inboxLinesJson) : new JSArray());
        } catch (JSONException e) {
            obj.put("inboxLines", new JSArray());
        }
        obj.put("conversationTitle", entity.conversationTitle);
        obj.put("isGroupConversation", entity.isGroupConversation);
        try {
            obj.put("messages", entity.messagesJson != null ? new JSArray(entity.messagesJson) : new JSArray());
        } catch (JSONException e) {
            obj.put("messages", new JSArray());
        }

        if (entity.progressMax > 0) {
            JSObject progressObj = new JSObject();
            progressObj.put("current", entity.progress);
            progressObj.put("max", entity.progressMax);
            progressObj.put("indeterminate", entity.progressIndeterminate);
            obj.put("progress", progressObj);
        }

        obj.put("callerName", entity.callerName);

        return obj;
    }

    private boolean isNotificationAccessEnabled() {
        String enabled = Settings.Secure.getString(getContext().getContentResolver(), "enabled_notification_listeners");
        return enabled != null && enabled.contains(getContext().getPackageName());
    }
}
