import { WebPlugin } from '@capacitor/core';

import type {
  SerialConnectionParameters,
  SerialPermissionsParameters,
  SerialPlugin,
  SerialMessage,
  SerialPermissions,
  SerialReadCallback,
  SerialReadParameters
} from './definitions';

export class SerialWeb extends WebPlugin implements SerialPlugin {
  closeConnection(): Promise<void> {
    throw this.unimplemented('Not implemented on web.');
  }

  openConnection(_: SerialConnectionParameters): Promise<void> {
    throw this.unimplemented('Not implemented on web.');
  }

  read(_: SerialReadParameters): Promise<SerialMessage> {
    throw this.unimplemented('Not implemented on web.');
  }

  registerReadCallback(_: SerialReadCallback): Promise<string> {
    throw this.unimplemented('Not implemented on web.');
  }

  requestSerialPermissions(_?: SerialPermissionsParameters): Promise<SerialPermissions> {
    throw this.unimplemented('Not implemented on web.');
  }

  unregisterReadCallback(): Promise<void> {
    throw this.unimplemented('Not implemented on web.');
  }

  write(_: SerialMessage): Promise<void> {
    throw this.unimplemented('Not implemented on web.');
  }

  writeHexadecimal(_: SerialMessage): Promise<void> {
    throw this.unimplemented('Not implemented on web.');
  }

  registerReadRawCallback(_: SerialReadCallback): Promise<string> {
    return Promise.resolve("");
  }

  unregisterReadRawCallback(): Promise<void> {
    return Promise.resolve(undefined);
  }

}
