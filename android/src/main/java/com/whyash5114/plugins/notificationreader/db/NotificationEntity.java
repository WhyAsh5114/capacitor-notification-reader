
package com.whyash5114.plugins.notificationreader.db;

import android.app.Notification;
import android.app.Person;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.service.notification.StatusBarNotification;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

@Entity(tableName = "notifications")
public class NotificationEntity {
    @PrimaryKey
    @NonNull
    public String id;

    // Base notification fields
    @NonNull
    public String packageName;
    @NonNull
    public String appName;
    public String title;
    public String text;
    public long postTime;
    public String smallIcon;
    public String largeIcon;
    public String appIcon;
    public String category;
    @NonNull
    public String style;
    public String subText;
    public String infoText;
    public String summaryText;
    public String groupKey;
    public boolean isGroupSummary;
    public String channelId;
    @NonNull
    public String actionsJson;
    public boolean isOngoing;
    public boolean autoCancel;
    public boolean isLocalOnly;
    public int priority;
    public int number;

    // Style-specific fields
    public String bigText;
    public String bigPicture;
    public String pictureContentDescription;
    public String inboxLinesJson;
    public String conversationTitle;
    public boolean isGroupConversation;
    public String messagesJson;
    public int progress;
    public int progressMax;
    public boolean progressIndeterminate;
    public String callerName; // For CallStyle

    public NotificationEntity() {
        this.id = UUID.randomUUID().toString();
        this.packageName = "";
        this.appName = "";
        this.style = "";
        this.actionsJson = "[]";
    }

    public NotificationEntity(Context context, StatusBarNotification sbn) {
        this.id = UUID.randomUUID().toString();
        Notification notification = sbn.getNotification();
        if (notification == null) {
            this.packageName = sbn.getPackageName();
            this.appName = getAppName(context, this.packageName);
            this.style = "default";
            this.actionsJson = "[]";
            return;
        }

        Bundle extras = notification.extras;

        // Base fields
        this.packageName = sbn.getPackageName();
        this.appName = getAppName(context, this.packageName);
        this.title = extras.getString(Notification.EXTRA_TITLE);

        CharSequence textChars = extras.getCharSequence(Notification.EXTRA_TEXT);
        this.text = textChars != null ? textChars.toString() : null;

        this.postTime = sbn.getPostTime();
        this.smallIcon = getSmallIconBase64(context, notification);
        this.largeIcon = getLargeIconBase64(context, notification);
        this.appIcon = getAppIconBase64(context, sbn.getPackageName());
        this.category = notification.category;
        this.style = getStyleString(extras);

        CharSequence subTextChars = extras.getCharSequence(Notification.EXTRA_SUB_TEXT);
        this.subText = subTextChars != null ? subTextChars.toString() : null;

        CharSequence infoTextChars = extras.getCharSequence(Notification.EXTRA_INFO_TEXT);
        this.infoText = infoTextChars != null ? infoTextChars.toString() : null;

        CharSequence summaryTextChars = extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT);
        this.summaryText = summaryTextChars != null ? summaryTextChars.toString() : null;

