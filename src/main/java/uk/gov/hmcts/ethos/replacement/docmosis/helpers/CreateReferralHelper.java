package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.ReferralTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ReferralType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.IntWrapper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CreateReferralHelper {
    private final UserService userService;
    private static final String GUIDANCE_DOC_LINK = "<hr>To help you complete this form, open the "
        + "<a href=\"url\">referral guidance documents</a>";
    private static final String HEARING_DETAILS = "<hr><h3>Hearing details %s</h3>"
        + "<pre>Date &nbsp;&#09&#09&#09&#09&#09&nbsp; %s"
        + "<br><br>Hearing &#09&#09&#09&#09&nbsp; %s"
        + "<br><br>Type &nbsp;&nbsp;&#09&#09&#09&#09&#09 %s</pre>";

    @Autowired
    public CreateReferralHelper(UserService userService) {
        this.userService = userService;
    }

    /**
     * Formats the hearing details into HTML for ExUI to display. It's expected that there are at least one hearing
     * already created before this event is started. Hearing details should contain the hearing date, hearing
     * type and the track type for each hearing.
     */
    public void populateHearingDetails(CaseData caseData) {
        if (CollectionUtils.isNotEmpty(caseData.getHearingCollection())) {
            String trackType = caseData.getTrackType();
            StringBuilder hearingDetails = new StringBuilder();
            hearingDetails.append(GUIDANCE_DOC_LINK);
            IntWrapper count = new IntWrapper(0);
            boolean singleHearing = caseData.getHearingCollection().size() == 1;
            for (HearingTypeItem hearing : caseData.getHearingCollection()) {
                hearingDetails.append(hearing.getValue().getHearingDateCollection().stream()
                    .map(h -> String.format(
                        HEARING_DETAILS, singleHearing ? "" : count.incrementAndReturnValue(),
                        UtilHelper.formatLocalDateTime(h.getValue().getListedDate()),
                        hearing.getValue().getHearingType(), trackType))
                    .collect(Collectors.joining()));
            }
            hearingDetails.append("<hr>");
            caseData.setReferralHearingDetails(hearingDetails.toString());
        }
    }

    /**
     * Creates a referral and adds it to the referral collection.
     * @param caseData contains all the case data
     * @param userToken The user token of the logged-in user
     */
    public void createReferral(CaseData caseData, String userToken) {
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

        UserDetails userDetails = userService.getUserDetails(userToken);
        referralType.setReferredBy(userDetails.getFirstName() + " " + userDetails.getLastName());

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
    private String getNearestHearingToReferral(CaseData caseData) {
        List<HearingTypeItem> hearingCollection = caseData.getHearingCollection();

        if (CollectionUtils.isEmpty(hearingCollection)) {
            return "None";
        }

        Date nextHearingAfterReferral = null;
        for (HearingTypeItem hearing : hearingCollection) {
            String hearingDateString =
                hearing.getValue().getHearingDateCollection().get(0).getValue().getHearingTimingStart();

            if (hearingDateString != null && !hearingDateString.isEmpty()) {
                Date hearingStartDate;
                try {
                    hearingStartDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").parse(hearingDateString);
                } catch (ParseException e) {
                    log.info("Failed to parse hearing date when creating new referral");
                    continue;
                }

                if (hearingStartDate.after(new Date())
                    && (nextHearingAfterReferral == null || hearingStartDate.before(nextHearingAfterReferral))
                ) {
                    nextHearingAfterReferral = hearingStartDate;
                }
            }
        }
        return nextHearingAfterReferral == null ? "None" :
            new SimpleDateFormat("dd MMM yyyy").format(nextHearingAfterReferral);
    }

    /**
     * Resets the case data fields relating to creating a referral so that they won't be auto populated when
     * creating
     * a new referral.
     * @param caseData contains all the case data
     */
    public void clearReferralDataFromCaseData(CaseData caseData) {
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
