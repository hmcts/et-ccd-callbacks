package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.NoticeOfChangeAnswers;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { NoticeOfChangeAnswersConverter.class })
class NoticeOfChangeAnswersConverterTest {
    private static final String RESPONDENT_NAME = "Harry Johnson";
    private static final String ETHOS_CASE_REFERENCE = "123456789";

    @Autowired
    private NoticeOfChangeAnswersConverter noticeOfChangeAnswersConverter;

    @Test
    void shouldConvertToNoticeOfChangeAnswer() {
        CaseData caseData = new CaseData();
        caseData.setEthosCaseReference(ETHOS_CASE_REFERENCE);

        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(
                RespondentSumType.builder().respondentName(RESPONDENT_NAME)
            .build());

        NoticeOfChangeAnswers expectedNocAnswer =
            NoticeOfChangeAnswers.builder()
                    .partyName(RESPONDENT_NAME)
                    .caseReference(ETHOS_CASE_REFERENCE)
                    .build();

        assertThat(noticeOfChangeAnswersConverter.generateForSubmission(respondentSumTypeItem, caseData))
            .isEqualTo(expectedNocAnswer);

    }
}