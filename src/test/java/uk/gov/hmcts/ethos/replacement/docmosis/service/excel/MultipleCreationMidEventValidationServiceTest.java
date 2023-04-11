package uk.gov.hmcts.ethos.replacement.docmosis.service.excel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.items.CaseIdTypeItem;
import uk.gov.hmcts.et.common.model.bulk.types.CaseType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultipleUtil;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultiplesHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ACCEPTED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ET1_ONLINE_CASE_SOURCE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MANUALLY_CREATED_POSITION;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MIGRATION_CASE_SOURCE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_BULK_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SUBMITTED_STATE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.excel.MultipleCreationMidEventValidationService.CASE_BELONGS_DIFFERENT_OFFICE;

@RunWith(SpringJUnit4ClassRunner.class)
@ExtendWith(SpringExtension.class)
class MultipleCreationMidEventValidationServiceTest {

    @Mock
    private SingleCasesReadingService singleCasesReadingService;
    @InjectMocks
    private MultipleCreationMidEventValidationService multipleCreationMidEventValidationService;

    private MultipleDetails multipleDetails;
    private List<String> errors;
    private String userToken;

    @BeforeEach
    void setUp() {
        multipleDetails = new MultipleDetails();
        multipleDetails.setCaseData(MultipleUtil.getMultipleData());
        errors = new ArrayList<>();
        userToken = "authString";
    }

    @Test
    void multipleCreationValidationLogicCaseDoesNotExist() {

        CaseData caseData = new CaseData();
        caseData.setEthosCaseReference("245004/2020");

        SubmitEvent submitEvent = new SubmitEvent();
        submitEvent.setCaseData(caseData);
        submitEvent.setState(ACCEPTED_STATE);
        submitEvent.setCaseId(1_232_121_232);

        multipleDetails.getCaseData().setLeadCase(null);

        when(singleCasesReadingService.retrieveSingleCases(userToken,
                multipleDetails.getCaseTypeId(),
                MultiplesHelper.getCaseIds(multipleDetails.getCaseData()),
                multipleDetails.getCaseData().getMultipleSource()))
                .thenReturn(new ArrayList<>(Collections.singletonList(submitEvent)));

        multipleCreationMidEventValidationService.multipleCreationValidationLogic(
                userToken,
                multipleDetails,
                errors,
                false);

        assertEquals(1, errors.size());
        assertEquals("[245000/2020, 245001/2020] cases do not exist.", errors.get(0));

    }

    @Test
    void multipleCreationValidationLogicMaxSizeAndLeadCaseDoesNotExist() {

        createCaseIdCollection(multipleDetails.getCaseData(), 60);

        multipleCreationMidEventValidationService.multipleCreationValidationLogic(
                userToken,
                multipleDetails,
                errors,
                false);

        assertEquals(3, errors.size());
        assertEquals("[21006/2020] lead case does not exist.", errors.get(0));
        assertEquals("There are 60 cases in the multiple. The limit is 50.", errors.get(1));
        assertEquals("[0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24,"
                + " 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, "
                + "48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59] cases do not exist.", errors.get(2));

    }

    @Test
    void multipleCreationValidationLogicWrongStateAndMultipleErrorEmptyLead() {

        multipleDetails.getCaseData().setLeadCase(null);

        when(singleCasesReadingService.retrieveSingleCases(userToken,
                multipleDetails.getCaseTypeId(),
                MultiplesHelper.getCaseIds(multipleDetails.getCaseData()),
                MANUALLY_CREATED_POSITION))
                .thenReturn(getSubmitEvents());

        multipleCreationMidEventValidationService.multipleCreationValidationLogic(
                userToken,
                multipleDetails,
                errors,
                false);

        assertEquals(3, errors.size());
        assertEquals("Case 245001/2020 is managed by Bristol", errors.get(0));
        assertEquals("[245000/2020, 245001/2020] cases have not been Accepted.", errors.get(1));
        assertEquals("[245000/2020] cases already belong to a different multiple", errors.get(2));

    }

    @Test
    void multipleCreationValidationLogicET1OnlineCase() {

        multipleDetails.getCaseData().setMultipleSource(ET1_ONLINE_CASE_SOURCE);

        multipleCreationMidEventValidationService.multipleCreationValidationLogic(
                userToken,
                multipleDetails,
                errors,
                false);

        assertEquals(0, errors.size());

    }

    @Test
    void multipleCreationValidationLogicMigrationCase() {

        multipleDetails.getCaseData().setMultipleSource(MIGRATION_CASE_SOURCE);

        multipleCreationMidEventValidationService.multipleCreationValidationLogic(
                userToken,
                multipleDetails,
                errors,
                false);

        assertEquals(0, errors.size());

    }

