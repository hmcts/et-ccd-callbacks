package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

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
    void whenSomething_thenSomething() {
        caseData.setHearingCollection();
        bundlesRespondentService.populateSelectHearings(caseData);
    }
}
