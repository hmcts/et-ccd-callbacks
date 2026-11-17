package uk.gov.hmcts.ethos.replacement.docmosis.service.multiples;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.bundle.Bundle;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.client.BundleApiClient;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultipleUtil;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.ResourceLoader;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_BULK_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.ET1;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.ET1_ATTACHMENT;

@ExtendWith(SpringExtension.class)
class MultiplesDigitalCaseFileServiceTest {
    private static final String ET_DCF_2_YAML = "et-dcf-2.yaml";
    @MockitoBean
    private AuthTokenGenerator authTokenGenerator;
    @MockitoBean
    private BundleApiClient bundleApiClient;

    private MultiplesDigitalCaseFileService multiplesDigitalCaseFileService;
    private MultipleDetails multipleDetails;
    private String userToken;
    private MultipleData caseData;

    @BeforeEach
    public void setUp() throws URISyntaxException, IOException {
        multipleDetails = new MultipleDetails();
        multipleDetails.setCaseTypeId(ENGLANDWALES_BULK_CASE_TYPE_ID);
        multipleDetails.setCaseData(MultipleUtil.getMultipleDataForNotification());
        userToken = "authString";
        multiplesDigitalCaseFileService = new MultiplesDigitalCaseFileService(
                authTokenGenerator,
                bundleApiClient);

        caseData = multipleDetails.getCaseData();
        caseData.setDocumentCollection(new ArrayList<>());
        when(authTokenGenerator.generate()).thenReturn("authToken");
        when(bundleApiClient.stitchMultipleBundle(eq(userToken), eq("authToken"), any()))
                .thenReturn(ResourceLoader.stitchBundleRequest());
    }

    @Test
    void shouldCreateCaseFileRequest() {
        caseData.setDocumentCollection(CaseDataBuilder.builder()
                .withDocumentCollection(ET1)
                .withDocumentCollection(ET1_ATTACHMENT)
                .build().getDocumentCollection());
        List<Bundle> bundles = multiplesDigitalCaseFileService.createCaseFileRequest(multipleDetails.getCaseData());

        assertEquals(ET_DCF_2_YAML, caseData.getBundleConfiguration());
        assertEquals("246000-DCF", bundles.get(0).value().getFileName());
        assertEquals("ET - DCF", bundles.get(0).value().getTitle());
        assertThat(bundles).hasSize(1);
        var bundle = bundles.getFirst();
        assertDoesNotThrow(() -> UUID.fromString(bundle.id()));
        assertThat(bundle.id()).isNotEqualTo(bundle.value().getId());
        assertThat(bundle.value().getDocuments()).hasSize(2).allSatisfy(document -> {
            assertDoesNotThrow(() -> UUID.fromString(document.id()));
            assertThat(document.id()).isNotEqualTo(bundle.id());
        }).extracting(document -> document.id()).doesNotHaveDuplicates();
    }

    @Test
    void shouldStitchCaseFileRequest() {
        List<Bundle> bundles = multiplesDigitalCaseFileService.stitchCaseFile(multipleDetails, userToken);

        assertEquals(ET_DCF_2_YAML, caseData.getBundleConfiguration());
        assertEquals("123456-2021-DCF", bundles.get(0).value().getFileName());
        assertEquals("ET DCF", bundles.get(0).value().getTitle());
        assertNotNull(bundles.get(0).value().getStitchedDocument());
    }

}
