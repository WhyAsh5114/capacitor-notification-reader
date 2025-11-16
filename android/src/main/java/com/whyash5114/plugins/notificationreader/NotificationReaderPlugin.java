package com.whyash5114.plugins.notificationreader;

import android.app.Notification;
import android.app.Person;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.service.notification.StatusBarNotification;
import android.util.Base64;
import androidx.activity.result.ActivityResult;
import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.ActivityCallback;
import com.getcapacitor.annotation.CapacitorPlugin;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Capacitor plugin for reading Android notifications.
 * Provides methods to check permission status, request permission, and read active notifications.
 */
@CapacitorPlugin(name = "NotificationReader")
public class NotificationReaderPlugin extends Plugin {

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
            Notification notification = sbn.getNotification();
            Bundle extras = notification.extras;

            JSObject obj = new JSObject();
            
            // Basic info
            obj.put("app", sbn.getPackageName());
            obj.put("title", extras.getString(Notification.EXTRA_TITLE));
            obj.put("text", extras.getCharSequence(Notification.EXTRA_TEXT));
            obj.put("timestamp", sbn.getPostTime());
            
            // Icons
            obj.put("smallIcon", getSmallIconBase64(notification));
            obj.put("largeIcon", getLargeIconBase64(notification));
            obj.put("appIcon", getAppIconBase64(sbn.getPackageName()));
            
            // Category and style
            obj.put("category", getCategoryString(notification.category));
            obj.put("style", getStyleString(extras));
            
