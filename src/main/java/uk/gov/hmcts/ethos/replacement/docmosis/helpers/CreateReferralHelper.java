package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.ReferralTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ReferralType;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
public class CreateReferralHelper {
    private static final String GUIDANCE_DOC_LINK = "<hr>To help you complete this form, open the "
        + "<a href=\"url\">referral guidance documents</a>";
    private static final String HEARING_DETAILS = "<hr><h3>Hearing details %s</h3>"
        + "<pre>Date &nbsp;&#09&#09&#09&#09&#09&nbsp; %s"
        + "<br><br>Hearing &#09&#09&#09&#09&nbsp; %s"
        + "<br><br>Type &nbsp;&nbsp;&#09&#09&#09&#09&#09 %s</pre>";

    private CreateReferralHelper() {
    }

    /**
     * Formats the hearing details into HTML for ExUI to display. It's expected that there are at least one hearing
     * already created before this event is started. Hearing details should contain the hearing date, hearing
     * type and the track type for each hearing.
     */
    public static void populateHearingDetails(CaseData caseData) {
        if (CollectionUtils.isNotEmpty(caseData.getHearingCollection())) {
            String trackType = caseData.getTrackType();
            StringBuilder hearingDetails = new StringBuilder();
            hearingDetails.append(GUIDANCE_DOC_LINK);
            int count = 0;
            boolean singleHearing = caseData.getHearingCollection().size() == 1;

            for (HearingTypeItem hearing : caseData.getHearingCollection()) {
                for (var hearingDates : hearing.getValue().getHearingDateCollection()) {
                    hearingDetails.append(
                        String.format(
                            HEARING_DETAILS,
                            singleHearing ? "" : ++count,
                            UtilHelper.formatLocalDateTime(hearingDates.getValue().getListedDate()),
                            hearing.getValue().getHearingType(),
                            trackType != null ? trackType : "N/A")
                    );
                }
            }

            hearingDetails.append("<hr>");
            caseData.setReferralHearingDetails(hearingDetails.toString());
        }
    }

    /**
     * Creates a referral and adds it to the referral collection.
     * @param caseData contains all the case data
     * @param userFullName The user token of the logged-in user
     */
    public static void createReferral(CaseData caseData, String userFullName) {
        if (CollectionUtils.isEmpty(caseData.getReferralCollection())) {
            caseData.setReferralCollection(new ArrayList<>());
        }

        ReferralType referralType = new ReferralType();

        referralType.setReferralNumber(String.valueOf((caseData.getReferralCollection().size() + 1)));
        referralType.setReferCaseTo(caseData.getReferCaseTo());
        referralType.setIsUrgent(caseData.getIsUrgent());
        referralType.setReferralSubject(caseData.getReferralSubject());
        referralType.setReferralSubjectSpecify(caseData.getReferralSubjectSpecify());
        referralType.setReferralDetails(caseData.getReferralDetails());
        referralType.setReferralDocument(caseData.getReferralDocument());
        referralType.setReferralInstruction(caseData.getReferralInstruction());
        referralType.setReferentEmail(caseData.getReferentEmail());

        referralType.setReferralDate(Helper.getCurrentDate());

        referralType.setReferredBy(userFullName);

        referralType.setReferralStatus(ReferralStatus.AWAITING_INSTRUCTIONS);

        referralType.setReferralHearingDate(getNearestHearingToReferral(caseData));

        ReferralTypeItem referralTypeItem = new ReferralTypeItem();
        referralTypeItem.setId(UUID.randomUUID().toString());
        referralTypeItem.setValue(referralType);

        List<ReferralTypeItem> referralCollection = caseData.getReferralCollection();
        referralCollection.add(referralTypeItem);
        caseData.setReferralCollection(referralCollection);
        clearReferralDataFromCaseData(caseData);
    }

    /**
     * Gets the next hearing date from the referral, returns "None" if no suitable hearing date exists.
     * @param caseData contains all the case data
     * @return Returns next hearing date in "dd MMM yyyy" format or "None"
     */
    private static String getNearestHearingToReferral(CaseData caseData) {
        String earliestFutureHearingDate = HearingsHelper.getEarliestFutureHearingDate(caseData.getHearingCollection());

        if (earliestFutureHearingDate == null) {
            return "None";
        }

        Date hearingStartDate;
        try {
            hearingStartDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").parse(earliestFutureHearingDate);
        } catch (ParseException e) {
            log.info("Failed to parse hearing date when creating new referral");
            return "None";
        }

        return new SimpleDateFormat("dd MMM yyyy").format(hearingStartDate);
    }

    /**
     * Resets the case data fields relating to creating a referral so that they won't be auto-populated when
     * creating
     * a new referral.
     * @param caseData contains all the case data
     */
    public static void clearReferralDataFromCaseData(CaseData caseData) {
        caseData.setReferralHearingDetails(null);
        caseData.setReferCaseTo(null);
        caseData.setReferentEmail(null);
        caseData.setIsUrgent(null);
        caseData.setReferralSubject(null);
        caseData.setReferralSubjectSpecify(null);
        caseData.setReferralDetails(null);
        caseData.setReferralDocument(null);
        caseData.setReferralInstruction(null);
        caseData.setReferredBy(null);
        caseData.setReferralDate(null);
    }
}
