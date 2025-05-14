package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.exceptions.DocumentManagementException;
import uk.gov.hmcts.ecm.common.helpers.DocumentHelper;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.RespondentTellSomethingElseHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.TseViewApplicationHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.TSEApplicationTypeData;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.RetentionPeriodDuration;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.OPEN_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TSE_APP_CHANGE_PERSONAL_DETAILS;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TSE_APP_CONSIDER_A_DECISION_AFRESH;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TSE_APP_ORDER_A_WITNESS_TO_ATTEND_TO_GIVE_EVIDENCE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TSE_APP_RECONSIDER_JUDGEMENT;
import static uk.gov.hmcts.et.common.model.ccd.types.citizenhub.ClaimantTse.CY_MONTHS_MAP;
import static uk.gov.hmcts.et.common.model.ccd.types.citizenhub.ClaimantTse.CY_RESPONDENT_APP_TYPE_MAP;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.CASE_NUMBER;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.CLAIMANT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.DATE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.DATE_PLUS_7;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.EMAIL_FLAG;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.EXUI_CASE_DETAILS_LINK;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.HEARING_DATE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.LINK_TO_CITIZEN_HUB;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.LINK_TO_DOCUMENT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.NOT_SET;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.NOT_SET_CY;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.RESPONDENTS;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.RESPONDENT_NAMES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.SHORT_TEXT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.WELSH_LANGUAGE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.WELSH_LANGUAGE_PARAM;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.DocumentHelper.createDocumentTypeItemFromTopLevel;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.getRespondentNames;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.isClaimantNonSystemUser;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReferralHelper.getNearestHearingToReferral;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.TornadoService.TSE_FILE_NAME;

@Slf4j
@Service
@RequiredArgsConstructor
public class RespondentTellSomethingElseService {
    private final EmailService emailService;
    private final UserIdamService userIdamService;
    private final TribunalOfficesService tribunalOfficesService;
    private final TornadoService tornadoService;
    private final DocumentManagementService documentManagementService;
    private final FeatureToggleService featureToggleService;

    @Value("${template.tse.respondent.application.respondent-no}")
    private String tseRespondentAcknowledgeNoTemplateId;
    @Value("${template.tse.respondent.application.respondent-type-a}")
    private String tseRespondentAcknowledgeTypeATemplateId;
    @Value("${template.tse.respondent.application.respondent-type-b}")
    private String tseRespondentAcknowledgeTypeBTemplateId;
    @Value("${template.tse.respondent.application.respondent-type-c}")
    private String tseRespondentAcknowledgeTypeCTemplateId;
    @Value("${template.tse.respondent.application.claimant-a}")
    private String tseRespondentToClaimantTypeATemplateId;
    @Value("${template.tse.respondent.application.claimant-b}")
    private String tseRespondentToClaimantTypeBTemplateId;
    @Value("${template.tse.respondent.application.cyClaimant-a}")
    private String cyTseRespondentToClaimantTypeATemplateId;
    @Value("${template.tse.respondent.application.cyClaimant-b}")
    private String cyTseRespondentToClaimantTypeBTemplateId;
    @Value("${template.tse.respondent.application.tribunal}")
    private String tseNewApplicationAdminTemplateId;
    private static final String GIVE_DETAIL_MISSING = "Use the text box or file upload to give details.";
    private static final List<String> GROUP_B_TYPES = List.of(TSE_APP_CHANGE_PERSONAL_DETAILS,
            TSE_APP_CONSIDER_A_DECISION_AFRESH, TSE_APP_RECONSIDER_JUDGEMENT);
    private static final String DOCGEN_ERROR = "Failed to generate document for case id: %s";

    private static final String EMPTY_TABLE_MESSAGE = "There are no applications to view";
    private static final String TABLE_COLUMNS_MARKDOWN =
            "| No | Application type | Applicant | Application date | Response due | Number of responses | Status |\r\n"
                    + "|:---------|:---------|:---------|:---------|:---------|:---------|:---------|\r\n"
                    + "%s\r\n";

