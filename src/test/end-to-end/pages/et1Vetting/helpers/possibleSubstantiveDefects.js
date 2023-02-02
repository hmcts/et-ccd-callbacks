const testConfig = require('../../../../config');
const commonConfig = require('../../../data/commonConfig.json');
const { I } = inject();

function verifyPossibleSubstantiveDefects() {

    I.waitForText('General notes (Optional)', testConfig.TestTimeToWaitForText);
    I.see('ET1 case vetting');
    I.see('Case Number:');
    I.see('Possible substantive defects (Optional)');
    I.see('Select all that apply. Does the claim, or part of it, appear to be a claim which:');
    I.see('The tribunal has no jurisdiction to consider - Rule 12(1)(a)');
    I.see('Is in a form which cannot sensibly be responded to or otherwise an abuse of process - Rule 12 (1)(b)');
    I.see('Has neither an EC number nor claims one of the EC exemptions - Rule 12 (1)(c)');
    I.see('States that one of the EC exceptions applies but it might not - Rule 12 (1)(d)');
    I.see('Institutes relevant proceedings and the EC number on the claim form does not match the EC number on the Acas certificate - Rule 12 (1)(da)');
    I.see('Has a different claimant name on the ET1 to the claimant name on the Acas certificate - Rule 12 (1)(e)');
    I.see('Has a different claimant name on the ET1 to the claimant name on the Acas certificate - Rule 12 (1)(e)');
    I.see('Has a different respondent name on the ET1 to the respondent name on the Acas certificate - Rule 12 (1)(f)');
    I.see('General notes (Optional)')

    I.checkOption("[value='rule121a']");
    I.checkOption("[value='rule121b']");
    I.checkOption("[value='rule121c']");
    I.checkOption("[value='rule121d']");
    I.checkOption("[value='rule121 da'] ");
    I.checkOption("[value='rule121e']");
    I.checkOption("[value='rule121f']");

    I.see('The tribunal has no jurisdiction to consider');
    I.see('Is in a form which cannot sensibly be responded to or otherwise an abuse of process');
    I.see('Has neither an EC number nor claims one of the EC exemptions');
    I.see('States that one of the EC exceptions applies but it might not');
    I.see('Institutes relevant proceedings and the EC number on the claim form does not match the EC number on the Acas certificate');
    I.see('Has a different claimant name on the ET1 to the claimant name on the Acas certificate');
    I.see('Has a different respondent name on the ET1 to the respondent name on the Acas certificate');
    I.see('Give details');

    I.fillField('#rule121aTextArea', 'Rule 121 a - Give Details Text');
    I.fillField('#rule121bTextArea', 'Rule 121 b - Give Details Text');
    I.fillField('#rule121cTextArea', 'Rule 121 c - Give Details Text');
    I.fillField('#rule121dTextArea', 'Rule 121 d - Give Details Text');
    I.fillField('#rule121daTextArea', 'Rule 121 da - Give Details Text');
    I.fillField('#rule121eTextArea', 'Rule 121 e - Give Details Text');
    I.fillField('#rule121fTextArea', 'Rule 121 f - Give Details Text');
    I.fillField('#et1SubstantiveDefectsGeneralNotes',"General Notes for Possible substantive defects");
    I.click(commonConfig.continue);
}

module.exports = { verifyPossibleSubstantiveDefects };
