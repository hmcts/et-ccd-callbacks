const testConfig = require('./config.js');
exports.config = {
  tests: './paths/*.js',
  output: `${process.cwd()}/${testConfig.TestOutputDir}`,
  helpers: {
    WebDriverIO: {
      host: 'ondemand.saucelabs.com',
      port: 443,
      protocol: 'https',
      url: testConfig.TestUrl,
      show: false,
      user: process.env.SAUCE_USERNAME || testConfig.saucelabs.username,
      key: process.env.SAUCE_ACCESS_KEY || testConfig.saucelabs.key,
      tunnelIdentifier: process.env.TUNNEL_IDENTIFIER || testConfig.saucelabs.tunnelId,
      acceptSslCerts: true,
      tags: ['ET'],
      desiredCapabilities: {
        // Common capabilities for all browsers
        build: process.env.SAUCE_BUILD_NAME || 'ET-CCD-X-Browser', // Set a build name in the environment variable
        platform: 'any',
        screenResolution: '1280x1024',
        name: 'ET ccd-callbacks Crossbrowser Tests',
      },
    },
  },
  plugins: {
    wdio: {
      enabled: true,
      services: ['sauce'],
    },
  },
  multiple: {
    parallel: {
      chunks: 2, // Number of browsers to run in parallel
    },
    // Define configurations for different browsers
    browsers: {
      chrome: {
        desiredCapabilities: {
          browserName: 'chrome',
          browserVersion: 'latest',
        },
      },
      firefox: {
        desiredCapabilities: {
          browserName: 'firefox',
          browserVersion: 'latest',
        },
      },
      safari: {
        desiredCapabilities: {
          browserName: 'safari',
          browserVersion: 'latest',
        },
      },
    },
  },
};