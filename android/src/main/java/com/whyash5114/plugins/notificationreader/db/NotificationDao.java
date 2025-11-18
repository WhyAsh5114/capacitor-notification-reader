package com.whyash5114.plugins.notificationreader.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface NotificationDao {
    @Insert
    void insert(NotificationEntity notification);

    @Query("SELECT * FROM notifications WHERE postTime < :cursor ORDER BY postTime DESC LIMIT :limit")
    List<NotificationEntity> getNotifications(long cursor, int limit);

    @Query("SELECT * FROM notifications ORDER BY postTime DESC LIMIT :limit")
    List<NotificationEntity> getNotifications(int limit);
}
