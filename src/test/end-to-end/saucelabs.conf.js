const testConfig = require('./config.js');
const supportedBrowsers = require('./supportedBrowsers');

const waitForTimeout = parseInt(testConfig.saucelabs.waitForTimeout);
const smartWait = parseInt(testConfig.saucelabs.smartWait);
const browser = process.env.SAUCE_BROWSER || testConfig.saucelabs.browser;

const defaultSauceOptions = {
  username: process.env.SAUCE_USERNAME || testConfig.saucelabs.username,
  accessKey: process.env.SAUCE_ACCESS_KEY || testConfig.saucelabs.key,
  tunnelIdentifier: process.env.TUNNEL_IDENTIFIER || testConfig.saucelabs.tunnelId,
  acceptSslCerts: true,
  tags: ['ET'],
  url: testConfig.TestUrl,
};

function merge(intoObject, fromObject) {
  return Object.assign({}, intoObject, fromObject);
}

// function getBrowserConfig(browserGroup) {
//   const browserConfig = [];
//   for (const candidateBrowser in supportedBrowsers[browserGroup]) {
//     if (candidateBrowser) {
//       const candidateCapabilities = supportedBrowsers[browserGroup][candidateBrowser];
//       candidateCapabilities['sauce:options'] = merge(defaultSauceOptions, candidateCapabilities['sauce:options']);
//       browserConfig.push({
//         browser: candidateCapabilities.browserName,
//         capabilities: candidateCapabilities,
//       });
//     } else {
//       console.error('ERROR: supportedBrowsers.js is empty or incorrectly defined');
//     }
//   }
//   return browserConfig;
// }

const setupConfig = {
  tests: './paths/*.js',
  output: './functional-output/',
  helpers: {
    Playwright: {
      url: testConfig.TestUrl,
      show:false,
      waitForNavigation: 'networkidle',
      waitForAction: 100,
      browser,
      waitForTimeout,
      smartWait,
      cssSelectorsEnabled: 'true',
      host: 'ondemand.eu-central-1.saucelabs.com',
      port: 80,
      region: 'eu',
      capabilities: {},
    },
  },
  SauceLabs:defaultSauceOptions,
  multiple: {
    saucelabs: {
      browsers: [
        {
          browserName: 'chromium',
          'sauce:options': {
            extendedDebugging: true,
          },
        },
        {
          browserName: 'firefox',
          'sauce:options': {
            extendedDebugging: true,
          },
        },
        {
          browserName: 'webkit',
          'sauce:options': {
            extendedDebugging: true,
          },
        },
        {
          browserName: 'electron',
          'sauce:options': {
            extendedDebugging: true,
          },
        },
      ],
    },
  },

  include: {
    I: './pages/steps.js',
  },

  bootstrap: null,

  mocha: {
    reporterOptions: {
      'codeceptjs-cli-reporter': {
        stdout: '-',
        options: { steps: true },
      },
      mochawesome: {
        stdout: './functional-output/console.log',
        options: {
          reportDir: './functional-output/reports',
          reportName: 'et-ccd-callbacks-xbrowser-test',
          inlineAssets: true,
        },
      },
    },
  },
  name: 'ET ccd-callbacks Crossbrowser Tests',
};

exports.config = setupConfig;
