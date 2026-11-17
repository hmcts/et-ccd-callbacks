package uk.gov.hmcts.ethos.replacement.docmosis.tasks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantIndType;
import uk.gov.hmcts.et.common.model.ccd.types.NoticeOfChangeAnswers;
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationPolicy;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.noc.NocRespondentRepresentativeService;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.EMPLOYMENT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@ExtendWith(SpringExtension.class)
class EcmMigrationNoticeOfChangeTaskTest {

    private static final String AUTH_TOKEN = "testAuthToken";
    private static final String CASE_ID = "1234567890123456";
    private static final String RESPONDENT_ID = "resp-uuid-1";
    private static final String REPRESENTATIVE_ID = "rep-uuid-1";
    private static final String FIX_CASE_API = "fixCaseAPI";

    @MockitoBean
    private AdminUserService adminUserService;
    @MockitoBean
    private CcdClient ccdClient;
    @MockitoBean
    private NocRespondentRepresentativeService nocRespondentRepresentativeService;

    @Captor
    private ArgumentCaptor<CaseData> caseDataCaptor;

    private EcmMigrationNoticeOfChangeTask task;

    @BeforeEach
    void setUp() {
        task = new EcmMigrationNoticeOfChangeTask(adminUserService, ccdClient, nocRespondentRepresentativeService);
        when(adminUserService.getAdminUserToken()).thenReturn(AUTH_TOKEN);
        ReflectionTestUtils.setField(task, "caseTypeIdsString", "ET_EnglandWales,ET_Scotland");
        ReflectionTestUtils.setField(task, "maxCases", 10);
        ReflectionTestUtils.setField(task, "ecmMigrationNocCaseIds", "");
        ReflectionTestUtils.setField(task, "dryRun", false);
        ReflectionTestUtils.setField(task, "concurrency", 20);
    }

    private CaseData createEcmCaseData() {
        CaseData caseData = new CaseData();
        caseData.setMigratedFromEcm(YES);

        ClaimantIndType claimant = new ClaimantIndType();
        claimant.setClaimantFirstNames("John");
        claimant.setClaimantLastName("Doe");
        caseData.setClaimantIndType(claimant);

        RespondentSumTypeItem respondentItem = new RespondentSumTypeItem();
        respondentItem.setId(RESPONDENT_ID);
        RespondentSumType respondentSumType = new RespondentSumType();
        respondentSumType.setRespondentName("Acme Corp");
        respondentItem.setValue(respondentSumType);
        caseData.setRespondentCollection(new ArrayList<>(List.of(respondentItem)));

        RepresentedTypeRItem repItem = new RepresentedTypeRItem();
        repItem.setId(REPRESENTATIVE_ID);
        RepresentedTypeR representedTypeR = new RepresentedTypeR();
        representedTypeR.setRespondentId(RESPONDENT_ID);
        representedTypeR.setRespRepName("Acme Corp");
        repItem.setValue(representedTypeR);
        caseData.setRepCollection(new ArrayList<>(List.of(repItem)));

        return caseData;
    }

    private CCDRequest createCcdRequest(CaseData caseData) {
        CCDRequest ccdRequest = CCDRequestBuilder.builder().withCaseData(caseData).build();
        CaseDetails caseDetails = ccdRequest.getCaseDetails();
        caseDetails.setCaseId(CASE_ID);
        caseDetails.setCaseTypeId(ENGLANDWALES_CASE_TYPE_ID);
        caseDetails.setJurisdiction(EMPLOYMENT);
        return ccdRequest;
    }

