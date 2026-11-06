import { test as base, expect } from '@hmcts/et-shared-test-library/fixtures/common'

export const test = base.extend({});
let startTime: number;

test.beforeEach(async ({}, testInfo) => {
  startTime = Date.now();
  if (!process.env.CI) console.log(`Running test: ${testInfo.title}`);
});

test.afterEach(async () => {
  const duration = Date.now() - startTime;
  if (!process.env.CI) console.log(`Test duration: ${duration}ms`);
})
