package uk.gov.hmcts.ethos.replacement.docmosis.service.excel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.items.JudgementTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.JurCodesTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.JudgementType;
import uk.gov.hmcts.et.common.model.ccd.types.JurCodesType;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultipleUtil;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EventValidationService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLOSED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MISSING_JUDGEMENT_JURISDICTION_MESSAGE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MISSING_JURISDICTION_OUTCOME_ERROR_MESSAGE;

@ExtendWith(SpringExtension.class)
class MultipleCloseEventValidationServiceTest {

    @Mock
    private MultipleHelperService multipleHelperService;
    @Mock
    private SingleCasesReadingService singleCasesReadingService;
    @Mock
    private EventValidationService eventValidationService;
    @InjectMocks
    private MultipleCloseEventValidationService multipleCloseEventValidationService;

    private MultipleDetails multipleDetails;
    private List<String> caseIdCollection;
    private List<String> errors;
    private String userToken;

    @BeforeEach
    public void setUp() {
        multipleDetails = new MultipleDetails();
        multipleDetails.setCaseData(MultipleUtil.getMultipleData());
        errors = new ArrayList<>();
        userToken = "authString";
        caseIdCollection = new ArrayList<>(List.of("245000/2020"));
    }

    @Test
    void multipleCloseEventValidationEmptyCaseIdCollection() {
        when(multipleHelperService.getEthosCaseRefCollection(
                userToken,
                multipleDetails.getCaseData(),
                errors)
        ).thenReturn(new ArrayList<>());

        List<String> errors = multipleCloseEventValidationService.validateCasesBeforeCloseEvent(
                                userToken,
                                multipleDetails);

        assertEquals(0, errors.size());
    }

    @Test
    void multipleCloseEventValidationReturnsErrors() {
        JurCodesTypeItem jurCodesTypeItem = new JurCodesTypeItem();
        jurCodesTypeItem.setId("TEST");
        jurCodesTypeItem.setValue(new JurCodesType());

        JudgementTypeItem judgmentTypeItem = new JudgementTypeItem();
        judgmentTypeItem.setId("TEST");
        judgmentTypeItem.setValue(new JudgementType());

        CaseData caseData = new CaseData();
        caseData.setEthosCaseReference("245004/2020");
        caseData.setJurCodesCollection(List.of(jurCodesTypeItem));
        caseData.setJudgementCollection(List.of(judgmentTypeItem));

        SubmitEvent submitEvent = getSubmitEventForCase(caseData);

        multipleDetails.getCaseData().setLeadCase(null);

        when(multipleHelperService.getEthosCaseRefCollection(
                userToken,
                multipleDetails.getCaseData(),
                errors)
        ).thenReturn(caseIdCollection);

        when(singleCasesReadingService.retrieveSingleCases(
                userToken,
                multipleDetails.getCaseTypeId(),
                caseIdCollection,
                multipleDetails.getCaseData().getMultipleSource())
        ).thenReturn(new ArrayList<>(Collections.singletonList(submitEvent)));

        doCallRealMethod().when(eventValidationService).validateCaseBeforeCloseEvent(isA(CaseData.class),
                eq(false), eq(true), anyList());

        doCallRealMethod().when(eventValidationService).validateJurisdictionOutcome(isA(CaseData.class),
                eq(false), eq(true), anyList());

        doCallRealMethod().when(eventValidationService).validateJudgementsHasJurisdiction(isA(CaseData.class),
                eq(true), anyList());

        List<String> errors = multipleCloseEventValidationService.validateCasesBeforeCloseEvent(
                userToken,
                multipleDetails);

        assertEquals(2, errors.size());
        assertEquals("245004/2020 - " + MISSING_JURISDICTION_OUTCOME_ERROR_MESSAGE, errors.get(0));
        assertEquals("245004/2020 - " + MISSING_JUDGEMENT_JURISDICTION_MESSAGE, errors.get(1));
    }

    @Test
    void multipleCloseEventValidationNoError() {
        JurCodesType jurCodeType = new JurCodesType();
        jurCodeType.setJudgmentOutcome("some outcome");
        JurCodesTypeItem jurCodesTypeItem = new JurCodesTypeItem();
        jurCodesTypeItem.setId("TEST");
        jurCodesTypeItem.setValue(jurCodeType);

        JudgementType judgmentType = new JudgementType();
        judgmentType.setJurisdictionCodes(List.of(jurCodesTypeItem));
        JudgementTypeItem judgmentTypeItem = new JudgementTypeItem();
        judgmentTypeItem.setId("TEST");
        judgmentTypeItem.setValue(judgmentType);

        CaseData caseData = new CaseData();
        caseData.setEthosCaseReference("245004/2020");
        caseData.setJurCodesCollection(List.of(jurCodesTypeItem));
        caseData.setJudgementCollection(List.of(judgmentTypeItem));

        SubmitEvent submitEvent = getSubmitEventForCase(caseData);

        multipleDetails.getCaseData().setLeadCase(null);

        when(multipleHelperService.getEthosCaseRefCollection(
                userToken,
                multipleDetails.getCaseData(),
                errors)
        ).thenReturn(caseIdCollection);

        when(singleCasesReadingService.retrieveSingleCases(
                userToken,
                multipleDetails.getCaseTypeId(),
                caseIdCollection,
                multipleDetails.getCaseData().getMultipleSource())
        ).thenReturn(new ArrayList<>(Collections.singletonList(submitEvent)));

        doCallRealMethod().when(eventValidationService).validateCaseBeforeCloseEvent(isA(CaseData.class),
                eq(false), eq(true), anyList());

        doCallRealMethod().when(eventValidationService).validateJurisdictionOutcome(isA(CaseData.class),
                eq(false), eq(true), anyList());

        doCallRealMethod().when(eventValidationService).validateJudgementsHasJurisdiction(isA(CaseData.class),
                eq(true), anyList());

        List<String> errors = multipleCloseEventValidationService.validateCasesBeforeCloseEvent(
                userToken,
                multipleDetails);

        assertEquals(0, errors.size());
    }

    private SubmitEvent getSubmitEventForCase(CaseData caseData) {
        SubmitEvent submitEvent = new SubmitEvent();
        submitEvent.setCaseData(caseData);
        submitEvent.setState(CLOSED_STATE);
        submitEvent.setCaseId(1_232_121_232);
        return submitEvent;
    }
}
