package uk.gov.hmcts.ethos.replacement.docmosis.service.ai;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantHearingPreference;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantIndType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantOtherType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantRequestType;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ai.AiDocumentAttachment;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.JurisdictionCode;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.COMPANY;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Et1VettingHelper.DD_MM_YYYY_DATE_FORMAT;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Et1VettingHelper.formatDateDdMmYyyy;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Et1VettingHelper.normalizeDatesToDdMmYyyy;

@Component
public class Et1VettingPromptBuilder {

    private static final String NOT_SPECIFIED = "Not specified";
    private static final String SYSTEM_INSTRUCTION_RESOURCE = "ai/et1-vetting-system-instruction.txt";
    private static final ZoneId LONDON_ZONE = ZoneId.of("Europe/London");
    private static final String DISCRIMINATION_PREFIX = "Discrimination/harassment: ";
    private static final Map<JurisdictionCode, String> ENUM_TO_CCD_CODE = Map.of(
            JurisdictionCode.ADTST, "ADT(ST)",
            JurisdictionCode.RPTS, "RPT(S)",
            JurisdictionCode.RTRST, "RTR(ST)",
            JurisdictionCode.TXCST, "TXC(ST)",
            JurisdictionCode.WTRAL, "WTR(AL)"
    );

    private final String systemInstruction;

    public Et1VettingPromptBuilder() {
        this.systemInstruction = loadSystemInstruction();
    }

    /**
     * Returns the static system instruction loaded from {@code resources/ai/et1-vetting-system-instruction.txt},
     * with official jurisdiction codes and regional office lists populated once at startup.
     *
     * @return static system instruction string
     */
    public String buildSystemInstruction() {
        return systemInstruction;
    }

    /**
     * Builds the dynamic per-case user prompt containing claim metadata, structured party/employment ")
     * details, valid regional offices, hearing venues, claim narrative, and attachment notices.
     *
     * @param caseDetails CaseDetails object from CCD
     * @param attachments List of attached files sent as multimodal parts
     * @param availableVenues Valid hearing venue names for the managing office
     * @return dynamic case prompt string
     */
    public String buildCasePrompt(CaseDetails caseDetails,
                                  List<AiDocumentAttachment> attachments,
                                  List<String> availableVenues) {
        CaseData caseData = caseDetails.getCaseData();
        String today = LocalDate.now(LONDON_ZONE).format(DateTimeFormatter.ofPattern(DD_MM_YYYY_DATE_FORMAT));
        StringBuilder sb = new StringBuilder(4_096);

        sb.append("=== CLAIM FACTS ===\n")
            .append("Today's Date: ").append(today)
            .append("\nCase Reference: ")
            .append(defaultString(caseData.getEthosCaseReference(), caseDetails.getCaseId()))
            .append("\nCase Type ID: ").append(defaultString(caseDetails.getCaseTypeId(), NOT_SPECIFIED))
            .append("\nReceipt Date: ")
            .append(formatDateDdMmYyyy(defaultString(caseData.getReceiptDate(), NOT_SPECIFIED)))
            .append("\nManaging Office: ").append(defaultString(caseData.getManagingOffice(), NOT_SPECIFIED))
            .append("\nCurrent Conciliation Track: ")
            .append(defaultString(caseData.getConciliationTrack(), NOT_SPECIFIED))
            .append("\n\n-- Claimant Details --\n")
            .append("Claimant Name: ").append(extractClaimantName(caseData)).append('\n')
            .append("Claimant Type: ").append(defaultString(caseData.getClaimantTypeOfClaimant(), "Person"))
            .append('\n');

        ClaimantIndType indType = caseData.getClaimantIndType();
        if (indType != null) {
            if (StringUtils.isNotBlank(indType.getClaimantDateOfBirth())) {
                sb.append("Claimant Date of Birth: ")
                    .append(formatDateDdMmYyyy(indType.getClaimantDateOfBirth())).append('\n');
            }
            if (StringUtils.isNotBlank(indType.getClaimantGender())) {
                sb.append("Claimant Gender: ").append(indType.getClaimantGender()).append('\n');
            }
        }

        sb.append("Claimant Address: ").append(extractAddress(caseData.getClaimantType() != null
            ? caseData.getClaimantType().getClaimantAddressUK() : null)).append('\n');

        if (caseData.getClaimantWorkAddress() != null
                && caseData.getClaimantWorkAddress().getClaimantWorkAddress() != null) {
            sb.append("Claimant Work Address: ")
                .append(extractAddress(caseData.getClaimantWorkAddress().getClaimantWorkAddress()))
                .append('\n');
        }

        if (StringUtils.isNotBlank(caseData.getEt1VettingEraAssessmentMarkUp())) {
            sb.append("\n-- ERA Assessment Triggered --\n")
                .append(normalizeDatesToDdMmYyyy(caseData.getEt1VettingEraAssessmentMarkUp())).append('\n');
        }

        if (CollectionUtils.isNotEmpty(availableVenues)) {
            sb.append("\n-- Available Hearing Venues for Managing Office --\n")
                .append(String.join(", ", availableVenues)).append('\n');
        }

        appendEmploymentDetails(sb, caseData);
        appendHearingPreferences(sb, caseData);
        appendRespondentDetails(sb, caseData);
        appendExistingJurisdictionCodes(sb, caseData);
        appendClaimNarrative(sb, caseData);
        appendAttachedFilesNotice(sb, attachments);
        appendExistingDocuments(sb, caseData, attachments);

        return sb.toString();
    }

