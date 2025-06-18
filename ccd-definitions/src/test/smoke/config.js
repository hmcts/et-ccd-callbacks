const testHeadlessBrowser = true;
const testUrl = process.env.XUI_URL || 'http://localhost:3455';
module.exports = {
  testUrl,
  name: 'et-ccd-definitions-admin-smoke',
  testHeadlessBrowser: true,
  tests: './features/**/*js',
  helpers: {
    Puppeteer: {
      url: testUrl,
      waitForTimeout: 10000,
      waitForAction: 4000,
      getPageTimeout: 60000,
      show: !testHeadlessBrowser,
      waitForNavigation: 'networkidle0',
      ignoreHTTPSErrors: true,
      headless: true,
      browser: 'chrome',
      chrome: {
        ignoreHTTPSErrors: true,
        args: ['--no-sandbox', '--ignore-certificate-errors'],
      },
    },
  },
};
