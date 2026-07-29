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
  timeout: 4 * 60 * 1000,
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
      name: 'setup',
      testMatch: /user\.setups\.ts/,
      workers: 1
    },
    {
      name: 'chromium',
      use: {
        ...devices['Desktop Chrome'],
        viewport: { width: 1920, height: 1080 },
      },
      dependencies: ["setup"]
    },

    {
      name: 'firefox',
      use: { ...devices['Desktop Firefox'] },
      dependencies: ["setup"]
    },

    {
      name: 'webkit',
      use: { ...devices['Desktop Safari'] },
      dependencies: ["setup"]
    },
  ],
});