    @Test
    void run_withSearchQuery_updatesMigratedCase() throws IOException {
        SubmitEvent submitEvent = new SubmitEvent();
        submitEvent.setCaseId(Long.parseLong(CASE_ID));

        when(ccdClient.buildAndGetElasticSearchRequest(anyString(), eq(ENGLANDWALES_CASE_TYPE_ID), anyString()))
                .thenReturn(List.of(submitEvent))
                .thenReturn(Collections.emptyList());
        when(ccdClient.buildAndGetElasticSearchRequest(anyString(), eq(SCOTLAND_CASE_TYPE_ID), anyString()))
                .thenReturn(Collections.emptyList());

        CaseData caseData = createEcmCaseData();
        CCDRequest ccdRequest = createCcdRequest(caseData);

        when(ccdClient.startEventForCase(AUTH_TOKEN, ENGLANDWALES_CASE_TYPE_ID, EMPLOYMENT,
                CASE_ID, FIX_CASE_API)).thenReturn(ccdRequest);

        CaseData populatedCaseData = createEcmCaseData();
        populatedCaseData.setRespondentOrganisationPolicy0(OrganisationPolicy.builder().build());
        populatedCaseData.setClaimantRepresentativeOrganisationPolicy(OrganisationPolicy.builder().build());
        populatedCaseData.setNoticeOfChangeAnswers0(NoticeOfChangeAnswers.builder().respondentName("Acme").build());

        when(nocRespondentRepresentativeService.prepopulateOrgPolicyAndNoc(any(CaseData.class)))
                .thenReturn(populatedCaseData);

        task.run();

        verify(ccdClient, times(1)).submitEventForCase(eq(AUTH_TOKEN), caseDataCaptor.capture(),
                eq(ENGLANDWALES_CASE_TYPE_ID), eq(EMPLOYMENT), eq(ccdRequest), eq(CASE_ID));

        CaseData submitted = caseDataCaptor.getValue();
        assertThat(submitted.getRespondentOrganisationPolicy0()).isNotNull();
        assertThat(submitted.getClaimantRepresentativeOrganisationPolicy()).isNotNull();
        assertThat(submitted.getNoticeOfChangeAnswers0()).isNotNull();
        assertThat(submitted.getStateAPI()).isNull();
    }

    @Test
    void run_withConfiguredCaseIds_processesSpecificCase() throws IOException {
        ReflectionTestUtils.setField(task, "ecmMigrationNocCaseIds", CASE_ID);

        CaseData caseData = createEcmCaseData();
        CCDRequest ccdRequest = createCcdRequest(caseData);

        when(ccdClient.startEventForCase(AUTH_TOKEN, ENGLANDWALES_CASE_TYPE_ID, EMPLOYMENT,
                CASE_ID, FIX_CASE_API)).thenReturn(ccdRequest);

        CaseData populatedCaseData = createEcmCaseData();
        populatedCaseData.setRespondentOrganisationPolicy0(OrganisationPolicy.builder().build());
        populatedCaseData.setClaimantRepresentativeOrganisationPolicy(OrganisationPolicy.builder().build());
        populatedCaseData.setNoticeOfChangeAnswers0(NoticeOfChangeAnswers.builder().respondentName("Acme").build());

        when(nocRespondentRepresentativeService.prepopulateOrgPolicyAndNoc(any(CaseData.class)))
                .thenReturn(populatedCaseData);

        task.run();

        verify(ccdClient, never()).buildAndGetElasticSearchRequest(any(), any(), any());
        verify(ccdClient, times(1)).submitEventForCase(eq(AUTH_TOKEN), any(CaseData.class),
                eq(ENGLANDWALES_CASE_TYPE_ID), eq(EMPLOYMENT), eq(ccdRequest), eq(CASE_ID));
    }

    @Test
    void run_dryRunEnabled_doesNotSubmitEvent() throws IOException {
        ReflectionTestUtils.setField(task, "ecmMigrationNocCaseIds", CASE_ID);
        ReflectionTestUtils.setField(task, "dryRun", true);

        CaseData caseData = createEcmCaseData();
        CCDRequest ccdRequest = createCcdRequest(caseData);

        when(ccdClient.startEventForCase(AUTH_TOKEN, ENGLANDWALES_CASE_TYPE_ID, EMPLOYMENT,
                CASE_ID, FIX_CASE_API)).thenReturn(ccdRequest);

        CaseData populatedCaseData = createEcmCaseData();
        populatedCaseData.setRespondentOrganisationPolicy0(OrganisationPolicy.builder().build());
        when(nocRespondentRepresentativeService.prepopulateOrgPolicyAndNoc(any(CaseData.class)))
                .thenReturn(populatedCaseData);

        task.run();

        verify(nocRespondentRepresentativeService, times(1)).prepopulateOrgPolicyAndNoc(any(CaseData.class));
        verify(ccdClient, never()).submitEventForCase(any(), any(), any(), any(), any(), any());
    }

