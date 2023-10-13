package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.CaseFlagsType;

@Slf4j
@Service
public class CaseFlagsService {

    /**
     * Setup case flags for Claimant, Respondent and Case level.
     * @param caseData Data about the current case
     */
    public void setupCaseFlags(CaseData caseData) {
        caseData.setCaseFlags(CaseFlagsType.builder().build());

        caseData.setClaimantFlags(CaseFlagsType.builder()
                .partyName(caseData.getClaimant())
                .roleOnCase("claimant")
                .build()
        );

        caseData.setRespondentFlags(CaseFlagsType.builder()
                .partyName(caseData.getRespondent())
                .roleOnCase("respondent")
                .build()
        );
    }
}
