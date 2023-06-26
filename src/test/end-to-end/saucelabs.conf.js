const testConfig = require('./config.js');

const sauceConfig = {
  username: process.env.SAUCE_USERNAME || testConfig.saucelabs.username,
  accessKey: process.env.SAUCE_ACCESS_KEY || testConfig.saucelabs.key,
  tunnelIdentifier: process.env.TUNNEL_IDENTIFIER || testConfig.saucelabs.tunnelId,
  acceptSslCerts: true,
  tags: ['ET'],
  url: testConfig.TestUrl,
};

const browsers = {
  chrome: {
    browser: 'chromium',
    desiredCapabilities: {
      browserName: 'chrome',
      'sauce:options': {
        ...sauceConfig,
      },
    },
  },
  firefox: {
    browser: 'firefox',
    desiredCapabilities: {
      browserName: 'firefox',
      'sauce:options': {
        ...sauceConfig,
      },
    },
  },
  safari: {
    browser: 'webkit',
    desiredCapabilities: {
      browserName: 'safari',
      'sauce:options': {
        ...sauceConfig,
      },
    },
  },
};

exports.config = {
  tests: './*_test.js',
  output: './output',
  helpers: {
    Playwright: {
      url: testConfig.TestUrl,
      show:false,
      waitForNavigation: 'networkidle',
      waitForAction: 100,
      cssSelectorsEnabled: 'true',
      browser:'chromium',
      host: 'ondemand.eu-central-1.saucelabs.com',
      port: 80,
      region: 'eu',
      capabilities: {},

    },
  },
  multiple: {
    basic: {
      browsers: Object.keys(browsers),
    },
  },
  services: {
    'sauce': {
      enabled: true,
      user: sauceConfig.username,
      key: sauceConfig.accessKey,
      browsers: browsers,
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
