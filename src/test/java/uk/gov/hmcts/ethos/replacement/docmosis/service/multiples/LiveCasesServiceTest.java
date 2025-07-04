package uk.gov.hmcts.ethos.replacement.docmosis.service.multiples;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultipleUtil;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.SingleCasesReadingService;

import java.util.List;
import java.util.SortedMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ACCEPTED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLOSED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultipleUtil.getSubmitEvents;

@ExtendWith(SpringExtension.class)
class LiveCasesServiceTest {
    @Mock
    SingleCasesReadingService singleCasesReadingService;
    @Mock
    FeatureToggleService featureToggleService;

    @InjectMocks
    private LiveCasesService liveCasesService;

    private SortedMap<String, Object> multipleObjectsFlags;
    private MultipleDetails multipleDetails;
    private String userToken;
    List<SubmitEvent> cases;

    @BeforeEach
    public void setUp() {
        multipleObjectsFlags = MultipleUtil.getMultipleObjectsFlags();
        multipleDetails = new MultipleDetails();
        multipleDetails.setCaseData(MultipleUtil.getMultipleData());
        userToken = "authString";
        cases = getSubmitEvents();
        cases.get(0).setState(CLOSED_STATE);
        when(featureToggleService.isMultiplesEnabled()).thenReturn(true);
        when(singleCasesReadingService.retrieveSingleCases(any(), any(), any(), any())).thenReturn(cases);
    }

    @Test
    void shouldRemoveClosedCases() {
        multipleDetails.getCaseData().setLiveCases(YES);

        liveCasesService.filterLiveCases(userToken, multipleDetails.getCaseTypeId(), multipleObjectsFlags,
                multipleDetails.getCaseData());

        assertEquals(2, multipleObjectsFlags.size());
        assertNull(multipleDetails.getCaseData().getLiveCases());
    }

    @Test
    void shouldNotRemoveClosedCasesWhenNoneClosed() {
        multipleDetails.getCaseData().setLiveCases(YES);
        cases.get(0).setState(ACCEPTED_STATE);

        liveCasesService.filterLiveCases(userToken, multipleDetails.getCaseTypeId(), multipleObjectsFlags,
                multipleDetails.getCaseData());

        assertEquals(3, multipleObjectsFlags.size());
        assertNull(multipleDetails.getCaseData().getLiveCases());
    }

    @Test
    void shouldNotRemoveClosedCases() {
        multipleDetails.getCaseData().setLiveCases(NO);

        liveCasesService.filterLiveCases(userToken, multipleDetails.getCaseTypeId(), multipleObjectsFlags,
                multipleDetails.getCaseData());

        assertEquals(3, multipleObjectsFlags.size());
        assertNull(multipleDetails.getCaseData().getLiveCases());
    }

    @Test
    void shouldNotRemoveClosedCasesWhenMultiplesDisabled() {
        when(featureToggleService.isMultiplesEnabled()).thenReturn(false);
        liveCasesService.filterLiveCases(userToken, multipleDetails.getCaseTypeId(), multipleObjectsFlags,
                multipleDetails.getCaseData());
        assertEquals(3, multipleObjectsFlags.size());
    }
}
