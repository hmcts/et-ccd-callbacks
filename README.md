# Employment Tribunals CCD Callbacks Service

This application is responsible for handling all CCD callback requests for employment tribunal cases.

## Prerequisites

### Java
- [JDK 11](https://www.oracle.com/java)

### CCD Common Components
The application should be run in an environment that includes CCD common components.

There are two options for achieving this:
- [CFTLib plugin](https://github.com/hmcts/rse-cft-lib)
- [ECM CCD Docker](https://github.com/hmcts/ecm-ccd-docker)

### Postgres Database
A local database is required. This is provided by one of the CCD Common Components environments.

[flyway](https://flywaydb.org/) migrations are automatically run on startup.

### Azure Service Bus
The application requires a connection to an Azure Service Bus queue.

There are two options for achieving this:
- Provide a connection string for a development queue in Azure
- Configure a fake connection client

### Docmosis Tornado
[Docmosis Tornado](https://www.docmosis.com/products/tornado.html) is a third-party document generation engine used to
generate reports. This is provided by one of the CCD Common Components environments.

A license is required to use this product.

## Building
The project uses [Gradle](https://gradle.org) as a build tool, but you don't have to install it locally since there is a
`./gradlew` wrapper script.

To build the project execute the following command:

```bash
    ./gradlew build
```

To get the project to build in IntelliJ IDEA, you have to:

- Install the Lombok plugin: Preferences -> Plugins
- Enable Annotation Processing: Preferences -> Build, Execution, Deployment -> Compiler -> Annotation Processors

## Running

To run the application, first set the mandatory environment variables.

Then either follow the instructions for [Running using CFTLib](#running-using-cftlib) or
[Running using ECM CCD Docker](#running-using-ecm-ccd-docker).

### Mandatory Environment Variables
| Variable | Purpose                        |
| -------- |--------------------------------|
| ET_COS_DB_PASSWORD | Local et_cos database password |

### Azure Service Bus
Either provide a connection string in an environment variable to a development queue.

If you require a development queue then ask one of the team to set this up.

| Variable | Purpose                                    |
| -------- |--------------------------------------------|
| CREATE_UPDATES_QUEUE_SEND_CONNECTION_STRING | Connection string for create-updates queue |

Or, if no development queue is available, set the following environment variable

| Variable | Purpose                                             |
| -------- |-----------------------------------------------------|
| SERVICEBUS_FAKE | Set to ```true``` to enable fake service bus client |

### Running using CFTLib
#### Set environment variables

| Variable | Purpose                                          |
| -------- |--------------------------------------------------|
| XUI_LD_ID | Launch Darkly Client Id                          |
| SPRING_PROFILES_ACTIVE | Set to ```cftlib``` to use cftlib Spring profile |

#### Setup
```bash
yarn setup
```
Make sure you have the latest RSE IdAM Simulator image
```bash
docker pull hmctspublic.azurecr.io/hmcts/rse/rse-idam-simulator:latest
```

#### Run
```bash
    ./gradlew bootWithCCD
```

Once the services are all booted (i.e. when the log messages stop) then ExUI is accessible from:

http://localhost:3000

Username: a@b.com

No password required

**_Note the CFTLib plugin supports Spring Boot DevTools automatic restart._**

### Running using ECM CCD Docker
#### Setup
See [ECM CCD Docker](https://github.com/hmcts/ecm-ccd-docker) for steps to start the CCD Docker environment.

Create the local et_cos database:
```bash
    ./bin/init-db.sh
```

#### Run
```bash
    ./gradlew bootRun --args='--spring.profiles.active=dev'
```

Once the services are all booted (i.e. when the log messages stop) then ExUI is accessible from:

http://localhost:3455

## API documentation
API documentation is provided with Swagger

http://localhost:8081/swagger-ui.html

## Developing

### Database
All database updates are applied using [flyway](https://flywaydb.org/). See src/main/resources/db

### Unit Tests
To run all unit tests:

```bash
    ./gradlew test
```

### Coding Style Tests
To run all checks (including unit tests):

```bash
    ./gradlew check
```

### OWASP Dependency Vulnerability Checks
To run the OWASP checks for vulnerabilities in dependencies:

```bash
    ./gradlew dependencyCheckAggregate
```

## License
This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details.
