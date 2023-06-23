import Foundation
import Capacitor

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(SerialPlugin)
public class SerialPlugin: CAPPlugin {
    private let implementation = Serial()

   @objc func closeConnection(_ call: CAPPluginCall) {
        call.unimplemented("Not implemented on iOS.")
  }

   @objc func openConnection(_ call: CAPPluginCall) {
        call.unimplemented("Not implemented on iOS.")
  }

  @objc func read(_ call: CAPPluginCall) {
        call.unimplemented("Not implemented on iOS.")
  }

 @objc func registerReadCallback(_ call: CAPPluginCall) {
        call.unimplemented("Not implemented on iOS.")
  }

 @objc func requestSerialPermissions(_ call: CAPPluginCall) {
        call.unimplemented("Not implemented on iOS.")
  }

  @objc func unregisterReadCallback(_ call: CAPPluginCall) {
        call.unimplemented("Not implemented on iOS.")
  }

  @objc func write(_ call: CAPPluginCall) {
        call.unimplemented("Not implemented on iOS.")
  }

  @objc func writeHexadecimal(_ call: CAPPluginCall) {
        call.unimplemented("Not implemented on iOS.")
  }

}
