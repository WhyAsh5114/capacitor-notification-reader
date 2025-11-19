# Type Discrimination Guide

## Understanding the Type System

This plugin uses **discriminated unions** to provide type-safe access to different notification types. However, the discrimination strategy differs based on how Android provides notification data.

## Why Not All Types Have Unique Styles

Android's notification system determines the "style" based on the `Notification.EXTRA_TEMPLATE` field, which represents the visual template used to display the notification. The key insight is:

**Some notification types share the same visual template (style) but differ in purpose (category).**

### Styles Directly from Android

These notification types have **unique, dedicated style templates** in Android:

| TypeScript Type | Style Value | Android Template |
|----------------|-------------|------------------|
| `BigTextNotification` | `BIG_TEXT` | `BigTextStyle` |
| `BigPictureNotification` | `BIG_PICTURE` | `BigPictureStyle` |
| `InboxNotification` | `INBOX` | `InboxStyle` |
| `MessagingNotification` | `MESSAGING` | `MessagingStyle` |
| `MediaNotification` | `MEDIA` or `DECORATED_MEDIA` | `MediaStyle` or `DecoratedMediaCustomViewStyle` |
| `CallNotification` | `CALL` or `DEFAULT` | `CallStyle` (Android 12+) or default |

### Types Determined by Category + Data

These notification types use the **default style** but are distinguished by their **category** and specific data fields:

| TypeScript Type | Category | Special Fields |
|----------------|----------|----------------|
| `ProgressNotification` | `PROGRESS` | `progress` object with current/max/indeterminate |
| `CallNotification` | `CALL` or `MISSED_CALL` | `callerName` |

## Type Narrowing Strategies

### Strategy 1: Check Style (For Unique Templates)

✅ **Recommended for**: BigText, BigPicture, Inbox, Messaging, Media notifications

```typescript
if (notification.style === NotificationStyle.BIG_TEXT) {
  // ✅ TypeScript correctly narrows to BigTextNotification
  console.log(notification.bigText);
}

if (notification.style === NotificationStyle.BIG_PICTURE) {
  // ✅ TypeScript correctly narrows to BigPictureNotification
  if (notification.bigPicture) {
    displayImage(notification.bigPicture);
  }
}

if (notification.style === NotificationStyle.MESSAGING) {
  // ✅ TypeScript correctly narrows to MessagingNotification
  notification.messages.forEach(msg => console.log(msg.text));
}
```

### Strategy 2: Check Category (For Shared Styles)

✅ **Recommended for**: Progress, Call notifications

```typescript
if (notification.category === NotificationCategory.PROGRESS) {
  // ✅ TypeScript correctly narrows to ProgressNotification
  const percent = (notification.progress.current / notification.progress.max) * 100;
  console.log(`${percent}%`);
}

if (notification.category === NotificationCategory.CALL || 
    notification.category === NotificationCategory.MISSED_CALL) {
  // ✅ TypeScript correctly narrows to CallNotification
  console.log('Call from:', notification.callerName);
}
```

### Strategy 3: Combined Checks (Most Robust)

✅ **Recommended for**: Maximum type safety and handling edge cases

```typescript
// For calls, check both style and category
if ((notification.style === NotificationStyle.CALL || 
     notification.category === NotificationCategory.CALL ||
     notification.category === NotificationCategory.MISSED_CALL)) {
  console.log('Call notification:', notification.callerName);
}
```

## Why the Initial Problem Occurred

The original issue was:

```typescript
notification.style === NotificationStyle.BIG_PICTURE
// ❌ Returned: BigPictureNotification | ProgressNotification | CallNotification
```

**Root cause**: `ProgressNotification` and `CallNotification` didn't specify a `style` property, so they inherited `style: NotificationStyle` from `BaseNotification`. TypeScript couldn't rule them out because technically they could have ANY style value, including `BIG_PICTURE`.

**Fix applied**: We explicitly specified the `style` property for each type:

```typescript
// Before (problematic)
export interface ProgressNotification extends BaseNotification {
  category: NotificationCategory.PROGRESS;
  progress: NotificationProgress;
}

// After (fixed)
export interface ProgressNotification extends BaseNotification {
  style: NotificationStyle.DEFAULT;  // ✅ Explicitly set
  category: NotificationCategory.PROGRESS;
  progress: NotificationProgress;
}
```

Now TypeScript knows that:
- `BigPictureNotification` has `style: NotificationStyle.BIG_PICTURE`
- `ProgressNotification` has `style: NotificationStyle.DEFAULT`
- These are **mutually exclusive**, so checking `style === BIG_PICTURE` correctly narrows to only `BigPictureNotification`

## Best Practices

### 1. Use Style Checks When Available

For notifications with unique styles, prefer style-based discrimination:

```typescript
// ✅ Good - clean and simple
if (notification.style === NotificationStyle.BIG_PICTURE) {
  displayBigPicture(notification);
}
```

### 2. Use Category Checks for Non-Unique Styles

For notifications without unique styles, use category-based discrimination:

```typescript
// ✅ Good - appropriate for progress notifications
if (notification.category === NotificationCategory.PROGRESS) {
  updateProgressBar(notification);
}
```

### 3. Create Type Guards for Reusability

```typescript
function isBigPictureNotification(n: NotificationItem): n is BigPictureNotification {
  return n.style === NotificationStyle.BIG_PICTURE;
}

function isProgressNotification(n: NotificationItem): n is ProgressNotification {
  return n.category === NotificationCategory.PROGRESS;
}

// ✅ Usage
if (isBigPictureNotification(notification)) {
  // TypeScript knows this is BigPictureNotification
  displayImage(notification.bigPicture);
}
```

### 4. Use Switch Statements for Multiple Types

```typescript
// ✅ Handles multiple notification types cleanly
switch (notification.style) {
  case NotificationStyle.BIG_TEXT:
    console.log('Text:', notification.bigText);
    break;
    
  case NotificationStyle.BIG_PICTURE:
    displayImage(notification.bigPicture);
    break;
    
  case NotificationStyle.MESSAGING:
    displayMessages(notification.messages);
    break;
    
  case NotificationStyle.INBOX:
    displayInbox(notification.inboxLines);
    break;
    
  default:
    // Handle generic or other notifications
    if (notification.category === NotificationCategory.PROGRESS) {
      updateProgress(notification.progress);
    }
}
```

## Summary

**The type system is designed to match Android's notification architecture:**

- **Style-based discrimination** works for notifications with unique visual templates
- **Category-based discrimination** works for notifications that share templates but differ in purpose
- **Combined checks** provide the most robust type safety

This hybrid approach gives you:
- ✅ Strong type safety
- ✅ Alignment with Android's actual notification system
- ✅ Clear, predictable type narrowing
- ✅ Flexibility to handle all notification types appropriately
