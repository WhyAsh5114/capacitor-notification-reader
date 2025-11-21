package com.whyash5114.plugins.notificationreader.db;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = { NotificationEntity.class }, version = 4, exportSchema = false)
public abstract class NotificationDatabase extends RoomDatabase {

    public abstract NotificationDao notificationDao();

    private static volatile NotificationDatabase INSTANCE;

    public static NotificationDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (NotificationDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), NotificationDatabase.class, "notification_database")
                        .fallbackToDestructiveMigration()
                        .build();
                }
            }
        }
        return INSTANCE;
    }
}
