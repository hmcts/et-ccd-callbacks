package uk.gov.hmcts.ethos.replacement.docmosis.tasks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.ecm.common.model.ccd.types.CaseNote;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.EMPLOYMENT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;

@ExtendWith(MockitoExtension.class)
class EcmMigrationCaseNotesTaskTest {

    private static final String ADMIN_TOKEN = "admin-token";
    private static final String S2S_TOKEN = "s2s-token";
    private static final String ECM_CASE_ID = "1620473176786991";
    private static final String REFORM_CASE_ID = "1620473176786992";

    @Mock
    private AdminUserService adminUserService;
    @Mock
    private CcdClient ccdClient;
    @Mock
    private CoreCaseDataApi coreCaseDataApi;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Captor
    private ArgumentCaptor<CaseData> caseDataCaptor;

    private EcmMigrationCaseNotesTask task;

    @BeforeEach
    void setUp() {
        task = new EcmMigrationCaseNotesTask(adminUserService, ccdClient, coreCaseDataApi, authTokenGenerator);
        ReflectionTestUtils.setField(task, "ecmCaseIds", ECM_CASE_ID);
        ReflectionTestUtils.setField(task, "dryRun", false);
        lenient().when(adminUserService.getAdminUserToken()).thenReturn(ADMIN_TOKEN);
        lenient().when(authTokenGenerator.generate()).thenReturn(S2S_TOKEN);
    }

    @Test
    void run_prependsEcmNotesToExistingReformNotes() throws IOException {
        mockEcmCaseType(ECM_CASE_ID, "Newcastle");
        when(ccdClient.startEventForEcmCase(ADMIN_TOKEN, "Newcastle", EMPLOYMENT, ECM_CASE_ID,
                "fixCaseAPI")).thenReturn(ecmRequest(ecmNote()));
        when(ccdClient.startEventForCase(ADMIN_TOKEN, ENGLANDWALES_CASE_TYPE_ID, EMPLOYMENT, REFORM_CASE_ID,
                "fixCaseAPI")).thenReturn(reformRequest(reformNote()));

        task.run();

        verify(ccdClient).submitEventForCase(eq(ADMIN_TOKEN), caseDataCaptor.capture(),
                eq(ENGLANDWALES_CASE_TYPE_ID), eq(EMPLOYMENT), any(CCDRequest.class), eq(REFORM_CASE_ID));
        List<uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem<uk.gov.hmcts.et.common.model.ccd.types.CaseNote>>
                notes = caseDataCaptor.getValue().getCaseNotesCollection();
        assertEquals(List.of("ecm-note", "reform-note"), notes.stream().map(
                uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem::getId).toList());
        assertEquals("ECM note", notes.getFirst().getValue().getNote());
    }

    @Test
    void run_doesNotSubmitWhenMergedNotesAlreadyMatchReformNotes() throws IOException {
        mockEcmCaseType(ECM_CASE_ID, "Newcastle");
        when(ccdClient.startEventForEcmCase(any(), any(), any(), any(), any()))
                .thenReturn(ecmRequest(ecmNote()));
        when(ccdClient.startEventForCase(any(), any(), any(), any(), any()))
                .thenReturn(reformRequest(reformNote("ecm-note", "ECM note", "ECM user", "1 Jan 2025 12:00")));

        task.run();

        verify(ccdClient, never()).submitEventForCase(any(), any(), any(), any(), any(), any());
    }

    @Test
    void run_doesNotSubmitInDryRunMode() throws IOException {
        ReflectionTestUtils.setField(task, "dryRun", true);
        mockEcmCaseType(ECM_CASE_ID, "Wales");
        when(ccdClient.startEventForEcmCase(any(), any(), any(), any(), any()))
                .thenReturn(ecmRequest(ecmNote()));
        when(ccdClient.startEventForCase(any(), any(), any(), any(), any())).thenReturn(reformRequest());

        task.run();

        verify(ccdClient, never()).submitEventForCase(any(), any(), any(), any(), any(), any());
    }

    @Test
    void run_usesEachSupportedEcmCaseType() throws IOException {
        String midlandsEastCaseId = "1620473176786993";
        String walesCaseId = "1620473176786994";
        ReflectionTestUtils.setField(task, "ecmCaseIds",
                String.join(",", ECM_CASE_ID, midlandsEastCaseId, walesCaseId));
        mockEcmCaseType(ECM_CASE_ID, "Newcastle");
        mockEcmCaseType(midlandsEastCaseId, "MidlandsEast");
        mockEcmCaseType(walesCaseId, "Wales");
        when(ccdClient.startEventForEcmCase(any(), any(), any(), any(), any()))
                .thenReturn(ecmRequest(ecmNote()));
        when(ccdClient.startEventForCase(any(), any(), any(), any(), any())).thenReturn(reformRequest());

        task.run();

        verify(ccdClient).startEventForEcmCase(ADMIN_TOKEN, "Newcastle", EMPLOYMENT, ECM_CASE_ID,
                "fixCaseAPI");
        verify(ccdClient).startEventForEcmCase(ADMIN_TOKEN, "MidlandsEast", EMPLOYMENT, midlandsEastCaseId,
                "fixCaseAPI");
        verify(ccdClient).startEventForEcmCase(ADMIN_TOKEN, "Wales", EMPLOYMENT, walesCaseId,
                "fixCaseAPI");
    }

