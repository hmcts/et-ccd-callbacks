package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ChangeOrganisationRequest;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.SolicitorRole;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.RespondentUtils;

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
        String respondentName = null;
        try {
            String selectedRole = changeRequest.getCaseRoleId()
                            .getSelectedCode();

            SolicitorRole solicitorRole = SolicitorRole.from(selectedRole).orElseThrow();

            respondentName =
                    solicitorRole.getRepresentationItem(caseDataNew).map(respondentSumTypeItem ->
                            respondentSumTypeItem.getValue().getRespondentName()).orElseThrow();
        } catch (NullPointerException e) {
            log.warn("Failed to get RespondentNameForNewSolicitor");
        }
        return isNullOrEmpty(respondentName) ? UNKNOWN : respondentName;
    }

    public static RespondentSumType getRespondent(ChangeOrganisationRequest changeRequest, CaseData caseData,
                                                  NocRespondentHelper nocRespondentHelper) {

        try {

            String selectedRole = changeRequest.getCaseRoleId().getSelectedCode();
            SolicitorRole solicitorRole = SolicitorRole.from(selectedRole).orElseThrow();
            RespondentSumTypeItem respondentSumTypeItem = solicitorRole.getRepresentationItem(caseData).orElseThrow();
            return nocRespondentHelper.getRespondent(respondentSumTypeItem.getValue().getRespondentName(), caseData);
        } catch (NullPointerException e) {
            return null;
        }

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

    /**
     * Resolves the respondent name associated with the given representative.
     * <p>
     * If the representative already contains a respondent name, that value is returned.
     * Otherwise, the method attempts to derive the respondent name using the respondent
     * ID from the provided {@link CaseData}. If the respondent cannot be resolved, or if
     * the required data is missing or invalid, {@code UNKNOWN} is returned.
     *
     * @param caseData the case data containing the respondent collection
     * @param representative the representative whose associated respondent name is required
     * @return the resolved respondent name, or {@code UNKNOWN} if it cannot be determined
     */
    public static RespondentSumTypeItem findRespondentByRepresentative(CaseData caseData,
                                                                       RepresentedTypeRItem representative) {
        if (ObjectUtils.isEmpty(caseData)
                || CollectionUtils.isEmpty(caseData.getRespondentCollection())
                || ObjectUtils.isEmpty(representative)
                || ObjectUtils.isEmpty(representative.getValue())
                || StringUtils.isBlank(representative.getValue().getRespondentId())
                && StringUtils.isBlank(representative.getValue().getRespRepName())
                && StringUtils.isBlank(representative.getId())) {
            return null;
        }
        RespondentSumTypeItem respondent = RespondentUtils.findRespondentById(caseData.getRespondentCollection(),
                representative.getValue().getRespondentId());
        if (ObjectUtils.isEmpty(respondent)) {
            respondent = RespondentUtils.findRespondentByName(caseData.getRespondentCollection(),
                    representative.getValue().getRespRepName());
        }
        if (ObjectUtils.isEmpty(respondent)) {
            respondent = RespondentUtils.findRespondentByRepresentativeId(caseData.getRespondentCollection(),
                    representative.getId());
        }
        return respondent;
    }
}