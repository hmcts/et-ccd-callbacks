# CUI RA with CFTLib

CUI RA can be started as an optional CFTLib extension for local reasonable-adjustment development. It starts:

* `rd-commondata-api`
* `rd-commondata-api-seed`
* `rd-idam-simulator-proxy`
* `cui-ra`
* `cui-ra-redis`

The services are defined in `src/cftlib/resources/compose/cui-ra-docker-compose.yml` and attach to the existing `cftlib_default` Docker network.

## Starting with CUI RA

Authenticate with the HMCTS ACR registries before pulling images:

```bash
az acr login --name hmctsprod
```

Start CFTLib with CUI RA and RD Common Data API:

```bash
./gradlew bootWithCcdAndCuiRa
```

Keep this Gradle process running while testing. It hosts the local callbacks app and the CFTLib S2S simulator on `http://localhost:8489`; if it is stopped, CUI RA and RD Common Data API will stay in Docker but their health checks will report S2S as `DOWN`.

Or:

```bash
ET_CUI_RA=true ./gradlew bootWithCCD
```

To run Work Allocation and CUI RA together:

```bash
./gradlew bootWithCcdAndWaAndCuiRa
```

## Local URLs

| Service | URL |
|---------|-----|
| CUI RA | `http://localhost:3100` |
| CUI RA health | `http://localhost:3100/health` |
| RD Common Data API | `http://localhost:4550` |
| RD Common Data API health | `http://localhost:4550/health` |

## Configuration

| Variable | Default | Purpose |
|----------|---------|---------|
| `ET_CUI_RA` | `false` | Set to `true` to load `cui-ra-docker-compose.yml` during `bootWithCCD`. |
| `CUI_RA_IMAGE` | `hmctsprod.azurecr.io/cui/ra:latest` | Override the CUI RA image under test. |
| `RD_COMMONDATA_API_IMAGE` | `hmctsprod.azurecr.io/rd/commondata-api:latest` | Override the RD Common Data API image under test. |
| `CUI_RA_PORT` | `3100` | Host port for CUI RA. |
| `RD_COMMONDATA_API_PORT` | `4550` | Host port for RD Common Data API. |
| `CUI_RA_S2S_SECRET` | `AAAAAAAAAAAAAAAA` | Secret used by CUI RA when requesting a token from the local S2S simulator. |
| `CUI_RA_SESSION_SECURE` | `false` | Controls CUI RA's secure session cookie flag. Keep this `false` for local HTTP testing. |
| `CUI_RA_ALLOWED_SERVICES` | `et_cos,et_sya,et_sya_api,et_syr,xui_webapp,prl_citizen_frontend` | Comma-separated S2S services allowed to call CUI RA. |
| `COMMONDATA_API_S2S_SECRET` | `AAAAAAAAAAAAAAAA` | Secret used by RD Common Data API when requesting a token from the local S2S simulator. |
| `CRD_S2S_AUTHORISED_SERVICES` | `rd_commondata_api,cui_ra,et_cos,xui_webapp` | S2S services allowed to call RD Common Data API. |
| `RD_COMMONDATA_LD_SDK_KEY` | empty | LaunchDarkly SDK key used by RD Common Data API. Case flag endpoints may return `403` if this is not set. |
| `RD_COMMONDATA_LAUNCH_DARKLY_ENV` | `aat` | LaunchDarkly environment attribute used by RD Common Data API. Keep this aligned with the SDK key environment. |
| `RD_COMMONDATA_OPEN_ID_API_BASE_URI` | `http://rd-idam-simulator-proxy:5062/o` | OIDC discovery URL for the local IDAM simulator from inside Docker. |

`cui-ra-redis` is not published on a host port. CUI RA reaches it through the Docker network, which avoids clashes with other local Redis containers.

## Data

The CFTLib database list includes `dbcommondata` when this mode is enabled. `rd-commondata-api` runs its own Flyway migrations against that database using the shared CFTLib Postgres instance.

After Flyway has created `flag_service`, the `rd-commondata-api-seed` container inserts local `BHA1` case-flag mappings from `src/cftlib/resources/compose/rd-commondata/seed-bha1-case-flags.sql`. This gives local ET flows data for:

* party reasonable-adjustment flags
* party language interpreter flags
* case flags currently covered by the existing WireMock stub
* sign-language and sample interpreter-language list-of-values rows needed by list-backed flags

To re-run only the seed against an already-running stack:

```bash
docker compose -f src/cftlib/resources/compose/cui-ra-docker-compose.yml run --rm --no-deps rd-commondata-api-seed
```

## Auth Flow

The intended local flow is:

1. ET/XUI calls CUI RA with the CFTLib simulator IDAM token.
2. CUI RA forwards that IDAM token to local RD Common Data API.
3. CUI RA obtains its own S2S token from the local S2S simulator as `cui_ra`.
4. RD Common Data API validates the IDAM token and allows the `cui_ra` S2S service.

For this to work, CUI RA must use the inbound IDAM token when calling reference data. Local temporary branches that hardcode an AAT IDAM JWT should remove that workaround when using this local RD Common Data API.

## Known Limits

`rd-commondata-api` still uses LaunchDarkly for the case flag and LOV endpoints. The Docker wiring can start the service without `RD_COMMONDATA_LD_SDK_KEY`, but useful case flag calls are expected to fail with `403` until the flag is enabled for the local service or the ref-data app provides a local flag override. If using the AAT key from the `rd-aat` key vault, keep `RD_COMMONDATA_LAUNCH_DARKLY_ENV=aat`.

`rd-idam-simulator-proxy` forwards OIDC discovery from Docker to the CFTLib IDAM simulator. It rewrites the simulator's `localhost` issuer metadata to a Docker-reachable service URL so Spring Security's issuer discovery can start inside the `rd-commondata-api` container.

If `/dc/p/<id>` redirects to `/journey/flags/display/PF0001-RA0001` and then appears to lose state or show a CUI 404, check that the first `/dc/p/<id>` response sets a `cui-session` cookie. Local HTTP runs need `CUI_RA_SESSION_SECURE=false`; the compose file passes this through `NODE_CONFIG` as a JSON boolean so Express will set the cookie over HTTP.

If startup fails with `Connection refused` for `http://localhost:5062/o/.well-known/openid-configuration`, check the simulator state:

```bash
docker ps -a --filter name=cftlib-rse-idam-simulator
```

If it exists but is only `Created`, start it and rerun the boot task:

```bash
docker start cftlib-rse-idam-simulator-1
./gradlew bootWithCcdAndCuiRa
```

Use `./bin/cftlib-clean.sh` only when you want a full reset. It stops and removes all Docker containers on the machine, prunes Docker volumes, and removes the `cftlib_default` network.
