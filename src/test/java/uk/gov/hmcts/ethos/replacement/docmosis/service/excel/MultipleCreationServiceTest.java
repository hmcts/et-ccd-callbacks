package uk.gov.hmcts.ethos.replacement.docmosis.service.excel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.model.servicebus.datamodel.LegalRepDataModel;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationUsersIdamUser;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationUsersResponse;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultipleUtil;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultiplesHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.rdprofessional.OrganisationClient;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementLocationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.multiples.MultipleReferenceService;
import uk.gov.hmcts.ethos.replacement.docmosis.servicebus.CreateUpdatesBusSender;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_BULK_CASE_TYPE_ID;
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
    @Mock
    private AdminUserService adminUserService;
    @Mock
    private CcdClient ccdClient;
    @Mock
    private OrganisationClient organisationClient;
    @Mock
    private CreateUpdatesBusSender createUpdatesBusSender;
    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private MultipleCreationService multipleCreationService;

    private MultipleDetails multipleDetails;
    private List<String> ethosCaseRefCollection;
    private String userToken;

    @BeforeEach
    public void setUp() {
        multipleDetails = new MultipleDetails();
        multipleDetails.setCaseTypeId(ENGLANDWALES_BULK_CASE_TYPE_ID);
        multipleDetails.setCaseData(MultipleUtil.getMultipleData());
        multipleDetails.getCaseData().setMultipleName("Multiple Test");
        ethosCaseRefCollection = MultiplesHelper.getCaseIds(multipleDetails.getCaseData());

        //Adding lead to the case id collection
        ethosCaseRefCollection.addFirst("21006/2020");
        userToken = "authString";
        when(featureToggleService.isMultiplesEnabled()).thenReturn(true);
    }

    @Test
    void bulkCreationLogic() throws IOException {
        multipleCreationService.bulkCreationLogic(userToken,
                multipleDetails,
                new ArrayList<>());
        verify(excelDocManagementService, times(1)).generateAndUploadExcel(ethosCaseRefCollection,
                userToken,
                multipleDetails);
        verifyNoMoreInteractions(excelDocManagementService);
    }

    @Test
    void bulkCreationLogicWithMultipleReference() throws IOException {
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
    void bulkCreationLogicETOnline() throws IOException {
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
    void bulkCreationLogicMigration() throws IOException {
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
    void bulkCreationLogicMigrationEmptyCaseMultipleCollection() throws IOException {
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
    void bulkCreationLogicEmptyCaseIdCollection() throws IOException {
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
    void bulkCreationLogicWithNullMultipleRef() throws IOException {
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

    @Test
    void addLegalRepsFromSinglesCases_success() throws IOException {
        when(featureToggleService.isMul2Enabled()).thenReturn(true);

        SubmitEvent event = new SubmitEvent();
        event.setCaseId(1_718_968_200);
        event.setCaseData(new CaseData());
        event.getCaseData().setEthosCaseReference("6000001/2024");

        var repItem = new RepresentedTypeRItem();
        var rep = new RepresentedTypeR();
        repItem.setValue(rep);
        rep.setRespondentOrganisation(Organisation.builder().organisationID("ABC123").build());
        rep.setRepresentativeEmailAddress("rep1@email.com");

        var rep2Item = new RepresentedTypeRItem();
        var rep2 = new RepresentedTypeR();
        rep2Item.setValue(rep2);
        rep2.setRespondentOrganisation(Organisation.builder().organisationID("ABC123").build());
        rep2.setRepresentativeEmailAddress("rep2@email.com");
        event.getCaseData().setRepCollection(List.of(repItem, rep2Item));

        var user = OrganisationUsersIdamUser.builder()
                .userIdentifier("1ced8d89-ae94-41ff-ad6f-7936e2123955")
                .email("rep1@email.com")
                .build();
        var user2 = OrganisationUsersIdamUser.builder()
                .userIdentifier("2ced8d89-ae94-41ff-ad6f-7936e2123955")
                .email("rep2@email.com")
                .build();

        OrganisationUsersResponse usersResponse = OrganisationUsersResponse.builder()
                .users(List.of(user, user2))
                .build();

        when(ccdClient.retrieveCasesElasticSearch(any(), any(), any())).thenReturn(List.of(event));
        when(organisationClient.getOrganisationUsers(any(), any(), any())).thenReturn(ResponseEntity.ok(usersResponse));
        multipleDetails.getCaseData().setMultipleSource(ET1_ONLINE_CASE_SOURCE);
        multipleCreationService.bulkCreationLogic(userToken,
                multipleDetails,
                new ArrayList<>());
        ArgumentCaptor<LegalRepDataModel> legalRepCaptor = ArgumentCaptor.forClass(LegalRepDataModel.class);
        verify(createUpdatesBusSender, times(1)).sendUpdatesToQueue(any(), legalRepCaptor.capture(), any(), any());

        var dataModel = legalRepCaptor.getValue();
        assertEquals(ENGLANDWALES_BULK_CASE_TYPE_ID, dataModel.getCaseType());
        assertEquals("Multiple Test", dataModel.getMultipleName());
        assertEquals(1, dataModel.getLegalRepIdsByCase().size());
        assertEquals(user.getUserIdentifier(), dataModel.getLegalRepIdsByCase().get("1718968200").get(0));
        assertEquals(user2.getUserIdentifier(), dataModel.getLegalRepIdsByCase().get("1718968200").get(1));

        verify(excelDocManagementService, times(1)).writeAndUploadExcelDocument(ethosCaseRefCollection,
                userToken,
                multipleDetails,
                new ArrayList<>());
        verifyNoMoreInteractions(excelDocManagementService);
    }

    @Test
    void addLegalRepsFromSinglesCases_noLead() throws IOException {
        when(featureToggleService.isMul2Enabled()).thenReturn(true);

        multipleDetails.getCaseData().setLeadCase(null);
        multipleDetails.getCaseData().setLeadCaseId(null);
        multipleDetails.getCaseData().setCaseIdCollection(new ArrayList<>());

        when(ccdClient.retrieveCasesElasticSearch(any(), any(), any())).thenReturn(List.of());

        multipleDetails.getCaseData().setMultipleSource(ET1_ONLINE_CASE_SOURCE);
        multipleCreationService.bulkCreationLogic(userToken,
                multipleDetails,
                new ArrayList<>());

        verify(excelDocManagementService, times(1)).writeAndUploadExcelDocument(List.of(),
                userToken,
                multipleDetails,
                new ArrayList<>());
        verifyNoMoreInteractions(excelDocManagementService);
    }

    @Test
    void addLegalRepsFromSinglesCases_noRep() throws IOException {
        when(featureToggleService.isMul2Enabled()).thenReturn(true);

        SubmitEvent event = new SubmitEvent();
        event.setCaseId(1_718_968_200);
        event.setCaseData(new CaseData());
        event.getCaseData().setEthosCaseReference("6000001/2024");
        event.getCaseData().setRepCollection(null);

        when(ccdClient.retrieveCasesElasticSearch(any(), any(), any())).thenReturn(List.of(event));
        multipleDetails.getCaseData().setMultipleSource(ET1_ONLINE_CASE_SOURCE);
        multipleCreationService.bulkCreationLogic(userToken,
                multipleDetails,
                new ArrayList<>());

        verify(excelDocManagementService, times(1)).writeAndUploadExcelDocument(ethosCaseRefCollection,
                userToken,
                multipleDetails,
                new ArrayList<>());
        verifyNoMoreInteractions(excelDocManagementService);
        verifyNoMoreInteractions(createUpdatesBusSender);
    }
}