import { test as base } from '@et-shared-test-library/core/fixtures/common'

export const test = base.extend({});

test.beforeEach(async ({}, testInfo) => {
  if (!process.env.CI) console.log(`Running test: ${testInfo.title}`);
});

