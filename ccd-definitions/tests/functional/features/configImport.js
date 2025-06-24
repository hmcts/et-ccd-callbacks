/*global
    tryTo
 */
Feature('CCD Admin Web');

const { I } = inject();
const ccdAdminLogin = require('../pages/ccdAdminLogin');
const ccdAdminWeb = require('../pages/ccdAdminWeb');
const ccdAdminUrl = process.env.CCD_ADMIN_URL;

if (process.env.IMPORT_PREVIEW) {
  Scenario('add all the roles', () => {
    I.amOnPage(ccdAdminUrl);
    ccdAdminLogin.signInWithCredentials();

    tryTo(() => ccdAdminWeb.createRole('caseworker'));
    tryTo(() => ccdAdminWeb.createRole('caseworker-employment'));
    tryTo(() => ccdAdminWeb.createRole('caseworker-employment-api'));
    tryTo(() => ccdAdminWeb.createRole('caseworker-employment-englandwales'));
    tryTo(() => ccdAdminWeb.createRole('caseworker-employment-scotland'));

    tryTo(() => ccdAdminWeb.createRole('caseworker-employment-etjudge'));
    tryTo(() => ccdAdminWeb.createRole('caseworker-employment-etjudge-englandwales'));
    tryTo(() => ccdAdminWeb.createRole('caseworker-employment-etjudge-scotland'));

    tryTo(() => ccdAdminWeb.createRole('caseworker-et-pcqextractor'));
    tryTo(() => ccdAdminWeb.createRole('citizen'));
    tryTo(() => ccdAdminWeb.createRole('caseworker-employment-legalrep-solicitor'));
    tryTo(() => ccdAdminWeb.createRole('caseworker-approver'));
    tryTo(() => ccdAdminWeb.createRole('caseworker-caa'));
    tryTo(() => ccdAdminWeb.createRole('et-acas-api'));
    tryTo(() => ccdAdminWeb.createRole('GS_profile'));

    tryTo(() => ccdAdminWeb.createRole('caseworker-ras-validation'));
    tryTo(() => ccdAdminWeb.createRole('caseworker-wa-task-configuration'));
    tryTo(() => ccdAdminWeb.createRole('TTL_profile'));
    tryTo(() => ccdAdminWeb.createRole('caseworker-wa'));
    tryTo(() => ccdAdminWeb.createRole('caseworker-wa-task-officer'));
    tryTo(() => ccdAdminWeb.createRole('caseworker-caa'));
    tryTo(() => ccdAdminWeb.createRole('caseworker-approver'));
    tryTo(() => ccdAdminWeb.createRole('caseworker-employment-etjudge-leeds'));
    tryTo(() => ccdAdminWeb.createRole('caseworker-employment-leeds'));
  });

  Scenario('import the preview CCD configuration files', () => {
    I.amOnPage(ccdAdminUrl);
    ccdAdminLogin.signInWithCredentials();
    let adminFile = '../../../definitions/xlsx/et-admin-ccd-config-preview.xlsx';
    tryTo(() => ccdAdminWeb.importCaseDefinition(adminFile));

    let englandFile =
      '../../../et-ccd-definitions-englandwales/definitions/xlsx/et-englandwales-ccd-config-preview.xlsx';
    tryTo(() => ccdAdminWeb.importCaseDefinition(englandFile));

    let scotlandFile = '../../../et-ccd-definitions-scotland/definitions/xlsx/et-scotland-ccd-config-preview.xlsx';
    tryTo(() => ccdAdminWeb.importCaseDefinition(scotlandFile));
  });
}
