const { spawn } = require('child_process');
const fs = require('fs/promises');
const matcher = require('matcher');
const path = require('path');
const XLSX = require('xlsx');
const XlsxPopulate = require('xlsx-populate');

const accessControl = require('../../ccd-definition-processor/src/main/lib/access-control-transformer');
const {
  compareWorkbooks,
  readWorkbook,
  stableStringify,
} = require('./workbook-comparator');

const ENVIRONMENTS = {
  cftlib: '*-prod.json',
  prod: '*-nonprod.json',
};

const JURISDICTIONS = {
  admin: ['ET_Admin', 'Pre_Hearing_Deposit'],
  'england-wales': [
    'ET_EnglandWales',
    'ET_EnglandWales_Listings',
    'ET_EnglandWales_Multiple',
  ],
  scotland: ['ET_Scotland', 'ET_Scotland_Listings', 'ET_Scotland_Multiple'],
};

const CASE_TYPE_TO_JURISDICTION = Object.entries(JURISDICTIONS).reduce(
  (result, [jurisdiction, caseTypes]) => {
    caseTypes.forEach((caseType) => {
      result[caseType] = jurisdiction;
    });
    return result;
  },
  {}
);

const SHEET_ALIASES = {
  'england-wales': {
    CaseEventToComplexTypes: 'EventToComplexTypes',
    FixedLists: 'EnglandWales Scrubbed',
    SearchCasesResultFields: 'SearchCaseResultFields',
  },
  scotland: {
    CaseEventToComplexTypes: 'EventToComplexTypes',
    FixedLists: 'Scotland Scrubbed',
    SearchCasesResultFields: 'SearchCaseResultFields',
  },
};

const GLOBAL_SHEETS = new Set([
  'CaseEventToComplexTypes',
  'ComplexTypes',
  'EventToComplexTypes',
  'FixedLists',
  'Jurisdiction',
]);

async function listFiles (root, predicate = () => true) {
  try {
    const entries = await fs.readdir(root, { withFileTypes: true });
    const files = [];
    for (const entry of entries) {
      const filename = path.join(root, entry.name);
      if (entry.isDirectory()) {
        files.push(...(await listFiles(filename, predicate)));
      } else if (predicate(filename)) {
        files.push(filename);
      }
    }
    return files;
  } catch (error) {
    if (error.code === 'ENOENT') {
      return [];
    }
    throw error;
  }
}

function sheetNameFor (relativeFilename) {
  const firstPart = relativeFilename.split(path.sep)[0];
  return path.basename(firstPart, '.json');
}

function aliasedSheetName (jurisdiction, sheetName) {
  return (SHEET_ALIASES[jurisdiction] || {})[sheetName] || sheetName;
}

function globalIdentity (sheetName, row) {
  if (sheetName === 'Jurisdiction') {
    return stableStringify([row.ID]);
  }
  if (sheetName === 'ComplexTypes' || sheetName === 'FixedLists' || sheetName.endsWith(' Scrubbed')) {
    return stableStringify([row.ID, row.ListElementCode]);
  }
  return stableStringify([
    row.ID,
    row.CaseEventID,
    row.CaseFieldID,
    row.ListElementCode,
  ]);
}

function aggregateGlobalRows (sheetName, contributions) {
  const byIdentity = new Map();
  contributions.forEach(({ owner, rows }) => {
    rows.forEach((row) => {
      const identity = globalIdentity(sheetName, row);
      const group = byIdentity.get(identity) || new Map();
      const ownerRows = group.get(owner) || [];
      ownerRows.push(row);
      group.set(owner, ownerRows);
      byIdentity.set(identity, group);
    });
  });

  const result = [];
  byIdentity.forEach((owners, identity) => {
    if (owners.size === 1) {
      result.push(...owners.values().next().value);
      return;
    }
    const values = new Set();
    let representative;
    let occurrences = 0;
    owners.forEach((rows) => {
      rows.forEach((row) => values.add(stableStringify(row)));
      representative = representative || rows[0];
      occurrences = Math.max(occurrences, rows.length);
    });
    if (values.size > 1) {
      throw new Error(
        `Conflicting generated ${sheetName} rows for ${identity} from ${Array.from(owners.keys()).join(', ')}`
      );
    }
    for (let index = 0; index < occurrences; index++) {
      result.push(representative);
    }
  });
  return result;
}