    @Test
    void multipleAmendCaseIdsValidationLogicCaseDoesNotExist() {

        CaseData caseData = new CaseData();
        caseData.setEthosCaseReference("245004/2020");

        SubmitEvent submitEvent = new SubmitEvent();
        submitEvent.setCaseData(caseData);
        submitEvent.setState(ACCEPTED_STATE);
        submitEvent.setCaseId(1_232_121_232);

        multipleDetails.getCaseData().setLeadCase(null);

        when(singleCasesReadingService.retrieveSingleCases(userToken,
                multipleDetails.getCaseTypeId(),
                MultiplesHelper.getCaseIds(multipleDetails.getCaseData()),
                multipleDetails.getCaseData().getMultipleSource()))
                .thenReturn(new ArrayList<>(Collections.singletonList(submitEvent)));

        multipleCreationMidEventValidationService.multipleCreationValidationLogic(
                userToken,
                multipleDetails,
                errors,
                true);

        assertEquals(1, errors.size());
        assertEquals("[245000/2020, 245001/2020] cases do not exist.", errors.get(0));

    }

    @Test
    void checkIfSingleIsInDifferentOffice_EnglandWales() {
        multipleDetails.getCaseData().setManagingOffice(TribunalOffice.BRISTOL.getOfficeName());
        when(singleCasesReadingService.retrieveSingleCases(userToken,
                multipleDetails.getCaseTypeId(),
                MultiplesHelper.getCaseIds(multipleDetails.getCaseData()),
                MANUALLY_CREATED_POSITION))
                .thenReturn(getSubmitEvents());

        multipleCreationMidEventValidationService.multipleCreationValidationLogic(
                userToken,
                multipleDetails,
                errors,
                true);

        assertTrue(errors.contains(String.format(CASE_BELONGS_DIFFERENT_OFFICE,
                "245000/2020", TribunalOffice.MANCHESTER.getOfficeName())));
        assertFalse(errors.contains(String.format(CASE_BELONGS_DIFFERENT_OFFICE,
                "245001/2020", TribunalOffice.BRISTOL.getOfficeName())));
    }

    @Test
    void checkIfSingleIsInDifferentOffice_Scotland() {
        multipleDetails.setCaseTypeId(SCOTLAND_BULK_CASE_TYPE_ID);
        List<SubmitEvent> submitEvents = getSubmitEvents();
        submitEvents.get(0).getCaseData().setManagingOffice(TribunalOffice.ABERDEEN.getOfficeName());
        submitEvents.get(1).getCaseData().setManagingOffice(TribunalOffice.DUNDEE.getOfficeName());
        when(singleCasesReadingService.retrieveSingleCases(userToken,
                multipleDetails.getCaseTypeId(),
                MultiplesHelper.getCaseIds(multipleDetails.getCaseData()),
                MANUALLY_CREATED_POSITION))
                .thenReturn(submitEvents);

        multipleCreationMidEventValidationService.multipleCreationValidationLogic(
                userToken,
                multipleDetails,
                errors,
                true);

        assertFalse(errors.contains(String.format(CASE_BELONGS_DIFFERENT_OFFICE,
                "245000/2020", TribunalOffice.ABERDEEN.getOfficeName())));
        assertFalse(errors.contains(String.format(CASE_BELONGS_DIFFERENT_OFFICE,
                "245001/2020", TribunalOffice.DUNDEE.getOfficeName())));
    }

    private void createCaseIdCollection(MultipleData multipleData, int numberCases) {

        List<CaseIdTypeItem> caseIdCollection = new ArrayList<>();

        for (int i = 0; i < numberCases; i++) {

            caseIdCollection.add(createCaseIdType(String.valueOf(i)));

        }

        multipleData.setCaseIdCollection(caseIdCollection);

    }

    private CaseIdTypeItem createCaseIdType(String ethosCaseReference) {

        CaseType caseType = new CaseType();
        caseType.setEthosCaseReference(ethosCaseReference);

        CaseIdTypeItem caseIdTypeItem = new CaseIdTypeItem();
        caseIdTypeItem.setId(ethosCaseReference);
        caseIdTypeItem.setValue(caseType);

        return caseIdTypeItem;

    }

    private static List<SubmitEvent> getSubmitEvents() {

        CaseData caseData = new CaseData();
        caseData.setEthosCaseReference("245000/2020");
        caseData.setMultipleReference("245000");
        caseData.setManagingOffice(TribunalOffice.MANCHESTER.getOfficeName());

        SubmitEvent submitEvent = new SubmitEvent();
        submitEvent.setCaseData(caseData);
        submitEvent.setState(SUBMITTED_STATE);
        submitEvent.setCaseId(1_232_121_232);

        CaseData caseData1 = new CaseData();
        caseData1.setEthosCaseReference("245001/2020");
        caseData1.setManagingOffice(TribunalOffice.BRISTOL.getOfficeName());

        SubmitEvent submitEvent1 = new SubmitEvent();
        submitEvent1.setCaseData(caseData1);
        submitEvent1.setState(SUBMITTED_STATE);
        submitEvent1.setCaseId(1_232_121_233);

        return new ArrayList<>(Arrays.asList(submitEvent, submitEvent1));

    }
}