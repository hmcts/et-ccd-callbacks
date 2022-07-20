package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.et.common.model.multiples.SubmitMultipleEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultipleUtil;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.MultipleCasesReadingService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.MultipleHelperService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_BULK_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MULTIPLE_CASE_TYPE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@RunWith(SpringJUnit4ClassRunner.class)
@ExtendWith(SpringExtension.class)
class AddSingleCaseToMultipleServiceTest {

    @Mock
    private MultipleHelperService multipleHelperService;
    @Mock
    private MultipleCasesReadingService multipleCasesReadingService;
    @InjectMocks
    private AddSingleCaseToMultipleService addSingleCaseToMultipleService;

    private CaseDetails caseDetails;
    private final String userToken = "authString";
    private final String multipleCaseTypeId = ENGLANDWALES_BULK_CASE_TYPE_ID;
    private MultipleDetails multipleDetails;
    private List<SubmitMultipleEvent> submitMultipleEvents;
    private final List<String> caseIdCollection = List.of("21006/2020", "245000/2020", "245001/2020");

    @BeforeEach
    void setUp() {
        multipleDetails = new MultipleDetails();
        multipleDetails.setCaseData(MultipleUtil.getMultipleData());
        multipleDetails.setCaseTypeId(multipleCaseTypeId);
        multipleDetails.setCaseId("12121212");

        caseDetails = new CaseDetails();
        caseDetails.setCaseTypeId(ENGLANDWALES_CASE_TYPE_ID);
        caseDetails.setCaseData(MultipleUtil.getCaseDataForSinglesToBeMoved());
        caseDetails.setCaseId("12321321");

        submitMultipleEvents = MultipleUtil.getSubmitMultipleEvents();
        submitMultipleEvents.get(0).setCaseId(12121212);
    }

    @Test
    void addSingleCaseToMultipleLogicLead() {
        List<String> errors = new ArrayList<>();
        when(multipleCasesReadingService.retrieveMultipleCases(userToken,
                multipleDetails.getCaseTypeId(),
                caseDetails.getCaseData().getMultipleReference())
        ).thenReturn(submitMultipleEvents);

        when(multipleHelperService.getEthosCaseRefCollection(userToken,
                submitMultipleEvents.get(0).getCaseData(),
                errors)
        ).thenReturn(caseIdCollection);

        addSingleCaseToMultipleService.addSingleCaseToMultipleLogic(userToken,
                caseDetails.getCaseData(),
                caseDetails.getCaseTypeId(),
                caseDetails.getJurisdiction(),
                caseDetails.getCaseId(),
                errors);

        verify(multipleHelperService, times(1)).getEthosCaseRefCollection(
                userToken,
                submitMultipleEvents.get(0).getCaseData(),
                errors);

        verify(multipleHelperService, times(1))
                .sendCreationUpdatesToSinglesWithoutConfirmation(
                    userToken,
                    multipleDetails.getCaseTypeId(),
                    multipleDetails.getJurisdiction(),
                    submitMultipleEvents.get(0).getCaseData(),
                    errors,
                    new ArrayList<>(Collections.singletonList("21006/2020")),
                    "",
                    multipleDetails.getCaseId());

        verify(multipleHelperService, times(1)).addLeadMarkUp(
                userToken,
                multipleCaseTypeId,
                submitMultipleEvents.get(0).getCaseData(),
                caseDetails.getCaseData().getEthosCaseReference(),
                caseDetails.getCaseId());
        String updatedSubMultipleName = caseDetails.getCaseData().getSubMultipleName();
        verify(multipleHelperService, times(1)).moveCasesAndSendUpdateToMultiple(
                userToken,
                updatedSubMultipleName,
                caseDetails.getJurisdiction(),
                multipleCaseTypeId,
                String.valueOf(submitMultipleEvents.get(0).getCaseId()),
                submitMultipleEvents.get(0).getCaseData(),
                new ArrayList<>(Collections.singletonList(caseDetails.getCaseData().getEthosCaseReference())),
                new ArrayList<>());

        assertEquals(MULTIPLE_CASE_TYPE, caseDetails.getCaseData().getEcmCaseType());
        assertEquals("246000", caseDetails.getCaseData().getMultipleReference());
        assertEquals(YES, caseDetails.getCaseData().getLeadClaimant());
    }

    @Test
    void addSingleCaseToMultipleLogicNoLead() {

        caseDetails.getCaseData().setLeadClaimant(NO);

        List<String> errors = new ArrayList<>();
        when(multipleCasesReadingService.retrieveMultipleCases(userToken,
                multipleDetails.getCaseTypeId(),
                caseDetails.getCaseData().getMultipleReference())
        ).thenReturn(submitMultipleEvents);

        when(multipleHelperService.getEthosCaseRefCollection(userToken,
                submitMultipleEvents.get(0).getCaseData(),
                errors)
        ).thenReturn(caseIdCollection);

        addSingleCaseToMultipleService.addSingleCaseToMultipleLogic(userToken,
                caseDetails.getCaseData(),
                caseDetails.getCaseTypeId(),
                caseDetails.getJurisdiction(),
                caseDetails.getCaseId(),
                errors);

        verify(multipleHelperService, times(1)).getEthosCaseRefCollection(
                userToken,
                submitMultipleEvents.get(0).getCaseData(),
                errors);
        String updatedSubMultipleName = caseDetails.getCaseData().getSubMultipleName();
        verify(multipleHelperService, times(1)).moveCasesAndSendUpdateToMultiple(
                userToken,
                updatedSubMultipleName,
                caseDetails.getJurisdiction(),
                multipleCaseTypeId,
                String.valueOf(submitMultipleEvents.get(0).getCaseId()),
                submitMultipleEvents.get(0).getCaseData(),
                new ArrayList<>(Collections.singletonList(caseDetails.getCaseData().getEthosCaseReference())),
                new ArrayList<>());

        verifyNoMoreInteractions(multipleHelperService);

        assertEquals(MULTIPLE_CASE_TYPE, caseDetails.getCaseData().getEcmCaseType());
        assertEquals("246000", caseDetails.getCaseData().getMultipleReference());
        assertEquals(NO, caseDetails.getCaseData().getLeadClaimant());
    }

