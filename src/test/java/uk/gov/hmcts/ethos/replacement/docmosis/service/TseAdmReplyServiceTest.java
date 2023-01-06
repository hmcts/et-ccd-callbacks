package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.TseRespondentReplyTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.TseAdminReplyType;
import uk.gov.hmcts.et.common.model.ccd.types.TseRespondentReplyType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CaseDataBuilder;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.DocumentTypeBuilder;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.TseApplicationBuilder;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@ExtendWith(SpringExtension.class)
@SuppressWarnings("squid:S5961")
class TseAdmReplyServiceTest {

    @MockBean
    private DocumentManagementService documentManagementService;

    private TseAdmReplyService tseAdmReplyService;
    private CaseData caseData;

    private static final String AUTH_TOKEN = "Bearer authToken";

    @BeforeEach
    void setUp() {
        tseAdmReplyService = new TseAdmReplyService(documentManagementService);
        caseData = CaseDataBuilder.builder().build();
    }

    @Test
    void initialTseAdminTableMarkUp_ReturnString() {
        caseData.setGenericTseApplicationCollection(
                List.of(GenericTseApplicationTypeItem.builder()
                        .id(UUID.randomUUID().toString())
                        .value(TseApplicationBuilder.builder()
                                .withNumber("1")
                                .withType("Amend response")
                                .withApplicant("Respondent")
                                .withDate("13 December 2022")
                                .withDocumentUpload(createUploadedDocumentType("document.txt"))
                                .withDetails("Details Text")
                                .withCopyToOtherPartyYesOrNo("I confirm I want to copy")
                                .withStatus("Open")
                                .withRespondentReply(List.of(TseRespondentReplyTypeItem.builder()
                                        .id(UUID.randomUUID().toString())
                                        .value(
                                                TseRespondentReplyType.builder()
                                                        .from("Claimant")
                                                        .date("23 December 2022")
                                                        .response("Response Details")
                                                        .hasSupportingMaterial(YES)
                                                        .supportingMaterial(List.of(
                                                                createDocumentTypeItem("image.png"),
                                                                createDocumentTypeItem("Form.pdf")))
                                                        .copyToOtherParty("I do not want to copy")
                                                        .build()
                                        ).build()))
                                .build())
                        .build())
        );

        caseData.setTseAdminSelectApplication(
                DynamicFixedListType.of(DynamicValueType.create("1", "1 - Amend response")));

        String fileDisplay1 = "<a href=\"/documents/%s\" target=\"_blank\">document (TXT, 1MB)</a>";
        when(documentManagementService.displayDocNameTypeSizeLink(
                createUploadedDocumentType("document.txt"), AUTH_TOKEN))
                .thenReturn(fileDisplay1);

        String fileDisplay2 = "<a href=\"/documents/%s\" target=\"_blank\">image (PNG, 2MB)</a>";
        when(documentManagementService.displayDocNameTypeSizeLink(
                createUploadedDocumentType("image.png"), AUTH_TOKEN))
                .thenReturn(fileDisplay2);

        String fileDisplay3 = "<a href=\"/documents/%s\" target=\"_blank\">Form (PDF, 3MB)</a>";
        when(documentManagementService.displayDocNameTypeSizeLink(
                createUploadedDocumentType("Form.pdf"), AUTH_TOKEN))
                .thenReturn(fileDisplay3);

        String expected = "| | |\r\n"
                + "|--|--|\r\n"
                + "|Applicant | Respondent|\r\n"
                + "|Type of application | Amend response|\r\n"
                + "|Application date | 13 December 2022|\r\n"
                + "|Give details | Details Text|\r\n"
                + "|Supporting material | " + fileDisplay1 + "|\r\n"
                + "|Do you want to copy this correspondence to the other party to satisfy the Rules of Procedure? "
                + "| Yes|\r\n"
                + "\r\n"
                + "|Response 1 | |\r\n"
                + "|--|--|\r\n"
                + "|Response from | Claimant|\r\n"
                + "|Response date | 23 December 2022|\r\n"
                + "|What’s your response to the claimant’s application? | Response Details|\r\n"
                + "|Supporting material | " + fileDisplay2 + "<br>" + fileDisplay3 + "<br>" + "|\r\n"
                + "|Do you want to copy this correspondence to the other party to satisfy the Rules of Procedure? "
                + "| No|\r\n"
                + "\r\n";

        String actual = tseAdmReplyService.initialTseAdmReplyTableMarkUp(caseData, AUTH_TOKEN);

        assertThat(actual)
                .isEqualTo(expected);
    }

    private UploadedDocumentType createUploadedDocumentType(String fileName) {
        UploadedDocumentType uploadedDocumentType = new UploadedDocumentType();
        uploadedDocumentType.setDocumentBinaryUrl("http://dm-store:8080/documents/1234/binary");
        uploadedDocumentType.setDocumentFilename(fileName);
        uploadedDocumentType.setDocumentUrl("http://dm-store:8080/documents/1234");
        return uploadedDocumentType;
    }

