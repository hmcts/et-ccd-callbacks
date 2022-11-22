const config = require('../config.js');

exports.config = {
    tests: config.TestPathToRun,
    output: `${process.cwd()}/${config.TestOutputDir}`,
    helpers: {
        Puppeteer: {
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
        },
        PuppeteerHelper: {
            require: './helpers/PuppeteerHelper.js'
        },
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
                    includeScreenshots: true,
                    reportDir: config.TestOutputDir || './functional-output',
                    reportFilename: 'ET-CCD-Callbacks-tests',
                    reportTitle: 'ET CCD Callbacks Tests',
                    inline: true,
                    html: true,
                    json: true,
                },
            },
        },
    },
    multiple: {
        parallel: {
            chunks: 2,
            browsers: ['chrome']
        }
    },
    'name': 'et-ccd-callbacks-tests'
};
