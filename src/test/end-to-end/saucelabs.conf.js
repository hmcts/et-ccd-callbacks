const config = require('../config.js');
const supportedBrowsers = require('../crossbrowser/supportedBrowsers');
const testUrl = process.env.TEST_URL || config.TestUrl;

const waitForTimeout = parseInt(process.env.WAIT_FOR_TIMEOUT) || 45000;
const smartWait = parseInt(process.env.SMART_WAIT) || 30000;
const browser = process.env.BROWSER_GROUP || 'chromium';

const defaultSauceOptions = {
    username: process.env.SAUCE_USERNAME || 'username',
    accessKey: process.env.SAUCE_ACCESS_KEY || 'privatekey',
    tunnelIdentifier: process.env.TUNNEL_IDENTIFIER || 'reformtunnel',
    acceptSslCerts: true,
    windowSize: '1600x900',
    tags: ['et-e2e'],
    extendedDebugging: true,
    capturePerformance: true
};

function merge(intoObject, fromObject) {
    return Object.assign({}, intoObject, fromObject);
}

function getBrowserConfig(browserGroup) {
    const browserConfig = [];
    for (const candidateBrowser in supportedBrowsers[browserGroup]) {
        if (candidateBrowser) {
            const candidateCapabilities = supportedBrowsers[browserGroup][candidateBrowser];
            candidateCapabilities['sauce:options'] = merge(
                defaultSauceOptions, candidateCapabilities['sauce:options']
            );
            browserConfig.push({
                browser: candidateCapabilities.browserName,
                capabilities: candidateCapabilities
            });
        } else {
            console.error('ERROR: supportedBrowsers.js is empty or incorrectly defined');
        }
    }
    return browserConfig;
}

const setupConfig = {
    tests: config.TestPathToRun,
    output: `${process.cwd()}/${config.TestOutputDir}`,
    helpers: {
        Playwright: {
            url: testUrl,
            browser,
            smartWait,
            waitForTimeout,
            cssSelectorsEnabled: 'true',
            host: 'ondemand.eu-central-1.saucelabs.com',
            port: 80,
            region: 'eu',
            capabilities: {}
        },
        MyHelper: {
            require: './helpers/saucelabsHelper.js',
            url: testUrl,
        },
        Mochawesome: {
            uniqueScreenshotNames: 'true'
        }
    },
    plugins: {
        retryFailedStep: {
            enabled: true,
            retries: 2
        },
        autoDelay: {
            enabled: config.TestAutoDelayEnabled,
            delayAfter: 2000
        }
    },
    include: {I: './pages/steps.js'},
    mocha: {
        reporterOptions: {
            'codeceptjs-cli-reporter': {
                stdout: '-',
                options: {
                    steps: true
                }
            },
            'mocha-junit-reporter': {
                stdout: '-',
                options: {
                    mochaFile: `${config.TestOutputDir}/result.xml`
                }
            },
            'mochawesome': {
                stdout: config.TestOutputDir + '/console.log',
                options: {
                    reportDir: config.TestOutputDir,
                    reportName: 'et ccd callback x-browsers test',
                    reportTitle: 'Crossbrowser results for: ' + browser.toUpperCase(),
                    inlineAssets: true
                }
            }
        }
    },
    multiple: {
        // microsoft: {
        //     browsers: getBrowserConfig('microsoft')
        // },
        chrome: {
            browsers: 'chromium'
        },
        firefox: {
            browsers: getBrowserConfig('firefox')
        }
    },
    name: 'ET CCD Callbacks Cross Browser Tests'
};

exports.config = setupConfig;
