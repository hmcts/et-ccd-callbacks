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

import java.util.ArrayList;
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

        referralType.setReferralDate(Helper.getCurrentDate());

        UserDetails userDetails = userService.getUserDetails(userToken);
        referralType.setReferredBy(userDetails.getFirstName() + " " + userDetails.getLastName());
        referralType.setReferrerEmail(userDetails.getEmail());

        referralType.setReferralStatus("Open");

        ReferralTypeItem referralTypeItem = new ReferralTypeItem();
        referralTypeItem.setId(UUID.randomUUID().toString());
        referralTypeItem.setValue(referralType);

        List<ReferralTypeItem> referralCollection = caseData.getReferralCollection();
        referralCollection.add(referralTypeItem);
        caseData.setReferralCollection(referralCollection);
        clearReferralDataFromCaseData(caseData);
    }

    public void clearReferralDataFromCaseData(CaseData caseData) {
        caseData.setReferralHearingDetails(null);
        caseData.setReferCaseTo(null);
        caseData.setReferrerEmail(null);
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
