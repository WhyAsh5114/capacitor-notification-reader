package com.whyash5114.plugins.notificationreader;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.provider.Settings;
import android.service.notification.StatusBarNotification;
import android.util.Base64;
import androidx.activity.result.ActivityResult;
import androidx.sqlite.db.SimpleSQLiteQuery;
import java.io.ByteArrayOutputStream;
import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.ActivityCallback;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.whyash5114.plugins.notificationreader.db.NotificationDatabase;
import com.whyash5114.plugins.notificationreader.db.NotificationEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

                JSONArray appNames = getJSONArrayOption(filterCopy, rootOptions);
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

                Long afterTimestamp = getLongOption(filterCopy, rootOptions, "afterTimestamp");
                if (afterTimestamp != null && afterTimestamp > 0) {
                    conditions.add("postTime > ?");
                    args.add(afterTimestamp);
                }

                Long beforeTimestamp = getLongOption(filterCopy, rootOptions, "beforeTimestamp");
                if (beforeTimestamp != null && beforeTimestamp > 0) {
                    conditions.add("postTime < ?");
                    args.add(beforeTimestamp);
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
                List<NotificationEntity> entities = NotificationDatabase.getDatabase(getContext())
                    .notificationDao()
                    .getNotifications(query);

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
        })
            .start();
    }

    @SuppressWarnings("unused")
    @PluginMethod
    public void deleteAllNotifications(PluginCall call) {
        new Thread(() -> {
            NotificationDatabase.getDatabase(getContext()).notificationDao().deleteAllNotifications();
            call.resolve();
        })
            .start();
    }

    /**
     * Retrieves the total count of notifications stored in the database.
     * This count includes all notifications regardless of their status or type.
     *
     * @param call PluginCall to resolve with the count
     */
    @SuppressWarnings("unused")
    @PluginMethod
    public void getTotalCount(PluginCall call) {
        new Thread(() -> {
            int count = NotificationDatabase.getDatabase(getContext()).notificationDao().getTotalCount();
            JSObject result = new JSObject();
            result.put("count", count);
            call.resolve(result);
        })
            .start();
    }

    /**
     * Retrieves a list of all installed applications with their metadata.
     * Returns app name, package name, and base64-encoded app icon for each app.
     *
     * @param call PluginCall to resolve with the list of installed apps
     */
    @SuppressWarnings("unused")
    @PluginMethod
    public void getInstalledApps(PluginCall call) {
        new Thread(() -> {
            try {
                PackageManager pm = getContext().getPackageManager();
                List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);
                JSArray appsArray = new JSArray();

                for (ApplicationInfo appInfo : apps) {
                    JSObject appObj = new JSObject();
                    appObj.put("packageName", appInfo.packageName);
                    appObj.put("appName", pm.getApplicationLabel(appInfo).toString());

                    try {
                        Drawable icon = pm.getApplicationIcon(appInfo);
                        appObj.put("appIcon", drawableToBase64(icon));
                    } catch (Exception e) {
                        appObj.put("appIcon", null);
                    }

                    // Check if it's a system app
                    boolean isSystemApp = (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
                    appObj.put("isSystemApp", isSystemApp);

                    appsArray.put(appObj);
                }

                JSObject result = new JSObject();
                result.put("apps", appsArray);
                call.resolve(result);
            } catch (Exception e) {
                call.reject("Error getting installed apps", e);
            }
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
                int importedCount = 0;
                for (int i = 0; i < notifications.length(); i++) {
                    try {
                        JSONObject jsonObj = notifications.getJSONObject(i);
                        JSObject item = JSObject.fromJSONObject(jsonObj);
                        NotificationEntity entity = jsObjectToNotificationEntity(item);
                        NotificationDatabase.getDatabase(getContext()).notificationDao().insert(entity);
                        importedCount++;
                    } catch (JSONException e) {
                        // Log individual item errors but continue processing
                        android.util.Log.e("NotificationReader", "Error importing notification at index " + i, e);
                    }
                }
                JSObject result = new JSObject();
                result.put("imported", importedCount);
                call.resolve(result);
            } catch (Exception e) {
                call.reject("Error importing notifications", e);
            }
        })
            .start();
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

        entity.id = Objects.requireNonNull(obj.getString("id", UUID.randomUUID().toString()));
        entity.appName = Objects.requireNonNull(obj.getString("appName", ""));
        entity.packageName = Objects.requireNonNull(obj.getString("packageName", ""));
        entity.title = obj.getString("title", "");
        entity.text = obj.getString("text", "");
        entity.postTime = obj.optLong("timestamp", 0L);
        entity.smallIcon = obj.getString("smallIcon", "");
        entity.largeIcon = obj.getString("largeIcon", "");
        entity.appIcon = obj.getString("appIcon", "");
        entity.category = obj.getString("category", "");
        entity.style = Objects.requireNonNull(obj.getString("style", "default"));
        entity.subText = obj.getString("subText", "");
        entity.infoText = obj.getString("infoText", "");
        entity.summaryText = obj.getString("summaryText", "");
        entity.groupKey = obj.getString("group", "");
        entity.isGroupSummary = Boolean.TRUE.equals(obj.getBoolean("isGroupSummary", false));
        entity.channelId = obj.getString("channelId", "");

        JSONArray actions = obj.optJSONArray("actions");
        if (actions != null) {
            entity.actionsJson = actions.toString();
        } else {
            entity.actionsJson = "[]";
        }

        entity.isOngoing = Boolean.TRUE.equals(obj.getBoolean("isOngoing", false));
        entity.autoCancel = Boolean.TRUE.equals(obj.getBoolean("autoCancel", false));
        entity.isLocalOnly = Boolean.TRUE.equals(obj.getBoolean("isLocalOnly", false));
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
        entity.isGroupConversation = Boolean.TRUE.equals(obj.getBoolean("isGroupConversation", false));

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
            entity.progressIndeterminate = Boolean.TRUE.equals(progressObj.getBoolean("indeterminate", false));
        }

        entity.callerName = obj.getString("callerName", "");

        return entity;
    }

    /**
     * Gets the current database size consumed by notifications.
     */
    @SuppressWarnings("unused")
    @PluginMethod
    public void getDatabaseSize(PluginCall call) {
        new Thread(() -> {
            try {
                Long sizeBytes = NotificationDatabase.getDatabase(getContext()).notificationDao().getDatabaseSizeBytes();
                JSObject ret = new JSObject();
                
                if (sizeBytes != null) {
                    ret.put("sizeBytes", sizeBytes);
                    ret.put("sizeMB", sizeBytes / (1024.0 * 1024.0));
                } else {
                    ret.put("sizeBytes", 0);
                    ret.put("sizeMB", 0.0);
                }
                
                call.resolve(ret);
            } catch (Exception e) {
                call.reject("Failed to get database size", e);
            }
        }).start();
    }

    /**
     * Gets the current configuration for the notification reader plugin.
     */
    @SuppressWarnings("unused")
    @PluginMethod
    public void getConfig(PluginCall call) {
        NotificationReaderConfig config = new NotificationReaderConfig(getContext());
        
        JSObject ret = new JSObject();
        ret.put("logProgressNotifications", config.shouldLogProgressNotifications());
        
        float storageLimit = config.getStorageLimit();
        if (storageLimit > 0) {
            ret.put("storageLimit", storageLimit);
        } else {
            ret.put("storageLimit", (Object) null);
        }
        
        call.resolve(ret);
    }

    /**
     * Sets the configuration for the notification reader plugin.
     */
    @SuppressWarnings("unused")
    @PluginMethod
    public void setConfig(PluginCall call) {
        NotificationReaderConfig config = new NotificationReaderConfig(getContext());
        
        Boolean logProgressNotifications = call.getBoolean("logProgressNotifications");
        if (logProgressNotifications != null) {
            config.setLogProgressNotifications(logProgressNotifications);
        }
        
        // Handle storageLimit - can be a number or null/undefined
        if (call.getData().has("storageLimit")) {
            if (call.getData().isNull("storageLimit")) {
                config.setStorageLimit(-1f);
            } else {
                Float storageLimit = call.getFloat("storageLimit");
                if (storageLimit != null) {
                    config.setStorageLimit(storageLimit);
                } else {
                    config.setStorageLimit(-1f);
                }
            }
        }
        
        call.resolve();
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
            return primary.optString(key);
        }
        if (hasKey(fallback, key)) {
            return fallback.optString(key);
        }
        return null;
    }

    private JSONArray getJSONArrayOption(JSONObject primary, JSONObject fallback) {
        JSONArray primaryArray = primary != null ? primary.optJSONArray("appNames") : null;
        if (primaryArray != null) {
            return primaryArray;
        }
        return fallback != null ? fallback.optJSONArray("appNames") : null;
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

    private Long getLongOption(JSONObject primary, JSONObject fallback, String key) {
        if (hasKey(primary, key)) {
            return primary.optLong(key);
        }
        if (hasKey(fallback, key)) {
            return fallback.optLong(key);
        }
        return null;
    }

    private String drawableToBase64(Drawable drawable) {
        if (drawable == null) return null;
        Bitmap bitmap;
        if (drawable instanceof BitmapDrawable) {
            bitmap = ((BitmapDrawable) drawable).getBitmap();
        } else {
            int width = drawable.getIntrinsicWidth() > 0 ? drawable.getIntrinsicWidth() : 96;
            int height = drawable.getIntrinsicHeight() > 0 ? drawable.getIntrinsicHeight() : 96;
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
        }
        return bitmapToBase64(bitmap);
    }

    private String bitmapToBase64(Bitmap bitmap) {
        if (bitmap == null) return null;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        byte[] byteArray = outputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.NO_WRAP);
    }
}
