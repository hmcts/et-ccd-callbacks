# et-xui-e2e-tests
Employment Tribunal XUI E2E Tests


## Getting Started

### Requirements

To run the application on your pc, please ensure you have the followings:

* [Node.JS](https://nodejs.org/en/)
* [yarn](https://yarnpkg.com/)

### Running the application on local environment
Please note the following before running the e2e test. The claimant is created on the fly , leveraging on API call to
reduce the complexity around account creation. However, all other accounts including caseworker legal rep et al are
available as environmental variables. Therefore, `config.js` must be updated accordingly.
Please install the dependencies with the following `cmd`:
```
$ yarn install
```

To run the e2e test, use the following `cmd`:
```
$ yarn run test:fullfunctional
```

To run the e2e test with specific tag/tags, use the following `cmd`:
```
$ yarn test:local --grep @<your tag>
```

The above `cmd` is important because CI pipeline is configured to use the same `cmd` for jenkins job

To run crossbrowser test on local environment, Please visit
*[saucelab-setup](https://tools.hmcts.net/confluence/display/RQA/Sauce+labs)

after setting up the tunnel please run
```
$ yarn run test:crossbrowser
```

To check any linting issue, please run
```
$ yarn lint
```
For the nsp check to work as part of the git pre commit hook  it requires  running
```
$ npm  install -g nsp
$ yarn test:nsp
These are one time task only -- the step is already included in the git precommit step
```
### Running the application on local environment
Please note the following before running the e2e test `config.js` must be updated accordingly.
Please install the dependencies with the following `cmd`:
```
$ yarn install
```

To run the e2e test in ui mode, use the following `cmd`:
```
$  yarn playwright test --ui 
```

To debug the e2e test
```
$  yarn playwright test --debug
```

To run codegen tool
```
$  npx playwright codegen    
