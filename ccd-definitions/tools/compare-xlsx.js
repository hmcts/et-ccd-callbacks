#!/usr/bin/env node

const path = require('path');
const xlsx = require(path.resolve(__dirname, 'ccd-definition-processor/node_modules/xlsx'));

const [expectedPath, actualPath] = process.argv.slice(2);

if (!expectedPath || !actualPath) {
  console.error('Usage: compare-xlsx.js <expected.xlsx> <actual.xlsx>');
  process.exit(2);
}

function workbookRows(file) {
  const workbook = xlsx.readFile(file, { cellDates: false });
  const result = {};
  for (const sheetName of workbook.SheetNames) {
    result[sheetName] = xlsx.utils.sheet_to_json(workbook.Sheets[sheetName], {
      header: 1,
      raw: false,
      defval: '',
    });
  }
  return result;
}

const expected = workbookRows(expectedPath);
const actual = workbookRows(actualPath);
const sheetNames = new Set([...Object.keys(expected), ...Object.keys(actual)]);

for (const sheetName of sheetNames) {
  const expectedSheet = JSON.stringify(expected[sheetName] ?? []);
  const actualSheet = JSON.stringify(actual[sheetName] ?? []);
  if (expectedSheet !== actualSheet) {
    console.error(`${path.basename(actualPath)} differs on sheet ${sheetName}`);
    process.exit(1);
  }
}

console.log(`${path.basename(actualPath)} matches`);
