const caseTransferEngWales = require("../pages/caseTransfer/caseTransferEngWales");

async function acceptCaseEvent(I, caseId, eventName) {
    await I.wait(5);
    await I.chooseNextStep(eventName, 3);
    await I.acceptTheCase();
}

async function rejectCaseEvent(I, caseId, eventName) {
    await I.authenticateWithIdam(username, password);
    await I.amOnPage('/case-details/' + caseId);
    await I.chooseNextStep(eventName, 3);
    await I.rejectTheCase();
}

async function submittedState(I, caseId) {
    await I.authenticateWithIdam(username, password);
    await I.amOnPage('/case-details/' + caseId);
}

async function caseDetails(I, caseId, eventName, clerkResponsible, physicalLocation, conciliationTrack) {
    await I.chooseNextStep(eventName, 3);
    await I.wait(3);
    await I.amendTheCaseDetails(clerkResponsible, physicalLocation, conciliationTrack);
}

async function caseDetailsEvent(I, caseId, eventName, clerkResponsible, currentPosition, physicalLocation, conciliationTrack) {
    await I.chooseNextStep(eventName, 3);
    await I.wait(3);
    await I.amendCaseDetailsWithCaseCurrentPosition(clerkResponsible, currentPosition, physicalLocation, conciliationTrack);
}

async function claimantDetails(I, eventName) {
    await I.chooseNextStep(eventName, 3);
    await I.wait(2);
    await I.executeClaimantDetails();
}

async function claimantRepresentative(I, eventName) {
    await I.chooseNextStep(eventName, 3);
    await I.wait(2);
    await I.executeClaimantRepresentative();
}

async function claimantRespondentDetails(I, eventName) {
    await I.chooseNextStep(eventName, 3);
    await I.wait(2);
    await I.executeRespondentDetails();
}

async function respondentRepresentative(I, eventName) {
    await I.chooseNextStep(eventName, 3);
    await I.wait(3);
    await I.executeRespondentRepresentative();
}

async function jurisdiction(I, eventName) {
    await I.chooseNextStep(eventName, 3);
    await I.wait(2);
    await I.executeAddAmendJurisdiction();
}

async function closeCase(I, eventName, clerkResponsible, physicalLocation) {
    await I.chooseNextStep(eventName, 3);
    await I.wait(2);
    await I.executeCloseCase(clerkResponsible, physicalLocation);
}

async function letters(I, eventName) {
    await I.chooseNextStep(eventName, 3);
    await I.wait(2);
    await I.executeLettersEvent();
}

async function restrictedReporting(I, eventName) {
    await I.chooseNextStep(eventName, 3);
    await I.wait(2);
    await I.setRestrictedReporting();
}

async function fixCaseAPI(I, eventName) {
    await I.chooseNextStep(eventName, 3);
    await I.wait(2);
    await I.executeFixCaseAPI();
}

async function bfAction(I, eventName) {
    await I.chooseNextStep(eventName, 3);
    await I.wait(3);
    await I.executeBFAction();
}

async function bfActionsOutstanding(I, eventName) {
    await I.chooseNextStep(eventName, 3);
    await I.executeBFActionsOutstanding();
}

async function listHearing(I, eventName, jurisdiction) {
    await I.chooseNextStep(eventName, 3);
    await I.wait(2);
    await I.executeAddAmendHearing(jurisdiction);
}

async function allocateHearing(I, eventName, jurisdiction) {
    await I.chooseNextStep(eventName, 3);
    await I.wait(2);
    await I.executeAllocateHearing(jurisdiction);
}

async function hearingDetails(I, eventName) {
    await I.chooseNextStep(eventName, 3);
    await I.wait(2);
    await I.executeHearingDetails();
}

async function updateHearingDetails(I, eventName) {
    await I.chooseNextStep(eventName, 3);
    await I.wait(2);
    await I.amendHearingDetails();
}

async function printHearingLists(I, eventName, jurisdiction) {
    await I.chooseNextStep(eventName, 3);
    await I.wait(3);
    await I.executePrintHearingLists(jurisdiction);
}

async function caseTransfer(I, eventName) {
    await I.chooseNextStep(eventName, 3);
    await I.wait(2);
    switch (eventName){
        case "Case Transfer (Eng/Wales)":
            await I.executeCaseTransferEngWales();
            break;
        case "Case Transfer (Scotland)":
            await I.executeCaseTransferScotland();
            break;
        case "Case Transfer to ECM":
            await I.executeCaseTransferECM();
            break;
        default:
            throw new Error("Control arrived at default block");
    }
}

