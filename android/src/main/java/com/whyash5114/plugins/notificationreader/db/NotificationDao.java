package com.whyash5114.plugins.notificationreader.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface NotificationDao {
    @Insert
    void insert(NotificationEntity notification);

    @Query("SELECT * FROM notifications WHERE id > :afterId ORDER BY id ASC LIMIT :limit")
    List<NotificationEntity> getNotifications(int afterId, int limit);
}
