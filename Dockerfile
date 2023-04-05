ARG APP_INSIGHTS_AGENT_VERSION=3.4.11
FROM hmctspublic.azurecr.io/base/java:11-distroless
LABEL maintainer="https://github.com/hmcts/et-ccd-callbacks"

COPY lib/AI-Agent.xml /opt/app/
COPY build/libs/et-cos.jar /opt/app/

USER root
RUN apt update
RUN apt install --yes libharfbuzz-dev
USER hmcts

FROM base

COPY --from=builder /usr/lib/x86_64-linux-gnu/libharfbuzz.so.0 /usr/lib/x86_64-linux-gnu/libharfbuzz.so.0
COPY --from=builder /usr/lib/x86_64-linux-gnu/libglib-2.0.so.0 /usr/lib/x86_64-linux-gnu/libglib-2.0.so.0
COPY --from=builder /usr/lib/x86_64-linux-gnu/libgraphite2.so.3 /usr/lib/x86_64-linux-gnu/libgraphite2.so.3
COPY --from=builder /lib/x86_64-linux-gnu/libpcre.so.3 /lib/x86_64-linux-gnu/libpcre.so.3

EXPOSE 8081

CMD ["et-cos.jar"]
