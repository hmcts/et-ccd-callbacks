# ECM CCD Docker

[ECM CCD Docker](https://github.com/hmcts/ecm-ccd-docker) is a docker compose project. It provides a local CCD/ExUI
environment in which et-ccd-callbacks can be developed and tested. A more recent alternative to ECM CCD Docker is [RSE CFT lib]().

## Setup

See [ECM CCD Docker](https://github.com/hmcts/ecm-ccd-docker) for steps to start the CCD Docker environment.

### Environment Variables
| Variable                | Purpose                                         |
|-------------------------|-------------------------------------------------|
| ET_COS_DB_PASSWORD      | Local et_cos database password                  |
| SPRING_PROFILES_ACTIVE  | Set to ```cftlib``` to use cftlib Spring profile |
| ENGLANDWALES_CCD_CONFIG_PATH | Set to the path of your local et-ccd-definitions-englandwales GitHub repository
| SCOTLAND_CCD_CONFIG_PATH | Set to the path of your local et-ccd-definitions-scotland GitHub repository
| ADMIN_CCD_CONFIG_PATH   | Set to the path of your local et-ccd-definitions-admin GitHub repository

### Azure Service Bus
You must either provide a connection string in an environment variable to a queue in Azure or
configure a fake service bus.

If you require a real Azure queue then ask one of the team to set this up.

| Variable | Purpose                                              |
| -------- |------------------------------------------------------|
| CREATE_UPDATES_QUEUE_SEND_CONNECTION_STRING | Connection string for create-updates queue in Azure  |

Or, if no development queue is available, set the following environment variable to use the fake.

| Variable | Purpose                                             |
| -------- |-----------------------------------------------------|
| SERVICEBUS_FAKE | Set to ```true``` to enable fake service bus client |

### PostgreSQL Database
Create the local et_cos database:
```bash
    ./bin/init-db.sh
```

## Run
```bash
    ./gradlew bootRun --args='--spring.profiles.active=dev'
```

Once the services are all booted (i.e. when the log messages stop) then ExUI is accessible from:

http://localhost:3455