async function stageJavaDefinitions (javaDefinitionsDir, destination) {
  await fs.rm(destination, { force: true, recursive: true });
  for (const jurisdiction of Object.keys(JURISDICTIONS)) {
    await fs.mkdir(path.join(destination, jurisdiction, 'json'), {
      recursive: true,
    });
  }

  const caseTypeDirectories = await fs
    .readdir(javaDefinitionsDir, { withFileTypes: true })
    .catch((error) => {
      if (error.code === 'ENOENT') {
        return [];
      }
      throw error;
    });

  const globalContributions = new Map();
  for (const entry of caseTypeDirectories.filter((candidate) =>
    candidate.isDirectory()
  )) {
    const jurisdiction = CASE_TYPE_TO_JURISDICTION[entry.name];
    if (!jurisdiction) {
      throw new Error(
        `Generated definitions contain unknown ET case type directory: ${entry.name}`
      );
    }
    const caseTypeRoot = path.join(javaDefinitionsDir, entry.name);
    const files = await listFiles(caseTypeRoot, (filename) =>
      filename.endsWith('.json')
    );
    for (const filename of files) {
      const relativeFilename = path.relative(caseTypeRoot, filename);
      const sourceSheet = sheetNameFor(relativeFilename);
      const targetSheet = aliasedSheetName(jurisdiction, sourceSheet);
      if (GLOBAL_SHEETS.has(sourceSheet)) {
        const key = `${jurisdiction}\u0000${targetSheet}`;
        const contributions = globalContributions.get(key) || [];
        const rows = JSON.parse(await fs.readFile(filename, 'utf8'));
        if (!Array.isArray(rows)) {
          throw new Error(`Generated definition must be a JSON array: ${filename}`);
        }
        contributions.push({ owner: entry.name, rows });
        globalContributions.set(key, contributions);
        continue;
      }
      const safeFilename = relativeFilename.replaceAll(path.sep, '--');
      const target = path.join(
        destination,
        jurisdiction,
        'json',
        targetSheet,
        `${entry.name}--${safeFilename}`
      );
      await fs.mkdir(path.dirname(target), { recursive: true });
      await fs.copyFile(filename, target);
    }
  }

  for (const [key, contributions] of globalContributions) {
    const [jurisdiction, targetSheet] = key.split('\u0000');
    const target = path.join(
      destination,
      jurisdiction,
      'json',
      targetSheet,
      '__jurisdiction-shared.json'
    );
    await fs.mkdir(path.dirname(target), { recursive: true });
    const rows = aggregateGlobalRows(targetSheet, contributions);
    await fs.writeFile(target, `${JSON.stringify(rows, null, 2)}\n`);
  }
}

async function resolveJavaDefinitionsDir (javaDefinitionsDir, environment) {
  const environmentDefinitions = path.join(javaDefinitionsDir, environment);
  return fs.access(environmentDefinitions)
    .then(() => environmentDefinitions)
    .catch(() => javaDefinitionsDir);
}

async function clearTemplateData (source, destination) {
  const workbook = await XlsxPopulate.fromFileAsync(source);
  workbook.sheets().forEach((sheet) => {
    const usedRange = sheet.usedRange();
    if (usedRange && usedRange.endCell().rowNumber() >= 4) {
      sheet.range(`A4:${usedRange.endCell().address()}`).clear();
    }
  });
  await fs.mkdir(path.dirname(destination), { recursive: true });
  await workbook.toFileAsync(destination);
}

function runProcess (command, args, options) {
  return new Promise((resolve, reject) => {
    const child = spawn(command, args, {
      ...options,
      stdio: ['ignore', 'pipe', 'pipe'],
    });
    let stdout = '';
    let stderr = '';
    child.stdout.on('data', (chunk) => {
      stdout += chunk;
    });
    child.stderr.on('data', (chunk) => {
      stderr += chunk;
    });
    child.on('error', reject);
    child.on('close', (code) => {
      if (code === 0) {
        resolve({ stdout, stderr });
      } else {
        reject(
          new Error(
            [
              `Command failed with exit code ${code}: ${command} ${args.join(
                ' '
              )}`,
              stdout.trim(),
              stderr.trim(),
            ]
              .filter(Boolean)
              .join('\n')
          )
        );
      }
    });
  });
}

