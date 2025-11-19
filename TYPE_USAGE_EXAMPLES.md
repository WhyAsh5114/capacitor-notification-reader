# Type-Safe Notification Usage Examples

This plugin provides comprehensive type safety through discriminated unions. TypeScript will automatically narrow the notification type based on the `style` and `category` fields.

## Overview: Active vs. Stored Notifications

This plugin offers two ways to access notifications:

1. **Active Notifications** (`getActiveNotifications()`): Returns notifications currently visible in the Android notification drawer. These disappear when dismissed by the user.

2. **Stored Notifications** (`getNotifications()`): Returns notifications from the RoomDB database. These are automatically saved when posted and persist even after:
   - The user dismisses the notification
   - Your app is closed or terminated
   - The device is rebooted

**Key Benefit**: The NotificationListenerService runs in the background and stores notifications in the database even when your app isn't running. This means you can retrieve a complete history of notifications that occurred while your app was closed.

## Basic Usage

### Get Active Notifications

```typescript
import { NotificationReader, NotificationItem, NotificationStyle, NotificationCategory } from 'capacitor-notification-reader';

// Get currently active notifications from the notification drawer
const { notifications } = await NotificationReader.getActiveNotifications();

notifications.forEach((notification) => {
  console.log(`${notification.appName}: ${notification.title}`);
  console.log(`Package: ${notification.packageName}`);
  console.log(`Category: ${notification.category}, Style: ${notification.style}`);
});
```

### Get Notifications from Database (with Pagination)

```typescript
import { NotificationReader } from 'capacitor-notification-reader';

// Get the first 20 notifications from the database (most recent first)
const { notifications } = await NotificationReader.getNotifications({
  limit: 20
});

// Pagination: get the next batch using cursor (timestamp-based)
if (notifications.length > 0) {
  const lastTimestamp = notifications[notifications.length - 1].timestamp;
  const { notifications: nextBatch } = await NotificationReader.getNotifications({
    cursor: lastTimestamp,
    limit: 20
  });
}

// Process notifications
notifications.forEach((notification) => {
  console.log(`ID: ${notification.id}`); // UUID
  console.log(`${notification.appName}: ${notification.title}`);
  console.log(`Package: ${notification.packageName}`);
  console.log(`Category: ${notification.category}, Style: ${notification.style}`);
});
```

## Type Narrowing with Discriminated Unions

### By Style

```typescript
notifications.forEach((notification) => {
  // TypeScript narrows the type based on the style
  switch (notification.style) {
    case NotificationStyle.BIG_TEXT:
      // notification is typed as BigTextNotification
      console.log('Big text:', notification.bigText);
      break;
      
    case NotificationStyle.BIG_PICTURE:
      // notification is typed as BigPictureNotification
      if (notification.bigPicture) {
        const img = document.createElement('img');
        img.src = `data:image/png;base64,${notification.bigPicture}`;
        document.body.appendChild(img);
      }
      break;
      
    case NotificationStyle.MESSAGING:
      // notification is typed as MessagingNotification
      console.log('Conversation:', notification.conversationTitle);
      notification.messages.forEach(msg => {
        console.log(`${msg.sender}: ${msg.text}`);
      });
      break;
      
    case NotificationStyle.INBOX:
      // notification is typed as InboxNotification
      notification.inboxLines.forEach(line => {
        console.log('- ' + line);
      });
      break;
  }
});
```

### By Category

```typescript
// Filter for messaging notifications
const messageNotifications = notifications.filter(
  n => n.category === NotificationCategory.MESSAGE
);

messageNotifications.forEach((notification) => {
  // Display message notifications
  if (notification.style === NotificationStyle.MESSAGING) {
    renderMessagingNotification(notification);
  }
});

// Filter for calls
const callNotifications = notifications.filter(
  n => n.category === NotificationCategory.CALL || 
       n.category === NotificationCategory.MISSED_CALL
) as CallNotification[];

callNotifications.forEach((call) => {
  console.log(`Call from: ${call.title}`);
});

// Filter for progress notifications (downloads, etc.)
const progressNotifications = notifications.filter(
  n => n.category === NotificationCategory.PROGRESS
) as ProgressNotification[];

progressNotifications.forEach((notification) => {
  if (notification.progress) {
    const percent = (notification.progress.current / notification.progress.max) * 100;
    console.log(`${notification.title}: ${percent.toFixed(0)}%`);
  }
});
```

## Working with Icons

