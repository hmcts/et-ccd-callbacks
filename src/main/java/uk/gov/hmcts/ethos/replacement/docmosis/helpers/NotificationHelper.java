package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.webjars.NotFoundException;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantType;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ecm.common.constants.PdfMapperConstants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.CASE_NUMBER;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.CCD_ID;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.EMAIL_ADDRESS;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.NAME;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.EMPTY_STRING;

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
        personalisation.put(EMAIL_ADDRESS, getEmailAddressForClaimant(caseData));
        personalisation.put(CCD_ID, caseId);
        personalisation.put(NAME, caseData.getClaimant());

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
        personalisation.put(EMAIL_ADDRESS, getEmailAddressForRespondent(caseData, respondent));
        personalisation.put(CCD_ID, caseDetails.getCaseId());
        RepresentedTypeR respondentRepresentative = getRespondentRepresentative(caseData, respondent);

        if (respondentRepresentative == null) {
            personalisation.put(NAME, respondent.getRespondentName());
            return personalisation;
        }

        String repName = respondentRepresentative.getNameOfRepresentative();
        personalisation.put(NAME, buildName(repName.substring(0, 1), getLastName(repName)));
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

    public static String getNameForClaimant(CaseData caseData) {
        RepresentedTypeC representativeClaimantType = caseData.getRepresentativeClaimantType();

        if (representativeClaimantType == null || representativeClaimantType.getNameOfRepresentative() == null) {
            return caseData.getClaimant();
        }

        return representativeClaimantType.getNameOfRepresentative();
    }

    public static String getEmailAddressForClaimant(CaseData caseData) {
        ClaimantType claimantType = caseData.getClaimantType();
        if (claimantType == null) {
            throw new NotFoundException("Could not find claimant");
        }
        String claimantEmailAddress = claimantType.getClaimantEmailAddress();
        return isNullOrEmpty(claimantEmailAddress) ? "" : claimantEmailAddress;
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
        // return empty string because if Respondents do not have portal access and should not be sent an email
        return EMPTY_STRING;
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
                .filter(r -> YES.equals(r.getValue().getMyHmctsYesNo())
                             && !ObjectUtils.isEmpty(r.getValue().getRespondentOrganisation())
                             && respondent.getRespondentName().equals(r.getValue().getRespRepName()))
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

    public static Map<String, String> buildMapForClaimantRepresentative(CaseData caseData) {
        Map<String, String> personalisation = new ConcurrentHashMap<>();
        personalisation.put(CASE_NUMBER, caseData.getEthosCaseReference());
        personalisation.put(NAME, caseData.getRepresentativeClaimantType().getNameOfRepresentative());
        personalisation.put(EMAIL_ADDRESS,
                isNullOrEmpty(caseData.getRepresentativeClaimantType().getRepresentativeEmailAddress()) ? EMPTY_STRING :
                caseData.getRepresentativeClaimantType().getRepresentativeEmailAddress());
        return personalisation;
    }

    /**
     * Retrieves a list of email addresses for respondents and their representatives from the given case data.
     *
     * @param caseData the case data containing respondent and representative information
     */
    public static void getRespondentAndRepEmailAddresses(CaseData caseData, RespondentSumTypeItem respondentSumTypeItem,
                                                         Map<String, String> emailAddressesMap) {
        RespondentSumType respondent = respondentSumTypeItem.getValue();
        String responseEmail = respondent.getResponseRespondentEmail();
        String respondentEmail = respondent.getRespondentEmail();

        if (StringUtils.isNotBlank(responseEmail)) {
            emailAddressesMap.put(responseEmail, respondentSumTypeItem.getId());
        } else if (StringUtils.isNotBlank(respondentEmail)) {
            emailAddressesMap.put(respondentEmail, respondentSumTypeItem.getId());
        }

        RepresentedTypeR representative = getRespondentRepresentative(caseData, respondent);
        if (representative != null && StringUtils.isNotBlank(representative.getRepresentativeEmailAddress())) {
            emailAddressesMap.put(representative.getRepresentativeEmailAddress(), "");
        }
    }
}