package uk.gov.hmcts.ethos.replacement.docmosis.service.excel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.items.JudgementTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.JurCodesTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.CasePreAcceptType;
import uk.gov.hmcts.et.common.model.ccd.types.JudgementType;
import uk.gov.hmcts.et.common.model.ccd.types.JurCodesType;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.et.common.model.multiples.SubmitMultipleEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.HelperTest;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultipleUtil;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserIdamService;
import uk.gov.hmcts.ethos.replacement.docmosis.servicebus.CreateUpdatesBusSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TRANSFERRED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@ExtendWith(SpringExtension.class)
class MultipleHelperServiceTest {

    @Mock
    private SingleCasesReadingService singleCasesReadingService;
    @Mock
    private MultipleCasesReadingService multipleCasesReadingService;
    @Mock
    private ExcelReadingService excelReadingService;
    @Mock
    private ExcelDocManagementService excelDocManagementService;
    @Mock
    private MultipleCasesSendingService multipleCasesSendingService;
    @Mock
    private CreateUpdatesBusSender createUpdatesBusSender;
    @Mock
    private UserIdamService userIdamService;
    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private MultipleHelperService multipleHelperService;
    private MultipleDetails multipleDetails;
    private String userToken;
    private List<SubmitEvent> submitEventList;
    private List<SubmitMultipleEvent> submitMultipleEvents;
    private TreeMap<String, Object> multipleObjects;
    private String gatewayURL;

    @BeforeEach
    public void setUp() {
        gatewayURL = "https://manage-case.test.platform.hmcts.net";
        ReflectionTestUtils.setField(multipleHelperService, "ccdGatewayBaseUrl", gatewayURL);
        multipleDetails = new MultipleDetails();
        multipleDetails.setCaseData(MultipleUtil.getMultipleData());
        multipleDetails.setCaseTypeId("Manchester_Multiple");
        multipleDetails.setCaseId("12121212");
        submitEventList = MultipleUtil.getSubmitEvents();
        UserDetails userDetails = HelperTest.getUserDetails();
        when(userIdamService.getUserDetails(anyString())).thenReturn(userDetails);
        userToken = "authString";
        submitMultipleEvents = MultipleUtil.getSubmitMultipleEvents();
        multipleObjects = MultipleUtil.getMultipleObjectsAll();
    }

    @Test
    void addLeadMarkUp() {
        when(singleCasesReadingService.retrieveSingleCase(userToken,
                multipleDetails.getCaseTypeId(),
                multipleDetails.getCaseData().getLeadCase(),
                multipleDetails.getCaseData().getMultipleSource()))
                .thenReturn(submitEventList.get(0));
        when(featureToggleService.isMultiplesEnabled()).thenReturn(true);
        multipleHelperService.addLeadMarkUp(userToken,
                multipleDetails.getCaseTypeId(),
                multipleDetails.getCaseData(),
                multipleDetails.getCaseData().getLeadCase(),
                "");
        assertEquals("<a target=\"_blank\" href=\"" + gatewayURL + "/cases/case-details/1232121232\">21006/2020</a>",
                multipleDetails.getCaseData().getLeadCase());
        assertEquals("1232121232", multipleDetails.getCaseData().getLeadCaseId());
    }

    @Test
    void addLeadMarkUpMultipleOff() {
        when(singleCasesReadingService.retrieveSingleCase(userToken,
                multipleDetails.getCaseTypeId(),
                multipleDetails.getCaseData().getLeadCase(),
                multipleDetails.getCaseData().getMultipleSource()))
                .thenReturn(submitEventList.get(0));
        when(featureToggleService.isMultiplesEnabled()).thenReturn(false);
        multipleHelperService.addLeadMarkUp(userToken,
                multipleDetails.getCaseTypeId(),
                multipleDetails.getCaseData(),
                multipleDetails.getCaseData().getLeadCase(),
                "");
        assertEquals("<a target=\"_blank\" href=\"" + gatewayURL + "/cases/case-details/1232121232\">21006/2020</a>",
                multipleDetails.getCaseData().getLeadCase());    
        assertNull(multipleDetails.getCaseData().getLeadCaseId());
    }

