# Preview Environment for Pull Requests

- [PR Environment Features](#pr-environment-features)
- [Configuration Options](#configuration-options)
- [Login URLs](#login-urls)
- [Technical Notes](#technical-notes)

This repo includes Jenkins and Helm configuration to create a full-stack CCD environment in the preview cluster.

The Jenkins pipeline will create a separate environment for each pull request.

This PR environment can then be used to test changes in isolation.

## PR Environment Features

Each PR environment includes the following features:

- ExUI/CCD common components to enable testing of Employment Tribunal cases.
- CCD Admin Web
- ECM (et-ccd-callbacks service)
- Integration with IdAM AAT. Any logins that work in AAT will work in the PR environment.
- All Employment Tribunals roles created
- England/Wales, Scotland and ECM Admin CCD definitions imported
- ECM Admin case created
- Reference data imported - venues, file locations, staff
- Jenkins pipeline runs unit tests, smoke tests, functional tests (which also double as data setup)

### What is not included
- et-message-handler service. Therefore, no multiples or case transfer (different country) functionality is supported.

### PR Environment Persistence
To prevent the environment for the PR from being deleted once the pipeline completes you must add the label
`enable_keep_helm` to the PR.

## Configuration options
Each PR environment will use the master versions of all CCD configuration and the latest prod image for the et-ccd-callbacks service.

To configure the PR environment to use branch versions the following changes are required

### England/Wales CCD Definition
Update [.gitmodules](https://github.com/hmcts/et-ccd-definitions-admin/blob/master/.gitmodules) to include a branch for the submodule

e.g. to use branch ret-1234:

```
[submodule "et-ccd-definitions-englandwales"]
path = et-ccd-definitions-englandwales
url = https://github.com/hmcts/et-ccd-definitions-englandwales
branch = ret-1234
```

### Scotland CCD Definition
Update [.gitmodules](https://github.com/hmcts/et-ccd-definitions-admin/blob/master/.gitmodules) to include a branch for the submodule

e.g. to use branch ret-1234:

```
[submodule "et-ccd-definitions-scotland"]
path = et-ccd-definitions-scotland
url = https://github.com/hmcts/et-ccd-definitions-scotland
branch = ret-1234
```

### PR Images
The below uses et-cos as an example but is the same process for other services.

Update [preview.template.yaml](https://github.com/hmcts/et-ccd-definitions-admin/blob/master/charts/et-ccd-definitions-admin/values.preview.template.yaml)
to use the appropriate et-cos PR image (note et-cos is the product name used in Azure for et-ccd-callbacks).

This line needs to be changed:

```
image: hmctspublic.azurecr.io/et/cos:latest # or pr
```

e.g. to use the image for et-ccd-callbacks pull request 1234

```
image: hmctspublic.azurecr.io/et/cos:pr-1234
```


## Login URLs

Each PR environment will have its own URLs for accessing ExUI/CCD services.

The URL for a PR can be determined by replacing {PR} in the examples below.

You will need to be on the VPN to access the URLs. Make sure to specify HTTPS protocol in URLs.

| System        | Login URL                                                                                               |
|---------------|---------------------------------------------------------------------------------------------------------|
| ExUI          | https://xui-et-ccd-definitions-admin-pr-{PR}.preview.platform.hmcts.net                                 |
| CCD Admin Web | https://admin-web-et-ccd-definitions-admin-pr-{PR}.preview.platform.hmcts.net                           |
| ET SYA        | https://et-sya-et-ccd-definitions-admin-pr-{PR}.preview.platform.hmcts.net                              |
| ET SYR        | https://et-syr-et-ccd-definitions-admin-pr-{PR}.preview.platform.hmcts.net                              |
| Manage Org    | https://xui-mo-webapp-et-ccd-definitions-admin-pr-{PR}.preview.platform.hmcts.net                       |
| Register Org  | https://xui-mo-webapp-et-ccd-definitions-admin-pr-{PR}.preview.platform.hmcts.net/register-org/register |
| Approve Org   | https://xui-ao-webapp-et-ccd-definitions-admin-pr-{PR}.preview.platform.hmcts.net                       |

## Technical Notes

### CCD Product Chart
The Helm configuration makes use of [charts-ccd](https://github.com/hmcts/chart-ccd)

### Data Setup
All data for the PR environment is created during the functional tests pipeline stage using codeceptjs.

To enable the England/Wales and Scotland CCD definitions to be imported they are included in this repo as submodules.

| Data              | Test file                                                                                                                     | Test Scenario                              |
|-------------------|-------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------|
| CCD roles         | [configImport.js](https://github.com/hmcts/et-ccd-definitions-admin/blob/master/src/test/functional/features/configImport.js) | add all the roles                          |
| CCD configuration | [configImport.js](https://github.com/hmcts/et-ccd-definitions-admin/blob/master/src/test/functional/features/configImport.js) | import the preview CCD configuration files |
| ECM Admin case    | [ecmAdminCase.js](https://github.com/hmcts/et-ccd-definitions-admin/blob/master/src/test/functional/features/ecmAdminCase.js) | create ECM Admin case                      |
| Reference data    | [ecmAdminCase.js](https://github.com/hmcts/et-ccd-definitions-admin/blob/master/src/test/functional/features/ecmAdminCase.js) | create ECM reference data                  |

