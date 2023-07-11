#import <Foundation/Foundation.h>
#import <Capacitor/Capacitor.h>

// Define the plugin using the CAP_PLUGIN Macro, and
// each method the plugin supports using the CAP_PLUGIN_METHOD macro.
CAP_PLUGIN(SerialPlugin, "Serial",
           CAP_PLUGIN_METHOD(closeConnection, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(openConnection, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(read, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(registerReadCallback, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(registerReadRawCallback, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(requestSerialPermissions, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(unregisterReadCallback, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(unregisterReadRawCallback, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(write, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(writeHexadecimal, CAPPluginReturnPromise);
)
