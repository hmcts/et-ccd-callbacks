const parseArgs = require('minimist');

const run = require('../src/main/json2xlsx');

run(parseArgs(process.argv.slice(2), {
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
})).catch(err => console.log(err.toString()));
