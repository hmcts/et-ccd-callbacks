#!/usr/bin/env node

const path = require('path');
const parseArgs = require('minimist');

const { run } = require('../src/progress');

const args = parseArgs(process.argv.slice(2), {
  boolean: ['write-snapshot'],
  string: [
    'definitions-root',
    'java-dir',
    'output-dir',
    'repo-root',
    'snapshot',
  ],
});

const definitionsRoot = path.resolve(
  args['definitions-root'] || path.join(__dirname, '..', '..', '..')
);
const etRoot = path.resolve(definitionsRoot, '..');
const repoRoot = path.resolve(
  args['repo-root'] || path.join(etRoot, '..', '..')
);

run({
  definitionsRoot,
  javaDefinitionsDir: path.resolve(
    args['java-dir'] ||
      path.join(etRoot, 'build', 'ccd-migration', 'java-definitions')
  ),
  outputDir: path.resolve(
    args['output-dir'] || path.join(etRoot, 'build', 'ccd-migration')
  ),
  repoRoot,
  snapshotPath: path.resolve(
    args.snapshot || path.join(definitionsRoot, 'migration-progress.json')
  ),
  writeSnapshot: args['write-snapshot'],
}).catch((error) => {
  console.error(error.stack || error.message || error);
  process.exitCode = 1;
});
