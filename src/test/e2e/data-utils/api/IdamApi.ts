import { getUserAuthToken } from './TokenHelperApi.ts';
import { axiosRequest } from './ApiHelper.ts';
import { CaseDetailsValues } from '../../config/case-data.ts';
import { v4 as uuidv4 } from "uuid";
import { staticUsers } from '../../config/config.static.ts';

const env = process.env.RUNNING_ENV && process.env.RUNNING_ENV.startsWith('pr-') ? 'aat' : (process.env.RUNNING_ENV || 'aat');
const idamTestingSupportUrl = `https://idam-testing-support-api.${env}.platform.hmcts.net`;
export class IdamApi {

  private async createCitizenPostRequest(
    authToken: string,
    userEmail: string,
    userPassword: string,
    forename: string = 'ET',
    surname: string = 'Respondent',
  ) {

    let idamUserCreationUrl = idamTestingSupportUrl + '/test/idam/users';

    let data = {
      password: `${userPassword}`,
      user: {
        email: `${userEmail}`,
        forename: `${forename}`,
        surname: `${surname}`,
        displayName: `${forename} ${surname}`,
        roleNames: ['citizen'],
        accountStatus: 'ACTIVE',
        recordType: 'LIVE',
      },
    };

    let apiConfig = {
      method: 'post',
      maxBodyLength: Infinity,
      url: idamUserCreationUrl,
      headers: {
        Authorization: `Bearer ${authToken}`,
        'Content-Type': 'application/json',
      },
      data: data,
    };

    return await axiosRequest(apiConfig);
  }

  async createDynamicRespondentUser() {
    const uniqueId = uuidv4();
    const userEmail = `ettestresp${uniqueId}@gmail.com`;
    const userPassword = 'Nagoya0102';
    const authToken = await getUserAuthToken(staticUsers.etApiUser.email, staticUsers.etApiUser.password);
    await this.createCitizenPostRequest(authToken, userEmail, userPassword);
    return { userEmail, userPassword };
  }

  async createDynamicClaimantUser() {
    const uniqueId = uuidv4();
    const userEmail = `ettestclaimant${uniqueId}@gmail.com`;
    const userPassword = 'Nagoya0102';
    const authToken = await getUserAuthToken(staticUsers.etApiUser.email, staticUsers.etApiUser.password);
    await this.createCitizenPostRequest(authToken, userEmail, userPassword, CaseDetailsValues.claimantFirstName, CaseDetailsValues.claimantLastName);
    return { userEmail, userPassword };
  }
}
