package com.whyash5114.plugins.notificationreader.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.sqlite.db.SupportSQLiteQuery;
import java.util.List;

@Dao
public interface NotificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(NotificationEntity notification);

    @Query("SELECT * FROM notifications WHERE postTime < :cursor ORDER BY postTime DESC LIMIT :limit")
    List<NotificationEntity> getNotifications(long cursor, int limit);

    @Query("SELECT * FROM notifications ORDER BY postTime DESC LIMIT :limit")
    List<NotificationEntity> getNotifications(int limit);

    @RawQuery
    List<NotificationEntity> getNotifications(SupportSQLiteQuery query);

    @Query("DELETE FROM notifications")
    void deleteAllNotifications();

    @Query("SELECT COUNT(*) FROM notifications")
    int getTotalCount();

    @Query("SELECT SUM(LENGTH(id) + LENGTH(packageName) + LENGTH(appName) + " +
            "COALESCE(LENGTH(title), 0) + COALESCE(LENGTH(text), 0) + " +
            "COALESCE(LENGTH(smallIcon), 0) + COALESCE(LENGTH(largeIcon), 0) + " +
            "COALESCE(LENGTH(appIcon), 0) + COALESCE(LENGTH(category), 0) + " +
            "LENGTH(style) + COALESCE(LENGTH(subText), 0) + COALESCE(LENGTH(infoText), 0) + " +
            "COALESCE(LENGTH(summaryText), 0) + COALESCE(LENGTH(groupKey), 0) + " +
            "COALESCE(LENGTH(channelId), 0) + LENGTH(actionsJson) + " +
            "COALESCE(LENGTH(bigText), 0) + COALESCE(LENGTH(bigPicture), 0) + " +
            "COALESCE(LENGTH(pictureContentDescription), 0) + COALESCE(LENGTH(inboxLinesJson), 0) + " +
            "COALESCE(LENGTH(conversationTitle), 0) + COALESCE(LENGTH(messagesJson), 0) + " +
            "COALESCE(LENGTH(callerName), 0)) FROM notifications")
    Long getDatabaseSizeBytes();

    @Query("DELETE FROM notifications WHERE id IN " +
            "(SELECT id FROM notifications ORDER BY postTime ASC LIMIT :count)")
    void deleteOldestNotifications(int count);

    @Query("SELECT id FROM notifications ORDER BY postTime ASC LIMIT 1")
    String getOldestNotificationId();
}
