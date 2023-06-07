package uk.gov.hmcts.ethos.replacement.docmosis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;

@ExtendWith(SpringExtension.class)
class ServingServiceTest {
    private static CaseDetails caseDetails;
    private static CaseData notifyCaseData;
    private static CaseDetails notifyCaseDetails;

    @MockBean
    private static EmailService emailService;

    private static ServingService servingService;

    @BeforeEach
    void setUp() throws Exception {
        caseDetails = generateCaseDetails("midServingCaseDetailsTest.json");
        servingService = new ServingService(emailService);
        notifyCaseDetails = CaseDataBuilder.builder()
            .withEthosCaseReference("12345/6789")
            .withClaimantType("claimant@unrepresented.com")
            .withRepresentativeClaimantType("Claimant Rep", "claimant@represented.com")
            .withClaimantIndType("Claimant", "LastName", "Mr", "Mr")
            .withRespondentWithAddress("Respondent Unrepresented",
                "32 Sweet Street", "14 House", null,
                "Manchester", "M11 4ED", "United Kingdom",
                null, "respondent@unrepresented.com")
            .withRespondentWithAddress("Respondent Represented",
                "32 Sweet Street", "14 House", null,
                "Manchester", "M11 4ED", "United Kingdom",
                null)
            .withRespondentRepresentative("Respondent Represented", "Rep LastName", "res@rep.com")
            .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);

        notifyCaseDetails.setCaseId("1234");
        notifyCaseData = notifyCaseDetails.getCaseData();
        notifyCaseData.setClaimant("Claimant LastName");
    }

    private static CaseDetails generateCaseDetails(String jsonFileName) throws Exception {
        String json = new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(
                ServingServiceTest.class.getClassLoader()
                .getResource(jsonFileName)).toURI())));
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, CaseDetails.class);
    }

    @Test
    void generateOtherTypeDocumentLink() {
        String expectedDocumentName = "**<big>test-filename.xlsx</big>**<br/><small><a target=\"_blank\" "
            + "href=\"/documents/test-document/binary\">test-filename.xlsx</a></small><br/>";
        List<DocumentTypeItem> documentTypeItems = caseDetails.getCaseData().getServingDocumentCollection();
        assertThat(servingService
            .generateOtherTypeDocumentLink(documentTypeItems), is(expectedDocumentName));
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
        assertThat(servingService.generateClaimantAndRespondentAddress(caseData),
                is(expectedClaimantAndRespondentAddress));
    }

    @Test
    void generateEmailLinkToAcas() {
        String expectedEt1EmailLinkToAcas = "mailto:etsmail@acas.org.uk?subject=2120001/2019"
            + "&body=Parties%20in%20claim%3A%20Doris%20Johnson%20vs%20Antonio%20Vazquez%2C%20Juan%20Garcia%0D%0A"
            + "Case%20reference%20number%3A%202120001/2019%0D%0A%0D%0ADear%20Acas%2C%0D%0A%0D%0AThe%20tribunal%20"
            + "has%20completed%20ET1%20serving%20to%20the%20respondent.%0D%0A%0D%0AThe%20documents%20we%20sent%20are"
            + "%20attached%20to%20this%20email.%0D%0A%0D%0A";
        String expectedEt3EmailLinkToAcas = "mailto:etsmail@acas.org.uk?subject=2120001/2019"
            + "&body=Parties%20in%20claim%3A%20Doris%20Johnson%20vs%20Antonio%20Vazquez%2C%20Juan%20Garcia%0D%0A"
            + "Case%20reference%20number%3A%202120001/2019%0D%0A%0D%0ADear%20Acas%2C%0D%0A%0D%0AThe%20tribunal%20"
            + "has%20completed%20ET3%20notifications%20to%20the%20relevant%20parties.%0D%0A%0D%0AThe%20documents%20we"
            + "%20sent%20are%20attached%20to%20this%20email.%0D%0A%0D%0A";
        CaseData caseData = caseDetails.getCaseData();
        assertThat(servingService.generateEmailLinkToAcas(caseData, false), is(expectedEt1EmailLinkToAcas));
        assertThat(servingService.generateEmailLinkToAcas(caseData, true), is(expectedEt3EmailLinkToAcas));
    }

    @Test
    void sendNotifications_shouldSendThreeNotifications() {
        servingService.sendNotifications(notifyCaseDetails);
        verify(emailService, times(1)).sendEmail(any(), eq("claimant@represented.com"), any());
        verify(emailService, times(1)).sendEmail(any(), eq("respondent@unrepresented.com"), any());
        verify(emailService, times(1)).sendEmail(any(), eq("res@rep.com"), any());
    }

    @Test
    void sendNotifications_shouldHandleMissingEmails() {
        reset(emailService);
        notifyCaseData.getRepresentativeClaimantType().setRepresentativeEmailAddress(null);
        notifyCaseData.getRepCollection().get(0).getValue().setRepresentativeEmailAddress(null);
        notifyCaseData.getRespondentCollection().get(0).getValue().setRespondentEmail(null);
        servingService.sendNotifications(notifyCaseDetails);
        verify(emailService, times(0)).sendEmail(any(), any(), any());
    }

    @ParameterizedTest
    @MethodSource("saveServingDocToDocumentCollectionParameter")
    void saveServingDocToDocumentCollection(String servingTypeOfDoc, String resultTypeOfDoc) throws Exception {
        DocumentType documentType = new DocumentType();
        documentType.setTypeOfDocument(servingTypeOfDoc);

        DocumentTypeItem documentTypeItem = new DocumentTypeItem();
        documentTypeItem.setId(UUID.randomUUID().toString());
        documentTypeItem.setValue(documentType);

        CaseData caseData = new CaseData();
        caseData.setServingDocumentCollection(List.of(documentTypeItem));

        servingService.addServingDocToDocumentCollection(caseData);
        assertThat(caseData.getDocumentCollection().get(0).getValue().getTypeOfDocument(), is(resultTypeOfDoc));
    }

    private static Stream<Arguments> saveServingDocToDocumentCollectionParameter() {
        return Stream.of(
            Arguments.of("1.1", "Acknowledgement of claim"),
            Arguments.of("2.6", "Notice of a claim"),
            Arguments.of("2.7", "Notice of a claim"),
            Arguments.of("2.8", "Notice of a claim"),
            Arguments.of("7.7", "Notice of Hearing"),
            Arguments.of("7.8", "Notice of Hearing"),
            Arguments.of("7.8a", "Notice of Hearing")
        );
    }

}
