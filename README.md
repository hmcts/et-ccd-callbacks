# Employment Tribunals CCD Callbacks Service

[![Java](https://img.shields.io/badge/Java-21-blue.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Gradle](https://img.shields.io/badge/Gradle-Wrapper-blue.svg)](https://gradle.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE.md)

This application is responsible for handling all CCD callback requests for Employment Tribunal cases.

## Supported Versions

| Component | Version | Status |
|-----------|---------|--------|
| Java | 21 | ✅ Supported |
| Spring Boot | 3.3.0 | ✅ Current |
| Spring Security | 6.x | ✅ Current |
| Node.js | 18+ | ✅ Supported |
| Gradle | Wrapper | ✅ Current |

> ⚠️ **Breaking Change**: Java 21 is now required. Java 17 and earlier versions are no longer supported.

## Prerequisites

### Java
- [JDK 21](https://www.oracle.com/java) (upgraded from JDK 17)

**Note: This application has been upgraded to Spring Boot 3.3.0 and requires Java 21.**

### CCD Common Components
The application should be run locally in an environment that includes CCD common components.

There are two options for achieving this:
- [RSE CFT lib](docs/cftlib.md)
- [ECM CCD Docker](docs/ecm-ccd-docker.md)

### Postgres Database
A local database is required. This is provided by one of the CCD common components environments.

[flyway](https://flywaydb.org/) migrations are automatically applied on startup.

### Docmosis Tornado
[Docmosis Tornado](https://www.docmosis.com/products/tornado.html) is a third-party product used by et-ccd-callbacks to
generate documents and reports.

More information about using Docmosis Tornado in a local development environment can be found [here](docs/docmosis.md).

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
To run the application locally, you should follow the instructions above for one of the CCD environments.

## Tasks

There are a number of tasks within the project; some make use of Spring Scheduling and run within the main application, and others are standalone which can be run from the command line.

*Note: the examples below assume you are using the RSE CFT lib environment, but the tasks can be run from any environment.*

To run the tasks from the command line, you can use the following command as an example:
```bash
SERVER_PORT=4551 TASK_NAME=BatchReconfigurationTask SPRING_PROFILES_ACTIVE=cftlib CRON_RECONFIGURATION_CASE_IDS=1756969501994958 ./gradlew bootRun
```

This will run the `BatchReconfigurationTask` on port 4551 with the cftlib profile active. 

Alternatively, some tasks can be executed if a shell script has been created for them. These can be found in the `./bin/tasks` directory.

An example of this is the `BatchReconfigurationTask` which can be run as follows:

```bash
./bin/tasks/BatchReconfigurationTask.sh 1756969501994958
```

When running a task locally, it is important to set the `SERVER_PORT` environment variable to a port that is not already in use.

## API documentation
API documentation is provided with SpringDoc OpenAPI

http://localhost:8081/swagger-ui.html

**Note:** After the Spring Boot 3 upgrade, the OpenAPI documentation is now provided by SpringDoc OpenAPI 2.5.0. The URL remains the same, but the underlying implementation has been updated.

## Developing

### Database
All database updates are applied using [flyway](https://flywaydb.org/). See src/main/resources/db

### Unit Tests
To run all unit tests:

```bash
./gradlew test
```
### Functional API Tests
To run all Functional API tests against AAT instances:
Ensure F5 VPN is on.
These three variables need to be set in your WSL:
```bash
IDAM_API_URL=https://idam-api.aat.platform.hmcts.net
ET_COS_URL=http://et-cos.service.core-compute-aat.internal
FT_SYA_URL=http://et-sya-api-aat.service.core-compute-aat.internal
```
Then run
```bash
./gradlew functional
```

To run all Functional API tests against local instances (useful for debugging purposes):
Note that some tests may fail as it uses the et.dev@hmcts.net user by default when being run locally, 
the workaround is to create a new user for test (need to replace username and password in getLocalAccessToken method).
Ensure your local environment is up and running (see instructions in ecm-ccd-docker), Callback and SYA API instances are started in separate terminals.
Then run
```bash
./gradlew functional
```

### UI Functional Tests
To run all UI functional test :
Ensure your node version is `18` or greater
proceed to `cofig.js` file change the test url to the desired environment
Add corresponding user details for those environment

To run all test (using script in package.json)
```bash
yarn test:fullfunctional
```
To run a specific test with specific tag
```bash
yarn test:local --grep @<yourtag>
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
