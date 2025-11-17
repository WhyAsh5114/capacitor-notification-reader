## Capacitor Notification Reader Example App

This example app demonstrates the capacitor-notification-reader plugin capabilities, including:
- Reading active notifications from the notification drawer
- Accessing stored notifications from the RoomDB database
- Real-time notification listeners
- Pagination through notification history

### Features Demonstrated

1. **Persistent Storage**: Notifications are automatically saved to a local database even when the app is closed
2. **Background Collection**: The NotificationListenerService runs in the background and captures all notifications
3. **Rich Data Access**: View notification titles, text, icons, actions, and style-specific data
4. **Type-Safe API**: Full TypeScript support with discriminated unions

### Running this example

To run the provided example, you can use `npm start` command.

```bash
npm start
```

### Testing the Plugin

1. Grant notification access when prompted
2. Generate some notifications from other apps
3. Close the example app completely
4. Generate more notifications
5. Reopen the app and use `getNotifications()` to see all captured notifications, including those from when the app was closed