    private void appendEmploymentDetails(StringBuilder sb, CaseData caseData) {
        ClaimantOtherType otherType = caseData.getClaimantOtherType();
        sb.append("\n-- Employment Details --\n");
        if (otherType != null) {
            appendDateField(sb, "Start Date", otherType.getClaimantEmployedFrom(), true);
            appendDateField(sb, "End Date", otherType.getClaimantEmployedTo(), true);
            appendIfNotBlank(sb, "Still Working", otherType.getClaimantEmployedCurrently());
            appendIfNotBlank(sb, "Job Title", otherType.getClaimantOccupation());
            appendIfNotBlank(sb, "Average Weekly Hours", otherType.getClaimantAverageWeeklyHours());
            appendIfNotBlank(sb, "Pay Before Tax", otherType.getClaimantPayBeforeTax());
            appendIfNotBlank(sb, "Pay After Tax", otherType.getClaimantPayAfterTax());
            appendIfNotBlank(sb, "Pay Cycle", otherType.getClaimantPayCycle());
            appendIfNotBlank(sb, "Notice Period", otherType.getClaimantNoticePeriod());
        } else {
            appendDateField(sb, "Start Date", caseData.getClaimantStartDate(), true);
            appendDateField(sb, "End Date", caseData.getClaimantEndDate(), true);
            if (CollectionUtils.isNotEmpty(caseData.getClaimantStillWorking())) {
                sb.append("Still Working: ").append(caseData.getClaimantStillWorking()).append('\n');
            }
            appendIfNotBlank(sb, "Job Title", caseData.getClaimantJobTitle());
        }
    }

    private void appendHearingPreferences(StringBuilder sb, CaseData caseData) {
        ClaimantHearingPreference preference = caseData.getClaimantHearingPreference();
        if (preference == null) {
            return;
        }
        StringBuilder prefBuilder = new StringBuilder();
        if (CollectionUtils.isNotEmpty(preference.getHearingPreferences())) {
            prefBuilder.append("Hearing Options: ").append(preference.getHearingPreferences()).append('\n');
        }
        appendIfNotBlank(prefBuilder, "Reasonable Adjustments", preference.getReasonableAdjustments());
        appendIfNotBlank(prefBuilder, "Reasonable Adjustments Details",
                preference.getReasonableAdjustmentsDetail());
        appendIfNotBlank(prefBuilder, "Contact Language", preference.getContactLanguage());
        appendIfNotBlank(prefBuilder, "Hearing Language", preference.getHearingLanguage());
        appendIfNotBlank(prefBuilder, "Panel Preference", preference.getClaimantHearingPanelPreference());
        appendIfNotBlank(prefBuilder, "Panel Preference Reason", preference.getClaimantHearingPanelPreferenceWhy());
        if (!prefBuilder.isEmpty()) {
            sb.append("\n-- Hearing Preferences --\n").append(prefBuilder);
        }
    }

