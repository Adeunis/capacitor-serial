export interface SerialPermissionsParameters {
    vendorId: number | string;
    productId: number | string;
    driver: SerialDriverEnum;
}

export interface SerialConnectionParameters {
    baudRate?: number;
    dataBits?: number;
    stopBits?: number;
    parity?: number;
    dtr?: boolean;
    rts?: boolean;
}

export interface SerialPermissions {
    granted: boolean;
}

export interface SerialMessage {
    data: string;
}

export interface SerialReadParameters {
    readRaw: boolean;
}

export enum SerialDriverEnum {
    FTDI_SERIAL_DRIVER = "FtdiSerialDriver",
    CDC_ACM_SERIAL_DRIVER = "CdcAcmSerialDriver",
    CP21XX_SERIAL_DRIVER = "Cp21xxSerialDriver",
    PROLIFIC_SERIAL_DRIVER = "ProlificSerialDriver",
    CH34X_SERIAL_DRIVER = "Ch34xSerialDriver"
}

export enum SerialError {
    UNKNOWN_DRIVER_ERROR = "UNKNOWN_DRIVER_ERROR",
    NO_DEVICE_ERROR = "NO_DEVICE_ERROR",
    PARAMETER_ERROR = "PARAMETER_ERROR",
    CONNECTION_ERROR = "CONNECTION_ERROR",
    PORT_CLOSED_ERROR = "PORT_CLOSED_ERROR"
}

export interface SerialErrorWrapper extends Error {
    message: SerialError
}


export type SerialReadCallback = (message: SerialMessage | undefined, error?: SerialErrorWrapper) => void;


export interface SerialPlugin {

    /**
     * Request permissions to connect to a device over a serial connection
     *
     * @param parameters {SerialPermissionsParameters} Parameters used to request permissions for a specific productId and vendorId (integer value or hexadecimal string), and a specific driver
     * @return {Promise<SerialPermissions>} Returns a promise that resolves when permissions are granted or refused
     */
    requestSerialPermissions(parameters?: SerialPermissionsParameters): Promise<SerialPermissions>;

    /**
     * Open a serial connection to a device
     *
     * @param parameters {SerialConnectionParameters} Parameters used to open a serial connection
     * @return {Promise<void>} Returns a promise that resolves when the serial connection is opened
     */
    openConnection(parameters: SerialConnectionParameters): Promise<void>;

    /**
     * Close the serial connection
     *
     * @return {Promise<void>} Returns a promise that resolves when the serial connection is closed
     */
    closeConnection(): Promise<void>;

    /**
     * Write a message to a serial connection
     *
     * @param message {SerialMessage} contains the data string to write to the serial connection
     * @return {Promise<void>} Returns a promise that resolves when the writing is complete
     */

    write(message: SerialMessage): Promise<void>;

    /**
     * Write a hexadecimal message to a serial connection
     *
     * @param message {SerialMessage} contains the data string in hexadecimal to write to the serial connection
     * @return {Promise<void>} Returns a promise that resolves when the writing is complete
     */
    writeHexadecimal(message: SerialMessage): Promise<void>;

    /**
     * Read from a serial connection
     *
     * @param parameters {SerialReadParameters} specify if the read data should be sent back as 'raw', meaning the byte array encoded in base64 string, or as a UTF-8 decoded string
     * @return {Promise<SerialMessage>} Returns a promise that resolves with data read from the serial connection
     */
    read(parameters: SerialReadParameters): Promise<SerialMessage>;

    /**
     * Register a callback to receive the incoming data from the serial connection
     * @param callback {SerialReadCallback} the callback called each time there is incoming data from the serial connection, the data being a UTF-8 decoded string
     * @returns {Promise<string>} returns a promise with the callbackId
     */
    registerReadCallback(callback: SerialReadCallback): Promise<string>;

    /**
     * Unregister the read callback
     *
     * @returns {Promise<void>} returns a promise that resolves when the unregistration is done
     */
    unregisterReadCallback(): Promise<void>;

    /**
     * Register a callback to receive the incoming raw data from the serial connection
     * @param callback {SerialReadCallback} the callback called each time there is incoming data from the serial connection, the data being the byte array encoded in base64 string
     * @returns {Promise<string>} returns a promise with the callbackId
     */
    registerReadRawCallback(callback: SerialReadCallback): Promise<string>;

    /**
     * Unregister the read raw callback
     *
     * @returns {Promise<void>} returns a promise that resolves when the unregistration is done
     */
    unregisterReadRawCallback(): Promise<void>;

}
