import { BasePage } from "./basePage";
import { Locator, Page } from "@playwright/test";

export class CaseNotesPage extends BasePage {
  private readonly titleField: Locator;
  private readonly noteField: Locator;

  constructor(page: Page) {
    super(page);
    this.titleField = page.locator('#addCaseNote_title');
    this.noteField = page.locator('#addCaseNote_note');
  }

  async addCaseNotes() {
    await this.titleField.fill('Case Notes');
    await this.noteField.fill('This is test');
    await this.clickSubmitButton();
  }
}
