import {  expect, Locator, Page } from "@playwright/test";
import { Selectors } from "./Selectors.ts";

export class Helpers {

  public static async assignTaskToMeAndValidateNextSteps(
    page: Page,
    taskName: string,
    nextStepsActionName: string, position: number = 0
  ) : Promise<Locator> {
    await this.waitForTask(page, taskName, position);
    const taskLocator = page.locator("exui-case-task", {
      hasText: taskName,
    }).nth(position);
    await taskLocator.locator(Selectors.a, { hasText: "Assign to me" }).click();
    await page
      .locator(Selectors.alertMessage, {
        hasText: "You've assigned yourself a task. It's available in My tasks.",
      })
      .waitFor();
    const nextStepLoc = taskLocator
        .locator(Selectors.a, { hasText: nextStepsActionName });

    await expect(nextStepLoc).toBeVisible();

    await page.waitForTimeout(2000);
    return nextStepLoc;
  }

    public static async assignTaskToMeAndTriggerNextSteps(
        page: Page,
        taskName: string,
        nextStepsActionName: string, position: number = 0
    ) {
        await (await this.assignTaskToMeAndValidateNextSteps(page, taskName, nextStepsActionName, position))
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
