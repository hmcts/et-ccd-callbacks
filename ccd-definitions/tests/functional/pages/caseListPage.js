const { I } = inject();

function startCreateCase(jurisdiction, caseType, event) {
  I.click('[href="/cases/case-filter"]');

  I.see('Create Case', 'h1');

  I.selectOption('#cc-jurisdiction', jurisdiction);
  I.waitForEnabled('#cc-case-type', 60);
  I.selectOption('#cc-case-type', caseType);
  I.waitForEnabled('#cc-event', 60);
  I.selectOption('#cc-event', event);

  I.waitForEnabled('[type="submit"]', 60);
  I.click('[type="submit"]');
}

function submitCreateCase(summary) {
  I.see('Create Admin Case', 'h1');
  I.fillField('#field-trigger-summary', summary);
  I.click('[type="submit"]');
}

function searchForCaseType(jurisdiction, caseType, state) {
  I.click('[href="/cases"]');
  I.see('Filters', 'h2');
  I.waitForEnabled('#wb-jurisdiction', 60);
  I.selectOption('#wb-jurisdiction', jurisdiction);
  I.waitForEnabled('#wb-case-type', 60);
  I.selectOption('#wb-case-type', caseType);
  I.waitForEnabled('#wb-case-state', 60);
  I.selectOption('#wb-case-state', state);
  I.click('Apply');
}

function selectCase(name) {
  I.see('Your cases', 'h2');
  I.click(name);
}

module.exports = { startCreateCase, submitCreateCase, searchForCaseType, selectCase };
