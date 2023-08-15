package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.exceptions.DocumentManagementException;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReferralHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.RespondentTellSomethingElseHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.TseViewApplicationHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.RespondentTSEApplicationTypeData;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.time.LocalDate;
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
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.APPLICATION_TYPE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.CASE_NUMBER;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.CLAIMANT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.DATE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.EMAIL_FLAG;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.LINK_TO_CITIZEN_HUB;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.LINK_TO_EXUI;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.RESPONDENTS;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.DocumentHelper.createDocumentTypeItem;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.getRespondentNames;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.TornadoService.TSE_FILE_NAME;

@Slf4j
@Service
@RequiredArgsConstructor
public class RespondentTellSomethingElseService {
    private final EmailService emailService;
    private final UserService userService;
    private final TribunalOfficesService tribunalOfficesService;
    private final TornadoService tornadoService;
    private final DocumentManagementService documentManagementService;

    @Value("${template.tse.respondent.application.respondent}")
    private String tseRespondentAcknowledgeTemplateId;
    @Value("${template.tse.respondent.application.respondent-type-c}")
    private String tseRespondentAcknowledgeTypeCTemplateId;
    @Value("${template.tse.respondent.application.claimant}")
    private String tseRespondentToClaimantTemplateId;
    @Value("${template.tse.respondent.application.tribunal}")
    private String tseNewApplicationAdminTemplateId;
    private static final String GIVE_DETAIL_MISSING = "Use the text box or file upload to give details.";
    private static final List<String> GROUP_B_TYPES = List.of(TSE_APP_CHANGE_PERSONAL_DETAILS,
            TSE_APP_CONSIDER_A_DECISION_AFRESH, TSE_APP_RECONSIDER_JUDGEMENT);
    private static final String DOCGEN_ERROR = "Failed to generate document for case id: %s";
    private static final String RULE92_ANSWERED_NO = "You have said that you do not want to copy this correspondence "
            + "to the other party. \n \n"
            + "The tribunal will consider all correspondence and let you know what happens next.";
    private static final String RULE92_ANSWERED_YES_GROUP_A = "The other party will be notified that any objections to "
            + "your %s application should be sent to the tribunal as soon as possible, and in any event within 7 days.";
    private static final String RULE92_ANSWERED_YES_GROUP_B = "The other party is not expected to respond to this "
            + "application.\n \nHowever, they have been notified that any objections to your %s application should be "
            + "sent to the tribunal as soon as possible, and in any event within 7 days.";
    private static final String CLAIMANT_EMAIL_GROUP_B = "You are not expected to respond to this application"
            + ".\r\n\r\nIf you do respond you should do so as soon as possible and in any event by %s.";
    private static final String CLAIMANT_EMAIL_GROUP_A = "You should respond as soon as possible, and in any "
            + "event by %s.";

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
        RespondentTSEApplicationTypeData selectedAppData =
                RespondentTellSomethingElseHelper.getSelectedApplicationType(caseData);
        if (selectedAppData.getResTseDocument() == null && isNullOrEmpty(selectedAppData.getSelectedTextBox())) {
            errors.add(GIVE_DETAIL_MISSING);
        }
        return errors;
    }

    /**
     * Uses {@link EmailService} to generate an email to Respondent.
     * Uses {@link UserService} to get Respondents email address.
     *
     * @param caseDetails in which the case details are extracted from
     * @param userToken   jwt used for authorization
     */
    public void sendAcknowledgeEmail(CaseDetails caseDetails, String userToken) {
        CaseData caseData = caseDetails.getCaseData();

        String email = userService.getUserDetails(userToken).getEmail();

        if (TSE_APP_ORDER_A_WITNESS_TO_ATTEND_TO_GIVE_EVIDENCE.equals(caseData.getResTseSelectApplication())) {
            emailService.sendEmail(tseRespondentAcknowledgeTypeCTemplateId, email,
                    buildPersonalisationTypeC(caseDetails));
            return;
        }

        if (NO.equals(caseData.getResTseCopyToOtherPartyYesOrNo())) {
            emailService.sendEmail(tseRespondentAcknowledgeTemplateId, email,
                    buildPersonalisation(caseDetails, RULE92_ANSWERED_NO));
            return;
        }

        String customisedText;

        if (GROUP_B_TYPES.contains(caseData.getResTseSelectApplication())) {
            customisedText = String.format(RULE92_ANSWERED_YES_GROUP_B, caseData.getResTseSelectApplication());
        } else {
            customisedText = String.format(RULE92_ANSWERED_YES_GROUP_A, caseData.getResTseSelectApplication());
        }

        emailService.sendEmail(tseRespondentAcknowledgeTemplateId, email,
                buildPersonalisation(caseDetails, customisedText));
    }

    private Map<String, String> buildPersonalisationTypeC(CaseDetails caseDetails) {
        CaseData caseData = caseDetails.getCaseData();
        return Map.of(
                CASE_NUMBER, caseData.getEthosCaseReference(),
                CLAIMANT, caseData.getClaimant(),
                RESPONDENTS, getRespondentNames(caseData),
                LINK_TO_EXUI, emailService.getExuiCaseLink(caseDetails.getCaseId())
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
                || caseData.getClaimantType().getClaimantEmailAddress() == null) {
            return;
        }

        String claimantEmail = caseData.getClaimantType().getClaimantEmailAddress();
        String instructions;
        String dueDate = UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 7);

        if (GROUP_B_TYPES.contains(caseData.getResTseSelectApplication())) {
            instructions = String.format(CLAIMANT_EMAIL_GROUP_B, dueDate);
        } else {
            instructions = String.format(CLAIMANT_EMAIL_GROUP_A, dueDate);
        }

        try {
            byte[] bytes = tornadoService.generateEventDocumentBytes(caseData, "", TSE_FILE_NAME);
            Map<String, Object> personalisation = claimantPersonalisation(caseDetails, instructions, bytes);
            emailService.sendEmail(tseRespondentToClaimantTemplateId, claimantEmail, personalisation);
        } catch (Exception e) {
            throw new DocumentManagementException(String.format(DOCGEN_ERROR, caseData.getEthosCaseReference()), e);
        }
    }

    private Map<String, String> buildPersonalisation(CaseDetails detail, String customisedText) {
        CaseData caseData = detail.getCaseData();
        Map<String, String> personalisation = new ConcurrentHashMap<>();
        personalisation.put(CASE_NUMBER, caseData.getEthosCaseReference());
        personalisation.put(CLAIMANT, caseData.getClaimant());
        personalisation.put(RESPONDENTS, getRespondentNames(caseData));
        personalisation.put("customisedText", customisedText);
        personalisation.put("shortText", caseData.getResTseSelectApplication());
        personalisation.put(LINK_TO_EXUI, emailService.getExuiCaseLink(detail.getCaseId()));
        return personalisation;
    }

    /**
     * Builds personalisation for sending an email to the claimant.
     *
     * @param caseDetails  Details about the case
     * @param instructions Instructions to be included
     * @param document     document to link off to
     * @return KeyValue mappings needed to populate the email
     * @throws NotificationClientException When the document cannot be uploaded
     */
    public Map<String, Object> claimantPersonalisation(CaseDetails caseDetails, String instructions, byte[] document)
            throws NotificationClientException {

        CaseData caseData = caseDetails.getCaseData();
        JSONObject documentJson = NotificationClient.prepareUpload(document, false, true, "52 weeks");

        return Map.of(
                LINK_TO_CITIZEN_HUB, emailService.getCitizenCaseLink(caseDetails.getCaseId()),
                CASE_NUMBER, caseData.getEthosCaseReference(),
                APPLICATION_TYPE, caseData.getResTseSelectApplication(),
                "instructions", instructions,
                CLAIMANT, caseData.getClaimant(),
                RESPONDENTS, getRespondentNames(caseData),
                "linkToDocument", documentJson
        );
    }

    public String generateTableMarkdown(CaseData caseData) {
        List<GenericTseApplicationTypeItem> genericApplicationList = caseData.getGenericTseApplicationCollection();
        if (CollectionUtils.isEmpty(genericApplicationList)) {
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
        personalisation.put(DATE, ReferralHelper.getNearestHearingToReferral(caseData, "Not set"));
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
            caseData.getDocumentCollection().add(createDocumentTypeItem(
                documentManagementService.addDocumentToDocumentField(
                    tornadoService.generateEventDocument(caseData, userToken, caseTypeId, TSE_FILE_NAME)
                ),
                "Respondent correspondence",
                caseData.getResTseSelectApplication()
            ));
        } catch (Exception e) {
            throw new DocumentManagementException(String.format(DOCGEN_ERROR, caseData.getEthosCaseReference()), e);
        }
    }
}