    private static final String TABLE_ROW_MARKDOWN = "|%s|%s|%s|%s|%s|%s|%s|\r\n";

    /**
     * Validate Give Details (free text box) or file upload is mandatory.
     *
     * @param caseData in which the case details are extracted from
     * @return errors Error message
     */
    public List<String> validateGiveDetails(CaseData caseData) {
        List<String> errors = new ArrayList<>();
        TSEApplicationTypeData selectedAppData =
                RespondentTellSomethingElseHelper.getSelectedApplicationType(caseData);
        if (selectedAppData.getUploadedTseDocument() == null && isNullOrEmpty(selectedAppData.getSelectedTextBox())) {
            errors.add(GIVE_DETAIL_MISSING);
        }
        return errors;
    }

    /**
     * Uses {@link EmailService} to generate an email to Respondent.
     * Uses {@link UserIdamService} to get Respondents email address.
     *
     * @param caseDetails in which the case details are extracted from
     * @param userToken   jwt used for authorization
     */
    public void sendAcknowledgeEmail(CaseDetails caseDetails, String userToken) {
        CaseData caseData = caseDetails.getCaseData();

        String email = userIdamService.getUserDetails(userToken).getEmail();

        if (TSE_APP_ORDER_A_WITNESS_TO_ATTEND_TO_GIVE_EVIDENCE.equals(caseData.getResTseSelectApplication())) {
            emailService.sendEmail(
                tseRespondentAcknowledgeTypeCTemplateId,
                email,
                buildPersonalisationTypeC(caseDetails));
            return;
        }

        if (NO.equals(caseData.getResTseCopyToOtherPartyYesOrNo())) {
            emailService.sendEmail(
                tseRespondentAcknowledgeNoTemplateId,
                email,
                buildPersonalisation(caseDetails));
            return;
        }

        emailService.sendEmail(
            GROUP_B_TYPES.contains(caseData.getResTseSelectApplication())
                ? tseRespondentAcknowledgeTypeBTemplateId
                : tseRespondentAcknowledgeTypeATemplateId,
            email,
            buildPersonalisation(caseDetails));
    }

    private Map<String, String> buildPersonalisationTypeC(CaseDetails caseDetails) {
        CaseData caseData = caseDetails.getCaseData();
        return Map.of(
            CASE_NUMBER, caseData.getEthosCaseReference(),
            CLAIMANT, caseData.getClaimant(),
            RESPONDENT_NAMES, getRespondentNames(caseData),
            EXUI_CASE_DETAILS_LINK, emailService.getExuiCaseLink(caseDetails.getCaseId())
        );
    }

    private Map<String, String> buildPersonalisation(CaseDetails detail) {
        CaseData caseData = detail.getCaseData();
        return Map.of(
            CASE_NUMBER, caseData.getEthosCaseReference(),
            CLAIMANT, caseData.getClaimant(),
            RESPONDENT_NAMES, getRespondentNames(caseData),
            HEARING_DATE, getNearestHearingToReferral(caseData, NOT_SET),
            SHORT_TEXT, caseData.getResTseSelectApplication(),
            EXUI_CASE_DETAILS_LINK, emailService.getExuiCaseLink(detail.getCaseId())
        );
    }

