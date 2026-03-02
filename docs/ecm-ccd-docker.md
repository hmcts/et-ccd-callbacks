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


