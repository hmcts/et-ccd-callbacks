package uk.gov.hmcts.ethos.replacement.docmosis.service.ai;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ai.AiDocumentAttachment;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;

class Et1VettingPromptBuilderTest {

    private final Et1VettingPromptBuilder promptBuilder = new Et1VettingPromptBuilder();

    @Test
    void buildSystemInstruction_loadsTemplateAndPopulatesPlaceholders() {
        String systemInstruction = promptBuilder.buildSystemInstruction();

        assertThat(systemInstruction)
                .doesNotContain("{{JURISDICTION_CODES}}")
                .doesNotContain("{{EW_OFFICES}}")
                .doesNotContain("{{SCOTLAND_OFFICES}}")
                .contains("ET Jurisdiction Vetting Bot")
                .contains("   - UDL:")
                .contains("   - EQP: Equal pay claim")
                .contains("   - TBA: Jurisdiction code to be confirmed / allocated")
                .contains("   - ZNON: No applicable jurisdiction code")
                .contains("   - RPT(S):")
                .containsOnlyOnce("   - EQP: Equal pay claim")
                .containsOnlyOnce("   - TBA: Jurisdiction code to be confirmed / allocated")
                .containsOnlyOnce("   - ZNON: No applicable jurisdiction code")
                .contains("London Central")
                .contains("Glasgow")
                .contains("ACAS receipt date and ACAS issue date")
                .contains("no evidence for the issues")
                .contains("ACAS EXEMPTION FIELD RULE")
                .contains("NON-EMPLOYMENT / UNRELATED DOCUMENT RULE")
                .contains("### Suggested Next Steps")
                .contains("Accept | Reject | Refer")
                .contains("Rule 10(2)/(3)")
                .contains("Rule 12(1)")
                .contains("Rule 13(3)-(4)")
                .contains("Return a SINGLE JSON object (NEVER wrap in [])");
    }

    @Test
    void buildCasePrompt_formatsAcasDatesAndOmitsBlankOptionalFields() {
        RespondentSumType respondent = new RespondentSumType();
        respondent.setRespondentName("Acme Ltd");
        respondent.setRespondentAcasQuestion("Yes");
        respondent.setAcasCertificateReceiptDate("2026-10-01");
        respondent.setAcasCertificateIssueDate("2026-10-15");

        RespondentSumTypeItem respondentItem = new RespondentSumTypeItem();
        respondentItem.setId("1");
        respondentItem.setValue(respondent);

        CaseData caseData = CaseDataBuilder.builder()
                .withEthosCaseReference("1800001/2026")
                .withManagingOffice("Leeds")
                .build();
        caseData.setReceiptDate("2026-11-01");
        caseData.setRespondentCollection(List.of(respondentItem));

        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseId("123456");
        caseDetails.setCaseTypeId(ENGLANDWALES_CASE_TYPE_ID);
        caseDetails.setCaseData(caseData);

        AiDocumentAttachment attachment = AiDocumentAttachment.builder()
                .filename("et1.pdf")
                .mimeType("application/pdf")
                .data(new byte[]{1, 2, 3})
                .build();

        String prompt = promptBuilder.buildCasePrompt(caseDetails, List.of(attachment), List.of("Leeds ET"));

        assertThat(prompt)
                .contains("Case Reference: 1800001/2026")
                .contains("Receipt Date: 01-11-2026")
                .contains("ACAS Certificate Receipt Date: 01-10-2026")
                .contains("ACAS Certificate Issue Date: 15-10-2026")
                .contains("Attached Document: et1.pdf (MIME: application/pdf)")
                .doesNotContain("ACAS Certificate Number:")
                .doesNotContain("Whistleblowing:");
    }
}
