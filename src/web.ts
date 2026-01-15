import { WebPlugin } from '@capacitor/core';

import type {
  GetActiveNotificationsResult,
  GetInstalledAppsResult,
  GetNotificationsOptions,
  GetNotificationsResult,
  ImportNotificationsOptions,
  NotificationReaderConfig,
  NotificationReaderPlugin,
} from './definitions';

export class NotificationReaderWeb extends WebPlugin implements NotificationReaderPlugin {
  async getActiveNotifications(): Promise<GetActiveNotificationsResult> {
    throw this.unimplemented('Not implemented on web.');
  }
  async openAccessSettings(): Promise<{ enabled: boolean }> {
    throw this.unimplemented('Not implemented on web.');
  }
  async isAccessEnabled(): Promise<{ enabled: boolean }> {
    throw this.unimplemented('Not implemented on web.');
  }
  async getNotifications(_options?: GetNotificationsOptions): Promise<GetNotificationsResult> {
    throw this.unimplemented('Not implemented on web.');
  }
  async deleteAllNotifications(): Promise<void> {
    throw this.unimplemented('Not implemented on web.');
  }
  async importNotifications(_options: ImportNotificationsOptions): Promise<void> {
    throw this.unimplemented('Not implemented on web.');
  }
  async getTotalCount(): Promise<{ count: number }> {
    throw this.unimplemented('Not implemented on web.');
  }
  async getDatabaseSize(): Promise<{ sizeBytes: number; sizeMB: number }> {
    throw this.unimplemented('Not implemented on web.');
  }
  async getInstalledApps(): Promise<GetInstalledAppsResult> {
    throw this.unimplemented('Not implemented on web.');
  }
  async getConfig(): Promise<NotificationReaderConfig> {
    throw this.unimplemented('Not implemented on web.');
  }
  async setConfig(_config: NotificationReaderConfig): Promise<void> {
    throw this.unimplemented('Not implemented on web.');
  }
}
