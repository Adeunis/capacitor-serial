import { registerPlugin } from '@capacitor/core';

import type { SerialPlugin } from './definitions';

const Serial = registerPlugin<SerialPlugin>('Serial', {
  web: () => import('./web').then(m => new m.SerialWeb()),
});

export * from './definitions';
export { Serial };
