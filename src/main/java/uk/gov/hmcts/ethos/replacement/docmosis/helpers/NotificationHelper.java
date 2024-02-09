package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.webjars.NotFoundException;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantIndType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantType;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationType;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NOT_STARTED_YET;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NOT_VIEWED_YET;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_ONLY;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SEND_NOTIFICATION_RESPONSE_REQUIRED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TRIBUNAL;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.CASE_NUMBER;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.CCD_ID;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.SendNotificationService.CASE_MANAGEMENT_ORDERS_REQUESTS;

@Slf4j
public final class NotificationHelper {
    private NotificationHelper() {
        // Access through static methods
    }

    /**
     * Formats message for names of parties that notifications will be sent to.
     */
    public static String getParties(CaseData caseData) {
        return String.format("%s, %s", getNameForClaimant(caseData), getNameOfRespondents(caseData));
    }

    public static Map<String, String> buildMapForClaimant(CaseData caseData, String caseId) {
        Map<String, String> personalisation = new ConcurrentHashMap<>();
        personalisation.put(CASE_NUMBER, caseData.getEthosCaseReference());
        personalisation.put("emailAddress", getEmailAddressForClaimant(caseData));
        personalisation.put(CCD_ID, caseId);
        RepresentedTypeC representativeClaimantType = caseData.getRepresentativeClaimantType();

        String initialTitle;

        if (representativeClaimantType == null || NO.equals(caseData.getClaimantRepresentedQuestion())) {
            ClaimantType claimantType = caseData.getClaimantType();

            if (claimantType == null) {
                throw new NotFoundException("Could not find claimant");
            }

            ClaimantIndType claimantIndType = caseData.getClaimantIndType();

            if (StringUtils.isNotEmpty(claimantIndType.getClaimantTitle())) {
                initialTitle = claimantIndType.getClaimantTitle();
            } else if (StringUtils.isNotEmpty(claimantIndType.getClaimantPreferredTitle())) {
                initialTitle = claimantIndType.getClaimantPreferredTitle();
            } else {
                initialTitle = caseData.getClaimant().substring(0, 1).toUpperCase(Locale.ROOT);
            }

            personalisation.put("name", buildName(initialTitle, claimantIndType.getClaimantLastName()));
            return personalisation;
        }

        String repName = representativeClaimantType.getNameOfRepresentative();
        personalisation.put("name", buildName(repName.substring(0, 1), getLastName(repName)));

        return personalisation;

    }

    /**
     * Builds personalisation object for sending an email to the claimant or claimant rep.
     */
    public static Map<String, String> buildMapForClaimant(CaseDetails caseDetails) {
        CaseData caseData = caseDetails.getCaseData();
        return buildMapForClaimant(caseData, caseDetails.getCaseId());

    }

    /**
     * Builds personalisation object for sending an email to the respondent or their rep.
     */
    public static Map<String, String> buildMapForRespondent(CaseDetails caseDetails, RespondentSumType respondent) {
        CaseData caseData = caseDetails.getCaseData();
        Map<String, String> personalisation = new ConcurrentHashMap<>();
        personalisation.put(CASE_NUMBER, caseData.getEthosCaseReference());
        personalisation.put("emailAddress", getEmailAddressForRespondent(caseData, respondent));
        personalisation.put(CCD_ID, caseDetails.getCaseId());
        RepresentedTypeR respondentRepresentative = getRespondentRepresentative(caseData, respondent);

        if (respondentRepresentative == null) {
            personalisation.put("name", respondent.getRespondentName());
            return personalisation;
        }

        String repName = respondentRepresentative.getNameOfRepresentative();
        personalisation.put("name", buildName(repName.substring(0, 1), getLastName(repName)));
        return personalisation;
    }

    private static String buildName(String initialTitle, String lastName) {
        if (isNullOrEmpty(lastName)) {
            return initialTitle;
        } else {
            return String.format("%s %s", initialTitle, lastName);
        }
    }

    private static String getLastName(String name) {
        return name.substring(name.lastIndexOf(' ') + 1);
    }

    private static String getNameForClaimant(CaseData caseData) {
        RepresentedTypeC representativeClaimantType = caseData.getRepresentativeClaimantType();

        if (representativeClaimantType == null || representativeClaimantType.getNameOfRepresentative() == null) {
            return caseData.getClaimant();
        }

        return representativeClaimantType.getNameOfRepresentative();
    }

