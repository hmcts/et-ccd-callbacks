package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.listing.ListingData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.CourtWorkerType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.SelectionServiceTestUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.CourtWorkerService;

import java.util.stream.Stream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_LISTING_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_LISTING_CASE_TYPE_ID;

class ClerkServiceTest {

    @Test
    void testInitialiseClerkResponsibleNoClerkSelected() {
        var courtWorkerService = mockCourtWorkerService(TribunalOffice.BRISTOL);
        var caseData = SelectionServiceTestUtils.createCaseData(TribunalOffice.BRISTOL);

        var clerkService = new ClerkService(courtWorkerService);
        clerkService.initialiseClerkResponsible(caseData);

        SelectionServiceTestUtils.verifyDynamicFixedListNoneSelected(caseData.getClerkResponsible(), "clerk", "Clerk ");
    }

    @Test
    void testInitialiseClerkResponsibleWithClerkSelected() {
        var courtWorkerService = mockCourtWorkerService(TribunalOffice.BRISTOL);
        var caseData = SelectionServiceTestUtils.createCaseData(TribunalOffice.BRISTOL);
        var selectedClerk = DynamicValueType.create("clerk2", "Clerk 2");
        caseData.setClerkResponsible(DynamicFixedListType.of(selectedClerk));

        var clerkService = new ClerkService(courtWorkerService);
        clerkService.initialiseClerkResponsible(caseData);

        SelectionServiceTestUtils.verifyDynamicFixedListSelected(caseData.getClerkResponsible(), "clerk", "Clerk ",
                selectedClerk);
    }

    @Test
    void testInitialiseClerkResponsibleMultipleDataNoClerkSelected() {
        var courtWorkerService = mockCourtWorkerService(TribunalOffice.BRISTOL);
        var caseData = SelectionServiceTestUtils.createMultipleData(TribunalOffice.BRISTOL.getOfficeName());

        var clerkService = new ClerkService(courtWorkerService);
        clerkService.initialiseClerkResponsible(caseData);

        SelectionServiceTestUtils.verifyDynamicFixedListNoneSelected(caseData.getClerkResponsible(), "clerk", "Clerk ");
    }

    @Test
    void testInitialiseClerkResponsibleMultipleDataWithClerkSelected() {
        var courtWorkerService = mockCourtWorkerService(TribunalOffice.BRISTOL);
        var caseData = SelectionServiceTestUtils.createMultipleData(TribunalOffice.BRISTOL.getOfficeName());
        var selectedClerk = DynamicValueType.create("clerk2", "Clerk 2");
        caseData.setClerkResponsible(DynamicFixedListType.of(selectedClerk));

        var clerkService = new ClerkService(courtWorkerService);
        clerkService.initialiseClerkResponsible(caseData);

        SelectionServiceTestUtils.verifyDynamicFixedListSelected(caseData.getClerkResponsible(), "clerk", "Clerk ",
                selectedClerk);
    }

    @Test
    void testInitialiseClerkResponsibleListingDataScotland() {
        var courtWorkerService = mockScotlandCourtWorkerService();
        var listingData = new ListingData();

        var clerkService = new ClerkService(courtWorkerService);
        clerkService.initialiseClerkResponsible(SCOTLAND_LISTING_CASE_TYPE_ID, listingData);

        SelectionServiceTestUtils.verifyDynamicFixedListNoneSelected(
                listingData.getClerkResponsible(), "scotland", "Scotland ");
    }

    @ParameterizedTest
    @MethodSource
    void testInitialiseClerkResponsibleListingDataEnglandWales(TribunalOffice tribunalOffice) {
        var courtWorkerService = mockCourtWorkerService(tribunalOffice);
        var listingData = new ListingData();
        listingData.setManagingOffice(tribunalOffice.getOfficeName());

        var clerkService = new ClerkService(courtWorkerService);
        clerkService.initialiseClerkResponsible(ENGLANDWALES_LISTING_CASE_TYPE_ID, listingData);

        SelectionServiceTestUtils.verifyDynamicFixedListNoneSelected(
                listingData.getClerkResponsible(), "clerk", "Clerk ");
    }

    private static Stream<Arguments> testInitialiseClerkResponsibleListingDataEnglandWales() {
        return TribunalOffice.ENGLANDWALES_OFFICES.stream().map(Arguments::of);
    }

    private CourtWorkerService mockCourtWorkerService(TribunalOffice tribunalOffice) {
        var courtWorkerService = mock(CourtWorkerService.class);
        var clerks = SelectionServiceTestUtils.createListItems("clerk", "Clerk ");
        when(courtWorkerService.getCourtWorkerByTribunalOffice(tribunalOffice,
                CourtWorkerType.CLERK)).thenReturn(clerks);

        return courtWorkerService;
    }

    private CourtWorkerService mockScotlandCourtWorkerService() {
        var courtWorkerService = mock(CourtWorkerService.class);
        var clerks = SelectionServiceTestUtils.createListItems("scotland", "Scotland ");
        when(courtWorkerService.getCourtWorkerByTribunalOffice(TribunalOffice.SCOTLAND,
                CourtWorkerType.CLERK)).thenReturn(clerks);

        return courtWorkerService;
    }

}
