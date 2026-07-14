const assert = require('assert');
const fs = require('fs/promises');
const os = require('os');
const path = require('path');
const { describe, it } = require('node:test');
const XLSX = require('xlsx');
const XlsxPopulate = require('xlsx-populate');

const {
  clearTemplateData,
  stageJavaDefinitions,
  validateJavaInput,
} = require('../progress');
const { compareWorkbooks, readWorkbook } = require('../workbook-comparator');
const { workbookToSheets } = require('../workbook-comparator');

function workbook (sheetName, headers, rows) {
  return {
    [sheetName]: { headers, rows },
  };
}

describe('XLSX migration progress comparator', function () {
  it('matches rows as a multiset without using row order', function () {
    const headers = ['CaseTypeID', 'ID', 'Label'];
    const first = { CaseTypeID: 'ET', ID: 'first', Label: 'First' };
    const duplicate = { CaseTypeID: 'ET', ID: 'duplicate', Label: 'Duplicate' };
    const result = compareWorkbooks(
      workbook('CaseField', headers, [first, duplicate, duplicate]),
      workbook('CaseField', headers, [duplicate, first, duplicate])
    ).CaseField;

    assert.deepStrictEqual(result, {
      goldenRows: 3,
      javaRows: 3,
      exact: 3,
      changed: 0,
      changedCells: 0,
      missing: 0,
      unexpected: 0,
      remaining: 0,
    });
  });

  it('counts duplicate occurrences independently', function () {
    const headers = ['CaseTypeID', 'ID'];
    const row = { CaseTypeID: 'ET', ID: 'duplicate' };
    const result = compareWorkbooks(
      workbook('CaseField', headers, [row, row]),
      workbook('CaseField', headers, [row])
    ).CaseField;

    assert.strictEqual(result.exact, 1);
    assert.strictEqual(result.missing, 1);
    assert.strictEqual(result.remaining, 1);
  });

  it('counts a row with the same CCD identity as one changed row', function () {
    const headers = ['CaseTypeID', 'ID', 'Label'];
    const result = compareWorkbooks(
      workbook('CaseField', headers, [
        { CaseTypeID: 'ET', ID: 'field', Label: 'Golden' },
      ]),
      workbook('CaseField', headers, [
        { CaseTypeID: 'ET', ID: 'field', Label: 'Java' },
      ])
    ).CaseField;

    assert.strictEqual(result.changed, 1);
    assert.strictEqual(result.changedCells, 1);
    assert.strictEqual(result.missing, 0);
    assert.strictEqual(result.unexpected, 0);
    assert.strictEqual(result.remaining, 1);
  });

  it('does not pair rows when an external CCD identity changes', function () {
    const headers = ['CaseTypeID', 'ID', 'Label'];
    const result = compareWorkbooks(
      workbook('CaseField', headers, [
        { CaseTypeID: 'ET', ID: 'golden', Label: 'Field' },
      ]),
      workbook('CaseField', headers, [
        { CaseTypeID: 'ET', ID: 'java', Label: 'Field' },
      ])
    ).CaseField;

    assert.strictEqual(result.changed, 0);
    assert.strictEqual(result.missing, 1);
    assert.strictEqual(result.unexpected, 1);
    assert.strictEqual(result.remaining, 2);
  });

  it('normalises canonical integer strings only in CCD integer columns', function () {
    const parsed = workbookToSheets({
      SheetNames: ['CaseEventToFields'],
      Sheets: {
        CaseEventToFields: XLSX.utils.aoa_to_sheet([
          [],
          [],
          ['PageID', 'DisplayOrder', 'PageFieldDisplayOrder', 'Label'],
          ['1', '2', '3', '01'],
        ]),
      },
    }).CaseEventToFields.rows[0];

    assert.deepStrictEqual(parsed, {
      DisplayOrder: 2,
      Label: '01',
      PageFieldDisplayOrder: '3',
      PageID: 1,
    });
  });

  it('does not normalise non-canonical integer strings', function () {
    const parsed = workbookToSheets({
      SheetNames: ['SearchResultFields'],
      Sheets: {
        SearchResultFields: XLSX.utils.aoa_to_sheet([
          [],
          [],
          ['DisplayOrder', 'PageID'],
          ['01', 'one'],
        ]),
      },
    }).SearchResultFields.rows[0];

    assert.deepStrictEqual(parsed, { DisplayOrder: '01', PageID: 'one' });
  });

  it('clears seeded template data without removing the CCD headers', async function () {
    const directory = await fs.mkdtemp(
      path.join(os.tmpdir(), 'et-migration-progress-')
    );
    const source = path.join(directory, 'source.xlsx');
    const destination = path.join(directory, 'empty.xlsx');
    const template = await XlsxPopulate.fromBlankAsync();
    const sheet = template.sheet(0).name('CaseField');
    sheet.cell('A3').value('CaseTypeID');
    sheet.cell('B3').value('ID');
    sheet.cell('A4').value('ET');
    sheet.cell('B4').value('legacyField');
    await template.toFileAsync(source);

    await clearTemplateData(source, destination);
    const result = readWorkbook(destination).CaseField;

    assert.deepStrictEqual(result.headers, ['CaseTypeID', 'ID']);
    assert.deepStrictEqual(result.rows, []);
    await fs.rm(directory, { force: true, recursive: true });
  });

  it('stages generated SDK output under the matching ET workbook sheet', async function () {
    const directory = await fs.mkdtemp(
      path.join(os.tmpdir(), 'et-migration-progress-')
    );
    const generated = path.join(directory, 'generated');
    const staged = path.join(directory, 'staged');
    const source = path.join(
      generated,
      'ET_EnglandWales',
      'CaseEventToComplexTypes',
      'event.json'
    );
    await fs.mkdir(path.dirname(source), { recursive: true });
    await fs.writeFile(source, '[]\n');

    await stageJavaDefinitions(generated, staged);
    const target = path.join(
      staged,
      'england-wales',
      'json',
      'EventToComplexTypes',
      '__jurisdiction-shared.json'
    );

    assert.strictEqual(await fs.readFile(target, 'utf8'), '[]\n');
    await fs.rm(directory, { force: true, recursive: true });
  });

  it('coalesces identical jurisdiction-global rows across case types', async function () {
    const directory = await fs.mkdtemp(
      path.join(os.tmpdir(), 'et-migration-progress-')
    );
    const generated = path.join(directory, 'generated');
    const staged = path.join(directory, 'staged');
    const shared = [
      { ID: 'ImportFile', ListElementCode: 'file', FieldType: 'Document' },
    ];
    for (const caseType of ['ET_Admin', 'Pre_Hearing_Deposit']) {
      const source = path.join(generated, caseType, 'ComplexTypes', 'ImportFile.json');
      await fs.mkdir(path.dirname(source), { recursive: true });
      await fs.writeFile(source, JSON.stringify(shared));
    }

    await stageJavaDefinitions(generated, staged);
    const target = path.join(
      staged,
      'admin',
      'json',
      'ComplexTypes',
      '__jurisdiction-shared.json'
    );

    assert.deepStrictEqual(JSON.parse(await fs.readFile(target, 'utf8')), shared);
    await fs.rm(directory, { force: true, recursive: true });
  });

  it('rejects conflicting jurisdiction-global rows with their owners', async function () {
    const directory = await fs.mkdtemp(
      path.join(os.tmpdir(), 'et-migration-progress-')
    );
    const generated = path.join(directory, 'generated');
    const staged = path.join(directory, 'staged');
    const definitions = [
      ['ET_Admin', 'Document'],
      ['Pre_Hearing_Deposit', 'Text'],
    ];
    for (const [caseType, fieldType] of definitions) {
      const source = path.join(generated, caseType, 'ComplexTypes', 'ImportFile.json');
      await fs.mkdir(path.dirname(source), { recursive: true });
      await fs.writeFile(source, JSON.stringify([
        { ID: 'ImportFile', ListElementCode: 'file', FieldType: fieldType },
      ]));
    }

    await assert.rejects(
      stageJavaDefinitions(generated, staged),
      /Conflicting generated ComplexTypes rows.*ET_Admin, Pre_Hearing_Deposit/
    );
    await fs.rm(directory, { force: true, recursive: true });
  });

  it('rejects generated values in columns that the XLSX processor would discard', async function () {
    const directory = await fs.mkdtemp(
      path.join(os.tmpdir(), 'et-migration-progress-')
    );
    const input = path.join(directory, 'json');
    const templateFile = path.join(directory, 'template.xlsx');
    const definitionFile = path.join(input, 'CaseField', 'fields.json');
    const template = await XlsxPopulate.fromBlankAsync();
    const sheet = template.sheet(0).name('CaseField');
    sheet.cell('A3').value('CaseTypeID');
    sheet.cell('B3').value('ID');
    await template.toFileAsync(templateFile);
    await fs.mkdir(path.dirname(definitionFile), { recursive: true });
    await fs.writeFile(
      definitionFile,
      JSON.stringify([
        {
          CaseTypeID: 'ET',
          ID: 'field',
          Unsupported: 'would disappear',
        },
      ])
    );

    await assert.rejects(
      validateJavaInput(input, templateFile, '*-prod.json'),
      /Unsupported: column is absent from the CaseField template/
    );
    await fs.rm(directory, { force: true, recursive: true });
  });
});
