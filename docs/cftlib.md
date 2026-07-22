# RSE CFT lib

[RSE CFT lib](https://github.com/hmcts/rse-cft-lib) is a Gradle plugin that delivers a local CCD/ExUI environment in
which et-ccd-callbacks can be developed and tested.
The key advantages of CFT lib over using [ECM CCD Docker](https://github.com/hmcts/ecm-ccd-docker) as a development
environment are:
* a lower PC resource requirement through minimising the use of docker
* version control which prevents problems caused by updates to common components impacting local development

The integration between the plugin and et-ccd-callbacks can be found in
* build.gradle - see bootWithCCD
* src/cftlib
* src/main/resources/application-cftlib.properties

The integration will boot the environment with the following automatically created:
* CCD roles
* Users
* CCD definitions imported (requires additional configuration)
* Starts dm-store service for document management

**_Note the CFTLib plugin supports Spring Boot DevTools automatic restart._**

## Version Control
The RSE CFT lib is integrated into this project as a plugin. The version used should be kept up-to-date.
See `build.gradle`:

```
plugins {
    ...
    id 'com.github.hmcts.rse-cft-lib' version '0.19.343'
}
```

The latest version of the RSE CFT lib can be found [here](https://github.com/hmcts/rse-cft-lib/tags)

## Setup

### Prerequisites
- **Java 21**: Required for Spring Boot 3 compatibility
- **Docker**: For RSE IdAM Simulator and other containerized services
- **Git**: For accessing CCD definition repositories
- **Azure Container Registry (ACR) Access**: Authenticate with HMCTS ACR repositories before using CFTLib:
  ```bash
  az acr login --name hmctsprivate && az acr login --name hmctsprod && az acr login --name hmctspublic
  ```

### Environment Variables
| Variable                       | Purpose                                                                                |
|--------------------------------|----------------------------------------------------------------------------------------|
| ET_COS_CFTLIB_DB_PASSWORD      | Local et_cos database password<br/>Set to `postgres`                                   |
| XUI_LD_ID                      | Launch Darkly Client Id                                                                |
| SPRING_PROFILES_ACTIVE         | Set to ```cftlib``` to use cftlib Spring profile                                       |
| CALLBACKS_PROJECT_PATH         | Set to the path of your local callbacks project. Used to import configuration files.   |
| CFTLIB_IMPORT_CCD_DEFS_ON_BOOT | Optional<br/>Set to `false` to prevent CCD definitions from being imported at startup  |
| ET_LAUNCH_DARKLY_SDK_KEY       | ET Launch Darkly SDK Key - this can be retrieved from the et-aat Key Vault             |
| ET_WORK_ALLOCATION             | Optional<br/>Set to `true` to enable Work Allocation containers, services, and stubs (see [Work Allocation CFTLib](work-allocation-cftlib.md)) |
| WA_LAUNCH_DARKLY_SDK_KEY       | Optional<br/>Work Allocation LaunchDarkly SDK key for `wa-workflow-api` and `wa-case-event-handler` (defaults to `sdk-key`) |

These can be set with the ./bin/set_env.sh script. Edit the script to add your own path to config-repos and any missing variables.
Run the script with source, so that the environment variables are set in your current shell that you invoke the gradle command from.
```bash
source ./bin/set_env.sh
```

### RSE IdAM Simulator

Make sure you have the latest RSE IdAM Simulator image
```bash
docker pull hmctspublic.azurecr.io/hmcts/rse/rse-idam-simulator:latest
```

## Run
```bash
    ./gradlew bootWithCCD
```

Once the services are all booted (i.e. when the log messages stop) then ExUI is accessible from:

http://localhost:3000

## Logins

All logins use a password of `password`.

| Username                       | Roles                                                                        | Purpose
|--------------------------------|------------------------------------------------------------------------------| --- 
| englandwales@hmcts.net         | caseworker, caseworker-employment, caseworker-employment-englandwales        | Caseworker access to England/Wales cases
| scotland@hmcts.net             | caseworker, caseworker-employment, caseworker-employment-scotland            | Caseworker access to Scotland cases
| admin@hmcts.net                | caseworker, caseworker-employment, caseworker-employment-api                 | Admin account with access to all cases
| solicitor1@etorganisation1.com | caseworker-employment-legalrep-solicitor                                     | Solicitor account |
| superuser@etorganisation1.com  | caseworker-caa, pui-case-manager, pui-organisation-manager, pui-user-manager | Organisation admin account
| citizen@gmail.com              | citizen                                                                      | Claimant Citizen account
| respondent@gmail.com           | citizen                                                                      | Respondent Citizen account

## Importing CCD Definitions

et-ccd-callbacks uses 3 CCD definition files that are maintained in separate repositories:
* [England/Wales](https://github.com/hmcts/et-ccd-definitions-englandwales)
* [Scotland](https://github.com/hmcts/et-ccd-definitions-scotland)
* [ECM Admin](https://github.com/hmcts/et-ccd-definitions-admin)

CCD definitions can be imported automatically at startup and also imported manually.

In order for the CCD definitions to be imported automatically as part of the Gradle bootWithCCD task it is
necessary to
* Configure where the local version is located
* Generate the cftlib versions locally

### Configure local CCD Definition locations
The following environment variables must be set to point to the local directory of the repository:

| Repository                      | Environment Variable 
|---------------------------------| --- 
| et-ccd-definitions-englandwales | ENGLANDWALES_CCD_CONFIG_PATH
| et-ccd-definitions-scotland     | SCOTLAND_CCD_CONFIG_PATH
| et-ccd-definitions-admin        | ADMIN_CCD_CONFIG_PATH

### Generate cftlib CCD definitions
From each of the CCD definition repositories execute:
```bash
yarn generate-excel-cftlib
```

### Import CCD definitions manually
The CCD definition locations must have been configured as in the previous section and the version 
of the definitions to be imported must have been generated. 

Execute the following
```bash
./bin/import-ccd-definition.sh [e|s|a]
```
The argument should be:
* `e` for importing England/Wales
* `s` for importing Scotland
* `a` for importing ECM Admin

### Configure CCD definitions not to be automatically imported
It is possible to configure the bootWithCCD task not to automatically import CCD definitions.
In this scenario it then becomes a manual task to import CCD definitions.

Set the following environment variable to `false`
```bash
CFTLIB_IMPORT_CCD_DEFS_ON_BOOT
```

## Reset
Execute the following command to remove all docker resources created by the CFTlib plugin.
```bash
./bin/cftlib-clean.sh
```

## Work Allocation

Work Allocation support is integrated into CFTLib and can be enabled on demand. For full documentation on architecture, setup steps, environment variables, and testing, see [Work Allocation with CFTLib](work-allocation-cftlib.md).

To start CFTLib with Work Allocation enabled:

```bash
./gradlew bootWithCcdAndWa
```

Or using the environment variable:

```bash
ET_WORK_ALLOCATION=true ./gradlew bootWithCCD
```
