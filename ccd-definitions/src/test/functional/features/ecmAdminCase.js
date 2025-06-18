/*global
    tryTo
 */
Feature('ECM Admin Case');

const { I } = inject();
const xuiUrl = process.env.XUI_URL;
const caseListPage = require('../pages/caseListPage');
const ecmAdminPage = require('../pages/ecmAdminPage');
const xuiLogin = require('../pages/xuiLogin');

if (process.env.IMPORT_PREVIEW) {
  Scenario('create ECM Admin case', () => {
    I.amOnPage(xuiUrl);
    xuiLogin.signInWithCredentials();
    tryTo(() => {
      caseListPage.startCreateCase('Employment', 'ECM Admin', 'Create Case');
      caseListPage.submitCreateCase('Created from pipeline');
    });
  });

  Scenario('create ECM reference data', () => {
    I.amOnPage(xuiUrl);
    xuiLogin.signInWithCredentials();

    tryTo(() => {
      caseListPage.searchForCaseType('Employment', 'ECM Admin', 'Any');
      caseListPage.selectCase('ECM Admin');

      ecmAdminPage.importStaffData('resources/staff-import-data.xlsx', 'Imported from pipeline');
      ecmAdminPage.importVenueData('Bristol', 'resources/bristol.xlsx', 'Imported from pipeline');
      ecmAdminPage.importVenueData('Leeds', 'resources/leeds.xlsx', 'Imported from pipeline');
      ecmAdminPage.importVenueData('London Central', 'resources/london-central.xlsx', 'Imported from pipeline');
      ecmAdminPage.importVenueData('London East', 'resources/london-east.xlsx', 'Imported from pipeline');
      ecmAdminPage.importVenueData('London South', 'resources/london-south.xlsx', 'Imported from pipeline');
      ecmAdminPage.importVenueData('Manchester', 'resources/manchester.xlsx', 'Imported from pipeline');
      ecmAdminPage.importVenueData('Midlands East', 'resources/midlands-east.xlsx', 'Imported from pipeline');
      ecmAdminPage.importVenueData('Midlands West', 'resources/midlands-west.xlsx', 'Imported from pipeline');
      ecmAdminPage.importVenueData('Newcastle', 'resources/newcastle.xlsx', 'Imported from pipeline');
      ecmAdminPage.importVenueData('Scotland', 'resources/scotland.xlsx', 'Imported from pipeline');
      ecmAdminPage.importVenueData('Wales', 'resources/wales.xlsx', 'Imported from pipeline');
      ecmAdminPage.importVenueData('Watford', 'resources/watford.xlsx', 'Imported from pipeline');
    });
  });
}
