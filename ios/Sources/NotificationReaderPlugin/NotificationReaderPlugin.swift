import Foundation
import Capacitor

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(NotificationReaderPlugin)
public class NotificationReaderPlugin: CAPPlugin, CAPBridgedPlugin {
    public let identifier = "NotificationReaderPlugin"
    public let jsName = "NotificationReader"
    public let pluginMethods: [CAPPluginMethod] = [
        CAPPluginMethod(name: "getActiveNotifications", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "openAccessSettings", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "isAccessEnabled", returnType: CAPPluginReturnPromise)
    ]
    private let implementation = NotificationReader()

    @objc func getActiveNotifications(_ call: CAPPluginCall) {
        call.reject("Not implemented on iOS. Notification access is an Android-only feature.")
    }

    @objc func openAccessSettings(_ call: CAPPluginCall) {
        call.reject("Not implemented on iOS. Notification access is an Android-only feature.")
    }

    @objc func isAccessEnabled(_ call: CAPPluginCall) {
        call.reject("Not implemented on iOS. Notification access is an Android-only feature.")
    }
}
