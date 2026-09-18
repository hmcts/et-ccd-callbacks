package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import uk.gov.hmcts.ccd.sdk.impl.json.JsonCallbackBridge;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.FlagDetailType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.ListTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.AllPartyFlags;
import uk.gov.hmcts.et.common.model.ccd.types.CaseFlagsType;
import uk.gov.hmcts.et.common.model.ccd.types.SupportTaskState;
import uk.gov.hmcts.ethos.replacement.docmosis.config.SupportTaskConfiguration;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericRuntimeException;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementForCaseWorkerService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.SupportTaskEventService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.SupportTaskService;
import uk.gov.hmcts.ethos.replacement.docmosis.wa.SupportTaskClientContextService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.SupportTaskConstants.EVENT_CLOSE_ADMIN_REVIEW_SUPPORT_TASK;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.SupportTaskConstants.EVENT_CLOSE_JUDGE_REVIEW_SUPPORT_TASK;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.SupportTaskConstants.EVENT_CLOSE_LEGAL_OFFICER_REVIEW_SUPPORT_TASK;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.SupportTaskConstants.EVENT_CREATE_ARRANGE_SUPPORT_TASK;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.SupportTaskConstants.EVENT_CREATE_FLAG;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.SupportTaskConstants.EVENT_MANAGE_FLAGS;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.SupportTaskConstants.EVENT_MANAGE_SUPPORT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.SupportTaskConstants.EVENT_REQUEST_SUPPORT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.SupportTaskConstants.EVENT_REVIEW_ADMIN_SUPPORT_REQUEST;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.SupportTaskConstants.EVENT_REVIEW_JUDGE_SUPPORT_REQUEST;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.SupportTaskConstants.EVENT_REVIEW_LEGAL_OFFICER_SUPPORT_REQUEST;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.SupportTaskConstants.FLAG_STATUS_ACTIVE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.SupportTaskConstants.FLAG_STATUS_REQUESTED;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.SupportTaskConstants.TASK_TYPE_REVIEW_SUPPORT_ADMIN;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.SupportTaskConstants.TASK_TYPE_REVIEW_SUPPORT_JUDGE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.SupportTaskConstants.TASK_TYPE_REVIEW_SUPPORT_LEGAL_OFFICER;

@ExtendWith(MockitoExtension.class)
class SupportTaskCallbackLifecycleTest {
    private static final String ABOUT_TO_SUBMIT = "/supportTasks/aboutToSubmit";
    private static final String SUBMITTED = "/supportTasks/submitted";
    private static final String REVIEW_START = "/reviewSupportRequest/aboutToStart";
    private static final String REVIEW_SUBMIT = "/reviewSupportRequest/aboutToSubmit";
    private static final String CLIENT_CONTEXT = "client-context";
    private static final String TOKEN = "token";
    private static final String CASE_ID = "1234567890123456";
    private static final String JURISDICTION = "EMPLOYMENT";
    private static final String SCOTLAND = "ET_Scotland";
    private static final String ENGLAND_WALES = "ET_EnglandWales";
    private static final String ADMIN_CODE = "RA0041";
    private static final String JUDGE_CODE = "RA0038";
    private static final String LO_CODE = "RA0034";
    private static final String LIP_SPEAKER = "Lip speaker";
    private static final String TASK_LINK = "taskLink";
    private static final String MANUAL_REVIEW = "manualReview";
    private static final String LOCAL_CALLBACK = "${ET_COS_URL}";

    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private CaseManagementForCaseWorkerService caseManagementForCaseWorkerService;
    @Mock
    private CcdClient ccdClient;
    @Mock
    private AdminUserService adminUserService;
    @InjectMocks
    private CaseActionsForCaseWorkerController controller;

    private final ObjectMapper mapper = new ObjectMapper();
    private final List<String> committedEvents = new ArrayList<>();
    private final List<String> committedArrangeFlags = new ArrayList<>();
    private MockMvc mvc;
    private CaseData persisted;
    private String caseType;
    private boolean failArrangeSubmission;

