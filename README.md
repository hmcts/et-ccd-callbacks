# Employment Tribunals CCD Callbacks Service

This application is responsible for handling all CCD callback requests for employment tribunal cases that have a case type of either ET_EnglandWales or ET_Scotland.

## CFTLIB Quickstart
### Set environment variables

| Variable | Purpose |
| -------- | ------- |
| ET_COS_DB_PASSWORD | Database password |
| XUI_LD_ID | Launch Darkly Client Id |
| SPRING_PROFILES_ACTIVE | cftlib |

### Setup
```bash
yarn setup
```

Make sure you have the latest RSE IdAM Simulator image
```bash
docker pull hmctspublic.azurecr.io/hmcts/rse/rse-idam-simulator:latest
```

### Run
```bash
    ./gradlew bootWithCCD
    ./gradlew installlocaldb
```

Note installlocaldb only needs to be executed once in order to install the et_cos database

Once the services are all booted (i.e. when the log messages stop) then ExUI is accessible from:

http://localhost:3000

Username: a@b.com

No password required

## Getting started

### Prerequisites

- [JDK 11](https://www.oracle.com/java)

### Building

The project uses [Gradle](https://gradle.org) as a build tool but you don't have to install it locally since there is a
`./gradlew` wrapper script.

To build the project please execute the following command:

```bash
    ./gradlew build
```

To get the project to build in IntelliJ IDEA, you have to:

 - Install the Lombok plugin: Preferences -> Plugins
 - Enable Annotation Processing: Preferences -> Build, Execution, Deployment -> Compiler -> Annotation Processors

### Running

Running the application is best achieved by setting up an environment containing all dependencies. A local development
environment can be created using the ecm-ccd-docker project.
See [here](https://github.com/hmcts/ecm-ccd-docker)

#### Environment Variables
Required:
- ET_COS_DB_PASSWORD
- CREATE_UPDATES_QUEUE_SEND_CONNECTION_STRING
- DB_URL

Optional:
- TORNADO_ACCESS_KEY - only needed if you want to generate reports using Docmosis


#### Setup
There is a dependency on a postgres database to be running locally.

To install the database schema required for et-ccd-callbacks execute the following command:
```bash
    ./gradlew installlocaldb
```

There is also a dependency on Azure Service Bus.

To run the project locally you should use the dev profile.
You can run the application by executing following command:

```bash
    ./gradlew bootRun --args='--spring.profiles.active=dev'
```

The application will start locally on `http://localhost:8081`

### API documentation

API documentation is provided with Swagger:
UI to interact with the API resources

```bash
    http://localhost:8081/swagger-ui.html
```

## Developing

### Database
All database updates are applied using [flyway](https://flywaydb.org/). See src/main/resources/db

### Unit tests

To run all unit tests please execute following command:

```bash
    ./gradlew test
```

### Coding style tests

To run all checks (including unit tests) please execute following command:

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