    /**
     * Uses {@link EmailService} to generate an email to Claimant.
     *
     * @param caseDetails in which the case details are extracted from
     */
    public void sendClaimantEmail(CaseDetails caseDetails) {
        CaseData caseData = caseDetails.getCaseData();

        if (TSE_APP_ORDER_A_WITNESS_TO_ATTEND_TO_GIVE_EVIDENCE.equals(caseData.getResTseSelectApplication())
                || NO.equals(caseData.getResTseCopyToOtherPartyYesOrNo())
                || caseData.getClaimantType().getClaimantEmailAddress() == null
                || isClaimantNonSystemUser(caseDetails.getCaseData())) {
            return;
        }

        String claimantEmail = caseData.getClaimantType().getClaimantEmailAddress();

        boolean isWelsh = featureToggleService.isWelshEnabled()
                && Optional.ofNullable(caseData.getClaimantHearingPreference())
                .map(preference -> WELSH_LANGUAGE.equals(preference.getContactLanguage()))
                .orElse(false);

        try {
            byte[] bytes = tornadoService.generateEventDocumentBytes(caseData, "", TSE_FILE_NAME);
            emailService.sendEmail(
                getEmailTemplate(isWelsh, caseData.getResTseSelectApplication()),
                claimantEmail,
                claimantPersonalisation(caseDetails, bytes, isWelsh)
            );
        } catch (Exception e) {
            throw new DocumentManagementException(String.format(DOCGEN_ERROR, caseData.getEthosCaseReference()), e);
        }
    }

    private String getEmailTemplate(boolean isWelsh, String applicationType) {
        if (GROUP_B_TYPES.contains(applicationType)) {
            return isWelsh
                ? cyTseRespondentToClaimantTypeBTemplateId
                : tseRespondentToClaimantTypeBTemplateId;
        } else {
            return isWelsh
                ? cyTseRespondentToClaimantTypeATemplateId
                : tseRespondentToClaimantTypeATemplateId;
        }
    }