    @BeforeEach
    void setUp() {
        SupportTaskConfiguration configuration = new SupportTaskConfiguration();
        configuration.getReview().setAdminFlagCodes(Set.of(ADMIN_CODE));
        configuration.getReview().setJudgeFlagCodes(Set.of(JUDGE_CODE));
        configuration.getReview().setLegalOfficerFlagCodes(Set.of(LO_CODE));
        configuration.getArrange().setFlagTitles(Map.of(ADMIN_CODE, LIP_SPEAKER, JUDGE_CODE, "Intermediary"));
        configuration.getClosureRetry().setMaxAttempts(3);
        configuration.getClosureRetry().setInitialBackoffMs(1);
        configuration.getClosureRetry().setMultiplier(2);
        configuration.getClosureRetry().setMaxBackoffMs(2);
        ReflectionTestUtils.setField(controller, "supportTaskService", new SupportTaskService(configuration));
        ReflectionTestUtils.setField(controller, "supportTaskClientContextService",
                new SupportTaskClientContextService(mapper));
        ReflectionTestUtils.setField(controller, "supportTaskEventService",
                new SupportTaskEventService(ccdClient, adminUserService, configuration));
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
        when(featureToggleService.isCaseFlagsV2Enabled(anyString())).thenReturn(true);
    }

    static Stream<Arguments> reviewLifecycles() {
        return Stream.of(
                new Category(ADMIN_CODE, "admin", TASK_TYPE_REVIEW_SUPPORT_ADMIN,
                        EVENT_REVIEW_ADMIN_SUPPORT_REQUEST, EVENT_CLOSE_ADMIN_REVIEW_SUPPORT_TASK),
                new Category(JUDGE_CODE, "judge", TASK_TYPE_REVIEW_SUPPORT_JUDGE,
                        EVENT_REVIEW_JUDGE_SUPPORT_REQUEST, EVENT_CLOSE_JUDGE_REVIEW_SUPPORT_TASK),
                new Category(LO_CODE, "legalOfficer", TASK_TYPE_REVIEW_SUPPORT_LEGAL_OFFICER,
                        EVENT_REVIEW_LEGAL_OFFICER_SUPPORT_REQUEST, EVENT_CLOSE_LEGAL_OFFICER_REVIEW_SUPPORT_TASK))
                .flatMap(category -> Stream.of(TASK_LINK, MANUAL_REVIEW, EVENT_MANAGE_FLAGS, EVENT_MANAGE_SUPPORT)
                        .flatMap(route -> Stream.of(ENGLAND_WALES, SCOTLAND)
                                .map(type -> Arguments.of(category, route, type))));
    }

    @ParameterizedTest
    @ValueSource(strings = {ABOUT_TO_SUBMIT, REVIEW_SUBMIT})
    void callbackSignatureIsAcceptedByDeployedCcdSdk(String callbackUrl) throws Exception {
        JsonCallbackBridge bridge = sdkBridge();

        assertDoesNotThrow(() -> bridge.validate(LOCAL_CALLBACK + callbackUrl));

        caseType = SCOTLAND;
        callback(callbackUrl, EVENT_CREATE_FLAG, new CaseData(), new CaseData(), null);
    }

    private JsonCallbackBridge sdkBridge() throws NoSuchMethodException {
        ApplicationContext context = mvc.getDispatcherServlet().getWebApplicationContext();
        return BeanUtils.instantiateClass(JsonCallbackBridge.class.getDeclaredConstructor(
                ApplicationContext.class, ObjectMapper.class, RequestMappingHandlerMapping.class, Environment.class),
                context, mapper, context.getBean(RequestMappingHandlerMapping.class),
                new MockEnvironment().withProperty("decentralisation.local-callback-placeholder", "ET_COS_URL"));
    }

    static Stream<Arguments> sdkInvocations() {
        return Stream.of(ABOUT_TO_SUBMIT, REVIEW_SUBMIT)
                .flatMap(url -> Stream.of(false, true).flatMap(requestedRemains -> Stream.of(false, true)
                        .map(withContext -> Arguments.of(url, requestedRemains, withContext))));
    }

