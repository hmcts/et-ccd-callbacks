package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ChangeOrganisationRequest;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.SolicitorRole;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
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
        try {
            String selectedRole = changeRequest.getCaseRoleId().getSelectedCode();
            SolicitorRole solicitorRole = SolicitorRole.from(selectedRole).orElse(null);

            if (solicitorRole != null) {
                Optional<RespondentSumTypeItem> representationItem = solicitorRole.getRepresentationItem(caseDataNew);
                return representationItem.map(respondentSumTypeItem ->
                                respondentSumTypeItem.getValue().getRespondentName())
                        .orElse(UNKNOWN);
            } else {
                log.warn("Invalid solicitor role: {}", selectedRole);
            }
        } catch (Exception e) {
            log.warn("Failed to get RespondentNameForNewSolicitor", e);
        }
        return UNKNOWN;
    }

    public static RespondentSumType getRespondent(ChangeOrganisationRequest changeRequest,
                                                  CaseData caseData,
                                                  NocRespondentHelper nocRespondentHelper) {
        try {
            String selectedRoleCode = changeRequest.getCaseRoleId().getSelectedCode();
            SolicitorRole solicitorRole = SolicitorRole.from(selectedRoleCode).orElse(null);

            if (solicitorRole != null) {
                Optional<RespondentSumTypeItem> respondentSumTypeItem = solicitorRole.getRepresentationItem(caseData);
                if (respondentSumTypeItem.isPresent()) {
                    String respondentName = respondentSumTypeItem.get().getValue().getRespondentName();
                    return nocRespondentHelper.getRespondent(respondentName, caseData);
                } else {
                    log.warn("No representation item found for solicitor role: {}", selectedRoleCode);
                }
            } else {
                log.warn("Invalid solicitor role: {}", selectedRoleCode);
            }
        } catch (Exception e) {
            log.warn("Failed to get Respondent for New Solicitor", e);
        }
        return null;
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

    public static Map<String, String> buildRespondentPersonalisation(CaseDetails detail, RespondentSumType respondent) {
        Map<String, String> personalisation = new ConcurrentHashMap<>();

        CaseData caseData = detail.getCaseData();
        addCommonValues(caseData, personalisation);
        personalisation.put("respondent_name", respondent.getRespondentName());
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