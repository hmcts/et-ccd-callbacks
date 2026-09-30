import fs from "fs";
import { Cookie } from "playwright-core";

export class CookieUtils {
  private static readonly sessionOwnerCookieName = 'session-owner-email';

  private static resolveHostname(url: string): string {
    try {
      return new URL(url).hostname;
    } catch {
      try {
        return new URL(`https://${url}`).hostname;
      } catch (fallbackError) {
        throw new Error(
          `Failed to resolve hostname from URL "${url}": ${
            fallbackError instanceof Error ? fallbackError.message : fallbackError
          }`
        );
      }
    }
  }

  public static async addSessionFreshnessCookie(sessionPath: string, url: string, userEmail?: string): Promise<void> {
    const domain = this.resolveHostname(url);
    const state = JSON.parse(fs.readFileSync(sessionPath, "utf-8"));
    const expires = Math.floor(Date.now() / 1000) + (60 * 60);
    const cookies = Array.isArray(state.cookies) ? state.cookies : [];
    state.cookies = cookies.filter(
      (cookie: Cookie) => cookie.name !== 'session-freshness-check' && cookie.name !== this.sessionOwnerCookieName
    );

    state.cookies.push({
      name: 'session-freshness-check',
      value: 'valid',
      domain: domain,
      path: '/',
      expires,
      httpOnly: false,
      secure: true,
      sameSite: 'Lax',
    });

    if (userEmail) {
      state.cookies.push({
        name: this.sessionOwnerCookieName,
        value: userEmail,
        domain: domain,
        path: '/',
        expires,
        httpOnly: false,
        secure: true,
        sameSite: 'Lax',
      });
    }

    fs.writeFileSync(sessionPath, JSON.stringify(state, null, 2));
  }

  public static isSessionValid(path: string, cookieName: string, expectedUserEmail?: string): boolean {
    if (!fs.existsSync(path)) {
      return false;
    }

    try {
      const data = JSON.parse(fs.readFileSync(path, "utf-8"));
      const cookies = Array.isArray(data?.cookies) ? data.cookies : [];
      const targetCookie = cookies.find(
        (cookie: Cookie) => cookie.name === cookieName
      );
      const ownerCookie = cookies.find(
        (cookie: Cookie) => cookie.name === this.sessionOwnerCookieName
      );

      if (!targetCookie || typeof targetCookie.expires !== "number") {
        return false;
      }

      const expiryMs = targetCookie.expires * 1_000;
      if (!Number.isFinite(expiryMs)) {
        return false;
      }

      if (expiryMs <= Date.now()) {
        return false;
      }

      if (!expectedUserEmail) {
        return true;
      }

      return ownerCookie?.value === expectedUserEmail;
    } catch (error) {
      throw new Error(
        `Could not read session data from ${path}: ${
          error instanceof Error ? error.message : String(error)
        }`
      );
    }
  }

}
