#!/usr/bin/env node

const fs = require('fs');
const path = require('path');

const [legacyRoot, generatedRoot, outputRoot, manifestPath] = process.argv.slice(2);

if (!legacyRoot || !generatedRoot || !outputRoot || !manifestPath) {
  throw new Error('Usage: merge-generated-ccd <legacyRoot> <generatedRoot> <outputRoot> <manifest>');
}

const manifest = JSON.parse(fs.readFileSync(manifestPath, 'utf8'));

fs.rmSync(outputRoot, { recursive: true, force: true });
copyDirectory(legacyRoot, outputRoot);

for (const conversion of manifest) {
  for (const sheet of conversion.sheets || ['CaseEvent']) {
    replaceConvertedRows(conversion, sheet);
  }
}

function replaceConvertedRows(conversion, sheet) {
  const rows = generatedRows(conversion, sheet);
  if (rows.length === 0) {
    throw new Error(`No generated ${sheet} rows for ${conversion.caseType}/${conversion.eventId}`);
  }

  const sheetRoot = path.join(outputRoot, conversion.jurisdiction, 'json', sheet);
  const files = jsonFiles(sheetRoot);
  const matches = rowMatcher(sheet, conversion.eventId);
  let replaced = false;

  for (const file of files) {
    const existing = JSON.parse(fs.readFileSync(file, 'utf8'));
    const firstIndex = existing.findIndex(matches);
    if (firstIndex === -1) {
      continue;
    }

    writeJson(file, replaceRowsInPlace(sheet, existing, rows, matches));
    replaced = true;
  }

  if (!replaced) {
    const target = defaultSheetFile(sheetRoot, sheet, conversion.eventId);
    fs.mkdirSync(path.dirname(target), { recursive: true });
    writeJson(target, rows);
  }
}

function generatedRows(conversion, sheet) {
  const sheetRoot = path.join(generatedRoot, conversion.caseType, sheet);
  return jsonFiles(sheetRoot)
    .flatMap(file => JSON.parse(fs.readFileSync(file, 'utf8')))
    .filter(row => rowMatcher(sheet, conversion.eventId)(row))
    .map(row => normaliseGeneratedRow(sheet, row));
}

function replaceRowsInPlace(sheet, existing, rows, matches) {
  const remaining = new Map(rows.map(row => [rowKey(sheet, row), row]));
  const merged = existing.map(row => {
    if (!matches(row)) {
      return row;
    }

    const key = rowKey(sheet, row);
    if (!remaining.has(key)) {
      throw new Error(`No generated ${sheet} row for ${key}`);
    }

    const replacement = remaining.get(key);
    remaining.delete(key);
    return replacement;
  });

  return merged.concat([...remaining.values()]);
}

function rowKey(sheet, row) {
  if (sheet === 'CaseEvent') {
    return row.ID;
  }
  if (sheet === 'AuthorisationCaseEvent') {
    return [row.CaseTypeID || row.CaseTypeId, row.CaseEventID, row.UserRole].join('|');
  }
  if (sheet === 'CaseEventToFields' || sheet === 'CaseEventToComplexTypes') {
    return [
      row.CaseTypeID,
      row.CaseEventID,
      row.CaseFieldID,
      row.ListElementCode || '',
      row.PageID || '',
      row.PageFieldDisplayOrder || '',
      row.FieldShowCondition || ''
    ].join('|');
  }
  return JSON.stringify(row);
}

function normaliseGeneratedRow(sheet, row) {
  const normalised = { ...row };
  if (sheet === 'AuthorisationCaseEvent' && normalised.CaseTypeID) {
    normalised.CaseTypeId = normalised.CaseTypeID;
    delete normalised.CaseTypeID;
  }
  return normalised;
}

function rowMatcher(sheet, eventId) {
  if (sheet === 'CaseEvent') {
    return row => row.ID === eventId;
  }
  return row => row.CaseEventID === eventId;
}

function defaultSheetFile(sheetRoot, sheet, eventId) {
  if (sheet === 'CaseEvent' || sheet === 'CaseEventToFields') {
    return path.join(sheetRoot, `${eventId}.json`);
  }
  return path.join(sheetRoot, `${sheet}.json`);
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

function copyDirectory(from, to) {
  fs.mkdirSync(to, { recursive: true });
  for (const entry of fs.readdirSync(from, { withFileTypes: true })) {
    const source = path.join(from, entry.name);
    const target = path.join(to, entry.name);
    if (entry.isDirectory()) {
      copyDirectory(source, target);
    } else {
      fs.copyFileSync(source, target);
    }
  }
}

function writeJson(file, rows) {
  fs.writeFileSync(file, `${JSON.stringify(rows, null, 2)}\n`);
}