function processorEnvironment (environment) {
  const cleanEnvironment = { ...process.env };
  Object.keys(cleanEnvironment)
    .filter(
      (key) =>
        key.startsWith('CCD_DEF') ||
        key.startsWith('ET_COS') ||
        key === 'ET_ENV'
    )
    .forEach((key) => delete cleanEnvironment[key]);
  cleanEnvironment.ET_ENV = environment;
  return cleanEnvironment;
}

async function generateWorkbook ({
  environment,
  excludedPattern,
  input,
  output,
  processor,
  template,
}) {
  await fs.mkdir(path.dirname(output), { recursive: true });
  await runProcess(
    process.execPath,
    [
      processor,
      '-D',
      input,
      '-o',
      output,
      '-t',
      template,
      '-e',
      excludedPattern,
      '--silent',
    ],
    {
      env: processorEnvironment(environment),
    }
  );
}

function meaningfulValue (value) {
  return value !== null && value !== undefined && value !== '';
}

async function validateJavaInput (input, template, excludedPattern) {
  const workbook = XLSX.readFile(template, { raw: true });
  const files = (
    await listFiles(input, (filename) => filename.endsWith('.json'))
  ).filter(
    (filename) =>
      !path
        .relative(input, filename)
        .split(path.sep)
        .some((part) => matcher.isMatch(part, excludedPattern))
  );
  const errors = [];

  for (const filename of files) {
    const relativeFilename = path.relative(input, filename);
    const sheetName = sheetNameFor(relativeFilename);
    const worksheet = workbook.Sheets[sheetName];
    if (!worksheet) {
      errors.push(
        `${relativeFilename}: no ${sheetName} sheet exists in the CCD template`
      );
      continue;
    }
    const headerRows = XLSX.utils.sheet_to_json(worksheet, {
      defval: null,
      header: 1,
      range: 2,
      raw: true,
    });
    const headers = new Set((headerRows[0] || []).filter(meaningfulValue));
    const parsed = JSON.parse(await fs.readFile(filename, 'utf8'));
    if (!Array.isArray(parsed)) {
      errors.push(
        `${relativeFilename}: CCD definition JSON must contain an array of rows`
      );
      continue;
    }
    const rows = accessControl.transform(parsed);
    rows.forEach((row, rowIndex) => {
      Object.entries(row).forEach(([column, value]) => {
        if (meaningfulValue(value) && !headers.has(column)) {
          errors.push(
            `${relativeFilename}[${rowIndex}].${column}: column is absent from the ${sheetName} template`
          );
        }
      });
    });
  }

  if (errors.length > 0) {
    throw new Error(
      `Generated Java definitions would be discarded by the XLSX processor:\n${errors.join(
        '\n'
      )}`
    );
  }
}

function sumComparisons (comparisons) {
  return Object.values(comparisons).reduce(
    (result, comparison) => {
      Object.keys(result).forEach((key) => {
        result[key] += comparison[key];
      });
      return result;
    },
    {
      goldenRows: 0,
      javaRows: 0,
      exact: 0,
      changed: 0,
      changedCells: 0,
      missing: 0,
      unexpected: 0,
      remaining: 0,
    }
  );
}

async function countPhysicalLines (files) {
  let lines = 0;
  for (const filename of files) {
    const content = await fs.readFile(filename, 'utf8');
    if (content.length > 0) {
      lines +=
        (content.match(/\n/g) || []).length + (content.endsWith('\n') ? 0 : 1);
    }
  }
  return lines;
}

function productionJavaFile (filename) {
  const normalised = filename.split(path.sep).join('/');
  return (
    normalised.includes('/src/main/java/') ||
    normalised.includes('/src/ccdMigration/java/')
  );
}

async function javaLineCounts (repoRoot) {
  const etRoot = path.join(repoRoot, 'test-projects', 'et-ccd-callbacks');
  const sdkRoot = path.join(repoRoot, 'sdk');
  const etFiles = await listFiles(
    etRoot,
    (filename) =>
      filename.endsWith('.java') &&
      !filename.includes(`${path.sep}build${path.sep}`)
  );
  const sdkFiles = await listFiles(
    sdkRoot,
    (filename) =>
      filename.endsWith('.java') &&
      !filename.includes(`${path.sep}build${path.sep}`)
  );
  const etMain = etFiles.filter(productionJavaFile);
  const sdkMain = sdkFiles.filter(productionJavaFile);
  const counts = {
    etMain: await countPhysicalLines(etMain),
    etVerification: await countPhysicalLines(
      etFiles.filter((filename) => !productionJavaFile(filename))
    ),
    sdkMain: await countPhysicalLines(sdkMain),
    sdkVerification: await countPhysicalLines(
      sdkFiles.filter((filename) => !productionJavaFile(filename))
    ),
  };
  counts.mainTotal = counts.etMain + counts.sdkMain;
  counts.verificationTotal = counts.etVerification + counts.sdkVerification;
  return counts;
}