```typescript
notifications.forEach((notification) => {
  // App icon (launcher icon)
  if (notification.appIcon) {
    const appIconImg = document.createElement('img');
    appIconImg.src = `data:image/png;base64,${notification.appIcon}`;
    appIconImg.className = 'app-icon';
  }
  
  // Small icon (status bar icon)
  if (notification.smallIcon) {
    const smallIconImg = document.createElement('img');
    smallIconImg.src = `data:image/png;base64,${notification.smallIcon}`;
    smallIconImg.className = 'small-icon';
  }
  
  // Large icon (notification icon)
  if (notification.largeIcon) {
    const largeIconImg = document.createElement('img');
    largeIconImg.src = `data:image/png;base64,${notification.largeIcon}`;
    largeIconImg.className = 'large-icon';
  }
});
```

## Working with Actions

```typescript
notifications.forEach((notification) => {
  notification.actions.forEach((action) => {
    console.log(`Action: ${action.title}`);
    
    if (action.allowsRemoteInput) {
      console.log('  → Supports inline reply');
    }
    
    if (action.icon) {
      const actionIcon = document.createElement('img');
      actionIcon.src = `data:image/png;base64,${action.icon}`;
    }
  });
});
```

## React Component Example

### Active Notifications

```typescript
import React, { useState, useEffect } from 'react';
import { NotificationReader, NotificationItem, NotificationStyle } from 'capacitor-notification-reader';

const NotificationList: React.FC = () => {
  const [notifications, setNotifications] = useState<NotificationItem[]>([]);
  
  useEffect(() => {
    loadNotifications();
  }, []);
  
  const loadNotifications = async () => {
    const { notifications } = await NotificationReader.getActiveNotifications();
    setNotifications(notifications);
  };
  
  return (
    <div className="notification-list">
      {notifications.map((notification, index) => (
        <div key={index} className="notification-item">
          {notification.appIcon && (
            <img 
              src={`data:image/png;base64,${notification.appIcon}`} 
              alt={notification.appName}
              className="app-icon"
              title={notification.packageName}
            />
          )}
          
          <div className="notification-content">
            <h3>{notification.title}</h3>
            <p>{notification.text}</p>
            
            {notification.style === NotificationStyle.BIG_TEXT && notification.bigText && (
              <p className="big-text">{notification.bigText}</p>
            )}
            
            {notification.style === NotificationStyle.MESSAGING && (
              <div className="messages">
                {notification.messages.map((msg, i) => (
                  <div key={i} className="message">
                    <strong>{msg.sender}:</strong> {msg.text}
                  </div>
                ))}
              </div>
            )}
            
            {notification.progress && (
              <div className="progress-bar">
                <div 
                  className="progress-fill" 
                  style={{ 
                    width: `${(notification.progress.current / notification.progress.max) * 100}%` 
                  }}
                />
              </div>
            )}
            
            {notification.actions.length > 0 && (
              <div className="actions">
                {notification.actions.map((action, i) => (
                  <button key={i}>{action.title}</button>
                ))}
              </div>
            )}
          </div>
        </div>
      ))}
    </div>
  );
};

export default NotificationList;
```

### Real-time Notification Listener

```typescript
import React, { useEffect } from 'react';
import { NotificationReader, NotificationItem } from 'capacitor-notification-reader';
import { PluginListenerHandle } from '@capacitor/core';

const RealtimeNotifications: React.FC = () => {
  useEffect(() => {
    let listener: PluginListenerHandle;
    
    const setupListener = async () => {
      // Listen for notifications posted in real-time
      // Note: Notifications are still stored in DB even if app is closed
      listener = await NotificationReader.addListener(
        'notificationPosted',
        (notification: NotificationItem) => {
          console.log('New notification received:', notification.title);
          console.log('Stored in DB with ID:', notification.id);
          
          // Update your UI or trigger actions
          handleNewNotification(notification);
        }
      );
    };
    
    setupListener();
    
    return () => {
      listener?.remove();
    };
  }, []);
  
  const handleNewNotification = (notification: NotificationItem) => {
    // Your custom logic here
    // The notification is already saved to the database automatically
  };
  
  return <div>Listening for notifications...</div>;
};

export default RealtimeNotifications;
```

### Database Notifications with Pagination

```typescript
import React, { useState, useEffect } from 'react';
import { NotificationReader, NotificationItem } from 'capacitor-notification-reader';

const NotificationHistory: React.FC = () => {
  const [notifications, setNotifications] = useState<NotificationItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [hasMore, setHasMore] = useState(true);
  
  const loadMore = async () => {
    if (loading || !hasMore) return;
    
    setLoading(true);
    try {
      const cursor = notifications.length > 0 
        ? notifications[notifications.length - 1].timestamp 
        : undefined;
      
      const { notifications: newNotifications } = await NotificationReader.getNotifications({
        cursor,
        limit: 20
      });
      
      if (newNotifications.length === 0) {
        setHasMore(false);
      } else {
        setNotifications([...notifications, ...newNotifications]);
      }
    } finally {
      setLoading(false);
    }
  };
  
  useEffect(() => {
    loadMore();
  }, []);
  
  return (
    <div className="notification-history">
      {notifications.map((notification) => (
        <div key={notification.id} className="notification-item">
          <h3>{notification.title}</h3>
          <p>{notification.text}</p>
          <small>{new Date(notification.timestamp).toLocaleString()}</small>
        </div>
      ))}
      
      {hasMore && (
        <button onClick={loadMore} disabled={loading}>
          {loading ? 'Loading...' : 'Load More'}
        </button>
      )}
    </div>
  );
};

export default NotificationHistory;
```