            // Additional text fields
            obj.put("subText", extras.getCharSequence(Notification.EXTRA_SUB_TEXT));
            obj.put("infoText", extras.getCharSequence(Notification.EXTRA_INFO_TEXT));
            obj.put("summaryText", extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT));
            
            // Group information
            obj.put("group", notification.getGroup());
            obj.put("isGroupSummary", (notification.flags & Notification.FLAG_GROUP_SUMMARY) != 0);
            
            // Channel ID (API 26+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                obj.put("channelId", notification.getChannelId());
            }
            
            // Actions
            obj.put("actions", getActionsArray(notification));
            
            // Flags
            obj.put("isOngoing", (notification.flags & Notification.FLAG_ONGOING_EVENT) != 0);
            obj.put("autoCancel", (notification.flags & Notification.FLAG_AUTO_CANCEL) != 0);
            obj.put("isLocalOnly", (notification.flags & Notification.FLAG_LOCAL_ONLY) != 0);
            
            // Priority
            obj.put("priority", notification.priority);
            
            // Number badge
            obj.put("number", notification.number);
            
            // Style-specific data
            addStyleSpecificData(obj, extras, notification);

            arr.put(obj);
        }

        JSObject ret = new JSObject();
        ret.put("notifications", arr);

        call.resolve(ret);
    }

    private boolean isNotificationAccessEnabled() {
        String enabled = Settings.Secure.getString(getContext().getContentResolver(), "enabled_notification_listeners");
        return enabled != null && enabled.contains(getContext().getPackageName());
    }

    /**
     * Converts notification category to string representation.
     */
    private String getCategoryString(String category) {
        if (category == null) return "unknown";
        return category;
    }

    /**
     * Extracts the notification style from extras.
     */
    private String getStyleString(Bundle extras) {
        String template = extras.getString(Notification.EXTRA_TEMPLATE);
        if (template != null) {
            // Extract just the style name from the template class
            if (template.contains("BigTextStyle")) return "BigTextStyle";
            if (template.contains("BigPictureStyle")) return "BigPictureStyle";
            if (template.contains("InboxStyle")) return "InboxStyle";
            if (template.contains("MessagingStyle")) return "MessagingStyle";
            if (template.contains("MediaStyle")) return "MediaStyle";
            if (template.contains("CallStyle")) return "CallStyle";
            if (template.contains("DecoratedMediaCustomViewStyle")) return "DecoratedMediaCustomViewStyle";
            if (template.contains("DecoratedCustomViewStyle")) return "DecoratedCustomViewStyle";
        }
        return "default";
    }

    /**
     * Extracts action buttons from notification.
     */
    private JSArray getActionsArray(Notification notification) {
        JSArray actionsArray = new JSArray();
        if (notification.actions != null) {
            for (Notification.Action action : notification.actions) {
                JSObject actionObj = new JSObject();
                actionObj.put("title", action.title);
                
                // Extract action icon
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && action.getIcon() != null) {
                    try {
                        Drawable drawable = action.getIcon().loadDrawable(getContext());
                        actionObj.put("icon", drawableToBase64(drawable));
                    } catch (Exception e) {
                        actionObj.put("icon", null);
                    }
                } else {
                    actionObj.put("icon", null);
                }
                
                // Check if action allows remote input (for inline replies)
                boolean allowsRemoteInput = action.getRemoteInputs() != null && action.getRemoteInputs().length > 0;
                actionObj.put("allowsRemoteInput", allowsRemoteInput);
                
                actionsArray.put(actionObj);
            }
        }
        return actionsArray;
    }

    /**
     * Adds style-specific data based on notification style.
     */
    private void addStyleSpecificData(JSObject obj, Bundle extras, Notification notification) {
        String style = getStyleString(extras);
        
        switch (style) {
            case "BigTextStyle":
                obj.put("bigText", extras.getCharSequence(Notification.EXTRA_BIG_TEXT));
                break;
                
            case "BigPictureStyle":
                // Extract big picture if available
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    try {
                        Icon pictureIcon = extras.getParcelable(Notification.EXTRA_PICTURE_ICON, Icon.class);
                        if (pictureIcon != null) {
                            Drawable drawable = pictureIcon.loadDrawable(getContext());
                            obj.put("bigPicture", drawableToBase64(drawable));
                        }
                    } catch (Exception e) {
                        obj.put("bigPicture", null);
                    }
                } else {
                    try {
                        Bitmap picture = extras.getParcelable(Notification.EXTRA_PICTURE);
                        if (picture != null) {
                            obj.put("bigPicture", bitmapToBase64(picture));
                        }
                    } catch (Exception e) {
                        obj.put("bigPicture", null);
                    }
                }
                obj.put("pictureContentDescription", extras.getString(Notification.EXTRA_PICTURE_CONTENT_DESCRIPTION));
                break;
                
            case "InboxStyle":
                CharSequence[] lines = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES);
                JSArray linesArray = new JSArray();
                if (lines != null) {
                    for (CharSequence line : lines) {
                        if (line != null) {
                            linesArray.put(line.toString());
                        }
                    }
                }
                obj.put("inboxLines", linesArray);
                break;
                
            case "MessagingStyle":
                obj.put("conversationTitle", extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE));
                obj.put("isGroupConversation", extras.getBoolean(Notification.EXTRA_IS_GROUP_CONVERSATION, false));
                obj.put("messages", extractMessages(extras));
                break;
        }
        
        // Progress information (for any notification with progress)
        if (extras.containsKey(Notification.EXTRA_PROGRESS)) {
            JSObject progressObj = new JSObject();
            progressObj.put("current", extras.getInt(Notification.EXTRA_PROGRESS, 0));
            progressObj.put("max", extras.getInt(Notification.EXTRA_PROGRESS_MAX, 0));
            progressObj.put("indeterminate", extras.getBoolean(Notification.EXTRA_PROGRESS_INDETERMINATE, false));
            obj.put("progress", progressObj);
        }
    }

    /**
     * Extracts messages from MessagingStyle notifications.
     */
    private JSArray extractMessages(Bundle extras) {
        JSArray messagesArray = new JSArray();
        
        try {
            Parcelable[] parcelables = extras.getParcelableArray(Notification.EXTRA_MESSAGES);
            if (parcelables != null) {
                for (Parcelable p : parcelables) {
                    if (p instanceof Bundle) {
                        Bundle messageBundle = (Bundle) p;
                        JSObject messageObj = new JSObject();
                        
                        messageObj.put("text", messageBundle.getCharSequence("text"));
                        messageObj.put("timestamp", messageBundle.getLong("time", 0));
                        
                        // Extract sender
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            Person sender = messageBundle.getParcelable("sender", Person.class);
                            if (sender != null && sender.getName() != null) {
                                messageObj.put("sender", sender.getName().toString());
                            } else {
                                messageObj.put("sender", messageBundle.getCharSequence("sender"));
                            }
                        } else {
                            messageObj.put("sender", messageBundle.getCharSequence("sender"));
                        }
                        
                        messagesArray.put(messageObj);
                    }
                }
            }
        } catch (Exception e) {
            // Failed to extract messages, return empty array
        }
        
        return messagesArray;
    }

    /**
     * Extracts the small icon from a notification and converts it to a base64-encoded PNG string.
     *
     * @param notification The notification to extract the icon from
     * @return Base64-encoded PNG string or null if extraction fails
     */
    private String getSmallIconBase64(Notification notification) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Icon icon = notification.getSmallIcon();
                if (icon != null) {
                    Drawable drawable = icon.loadDrawable(getContext());
                    return drawableToBase64(drawable);
                }
            }
        } catch (Exception e) {
            // Icon extraction failed, return null
        }
        return null;
    }

    /**
     * Extracts the large icon from a notification and converts it to a base64-encoded PNG string.
     *
     * @param notification The notification to extract the icon from
     * @return Base64-encoded PNG string or null if no large icon or extraction fails
     */
    private String getLargeIconBase64(Notification notification) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Icon icon = notification.getLargeIcon();
                if (icon != null) {
                    Drawable drawable = icon.loadDrawable(getContext());
                    return drawableToBase64(drawable);
                }
            } else {
                Bitmap bitmap = notification.largeIcon;
                if (bitmap != null) {
                    return bitmapToBase64(bitmap);
                }
            }
        } catch (Exception e) {
            // Icon extraction failed, return null
        }
        return null;
    }

    /**
     * Retrieves the app's launcher icon and converts it to a base64-encoded PNG string.
     *
     * @param packageName The package name of the app
     * @return Base64-encoded PNG string or null if extraction fails
     */
    private String getAppIconBase64(String packageName) {
        try {
            PackageManager pm = getContext().getPackageManager();
            Drawable drawable = pm.getApplicationIcon(packageName);
            return drawableToBase64(drawable);
        } catch (Exception e) {
            // Icon extraction failed, return null
        }
        return null;
    }

    /**
     * Converts a Drawable to a base64-encoded PNG string.
     *
     * @param drawable The drawable to convert
     * @return Base64-encoded PNG string or null if conversion fails
     */
    private String drawableToBase64(Drawable drawable) {
        if (drawable == null) return null;
        
        Bitmap bitmap;
        if (drawable instanceof BitmapDrawable) {
            bitmap = ((BitmapDrawable) drawable).getBitmap();
        } else {
            // Convert drawable to bitmap
            int width = drawable.getIntrinsicWidth();
            int height = drawable.getIntrinsicHeight();
            
            // Use default size if intrinsic dimensions are invalid
            if (width <= 0) width = 96;
            if (height <= 0) height = 96;
            
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
        }
        
        return bitmapToBase64(bitmap);
    }

    /**
     * Converts a Bitmap to a base64-encoded PNG string.
     *
     * @param bitmap The bitmap to convert
     * @return Base64-encoded PNG string or null if conversion fails
     */
    private String bitmapToBase64(Bitmap bitmap) {
        if (bitmap == null) return null;
        
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            byte[] byteArray = outputStream.toByteArray();
            return Base64.encodeToString(byteArray, Base64.NO_WRAP);
        } catch (Exception e) {
            return null;
        }
    }
}