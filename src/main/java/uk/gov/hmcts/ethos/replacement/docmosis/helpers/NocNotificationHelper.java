package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import uk.gov.hmcts.et.common.model.ccd.CallbackRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.SolicitorRole;
import uk.gov.hmcts.ethos.replacement.docmosis.service.RespondentRepresentativeService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public final class NocNotificationHelper {

    private NocNotificationHelper() {
        // Access through static methods
    }

    public static int getIndexOfSolicitor(CaseData caseData) {
        String caseRoleId = caseData.getChangeOrganisationRequestField().getCaseRoleId().getSelectedCode();
        SolicitorRole solicitorRole = SolicitorRole.from(caseRoleId).orElseThrow();
        return solicitorRole.getIndex();
    }

    public static String getOldSolicitorEmail(CallbackRequest callbackRequest) {
        CaseData prevCaseData = callbackRequest.getCaseDetailsBefore().getCaseData();
        return prevCaseData.getRepCollection().get(getIndexOfSolicitor(prevCaseData)).getValue().getRepresentativeEmailAddress();
    }

    public static String getNewSolicitorEmail(CallbackRequest callbackRequest) {
        CaseData newDetails = callbackRequest.getCaseDetails().getCaseData();
        return newDetails.getRepCollection().get(getIndexOfSolicitor(newDetails)).getValue().getRepresentativeEmailAddress();
    }

    public static String getRespondentNameForNewSolicitor(CallbackRequest callbackRequest) {
        CaseData caseData = callbackRequest.getCaseDetails().getCaseData();
        return caseData.getChangeOrganisationRequestField().getOrganisationToAdd().getOrganisationName();
    }

    public static RespondentSumType getRespondent(CallbackRequest callbackRequest, CaseData caseData,
                                                  RespondentRepresentativeService respondentRepresentativeService) {
        String selectedRole =
            callbackRequest.getCaseDetailsBefore().getCaseData().getChangeOrganisationRequestField().getCaseRoleId()
                .getSelectedCode();

        SolicitorRole solicitorRole = SolicitorRole.from(selectedRole).orElseThrow();

        RepresentedTypeRItem representedPerson =
            solicitorRole.getRepresentedPerson(caseData).orElseThrow();

        return respondentRepresentativeService.getRespondent(representedPerson.getValue().getRespRepName(), caseData);
    }

    public static Map<String, String> buildClaimantPersonalisation(CaseData caseData, String party_name) {
        Map<String, String> personalisation = new HashMap<>();

        addCommonValues(caseData, personalisation);
        personalisation.put("party_name", party_name);
        personalisation.put("first_name", caseData.getClaimantIndType().getClaimantFirstNames());
        personalisation.put("last_name", caseData.getClaimantIndType().getClaimantLastName());

        return personalisation;
    }

    public static Map<String, String> buildPreviousRespondentSolicitorPersonalisation(CaseData caseData) {
        Map<String, String> personalisation = new HashMap<>();

        addCommonValues(caseData, personalisation);
        personalisation.put("email_flag", caseData.getEthosCaseReference());
        personalisation.put("first_name", caseData.getClaimantIndType().getClaimantFirstNames());
        personalisation.put("last_name", caseData.getClaimantIndType().getClaimantLastName());

        return personalisation;
    }

    public static Map<String, String> buildNewRespondentSolicitorPersonalisation(CaseData caseData, String partyName) {
        Map<String, String> personalisation = new HashMap<>();

        addCommonValues(caseData, personalisation);
        personalisation.put("email_flag", caseData.getEthosCaseReference());
        personalisation.put("first_name", caseData.getClaimantIndType().getClaimantFirstNames());
        personalisation.put("last_name", caseData.getClaimantIndType().getClaimantLastName());
        personalisation.put("party_name", partyName);

        return personalisation;
    }

    public static Map<String, String> buildRespondentPersonalisation(CaseData caseData, RespondentSumType respondent) {
        Map<String, String> personalisation = new HashMap<>();

        addCommonValues(caseData, personalisation);
        personalisation.put("email_flag", caseData.getEthosCaseReference());
        personalisation.put("respondent_name", respondent.getRespondentName());

        String nextHearingDate = HearingsHelper.getEarliestFutureHearingDate(caseData.getHearingCollection());

        if (nextHearingDate != null) {
            try {
                Date hearingStartDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").parse(nextHearingDate);
                personalisation.put("date", new SimpleDateFormat("dd MMM yyyy").format(hearingStartDate));
            } catch (ParseException e) {
                personalisation.put("date", "Not set");
            }
        } else {
            personalisation.put("date", "Not set");
        }

        return personalisation;
    }

    public static Map<String, String> buildTribunalPersonalisation(CaseData caseData) {
        Map<String, String> personalisation = new HashMap<>();

        addCommonValues(caseData, personalisation);
        personalisation.put("tribunal", caseData.getTribunalAndOfficeLocation());

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