    private DocumentTypeItem createDocumentTypeItem(String fileName) {
        DocumentTypeItem documentTypeItem = new DocumentTypeItem();
        documentTypeItem.setId("1234");
        documentTypeItem.setValue(DocumentTypeBuilder.builder().withUploadedDocument(fileName, "1234").build());
        return documentTypeItem;
    }

    @Test
    void saveTseAdmReplyDataFromCaseData_CmoYes_SaveString() {
        caseData.setGenericTseApplicationCollection(
                List.of(GenericTseApplicationTypeItem.builder()
                        .id(UUID.randomUUID().toString())
                        .value(TseApplicationBuilder.builder()
                                .withNumber("2")
                                .withType("Change personal details")
                                .build())
                        .build())
        );

        caseData.setTseAdminSelectApplication(
                DynamicFixedListType.of(DynamicValueType.create("2", "2 - Change personal details")));
        caseData.setTseAdmReplyEnterResponseTitle("Submit hearing agenda");
        caseData.setTseAdmReplyAdditionalInformation("Additional Information Details");
        caseData.setTseAdmReplyAddDocumentMandatory(createUploadedDocumentType("document.txt"));
        caseData.setTseAdmReplyIsCmoOrRequest("Case management order");
        caseData.setTseAdmReplyCmoMadeBy("Legal Officer");
        caseData.setTseAdmReplyEnterFullName("Full Name");
        caseData.setTseAdmReplyIsResponseRequired(YES);
        caseData.setTseAdmReplySelectPartyRespond("Both parties");
        caseData.setTseAdmReplySelectPartyNotify("Claimant only");

        tseAdmReplyService.saveTseAdmReplyDataFromCaseData(caseData);

        TseAdminReplyType actual =
                caseData.getGenericTseApplicationCollection().get(0).getValue()
                        .getAdminReply().get(0).getValue();

        assertThat(actual.getDate())
                .isEqualTo(UtilHelper.formatCurrentDate(LocalDate.now()));
        assertThat(actual.getEnterResponseTitle())
                .isEqualTo("Submit hearing agenda");
        assertThat(actual.getAdditionalInformation())
                .isEqualTo("Additional Information Details");
        assertThat(actual.getAddDocument())
                .isEqualTo(createUploadedDocumentType("document.txt"));
        assertThat(actual.getIsCmoOrRequest())
                .isEqualTo("Case management order");
        assertThat(actual.getCmoMadeBy())
                .isEqualTo("Legal Officer");
        assertThat(actual.getRequestMadeBy())
                .isNull();
        assertThat(actual.getEnterFullName())
                .isEqualTo("Full Name");
        assertThat(actual.getIsResponseRequired())
                .isEqualTo(YES);
        assertThat(actual.getSelectPartyRespond())
                .isEqualTo("Both parties");
        assertThat(actual.getSelectPartyNotify())
                .isEqualTo("Claimant only");
    }

    @Test
    void saveTseAdmReplyDataFromCaseData_RequestNo_SaveString() {
        caseData.setGenericTseApplicationCollection(
                List.of(GenericTseApplicationTypeItem.builder()
                        .id(UUID.randomUUID().toString())
                        .value(TseApplicationBuilder.builder()
                                .withNumber("3")
                                .withType("Claimant not complied")
                                .build())
                        .build())
        );

        caseData.setTseAdminSelectApplication(
                DynamicFixedListType.of(DynamicValueType.create("3", "3 - Claimant not complied")));
        caseData.setTseAdmReplyAddDocumentOptional(createUploadedDocumentType("document.txt"));
        caseData.setTseAdmReplyIsCmoOrRequest("Request");
        caseData.setTseAdmReplyRequestMadeBy("Judge");
        caseData.setTseAdmReplyEnterFullName("Full Name");
        caseData.setTseAdmReplyIsResponseRequired(NO);
        caseData.setTseAdmReplySelectPartyNotify("Respondent only");

        tseAdmReplyService.saveTseAdmReplyDataFromCaseData(caseData);

        TseAdminReplyType actual =
                caseData.getGenericTseApplicationCollection().get(0).getValue()
                        .getAdminReply().get(0).getValue();

        assertThat(actual.getDate())
                .isEqualTo(UtilHelper.formatCurrentDate(LocalDate.now()));
        assertThat(actual.getEnterResponseTitle())
                .isNull();
        assertThat(actual.getAdditionalInformation())
                .isNull();
        assertThat(actual.getAddDocument())
                .isEqualTo(createUploadedDocumentType("document.txt"));
        assertThat(actual.getIsCmoOrRequest())
                .isEqualTo("Request");
        assertThat(actual.getCmoMadeBy())
                .isNull();
        assertThat(actual.getRequestMadeBy())
                .isEqualTo("Judge");
        assertThat(actual.getEnterFullName())
                .isEqualTo("Full Name");
        assertThat(actual.getIsResponseRequired())
                .isEqualTo(NO);
        assertThat(actual.getSelectPartyRespond())
                .isNull();
        assertThat(actual.getSelectPartyNotify())
                .isEqualTo("Respondent only");
    }

