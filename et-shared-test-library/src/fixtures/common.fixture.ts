import { mergeTests } from '@playwright/test';
import { pageFixtures } from './page.fixture';
import { allyTest } from './axe-fixture';
import { CcdApi } from '../data-utils/api/CcdApi';
import { CuiApi } from '../data-utils/api/CuiApi';
import { utilFixtures } from './utils.fixture';

export const test = mergeTests(pageFixtures, allyTest, utilFixtures);

export const ccdApi = new CcdApi();
export const cuiApi = new CuiApi();
