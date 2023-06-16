'use strict';
const testConfig = require('../../config');
const beforeYouStartVetting = require('./helpers/beforeYouStartVetting');
const canWeServeTheClaim = require('./helpers/canWeServeTheClaim');
const isThereAnACASCertificate = require('./helpers/isThereAnACASCertificate');
const possibleSubstantiveDefects = require('./helpers/possibleSubstantiveDefects');
const jurisdictionCodes = require('./helpers/jurisdictionCodes');
const trackAllocation = require('./helpers/trackAllocation');
const tribunalLocation = require('./helpers/tribunalLocation');
const listingDetails = require('./helpers/listingDetails');
const furtherDetails = require('./helpers/furtherDetails');
const referalToJudgeOrLegalOfficer = require('./helpers/referalToJudgeOrLegalOfficer');
const referalToRegionalEmploymentJudgeOrVicePresident = require('./helpers/referalToRegionalEmploymentJudgeOrVicePresident');
const includeAnyOtherFactors = require('./helpers/includeAnyOtherFactors');
const additionalInformation = require('./helpers/additionalInformation');
const checkYourAnswers = require('./helpers/checkYourAnswers');

module.exports = async function () {

    const I = this;
    beforeYouStartVetting.verifyBeforeYouStartVetting();
    canWeServeTheClaim.verifyCanWeServeTheClaim();
    isThereAnACASCertificate.verifyIsThereAnACASCertificate();
    possibleSubstantiveDefects.verifyPossibleSubstantiveDefects();
    jurisdictionCodes.verifyJurisdictionCodes();
    trackAllocation.verifyTrackAllocation();
    tribunalLocation.verifyTribunalLocation();
    listingDetails.verifyListingDetails();
    furtherDetails.verifyFurtherDetails();
    referalToJudgeOrLegalOfficer.verifyReferalToAJudgeOrALegalOfficer();
    referalToRegionalEmploymentJudgeOrVicePresident.verifyReferalToARegionalJudgeOrVicepresident();
    includeAnyOtherFactors.verifyIncludeAnyOtherFactors();
    additionalInformation.verifyAdditionalInformation();
    checkYourAnswers.verifyCheckYourAnswers();

    //Confirmation Page Verification and Checking the Status ........
    I.waitForText('You must accept or reject the case or refer the case.', testConfig.TestTimeToWaitForText);
    I.see('ET1 case vetting');
    I.see('Case Number:');
    I.see('Do this next');
    I.click('Close and Return to case details');
    I.waitForText('Case Status: Vetted', testConfig.TestTimeToWaitForText);
};
