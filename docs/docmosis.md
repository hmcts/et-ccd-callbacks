# Docmosis Tornado

et-ccd-callbacks has a dependency on [Docmosis Tornado](https://www.docmosis.com/products/tornado.html) to generate
documents and reports.

There are two options available to use Docmosis in a local development environment:

* Docmosis Cloud Service
* tornado-docker

## Docmosis Cloud Service

Details [here](https://tools.hmcts.net/confluence/display/DATS/Docmosis+template+generation+and+testing).

### Setup

#### Applicaiton properties tornado.url value
In `application-dev.properties` or `application-cftlib.properties` you need to use
```bash
tornado.url=https://console.dws3.docmosis.com/console/login.html#eu/templates
```
instead of 
```bash
tornado.url=http://localhost:9095/api/render
```

#### Tornado Access Key
You will need an access key to submit requests to the cloud service.

Login to the Docmosis Cloud Console as described in the Confluence page above.

Click on Account from the left-hand menu.

Click Show Access Key.

Add the key value as an environment variable `TORNADO_ACCESS_KEY`.

#### Templates
Ensure any templates you want the cloud service to use are uploaded to the Templates directory of the cloud service.

All Employment Tribunal templates can be download from GitHub
[here](https://github.com/hmcts/rdo-docmosis/tree/master/Templates/Base).


## tornado-docker

Docmosis Tornado can be run in a docker container. There is a GitHub repository https://github.com/Docmosis/tornado-docker

Follow the instructions [here](https://tools.hmcts.net/confluence/display/DATS/Develop+docmosis+templates+locally)
for setup.
