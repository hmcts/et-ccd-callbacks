package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.et.common.client.CcdClient;
import uk.gov.hmcts.et.common.model.reference.ReferenceData;
import uk.gov.hmcts.et.common.model.reference.ReferenceDetails;
import uk.gov.hmcts.et.common.model.reference.ReferenceSubmitEvent;
import uk.gov.hmcts.et.common.model.reference.types.ClerkType;
import uk.gov.hmcts.et.common.model.reference.types.JudgeType;
import uk.gov.hmcts.et.common.model.reference.types.VenueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.InternalException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.InternalException.ERROR_MESSAGE;

@RunWith(SpringJUnit4ClassRunner.class)
public class ReferenceServiceTest {

    @Mock
    private CcdClient ccdClient;

    @InjectMocks
    private ReferenceService referenceService;

    private CaseDetails caseDetails;

    private List<ReferenceSubmitEvent> referenceSubmitEvents;
    private List<ReferenceSubmitEvent> referenceSubmitEventsNoVenues;
    private List<ReferenceSubmitEvent> referenceSubmitEventsNoClerks;
    private List<ReferenceSubmitEvent> referenceSubmitEventsNoJudges;

    @Before
    public void setUp() {

        caseDetails = new CaseDetails();
        CaseData caseData = new CaseData();
        caseData.setHearingVenue(null);
        caseDetails.setCaseData(caseData);
        caseDetails.setJurisdiction("EMPLOYMENT");

        ReferenceSubmitEvent referenceSubmitEvent1 = new ReferenceSubmitEvent();
        ReferenceData referenceData1 = new ReferenceData();
        VenueType venueType1 = new VenueType();
        venueType1.setVenueName("Venue1");
        referenceData1.setVenueType(venueType1);
        referenceSubmitEvent1.setCaseData(referenceData1);
        referenceSubmitEvent1.setCaseId(1);

        ReferenceSubmitEvent referenceSubmitEvent2 = new ReferenceSubmitEvent();
        ReferenceData referenceData2 = new ReferenceData();
        VenueType venueType2 = new VenueType();
        venueType2.setVenueName("Venue2");
        referenceData2.setVenueType(venueType2);
        referenceSubmitEvent2.setCaseData(referenceData2);
        referenceSubmitEvent2.setCaseId(2);

        ReferenceSubmitEvent referenceSubmitEvent3 = new ReferenceSubmitEvent();
        ReferenceData referenceData3 = new ReferenceData();
        VenueType venueType3 = new VenueType();
        venueType3.setVenueName("Venue3");
        referenceData3.setVenueType(venueType3);
        referenceSubmitEvent3.setCaseData(referenceData3);
        referenceSubmitEvent3.setCaseId(3);

        ReferenceData referenceData4 = new ReferenceData();
        ClerkType clerkType4 = new ClerkType();
        clerkType4.setFirstName("First Name 4");
        clerkType4.setLastName("Last Name 4");
        referenceData4.setClerkType(clerkType4);
        ReferenceSubmitEvent referenceSubmitEvent4 = new ReferenceSubmitEvent();
        referenceSubmitEvent4.setCaseData(referenceData4);
        referenceSubmitEvent4.setCaseId(4);
        ReferenceData referenceData5 = new ReferenceData();
        ClerkType clerkType5 = new ClerkType();
        clerkType5.setFirstName("First Name 5");
        clerkType5.setLastName("Last Name 5");
        referenceData5.setClerkType(clerkType5);
        ReferenceSubmitEvent referenceSubmitEvent5 = new ReferenceSubmitEvent();
        referenceSubmitEvent5.setCaseData(referenceData5);
        referenceSubmitEvent5.setCaseId(5);

        ReferenceSubmitEvent referenceSubmitEvent6 = new ReferenceSubmitEvent();
        ReferenceData referenceData6 = new ReferenceData();
        JudgeType judgeType6 = new JudgeType();
        judgeType6.setJudgeDisplayName("Judge6");
        referenceData6.setJudgeType(judgeType6);
        referenceSubmitEvent6.setCaseData(referenceData6);
        referenceSubmitEvent6.setCaseId(6);

        referenceSubmitEvents = new ArrayList<>(Arrays.asList(referenceSubmitEvent1,
                referenceSubmitEvent2, referenceSubmitEvent3));
        referenceSubmitEvents.add(referenceSubmitEvent4);
        referenceSubmitEvents.add(referenceSubmitEvent5);
        referenceSubmitEvents.add(referenceSubmitEvent6);

        referenceSubmitEventsNoVenues = new ArrayList<>(Arrays.asList(referenceSubmitEvent4,
                referenceSubmitEvent5, referenceSubmitEvent6));

        referenceSubmitEventsNoClerks = new ArrayList<>(Arrays.asList(referenceSubmitEvent1,
                referenceSubmitEvent2, referenceSubmitEvent3));
        referenceSubmitEventsNoClerks.add(referenceSubmitEvent6);

        referenceSubmitEventsNoJudges = new ArrayList<>(Arrays.asList(referenceSubmitEvent1,
                referenceSubmitEvent2, referenceSubmitEvent3));
        referenceSubmitEventsNoJudges.add(referenceSubmitEvent4);
        referenceSubmitEventsNoJudges.add(referenceSubmitEvent5);

        var referenceDetails = new ReferenceDetails();
        referenceDetails.setCaseData(referenceData6);
        referenceDetails.setJurisdiction("EMPLOYMENT");
    }

