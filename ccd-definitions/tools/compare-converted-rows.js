#!/usr/bin/env node

const fs = require('fs');
const path = require('path');

const [legacyRoot, mergedRoot, manifestPath] = process.argv.slice(2);

if (!legacyRoot || !mergedRoot || !manifestPath) {
  throw new Error('Usage: compare-converted-rows <legacyRoot> <mergedRoot> <manifest>');
}

const manifest = JSON.parse(fs.readFileSync(manifestPath, 'utf8'));
const failures = [];

for (const conversion of manifest) {
  for (const sheet of conversion.sheets || ['CaseEvent']) {
    const expected = selectedRows(legacyRoot, conversion, sheet);
    const actual = selectedRows(mergedRoot, conversion, sheet);
    if (stableJson(expected) !== stableJson(actual)) {
      failures.push(`${conversion.caseType}/${conversion.eventId}/${sheet}`);
    }
  }
}

if (failures.length > 0) {
  throw new Error(`Converted row mismatch:\n${failures.join('\n')}`);
}

function selectedRows(root, conversion, sheet) {
  const sheetRoot = path.join(root, conversion.jurisdiction, 'json', sheet);
  const rows = sheetFiles(sheetRoot, conversion, sheet)
    .flatMap(file => JSON.parse(fs.readFileSync(file, 'utf8')))
    .filter(row => rowMatcher(sheet, conversion)(row))
    .flatMap(row => normaliseRow(sheet, row));

  if (sheet === 'AuthorisationCaseEvent') {
    return rows.sort((left, right) => stableJson(left).localeCompare(stableJson(right)));
  }
  return rows;
}

function sheetFiles(sheetRoot, conversion, sheet) {
  const files = jsonFiles(sheetRoot);
  const configured = conversion.files && conversion.files[sheet];
  if (!configured) {
    return files;
  }

  const allowed = new Set(configured);
  return files.filter(file => allowed.has(path.basename(file)) || allowed.has(path.relative(sheetRoot, file)));
}

function rowMatcher(sheet, conversion) {
  if (sheet === 'CaseEvent') {
    return row => row.CaseTypeID === conversion.caseType && row.ID === conversion.eventId;
  }
  return row => (row.CaseTypeID || row.CaseTypeId) === conversion.caseType
    && row.CaseEventID === conversion.eventId;
}

function normaliseRow(sheet, row) {
  if (sheet !== 'AuthorisationCaseEvent' || !row.AccessControl) {
    return [row];
  }

  return row.AccessControl.flatMap(accessControl =>
    accessControl.UserRoles.map(role => ({
      CaseTypeId: row.CaseTypeID || row.CaseTypeId,
      CaseEventID: row.CaseEventID,
      UserRole: role,
      CRUD: accessControl.CRUD
    }))
  );
}

function stableJson(value) {
  if (Array.isArray(value)) {
    return `[${value.map(stableJson).join(',')}]`;
  }
  if (value && typeof value === 'object') {
    return `{${Object.keys(value).sort().map(key => `${JSON.stringify(key)}:${stableJson(value[key])}`).join(',')}}`;
  }
  return JSON.stringify(value);
}

function jsonFiles(root) {
  if (!fs.existsSync(root)) {
    return [];
  }
  const stat = fs.statSync(root);
  if (stat.isFile()) {
    return root.endsWith('.json') ? [root] : [];
  }
  return fs.readdirSync(root, { withFileTypes: true }).flatMap(entry => {
    const child = path.join(root, entry.name);
    return entry.isDirectory() ? jsonFiles(child) : (entry.name.endsWith('.json') ? [child] : []);
  }).sort();
}
