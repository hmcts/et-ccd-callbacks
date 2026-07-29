const testConfig = require('../config.js');

exports.config = {
    tests: testConfig.TestPathToRun,
    output: `${process.cwd()}/${testConfig.TestOutputDir}`,
    helpers: {
        Playwright: {
            url: testConfig.TestUrl,
            show: testConfig.TestShowBrowserWindow,
            restart: false,
            timeout: 2500,
            waitForNavigation: 'domcontentloaded',
            waitForTimeout: 10000,
            ignoreHTTPSErrors: true,
            windowSize: '1920x1080',
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
                    reportDir: testConfig.TestOutputDir || './functional-output',
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
