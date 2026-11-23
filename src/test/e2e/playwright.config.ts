import { defineConfig, devices } from '@playwright/test';
/**
 * See https://playwright.dev/docs/test-configuration.
 */
export default defineConfig({
  testDir: './',
  testMatch: '*test.ts',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 6 : undefined,
  reporter: process.env.CI ? [["html"], ["list"]] : [["list"]],
  timeout: 5 * 60 * 1000,
  globalSetup: './config/global/global-setup',
  globalTeardown: './config/global/global-teardown',
  expect: {
    timeout: 1 * 60 * 1000, // 1 minute expect wait time
  },
  /* Shared settings for all the projects below. See https://playwright.dev/docs/api/class-testoptions. */
  use: {
    /* Collect trace when retrying the failed test. See https://playwright.dev/docs/trace-viewer */
    trace: 'retain-on-failure',
    video: "retain-on-failure",
    screenshot: 'only-on-failure',
  },

  /* Configure projects for major browsers */
  projects: [
    {
      name: 'setup-chromium',
      testMatch: /user\.setups\.ts/,
      use: { ...devices['Desktop Chrome'] },
      // Logins are independent; cap concurrency to keep the load on IDAM modest.
      workers: 4
    },
    {
      name: 'chromium',
      use: {
        ...devices['Desktop Chrome'],
        viewport: { width: 1920, height: 1080 },
      },
      dependencies: ["setup-chromium"]
    },
    {
      name: 'setup-fire-fox',
      testMatch: /user\.setups\.ts/,
      use: { ...devices['Desktop Firefox'] },
      // Logins are independent; cap concurrency to keep the load on IDAM modest.
      workers: 4
    },
    {
      name: 'firefox',
      use: { ...devices['Desktop Firefox'] },
      dependencies: ["setup-fire-fox"]
    },
    {
      name: 'setup-webkit',
      testMatch: /user\.setups\.ts/,
      use: { ...devices['Desktop Safari'] },
      // Logins are independent; cap concurrency to keep the load on IDAM modest.
      workers: 4
    },
    {
      name: 'webkit',
      use: { ...devices['Desktop Safari'] },
      dependencies: ["setup-webkit"]
    },
  ],
});
