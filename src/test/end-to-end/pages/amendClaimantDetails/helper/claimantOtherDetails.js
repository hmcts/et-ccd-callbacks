const testConfig = require('../../../config');
const { I } = inject();

function verifyClaimantOtherDetails() {

    I.waitForText('Claimant Details', testConfig.TestTimeToWaitForText);
    I.see('Case Number:');
    I.see('Other details');
    I.see('Occupation (Optional)');
    I.see('Employed from (Optional)');
    I.see('Day');I.see('Month');I.see('Year');
    I.see('Is the employment continuing? (Optional)');
    I.see('Are there any disabilities or special requirements? (Optional)');
    I.see('Notice Period (Optional)');
    I.see('Notice Weeks or Months (Optional)');
    I.see('Notice Period Duration (Optional)');
    I.see('Average weekly hours (Optional)');
    I.see('Pay before tax (Optional)');
    I.see('Pay after tax (Optional)');
    I.see('Weekly, monthly or annual pay (Optional)');
    I.see('Pension Contribution (Optional)');
    I.see('Employee Benefits (Optional)');
    I.see('Employee Benefits Details (Optional)');

    I.fillField('#claimantOtherType_claimant_occupation','Claimant occupation');
    I.fillField('#claimant_employed_from-day','20');
    I.fillField('#claimant_employed_from-month','09');
    I.fillField('#claimant_employed_from-year','2017');
    I.click('#claimantOtherType_claimant_employed_currently_Yes');
    I.fillField('#claimant_employed_notice_period-day','30');
    I.fillField('#claimant_employed_notice_period-month','09');
    I.fillField('#claimant_employed_notice_period-year','2017');
    I.click('#claimantOtherType_claimant_disabled_Yes');
    I.fillField('#claimantOtherType_claimant_disabled_details', 'Walking issues');
    I.click('#claimantOtherType_claimant_notice_period_Yes');
    I.selectOption('#claimantOtherType_claimant_notice_period_unit','2: Months');
    I.fillField('#claimantOtherType_claimant_notice_period_duration','1');
    I.fillField('#claimantOtherType_claimant_average_weekly_hours','40');
    I.fillField('#claimantOtherType_claimant_pay_before_tax','40000');
    I.fillField('#claimantOtherType_claimant_pay_after_tax','35000');
    I.selectOption('#claimantOtherType_claimant_pay_cycle','2: Months');
    I.selectOption('#claimantOtherType_claimant_pension_contribution','3: Not Sure');
    I.fillField('#claimantOtherType_claimant_pension_weekly_contribution','10000');
    I.click('#claimantOtherType_claimant_benefits_Yes');
    I.fillField('#claimantOtherType_claimant_benefits_detail','Moble Phone and Health Insurances');
    I.click('Continue');
}
module.exports = { verifyClaimantOtherDetails };
