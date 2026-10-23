import { test as base } from '@hmcts/et-shared-test-library/fixtures/common'

export const test = base.extend({});

test.beforeEach(async ({}, testInfo) => {
  if (!process.env.CI) console.log(`Running test: ${testInfo.title}`);
});

