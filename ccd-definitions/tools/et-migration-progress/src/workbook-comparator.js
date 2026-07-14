const XLSX = require('xlsx');

const CASE_TYPE = ['CaseTypeID', 'CaseTypeId'];

const IDENTITY_COLUMNS = {
  AuthorisationCaseEvent: [CASE_TYPE, ['CaseEventID'], ['UserRole']],
  AuthorisationCaseField: [CASE_TYPE, ['CaseFieldID'], ['UserRole']],
  AuthorisationCaseState: [CASE_TYPE, ['CaseStateID'], ['UserRole']],
  AuthorisationCaseType: [CASE_TYPE, ['UserRole']],
  AuthorisationComplexType: [
    CASE_TYPE,
    ['CaseFieldID'],
    ['ListElementCode'],
    ['UserRole'],
  ],
  Banner: [['BannerEnabled']],
  CaseEvent: [CASE_TYPE, ['ID']],
  CaseEventToComplexTypes: [
    ['ID'],
    ['CaseEventID'],
    ['CaseFieldID'],
    ['ListElementCode'],
  ],
  CaseEventToFields: [
    CASE_TYPE,
    ['CaseEventID'],
    ['CaseFieldID'],
    ['PageID'],
    ['PageFieldDisplayOrder'],
  ],
  CaseField: [CASE_TYPE, ['ID']],
  CaseRoles: [CASE_TYPE, ['ID']],
  CaseType: [['ID']],
  CaseTypeTab: [
    CASE_TYPE,
    ['TabID'],
    ['CaseFieldID'],
    ['UserRole'],
    ['TabFieldDisplayOrder'],
  ],
  Categories: [CASE_TYPE, ['CategoryID']],
  ChallengeQuestion: [CASE_TYPE, ['ID'], ['QuestionId', 'questionId']],
  ComplexTypes: [['ID'], ['ListElementCode']],
  EventToComplexTypes: [
    ['ID'],
    ['CaseEventID'],
    ['CaseFieldID'],
    ['ListElementCode'],
  ],
  FixedLists: [['ID'], ['ListElementCode']],
  Jurisdiction: [['ID']],
  RoleToAccessProfiles: [CASE_TYPE, ['RoleName']],
  SearchAlias: [CASE_TYPE, ['SearchAliasID'], ['CaseFieldID']],
  SearchCaseResultFields: [
    CASE_TYPE,
    ['CaseFieldID'],
    ['UserRole'],
    ['UseCase'],
  ],
  SearchCasesResultFields: [
    CASE_TYPE,
    ['CaseFieldID'],
    ['UserRole'],
    ['UseCase'],
  ],
  SearchCriteria: [CASE_TYPE],
  SearchInputFields: [
    CASE_TYPE,
    ['CaseFieldID'],
    ['ListElementCode'],
    ['UserRole'],
  ],
  SearchParty: [CASE_TYPE],
  SearchResultFields: [
    CASE_TYPE,
    ['CaseFieldID'],
    ['ListElementCode'],
    ['UserRole'],
  ],
  State: [CASE_TYPE, ['ID']],
  UserProfile: [['UserIDAMId']],
  WorkBasketInputFields: [
    CASE_TYPE,
    ['CaseFieldID'],
    ['ListElementCode'],
    ['UserRole'],
  ],
  WorkBasketResultFields: [
    CASE_TYPE,
    ['CaseFieldID'],
    ['ListElementCode'],
    ['UserRole'],
  ],
};

const INTEGER_COLUMNS = new Set(['PageID', 'DisplayOrder']);

function normaliseValue (column, value) {
  if (value instanceof Date) {
    return value.toISOString();
  }
  if (typeof value === 'string') {
    const lineNormalised = value.replace(/\r\n/g, '\n');
    if (INTEGER_COLUMNS.has(column) && /^(0|[1-9]\d*)$/.test(lineNormalised)) {
      return Number(lineNormalised);
    }
    return lineNormalised;
  }
  return value;
}

function normaliseRow (row) {
  return Object.keys(row)
    .sort()
    .reduce((result, key) => {
      const value = normaliseValue(key, row[key]);
      if (value !== null && value !== undefined && value !== '') {
        result[key] = value;
      }
      return result;
    }, {});
}

function workbookToSheets (workbook) {
  return workbook.SheetNames.reduce((result, sheetName) => {
    const worksheet = workbook.Sheets[sheetName];
    const table = XLSX.utils.sheet_to_json(worksheet, {
      blankrows: false,
      defval: null,
      raw: true,
      range: 2,
    });
    const headerRow =
      XLSX.utils.sheet_to_json(worksheet, {
        blankrows: false,
        defval: null,
        header: 1,
        raw: true,
        range: 2,
      })[0] || [];
    result[sheetName] = {
      headers: headerRow.filter((value) => value !== null && value !== ''),
      rows: table
        .map(normaliseRow)
        .filter((row) => Object.keys(row).length > 0),
    };
    return result;
  }, {});
}

function readWorkbook (filename) {
  return workbookToSheets(
    XLSX.readFile(filename, { cellDates: false, raw: true })
  );
}