        this.groupKey = notification.getGroup();
        this.isGroupSummary = (notification.flags & Notification.FLAG_GROUP_SUMMARY) != 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.channelId = notification.getChannelId();
        }
        this.actionsJson = getActionsJson(context, notification);
        this.isOngoing = (notification.flags & Notification.FLAG_ONGOING_EVENT) != 0;
        this.autoCancel = (notification.flags & Notification.FLAG_AUTO_CANCEL) != 0;
        this.isLocalOnly = (notification.flags & Notification.FLAG_LOCAL_ONLY) != 0;
        this.priority = notification.priority;
        this.number = notification.number;

        // Style-specific fields
        addStyleSpecificData(extras);
    }

    private String getAppName(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        ApplicationInfo ai;
        try {
            ai = pm.getApplicationInfo(packageName, 0);
        } catch (final PackageManager.NameNotFoundException e) {
            ai = null;
        }
        return (String) (ai != null ? pm.getApplicationLabel(ai) : packageName);
    }

    private String getStyleString(Bundle extras) {
        String template = extras.getString(Notification.EXTRA_TEMPLATE);
        if (template != null) {
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

    private String getActionsJson(Context context, Notification notification) {
        JSArray actionsArray = new JSArray();
        if (notification.actions != null) {
            for (Notification.Action action : notification.actions) {
                JSObject actionObj = new JSObject();
                actionObj.put("title", action.title);
                if (action.getIcon() != null) {
                    actionObj.put("icon", drawableToBase64(action.getIcon().loadDrawable(context)));
                } else {
                    actionObj.put("icon", null);
                }
                actionObj.put("allowsRemoteInput", action.getRemoteInputs() != null && action.getRemoteInputs().length > 0);
                actionsArray.put(actionObj);
            }
        }
        return actionsArray.toString();
    }

    private void addStyleSpecificData(Bundle extras) {
        switch (this.style) {
            case "BigTextStyle":
                CharSequence bigTextChars = extras.getCharSequence(Notification.EXTRA_BIG_TEXT);
                this.bigText = bigTextChars != null ? bigTextChars.toString() : null;
                break;
            case "BigPictureStyle":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    this.pictureContentDescription = extras.getString(Notification.EXTRA_PICTURE_CONTENT_DESCRIPTION);
                }
                Bitmap picture = extras.getParcelable(Notification.EXTRA_PICTURE);
                if (picture != null) {
                    this.bigPicture = bitmapToBase64(picture);
                }
                break;
            case "InboxStyle":
                CharSequence[] lines = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES);
                if (lines != null) {
                    JSArray linesArray = new JSArray();
                    for (CharSequence line : lines) {
                        linesArray.put(line.toString());
                    }
                    this.inboxLinesJson = linesArray.toString();
                }
                break;
            case "MessagingStyle":
                 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    CharSequence conversationTitleChars = extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE);
                    this.conversationTitle = conversationTitleChars != null ? conversationTitleChars.toString() : null;
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    this.isGroupConversation = extras.getBoolean(Notification.EXTRA_IS_GROUP_CONVERSATION, false);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    this.messagesJson = extractMessagesJson(extras);
                }
                break;
        }

        if (extras.containsKey(Notification.EXTRA_PROGRESS)) {
            this.progress = extras.getInt(Notification.EXTRA_PROGRESS, 0);
            this.progressMax = extras.getInt(Notification.EXTRA_PROGRESS_MAX, 0);
            this.progressIndeterminate = extras.getBoolean(Notification.EXTRA_PROGRESS_INDETERMINATE, false);
        }
    }

    private String extractMessagesJson(Bundle extras) {
        JSArray messagesArray = new JSArray();
        Parcelable[] parcelables = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            parcelables = extras.getParcelableArray(Notification.EXTRA_MESSAGES);
        }
        if (parcelables != null) {
            for (Parcelable p : parcelables) {
                if (p instanceof Bundle messageBundle) {
                    JSObject messageObj = new JSObject();
                    messageObj.put("text", messageBundle.getCharSequence("text"));
                    messageObj.put("timestamp", messageBundle.getLong("time", 0));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        Person sender;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            sender = messageBundle.getParcelable("sender", Person.class);
                        } else {
                            sender = messageBundle.getParcelable("sender");
                        }

                        if (sender != null) {
                            messageObj.put("sender", sender.getName());
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
        return messagesArray.toString();
    }

    private String getSmallIconBase64(Context context, Notification notification) {
        Icon icon = notification.getSmallIcon();
        if (icon != null) {
            return drawableToBase64(icon.loadDrawable(context));
        }
        return null;
    }

    private String getLargeIconBase64(Context context, Notification notification) {
        Icon icon = notification.getLargeIcon();
        if (icon != null) {
            return drawableToBase64(icon.loadDrawable(context));
        }
        return null;
    }

    private String getAppIconBase64(Context context, String packageName) {
        try {
            Drawable drawable = context.getPackageManager().getApplicationIcon(packageName);
            return drawableToBase64(drawable);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
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
