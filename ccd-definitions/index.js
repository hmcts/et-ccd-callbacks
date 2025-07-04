const healthcheck = require('@hmcts/nodejs-healthcheck');
const config = require('config');
const express = require('express');

const app = express();
const payload = { message: 'This is et-ccd-definitions-admin' };
const port = config.get('server.port');

healthcheck.addTo(app, {
  checks: {
    sampleCheck: healthcheck.raw(() => healthcheck.up()),
  },
});

app
  .get('/', (req, res) => {
    return res.send(payload);
  })
  .listen(port, () => {
    console.log(`App healthcheck http://0.0.0.0:${port}/health`);
  });
