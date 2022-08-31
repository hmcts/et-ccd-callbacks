# Employment Tribunals CCD Callbacks Service

This application is responsible for handling all CCD callback requests for Employment Tribunal cases.

## Prerequisites

### Java
- [JDK 11](https://www.oracle.com/java)

### CCD Common Components
The application should be run locally in an environment that includes CCD common components.

There are two options for achieving this:
- [RSE CFT lib](docs/cftlib)
- [ECM CCD Docker](docs/ecm-ccd-docker)

### Postgres Database
A local database is required. This is provided by one of the CCD common components environments.

[flyway](https://flywaydb.org/) migrations are automatically applied on startup.

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
