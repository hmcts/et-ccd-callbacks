package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;


class CustomMarkdownHelperTest {
    private static CaseDetails caseDetails;

    @BeforeAll
    static void setUp() throws Exception {
        caseDetails = generateCaseDetails("midServingCaseDetailsTest.json");
    }

    private static CaseDetails generateCaseDetails(String jsonFileName) throws Exception {
        String json = new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(
                CustomMarkdownHelperTest.class.getClassLoader()
                .getResource(jsonFileName)).toURI())));
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, CaseDetails.class);
    }

    @Test
    void generateOtherTypeDocumentName() {
        String expectedDocumentName = "**<big>test-filename.xlsx</big>**<br/><small>Test description</small><br/>";
        List<DocumentTypeItem> documentTypeItems = caseDetails.getCaseData().getServingDocumentCollection();
        Assertions.assertEquals(expectedDocumentName, CustomMarkdownHelper.
                generateOtherTypeDocumentName(documentTypeItems));
    }

    @Test
    void generateClaimantAndRespondentAddress() {
        String expectedClaimantAndRespondentAddress = "**<big>Claimant</big>**<br/>Doris Johnson"
                + "<br/>232 Petticoat Square<br/>22 House<br/>London<br/>W10 4AG<br/><br/>"
                + "**<big>Respondent 1</big>**<br/>Antonio Vazquez"
                + "<br/>11 Small Street<br/>22 House<br/>Manchester<br/>M12 42R<br/><br/>"
                + "**<big>Respondent 2</big>**<br/>Juan Garcia<br/>12 Small Street<br/>24 House"
                + "<br/>Manchester<br/>M12 4ED<br/><br/>";

        CaseData caseData = caseDetails.getCaseData();
        Assertions.assertEquals(expectedClaimantAndRespondentAddress,
                CustomMarkdownHelper.generateClaimantAndRespondentAddress(
                caseData.getServingDocumentRecipient(), caseData.getClaimantIndType(),
                        caseData.getClaimantType().getClaimantAddressUK(), caseData.getRespondentCollection()));
    }
}