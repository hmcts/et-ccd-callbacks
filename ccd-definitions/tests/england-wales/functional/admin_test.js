Feature('Admin Web');

Scenario('add all the roles @pipeline', I => {
  I.loginToAdminConsole();
  I.createRole('citizen');
  I.createRole('caseworker');
  I.createRole('caseworker-employment');
  I.createRole('caseworker-employment-englandwales');
  I.createRole('caseworker-employment-etjudge');
  I.createRole('caseworker-employment-etjudge-englandwales');
  I.createRole('caseworker-employment-api');
  I.click('Manage User Roles');
  I.see('citizen');
  I.see('caseworker');
  I.see('caseworker-employment');
  I.see('caseworker-employment-englandwales');
  I.see('caseworker-employment-etjudge');
  I.see('caseworker-employment-etjudge-englandwales');
  I.see('caseworker-employment-api');
}).retry({ retries: 3, minTimeout: 30000 }); // eslint-disable-line no-magic-numbers

if (process.env.IMPORT_PREVIEW) {
  Scenario('upload England/Wales preview CCD configuration @pipeline', I => {
    I.loginToAdminConsole();
    I.uploadConfig(`../../../definitions/xlsx/et-englandwales-ccd-config-${process.env.GIT_COMMIT}.xlsx`);
    I.see('Case Definition data successfully imported');
  }).retry({retries: 3, minTimeout: 30000});

}
