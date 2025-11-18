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
  icon?: string | null;
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
  sender: string | null;
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
  /**
   * Base64-encoded PNG of the notification's small icon (status bar icon).
   */
  smallIcon?: string | null;
  /**
   * Base64-encoded PNG of the notification's large icon.
   */
  largeIcon?: string | null;
  /**
   * Base64-encoded PNG of the app's launcher icon.
   */
  appIcon?: string | null;
  /**
   * Notification category (call, message, email, etc.)
   */
  category: NotificationCategory;
  /**
   * Notification style template used
   */
  style: NotificationStyle;
  /**
   * Sub-text shown below the main text
   */
  subText?: string | null;
  /**
   * Additional info text
   */
  infoText?: string | null;
  /**
   * Summary text for expanded notifications
   */
  summaryText?: string | null;
  /**
   * Group key for grouped notifications
   */
  group?: string | null;
  /**
   * Whether this is a group summary notification
   */
  isGroupSummary: boolean;
  /**
   * Notification channel ID (Android 8+)
   */
  channelId?: string | null;
  /**
   * Action buttons available on the notification
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
  number?: number;
}

/**
 * Big text style notification with expanded text content
 */
export interface BigTextNotification extends BaseNotification {
  style: NotificationStyle.BIG_TEXT;
  /**
   * The full expanded text content
   */
  bigText?: string | null;
}

/**
 * Big picture style notification with an image
 */
export interface BigPictureNotification extends BaseNotification {
  style: NotificationStyle.BIG_PICTURE;
  /**
   * Base64-encoded picture shown in expanded view
   */
  bigPicture?: string | null;
  /**
   * Content description for the picture
   */
  pictureContentDescription?: string | null;
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
  conversationTitle?: string | null;
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
  category: NotificationCategory.CALL | NotificationCategory.MISSED_CALL;
  /**
   * Caller name
   */
  callerName?: string | null;
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
}
