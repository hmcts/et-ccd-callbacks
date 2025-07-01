package uk.gov.hmcts.ethos.replacement.apitest.multiples;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ParseException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicMultiSelectListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.et.common.model.multiples.MultipleRequest;
import uk.gov.hmcts.ethos.replacement.apitest.BaseFunctionalTest;

import java.io.IOException;
import java.util.ArrayList;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_BULK_CASE_TYPE_ID;

@Slf4j
public class DocumentAccessMultiplesControllerFunctionalTest extends BaseFunctionalTest {
    private static final String ABOUT_TO_START_URL = "/multiples/documentAccess/aboutToStart";
    private static final String ABOUT_TO_SUBMIT_URL = "/multiples/documentAccess/aboutToSubmit";
    private MultipleRequest request;

    @BeforeAll
    public void setUpCaseData() throws IOException, InterruptedException, ParseException {
        MultipleData multipleData = MultipleData.builder().build();
        request = new MultipleRequest();
        MultipleDetails multipleDetails = new MultipleDetails();
        multipleDetails.setCaseData(multipleData);
        multipleDetails.setCaseTypeId(SCOTLAND_BULK_CASE_TYPE_ID);
        request.setCaseDetails(multipleDetails);
        multipleData.setDocumentCollection(new ArrayList<>());

        UploadedDocumentType uploadedDocumentType = new UploadedDocumentType();
        uploadedDocumentType.setDocumentFilename("random.pdf");

        DocumentType documentType = new DocumentType();
        documentType.setUploadedDocument(uploadedDocumentType);

        DocumentTypeItem document1 = new DocumentTypeItem();
        document1.setValue(documentType);
        document1.setId("1");
        
        DynamicMultiSelectListType multiSelectList = new DynamicMultiSelectListType();
        multiSelectList.setValue(new ArrayList<DynamicValueType>());

        multipleData.setDocumentSelect(multiSelectList);
        multipleData.getDocumentCollection().add(document1);

        JSONObject singleCase = createSinglesCaseDataStore();
        multipleData.setLeadCase(String.valueOf(singleCase.getLong("id")));

    }

    @Test
    void aboutToStartUrl() {
        RestAssured.given()
                .spec(spec)
                .contentType(ContentType.JSON)
                .header(new Header(AUTHORIZATION, userToken))
                .body(request)
                .post(ABOUT_TO_START_URL)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .log()
                .all(true);
    }

    @Test
    void aboutToSubmit() {
        RestAssured.given()
                .spec(spec)
                .contentType(ContentType.JSON)
                .header(new Header(AUTHORIZATION, userToken))
                .body(request)
                .post(ABOUT_TO_SUBMIT_URL)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .log()
                .all(true);
    }
}
