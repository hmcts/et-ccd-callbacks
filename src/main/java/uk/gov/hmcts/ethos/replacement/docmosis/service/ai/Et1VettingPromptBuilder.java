package uk.gov.hmcts.ethos.replacement.docmosis.service.ai;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
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

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.COMPANY;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Et1VettingHelper.formatDateDdMmYyyy;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Et1VettingHelper.normalizeDatesToDdMmYyyy;

@Component
public class Et1VettingPromptBuilder {

    private static final String NOT_SPECIFIED = "Not specified";
    private static final Map<JurisdictionCode, String> ENUM_TO_CCD_CODE = Map.of(
            JurisdictionCode.ADTST, "ADT(ST)",
            JurisdictionCode.RPTS, "RPT(S)",
            JurisdictionCode.RTRST, "RTR(ST)",
            JurisdictionCode.TXCST, "TXC(ST)",
            JurisdictionCode.WTRAL, "WTR(AL)"
    );

    /**
     * Builds a comprehensive prompt containing case facts, attached document references, and vetting instructions.
     *
     * @param caseDetails CaseDetails object from CCD
     * @param attachments List of attached files sent as multimodal parts to Gemini
     * @return Complete prompt for Gemini
     */
    public String buildPrompt(CaseDetails caseDetails, List<AiDocumentAttachment> attachments) {
        return buildPrompt(caseDetails, attachments, List.of());
    }

    /**
     * Builds a comprehensive prompt containing case facts, attached document references,
     * available hearing venues for the office, and vetting instructions.
     *
     * @param caseDetails CaseDetails object from CCD
     * @param attachments List of attached files sent as multimodal parts to Gemini
     * @param availableVenues Valid hearing venue names for the managing office
     * @return Complete prompt for Gemini
     */
    public String buildPrompt(CaseDetails caseDetails,
                              List<AiDocumentAttachment> attachments,
                              List<String> availableVenues) {
        CaseData caseData = caseDetails.getCaseData();
        StringBuilder sb = new StringBuilder(12_288);

        sb.append("You are the \"ET Jurisdiction Vetting Bot\" and expert ")
            .append("UK Employment Tribunal Case Officer Co-Pilot.\n")
            .append("You assist tribunal clerks by vetting ET1 claims and identifying official jurisdiction codes.\n")
            .append("You do NOT assess legal merits or provide legal advice.\n\n")
            .append("=== CLAIM FACTS ===\n")
            .append("Case Reference: ")
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
        appendExistingDocuments(sb, caseData);
        appendVettingBotRules(sb);

        return sb.toString();
    }

    private void appendEmploymentDetails(StringBuilder sb, CaseData caseData) {
        ClaimantOtherType otherType = caseData.getClaimantOtherType();
        sb.append("\n-- Employment Details --\n");
        if (otherType != null) {
            sb.append("Start Date: ")
                .append(formatDateDdMmYyyy(defaultString(otherType.getClaimantEmployedFrom(), NOT_SPECIFIED)))
                .append("\nEnd Date: ")
                .append(formatDateDdMmYyyy(defaultString(otherType.getClaimantEmployedTo(), NOT_SPECIFIED)))
                .append("\nStill Working: ")
                .append(defaultString(otherType.getClaimantEmployedCurrently(), NOT_SPECIFIED))
                .append("\nJob Title: ").append(defaultString(otherType.getClaimantOccupation(), NOT_SPECIFIED))
                .append("\nAverage Weekly Hours: ")
                .append(defaultString(otherType.getClaimantAverageWeeklyHours(), NOT_SPECIFIED))
                .append("\nPay Before Tax: ")
                .append(defaultString(otherType.getClaimantPayBeforeTax(), NOT_SPECIFIED))
                .append("\nPay After Tax: ")
                .append(defaultString(otherType.getClaimantPayAfterTax(), NOT_SPECIFIED))
                .append("\nPay Cycle: ")
                .append(defaultString(otherType.getClaimantPayCycle(), NOT_SPECIFIED))
                .append("\nNotice Period: ")
                .append(defaultString(otherType.getClaimantNoticePeriod(), NOT_SPECIFIED))
                .append('\n');
        } else {
            sb.append("Start Date: ")
                .append(formatDateDdMmYyyy(defaultString(caseData.getClaimantStartDate(), NOT_SPECIFIED)))
                .append("\nEnd Date: ")
                .append(formatDateDdMmYyyy(defaultString(caseData.getClaimantEndDate(), NOT_SPECIFIED)))
                .append("\nStill Working: ")
                .append(caseData.getClaimantStillWorking() != null
                    ? caseData.getClaimantStillWorking() : NOT_SPECIFIED)
                .append("\nJob Title: ").append(defaultString(caseData.getClaimantJobTitle(), NOT_SPECIFIED))
                .append('\n');
        }
    }

