# Employment Tribunals CCD Callbacks Service

This application is responsible for handling all CCD callback requests for Employment Tribunal cases.

## Prerequisites

### Java
- [JDK 17](https://www.oracle.com/java)

### CCD Common Components
The application should be run locally in an environment that includes CCD common components.

There are two options for achieving this:
- [RSE CFT lib](docs/cftlib.md)
- [ECM CCD Docker](docs/ecm-ccd-docker.md)

### Postgres Database
A local database is required. This is provided by one of the CCD common components environments.

[flyway](https://flywaydb.org/) migrations are automatically applied on startup.

### Azure Service Bus
The application requires a connection to an Azure Service Bus queue.

There are two options for achieving this:
- Provide a connection string for a development queue in Azure
- Configure a fake connection client

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
To run the application locally you should follow the instructions above for one of the CCD environments.

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
