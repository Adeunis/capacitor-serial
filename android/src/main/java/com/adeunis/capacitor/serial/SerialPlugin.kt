package com.adeunis.capacitor.serial

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbManager
import android.util.Log
import com.getcapacitor.JSObject
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin
import com.hoho.android.usbserial.driver.CdcAcmSerialDriver
import com.hoho.android.usbserial.driver.Ch34xSerialDriver
import com.hoho.android.usbserial.driver.Cp21xxSerialDriver
import com.hoho.android.usbserial.driver.FtdiSerialDriver
import com.hoho.android.usbserial.driver.ProbeTable
import com.hoho.android.usbserial.driver.ProlificSerialDriver
import com.hoho.android.usbserial.driver.SerialTimeoutException
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.hoho.android.usbserial.util.SerialInputOutputManager
import org.json.JSONException
import java.io.IOException
import java.nio.ByteBuffer
import java.util.concurrent.Executors

@CapacitorPlugin(name = "Serial")
class SerialPlugin : Plugin() {
    private lateinit var usbManager: UsbManager

    private var driver: UsbSerialDriver? = null
    private var port: UsbSerialPort? = null
    private val threadExecutor = Executors.newSingleThreadExecutor()
    private var serialIoManager: SerialInputOutputManager? = null
    private var readCallback: PluginCall? = null

    //Connection parameters
    private var baudRate = DEFAULT_BAUD_RATE
    private var dataBits = UsbSerialPort.DATABITS_8
    private var stopBits = UsbSerialPort.STOPBITS_1
    private var parity = UsbSerialPort.PARITY_NONE
    private var setDTR = false
    private var setRTS = false

    private val serialIoListener: SerialInputOutputManager.Listener =
        object : SerialInputOutputManager.Listener {
            override fun onRunError(e: Exception) {
                Log.w(TAG, "SerialInputOutputManager run error: " + e.message)
                onSerialIoRunError()
            }

            override fun onNewData(data: ByteArray) {
                onSerialIoNewData(data)
            }
        }

    @PluginMethod
    fun requestSerialPermissions(call: PluginCall) {
        call.setKeepAlive(true)
        execute {

            val prober: UsbSerialProber = when {
                call.hasOption("vendorId") && call.hasOption("productId") -> {
                    val customTable = ProbeTable()
                    val vendorIdObject = call.data.get("vendorId") //Number or hexadecimal string
                    val productIdObject = call.data.get("productId") //Number or hexadecimal string
                    val vid = when (vendorIdObject) {
                        is Number -> vendorIdObject.toInt()
                        is String -> vendorIdObject.toInt(16)
                        else -> 0
                    }
                    val pid = when (productIdObject) {
                        is Number -> productIdObject.toInt()
                        is String -> productIdObject.toInt(16)
                        else -> 0
                    }
                    customTable.addProduct(
                        vid,
                        pid,
                        when (call.getString("driver", CDC_ACM_SERIAL_DRIVER)) {
                            FTDI_SERIAL_DRIVER -> FtdiSerialDriver::class.java
                            CDC_ACM_SERIAL_DRIVER -> CdcAcmSerialDriver::class.java
                            CP21XX_SERIAL_DRIVER -> Cp21xxSerialDriver::class.java
                            PROLIFIC_SERIAL_DRIVER -> ProlificSerialDriver::class.java
                            CH34X_SERIAL_DRIVER -> Ch34xSerialDriver::class.java
                            else -> CdcAcmSerialDriver::class.java
                        }
                    )
                    UsbSerialProber(customTable)
                }

                else -> UsbSerialProber.getDefaultProber()
            }
            val availableDrivers = prober.findAllDrivers(usbManager)
            if (availableDrivers.isEmpty()) {
                call.reject(NO_DEVICE_ERROR)
                return@execute
            }

            val safeDriver = availableDrivers.first()
            driver = safeDriver
            val device = safeDriver.device
            val pendingIntent = PendingIntent.getBroadcast(
                this.context,
                0,
                Intent(SerialPermissionsBroadcastReceiver.USB_PERMISSION),
                PendingIntent.FLAG_MUTABLE
            )

            val filter = IntentFilter()
            filter.addAction(SerialPermissionsBroadcastReceiver.USB_PERMISSION)

            this.context.registerReceiver(
                SerialPermissionsBroadcastReceiver(
                    call,
                    this.context
                ), filter
            )

            usbManager.requestPermission(device, pendingIntent)

        }
    }

