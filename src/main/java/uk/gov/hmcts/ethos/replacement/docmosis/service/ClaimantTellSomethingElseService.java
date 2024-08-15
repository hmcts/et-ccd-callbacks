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
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.citizenhub.ClaimantTse;
import uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.ClaimantTellSomethingElseHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.exceptions.EmailServiceException;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.TSEApplicationTypeData;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.et.common.model.ccd.types.citizenhub.ClaimantTse.CY_APP_TYPE_MAP;
import static uk.gov.hmcts.et.common.model.ccd.types.citizenhub.ClaimantTse.CY_MONTHS_MAP;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.CASE_NUMBER;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.CLAIMANT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.DATE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.DATE_PLUS_7;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.EMAIL_FLAG;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.EXUI_CASE_DETAILS_LINK;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.HEARING_DATE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.LINK_TO_DOCUMENT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.NOT_SET;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.NOT_SET_CY;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.RESPONDENTS;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.RESPONDENT_NAMES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.SHORT_TEXT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.WELSH_LANGUAGE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.WELSH_LANGUAGE_PARAM;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants.APPLICATION_COMPLETE_RULE92_ANSWERED_NO;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants.APPLICATION_COMPLETE_RULE92_ANSWERED_YES_RESP_OFFLINE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants.APPLICATION_COMPLETE_RULE92_ANSWERED_YES_RESP_ONLINE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants.APPLICATION_TYPE_MAP;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants.CLAIMANT_TSE_CHANGE_PERSONAL_DETAILS;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants.CLAIMANT_TSE_CONSIDER_DECISION_AFRESH;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants.CLAIMANT_TSE_ORDER_A_WITNESS_TO_ATTEND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants.CLAIMANT_TSE_RECONSIDER_JUDGMENT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants.CLAIMANT_TSE_WITHDRAW_CLAIM;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.ClaimantTellSomethingElseHelper.claimantSelectApplicationToType;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.DOCGEN_ERROR;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.DocumentHelper.createDocumentTypeItemFromTopLevel;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.getRespondentNames;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReferralHelper.getNearestHearingToReferral;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.TornadoService.CLAIMANT_TSE_FILE_NAME;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClaimantTellSomethingElseService {

    private final DocumentManagementService documentManagementService;
    private final TornadoService tornadoService;
    private final UserIdamService userIdamService;
    private final EmailService emailService;
    private final FeatureToggleService featureToggleService;
    private final TribunalOfficesService tribunalOfficesService;

    @Value("${template.tse.claimant-rep.application.claimant-rep-a}")
    private String tseClaimantRepAcknowledgeTypeATemplateId;
    @Value("${template.tse.claimant-rep.application.claimant-rep-b}")
    private String tseClaimantRepAcknowledgeTypeBTemplateId;
    @Value("${template.tse.claimant-rep.application.claimant-rep-c}")
    private String tseClaimantRepAcknowledgeTypeCTemplateId;
    @Value("${template.tse.claimant-rep.application.claimant-no}")
    private String tseClaimantRepAcknowledgeNoTemplateId;
    @Value("${template.tse.claimant-rep.application.respondent-type-a}")
    private String tseClaimantRepToRespAcknowledgeTypeATemplateId;
    @Value("${template.tse.claimant-rep.application.respondent-type-b}")
    private String tseClaimantRepToRespAcknowledgeTypeBTemplateId;
    @Value("${template.tse.claimant-rep.application.tribunal}")
    private String tseClaimantRepNewApplicationTemplateId;
    @Value("${template.tse.claimant-rep.application.cyRespondent-type-a}")
    private String cyTseClaimantToRespondentTypeATemplateId;
    @Value("${template.tse.claimant-rep.application.cyRespondent-type-b}")
    private String cyTseClaimantToRespondentTypeBTemplateId;

    private static final List<String> GROUP_B_TYPES = List.of(CLAIMANT_TSE_WITHDRAW_CLAIM,
            CLAIMANT_TSE_CHANGE_PERSONAL_DETAILS, CLAIMANT_TSE_CONSIDER_DECISION_AFRESH,
            CLAIMANT_TSE_RECONSIDER_JUDGMENT);

    public List<String> validateGiveDetails(CaseData caseData) {
        List<String> errors = new ArrayList<>();
        TSEApplicationTypeData selectedAppData =
                ClaimantTellSomethingElseHelper.getSelectedApplicationType(caseData);
        if (selectedAppData.getUploadedTseDocument() == null && isNullOrEmpty(selectedAppData.getSelectedTextBox())) {
            errors.add(TSEConstants.GIVE_DETAIL_MISSING);
        }
        return errors;
    }

    public void populateClaimantTse(CaseData caseData) {
        ClaimantTse claimantTse = new ClaimantTse();
        claimantTse.setContactApplicationType(caseData.getClaimantTseSelectApplication());
        claimantTse.setCopyToOtherPartyYesOrNo(caseData.getClaimantTseRule92());
        claimantTse.setCopyToOtherPartyText(caseData.getClaimantTseRule92AnsNoGiveDetails());

        TSEApplicationTypeData selectedAppData =
                ClaimantTellSomethingElseHelper.getSelectedApplicationType(caseData);
        claimantTse.setContactApplicationText(selectedAppData.getSelectedTextBox());
        claimantTse.setContactApplicationFile(selectedAppData.getUploadedTseDocument());
        caseData.setClaimantTse(claimantTse);
    }

    public void generateAndAddApplicationPdf(CaseData caseData, String userToken, String caseTypeId) {
        try {
            if (isEmpty(caseData.getDocumentCollection())) {
                caseData.setDocumentCollection(new ArrayList<>());
            }

            String selectApplicationType = claimantSelectApplicationToType(caseData.getClaimantTseSelectApplication());
            String applicationDocMapping =
                    DocumentHelper.claimantApplicationTypeToDocType(selectApplicationType);
            String topLevel = DocumentHelper.getTopLevelDocument(applicationDocMapping);
            DocumentInfo documentInfo =
                    tornadoService.generateEventDocument(caseData, userToken, caseTypeId, CLAIMANT_TSE_FILE_NAME);
            caseData.setDocMarkUp(documentInfo.getMarkUp().replace("Document", "Download a copy of your application"));
            caseData.getDocumentCollection().add(createDocumentTypeItemFromTopLevel(
                    documentManagementService.addDocumentToDocumentField(documentInfo),
                    topLevel,
                    applicationDocMapping,
                    caseData.getClaimantTseSelectApplication()
            ));

        } catch (Exception e) {
            throw new DocumentManagementException(String.format(DOCGEN_ERROR, caseData.getEthosCaseReference()), e);
        }
    }

    public String buildApplicationCompleteResponse(CaseData caseData) {
        List<GenericTseApplicationTypeItem> tseApplicationCollection =
                caseData.getGenericTseApplicationCollection();
        GenericTseApplicationTypeItem latestTSEApplication =
                tseApplicationCollection.get(tseApplicationCollection.size() - 1);

        String ansRule92 = latestTSEApplication.getValue().getCopyToOtherPartyYesOrNo();
        String tseRespNotAvailability = caseData.getClaimantTseRespNotAvailable();
        String body;
        if (YES.equals(ansRule92)) {
            if (YES.equals(tseRespNotAvailability)) {
                body = String.format(APPLICATION_COMPLETE_RULE92_ANSWERED_YES_RESP_OFFLINE,
                        caseData.getDocMarkUp());
            } else {
                body = String.format(APPLICATION_COMPLETE_RULE92_ANSWERED_YES_RESP_ONLINE,
                        UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 7), caseData.getDocMarkUp());
            }
        } else {
            body = String.format(APPLICATION_COMPLETE_RULE92_ANSWERED_NO, caseData.getDocMarkUp());
        }
        return body;
    }

    public void sendAcknowledgementEmail(CaseDetails caseDetails, String userToken) {
        String email = userIdamService.getUserDetails(userToken).getEmail();
        CaseData caseData = caseDetails.getCaseData();
        String applicationType = caseData.getClaimantTseSelectApplication();

        if (CLAIMANT_TSE_ORDER_A_WITNESS_TO_ATTEND.equals(applicationType)) {
            emailService.sendEmail(
                    tseClaimantRepAcknowledgeTypeCTemplateId,
                    email,
                    prepareEmailContentTypeC(caseDetails));
            return;
        }

        if (NO.equals(caseData.getClaimantTseRule92())) {
            emailService.sendEmail(
                    tseClaimantRepAcknowledgeNoTemplateId,
                    email,
                    prepareEmailContent(caseDetails));
            return;
        }

        emailService.sendEmail(
                GROUP_B_TYPES.contains(applicationType)
                        ? tseClaimantRepAcknowledgeTypeBTemplateId
                        : tseClaimantRepAcknowledgeTypeATemplateId,
                email,
                prepareEmailContent(caseDetails));
    }

    private Map<String, String> prepareEmailContentTypeC(CaseDetails caseDetails) {
        CaseData caseData = caseDetails.getCaseData();
        return Map.of(
                CASE_NUMBER, caseData.getEthosCaseReference(),
                CLAIMANT, caseData.getClaimant(),
                RESPONDENT_NAMES, getRespondentNames(caseData),
                EXUI_CASE_DETAILS_LINK, emailService.getExuiCaseLink(caseDetails.getCaseId())
        );
    }

    private Map<String, String> prepareEmailContent(CaseDetails caseDetails) {
        CaseData caseData = caseDetails.getCaseData();

        Map<String, String> content = new ConcurrentHashMap<>();
        content.put(CASE_NUMBER, caseData.getEthosCaseReference());
        content.put(CLAIMANT, caseData.getClaimant());
        content.put(RESPONDENT_NAMES, getRespondentNames(caseData));
        content.put(HEARING_DATE, getNearestHearingToReferral(caseData, NOT_SET));
        content.put(SHORT_TEXT, caseData.getClaimantTseSelectApplication());
        content.put(EXUI_CASE_DETAILS_LINK, emailService.getExuiCaseLink(caseDetails.getCaseId()));

        return content;
    }

    public void sendRespondentsEmail(CaseDetails caseDetails) {
        CaseData caseData = caseDetails.getCaseData();
        String applicationType = caseData.getClaimantTseSelectApplication();

        if (CLAIMANT_TSE_ORDER_A_WITNESS_TO_ATTEND.equals(applicationType)
            || NO.equals(caseData.getClaimantTseRule92())
            || YES.equals(caseData.getClaimantTseRespNotAvailable())) {
            return;
        }

        List<String> respondentEmailAddressList = getRespondentEmailAddressList(caseData);
        boolean isWelsh = featureToggleService.isWelshEnabled()
                && WELSH_LANGUAGE.equals(caseData.getClaimantHearingPreference().getContactLanguage());

        try {
            log.info("Sending application email to respondent on case {}", caseData.getEthosCaseReference());
            byte[] bytes = tornadoService.generateEventDocumentBytes(caseData, "", CLAIMANT_TSE_FILE_NAME);
            String emailTemplate = getRespondentEmailTemplate(isWelsh, applicationType);
            log.info("Email template: {}", emailTemplate);
            respondentEmailAddressList.forEach(respondentEmail ->
                    sendEmailToRespondent(
                            emailTemplate,
                            respondentEmail,
                            caseDetails,
                            bytes,
                            isWelsh));
        } catch (Exception e) {
            throw new DocumentManagementException(String.format(DOCGEN_ERROR, caseData.getEthosCaseReference()), e);
        }
    }

    private void sendEmailToRespondent(String emailTemplate, String respondentEmail, CaseDetails caseDetails,
                                       byte[] bytes, boolean isWelsh) {
        CaseData caseData = caseDetails.getCaseData();
        try {
            emailService.sendEmail(
                    emailTemplate,
                    respondentEmail,
                    prepareRespondentEmailContent(caseDetails, bytes, isWelsh)
            );
        } catch (NotificationClientException e) {
            log.warn("Failed to send email. Reference ID: {}. Reason:", caseData.getEthosCaseReference(), e);
            throw new EmailServiceException("Failed to send email", e);
        }
    }

    private List<String> getRespondentEmailAddressList(CaseData caseData) {
        return caseData.getRepCollection().stream()
                .filter(r -> Objects.nonNull(r)
                        && YES.equals(defaultIfEmpty(r.getValue().getMyHmctsYesNo(), ""))
                        && !isNullOrEmpty(r.getValue().getRepresentativeEmailAddress()))
                .map(r -> r.getValue().getRepresentativeEmailAddress())
                .toList();
    }

    private String getRespondentEmailTemplate(boolean isWelsh, String applicationType) {
        if (GROUP_B_TYPES.contains(applicationType)) {
            return isWelsh
                    ? cyTseClaimantToRespondentTypeBTemplateId
                    : tseClaimantRepToRespAcknowledgeTypeBTemplateId;
        } else {
            return isWelsh
                    ? cyTseClaimantToRespondentTypeATemplateId
                    : tseClaimantRepToRespAcknowledgeTypeATemplateId;
        }
    }

    private Map<String, Object> prepareRespondentEmailContent(CaseDetails caseDetails, byte[] document,
                                                              boolean isWelsh) throws NotificationClientException {
        CaseData caseData = caseDetails.getCaseData();
        JSONObject documentJson =
                NotificationClient.prepareUpload(document, false, true, "52 weeks");
        String shortText = isWelsh
                ? CY_APP_TYPE_MAP.get(APPLICATION_TYPE_MAP.get(caseData.getClaimantTseSelectApplication()))
                : caseData.getClaimantTseSelectApplication();
        String datePlus7 = isWelsh
                ? translateDateToWelsh(UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 7))
                : UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 7);
        String exuiCaseDetailsLink = emailService.getExuiCaseLink(
                caseDetails.getCaseId()) + (isWelsh ? WELSH_LANGUAGE_PARAM : "");

        return Map.of(
                CLAIMANT, caseData.getClaimant(),
                RESPONDENT_NAMES, getRespondentNames(caseData),
                CASE_NUMBER, caseData.getEthosCaseReference(),
                HEARING_DATE, getNearestHearingToReferralForRespondent(caseData, isWelsh),
                SHORT_TEXT, shortText,
                DATE_PLUS_7, datePlus7,
                LINK_TO_DOCUMENT, documentJson,
                EXUI_CASE_DETAILS_LINK, exuiCaseDetailsLink
        );
    }

    private String translateDateToWelsh(String date) {
        return CY_MONTHS_MAP.entrySet().stream()
                .filter(entry -> date.contains(entry.getKey()))
                .findFirst()
                .map(entry -> date.replace(entry.getKey(), entry.getValue()))
                .orElse(date);
    }

    private String getNearestHearingToReferralForRespondent(CaseData caseData, boolean isWelsh) {
        String hearingDate = getNearestHearingToReferral(caseData.getHearingCollection(), NOT_SET);
        if (isWelsh) {
            return NOT_SET.equals(hearingDate)
                    ? NOT_SET_CY
                    : translateDateToWelsh(hearingDate);
        }
        return hearingDate;
    }

    public void sendAdminEmail(CaseDetails caseDetails) {
        String email = getTribunalEmail(caseDetails.getCaseData());
        if (isNullOrEmpty(email)) {
            return;
        }

        Map<String, String> personalisation = prepareContentAdminEmail(caseDetails);
        emailService.sendEmail(tseClaimantRepNewApplicationTemplateId, email, personalisation);
    }

    public String getTribunalEmail(CaseData caseData) {
        String managingOffice = caseData.getManagingOffice();
        TribunalOffice tribunalOffice = tribunalOfficesService.getTribunalOffice(managingOffice);

        if (tribunalOffice == null) {
            return null;
        }

        return tribunalOffice.getOfficeEmail();
    }

    private Map<String, String> prepareContentAdminEmail(CaseDetails caseDetails) {
        CaseData caseData = caseDetails.getCaseData();

        Map<String, String> content = new ConcurrentHashMap<>();
        content.put(CASE_NUMBER, caseData.getEthosCaseReference());
        content.put(EMAIL_FLAG, "");
        content.put(CLAIMANT, caseData.getClaimant());
        content.put(DATE, getNearestHearingToReferral(caseData, NOT_SET));
        content.put("url", emailService.getExuiCaseLink(caseDetails.getCaseId()));
        content.put(RESPONDENTS, getRespondentNames(caseData));
        return content;
    }
  
    public String generateClaimantApplicationTableMarkdown(CaseData caseData) {
        return ClaimantTellSomethingElseHelper.generateClaimantRepApplicationMarkdown(caseData);
    }
}
