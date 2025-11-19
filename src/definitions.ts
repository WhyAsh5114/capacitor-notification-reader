import { Plugin } from '@capacitor/core';

/**
 * Android notification categories
 */
export enum NotificationCategory {
  ALARM = 'alarm',
  CALL = 'call',
  EMAIL = 'email',
  ERROR = 'err',
  EVENT = 'event',
  LOCATION_SHARING = 'location_sharing',
  MESSAGE = 'msg',
  MISSED_CALL = 'missed_call',
  NAVIGATION = 'navigation',
  PROGRESS = 'progress',
  PROMO = 'promo',
  RECOMMENDATION = 'recommendation',
  REMINDER = 'reminder',
  SERVICE = 'service',
  SOCIAL = 'social',
  STATUS = 'status',
  STOPWATCH = 'stopwatch',
  SYSTEM = 'sys',
  TRANSPORT = 'transport',
  VOICEMAIL = 'voicemail',
  WORKOUT = 'workout',
  UNKNOWN = 'unknown',
}

/**
 * Android notification styles
 */
export enum NotificationStyle {
  BIG_TEXT = 'BigTextStyle',
  BIG_PICTURE = 'BigPictureStyle',
  INBOX = 'InboxStyle',
  MESSAGING = 'MessagingStyle',
  MEDIA = 'MediaStyle',
  CALL = 'CallStyle',
  DECORATED_CUSTOM = 'DecoratedCustomViewStyle',
  DECORATED_MEDIA = 'DecoratedMediaCustomViewStyle',
  DEFAULT = 'default',
}

/**
 * Notification action button
 */
export interface NotificationAction {
  /**
   * Action title/label
   */
  title: string;
  /**
   * Base64-encoded icon for the action (if available)
   */
  icon?: string;
  /**
   * Whether this action allows remote input (for inline replies)
   */
  allowsRemoteInput: boolean;
}

/**
 * Progress information for notifications with progress bars
 */
export interface NotificationProgress {
  /**
   * Current progress value
   */
  current: number;
  /**
   * Maximum progress value
   */
  max: number;
  /**
   * Whether the progress is indeterminate
   */
  indeterminate: boolean;
}

/**
 * Message in a messaging-style notification
 */
export interface NotificationMessage {
  /**
   * Message text
   */
  text: string;
  /**
   * Timestamp of the message
   */
  timestamp: number;
  /**
   * Sender name
   */
  sender?: string;
}

/**
 * Base notification properties common to all notifications
 */
export interface BaseNotification {
  /**
   * The unique database ID of the notification (UUID).
   */
  id: string;
  /**
   * The human-readable name of the app that posted the notification.
   */
  appName: string;
  /**
   * The package name of the app that posted the notification.
   */
  packageName: string;
  /**
   * The title of the notification.
   */
  title?: string;
  /**
   * The text content of the notification.
   */
  text?: string;
  /**
   * The timestamp when the notification was posted (in milliseconds).
   */
  timestamp: number;
  /**
   * Base64-encoded PNG of the notification's small icon (status bar icon).
   */
  smallIcon?: string;
  /**
   * Base64-encoded PNG of the notification's large icon.
   */
  largeIcon?: string;
  /**
   * Base64-encoded PNG of the app's launcher icon.
   */
  appIcon?: string;
  /**
   * Notification category (call, message, email, etc.)
   */
  category?: string;
  /**
   * Notification style template used
   */
  style: NotificationStyle;
  /**
   * Sub-text shown below the main text
   */
  subText?: string;
  /**
   * Additional info text
   */
  infoText?: string;
  /**
   * Summary text for expanded notifications
   */
  summaryText?: string;
  /**
   * Group key for grouped notifications
   */
  group?: string;
  /**
   * Whether this is a group summary notification
   */
  isGroupSummary: boolean;
  /**
   * Notification channel ID (Android 8+)
   */
  channelId?: string;
  /**
   * Action buttons available on the notification (always an array, may be empty)
   */
  actions: NotificationAction[];
  /**
   * Whether the notification is ongoing (can't be dismissed)
   */
  isOngoing: boolean;
  /**
   * Whether the notification auto-cancels when clicked
   */
  autoCancel: boolean;
  /**
   * Whether the notification is local only (doesn't bridge to other devices)
   */
  isLocalOnly: boolean;
  /**
   * Priority level (-2 to 2, where 0 is default)
   */
  priority: number;
  /**
   * Number badge (e.g., unread count)
   */
  number: number;
}

/**
 * Big text style notification with expanded text content
 */
export interface BigTextNotification extends BaseNotification {
  style: NotificationStyle.BIG_TEXT;
  /**
   * The full expanded text content
   */
  bigText?: string;
}

/**
 * Big picture style notification with an image
 */
export interface BigPictureNotification extends BaseNotification {
  style: NotificationStyle.BIG_PICTURE;
  /**
   * Base64-encoded picture shown in expanded view
   */
  bigPicture?: string;
  /**
   * Content description for the picture
   */
  pictureContentDescription?: string;
}

/**
 * Inbox style notification with multiple lines
 */
export interface InboxNotification extends BaseNotification {
  style: NotificationStyle.INBOX;
  /**
   * Array of text lines in the inbox
   */
  inboxLines: string[];
}

/**
 * Messaging style notification for chat/messaging apps
 */
export interface MessagingNotification extends BaseNotification {
  style: NotificationStyle.MESSAGING;
  category: NotificationCategory.MESSAGE;
  /**
   * Conversation title for group chats
   */
  conversationTitle?: string;
  /**
   * Whether this is a group conversation
   */
  isGroupConversation: boolean;
  /**
   * Array of messages in the conversation
   */
  messages: NotificationMessage[];
}

