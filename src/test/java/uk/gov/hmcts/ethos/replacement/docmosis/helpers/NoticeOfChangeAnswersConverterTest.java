package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { NoticeOfChangeAnswersConverter.class })
class NoticeOfChangeAnswersConverterTest {
    private static final String RESPONDENT_NAME = "Harry Johnson";
    private static final String CLAIMANT_FIRST_NAME = "Mary";
    private static final String CLAIMANT_LAST_NAME = "Clyde";

    @Autowired
    private NoticeOfChangeAnswersConverter noticeOfChangeAnswersConverter;

//    @Test
//    void shouldConvertToNoticeOfChangeAnswer() {
//        ClaimantIndType claimantIndType = new ClaimantIndType();
//        claimantIndType.setClaimantFirstNames(CLAIMANT_FIRST_NAME);
//        claimantIndType.setClaimantLastName(CLAIMANT_LAST_NAME);
//
//        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
//        respondentSumTypeItem.setValue(RespondentSumType.builder().respondentName(RESPONDENT_NAME)
//            .build());
//
//        NoticeOfChangeAnswers expectedNocAnswer =
//            NoticeOfChangeAnswers.builder().partyName(RESPONDENT_NAME).build();
//
//        assertThat(noticeOfChangeAnswersConverter.generateForSubmission(respondentSumTypeItem, claimantIndType))
//            .isEqualTo(expectedNocAnswer);
//
//    }
}