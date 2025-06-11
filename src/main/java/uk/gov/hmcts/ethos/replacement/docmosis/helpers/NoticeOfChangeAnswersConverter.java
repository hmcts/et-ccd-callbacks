package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.NoticeOfChangeAnswers;

@Component
public class NoticeOfChangeAnswersConverter {
    /**
     * Creates notice of change answer required for validating challenge questions.
     * @param respondent respondent representative
     * @return notice of change answer
     */
    public NoticeOfChangeAnswers generateForSubmission(RespondentSumTypeItem respondent,
                                                       CaseData caseData) {
        return NoticeOfChangeAnswers.builder()
                .partyName(respondent.getValue().getRespondentName())
                .caseReference(caseData.getEthosCaseReference())
                .build();
    }
}