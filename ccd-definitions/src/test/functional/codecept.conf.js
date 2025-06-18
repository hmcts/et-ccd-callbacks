const testConfig = require('./config.js');
exports.config = {
  tests: testConfig.tests,
  output: '../../../functional-output',
  helpers: testConfig.helpers,
  timout: 60,
  include: {
    I: './pages/steps.js',
  },
  mocha: {
    reporterEnabled: 'codeceptjs-cli-reporter, mochawesome',
    reporterOptions: {
      'codeceptjs-cli-reporter': {
        stdout: '-',
        options: {
          verbose: false,
          steps: true,
        },
      },
      mochawesome: {
        stdout: 'functional-output/console.log',
        options: {
          includeScreenshots: true,
          reportDir: 'functional-output/reports',
          reportFilename: 'et-ccd-definitions-admin-functional-tests',
          inline: true,
          html: true,
          json: true,
        },
      },
    },
  },
  name: 'et-ccd-definitions-admin-functional-tests',
  plugins: {
    allure: {
      enabled: true,
    },
    pauseOnFail: {
      enabled: false,
    },
    retryFailedStep: {
      enabled: true,
    },
    tryTo: {
      enabled: true,
    },
    screenshotOnFail: {
      enabled: true,
      fullPageScreenshots: true,
    },
  },
};
