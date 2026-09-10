import { defineConfig } from '@playwright/test';

// Page helper regressions use local HTML and need no HMCTS services or accounts.
export default defineConfig({
  testDir: '.',
  testMatch: '*.spec.ts',
  timeout: 20000,
  use: { browserName: 'chromium' },
});