    private void appendRespondentDetails(StringBuilder sb, CaseData caseData) {
        List<RespondentSumTypeItem> respondents = caseData.getRespondentCollection();
        sb.append("\n-- Respondents --\n");
        if (CollectionUtils.isEmpty(respondents)) {
            sb.append("No respondents listed\n");
            return;
        }

        int idx = 1;
        for (RespondentSumTypeItem item : respondents) {
            RespondentSumType resp = item.getValue();
            if (resp == null) {
                idx++;
                continue;
            }
            sb.append("Respondent ").append(idx).append(":\n")
                .append("  Name: ").append(defaultString(resp.getRespondentName(), NOT_SPECIFIED)).append('\n')
                .append("  Address: ").append(extractAddress(resp.getRespondentAddress())).append('\n')
                .append("  ACAS Certificate Question: ")
                .append(defaultString(resp.getRespondentAcasQuestion(), NOT_SPECIFIED)).append('\n');
            appendIfNotBlank(sb, "  ACAS Certificate Number", resp.getRespondentAcas());
            appendDateField(sb, "  ACAS Certificate Receipt Date", resp.getAcasCertificateReceiptDate(), false);
            appendDateField(sb, "  ACAS Certificate Issue Date", resp.getAcasCertificateIssueDate(), false);
            appendIfNotBlank(sb, "  ACAS Exemption Reason", resp.getRespondentAcasNo());
            idx++;
        }
    }

    private void appendExistingJurisdictionCodes(StringBuilder sb, CaseData caseData) {
        sb.append("\n-- Existing System Jurisdiction Codes --\n");
        if (CollectionUtils.isNotEmpty(caseData.getJurCodesCollection())) {
            List<String> codes = caseData.getJurCodesCollection().stream()
                .filter(item -> item.getValue() != null
                    && StringUtils.isNotBlank(item.getValue().getJuridictionCodesList()))
                .map(item -> item.getValue().getJuridictionCodesList())
                .toList();
            sb.append("Currently Assigned Codes: ").append(codes).append('\n');
        } else {
            sb.append("Currently Assigned Codes: None\n");
        }
    }

    private void appendClaimNarrative(StringBuilder sb, CaseData caseData) {
        sb.append("\n-- Claim Grounds & Narrative --\n");

        List<String> types = resolveTypesOfClaim(caseData);
        if (CollectionUtils.isNotEmpty(types)) {
            sb.append("Types of Claim: ").append(types).append('\n');
        }

        ClaimantRequestType requests = caseData.getClaimantRequests();
        List<String> discriminations = resolveFirstNonEmptyList(
                requests != null ? requests.getDiscriminationClaims() : null,
                caseData.getDiscriminationTypesOfClaim());
        if (CollectionUtils.isNotEmpty(discriminations)) {
            sb.append("Discrimination Claims: ").append(discriminations).append('\n');
        }

        List<String> payClaims = resolveFirstNonEmptyList(
                requests != null ? requests.getPayClaims() : null,
                caseData.getPayTypesOfClaim());
        if (CollectionUtils.isNotEmpty(payClaims)) {
            sb.append("Pay Claims: ").append(payClaims).append('\n');
        }

        appendIfNotBlank(sb, "Whistleblowing", resolveWhistleblowing(requests, caseData));

        String wbAuthority = resolveFirstNonBlank(
                requests != null ? requests.getWhistleblowingAuthority() : null,
                caseData.getWhistleblowingRegulator());
        appendIfNotBlank(sb, "Whistleblowing Regulator / Authority", wbAuthority);

        String otherClaim = resolveFirstNonBlank(
                requests != null ? requests.getOtherClaim() : null,
                caseData.getOtherTypeOfClaimDetails());
        appendIfNotBlank(sb, "Other Claim Details", otherClaim);

        appendCompensationAndLinkedCases(sb, requests);

        String lastEvent = caseData.getClaimantOtherType() != null
            ? resolveFirstNonBlank(caseData.getClaimantOtherType().getDateOfLastEvent(),
                    caseData.getEt1SectionThreeDateOfLastEvent())
            : caseData.getEt1SectionThreeDateOfLastEvent();
        appendDateField(sb, "Date of Last Event", lastEvent, true);

        String claimNarrative = resolveFirstNonBlank(
                requests != null ? requests.getClaimDescription() : null,
                caseData.getEt1SectionThreeClaimDetails());
        if (StringUtils.isNotBlank(claimNarrative)) {
            sb.append("Claim Narrative Text:\n").append(claimNarrative).append('\n');
        }
    }

