package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(SpringExtension.class)
class AddAmendClaimantRepresentativeServiceTest {
    private AddAmendClaimantRepresentativeService addAmendClaimantRepresentativeService;
    private CaseData caseData;

    @BeforeEach
    void setUp() {
        addAmendClaimantRepresentativeService = new AddAmendClaimantRepresentativeService();
        caseData = CaseDataBuilder.builder().build();
    }

    @Test
    void setRepresentativeIdWithNoClaimant() {
        addAmendClaimantRepresentativeService.setRepresentativeId(caseData);

        assertNull(caseData.getRepresentativeClaimantType());
    }

    @Test
    void setRepresentativeIdWithClaimant() {
        RepresentedTypeC representedTypeC = new RepresentedTypeC();
        representedTypeC.setNameOfRepresentative("Sally Solicitor");
        caseData.setRepresentativeClaimantType(representedTypeC);

        addAmendClaimantRepresentativeService.setRepresentativeId(caseData);

        assertNotNull(caseData.getRepresentativeClaimantType().getRepresentativeId());
    }
}