    @ParameterizedTest
    @MethodSource("sdkInvocations")
    void sdkInvocationPreservesCompletionHeaderAndManualClosureFallback(
            String url, boolean requestedRemains, boolean withContext) throws Exception {
        caseType = SCOTLAND;
        CaseData before = new CaseData();
        before.setAllPartyFlags(AllPartyFlags.builder()
                .claimantFlags(flags("claimant-request", ADMIN_CODE, FLAG_STATUS_REQUESTED)).build());
        before.setSupportTaskState(SupportTaskState.builder().adminTaskCreated(YES).build());
        if (requestedRemains) {
            before.getAllPartyFlags().setRespondentExternalFlags(
                    flags("remaining-request", ADMIN_CODE, FLAG_STATUS_REQUESTED));
        }
        CaseData data = copy(before);
        if (REVIEW_SUBMIT.equals(url)) {
            data.setReviewSupportRequestFlags(ListTypeItem.from(GenericTypeItem.from("reviewed-section",
                    flags("claimant-request", ADMIN_CODE, FLAG_STATUS_ACTIVE))));
        } else {
            data.getAllPartyFlags().getClaimantFlags().getDetails().getFirst().getValue().setStatus(FLAG_STATUS_ACTIVE);
        }
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", TOKEN);
        if (withContext) {
            request.addHeader(CLIENT_CONTEXT, taskContext(TASK_TYPE_REVIEW_SUPPORT_ADMIN));
        }
        MockHttpServletResponse response = new MockHttpServletResponse();
        JsonCallbackBridge bridge = sdkBridge();
        Map<String, Object> payload = Map.of("case_details", details(data), "case_details_before", details(before),
                "event_id", REVIEW_SUBMIT.equals(url) ? EVENT_REVIEW_ADMIN_SUPPORT_REQUEST : EVENT_MANAGE_FLAGS);

        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, response));
        try {
            bridge.validate(LOCAL_CALLBACK + url);
            Object body = ReflectionTestUtils.invokeMethod(bridge, "invoke", LOCAL_CALLBACK + url, payload);
            JsonNode result = mapper.valueToTree(body);
            assertEquals(withContext && !requestedRemains ? NO : YES,
                    result.at("/data/supportTaskState/adminTaskCreated").textValue());
            assertEquals(FLAG_STATUS_ACTIVE,
                    result.at("/data/claimantFlags/details/0/value/status").textValue());
            if (withContext) {
                JsonNode returnedContext = mapper.readTree(
                        Base64.getDecoder().decode(response.getHeader(CLIENT_CONTEXT)));
                assertTrue(returnedContext.at("/client_context/user_task/complete_task").isBoolean());
                assertEquals(!requestedRemains,
                        returnedContext.at("/client_context/user_task/complete_task").booleanValue());
                assertEquals("review-task-id",
                        returnedContext.at("/client_context/user_task/task_data/id").textValue());
            } else {
                assertNull(response.getHeader(CLIENT_CONTEXT));
            }
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @ParameterizedTest(name = "{0}, {1}, {2}")
    @MethodSource("reviewLifecycles")
    void reviewLifecycleThroughHttpCallbacks(Category category, String route, String type) throws Exception {
        caseType = type;
        CaseData data = new CaseData();
        data.setAllPartyFlags(AllPartyFlags.builder()
                .claimantFlags(flags("claimant-request", category.code(), FLAG_STATUS_REQUESTED)).build());
        data = responseData(callback(ABOUT_TO_SUBMIT, EVENT_CREATE_FLAG, data, new CaseData(), null));
        assertState(data, category, YES, YES);

        CaseData before = copy(data);
        data.getAllPartyFlags().setRespondentExternalFlags(
                flags("respondent-request", category.code(), FLAG_STATUS_REQUESTED));
        data = responseData(callback(ABOUT_TO_SUBMIT, EVENT_REQUEST_SUPPORT, data, before, null));
        assertState(data, category, YES, null);

        data = actionFlag(data, category, route, "claimant-request", false);
        assertState(data, category, YES, null);
        verifyNoInteractions(ccdClient);

        if (!TASK_LINK.equals(route)) {
            mockCcdPersistence();
        }
        data = actionFlag(data, category, route, "respondent-request", true);
        assertState(data, category, NO, null);
        assertEquals(TASK_LINK.equals(route) ? List.of() : List.of(category.closeEvent()), committedEvents);
        if (TASK_LINK.equals(route)) {
            verifyNoInteractions(ccdClient);
        } else {
            verify(ccdClient).submitEventForCase(eq(TOKEN), any(CaseData.class), eq(type),
                    eq(JURISDICTION), any(CCDRequest.class), eq(CASE_ID));
        }

        before = copy(data);
        data.getAllPartyFlags().setClaimantRepresentativeExternalFlags(
                flags("new-request", category.code(), FLAG_STATUS_REQUESTED));
        data = responseData(callback(ABOUT_TO_SUBMIT, EVENT_CREATE_FLAG, data, before, null));
        assertState(data, category, YES, YES);
        verify(caseManagementForCaseWorkerService, times(5)).setNextListedDate(any(CaseData.class));
    }

    private CaseData actionFlag(CaseData caseData, Category category, String route, String flagId, boolean last)
            throws IOException {
        CaseData data = caseData;
        CaseData before = copy(data);
        boolean review = TASK_LINK.equals(route) || MANUAL_REVIEW.equals(route);
        String eventId = review ? category.reviewEvent() : route;
        if (review) {
            data = responseData(callback(REVIEW_START, eventId, data, before, null));
            assertEquals(last ? 1 : 2, data.getReviewSupportRequestFlags().size());
            data.getReviewSupportRequestFlags().stream().map(GenericTypeItem::getValue)
                    .flatMap(section -> section.getDetails().stream())
                    .filter(item -> flagId.equals(item.getId()))
                    .forEach(item -> item.getValue().setStatus(FLAG_STATUS_ACTIVE));
        } else {
            CaseFlagsType section = last ? data.getAllPartyFlags().getRespondentExternalFlags()
                    : data.getAllPartyFlags().getClaimantFlags();
            section.getDetails().getFirst().getValue().setStatus(FLAG_STATUS_ACTIVE);
        }
        String context = TASK_LINK.equals(route) ? taskContext(category.taskType()) : null;
        MvcResult result = callback(review ? REVIEW_SUBMIT : ABOUT_TO_SUBMIT, eventId, data, before, context);
        JsonNode response = mapper.readTree(result.getResponse().getContentAsString());
        assertTrue(response.path("errors").isMissingNode() || response.path("errors").isNull()
                || response.path("errors").isEmpty());
        if (context == null) {
            assertNull(result.getResponse().getHeader(CLIENT_CONTEXT));
        } else {
            JsonNode returnedContext = mapper.readTree(Base64.getDecoder().decode(
                    result.getResponse().getHeader(CLIENT_CONTEXT)));
            assertTrue(returnedContext.at("/client_context/user_task/complete_task").isBoolean());
            assertEquals(last, returnedContext.at("/client_context/user_task/complete_task").booleanValue());
            assertEquals("review-task-id", returnedContext.at("/client_context/user_task/task_data/id").textValue());
        }
        data = responseData(result);
        CaseFlagsType updated = last ? data.getAllPartyFlags().getRespondentExternalFlags()
                : data.getAllPartyFlags().getClaimantFlags();
        assertEquals(FLAG_STATUS_ACTIVE, updated.getDetails().getFirst().getValue().getStatus());
        if (review) {
            assertNull(data.getReviewSupportRequestFlags());
        }
        assertState(data, category, last && context != null ? NO : YES, null);
        persisted = copy(data);
        callback(SUBMITTED, eventId, data, before, null);
        return copy(persisted);
    }

    @Test
    void retriesArrangeFailureBeforeClosingPendingReviewWithoutDuplicatingCommittedTasks() throws Exception {
        caseType = SCOTLAND;
        mockCcdPersistence();
        CaseData before = new CaseData();
        before.setAllPartyFlags(AllPartyFlags.builder()
                .claimantFlags(flags("flag-1", ADMIN_CODE, FLAG_STATUS_REQUESTED)).build());
        for (int index = 2; index <= 4; index++) {
            before.getAllPartyFlags().getClaimantFlags().getDetails()
                    .addAll(flags("flag-" + index, ADMIN_CODE, FLAG_STATUS_REQUESTED).getDetails());
        }
        before.setSupportTaskState(SupportTaskState.builder().adminTaskCreated(YES).build());
        CaseData current = copy(before);
        current.getAllPartyFlags().getClaimantFlags().getDetails()
                .forEach(item -> item.getValue().setStatus(FLAG_STATUS_ACTIVE));
        CaseData submitted = responseData(callback(ABOUT_TO_SUBMIT, EVENT_MANAGE_FLAGS, current, before, null));
        assertEquals(YES, submitted.getSupportTaskState().getAdminTaskCreated());
        assertEquals(LIP_SPEAKER, submitted.getSupportTaskState().getArrangeSupportTaskName());
        persisted = copy(submitted);
        failArrangeSubmission = true;

        ServletException error = assertThrows(ServletException.class,
                () -> callback(SUBMITTED, EVENT_MANAGE_FLAGS, submitted, before, null));
        assertInstanceOf(GenericRuntimeException.class, error.getCause());
        assertEquals(List.of("flag-2", "flag-3"), committedArrangeFlags);
        assertEquals(YES, persisted.getSupportTaskState().getAdminTaskCreated());
        verify(ccdClient, times(5)).submitEventForCase(eq(TOKEN), any(CaseData.class), eq(caseType),
                eq(JURISDICTION), any(CCDRequest.class), eq(CASE_ID));
        verify(ccdClient, never()).startEventForCase(TOKEN, caseType, JURISDICTION, CASE_ID,
                EVENT_CLOSE_ADMIN_REVIEW_SUPPORT_TASK);

        failArrangeSubmission = false;
        callback(SUBMITTED, EVENT_MANAGE_FLAGS, submitted, before, null);
        assertEquals(List.of("flag-2", "flag-3", "flag-4"), committedArrangeFlags);
        assertEquals(List.of(EVENT_CREATE_ARRANGE_SUPPORT_TASK, EVENT_CREATE_ARRANGE_SUPPORT_TASK,
                EVENT_CREATE_ARRANGE_SUPPORT_TASK, EVENT_CLOSE_ADMIN_REVIEW_SUPPORT_TASK), committedEvents);
        assertEquals(NO, persisted.getSupportTaskState().getAdminTaskCreated());

        callback(SUBMITTED, EVENT_MANAGE_FLAGS, submitted, before, null);
        assertEquals(4, committedEvents.size());
        assertEquals(List.of("flag-2", "flag-3", "flag-4"), committedArrangeFlags);
        verify(ccdClient, times(7)).submitEventForCase(eq(TOKEN), any(CaseData.class), eq(caseType),
                eq(JURISDICTION), any(CCDRequest.class), eq(CASE_ID));
    }

    private void mockCcdPersistence() throws IOException {
        when(adminUserService.getAdminUserToken()).thenReturn(TOKEN);
        when(ccdClient.startEventForCase(eq(TOKEN), eq(caseType), eq(JURISDICTION), eq(CASE_ID), anyString()))
                .thenAnswer(invocation -> {
                    CCDRequest request = new CCDRequest(details(copy(persisted)));
                    request.setEventId(invocation.getArgument(4));
                    return request;
                });
        when(ccdClient.submitEventForCase(eq(TOKEN), any(CaseData.class), eq(caseType), eq(JURISDICTION),
                any(CCDRequest.class), eq(CASE_ID))).thenAnswer(invocation -> {
                    CaseData data = invocation.getArgument(1);
                    CCDRequest request = invocation.getArgument(4);
                    String flagId = data.getSupportTaskState().getArrangeSupportTaskFlagId();
                    if (EVENT_CREATE_ARRANGE_SUPPORT_TASK.equals(request.getEventId())) {
                        if (failArrangeSubmission && "flag-4".equals(flagId)) {
                            throw new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE);
                        }
                        committedArrangeFlags.add(flagId);
                    }
                    persisted = copy(data);
                    committedEvents.add(request.getEventId());
                    return null;
                });
    }

    @SneakyThrows
    private MvcResult callback(String url, String event, CaseData data, CaseData before, String context) {
        MockHttpServletRequestBuilder request = post(url).contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", TOKEN).content(mapper.writeValueAsBytes(Map.of(
                        "event_id", event, "case_details", details(data), "case_details_before", details(before))));
        if (context != null) {
            request.header(CLIENT_CONTEXT, context);
        }
        return mvc.perform(request).andExpect(status().isOk()).andReturn();
    }

    private CaseDetails details(CaseData data) {
        CaseDetails details = new CaseDetails();
        details.setCaseId(CASE_ID);
        details.setCaseTypeId(caseType);
        details.setJurisdiction(JURISDICTION);
        details.setCaseData(data);
        return details;
    }

    private CaseData responseData(MvcResult result) throws IOException {
        return mapper.treeToValue(mapper.readTree(result.getResponse().getContentAsString()).path("data"),
                CaseData.class);
    }

    private CaseData copy(CaseData data) throws IOException {
        return mapper.readValue(mapper.writeValueAsBytes(data), CaseData.class);
    }

    private String taskContext(String type) throws IOException {
        return Base64.getEncoder().encodeToString(mapper.writeValueAsBytes(Map.of("client_context",
                Map.of("user_task", Map.of("task_data", Map.of("id", "review-task-id", "type", type),
                        "complete_task", true)))));
    }

    private void assertState(CaseData data, Category category, String created, String required) {
        JsonNode state = mapper.valueToTree(data.getSupportTaskState());
        assertEquals(created, state.path(category.fieldPrefix() + "TaskCreated").asText(null));
        assertEquals(required, state.path(category.fieldPrefix() + "TaskRequired").asText(null));
    }

    private static CaseFlagsType flags(String id, String code, String status) {
        return CaseFlagsType.builder().details(ListTypeItem.from(GenericTypeItem.from(id,
                FlagDetailType.builder().flagCode(code).status(status).build()))).build();
    }

    private record Category(String code, String fieldPrefix, String taskType, String reviewEvent, String closeEvent) {
    }
}
