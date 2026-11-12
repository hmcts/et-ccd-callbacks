package uk.gov.hmcts.ethos.replacement.docmosis.service.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.client.CcdSubmitEventParams;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.JurCodesTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.VettingJurCodesTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.et.common.model.ccd.types.VettingJurisdictionCodesType;
import uk.gov.hmcts.ethos.replacement.docmosis.config.ai.GeminiProperties;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ai.AiDocumentAttachment;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ai.Et1AiVettingResult;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.jpaservice.JpaVenueService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.EMPLOYMENT;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.ACAS_CERTIFICATE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Et1VettingHelper.DD_MM_YYYY_DATE_FORMAT;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Et1VettingHelper.normalizeDatesToDdMmYyyy;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.EmploymentRightsActService.NOT_APPLICABLE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.TribunalOfficesService.UNASSIGNED_OFFICE;

@Slf4j
@Service
public class Et1AiVettingService {

    private static final String AI_NOTE_PREFIX = "[AI Suggestion] ";
    private static final String ET1_VETTING_EVENT_ID = "et1Vetting";
    private static final Set<String> VALID_JURISDICTION_CODES = Set.of(
            "ADT", "ADG", "ADT(ST)", "APA", "AWR", "BOC", "CCP", "COM", "DAG", "DDA", "DOD", "DRB", "DSO",
            "EAP", "ECC", "EQP", "FCT", "FLW", "FML", "FPA", "FPI", "FT1", "FTC", "FTE", "FTO", "FTP",
            "FTR", "FTS", "FTU", "FWP", "FWS", "GRA", "HAS", "HSD", "HSR", "IRF", "ISV", "LEV ", "LSO",
            "MAT", "MWA", "MWD", "NNA", "PAC", "PAY", "PEN", "PID", "PLD", "PTE", "RPT", "RPT(S) ", "RRD",
            "RTR", "RTR(ST)", "SUN", "SXD", "SXH", "TBA", "TIP", "TPE", "TT", "TUA", "TUD", "TUE", "TUI",
            "TUM", "TUR", "TUS", "TXC", "TXC(ST)", "UDC", "UDL", "UIA", "VIC", "WA", "WTA", "WTR",
            "WTR(AL)", "ZNON"
    );
    private static final Map<String, String> JURISDICTION_ALIASES = Map.of(
            "EQU", "EQP",
            "IR", "IRF",
            "SEX", "SXD",
            "DGR", "GRA",
            "DPM", "MAT",
            "LEV", "LEV ",
            "RPT(S)", "RPT(S) "
    );

    private final GeminiProperties geminiProperties;
    private final GeminiClient geminiClient;
    private final Et1VettingPromptBuilder promptBuilder;
    private final Et1DocumentReaderService documentReaderService;
    private final ObjectMapper objectMapper;
    private final CcdClient ccdClient;
    private final AdminUserService adminUserService;
    private final JpaVenueService jpaVenueService;
    private final TaskExecutor taskExecutor;
    private final Set<String> inFlightAutoVets = ConcurrentHashMap.newKeySet();
    private final ConcurrentMap<String, Et1AiVettingResult> pendingAutoVetResults = new ConcurrentHashMap<>();

    public Et1AiVettingService(GeminiProperties geminiProperties,
                               GeminiClient geminiClient,
                               Et1VettingPromptBuilder promptBuilder,
                               Et1DocumentReaderService documentReaderService,
                               ObjectMapper objectMapper) {
        this(geminiProperties, geminiClient, promptBuilder, documentReaderService, objectMapper,
                null, null, null, null);
    }

    @Autowired
    public Et1AiVettingService(
            GeminiProperties geminiProperties,
            GeminiClient geminiClient,
            Et1VettingPromptBuilder promptBuilder,
            Et1DocumentReaderService documentReaderService,
            ObjectMapper objectMapper,
            @Autowired(required = false) CcdClient ccdClient,
            @Autowired(required = false) AdminUserService adminUserService,
            @Autowired(required = false) JpaVenueService jpaVenueService,
            @Autowired(required = false) @Qualifier("applicationTaskExecutor") TaskExecutor taskExecutor) {
        this.geminiProperties = geminiProperties;
        this.geminiClient = geminiClient;
        this.promptBuilder = promptBuilder;
        this.documentReaderService = documentReaderService;
        this.objectMapper = objectMapper;
        this.ccdClient = ccdClient;
        this.adminUserService = adminUserService;
        this.jpaVenueService = jpaVenueService;
        this.taskExecutor = taskExecutor;
    }

    /**
     * Triggers AI ET1 vetting asynchronously on case submission so the submission response
     * is not blocked, then triggers and submits the {@code et1Vetting} event in CCD using
     * the system account.
     *
     * @param caseDetails CaseDetails object from CCD
     * @param userToken user authorization token for DM Store document download
     */
    public void vetClaimInBackground(CaseDetails caseDetails, String userToken) {
        if (!geminiProperties.isEnabled() || StringUtils.isBlank(geminiProperties.getApiKey())) {
            log.info("AI vetting is not enabled or API key is not configured; skipping background vetting.");
            return;
        }
        if (caseDetails == null || caseDetails.getCaseData() == null || StringUtils.isBlank(caseDetails.getCaseId())) {
            return;
        }
        if (UNASSIGNED_OFFICE.equalsIgnoreCase(caseDetails.getCaseData().getManagingOffice())) {
            log.info("Managing office is Unassigned for case {}; skipping automatic ET1 vetting trigger.",
                    caseDetails.getCaseId());
            return;
        }
        if (ccdClient == null || adminUserService == null) {
            log.debug("CcdClient or AdminUserService unavailable; skipping automatic ET1 vetting trigger.");
            return;
        }

        String caseId = caseDetails.getCaseId();
        if (!inFlightAutoVets.add(caseId)) {
            log.info("Automated AI vetting already in progress for case {}, skipping duplicate trigger.", caseId);
            return;
        }

        String resolvedToken = StringUtils.isNotBlank(userToken) ? userToken : resolveRequestAuthToken();
        Executor executor = taskExecutor != null ? taskExecutor : ForkJoinPool.commonPool();

        log.info("Scheduling background automated AI ET1 vetting for case {}", caseId);
        CompletableFuture.runAsync(() -> autoVetAndSubmitCase(caseDetails, resolvedToken), executor)
                .whenComplete((res, ex) -> inFlightAutoVets.remove(caseId));
    }

    /**
     * Vets the ET1 claim using AI (or reuses an in-progress automated vetting result during
     * system-triggered {@code startEventForCase}) and pre-populates CaseData vetting fields.
     *
     * @param caseDetails CaseDetails object from CCD
     * @param userToken user authorization token for DM Store
     */
    public void vetClaimAndPrepopulate(CaseDetails caseDetails, String userToken) {
        if (!geminiProperties.isEnabled()) {
            log.info("AI vetting is not enabled.");
            return;
        }
        if (caseDetails == null || caseDetails.getCaseData() == null) {
            return;
        }

        String caseId = caseDetails.getCaseId();
        if (StringUtils.isNotBlank(caseId)) {
            Et1AiVettingResult pendingResult = pendingAutoVetResults.remove(caseId);
            if (pendingResult != null) {
                log.info("Applying pre-computed automated AI vetting result to case {}", caseId);
                applyVettingResult(caseDetails.getCaseData(), pendingResult);
                return;
            }
        }

        runGeminiAiVetting(caseDetails, userToken).ifPresent(result -> {
            applyVettingResult(caseDetails.getCaseData(), result);
            log.info("Successfully applied AI vetting suggestions to case {}", caseId);
        });
    }

    private void autoVetAndSubmitCase(CaseDetails submittedCaseDetails, String submissionUserToken) {
        String caseId = submittedCaseDetails.getCaseId();
        String caseTypeId = submittedCaseDetails.getCaseTypeId();
        if (StringUtils.isBlank(caseTypeId)) {
            log.warn("Missing caseTypeId for case {}; cannot trigger ET1 vetting event.", caseId);
            return;
        }

        try {
            String adminToken = adminUserService.getAdminUserToken();
            String docToken = StringUtils.isNotBlank(submissionUserToken) ? submissionUserToken : adminToken;

            Optional<Et1AiVettingResult> maybeResult = runGeminiAiVetting(submittedCaseDetails, docToken);
            if (maybeResult.isEmpty()) {
                log.warn("AI vetting produced no result for case {}; skipping automatic ET1 vetting submission.",
                        caseId);
                return;
            }

            Et1AiVettingResult result = maybeResult.get();
            pendingAutoVetResults.put(caseId, result);
            try {
                CCDRequest ccdRequest = ccdClient.startEventForCase(
                        adminToken, caseTypeId, EMPLOYMENT, caseId, ET1_VETTING_EVENT_ID);
                CaseDetails eventCaseDetails = ccdRequest.getCaseDetails();
                if (eventCaseDetails == null || eventCaseDetails.getCaseData() == null) {
                    log.warn("CCD returned empty case details when starting et1Vetting for case {}", caseId);
                    return;
                }

                CaseData eventCaseData = eventCaseDetails.getCaseData();
                if (pendingAutoVetResults.remove(caseId) != null) {
                    applyVettingResult(eventCaseData, result);
                }

                CcdSubmitEventParams submitParams = CcdSubmitEventParams.builder()
                        .authToken(adminToken)
                        .caseId(caseId)
                        .caseTypeId(caseTypeId)
                        .jurisdiction(EMPLOYMENT)
                        .caseData(eventCaseData)
                        .ccdRequest(ccdRequest)
                        .eventSummary("AI Automated ET1 Vetting")
                        .eventDescription("ET1 vetting triggered and submitted automatically using system account")
                        .build();
                ccdClient.submitEventForCase(submitParams);
                log.info("Successfully triggered and submitted ET1 Vetting event via system account for case {}",
                        caseId);
            } finally {
                pendingAutoVetResults.remove(caseId);
            }
        } catch (Exception ex) {
            log.error("Failed to automatically trigger ET1 Vetting for case {}: {}", caseId, ex.getMessage(), ex);
        }
    }

