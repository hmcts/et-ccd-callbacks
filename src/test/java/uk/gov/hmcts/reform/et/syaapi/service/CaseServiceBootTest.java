package uk.gov.hmcts.reform.et.syaapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.ecm.common.service.PostcodeToOfficeService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants;
import uk.gov.hmcts.reform.et.syaapi.helper.JurisdictionCodesMapper;
import uk.gov.hmcts.reform.et.syaapi.model.CaseTestData;
import uk.gov.hmcts.reform.et.syaapi.service.pdf.PdfUploadService;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.USER_ID;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CaseService.class, properties = {
    "spring.flyway.enabled=false",
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration,"
        + "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration"
})
class CaseServiceBootTest {
    @Autowired
    CaseService caseService;
    @MockitoBean
    private AuthTokenGenerator authTokenGenerator;
    @MockitoBean
    private IdamClient idamClient;
    @MockitoBean
    private IdamApi idamApi;
    @MockitoBean
    private CoreCaseDataApi ccdApiClient;
    @MockitoBean
    private CaseDocumentService caseDocumentService;
    @MockitoBean
    private NotificationService notificationService;
    @MockitoBean
    private AcasService acasService;
    @MockitoBean
    BundlesService bundlesService;
    @MockitoBean
    private PostcodeToOfficeService postcodeToOfficeService;
    @MockitoBean
    private PdfUploadService pdfUploadService;
    @MockitoBean
    private JurisdictionCodesMapper jurisdictionCodesMapper;
    @MockitoBean
    private CaseOfficeService caseOfficeService;
    @MockitoBean
    private FeatureToggleService featureToggleService;
    @MockitoBean
    private VerifyTokenService verifyTokenService;
    private CaseTestData caseTestData;

    @BeforeEach
    void beforeEach() throws CaseDocumentException {
        caseTestData = new CaseTestData();

        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(idamApi.retrieveUserInfo(TEST_SERVICE_AUTH_TOKEN)).thenReturn(new UserInfo(
            null,
            USER_ID,
            "name",
            caseTestData.getCaseData().getClaimantIndType().getClaimantFirstNames(),
            caseTestData.getCaseData().getClaimantIndType().getClaimantLastName(),
            null
        ));
        when(idamClient.getUserInfo(TEST_SERVICE_AUTH_TOKEN)).thenReturn(new UserInfo(
            null,
            USER_ID,
            "name",
            caseTestData.getCaseData().getClaimantIndType().getClaimantFirstNames(),
            caseTestData.getCaseData().getClaimantIndType().getClaimantLastName(),
            null
        ));
        when(ccdApiClient.startEventForCitizen(
            eq(TEST_SERVICE_AUTH_TOKEN),
            eq(TEST_SERVICE_AUTH_TOKEN),
            eq(USER_ID),
            eq(EtSyaConstants.JURISDICTION_ID),
            eq(caseTestData.getCaseRequest().getCaseTypeId()),
            eq(caseTestData.getCaseRequest().getCaseId()),
            any()
        )).thenReturn(
            caseTestData.getStartEventResponse());
        when(ccdApiClient.submitEventForCitizen(
            eq(TEST_SERVICE_AUTH_TOKEN),
            eq(TEST_SERVICE_AUTH_TOKEN),
            eq(USER_ID),
            eq(EtSyaConstants.JURISDICTION_ID),
            eq(caseTestData.getCaseRequest().getCaseTypeId()),
            eq(caseTestData.getCaseRequest().getCaseId()),
            eq(true),
            any(CaseDataContent.class)
        )).thenReturn(caseTestData.getExpectedDetails());
        when(caseDocumentService.uploadAllDocuments(
            eq(TEST_SERVICE_AUTH_TOKEN),
            eq(caseTestData.getCaseRequest().getCaseTypeId()),
            anyList(),
            anyList(),
            any()
        )).thenReturn(caseTestData.getUploadDocumentResponse());
        when(notificationService.sendSubmitCaseConfirmationEmail(
            eq(caseTestData.getCaseRequest()),
            eq(caseTestData.getCaseData()),
            eq(caseTestData.getUserInfo()),
            any())
        ).thenReturn(null);
        when(featureToggleService.citizenEt1Generation()).thenReturn(true);
    }

    @Test
    void theSubmitCaseProducesCaseDetails() {
        when(acasService.getAcasCertificatesByCaseData(caseTestData.getCaseData())).thenReturn(
            new ArrayList<>()
        );

        CaseDetails caseDetails = caseService.submitCase(TEST_SERVICE_AUTH_TOKEN, caseTestData.getCaseRequest());
        assertEquals(caseDetails.getId(), caseTestData.getExpectedDetails().getId());
        assertEquals(caseDetails.getJurisdiction(), caseTestData.getExpectedDetails().getJurisdiction());
        assertEquals(caseDetails.getCaseTypeId(), caseTestData.getExpectedDetails().getCaseTypeId());
        assertEquals(caseDetails.getState(), caseTestData.getExpectedDetails().getState());
    }
}