    @Test
    void saveTseAdmReplyDataFromCaseData_Neither_SaveString() {
        caseData.setGenericTseApplicationCollection(
                List.of(GenericTseApplicationTypeItem.builder()
                        .id(UUID.randomUUID().toString())
                        .value(TseApplicationBuilder.builder()
                                .withNumber("4")
                                .withType("Consider a decision afresh")
                                .build())
                        .build())
        );

        caseData.setTseAdminSelectApplication(
                DynamicFixedListType.of(DynamicValueType.create("4", "4 - Consider a decision afresh")));
        caseData.setTseAdmReplyIsCmoOrRequest("Neither");
        caseData.setTseAdmReplySelectPartyNotify("Both parties");

        tseAdmReplyService.saveTseAdmReplyDataFromCaseData(caseData);

        TseAdminReplyType actual =
                caseData.getGenericTseApplicationCollection().get(0).getValue()
                        .getAdminReply().get(0).getValue();

        assertThat(actual.getDate())
                .isEqualTo(UtilHelper.formatCurrentDate(LocalDate.now()));
        assertThat(actual.getEnterResponseTitle())
                .isNull();
        assertThat(actual.getAdditionalInformation())
                .isNull();
        assertThat(actual.getAddDocument())
                .isNull();
        assertThat(actual.getIsCmoOrRequest())
                .isEqualTo("Neither");
        assertThat(actual.getCmoMadeBy())
                .isNull();
        assertThat(actual.getRequestMadeBy())
                .isNull();
        assertThat(actual.getEnterFullName())
                .isNull();
        assertThat(actual.getIsResponseRequired())
                .isNull();
        assertThat(actual.getSelectPartyRespond())
                .isNull();
        assertThat(actual.getSelectPartyNotify())
                .isEqualTo("Both parties");
    }

    @Test
    void clearTseAdminDataFromCaseData() {
        caseData.setTseAdminSelectApplication(
                DynamicFixedListType.of(DynamicValueType.create("1", "1 - Amend response")));
        caseData.setTseAdmReplyTableMarkUp("| | |\r\n|--|--|\r\n|%s application | %s|\r\n\r\n");
        caseData.setTseAdmReplyEnterResponseTitle("View notice of hearing");
        caseData.setTseAdmReplyAdditionalInformation("Additional information text");
        caseData.setTseAdmReplyAddDocumentMandatory(createUploadedDocumentType("document.txt"));
        caseData.setTseAdmReplyAddDocumentOptional(createUploadedDocumentType("document.txt"));
        caseData.setTseAdmReplyIsCmoOrRequest("Case management order");
        caseData.setTseAdmReplyCmoMadeBy("Legal Officer");
        caseData.setTseAdmReplyRequestMadeBy("Legal Officer");
        caseData.setTseAdmReplyEnterFullName("Enter Full Name");
        caseData.setTseAdmReplyIsResponseRequired(YES);
        caseData.setTseAdmReplySelectPartyRespond("Both parties");
        caseData.setTseAdmReplySelectPartyNotify("Claimant only");

        tseAdmReplyService.clearTseAdmReplyDataFromCaseData(caseData);

        assertThat(caseData.getTseAdminSelectApplication()).isNull();
        assertThat(caseData.getTseAdmReplyTableMarkUp()).isNull();
        assertThat(caseData.getTseAdmReplyEnterResponseTitle()).isNull();
        assertThat(caseData.getTseAdmReplyAdditionalInformation()).isNull();
        assertThat(caseData.getTseAdmReplyAddDocumentMandatory()).isNull();
        assertThat(caseData.getTseAdmReplyAddDocumentOptional()).isNull();
        assertThat(caseData.getTseAdmReplyIsCmoOrRequest()).isNull();
        assertThat(caseData.getTseAdmReplyCmoMadeBy()).isNull();
        assertThat(caseData.getTseAdmReplyRequestMadeBy()).isNull();
        assertThat(caseData.getTseAdmReplyEnterFullName()).isNull();
        assertThat(caseData.getTseAdmReplyIsResponseRequired()).isNull();
        assertThat(caseData.getTseAdmReplySelectPartyRespond()).isNull();
        assertThat(caseData.getTseAdmReplySelectPartyNotify()).isNull();
    }

}