    @Test
    void addLeadMarkUpMultiple2On() {
        submitEventList.get(0).getCaseData().setNextListedDate("2020-01-01");

        when(singleCasesReadingService.retrieveSingleCase(userToken,
                multipleDetails.getCaseTypeId(),
                multipleDetails.getCaseData().getLeadCase(),
                multipleDetails.getCaseData().getMultipleSource()))
                .thenReturn(submitEventList.get(0));

        when(featureToggleService.isMul2Enabled()).thenReturn(true);

        multipleHelperService.addLeadMarkUp(userToken,
                multipleDetails.getCaseTypeId(),
                multipleDetails.getCaseData(),
                multipleDetails.getCaseData().getLeadCase(),
                "");

        assertEquals("2020-01-01", multipleDetails.getCaseData().getNextListedDate());
    }

    @Test
    void addLeadMarkUpWithCaseId() {
        submitEventList.get(0).setCaseId(12_345L);
        when(singleCasesReadingService.retrieveSingleCase(userToken,
                multipleDetails.getCaseTypeId(),
                multipleDetails.getCaseData().getLeadCase(),
                multipleDetails.getCaseData().getMultipleSource()))
                .thenReturn(submitEventList.get(0));
        when(featureToggleService.isMultiplesEnabled()).thenReturn(true);
        multipleHelperService.addLeadMarkUp(userToken,
                multipleDetails.getCaseTypeId(),
                multipleDetails.getCaseData(),
                multipleDetails.getCaseData().getLeadCase(),
                "12345");
        assertEquals("<a target=\"_blank\" href=\"" + gatewayURL + "/cases/case-details/12345\">21006/2020</a>",
                multipleDetails.getCaseData().getLeadCase());
        assertEquals("12345", multipleDetails.getCaseData().getLeadCaseId());
    }

    @Test
    void addLeadMarkUpWithCaseIdMultipleOff() {
        submitEventList.get(0).setCaseId(12_345L);
        when(singleCasesReadingService.retrieveSingleCase(userToken,
                multipleDetails.getCaseTypeId(),
                multipleDetails.getCaseData().getLeadCase(),
                multipleDetails.getCaseData().getMultipleSource()))
                .thenReturn(submitEventList.get(0));
        when(featureToggleService.isMultiplesEnabled()).thenReturn(false);
        multipleHelperService.addLeadMarkUp(userToken,
                multipleDetails.getCaseTypeId(),
                multipleDetails.getCaseData(),
                multipleDetails.getCaseData().getLeadCase(),
                "12345");
        assertEquals("<a target=\"_blank\" href=\"" + gatewayURL + "/cases/case-details/12345\">21006/2020</a>",
                multipleDetails.getCaseData().getLeadCase());
        assertNull(multipleDetails.getCaseData().getLeadCaseId());
    }

    @Test
    void addLeadMarkUpEmptyCase() {

        when(singleCasesReadingService.retrieveSingleCase(userToken,
                multipleDetails.getCaseTypeId(),
                multipleDetails.getCaseData().getLeadCase(),
                multipleDetails.getCaseData().getMultipleSource()))
                .thenReturn(null);
        when(featureToggleService.isMultiplesEnabled()).thenReturn(true);
        multipleHelperService.addLeadMarkUp(userToken,
                multipleDetails.getCaseTypeId(),
                multipleDetails.getCaseData(),
                multipleDetails.getCaseData().getLeadCase(),
                "");
        assertEquals("21006/2020", multipleDetails.getCaseData().getLeadCase());
        assertNull(multipleDetails.getCaseData().getLeadCaseId());
    }

    @Test
    void multipleValidationLogicMultipleAndSubExist() {

        List<String> errors = new ArrayList<>();

        String multipleReference = "246001";
        String subMultipleName = "SubMultiple";

        when(multipleCasesReadingService.retrieveMultipleCases(userToken,
                multipleDetails.getCaseTypeId(),
                multipleReference)
        ).thenReturn(submitMultipleEvents);

        multipleHelperService.validateExternalMultipleAndSubMultiple(userToken,
                multipleDetails.getCaseTypeId(),
                multipleReference,
                subMultipleName,
                errors);

        assertEquals(0, errors.size());

    }

    @Test
    void multipleValidationLogicSubMultipleDoesNotExist() {

        List<String> errors = new ArrayList<>();

        String multipleReference = "246001";
        String subMultipleName = "SubMultiple3";

        when(multipleCasesReadingService.retrieveMultipleCases(userToken,
                multipleDetails.getCaseTypeId(),
                multipleReference)
        ).thenReturn(submitMultipleEvents);

        multipleHelperService.validateExternalMultipleAndSubMultiple(userToken,
                multipleDetails.getCaseTypeId(),
                multipleReference,
                subMultipleName,
                errors);

        assertEquals("Sub multiple SubMultiple3 does not exist in 246001", errors.get(0));

    }