    @Test
    void run_caseAlreadyHasOrgPolicyAndNoc_skipsUpdate() throws IOException {
        ReflectionTestUtils.setField(task, "ecmMigrationNocCaseIds", CASE_ID);

        CaseData caseData = createEcmCaseData();
        caseData.setRespondentOrganisationPolicy0(OrganisationPolicy.builder().build());
        caseData.setClaimantRepresentativeOrganisationPolicy(OrganisationPolicy.builder().build());
        caseData.setNoticeOfChangeAnswers0(NoticeOfChangeAnswers.builder().respondentName("Acme").build());

        CCDRequest ccdRequest = createCcdRequest(caseData);
        when(ccdClient.startEventForCase(AUTH_TOKEN, ENGLANDWALES_CASE_TYPE_ID, EMPLOYMENT,
                CASE_ID, FIX_CASE_API)).thenReturn(ccdRequest);

        task.run();

        verify(nocRespondentRepresentativeService, never()).prepopulateOrgPolicyAndNoc(any());
        verify(ccdClient, never()).submitEventForCase(any(), any(), any(), any(), any(), any());
    }

    @Test
    void run_notMigratedFromEcm_skipsCase() throws IOException {
        ReflectionTestUtils.setField(task, "ecmMigrationNocCaseIds", CASE_ID);

        CaseData caseData = new CaseData();
        caseData.setMigratedFromEcm(null);
        caseData.setEcmCaseLink(null);

        CCDRequest ccdRequest = createCcdRequest(caseData);
        when(ccdClient.startEventForCase(AUTH_TOKEN, ENGLANDWALES_CASE_TYPE_ID, EMPLOYMENT,
                CASE_ID, FIX_CASE_API)).thenReturn(ccdRequest);

        task.run();

        verify(nocRespondentRepresentativeService, never()).prepopulateOrgPolicyAndNoc(any());
        verify(ccdClient, never()).submitEventForCase(any(), any(), any(), any(), any(), any());
    }

