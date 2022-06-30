package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ET1ServingService;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

class ET1ServingServiceTest {
    private static CaseDetails caseDetails;

    private static uk.gov.hmcts.ethos.replacement.docmosis.service.ET1ServingService ET1ServingService;

    @BeforeAll
    static void setUp() throws Exception {
        caseDetails = generateCaseDetails("midServingCaseDetailsTest.json");
        ET1ServingService = new ET1ServingService();
    }

    private static CaseDetails generateCaseDetails(String jsonFileName) throws Exception {
        String json = new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(
                ET1ServingServiceTest.class.getClassLoader()
                .getResource(jsonFileName)).toURI())));
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, CaseDetails.class);
    }

    @Test
    void generateOtherTypeDocumentName() {
        String expectedDocumentName = "**<big>test-filename.xlsx</big>**<br/><small>Test description</small><br/>";
        List<DocumentTypeItem> documentTypeItems = caseDetails.getCaseData().getServingDocumentCollection();
        assertThat(ET1ServingService
            .generateOtherTypeDocumentName(documentTypeItems), is(expectedDocumentName));
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
        assertThat(ET1ServingService.generateClaimantAndRespondentAddress(caseData),
                is(expectedClaimantAndRespondentAddress));
    }

    @Test
    void generateEmailLinkToAcas() {
        String expectedEmailLinkToAcas = "mailto:ET3@acas.org.uk?subject=2120001/2019"
            + "&body=Parties%20in%20claim%3A%20Doris%20Johnson%20vs%20Antonio%20Vazquez%2C%20Juan%20Garcia%0D%0A"
            + "Case%20reference%20number%3A%202120001/2019%0D%0A%0D%0ADear%20Acas%2C%0D%0A%0D%0AThe%20tribunal%20"
            + "has%20completed%20ET1%20serving%20to%20the%20respondent.%0D%0A%0D%0AThe%20documents%20we%20sent%20are"
            + "%20attached%20to%20this%20email.%0D%0A%0D%0A";
        CaseData caseData = caseDetails.getCaseData();
        assertThat(ET1ServingService.generateEmailLinkToAcas(caseData), is(expectedEmailLinkToAcas));
    }
}