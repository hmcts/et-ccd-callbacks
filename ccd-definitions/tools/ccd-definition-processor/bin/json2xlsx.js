const parseArgs = require('minimist');

const run = require('../src/main/json2xlsx');

run(parseArgs(process.argv.slice(2), {
  boolean: [
    'silent'
  ],
  string: [
    'D',
    'o',
    'e',
    't'
  ],
  alias: {
    sheetsDir: 'D',
    destinationXlsx: 'o',
    excludedFilenamePatterns: 'e',
    template: 't'
  }
})).catch(err => {
  console.error(err.stack || err.toString());
  process.exitCode = 1;
});
