import { test as base, expect } from '@hmcts/et-shared-test-library/fixtures/common'

export const test = base.extend({});
export { expect };

test.beforeEach(async ({}, testInfo) => {
  if (!process.env.CI) console.log(`Running test: ${testInfo.title}`);
});

