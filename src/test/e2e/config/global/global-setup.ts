import * as fs from 'fs';
import * as path from 'path';
import { IdamApi } from '../../data-utils/api/IdamApi.ts';

/**
 * Playwright global setup script to create dynamic test users and store their credentials.
 * This runs once before all tests.
 */
async function globalSetup() {
  // Create dynamic users using IdamApi
  const idamApi = new IdamApi();
  const etClaimant = await idamApi.createDynamicClaimantUser();
  const etClaimant2 = await idamApi.createDynamicClaimantUser();
  const etRespondent = await idamApi.createDynamicRespondentUser();
  const etRespondent2 = await idamApi.createDynamicRespondentUser();

  // Store credentials in a temp file
  const creds = {
    etClaimant: {
      email: etClaimant.userEmail,
      password: etClaimant.userPassword,
    },
    etClaimant2: {
      email: etClaimant2.userEmail,
      password: etClaimant2.userPassword,
    },
    etRespondent: {
      email: etRespondent.userEmail,
      password: etRespondent.userPassword,
    },
    etRespondent2: {
      email: etRespondent2.userEmail,
      password: etRespondent2.userPassword,
    },
  };
  const tmpDir = path.resolve(__dirname, '../../../.tmp');
  if (!fs.existsSync(tmpDir)) {
    fs.mkdirSync(tmpDir);
  }
  fs.writeFileSync(path.join(tmpDir, 'test-users.json'), JSON.stringify(creds, null, 2));
  console.log(`Et Claimant created: ${etClaimant.userEmail}`);
  console.log(`Et Claimant2 created: ${etClaimant2.userEmail}`);
  console.log(`Et Respondent created: ${etRespondent.userEmail}`);
  console.log(`Et Respondent2 created: ${etRespondent2.userEmail}`);
}

export default globalSetup;
