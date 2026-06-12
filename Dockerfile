ARG APP_INSIGHTS_AGENT_VERSION=3.5.1
FROM hmctsprod.azurecr.io/base/java:21-distroless AS base
LABEL maintainer="https://github.com/hmcts/et-ccd-callbacks"

WORKDIR /opt/app

COPY lib/applicationinsights.json /opt/app/
COPY build/libs/et-cos.jar /opt/app/
COPY ccd-definitions/jurisdictions/admin/json /opt/app/ccd-definitions/jurisdictions/admin/json
COPY ccd-definitions/jurisdictions/england-wales/json /opt/app/ccd-definitions/jurisdictions/england-wales/json
COPY ccd-definitions/jurisdictions/scotland/json /opt/app/ccd-definitions/jurisdictions/scotland/json
COPY build/cftlib/definition-snapshots /opt/app/build/cftlib/definition-snapshots

EXPOSE 8081

CMD ["et-cos.jar"]
