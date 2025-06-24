const { I } = inject();

function createRole(role) {
  I.click('Manage User Roles');
  I.click('Create User Role');
  I.fillField('role', role);
  I.click('Create');
  I.see('User role created.');
}

function importCaseDefinition(file) {
  I.click('Import Case Definition');
  I.see('Choose file containing case definitions');
  I.attachFile('file', file);
  I.click('Submit');
  I.see('Case Definition data successfully imported');
}

module.exports = { createRole, importCaseDefinition };