function lineDeltas (baseline, current) {
  return Object.keys(current).reduce((result, key) => {
    result[key] = current[key] - (baseline[key] || 0);
    return result;
  }, {});
}

function roundPercentage (value) {
  return Math.round(value * 100) / 100;
}

function buildCurrent (environments, baseline, lines) {
  const totals = sumComparisons(
    Object.fromEntries(
      Object.entries(environments).map(([environment, value]) => [
        environment,
        value.totals,
      ])
    )
  );
  const completed = baseline.remainingDifferences - totals.remaining;
  const completionPercentage =
    baseline.remainingDifferences === 0
      ? 100
      : roundPercentage((completed * 100) / baseline.remainingDifferences);
  const mainLineDelta = lines.mainTotal - baseline.javaLines.mainTotal;

  return {
    environments,
    totals,
    completion: {
      baselineRemainingDifferences: baseline.remainingDifferences,
      completedDifferences: completed,
      percentage: completionPercentage,
    },
    javaLines: {
      current: lines,
      deltaFromBaseline: lineDeltas(baseline.javaLines, lines),
      mainLinesPerCompletedDifference:
        completed > 0
          ? Math.round((mainLineDelta * 100) / completed) / 100
          : null,
    },
  };
}

function pad (value, width) {
  return String(value).padStart(width);
}

function printReport (current) {
  console.log('ET JSON-to-Java XLSX convergence');
  console.log(
    'Environment  Jurisdiction       Golden     Java    Exact  Changed  Missing  Extra  Remaining'
  );
  Object.entries(current.environments).forEach(
    ([environment, environmentResult]) => {
      Object.entries(environmentResult.jurisdictions).forEach(
        ([jurisdiction, result]) => {
          console.log(
            [
              environment.padEnd(12),
              jurisdiction.padEnd(18),
              pad(result.totals.goldenRows, 7),
              pad(result.totals.javaRows, 8),
              pad(result.totals.exact, 8),
              pad(result.totals.changed, 8),
              pad(result.totals.missing, 8),
              pad(result.totals.unexpected, 6),
              pad(result.totals.remaining, 10),
            ].join(' ')
          );
        }
      );
    }
  );
  console.log(
    [
      'TOTAL'.padEnd(31),
      pad(current.totals.goldenRows, 7),
      pad(current.totals.javaRows, 8),
      pad(current.totals.exact, 8),
      pad(current.totals.changed, 8),
      pad(current.totals.missing, 8),
      pad(current.totals.unexpected, 6),
      pad(current.totals.remaining, 10),
    ].join(' ')
  );
  console.log(
    `Completion: ${current.completion.percentage.toFixed(2)}% (${
      current.completion.completedDifferences
    } of ${
      current.completion.baselineRemainingDifferences
    } differences removed)`
  );
  console.log('Java physical lines:');
  Object.entries(current.javaLines.current).forEach(([key, value]) => {
    const delta = current.javaLines.deltaFromBaseline[key];
    console.log(`  ${key}: ${value} (${delta >= 0 ? '+' : ''}${delta})`);
  });
}

async function readSnapshot (snapshotPath) {
  try {
    return JSON.parse(await fs.readFile(snapshotPath, 'utf8'));
  } catch (error) {
    if (error.code === 'ENOENT') {
      return null;
    }
    throw error;
  }
}

async function writeJson (filename, value) {
  await fs.mkdir(path.dirname(filename), { recursive: true });
  await fs.writeFile(filename, `${JSON.stringify(value, null, 2)}\n`);
}

