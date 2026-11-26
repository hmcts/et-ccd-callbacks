package uk.gov.hmcts.ethos.replacement.docmosis.service.multiples;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.et.common.model.bulk.items.CaseIdTypeItem;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.MultipleCreationMidEventValidationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.SingleCasesReadingService;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_BULK_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MANUALLY_CREATED_POSITION;

@ExtendWith(MockitoExtension.class)
class MultipleAmendReviewServiceTest {

    private static final String USER_TOKEN = "authString";
    private static final String FIRST_REFERENCE = "6000187/2026";
    private static final String SECOND_REFERENCE = "6000188/2026";

    @Mock
    private MultipleCreationMidEventValidationService validationService;
    @Mock
    private SingleCasesReadingService singleCasesReadingService;
    @Mock
    private MultipleAmendReviewRenderer renderer;
    @InjectMocks
    private MultipleAmendReviewService service;

    private MultipleDetails details;
    private CaseData firstCase;
    private CaseData secondCase;
    private List<String> errors;
    private List<String> warnings;

    @BeforeEach
    void setUp() {
        details = new MultipleDetails();
        details.setCaseTypeId(ENGLANDWALES_BULK_CASE_TYPE_ID);
        details.setCaseData(new MultipleData());
        details.getCaseData().setCaseIdCollection(List.of(
                CaseIdTypeItem.from(SECOND_REFERENCE),
                CaseIdTypeItem.from(FIRST_REFERENCE),
                CaseIdTypeItem.from(SECOND_REFERENCE)));
        details.getCaseData().setAltCaseIdCollection(List.of(CaseIdTypeItem.from(FIRST_REFERENCE)));
        details.getCaseData().setMultipleAmendReview("Outdated review");
        firstCase = new CaseData();
        firstCase.setEthosCaseReference(FIRST_REFERENCE);
        secondCase = new CaseData();
        secondCase.setEthosCaseReference(SECOND_REFERENCE);
        errors = new ArrayList<>();
        warnings = new ArrayList<>();
    }

    @Test
    void preparesAddReviewInInputOrderWithDuplicatesRemoved() {
        List<String> references = List.of(SECOND_REFERENCE, FIRST_REFERENCE);
        when(singleCasesReadingService.retrieveSingleCases(
                USER_TOKEN, ENGLANDWALES_BULK_CASE_TYPE_ID, references, MANUALLY_CREATED_POSITION))
                .thenReturn(List.of(event(firstCase), event(secondCase)));
        when(renderer.render(List.of(secondCase, firstCase), false)).thenReturn("Fresh add review");

        service.prepareReview(USER_TOKEN, details, false, errors, warnings);

        verify(validationService).multipleCreationValidationLogic(USER_TOKEN, details, errors, warnings, true);
        assertThat(details.getCaseData().getMultipleAmendReview()).isEqualTo("Fresh add review");
        assertThat(errors).isEmpty();
    }

    @Test
    void preparesRemoveReviewUsingOnlyTheRemovalSelection() {
        when(singleCasesReadingService.retrieveSingleCases(
                USER_TOKEN, ENGLANDWALES_BULK_CASE_TYPE_ID, List.of(FIRST_REFERENCE), MANUALLY_CREATED_POSITION))
                .thenReturn(List.of(event(firstCase)));
        when(renderer.render(List.of(firstCase), true)).thenReturn("Fresh removal review");

        service.prepareReview(USER_TOKEN, details, true, errors, warnings);

        verify(validationService).multipleRemoveCasesValidationLogic(USER_TOKEN, details, errors);
        assertThat(details.getCaseData().getMultipleAmendReview()).isEqualTo("Fresh removal review");
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void clearsStaleReviewAndStopsWhenValidationFails(boolean removing) {
        if (removing) {
            doAnswer(invocation -> errors.add("Case is not part of the multiple"))
                    .when(validationService).multipleRemoveCasesValidationLogic(any(), any(), anyList());
        } else {
            doAnswer(invocation -> errors.add("Case does not exist"))
                    .when(validationService).multipleCreationValidationLogic(
                            any(), any(), anyList(), anyList(), anyBoolean());
        }

        service.prepareReview(USER_TOKEN, details, removing, errors, warnings);

        assertThat(details.getCaseData().getMultipleAmendReview()).isNull();
        assertThat(errors).hasSize(1);
        verifyNoInteractions(singleCasesReadingService, renderer);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void rejectsEmptySelections(boolean removing) {
        details.getCaseData().setCaseIdCollection(null);
        details.getCaseData().setAltCaseIdCollection(null);

        service.prepareReview(USER_TOKEN, details, removing, errors, warnings);

        assertThat(errors).containsExactly("Enter at least one case number.");
        assertThat(details.getCaseData().getMultipleAmendReview()).isNull();
        verifyNoInteractions(singleCasesReadingService, renderer);
    }

    @Test
    void doesNotShowAnIncompleteTableWhenDetailsCannotBeLoaded() {
        when(singleCasesReadingService.retrieveSingleCases(
                eq(USER_TOKEN), eq(ENGLANDWALES_BULK_CASE_TYPE_ID), anyList(), eq(MANUALLY_CREATED_POSITION)))
                .thenReturn(List.of(event(firstCase)));

        service.prepareReview(USER_TOKEN, details, false, errors, warnings);

        assertThat(errors).hasSize(1).first().asString().contains(SECOND_REFERENCE);
        assertThat(details.getCaseData().getMultipleAmendReview()).isNull();
        verifyNoInteractions(renderer);
    }

    @Test
    void preservesWarningsWhilePreparingReviewForAcknowledgement() {
        doAnswer(invocation -> warnings.add("Case has not been accepted"))
                .when(validationService).multipleCreationValidationLogic(
                        any(), any(), anyList(), anyList(), anyBoolean());
        when(singleCasesReadingService.retrieveSingleCases(
                eq(USER_TOKEN), eq(ENGLANDWALES_BULK_CASE_TYPE_ID), anyList(), eq(MANUALLY_CREATED_POSITION)))
                .thenReturn(List.of(event(firstCase), event(secondCase)));
        when(renderer.render(List.of(secondCase, firstCase), false)).thenReturn("Review after warning");

        service.prepareReview(USER_TOKEN, details, false, errors, warnings);

        assertThat(warnings).containsExactly("Case has not been accepted");
        assertThat(details.getCaseData().getMultipleAmendReview()).isEqualTo("Review after warning");
    }

    @Test
    void rebuildsReviewAfterCaseNumbersAreEdited() {
        when(singleCasesReadingService.retrieveSingleCases(
                eq(USER_TOKEN), eq(ENGLANDWALES_BULK_CASE_TYPE_ID), anyList(), eq(MANUALLY_CREATED_POSITION)))
                .thenReturn(List.of(event(firstCase), event(secondCase)));
        when(renderer.render(List.of(secondCase, firstCase), false)).thenReturn("Two cases");
        when(renderer.render(List.of(firstCase), false)).thenReturn("One case");
        service.prepareReview(USER_TOKEN, details, false, errors, warnings);

        details.getCaseData().setCaseIdCollection(List.of(CaseIdTypeItem.from(FIRST_REFERENCE)));
        service.prepareReview(USER_TOKEN, details, false, errors, warnings);

        assertThat(details.getCaseData().getMultipleAmendReview()).isEqualTo("One case");
        assertThat(details.getCaseData().getCaseIdCollection()).hasSize(1);
    }

    private SubmitEvent event(CaseData data) {
        SubmitEvent event = new SubmitEvent();
        event.setCaseData(data);
        return event;
    }
}