    /**
     * Builds personalisation for sending an email to the claimant.
     *
     * @param caseDetails  Details about the case
     * @param document     document to link off to
     * @return KeyValue mappings needed to populate the email
     * @throws NotificationClientException When the document cannot be uploaded
     */
    private Map<String, Object> claimantPersonalisation(CaseDetails caseDetails, byte[] document, boolean isWelsh)
            throws NotificationClientException {

        CaseData caseData = caseDetails.getCaseData();
        JSONObject documentJson = NotificationClient.prepareUpload(document, true, new RetentionPeriodDuration(52, ChronoUnit.WEEKS));
        String shortText = isWelsh
                ? CY_RESPONDENT_APP_TYPE_MAP.get(caseData.getResTseSelectApplication())
                : caseData.getResTseSelectApplication();
        String datePlus7 = isWelsh
                ? translateDateToWelsh(UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 7))
                : UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 7);
        String citizenPortalLink = emailService.getCitizenCaseLink(
                caseDetails.getCaseId()) + (isWelsh ? WELSH_LANGUAGE_PARAM : "");

        return Map.of(
                CLAIMANT, caseData.getClaimant(),
                RESPONDENT_NAMES, getRespondentNames(caseData),
                CASE_NUMBER, caseData.getEthosCaseReference(),
                HEARING_DATE, getNearestHearingToReferralForClaimant(caseData, isWelsh),
                SHORT_TEXT, shortText,
                DATE_PLUS_7, datePlus7,
                LINK_TO_DOCUMENT, documentJson,
                LINK_TO_CITIZEN_HUB, citizenPortalLink
        );
    }

    private String getNearestHearingToReferralForClaimant(CaseData caseData, boolean isWelsh) {
        String hearingDate = getNearestHearingToReferral(caseData.getHearingCollection(), NOT_SET);
        if (isWelsh) {
            return NOT_SET.equals(hearingDate)
                ? NOT_SET_CY
                : translateDateToWelsh(hearingDate);
        }
        return hearingDate;
    }

    private String translateDateToWelsh(String date) {
        return CY_MONTHS_MAP.entrySet().stream()
            .filter(entry -> date.contains(entry.getKey()))
            .findFirst()
            .map(entry -> date.replace(entry.getKey(), entry.getValue()))
            .orElse(date);
    }

    public String generateTableMarkdown(CaseData caseData) {
        List<GenericTseApplicationTypeItem> genericApplicationList = caseData.getGenericTseApplicationCollection();
        if (isEmpty(genericApplicationList)) {
            return EMPTY_TABLE_MESSAGE;
        }

        AtomicInteger applicationNumber = new AtomicInteger(1);

        String tableRows = genericApplicationList.stream()
                .filter(TseViewApplicationHelper::applicationsSharedWithRespondent)
                .map(o -> formatRow(o, applicationNumber))
                .collect(Collectors.joining());

        return String.format(TABLE_COLUMNS_MARKDOWN, tableRows);
    }

    private String formatRow(GenericTseApplicationTypeItem genericTseApplicationTypeItem, AtomicInteger count) {
        GenericTseApplicationType value = genericTseApplicationTypeItem.getValue();
        int responses = value.getRespondCollection() == null ? 0 : value.getRespondCollection().size();
        String status = Optional.ofNullable(value.getStatus()).orElse(OPEN_STATE);

        return String.format(TABLE_ROW_MARKDOWN, count.getAndIncrement(), value.getType(), value.getApplicant(),
                value.getDate(), value.getDueDate(), responses, status);
    }

    /**
     * Sends an email notifying the admin that an application has been created/replied to.
     */
    public void sendAdminEmail(CaseDetails caseDetails) {
        String email = getTribunalEmail(caseDetails.getCaseData());
        if (isNullOrEmpty(email)) {
            return;
        }

        Map<String, String> personalisation = buildPersonalisationForAdminEmail(caseDetails);
        emailService.sendEmail(tseNewApplicationAdminTemplateId, email, personalisation);
    }

    public String getTribunalEmail(CaseData caseData) {
        String managingOffice = caseData.getManagingOffice();
        TribunalOffice tribunalOffice = tribunalOfficesService.getTribunalOffice(managingOffice);

        if (tribunalOffice == null) {
            return null;
        }

        return tribunalOffice.getOfficeEmail();
    }

    /**
     * Builds personalisation data for sending an email to the admin about an application.
     */
    public Map<String, String> buildPersonalisationForAdminEmail(CaseDetails caseDetails) {
        CaseData caseData = caseDetails.getCaseData();
        Map<String, String> personalisation = new ConcurrentHashMap<>();
        personalisation.put(CASE_NUMBER, caseData.getEthosCaseReference());
        personalisation.put(EMAIL_FLAG, "");
        personalisation.put(CLAIMANT, caseData.getClaimant());
        personalisation.put(RESPONDENTS, getRespondentNames(caseData));
        personalisation.put(DATE, getNearestHearingToReferral(caseData, NOT_SET));
        personalisation.put("url", emailService.getExuiCaseLink(caseDetails.getCaseId()));
        return personalisation;
    }

    /**
     * Generates and adds a pdf to summarise the respondent's TSE application to the document collection. Sets type
     * of document to Respondent correspondence and sets the short description to a description of the application
     * type.
     *
     * @param caseData contains all the case data
     * @param userToken token used for authorisation
     * @param caseTypeId reference which casetype the document will be uploaded to
     */
    public void generateAndAddTsePdf(CaseData caseData, String userToken, String caseTypeId) {
        try {
            if (isEmpty(caseData.getDocumentCollection())) {
                caseData.setDocumentCollection(new ArrayList<>());
            }
            String applicationDocMapping =
                    DocumentHelper.respondentApplicationToDocType(caseData.getResTseSelectApplication());
            String topLevel = DocumentHelper.getTopLevelDocument(applicationDocMapping);
            caseData.getDocumentCollection().add(createDocumentTypeItemFromTopLevel(
                    documentManagementService.addDocumentToDocumentField(
                            tornadoService.generateEventDocument(caseData, userToken, caseTypeId, TSE_FILE_NAME)
                    ),
                    topLevel,
                    applicationDocMapping,
                    caseData.getResTseSelectApplication()
            ));

        } catch (Exception e) {
            throw new DocumentManagementException(String.format(DOCGEN_ERROR, caseData.getEthosCaseReference()), e);
        }
    }
}
