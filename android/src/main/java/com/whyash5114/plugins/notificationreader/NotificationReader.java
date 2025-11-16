package com.whyash5114.plugins.notificationreader;

import com.getcapacitor.Logger;

public class NotificationReader {

    public String echo(String value) {
        Logger.info("Echo", value);
        return value;
    }
}
