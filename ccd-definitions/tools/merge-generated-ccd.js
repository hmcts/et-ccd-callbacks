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
  if ((conversion.replaceInAllMatchingFilesSheets || []).includes(sheet)) {
    replaceConvertedRowsInEveryMatchingFile(conversion, sheet, sheetRoot, rows);
    return;
  }

  const files = sheetFiles(sheetRoot, conversion, sheet);
  const matches = rowMatcher(sheet, conversion);
  const remaining = new Map(rows.map(row => [rowKey(sheet, row), row]));
  let replaced = false;
  let appendFile = null;

  for (const file of files) {
    const existing = JSON.parse(fs.readFileSync(file, 'utf8'));
    const fileRows = matchingRowsForFile(sheet, existing, matches, remaining);
    if (fileRows.length === 0) {
      continue;
    }

    writeJson(file, replaceRowsInPlace(sheet, existing, fileRows, matches, false));
    fileRows.forEach(row => remaining.delete(rowKey(sheet, row)));
    replaced = true;
    appendFile ??= file;
  }

  if (remaining.size === 0) {
    return;
  }

  if (appendFile) {
    const existing = JSON.parse(fs.readFileSync(appendFile, 'utf8'));
    writeJson(appendFile, existing.concat([...remaining.values()]));
  } else if (!replaced) {
    const target = defaultSheetFile(sheetRoot, sheet, conversion.eventId);
    fs.mkdirSync(path.dirname(target), { recursive: true });
    writeJson(target, [...remaining.values()]);
  }
}

function replaceConvertedRowsInEveryMatchingFile(conversion, sheet, sheetRoot, rows) {
  const matches = rowMatcher(sheet, conversion);
  for (const file of sheetFiles(sheetRoot, conversion, sheet)) {
    const existing = JSON.parse(fs.readFileSync(file, 'utf8'));
    if (existing.some(matches)) {
      writeJson(file, replaceRowsInPlace(sheet, existing, rows, matches));
    }
  }
}

function generatedRows(conversion, sheet) {
  const sheetRoot = path.join(generatedRoot, conversion.caseType, generatedSheetName(conversion, sheet));
  return jsonFiles(sheetRoot)
    .flatMap(file => JSON.parse(fs.readFileSync(file, 'utf8')))
    .filter(row => rowMatcher(sheet, conversion)(row))
    .map(row => normaliseGeneratedRow(sheet, row));
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

function matchingRowsForFile(sheet, existing, matches, remaining) {
  return existing
    .filter(matches)
    .flatMap(row => rowKeys(sheet, row))
    .filter(key => remaining.has(key))
    .map(key => remaining.get(key));
}

function replaceRowsInPlace(sheet, existing, rows, matches, appendRemaining = true) {
  const remaining = new Map(rows.map(row => [rowKey(sheet, row), row]));
  const merged = existing.flatMap(row => {
    if (!matches(row)) {
      return [row];
    }

    return rowKeys(sheet, row).map(key => {
      if (!remaining.has(key)) {
        throw new Error(`No generated ${sheet} row for ${key}`);
      }

      const replacement = remaining.get(key);
      remaining.delete(key);
      return replacement;
    });
  });

  return appendRemaining ? merged.concat([...remaining.values()]) : merged;
}

function rowKeys(sheet, row) {
  if (sheet === 'AuthorisationCaseEvent' && row.AccessControl) {
    return row.AccessControl.flatMap(accessControl =>
      accessControl.UserRoles.map(role => [
        row.CaseTypeID || row.CaseTypeId,
        row.CaseEventID,
        role
      ].join('|'))
    );
  }
  return [rowKey(sheet, row)];
}

function rowKey(sheet, row) {
  if (sheet === 'CaseEvent') {
    return row.ID;
  }
  if (sheet === 'AuthorisationCaseEvent') {
    return [row.CaseTypeID || row.CaseTypeId, row.CaseEventID, row.UserRole].join('|');
  }
  if (sheet === 'CaseEventToFields' || sheet === 'CaseEventToComplexTypes' || sheet === 'EventToComplexTypes') {
    return [
      row.CaseTypeID,
      row.CaseEventID,
      row.CaseFieldID,
      row.ListElementCode || '',
      row.PageID || '',
      row.PageFieldDisplayOrder || '',
      row.FieldDisplayOrder || '',
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

function rowMatcher(sheet, conversion) {
  if (sheet === 'CaseEvent') {
    return row => row.CaseTypeID === conversion.caseType && row.ID === conversion.eventId;
  }
  if (sheet === 'EventToComplexTypes' || sheet === 'CaseEventToComplexTypes') {
    return row => row.CaseEventID === conversion.eventId;
  }
  return row => (row.CaseTypeID || row.CaseTypeId) === conversion.caseType
    && row.CaseEventID === conversion.eventId;
}

function generatedSheetName(conversion, sheet) {
  return (conversion.generatedSheets && conversion.generatedSheets[sheet]) || sheet;
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
