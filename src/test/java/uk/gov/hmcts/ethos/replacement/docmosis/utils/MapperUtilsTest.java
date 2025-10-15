package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.model.TestData;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.UtilsTestConstants.GENERIC_SERVICE_UTILS_TEST_CONSTANT_DOCUMENT_OWNER;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.UtilsTestConstants.GENERIC_SERVICE_UTILS_TEST_CONSTANT_NOT_EMPTY_ET1_DOCUMENT_TYPE_RAW_HASHMAP;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.UtilsTestConstants.GENERIC_SERVICE_UTILS_TEST_CONSTANT_NOT_EMPTY_UPLOADED_DOCUMENT_TYPE_FILE;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.UtilsTestConstants.GENERIC_SERVICE_UTILS_TEST_CONSTANT_TYPE_OF_DOCUMENT_ET1;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.UtilsTestConstants.TYPE_OF_CLAIM_BREACH_OF_CONTRACT;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.UtilsTestConstants.TYPE_OF_CLAIM_DISCRIMINATION;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.UtilsTestConstants.TYPE_OF_CLAIM_PAY_RELATED_CLAIM;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.UtilsTestConstants.TYPE_OF_CLAIM_UNFAIR_DISMISSAL;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.UtilsTestConstants.TYPE_OF_CLAIM_WHISTLE_BLOWING;

public class MapperUtilsTest {

    private final TestData testData = new TestData();

    @Test
    void shouldMapCaseRequestToCaseData() {
        Map<String, Object> requestCaseData = testData.getCaseRequestCaseDataMap();
        CaseData caseData = MapperUtils.convertCaseDataMapToCaseDataObject(requestCaseData);
        assertThat(caseData.getTypesOfClaim().getFirst()).isEqualTo(TYPE_OF_CLAIM_DISCRIMINATION);
        assertThat(caseData.getTypesOfClaim().get(1)).isEqualTo(TYPE_OF_CLAIM_BREACH_OF_CONTRACT);
        assertThat(caseData.getTypesOfClaim().get(2)).isEqualTo(TYPE_OF_CLAIM_PAY_RELATED_CLAIM);
        assertThat(caseData.getTypesOfClaim().get(3)).isEqualTo(TYPE_OF_CLAIM_UNFAIR_DISMISSAL);
        assertThat(caseData.getTypesOfClaim().get(4)).isEqualTo(TYPE_OF_CLAIM_WHISTLE_BLOWING);
        assertThat(caseData.getEcmCaseType()).isEqualTo(requestCaseData.get("caseType"));
        assertThat(caseData.getCaseSource()).isEqualTo(requestCaseData.get("caseSource"));
        assertThat(caseData.getClaimantRepresentedQuestion()).isEqualTo(
                requestCaseData.get("claimantRepresentedQuestion"));
        assertThat(caseData.getJurCodesCollection()).isEqualTo(requestCaseData.get("jurCodesCollection"));
        assertThat(caseData.getClaimantIndType()).isEqualTo(requestCaseData.get("claimantIndType"));
        assertThat(caseData.getClaimantType()).isEqualTo(requestCaseData.get("claimantType"));
        assertThat(caseData.getRepresentativeClaimantType()).isEqualTo(
                requestCaseData.get("representativeClaimantType"));
        assertThat(caseData.getClaimantOtherType()).isEqualTo(requestCaseData.get("claimantOtherType"));
        assertThat(caseData.getRespondentCollection()).isEqualTo(requestCaseData.get("respondentCollection"));
        assertThat(caseData.getClaimantWorkAddress()).isEqualTo(requestCaseData.get("claimantWorkAddress"));
        assertThat(caseData.getCaseNotes()).isEqualTo(requestCaseData.get("caseNotes"));
        assertThat(caseData.getManagingOffice()).isEqualTo(requestCaseData.get("managingOffice"));
        assertThat(caseData.getNewEmploymentType()).isEqualTo(requestCaseData.get("newEmploymentType"));
        assertThat(caseData.getClaimantRequests()).isEqualTo(requestCaseData.get("claimantRequests"));
        assertThat(caseData.getClaimantHearingPreference()).isEqualTo(
                requestCaseData.get("claimantHearingPreference"));
        assertThat(caseData.getClaimantTaskListChecks()).isEqualTo(requestCaseData.get("claimantTaskListChecks"));
    }

    @SneakyThrows
    @Test
    void theMapJavaObjectToClass() {
        DocumentType documentType = MapperUtils.mapJavaObjectToClass(DocumentType.class,
                GENERIC_SERVICE_UTILS_TEST_CONSTANT_NOT_EMPTY_ET1_DOCUMENT_TYPE_RAW_HASHMAP);
        assertThat(documentType)
                .extracting(DocumentType::getTypeOfDocument,
                        DocumentType::getOwnerDocument,
                        DocumentType::getDocumentType,
                        DocumentType::getUploadedDocument)
                .containsExactly(GENERIC_SERVICE_UTILS_TEST_CONSTANT_TYPE_OF_DOCUMENT_ET1,
                        GENERIC_SERVICE_UTILS_TEST_CONSTANT_DOCUMENT_OWNER,
                        GENERIC_SERVICE_UTILS_TEST_CONSTANT_TYPE_OF_DOCUMENT_ET1,
                        GENERIC_SERVICE_UTILS_TEST_CONSTANT_NOT_EMPTY_UPLOADED_DOCUMENT_TYPE_FILE);
    }
}
