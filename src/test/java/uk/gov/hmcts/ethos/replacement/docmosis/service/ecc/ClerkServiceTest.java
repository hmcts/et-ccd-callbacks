package uk.gov.hmcts.ethos.replacement.docmosis.service.ecc;

import org.junit.Test;
import uk.gov.hmcts.ecm.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.ecm.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.CourtWorkerType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.tribunaloffice.TribunalOffice;
import uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.SelectionServiceTestUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.CourtWorkerService;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ClerkServiceTest {

    private final TribunalOffice tribunalOffice = TribunalOffice.ABERDEEN;

    @Test
    public void testInitialiseClerkResponsibleNoClerkSelected() {
        var courtWorkerService = mockCourtWorkerService();
        var caseData = SelectionServiceTestUtils.createCaseData(tribunalOffice.name());

        var clerkService = new ClerkService(courtWorkerService);
        clerkService.initialiseClerkResponsible(caseData);

        SelectionServiceTestUtils.verifyDynamicFixedListNoneSelected(caseData.getClerkResponsible(), "clerk", "Clerk ");
    }

    @Test
    public void testInitialiseClerkResponsibleWithClerkSelected() {
        var courtWorkerService = mockCourtWorkerService();
        var caseData = SelectionServiceTestUtils.createCaseData(tribunalOffice.name());
        var selectedClerk = DynamicValueType.create("clerk2", "Clerk 2");
        caseData.setClerkResponsible(DynamicFixedListType.of(selectedClerk));

        var clerkService = new ClerkService(courtWorkerService);
        clerkService.initialiseClerkResponsible(caseData);

        SelectionServiceTestUtils.verifyDynamicFixedListSelected(caseData.getClerkResponsible(), "clerk", "Clerk ", selectedClerk);
    }

    private CourtWorkerService mockCourtWorkerService() {
        var courtWorkerService = mock(CourtWorkerService.class);
        var clerks = SelectionServiceTestUtils.createListItems("clerk", "Clerk ");
        when(courtWorkerService.getCourtWorkerByTribunalOffice(tribunalOffice,
                CourtWorkerType.CLERK)).thenReturn(clerks);

        return courtWorkerService;
    }
}
