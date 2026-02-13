package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.types.ChangeOrganisationRequest;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.SolicitorRole;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Locale.UK;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.CLAIMANT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.DATE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.MONTH_STRING_DATE_FORMAT;

@Slf4j
public final class NocNotificationHelper {

    public static final String UNKNOWN = "Unknown";

    private NocNotificationHelper() {
        // Access through static methods
    }

    public static String getRespondentNameForNewSolicitor(ChangeOrganisationRequest changeRequest,
                                                          CaseData caseDataNew) {
        if (changeRequest == null || changeRequest.getCaseRoleId() == null || caseDataNew == null) {
            log.warn("Failed to get RespondentNameForNewSolicitor - changeRequest, caseRoleId, or caseDataNew is null");
            return UNKNOWN;
        }

        String selectedRole = changeRequest.getCaseRoleId().getSelectedCode();
        if (isNullOrEmpty(selectedRole)) {
            log.warn("Failed to get RespondentNameForNewSolicitor - selectedRole is null or empty");
            return UNKNOWN;
        }

        return SolicitorRole.from(selectedRole)
                .flatMap(role -> role.getRepresentationItem(caseDataNew))
                .map(item -> item.getValue().getRespondentName())
                .filter(name -> !isNullOrEmpty(name))
                .orElse(UNKNOWN);
    }

    public static RespondentSumType getRespondent(ChangeOrganisationRequest changeRequest, CaseData caseData,
                                                  NocRespondentHelper nocRespondentHelper) {
        if (changeRequest == null || changeRequest.getCaseRoleId() == null 
                || caseData == null || nocRespondentHelper == null) {
            return null;
        }

        String selectedRole = changeRequest.getCaseRoleId().getSelectedCode();
        if (isNullOrEmpty(selectedRole)) {
            return null;
        }

        return SolicitorRole.from(selectedRole)
                .flatMap(role -> role.getRepresentationItem(caseData))
                .map(item -> nocRespondentHelper.getRespondent(
                        item.getValue().getRespondentName(), caseData))
                .orElse(null);
    }

    public static Map<String, String> buildPersonalisationWithPartyName(CaseDetails caseDetails, String partyName,
                                                                        String linkToCitUI) {
        Map<String, String> personalisation = new ConcurrentHashMap<>();

        addCommonValues(caseDetails.getCaseData(), personalisation);
        personalisation.put("party_name", partyName);
        personalisation.put("ccdId", caseDetails.getCaseId());
        personalisation.put("linkToCitUI", linkToCitUI);

        return personalisation;
    }

    public static Map<String, String> buildPreviousRespondentSolicitorPersonalisation(CaseData caseData) {
        Map<String, String> personalisation = new ConcurrentHashMap<>();

        addCommonValues(caseData, personalisation);
        return personalisation;
    }

    public static Map<String, String> buildNoCPersonalisation(CaseDetails detail, String partyName) {
        Map<String, String> personalisation = new ConcurrentHashMap<>();

        CaseData caseData = detail.getCaseData();
        addCommonValues(caseData, personalisation);
        personalisation.put("respondent_name", partyName);
        personalisation.put("ccdId", detail.getCaseId());
        personalisation.put("date", "Not set");

        String nextHearingDate = HearingsHelper.getEarliestFutureHearingDate(caseData.getHearingCollection());

        if (nextHearingDate != null) {
            try {
                Date hearingStartDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", UK).parse(nextHearingDate);
                personalisation.put("date",
                        new SimpleDateFormat(MONTH_STRING_DATE_FORMAT, UK).format(hearingStartDate));
            } catch (ParseException ignored) {
                log.warn("Failed to parse nextHearingDate");
            }
        }

        return personalisation;
    }

    public static Map<String, String> buildTribunalPersonalisation(CaseData caseData) {
        Map<String, String> personalisation = new ConcurrentHashMap<>();

        addCommonValues(caseData, personalisation);
        personalisation.put(DATE, ReferralHelper.getNearestHearingToReferral(caseData, "Not set"));
        personalisation.put("tribunal", isNullOrEmpty(caseData.getTribunalAndOfficeLocation()) ? UNKNOWN :
                caseData.getTribunalAndOfficeLocation());

        return personalisation;
    }

    private static void addCommonValues(CaseData caseData, Map<String, String> personalisation) {
        personalisation.put(CLAIMANT, caseData.getClaimant());
        personalisation.put("list_of_respondents", getListOfRespondents(caseData));
        personalisation.put("case_number", caseData.getEthosCaseReference());
    }

    @NotNull
    private static String getListOfRespondents(CaseData caseData) {
        return caseData.getRespondentCollection().stream()
                .map(respondent -> respondent.getValue().getRespondentName())
                .collect(Collectors.joining(" "));
    }
}