async function judgment(I, eventName) {
    await I.chooseNextStep(eventName, 3);
    await I.wait(2);
    await I.executeJudgment();
}

async function generateReport(I, jurisdiction, caseType, eventName) {
    await I.authenticateWithIdam(username, password);
    await I.wait(2);
    await I.executeCreateReport(jurisdiction, caseType, eventName);
}

async function scheduleHearingDuringTheWeekend(I, eventName, jurisdiction) {
    await I.chooseNextStep(eventName, 3);
    await I.executeHearingListedInWeekend(jurisdiction);
}

async function uploadDocumentEvent(I, eventName) {
    await I.chooseNextStep(eventName, 3);
    await I.executeUploadDocument();
}

async function initialConsideration(I, eventName) {
    await I.chooseNextStep(eventName, 3);
    await I.wait(3);
    await I.startInitialConsideration();
    await I.initialConsiderationRule26();
    await I.initialConsiderationCheckYourAnswers();
}

async function et1Serving(I, eventName) {
    await I.chooseNextStep(eventName, 3);
    await I.wait(3);
    await I.et1ServingProcess();
}

async function et3Notification(I, eventName) {
    await I.chooseNextStep(eventName, 3);
    await I.wait(3);
    await I.et3NotificationProcess
}

async function et3ProcessingPage(I, eventName) {
    await I.chooseNextStep(eventName, 3);
    await I.wait(3);
    await I.et3Processing();
}

async function et3Response(I, eventName) {
    await I.chooseNextStep(eventName, 3);
    await I.wait(3);
    await I.et3ResponseProcess();
}

async function et1Vetting(I, eventName) {
    await I.chooseNextStep(eventName, 3);
    await I.wait(3);
    await I.startet1Vetting();
    await I.minReqInfoET1Vetting();
    await I.minReqInfo2ET1Vetting();
    await I.et1CaseVettingOptions1();
    await I.caseDetails1ET1Vetting();
    await I.caseDetails2ET1Vetting();
    await I.caseDetails3ET1Vetting();
    await I.caseDetails4ET1Vetting();
    await I.furtherQET1Vetting();
    await I.possibleReferal1ET1Vetting();
    await I.possibleReferal2ET1Vetting();
    await I.otherFactorsET1Vetting();
    await I.finalNotesET1Vetting();
    await I.checkYourAnswersET1Vetting();
}

async function createAdminReferral(emailAddress, details) {
    await I.createAdminReferrals(emailAddress, details)
    await I.wait(3);
}

async function createJudgeReferral(emailAddress, details) {
    await I.createJudgeReferrals(emailAddress, details)
    await I.wait(3);
}

async function createLegalRepReferral(emailAddress, details) {
    await I.createLegalRepReferrals(emailAddress, details)
    await I.wait(3);
}

async function clickCreateCase(I) {
    I.wait(3);
    await I.caseListForCreateCase();
    I.wait(2);
    await I.caseJurisdictionForCreateCase();
    I.wait(2);
    await I.caseDateOfReceiptForCreateCase();
    I.wait(2);
    await I.caseTypeOfClaimantForCreateCase();
    I.wait(2);
    await I.caseRespondentsForCreateCase();
    I.wait(2);
    await I.caseClaimantIsWAForCreateCase();
    I.wait(2);
    await I.caseClaimantWAForCreateCase();
    I.wait(2);
    await I.caseOtherDetailsForCreateCase();
    I.wait(2);
    await I.caseClaimantRepresented();
    I.wait(2);
    await I.caseClaimantHearingPreferences();
    I.wait(2);
}

async function verifyApplicationTabs(I) {
    await I.caseApplicationTabs();
}

module.exports = {
    acceptCaseEvent,
    rejectCaseEvent,
    submittedState,
    caseDetails,
    claimantDetails,
    claimantRepresentative,
    claimantRespondentDetails,
    respondentRepresentative,
    restrictedReporting,
    jurisdiction,
    closeCase,
    letters,
    fixCaseAPI,
    bfAction,
    listHearing,
    printHearingLists,
    allocateHearing,
    hearingDetails,
    caseTransfer,
    judgment,
    generateReport,
    updateHearingDetails,
    caseDetailsEvent,
    scheduleHearingDuringTheWeekend,
    bfActionsOutstanding,
    uploadDocumentEvent,
    initialConsideration,
    et1Vetting,
    et1Serving,
    et3ProcessingPage,
    et3Notification,
    et3Response,
    clickCreateCase,
    verifyApplicationTabs
};
