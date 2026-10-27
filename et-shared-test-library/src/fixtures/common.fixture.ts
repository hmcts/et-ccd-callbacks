import { mergeTests, expect } from '@playwright/test';
import { pageFixtures } from './page.fixture.js';
import { allyTest } from './axe-fixture.js';
import { CcdApi } from '../data-utils/api/CcdApi.js';
import { CuiApi } from '../data-utils/api/CuiApi.js';
import { utilFixtures } from './utils.fixture.js';

export const test = mergeTests(pageFixtures, allyTest, utilFixtures);
export { expect };

export const ccdApi = new CcdApi();
export const cuiApi = new CuiApi();