    public static String getEmailAddressForClaimant(CaseData caseData) {
        RepresentedTypeC representativeClaimantType = caseData.getRepresentativeClaimantType();

        if (representativeClaimantType == null || NO.equals(caseData.getClaimantRepresentedQuestion())) {
            ClaimantType claimantType = caseData.getClaimantType();
            if (claimantType == null) {
                throw new NotFoundException("Could not find claimant");
            }
            String claimantEmailAddress = claimantType.getClaimantEmailAddress();
            return isNullOrEmpty(claimantEmailAddress) ? "" : claimantEmailAddress;
        }

        String representativeEmailAddress = representativeClaimantType.getRepresentativeEmailAddress();
        return isNullOrEmpty(representativeEmailAddress) ? "" : representativeEmailAddress;
    }

    private static String getNameOfRespondents(CaseData caseData) {
        return caseData.getRespondentCollection().stream()
                .map(o -> getNameForRespondent(caseData, o.getValue()))
                .collect(Collectors.joining(", "));
    }

    /**
     * Gets the email address for the respondent's legal rep (if available) or their own email address.
     */
    public static String getEmailAddressForRespondent(CaseData caseData, RespondentSumType respondent) {
        RepresentedTypeR representative = getRespondentRepresentative(caseData, respondent);
        if (representative != null) {
            String email = representative.getRepresentativeEmailAddress();
            if (!isNullOrEmpty(email)) {
                return email;
            }
        }
        return isNullOrEmpty(respondent.getRespondentEmail()) ? "" : respondent.getRespondentEmail();
    }

    private static String getNameForRespondent(CaseData caseData, RespondentSumType respondent) {
        RepresentedTypeR respondentRepresentative = getRespondentRepresentative(caseData, respondent);
        if (respondentRepresentative != null) {
            return respondentRepresentative.getNameOfRepresentative();
        }
        return respondent.getRespondentName();
    }

    /**
     * Gets the representative for the respondent if present.
     */
    public static RepresentedTypeR getRespondentRepresentative(CaseData caseData, RespondentSumType respondent) {
        List<RepresentedTypeRItem> repCollection = caseData.getRepCollection();

        if (CollectionUtils.isEmpty(repCollection)) {
            return null;
        }

        Optional<RepresentedTypeRItem> respondentRep = repCollection.stream()
                .filter(o -> respondent.getRespondentName().equals(o.getValue().getRespRepName()))
                .findFirst();

        return respondentRep.map(RepresentedTypeRItem::getValue).orElse(null);
    }

    /**
     * Gets the email address for the respondent if unrepresented.
     */
    public static String getEmailAddressForUnrepresentedRespondent(CaseData caseData, RespondentSumType respondent) {
        RepresentedTypeR representative = getRespondentRepresentative(caseData, respondent);
        if (representative == null) {
            return respondent.getRespondentEmail();
        }
        return null;
    }

    public static void setSendNotificationValues(CaseData caseData, SendNotificationType sendNotificationType) {
        sendNotificationType.setDate(UtilHelper.formatCurrentDate(LocalDate.now()));
        sendNotificationType.setSendNotificationTitle(caseData.getSendNotificationTitle());
        sendNotificationType.setSendNotificationLetter(caseData.getSendNotificationLetter());
        sendNotificationType.setSendNotificationUploadDocument(caseData.getSendNotificationUploadDocument());
        sendNotificationType.setSendNotificationSubject(caseData.getSendNotificationSubject());
        sendNotificationType.setSendNotificationAdditionalInfo(caseData.getSendNotificationAdditionalInfo());
        sendNotificationType.setSendNotificationNotify(caseData.getSendNotificationNotify());
        sendNotificationType.setSendNotificationSelectHearing(caseData.getSendNotificationSelectHearing());
        sendNotificationType.setSendNotificationCaseManagement(caseData.getSendNotificationCaseManagement());
        sendNotificationType.setSendNotificationResponseTribunal(caseData.getSendNotificationResponseTribunal());
        sendNotificationType.setSendNotificationWhoCaseOrder(caseData.getSendNotificationWhoCaseOrder());
        sendNotificationType.setSendNotificationSelectParties(caseData.getSendNotificationSelectParties());
        sendNotificationType.setSendNotificationFullName(caseData.getSendNotificationFullName());
        sendNotificationType.setSendNotificationFullName2(caseData.getSendNotificationFullName2());
        sendNotificationType.setSendNotificationDecision(caseData.getSendNotificationDecision());
        sendNotificationType.setSendNotificationDetails(caseData.getSendNotificationDetails());
        sendNotificationType.setSendNotificationRequestMadeBy(caseData.getSendNotificationRequestMadeBy());
        sendNotificationType.setSendNotificationEccQuestion(caseData.getSendNotificationEccQuestion());
        sendNotificationType.setSendNotificationWhoMadeJudgement(caseData.getSendNotificationWhoMadeJudgement());

        if (sendNotificationType.getSendNotificationSubject().contains(CASE_MANAGEMENT_ORDERS_REQUESTS)
                && caseData.getSendNotificationResponseTribunal().equals(SEND_NOTIFICATION_RESPONSE_REQUIRED)
                && !caseData.getSendNotificationSelectParties().equals(RESPONDENT_ONLY)) {
            sendNotificationType.setNotificationState(NOT_STARTED_YET);
        } else {
            sendNotificationType.setNotificationState(NOT_VIEWED_YET);
        }

        sendNotificationType.setSendNotificationSentBy(TRIBUNAL);
        sendNotificationType.setSendNotificationSubjectString(
                String.join(", ", caseData.getSendNotificationSubject())
        );
        sendNotificationType.setSendNotificationResponsesCount("0");
        sendNotificationType.setSendNotificationResponseTribunalTable(
                NO.equals(caseData.getSendNotificationResponseTribunal()) ? NO : YES
        );
    }

