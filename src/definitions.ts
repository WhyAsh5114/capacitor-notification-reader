/**
 * Represents a notification item.
 */
export interface NotificationItem {
  /**
   * The package name of the app that posted the notification.
   */
  app: string;
  /**
   * The title of the notification.
   */
  title: string | null;
  /**
   * The text content of the notification.
   */
  text: string | null;
  /**
   * The timestamp when the notification was posted (in milliseconds).
   */
  timestamp: number;
}

/**
 * Result returned by getActiveNotifications.
 */
export interface GetActiveNotificationsResult {
  /**
   * Array of active notifications.
   */
  notifications: NotificationItem[];
}

export interface NotificationReaderPlugin {
  /**
   * Gets all active notifications from the notification listener service.
   *
   * @returns Promise resolving with the list of active notifications
   * @throws Error if notification listener service is not connected or permission is not granted
   * @since 1.0.0
   * @platform Android
   */
  getActiveNotifications(): Promise<GetActiveNotificationsResult>;

  /**
   * Opens the system settings page to allow the user to grant notification access
   * to the app.
   *
   * @returns Promise that resolves when settings are opened
   * @throws Error if unable to open settings
   * @since 1.0.0
   * @platform Android
   */
  openAccessSettings(): Promise<void>;

  /**
   * Checks if the app has notification access enabled.
   *
   * @returns Promise resolving with an object indicating if access is enabled
   * @since 1.0.0
   * @platform Android
   */
  isAccessEnabled(): Promise<{ enabled: boolean }>;
}
