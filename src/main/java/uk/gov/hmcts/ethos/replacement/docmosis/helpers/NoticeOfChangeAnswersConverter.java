package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantIndType;
import uk.gov.hmcts.et.common.model.ccd.types.NoticeOfChangeAnswers;

@Component
public class NoticeOfChangeAnswersConverter {
    /**
     * Creates notice of change answer required for validating challenge questions.
     * @param respondentRepresentative respondent representative
     * @return notice of change answer
     */
    public NoticeOfChangeAnswers generateForSubmission(RepresentedTypeRItem respondentRepresentative,
        ClaimantIndType claimant) {
        String respRepName = respondentRepresentative.getValue().getRespRepName();
        return NoticeOfChangeAnswers.builder()
                .respondentName(respRepName)
                .claimantFirstName(claimant.getClaimantFirstNames())
                .claimantLastName(claimant.getClaimantLastName())
                .build();
    }
}