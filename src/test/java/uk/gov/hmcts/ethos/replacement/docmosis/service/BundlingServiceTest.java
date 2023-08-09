package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.client.BundleApiClient;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.ResourceLoader;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.DocumentConstants.ET1;

@ExtendWith(SpringExtension.class)
class BundlingServiceTest {

    @MockBean
    private BundleApiClient bundleApiClient;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @MockBean
    private BundlingService bundlingService;
    private CaseData caseData;
    private CaseDetails caseDetails;
    private String authToken = "Bearer token";

    @BeforeEach
    void setUp() throws URISyntaxException, IOException {
        bundlingService = new BundlingService(bundleApiClient, authTokenGenerator);
        caseData = CaseDataBuilder.builder()
                .withEthosCaseReference("123456/2021")
                .withDocumentCollection(ET1)
                .build();
        caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);
        caseDetails.setCaseId("1234123412341234");
        when(bundleApiClient.createBundleServiceRequest(any(), any(), any()))
                .thenReturn(ResourceLoader.createBundleServiceRequests());
        when(bundleApiClient.stitchBundle(any(), any(), any()))
                .thenReturn(ResourceLoader.stitchBundleRequest());
        when(authTokenGenerator.generate()).thenReturn("authToken");
        ReflectionTestUtils.setField(bundlingService, "defaultBundle", "et-dcf-2.yaml");

    }

    @Test
    void createBundleRequest() {
        caseDetails.getCaseData().setCaseBundles(bundlingService.createBundleRequest(caseDetails, authToken));
        assertNotNull(caseDetails.getCaseData().getCaseBundles());
        assertEquals(YES, caseDetails.getCaseData().getCaseBundles().get(0).getValue().getEligibleForStitching());
    }

    @Test
    void stitchBundleRequest() {
        caseDetails.getCaseData().setCaseBundles(bundlingService.stitchBundle(caseDetails, authToken));
        assertNotNull(caseDetails.getCaseData().getCaseBundles());
        System.out.println(caseDetails.getCaseData().getCaseBundles());
        assertNotNull(caseDetails.getCaseData().getCaseBundles().get(0).getValue().getStitchedDocument());
    }

    @ParameterizedTest
    @MethodSource
    void shouldSetBundleConfiguration(String bundleConfig, String expectedConfig) {
        caseData.setBundleConfiguration(bundleConfig);
        bundlingService.setBundleConfig(caseData);
        assertEquals(caseData.getBundleConfiguration(), expectedConfig);
    }

    private static Stream<Arguments> shouldSetBundleConfiguration() {
        return Stream.of(
                Arguments.of("et-dcf-ordered.yaml", "et-dcf-ordered.yaml"),
                Arguments.of("", "et-dcf-2.yaml"),
                Arguments.of(null, "et-dcf-2.yaml")

        );
    }
}
