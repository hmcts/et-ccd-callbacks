package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantIndType;
import uk.gov.hmcts.et.common.model.ccd.types.NoticeOfChangeAnswers;

@Component
public class NoticeOfChangeAnswersConverter {
    /**
     * Creates notice of change answer required for validating challenge questions.
     * @param respondent respondent representative
     * @return notice of change answer
     */
    public NoticeOfChangeAnswers generateForSubmission(RespondentSumTypeItem respondent,
                                                       ClaimantIndType claimant) {
        String respondentName = respondent != null && respondent.getValue() != null
                ? respondent.getValue().getRespondentName() : null;
        String claimantFirstName = claimant != null ? claimant.getClaimantFirstNames() : null;
        String claimantLastName = claimant != null ? claimant.getClaimantLastName() : null;

        return NoticeOfChangeAnswers.builder()
                .respondentName(respondentName)
                .claimantFirstName(claimantFirstName)
                .claimantLastName(claimantLastName)
                .build();
    }
}