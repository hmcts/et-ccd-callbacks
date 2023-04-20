package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@ExtendWith(SpringExtension.class)
class BundlesRespondentServiceTest {

    private BundlesRespondentService bundlesRespondentService;
    private CaseData caseData;

    @BeforeEach
    void setUp() {
        bundlesRespondentService = new BundlesRespondentService();
        caseData = CaseDataBuilder.builder().build();
    }

    @Test
    void clearInputData() {
        caseData.setBundlesRespondentPrepareDocNotesShow(YES);
        caseData.setBundlesRespondentAgreedDocWith(NO);
        caseData.setBundlesRespondentAgreedDocWithBut("Some input");
        caseData.setBundlesRespondentAgreedDocWithNo("Some input");

        bundlesRespondentService.clearInputData(caseData);

        assertThat(caseData.getBundlesRespondentPrepareDocNotesShow()).isNull();
        assertThat(caseData.getBundlesRespondentAgreedDocWith()).isNull();
        assertThat(caseData.getBundlesRespondentAgreedDocWithBut()).isNull();
        assertThat(caseData.getBundlesRespondentAgreedDocWithNo()).isNull();
    }
}
