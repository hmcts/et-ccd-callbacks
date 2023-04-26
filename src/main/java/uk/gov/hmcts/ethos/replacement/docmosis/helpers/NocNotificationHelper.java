package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ChangeOrganisationRequest;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.SolicitorRole;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;

@Slf4j
@SuppressWarnings({"PMD.AvoidCatchingNPE"})
public final class NocNotificationHelper {

    public static final String UNKNOWN = "Unknown";

    private NocNotificationHelper() {
        // Access through static methods
    }

    public static String getOldSolicitorEmailForRepUpdate(CaseData previousCaseData,
                                                          ChangeOrganisationRequest changeRequest) {
        try {
            String previousOrgId = changeRequest.getOrganisationToRemove().getOrganisationID();
            Optional<RepresentedTypeRItem> representedTypeRItem = previousCaseData.getRepCollection()
                    .stream().filter(r -> r.getValue().getRespondentOrganisation().getOrganisationID()
                            .equals(previousOrgId))
                    .findAny();

            if (representedTypeRItem.isPresent()) {
                return representedTypeRItem.get().getValue().getRepresentativeEmailAddress();
            }
            return null;
        } catch (NullPointerException ex) {
            return null;
        }

    }

    public static String getNewSolicitorEmailForRepUpdate(CaseData caseData, ChangeOrganisationRequest changeRequest) {
        try {
            String newOrgId = changeRequest.getOrganisationToAdd().getOrganisationID();
            Optional<RepresentedTypeRItem> representedTypeRItem = caseData.getRepCollection()
                    .stream().filter(r -> r.getValue().getRespondentOrganisation().getOrganisationID()
                            .equals(newOrgId))
                    .findAny();
            if (representedTypeRItem.isPresent()) {
                return representedTypeRItem.get().getValue().getRepresentativeEmailAddress();
            }
            return null;

        } catch (NullPointerException e) {
            return null;
        }

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
            personalisation.put("date", "Not set");
        } else {
            try {
                Date hearingStartDate =
                        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.UK).parse(nextHearingDate);
                personalisation.put("date", new SimpleDateFormat("dd MMM yyyy", Locale.UK).format(hearingStartDate));
            } catch (ParseException e) {
                personalisation.put("date", "Not set");
            }
        }

        return personalisation;
    }

    public static Map<String, String> buildTribunalPersonalisation(CaseData caseData) {
        Map<String, String> personalisation = new ConcurrentHashMap<>();

        addCommonValues(caseData, personalisation);
        personalisation.put("date",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")));
        personalisation.put("tribunal", isNullOrEmpty(caseData.getTribunalAndOfficeLocation()) ? UNKNOWN :
                caseData.getTribunalAndOfficeLocation());

        return personalisation;
    }

    private static void addCommonValues(CaseData caseData, Map<String, String> personalisation) {
        personalisation.put("claimant", caseData.getClaimant());
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