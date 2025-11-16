import { WebPlugin } from '@capacitor/core';

import type { NotificationReaderPlugin } from './definitions';

export class NotificationReaderWeb extends WebPlugin implements NotificationReaderPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
