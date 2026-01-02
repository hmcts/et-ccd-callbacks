package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.ecm.common.model.helper.Constants;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DateListedType;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;
import uk.gov.hmcts.ethos.replacement.docmosis.DocmosisApplication;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.HearingsHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseFlagsService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementForCaseWorkerService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DocumentManagementService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.InitialConsiderationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ReportDataService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.TornadoService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_TYPE_JUDICIAL_HEARING;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@ExtendWith(SpringExtension.class)
@WebMvcTest({InitialConsiderationController.class, JsonMapper.class})
@ContextConfiguration(classes = DocmosisApplication.class)
class InitialConsiderationControllerTest extends BaseControllerTest {

    private static final String COMPLETE_INITIAL_CONSIDERATION_URL = "/completeInitialConsideration";
    private static final String START_INITIAL_CONSIDERATION_URL = "/startInitialConsideration";
    private static final String SUBMIT_INITIAL_CONSIDERATION_URL = "/submitInitialConsideration";

    @Autowired
    private WebApplicationContext applicationContext;

    @MockBean
    private InitialConsiderationService initialConsiderationService;

    @MockBean
    private CaseManagementForCaseWorkerService caseManagementForCaseWorkerService;

    @MockBean
    private DocumentManagementService documentManagementService;

    @MockBean
    private ReportDataService reportDataService;

    @MockBean
    private TornadoService tornadoService;

    @MockBean
    private CaseFlagsService caseFlagsService;

    @MockBean
    private FeatureToggleService featureToggleService;

    private MockMvc mvc;

    private CCDRequest ccdRequest;

    @Autowired
    private JsonMapper jsonMapper;

    @BeforeEach
    @Override
    protected void setUp() throws IOException, URISyntaxException {
        super.setUp();
        mvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
        when(featureToggleService.isHmcEnabled()).thenReturn(true);

        String hearingNumber = "123";
        String venue = "Some venue";

        CaseDetails caseDetails = CaseDataBuilder.builder()
            .withChooseEt3Respondent("Jack")
            .withRespondent("Jack", YES, "2022-03-01", false)
            .withClaimServedDate("2022-01-01")
            .withHearing(hearingNumber, HEARING_TYPE_JUDICIAL_HEARING, "Judge", venue, null, null, null, null)
            .withHearingSession(0, "2019-11-25T12:11:00.000", Constants.HEARING_STATUS_LISTED, false)
            .withRegionalOfficeList("Leeds")
            .withEt1TribunalRegion("Leeds")
            .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);