    @PluginMethod(returnType = PluginMethod.RETURN_NONE)
    fun openConnection(call: PluginCall) {
        if (driver == null) {
            call.reject(UNKNOWN_DRIVER_ERROR)
            return
        }
        val driver = driver ?: return
        execute {
            val connection = usbManager.openDevice(driver.device)
            if (connection == null) {
                stopSerialIoManager()
                call.reject(CONNECTION_ERROR)
                return@execute
            }

            try {
                baudRate = call.getInt("baudRate") ?: DEFAULT_BAUD_RATE
                dataBits = call.getInt("dataBits") ?: UsbSerialPort.DATABITS_8
                stopBits = call.getInt("stopBits") ?: UsbSerialPort.STOPBITS_1
                parity = call.getInt("parity") ?: UsbSerialPort.PARITY_NONE
                setDTR = call.hasOption("dtr") && (call.getBoolean("dtr") ?: false)
                setRTS = call.hasOption("rts") && (call.getBoolean("rts") ?: false)
            } catch (e: JSONException) {
                Log.w(TAG, "openConnection options error: " + e.message)
                call.reject(PARAMETER_ERROR)
                return@execute
            }

            try {
                val safePort = driver.ports.first()

                port = safePort
                safePort.open(connection)
                safePort.setParameters(baudRate, dataBits, stopBits, parity)
                if (setDTR) safePort.dtr = true
                if (setRTS) safePort.rts = true
            } catch (e: IOException) {
                Log.w(TAG, "openConnection error: " + e.message)
                call.reject(CONNECTION_ERROR)
                return@execute
            }
            startSerialIoManager()
            call.resolve()
        }
    }

    @PluginMethod(returnType = PluginMethod.RETURN_NONE)
    fun closeConnection(call: PluginCall) {
        execute {
            stopSerialIoManager()
            try {
                port?.close()
                port = null
                call.resolve()
            } catch (e: IOException) {
                Log.w(TAG, "closeConnection error: " + e.message)
                call.reject(CONNECTION_ERROR)
            }
        }
    }

    @PluginMethod(returnType = PluginMethod.RETURN_NONE)
    fun write(call: PluginCall) {
        val data = call.getString("data")
        execute {
            if (port == null) {
                call.reject(PORT_CLOSED_ERROR)
                return@execute
            } else if (data == null) {
                call.reject(PARAMETER_ERROR)
                return@execute
            }
            val port = port ?: return@execute

            try {
                port.write(data.toByteArray(), WRITE_TIMEOUT)
                call.resolve()
            } catch (e: SerialTimeoutException) {
                Log.w(TAG, "write error: " + e.message)
                call.reject(CONNECTION_ERROR)
            } catch (e: IOException) {
                Log.w(TAG, "write error: " + e.message)
                call.reject(CONNECTION_ERROR)
            }

        }
    }

    @PluginMethod(returnType = PluginMethod.RETURN_NONE)
    fun writeHexadecimal(call: PluginCall) {
        val data = call.getString("data")
        execute {
            if (port == null) {
                call.reject(PORT_CLOSED_ERROR)
                return@execute
            } else if (data == null) {
                call.reject(PARAMETER_ERROR)
                return@execute
            }
            val port = port ?: return@execute

            try {
                port.write(data.toByteArrayFromHexadecimal(), WRITE_TIMEOUT)
                call.resolve()
            } catch (e: SerialTimeoutException) {
                Log.w(TAG, "write hexadecimal error: " + e.message)
                call.reject(CONNECTION_ERROR)
            } catch (e: IOException) {
                Log.w(TAG, "write hexadecimal error: " + e.message)
                call.reject(CONNECTION_ERROR)
            }

        }
    }

    @PluginMethod
    fun read(call: PluginCall) {
        execute {
            if (port == null) {
                call.reject(PORT_CLOSED_ERROR)
                return@execute
            }
            val port = port ?: return@execute
            val readBuffer = ByteBuffer.allocate(BUFFER_READ_SIZE)
            val buffer = readBuffer.array()

            val readSize = try {
                port.read(buffer, READ_TIMEOUT)
            } catch (e: SerialTimeoutException) {
                Log.w(TAG, "read error: " + e.message)
                call.reject(CONNECTION_ERROR)
                return@execute
            } catch (e: IOException) {
                Log.w(TAG, "read error: " + e.message)
                call.reject(CONNECTION_ERROR)
                return@execute
            }
            val response = JSObject()
            val data = ByteArray(readSize)
            System.arraycopy(buffer, 0, data, 0, readSize)
            readBuffer.clear()
            response.put("data", data.decodeToString())
            call.resolve(response)


        }
    }

