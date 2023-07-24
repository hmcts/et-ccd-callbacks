package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.TseAdminRecordDecisionTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.TseAdminRecordDecisionType;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.UploadedDocumentBuilder;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;
import uk.gov.hmcts.ethos.utils.TseApplicationBuilder;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLOSED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.OPEN_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TSE_APP_AMEND_RESPONSE;

@ExtendWith(SpringExtension.class)
class TseAdmCloseServiceTest {

    private TseAdmCloseService tseAdmCloseService;
    @MockBean
    private TseService tseService;

    private CaseData caseData;

    private static final String AUTH_TOKEN = "Bearer authToken";

    @BeforeEach
    void setUp() {
        tseAdmCloseService = new TseAdmCloseService(tseService);
        caseData = CaseDataBuilder.builder().build();
        when(tseService.formatViewApplication(any(), any(), false)).thenReturn("Application Details\r\n");
    }

    @Test
    void updateStatusToClose() {
        caseData.setGenericTseApplicationCollection(
            List.of(GenericTseApplicationTypeItem.builder()
                .id(UUID.randomUUID().toString())
                .value(TseApplicationBuilder.builder()
                    .withNumber("1")
                    .withType(TSE_APP_AMEND_RESPONSE)
                    .withStatus(OPEN_STATE)
                    .build())
                .build())
        );

        caseData.setTseAdminSelectApplication(
            DynamicFixedListType.of(DynamicValueType.create("1", "1 - Amend response")));
        caseData.setTseAdminCloseApplicationTable("| | |\r\n|--|--|\r\n|%s application | %s|\r\n");
        caseData.setTseAdminCloseApplicationText("General notes");

        tseAdmCloseService.aboutToSubmitCloseApplication(caseData);

        assertThat(caseData.getGenericTseApplicationCollection().get(0).getValue().getStatus())
            .isEqualTo(CLOSED_STATE);
        assertThat(caseData.getTseAdminSelectApplication())
            .isNull();
        assertThat(caseData.getTseAdminCloseApplicationTable())
            .isNull();
        assertThat(caseData.getTseAdminCloseApplicationText())
            .isNull();
    }

    @Test
    void generateCloseApplicationMarkdown() {
        GenericTseApplicationType tseApplicationType = getTseAppType();

        caseData.setGenericTseApplicationCollection(
            List.of(GenericTseApplicationTypeItem.builder()
                .id(UUID.randomUUID().toString())
                .value(tseApplicationType)
                .build())
        );

        String expected = "Application Details\r\n";

        String fileDisplay1 = "<a href=\"/documents/%s\" target=\"_blank\">document (TXT, 1MB)</a>";

        when(tseService.addDocumentRows(any(), any())).thenReturn(List.of(
                new String[]{"Document", fileDisplay1},
                new String[]{"Description", ""})
        );

        caseData.setTseAdminSelectApplication(
            DynamicFixedListType.of(DynamicValueType.create("1", "1 - Amend response")));

        assertThat(tseAdmCloseService.generateCloseApplicationDetailsMarkdown(caseData, AUTH_TOKEN))
            .isEqualTo(expected);

    }

    private static GenericTseApplicationType getTseAppType() {
        TseAdminRecordDecisionTypeItem recordDecisionTypeItem = TseAdminRecordDecisionTypeItem.builder()
            .id(UUID.randomUUID().toString())
            .value(
                TseAdminRecordDecisionType.builder()
                    .date("23 December 2022")
                    .enterNotificationTitle("Response Details")
                    .decision("decision")
                    .decisionDetails("decision details")
                    .typeOfDecision("type of decision")
                    .additionalInformation("additional info")
                    .decisionMadeBy("decision made by")
                    .decisionMadeByFullName("made by full name")
                    .selectPartyNotify("party notify")
                    .responseRequiredDoc(
                        List.of(GenericTypeItem.from(DocumentType.from(UploadedDocumentBuilder.builder()
                            .withFilename("admin.txt")
                            .withUuid("1234")
                            .build()))))
                    .build()
            ).build();

        GenericTseApplicationType tseApplicationBuilder = TseApplicationBuilder.builder()
            .withNumber("1")
            .withType(TSE_APP_AMEND_RESPONSE)
            .withApplicant(RESPONDENT_TITLE)
            .withDate("13 December 2022")
            .withDetails("Details Text")
            .withStatus(OPEN_STATE)
            .withDecisionCollection(List.of(
                recordDecisionTypeItem
            ))
            .build();

        tseApplicationBuilder.setDocumentUpload(
            UploadedDocumentBuilder.builder()
                .withFilename("test")
                .withUuid("1234")
                .build());
        return tseApplicationBuilder;
    }

}
