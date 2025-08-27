package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        addAmendClaimantRepresentativeService.addAmendClaimantRepresentative(caseData);

        assertNull(caseData.getRepresentativeClaimantType());
    }

    @Test
    void setRepresentativeIdWithClaimant() {
        RepresentedTypeC representedTypeC = RepresentedTypeC.builder().nameOfRepresentative("Sally").build();
        caseData.setRepresentativeClaimantType(representedTypeC);

        addAmendClaimantRepresentativeService.addAmendClaimantRepresentative(caseData);

        assertNotNull(caseData.getRepresentativeClaimantType().getRepresentativeId());
        assertNotNull(caseData.getRepresentativeClaimantType().getOrganisationId());
    }

    @Test
    void setRepresentativeIdWithClaimant_alreadySet() {
        String alreadySetId = UUID.randomUUID().toString();
        RepresentedTypeC representedTypeC = RepresentedTypeC.builder()
                .nameOfRepresentative("Sally")
                .organisationId(alreadySetId)
                .representativeId(alreadySetId)
                .build();

        caseData.setRepresentativeClaimantType(representedTypeC);
        addAmendClaimantRepresentativeService.addAmendClaimantRepresentative(caseData);

        assertEquals(alreadySetId, caseData.getRepresentativeClaimantType().getRepresentativeId());
        assertEquals(alreadySetId, caseData.getRepresentativeClaimantType().getOrganisationId());
    }
}