function stableStringify (value) {
  if (Array.isArray(value)) {
    return `[${value.map(stableStringify).join(',')}]`;
  }
  if (value && typeof value === 'object') {
    const properties = Object.keys(value)
      .sort()
      .map((key) => `${JSON.stringify(key)}:${stableStringify(value[key])}`);
    return `{${properties.join(',')}}`;
  }
  return JSON.stringify(value);
}

function removeExactMatches (goldenRows, javaRows) {
  const javaByValue = new Map();
  javaRows.forEach((row) => {
    const value = stableStringify(row);
    const matches = javaByValue.get(value) || [];
    matches.push(row);
    javaByValue.set(value, matches);
  });

  const remainingGolden = [];
  let exact = 0;
  goldenRows.forEach((row) => {
    const value = stableStringify(row);
    const matches = javaByValue.get(value);
    if (matches && matches.length > 0) {
      matches.pop();
      exact++;
    } else {
      remainingGolden.push(row);
    }
  });

  return {
    exact,
    golden: remainingGolden,
    java: Array.from(javaByValue.values()).flat(),
  };
}

function identityKey (sheetName, headers, row) {
  let columns = IDENTITY_COLUMNS[sheetName];
  if (!columns && sheetName.endsWith(' Scrubbed')) {
    columns = [['ID'], ['ListElementCode']];
  }
  if (!columns) {
    return null;
  }

  const resolved = columns.map((aliases) =>
    aliases.find((alias) => headers.includes(alias))
  );
  if (resolved.some((column) => !column)) {
    return null;
  }
  return stableStringify(
    resolved.map((column) => (row[column] === undefined ? null : row[column]))
  );
}

function differingCells (left, right) {
  const keys = new Set([...Object.keys(left), ...Object.keys(right)]);
  let differences = 0;
  keys.forEach((key) => {
    if (stableStringify(left[key]) !== stableStringify(right[key])) {
      differences++;
    }
  });
  return differences;
}

function groupByIdentity (sheetName, headers, rows) {
  const groups = new Map();
  const withoutIdentity = [];
  rows.forEach((row) => {
    const identity = identityKey(sheetName, headers, row);
    if (identity === null) {
      withoutIdentity.push(row);
      return;
    }
    const values = groups.get(identity) || [];
    values.push(row);
    groups.set(identity, values);
  });
  return { groups, withoutIdentity };
}

function pairChangedRows (goldenRows, javaRows) {
  const golden = [...goldenRows];
  const java = [...javaRows];
  let changed = 0;
  let changedCells = 0;

  while (golden.length > 0 && java.length > 0) {
    let best = null;
    golden.forEach((goldenRow, goldenIndex) => {
      java.forEach((javaRow, javaIndex) => {
        const cells = differingCells(goldenRow, javaRow);
        if (!best || cells < best.cells) {
          best = { cells, goldenIndex, javaIndex };
        }
      });
    });
    golden.splice(best.goldenIndex, 1);
    java.splice(best.javaIndex, 1);
    changed++;
    changedCells += best.cells;
  }

  return { changed, changedCells, golden, java };
}

function compareSheet (sheetName, goldenSheet, javaSheet) {
  const goldenRows = goldenSheet ? goldenSheet.rows : [];
  const javaRows = javaSheet ? javaSheet.rows : [];
  const headers = Array.from(
    new Set([
      ...(goldenSheet ? goldenSheet.headers : []),
      ...(javaSheet ? javaSheet.headers : []),
    ])
  );
  const unmatched = removeExactMatches(goldenRows, javaRows);
  const goldenGroups = groupByIdentity(sheetName, headers, unmatched.golden);
  const javaGroups = groupByIdentity(sheetName, headers, unmatched.java);
  let changed = 0;
  let changedCells = 0;
  let missing = goldenGroups.withoutIdentity.length;
  let unexpected = javaGroups.withoutIdentity.length;

  const identities = new Set([
    ...goldenGroups.groups.keys(),
    ...javaGroups.groups.keys(),
  ]);
  identities.forEach((identity) => {
    const paired = pairChangedRows(
      goldenGroups.groups.get(identity) || [],
      javaGroups.groups.get(identity) || []
    );
    changed += paired.changed;
    changedCells += paired.changedCells;
    missing += paired.golden.length;
    unexpected += paired.java.length;
  });

  return {
    goldenRows: goldenRows.length,
    javaRows: javaRows.length,
    exact: unmatched.exact,
    changed,
    changedCells,
    missing,
    unexpected,
    remaining: changed + missing + unexpected,
  };
}

function compareWorkbooks (goldenWorkbook, javaWorkbook) {
  const sheets = new Set([
    ...Object.keys(goldenWorkbook),
    ...Object.keys(javaWorkbook),
  ]);
  const result = {};
  sheets.forEach((sheetName) => {
    const comparison = compareSheet(
      sheetName,
      goldenWorkbook[sheetName],
      javaWorkbook[sheetName]
    );
    if (comparison.goldenRows > 0 || comparison.javaRows > 0) {
      result[sheetName] = comparison;
    }
  });
  return result;
}

module.exports = {
  compareSheet,
  compareWorkbooks,
  readWorkbook,
  stableStringify,
  workbookToSheets,
};