    @Test
    void addSingleCaseToMultipleLogicNoLeadButWithEmptyMultiple() {

        caseDetails.getCaseData().setLeadClaimant(NO);
        submitMultipleEvents.get(0).getCaseData().setCaseIdCollection(null);
        submitMultipleEvents.get(0).getCaseData().setLeadCase(null);
        List<String> errors = new ArrayList<>();

        when(multipleCasesReadingService.retrieveMultipleCases(userToken,
                multipleDetails.getCaseTypeId(),
                caseDetails.getCaseData().getMultipleReference())
        ).thenReturn(submitMultipleEvents);

        when(multipleHelperService.getEthosCaseRefCollection(userToken,
                submitMultipleEvents.get(0).getCaseData(),
                errors)
        ).thenReturn(new ArrayList<>());

        addSingleCaseToMultipleService.addSingleCaseToMultipleLogic(userToken,
                caseDetails.getCaseData(),
                caseDetails.getCaseTypeId(),
                caseDetails.getJurisdiction(),
                caseDetails.getCaseId(),
                errors);

        verify(multipleHelperService, times(1)).getEthosCaseRefCollection(
                userToken,
                submitMultipleEvents.get(0).getCaseData(),
                errors);

        verify(multipleHelperService, times(1)).addLeadMarkUp(
                userToken,
                multipleCaseTypeId,
                submitMultipleEvents.get(0).getCaseData(),
                caseDetails.getCaseData().getEthosCaseReference(),
                caseDetails.getCaseId());
        String updatedSubMultipleName = caseDetails.getCaseData().getSubMultipleName();
        verify(multipleHelperService, times(1)).moveCasesAndSendUpdateToMultiple(
                userToken,
                updatedSubMultipleName,
                caseDetails.getJurisdiction(),
                multipleCaseTypeId,
                String.valueOf(submitMultipleEvents.get(0).getCaseId()),
                submitMultipleEvents.get(0).getCaseData(),
                new ArrayList<>(Collections.singletonList(caseDetails.getCaseData().getEthosCaseReference())),
                new ArrayList<>());

        verifyNoMoreInteractions(multipleHelperService);

        assertEquals(MULTIPLE_CASE_TYPE, caseDetails.getCaseData().getEcmCaseType());
        assertEquals("246000", caseDetails.getCaseData().getMultipleReference());
        assertEquals(YES, caseDetails.getCaseData().getLeadClaimant());

    }

    @Test
    void addSingleCaseToMultiple_DifferentManagingOffice() {
        List<String> errors = new ArrayList<>();
        submitMultipleEvents.get(0).getCaseData().setManagingOffice(TribunalOffice.NEWCASTLE.getOfficeName());
        caseDetails.getCaseData().setManagingOffice(TribunalOffice.MANCHESTER.getOfficeName());

        when(multipleCasesReadingService.retrieveMultipleCases(userToken,
                multipleDetails.getCaseTypeId(),
                caseDetails.getCaseData().getMultipleReference())
        ).thenReturn(submitMultipleEvents);

        when(multipleHelperService.getEthosCaseRefCollection(userToken,
                submitMultipleEvents.get(0).getCaseData(),
                errors)
        ).thenReturn(caseIdCollection);

        addSingleCaseToMultipleService.addSingleCaseToMultipleLogic(userToken,
                caseDetails.getCaseData(),
                caseDetails.getCaseTypeId(),
                caseDetails.getJurisdiction(),
                caseDetails.getCaseId(),
                errors);

        verify(multipleHelperService, times(0)).getEthosCaseRefCollection(
                userToken,
                submitMultipleEvents.get(0).getCaseData(),
                errors);

        verify(multipleHelperService, times(0))
                .sendCreationUpdatesToSinglesWithoutConfirmation(
                        userToken,
                        multipleDetails.getCaseTypeId(),
                        multipleDetails.getJurisdiction(),
                        submitMultipleEvents.get(0).getCaseData(),
                        errors,
                        new ArrayList<>(Collections.singletonList("21006/2020")),
                        "",
                        multipleDetails.getCaseId());

        verify(multipleHelperService, times(0)).addLeadMarkUp(
                userToken,
                multipleCaseTypeId,
                submitMultipleEvents.get(0).getCaseData(),
                caseDetails.getCaseData().getEthosCaseReference(),
                caseDetails.getCaseId());
        String updatedSubMultipleName = caseDetails.getCaseData().getSubMultipleName();
        verify(multipleHelperService, times(0)).moveCasesAndSendUpdateToMultiple(
                userToken,
                updatedSubMultipleName,
                caseDetails.getJurisdiction(),
                multipleCaseTypeId,
                String.valueOf(submitMultipleEvents.get(0).getCaseId()),
                submitMultipleEvents.get(0).getCaseData(),
                new ArrayList<>(Collections.singletonList(caseDetails.getCaseData().getEthosCaseReference())),
                new ArrayList<>());

        var multipleData = submitMultipleEvents.get(0).getCaseData();
        assertEquals(1, errors.size());
        assertEquals(String.format("Multiple %s is managed by %s", multipleData.getMultipleReference(),
                multipleData.getManagingOffice()), errors.get(0));
    }

}