    private void appendCompensationAndLinkedCases(StringBuilder sb, ClaimantRequestType requests) {
        if (requests == null) {
            return;
        }
        if (StringUtils.isNotBlank(requests.getClaimantCompensationAmount())) {
            sb.append("Compensation Amount Claimed: £")
                .append(requests.getClaimantCompensationAmount()).append('\n');
        }
        appendIfNotBlank(sb, "Compensation Details", requests.getClaimantCompensationText());
        if (CollectionUtils.isNotEmpty(requests.getClaimOutcome())) {
            sb.append("Remedies / Outcomes Sought: ").append(requests.getClaimOutcome()).append('\n');
        }
        appendIfNotBlank(sb, "Tribunal Recommendations Sought", requests.getClaimantTribunalRecommendation());
        if (StringUtils.isNotBlank(requests.getLinkedCases())) {
            sb.append("Linked Cases Stated: ").append(requests.getLinkedCases())
                .append(" - ").append(defaultString(requests.getLinkedCasesDetail(), "")).append('\n');
        }
    }

    private List<String> resolveTypesOfClaim(CaseData caseData) {
        if (CollectionUtils.isNotEmpty(caseData.getTypesOfClaim())) {
            return caseData.getTypesOfClaim();
        }
        return CollectionUtils.isNotEmpty(caseData.getTypeOfClaim())
                ? caseData.getTypeOfClaim()
                : caseData.getEt1SectionThreeTypeOfClaim();
    }

    private List<String> resolveFirstNonEmptyList(List<String> primary, List<String> fallback) {
        return CollectionUtils.isNotEmpty(primary) ? primary : fallback;
    }

    private String resolveWhistleblowing(ClaimantRequestType requests, CaseData caseData) {
        if (requests != null && StringUtils.isNotBlank(requests.getWhistleblowing())) {
            return requests.getWhistleblowing();
        }
        return CollectionUtils.isNotEmpty(caseData.getWhistleblowingYesNo())
                ? caseData.getWhistleblowingYesNo().toString()
                : null;
    }

    private String resolveFirstNonBlank(String primary, String fallback) {
        return StringUtils.isNotBlank(primary) ? primary : fallback;
    }

    private void appendAttachedFilesNotice(StringBuilder sb, List<AiDocumentAttachment> attachments) {
        if (CollectionUtils.isNotEmpty(attachments)) {
            sb.append("\n-- Attached Claim & ACAS Documents (Multimodal Input) --\n");
            for (AiDocumentAttachment attachment : attachments) {
                sb.append("Attached Document: ").append(attachment.getFilename())
                    .append(" (MIME: ").append(attachment.getMimeType()).append(")\n");
            }
            sb.append("NOTE: All ").append(attachments.size()).append(" file(s) listed above are attached directly. ")
                .append("Cross-check all attached documents and typed narrative to verify names, ACAS EC numbers, ")
                .append("ACAS receipt and issue dates, event dates, remedies, and claims for Rule 13, ERA, and ")
                .append("vetting.\n");
        }
    }

    private void appendExistingDocuments(StringBuilder sb,
                                         CaseData caseData,
                                         List<AiDocumentAttachment> attachments) {
        List<DocumentTypeItem> docs = caseData.getDocumentCollection();
        if (CollectionUtils.isEmpty(docs)) {
            return;
        }
        Set<String> attachedNames = new HashSet<>();
        if (CollectionUtils.isNotEmpty(attachments)) {
            for (AiDocumentAttachment attachment : attachments) {
                if (StringUtils.isNotBlank(attachment.getFilename())) {
                    attachedNames.add(attachment.getFilename().trim());
                }
            }
        }
        StringBuilder unattachedDocs = new StringBuilder();
        for (DocumentTypeItem item : docs) {
            if (item.getValue() != null && item.getValue().getUploadedDocument() != null) {
                String filename = item.getValue().getUploadedDocument().getDocumentFilename();
                if (!attachedNames.contains(StringUtils.trimToEmpty(filename))) {
                    unattachedDocs.append("Document: ").append(item.getValue().getTypeOfDocument())
                        .append(" - ").append(filename).append('\n');
                }
            }
        }
        if (!unattachedDocs.isEmpty()) {
            sb.append("\n-- Other Uploaded Documents (Metadata Only) --\n").append(unattachedDocs);
        }
    }

