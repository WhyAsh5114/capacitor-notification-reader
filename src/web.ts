import { WebPlugin } from '@capacitor/core';

import type { GetActiveNotificationsResult, NotificationReaderPlugin } from './definitions';

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
}
