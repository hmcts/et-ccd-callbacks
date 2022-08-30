package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.validator.routines.EmailValidator;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.ReferralReplyTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.ReferralTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ReferralReplyType;
import uk.gov.hmcts.et.common.model.ccd.types.ReferralType;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@Slf4j
public final class ReferralHelper {

    private ReferralHelper() {
    }

    private static final String TRUE = "True";
    private static final String FALSE = "False";
    private static final String JUDGE_ROLE_ENG = "caseworker-employment-etjudge-englandwales";
    private static final String JUDGE_ROLE_SCOT = "caseworker-employment-etjudge-scotland";
    private static final String HEARING_DETAILS = "<hr><h3>Hearing details %s</h3>"
        + "<pre>Date &nbsp;&#09&#09&#09&#09&#09&nbsp; %s"
        + "<br><br>Hearing &#09&#09&#09&#09&nbsp; %s"
        + "<br><br>Type &nbsp;&nbsp;&#09&#09&#09&#09&#09 %s</pre>";

    private static final String REFERRAL_DETAILS = "<h3>Referral</h3>"
        + "<pre>Referred by &nbsp;&#09&#09&#09&#09&#09&#09&#09&#09&#09&nbsp; %s"
        + "<br><br>Referred to &nbsp;&nbsp;&#09&#09&#09&#09&#09&#09&#09&#09&#09&nbsp; %s"
        + "<br><br>Email address &nbsp;&#09&#09&#09&#09&#09&#09&#09&#09&nbsp; %s"
        + "<br><br>Urgent &nbsp;&#09&#09&#09&#09&#09&#09&#09&#09&#09&#09&#09&nbsp; %s"
        + "<br><br>Referral date &#09&#09&#09&#09&#09&#09&#09&#09&#09 %s"
        + "<br><br>Next hearing date &#09&#09&#09&#09&#09&#09&#09 %s"
        + "<br><br>Referral subject &#09&#09&#09&#09&#09&#09&#09&#09 %s"
        + "<br><br>Details of the referral &#09&#09&#09&#09&#09&#09 %s%s%s</pre><hr>";

    private static final String REPLY_DETAILS = "<h3>Reply %s</h3>"
        + "<pre>Reply by &nbsp;&nbsp;&#09&#09&#09&#09&#09&#09&#09&#09&#09&#09 %s"
        + "<br><br>Reply to &nbsp;&nbsp;&#09&#09&#09&#09&#09&#09&#09&#09&#09&#09 %s"
        + "<br><br>Email address &nbsp;&#09&#09&#09&#09&#09&#09&#09&#09 %s"
        + "<br><br>Urgent &nbsp;&#09&#09&#09&#09&#09&#09&#09&#09&#09&#09&#09 %s"
        + "<br><br>Referral date &nbsp;&nbsp;&#09&#09&#09&#09&#09&#09&#09&#09 %s"
        + "<br><br>Hearing date &nbsp;&nbsp;&#09&#09&#09&#09&#09&#09&#09&#09 %s"
        + "<br><br>Referral subject &nbsp;&nbsp;&#09&#09&#09&#09&#09&#09&#09 %s"
        + "<br><br>Directions &nbsp;&nbsp;&nbsp;&#09&#09&#09&#09&#09&#09&#09&#09&#09 %s%s%s</pre><hr>";

    private static final String DOCUMENT_LINK = "<br><br>Documents &nbsp;&#09&#09&#09&#09&#09&#09&#09&#09&#09"
        + " <a href=\"%s\" download>%s</a>&nbsp;";

    private static final String GENERAL_NOTES = "<br><br>General notes &nbsp;&#09&#09&#09&#09&#09&#09&#09&#09 %s";

    private static final String INSTRUCTIONS = "<br><br>Recommended instructions &nbsp;&#09&#09&#09&nbsp; %s";

    private static final String INVALID_EMAIL_ERROR_MESSAGE = "The email address entered is invalid.";

    private static final String JUDGE_DIRECTION_BODY = "A judge has sent directions on this employment tribunal case.";

    private static final String GENERIC_MESSAGE_BODY = "You have a new message about this employment tribunal case.";

    /**
     * Checks to see if the user is a judge.
     */
    public static String isJudge(List<String> roles) {
        if (roles.contains(JUDGE_ROLE_ENG) || roles.contains(JUDGE_ROLE_SCOT)) {
            return TRUE;
        }
        return FALSE;
    }

    /**
     * Populates Hearing, Referral and Replies details. For judges only hearing and referral details will be displayed.
     */
    public static String populateHearingReferralDetails(CaseData caseData) {
        return populateHearingDetails(caseData) + populateReferralDetails(caseData)
            + populateReplyDetails(caseData);
    }

