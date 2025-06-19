const expect = require('chai').expect;
const { uniq } = require('lodash');

const Config = {
    AuthorisationCaseEvent: Object.assign(require('definitions/json/AuthorisationCaseEvent.json'), []),
    AuthorisationCaseField: Object.assign(require('definitions/json/AuthorisationCaseField.json'), []),
    AuthorisationCaseState: Object.assign(require('definitions/json/AuthorisationCaseState.json'), []),
    AuthorisationCaseType: Object.assign(require('definitions/json/AuthorisationCaseType.json'), []),
    AuthorisationComplexType: Object.assign(require('definitions/json/AuthorisationComplexType.json'), []),
    CaseEvent: Object.assign(require('definitions/json/CaseEvent.json'), []),
    CaseEventToComplexTypes: Object.assign(require('definitions/json/CaseEventToComplexTypes.json'), []),
    CaseEventToFields: Object.assign(require('definitions/json/CaseEventToFields'), []),
    CaseField: Object.assign(require('definitions/json/CaseField'), []),
    CaseType: Object.assign(require('definitions/json/CaseType.json'), []),
    CaseTypeTab: Object.assign(require('definitions/json/CaseTypeTab'), []),
    ComplexTypes: Object.assign(require('definitions/json/ComplexTypes.json'), []),
    FixedLists: Object.assign(require('definitions/json/FixedLists.json'), []),
    Jurisdiction: Object.assign(require('definitions/json/Jurisdiction.json'), []),
    SearchInputFields: Object.assign(require('definitions/json/SearchInputFields.json'), []),
    RoleToAccessProfiles: Object.assign(require('definitions/json/RoleToAccessProfiles.json'), []),
    SearchResultFields: Object.assign(require('definitions/json/SearchResultFields.json'), []),
    State: Object.assign(require('definitions/json/State.json'), []),
    UserProfile: Object.assign(require('definitions/json/UserProfile.json'), []),
    WorkBasketInputFields: Object.assign(require('definitions/json/WorkBasketInputFields.json'), []),
    WorkBasketResultFields: Object.assign(require('definitions/json/WorkBasketResultFields.json'), [])
};

describe('For each config sheet', () => {
    it('should have unique rows', () => {
        Object.keys(Config).forEach(sheetName => {
            const originalContent = Config[sheetName];
            const uniqueList = uniq(originalContent);
            expect(uniqueList.length).to.eq(originalContent.length);
        });
    });

    it('should not have any special characters, tabs or line breaks in any of the priority user fields', () => {
        const accepted = /^[\w|*|\-|.|[|\]]+$/;
        const priorityUserFields = ['CaseFieldID', 'CaseStateID', 'ID', 'CaseEventID'];
        Object.keys(Config).forEach(sheetName => {
            const content = Config[sheetName];
            content.forEach(row => {
                priorityUserFields.forEach(field => {
                    const cellValue = row[field];
                    if (cellValue && !cellValue.match(accepted)) {
                        console.log(`Cell ${field} value in sheet ${sheetName} has unexpected characters for value ${cellValue}.`);
                        expect(cellValue.toString()).to.match(accepted);
                    }
                });
            });
        });
    });
});