## Grouping Notifications

```typescript
// Group notifications by app (using package name for unique identification)
const groupedByApp = notifications.reduce((acc, notification) => {
  if (!acc[notification.packageName]) {
    acc[notification.packageName] = [];
  }
  acc[notification.packageName].push(notification);
  return acc;
}, {} as Record<string, NotificationItem[]>);

// Or group by app name (for display purposes)
const groupedByAppName = notifications.reduce((acc, notification) => {
  if (!acc[notification.appName]) {
    acc[notification.appName] = [];
  }
  acc[notification.appName].push(notification);
  return acc;
}, {} as Record<string, NotificationItem[]>);

// Find group summaries
const groupSummaries = notifications.filter(n => n.isGroupSummary);

// Get notifications by group key
const groupedNotifications = notifications.reduce((acc, notification) => {
  if (notification.group) {
    if (!acc[notification.group]) {
      acc[notification.group] = [];
    }
    acc[notification.group].push(notification);
  }
  return acc;
}, {} as Record<string, NotificationItem[]>);
```

## Filtering by Characteristics

```typescript
// Only ongoing notifications (can't be dismissed)
const ongoingNotifications = notifications.filter(n => n.isOngoing);

// Notifications with high priority
const highPriorityNotifications = notifications.filter(n => n.priority > 0);

// Notifications with badges/numbers
const badgedNotifications = notifications.filter(n => n.number && n.number > 0);

// Social notifications with pictures
const socialWithPictures = notifications.filter(n => 
  n.category === NotificationCategory.SOCIAL && 
  n.style === NotificationStyle.BIG_PICTURE
) as BigPictureNotification[];
```

## Database Management

### Clear All Notifications

```typescript
import { NotificationReader } from 'capacitor-notification-reader';

// Delete all stored notifications from the database
await NotificationReader.deleteAllNotifications();
console.log('Notification history cleared');

// Note: This only affects the database, not the system notification drawer
// New notifications will continue to be stored as they arrive
```

### Import Notifications

```typescript
import { NotificationReader, NotificationStyle, NotificationCategory } from 'capacitor-notification-reader';

// Import notifications from backup or migration
const backupNotifications = [
  {
    id: '550e8400-e29b-41d4-a716-446655440001',
    appName: 'WhatsApp',
    packageName: 'com.whatsapp',
    title: 'Alice',
    text: 'Hey, are we still meeting tomorrow?',
    timestamp: Date.now() - 7200000, // 2 hours ago
    style: NotificationStyle.MESSAGING,
    category: NotificationCategory.MESSAGE,
    actions: [
      {
        title: 'Reply',
        allowsRemoteInput: true
      }
    ],
    isGroupSummary: false,
    isOngoing: false,
    autoCancel: true,
    isLocalOnly: false,
    priority: 1,
    number: 1,
    messages: [
      {
        text: 'Hey, are we still meeting tomorrow?',
        timestamp: Date.now() - 7200000,
        sender: 'Alice'
      }
    ],
    conversationTitle: 'Alice',
    isGroupConversation: false
  },
  {
    id: '550e8400-e29b-41d4-a716-446655440002',
    appName: 'Gmail',
    packageName: 'com.google.android.gm',
    title: 'Important Update',
    text: 'Your monthly report is ready for review.',
    timestamp: Date.now() - 3600000, // 1 hour ago
    style: NotificationStyle.BIG_TEXT,
    category: NotificationCategory.EMAIL,
    bigText: 'Your monthly report is ready for review. Please check the attached documents and provide feedback by end of week. This report includes Q4 metrics and annual projections.',
    actions: [
      {
        title: 'Open',
        allowsRemoteInput: false
      },
      {
        title: 'Archive',
        allowsRemoteInput: false
      }
    ],
    isGroupSummary: false,
    isOngoing: false,
    autoCancel: true,
    isLocalOnly: false,
    priority: 0,
    number: 1
  }
];

// Import the notifications
await NotificationReader.importNotifications({
  notifications: backupNotifications
});

console.log('Imported', backupNotifications.length, 'notifications');

// Verify the import
const { notifications } = await NotificationReader.getNotifications({ limit: 10 });
console.log('Total notifications in database:', notifications.length);
```

### Export and Import Pattern