async function run ({
  definitionsRoot,
  javaDefinitionsDir,
  outputDir,
  repoRoot,
  snapshotPath,
  writeSnapshot,
}) {
  const processor = path.join(
    definitionsRoot,
    'tools',
    'ccd-definition-processor',
    'bin',
    'json2xlsx.js'
  );
  const stagedJavaRoot = path.join(outputDir, 'staged-java');

  const clearedTemplates = {};
  for (const jurisdiction of Object.keys(JURISDICTIONS)) {
    const template = path.join(
      definitionsRoot,
      'jurisdictions',
      jurisdiction,
      'data',
      'ccd-template.xlsx'
    );
    const cleared = path.join(
      outputDir,
      'templates',
      jurisdiction,
      'ccd-template-empty.xlsx'
    );
    await clearTemplateData(template, cleared);
    clearedTemplates[jurisdiction] = cleared;
  }

  const environments = {};
  for (const [environment, excludedPattern] of Object.entries(ENVIRONMENTS)) {
    const javaSource = await resolveJavaDefinitionsDir(javaDefinitionsDir, environment);
    const environmentStagedRoot = path.join(stagedJavaRoot, environment);
    await stageJavaDefinitions(javaSource, environmentStagedRoot);
    const jurisdictions = {};
    for (const jurisdiction of Object.keys(JURISDICTIONS)) {
      const goldenInput = path.join(
        definitionsRoot,
        'jurisdictions',
        jurisdiction,
        'json'
      );
      const javaInput = path.join(environmentStagedRoot, jurisdiction, 'json');
      const template = path.join(
        definitionsRoot,
        'jurisdictions',
        jurisdiction,
        'data',
        'ccd-template.xlsx'
      );
      const goldenOutput = path.join(
        outputDir,
        'workbooks',
        environment,
        'golden',
        `${jurisdiction}.xlsx`
      );
      const javaOutput = path.join(
        outputDir,
        'workbooks',
        environment,
        'java',
        `${jurisdiction}.xlsx`
      );
      await validateJavaInput(javaInput, template, excludedPattern);
      await generateWorkbook({
        environment,
        excludedPattern,
        input: goldenInput,
        output: goldenOutput,
        processor,
        template,
      });
      await generateWorkbook({
        environment,
        excludedPattern,
        input: javaInput,
        output: javaOutput,
        processor,
        template: clearedTemplates[jurisdiction],
      });
      const sheets = compareWorkbooks(
        readWorkbook(goldenOutput),
        readWorkbook(javaOutput)
      );
      jurisdictions[jurisdiction] = {
        totals: sumComparisons(sheets),
        sheets,
      };
    }
    environments[environment] = {
      jurisdictions,
      totals: sumComparisons(
        Object.fromEntries(
          Object.entries(jurisdictions).map(([jurisdiction, value]) => [
            jurisdiction,
            value.totals,
          ])
        )
      ),
    };
  }

  const lines = await javaLineCounts(repoRoot);
  const existing = await readSnapshot(snapshotPath);
  if (!existing && !writeSnapshot) {
    throw new Error(
      `No migration progress snapshot exists. Run with --write-snapshot to establish the 0% baseline: ${snapshotPath}`
    );
  }
  const baseline = existing
    ? existing.baseline
    : {
      goldenRows: sumComparisons(
        Object.fromEntries(
          Object.entries(environments).map(([environment, value]) => [
            environment,
            value.totals,
          ])
        )
      ).goldenRows,
      javaLines: lines,
      remainingDifferences: sumComparisons(
        Object.fromEntries(
          Object.entries(environments).map(([environment, value]) => [
            environment,
            value.totals,
          ])
        )
      ).remaining,
    };
  const snapshot = {
    schemaVersion: 1,
    baseline,
    current: buildCurrent(environments, baseline, lines),
  };

  printReport(snapshot.current);
  await writeJson(path.join(outputDir, 'migration-progress.json'), snapshot);

  if (writeSnapshot) {
    await writeJson(snapshotPath, snapshot);
    console.log(`Updated migration progress snapshot: ${snapshotPath}`);
  } else if (stableStringify(existing) !== stableStringify(snapshot)) {
    throw new Error(
      `Migration progress snapshot is stale. Run updateEtMigrationProgress and review the change: ${snapshotPath}`
    );
  } else {
    console.log(`Migration progress snapshot is current: ${snapshotPath}`);
  }

  return snapshot;
}

module.exports = {
  buildCurrent,
  clearTemplateData,
  javaLineCounts,
  resolveJavaDefinitionsDir,
  run,
  stageJavaDefinitions,
  validateJavaInput,
};
