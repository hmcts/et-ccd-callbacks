package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.CaseFlagsType;

import java.util.UUID;

@Slf4j
@Service
public class CaseFlagsService {

    /**
     * Setup case flags for Claimant, Respondent and Case level.
     * @param caseData Data about the current case
     */
    public void setupCaseFlags(CaseData caseData) {
        caseData.setCaseFlags(CaseFlagsType.builder().build());
        String claimantGroupId = UUID.randomUUID().toString();
        String respondentGroupId = UUID.randomUUID().toString();

        caseData.setClaimantFlags(CaseFlagsType.builder()
                .partyName(caseData.getClaimant())
                .roleOnCase("claimant")
                //.groupId(claimantGroupId)
                //.visibility("Internal")
                .build()
        );

        caseData.setRespondentFlags(CaseFlagsType.builder()
                .partyName(caseData.getRespondent())
                .roleOnCase("respondent")
                //.groupId(respondentGroupId)
                //.visibility("Internal")
                .build()
        );

        caseData.setExternalClaimantFlags(CaseFlagsType.builder()
                .partyName(caseData.getClaimant())
                .roleOnCase("claimant")
                //.groupId(claimantGroupId)
                //.visibility("External")
                .build()
        );

        caseData.setExternalRespondentFlags(CaseFlagsType.builder()
                .partyName(caseData.getRespondent())
                .roleOnCase("respondent")
                //.groupId(respondentGroupId)
                //.visibility("External")
                .build()
        );
    }
}
