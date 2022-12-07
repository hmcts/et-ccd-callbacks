const testConfig = require('../config.js');

exports.config = {
    tests: testConfig.TestPathToRun,
    output: `${process.cwd()}/${testConfig.TestOutputDir}`,
    helpers: {
        Playwright: {
            url: testConfig.TestUrl,
            show: testConfig.TestShowBrowserWindow,
            restart: false,
            timeout: 5000,
            waitForNavigation: 'domcontentloaded',
            waitForTimeout: 10000,
            ignoreHTTPSErrors: true,
            windowSize: '1920x1080',
        },
        /*Puppeteer: {
            url: config.TestUrl,
            waitForTimeout: 40000,
            getPageTimeout: 40000,
            //waitForAction: 1000,
            show: config.TestShowBrowserWindow,
            waitForNavigation: ['domcontentloaded'],
            restart: true,
            keepCookies: false,
            keepBrowserState: false,
            chrome: {
                ignoreHTTPSErrors: true,
                'ignore-certificate-errors': true,
                'defaultViewport': {
                    'width': 1280,
                    'height': 960
                },
                args: [
                    '--headless',
                    '--disable-gpu',
                    '--no-sandbox',
                    '--allow-running-insecure-content',
                    '--ignore-certificate-errors',
                    '--window-size=1440,1400'
                ]
            },
        },*/
        /*PuppeteerHelper: {
            require: './helpers/PuppeteerHelper.js'
        },*/
        REST: {
            endpoint: 'https://idam-api.aat.platform.hmcts.net/loginUser'
        },
        JSWait: {require: './helpers/JSWait.js'},
    },
    include: {
        I: './pages/steps.js'
    },
    plugins: {
        screenshotOnFail: {
            enabled: true,
            fullPageScreenshots: true
        },
        retryFailedStep: {
            enabled: true,
            retries: 1
        },
        autoDelay: {
            enabled: true
        }
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
                stdout: './functional-output/console.log',
                options: {
                    reportDir: config.TestOutputDir || './functional-output',
                    reportFilename: 'et-xui-e2e-result',
                    inlineAssets: true,
                    reportTitle: 'ET XUI E2E Tests'
                }
            }
        }
    },
    multiple: {
        parallel: {
            chunks: 2,
            browsers: ['chrome']
        }
    },
    'name': 'et-ccd-callbacks-tests'
};
