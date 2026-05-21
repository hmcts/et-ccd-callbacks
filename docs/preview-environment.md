# Preview Environment Setup

- [API-Based Setup](#api-based-setup)
- [PR Environment Features](#pr-environment-features)
- [Configuration Options](#configuration-options)
- [Login URLs](#login-urls)
- [Technical Notes](#technical-notes)

This repository includes robust API-based scripts to create a complete local CCD environment, as well as Jenkins and Helm configuration for preview environments in the cluster.

## API-Based Setup

The workspace now includes modern, reliable API-based preview environment setup scripts located in `bin/preview/`. These replace the slower UI-based functional tests with direct API calls for better speed and reliability.

### Quick Setup

```bash
# Complete preview environment setup
./bin/preview/setup-preview-environment.sh

# Or run individual steps
./bin/preview/create-ccd-roles.sh
./bin/preview/import-ccd-definitions.sh
./bin/preview/create-admin-case.sh
./bin/preview/import-ref-data.sh
./bin/preview/add-org-roles-to-users-new.sh
```

### Environment Requirements

Ensure you have the required environment variables set:

```bash
export CCD_DEF_CASE_SERVICE_BASE_URL="http://localhost:4452"
export IDAM_ADMIN_WEB_SERVICE_KEY="your-service-key"
export IDAM_ADMIN_SECRET="your-admin-secret"
export S2S_SECRET="your-s2s-secret"
```

## Pull Request Environments

The Jenkins pipeline creates a separate environment for each pull request that can be used to test changes in isolation.

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

### CCD Definitions

With the unified workspace structure, all jurisdictions (admin, england-wales, scotland) are now managed within the same repository. No submodule configuration is required.

To test changes to specific packages:

```bash
# Build specific package for testing
node tools/build-package.js england-wales --env=preview
node tools/build-package.js scotland --env=preview
node tools/build-package.js admin --env=preview

# Or build all packages
yarn generate-excel:preview
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

Data for the PR environment is created using modern API-based scripts that are faster and more reliable than the previous UI-based approach.

All jurisdictions (admin, england-wales, scotland) are now part of the unified workspace, eliminating the need for submodules.

|| Data              | Script                                    | Description                                 |
||-------------------|-------------------------------------------|---------------------------------------------|
|| CCD roles         | `bin/preview/create-ccd-roles.sh`        | Create all ET roles via API               |
|| CCD configuration | `bin/preview/import-ccd-definitions.sh`  | Import all jurisdiction definitions via API |
|| ECM Admin case    | `bin/preview/create-admin-case.sh`       | Create ECM Admin cases via API            |
|| Reference data    | `bin/preview/import-ref-data.sh`         | Import venue/staff reference data via API |

### API Scripts Benefits

- **Faster execution**: Direct API calls vs slow UI automation
- **More reliable**: Less prone to timeout and UI flakiness
- **Better error handling**: Clear API responses vs DOM inspection
- **Easier maintenance**: Simple script logic vs complex codeceptjs scenarios
- **Modular setup**: Run individual setup steps as needed

