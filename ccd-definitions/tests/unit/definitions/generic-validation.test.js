const expect = require('chai').expect;
const { uniq } = require('lodash');

const path = require('path');
const basePath = path.join(__dirname, '../../../jurisdictions/admin/json');

const Config = {
    AuthorisationCaseEvent: Object.assign(require(path.join(basePath, 'AuthorisationCaseEvent.json')), []),
    AuthorisationCaseField: Object.assign(require(path.join(basePath, 'AuthorisationCaseField.json')), []),
    AuthorisationCaseState: Object.assign(require(path.join(basePath, 'AuthorisationCaseState.json')), []),
    AuthorisationCaseType: Object.assign(require(path.join(basePath, 'AuthorisationCaseType.json')), []),
    AuthorisationComplexType: Object.assign(require(path.join(basePath, 'AuthorisationComplexType.json')), []),
    CaseEvent: Object.assign(require(path.join(basePath, 'CaseEvent.json')), []),
    CaseEventToComplexTypes: Object.assign(require(path.join(basePath, 'CaseEventToComplexTypes.json')), []),
    CaseEventToFields: Object.assign(require(path.join(basePath, 'CaseEventToFields.json')), []),
    CaseField: Object.assign(require(path.join(basePath, 'CaseField.json')), []),
    CaseType: Object.assign(require(path.join(basePath, 'CaseType.json')), []),
    CaseTypeTab: Object.assign(require(path.join(basePath, 'CaseTypeTab.json')), []),
    ComplexTypes: Object.assign(require(path.join(basePath, 'ComplexTypes.json')), []),
    FixedLists: Object.assign(require(path.join(basePath, 'FixedLists.json')), []),
    Jurisdiction: Object.assign(require(path.join(basePath, 'Jurisdiction.json')), []),
    SearchInputFields: Object.assign(require(path.join(basePath, 'SearchInputFields.json')), []),
    RoleToAccessProfiles: Object.assign(require(path.join(basePath, 'RoleToAccessProfiles.json')), []),
    SearchResultFields: Object.assign(require(path.join(basePath, 'SearchResultFields.json')), []),
    State: Object.assign(require(path.join(basePath, 'State.json')), []),
    UserProfile: Object.assign(require(path.join(basePath, 'UserProfile.json')), []),
    WorkBasketInputFields: Object.assign(require(path.join(basePath, 'WorkBasketInputFields.json')), []),
    WorkBasketResultFields: Object.assign(require(path.join(basePath, 'WorkBasketResultFields.json')), [])
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
