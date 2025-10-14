package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class UtilsTestConstants {

    //********************************************************************************
    // GenericServiceUtils test constants
    // *******************************************************************************
    public static final String GENERIC_SERVICE_UTILS_TEST_CONSTANT_TYPE_OF_DOCUMENT_ET1 = "ET1";
    public static final String GENERIC_SERVICE_UTILS_TEST_CONSTANT_UPLOADED_DOCUMENT_BINARY_URL =
            "https://uploaded.document.binary.url";
    public static final String GENERIC_SERVICE_UTILS_TEST_CONSTANT_UPLOADED_DOCUMENT_URL =
            "https://uploaded.document.url";
    public static final String GENERIC_SERVICE_UTILS_TEST_CONSTANT_UPLOADED_DOCUMENT_NAME = "Uploaded Document Name";
    public static final UploadedDocumentType
            GENERIC_SERVICE_UTILS_TEST_CONSTANT_NOT_EMPTY_UPLOADED_DOCUMENT_TYPE_FILE =
            generateUploadedDocumentTypeByParams();
    public static final Map<String, Object>
            GENERIC_SERVICE_UTILS_TEST_CONSTANT_NOT_EMPTY_UPLOADED_DOCUMENT_TYPE_RAW_HASHMAP =
            generateUploadedDocumentTypeLinkedHashMap();
    public static final Map<String, Object>
            GENERIC_SERVICE_UTILS_TEST_CONSTANT_NOT_EMPTY_ET1_DOCUMENT_TYPE_RAW_HASHMAP =
            generateDocumentTypeLinkedHashMapByDocumentType();
    public static final String GENERIC_SERVICE_UTILS_TEST_CONSTANT_DOCUMENT_OWNER = "Test Owner";

    //********************************************************************************
    // HTTPUtils test constants
    // *******************************************************************************
    public static final String DUMMY_AUTHORISATION_TOKEN = "dummy_authorisation_token";
    public static final String DUMMY_SERVICE_AUTHORISATION_TOKEN = "dummy_service_authorisation_token";
    public static final String TEST_EXCEPTION_INVALID_MODIFICATION_TYPE =
            "java.lang.Exception: Invalid modification type";

    //********************************************************************************
    // MapperUtils test constants
    // *******************************************************************************
    public static final String TYPE_OF_CLAIM_BREACH_OF_CONTRACT = "breachOfContract";
    public static final String TYPE_OF_CLAIM_DISCRIMINATION = "discrimination";
    public static final String TYPE_OF_CLAIM_PAY_RELATED_CLAIM = "payRelated";
    public static final String TYPE_OF_CLAIM_UNFAIR_DISMISSAL = "unfairDismissal";
    public static final String TYPE_OF_CLAIM_WHISTLE_BLOWING = "whistleBlowing";
    public static final String TEST_CCD_DATA_STORE_BASE_URL = "http://localhost:8080/ccd/data-store";

    //********************************************************************************
    // CaseSearchServiceUtils test constants
    // *******************************************************************************
    public static final String TEST_DUMMY_ROLE = "DummyRole";
    public static final long TEST_CASE_ID_LONG = 123L;
    public static final String TEST_CASE_ID_STRING = "123";
    public static final String TEST_CASE_ID_STRING_NOT_MATCH = "456";

    //********************************************************************************
    // CaseRoleServiceUtils test constants
    // *******************************************************************************
    public static final String TEST_CLAIMANT_SOLICITOR_IDAM_ID = "test_claimant_solicitor_idam_id";
    public static final String TEST_CASE_USER_ROLE_CLAIMANT_SOLICITOR = "[CLAIMANTSOLICITOR]";

    //********************************************************************************
    // RespondentServiceUtils test constants
    // *******************************************************************************
    public static final String TEST_INVALID_INTEGER = "abc";
    public static final String STRING_TEN = "10";
    public static final String TEST_RESPONDENT_NAME = "Test respondent name";
    public static final String EXCEPTION_CASE_DETAILS_NOT_FOUND =
            "java.lang.Exception: Case details not found with the given caseId, ";
    public static final String EXCEPTION_CASE_DETAILS_WITH_ID_123_NOT_HAVE_CASE_DATA =
            "java.lang.Exception: Case details with Case Id, 123 doesn't have case data values";
    public static final String EXCEPTION_INVALID_RESPONDENT_INDEX_EMPTY =
            "java.lang.Exception: Respondent index,  is not valid for the case with id, 1646225213651590";
    public static final String EXCEPTION_INVALID_RESPONDENT_INDEX_NOT_NUMERIC =
            "java.lang.Exception: Respondent index, abc is not valid for the case with id, 1646225213651590";
    public static final String EXCEPTION_INVALID_RESPONDENT_INDEX_NEGATIVE =
            "java.lang.Exception: Respondent index, -1 is not valid for the case with id, 1646225213651590";
    public static final String EXCEPTION_CASE_DETAILS_WITH_ID_1646225213651590_NOT_HAVE_CASE_DATA =
            "java.lang.Exception: Case details with Case Id, 1646225213651590 doesn't have case data values";
    public static final String EXCEPTION_EMPTY_RESPONDENT_COLLECTION =
            "java.lang.Exception: Respondent collection not found for the case with id, 1646225213651590";
    public static final String EXCEPTION_INVALID_RESPONDENT_INDEX_NOT_WITHIN_BOUNDS =
            "java.lang.Exception: Respondent index, 10 is not valid for the case with id, 1646225213651590";
    public static final String EXCEPTION_NOTICE_OF_CHANGE_ANSWER_NOT_FOUND =
            "java.lang.Exception: Notice of change answer not found for the respondent with name, Test respondent name"
                    + " for the case with id, 1646225213651590";
    public static final String EXCEPTION_RESPONDENT_SOLICITOR_TYPE_NOT_FOUND =
            "java.lang.Exception: Respondent solicitor type not found for case with id, 1646225213651590 and "
                    + "respondent organisation policy index, 10";

    private static Map<String, Object> generateDocumentTypeLinkedHashMapByDocumentType() {
        Map<String, Object> object = new ConcurrentHashMap<>();
        object.put("typeOfDocument", GENERIC_SERVICE_UTILS_TEST_CONSTANT_TYPE_OF_DOCUMENT_ET1);
        object.put("ownerDocument", GENERIC_SERVICE_UTILS_TEST_CONSTANT_DOCUMENT_OWNER);
        object.put("documentType", GENERIC_SERVICE_UTILS_TEST_CONSTANT_TYPE_OF_DOCUMENT_ET1);
        object.put("uploadedDocument",
                GENERIC_SERVICE_UTILS_TEST_CONSTANT_NOT_EMPTY_UPLOADED_DOCUMENT_TYPE_RAW_HASHMAP);
        return object;
    }

    private static Map<String, Object> generateUploadedDocumentTypeLinkedHashMap() {
        Map<String, Object> object = new ConcurrentHashMap<>();
        object.put("document_url",
                GENERIC_SERVICE_UTILS_TEST_CONSTANT_NOT_EMPTY_UPLOADED_DOCUMENT_TYPE_FILE.getDocumentUrl());
        object.put("document_filename",
                GENERIC_SERVICE_UTILS_TEST_CONSTANT_NOT_EMPTY_UPLOADED_DOCUMENT_TYPE_FILE.getDocumentFilename());
        object.put("document_binary_url",
                GENERIC_SERVICE_UTILS_TEST_CONSTANT_NOT_EMPTY_UPLOADED_DOCUMENT_TYPE_FILE.getDocumentBinaryUrl());
        return object;
    }

    private static UploadedDocumentType generateUploadedDocumentTypeByParams() {
        UploadedDocumentType uploadedDocumentType = new UploadedDocumentType();
        uploadedDocumentType.setDocumentBinaryUrl(
                UtilsTestConstants.GENERIC_SERVICE_UTILS_TEST_CONSTANT_UPLOADED_DOCUMENT_BINARY_URL);
        uploadedDocumentType.setDocumentUrl(
                UtilsTestConstants.GENERIC_SERVICE_UTILS_TEST_CONSTANT_UPLOADED_DOCUMENT_URL);
        uploadedDocumentType.setDocumentFilename(
                UtilsTestConstants.GENERIC_SERVICE_UTILS_TEST_CONSTANT_UPLOADED_DOCUMENT_NAME);
        return uploadedDocumentType;
    }

    private UtilsTestConstants() {
        // Final classes should not have a public or default constructor.
    }
}
