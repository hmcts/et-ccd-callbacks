import { test as base } from '@et-shared-test-library/core/fixtures/common'

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
