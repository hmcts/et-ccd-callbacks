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

### Default Environment Variables

| Variable                         | Purpose                                                                              |
|----------------------------------|--------------------------------------------------------------------------------------|
| JAVA_HOME                        | Directory of your locally installed JDK (/Library/Java/jdk-17.0.4.jdk/Contents/Home) |
| XUI_LD_ID                        | Launch Darkly Client Id                                                              |
| DB_URL                           | Postgresql Localhost DB URL                                                          |
| IDAM_WEB_URL                     | Local IDAM SIMULATOR WEB URL                                                         |
| IDAM_API_URL                     | Local IDAM SIMULATOR API URL                                                         |

### Environment Variables for Notice of Change

| Variable                                      | Purpose                                                                  |
|-----------------------------------------------|--------------------------------------------------------------------------|
| NOTIFY_MCA_API_KEY                            |                                                                          |
| MCA_DS_PROXY_URLS_ALLOWED_LIST                | Manage Case Assignment Allowed Proxy URLs                                |
| MCA_DEF_STORE_PROXY_URLS_ALLOWED_LIS          | Manage Case Assignment Allowed Definition Store Proxy URLs               |
| MCA_CONDITIONAL_APIS_CASE_ASSIGNMENTS_ENABLED | Manage Case Assignment Conditional APIs Case Assignments Enabling Status |
| IDAM_CAA_USERNAME                             | IDAM Caseworker Approver Username                                        |
| IDAM_CAA_PASSWORD                             | IDAM Caseworker Approver Password                                        |
| PRD_HOST                                      | Professional Reference Data Host                                         |
| IDAM_NOC_APPROVER_USERNAME                    | Notice of Change Approver Username                                       |
| IDAM_NOC_APPROVER_PASSWORD                    | Notice of Change Approver Password                                       |

### Environment Variables for CFTLIB
| Variable                       | Purpose                                                                               |
|--------------------------------|---------------------------------------------------------------------------------------|
| ET_COS_CFTLIB_DB_PASSWORD      | Local et_cos database password<br/>Set to `postgres`                                  |
| SPRING_PROFILES_ACTIVE         | Set to ```cftlib``` to use cftlib Spring profile                                      |
| ENGLANDWALES_CCD_CONFIG_PATH   | Set to the path of your local et-ccd-definitions-englandwales GitHub repository       |     
| SCOTLAND_CCD_CONFIG_PATH       | Set to the path of your local et-ccd-definitions-scotland GitHub repository           | 
| ADMIN_CCD_CONFIG_PATH          | Set to the path of your local et-ccd-definitions-admin GitHub repository              |
| CFTLIB_IMPORT_CCD_DEFS_ON_BOOT | Optional<br/>Set to `false` to prevent CCD definitions from being imported at startup |

### Variables to be updated on application.yaml

| Variable                                    | Purpose                                                              |
|---------------------------------------------|----------------------------------------------------------------------|
| ET_COS_DB_PASSWORD                          | Your local DB ET_COS schema password                                 |
| TORNADO_ACCESS_KEY                          | Docmosis Tornado Access Key                                          |
| CREATE_UPDATES_QUEUE_NAME                   | Queue name that you created on Azure                                 | 
| GOV_NOTIFY_API_KEY                          | API key that you created on GOV NOTIFICATION TOOL                    | 
| ET_COS_SYSTEM_USER                          | Employment Tribunals Case Orchestration Service System User          |
| ET_COS_SYSTEM_USER_PASSWORD                 | Employment Tribunals Case Orchestration Service System User Password |

### Variables to be updated on application-cftlib.yaml

| Variable                                    | Purpose                                                              |
|---------------------------------------------|----------------------------------------------------------------------|
| ET_COS_CFTLIB_DB_PASSWORD                   | Employment Tribunals Case Orchestration Service Database Password    |


### Azure Service Bus
You must either provide a connection string in an environment variable to a queue in Azure or
configure a fake service bus.

If you require a real Azure queue then ask one of the team to set this up.

| Variable                                    | Purpose                                             |
|---------------------------------------------|-----------------------------------------------------|
| CREATE_UPDATES_QUEUE_SEND_CONNECTION_STRING | Connection string for create-updates queue in Azure |

Or, if no development queue is available, set the following environment variable to use the fake.

| Variable | Purpose                                             |
| -------- |-----------------------------------------------------|
| SERVICEBUS_FAKE | Set to ```true``` to enable fake service bus client |

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

| Username                       | Roles                                                                        | Purpose                                  |
|--------------------------------|------------------------------------------------------------------------------|------------------------------------------|
| englandwales@hmcts.net         | caseworker, caseworker-employment, caseworker-employment-englandwales        | Caseworker access to England/Wales cases |
| scotland@hmcts.net             | caseworker, caseworker-employment, caseworker-employment-scotland            | Caseworker access to Scotland cases      |
| admin@hmcts.net                | caseworker, caseworker-employment, caseworker-employment-api                 | Admin account with access to all cases   |
| solicitor1@etorganisation1.com | caseworker-employment-legalrep-solicitor                                     | Solicitor account                        |
| superuser@etorganisation1.com  | caseworker-caa, pui-case-manager, pui-organisation-manager, pui-user-manager | Organisation admin account               |
| citizen@gmail.com              | citizen                                                                      | Citizen account                          |

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

| Repository                      | Environment Variable         |
|---------------------------------|------------------------------|
| et-ccd-definitions-englandwales | ENGLANDWALES_CCD_CONFIG_PATH |
| et-ccd-definitions-scotland     | SCOTLAND_CCD_CONFIG_PATH     |
| et-ccd-definitions-admin        | ADMIN_CCD_CONFIG_PATH        |

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
