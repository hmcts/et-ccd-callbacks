import * as OTPAuth from 'totp-generator';
import {axiosRequest} from './ApiHelper.ts';
import {readCache, writeCache} from '../CachingHelper.ts';
import querystring from 'querystring';
import { staticConfig } from '../../config/config.static.ts';

const env = process.env.RUNNING_ENV && process.env.RUNNING_ENV.startsWith('pr-') ? 'aat' : (process.env.RUNNING_ENV || 'aat');
const idamBaseUrl = staticConfig.idamUrl;

/**
 * Utility functions for obtaining authentication and service tokens for HMCTS IDAM and S2S.
 * Includes token caching for efficiency and helper for JWT expiry extraction.
 */

/**
 * Retrieves a user authentication token from IDAM for the given username and password.
 * Uses a cache to avoid unnecessary requests if the token is still valid.
 *
 * @param username - The username for authentication.
 * @param password - The password for authentication.
 * @returns Promise resolving to the user's authentication token as a string.
 */
export async function getUserAuthToken(username: string, password: string): Promise<string> {
  const tokenCache = await readCache();
  const cached = tokenCache.get(username);
  const now = Date.now();
  if (cached && cached.expiry > now) {
    return cached.token;
  }
  let data = querystring.stringify({
    username: username,
    password: password,
  });
  const loginUrl = "loginUser";
  const authTokenResponse = await axiosRequest({
    method: 'post',
    url: idamBaseUrl + loginUrl,
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    data: data,
  });

  tokenCache.set(username, {
    token: authTokenResponse.data.access_token,
    expiry: authTokenResponse.data.expires_in * 1000 + now - 60000,
    userId: cached?.userId ?? '',
  });
  await writeCache(tokenCache);
  return authTokenResponse.data.access_token;
}

/**
 * Retrieves the user ID from IDAM for the given authentication token and username.
 * Uses a cache to avoid unnecessary requests if the user ID is already known.
 *
 * @param authToken - The authentication token for the user.
 * @param username - The username for which to retrieve the user ID.
 * @returns Promise resolving to the user ID as a string.
 */
export async function getUserId(authToken: string, username: string): Promise<string> {
  const tokenCache = await readCache();
  const cached = tokenCache.get(username);
  if (cached && cached.userId) {
    return cached.userId;
  }

  const idamDetailsPath = 'details';

  const userDetailsResponse = await axiosRequest({
    method: 'get',
    url: idamBaseUrl + idamDetailsPath,
    headers: { Authorization: `Bearer ${authToken}` }
  });

  if (cached) {
    cached.userId = userDetailsResponse.data.id;
    tokenCache.set(username, cached);
  } else {
    tokenCache.set(username,
      {
        token: authToken,
        expiry: 0,
        userId: userDetailsResponse.data.id
      }
    );
  }
  await writeCache(tokenCache);
  return userDetailsResponse.data.id;
}

/**
 * Retrieves a service-to-service (S2S) token for the xui_webapp microservice.
 * Uses a cache to avoid unnecessary requests if the token is still valid.
 *
 * @returns Promise resolving to the S2S service token as a string.
 */
export async function getServiceToken(): Promise<string> {
  const tokenCache = await readCache();
  const cached = tokenCache.get('et-service-token');
  const now = Date.now();
  if (cached && cached.expiry > now) {
    return cached.token;
  }

  const serviceSecret = staticConfig.TestCcdGwSecret;
  const s2sBaseUrl = `http://rpe-service-auth-provider-${env}.service.core-compute-${env}.internal`;
  const s2sAuthPath = '/testing-support/lease';

  const oneTimePassword = (await OTPAuth.TOTP.generate(serviceSecret, {digits: 6, period: 30})).otp;

  const serviceTokenResponse = await axiosRequest({
    url: s2sBaseUrl + s2sAuthPath,
    method: 'post',
    data: {
      microservice: 'xui_webapp',
      oneTimePassword: oneTimePassword,
    },
    headers: {
      'Content-Type': 'application/json',
    },
  });

  tokenCache.set('et-service-token',
    {
      token: serviceTokenResponse.data,
      expiry: 1000 * getJwtExpiry(serviceTokenResponse.data) + now - 60000,
      userId: ''
    }
  );
  await writeCache(tokenCache);
  return serviceTokenResponse.data;
}

/**
 * Extracts the expiry time from a JWT token.
 *
 * @param token - The JWT token string.
 * @returns The expiry time as seconds since epoch, or 0 if extraction fails.
 */
export function getJwtExpiry(token: string): number {
  const payload = token.split('.')[1];
  if (!payload) return 0;
  const decoded = Buffer.from(payload, 'base64').toString('utf8');
  try {
    const { exp } = JSON.parse(decoded);
    return exp; // This is in seconds since epoch
  } catch {
    return 0;
  }
}
