async function acceptCaseEvent(I, caseId, eventName) {
    await I.wait(5);
    await I.authenticateWithIdam();
    await I.wait(5);
    await I.amOnPage('/case-details/' + caseId);
    await I.chooseNextStep(eventName, 3);
    await I.acceptTheCase();
}

async function caseDetails(I, caseId, eventName, clerkResponcible, physicalLocation, conciliationTrack) {
    await I.chooseNextStep(eventName, 3);
    await I.wait(5);
    await I.amendTheCaseDetails(clerkResponcible, physicalLocation, conciliationTrack);
}

async function claimantDetails(I, eventName) {
    await I.chooseNextStep(eventName, 3);
    await I.wait(5);
    await I.executeClaimantDetails();
}

async function claimantRepresentative(I, eventName) {
    await I.chooseNextStep(eventName, 3);
    await I.wait(5);
    await I.executeClaimantRepresentative();
}

async function claimantRespondentDetails(I, eventName) {
    await I.chooseNextStep(eventName, 3);
    await I.wait(5);
    await I.executeRespondentDetails();
}

async function respondentRepresentative(I, eventName) {
    await I.chooseNextStep(eventName, 3);
    await I.wait(5);
    await I.executeRespondentRepresentative();
}

async function jurisdiction(I, eventName) {
    await I.chooseNextStep(eventName, 3);
    await I.wait(5);
    await I.executeAddAmendJurisdiction();
}

async function closeCase(I, eventName, clerkResponsible, physicalLocation) {
    await I.chooseNextStep(eventName, 3);
    await I.wait(5);
    await I.executeCloseCase(clerkResponsible, physicalLocation);
}

async function letters(I, eventName) {
    await I.chooseNextStep(eventName, 3);
    await I.wait(5);
    await I.executeLettersEvent();
}

async function restrictedReporting(I, eventName) {
    await I.chooseNextStep(eventName, 3);
    await I.wait(5);
    await I.setRestrictedReporting();
}

async function fixCaseAPI(I, eventName) {
    await I.chooseNextStep(eventName, 3);
    await I.wait(3);
    await I.executeFixCaseAPI();
}

async function bfAction(I, eventName) {
    await I.chooseNextStep(eventName, 3);
    await I.wait(3);
    await I.executeBFAction();
}

async function listHearing(I, eventName, jurisdiction) {
    await I.chooseNextStep(eventName, 3);
    await I.wait(3);
    await I.executeAddAmendHearing(jurisdiction);
}

async function allocateHearing(I, eventName, jurisdiction) {
    await I.chooseNextStep(eventName, 3);
    await I.wait(3);
    await I.executeAllocateHearing(jurisdiction);
}

async function hearingDetails(I, eventName) {
    await I.chooseNextStep(eventName, 3);
    await I.wait(3);
    await I.executeHearingDetails();
}

// async function printHearingLists(I, eventName, jurisdiction) {
//     await I.chooseNextStep(eventName, 3);
//     await I.wait(3);
//     await I.executePrintHearingLists(jurisdiction);
// }

async function caseTransfer(I, eventName) {
    await I.chooseNextStep(eventName, 3);
    await I.wait(3);
    await I.executeCaseTransfer();
}

async function judgment(I, eventName) {
    await I.chooseNextStep(eventName, 3);
    await I.wait(3);
    await I.executeJudgment();
}
async function generateReport(I, jurisdiction, caseType, eventName) {
    await I.authenticateWithIdam();
    await I.wait(3);
    await I.executeCreateReport(jurisdiction, caseType, eventName);
}

async function initialConsideration(I, eventName) {
    await I.chooseNextStep(eventName, 3);
    await I.wait(3);
    await I.startInitialConsideration();
    await I.initialConsiderationRule26();
    //await I.initialConsiderationCheckYourAnswers();
}

async function et3ProcessingPage(I, eventName) {
    await I.chooseNextStep(eventName, 3);
    await I.wait(3);
    await I.et3Processing();
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
    //await I.checkYourAnswersET1Vetting();
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

module.exports = {
    acceptCaseEvent,
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
    // printHearingLists,
    allocateHearing,
    hearingDetails,
    caseTransfer,
    judgment,
    generateReport,
    initialConsideration,
    et1Vetting,
    initialConsideration,
    et1Serving,
    et3Notification
};