        ccdRequest = CCDRequestBuilder.builder()
            .withCaseData(caseDetails.getCaseData())
            .build();
    }

    @Test
    void shouldSetEtICHearingAlreadyListedToYes_WhenEarliestListedHearingTypeIsNotNull() {
        // Mock hearing collection with at least one listed hearing
        var hearingType = new HearingType();
        hearingType.setHearingType(HEARING_TYPE_JUDICIAL_HEARING);
        var dateListedTypeItem = new DateListedTypeItem();
        var dateListedType = new DateListedType();
        dateListedType.setListedDate("2023-10-10T10:00:00.000");
        dateListedTypeItem.setValue(dateListedType);
        hearingType.setHearingDateCollection(List.of(dateListedTypeItem));
        var hearingTypeItem = new HearingTypeItem();
        hearingTypeItem.setValue(hearingType);
        CaseData caseData = new CaseData();
        caseData.setHearingCollection(List.of(hearingTypeItem));

        // Mock HearingsHelper to return a non-null HearingType instance
        try (MockedStatic<HearingsHelper> hearingsHelperMock = Mockito.mockStatic(HearingsHelper.class)) {
            hearingsHelperMock.when(() -> HearingsHelper.getEarliestListedHearingType(caseData.getHearingCollection()))
                    .thenReturn(new HearingType());

            if (HearingsHelper.getEarliestListedHearingType(caseData.getHearingCollection()) != null) {
                caseData.setEtICHearingAlreadyListed("Yes");
            }

            assertEquals("Yes", caseData.getEtICHearingAlreadyListed());
        }
    }

    @Test
    void initICCompleteTokenOk() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(COMPLETE_INITIAL_CONSIDERATION_URL)
                .content(jsonMapper.toJson(ccdRequest))
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.confirmation_header", notNullValue()));
    }

    @Test
    void initICCompleteTokenFail() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(COMPLETE_INITIAL_CONSIDERATION_URL)
                .content(jsonMapper.toJson(ccdRequest))
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    void initICCompleteBadRequest() throws Exception {
        mvc.perform(post(COMPLETE_INITIAL_CONSIDERATION_URL)
                .content("bad request")
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    void submitInitialConsideration_TokenOk() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(SUBMIT_INITIAL_CONSIDERATION_URL)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
        verify(caseFlagsService, times(1)).setPrivateHearingFlag(any());
    }

    @Test
    void submitInitialConsideration_TokenFail() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(SUBMIT_INITIAL_CONSIDERATION_URL)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void submitInitialConsideration_BadRequest() throws Exception {
        mvc.perform(post(SUBMIT_INITIAL_CONSIDERATION_URL)
                        .content("bad request")
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void startInitialConsiderationTest() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(initialConsiderationService.generateJurisdictionCodesHtml(anyList(), any())).thenReturn("Jurisdictions");
        when(initialConsiderationService.getHearingDetails(anyList())).thenReturn("hearings");

        mvc.perform(post(START_INITIAL_CONSIDERATION_URL)
                .content(jsonMapper.toJson(ccdRequest))
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath(JsonMapper.DATA, notNullValue()));
    }

    @Test
    void setRegionalOfficeAndEt1TribunalRegion_BothListsNotNull() {
        DynamicFixedListType regionalOfficeList = new DynamicFixedListType();
        DynamicValueType dvt = new DynamicValueType();
        dvt.setLabel("Leeds");
        regionalOfficeList.setValue(dvt);

        DynamicFixedListType et1HearingVenues = new DynamicFixedListType();
        DynamicValueType et1dvt = new DynamicValueType();
        et1dvt.setLabel("Manchester");
        et1HearingVenues.setValue(et1dvt);
        CaseData caseData = new CaseData();
        caseData.setRegionalOfficeList(regionalOfficeList);
        caseData.setEt1HearingVenues(et1HearingVenues);

        caseData.setRegionalOffice(caseData.getRegionalOfficeList() != null
                ? caseData.getRegionalOfficeList().getSelectedLabel() : null);
        caseData.setEt1TribunalRegion(caseData.getEt1HearingVenues() != null
                ? caseData.getEt1HearingVenues().getSelectedLabel() : null);

        assertEquals("Leeds", caseData.getRegionalOffice());
        assertEquals("Manchester", caseData.getEt1TribunalRegion());
    }

    @Test
    void setRegionalOfficeAndEt1TribunalRegion_RegionalOfficeListNull() {
        CaseData caseData = new CaseData();
        DynamicFixedListType et1HearingVenues = new DynamicFixedListType();
        DynamicValueType dvt = new DynamicValueType();
        dvt.setLabel("Manchester");
        et1HearingVenues.setValue(dvt);
        caseData.setRegionalOfficeList(null);
        caseData.setEt1HearingVenues(et1HearingVenues);

        caseData.setRegionalOffice(caseData.getRegionalOfficeList() != null
                ? caseData.getRegionalOfficeList().getSelectedLabel() : null);
        caseData.setEt1TribunalRegion(caseData.getEt1HearingVenues() != null
                ? caseData.getEt1HearingVenues().getSelectedLabel() : null);

        assertNull(caseData.getRegionalOffice());
        assertEquals("Manchester", caseData.getEt1TribunalRegion());
    }

    @Test
    void setRegionalOfficeAndEt1TribunalRegion_Et1HearingVenuesNull() {
        CaseData caseData = new CaseData();
        DynamicFixedListType regionalOfficeList = new DynamicFixedListType();
        DynamicValueType dvt = new DynamicValueType();
        dvt.setLabel("Leeds");
        regionalOfficeList.setValue(dvt);

        caseData.setRegionalOfficeList(regionalOfficeList);
        caseData.setEt1HearingVenues(null);

        caseData.setRegionalOffice(caseData.getRegionalOfficeList() != null
                ? caseData.getRegionalOfficeList().getSelectedLabel() : null);
        caseData.setEt1TribunalRegion(caseData.getEt1HearingVenues() != null
                ? caseData.getEt1HearingVenues().getSelectedLabel() : null);

        assertEquals("Leeds", caseData.getRegionalOffice());
        assertNull(caseData.getEt1TribunalRegion());
    }

    @Test
    void setRegionalOfficeAndEt1TribunalRegion_BothListsNull() {
        CaseData caseData = new CaseData();

        caseData.setRegionalOfficeList(null);
        caseData.setEt1HearingVenues(null);

        caseData.setRegionalOffice(caseData.getRegionalOfficeList() != null
                ? caseData.getRegionalOfficeList().getSelectedLabel() : null);
        caseData.setEt1TribunalRegion(caseData.getEt1HearingVenues() != null
                ? caseData.getEt1HearingVenues().getSelectedLabel() : null);

        assertNull(caseData.getRegionalOffice());
        assertNull(caseData.getEt1TribunalRegion());
    }

    @Test
    void mapOldIcHearingNotListedOptionsToNew_WhenListIsNotEmpty() {
        CaseData caseData = new CaseData();
        caseData.setEtICHearingNotListedList(List.of("Option1", "Option2"));

        initialConsiderationService.mapOldIcHearingNotListedOptionsToNew(caseData, ENGLANDWALES_CASE_TYPE_ID);

        verify(initialConsiderationService, times(1))
                .mapOldIcHearingNotListedOptionsToNew(caseData, ENGLANDWALES_CASE_TYPE_ID);
    }
}
