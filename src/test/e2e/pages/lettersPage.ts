import { expect, Locator, Page } from "@playwright/test";
import { BasePage } from "./basePage";
import letterPageData from '../resources/payload/letter-content.json';

export default class LettersPage extends BasePage {
  private readonly part1DocsEle: Locator;
  private readonly part2DocsEle: Locator;
  private readonly hearingNumberEle: Locator;
  private readonly markdownPara: Locator;

  constructor(page: Page) {
    super(page);
    this.part1DocsEle = page.locator('#correspondenceType_topLevel_Documents');
    this.part2DocsEle = page.locator('#correspondenceType_part_2_Documents');
    this.hearingNumberEle = page.locator('#correspondenceType_dynamicHearingNumber');
    this.markdownPara = page.locator('markdown p');
  }

  async generateShortTrackLetter() {
    await expect(this.page.getByText(letterPageData.headerText)).toBeVisible();
    await this.part1DocsEle.selectOption({ label: letterPageData.part1DocOption });
    await expect(this.part2DocsEle).toBeVisible();
    await this.part2DocsEle.selectOption({ label: letterPageData.part2DocOption });
    await expect(this.hearingNumberEle).toBeVisible();

    const selectBox = this.hearingNumberEle;
    const option = await selectBox.locator("option").filter({ hasText: letterPageData.hearingOption }).getAttribute('value');
    if(option) await selectBox.selectOption(option);

    await this.clickSubmitButton();
    await expect(this.closeAndReturnButton).toBeVisible();
    //await expect(this.markdownPara).toContainText(letterPageData.confirmationTxt);
    await this.clickCloseAndReturn();
  }

  async generateNoHearingDateLetter() {
    await expect(this.page.getByText(letterPageData.headerText)).toBeVisible();
    await this.part1DocsEle.selectOption({ label: letterPageData.part1DocOption });
    await expect(this.part2DocsEle).toBeVisible();
    await this.part2DocsEle.selectOption({ label: letterPageData.part2DocOptionNoHearing });

    await this.clickSubmitButton();
    await this.markdownPara.waitFor({ timeout: 30000 });
    await expect(this.markdownPara).toContainText(letterPageData.confirmationTxt);
    await this.clickCloseAndReturn();
  }
}
