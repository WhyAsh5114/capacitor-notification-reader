# capacitor-notification-reader

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
openAccessSettings() => Promise<void>
```

Opens the system settings page to allow the user to grant notification access
to the app.

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

| Prop                | Type                            | Description                    |
| ------------------- | ------------------------------- | ------------------------------ |
| **`notifications`** | <code>NotificationItem[]</code> | Array of active notifications. |


#### NotificationItem

Represents a notification item.

| Prop            | Type                        | Description                                                       |
| --------------- | --------------------------- | ----------------------------------------------------------------- |
| **`app`**       | <code>string</code>         | The package name of the app that posted the notification.         |
| **`title`**     | <code>string \| null</code> | The title of the notification.                                    |
| **`text`**      | <code>string \| null</code> | The text content of the notification.                             |
| **`timestamp`** | <code>number</code>         | The timestamp when the notification was posted (in milliseconds). |

</docgen-api>
