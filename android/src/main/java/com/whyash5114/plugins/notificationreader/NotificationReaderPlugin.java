
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
            NotificationEntity entity = new NotificationEntity(getContext(), sbn);
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
        Long cursor = call.getLong("cursor");
        Integer limit = call.getInt("limit", 10);

        new Thread(() -> {
            List<NotificationEntity> entities;
            if (cursor != null) {
                entities = NotificationDatabase.getDatabase(getContext()).notificationDao().getNotifications(cursor, limit);
            } else {
                entities = NotificationDatabase.getDatabase(getContext()).notificationDao().getNotifications(limit);
            }
            JSArray notificationArray = new JSArray();
            for (NotificationEntity entity : entities) {
                notificationArray.put(notificationEntityToJSObject(entity));
            }
            JSObject result = new JSObject();
            result.put("notifications", notificationArray);
            call.resolve(result);
        }).start();
    }

    @PluginMethod
    public void deleteAllNotifications(PluginCall call) {
        new Thread(() -> {
            NotificationDatabase.getDatabase(getContext()).notificationDao().deleteAllNotifications();
            call.resolve();
        }).start();
    }

    /**
     * Imports an array of notifications into the database.
     * This method is useful for restoring previously exported notifications,
     * migrating data from another source, or bulk-importing notification data.
     * 
     * Each notification will be inserted using REPLACE strategy, meaning if a
     * notification with the same ID already exists, it will be updated.
     *
     * @param call PluginCall containing the array of notifications to import
     *             Expected parameter: "notifications" - JSArray of notification objects
     * @throws Exception if the notifications array is missing or if an error occurs during import
     */
    @PluginMethod
    public void importNotifications(PluginCall call) {
        JSArray notifications = call.getArray("notifications");
        if (notifications == null) {
            call.reject("Missing 'notifications' argument");
            return;
        }

        new Thread(() -> {
            try {
                for (Object item : notifications.toList()) {
                    if (item instanceof JSObject) {
                        NotificationEntity entity = jsObjectToNotificationEntity((JSObject) item);
                        NotificationDatabase.getDatabase(getContext()).notificationDao().insert(entity);
                    }
                }
                call.resolve();
            } catch (Exception e) {
                call.reject("Error importing notifications", e);
            }
        }).start();
    }

    public JSObject notificationEntityToJSObject(NotificationEntity entity) {
        JSObject obj = new JSObject();
        obj.put("id", entity.id);
        obj.put("appName", entity.appName);
        obj.put("packageName", entity.packageName);
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

    /**
     * Converts a JSObject (from JavaScript) to a NotificationEntity (for database storage).
     * This method is used when importing notifications to map the JavaScript notification
     * object structure to the Room database entity structure.
     *
     * @param obj JSObject containing notification data from JavaScript
     * @return NotificationEntity populated with data from the JSObject
     * @throws JSONException if there's an error accessing JSObject properties
     */
    private NotificationEntity jsObjectToNotificationEntity(JSObject obj) throws JSONException {
        NotificationEntity entity = new NotificationEntity();
        entity.id = obj.getString("id");
        entity.appName = obj.getString("appName");
        entity.packageName = obj.getString("packageName");
        entity.title = obj.getString("title");
        entity.text = obj.getString("text");
        entity.postTime = obj.getLong("timestamp");
        entity.smallIcon = obj.getString("smallIcon");
        entity.largeIcon = obj.getString("largeIcon");
        entity.appIcon = obj.getString("appIcon");
        entity.category = obj.getString("category");
        entity.style = obj.getString("style");
        entity.subText = obj.getString("subText");
        entity.infoText = obj.getString("infoText");
        entity.summaryText = obj.getString("summaryText");
        entity.groupKey = obj.getString("group");
        entity.isGroupSummary = obj.getBoolean("isGroupSummary");
        entity.channelId = obj.getString("channelId");
        try {
            Object actionsObj = obj.get("actions");
            if (actionsObj instanceof JSArray) {
                entity.actionsJson = ((JSArray) actionsObj).toString();
            } else if (actionsObj != null) {
                entity.actionsJson = new JSArray(actionsObj.toString()).toString();
            } else {
                entity.actionsJson = new JSArray().toString();
            }
        } catch (Exception e) {
            entity.actionsJson = new JSArray().toString();
        }
        entity.isOngoing = obj.getBoolean("isOngoing");
        entity.autoCancel = obj.getBoolean("autoCancel");
        entity.isLocalOnly = obj.getBoolean("isLocalOnly");
        entity.priority = obj.getInteger("priority");
        entity.number = obj.getInteger("number");

        // Style-specific fields
        entity.bigText = obj.getString("bigText");
        entity.bigPicture = obj.getString("bigPicture");
        entity.pictureContentDescription = obj.getString("pictureContentDescription");
        try {
            Object inboxLinesObj = obj.get("inboxLines");
            if (inboxLinesObj instanceof JSArray) {
                entity.inboxLinesJson = ((JSArray) inboxLinesObj).toString();
            } else if (inboxLinesObj != null) {
                entity.inboxLinesJson = new JSArray(inboxLinesObj.toString()).toString();
            } else {
                entity.inboxLinesJson = new JSArray().toString();
            }
        } catch (Exception e) {
            entity.inboxLinesJson = new JSArray().toString();
        }
        entity.conversationTitle = obj.getString("conversationTitle");
        entity.isGroupConversation = obj.getBoolean("isGroupConversation");
        try {
            Object messagesObj = obj.get("messages");
            if (messagesObj instanceof JSArray) {
                entity.messagesJson = ((JSArray) messagesObj).toString();
            } else if (messagesObj != null) {
                entity.messagesJson = new JSArray(messagesObj.toString()).toString();
            } else {
                entity.messagesJson = new JSArray().toString();
            }
        } catch (Exception e) {
            entity.messagesJson = new JSArray().toString();
        }

        JSObject progressObj = obj.getJSObject("progress");
        if (progressObj != null) {
            entity.progress = progressObj.getInteger("current");
            entity.progressMax = progressObj.getInteger("max");
            entity.progressIndeterminate = progressObj.getBoolean("indeterminate");
        }

        entity.callerName = obj.getString("callerName");

        return entity;
    }

    private boolean isNotificationAccessEnabled() {
        String enabled = Settings.Secure.getString(getContext().getContentResolver(), "enabled_notification_listeners");
        return enabled != null && enabled.contains(getContext().getPackageName());
    }
}