    @Test
    void multipleValidationLogicSubMultipleNull() {

        List<String> errors = new ArrayList<>();

        String multipleReference = "246001";
        String subMultipleName = "SubMultiple3";

        when(multipleCasesReadingService.retrieveMultipleCases(userToken,
                multipleDetails.getCaseTypeId(),
                multipleReference)
        ).thenReturn(submitMultipleEvents);

        submitMultipleEvents.get(0).getCaseData().setSubMultipleCollection(null);

        multipleHelperService.validateExternalMultipleAndSubMultiple(userToken,
                multipleDetails.getCaseTypeId(),
                multipleReference,
                subMultipleName,
                errors);

        assertEquals("Sub multiple SubMultiple3 does not exist in 246001", errors.get(0));

    }

    @Test
    void multipleValidationLogicMultipleDoesNotExist() {

        List<String> errors = new ArrayList<>();

        String multipleReference = "246002";
        String subMultipleName = "SubMultiple3";

        when(multipleCasesReadingService.retrieveMultipleCases(userToken,
                multipleDetails.getCaseTypeId(),
                multipleReference)
        ).thenReturn(new ArrayList<>());

        multipleHelperService.validateExternalMultipleAndSubMultiple(userToken,
                multipleDetails.getCaseTypeId(),
                multipleReference,
                subMultipleName,
                errors);

        assertEquals("Multiple 246002 does not exist", errors.get(0));

    }

    @Test
    void multipleValidationLogicMultipleTransferred() {

        List<String> errors = new ArrayList<>();

        String multipleReference = "246001";
        String subMultipleName = "SubMultiple";

        submitMultipleEvents.get(0).setState(TRANSFERRED_STATE);
        when(multipleCasesReadingService.retrieveMultipleCases(userToken,
                multipleDetails.getCaseTypeId(),
                multipleReference)
        ).thenReturn(submitMultipleEvents);

        multipleHelperService.validateExternalMultipleAndSubMultiple(userToken,
                multipleDetails.getCaseTypeId(),
                multipleReference,
                subMultipleName,
                errors);

        assertEquals(1, errors.size());
        assertEquals("Multiple 246001 has been transferred. "
                + "The case cannot be moved to this multiple", errors.get(0));

    }

    @Test
    void moveCasesAndSendUpdateToMultiple() {

        String subMultipleName = "SubMultiple3";

        when(excelReadingService.readExcel(anyString(), anyString(), anyList(), any(), any()))
                .thenReturn(multipleObjects);

        multipleHelperService.moveCasesAndSendUpdateToMultiple(userToken,
                subMultipleName,
                multipleDetails.getJurisdiction(),
                multipleDetails.getCaseTypeId(),
                multipleDetails.getCaseId(),
                multipleDetails.getCaseData(),
                new ArrayList<>(Arrays.asList("245002/2020", "245003/2020")),
                new ArrayList<>());

        verify(excelDocManagementService, times(1)).generateAndUploadExcel(
                anyList(),
                anyString(),
                any());
        verifyNoMoreInteractions(excelDocManagementService);

        verify(multipleCasesSendingService, times(1)).sendUpdateToMultiple(
                userToken,
                multipleDetails.getCaseTypeId(),
                multipleDetails.getJurisdiction(),
                multipleDetails.getCaseData(),
                multipleDetails.getCaseId());
        verifyNoMoreInteractions(multipleCasesSendingService);

    }

    @Test
    void moveCasesAndSendUpdateToMultipleWithoutSubMultiple() {

        String subMultipleName = "";

        when(excelReadingService.readExcel(anyString(), anyString(), anyList(), any(), any()))
                .thenReturn(multipleObjects);

        multipleHelperService.moveCasesAndSendUpdateToMultiple(userToken,
                subMultipleName,
                multipleDetails.getJurisdiction(),
                multipleDetails.getCaseTypeId(),
                multipleDetails.getCaseId(),
                multipleDetails.getCaseData(),
                new ArrayList<>(Arrays.asList("245002/2020", "245003/2020")),
                new ArrayList<>());

        verify(excelDocManagementService, times(1)).generateAndUploadExcel(
                anyList(),
                anyString(),
                any());
        verifyNoMoreInteractions(excelDocManagementService);

        verify(multipleCasesSendingService, times(1)).sendUpdateToMultiple(
                userToken,
                multipleDetails.getCaseTypeId(),
                multipleDetails.getJurisdiction(),
                multipleDetails.getCaseData(),
                multipleDetails.getCaseId());
        verifyNoMoreInteractions(multipleCasesSendingService);

    }