    @Test
    public void fetchHearingVenueRefDataWithThreeVenuesPresent() throws IOException {
        when(ccdClient.retrieveReferenceDataCases(anyString(), anyString(), anyString()))
                .thenReturn(referenceSubmitEvents);
        CaseData caseDataResult = referenceService.fetchHearingVenueRefData(caseDetails, "authToken");
        assertNotNull(caseDataResult);
    }

    @Test
    public void fetchHearingVenueRefDataWithNoVenuesRefData() throws IOException {
        when(ccdClient.retrieveReferenceDataCases(anyString(), anyString(), anyString()))
                .thenReturn(referenceSubmitEventsNoVenues);
        CaseData caseDataResult = referenceService.fetchHearingVenueRefData(caseDetails, "authToken");
        assertNotNull(caseDataResult);
    }

    @Test
    public void fetchHearingVenueRefDataWithNoReferenceData() throws IOException {
        referenceSubmitEvents.clear();
        when(ccdClient.retrieveReferenceDataCases(anyString(), anyString(), anyString()))
                .thenReturn(referenceSubmitEvents);
        CaseData caseDataResult = referenceService.fetchHearingVenueRefData(caseDetails, "authToken");
        assertNotNull(caseDataResult);
    }

    @Test(expected = Exception.class)
    public void fetchHearingVenueRefDataException() throws IOException {
        when(ccdClient.retrieveReferenceDataCases(anyString(), anyString(), anyString()))
                .thenThrow(new InternalException(ERROR_MESSAGE));
        referenceService.fetchHearingVenueRefData(caseDetails, "authToken");
    }

    @Test
    public void fetchDateListedRefDataWithAllRefDataPresent() throws IOException {
        when(ccdClient.retrieveReferenceDataCases(anyString(), anyString(), anyString()))
                .thenReturn(referenceSubmitEvents);
        CaseData caseDataResult = referenceService.fetchDateListedRefData(caseDetails, "authToken");
        assertNotNull(caseDataResult);
    }

    @Test
    public void fetchDateListedRefDataWithNoVenuesRefData() throws IOException {
        when(ccdClient.retrieveReferenceDataCases(anyString(), anyString(), anyString()))
                .thenReturn(referenceSubmitEventsNoVenues);
        CaseData caseDataResult = referenceService.fetchDateListedRefData(caseDetails, "authToken");
        assertNotNull(caseDataResult);
    }

    @Test
    public void fetchDateListedRefDataWithNoClerksRefData() throws IOException {
        when(ccdClient.retrieveReferenceDataCases(anyString(), anyString(), anyString()))
                .thenReturn(referenceSubmitEventsNoClerks);
        CaseData caseDataResult = referenceService.fetchDateListedRefData(caseDetails, "authToken");
        assertNotNull(caseDataResult);
    }

    @Test
    public void fetchDateListedRefDataWithNoJudgesRefData() throws IOException {
        when(ccdClient.retrieveReferenceDataCases(anyString(), anyString(), anyString()))
                .thenReturn(referenceSubmitEventsNoJudges);
        CaseData caseDataResult = referenceService.fetchDateListedRefData(caseDetails, "authToken");
        assertNotNull(caseDataResult);
    }

    @Test
    public void fetchDateListedRefDataWithNoReferenceData() throws IOException {
        referenceSubmitEvents.clear();
        when(ccdClient.retrieveReferenceDataCases(anyString(), anyString(), anyString()))
                .thenReturn(referenceSubmitEvents);
        CaseData caseDataResult = referenceService.fetchDateListedRefData(caseDetails, "authToken");
        assertNotNull(caseDataResult);
    }

    @Test(expected = Exception.class)
    public void fetchDateListedRefDataException() throws IOException {
        when(ccdClient.retrieveReferenceDataCases(anyString(), anyString(), anyString()))
                .thenThrow(new InternalException(ERROR_MESSAGE));
        referenceService.fetchDateListedRefData(caseDetails, "authToken");
    }

}
