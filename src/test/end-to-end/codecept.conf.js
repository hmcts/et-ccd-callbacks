const { setCommonPlugins } = require('@codeceptjs/configure');

const testConfig = require('./config.js');

// enable all common plugins https://github.com/codeceptjs/configure#setcommonplugins
setCommonPlugins();

exports.config = {
    tests: './paths/*.js',
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
                    reportName: 'et-ccd-callbacks-functional-test',
                    inlineAssets: true,
                },
            },
        },
    },
    multiple: {
        parallel: {
            chunks: 2,
            browsers: ['firefox']
        }
    },
    plugins: {
        retryFailedStep: {
            enabled: true,
            retries: 1
        },
        screenshotOnFail: {
            enabled: true,
            outputPath: './functional-output/reports/screenshots',
        },
        autoDelay: {
            enabled: true
        }
    },
    name: 'et-ccd-callbacks-tests'
};