    @Test
    void run_ccdClientThrowsException_handlesGracefully() throws IOException {
        SubmitEvent submitEvent = new SubmitEvent();
        submitEvent.setCaseId(Long.parseLong(CASE_ID));

        when(ccdClient.buildAndGetElasticSearchRequest(anyString(), eq(ENGLANDWALES_CASE_TYPE_ID), anyString()))
                .thenThrow(new IOException("Search failure"));
        when(ccdClient.buildAndGetElasticSearchRequest(anyString(), eq(SCOTLAND_CASE_TYPE_ID), anyString()))
                .thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> task.run());
    }

    @Test
    void helperMethods_testCoverage() {
        CaseData caseData = new CaseData();
        assertFalse(EcmMigrationNoticeOfChangeTask.isMigratedFromEcm(null));
        assertFalse(EcmMigrationNoticeOfChangeTask.isMigratedFromEcm(caseData));

        caseData.setEcmCaseLink("1234567890123456");
        assertTrue(EcmMigrationNoticeOfChangeTask.isMigratedFromEcm(caseData));

        caseData.setEcmCaseLink(null);
        caseData.setMigratedFromEcm(YES);
        assertTrue(EcmMigrationNoticeOfChangeTask.isMigratedFromEcm(caseData));

        assertFalse(EcmMigrationNoticeOfChangeTask.needsOrgPolicyOrNoc(null));
        assertTrue(EcmMigrationNoticeOfChangeTask.needsOrgPolicyOrNoc(caseData));

        caseData.setRespondentOrganisationPolicy0(OrganisationPolicy.builder().build());
        assertTrue(EcmMigrationNoticeOfChangeTask.needsOrgPolicyOrNoc(caseData));

        caseData.setClaimantRepresentativeOrganisationPolicy(OrganisationPolicy.builder().build());
        assertFalse(EcmMigrationNoticeOfChangeTask.needsOrgPolicyOrNoc(caseData));

        caseData.setRespondentCollection(List.of(new RespondentSumTypeItem()));
        assertTrue(EcmMigrationNoticeOfChangeTask.needsOrgPolicyOrNoc(caseData));

        caseData.setNoticeOfChangeAnswers0(NoticeOfChangeAnswers.builder().build());
        assertTrue(EcmMigrationNoticeOfChangeTask.needsOrgPolicyOrNoc(caseData));

        caseData.setNoticeOfChangeAnswers0(NoticeOfChangeAnswers.builder().respondentName("Acme").build());
        assertFalse(EcmMigrationNoticeOfChangeTask.needsOrgPolicyOrNoc(caseData));

        caseData.setRespondentCollection(List.of(new RespondentSumTypeItem(), new RespondentSumTypeItem()));
        assertTrue(EcmMigrationNoticeOfChangeTask.needsOrgPolicyOrNoc(caseData));

        caseData.setNoticeOfChangeAnswers1(NoticeOfChangeAnswers.builder().respondentName("Beta").build());
        assertFalse(EcmMigrationNoticeOfChangeTask.needsOrgPolicyOrNoc(caseData));

        assertThat(EcmMigrationNoticeOfChangeTask.getNoticeOfChangeAnswersByIndex(caseData, 0)).isNotNull();
        assertThat(EcmMigrationNoticeOfChangeTask.getNoticeOfChangeAnswersByIndex(caseData, 1)).isNotNull();
        assertThat(EcmMigrationNoticeOfChangeTask.getNoticeOfChangeAnswersByIndex(caseData, 2)).isNull();
        assertThat(EcmMigrationNoticeOfChangeTask.getNoticeOfChangeAnswersByIndex(caseData, 10)).isNull();

        SubmitEvent nullEvent = null;
        assertThat(EcmMigrationNoticeOfChangeTask.findCaseId(nullEvent)).isEqualTo("<unknown>");
        SubmitEvent zeroEvent = new SubmitEvent();
        zeroEvent.setCaseId(0);
        assertThat(EcmMigrationNoticeOfChangeTask.findCaseId(zeroEvent)).isEqualTo("<unknown>");

        assertThat(EcmMigrationNoticeOfChangeTask.parseCaseIds("")).isEmpty();
        assertThat(EcmMigrationNoticeOfChangeTask.parseCaseIds("111, 222, 333"))
                .containsExactly("111", "222", "333");

        assertFalse(EcmMigrationNoticeOfChangeTask.isCcdRequestValid(null));
        assertFalse(EcmMigrationNoticeOfChangeTask.isCcdRequestValid(new CCDRequest()));
    }

    @Test
    void processCase_preservesPreExistingPopulatedValues() throws Exception {
        final CaseData caseData = createEcmCaseData();
        // Add a second respondent so needsOrgPolicyOrNoc is true
        RespondentSumTypeItem respondent2 = new RespondentSumTypeItem();
        respondent2.setId("resp-uuid-2");
        RespondentSumType respondentSumType2 = new RespondentSumType();
        respondentSumType2.setRespondentName("Second Corp");
        respondent2.setValue(respondentSumType2);
        caseData.getRespondentCollection().add(respondent2);

        Organisation preExistingOrg = Organisation.builder().organisationID("ORG-EXISTING").build();
        OrganisationPolicy preExistingPolicy = OrganisationPolicy.builder()
                .organisation(preExistingOrg)
                .orgPolicyCaseAssignedRole("[RESPONDENTSOLICITOR1]")
                .build();
        caseData.setRespondentOrganisationPolicy0(preExistingPolicy);

        NoticeOfChangeAnswers preExistingAnswers = NoticeOfChangeAnswers.builder()
                .respondentName("Acme Corp")
                .build();
        caseData.setNoticeOfChangeAnswers0(preExistingAnswers);

        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseId(CASE_ID);
        caseDetails.setCaseTypeId(ENGLANDWALES_CASE_TYPE_ID);
        caseDetails.setJurisdiction(EMPLOYMENT);
        caseDetails.setCaseData(caseData);

        CCDRequest ccdRequest = CCDRequestBuilder.builder()
                .withCaseData(caseData)
                .withCaseId(CASE_ID)
                .build();
        ccdRequest.setCaseDetails(caseDetails);

        when(ccdClient.startEventForCase(AUTH_TOKEN, ENGLANDWALES_CASE_TYPE_ID, EMPLOYMENT, CASE_ID, FIX_CASE_API))
                .thenReturn(ccdRequest);

        // Simulate prepopulateOrgPolicyAndNoc generating blank org on respondentOrganisationPolicy0
        CaseData prepopulatedData = createEcmCaseData();
        prepopulatedData.setRespondentOrganisationPolicy0(OrganisationPolicy.builder()
                .organisation(Organisation.builder().build())
                .orgPolicyCaseAssignedRole("[RESPONDENTSOLICITOR1]")
                .build());
        prepopulatedData.setRespondentOrganisationPolicy1(OrganisationPolicy.builder()
                .organisation(Organisation.builder().build())
                .orgPolicyCaseAssignedRole("[RESPONDENTSOLICITOR2]")
                .build());
        when(nocRespondentRepresentativeService.prepopulateOrgPolicyAndNoc(caseData))
                .thenReturn(prepopulatedData);

        boolean result = task.processCase(AUTH_TOKEN, ENGLANDWALES_CASE_TYPE_ID, CASE_ID);

        assertTrue(result);
        verify(ccdClient).submitEventForCase(eq(AUTH_TOKEN), caseDataCaptor.capture(),
                eq(ENGLANDWALES_CASE_TYPE_ID), eq(EMPLOYMENT), eq(ccdRequest), eq(CASE_ID));

        CaseData submittedData = caseDataCaptor.getValue();
        // Verify pre-existing OrganisationPolicy was preserved
        assertThat(submittedData.getRespondentOrganisationPolicy0().getOrganisation().getOrganisationID())
                .isEqualTo("ORG-EXISTING");
        // Verify pre-existing NoticeOfChangeAnswers was preserved
        assertThat(submittedData.getNoticeOfChangeAnswers0().getRespondentName())
                .isEqualTo("Acme Corp");
    }

    @Test
    void preserveExistingValues_preservesAllIndexedPoliciesAndAnswers() {
        CaseData source = new CaseData();
        CaseData target = new CaseData();

        assertDoesNotThrow(() -> EcmMigrationNoticeOfChangeTask.preserveExistingValues(null, target));
        assertDoesNotThrow(() -> EcmMigrationNoticeOfChangeTask.preserveExistingValues(source, null));

        Organisation claimantOrg = Organisation.builder().organisationID("CLAIMANT-ORG").build();
        source.setClaimantRepresentativeOrganisationPolicy(OrganisationPolicy.builder()
                .organisation(claimantOrg)
                .build());

        for (int idx = 0; idx < 10; idx++) {
            Organisation respOrg = Organisation.builder().organisationID("RESP-ORG-" + idx).build();
            OrganisationPolicy policy = OrganisationPolicy.builder().organisation(respOrg).build();
            EcmMigrationNoticeOfChangeTask.setRespondentOrganisationPolicyByIndex(source, idx, policy);

            NoticeOfChangeAnswers answer = NoticeOfChangeAnswers.builder()
                    .respondentName("Respondent " + idx)
                    .build();
            EcmMigrationNoticeOfChangeTask.setNoticeOfChangeAnswersByIndex(source, idx, answer);
        }

        EcmMigrationNoticeOfChangeTask.preserveExistingValues(source, target);

        assertThat(target.getClaimantRepresentativeOrganisationPolicy().getOrganisation().getOrganisationID())
                .isEqualTo("CLAIMANT-ORG");

        for (int idx = 0; idx < 10; idx++) {
            OrganisationPolicy targetPolicy =
                    EcmMigrationNoticeOfChangeTask.getRespondentOrganisationPolicyByIndex(target, idx);
            assertThat(targetPolicy).isNotNull();
            assertThat(targetPolicy.getOrganisation().getOrganisationID()).isEqualTo("RESP-ORG-" + idx);

            NoticeOfChangeAnswers targetAnswer =
                    EcmMigrationNoticeOfChangeTask.getNoticeOfChangeAnswersByIndex(target, idx);
            assertThat(targetAnswer).isNotNull();
            assertThat(targetAnswer.getRespondentName()).isEqualTo("Respondent " + idx);
        }

        // Test boundary conditions for getters/setters
        assertThat(EcmMigrationNoticeOfChangeTask.getRespondentOrganisationPolicyByIndex(null, 0)).isNull();
        assertThat(EcmMigrationNoticeOfChangeTask.getRespondentOrganisationPolicyByIndex(target, 11)).isNull();
        assertDoesNotThrow(() -> EcmMigrationNoticeOfChangeTask.setRespondentOrganisationPolicyByIndex(null, 0, null));
        assertDoesNotThrow(() ->
                EcmMigrationNoticeOfChangeTask.setRespondentOrganisationPolicyByIndex(target, 11, null));
        assertDoesNotThrow(() -> EcmMigrationNoticeOfChangeTask.setNoticeOfChangeAnswersByIndex(null, 0, null));
        assertDoesNotThrow(() -> EcmMigrationNoticeOfChangeTask.setNoticeOfChangeAnswersByIndex(target, 11, null));
    }
}
