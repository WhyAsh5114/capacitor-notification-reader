# Type-Safe Notification Usage Examples

This plugin provides comprehensive type safety through discriminated unions. TypeScript will automatically narrow the notification type based on the `style` and `category` fields.

## Basic Usage

```typescript
import { NotificationReader, NotificationItem, NotificationStyle, NotificationCategory } from 'capacitor-notification-reader';

const { notifications } = await NotificationReader.getActiveNotifications();

notifications.forEach((notification) => {
  console.log(`${notification.app}: ${notification.title}`);
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
              alt={notification.app}
              className="app-icon"
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

## Grouping Notifications

```typescript
// Group notifications by app
const groupedByApp = notifications.reduce((acc, notification) => {
  if (!acc[notification.app]) {
    acc[notification.app] = [];
  }
  acc[notification.app].push(notification);
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

## Type Guards for Runtime Checks

```typescript
function isMessagingNotification(notification: NotificationItem): notification is MessagingNotification {
  return notification.style === NotificationStyle.MESSAGING;
}

function hasBigPicture(notification: NotificationItem): notification is BigPictureNotification {
  return notification.style === NotificationStyle.BIG_PICTURE;
}

function hasProgress(notification: NotificationItem): notification is ProgressNotification {
  return notification.category === NotificationCategory.PROGRESS && 'progress' in notification;
}

// Usage
notifications.forEach(notification => {
  if (isMessagingNotification(notification)) {
    // TypeScript knows this is a MessagingNotification
    console.log(notification.messages);
  }
});
```