    @PluginMethod(returnType = PluginMethod.RETURN_CALLBACK)
    fun registerReadCallback(call: PluginCall) {
        execute {
            Log.i(TAG, "Registering Read Callback")
            readCallback = call
            call.setKeepAlive(true)
        }
    }

    @PluginMethod(returnType = PluginMethod.RETURN_NONE)
    fun unregisterReadCallback(call: PluginCall) {
        execute {
            Log.i(TAG, "Unregistering Read Callback")
            readCallback = null
            call.resolve()
        }
    }

    private fun onSerialIoNewData(data: ByteArray) {
        val readCallback = readCallback ?: return
        val response = JSObject()
        response.put("data", data.decodeToString())
        readCallback.resolve(response)

    }

    private fun onSerialIoRunError() {
        val readCallback = readCallback ?: return
        val response = JSObject()
        response.put("error", CONNECTION_ERROR)
        readCallback.resolve(response)
    }

    public override fun handleOnStart() {
        this.usbManager = this@SerialPlugin.context.getSystemService(UsbManager::class.java)

    }


    public override fun handleOnPause() {
        stopSerialIoManager()
        try {
            port?.close()
        } catch (_: IOException) {
        }
        port = null
    }

    public override fun handleOnResume() {
        val driver = driver ?: return
        val connection = usbManager.openDevice(driver.device)
        if (connection == null) {
            Log.w(TAG, "Cannot reconnect to the device")
            return
        }

        val safePort = driver.ports.first()
        port = safePort
        try {
            safePort.open(connection)
            safePort.setParameters(baudRate, dataBits, stopBits, parity)
            if (setDTR) safePort.dtr = true
            if (setRTS) safePort.rts = true
        } catch (e: IOException) {
            Log.w(TAG, "Cannot open port" + e.message)
        }

        Log.i(TAG, "Serial device: " + driver.javaClass.simpleName)
        startSerialIoManager()
    }

    public override fun handleOnDestroy() {
        stopSerialIoManager()
        try {
            port?.close()
        } catch (_: IOException) {
        }
        port = null

    }

    private fun stopSerialIoManager() {
        val serialIoManager = serialIoManager ?: return
        Log.i(TAG, "Stopping Serial IO Manager.")
        serialIoManager.stop()
        this.serialIoManager = null

    }

    private fun startSerialIoManager() {
        val port = port ?: return
        Log.i(TAG, "Starting Serial IO Manager.")
        serialIoManager?.stop()
        serialIoManager = SerialInputOutputManager(port, serialIoListener)
        threadExecutor.submit(serialIoManager)
    }

    private fun String.toByteArrayFromHexadecimal(): ByteArray =
        chunked(2).map { it.uppercase().toInt(16).toByte() }.toByteArray()


    companion object {

        private const val TAG = "CapacitorSerialPlugin"
        private const val DEFAULT_BAUD_RATE = 115200
        private const val READ_TIMEOUT = 200
        private const val WRITE_TIMEOUT: Int = 2000
        private const val BUFFER_READ_SIZE: Int = 4096

        //DRIVERS
        private const val CDC_ACM_SERIAL_DRIVER = "CdcAcmSerialDriver"
        private const val CH34X_SERIAL_DRIVER = "Ch34xSerialDriver"
        private const val CP21XX_SERIAL_DRIVER = "Cp21xxSerialDriver"
        private const val FTDI_SERIAL_DRIVER = "FtdiSerialDriver"
        private const val PROLIFIC_SERIAL_DRIVER = "ProlificSerialDriver"

        //ERRORS
        private const val UNKNOWN_DRIVER_ERROR = "UNKNOWN_DRIVER_ERROR"
        private const val NO_DEVICE_ERROR = "NO_DEVICE_ERROR"
        private const val PARAMETER_ERROR = "PARAMETER_ERROR"
        private const val CONNECTION_ERROR = "CONNECTION_ERROR"
        private const val PORT_CLOSED_ERROR = "PORT_CLOSED_ERROR"
    }
}

