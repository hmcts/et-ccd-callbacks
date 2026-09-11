import { mergeTests } from '@playwright/test';
import { pageFixtures } from './page.fixture';
import { allyTest } from './axe-fixture';
import { CcdApi } from '../data-utils/api/CcdApi.ts';
import { CuiApi } from '../data-utils/api/CuiApi.ts';
import { utilFixtures } from './utils.fixture.ts';

export const test = mergeTests(pageFixtures, allyTest, utilFixtures);

test.beforeEach(async ({}, testInfo) => {
  if (!process.env.CI) console.log(`Running test: ${testInfo.title}`);
});

export const ccdApi = new CcdApi();
export const cuiApi = new CuiApi();