    @Test
    void run_skipsInvalidEcmCaseIds() throws IOException {
        ReflectionTestUtils.setField(task, "ecmCaseIds", "invalid-case-id");

        task.run();

        verify(coreCaseDataApi, never()).getCase(any(), any(), any());
        verify(ccdClient, never()).startEventForEcmCase(any(), any(), any(), any(), any());
    }

    @Test
    void run_continuesAfterAnIndividualCaseFails() throws IOException {
        String successfulCaseId = "1620473176786993";
        ReflectionTestUtils.setField(task, "ecmCaseIds", ECM_CASE_ID + "," + successfulCaseId);
        mockEcmCaseType(ECM_CASE_ID, "Newcastle");
        mockEcmCaseType(successfulCaseId, "Wales");
        when(ccdClient.startEventForEcmCase(ADMIN_TOKEN, "Newcastle", EMPLOYMENT, ECM_CASE_ID,
                "fixCaseAPI")).thenThrow(new IOException("CCD unavailable"));
        when(ccdClient.startEventForEcmCase(ADMIN_TOKEN, "Wales", EMPLOYMENT, successfulCaseId,
                "fixCaseAPI")).thenReturn(ecmRequest(ecmNote()));
        when(ccdClient.startEventForCase(ADMIN_TOKEN, ENGLANDWALES_CASE_TYPE_ID, EMPLOYMENT, REFORM_CASE_ID,
                "fixCaseAPI")).thenReturn(reformRequest());

        task.run();

        verify(ccdClient, times(1)).submitEventForCase(eq(ADMIN_TOKEN), any(CaseData.class),
                eq(ENGLANDWALES_CASE_TYPE_ID), eq(EMPLOYMENT), any(), eq(REFORM_CASE_ID));
    }

    @Test
    void parseCaseIds_trimsAndIgnoresBlankValues() {
        assertEquals(List.of(ECM_CASE_ID, REFORM_CASE_ID),
                EcmMigrationCaseNotesTask.parseCaseIds(" " + ECM_CASE_ID + ",, " + REFORM_CASE_ID + " "));
        assertTrue(EcmMigrationCaseNotesTask.parseCaseIds(" , ").isEmpty());
    }

    private void mockEcmCaseType(String caseId, String caseTypeId) {
        uk.gov.hmcts.reform.ccd.client.model.CaseDetails ecmCaseDetails =
                uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().caseTypeId(caseTypeId).build();
        when(coreCaseDataApi.getCase(ADMIN_TOKEN, S2S_TOKEN, caseId)).thenReturn(ecmCaseDetails);
    }

    @SafeVarargs
    private static uk.gov.hmcts.ecm.common.model.ccd.CCDRequest ecmRequest(
            GenericTypeItem<CaseNote>... notes) {
        uk.gov.hmcts.ecm.common.model.ccd.CaseData caseData = new uk.gov.hmcts.ecm.common.model.ccd.CaseData();
        caseData.setReformCaseLink(reformCaseLink());
        caseData.setCaseNotesCollection(List.of(notes));
        uk.gov.hmcts.ecm.common.model.ccd.CaseDetails caseDetails = new uk.gov.hmcts.ecm.common.model.ccd.CaseDetails();
        caseDetails.setCaseData(caseData);
        uk.gov.hmcts.ecm.common.model.ccd.CCDRequest request = new uk.gov.hmcts.ecm.common.model.ccd.CCDRequest();
        request.setCaseDetails(caseDetails);
        return request;
    }

    @SafeVarargs
    private static CCDRequest reformRequest(
            uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem<uk.gov.hmcts.et.common.model.ccd.types.CaseNote>...
                notes) {
        CaseData caseData = new CaseData();
        caseData.setCaseNotesCollection(List.of(notes));
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseId(REFORM_CASE_ID);
        caseDetails.setCaseTypeId(ENGLANDWALES_CASE_TYPE_ID);
        caseDetails.setJurisdiction(EMPLOYMENT);
        caseDetails.setCaseData(caseData);
        CCDRequest request = new CCDRequest();
        request.setCaseDetails(caseDetails);
        return request;
    }

    private static GenericTypeItem<CaseNote> ecmNote() {
        return GenericTypeItem.from("ecm-note", CaseNote.builder().title("Telephone note").note("ECM note")
                .author("ECM user").date("1 Jan 2025 12:00").build());
    }

    private static
        uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem<uk.gov.hmcts.et.common.model.ccd.types.CaseNote>
            reformNote() {
        return reformNote("reform-note", "Reform note", "Reform user", "2 Jan 2025 12:00");
    }

    private static
        uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem<uk.gov.hmcts.et.common.model.ccd.types.CaseNote>
            reformNote(String id, String note, String author, String date) {
        return uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem.from(id,
                uk.gov.hmcts.et.common.model.ccd.types.CaseNote.builder().title("Telephone note").note(note)
                        .author(author).date(date).build());
    }

    private static String reformCaseLink() {
        return "<a target=\"_blank\" href=\"https://manage-case.platform.hmcts.net/cases/case-details/"
                + REFORM_CASE_ID + "\">2503178/2018</a>";
    }
}
