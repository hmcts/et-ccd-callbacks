import { ReplacementAction } from '../../types/replacement-action';
import { ccdApi } from '../../fixtures/common.fixture.ts';
import { CaseTypeLocation } from '../../config/case-data.ts';
import { users } from '../../config/config.dynamic.ts';

export class CaseDataBuilder {
  private email: string;
  private password: string;
  private payloadPath!: string;
  private caseType: CaseTypeLocation;
  private eventType: string;
  private replacements: ReplacementAction[] = [];

  constructor(caseType: CaseTypeLocation, eventType: string) {
    this.caseType = caseType;
    this.eventType = eventType;
    this.email = users.etCaseWorker.email;
    this.password = users.etCaseWorker.password;
  }

  withCaseWorkerUser(): CaseDataBuilder {
    this.email = users.etCaseWorker.email;
    this.password = users.etCaseWorker.password;
    return this;
  }

  withLegalRepUser(): CaseDataBuilder {
    this.email = users.etLegalRepresentative.email;
    this.password = users.etLegalRepresentative.password;
    return this;
  }

  withPayload(path: string): CaseDataBuilder {
    this.payloadPath = path;
    return this;
  }

  addReplacements(...replacements: ReplacementAction[]): CaseDataBuilder {
    this.replacements.push(...replacements);
    return this;
  }

  async create(): Promise<any> {
    return await ccdApi.createCaseInCcd(
      this.email,
      this.password,
      this.payloadPath,
      this.caseType,
      this.eventType,
      this.replacements,
    );
  }
}
