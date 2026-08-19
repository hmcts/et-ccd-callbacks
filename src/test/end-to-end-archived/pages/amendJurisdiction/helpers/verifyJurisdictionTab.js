const {I} = inject();

function verifyJurisdictionTab(jurisdictionNumber, jurisdictionCodeValue, jurisdictionCodeDescription, jurisdictionOutcome, disposalDateFlag = false) {

    I.click('//div[contains(text(),\'Jurisdictions\')]');
    I.see("Jurisdiction");
    I.see("Jurisdiction " + jurisdictionNumber);
    I.see("Jurisdiction Code");
    I.see(jurisdictionCodeValue);
    I.see(jurisdictionCodeDescription);
    I.see("Outcome");
    I.see(jurisdictionOutcome);
    if (disposalDateFlag) {
        I.see("Disposal date");
    }
}

module.exports = {verifyJurisdictionTab};