    @Test
    void sendCreationUpdatesToSinglesWithoutConfirmation() {

        multipleHelperService.sendCreationUpdatesToSinglesWithoutConfirmation(
                userToken,
                multipleDetails.getCaseTypeId(),
                multipleDetails.getJurisdiction(),
                multipleDetails.getCaseData(),
                new ArrayList<>(),
                new ArrayList<>(),
                "",
                multipleDetails.getCaseId()
        );

        verify(userIdamService).getUserDetails(userToken);
        verifyNoMoreInteractions(userIdamService);

    }

    @Test
    void sendDetachUpdatesToSinglesWithoutConfirmation() {

        multipleHelperService.sendDetachUpdatesToSinglesWithoutConfirmation(
                userToken,
                multipleDetails,
                new ArrayList<>(),
                multipleObjects
        );

        verify(userIdamService).getUserDetails(userToken);
        verifyNoMoreInteractions(userIdamService);

    }

    @Test
    void sendResetMultipleStateWithoutConfirmation() {

        multipleHelperService.sendResetMultipleStateWithoutConfirmation(
                userToken,
                multipleDetails.getCaseTypeId(),
                multipleDetails.getJurisdiction(),
                multipleDetails.getCaseData(),
                new ArrayList<>(),
                multipleDetails.getCaseId()
        );

        verify(userIdamService).getUserDetails(userToken);
        verifyNoMoreInteractions(userIdamService);

    }

    @Test
    void sendUpdatesToSinglesWithConfirmationNullCaseData() {

        multipleHelperService.sendUpdatesToSinglesWithConfirmation(
                userToken,
                multipleDetails,
                new ArrayList<>(),
                multipleObjects,
                null
        );

        verify(userIdamService).getUserDetails(userToken);
        verifyNoMoreInteractions(userIdamService);

    }

    @Test
    void sendUpdatesToSinglesWithConfirmation() {

        RepresentedTypeC representedTypeC = new RepresentedTypeC();
        representedTypeC.setNameOfRepresentative("Rep");
        submitEventList.get(0).getCaseData().setRepresentativeClaimantType(representedTypeC);

        JurCodesTypeItem jurCodesTypeItem = new JurCodesTypeItem();
        JurCodesType jurCodesType = new JurCodesType();
        jurCodesType.setJuridictionCodesList("AA");
        jurCodesTypeItem.setValue(jurCodesType);
        submitEventList.get(0).getCaseData().setJurCodesCollection(
                new ArrayList<>(Collections.singletonList(jurCodesTypeItem)));

        JudgementTypeItem judgementTypeItem = new JudgementTypeItem();
        JudgementType judgementType = new JudgementType();
        judgementType.setJudgementType("JudgementType");
        judgementType.setDateJudgmentMade("25/01/2021");
        judgementTypeItem.setValue(judgementType);
        judgementTypeItem.setId("JD");
        submitEventList.get(0).getCaseData().setJudgementCollection(
                new ArrayList<>(Collections.singletonList(judgementTypeItem)));

        multipleDetails.getCaseData().setBatchUpdateClaimantRep(MultipleUtil.generateDynamicList("Rep"));
        multipleDetails.getCaseData().setBatchUpdateJurisdiction(MultipleUtil.generateDynamicList("AA"));
        multipleDetails.getCaseData().setBatchUpdateRespondent(MultipleUtil.generateDynamicList("Andrew Smith"));
        multipleDetails.getCaseData().setBatchUpdateJudgment(MultipleUtil.generateDynamicList("JD"));
        multipleDetails.getCaseData().setBatchUpdateRespondentRep(MultipleUtil.generateDynamicList("1"));

        multipleHelperService.sendUpdatesToSinglesWithConfirmation(
                userToken,
                multipleDetails,
                new ArrayList<>(),
                multipleObjects,
                submitEventList.get(0).getCaseData()
        );

        verify(userIdamService).getUserDetails(userToken);
        verifyNoMoreInteractions(userIdamService);

    }

