import { expect, Locator, Page } from "@playwright/test";
import { BasePage } from "./basePage";
import path from "path";
import icPageData from '../resources/payload/ic-page-content.json';
import respPageData from '../resources/payload/respondent-page-content.json';
import { Events } from '../config/case-data.ts';

export default class ICUploadDocPage extends BasePage {
    private readonly respNameEle: Locator;
    private readonly jurisdictionCodeInvalidYes: Locator;
    private readonly invalidDetails: Locator;
    private readonly canProceedYes: Locator;
    private readonly documentHeading: Locator;
    private readonly fileUploadEle: Locator;
    private readonly claimantRespondentHearingPanel: Locator;

    constructor(page: Page) {
        super(page);
        this.respNameEle = page.locator('#etInitialConsiderationRespondentLabel td:nth-child(2)');
        this.jurisdictionCodeInvalidYes = page.locator('#etICJuridictionCodesInvalid_Yes');
        this.invalidDetails = page.locator('#etICInvalidDetails');
        this.canProceedYes = page.locator('#etICCanProceed_Yes');
        this.documentHeading = page.locator('#icDocumentCollection1 h2');
        this.fileUploadEle = page.locator('#icDocumentCollection1_0_uploadedDocument');
        this.claimantRespondentHearingPanel = page.locator('#etIcPartiesHearingPanelPreferenceLabel td');
    }

    async judgeUploadsDocument() {
        const expUrl = Events.initialConsideration.ccdCallback;
        //RET-5795
        await expect(this.page.locator('p').filter({hasText:icPageData.icLandingPageContent}).first()).toBeVisible();
        let actRespNameText = await this.respNameEle.first().textContent();
        expect(actRespNameText?.trim()).toEqual(icPageData.respName);
        await this.jurisdictionCodeInvalidYes.click();
        await this.invalidDetails.fill(icPageData.invalidDetailsText);
        await this.canProceedYes.click();
        await this.clickContinue(expUrl, 2);

        let txtHeading = await this.documentHeading.textContent();
        expect(txtHeading?.trim()).toEqual(icPageData.docHeadingText);
        await this.page.getByLabel(icPageData.hearingOptionText).check();

        // Click and upload a document
        await this.addNewBtn.click();
        await this.commonActionsHelper.uploadWithRateLimitRetry(this.page, this.fileUploadEle,'resources/test_file/test-doc.pdf' )
        await this.delay(2000);
        await this.clickContinue(expUrl+ '/submit');

        await this.clickSubmitButton();
        await expect(this.page.getByText(icPageData.icConfirmationText)).toBeVisible();
        await this.clickCloseAndReturn();
    }

    async verifyRespondentHearingPanelValues() {
      await expect(this.page.locator('p').filter({hasText:icPageData.icLandingPageContent}).first()).toBeVisible();
      await expect(this.claimantRespondentHearingPanel.nth(4)).toContainText(respPageData.preferenceNameisJudge);
      await expect(this.claimantRespondentHearingPanel.nth(5)).toContainText(respPageData.panelReason);
    }

    async verifyClaimantHearingPanelValues() {
        await expect(this.page.locator('p').filter({hasText:icPageData.icLandingPageContent}).first()).toBeVisible();
        await expect(this.claimantRespondentHearingPanel.nth(1)).toContainText(respPageData.preferenceNameisPanel);
        await expect(this.claimantRespondentHearingPanel.nth(2)).toContainText(respPageData.panelReason);
    }

    async verifyJurisdictionCodeInICevent(){
        await expect(this.page.locator('p').filter({hasText:icPageData.icLandingPageContent}).first()).toBeVisible();
        await expect(this.page.locator('#etInitialConsiderationJurisdictionCodesLabel')).toContainText('DAG');
    }
}