    public static void setSendNotificationValues(MultipleData caseData, SendNotificationType sendNotificationType) {
        sendNotificationType.setDate(UtilHelper.formatCurrentDate(LocalDate.now()));
        sendNotificationType.setSendNotificationTitle(caseData.getSendNotificationTitle());
        sendNotificationType.setSendNotificationLetter(caseData.getSendNotificationLetter());
        sendNotificationType.setSendNotificationUploadDocument(caseData.getSendNotificationUploadDocument());
        sendNotificationType.setSendNotificationSubject(caseData.getSendNotificationSubject());
        sendNotificationType.setSendNotificationAdditionalInfo(caseData.getSendNotificationAdditionalInfo());
        sendNotificationType.setSendNotificationNotify(caseData.getSendNotificationNotify());
        sendNotificationType.setSendNotificationSelectHearing(caseData.getSendNotificationSelectHearing());
        sendNotificationType.setSendNotificationCaseManagement(caseData.getSendNotificationCaseManagement());
        sendNotificationType.setSendNotificationResponseTribunal(caseData.getSendNotificationResponseTribunal());
        sendNotificationType.setSendNotificationWhoCaseOrder(caseData.getSendNotificationWhoCaseOrder());
        sendNotificationType.setSendNotificationSelectParties(caseData.getSendNotificationSelectParties());
        sendNotificationType.setSendNotificationFullName(caseData.getSendNotificationFullName());
        sendNotificationType.setSendNotificationFullName2(caseData.getSendNotificationFullName2());
        sendNotificationType.setSendNotificationDecision(caseData.getSendNotificationDecision());
        sendNotificationType.setSendNotificationDetails(caseData.getSendNotificationDetails());
        sendNotificationType.setSendNotificationRequestMadeBy(caseData.getSendNotificationRequestMadeBy());
        sendNotificationType.setSendNotificationEccQuestion(caseData.getSendNotificationEccQuestion());
        sendNotificationType.setSendNotificationWhoMadeJudgement(caseData.getSendNotificationWhoMadeJudgement());

        if (sendNotificationType.getSendNotificationSubject().contains(CASE_MANAGEMENT_ORDERS_REQUESTS)
                && caseData.getSendNotificationResponseTribunal().equals(SEND_NOTIFICATION_RESPONSE_REQUIRED)
                && !caseData.getSendNotificationSelectParties().equals(RESPONDENT_ONLY)) {
            sendNotificationType.setNotificationState(NOT_STARTED_YET);
        } else {
            sendNotificationType.setNotificationState(NOT_VIEWED_YET);
        }

        sendNotificationType.setSendNotificationSentBy(TRIBUNAL);
        sendNotificationType.setSendNotificationSubjectString(
                String.join(", ", caseData.getSendNotificationSubject())
        );
        sendNotificationType.setSendNotificationResponsesCount("0");
        sendNotificationType.setSendNotificationResponseTribunalTable(
                NO.equals(caseData.getSendNotificationResponseTribunal()) ? NO : YES
        );
    }
}