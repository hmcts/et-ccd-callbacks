import { BasePage } from "./basePage";
import { expect, Locator, Page } from "@playwright/test";

export default class ManageTelephoneNotePage extends BasePage {
  private readonly titleField: Locator;
  private readonly noteField: Locator;
  private readonly editButton: Locator;
  private readonly deleteButton: Locator
  private readonly caseNoteList: Locator

  constructor(page: Page) {
    super(page);
    this.editButton = page.locator('#editOrDeleteCaseNote-Edit');
    this.deleteButton = page.locator('#editOrDeleteCaseNote-Delete');
    this.titleField = page.locator('#addCaseNote_title');
    this.noteField = page.locator('#addCaseNote_note');
      this.caseNoteList = page.locator('#caseNoteList');
  }

  async editTelephoneNotes() {
    await this.editButton.check();
    await this.caseNoteList.selectOption({ index: 1 });
    await this.clickContinue();
    await this.noteField.isVisible();
    await this.noteField.fill('This is amended test');
    await this.clickContinue();
    await this.page.locator('text=Check your answers').waitFor({ state: 'visible' });
    await this.clickSubmitButton();
  }
}
