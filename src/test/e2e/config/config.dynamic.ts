// Dynamic config: loads dynamic users if present, otherwise falls back to env/static
import { staticUsers, staticConfig } from './config.static';
import { getDynamicUser } from '../data-utils/CachingHelper.ts';

// Helper to read dynamic test users from JSON if env vars are not set
const dynamicClaimant = getDynamicUser('etClaimant');
const dynamicClaimant2 = getDynamicUser('etClaimant2');
const dynamicRespondent = getDynamicUser('etRespondent');
const dynamicRespondent2 = getDynamicUser('etRespondent2');

const sessionDir = '.sessions/';

export type UserCredentials = {
  email: string;
  password: string;
  sessionFile: string;
};

export const users = {
  etClaimant: {
    email: dynamicClaimant.email || process.env.ET_CITIZEN_USER_NAME || '',
    password: dynamicClaimant.password || process.env.ET_CITIZEN_PASSWORD || '',
    sessionFile: sessionDir + 'etClaimant.json',
  },
  etClaimant2: {
    email: dynamicClaimant2.email  || '',
    password: dynamicClaimant2.password || '',
    sessionFile: sessionDir + 'etClaimant2.json',
  },
  etRespondent: {
    email: dynamicRespondent.email || process.env.ET3_REPSONDENT_USER_NAME || '',
    password: dynamicRespondent.password || process.env.ET3_REPSONDENT_PASSWORD || '',
    sessionFile: sessionDir + 'etRespondent.json',
  },
  etRespondent2: {
    email: dynamicRespondent2.email || process.env.ET_RESP2_USER_NAME || '',
    password: dynamicRespondent2.password || process.env.ET_RESP2_PASSWORD || '',
    sessionFile: sessionDir + 'etRespondent2.json',
  },
  etCaseWorker: {
    sessionFile: sessionDir + 'etCaseWorker.json',
    ...staticUsers.etCaseWorker,
  },
  etManageCaseUser: {
    sessionFile: sessionDir + 'etManageCaseUser.json',
    ...staticUsers.etManageCaseUser,
  },
  etLegalOpsUser: {
    sessionFile: sessionDir + 'etLegalOpsUser.json',
    ...staticUsers.etLegalOpsUser,
  },
  etLegalRepresentative: {
    sessionFile: sessionDir + 'etLegalRepresentative.json',
    ...staticUsers.etLegalRepresentative,
  },
  etApiUser: {
    sessionFile: sessionDir + 'etApiUser.json',
    ...staticUsers.etApiUser,
  },
  etEnglandJudge: {
    sessionFile: sessionDir + 'etEnglandJudge.json',
    ...staticUsers.etEnglandJudge,
  },
  etLegalRepresentative2: {
    sessionFile: sessionDir + 'etLegalRepresentative2.json',
    ...staticUsers.etLegalRepresentative2,
  },
  etManageOrgSuperUser: {
    sessionFile: sessionDir + 'etManageOrgSuperUser.json',
    ...staticUsers.etManageOrgSuperUser,
  },
  etWorkAllocationJudge: {
    sessionFile: sessionDir + 'etWorkAllocationJudge.json',
    ...staticUsers.etWorkAllocationJudge,
  },
};

export const config = staticConfig;
