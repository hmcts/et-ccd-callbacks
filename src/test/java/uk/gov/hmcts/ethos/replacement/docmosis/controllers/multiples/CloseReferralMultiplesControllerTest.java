package uk.gov.hmcts.ethos.replacement.docmosis.controllers.multiples;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.ecm.common.model.helper.Constants;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.et.common.model.multiples.MultipleRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.controllers.BaseControllerTest;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReferralHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseLookupService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_BULK_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_TYPE_JUDICIAL_HEARING;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.ReferralsUtil.createReferralTypeItem;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.ReferralsUtil.createRespondentType;

@ExtendWith(SpringExtension.class)
@WebMvcTest({CloseReferralMultiplesController.class, JsonMapper.class})
class CloseReferralMultiplesControllerTest extends BaseControllerTest {

    private static final String ABOUT_TO_START = "/multiples/closeReferral/aboutToStart";
    private static final String MID_HEARING_DETAILS_URL = "/multiples/closeReferral/initHearingAndReferralDetails";
    private static final String ABOUT_TO_SUBMIT_URL = "/multiples/closeReferral/aboutToSubmit";
    private static final String SUBMITTED_URL = "/multiples/closeReferral/completeCloseReferral";

    @MockBean
    private CaseLookupService caseLookupService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JsonMapper jsonMapper;

    private MultipleRequest request;

    @BeforeEach
    @Override
    protected void setUp() throws IOException, URISyntaxException {
        super.setUp();
        CaseData caseData = CaseDataBuilder.builder()
                .withHearing("1", "test", "Judy", "Venue", List.of("Telephone", "Video"),
                        "length num", "type", "Yes")
                .withHearingScotland("hearingNumber", HEARING_TYPE_JUDICIAL_HEARING, "Judge",
                        TribunalOffice.ABERDEEN, "venue")
                .withHearingSession(
                        0, "2019-11-25T12:11:00.000",
                        Constants.HEARING_STATUS_HEARD,
                        true)
                .build();

        List<HearingTypeItem> hearings = new ArrayList<>();
        caseData.setHearingCollection(hearings);
        caseData.setIsJudge("Yes");
        caseData.setRespondentCollection(new ArrayList<>(Collections.singletonList(createRespondentType())));
        caseData.setEthosCaseReference("caseRef");
        caseData.setClaimant("claimant");
        caseData.setIsUrgent("Yes");
        caseData.setReplyToEmailAddress("test@gmail.com");

        MultipleData multipleData = MultipleData.builder().build();
        request = new MultipleRequest();
        MultipleDetails multipleDetails = new MultipleDetails();
        multipleDetails.setCaseData(multipleData);
        multipleDetails.setCaseTypeId(ENGLANDWALES_BULK_CASE_TYPE_ID);
        request.setCaseDetails(multipleDetails);

        multipleData.setReferralCollection(List.of(createReferralTypeItem()));
        DynamicFixedListType selectReferralList =
                ReferralHelper.populateSelectReferralDropdown(multipleData.getReferralCollection());
        selectReferralList.setValue(new DynamicValueType());
        selectReferralList.getValue().setCode("1");
        multipleData.setSelectReferral(selectReferralList);

        UserDetails userDetails = new UserDetails();
        userDetails.setRoles(List.of("role1"));
        when(caseLookupService.getLeadCaseFromMultipleAsAdmin(any())).thenReturn(caseData);
    }

    @Test
    void aboutToStartCloseReferral_Success() throws Exception {

        mockMvc.perform(post(ABOUT_TO_START)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    void initHearingAndReferralDetails_Success() throws Exception {

        mockMvc.perform(post(MID_HEARING_DETAILS_URL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    void aboutToSubmit_CloseReferral_tokenOk() throws Exception {

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    void completeCloseReferral_tokenOk() throws Exception {

        mockMvc.perform(post(SUBMITTED_URL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmation_body", notNullValue()));
    }

}
