import { test as base } from '@playwright/test';
import { AxeUtils } from '@hmcts/playwright-common';

type AxeFixture = {
  axeUtils: AxeUtils;
};

export const allyTest = base.extend<AxeFixture>({
  axeUtils: async ({ page }, use, testInfo) => {
    const axeUtils = new AxeUtils(page);
    await use(axeUtils);
    await axeUtils.generateReport(testInfo);
  },
});