    private void appendHearingPreferences(StringBuilder sb, CaseData caseData) {
        ClaimantHearingPreference preference = caseData.getClaimantHearingPreference();
        sb.append("\n-- Hearing Preferences --\n");
        if (preference != null) {
            sb.append("Hearing Options: ")
                .append(preference.getHearingPreferences() != null
                    ? preference.getHearingPreferences() : NOT_SPECIFIED)
                .append("\nReasonable Adjustments: ")
                .append(defaultString(preference.getReasonableAdjustments(), NOT_SPECIFIED))
                .append("\nReasonable Adjustments Details: ")
                .append(defaultString(preference.getReasonableAdjustmentsDetail(), NOT_SPECIFIED))
                .append("\nContact Language: ")
                .append(defaultString(preference.getContactLanguage(), NOT_SPECIFIED))
                .append("\nHearing Language: ")
                .append(defaultString(preference.getHearingLanguage(), NOT_SPECIFIED))
                .append("\nPanel Preference: ")
                .append(defaultString(preference.getClaimantHearingPanelPreference(), NOT_SPECIFIED))
                .append("\nPanel Preference Reason: ")
                .append(defaultString(preference.getClaimantHearingPanelPreferenceWhy(), NOT_SPECIFIED))
                .append('\n');
        } else {
            sb.append("None provided\n");
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
            sb.append("Respondent ").append(idx).append(":\n")
                .append("  Name: ").append(defaultString(resp.getRespondentName(), NOT_SPECIFIED)).append('\n')
                .append("  Address: ").append(extractAddress(resp.getRespondentAddress())).append('\n')
                .append("  ACAS Certificate Question: ")
                .append(defaultString(resp.getRespondentAcasQuestion(), NOT_SPECIFIED)).append('\n')
                .append("  ACAS Certificate Number: ")
                .append(defaultString(resp.getRespondentAcas(), NOT_SPECIFIED)).append('\n')
                .append("  ACAS Exemption Reason: ")
                .append(defaultString(resp.getRespondentAcasNo(), NOT_SPECIFIED)).append('\n');
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

        List<String> types = CollectionUtils.isNotEmpty(caseData.getTypesOfClaim())
            ? caseData.getTypesOfClaim()
            : (CollectionUtils.isNotEmpty(caseData.getTypeOfClaim())
            ? caseData.getTypeOfClaim()
            : caseData.getEt1SectionThreeTypeOfClaim());
        sb.append("Types of Claim: ").append(CollectionUtils.isNotEmpty(types) ? types : NOT_SPECIFIED).append('\n');

        ClaimantRequestType requests = caseData.getClaimantRequests();

        List<String> discriminations = requests != null
            && CollectionUtils.isNotEmpty(requests.getDiscriminationClaims())
            ? requests.getDiscriminationClaims()
            : caseData.getDiscriminationTypesOfClaim();
        sb.append("Discrimination Claims: ")
            .append(CollectionUtils.isNotEmpty(discriminations) ? discriminations : NOT_SPECIFIED).append('\n');

        List<String> payClaims = requests != null && CollectionUtils.isNotEmpty(requests.getPayClaims())
            ? requests.getPayClaims()
            : caseData.getPayTypesOfClaim();
        sb.append("Pay Claims: ")
            .append(CollectionUtils.isNotEmpty(payClaims) ? payClaims : NOT_SPECIFIED).append('\n');

        String whistleblowing = requests != null && StringUtils.isNotBlank(requests.getWhistleblowing())
            ? requests.getWhistleblowing()
            : (CollectionUtils.isNotEmpty(caseData.getWhistleblowingYesNo())
            ? caseData.getWhistleblowingYesNo().toString() : NOT_SPECIFIED);
        sb.append("Whistleblowing: ").append(whistleblowing).append('\n');

        String wbAuthority = requests != null && StringUtils.isNotBlank(requests.getWhistleblowingAuthority())
            ? requests.getWhistleblowingAuthority()
            : defaultString(caseData.getWhistleblowingRegulator(), NOT_SPECIFIED);
        sb.append("Whistleblowing Regulator / Authority: ").append(wbAuthority).append('\n');

        String otherClaim = requests != null && StringUtils.isNotBlank(requests.getOtherClaim())
            ? requests.getOtherClaim()
            : defaultString(caseData.getOtherTypeOfClaimDetails(), NOT_SPECIFIED);
        sb.append("Other Claim Details: ").append(otherClaim).append('\n');

        if (requests != null) {
            if (StringUtils.isNotBlank(requests.getClaimantCompensationAmount())) {
                sb.append("Compensation Amount Claimed: £")
                    .append(requests.getClaimantCompensationAmount()).append('\n');
            }
            if (StringUtils.isNotBlank(requests.getClaimantCompensationText())) {
                sb.append("Compensation Details: ").append(requests.getClaimantCompensationText()).append('\n');
            }
            if (CollectionUtils.isNotEmpty(requests.getClaimOutcome())) {
                sb.append("Remedies / Outcomes Sought: ").append(requests.getClaimOutcome()).append('\n');
            }
            if (StringUtils.isNotBlank(requests.getClaimantTribunalRecommendation())) {
                sb.append("Tribunal Recommendations Sought: ")
                    .append(requests.getClaimantTribunalRecommendation()).append('\n');
            }
            if (StringUtils.isNotBlank(requests.getLinkedCases())) {
                sb.append("Linked Cases Stated: ").append(requests.getLinkedCases())
                    .append(" - ").append(defaultString(requests.getLinkedCasesDetail(), "")).append('\n');
            }
        }

        String lastEvent = caseData.getClaimantOtherType() != null
            && StringUtils.isNotBlank(caseData.getClaimantOtherType().getDateOfLastEvent())
            ? caseData.getClaimantOtherType().getDateOfLastEvent()
            : defaultString(caseData.getEt1SectionThreeDateOfLastEvent(), NOT_SPECIFIED);
        sb.append("Date of Last Event: ").append(formatDateDdMmYyyy(lastEvent)).append('\n');

        String claimNarrative = requests != null && StringUtils.isNotBlank(requests.getClaimDescription())
            ? requests.getClaimDescription()
            : caseData.getEt1SectionThreeClaimDetails();
        sb.append("Claim Narrative Text:\n")
            .append(defaultString(claimNarrative, "No narrative text supplied")).append('\n');
    }

    private void appendAttachedFilesNotice(StringBuilder sb, List<AiDocumentAttachment> attachments) {
        if (CollectionUtils.isNotEmpty(attachments)) {
            sb.append("\n-- Attached Claim & ACAS Documents (Multimodal Input) --\n");
            for (AiDocumentAttachment attachment : attachments) {
                sb.append("Attached Document: ").append(attachment.getFilename())
                    .append(" (MIME: ").append(attachment.getMimeType()).append(")\n");
            }
            sb.append("NOTE: All ").append(attachments.size()).append(" file(s) listed above are attached directly ")
                .append("to this request (e.g. ET1 form, Particulars of Claim, and ACAS Early Conciliation ")
                .append("certificates). Cross-check ALL attached documents alongside the typed Claim Narrative Text ")
                .append("above to verify claimant/respondent names, ACAS EC numbers, Day A/Day B dates, ")
                .append("dates of events, remedy requests, and causes of action for Rule 13, ERA, and vetting.\n");
        }
    }

    private void appendExistingDocuments(StringBuilder sb, CaseData caseData) {
        sb.append("\n-- Uploaded Documents --\n");
        List<DocumentTypeItem> docs = caseData.getDocumentCollection();
        if (CollectionUtils.isNotEmpty(docs)) {
            for (DocumentTypeItem item : docs) {
                if (item.getValue() != null && item.getValue().getUploadedDocument() != null) {
                    sb.append("Document: ").append(item.getValue().getTypeOfDocument())
                        .append(" - ")
                        .append(item.getValue().getUploadedDocument().getDocumentFilename())
                        .append('\n');
                }
            }
        } else {
            sb.append("No documents in collection\n");
        }
    }

    private void appendVettingBotRules(StringBuilder sb) {
        sb.append("\n=== ET VETTING BOT RULES ===\n")
            .append("1. EXPLICIT CLAIM PRIORITY RULE (CRITICAL):\n")
            .append("Where the document explicitly states claims (\"I bring claims for...\", ")
            .append("\"The claimant claims...\"), extract and classify those claims first. ")
            .append("Explicit claims override clustering logic.\n")
            .append("2. CLAIM VS CONTEXT DISTINCTION:\n")
            .append("Classify asserted legal rights, remedy requested, or wrongdoing alleged.\n")
            .append("Do NOT assign codes to background context (e.g. employment status, zero hours, TUPE mentioned ")
            .append("without breach). If potentially relevant but unclear, refer to Legal Officer.\n")
            .append("3. ALL OFFICIAL JURISDICTION CODES (STRICT - DO NOT INVENT CODES; SELECT FROM ALL BELOW):\n");
        appendAllJurisdictionCodes(sb);
        sb.append("4. SUBSTANTIVE DEFECT CODES — Rule 13 (use EXACT backend codes below; note Rule 12 was ")
            .append("renumbered to Rule 13 and Rule 50 to Rule 49, so ALWAYS refer to Rule 13 / Rule 49 in text):\n")
            .append("   rule121a: The tribunal has no jurisdiction to consider - Rule 13(1)(a)\n")
            .append("   rule121b: Form cannot sensibly be responded to or abuse of process - Rule 13(1)(b)\n")
            .append("   rule121c: Neither an EC number nor an EC exemption - Rule 13(1)(c)\n")
            .append("   rule121d: Claims EC exception applies but it might not - Rule 13(1)(d)\n")
            .append("   rule121 da: EC number on ET1 does not match ACAS certificate - Rule 13(1)(e)\n")
            .append("   rule121e: Claimant name on ET1 does not match ACAS certificate - Rule 13(1)(f)\n")
            .append("   rule121f: Respondent name on ET1 does not match ACAS certificate - Rule 13(1)(g)\n")
            .append("5. REFERRAL CODES (use exact codes in list fields):\n")
            .append("   - referralsToJudge: aClaimOfInterimRelief, aStatutoryAppeal, ")
            .append("anAllegationOfCommissionOfSexualOffence, insolvency, jurisdictionsUnclear, lengthOfService, ")
            .append("potentiallyLinkedCasesInTheEcm, rule50Issues, anotherReasonForJudicialReferral\n")
            .append("   - referralsToRejOrVp: vexatiousLitigantOrder, aNationalSecurityIssue, ")
            .append("nationalMultipleOrPresidentialOrder, transferToOtherRegion, serviceAbroad, aSensitiveIssue, ")
            .append("anyPotentialConflict, anotherReasonREJOrVP\n")
            .append("   - otherReferrals: claimOutOfTime, multipleClaim, employmentStatusIssues, ")
            .append("pidJurisdictionRegulator, videoHearingPreference, ")
            .append("rule50IssuesOtherFactors, otherRelevantFactors\n")
            .append("6. MANDATORY REASONING RULE:\n")
            .append("   For EVERY defect or referral ticked in defectsList, referralsToJudge, referralsToRejOrVp, ")
            .append("or otherReferrals, you MUST provide clear factual details in the corresponding *Notes or ")
            .append("*TextArea field. The tribunal system enforces mandatory text fields whenever a checkbox ")
            .append("is ticked.\n")
            .append("7. UNCERTAINTIES & ITEMS REQUIRING DOUBLE-CHECK:\n")
            .append("   If you are unable to answer any vetting question with confidence, missing information ")
            .append("prevents a definitive answer, or something requires manual double-checking by a ")
            .append("caseworker/judge (e.g. unclear dates, ERA applicability, missing ACAS details, hearing ")
            .append("venue choice, ambiguous employment status, or conflicts between text and attached ")
            .append("documents), list each point clearly in \"itemsRequiringReview\". Omit/return [] only if ")
            .append("all items are completely clear.\n")
            .append("8. DATE FORMAT RULE (STRICT):\n")
            .append("   ALWAYS format every date across ALL summary, review, defect, and note fields as ")
            .append("dd-MM-yyyy (e.g. 09-11-2026). NEVER output dates in yyyy-MM-dd format.\n\n")
            .append("Produce a JSON response matching this schema:\n")
            .append("{\n")
            .append("  \"canServeClaim\": {\"canServe\": \"Yes|No\", \"reason\": \"...\", \"notes\": \"...\"},\n")
            .append("  \"acasAssessments\": [{\"respondentNumber\": 1, \"hasCertificate\": \"Yes|No\", ")
            .append("\"isExempt\": \"Yes|No\", \"notes\": \"...\"}],\n")
            .append("  \"et1VettingEra\": \"Yes|No|Not applicable\",\n")
            .append("  \"substantiveDefects\": {\"defectsList\": [\"rule121b\"], ")
            .append("\"rule121aTextArea\": \"...\", \"rule121bTextArea\": \"...\", ")
            .append("\"rule121cTextArea\": \"...\", \"rule121dTextArea\": \"...\", ")
            .append("\"rule121daTextArea\": \"...\", \"rule121eTextArea\": \"...\", ")
            .append("\"rule121fTextArea\": \"...\", \"generalNotes\": \"...\"},\n")
            .append("  \"jurisdictionAssessment\": {\"areCodesCorrect\": \"Yes|No\", ")
            .append("\"suggestedJurisdictionCodes\": [\"UDL\", \"BOC\"], \"details\": \"...\", ")
            .append("\"generalNotes\": \"...\"},\n")
            .append("  \"trackAllocation\": {\"isTrackCorrect\": \"Yes|No\", ")
            .append("\"suggestedTrack\": \"Open|Standard|Short|No track\", \"whyChangeTrackAllocation\": \"...\", ")
            .append("\"generalNotes\": \"...\"},\n")
            .append("  \"locationAndVenue\": {\"isLocationCorrect\": \"Yes|No\", \"suggestedOffice\": \"...\", ")
            .append("\"whyChangeOffice\": \"...\", \"generalNotes\": \"...\", ")
            .append("\"suggestHearingVenue\": \"Yes|No\", ")
            .append("\"suggestedHearingVenue\": \"Exact venue from Available Hearing Venues list\", ")
            .append("\"hearingVenueGeneralNotes\": \"...\"},\n")
            .append("  \"furtherQuestions\": {\"isGovOrMajorEmployer\": \"Yes|No\", ")
            .append("\"hasReasonableAdjustments\": \"Yes|No\", \"reasonableAdjustmentsDetails\": \"...\", ")
            .append("\"videoHearingPreference\": \"Yes|No\", \"videoHearingDetails\": \"...\", ")
            .append("\"generalNotes\": \"...\"},\n")
            .append("  \"judicialReferrals\": {\"referralsToJudge\": [\"lengthOfService\"], ")
            .append("\"claimOfInterimReliefNotes\": \"...\", \"statutoryAppealNotes\": \"...\", ")
            .append("\"sexualOffenceNotes\": \"...\", \"insolvencyNotes\": \"...\", ")
            .append("\"jurisdictionsUnclearNotes\": \"...\", \"lengthOfServiceNotes\": \"...\", ")
            .append("\"potentiallyLinkedCasesNotes\": \"...\", \"rule50IssuesNotes\": \"...\", ")
            .append("\"anotherReasonNotes\": \"...\", \"generalNotes\": \"...\"},\n")
            .append("  \"rejOrVpReferrals\": {\"referralsToRejOrVp\": [\"serviceAbroad\"], ")
            .append("\"vexatiousLitigantOrderNotes\": \"...\", \"nationalSecurityNotes\": \"...\", ")
            .append("\"nationalMultipleNotes\": \"...\", \"transferToOtherRegionNotes\": \"...\", ")
            .append("\"serviceAbroadNotes\": \"...\", \"sensitiveIssueNotes\": \"...\", ")
            .append("\"potentialConflictNotes\": \"...\", \"anotherReasonNotes\": \"...\", ")
            .append("\"generalNotes\": \"...\"},\n")
            .append("  \"otherReferrals\": {\"otherReferrals\": [\"claimOutOfTime\", \"videoHearingPreference\"], ")
            .append("\"claimOutOfTimeNotes\": \"...\", \"multipleClaimNotes\": \"...\", ")
            .append("\"employmentStatusIssuesNotes\": \"...\", \"pidRegulatorNotes\": \"...\", ")
            .append("\"videoHearingNotes\": \"...\", \"rule50Notes\": \"...\", ")
            .append("\"anotherReasonNotes\": \"...\", \"generalNotes\": \"...\"},\n")
            .append("  \"confidence\": \"HIGH|MEDIUM|LOW\",\n")
            .append("  \"executiveSummaryMarkdown\": \"### ET1 Vetting Summary\\n- **Claimant**: ...\\n")
            .append("- **Respondents**: ...\\n- **Jurisdiction Codes**: ...\\n- **Track**: ...\\n\\n")
            .append("### Key Issues Identified\\n- **Issue**: ...\",\n")
            .append("  \"itemsRequiringReview\": [\"Specific question or point needing manual verification...\"]\n")
            .append("}\n");
    }

    private void appendAllJurisdictionCodes(StringBuilder sb) {
        for (JurisdictionCode code : JurisdictionCode.values()) {
            String ccdCode = ENUM_TO_CCD_CODE.getOrDefault(code, code.name());
            sb.append("   - ").append(ccdCode).append(": ").append(code.getDescription()).append('\n');
        }
        sb.append("   - EQP: Equal pay claim\n")
            .append("   - TBA: Jurisdiction code to be confirmed / allocated\n")
            .append("   - ZNON: No applicable jurisdiction code\n");
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