    /**
     * Formats the hearing details into HTML for ExUI to display. It's expected that there are at least one hearing
     * already created before this event is started. Hearing details should contain the hearing date, hearing
     * type and the track type for each hearing.
     */
    public static String populateHearingDetails(CaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getHearingCollection())) {
            return "";
        }
        String trackType = caseData.getTrackType();
        StringBuilder hearingDetails = new StringBuilder();
        int count = 0;
        boolean singleHearing = caseData.getHearingCollection().size() == 1;

        for (HearingTypeItem hearing : caseData.getHearingCollection()) {
            for (DateListedTypeItem hearingDates : hearing.getValue().getHearingDateCollection()) {
                hearingDetails.append(
                    String.format(
                        HEARING_DETAILS,
                        singleHearing ? "" : ++count,
                        UtilHelper.formatLocalDate(hearingDates.getValue().getListedDate()),
                        hearing.getValue().getHearingType(),
                        trackType != null ? trackType : "N/A")
                );
            }
        }

        hearingDetails.append("<hr>");
        return hearingDetails.toString();
    }

    private static String populateReferralDetails(CaseData caseData) {
        ReferralType referral = getSelectedReferral(caseData);
        String referralDocLink = "";
        if (CollectionUtils.isNotEmpty(referral.getReferralDocument())) {
            referralDocLink = referral.getReferralDocument().stream()
                .map(d -> String.format(DOCUMENT_LINK, createDocLinkBinary(d),
                    d.getValue().getUploadedDocument().getDocumentFilename()))
                .collect(Collectors.joining());
        }
        return String.format(REFERRAL_DETAILS, referral.getReferredBy(), referral.getReferCaseTo(),
            referral.getReferentEmail(), referral.getIsUrgent(), referral.getReferralDate(),
            getNearestHearingToReferral(caseData, "None"),
            referral.getReferralSubject(), referral.getReferralDetails(), referralDocLink,
            createReferralInstructions(referral.getReferralInstruction()));
    }

    private static String populateReplyDetails(CaseData caseData) {
        ReferralType referral = getSelectedReferral(caseData);
        List<ReferralReplyTypeItem> replyCollection = referral.getReferralReplyCollection();
        if (replyCollection == null) {
            return "";
        }

        AtomicInteger count = new AtomicInteger();
        boolean singleReply = replyCollection.size() == 1;
        return replyCollection.stream()
            .map(r -> String.format(REPLY_DETAILS, singleReply ? "" : count.incrementAndGet(),
                r.getValue().getReplyBy(), r.getValue().getDirectionTo(), r.getValue().getReplyToEmailAddress(),
                r.getValue().getIsUrgentReply(), r.getValue().getReplyDate(),
                getNearestHearingToReferral(caseData, "None"), referral.getReferralSubject(),
                r.getValue().getDirectionDetails(), createDocLinkFromCollection(r.getValue().getReplyDocument()),
                createGeneralNotes(r.getValue().getReplyGeneralNotes())))
            .collect(Collectors.joining());
    }

    private static String createReferralInstructions(String instructions) {
        if (instructions == null) {
            return "";
        }
        return String.format(INSTRUCTIONS, instructions);
    }

    private static String createGeneralNotes(String notes) {
        if (notes == null) {
            return "";
        }
        return String.format(GENERAL_NOTES, notes);
    }

    private static String createDocLinkFromCollection(List<DocumentTypeItem> docItem) {
        if (docItem == null) {
            return "";
        }

        return docItem.stream()
            .map(d -> String.format(DOCUMENT_LINK, createDocLinkBinary(d),
                d.getValue().getUploadedDocument().getDocumentFilename()))
            .collect(Collectors.joining());
    }

    private static String createDocLinkBinary(DocumentTypeItem documentTypeItem) {
        String documentBinaryUrl = documentTypeItem.getValue().getUploadedDocument().getDocumentBinaryUrl();
        return documentBinaryUrl.substring(documentBinaryUrl.indexOf("/documents/"));
    }

    private static ReferralType getSelectedReferral(CaseData caseData) {
        return caseData.getReferralCollection()
            .get(Integer.parseInt(caseData.getSelectReferral().getValue().getCode()) - 1).getValue();
    }

    /**
     * Creates a referral and adds it to the referral collection.
     * @param caseData contains all the case data
     * @param userFullName Full name of the logged-in user
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

        referralType.setReferralHearingDate(getNearestHearingToReferral(caseData, "None"));

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
    private static String getNearestHearingToReferral(CaseData caseData, String defaultValue) {
        String earliestFutureHearingDate = HearingsHelper.getEarliestFutureHearingDate(caseData.getHearingCollection());

        if (earliestFutureHearingDate == null) {
            return defaultValue;
        }

        try {
            Date hearingStartDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").parse(earliestFutureHearingDate);
            return new SimpleDateFormat("dd MMM yyyy").format(hearingStartDate);
        } catch (ParseException e) {
            log.info("Failed to parse hearing date when creating new referral");
            return defaultValue;
        }
    }

    /**
     * Resets the case data fields relating to creating a referral so that they won't be auto populated when
     * creating a new referral.
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

    /**
     * Create fields for referral dropdown selector.
     * @param caseData contains all the case data
     */
    public static DynamicFixedListType populateSelectReferralDropdown(CaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getReferralCollection())) {
            return null;
        }

        return DynamicFixedListType.from(caseData.getReferralCollection().stream()
            .filter(r -> !r.getValue().getReferralStatus().equals(ReferralStatus.CLOSED))
            .map(r -> DynamicValueType.create(
                r.getValue().getReferralNumber(),
                r.getValue().getReferralNumber() + " " + r.getValue().getReferralSubject()))
            .collect(Collectors.toList()));
    }

    /**
     * Creates a referral reply and adds it to the referral reply collection.
     * @param caseData contains all the case data
     * @param userFullName The full name of the logged-in user
     */
    public static void createReferralReply(CaseData caseData, String userFullName) {
        ReferralType referral = getSelectedReferral(caseData);
        if (CollectionUtils.isEmpty(referral.getReferralReplyCollection())) {
            referral.setReferralReplyCollection(new ArrayList<>());
        }
        ReferralReplyType referralReply = new ReferralReplyType();

        referralReply.setReplyBy(userFullName);
        referralReply.setReplyDate(Helper.getCurrentDate());
        referralReply.setReplyToEmailAddress(caseData.getReplyToEmailAddress());
        referralReply.setIsUrgentReply(caseData.getIsUrgentReply());
        referralReply.setReplyDocument(caseData.getReplyDocument());
        referralReply.setReplyGeneralNotes(caseData.getReplyGeneralNotes());
        referralReply.setDirectionTo(caseData.getDirectionTo() != null
            ? caseData.getDirectionTo() : caseData.getReplyTo());

        referralReply.setDirectionDetails(caseData.getDirectionDetails() != null
            ? caseData.getDirectionDetails() : caseData.getReplyDetails());

        ReferralReplyTypeItem referralReplyTypeItem = new ReferralReplyTypeItem();
        referralReplyTypeItem.setId(UUID.randomUUID().toString());
        referralReplyTypeItem.setValue(referralReply);

        List<ReferralReplyTypeItem> referralReplyCollection = referral.getReferralReplyCollection();
        referralReplyCollection.add(referralReplyTypeItem);
        referral.setReferralReplyCollection(referralReplyCollection);
        referral.setReferralStatus(ReferralStatus.INSTRUCTIONS_ISSUED);
        clearReferralReplyDataFromCaseData(caseData);
    }

    /**
     * Resets the case data fields relating to replying to a referral so that they won't be auto populated when
     * creating a new referral.
     * @param caseData contains all the case data
     */
    public static void clearReferralReplyDataFromCaseData(CaseData caseData) {
        caseData.setHearingAndReferralDetails(null);
        caseData.setDirectionTo(null);
        caseData.setReplyToEmailAddress(null);
        caseData.setIsUrgentReply(null);
        caseData.setDirectionDetails(null);
        caseData.setReplyDocument(null);
        caseData.setReplyGeneralNotes(null);
        caseData.setReplyTo(null);
        caseData.setReplyDetails(null);
    }

    /**
     * Resets the case data fields relating to closing a referral so that they won't be auto populated when
     * creating a new referral.
     * @param caseData contains all the case data
     */
    public static void clearCloseReferralDataFromCaseData(CaseData caseData) {
        caseData.setSelectReferral(null);
        caseData.setCloseReferralHearingDetails(null);
        caseData.setConfirmCloseReferral(null);
        caseData.setCloseReferralGeneralNotes(null);
    }

    public static void setReferralStatusToClosed(CaseData caseData) {
        ReferralType referral = getSelectedReferral(caseData);
        referral.setReferralStatus(ReferralStatus.CLOSED);
    }

    public static List<String> validateEmail(String email) {
        List<String> errors = new ArrayList<>();
        if (!EmailValidator.getInstance().isValid(email)) {
            errors.add(INVALID_EMAIL_ERROR_MESSAGE);
        }

        return errors;
    }

    public static Map<String, String> sendReferralEmail(CaseData caseData, boolean isJudge, boolean isNewReferral) {
        Map<String, String> personalisation = buildPersonalisation(caseData, isNewReferral);
        personalisation.put("body", isJudge ? JUDGE_DIRECTION_BODY : GENERIC_MESSAGE_BODY);

        return personalisation;
    }

    private static Map<String, String> buildPersonalisation(CaseData caseData, boolean isNewReferral) {
        Map<String, String> personalisation = new HashMap<>();
        personalisation.put("caseNumber", caseData.getEthosCaseReference());
        personalisation.put("emailFlag", isNewReferral
            ? getEmailFlag(caseData.getIsUrgent()) : getEmailFlag(caseData.getIsUrgentReply()));
        personalisation.put("claimant", caseData.getClaimant());
        personalisation.put("respondents",
            caseData.getRespondentCollection().stream().map(o -> o.getValue().getRespondentName())
                .collect(Collectors.joining(", ")));
        personalisation.put("date", getNearestHearingToReferral(caseData, "Not set"));
        return personalisation;
    }

    private static String getEmailFlag(String isUrgent) {
        return YES.equals(isUrgent) ? "URGENT" : "";
    }
}