    private Optional<Et1AiVettingResult> runGeminiAiVetting(CaseDetails caseDetails, String userToken) {
        if (StringUtils.isBlank(geminiProperties.getApiKey())) {
            log.info("API key is not configured; cannot run AI vetting for case {}", caseDetails.getCaseId());
            return Optional.empty();
        }
        try {
            log.info("Running AI ET1 Vetting Co-Pilot for case {}", caseDetails.getCaseId());
            List<AiDocumentAttachment> attachments = documentReaderService.findClaimDocumentAttachments(
                    caseDetails.getCaseData(), userToken);
            List<String> availableVenues = resolveAvailableVenueNames(caseDetails.getCaseData());
            String systemInstruction = promptBuilder.buildSystemInstruction();
            String casePrompt = promptBuilder.buildCasePrompt(caseDetails, attachments, availableVenues);
            String rawJson = geminiClient.generateContent(systemInstruction, casePrompt, attachments);

            if (StringUtils.isBlank(rawJson)) {
                log.warn("AI returned empty response for case {}", caseDetails.getCaseId());
                return Optional.empty();
            }

            return parseVettingResult(rawJson, caseDetails.getCaseId());
        } catch (Exception ex) {
            log.error("Failed to run AI vetting for case {}: {}", caseDetails.getCaseId(), ex.getMessage(), ex);
            return Optional.empty();
        }
    }

    private Optional<Et1AiVettingResult> parseVettingResult(String rawJson, String caseId)
            throws JsonProcessingException {
        JsonNode root = objectMapper.readTree(rawJson);
        if (root == null) {
            return Optional.empty();
        }
        if (root.isArray()) {
            if (root.isEmpty()) {
                log.warn("AI returned empty JSON array for case {}", caseId);
                return Optional.empty();
            }
            if (root.size() > 1) {
                log.warn("AI returned JSON array of size {} for case {}; using first object",
                        root.size(), caseId);
            }
            root = root.get(0);
        }
        if (!root.isObject()) {
            log.warn("AI returned non-object JSON root ({}) for case {}", root.getNodeType(), caseId);
            return Optional.empty();
        }
        return Optional.of(objectMapper.treeToValue(root, Et1AiVettingResult.class));
    }

