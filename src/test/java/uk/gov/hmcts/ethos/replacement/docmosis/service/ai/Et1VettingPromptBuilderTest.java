package uk.gov.hmcts.ethos.replacement.docmosis.service.ai;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.JurCodesTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantHearingPreference;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantIndType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantOtherType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantRequestType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantWorkAddressType;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.JurCodesType;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
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

    @Test
    void buildCasePrompt_includesRichClaimantEmploymentNarrativeAndUnattachedDocs() {
        CaseData caseData = CaseDataBuilder.builder()
                .withEthosCaseReference("1800002/2026")
                .withManagingOffice("Manchester")
                .build();
        caseData.setClaimantTypeOfClaimant("Individual");
        ClaimantIndType indType = new ClaimantIndType();
        indType.setClaimantFirstNames("Jane");
        indType.setClaimantLastName("Doe");
        indType.setClaimantDateOfBirth("1990-05-12");
        indType.setClaimantGender("Female");
        caseData.setClaimantIndType(indType);

        Address workAddr = new Address();
        workAddr.setAddressLine1("10 High Street");
        workAddr.setPostTown("Manchester");
        workAddr.setPostCode("M1 1AA");
        ClaimantWorkAddressType workAddrType = new ClaimantWorkAddressType();
        workAddrType.setClaimantWorkAddress(workAddr);
        caseData.setClaimantWorkAddress(workAddrType);

        caseData.setEt1VettingEraAssessmentMarkUp("ERA triggered on 2026-10-05");

        ClaimantOtherType otherType = new ClaimantOtherType();
        otherType.setClaimantEmployedFrom("2020-01-01");
        otherType.setClaimantEmployedTo("2026-09-30");
        otherType.setClaimantEmployedCurrently("No");
        otherType.setClaimantOccupation("Analyst");
        otherType.setClaimantAverageWeeklyHours("37.5");
        otherType.setClaimantPayBeforeTax("40000");
        otherType.setDateOfLastEvent("2026-09-30");
        caseData.setClaimantOtherType(otherType);

        ClaimantHearingPreference hearingPref = new ClaimantHearingPreference();
        hearingPref.setHearingPreferences(List.of("Video"));
        hearingPref.setReasonableAdjustments("Yes");
        hearingPref.setReasonableAdjustmentsDetail("Screen reader");
        caseData.setClaimantHearingPreference(hearingPref);

        JurCodesType jurCode = new JurCodesType();
        jurCode.setJuridictionCodesList("UDL");
        JurCodesTypeItem jurItem = new JurCodesTypeItem();
        jurItem.setValue(jurCode);
        caseData.setJurCodesCollection(List.of(jurItem));

        ClaimantRequestType requests = new ClaimantRequestType();
        requests.setDiscriminationClaims(List.of("Sex"));
        requests.setPayClaims(List.of("Holiday Pay"));
        requests.setWhistleblowing("Yes");
        requests.setWhistleblowingAuthority("HSE");
        requests.setOtherClaim("Breach of contract");
        requests.setClaimantCompensationAmount("12000");
        requests.setClaimantCompensationText("Loss of earnings");
        requests.setClaimOutcome(List.of("Compensation"));
        requests.setClaimantTribunalRecommendation("Policy review");
        requests.setLinkedCases("Yes");
        requests.setLinkedCasesDetail("1800000/2026");
        requests.setClaimDescription("Dismissed after raising safety concern.");
        caseData.setClaimantRequests(requests);
        caseData.setTypesOfClaim(List.of("unfairDismissal"));

        UploadedDocumentType unattached = new UploadedDocumentType();
        unattached.setDocumentFilename("other-evidence.pdf");
        DocumentType docType = new DocumentType();
        docType.setTypeOfDocument("Other");
        docType.setUploadedDocument(unattached);
        DocumentTypeItem docItem = new DocumentTypeItem();
        docItem.setValue(docType);
        caseData.setDocumentCollection(List.of(docItem));

        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseId("999999");
        caseDetails.setCaseTypeId(ENGLANDWALES_CASE_TYPE_ID);
        caseDetails.setCaseData(caseData);

        String prompt = promptBuilder.buildCasePrompt(caseDetails, List.of(), List.of("Manchester"));

        assertThat(prompt)
                .contains("Claimant Name: Jane Doe")
                .contains("Claimant Date of Birth: 12-05-1990")
                .contains("Claimant Work Address: 10 High Street, Manchester, M1 1AA")
                .contains("ERA triggered on 05-10-2026")
                .contains("Currently Assigned Codes: [UDL]")
                .contains("Discrimination Claims: [Sex]")
                .contains("Compensation Amount Claimed: £12000")
                .contains("Linked Cases Stated: Yes - 1800000/2026")
                .contains("Dismissed after raising safety concern.")
                .contains("Other Uploaded Documents (Metadata Only)")
                .contains("other-evidence.pdf");
    }
}
