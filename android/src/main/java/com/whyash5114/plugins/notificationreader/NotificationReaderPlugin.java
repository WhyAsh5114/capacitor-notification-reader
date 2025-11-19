package com.whyash5114.plugins.notificationreader;

import android.content.Intent;
import android.provider.Settings;
import android.service.notification.StatusBarNotification;

import androidx.activity.result.ActivityResult;
import androidx.sqlite.db.SimpleSQLiteQuery;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.ActivityCallback;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.whyash5114.plugins.notificationreader.db.NotificationDatabase;
import com.whyash5114.plugins.notificationreader.db.NotificationEntity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
     */
    @SuppressWarnings("unused")
    @PluginMethod
    public void openAccessSettings(PluginCall call) {
        Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
        startActivityForResult(call, intent, "settingsResult");
    }

    @SuppressWarnings("unused")
    @ActivityCallback
    protected void settingsResult(PluginCall call, ActivityResult result) {
        JSObject ret = new JSObject();
        ret.put("enabled", isNotificationAccessEnabled());
        call.resolve(ret);
    }

    /**
     * Checks if the app has been granted notification listener access.
     */
    @SuppressWarnings("unused")
    @PluginMethod
    public void isAccessEnabled(PluginCall call) {
        JSObject ret = new JSObject();
        ret.put("enabled", isNotificationAccessEnabled());
        call.resolve(ret);
    }

    /**
     * Retrieves all currently active notifications from the notification drawer.
     * Requires notification listener permission to be granted.
     */
    @SuppressWarnings("unused")
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
            arr.put(notificationEntityToJSObject(entity));
        }

        JSObject ret = new JSObject();
        ret.put("notifications", arr);

        call.resolve(ret);
    }

    @SuppressWarnings("unused")
    @PluginMethod
    public void getNotifications(PluginCall call) {
        JSObject rawFilter = call.getObject("filter");
        JSONObject filterCopy = cloneJSONObject(rawFilter);
        JSONObject rootOptions = cloneJSONObject(call.getData());
        Long cursor = call.getLong("cursor");
        Integer limit = call.getInt("limit", 10);
        int safeLimit = limit != null && limit > 0 ? limit : 10;

        new Thread(() -> {
            try {
                StringBuilder queryBuilder = new StringBuilder("SELECT * FROM notifications");
                List<Object> args = new ArrayList<>();
                List<String> conditions = new ArrayList<>();

                String textContains = getStringOption(filterCopy, rootOptions, "textContains");
                if (textContains != null && !textContains.isEmpty()) {
                    conditions.add("text LIKE ?");
                    args.add("%" + textContains + "%");
                }

                String titleContains = getStringOption(filterCopy, rootOptions, "titleContains");
                if (titleContains != null && !titleContains.isEmpty()) {
                    conditions.add("title LIKE ?");
                    args.add("%" + titleContains + "%");
                }

                String textContainsInsensitive = getStringOption(filterCopy, rootOptions, "textContainsInsensitive");
                if (textContainsInsensitive != null && !textContainsInsensitive.isEmpty()) {
                    conditions.add("LOWER(text) LIKE LOWER(?)");
                    args.add("%" + textContainsInsensitive + "%");
                }

                String titleContainsInsensitive = getStringOption(filterCopy, rootOptions, "titleContainsInsensitive");
                if (titleContainsInsensitive != null && !titleContainsInsensitive.isEmpty()) {
                    conditions.add("LOWER(title) LIKE LOWER(?)");
                    args.add("%" + titleContainsInsensitive + "%");
                }

                JSONArray appNames = getJSONArrayOption(filterCopy, rootOptions, "appNames");
                if (appNames != null && appNames.length() > 0) {
                    StringBuilder appNameCondition = new StringBuilder("appName IN (");
                    for (int i = 0; i < appNames.length(); i++) {
                        appNameCondition.append(i == 0 ? "?" : ", ?");
                        args.add(appNames.getString(i));
                    }
                    appNameCondition.append(")");
                    conditions.add(appNameCondition.toString());
                }

                String packageNameFilter = getStringOption(filterCopy, rootOptions, "packageName");
                if (packageNameFilter != null && !packageNameFilter.isEmpty()) {
                    conditions.add("packageName = ?");
                    args.add(packageNameFilter);
                }

                String categoryFilter = getStringOption(filterCopy, rootOptions, "category");
                if (categoryFilter != null && !categoryFilter.isEmpty()) {
                    conditions.add("category = ?");
                    args.add(categoryFilter);
                }

                String styleFilter = getStringOption(filterCopy, rootOptions, "style");
                if (styleFilter != null && !styleFilter.isEmpty()) {
                    conditions.add("style = ?");
                    args.add(styleFilter);
                }

                Boolean isOngoingFilter = getBooleanOption(filterCopy, rootOptions, "isOngoing");
                if (isOngoingFilter != null) {
                    conditions.add("isOngoing = ?");
                    args.add(isOngoingFilter ? 1 : 0);
                }

                Boolean isGroupSummaryFilter = getBooleanOption(filterCopy, rootOptions, "isGroupSummary");
                if (isGroupSummaryFilter != null) {
                    conditions.add("isGroupSummary = ?");
                    args.add(isGroupSummaryFilter ? 1 : 0);
                }

                String channelIdFilter = getStringOption(filterCopy, rootOptions, "channelId");
                if (channelIdFilter != null && !channelIdFilter.isEmpty()) {
                    conditions.add("channelId = ?");
                    args.add(channelIdFilter);
                }

                if (cursor != null && cursor > 0) {
                    conditions.add("postTime < ?");
                    args.add(cursor);
                }

                if (!conditions.isEmpty()) {
                    queryBuilder.append(" WHERE ").append(String.join(" AND ", conditions));
                }

                queryBuilder.append(" ORDER BY postTime DESC LIMIT ?");
                args.add(safeLimit);

                SimpleSQLiteQuery query = new SimpleSQLiteQuery(queryBuilder.toString(), args.toArray());
                List<NotificationEntity> entities = NotificationDatabase.getDatabase(getContext()).notificationDao().getNotifications(query);

                JSArray notificationArray = new JSArray();
                for (NotificationEntity entity : entities) {
                    notificationArray.put(notificationEntityToJSObject(entity));
                }
                JSObject result = new JSObject();
                result.put("notifications", notificationArray);
                call.resolve(result);
            } catch (JSONException e) {
                call.reject("Invalid filter options", e);
            }
        }).start();
    }

    @SuppressWarnings("unused")
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
     * <p>
     * Each notification will be inserted using REPLACE strategy, meaning if a
     * notification with the same ID already exists, it will be updated.
     *
     * @param call PluginCall containing the array of notifications to import
     *             Expected parameter: "notifications" - JSArray of notification objects
     */
    @SuppressWarnings("unused")
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
            obj.put("actions", new JSArray(entity.actionsJson));
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
     */
    private NotificationEntity jsObjectToNotificationEntity(JSObject obj) {
        NotificationEntity entity = new NotificationEntity();

        entity.id = obj.getString("id", UUID.randomUUID().toString());
        entity.appName = obj.getString("appName", "");
        entity.packageName = obj.getString("packageName", "");
        entity.title = obj.getString("title", "");
        entity.text = obj.getString("text", "");
        entity.postTime = obj.optLong("timestamp", 0L);
        entity.smallIcon = obj.getString("smallIcon", "");
        entity.largeIcon = obj.getString("largeIcon", "");
        entity.appIcon = obj.getString("appIcon", "");
        entity.category = obj.getString("category", "");
        entity.style = obj.getString("style", "default");
        entity.subText = obj.getString("subText", "");
        entity.infoText = obj.getString("infoText", "");
        entity.summaryText = obj.getString("summaryText", "");
        entity.groupKey = obj.getString("group", "");
        entity.isGroupSummary = obj.getBoolean("isGroupSummary", false);
        entity.channelId = obj.getString("channelId", "");

        JSONArray actions = obj.optJSONArray("actions");
        if (actions != null) {
            entity.actionsJson = actions.toString();
        } else {
            entity.actionsJson = "[]";
        }

        entity.isOngoing = obj.getBoolean("isOngoing", false);
        entity.autoCancel = obj.getBoolean("autoCancel", false);
        entity.isLocalOnly = obj.getBoolean("isLocalOnly", false);
        entity.priority = obj.getInteger("priority", 0);
        entity.number = obj.getInteger("number", 0);

        // Style-specific fields
        entity.bigText = obj.getString("bigText", "");
        entity.bigPicture = obj.getString("bigPicture", "");
        entity.pictureContentDescription = obj.getString("pictureContentDescription", "");

        JSONArray inboxLines = obj.optJSONArray("inboxLines");
        if (inboxLines != null) {
            entity.inboxLinesJson = inboxLines.toString();
        } else {
            entity.inboxLinesJson = null;
        }

        entity.conversationTitle = obj.getString("conversationTitle", "");
        entity.isGroupConversation = obj.getBoolean("isGroupConversation", false);

        JSONArray messages = obj.optJSONArray("messages");
        if (messages != null) {
            entity.messagesJson = messages.toString();
        } else {
            entity.messagesJson = null;
        }

        JSObject progressObj = obj.getJSObject("progress");
        if (progressObj != null) {
            entity.progress = progressObj.getInteger("current", 0);
            entity.progressMax = progressObj.getInteger("max", 0);
            entity.progressIndeterminate = progressObj.getBoolean("indeterminate", false);
        }

        entity.callerName = obj.getString("callerName", "");

        return entity;
    }

    private boolean isNotificationAccessEnabled() {
        String enabled = Settings.Secure.getString(getContext().getContentResolver(), "enabled_notification_listeners");
        return enabled != null && enabled.contains(getContext().getPackageName());
    }

    private JSONObject cloneJSONObject(JSONObject source) {
        if (source == null) {
            return null;
        }
        try {
            return new JSONObject(source.toString());
        } catch (JSONException e) {
            return null;
        }
    }

    private boolean hasKey(JSONObject obj, String key) {
        return obj != null && obj.has(key) && !obj.isNull(key);
    }

    private String getStringOption(JSONObject primary, JSONObject fallback, String key) {
        if (hasKey(primary, key)) {
            return primary.optString(key, null);
        }
        if (hasKey(fallback, key)) {
            return fallback.optString(key, null);
        }
        return null;
    }

    private JSONArray getJSONArrayOption(JSONObject primary, JSONObject fallback, String key) {
        JSONArray primaryArray = primary != null ? primary.optJSONArray(key) : null;
        if (primaryArray != null) {
            return primaryArray;
        }
        return fallback != null ? fallback.optJSONArray(key) : null;
    }

    private Boolean getBooleanOption(JSONObject primary, JSONObject fallback, String key) {
        if (hasKey(primary, key)) {
            return primary.optBoolean(key);
        }
        if (hasKey(fallback, key)) {
            return fallback.optBoolean(key);
        }
        return null;
    }
}
