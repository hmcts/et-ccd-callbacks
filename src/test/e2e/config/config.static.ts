// Static config: only static users and session file names, no dynamic user loading
import 'dotenv/config';

const env = process.env.RUNNING_ENV || 'aat';

export const staticUsers = {
  etCaseWorker: {
    email: process.env.ET_CASEWORKER_USER_NAME || '',
    password: process.env.ET_CASEWORKER_PASSWORD || ''
  },
  etManageCaseUser: {
    email: process.env.ET_LEGAL_OPS_USER_NAME || '',
    password: process.env.ET_LEGAL_OPS_PASSWORD || '',
  },
  etLegalOpsUser: {
    email: process.env.ET_LEGAL_OPS2_USER_NAME || '',
    password: process.env.ET_LEGAL_OPS2_PASSWORD || '',
  },

  etLegalRepresentative: {
    email: process.env.ET_LEGALREP_USER_NAME || '',
    password: process.env.ET_LEGALREP_PASSWORD || '',
  },
  etApiUser: {
    email: process.env.ET_CCD_API_USER_NAME || '',
    password: process.env.ET_CCD_API_PASSWORD || '',
  },
  etEnglandJudge: {
    email: process.env.ET_JUDGE_USER_NAME_ENG || '',
    password: process.env.ET_JUDGE_USER_ENG_PASSWORD || '',
 },
  etLegalRepresentative2: {
    email: process.env.ET_REPSONDENT_USER_NAME || '',
    password: process.env.ET_REPSONDENT_PASSWORD || '',
  },
  etManageOrgSuperUser: {
    email: process.env.ET_MANAGE_ORG_USERNAME || '',
    password: process.env.ET_MANAGE_ORG_PASSWORD || '',
  },
  etWorkAllocationJudge: {
    email: process.env.ET_JUDGE_USER_NAME_WORKALLOCATION || '',
    password: process.env.ET_JUDGE_USER_PASSWORD_WORKALLOCATION || '',
  },
};

export const staticConfig = {
  env: process.env.RUNNING_ENV || 'aat',

  etSyaUiUrl:
    process.env.TEST_URL_CITIZEN_UI
    || (
      env.startsWith('pr-')
     ? `https://et-sya-et-cos-${env}.preview.platform.hmcts.net/`
     : `https://et-sya.${env}.platform.hmcts.net/`
    ),

  etSyrUiUrl:
    process.env.TEST_RESP_URL
    || (
      env.startsWith( 'pr-')
      ? `https://et-syr-et-cos-${env}.preview.platform.hmcts.net/`
      : `https://et-syr.${env}.platform.hmcts.net/`
      ),

  manageCaseBaseUrl:
    process.env.TEST_MANAGE_CASE_URL
    || (env.startsWith( 'pr-')
      ? `https://xui-et-cos-${env}.preview.platform.hmcts.net/`
      : `https://manage-case.${env}.platform.hmcts.net/`),

  TestUrlForManageOrg:
    process.env.MANAGE_ORG_URL
    || (env.startsWith( 'pr-')
      ? `https://xui-mo-webapp-et-cos-${env}.preview.platform.hmcts.net/`
      : `https://manage-org.${env}.platform.hmcts.net/`),

  idamUrl:
    process.env.IDAM_URL
    || (env.startsWith( 'pr-')
      ? `https://idam-api.aat.platform.hmcts.net/`
      : `https://idam-api.${env}.platform.hmcts.net/`),

  EtCosPreviewEtSyaApiUrl:
    process.env.ET_COS_PREVIEW_ET_SYA_API
    || (env.startsWith( 'pr-')
      ? `https://et-sya-api-et-cos-${env}.preview.platform.hmcts.net/`
      : `http://et-sya-api-${env}.service.core-compute-${env}.internal/`),

  ccdDataStoreApi:
    process.env.ET_COS_PREVIEW_CCD
    || (env.startsWith( 'pr-')
      ? `https://ccd-data-store-api-et-cos-${env}.preview.platform.hmcts.net/`
      : `http://ccd-data-store-api-${env}.service.core-compute-${env}.internal/`),

  TestCcdGwSecret: process.env.MICROSERVICE_CCD_GW || '',

  loginPaths: {
    cases: 'cases',
    worklist: 'work/my-work/list',
    organisation: 'organisation',
  },
};

