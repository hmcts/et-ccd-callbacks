import { BasePage } from "./basePage";
import { Locator, Page, expect } from '@playwright/test';
import depositOrderData from '../resources/payload/deposit-order-content.json';

export default class DepositOrderPage extends BasePage {
    private readonly depositAmnt: Locator;
    private readonly depositOrderAgainst: Locator;
    private readonly depositOrderRequestedBy: Locator;
    private readonly depositCover: Locator;
    private readonly depositOrderSentDay: Locator;
    private readonly depositOrderSentMonth: Locator;
    private readonly depositOrderSentYear: Locator;
    private readonly depositDueDay: Locator;
    private readonly depositDueMonth: Locator;
    private readonly depositDueYear: Locator;
    private readonly depositTimeExtNo: Locator;
    private readonly depositReceivedNo: Locator;
    private readonly depositRefundNo: Locator;
    private readonly depositNotes: Locator;
    private readonly depositAmountLabel: Locator;

    constructor(page: Page) {
        super(page);
        this.depositAmnt = page.locator('#depositType_0_Deposit_amount');
        this.depositOrderAgainst = page.locator('#depositType_0_dynamicDepositOrderAgainst');
        this.depositOrderRequestedBy = page.locator('#depositType_0_dynamicDepositRequestedBy');
        this.depositCover = page.locator('#depositType_0_deposit_covers');
        this.depositOrderSentDay = page.locator('#deposit_order_sent-day');
        this.depositOrderSentMonth = page.locator('#deposit_order_sent-month');
        this.depositOrderSentYear = page.locator('#deposit_order_sent-year');
        this.depositDueDay = page.locator('#deposit_due-day');
        this.depositDueMonth = page.locator('#deposit_due-month');
        this.depositDueYear = page.locator('#deposit_due-year');
        this.depositTimeExtNo = page.locator('#depositType_0_deposit_time_ext_No');
        this.depositReceivedNo = page.locator('#depositType_0_depositReceived_No');
        this.depositRefundNo = page.locator('#depositType_0_deposit_refund_No');
        this.depositNotes = page.locator('#depositType_0_depositNotes');
        this.depositAmountLabel = page.locator('[for="depositType_0_Deposit_amount"] span');
    }

    async submitADepositOrder() {
        await expect(this.depositAmountLabel).toContainText('Deposit amount (£)');
        await this.depositAmnt.fill(depositOrderData.depositAmount);
        await this.depositOrderAgainst.selectOption({ label: depositOrderData.respondentName });
        await this.depositOrderRequestedBy.selectOption({ label: depositOrderData.claimantName });
        await this.depositCover.selectOption({ label: depositOrderData.coverTypeAll });

        await this.depositOrderSentDay.fill('3');
        await this.depositOrderSentMonth.fill('3');
        await this.depositOrderSentYear.fill('2025');

        await this.depositDueDay.fill('2');
        await this.depositDueMonth.fill('3');
        await this.depositDueYear.fill('2025');

        await this.depositTimeExtNo.click();
        await this.depositReceivedNo.click();
        await this.depositRefundNo.click();

        await this.depositNotes.fill(depositOrderData.depositNote);
        await this.clickSubmitButton();
    }
}
