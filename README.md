# capacitor-notification-reader

[![npm version](https://badge.fury.io/js/capacitor-notification-reader.svg)](https://badge.fury.io/js/capacitor-notification-reader)
[![npm downloads](https://img.shields.io/npm/dm/capacitor-notification-reader.svg)](https://www.npmjs.com/package/capacitor-notification-reader)

Capacitor plugin to read active notifications on Android with persistent storage.

## Features

- **Read Active Notifications**: Access all currently active notifications from the notification drawer
- **Persistent Storage**: Notifications are automatically stored in a RoomDB database when posted
- **Background Collection**: Notifications are captured even when your app isn't running
- **Pagination Support**: Efficiently retrieve stored notifications with pagination
- **Comprehensive Type Safety**: Full TypeScript support with discriminated unions
- **Rich Notification Data**: Access notification styles, actions, icons, and metadata

## Install

```bash
npm install capacitor-notification-reader
npx cap sync
```

## How It Works

### Persistent Notification Storage

This plugin uses Android's NotificationListenerService combined with RoomDB to provide robust notification tracking:

1. **Notification Listener Service**: Once you grant notification access, the Android NotificationListenerService runs in the background and receives all notifications system-wide.

2. **Automatic Storage**: When a notification is posted, the service automatically:
   - Parses all notification data (title, text, style, actions, icons, etc.)
   - Stores it in a local RoomDB database
   - Notifies your app via the `notificationPosted` event listener (if the app is running)

3. **Background Operation**: The listener service operates independently of your app:
   - Notifications are captured even when your app is closed
   - Data persists across app restarts
   - No battery-intensive polling required

4. **Dual Access Methods**:
   - `getActiveNotifications()`: Returns currently visible notifications from the notification drawer
   - `getNotifications()`: Retrieves stored notifications from the database with pagination support

### Database Schema

The RoomDB database stores comprehensive notification data including:
- Primary key: UUID (automatically generated for each notification)
- Basic fields: app name (human-readable), package name, title, text, timestamp
- Icons: small icon, large icon, app icon (all as base64)
- Metadata: category, style, channel ID, group info, priority
- Style-specific data: big text, big picture, inbox lines, messaging conversations
- Action buttons with inline reply support
- Progress information for download/upload notifications
- Call-style specific data (caller name)

**Note:** When the NotificationListenerService first connects (e.g., when your app is installed or after device reboot), it automatically loads all currently active notifications into the database. This ensures you have a complete history from the moment the service starts.

## Usage Examples

### Listen for Real-time Notifications

```typescript
import { NotificationReader } from 'capacitor-notification-reader';

// Listen for notifications as they are posted
await NotificationReader.addListener('notificationPosted', (notification) => {
  console.log('New notification:', notification.title);
  console.log('Stored in DB with ID:', notification.id);
  // The notification is automatically saved to the database
});
```

### Retrieve Stored Notifications with Pagination

```typescript
import { NotificationReader } from 'capacitor-notification-reader';

// Get first batch (most recent notifications)
const { notifications } = await NotificationReader.getNotifications({
  limit: 20
});

// Get next batch using cursor (timestamp-based pagination)
if (notifications.length > 0) {
  const lastTimestamp = notifications[notifications.length - 1].timestamp;
  const { notifications: nextBatch } = await NotificationReader.getNotifications({
    cursor: lastTimestamp,
    limit: 20
  });
}
```

### Type-Safe Notification Handling

```typescript
import { NotificationStyle, NotificationCategory } from 'capacitor-notification-reader';

notifications.forEach((notification) => {
  // TypeScript automatically narrows the type
  switch (notification.style) {
    case NotificationStyle.MESSAGING:
      // notification is MessagingNotification
      console.log('Messages:', notification.messages);
      break;
    case NotificationStyle.BIG_PICTURE:
      // notification is BigPictureNotification
      console.log('Picture:', notification.bigPicture);
      break;
  }
});
```

### Clear Notification History

```typescript
import { NotificationReader } from 'capacitor-notification-reader';

// Delete all stored notifications from the database
await NotificationReader.deleteAllNotifications();
console.log('All notification history cleared');

// Note: This only clears the database, not the system notification drawer
```

### Import Notifications

```typescript
import { NotificationReader, NotificationStyle } from 'capacitor-notification-reader';

// Prepare notifications to import (e.g., from a backup or migration)
const notificationsToImport = [
  {
    id: 'notification-1',
    appName: 'WhatsApp',
    packageName: 'com.whatsapp',
    title: 'John Doe',
    text: 'Hey, how are you?',
    timestamp: Date.now() - 3600000, // 1 hour ago
    style: NotificationStyle.MESSAGING,
    category: 'msg',
    actions: [],
    isGroupSummary: false,
    isOngoing: false,
    autoCancel: true,
    isLocalOnly: false,
    priority: 0,
    number: 1,
    messages: [
      {
        text: 'Hey, how are you?',
        timestamp: Date.now() - 3600000,
        sender: 'John Doe'
      }
    ],
    conversationTitle: 'John Doe',
    isGroupConversation: false
  }
];

// Import notifications into the database
await NotificationReader.importNotifications({
  notifications: notificationsToImport
});
console.log('Notifications imported successfully');

// Verify import
const { notifications } = await NotificationReader.getNotifications({ limit: 10 });
console.log('Imported notifications:', notifications);
```

For more detailed examples, see [TYPE_USAGE_EXAMPLES.md](TYPE_USAGE_EXAMPLES.md).

## API

<docgen-index>

* [`getActiveNotifications()`](#getactivenotifications)
* [`openAccessSettings()`](#openaccesssettings)
* [`isAccessEnabled()`](#isaccessenabled)
* [`getNotifications(...)`](#getnotifications)
* [`deleteAllNotifications()`](#deleteallnotifications)
* [`importNotifications(...)`](#importnotifications)
* [`addListener('notificationPosted', ...)`](#addlistenernotificationposted-)
* [Interfaces](#interfaces)
* [Type Aliases](#type-aliases)
* [Enums](#enums)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### getActiveNotifications()

```typescript
getActiveNotifications() => Promise<GetActiveNotificationsResult>
```

Gets all active notifications from the notification listener service.

**Returns:** <code>Promise&lt;<a href="#getactivenotificationsresult">GetActiveNotificationsResult</a>&gt;</code>

**Since:** 1.0.0

--------------------


### openAccessSettings()

```typescript
openAccessSettings() => Promise<{ enabled: boolean; }>
```

Opens the system settings page to allow the user to grant notification access
to the app. The promise resolves when the user returns from settings with
the current permission status.

**Returns:** <code>Promise&lt;{ enabled: boolean; }&gt;</code>

**Since:** 1.0.0

--------------------


### isAccessEnabled()

```typescript
isAccessEnabled() => Promise<{ enabled: boolean; }>
```

Checks if the app has notification access enabled.

**Returns:** <code>Promise&lt;{ enabled: boolean; }&gt;</code>

**Since:** 1.0.0

--------------------


### getNotifications(...)

```typescript
getNotifications(options?: GetNotificationsOptions | undefined) => Promise<GetNotificationsResult>
```

Retrieves notifications from the persistent Room database with optional
filtering and cursor-based pagination. Notifications are cached when they are posted and can be
queried later even after dismissal from the notification drawer.

| Param         | Type                                                                        | Description                            |
| ------------- | --------------------------------------------------------------------------- | -------------------------------------- |
| **`options`** | <code><a href="#getnotificationsoptions">GetNotificationsOptions</a></code> | - Cursor, limit, and filtering options |

**Returns:** <code>Promise&lt;<a href="#getnotificationsresult">GetNotificationsResult</a>&gt;</code>

**Since:** 1.0.0

--------------------


### deleteAllNotifications()

```typescript
deleteAllNotifications() => Promise<void>
```

Deletes all notifications from the database.
This does not affect notifications in the system notification drawer.

**Since:** 1.0.0

--------------------


### importNotifications(...)

```typescript
importNotifications(options: ImportNotificationsOptions) => Promise<void>
```

Imports an array of notifications into the database.
This method is useful for restoring previously exported notifications,
migrating data from another source, or bulk-importing notification data.

Each notification will be inserted using REPLACE strategy, meaning if a
notification with the same ID already exists, it will be updated.

| Param         | Type                                                                              | Description                                              |
| ------------- | --------------------------------------------------------------------------------- | -------------------------------------------------------- |
| **`options`** | <code><a href="#importnotificationsoptions">ImportNotificationsOptions</a></code> | - Object containing the array of notifications to import |

**Since:** 1.0.0

--------------------


### addListener('notificationPosted', ...)

```typescript
addListener(eventName: 'notificationPosted', listenerFunc: (notification: NotificationItem) => void) => Promise<PluginListenerHandle>
```

Listen for notifications that are posted while the listener service is running.
Fires with the freshly-captured notification payload.

| Param              | Type                                                                                     |
| ------------------ | ---------------------------------------------------------------------------------------- |
| **`eventName`**    | <code>'notificationPosted'</code>                                                        |
| **`listenerFunc`** | <code>(notification: <a href="#notificationitem">NotificationItem</a>) =&gt; void</code> |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt;</code>

--------------------


### Interfaces


#### GetActiveNotificationsResult

Result returned by getActiveNotifications.

| Prop                | Type                            | Description                                              |
| ------------------- | ------------------------------- | -------------------------------------------------------- |
| **`notifications`** | <code>NotificationItem[]</code> | Array of active notifications with type-specific shapes. |


#### BigTextNotification

Big text style notification with expanded text content

| Prop          | Type                                                                     | Description                      |
| ------------- | ------------------------------------------------------------------------ | -------------------------------- |
| **`style`**   | <code><a href="#notificationstyle">NotificationStyle.BIG_TEXT</a></code> | Notification style template used |
| **`bigText`** | <code>string</code>                                                      | The full expanded text content   |


#### BigPictureNotification

Big picture style notification with an image

| Prop                            | Type                                                                        | Description                                   |
| ------------------------------- | --------------------------------------------------------------------------- | --------------------------------------------- |
| **`style`**                     | <code><a href="#notificationstyle">NotificationStyle.BIG_PICTURE</a></code> | Notification style template used              |
| **`bigPicture`**                | <code>string</code>                                                         | Base64-encoded picture shown in expanded view |
| **`pictureContentDescription`** | <code>string</code>                                                         | Content description for the picture           |


#### InboxNotification

Inbox style notification with multiple lines

| Prop             | Type                                                                  | Description                      |
| ---------------- | --------------------------------------------------------------------- | -------------------------------- |
| **`style`**      | <code><a href="#notificationstyle">NotificationStyle.INBOX</a></code> | Notification style template used |
| **`inboxLines`** | <code>string[]</code>                                                 | Array of text lines in the inbox |


#### MessagingNotification

Messaging style notification for chat/messaging apps

| Prop                      | Type                                                                          | Description                                        |
| ------------------------- | ----------------------------------------------------------------------------- | -------------------------------------------------- |
| **`style`**               | <code><a href="#notificationstyle">NotificationStyle.MESSAGING</a></code>     | Notification style template used                   |
| **`category`**            | <code><a href="#notificationcategory">NotificationCategory.MESSAGE</a></code> | Notification category (call, message, email, etc.) |
| **`conversationTitle`**   | <code>string</code>                                                           | Conversation title for group chats                 |
| **`isGroupConversation`** | <code>boolean</code>                                                          | Whether this is a group conversation               |
| **`messages`**            | <code>NotificationMessage[]</code>                                            | Array of messages in the conversation              |


#### NotificationMessage

Message in a messaging-style notification

| Prop            | Type                | Description              |
| --------------- | ------------------- | ------------------------ |
| **`text`**      | <code>string</code> | Message text             |
| **`timestamp`** | <code>number</code> | Timestamp of the message |
| **`sender`**    | <code>string</code> | Sender name              |


#### ProgressNotification

Progress style notification for downloads, uploads, etc.

| Prop           | Type                                                                           | Description                                        |
| -------------- | ------------------------------------------------------------------------------ | -------------------------------------------------- |
| **`style`**    | <code><a href="#notificationstyle">NotificationStyle.DEFAULT</a></code>        | Notification style template used                   |
| **`category`** | <code><a href="#notificationcategory">NotificationCategory.PROGRESS</a></code> | Notification category (call, message, email, etc.) |
| **`progress`** | <code><a href="#notificationprogress">NotificationProgress</a></code>          | Progress information                               |


#### NotificationProgress

Progress information for notifications with progress bars

| Prop                | Type                 | Description                           |
| ------------------- | -------------------- | ------------------------------------- |
| **`current`**       | <code>number</code>  | Current progress value                |
| **`max`**           | <code>number</code>  | Maximum progress value                |
| **`indeterminate`** | <code>boolean</code> | Whether the progress is indeterminate |


#### CallNotification

Call notification

| Prop             | Type                                                                                                                                               | Description                                        |
| ---------------- | -------------------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------- |
| **`style`**      | <code><a href="#notificationstyle">NotificationStyle.CALL</a> \| <a href="#notificationstyle">NotificationStyle.DEFAULT</a></code>                 | Notification style template used                   |
| **`category`**   | <code><a href="#notificationcategory">NotificationCategory.CALL</a> \| <a href="#notificationcategory">NotificationCategory.MISSED_CALL</a></code> | Notification category (call, message, email, etc.) |
| **`callerName`** | <code>string</code>                                                                                                                                | Caller name                                        |


#### MediaNotification

Media playback notification

| Prop           | Type                                                                                                                                        | Description                                        |
| -------------- | ------------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------- |
| **`style`**    | <code><a href="#notificationstyle">NotificationStyle.MEDIA</a> \| <a href="#notificationstyle">NotificationStyle.DECORATED_MEDIA</a></code> | Notification style template used                   |
| **`category`** | <code><a href="#notificationcategory">NotificationCategory.TRANSPORT</a></code>                                                             | Notification category (call, message, email, etc.) |


#### GenericNotification

Generic notification that doesn't fit specific patterns

| Prop        | Type                                                                                                                                           | Description                      |
| ----------- | ---------------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------- |
| **`style`** | <code><a href="#notificationstyle">NotificationStyle.DECORATED_CUSTOM</a> \| <a href="#notificationstyle">NotificationStyle.DEFAULT</a></code> | Notification style template used |


#### GetNotificationsResult

Result returned by getNotifications.

| Prop                | Type                            | Description                               |
| ------------------- | ------------------------------- | ----------------------------------------- |
| **`notifications`** | <code>NotificationItem[]</code> | Array of notifications from the database. |


#### GetNotificationsOptions

Options for getNotifications.

| Prop         | Type                                                              | Description                                                                                                                                                 | Default         |
| ------------ | ----------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------- |
| **`cursor`** | <code>number</code>                                               | Return notifications whose timestamp is strictly less than this value (in ms). Use the `timestamp` from the last item of the previous page when paginating. |                 |
| **`limit`**  | <code>number</code>                                               | Maximum number of notifications to retrieve.                                                                                                                | <code>10</code> |
| **`filter`** | <code><a href="#notificationfilter">NotificationFilter</a></code> | Optional filter criteria applied on the stored notifications.                                                                                               |                 |


#### NotificationFilter

Advanced filters for querying stored notifications.
Each filter is optional and multiple filters are combined with AND logic.

| Prop                           | Type                  | Description                                                                          |
| ------------------------------ | --------------------- | ------------------------------------------------------------------------------------ |
| **`textContains`**             | <code>string</code>   | Match notifications whose text contains the provided value (case-sensitive).         |
| **`titleContains`**            | <code>string</code>   | Match notifications whose title contains the provided value (case-sensitive).        |
| **`textContainsInsensitive`**  | <code>string</code>   | Match notifications whose text contains the provided value (case-insensitive).       |
| **`titleContainsInsensitive`** | <code>string</code>   | Match notifications whose title contains the provided value (case-insensitive).      |
| **`appNames`**                 | <code>string[]</code> | Only return notifications whose `appName` exactly matches one of the supplied names. |
| **`packageName`**              | <code>string</code>   | Filter by package name of the posting application.                                   |
| **`category`**                 | <code>string</code>   | Filter by notification category.                                                     |
| **`style`**                    | <code>string</code>   | Filter by notification style template.                                               |
| **`isOngoing`**                | <code>boolean</code>  | Filter for ongoing (non-dismissible) notifications only.                             |
| **`isGroupSummary`**           | <code>boolean</code>  | Filter for group summary notifications only.                                         |
| **`channelId`**                | <code>string</code>   | Filter by notification channel ID (Android 8+).                                      |


#### ImportNotificationsOptions

Options for importNotifications.

| Prop                | Type                            | Description                                                                                                                                                       |
| ------------------- | ------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **`notifications`** | <code>NotificationItem[]</code> | Array of notification items to import into the database. Each notification should conform to the <a href="#notificationitem">NotificationItem</a> type structure. |


#### PluginListenerHandle

| Prop         | Type                                      |
| ------------ | ----------------------------------------- |
| **`remove`** | <code>() =&gt; Promise&lt;void&gt;</code> |


### Type Aliases


#### NotificationItem

Union type of all specific notification types.
Use discriminated union on 'style' and 'category' for type narrowing.

Type narrowing examples:
- For <a href="#bigtextnotification">BigTextNotification</a>: check `notification.style === <a href="#notificationstyle">NotificationStyle</a>.BIG_TEXT`
- For <a href="#bigpicturenotification">BigPictureNotification</a>: check `notification.style === <a href="#notificationstyle">NotificationStyle</a>.BIG_PICTURE`
- For <a href="#inboxnotification">InboxNotification</a>: check `notification.style === <a href="#notificationstyle">NotificationStyle</a>.INBOX`
- For <a href="#messagingnotification">MessagingNotification</a>: check `notification.style === <a href="#notificationstyle">NotificationStyle</a>.MESSAGING`
- For <a href="#progressnotification">ProgressNotification</a>: check `notification.category === <a href="#notificationcategory">NotificationCategory</a>.PROGRESS`
- For <a href="#callnotification">CallNotification</a>: check category is CALL or MISSED_CALL
- For <a href="#medianotification">MediaNotification</a>: check style is MEDIA or DECORATED_MEDIA

<code><a href="#bigtextnotification">BigTextNotification</a> | <a href="#bigpicturenotification">BigPictureNotification</a> | <a href="#inboxnotification">InboxNotification</a> | <a href="#messagingnotification">MessagingNotification</a> | <a href="#progressnotification">ProgressNotification</a> | <a href="#callnotification">CallNotification</a> | <a href="#medianotification">MediaNotification</a> | <a href="#genericnotification">GenericNotification</a></code>


### Enums


#### NotificationStyle

| Members                | Value                                        |
| ---------------------- | -------------------------------------------- |
| **`BIG_TEXT`**         | <code>'BigTextStyle'</code>                  |
| **`BIG_PICTURE`**      | <code>'BigPictureStyle'</code>               |
| **`INBOX`**            | <code>'InboxStyle'</code>                    |
| **`MESSAGING`**        | <code>'MessagingStyle'</code>                |
| **`MEDIA`**            | <code>'MediaStyle'</code>                    |
| **`CALL`**             | <code>'CallStyle'</code>                     |
| **`DECORATED_CUSTOM`** | <code>'DecoratedCustomViewStyle'</code>      |
| **`DECORATED_MEDIA`**  | <code>'DecoratedMediaCustomViewStyle'</code> |
| **`DEFAULT`**          | <code>'default'</code>                       |


#### NotificationCategory

| Members                | Value                           |
| ---------------------- | ------------------------------- |
| **`ALARM`**            | <code>'alarm'</code>            |
| **`CALL`**             | <code>'call'</code>             |
| **`EMAIL`**            | <code>'email'</code>            |
| **`ERROR`**            | <code>'err'</code>              |
| **`EVENT`**            | <code>'event'</code>            |
| **`LOCATION_SHARING`** | <code>'location_sharing'</code> |
| **`MESSAGE`**          | <code>'msg'</code>              |
| **`MISSED_CALL`**      | <code>'missed_call'</code>      |
| **`NAVIGATION`**       | <code>'navigation'</code>       |
| **`PROGRESS`**         | <code>'progress'</code>         |
| **`PROMO`**            | <code>'promo'</code>            |
| **`RECOMMENDATION`**   | <code>'recommendation'</code>   |
| **`REMINDER`**         | <code>'reminder'</code>         |
| **`SERVICE`**          | <code>'service'</code>          |
| **`SOCIAL`**           | <code>'social'</code>           |
| **`STATUS`**           | <code>'status'</code>           |
| **`STOPWATCH`**        | <code>'stopwatch'</code>        |
| **`SYSTEM`**           | <code>'sys'</code>              |
| **`TRANSPORT`**        | <code>'transport'</code>        |
| **`VOICEMAIL`**        | <code>'voicemail'</code>        |
| **`WORKOUT`**          | <code>'workout'</code>          |
| **`UNKNOWN`**          | <code>'unknown'</code>          |

</docgen-api>

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for details on how to contribute to this project.

## Release Process

This package uses [npm Trusted Publishers](https://docs.npmjs.com/trusted-publishers) with OIDC for secure, automated releases. Releases are triggered automatically when commits are pushed to the `main` branch.

### Setup (One-time)

Configure the trusted publisher on npm:

1. Go to your package settings on [npmjs.com](https://www.npmjs.com/package/capacitor-notification-reader)
2. Navigate to "Publishing access" → "Trusted Publisher"
3. Select "GitHub Actions"
4. Configure:
   - **Organization/User**: `WhyAsh5114`
   - **Repository**: `capacitor-notification-reader`
   - **Workflow filename**: `release.yml`

No npm tokens needed! The workflow uses OpenID Connect (OIDC) for authentication.

### Commit Convention

Follow [Conventional Commits](https://www.conventionalcommits.org/) for your commit messages:

- `feat:` - New features (triggers minor version bump)
- `fix:` - Bug fixes (triggers patch version bump)
- `docs:` - Documentation changes
- `chore:` - Maintenance tasks
- `BREAKING CHANGE:` - Breaking changes (triggers major version bump)

Example:
```bash
git commit -m "feat: add support for notification icons"
git commit -m "fix: resolve crash when notification has no title"
```

The CI pipeline will automatically:
1. Build the package
2. Determine version bump based on commits
3. Create a GitHub release with changelog
4. Publish to npm with provenance attestation