/**
 * Progress style notification for downloads, uploads, etc.
 */
export interface ProgressNotification extends BaseNotification {
  style: NotificationStyle.DEFAULT;
  category: NotificationCategory.PROGRESS;
  /**
   * Progress information
   */
  progress: NotificationProgress;
}

/**
 * Call notification
 */
export interface CallNotification extends BaseNotification {
  style: NotificationStyle.CALL | NotificationStyle.DEFAULT;
  category: NotificationCategory.CALL | NotificationCategory.MISSED_CALL;
  /**
   * Caller name
   */
  callerName?: string;
}

/**
 * Media playback notification
 */
export interface MediaNotification extends BaseNotification {
  style: NotificationStyle.MEDIA | NotificationStyle.DECORATED_MEDIA;
  category: NotificationCategory.TRANSPORT;
}

/**
 * Generic notification that doesn't fit specific patterns
 */
export interface GenericNotification extends BaseNotification {
  style: NotificationStyle.DEFAULT | NotificationStyle.DECORATED_CUSTOM;
}

/**
 * Union type of all specific notification types.
 * Use discriminated union on 'style' and 'category' for type narrowing.
 * 
 * Type narrowing examples:
 * - For BigTextNotification: check `notification.style === NotificationStyle.BIG_TEXT`
 * - For BigPictureNotification: check `notification.style === NotificationStyle.BIG_PICTURE`
 * - For InboxNotification: check `notification.style === NotificationStyle.INBOX`
 * - For MessagingNotification: check `notification.style === NotificationStyle.MESSAGING`
 * - For ProgressNotification: check `notification.category === NotificationCategory.PROGRESS`
 * - For CallNotification: check category is CALL or MISSED_CALL
 * - For MediaNotification: check style is MEDIA or DECORATED_MEDIA
 */
export type NotificationItem =
  | BigTextNotification
  | BigPictureNotification
  | InboxNotification
  | MessagingNotification
  | ProgressNotification
  | CallNotification
  | MediaNotification
  | GenericNotification;

/**
 * Result returned by getActiveNotifications.
 */
export interface GetActiveNotificationsResult {
  /**
   * Array of active notifications with type-specific shapes.
   */
  notifications: NotificationItem[];
}

/**
 * Options for getNotifications.
 */
export interface GetNotificationsOptions {
  /**
   * Retrieve notifications with timestamps before this value (cursor-based pagination).
   * Use the timestamp of the last notification from the previous batch to get the next batch.
   * If not provided, returns the most recent notifications.
   */
  cursor?: number;
  /**
   * Maximum number of notifications to retrieve.
   * @default 10
   */
  limit?: number;
}

/**
 * Result returned by getNotifications.
 */
export interface GetNotificationsResult {
  /**
   * Array of notifications from the database.
   */
  notifications: NotificationItem[];
}

/**
 * Options for importNotifications.
 */
export interface ImportNotificationsOptions {
  /**
   * Array of notification items to import into the database.
   * Each notification should conform to the NotificationItem type structure.
   */
  notifications: NotificationItem[];
}

export interface NotificationReaderPlugin extends Plugin {
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
   * to the app. The promise resolves when the user returns from settings with
   * the current permission status.
   *
   * @returns Promise resolving with an object indicating if permission was granted
   * @throws Error if unable to open settings
   * @since 1.0.0
   * @platform Android
   */
  openAccessSettings(): Promise<{ enabled: boolean }>;

  /**
   * Checks if the app has notification access enabled.
   *
   * @returns Promise resolving with an object indicating if access is enabled
   * @since 1.0.0
   * @platform Android
   */
  isAccessEnabled(): Promise<{ enabled: boolean }>;

  /**
   * Retrieves notifications from the database with pagination support.
   * Notifications are stored in the database when they are posted and can be
   * retrieved later even after they are dismissed from the notification drawer.
   *
   * @param options - Pagination options (afterId and limit)
   * @returns Promise resolving with the list of notifications from the database
   * @since 1.0.0
   * @platform Android
   */
  getNotifications(options?: GetNotificationsOptions): Promise<GetNotificationsResult>;

  /**
   * Deletes all notifications from the database.
   * This does not affect notifications in the system notification drawer.
   *
   * @returns Promise resolving when all notifications have been deleted
   * @since 1.0.0
   * @platform Android
   */
  deleteAllNotifications(): Promise<void>;

  /**
   * Imports an array of notifications into the database.
   * This method is useful for restoring previously exported notifications,
   * migrating data from another source, or bulk-importing notification data.
   * 
   * Each notification will be inserted using REPLACE strategy, meaning if a
   * notification with the same ID already exists, it will be updated.
   *
   * @param options - Object containing the array of notifications to import
   * @returns Promise resolving when all notifications have been imported
   * @throws Error if the notifications array is missing or if an error occurs during import
   * @since 1.0.0
   * @platform Android
   * 
   * @example
   * ```typescript
   * const notificationsToImport = [
   *   {
   *     id: 'notification-1',
   *     appName: 'Example App',
   *     packageName: 'com.example.app',
   *     title: 'Test Notification',
   *     text: 'This is a test',
   *     timestamp: Date.now(),
   *     style: NotificationStyle.DEFAULT,
   *     actions: [],
   *     isGroupSummary: false,
   *     isOngoing: false,
   *     autoCancel: true,
   *     isLocalOnly: false,
   *     priority: 0,
   *     number: 0
   *   }
   * ];
   * 
   * await NotificationReader.importNotifications({
   *   notifications: notificationsToImport
   * });
   * ```
   */
  importNotifications(options: ImportNotificationsOptions): Promise<void>;
}
