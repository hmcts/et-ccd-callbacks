package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import lombok.extern.slf4j.Slf4j;
import org.webjars.NotFoundException;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantType;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * ET3 Notification Helper provides methods to assist with the ET3 Notification event.
 */
@Slf4j
@SuppressWarnings({"PMD.ClassWithOnlyPrivateConstructorsShouldBeFinal", "PMD.LinguisticNaming",
    "PMD.ExcessiveMethodLength", "PMD.ClassNamingConventions", "PMD.PrematureDeclaration"})
public class Et3NotificationHelper {

    private Et3NotificationHelper() {
        // Access through static methods
    }

    /**
     * Formats message for names of parties that notifications will be sent to.
     */
    public static String getParties(CaseData caseData) {
        return String.format("%s, %s", getNameForClaimant(caseData), getNameOfRespondents(caseData));
    }

    /**
     * Builds personalisation object for sending an email to the claimant or claimant rep.
     */
    public static Map<String, String> buildMapForClaimant(CaseData caseData) {
        Map<String, String> personalisation = new ConcurrentHashMap<>();
        personalisation.put("caseNumber", caseData.getEthosCaseReference());
        personalisation.put("emailAddress", getEmailAddressForClaimant(caseData));
        RepresentedTypeC representativeClaimantType = caseData.getRepresentativeClaimantType();

        String initialTitle;

        if (representativeClaimantType == null) {
            ClaimantType claimantType = caseData.getClaimantType();

            if (claimantType == null) {
                throw new NotFoundException("Could not find claimant");
            }

            if (!isNullOrEmpty(caseData.getClaimantIndType().getClaimantTitle())) {
                initialTitle = caseData.getClaimantIndType().getClaimantTitle();
            } else if (!isNullOrEmpty(caseData.getClaimantIndType().getClaimantPreferredTitle())) {
                initialTitle = caseData.getClaimantIndType().getClaimantPreferredTitle();
            } else {
                initialTitle = caseData.getClaimant().substring(0, 1).toUpperCase(Locale.ROOT);
            }

            personalisation.put("name", buildName(initialTitle, caseData.getClaimantIndType().getClaimantLastName()));
            return personalisation;
        }

        String repName = representativeClaimantType.getNameOfRepresentative();
        personalisation.put("name", buildName(repName.substring(0, 1), getLastName(repName)));

        return personalisation;
    }

    /**
     * Builds personalisation object for sending an email to the respondent or their rep.
     */
    public static Map<String, String> buildMapForRespondent(CaseData caseData, RespondentSumType respondent) {
        Map<String, String> personalisation = new ConcurrentHashMap<>();
        personalisation.put("caseNumber", caseData.getEthosCaseReference());
        personalisation.put("emailAddress", getEmailAddressForRespondent(caseData, respondent));
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

        if (representativeClaimantType == null) {
            return caseData.getClaimant();
        }

        return representativeClaimantType.getNameOfRepresentative();
    }

    private static String getEmailAddressForClaimant(CaseData caseData) {
        RepresentedTypeC representativeClaimantType = caseData.getRepresentativeClaimantType();

        if (representativeClaimantType == null) {
            ClaimantType claimantType = caseData.getClaimantType();
            if (claimantType == null) {
                throw new NotFoundException("Could not find claimant");
            }
            return claimantType.getClaimantEmailAddress();
        }

        return representativeClaimantType.getRepresentativeEmailAddress();
    }

    private static String getNameOfRespondents(CaseData caseData) {
        return caseData.getRespondentCollection().stream()
            .map(o -> getNameForRespondent(caseData, o.getValue()))
            .collect(Collectors.joining(", "));
    }

    private static String getEmailAddressForRespondent(CaseData caseData, RespondentSumType respondent) {
        RepresentedTypeR respondentRepresentative = getRespondentRepresentative(caseData, respondent);
        if (respondentRepresentative != null) {
            return respondentRepresentative.getRepresentativeEmailAddress();
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

    private static RepresentedTypeR getRespondentRepresentative(CaseData caseData, RespondentSumType respondent) {
        List<RepresentedTypeRItem> repCollection = caseData.getRepCollection();
        Optional<RepresentedTypeRItem> respondentRep = repCollection.stream()
            .filter(o -> respondent.getRespondentName().equals(o.getValue().getRespRepName()))
            .findFirst();

        return respondentRep.map(RepresentedTypeRItem::getValue).orElse(null);
    }
}

