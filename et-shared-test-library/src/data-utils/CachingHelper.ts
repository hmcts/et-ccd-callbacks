import lockfile from 'proper-lockfile';
import fs from 'fs-extra';
import path from 'path';

const TEMP_DIR = './.tmp';
const CACHE_FILE_NAME = 'token-cache.json';
const TEST_USERS_FILE_NAME = 'test-users.json';

export type CachedToken = {
  token: string;
  expiry: number;
  userId: string;
};

export async function writeCache(cache: Map<string, CachedToken>): Promise<void> {
  let release: (() => Promise<void>) | undefined;
  const cachePath = path.join(TEMP_DIR, CACHE_FILE_NAME);
  if (!fs.existsSync(TEMP_DIR)) {
    fs.mkdirSync(TEMP_DIR);
  }
  try {
    release = await lockfile.lock(cachePath, { retries: 5, realpath: false });
    const obj = Object.fromEntries(cache);
    await fs.writeJson(cachePath, obj, { spaces: 2 });
  } catch (err) {
    console.error('Error writing token cache:', err);
  } finally {
    if (release) {
      await release().catch(err => {
        console.error('Error releasing lock after write:', err);
      });
    }
  }
}

export async function readCache(): Promise<Map<string, CachedToken>> {
  let release: (() => Promise<void>) | undefined;
  const cachePath = path.join(TEMP_DIR, CACHE_FILE_NAME);
  if (!fs.existsSync(TEMP_DIR)) {
    fs.mkdirSync(TEMP_DIR);
  }
  try {
    release = await lockfile.lock(cachePath, { retries: 5, realpath: false });
    const exists = await fs.pathExists(cachePath);
    if (!exists) return new Map();
    const data = await fs.readJson(cachePath);
    return new Map(Object.entries(data));
  } catch (err) {
    console.error('Error reading token cache:', err);
    return new Map();
  } finally {
    if (release) {
      await release().catch(err => {
        console.error('Error releasing lock after read:', err);
        return new Map();
      });
    }
  }
}

export async function deleteCacheFile(): Promise<void> {
  let release: (() => Promise<void>) | undefined;
  const cachePath = path.join(TEMP_DIR, CACHE_FILE_NAME);
  try {
    release = await lockfile.lock(cachePath, { retries: 5, realpath: false });
    const exists = await fs.pathExists(cachePath);
    if (exists) {
      await fs.remove(cachePath);
    }
  } catch (err) {
    console.error('Error deleting token cache:', err);
  } finally {
    if (release) {
      await release().catch(err => {
        console.error('Error releasing lock after delete:', err);
      });
    }
  }
}

export async function deleteUserCredFile(): Promise<void> {
  let release: (() => Promise<void>) | undefined;
  const userFilePath = path.join(TEMP_DIR, TEST_USERS_FILE_NAME);
  try {
    release = await lockfile.lock(userFilePath, { retries: 5, realpath: false });
    const exists = await fs.pathExists(userFilePath);
    if (exists) {
      await fs.remove(userFilePath);
    }
  } catch (err) {
    console.error('Error deleting test users cache:', err);
  } finally {
    if (release) {
      await release().catch(err => {
        console.error('Error releasing lock after delete:', err);
      });
    }
  }
}

export function getDynamicUser(key: string) {
  const tmpFile = path.join(TEMP_DIR, TEST_USERS_FILE_NAME);
  if (fs.existsSync(tmpFile)) {
    try {
      const users = JSON.parse(fs.readFileSync(tmpFile, 'utf-8'));
      return users[key] || {};
    } catch {
      return {};
    }
  }
  return {};
}

/**
 * Deletes all files in the .sessions directory (used for Playwright storageState/session files).
 * Call this in global teardown to ensure a clean state.
 */
export async function deleteAllSessionFiles(): Promise<void> {
  const sessionsDir = './.sessions';
  try {
    if (await fs.pathExists(sessionsDir)) {
      const files = await fs.readdir(sessionsDir);
      for (const file of files) {
        const filePath = path.join(sessionsDir, file);
        await fs.remove(filePath);
      }
      // Remove the .sessions directory itself
      fs.rmdir(sessionsDir);
    }
  } catch (err) {
    console.error('Error deleting session files:', err);
  }
}
