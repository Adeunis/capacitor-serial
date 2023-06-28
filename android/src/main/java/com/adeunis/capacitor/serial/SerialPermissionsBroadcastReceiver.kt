package com.adeunis.capacitor.serial

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbManager
import com.getcapacitor.JSObject
import com.getcapacitor.PluginCall

class SerialPermissionsBroadcastReceiver(private val call: PluginCall, private val context: Context) :
    BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (USB_PERMISSION == action) {
            val res = JSObject()
            res.put("granted", intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false))
            call.resolve(res)
            call.setKeepAlive(false)
            this.context.unregisterReceiver(this)
        }
    }

    companion object {
        const val USB_PERMISSION = "com.adeunis.capacitor.serial.USB_PERMISSION"
    }
}