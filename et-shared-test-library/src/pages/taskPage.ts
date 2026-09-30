import { BasePage } from "./basePage";
import { expect } from "@playwright/test";


export default class TaskPage extends BasePage {

  async validateTaskAssignToUser(){
    //links visible when task assign to user
    await expect(this.page.locator('#action_unclaim')).toContainText('Unassign task');
    await expect(this.page.locator('#action_reassign')).toContainText('Reassign task');
  }

}
