package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import uk.gov.hmcts.et.common.model.ccd.CallbackRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.SolicitorRole;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.EtCcdCallbacksConstants.CLAIMANT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.EtCcdCallbacksConstants.DATE;

@Slf4j
@SuppressWarnings({"PMD.AvoidCatchingNPE"})
public final class NocNotificationHelper {

    public static final String UNKNOWN = "Unknown";

    private NocNotificationHelper() {
        // Access through static methods
    }

    public static int getIndexOfSolicitor(CaseData caseData) {
        String caseRoleId = caseData.getChangeOrganisationRequestField().getCaseRoleId().getSelectedCode();
        SolicitorRole solicitorRole = SolicitorRole.from(caseRoleId).orElseThrow();
        return solicitorRole.getIndex();
    }

    public static String getOldSolicitorEmail(CallbackRequest callbackRequest) {
        try {
            CaseData prevCaseData = callbackRequest.getCaseDetailsBefore().getCaseData();
            return prevCaseData.getRepCollection()
                    .get(getIndexOfSolicitor(prevCaseData))
                    .getValue().getRepresentativeEmailAddress();
        } catch (NullPointerException ex) {
            return null;
        }

    }

    public static String getNewSolicitorEmail(CallbackRequest callbackRequest) {
        try {
            CaseData oldCaseData = callbackRequest.getCaseDetailsBefore().getCaseData();
            CaseData newDetails = callbackRequest.getCaseDetails().getCaseData();
            return newDetails.getRepCollection()
                    .get(getIndexOfSolicitor(oldCaseData))
                    .getValue().getRepresentativeEmailAddress();
        } catch (NullPointerException e) {
            return null;
        }

    }

    public static String getRespondentNameForNewSolicitor(CallbackRequest callbackRequest) {
        String respondentName = null;
        try {
            CaseData caseData = callbackRequest.getCaseDetails().getCaseData();
            CaseData caseDetailsBefore = callbackRequest.getCaseDetailsBefore().getCaseData();
            String selectedRole =
                    caseDetailsBefore.getChangeOrganisationRequestField().getCaseRoleId()
                            .getSelectedCode();

            SolicitorRole solicitorRole = SolicitorRole.from(selectedRole).orElseThrow();

            respondentName =
                    solicitorRole.getRepresentationItem(caseData).map(respondentSumTypeItem ->
                            respondentSumTypeItem.getValue().getRespondentName()).orElseThrow();
        } catch (NullPointerException e) {
            log.warn("Failed to get RespondentNameForNewSolicitor");
        }
        return isNullOrEmpty(respondentName) ? UNKNOWN : respondentName;
    }

    public static RespondentSumType getRespondent(CallbackRequest callbackRequest, CaseData caseData,
                                                  NocRespondentHelper nocRespondentHelper) {

        try {

            CaseData caseDataBefore = callbackRequest.getCaseDetailsBefore().getCaseData();
            String selectedRole =
                    caseDataBefore.getChangeOrganisationRequestField().getCaseRoleId().getSelectedCode();
            SolicitorRole solicitorRole = SolicitorRole.from(selectedRole).orElseThrow();
            RespondentSumTypeItem respondentSumTypeItem = solicitorRole.getRepresentationItem(caseData).orElseThrow();
            return nocRespondentHelper.getRespondent(respondentSumTypeItem.getValue().getRespondentName(), caseData);
        } catch (NullPointerException e) {
            return null;
        }

    }

    public static Map<String, String> buildPersonalisationWithPartyName(CaseData caseData, String partyName) {
        Map<String, String> personalisation = new ConcurrentHashMap<>();

        addCommonValues(caseData, personalisation);
        personalisation.put("party_name", partyName);

        return personalisation;
    }

    public static Map<String, String> buildPreviousRespondentSolicitorPersonalisation(CaseData caseData) {
        Map<String, String> personalisation = new ConcurrentHashMap<>();

        addCommonValues(caseData, personalisation);
        return personalisation;
    }

    public static Map<String, String> buildRespondentPersonalisation(CaseData caseData, RespondentSumType respondent) {
        Map<String, String> personalisation = new ConcurrentHashMap<>();

        addCommonValues(caseData, personalisation);
        personalisation.put("respondent_name", respondent.getRespondentName());

        String nextHearingDate = HearingsHelper.getEarliestFutureHearingDate(caseData.getHearingCollection());

        if (nextHearingDate == null) {
            personalisation.put(DATE, "Not set");
        } else {
            try {
                Date hearingStartDate =
                        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.UK).parse(nextHearingDate);
                personalisation.put(DATE, new SimpleDateFormat("dd MMM yyyy", Locale.UK).format(hearingStartDate));
            } catch (ParseException e) {
                personalisation.put(DATE, "Not set");
            }
        }

        return personalisation;
    }

    public static Map<String, String> buildTribunalPersonalisation(CaseData caseData) {
        Map<String, String> personalisation = new ConcurrentHashMap<>();

        addCommonValues(caseData, personalisation);
        personalisation.put(DATE,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")));
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