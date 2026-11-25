package uk.gov.hmcts.ethos.replacement.docmosis.controllers.multiples;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.et.common.model.multiples.MultipleRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.MultipleAmendService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.MultipleCreationMidEventValidationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.multiples.MultipleAmendReviewService;

import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ADD_CASES_TO_MULTIPLE_AMENDMENT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.LEAD_CASE_AMENDMENT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.REMOVE_CASES_FROM_MULTIPLE_AMENDMENT;

@ExtendWith(MockitoExtension.class)
class MultipleAmendControllerTest {

    private static final String USER_TOKEN = "authString";

    @Mock
    private MultipleAmendService multipleAmendService;
    @Mock
    private MultipleAmendReviewService multipleAmendReviewService;
    @Mock
    private MultipleCreationMidEventValidationService validationService;
    @InjectMocks
    private MultipleAmendController controller;

    private MockMvc mvc;
    private String requestBody;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
        MultipleData data = new MultipleData();
        data.setMultipleName("Mrs Test Auto");
        data.setMultipleReference("6000007");
        data.setTypeOfAmendmentMSL(List.of(LEAD_CASE_AMENDMENT,
                ADD_CASES_TO_MULTIPLE_AMENDMENT, REMOVE_CASES_FROM_MULTIPLE_AMENDMENT));
        MultipleDetails details = new MultipleDetails();
        details.setCaseId("1792493410971326");
        details.setCaseData(data);
        requestBody = new ObjectMapper().writeValueAsString(new MultipleRequest(details));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Updated note", ""})
    void savesNameAndNoteWithoutUpdatingCases(String note) throws Exception {
        ObjectNode request = (ObjectNode) new ObjectMapper().readTree(requestBody);
        ObjectNode data = (ObjectNode) request.path("case_details").path("case_data");
        data.put("multipleName", "Updated multiple name");
        data.put("multipleNote", note);

        mvc.perform(post("/multiples/amendDetails")
                .header("Authorization", USER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.multipleName").value("Updated multiple name"))
                .andExpect(jsonPath("$.data.multipleNote").value(note))
                .andExpect(jsonPath("$.data.typeOfAmendment").isEmpty())
                .andExpect(jsonPath("$.errors", hasSize(0)));

        verifyNoInteractions(multipleAmendService, multipleAmendReviewService);
    }

    @ParameterizedTest
    @MethodSource("amendments")
    void submitsOnlyTheRequestedAmendment(String url, String amendment) throws Exception {
        doAnswer(invocation -> {
            MultipleDetails details = invocation.getArgument(1);
            assertEquals("1792493410971326", details.getCaseId());
            assertEquals(List.of(amendment), details.getCaseData().getTypeOfAmendmentMSL());
            details.getCaseData().setMultipleAmendReview("Temporary review");
            return null;
        }).when(multipleAmendService).bulkAmendMultipleLogic(eq(USER_TOKEN), any(), anyList());

        mvc.perform(post(url)
                .header("Authorization", USER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.multipleReference").value("6000007"))
                .andExpect(jsonPath("$.errors", hasSize(0)))
                .andExpect(jsonPath("$.data.multipleAmendReview").isEmpty())
                .andExpect(jsonPath("$.confirmation_header").doesNotExist())
                .andExpect(jsonPath("$.confirmation_body").doesNotExist());

        verify(multipleAmendService).bulkAmendMultipleLogic(eq(USER_TOKEN), any(), anyList());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "/multiples/addCases", "/multiples/removeCases", "/multiples/updateLeadCase",
        "/multiples/addCases/review", "/multiples/removeCases/review"
    })
    void requiresAuthorizationHeader(String url) throws Exception {
        mvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(multipleAmendService, multipleAmendReviewService, validationService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/multiples/addCases", "/multiples/removeCases", "/multiples/updateLeadCase"})
    void returnsErrorsToPreventSubmission(String url) throws Exception {
        doAnswer(invocation -> {
            List<String> errors = invocation.getArgument(2);
            errors.add("Unable to amend the multiple");
            return null;
        }).when(multipleAmendService).bulkAmendMultipleLogic(eq(USER_TOKEN), any(), anyList());

        mvc.perform(post(url)
                .header("Authorization", USER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors[0]").value("Unable to amend the multiple"))
                .andExpect(jsonPath("$.data.multipleReference").value("6000007"));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void reviewsCasesWithoutSubmittingTheAmendment(boolean removing) throws Exception {
        doAnswer(invocation -> {
            MultipleDetails details = invocation.getArgument(1);
            details.getCaseData().setMultipleAmendReview("<table>Review</table>");
            List<String> warnings = invocation.getArgument(4);
            warnings.add("Case has not been accepted");
            return null;
        }).when(multipleAmendReviewService).prepareReview(
                eq(USER_TOKEN), any(), eq(removing), anyList(), anyList());

        mvc.perform(post(removing ? "/multiples/removeCases/review" : "/multiples/addCases/review")
                .header("Authorization", USER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.multipleAmendReview").value("<table>Review</table>"))
                .andExpect(jsonPath("$.warnings[0]").value("Case has not been accepted"))
                .andExpect(jsonPath("$.errors", hasSize(0)));

        verify(multipleAmendReviewService).prepareReview(
                eq(USER_TOKEN), any(), eq(removing), anyList(), anyList());
        verifyNoInteractions(multipleAmendService);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void returnsReviewErrorsWithoutSubmitting(boolean removing) throws Exception {
        doAnswer(invocation -> {
            List<String> errors = invocation.getArgument(3);
            errors.add("Unable to load case details");
            return null;
        }).when(multipleAmendReviewService).prepareReview(
                eq(USER_TOKEN), any(), eq(removing), anyList(), anyList());

        mvc.perform(post(removing ? "/multiples/removeCases/review" : "/multiples/addCases/review")
                .header("Authorization", USER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors[0]").value("Unable to load case details"));

        verifyNoInteractions(multipleAmendService);
    }

    @Test
    void rejectsUnacceptedCasesAtSubmissionEvenWhenWarningsAreIgnored() throws Exception {
        doAnswer(invocation -> {
            List<String> errors = invocation.getArgument(2);
            errors.add("[6000188/2026] cases have not been Accepted.");
            return null;
        }).when(validationService).multipleCreationValidationLogic(
                eq(USER_TOKEN), any(), anyList(), anyList(), eq(true));
        ObjectNode request = (ObjectNode) new ObjectMapper().readTree(requestBody);
        request.put("ignore_warning", true);

        mvc.perform(post("/multiples/addCases")
                .header("Authorization", USER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors[0]").value("[6000188/2026] cases have not been Accepted."))
                .andExpect(jsonPath("$.warnings").isEmpty());

        verifyNoInteractions(multipleAmendService);
    }

    private static Stream<Arguments> amendments() {
        return Stream.of(
                Arguments.of("/multiples/addCases", ADD_CASES_TO_MULTIPLE_AMENDMENT),
                Arguments.of("/multiples/removeCases", REMOVE_CASES_FROM_MULTIPLE_AMENDMENT),
                Arguments.of("/multiples/updateLeadCase", LEAD_CASE_AMENDMENT));
    }
}
