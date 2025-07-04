FROM hmctspublic.azurecr.io/base/node:20-alpine as base
USER root
RUN corepack enable
WORKDIR /opt/app
COPY --chown=hmcts:hmcts . .
USER hmcts
ADD ./configs "/config"
RUN yarn install && yarn cache clean
COPY index.js ./
ENV NODE_CONFIG_DIR="/config"
CMD ["yarn", "start"]
EXPOSE 3000