    private String resolveRequestAuthToken() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes servletAttrs) {
            return servletAttrs.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
        }
        return null;
    }

    private void applyVettingResult(CaseData caseData, Et1AiVettingResult result) {
        populateCanServeClaim(caseData, result);
        populateAcasAssessments(caseData, result);
        populateEraAssessment(caseData, result);
        populateSubstantiveDefects(caseData, result);
        populateJurisdictionCodes(caseData, result);
        populateTrackAllocation(caseData, result);
        populateLocationAndVenue(caseData, result);
        populateFurtherQuestions(caseData, result);
        populateJudicialReferrals(caseData, result);
        populateRejOrVpReferrals(caseData, result);
        populateOtherReferrals(caseData, result);
        populateAdditionalInformationAndBanner(caseData, result);
    }

    private void populateEraAssessment(CaseData caseData, Et1AiVettingResult result) {
        if (StringUtils.isBlank(caseData.getEt1VettingEraAssessmentMarkUp())
                || StringUtils.isNotBlank(caseData.getEt1VettingEra())) {
            return;
        }
        String sanitized = sanitizeYesNoNotApplicable(result.getEt1VettingEra());
        if (sanitized != null) {
            caseData.setEt1VettingEra(sanitized);
        }
    }

    private void populateCanServeClaim(CaseData caseData, Et1AiVettingResult result) {
        if (result.getCanServeClaim() == null) {
            return;
        }
        if (StringUtils.isBlank(caseData.getEt1VettingCanServeClaimYesOrNo())) {
            caseData.setEt1VettingCanServeClaimYesOrNo(sanitizeYesNo(result.getCanServeClaim().getCanServe()));
        }
        if ("No".equalsIgnoreCase(caseData.getEt1VettingCanServeClaimYesOrNo())
                && StringUtils.isBlank(caseData.getEt1VettingCanServeClaimNoReason())) {
            caseData.setEt1VettingCanServeClaimNoReason(resolveNote(result.getCanServeClaim().getReason(),
                    AI_NOTE_PREFIX + "Service requirements not met based on respondent address or party details."));
        }
        if (StringUtils.isNotBlank(result.getCanServeClaim().getNotes())
                && StringUtils.isBlank(caseData.getEt1VettingCanServeClaimGeneralNote())) {
            caseData.setEt1VettingCanServeClaimGeneralNote(formatAiNote(result.getCanServeClaim().getNotes()));
        }
    }

    private void populateAcasAssessments(CaseData caseData, Et1AiVettingResult result) {
        if (CollectionUtils.isNotEmpty(result.getAcasAssessments())) {
            for (Et1AiVettingResult.AcasAssessment assessment : result.getAcasAssessments()) {
                setAcasFieldsForRespondent(caseData, assessment);
            }
        }
        appendMissingAcasDocumentWarningNote(caseData);
    }

    private void appendMissingAcasDocumentWarningNote(CaseData caseData) {
        if (hasAcasCertificateDocument(caseData) || !hasRespondentAcasDates(caseData)) {
            return;
        }
        String warning = "No ACAS Certificate document is attached; respondent ACAS dates were used and "
                + "the actual certificate needs to be checked.";
        String existing = caseData.getEt1VettingAcasCertGeneralNote();
        if (StringUtils.isBlank(existing)) {
            caseData.setEt1VettingAcasCertGeneralNote(AI_NOTE_PREFIX + warning);
        } else if (!existing.toLowerCase(Locale.ROOT).contains("actual certificate")) {
            caseData.setEt1VettingAcasCertGeneralNote(existing + " " + warning);
        }
    }

    private void setAcasFieldsForRespondent(CaseData caseData, Et1AiVettingResult.AcasAssessment assessment) {
        int num = assessment.getRespondentNumber();
        switch (num) {
            case 1:
                setAcasCertValues(caseData, assessment,
                        caseData.getEt1VettingAcasCertIsYesOrNo1(), caseData.getEt1VettingAcasCertExemptYesOrNo1(), 1);
                break;
            case 2:
                setAcasCertValues(caseData, assessment,
                        caseData.getEt1VettingAcasCertIsYesOrNo2(), caseData.getEt1VettingAcasCertExemptYesOrNo2(), 2);
                break;
            case 3:
                setAcasCertValues(caseData, assessment,
                        caseData.getEt1VettingAcasCertIsYesOrNo3(), caseData.getEt1VettingAcasCertExemptYesOrNo3(), 3);
                break;
            case 4:
                setAcasCertValues(caseData, assessment,
                        caseData.getEt1VettingAcasCertIsYesOrNo4(), caseData.getEt1VettingAcasCertExemptYesOrNo4(), 4);
                break;
            case 5:
                setAcasCertValues(caseData, assessment,
                        caseData.getEt1VettingAcasCertIsYesOrNo5(), caseData.getEt1VettingAcasCertExemptYesOrNo5(), 5);
                break;
            case 6:
                setAcasCertValues(caseData, assessment,
                        caseData.getEt1VettingAcasCertIsYesOrNo6(), caseData.getEt1VettingAcasCertExemptYesOrNo6(), 6);
                break;
            default:
                break;
        }
        if (StringUtils.isNotBlank(assessment.getNotes())
                && StringUtils.isBlank(caseData.getEt1VettingAcasCertGeneralNote())) {
            caseData.setEt1VettingAcasCertGeneralNote(formatAiNote(assessment.getNotes()));
        }
    }

    private void setAcasCertValues(CaseData caseData, Et1AiVettingResult.AcasAssessment assessment,
                                   String currentCert, String currentExempt, int respondentNum) {
        if (StringUtils.isBlank(currentCert)) {
            setAcasCertFieldByNumber(caseData, respondentNum, sanitizeYesNo(assessment.getHasCertificate()));
        }
        if (StringUtils.isBlank(currentExempt)
                && "No".equalsIgnoreCase(getAcasCertFieldByNumber(caseData, respondentNum))) {
            setAcasExemptFieldByNumber(caseData, respondentNum, sanitizeYesNo(assessment.getIsExempt()));
        }
    }

    private void setAcasCertFieldByNumber(CaseData caseData, int respondentNum, String value) {
        switch (respondentNum) {
            case 1 -> caseData.setEt1VettingAcasCertIsYesOrNo1(value);
            case 2 -> caseData.setEt1VettingAcasCertIsYesOrNo2(value);
            case 3 -> caseData.setEt1VettingAcasCertIsYesOrNo3(value);
            case 4 -> caseData.setEt1VettingAcasCertIsYesOrNo4(value);
            case 5 -> caseData.setEt1VettingAcasCertIsYesOrNo5(value);
            case 6 -> caseData.setEt1VettingAcasCertIsYesOrNo6(value);
            default -> { }
        }
    }

    private void setAcasExemptFieldByNumber(CaseData caseData, int respondentNum, String value) {
        switch (respondentNum) {
            case 1 -> caseData.setEt1VettingAcasCertExemptYesOrNo1(value);
            case 2 -> caseData.setEt1VettingAcasCertExemptYesOrNo2(value);
            case 3 -> caseData.setEt1VettingAcasCertExemptYesOrNo3(value);
            case 4 -> caseData.setEt1VettingAcasCertExemptYesOrNo4(value);
            case 5 -> caseData.setEt1VettingAcasCertExemptYesOrNo5(value);
            case 6 -> caseData.setEt1VettingAcasCertExemptYesOrNo6(value);
            default -> { }
        }
    }

    private void populateSubstantiveDefects(CaseData caseData, Et1AiVettingResult result) {
        if (result.getSubstantiveDefects() == null && CollectionUtils.isEmpty(caseData.getSubstantiveDefectsList())) {
            return;
        }
        Et1AiVettingResult.SubstantiveDefectsAssessment defects = result.getSubstantiveDefects();
        List<String> sanitizedDefects = defects != null
                && CollectionUtils.isNotEmpty(defects.getDefectsList())
                ? sanitizeDefects(defects.getDefectsList()) : new ArrayList<>();

        if (CollectionUtils.isNotEmpty(caseData.getSubstantiveDefectsList())) {
            caseData.setSubstantiveDefectsList(sanitizeDefects(caseData.getSubstantiveDefectsList()));
        } else if (CollectionUtils.isNotEmpty(sanitizedDefects)) {
            caseData.setSubstantiveDefectsList(sanitizedDefects);
        }

        List<String> activeDefects = CollectionUtils.isNotEmpty(caseData.getSubstantiveDefectsList())
                ? caseData.getSubstantiveDefectsList() : sanitizedDefects;

        if (activeDefects.contains("rule121a") && StringUtils.isBlank(caseData.getRule121aTextArea())) {
            caseData.setRule121aTextArea(resolveNote(defects != null ? defects.getRule121aTextArea() : null,
                    AI_NOTE_PREFIX + "Tribunal may lack jurisdiction to consider this claim."));
        }
        if (activeDefects.contains("rule121b") && StringUtils.isBlank(caseData.getRule121bTextArea())) {
            caseData.setRule121bTextArea(resolveNote(defects != null ? defects.getRule121bTextArea() : null,
                    AI_NOTE_PREFIX + "Claim details may lack necessary particulars to be sensibly responded to."));
        }
        if (activeDefects.contains("rule121c") && StringUtils.isBlank(caseData.getRule121cTextArea())) {
            caseData.setRule121cTextArea(resolveNote(defects != null ? defects.getRule121cTextArea() : null,
                    AI_NOTE_PREFIX + "No ACAS early conciliation certificate or valid exemption provided."));
        }
        if (activeDefects.contains("rule121d") && StringUtils.isBlank(caseData.getRule121dTextArea())) {
            caseData.setRule121dTextArea(resolveNote(defects != null ? defects.getRule121dTextArea() : null,
                    AI_NOTE_PREFIX + "Claimed ACAS early conciliation exemption may not apply."));
        }
        if (activeDefects.contains("rule121 da") && StringUtils.isBlank(caseData.getRule121daTextArea())) {
            caseData.setRule121daTextArea(resolveNote(defects != null ? defects.getRule121daTextArea() : null,
                    AI_NOTE_PREFIX + "Early conciliation number mismatch or conciliation not undertaken."));
        }
        if (activeDefects.contains("rule121e") && StringUtils.isBlank(caseData.getRule121eTextArea())) {
            caseData.setRule121eTextArea(resolveNote(defects != null ? defects.getRule121eTextArea() : null,
                    AI_NOTE_PREFIX + "Claimant name does not match name on ACAS certificate."));
        }
        if (activeDefects.contains("rule121f") && StringUtils.isBlank(caseData.getRule121fTextArea())) {
            caseData.setRule121fTextArea(resolveNote(defects != null ? defects.getRule121fTextArea() : null,
                    AI_NOTE_PREFIX + "Respondent name does not match name on ACAS certificate."));
        }
        if (defects != null && StringUtils.isNotBlank(defects.getGeneralNotes())
                && StringUtils.isBlank(caseData.getEt1SubstantiveDefectsGeneralNotes())) {
            caseData.setEt1SubstantiveDefectsGeneralNotes(formatAiNote(defects.getGeneralNotes()));
        }
    }

    private List<String> sanitizeDefects(List<String> rawDefects) {
        if (CollectionUtils.isEmpty(rawDefects)) {
            return new ArrayList<>();
        }
        List<String> sanitized = new ArrayList<>();
        for (String raw : rawDefects) {
            String lower = raw.toLowerCase(Locale.ROOT);
            if (lower.contains("121a") || lower.contains("12(1)(a)") || lower.contains("13(1)(a)")) {
                addIfAbsent(sanitized, "rule121a");
            } else if (lower.contains("121b") || lower.contains("12(1)(b)") || lower.contains("13(1)(b)")) {
                addIfAbsent(sanitized, "rule121b");
            } else if (lower.contains("121c") || lower.contains("12(1)(c)") || lower.contains("13(1)(c)")) {
                addIfAbsent(sanitized, "rule121c");
            } else if (lower.contains("121da") || lower.contains("121 da") || lower.contains("12(1)(da)")
                    || lower.contains("13(1)(e)")) {
                addIfAbsent(sanitized, "rule121 da");
            } else if (lower.contains("121d") || lower.contains("12(1)(d)") || lower.contains("13(1)(d)")) {
                addIfAbsent(sanitized, "rule121d");
            } else if (lower.contains("121e") || lower.contains("12(1)(e)") || lower.contains("13(1)(f)")) {
                addIfAbsent(sanitized, "rule121e");
            } else if (lower.contains("121f") || lower.contains("12(1)(f)") || lower.contains("13(1)(g)")) {
                addIfAbsent(sanitized, "rule121f");
            }
        }
        return sanitized;
    }

    private void populateJurisdictionCodes(CaseData caseData, Et1AiVettingResult result) {
        if (result.getJurisdictionAssessment() == null) {
            return;
        }
        Et1AiVettingResult.JurisdictionAssessment jurAssessment = result.getJurisdictionAssessment();
        if (StringUtils.isBlank(caseData.getAreTheseCodesCorrect())) {
            caseData.setAreTheseCodesCorrect(sanitizeYesNo(jurAssessment.getAreCodesCorrect()));
        }
        if ("No".equalsIgnoreCase(caseData.getAreTheseCodesCorrect())
                && StringUtils.isBlank(caseData.getCodesCorrectGiveDetails())) {
            caseData.setCodesCorrectGiveDetails(resolveNote(jurAssessment.getDetails(),
                    AI_NOTE_PREFIX + "Additional jurisdiction codes recommended based on claim grounds."));
        }
        if (StringUtils.isNotBlank(jurAssessment.getGeneralNotes())
                && StringUtils.isBlank(caseData.getEt1JurisdictionCodeGeneralNotes())) {
            caseData.setEt1JurisdictionCodeGeneralNotes(formatAiNote(jurAssessment.getGeneralNotes()));
        }

        List<String> suggestedCodes = jurAssessment.getSuggestedJurisdictionCodes();
        if (CollectionUtils.isNotEmpty(suggestedCodes)) {
            appendSuggestedJurisdictionCodes(caseData, suggestedCodes);
        }
    }

    private void appendSuggestedJurisdictionCodes(CaseData caseData, List<String> suggestedCodes) {
        List<String> existingCodeStrings = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(caseData.getJurCodesCollection())) {
            for (JurCodesTypeItem item : caseData.getJurCodesCollection()) {
                if (item.getValue() != null && item.getValue().getJuridictionCodesList() != null) {
                    existingCodeStrings.add(item.getValue().getJuridictionCodesList().trim());
                }
            }
        }

        if (caseData.getVettingJurisdictionCodeCollection() == null) {
            caseData.setVettingJurisdictionCodeCollection(new ArrayList<>());
        }

        for (String rawCode : suggestedCodes) {
            String sanitizedCode = sanitizeJurisdictionCode(rawCode);
            if (sanitizedCode != null && !existingCodeStrings.contains(sanitizedCode.trim())) {
                boolean alreadyInVetting = caseData.getVettingJurisdictionCodeCollection().stream()
                        .anyMatch(v -> v.getValue() != null && sanitizedCode.trim().equalsIgnoreCase(
                                StringUtils.trimToEmpty(v.getValue().getEt1VettingJurCodeList())));
                if (!alreadyInVetting) {
                    VettingJurisdictionCodesType jurType = new VettingJurisdictionCodesType();
                    jurType.setEt1VettingJurCodeList(sanitizedCode);
                    VettingJurCodesTypeItem item = new VettingJurCodesTypeItem();
                    item.setId(UUID.randomUUID().toString());
                    item.setValue(jurType);
                    caseData.getVettingJurisdictionCodeCollection().add(item);
                }
            }
        }
    }

    private String sanitizeJurisdictionCode(String rawCode) {
        if (StringUtils.isBlank(rawCode)) {
            return null;
        }
        String upper = rawCode.trim().toUpperCase(Locale.ROOT);
        String aliased = JURISDICTION_ALIASES.getOrDefault(upper, upper);
        if (VALID_JURISDICTION_CODES.contains(aliased)) {
            return aliased;
        }
        log.warn("Ignoring unrecognized AI-suggested jurisdiction code '{}'", rawCode);
        return null;
    }

    private void populateTrackAllocation(CaseData caseData, Et1AiVettingResult result) {
        if (result.getTrackAllocation() == null) {
            return;
        }
        if (StringUtils.isBlank(caseData.getIsTrackAllocationCorrect())) {
            caseData.setIsTrackAllocationCorrect(sanitizeYesNo(result.getTrackAllocation().getIsTrackCorrect()));
        }
        if ("No".equalsIgnoreCase(caseData.getIsTrackAllocationCorrect())) {
            if (StringUtils.isBlank(caseData.getSuggestAnotherTrack())) {
                caseData.setSuggestAnotherTrack(sanitizeTrack(result.getTrackAllocation().getSuggestedTrack()));
            }
            if (StringUtils.isBlank(caseData.getWhyChangeTrackAllocation())) {
                caseData.setWhyChangeTrackAllocation(resolveNote(
                        result.getTrackAllocation().getWhyChangeTrackAllocation(),
                        AI_NOTE_PREFIX + "Track allocation adjusted to align with recommended jurisdiction codes."));
            }
        }
        if (StringUtils.isNotBlank(result.getTrackAllocation().getGeneralNotes())
                && StringUtils.isBlank(caseData.getTrackAllocationGeneralNotes())) {
            caseData.setTrackAllocationGeneralNotes(formatAiNote(result.getTrackAllocation().getGeneralNotes()));
        }
    }

    private String sanitizeTrack(String rawTrack) {
        if (StringUtils.isBlank(rawTrack)) {
            return "Standard";
        }
        String lower = rawTrack.trim().toLowerCase(Locale.ROOT);
        if (lower.contains("open")) {
            return "Open";
        }
        if (lower.contains("short")) {
            return "Short";
        }
        if (lower.contains("no track") || lower.equals("none")) {
            return "No track";
        }
        return "Standard";
    }

    private void populateLocationAndVenue(CaseData caseData, Et1AiVettingResult result) {
        if (result.getLocationAndVenue() == null) {
            return;
        }
        Et1AiVettingResult.LocationAndVenueAssessment loc = result.getLocationAndVenue();
        if (StringUtils.isBlank(caseData.getIsLocationCorrect())) {
            String aiLocation = sanitizeYesNo(loc.getIsLocationCorrect());
            if ("No".equalsIgnoreCase(aiLocation)) {
                DynamicFixedListType regionalList = buildRegionalOfficeList(
                        caseData.getManagingOffice(), loc.getSuggestedOffice());
                if (regionalList != null && regionalList.getValue() != null) {
                    caseData.setIsLocationCorrect("No");
                    caseData.setRegionalOfficeList(regionalList);
                } else {
                    caseData.setIsLocationCorrect("Yes");
                }
            } else if (aiLocation != null) {
                caseData.setIsLocationCorrect(aiLocation);
            }
        }
        if ("No".equalsIgnoreCase(caseData.getIsLocationCorrect())
                && StringUtils.isBlank(caseData.getWhyChangeOffice())) {
            caseData.setWhyChangeOffice(resolveNote(loc.getWhyChangeOffice(),
                    AI_NOTE_PREFIX + "Office adjustment recommended based on respondent and workplace location."));
        }
        if (StringUtils.isNotBlank(loc.getGeneralNotes())
                && StringUtils.isBlank(caseData.getEt1LocationGeneralNotes())) {
            caseData.setEt1LocationGeneralNotes(formatAiNote(loc.getGeneralNotes()));
        }

        populateHearingVenueSelection(caseData, loc);
    }

    private void populateHearingVenueSelection(CaseData caseData,
                                               Et1AiVettingResult.LocationAndVenueAssessment loc) {
        if (StringUtils.isBlank(caseData.getEt1SuggestHearingVenue())) {
            String suggestVenue = sanitizeYesNo(loc.getSuggestHearingVenue());
            if ("Yes".equalsIgnoreCase(suggestVenue) && StringUtils.isNotBlank(loc.getSuggestedHearingVenue())) {
                DynamicFixedListType venueList = buildHearingVenuesSelection(
                        caseData, loc.getSuggestedHearingVenue());
                if (venueList != null && venueList.getValue() != null) {
                    caseData.setEt1SuggestHearingVenue("Yes");
                    caseData.setEt1HearingVenues(venueList);
                } else {
                    caseData.setEt1SuggestHearingVenue("No");
                }
            } else if (suggestVenue != null) {
                caseData.setEt1SuggestHearingVenue("No");
            }
        }
        if (StringUtils.isNotBlank(loc.getHearingVenueGeneralNotes())
                && StringUtils.isBlank(caseData.getEt1HearingVenueGeneralNotes())) {
            caseData.setEt1HearingVenueGeneralNotes(formatAiNote(loc.getHearingVenueGeneralNotes()));
        }
    }

    private DynamicFixedListType buildRegionalOfficeList(String currentManagingOffice, String suggestedOffice) {
        if (StringUtils.isBlank(suggestedOffice)) {
            return null;
        }
        List<TribunalOffice> offices = TribunalOffice.isScotlandOffice(currentManagingOffice)
                ? TribunalOffice.SCOTLAND_OFFICES : TribunalOffice.ENGLANDWALES_OFFICES;
        List<DynamicValueType> items = offices.stream()
                .filter(office -> !office.getOfficeName().equals(currentManagingOffice))
                .map(office -> DynamicValueType.create(office.getOfficeName(), office.getOfficeName()))
                .toList();
        DynamicValueType selected = items.stream()
                .filter(item -> item.getLabel().equalsIgnoreCase(suggestedOffice.trim())
                        || item.getLabel().toLowerCase(Locale.ROOT)
                                .contains(suggestedOffice.trim().toLowerCase(Locale.ROOT)))
                .findFirst()
                .orElse(null);
        if (selected == null) {
            return null;
        }
        DynamicFixedListType listType = DynamicFixedListType.from(items);
        listType.setValue(selected);
        return listType;
    }

    private DynamicFixedListType buildHearingVenuesSelection(CaseData caseData, String suggestedVenue) {
        if (jpaVenueService == null || StringUtils.isBlank(suggestedVenue)) {
            return null;
        }
        List<DynamicValueType> venues = resolveVenuesForCase(caseData);
        if (CollectionUtils.isEmpty(venues)) {
            return null;
        }
        String trimmedTarget = suggestedVenue.trim().toLowerCase(Locale.ROOT);
        DynamicValueType matched = venues.stream()
                .filter(dv -> dv.getLabel().equalsIgnoreCase(suggestedVenue.trim())
                        || dv.getCode().equalsIgnoreCase(suggestedVenue.trim())
                        || dv.getLabel().toLowerCase(Locale.ROOT).contains(trimmedTarget))
                .findFirst()
                .orElse(null);
        if (matched == null) {
            return null;
        }
        DynamicFixedListType listType = DynamicFixedListType.from(venues);
        listType.setValue(matched);
        return listType;
    }

    private List<String> resolveAvailableVenueNames(CaseData caseData) {
        if (jpaVenueService == null) {
            return List.of();
        }
        return resolveVenuesForCase(caseData).stream()
                .map(DynamicValueType::getLabel)
                .toList();
    }

    private List<DynamicValueType> resolveVenuesForCase(CaseData caseData) {
        if (jpaVenueService == null || caseData == null) {
            return List.of();
        }
        String officeName = DynamicFixedListType.getSelectedLabel(caseData.getRegionalOfficeList())
                .orElse(caseData.getManagingOffice());
        if (StringUtils.isBlank(officeName) || UNASSIGNED_OFFICE.equalsIgnoreCase(officeName)) {
            return List.of();
        }
        try {
            return jpaVenueService.getVenues(TribunalOffice.valueOfOfficeName(officeName));
        } catch (Exception ex) {
            log.debug("Could not resolve venues for office {}: {}", officeName, ex.getMessage());
            return List.of();
        }
    }

    private void populateFurtherQuestions(CaseData caseData, Et1AiVettingResult result) {
        if (result.getFurtherQuestions() == null) {
            return;
        }
        Et1AiVettingResult.FurtherQuestionsAssessment questions = result.getFurtherQuestions();
        if (StringUtils.isBlank(caseData.getEt1GovOrMajorQuestion())) {
            caseData.setEt1GovOrMajorQuestion(sanitizeYesNo(questions.getIsGovOrMajorEmployer()));
        }
        if (StringUtils.isBlank(caseData.getEt1ReasonableAdjustmentsQuestion())) {
            caseData.setEt1ReasonableAdjustmentsQuestion(sanitizeYesNo(questions.getHasReasonableAdjustments()));
        }
        if ("Yes".equalsIgnoreCase(caseData.getEt1ReasonableAdjustmentsQuestion())
                && StringUtils.isBlank(caseData.getEt1ReasonableAdjustmentsTextArea())) {
            caseData.setEt1ReasonableAdjustmentsTextArea(resolveNote(questions.getReasonableAdjustmentsDetails(),
                    AI_NOTE_PREFIX + "Reasonable adjustments requested in claim preferences."));
        }
        if (StringUtils.isBlank(caseData.getEt1VideoHearingQuestion())) {
            caseData.setEt1VideoHearingQuestion(sanitizeYesNo(questions.getVideoHearingPreference()));
        }
        if ("No".equalsIgnoreCase(caseData.getEt1VideoHearingQuestion())
                && StringUtils.isBlank(caseData.getEt1VideoHearingTextArea())) {
            caseData.setEt1VideoHearingTextArea(resolveNote(questions.getVideoHearingDetails(),
                    AI_NOTE_PREFIX + "Claimant preferences indicate video hearing may not be suitable."));
        }
        if (StringUtils.isNotBlank(questions.getGeneralNotes())
                && StringUtils.isBlank(caseData.getEt1FurtherQuestionsGeneralNotes())) {
            caseData.setEt1FurtherQuestionsGeneralNotes(formatAiNote(questions.getGeneralNotes()));
        }
    }

    private String sanitizeYesNo(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        String trimmed = value.trim();
        if ("Yes".equalsIgnoreCase(trimmed)) {
            return "Yes";
        }
        if ("No".equalsIgnoreCase(trimmed)) {
            return "No";
        }
        return null;
    }

    private String sanitizeYesNoNotApplicable(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        String trimmed = value.trim();
        if ("Yes".equalsIgnoreCase(trimmed)) {
            return "Yes";
        }
        if ("No".equalsIgnoreCase(trimmed)) {
            return "No";
        }
        if (NOT_APPLICABLE.equalsIgnoreCase(trimmed)) {
            return NOT_APPLICABLE;
        }
        return null;
    }

    private void populateJudicialReferrals(CaseData caseData, Et1AiVettingResult result) {
        if (result.getJudicialReferrals() == null && CollectionUtils.isEmpty(caseData.getReferralToJudgeOrLOList())) {
            return;
        }
        Et1AiVettingResult.JudicialReferralAssessment referrals = result.getJudicialReferrals();
        List<String> sanitizedReferrals = referrals != null
                && CollectionUtils.isNotEmpty(referrals.getReferralsToJudge())
                ? sanitizeJudgeReferrals(referrals.getReferralsToJudge()) : new ArrayList<>();

        if (CollectionUtils.isNotEmpty(caseData.getReferralToJudgeOrLOList())) {
            caseData.setReferralToJudgeOrLOList(sanitizeJudgeReferrals(caseData.getReferralToJudgeOrLOList()));
        } else if (CollectionUtils.isNotEmpty(sanitizedReferrals)) {
            caseData.setReferralToJudgeOrLOList(sanitizedReferrals);
        }

        List<String> activeReferrals = CollectionUtils.isNotEmpty(caseData.getReferralToJudgeOrLOList())
                ? caseData.getReferralToJudgeOrLOList() : sanitizedReferrals;

        if (activeReferrals.contains("aClaimOfInterimRelief")
                && StringUtils.isBlank(caseData.getAclaimOfInterimReliefTextArea())) {
            caseData.setAclaimOfInterimReliefTextArea(resolveNote(
                    referrals != null ? referrals.getClaimOfInterimReliefNotes() : null,
                    AI_NOTE_PREFIX + "Interim relief application identified in claim grounds."));
        }
        if (activeReferrals.contains("aStatutoryAppeal")
                && StringUtils.isBlank(caseData.getAstatutoryAppealTextArea())) {
            caseData.setAstatutoryAppealTextArea(resolveNote(
                    referrals != null ? referrals.getStatutoryAppealNotes() : null,
                    AI_NOTE_PREFIX + "Statutory appeal identified in claim grounds."));
        }
        if (activeReferrals.contains("anAllegationOfCommissionOfSexualOffence")
                && StringUtils.isBlank(caseData.getAnAllegationOfCommissionOfSexualOffenceTextArea())) {
            caseData.setAnAllegationOfCommissionOfSexualOffenceTextArea(resolveNote(
                    referrals != null ? referrals.getSexualOffenceNotes() : null,
                    AI_NOTE_PREFIX + "Allegation of sexual offence identified in claim particulars."));
        }
        if (activeReferrals.contains("insolvency")
                && StringUtils.isBlank(caseData.getInsolvencyTextArea())) {
            caseData.setInsolvencyTextArea(resolveNote(
                    referrals != null ? referrals.getInsolvencyNotes() : null,
                    AI_NOTE_PREFIX + "Respondent administration, liquidation or insolvency noted."));
        }
        if (activeReferrals.contains("jurisdictionsUnclear")
                && StringUtils.isBlank(caseData.getJurisdictionsUnclearTextArea())) {
            caseData.setJurisdictionsUnclearTextArea(resolveNote(
                    referrals != null ? referrals.getJurisdictionsUnclearNotes() : null,
                    AI_NOTE_PREFIX + "Claimed jurisdictions or legal bases require judicial clarification."));
        }
        if (activeReferrals.contains("lengthOfService")
                && StringUtils.isBlank(caseData.getLengthOfServiceTextArea())) {
            caseData.setLengthOfServiceTextArea(resolveNote(
                    referrals != null ? referrals.getLengthOfServiceNotes() : null,
                    AI_NOTE_PREFIX + "Claimant may have less than 2 years service for ordinary unfair dismissal."));
        }
        if (activeReferrals.contains("potentiallyLinkedCasesInTheEcm")
                && StringUtils.isBlank(caseData.getPotentiallyLinkedCasesInTheEcmTextArea())) {
            caseData.setPotentiallyLinkedCasesInTheEcmTextArea(resolveNote(
                    referrals != null ? referrals.getPotentiallyLinkedCasesNotes() : null,
                    AI_NOTE_PREFIX + "Potentially related or linked proceedings identified."));
        }
        if (activeReferrals.contains("rule50Issues")
                && StringUtils.isBlank(caseData.getRule50IssuesTextArea())) {
            caseData.setRule50IssuesTextArea(resolveNote(
                    referrals != null ? referrals.getRule50IssuesNotes() : null,
                    AI_NOTE_PREFIX + "Privacy, restricted reporting or Rule 49 anonymity issues flagged."));
        }
        if (activeReferrals.contains("anotherReasonForJudicialReferral")
                && StringUtils.isBlank(caseData.getAnotherReasonForJudicialReferralTextArea())) {
            caseData.setAnotherReasonForJudicialReferralTextArea(resolveNote(
                    referrals != null ? referrals.getAnotherReasonNotes() : null,
                    AI_NOTE_PREFIX + "Judicial referral recommended based on claim particulars."));
        }
        if (referrals != null && StringUtils.isNotBlank(referrals.getGeneralNotes())
                && StringUtils.isBlank(caseData.getEt1JudgeReferralGeneralNotes())) {
            caseData.setEt1JudgeReferralGeneralNotes(formatAiNote(referrals.getGeneralNotes()));
        }
    }

    private List<String> sanitizeJudgeReferrals(List<String> rawReferrals) {
        if (CollectionUtils.isEmpty(rawReferrals)) {
            return new ArrayList<>();
        }
        List<String> sanitized = new ArrayList<>();
        for (String raw : rawReferrals) {
            String lower = raw.toLowerCase(Locale.ROOT);
            if (lower.contains("interim") || lower.contains("aclaimofinterimrelief")) {
                addIfAbsent(sanitized, "aClaimOfInterimRelief");
            } else if (lower.contains("statutory") || lower.contains("astatutoryappeal")) {
                addIfAbsent(sanitized, "aStatutoryAppeal");
            } else if (lower.contains("sexual") || lower.contains("anallegationofcommissionofsexualoffence")) {
                addIfAbsent(sanitized, "anAllegationOfCommissionOfSexualOffence");
            } else if (lower.contains("insolvency")) {
                addIfAbsent(sanitized, "insolvency");
            } else if (lower.contains("unclear") || lower.contains("jurisdictionsunclear")) {
                addIfAbsent(sanitized, "jurisdictionsUnclear");
            } else if (lower.contains("lengthofservice") || lower.contains("length of service")) {
                addIfAbsent(sanitized, "lengthOfService");
            } else if (lower.contains("linked") || lower.contains("potentiallylinkedcasesintheecm")) {
                addIfAbsent(sanitized, "potentiallyLinkedCasesInTheEcm");
            } else if (lower.contains("rule50") || lower.contains("rule49") || lower.contains("rule50issues")) {
                addIfAbsent(sanitized, "rule50Issues");
            } else if (lower.contains("another") || lower.contains("anotherreasonforjudicialreferral")) {
                addIfAbsent(sanitized, "anotherReasonForJudicialReferral");
            }
        }
        return sanitized;
    }

    private void populateRejOrVpReferrals(CaseData caseData, Et1AiVettingResult result) {
        if (result.getRejOrVpReferrals() == null && CollectionUtils.isEmpty(caseData.getReferralToREJOrVPList())) {
            return;
        }
        Et1AiVettingResult.RejOrVpReferralAssessment referrals = result.getRejOrVpReferrals();
        List<String> sanitizedReferrals = referrals != null
                && CollectionUtils.isNotEmpty(referrals.getReferralsToRejOrVp())
                ? sanitizeRejOrVpReferrals(referrals.getReferralsToRejOrVp()) : new ArrayList<>();

        if (CollectionUtils.isNotEmpty(caseData.getReferralToREJOrVPList())) {
            caseData.setReferralToREJOrVPList(sanitizeRejOrVpReferrals(caseData.getReferralToREJOrVPList()));
        } else if (CollectionUtils.isNotEmpty(sanitizedReferrals)) {
            caseData.setReferralToREJOrVPList(sanitizedReferrals);
        }

        List<String> activeReferrals = CollectionUtils.isNotEmpty(caseData.getReferralToREJOrVPList())
                ? caseData.getReferralToREJOrVPList() : sanitizedReferrals;

        if (activeReferrals.contains("vexatiousLitigantOrder")
                && StringUtils.isBlank(caseData.getVexatiousLitigantOrderTextArea())) {
            caseData.setVexatiousLitigantOrderTextArea(resolveNote(
                    referrals != null ? referrals.getVexatiousLitigantOrderNotes() : null,
                    AI_NOTE_PREFIX + "Claimant may be subject to a vexatious litigant restriction."));
        }
        if (activeReferrals.contains("aNationalSecurityIssue")
                && StringUtils.isBlank(caseData.getAnationalSecurityIssueTextArea())) {
            caseData.setAnationalSecurityIssueTextArea(resolveNote(
                    referrals != null ? referrals.getNationalSecurityNotes() : null,
                    AI_NOTE_PREFIX + "National security matters identified in claim particulars."));
        }
        if (activeReferrals.contains("nationalMultipleOrPresidentialOrder")
                && StringUtils.isBlank(caseData.getNationalMultipleOrPresidentialOrderTextArea())) {
            caseData.setNationalMultipleOrPresidentialOrderTextArea(resolveNote(
                    referrals != null ? referrals.getNationalMultipleNotes() : null,
                    AI_NOTE_PREFIX + "Potential national multiple or presidential order case."));
        }
        if (activeReferrals.contains("transferToOtherRegion")
                && StringUtils.isBlank(caseData.getTransferToOtherRegionTextArea())) {
            caseData.setTransferToOtherRegionTextArea(resolveNote(
                    referrals != null ? referrals.getTransferToOtherRegionNotes() : null,
                    AI_NOTE_PREFIX + "Request or basis for transfer to another tribunal office."));
        }
        if (activeReferrals.contains("serviceAbroad")
                && StringUtils.isBlank(caseData.getServiceAbroadTextArea())) {
            caseData.setServiceAbroadTextArea(resolveNote(
                    referrals != null ? referrals.getServiceAbroadNotes() : null,
                    AI_NOTE_PREFIX + "Respondent address is outside the UK, requiring service abroad."));
        }
        if (activeReferrals.contains("aSensitiveIssue")
                && StringUtils.isBlank(caseData.getAsensitiveIssueTextArea())) {
            caseData.setAsensitiveIssueTextArea(resolveNote(
                    referrals != null ? referrals.getSensitiveIssueNotes() : null,
                    AI_NOTE_PREFIX + "Sensitive or high-profile issues requiring REJ or VP allocation."));
        }
        if (activeReferrals.contains("anyPotentialConflict")
                && StringUtils.isBlank(caseData.getAnyPotentialConflictTextArea())) {
            caseData.setAnyPotentialConflictTextArea(resolveNote(
                    referrals != null ? referrals.getPotentialConflictNotes() : null,
                    AI_NOTE_PREFIX + "Potential judicial or administrative conflict identified."));
        }
        if (activeReferrals.contains("anotherReasonREJOrVP")
                && StringUtils.isBlank(caseData.getAnotherReasonREJOrVPTextArea())) {
            caseData.setAnotherReasonREJOrVPTextArea(resolveNote(
                    referrals != null ? referrals.getAnotherReasonNotes() : null,
                    AI_NOTE_PREFIX + "Referral to Regional Employment Judge / VP recommended."));
        }
        if (referrals != null && StringUtils.isNotBlank(referrals.getGeneralNotes())
                && StringUtils.isBlank(caseData.getEt1REJOrVPReferralGeneralNotes())) {
            caseData.setEt1REJOrVPReferralGeneralNotes(formatAiNote(referrals.getGeneralNotes()));
        }
    }

    private List<String> sanitizeRejOrVpReferrals(List<String> rawReferrals) {
        if (CollectionUtils.isEmpty(rawReferrals)) {
            return new ArrayList<>();
        }
        List<String> sanitized = new ArrayList<>();
        for (String raw : rawReferrals) {
            String lower = raw.toLowerCase(Locale.ROOT);
            if (lower.contains("vexatious") || lower.contains("vexatiouslitigantorder")) {
                addIfAbsent(sanitized, "vexatiousLitigantOrder");
            } else if (lower.contains("security") || lower.contains("anationalsecurityissue")) {
                addIfAbsent(sanitized, "aNationalSecurityIssue");
            } else if (lower.contains("multiple") || lower.contains("presidential")
                    || lower.contains("nationalmultipleorpresidentialorder")) {
                addIfAbsent(sanitized, "nationalMultipleOrPresidentialOrder");
            } else if (lower.contains("transfer") || lower.contains("transfertootherregion")) {
                addIfAbsent(sanitized, "transferToOtherRegion");
            } else if (lower.contains("abroad") || lower.contains("serviceabroad")) {
                addIfAbsent(sanitized, "serviceAbroad");
            } else if (lower.contains("sensitive") || lower.contains("asensitiveissue")) {
                addIfAbsent(sanitized, "aSensitiveIssue");
            } else if (lower.contains("conflict") || lower.contains("anypotentialconflict")) {
                addIfAbsent(sanitized, "anyPotentialConflict");
            } else if (lower.contains("another") || lower.contains("anotherreasonrejorvp")) {
                addIfAbsent(sanitized, "anotherReasonREJOrVP");
            }
        }
        return sanitized;
    }

    private void populateOtherReferrals(CaseData caseData, Et1AiVettingResult result) {
        if (result.getOtherReferrals() == null && CollectionUtils.isEmpty(caseData.getOtherReferralList())) {
            return;
        }
        Et1AiVettingResult.OtherReferralAssessment referrals = result.getOtherReferrals();
        List<String> sanitizedReferrals = referrals != null
                && CollectionUtils.isNotEmpty(referrals.getOtherReferrals())
                ? sanitizeOtherReferrals(referrals.getOtherReferrals()) : new ArrayList<>();

        if (CollectionUtils.isNotEmpty(caseData.getOtherReferralList())) {
            caseData.setOtherReferralList(sanitizeOtherReferrals(caseData.getOtherReferralList()));
        } else if (CollectionUtils.isNotEmpty(sanitizedReferrals)) {
            caseData.setOtherReferralList(sanitizedReferrals);
        }

        List<String> activeReferrals = CollectionUtils.isNotEmpty(caseData.getOtherReferralList())
                ? caseData.getOtherReferralList() : sanitizedReferrals;

        if (activeReferrals.contains("claimOutOfTime")
                && StringUtils.isBlank(caseData.getClaimOutOfTimeTextArea())) {
            caseData.setClaimOutOfTimeTextArea(resolveNote(
                    referrals != null ? referrals.getClaimOutOfTimeNotes() : null,
                    AI_NOTE_PREFIX + "Potential limitation issue: claim may be outside statutory window."));
        }
        if (activeReferrals.contains("multipleClaim")
                && StringUtils.isBlank(caseData.getMultipleClaimTextArea())) {
            caseData.setMultipleClaimTextArea(resolveNote(
                    referrals != null ? referrals.getMultipleClaimNotes() : null,
                    AI_NOTE_PREFIX + "Multiple claim indicators identified in claim particulars."));
        }
        if (activeReferrals.contains("employmentStatusIssues")
                && StringUtils.isBlank(caseData.getEmploymentStatusIssuesTextArea())) {
            caseData.setEmploymentStatusIssuesTextArea(resolveNote(
                    referrals != null ? referrals.getEmploymentStatusIssuesNotes() : null,
                    AI_NOTE_PREFIX + "Dispute over employment status (employee vs worker vs contractor) identified."));
        }
        if (activeReferrals.contains("pidJurisdictionRegulator")
                && StringUtils.isBlank(caseData.getPidJurisdictionRegulatorTextArea())) {
            caseData.setPidJurisdictionRegulatorTextArea(resolveNote(
                    referrals != null ? referrals.getPidRegulatorNotes() : null,
                    AI_NOTE_PREFIX + "Protected disclosure with request to notify relevant regulator."));
        }
        if (activeReferrals.contains("videoHearingPreference")
                && StringUtils.isBlank(caseData.getVideoHearingPreferenceTextArea())) {
            caseData.setVideoHearingPreferenceTextArea(resolveNote(
                    referrals != null ? referrals.getVideoHearingNotes() : null,
                    AI_NOTE_PREFIX + "Claimant indicated a preference for a video hearing in the claim submission."));
        }
        if (activeReferrals.contains("rule50IssuesOtherFactors")
                && StringUtils.isBlank(caseData.getRule50IssuesForOtherReferralTextArea())) {
            caseData.setRule50IssuesForOtherReferralTextArea(resolveNote(
                    referrals != null ? referrals.getRule50Notes() : null,
                    AI_NOTE_PREFIX + "Rule 49 privacy or anonymity considerations flagged."));
        }
        if (activeReferrals.contains("otherRelevantFactors")
                && StringUtils.isBlank(caseData.getAnotherReasonForOtherReferralTextArea())) {
            caseData.setAnotherReasonForOtherReferralTextArea(resolveNote(
                    referrals != null ? referrals.getAnotherReasonNotes() : null,
                    AI_NOTE_PREFIX + "Additional relevant factors identified in claim particulars."));
        }
        if (referrals != null && StringUtils.isNotBlank(referrals.getGeneralNotes())
                && StringUtils.isBlank(caseData.getEt1OtherReferralGeneralNotes())) {
            caseData.setEt1OtherReferralGeneralNotes(formatAiNote(referrals.getGeneralNotes()));
        }
    }

    private List<String> sanitizeOtherReferrals(List<String> rawReferrals) {
        if (CollectionUtils.isEmpty(rawReferrals)) {
            return new ArrayList<>();
        }
        List<String> sanitized = new ArrayList<>();
        for (String raw : rawReferrals) {
            String lower = raw.toLowerCase(Locale.ROOT);
            if (lower.contains("time") || lower.contains("claimoutoftime") || lower.contains("out of time")) {
                addIfAbsent(sanitized, "claimOutOfTime");
            } else if (lower.contains("multiple") || lower.contains("multipleclaim")) {
                addIfAbsent(sanitized, "multipleClaim");
            } else if (lower.contains("status") || lower.contains("employmentstatusissues")) {
                addIfAbsent(sanitized, "employmentStatusIssues");
            } else if (lower.contains("regulator") || lower.contains("pidjurisdictionregulator")) {
                addIfAbsent(sanitized, "pidJurisdictionRegulator");
            } else if (lower.contains("video") || lower.contains("videohearingpreference")) {
                addIfAbsent(sanitized, "videoHearingPreference");
            } else if (lower.contains("rule50") || lower.contains("rule49")
                    || lower.contains("rule50issuesotherfactors")) {
                addIfAbsent(sanitized, "rule50IssuesOtherFactors");
            } else if (lower.contains("other") || lower.contains("otherrelevantfactors")) {
                addIfAbsent(sanitized, "otherRelevantFactors");
            }
        }
        return sanitized;
    }

    private String resolveNote(String aiNote, String fallback) {
        if (StringUtils.isNotBlank(aiNote)) {
            return formatAiNote(aiNote);
        }
        return fallback;
    }

    private String formatAiNote(String aiNote) {
        String normalized = normalizeAiText(aiNote.trim());
        return normalized.startsWith(AI_NOTE_PREFIX) ? normalized : AI_NOTE_PREFIX + normalized;
    }

    private String normalizeAiText(String text) {
        return normalizeDatesToDdMmYyyy(normalizeRuleReferences(text));
    }

    private String normalizeRuleReferences(String text) {
        if (StringUtils.isBlank(text)) {
            return text;
        }
        return text
                .replaceAll("(?i)\\brule\\s+12\\(1\\)\\(da\\)", "Rule 13(1)(e)")
                .replaceAll("(?i)\\brule\\s+12\\(1\\)\\(e\\)", "Rule 13(1)(f)")
                .replaceAll("(?i)\\brule\\s+12\\(1\\)\\(f\\)", "Rule 13(1)(g)")
                .replaceAll("(?i)\\brule\\s+12\\b", "Rule 13")
                .replaceAll("(?i)\\brule\\s+50\\b", "Rule 49");
    }

    private void addIfAbsent(List<String> list, String value) {
        if (!list.contains(value)) {
            list.add(value);
        }
    }

    private void populateAdditionalInformationAndBanner(CaseData caseData, Et1AiVettingResult result) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern(DD_MM_YYYY_DATE_FORMAT));
        String normalizedSummary = normalizeAiText(
                StringUtils.defaultIfBlank(result.getExecutiveSummaryMarkdown(), "No summary supplied"));
        List<String> reviewItems = buildItemsRequiringReview(caseData, result);

        StringBuilder summaryBuilder = new StringBuilder();
        summaryBuilder.append(String.format("""
                [AI ET1 Vetting Co-Pilot Recommendation - %s]
                Model: %s | Confidence: %s

                Summary:
                %s""", today, geminiProperties.getModel(),
                StringUtils.defaultIfBlank(result.getConfidence(), "MEDIUM"),
                formatMarkdownForPlainText(normalizedSummary)));

        if (CollectionUtils.isNotEmpty(reviewItems)) {
            summaryBuilder.append("\n\nItems Requiring Double-Checking / Unable to Answer:\n");
            for (String item : reviewItems) {
                summaryBuilder.append("- ").append(formatMarkdownForPlainText(item)).append('\n');
            }
        }

        if (StringUtils.isBlank(caseData.getEt1VettingAdditionalInformationTextArea())) {
            caseData.setEt1VettingAdditionalInformationTextArea(summaryBuilder.toString().trim());
        }

        String bannerSummaryHtml = renderMarkdownToHtml(normalizeAiText(
                StringUtils.defaultIfBlank(result.getExecutiveSummaryMarkdown(), "Review pre-populated fields.")));
        StringBuilder reviewHtml = new StringBuilder();
        if (CollectionUtils.isNotEmpty(reviewItems)) {
            reviewHtml.append("<h4 style=\"margin-bottom: 4px;\">")
                    .append("Items Requiring Double-Checking / Unable to Answer</h4><ul>");
            for (String item : reviewItems) {
                reviewHtml.append("<li>").append(formatInlineMarkdownToHtml(escapeHtml(item))).append("</li>");
            }
            reviewHtml.append("</ul>");
        }

        String bannerHtml = String.format("""
                <div style="background-color: #f3f2f1; padding: 14px; border-left: 5px solid #1d70b8; \
                margin-bottom: 20px;">
                <h3 style="margin-top: 0; color: #1d70b8;">🤖 AI ET1 Vetting Co-Pilot Recommendation</h3>
                <p><strong>Evaluation Confidence:</strong> %s &nbsp;|&nbsp; <strong>AI Model:</strong> %s</p>
                %s
                %s
                <p><em>Checklist fields have been pre-populated with AI recommendations. \
                Please review and verify all details before submitting.</em></p>
                </div>
                """, StringUtils.defaultIfBlank(result.getConfidence(), "MEDIUM"),
                geminiProperties.getModel(),
                bannerSummaryHtml,
                reviewHtml);

        String currentBefore = caseData.getEt1VettingBeforeYouStart();
        if (currentBefore != null) {
            caseData.setEt1VettingBeforeYouStart(bannerHtml + currentBefore);
        } else {
            caseData.setEt1VettingBeforeYouStart(bannerHtml);
        }
    }

    private String renderMarkdownToHtml(String markdown) {
        if (StringUtils.isBlank(markdown)) {
            return "";
        }
        String normalized = normalizeMarkdownLineBreaks(escapeHtml(markdown));
        String[] lines = normalized.split("\\r?\\n");
        StringBuilder html = new StringBuilder();
        boolean inList = false;

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                if (inList) {
                    html.append("</ul>");
                    inList = false;
                }
            } else if (trimmed.matches("^#{1,6}\\s+.+")) {
                if (inList) {
                    html.append("</ul>");
                    inList = false;
                }
                String heading = trimmed.replaceFirst("^#{1,6}\\s+", "");
                html.append("<h4 style=\"margin: 8px 0 4px;\">")
                        .append(formatInlineMarkdownToHtml(heading))
                        .append("</h4>");
            } else if (isStandaloneSubheadingLine(trimmed)) {
                if (inList) {
                    html.append("</ul>");
                    inList = false;
                }
                String heading = extractStandaloneSubheading(trimmed);
                html.append("<h4 style=\"margin: 8px 0 4px;\">")
                        .append(formatInlineMarkdownToHtml(heading))
                        .append("</h4>");
            } else if (trimmed.startsWith("- ") || trimmed.startsWith("* ")) {
                if (!inList) {
                    html.append("<ul style=\"margin: 4px 0 8px;\">");
                    inList = true;
                }
                html.append("<li>")
                        .append(formatInlineMarkdownToHtml(trimmed.substring(2).trim()))
                        .append("</li>");
            } else {
                if (inList) {
                    html.append("</ul>");
                    inList = false;
                }
                html.append("<p style=\"margin: 4px 0;\">")
                        .append(formatInlineMarkdownToHtml(trimmed))
                        .append("</p>");
            }
        }
        if (inList) {
            html.append("</ul>");
        }
        return html.toString();
    }

    private boolean isStandaloneSubheadingLine(String trimmed) {
        return trimmed.matches("^([-*]\\s+)?\\*{0,2}[A-Za-z][^:\\r\\n]{2,60}:\\*{0,2}$");
    }

    private String extractStandaloneSubheading(String trimmed) {
        return trimmed
                .replaceFirst("^[-*]\\s+", "")
                .replaceAll("^\\*+|\\*+$", "")
                .replaceFirst(":$", "")
                .trim();
    }

    private String formatMarkdownForPlainText(String markdown) {
        if (StringUtils.isBlank(markdown)) {
            return "";
        }
        return normalizeMarkdownLineBreaks(markdown)
                .replaceAll("(?m)^#{1,6}\\s+", "")
                .replaceAll("(?m)^[-*]\\s+\\*{0,2}([A-Za-z][^:\\r\\n]{2,60}:)\\*{0,2}$", "\n$1")
                .replaceAll("\\*\\*(.+?)\\*\\*", "$1")
                .replaceAll("`([^`]+)`", "$1")
                .trim();
    }

    private String normalizeMarkdownLineBreaks(String text) {
        return text
                .replaceAll("(###+\\s+[^\\r\\n-]+?)\\s+-\\s+", "$1\n- ")
                .replaceAll("\\s+-\\s+(\\*\\*)", "\n- $1")
                .replaceAll("\\s+(\\d+\\.\\s+\\*\\*)", "\n$1");
    }

    private String formatInlineMarkdownToHtml(String text) {
        return text
                .replaceAll("\\*\\*(.+?)\\*\\*", "<strong>$1</strong>")
                .replaceAll("`([^`]+)`", "<code>$1</code>");
    }

    private String escapeHtml(String text) {
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private List<String> buildItemsRequiringReview(CaseData caseData, Et1AiVettingResult result) {
        List<String> items = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(result.getItemsRequiringReview())) {
            for (String item : result.getItemsRequiringReview()) {
                if (StringUtils.isNotBlank(item)) {
                    addIfAbsent(items, normalizeAiText(item.trim()));
                }
            }
        }
        if (StringUtils.isBlank(caseData.getEt1VettingCanServeClaimYesOrNo())) {
            addIfAbsent(items, "Can serve claim: Unable to determine automatically; please verify service details.");
        }
        if (CollectionUtils.isNotEmpty(caseData.getRespondentCollection())) {
            boolean hasAcasDoc = hasAcasCertificateDocument(caseData);
            int count = Math.min(caseData.getRespondentCollection().size(), 6);
            for (int idx = 1; idx <= count; idx++) {
                if (StringUtils.isBlank(getAcasCertFieldByNumber(caseData, idx))) {
                    addIfAbsent(items, "Respondent " + idx
                            + " ACAS certificate check: Unable to confirm automatically; please double-check.");
                }
                RespondentSumType respondent = caseData.getRespondentCollection().get(idx - 1).getValue();
                if (!hasAcasDoc && hasAcasDates(respondent)) {
                    addIfAbsent(items, "Respondent " + idx
                            + " ACAS certificate: No ACAS Certificate document is attached; respondent ACAS dates "
                            + "were used, so the actual certificate needs to be checked.");
                }
            }
        }
        if (StringUtils.isNotBlank(caseData.getEt1VettingEraAssessmentMarkUp())
                && StringUtils.isBlank(caseData.getEt1VettingEra())) {
            addIfAbsent(items, "ERA regulations assessment: Case triggered ERA review window; "
                    + "please confirm whether ERA applies.");
        }
        if (StringUtils.isBlank(caseData.getAreTheseCodesCorrect())) {
            addIfAbsent(items, "Jurisdiction codes: Unable to verify automatically whether codes are correct.");
        }
        if (StringUtils.isBlank(caseData.getIsTrackAllocationCorrect())) {
            addIfAbsent(items, "Track allocation: Unable to confirm automatically whether "
                    + "track allocation is correct.");
        }
        if (StringUtils.isBlank(caseData.getIsLocationCorrect())) {
            addIfAbsent(items, "Tribunal location: Unable to verify whether the current managing office is correct.");
        } else if ("No".equalsIgnoreCase(caseData.getIsLocationCorrect())
                && caseData.getRegionalOfficeList() == null) {
            addIfAbsent(items, "Regional office change: Location marked incorrect; please confirm target office.");
        }
        if (StringUtils.isBlank(caseData.getEt1SuggestHearingVenue())) {
            addIfAbsent(items, "Hearing venue suggestion: Left unanswered; please confirm if a specific hearing "
                    + "venue should be suggested.");
        }
        if (StringUtils.isBlank(caseData.getEt1GovOrMajorQuestion())
                || StringUtils.isBlank(caseData.getEt1ReasonableAdjustmentsQuestion())
                || StringUtils.isBlank(caseData.getEt1VideoHearingQuestion())) {
            addIfAbsent(items, "Further questions (Government/major employer, reasonable adjustments, or video "
                    + "hearing): One or more questions could not be determined automatically.");
        }
        return items;
    }

    private String getAcasCertFieldByNumber(CaseData caseData, int respondentNum) {
        return switch (respondentNum) {
            case 1 -> caseData.getEt1VettingAcasCertIsYesOrNo1();
            case 2 -> caseData.getEt1VettingAcasCertIsYesOrNo2();
            case 3 -> caseData.getEt1VettingAcasCertIsYesOrNo3();
            case 4 -> caseData.getEt1VettingAcasCertIsYesOrNo4();
            case 5 -> caseData.getEt1VettingAcasCertIsYesOrNo5();
            case 6 -> caseData.getEt1VettingAcasCertIsYesOrNo6();
            default -> null;
        };
    }

    private boolean hasRespondentAcasDates(CaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getRespondentCollection())) {
            return false;
        }
        return caseData.getRespondentCollection().stream()
                .map(RespondentSumTypeItem::getValue)
                .anyMatch(this::hasAcasDates);
    }

    private boolean hasAcasDates(RespondentSumType respondent) {
        return respondent != null
                && (StringUtils.isNotBlank(respondent.getAcasCertificateReceiptDate())
                || StringUtils.isNotBlank(respondent.getAcasCertificateIssueDate()));
    }

    private boolean hasAcasCertificateDocument(CaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getDocumentCollection())) {
            return false;
        }
        return caseData.getDocumentCollection().stream()
                .map(DocumentTypeItem::getValue)
                .anyMatch(this::isAcasCertificateDocument);
    }

    private boolean isAcasCertificateDocument(DocumentType docType) {
        if (docType == null || docType.getUploadedDocument() == null) {
            return false;
        }
        String filename = StringUtils.trimToEmpty(docType.getUploadedDocument().getDocumentFilename())
                .toLowerCase(Locale.ROOT);
        return ACAS_CERTIFICATE.equalsIgnoreCase(StringUtils.trimToEmpty(docType.getTypeOfDocument()))
                || ACAS_CERTIFICATE.equalsIgnoreCase(StringUtils.trimToEmpty(docType.getStartingClaimDocuments()))
                || ACAS_CERTIFICATE.equalsIgnoreCase(StringUtils.trimToEmpty(docType.getDocumentType()))
                || filename.contains("acas");
    }
}
