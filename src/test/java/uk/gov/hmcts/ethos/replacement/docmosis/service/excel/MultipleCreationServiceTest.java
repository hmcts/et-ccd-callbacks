package uk.gov.hmcts.ethos.replacement.docmosis.service.excel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultipleUtil;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultiplesHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementLocationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.multiples.MultipleReferenceService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ET1_ONLINE_CASE_SOURCE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MIGRATION_CASE_SOURCE;

@ExtendWith(SpringExtension.class)
class MultipleCreationServiceTest {

    @Mock
    private ExcelDocManagementService excelDocManagementService;
    @Mock
    private MultipleReferenceService multipleReferenceService;
    @Mock
    private MultipleHelperService multipleHelperService;
    @Mock
    private SubMultipleUpdateService subMultipleUpdateService;
    @Mock
    private MultipleTransferService multipleTransferService;
    @Mock
    private CaseManagementLocationService caseManagementLocationService;
    @Mock
    FeatureToggleService featureToggleService;

    @InjectMocks
    private MultipleCreationService multipleCreationService;

    private MultipleDetails multipleDetails;
    private List<String> ethosCaseRefCollection;
    private String userToken;

    @BeforeEach
    public void setUp() {
        multipleDetails = new MultipleDetails();
        multipleDetails.setCaseData(MultipleUtil.getMultipleData());
        ethosCaseRefCollection = MultiplesHelper.getCaseIds(multipleDetails.getCaseData());

        //Adding lead to the case id collection
        ethosCaseRefCollection.add(0, "21006/2020");
        userToken = "authString";
        when(featureToggleService.isMultiplesEnabled()).thenReturn(true);
    }

    @Test
    void bulkCreationLogic() {
        multipleCreationService.bulkCreationLogic(userToken,
                multipleDetails,
                new ArrayList<>());
        verify(excelDocManagementService, times(1)).generateAndUploadExcel(ethosCaseRefCollection,
                userToken,
                multipleDetails);
        verifyNoMoreInteractions(excelDocManagementService);
    }

    @Test
    void bulkCreationLogicWithMultipleReference() {
        multipleDetails.getCaseData().setMultipleReference("2100001");
        multipleCreationService.bulkCreationLogic(userToken,
                multipleDetails,
                new ArrayList<>());
        verify(excelDocManagementService, times(1)).generateAndUploadExcel(ethosCaseRefCollection,
                userToken,
                multipleDetails);
        verifyNoMoreInteractions(excelDocManagementService);
    }

    @Test
    void bulkCreationLogicETOnline() {
        multipleDetails.getCaseData().setMultipleSource(ET1_ONLINE_CASE_SOURCE);
        multipleCreationService.bulkCreationLogic(userToken,
                multipleDetails,
                new ArrayList<>());
        verify(excelDocManagementService, times(1)).writeAndUploadExcelDocument(ethosCaseRefCollection,
                userToken,
                multipleDetails,
                new ArrayList<>());
        verifyNoMoreInteractions(excelDocManagementService);
    }

    @Test
    void bulkCreationLogicMigration() {
        multipleDetails.getCaseData().setLeadCase("");
        multipleDetails.getCaseData().setCaseIdCollection(new ArrayList<>());
        multipleDetails.getCaseData().setMultipleSource(MIGRATION_CASE_SOURCE);
        multipleDetails.getCaseData().setCaseMultipleCollection(MultipleUtil.getCaseMultipleCollection());
        multipleCreationService.bulkCreationLogic(userToken,
                multipleDetails,
                new ArrayList<>());
        verify(excelDocManagementService, times(1)).writeAndUploadExcelDocument(
                MultipleUtil.getCaseMultipleObjectCollection(),
                userToken,
                multipleDetails,
                new ArrayList<>(Arrays.asList("Sub3", "Sub2", "Sub1")));
        verifyNoMoreInteractions(excelDocManagementService);
    }

    @Test
    void bulkCreationLogicMigrationEmptyCaseMultipleCollection() {
        multipleDetails.getCaseData().setLeadCase("");
        multipleDetails.getCaseData().setCaseIdCollection(new ArrayList<>());
        multipleDetails.getCaseData().setMultipleSource(MIGRATION_CASE_SOURCE);
        multipleDetails.getCaseData().setCaseMultipleCollection(new ArrayList<>());
        multipleCreationService.bulkCreationLogic(userToken,
                multipleDetails,
                new ArrayList<>());
        verify(excelDocManagementService, times(1)).writeAndUploadExcelDocument(
                new ArrayList<>(),
                userToken,
                multipleDetails,
                new ArrayList<>());
        verifyNoMoreInteractions(excelDocManagementService);
    }

    @Test
    void bulkCreationLogicEmptyCaseIdCollection() {
        multipleDetails.getCaseData().setCaseIdCollection(new ArrayList<>());
        multipleDetails.getCaseData().setLeadCase(null);
        multipleCreationService.bulkCreationLogic(userToken,
                multipleDetails,
                new ArrayList<>());
        verify(excelDocManagementService, times(1)).generateAndUploadExcel(new ArrayList<>(),
                userToken,
                multipleDetails);
        verifyNoMoreInteractions(excelDocManagementService);
    }

    @Test
    void bulkCreationLogicWithNullMultipleRef() {
        multipleDetails.getCaseData().setMultipleReference(null);
        multipleCreationService.bulkCreationLogic(userToken,
                multipleDetails,
                new ArrayList<>());
        verify(excelDocManagementService, times(1)).generateAndUploadExcel(ethosCaseRefCollection,
                userToken,
                multipleDetails);
        verifyNoMoreInteractions(excelDocManagementService);
        verify(multipleReferenceService, times(1)).createReference(multipleDetails.getCaseTypeId());
        verifyNoMoreInteractions(multipleReferenceService);
    }

}