    @Test
    void sendPreAcceptToSinglesWithConfirmation() {

        CasePreAcceptType casePreAcceptType = new CasePreAcceptType();
        casePreAcceptType.setCaseAccepted(YES);
        casePreAcceptType.setDateAccepted("2021-02-23");
        multipleDetails.getCaseData().setPreAcceptCase(casePreAcceptType);

        multipleHelperService.sendPreAcceptToSinglesWithConfirmation(
                userToken,
                multipleDetails,
                new ArrayList<>()
        );

        verify(userIdamService).getUserDetails(userToken);
        verifyNoMoreInteractions(userIdamService);

    }

    @Test
    void sendPreAcceptRejectedToSinglesWithConfirmation() {

        CasePreAcceptType casePreAcceptType = new CasePreAcceptType();
        casePreAcceptType.setCaseAccepted(NO);
        casePreAcceptType.setDateRejected("2021-02-23");
        casePreAcceptType.setRejectReason(new ArrayList<>());
        multipleDetails.getCaseData().setPreAcceptCase(casePreAcceptType);

        multipleHelperService.sendRejectToSinglesWithConfirmation(
                userToken,
                multipleDetails,
                new ArrayList<>()
        );

        verify(userIdamService).getUserDetails(userToken);
        verifyNoMoreInteractions(userIdamService);

    }

    @Test
    void sendCloseToSinglesWithConfirmation() {
        multipleDetails.getCaseData().setClerkResponsible(new DynamicFixedListType("Clerk"));
        multipleDetails.getCaseData().setFileLocation(new DynamicFixedListType("FileLocation"));
        multipleDetails.getCaseData().setNotes("Notes");

        multipleHelperService.sendCloseToSinglesWithoutConfirmation(
                userToken,
                multipleDetails,
                new ArrayList<>()
        );

        verify(userIdamService).getUserDetails(userToken);
        verifyNoMoreInteractions(userIdamService);
    }

    @Test
    void getLeadCaseFromExcel() {

        when(excelReadingService.readExcel(anyString(), anyString(), anyList(), any(), any()))
                .thenReturn(multipleObjects);

        assertEquals("245000/2020", multipleHelperService.getLeadCaseFromExcel(
                userToken,
                multipleDetails.getCaseData(),
                new ArrayList<>()));

    }

    @Test
    void getEmptyLeadCaseFromExcel() {

        when(excelReadingService.readExcel(anyString(), anyString(), anyList(), any(), any()))
                .thenReturn(new TreeMap<>());

        assertEquals("", multipleHelperService.getLeadCaseFromExcel(
                userToken,
                multipleDetails.getCaseData(),
                new ArrayList<>()));

    }

    @Test
    void sendUpdatesToSinglesLogicCheckingLead() {

        String leadLink = "<a target=\"_blank\" "
                + "href=\"https://www-ccd.perftest.platform.hmcts.net/v2/case/1604313560561842\">245007/2020</a>";
        multipleDetails.getCaseData().setLeadCase(leadLink);
        String newLeadCase = "245000/2020";
        SubmitEvent submitEvent = new SubmitEvent();
        submitEvent.setCaseId(10_561_843);
        when(singleCasesReadingService.retrieveSingleCase(userToken, multipleDetails.getCaseTypeId(),
                newLeadCase, multipleDetails.getCaseData().getMultipleSource()))
                .thenReturn(submitEvent);

        multipleHelperService.sendUpdatesToSinglesLogic(
                userToken,
                multipleDetails,
                new ArrayList<>(),
                newLeadCase,
                multipleObjects,
                new ArrayList<>(Arrays.asList("245008/2020", "245009/2020")));

        assertEquals("<a target=\"_blank\" href=\"" + gatewayURL + "/cases/case-details/10561843\">245000/2020</a>",
                multipleDetails.getCaseData().getLeadCase());

    }

    @Test
    void sendUpdatesToSinglesLogicCheckingSameLead() {

        String leadLink = "<a target=\"_blank\" href=\"https://www-ccd.perftest.platform.hmcts.net/v2/case/1604313560561842\">245007/2020</a>";
        multipleDetails.getCaseData().setLeadCase(leadLink);
        String newLeadCase = "245007/2020";

        multipleHelperService.sendUpdatesToSinglesLogic(
                userToken,
                multipleDetails,
                new ArrayList<>(),
                newLeadCase,
                multipleObjects,
                new ArrayList<>());

        assertEquals(leadLink, multipleDetails.getCaseData().getLeadCase());

    }

}