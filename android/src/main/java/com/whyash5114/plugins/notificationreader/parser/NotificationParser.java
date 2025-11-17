
package com.whyash5114.plugins.notificationreader.parser;

import android.app.Notification;
import android.app.Person;
import android.content.Context;
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

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.google.gson.Gson;
import com.whyash5114.plugins.notificationreader.db.NotificationEntity;

import java.io.ByteArrayOutputStream;

public class NotificationParser {

    public static NotificationEntity parse(Context context, StatusBarNotification sbn) {
        Notification notification = sbn.getNotification();
        if (notification == null) {
            return null;
        }

        NotificationEntity entity = new NotificationEntity();
        Bundle extras = notification.extras;

        // Base fields
        entity.packageName = sbn.getPackageName();
        entity.title = extras.getString(Notification.EXTRA_TITLE);
        entity.text = extras.getCharSequence(Notification.EXTRA_TEXT) != null ? extras.getCharSequence(Notification.EXTRA_TEXT).toString() : null;
        entity.postTime = sbn.getPostTime();
        entity.smallIcon = getSmallIconBase64(context, notification);
        entity.largeIcon = getLargeIconBase64(context, notification);
        entity.appIcon = getAppIconBase64(context, sbn.getPackageName());
        entity.category = notification.category;
        entity.style = getStyleString(extras);
        entity.subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT) != null ? extras.getCharSequence(Notification.EXTRA_SUB_TEXT).toString() : null;
        entity.infoText = extras.getCharSequence(Notification.EXTRA_INFO_TEXT) != null ? extras.getCharSequence(Notification.EXTRA_INFO_TEXT).toString() : null;
        entity.summaryText = extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT) != null ? extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT).toString() : null;
        entity.groupKey = notification.getGroup();
        entity.isGroupSummary = (notification.flags & Notification.FLAG_GROUP_SUMMARY) != 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            entity.channelId = notification.getChannelId();
        }
        entity.actionsJson = getActionsJson(context, notification);
        entity.isOngoing = (notification.flags & Notification.FLAG_ONGOING_EVENT) != 0;
        entity.autoCancel = (notification.flags & Notification.FLAG_AUTO_CANCEL) != 0;
        entity.isLocalOnly = (notification.flags & Notification.FLAG_LOCAL_ONLY) != 0;
        entity.priority = notification.priority;
        entity.number = notification.number;

        // Style-specific fields
        addStyleSpecificData(entity, extras);

        return entity;
    }

    private static String getStyleString(Bundle extras) {
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

    private static String getActionsJson(Context context, Notification notification) {
        JSArray actionsArray = new JSArray();
        if (notification.actions != null) {
            for (Notification.Action action : notification.actions) {
                JSObject actionObj = new JSObject();
                actionObj.put("title", action.title);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && action.getIcon() != null) {
                    actionObj.put("icon", drawableToBase64(context, action.getIcon().loadDrawable(context)));
                } else {
                    actionObj.put("icon", null);
                }
                actionObj.put("allowsRemoteInput", action.getRemoteInputs() != null && action.getRemoteInputs().length > 0);
                actionsArray.put(actionObj);
            }
        }
        return actionsArray.toString();
    }

    private static void addStyleSpecificData(NotificationEntity entity, Bundle extras) {
        switch (entity.style) {
            case "BigTextStyle":
                entity.bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT) != null ? extras.getCharSequence(Notification.EXTRA_BIG_TEXT).toString() : null;
                break;
            case "BigPictureStyle":
                Bitmap picture = extras.getParcelable(Notification.EXTRA_PICTURE);
                if (picture != null) {
                    entity.bigPicture = bitmapToBase64(picture);
                }
                entity.pictureContentDescription = extras.getString(Notification.EXTRA_PICTURE_CONTENT_DESCRIPTION);
                break;
            case "InboxStyle":
                CharSequence[] lines = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES);
                if (lines != null) {
                    JSArray linesArray = new JSArray();
                    for (CharSequence line : lines) {
                        linesArray.put(line.toString());
                    }
                    entity.inboxLinesJson = linesArray.toString();
                }
                break;
            case "MessagingStyle":
                entity.conversationTitle = extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE) != null ? extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE).toString() : null;
                entity.isGroupConversation = extras.getBoolean(Notification.EXTRA_IS_GROUP_CONVERSATION, false);
                entity.messagesJson = extractMessagesJson(extras);
                break;
        }

        if (extras.containsKey(Notification.EXTRA_PROGRESS)) {
            entity.progress = extras.getInt(Notification.EXTRA_PROGRESS, 0);
            entity.progressMax = extras.getInt(Notification.EXTRA_PROGRESS_MAX, 0);
            entity.progressIndeterminate = extras.getBoolean(Notification.EXTRA_PROGRESS_INDETERMINATE, false);
        }
    }

    private static String extractMessagesJson(Bundle extras) {
        JSArray messagesArray = new JSArray();
        Parcelable[] parcelables = extras.getParcelableArray(Notification.EXTRA_MESSAGES);
        if (parcelables != null) {
            for (Parcelable p : parcelables) {
                if (p instanceof Bundle) {
                    Bundle messageBundle = (Bundle) p;
                    JSObject messageObj = new JSObject();
                    messageObj.put("text", messageBundle.getCharSequence("text"));
                    messageObj.put("timestamp", messageBundle.getLong("time", 0));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        Person sender = messageBundle.getParcelable("sender", Person.class);
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

    private static String getSmallIconBase64(Context context, Notification notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Icon icon = notification.getSmallIcon();
            if (icon != null) {
                return drawableToBase64(context, icon.loadDrawable(context));
            }
        }
        return null;
    }

    private static String getLargeIconBase64(Context context, Notification notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Icon icon = notification.getLargeIcon();
            if (icon != null) {
                return drawableToBase64(context, icon.loadDrawable(context));
            }
        } else {
            Bitmap bitmap = notification.largeIcon;
            if (bitmap != null) {
                return bitmapToBase64(bitmap);
            }
        }
        return null;
    }

    private static String getAppIconBase64(Context context, String packageName) {
        try {
            Drawable drawable = context.getPackageManager().getApplicationIcon(packageName);
            return drawableToBase64(context, drawable);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    private static String drawableToBase64(Context context, Drawable drawable) {
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

    private static String bitmapToBase64(Bitmap bitmap) {
        if (bitmap == null) return null;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        byte[] byteArray = outputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.NO_WRAP);
    }
}
