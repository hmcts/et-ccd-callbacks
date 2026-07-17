import {  expect, Page } from "@playwright/test";
import { Selectors } from "./Selectors.ts";

export class Helpers {

    public static async assignTaskToMeAndTriggerNextSteps(
        page: Page,
        taskName: string,
        nextStepsActionName: string,
    ) {
        await this.waitForTask(page, taskName);
        const taskLocator = page.locator("exui-case-task", {
            hasText: taskName,
        });
        await taskLocator.locator(Selectors.a, { hasText: "Assign to me" }).click();
        await page
            .locator(Selectors.alertMessage, {
                hasText: "You've assigned yourself a task. It's available in My tasks.",
            })
            .waitFor();
        await taskLocator
            .locator(Selectors.a, { hasText: nextStepsActionName })
            .click({clickCount: 2, force: true});
        await page.waitForTimeout(2000);
    }

    public static async waitForTask(page: Page, taskName: string, position = 0) {
        // refresh page until the task shows up at the requested position - there can be some delay
        await expect
            .poll(
                async () => {
                    const visible = await page
                        .locator(Selectors.strong, {
                            hasText: taskName,
                        })
                        .nth(position)
                        .isVisible();
                    if (!visible) {
                        await page.reload();
                    }
                    return visible;
                },
                {
                    // Allow 10s delay before retrying
                    intervals: [10_000],
                    // Allow up to a minute for it to become visible
                    timeout: 180_000,
                },
            )
            .toBeTruthy();
    }

}
