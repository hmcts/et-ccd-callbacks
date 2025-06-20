const testConfig = require('./config.js');
exports.config = {
  tests: testConfig.tests,
  output: '../../../smoke-output',
  helpers: testConfig.helpers,
  timeout: 60,
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
        stdout: 'smoke-output/console.log',
        options: {
          includeScreenshots: true,
          reportDir: 'smoke-output/reports',
          reportFilename: 'et-ccd-definitions-admin-smoke-tests',
          inline: true,
          html: true,
          json: true,
        },
      },
    },
  },
  name: 'et-ccd-definitions-admin-smoke-tests',
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
