ARG APP_INSIGHTS_AGENT_VERSION=3.5.1
FROM hmctsprod.azurecr.io/base/java:21-distroless AS base
LABEL maintainer="https://github.com/hmcts/et-ccd-callbacks"

COPY lib/applicationinsights.json /opt/app/
COPY build/libs/et-cos.jar /opt/app/

EXPOSE 8081

CMD ["et-cos.jar"]
