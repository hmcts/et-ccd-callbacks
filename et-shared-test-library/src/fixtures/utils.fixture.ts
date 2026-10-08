import { test as base } from '@playwright/test';
import { BrowserUtils } from '@hmcts/playwright-common';

export type UtilFixtures = {
  browserUtils: BrowserUtils;
};

export const utilFixtures = base.extend<UtilFixtures>({
  browserUtils: async ({ browser }, use) => {
    await use(new BrowserUtils(browser));
  },
});
