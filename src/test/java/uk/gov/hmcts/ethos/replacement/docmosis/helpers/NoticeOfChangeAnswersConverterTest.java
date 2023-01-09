package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.types.NoticeOfChangeAnswers;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantIndType;
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { NoticeOfChangeAnswersConverter.class })
class NoticeOfChangeAnswersConverterTest {
    private static final String RESPONDENT_NAME = "Harry Johnson";
    private static final String RESPONDENT_REP_ID = "1111-2222-3333-1111";
    private static final String RESPONDENT_REP_NAME = "Legal One";

    @Autowired
    private NoticeOfChangeAnswersConverter noticeOfChangeAnswersConverter;

    @Test
    void shouldConvertToNoticeOfChangeAnswer() {
        Organisation org1 = Organisation.builder().organisationID("ORG1").organisationName("ET Org 1").build();

        RepresentedTypeR representedType =
            RepresentedTypeR.builder()
                .nameOfRepresentative(RESPONDENT_REP_NAME)
                .respRepName(RESPONDENT_NAME)
                .respondentOrganisation(org1).build();
        RepresentedTypeRItem representedTypeRItem = new RepresentedTypeRItem();
        representedTypeRItem.setId(RESPONDENT_REP_ID);
        representedTypeRItem.setValue(representedType);

        NoticeOfChangeAnswers expectedNocAnswer =
            NoticeOfChangeAnswers.builder().respondentName(RESPONDENT_NAME).build();

        assertThat(noticeOfChangeAnswersConverter.generateForSubmission(representedTypeRItem, new ClaimantIndType()))
            .isEqualTo(expectedNocAnswer);

    }
}