```typescript
import { NotificationReader } from 'capacitor-notification-reader';

// Export notifications to JSON
const exportNotifications = async () => {
  const { notifications } = await NotificationReader.getNotifications({ limit: 1000 });
  const json = JSON.stringify(notifications, null, 2);
  
  // Save to file or send to server
  const blob = new Blob([json], { type: 'application/json' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `notifications-backup-${Date.now()}.json`;
  a.click();
  
  return notifications;
};

// Import notifications from JSON
const importNotifications = async (jsonString: string) => {
  const notifications = JSON.parse(jsonString);
  
  await NotificationReader.importNotifications({
    notifications
  });
  
  console.log('Successfully imported', notifications.length, 'notifications');
};

// Restore from backup
const restoreFromBackup = async (backupFile: File) => {
  const jsonString = await backupFile.text();
  await importNotifications(jsonString);
  
  console.log('Backup restored successfully');
};
```

### Merge Notifications (Avoid Duplicates)

```typescript
import { NotificationReader } from 'capacitor-notification-reader';

// Import notifications while avoiding duplicates
const mergeNotifications = async (newNotifications: NotificationItem[]) => {
  // Get existing notifications
  const { notifications: existing } = await NotificationReader.getNotifications({ 
    limit: 10000 
  });
  
  const existingIds = new Set(existing.map(n => n.id));
  
  // Filter out duplicates
  const uniqueNotifications = newNotifications.filter(n => !existingIds.has(n.id));
  
  if (uniqueNotifications.length > 0) {
    await NotificationReader.importNotifications({
      notifications: uniqueNotifications
    });
    console.log('Imported', uniqueNotifications.length, 'new notifications');
    console.log('Skipped', newNotifications.length - uniqueNotifications.length, 'duplicates');
  } else {
    console.log('No new notifications to import');
  }
};
```

### Clear and Reload

```typescript
import { NotificationReader } from 'capacitor-notification-reader';

// Clear all notifications and reload the list
const clearAndReload = async () => {
  await NotificationReader.deleteAllNotifications();
  
  // Reload notifications (should be empty)
  const { notifications } = await NotificationReader.getNotifications({
    limit: 20
  });
  
  console.log('Notifications after clear:', notifications.length); // Should be 0
};
```

## Type Guards for Runtime Checks

```typescript
// Style-based type guards (most reliable for unique styles)
function isBigTextNotification(notification: NotificationItem): notification is BigTextNotification {
  return notification.style === NotificationStyle.BIG_TEXT;
}

function isBigPictureNotification(notification: NotificationItem): notification is BigPictureNotification {
  return notification.style === NotificationStyle.BIG_PICTURE;
}

function isInboxNotification(notification: NotificationItem): notification is InboxNotification {
  return notification.style === NotificationStyle.INBOX;
}

function isMessagingNotification(notification: NotificationItem): notification is MessagingNotification {
  return notification.style === NotificationStyle.MESSAGING;
}

// Category-based type guards (for types without unique styles)
function isProgressNotification(notification: NotificationItem): notification is ProgressNotification {
  return notification.category === NotificationCategory.PROGRESS;
}

function isCallNotification(notification: NotificationItem): notification is CallNotification {
  return notification.category === NotificationCategory.CALL || 
         notification.category === NotificationCategory.MISSED_CALL;
}

function isMediaNotification(notification: NotificationItem): notification is MediaNotification {
  return notification.style === NotificationStyle.MEDIA || 
         notification.style === NotificationStyle.DECORATED_MEDIA;
}

// Usage examples
notifications.forEach(notification => {
  // Type guard automatically narrows the type
  if (isBigPictureNotification(notification)) {
    // TypeScript knows notification is BigPictureNotification
    if (notification.bigPicture) {
      displayImage(notification.bigPicture);
    }
  }
  
  if (isMessagingNotification(notification)) {
    // TypeScript knows notification is MessagingNotification
    notification.messages.forEach(msg => {
      console.log(`${msg.sender}: ${msg.text}`);
    });
  }
  
  if (isProgressNotification(notification)) {
    // TypeScript knows notification is ProgressNotification
    const percent = (notification.progress.current / notification.progress.max) * 100;
    updateProgressBar(notification.id, percent);
  }
});

// Combined type narrowing with inline checks
notifications.forEach(notification => {
  // Direct style check for unique styles
  if (notification.style === NotificationStyle.BIG_TEXT) {
    console.log('Expanded text:', notification.bigText);
  }
  
  // Category check for types without unique styles
  if (notification.category === NotificationCategory.PROGRESS) {
    console.log('Progress:', notification.progress);
  }
  
  // Combined check when needed
  if (notification.style === NotificationStyle.CALL || 
      notification.category === NotificationCategory.CALL) {
    console.log('Call from:', notification.callerName);
  }
});
```
