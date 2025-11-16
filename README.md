# capacitor-notification-reader

[![npm version](https://badge.fury.io/js/capacitor-notification-reader.svg)](https://badge.fury.io/js/capacitor-notification-reader)
[![npm downloads](https://img.shields.io/npm/dm/capacitor-notification-reader.svg)](https://www.npmjs.com/package/capacitor-notification-reader)

Capacitor plugin to read active notifications on Android

## Install

```bash
npm install capacitor-notification-reader
npx cap sync
```

## API

<docgen-index>

* [`getActiveNotifications()`](#getactivenotifications)
* [`openAccessSettings()`](#openaccesssettings)
* [`isAccessEnabled()`](#isaccessenabled)
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
| **`bigText`** | <code>string \| null</code>                                              | The full expanded text content   |


#### BigPictureNotification

Big picture style notification with an image

| Prop                            | Type                                                                        | Description                                   |
| ------------------------------- | --------------------------------------------------------------------------- | --------------------------------------------- |
| **`style`**                     | <code><a href="#notificationstyle">NotificationStyle.BIG_PICTURE</a></code> | Notification style template used              |
| **`bigPicture`**                | <code>string \| null</code>                                                 | Base64-encoded picture shown in expanded view |
| **`pictureContentDescription`** | <code>string \| null</code>                                                 | Content description for the picture           |


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
| **`conversationTitle`**   | <code>string \| null</code>                                                   | Conversation title for group chats                 |
| **`isGroupConversation`** | <code>boolean</code>                                                          | Whether this is a group conversation               |
| **`messages`**            | <code>NotificationMessage[]</code>                                            | Array of messages in the conversation              |


#### NotificationMessage

Message in a messaging-style notification

| Prop            | Type                        | Description              |
| --------------- | --------------------------- | ------------------------ |
| **`text`**      | <code>string</code>         | Message text             |
| **`timestamp`** | <code>number</code>         | Timestamp of the message |
| **`sender`**    | <code>string \| null</code> | Sender name              |


#### ProgressNotification

Progress style notification for downloads, uploads, etc.

| Prop           | Type                                                                           | Description                                        |
| -------------- | ------------------------------------------------------------------------------ | -------------------------------------------------- |
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
| **`category`**   | <code><a href="#notificationcategory">NotificationCategory.CALL</a> \| <a href="#notificationcategory">NotificationCategory.MISSED_CALL</a></code> | Notification category (call, message, email, etc.) |
| **`callerName`** | <code>string \| null</code>                                                                                                                        | Caller name                                        |


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


### Type Aliases


#### NotificationItem

Union type of all specific notification types.
Use discriminated union on 'style' and 'category' for type narrowing.

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
