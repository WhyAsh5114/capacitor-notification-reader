package com.whyash5114.plugins.notificationreader.db.migrations;

import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public class Migration_1_2 extends Migration {
    public Migration_1_2() {
        super(1, 2);
    }

    @Override
    public void migrate(SupportSQLiteDatabase database) {
        // Create a new table with the desired schema
        database.execSQL("CREATE TABLE `notifications_new` (`id` TEXT NOT NULL, `packageName` TEXT, `title` TEXT, `text` TEXT, `postTime` INTEGER NOT NULL, `smallIcon` TEXT, `largeIcon` TEXT, `appIcon` TEXT, `category` TEXT, `style` TEXT, `subText` TEXT, `infoText` TEXT, `summaryText` TEXT, `groupKey` TEXT, `isGroupSummary` INTEGER NOT NULL, `channelId` TEXT, `actionsJson` TEXT, `isOngoing` INTEGER NOT NULL, `autoCancel` INTEGER NOT NULL, `isLocalOnly` INTEGER NOT NULL, `priority` INTEGER NOT NULL, `number` INTEGER NOT NULL, `bigText` TEXT, `bigPicture` TEXT, `pictureContentDescription` TEXT, `inboxLinesJson` TEXT, `conversationTitle` TEXT, `isGroupConversation` INTEGER NOT NULL, `messagesJson` TEXT, `progress` INTEGER NOT NULL, `progressMax` INTEGER NOT NULL, `progressIndeterminate` INTEGER NOT NULL, `callerName` TEXT, PRIMARY KEY(`id`))");

        // Copy the data from the old table to the new table, generating UUIDs for the `id` column
        database.execSQL("INSERT INTO `notifications_new` (id, packageName, title, text, postTime, smallIcon, largeIcon, appIcon, category, style, subText, infoText, summaryText, groupKey, isGroupSummary, channelId, actionsJson, isOngoing, autoCancel, isLocalOnly, priority, number, bigText, bigPicture, pictureContentDescription, inboxLinesJson, conversationTitle, isGroupConversation, messagesJson, progress, progressMax, progressIndeterminate, callerName) SELECT randomblob(16), packageName, title, text, postTime, smallIcon, largeIcon, appIcon, category, style, subText, infoText, summaryText, groupKey, isGroupSummary, channelId, actionsJson, isOngoing, autoCancel, isLocalOnly, priority, number, bigText, bigPicture, pictureContentDescription, inboxLinesJson, conversationTitle, isGroupConversation, messagesJson, progress, progressMax, progressIndeterminate, callerName FROM `notifications`");

        // Drop the old table
        database.execSQL("DROP TABLE `notifications`");

        // Rename the new table to the original table name
        database.execSQL("ALTER TABLE `notifications_new` RENAME TO `notifications`");
    }
}
