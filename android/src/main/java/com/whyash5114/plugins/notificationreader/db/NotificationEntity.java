
package com.whyash5114.plugins.notificationreader.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.UUID;

@Entity(tableName = "notifications")
public class NotificationEntity {
    @PrimaryKey
    @NonNull
    public String id;

    // Base notification fields
    public String packageName;
    public String title;
    public String text;
    public long postTime;
    public String smallIcon;
    public String largeIcon;
    public String appIcon;
    public String category;
    public String style;
    public String subText;
    public String infoText;
    public String summaryText;
    public String groupKey;
    public boolean isGroupSummary;
    public String channelId;
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
    }
}
