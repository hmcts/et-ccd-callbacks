const { I } = inject();

function selectNextStep(action) {
  I.selectOption('#next-step', action);
  I.click('Go');
}

function importStaffData(importFile, summary) {
  selectNextStep('Import Staff Data');
  I.see('Import Staff Data', 'h2');
  I.attachFile('#staffImportFile_file', importFile);
  I.click('Continue');
  I.fillField('#field-trigger-summary', summary);
  I.click('Import');
}

function importVenueData(tribunalOffice, importFile, summary) {
  selectNextStep('Import Venue Data');
  I.see('Import Venue Data', 'h2');
  I.selectOption('#venueImport_venueImportOffice', tribunalOffice);
  I.attachFile('#venueImport_venueImportFile_file', importFile);
  I.click('Continue');
  I.fillField('#field-trigger-summary', summary);
  I.click('Import');
}

module.exports = { importStaffData, importVenueData };
