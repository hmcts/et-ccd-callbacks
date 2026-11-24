const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const test = require('node:test');

const jurisdictions = [
  ['england-wales', 'ET_EnglandWales_Multiple'],
  ['scotland', 'ET_Scotland_Multiple'],
];
const events = [
  ['addCasesToMultiple', 'Add Cases to Multiple', '/multiples/addCases'],
  ['removeCasesFromMultiple', 'Remove Cases from Multiple', '/multiples/removeCases'],
  ['updateLeadCase', 'Update Lead Case', '/multiples/updateLeadCase'],
];

for (const [jurisdiction, caseType] of jurisdictions) {
  for (const production of [false, true]) {
    const context = `${jurisdiction} ${production ? 'production' : 'non-production'}`;
    const directory = path.resolve(__dirname, '../../jurisdictions', jurisdiction, 'json');
    const rows = sheet =>
      fs
        .readdirSync(path.join(directory, sheet))
        .filter(file => file.endsWith('.json'))
        .filter(file => !file.endsWith(production ? '-nonprod.json' : '-prod.json'))
        .flatMap(file => JSON.parse(fs.readFileSync(path.join(directory, sheet, file), 'utf8')))
        .filter(row => (row.CaseTypeID || row.CaseTypeId) === caseType);
    const caseEvents = rows('CaseEvent');
    const original = caseEvents.find(row => row.ID === 'amendMultipleDetails');
    const eventFields = rows('CaseEventToFields');
    const fields = rows('CaseField');
    const eventPermissions = rows('AuthorisationCaseEvent');
    const fieldPermissions = rows('AuthorisationCaseField');
    test(`${context}: Amend Multiple Details edits only name and note without checkboxes`, () => {
      const amendmentFields = eventFields.filter(row => row.CaseEventID === 'amendMultipleDetails');
      assert.deepEqual(
        amendmentFields.map(row => row.CaseFieldID),
        ['multipleName', 'multipleNote']
      );
      assert.equal(original.CallBackURLAboutToSubmitEvent, '${ET_COS_URL}/multiples/amendDetails');
      assert.equal(original.ShowSummary, 'N');
      assert.ok(
        amendmentFields.every(
          row => !row.FieldShowCondition && !row.PageShowCondition && !row.CallBackURLMidEvent
        )
      );
      assert.equal(amendmentFields[0].DisplayContext, 'MANDATORY');
      assert.equal(amendmentFields[1].DisplayContext, 'OPTIONAL');
      for (const field of amendmentFields) {
        assert.equal(fields.filter(row => row.ID === field.CaseFieldID).length, 1);
        for (const permission of eventPermissions.filter(
          row => row.CaseEventID === 'amendMultipleDetails'
        )) {
          assert.ok(
            fieldPermissions.some(
              row =>
                row.CaseFieldID === field.CaseFieldID &&
                row.UserRole === permission.UserRole &&
                row.CRUD.includes('U')
            )
          );
        }
      }
    });
    if (production) {
      test(`${context}: standalone events and permissions remain non-production-only`, () => {
        for (const [id] of events) {
          assert.ok(!caseEvents.some(row => row.ID === id));
          assert.ok(!eventFields.some(row => row.CaseEventID === id));
          assert.ok(!eventPermissions.some(row => row.CaseEventID === id));
        }
        assert.ok(!fields.some(row => row.ID === 'regenerateSelectionCriteriaLabel'));
        assert.ok(
          !fieldPermissions.some(row => row.CaseFieldID === 'regenerateSelectionCriteriaLabel')
        );
        assert.ok(!fields.some(row => row.ID === 'multipleAmendReview'));
        assert.ok(!fields.some(row => row.ID === 'multipleAmendReviewLabel'));
      });
      continue;
    }

    test(`${context}: standalone events preserve availability and default success banner`, () => {
      assert.ok(original);
      assert.equal(original.CallBackURLAboutToSubmitEvent, '${ET_COS_URL}/multiples/amendDetails');
      for (const [id, name, callback] of events) {
        const matches = caseEvents.filter(row => row.ID === id);
        assert.equal(matches.length, 1);
        const event = matches[0];
        assert.equal(event.Name, name);
        assert.equal(event['PreConditionState(s)'], original['PreConditionState(s)']);
        assert.equal(event.PostConditionState, original.PostConditionState);
        assert.equal(event.EventEnablingCondition, original.EventEnablingCondition);
        assert.equal(event.CallBackURLAboutToSubmitEvent, '${ET_COS_URL}' + callback);
        assert.equal(event.CallBackURLSubmittedEvent, undefined);
        assert.equal(event.CallBackURLAboutToStartEvent, undefined);
        assert.equal(event.ShowSummary, 'N');
        assert.equal(event.EndButtonLabel, 'Submit');
        const pages = eventFields.filter(row => row.CaseEventID === id);
        assert.deepEqual(
          [...new Set(pages.map(row => row.PageID))],
          id === 'updateLeadCase' ? ['Details'] : ['Details', 'Review']
        );
        assert.ok(pages.every(row => row.PageDisplayOrder === (row.PageID === 'Details' ? 1 : 2)));
      }
    });

    test(`${context}: event permissions match Amend Multiple Details`, () => {
      const permissions = id =>
        eventPermissions
          .filter(row => row.CaseEventID === id)
          .map(row => `${row.UserRole}:${row.CRUD}`)
          .sort();
      const expected = permissions('amendMultipleDetails');
      assert.ok(expected.length > 0);
      for (const [id] of events) {
        assert.deepEqual(permissions(id), expected);
      }
    });

    test(`${context}: every standalone page field exists and is readable by event users`, () => {
      for (const [id] of events) {
        const pages = eventFields.filter(row => row.CaseEventID === id);
        assert.ok(pages.length > 0);
        assert.ok(!pages.some(row => row.CaseFieldID === 'typeOfAmendment'));
        for (const page of pages) {
          assert.equal(fields.filter(row => row.ID === page.CaseFieldID).length, 1);
          for (const permission of eventPermissions.filter(row => row.CaseEventID === id)) {
            assert.ok(
              fieldPermissions.some(
                row =>
                  row.CaseFieldID === page.CaseFieldID &&
                  row.UserRole === permission.UserRole &&
                  row.CRUD.includes('R')
              ),
              `${id}/${page.CaseFieldID} missing read permission for ${permission.UserRole}`
            );
          }
        }
      }
    });

    test(`${context}: add/remove review all entered case numbers before submission`, () => {
      for (const [id, field, callback] of [
        ['addCasesToMultiple', 'caseIdCollection', 'addCases/review'],
        ['removeCasesFromMultiple', 'altCaseIdCollection', 'removeCases/review'],
      ]) {
        const pages = eventFields.filter(row => row.CaseEventID === id);
        const selection = pages.find(row => row.CaseFieldID === field);
        assert.equal(pages.length, 3);
        assert.equal(selection.PageID, 'Details');
        assert.equal(selection.PageDisplayOrder, 1);
        assert.equal(selection.DisplayContext, 'MANDATORY');
        assert.equal(selection.ShowSummaryChangeOption, 'N');
        assert.equal(selection.CallBackURLMidEvent, '${ET_COS_URL}/multiples/' + callback);
        assert.equal(caseEvents.find(row => row.ID === id).ShowSummary, 'N');
        assert.ok(!selection.FieldShowCondition && !selection.PageShowCondition);
        const review = pages.find(row => row.CaseFieldID === 'multipleAmendReview');
        const label = pages.find(row => row.CaseFieldID === 'multipleAmendReviewLabel');
        assert.equal(review.PageID, 'Review');
        assert.equal(review.PageLabel, 'Check your answers');
        assert.equal(review.DisplayContext, 'READONLY');
        assert.equal(review.FieldShowCondition, 'multipleAmendReviewLabel="dummy"');
        assert.equal(review.RetainHiddenValue, 'Yes');
        assert.equal(label.PageID, 'Review');
        assert.equal(label.DisplayContext, 'READONLY');
        assert.equal(label.CallBackURLMidEvent, undefined);
        assert.equal(
          fields.find(row => row.ID === 'multipleAmendReviewLabel').Label,
          '${multipleAmendReview}'
        );
      }
      assert.ok(!fields.some(row => row.ID === 'regenerateSelectionCriteriaLabel'));
      assert.ok(
        !fieldPermissions.some(row => row.CaseFieldID === 'regenerateSelectionCriteriaLabel')
      );
    });

    test(`${context}: standalone show conditions reference another unconditional event field`, () => {
      for (const [id] of events) {
        const pages = eventFields.filter(row => row.CaseEventID === id);
        for (const field of pages.filter(row => row.FieldShowCondition)) {
          const condition = field.FieldShowCondition.match(/^\s*(\w+)\s*=/);
          assert.ok(condition, `${id}/${field.CaseFieldID}: unsupported condition`);
          const reference = condition[1];
          assert.notEqual(reference, field.CaseFieldID, `${id}: self-referencing condition`);
          const dependency = pages.find(row => row.CaseFieldID === reference);
          assert.ok(dependency, `${id}: unknown show-condition field ${reference}`);
          assert.ok(!dependency.FieldShowCondition);
        }
      }
    });

    test(`${context}: lead-case show condition references an editable event field`, () => {
      const pages = eventFields.filter(row => row.CaseEventID === 'updateLeadCase');
      const currentLead = pages.find(row => row.CaseFieldID === 'leadCase');
      const newLead = pages.find(row => row.CaseFieldID === 'newLeadCase');
      assert.equal(currentLead.DisplayContext, 'READONLY');
      assert.equal(currentLead.FieldShowCondition, 'newLeadCase="dummy"');
      assert.equal(currentLead.RetainHiddenValue, 'Yes');
      assert.equal(newLead.DisplayContext, 'MANDATORY');
      assert.ok(pages.some(row => row.CaseFieldID === 'leadCaseLabel'));
    });
  }
}
