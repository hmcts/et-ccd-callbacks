package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.listing.ListingData;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.CourtWorkerType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.SelectionServiceTestUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.CourtWorkerService;

import java.util.List;
import java.util.stream.Stream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_LISTING_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_LISTING_CASE_TYPE_ID;

@SuppressWarnings({"PMD.LawOfDemeter", "PMD.UnusedPrivateMethod"})
class ClerkServiceTest {

    @Test
    void testInitialiseClerkResponsibleNoClerkSelected() {
        CourtWorkerService courtWorkerService = mockCourtWorkerService(TribunalOffice.BRISTOL);
        CaseData caseData = SelectionServiceTestUtils.createCaseData(TribunalOffice.BRISTOL);

        ClerkService clerkService = new ClerkService(courtWorkerService);
        clerkService.initialiseClerkResponsible(caseData);

        SelectionServiceTestUtils.verifyDynamicFixedListNoneSelected(caseData.getClerkResponsible(), "clerk", "Clerk ");
    }

    @Test
    void testInitialiseClerkResponsibleWithClerkSelected() {
        CourtWorkerService courtWorkerService = mockCourtWorkerService(TribunalOffice.BRISTOL);
        CaseData caseData = SelectionServiceTestUtils.createCaseData(TribunalOffice.BRISTOL);
        DynamicValueType selectedClerk = DynamicValueType.create("clerk2", "Clerk 2");
        caseData.setClerkResponsible(DynamicFixedListType.of(selectedClerk));

        ClerkService clerkService = new ClerkService(courtWorkerService);
        clerkService.initialiseClerkResponsible(caseData);

        SelectionServiceTestUtils.verifyDynamicFixedListSelected(caseData.getClerkResponsible(), "clerk", "Clerk ",
                selectedClerk);
    }

    @Test
    void testInitialiseClerkResponsibleMultipleDataNoClerkSelected() {
        CourtWorkerService courtWorkerService = mockCourtWorkerService(TribunalOffice.BRISTOL);
        MultipleData caseData = SelectionServiceTestUtils.createMultipleData(TribunalOffice.BRISTOL.getOfficeName());

        ClerkService clerkService = new ClerkService(courtWorkerService);
        clerkService.initialiseClerkResponsible(caseData);

        SelectionServiceTestUtils.verifyDynamicFixedListNoneSelected(caseData.getClerkResponsible(), "clerk", "Clerk ");
    }

    @Test
    void testInitialiseClerkResponsibleMultipleDataWithClerkSelected() {
        CourtWorkerService courtWorkerService = mockCourtWorkerService(TribunalOffice.BRISTOL);
        MultipleData caseData = SelectionServiceTestUtils.createMultipleData(TribunalOffice.BRISTOL.getOfficeName());
        DynamicValueType selectedClerk = DynamicValueType.create("clerk2", "Clerk 2");
        caseData.setClerkResponsible(DynamicFixedListType.of(selectedClerk));

        ClerkService clerkService = new ClerkService(courtWorkerService);
        clerkService.initialiseClerkResponsible(caseData);

        SelectionServiceTestUtils.verifyDynamicFixedListSelected(caseData.getClerkResponsible(), "clerk", "Clerk ",
                selectedClerk);
    }

    @Test
    void testInitialiseClerkResponsibleListingDataScotland() {
        CourtWorkerService courtWorkerService = mockScotlandCourtWorkerService();
        ListingData listingData = new ListingData();

        ClerkService clerkService = new ClerkService(courtWorkerService);
        clerkService.initialiseClerkResponsible(SCOTLAND_LISTING_CASE_TYPE_ID, listingData);

        SelectionServiceTestUtils.verifyDynamicFixedListNoneSelected(
                listingData.getClerkResponsible(), "scotland", "Scotland ");
    }

    @ParameterizedTest
    @MethodSource
    void testInitialiseClerkResponsibleListingDataEnglandWales(TribunalOffice tribunalOffice) {
        CourtWorkerService courtWorkerService = mockCourtWorkerService(tribunalOffice);
        ListingData listingData = new ListingData();
        listingData.setManagingOffice(tribunalOffice.getOfficeName());

        ClerkService clerkService = new ClerkService(courtWorkerService);
        clerkService.initialiseClerkResponsible(ENGLANDWALES_LISTING_CASE_TYPE_ID, listingData);

        SelectionServiceTestUtils.verifyDynamicFixedListNoneSelected(
                listingData.getClerkResponsible(), "clerk", "Clerk ");
    }

    private static Stream<Arguments> testInitialiseClerkResponsibleListingDataEnglandWales() {
        return TribunalOffice.ENGLANDWALES_OFFICES.stream().map(Arguments::of);
    }

    private CourtWorkerService mockCourtWorkerService(TribunalOffice tribunalOffice) {
        CourtWorkerService courtWorkerService = mock(CourtWorkerService.class);
        List<DynamicValueType> clerks = SelectionServiceTestUtils.createListItems("clerk", "Clerk ");
        when(courtWorkerService.getCourtWorkerByTribunalOffice(tribunalOffice,
                CourtWorkerType.CLERK)).thenReturn(clerks);

        return courtWorkerService;
    }

    private CourtWorkerService mockScotlandCourtWorkerService() {
        CourtWorkerService courtWorkerService = mock(CourtWorkerService.class);
        List<DynamicValueType> clerks = SelectionServiceTestUtils.createListItems("scotland", "Scotland ");
        when(courtWorkerService.getCourtWorkerByTribunalOffice(TribunalOffice.SCOTLAND,
                CourtWorkerType.CLERK)).thenReturn(clerks);

        return courtWorkerService;
    }

}