    private String loadSystemInstruction() {
        ClassPathResource resource = new ClassPathResource(SYSTEM_INSTRUCTION_RESOURCE);
        try (InputStream inputStream = resource.getInputStream()) {
            String template = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            return template
                    .replace("{{JURISDICTION_CODES}}", buildJurisdictionCodesBlock())
                    .replace("{{EW_OFFICES}}", formatOfficeNames(TribunalOffice.ENGLANDWALES_OFFICES))
                    .replace("{{SCOTLAND_OFFICES}}", formatOfficeNames(TribunalOffice.SCOTLAND_OFFICES));
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load ET1 vetting system instruction template", ex);
        }
    }

    private String formatOfficeNames(List<TribunalOffice> offices) {
        return offices.stream()
                .map(TribunalOffice::getOfficeName)
                .collect(Collectors.joining(", "));
    }

    private String buildJurisdictionCodesBlock() {
        StringBuilder sb = new StringBuilder(4_096);
        for (JurisdictionCode code : JurisdictionCode.values()) {
            String ccdCode = ENUM_TO_CCD_CODE.getOrDefault(code, code.name());
            sb.append("   - ").append(ccdCode).append(": ")
                .append(compactDescription(code.getDescription())).append('\n');
        }
        return sb.toString().stripTrailing();
    }

    private String compactDescription(String description) {
        return description
                .replace("Discrimination, including indirect discrimination based on association or perception,"
                        + " or harassment on grounds of ", DISCRIMINATION_PREFIX)
                .replace("Discrimination, including indirect discrimination, discrimination based on association or "
                        + "perception, or harassment because of ", DISCRIMINATION_PREFIX)
                .replace("Discrimination, including indirect discrimination, discrimination based on association or "
                        + "perception, or harassment on grounds of ", DISCRIMINATION_PREFIX)
                .replace("Discrimination, including indirect discrimination based on association or perception "
                        + "or harassment on grounds of ", DISCRIMINATION_PREFIX)
                .replace("Suffered a detriment and/or dismissal ", "Detriment/dismissal ")
                .replace("Suffer a detriment and/or dismissal ", "Detriment/dismissal ");
    }

    private void appendIfNotBlank(StringBuilder sb, String label, String value) {
        if (StringUtils.isNotBlank(value)) {
            sb.append(label).append(": ").append(value).append('\n');
        }
    }

    private void appendDateField(StringBuilder sb, String label, String value, boolean includeWhenMissing) {
        if (StringUtils.isNotBlank(value)) {
            sb.append(label).append(": ").append(formatDateDdMmYyyy(value)).append('\n');
        } else if (includeWhenMissing) {
            sb.append(label).append(": ").append(NOT_SPECIFIED).append('\n');
        }
    }

    private String extractClaimantName(CaseData caseData) {
        if (COMPANY.equals(caseData.getClaimantTypeOfClaimant())) {
            return defaultString(caseData.getClaimantCompany(), "Company name not specified");
        }
        ClaimantIndType indType = caseData.getClaimantIndType();
        if (indType != null) {
            String firstName = defaultString(indType.getClaimantFirstNames(), "");
            String lastName = defaultString(indType.getClaimantLastName(), "");
            String fullName = (firstName + " " + lastName).trim();
            if (StringUtils.isNotBlank(fullName)) {
                return fullName;
            }
        }
        return defaultString(caseData.getClaimant(), NOT_SPECIFIED);
    }

    private String extractAddress(Address address) {
        if (address == null) {
            return "Address not entered";
        }
        StringBuilder addr = new StringBuilder();
        if (StringUtils.isNotBlank(address.getAddressLine1())) {
            addr.append(address.getAddressLine1());
        }
        if (StringUtils.isNotBlank(address.getPostTown())) {
            if (!addr.isEmpty()) {
                addr.append(", ");
            }
            addr.append(address.getPostTown());
        }
        if (StringUtils.isNotBlank(address.getPostCode())) {
            if (!addr.isEmpty()) {
                addr.append(", ");
            }
            addr.append(address.getPostCode());
        }
        return addr.isEmpty() ? "Address not entered" : addr.toString();
    }

    private String defaultString(String value, String fallback) {
        return Optional.ofNullable(value).filter(StringUtils::isNotBlank).orElse(fallback);
    }
}
