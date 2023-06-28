# capacitor-serial

Manage serial connections to Android devices for Capacitor projects.

_NB: Does not work for Web or iOS._

## Install

```bash
npm install capacitor-serial
npx cap sync
```

## Basic usage

```typescript

    //Connection
    Serial.requestSerialPermissions({vendorId: '1111', productId: '2222', driver: SerialDriverEnum.CDC_ACM_SERIAL_DRIVER})
    .then((permissionResponse) => {
        if (!permissionResponse.granted) {
            return Promise.reject('Permission refused');
        }
        return Promise.resolve();

    })
    .then(() => Serial.openConnection({baudRate: 115200}))
    .then(() => log.info('Serial connection opend'))
    .catch((error) => log.error(error));

    //Write
    Serial.write({data: 'my_message'})
        .then(() => log.info('Data sent'))
        .catch((error: SerialError ) => {
            log.error(`error : ${error}`);
        });
    
    
    //Read
    Serial.read().then((message) => log.info(message.data));

    //Read callback 
    Serial.registerReadCallback((message, error) => {
        if (message !== undefined && message !== null) {
            log.info(message.data);

        } else if (error !== undefined && error !== null) {
            log.error(`error : ${error}`);
        }
    })

    Serial.closeConnection().then(() => log.info('Serial connection closed'));


```

## API

<docgen-index>

* [`requestSerialPermissions(...)`](#requestserialpermissions)
* [`openConnection(...)`](#openconnection)
* [`closeConnection()`](#closeconnection)
* [`write(...)`](#write)
* [`writeHexadecimal(...)`](#writehexadecimal)
* [`read()`](#read)
* [`registerReadCallback(...)`](#registerreadcallback)
* [`unregisterReadCallback()`](#unregisterreadcallback)
* [Interfaces](#interfaces)
* [Type Aliases](#type-aliases)
* [Enums](#enums)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### requestSerialPermissions(...)

```typescript
requestSerialPermissions(parameters?: SerialPermissionsParameters | undefined) => Promise<SerialPermissions>
```

Request permissions to connect to a device over a serial connection

| Param            | Type                                                                                | Description                                                                                                                               |
| ---------------- | ----------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------- |
| **`parameters`** | <code><a href="#serialpermissionsparameters">SerialPermissionsParameters</a></code> | Parameters used to request permissions for a specific productId and vendorId (integer value or hexadecimal string), and a specific driver |

**Returns:** <code>Promise&lt;<a href="#serialpermissions">SerialPermissions</a>&gt;</code>

--------------------


### openConnection(...)

```typescript
openConnection(parameters: SerialConnectionParameters) => Promise<void>
```

Open a serial connection to a device

| Param            | Type                                                                              | Description                                 |
| ---------------- | --------------------------------------------------------------------------------- | ------------------------------------------- |
| **`parameters`** | <code><a href="#serialconnectionparameters">SerialConnectionParameters</a></code> | Parameters used to open a serial connection |

--------------------


### closeConnection()

```typescript
closeConnection() => Promise<void>
```

Close the serial connection

--------------------


### write(...)

```typescript
write(message: SerialMessage) => Promise<void>
```

Write a message to a serial connection

| Param         | Type                                                    | Description                                                |
| ------------- | ------------------------------------------------------- | ---------------------------------------------------------- |
| **`message`** | <code><a href="#serialmessage">SerialMessage</a></code> | contains the data string to write to the serial connection |

--------------------


### writeHexadecimal(...)

```typescript
writeHexadecimal(message: SerialMessage) => Promise<void>
```

Write a hexadecimal message to a serial connection

| Param         | Type                                                    | Description                                                               |
| ------------- | ------------------------------------------------------- | ------------------------------------------------------------------------- |
| **`message`** | <code><a href="#serialmessage">SerialMessage</a></code> | contains the data string in hexadecimal to write to the serial connection |

--------------------


### read()

```typescript
read() => Promise<SerialMessage>
```

Read from a serial connection

**Returns:** <code>Promise&lt;<a href="#serialmessage">SerialMessage</a>&gt;</code>

--------------------


### registerReadCallback(...)

```typescript
registerReadCallback(callback: SerialReadCallback) => Promise<string>
```

Register a callback to receive the incoming data from the serial connection

| Param          | Type                                                              | Description                                                                     |
| -------------- | ----------------------------------------------------------------- | ------------------------------------------------------------------------------- |
| **`callback`** | <code><a href="#serialreadcallback">SerialReadCallback</a></code> | the callback called each time there is incoming data from the serial connection |

**Returns:** <code>Promise&lt;string&gt;</code>

--------------------


### unregisterReadCallback()

```typescript
unregisterReadCallback() => Promise<void>
```

Unregister the read callback

--------------------


### Interfaces


#### SerialPermissions

| Prop          | Type                 |
| ------------- | -------------------- |
| **`granted`** | <code>boolean</code> |


#### SerialPermissionsParameters

| Prop            | Type                                                          |
| --------------- | ------------------------------------------------------------- |
| **`vendorId`**  | <code>string \| number</code>                                 |
| **`productId`** | <code>string \| number</code>                                 |
| **`driver`**    | <code><a href="#serialdriverenum">SerialDriverEnum</a></code> |


#### SerialConnectionParameters

| Prop           | Type                 |
| -------------- | -------------------- |
| **`baudRate`** | <code>number</code>  |
| **`dataBits`** | <code>number</code>  |
| **`stopBits`** | <code>number</code>  |
| **`parity`**   | <code>number</code>  |
| **`dtr`**      | <code>boolean</code> |
| **`rts`**      | <code>boolean</code> |


#### SerialMessage

| Prop       | Type                |
| ---------- | ------------------- |
| **`data`** | <code>string</code> |


### Type Aliases


#### SerialReadCallback

<code>(message: <a href="#serialmessage">SerialMessage</a>, error?: <a href="#serialerror">SerialError</a>): void</code>


### Enums


#### SerialDriverEnum

| Members                      | Value                               |
| ---------------------------- | ----------------------------------- |
| **`FTDI_SERIAL_DRIVER`**     | <code>"FtdiSerialDriver"</code>     |
| **`CDC_ACM_SERIAL_DRIVER`**  | <code>"CdcAcmSerialDriver"</code>   |
| **`CP21XX_SERIAL_DRIVER`**   | <code>"Cp21xxSerialDriver"</code>   |
| **`PROLIFIC_SERIAL_DRIVER`** | <code>"ProlificSerialDriver"</code> |
| **`CH34X_SERIAL_DRIVER`**    | <code>"Ch34xSerialDriver"</code>    |


#### SerialError

| Members                    | Value                               |
| -------------------------- | ----------------------------------- |
| **`UNKNOWN_DRIVER_ERROR`** | <code>"UNKNOWN_DRIVER_ERROR"</code> |
| **`NO_DEVICE_ERROR`**      | <code>"NO_DEVICE_ERROR"</code>      |
| **`PARAMETER_ERROR`**      | <code>"PARAMETER_ERROR"</code>      |
| **`CONNECTION_ERROR`**     | <code>"CONNECTION_ERROR"</code>     |
| **`PORT_CLOSED_ERROR`**    | <code>"PORT_CLOSED_ERROR"</code>    |

</docgen-api>
