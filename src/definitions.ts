export interface NotificationReaderPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
