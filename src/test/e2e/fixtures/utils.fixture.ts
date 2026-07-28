import { test as base } from '@playwright/test';
import { CookieUtils } from '../data-utils/cookie.utils';
import { BrowserUtils, SessionUtils } from '@hmcts/playwright-common';

export type UtilFixtures = {
  browserUtils: BrowserUtils;
};

export const utilFixtures = base.extend<UtilFixtures>({
  browserUtils: async ({ browser }, use) => {
    await use(new BrowserUtils(browser));
  },
});
