package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.elasticsearch.common.Strings;
import org.webjars.NotFoundException;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.ecm.common.model.helper.DocumentCategory;
import uk.gov.hmcts.ecm.common.model.helper.DocumentConstants;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.ListTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.ReferralReplyTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.ReferralTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ReferralReplyType;
import uk.gov.hmcts.et.common.model.ccd.types.ReferralType;
import uk.gov.hmcts.et.common.model.ccd.types.UpdateReferralType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.et.common.model.generic.BaseCaseData;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.documents.ReferralTypeData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.documents.ReferralTypeDocument;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.documents.TornadoDocument;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CONCILIATION_TRACK_FAST_TRACK;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CONCILIATION_TRACK_NO_CONCILIATION;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.CASE_NUMBER;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.CLAIMANT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.DATE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.EMAIL_FLAG;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.LINK_TO_EXUI;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.RESPONDENTS;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.DocumentHelper.createDocumentTypeItemFromTopLevel;

@Slf4j
public final class ReferralHelper {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String TRUE = "True";
    private static final String FALSE = "False";
    private static final String NO_TRACK_NAME = "No Track";
    private static final String SHORT_TRACK_NAME = "Short track";

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

    private static final String REF_OUTPUT_NAME = "Referral Summary.pdf";
    private static final String REF_SUMMARY_TEMPLATE_NAME = "EM-TRB-EGW-ENG-00067.docx";

    private static final String GENERAL_NOTES = "<br><br>General notes &nbsp;&#09&#09&#09&#09&#09&#09&#09&#09 %s";

    private static final String INSTRUCTIONS = "<br><br>Recommended instructions &nbsp;&#09&#09&#09&nbsp; %s";

    private static final String INVALID_EMAIL_ERROR_MESSAGE = "The email address entered is invalid.";

    private static final String EMAIL_BODY_NEW = "You have a new referral on this case.";

    private static final String EMAIL_BODY_REPLY = "You have a reply to a referral on this case.";

    private static final String REPLY_REFERRAL_REP = "Reply by";

    private static final String REPLY_REFERRAL_REF = "Referred by";
    private static final String REFERRAL_DOCUMENT_NAME = "Referral %s - %s.pdf";

    private ReferralHelper() {
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

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
        return populateHearingDetails(caseData.getHearingCollection(), caseData.getConciliationTrack())
                + populateReferralDetails(caseData, caseData)
                + populateReplyDetails(caseData, caseData);
    }

    /**
     * Populates Hearing, Referral and Replies details. For judges only hearing and referral details will be displayed.
     */
    public static String populateHearingReferralDetails(MultipleData caseData, CaseData leadCase) {
        return populateHearingDetails(leadCase.getHearingCollection(), leadCase.getConciliationTrack())
                + populateReferralDetails(caseData, leadCase)
                + populateReplyDetails(caseData, leadCase);
    }

    public static void populateUpdateReferralDetails(CaseData caseData) {
        ReferralType referral = getSelectedReferral(caseData);
        caseData.setUpdateReferCaseTo(referral.getReferCaseTo());
        caseData.setUpdateReferralSubject(referral.getReferralSubject());
        caseData.setUpdateReferralDetails(referral.getReferralDetails());
        caseData.setUpdateReferentEmail(referral.getReferentEmail());
        caseData.setUpdateIsUrgent(referral.getIsUrgent());
        caseData.setUpdateReferralInstruction(referral.getReferralInstruction());
        caseData.setUpdateReferralSubjectSpecify(referral.getReferralSubjectSpecify());
        caseData.setUpdateReferralDocument(referral.getReferralDocument());
    }

    public static String populateHearingDetails(CaseData caseData) {
        return populateHearingDetails(caseData.getHearingCollection(), caseData.getConciliationTrack());
    }

    /**
     * Formats the hearing details into HTML for ExUI to display. It's expected that there are at least one hearing
     * already created before this event is started. Hearing details should contain the hearing date, hearing
     * type and the track type for each hearing.
     */
    public static String populateHearingDetails(List<HearingTypeItem> hearingCollection, String conciliationTrack) {
        if (CollectionUtils.isEmpty(hearingCollection)) {
            return "";
        }

        StringBuilder hearingDetails = new StringBuilder();
        int count = 0;
        boolean singleHearing = hearingCollection.size() == 1;

        for (HearingTypeItem hearing : hearingCollection) {
            for (DateListedTypeItem hearingDates : hearing.getValue().getHearingDateCollection()) {
                hearingDetails.append(
                        String.format(
                                HEARING_DETAILS,
                                singleHearing ? "" : ++count,
                                UtilHelper.formatLocalDate(hearingDates.getValue().getListedDate()),
                                hearing.getValue().getHearingType(),
                                getConciliationTrackName(conciliationTrack))
                );
            }
        }

        hearingDetails.append("<hr>");
        return hearingDetails.toString();
    }

    /**
     * This is required as we have to keep the old track names in CCD Config so that
     * existing cases with the track values stored in database won't break.
     */
    private static String getConciliationTrackName(String conciliationTrack) {
        if (conciliationTrack == null) {
            return "N/A";
        }
        if (CONCILIATION_TRACK_NO_CONCILIATION.equals(conciliationTrack)) {
            return NO_TRACK_NAME;
        }
        if (CONCILIATION_TRACK_FAST_TRACK.equals(conciliationTrack)) {
            return SHORT_TRACK_NAME;
        }

        return conciliationTrack;
    }

    private static String populateReferralDetails(BaseCaseData referralCase, CaseData leadCase) {
        ReferralType referral = getSelectedReferral(referralCase);
        String referralDocLink = "";
        if (CollectionUtils.isNotEmpty(referral.getReferralDocument())) {
            referralDocLink = referral.getReferralDocument().stream()
                .map(ReferralHelper::getReferralDocLink)
                .collect(Collectors.joining());

        }
        return String.format(REFERRAL_DETAILS, referral.getReferredBy(), referral.getReferCaseTo(),
                referral.getReferentEmail(), referral.getIsUrgent(), referral.getReferralDate(),
                getNearestHearingToReferral(leadCase, "None"),
                referral.getReferralSubject(), referral.getReferralDetails(), referralDocLink,
                createReferralInstructions(referral.getReferralInstruction()));
    }

    private static String getReferralDocLink(DocumentTypeItem documentTypeItem) {
        String docFileName = "";
        if (documentExists(documentTypeItem)) {
            docFileName = documentTypeItem.getValue().getUploadedDocument().getDocumentFilename();
            return String.format(DOCUMENT_LINK, createDocLinkBinary(documentTypeItem),
                    docFileName);
        } else {
            return docFileName;
        }
    }

    private static boolean documentExists(DocumentTypeItem documentTypeItem) {
        return documentTypeItem != null && documentTypeItem.getValue() != null
                && documentTypeItem.getValue().getUploadedDocument() != null
                && !Strings.isNullOrEmpty(documentTypeItem.getValue()
                .getUploadedDocument().getDocumentBinaryUrl());
    }

    private static String populateReplyDetails(BaseCaseData referralCase, CaseData leadCase) {
        ReferralType referral = getSelectedReferral(referralCase);
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
                        getNearestHearingToReferral(leadCase, "None"), referral.getReferralSubject(),
                        r.getValue().getDirectionDetails(), createDocLinkFromCollection(
                                r.getValue().getReplyDocument()),
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
            .map(ReferralHelper::getReferralDocLink)
            .collect(Collectors.joining());
    }

    private static String createDocLinkBinary(DocumentTypeItem documentTypeItem) {
        String documentBinaryUrl = documentTypeItem.getValue().getUploadedDocument().getDocumentBinaryUrl();
        if (!Strings.isNullOrEmpty(documentBinaryUrl) && documentBinaryUrl.contains("/documents/")) {
            return documentBinaryUrl.substring(documentBinaryUrl.indexOf("/documents/"));
        } else {
            return "";
        }
    }

    public static ReferralType getSelectedReferral(BaseCaseData caseData) {
        return caseData.getReferralCollection()
                .get(Integer.parseInt(caseData.getSelectReferral().getValue().getCode()) - 1).getValue();
    }
    
    /**
     * Gets the number a new referral should be labelled as.
     * @param referrals contains the list of referrals
     */
    public static int getNextReferralNumber(List<?> referrals) {
        if (CollectionUtils.isEmpty(referrals)) {
            return 1;
        }
        return referrals.size() + 1;
    }

    /**
     * Creates a referral and adds it to the referral collection.
     * @param caseData contains all the case data
     * @param userFullName Full name of the logged-in user
     */
    public static void createReferral(CaseData caseData, String userFullName,
                                      UploadedDocumentType documentInfo) {
        if (CollectionUtils.isEmpty(caseData.getReferralCollection())) {
            caseData.setReferralCollection(new ArrayList<>());
        }

        ReferralType referralType = new ReferralType();

        referralType.setReferralNumber(String.valueOf(getNextReferralNumber(caseData.getReferralCollection())));
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
        referralType.setReferralSummaryPdf(documentInfo);

        ReferralTypeItem referralTypeItem = new ReferralTypeItem();
        referralTypeItem.setId(UUID.randomUUID().toString());
        referralTypeItem.setValue(referralType);

        List<ReferralTypeItem> referralCollection = caseData.getReferralCollection();
        referralCollection.add(referralTypeItem);
        caseData.setReferralCollection(referralCollection);
    }

    public static boolean isValidReferralStatus(CaseData caseData) {
        ReferralType referral = caseData.getReferralCollection()
                .get(Integer.parseInt(caseData.getSelectReferral().getValue().getCode()) - 1).getValue();
        return ReferralStatus.AWAITING_INSTRUCTIONS.equals(referral.getReferralStatus());
    }

    /**
     * Updates a referral and adds it to the update referral collection.
     * @param caseData contains all the case data
     * @param userFullName Full name of the logged-in user
     */
    public static void updateReferral(CaseData caseData, String userFullName) {
        ReferralType referral = caseData.getReferralCollection()
                .get(Integer.parseInt(caseData.getSelectReferral().getValue().getCode()) - 1).getValue();
        if (CollectionUtils.isEmpty(referral.getUpdateReferralCollection())) {
            referral.setUpdateReferralCollection(new ListTypeItem<>());
        }
        UpdateReferralType updateReferralType = new UpdateReferralType();
        updateReferralType.setUpdateReferralNumber(String.valueOf(
                getNextReferralNumber(referral.getUpdateReferralCollection())));
        updateReferralType.setUpdateReferCaseTo(caseData.getUpdateReferCaseTo());
        updateReferralType.setUpdateIsUrgent(caseData.getUpdateIsUrgent());
        updateReferralType.setUpdateReferralSubject(caseData.getUpdateReferralSubject());
        updateReferralType.setUpdateReferralSubjectSpecify(caseData.getUpdateReferralSubjectSpecify());
        updateReferralType.setUpdateReferralDetails(caseData.getUpdateReferralDetails());
        updateReferralType.setUpdateReferralDocument(caseData.getUpdateReferralDocument());
        updateReferralType.setUpdateReferralInstruction(caseData.getUpdateReferralInstruction());
        updateReferralType.setUpdateReferralDate(Helper.getCurrentDate());
        updateReferralType.setUpdateReferredBy(userFullName);
        updateReferralType.setUpdateReferentEmail(caseData.getUpdateReferentEmail());
        updateReferralType.setUpdateReferralHearingDate(getNearestHearingToReferral(caseData, "None"));
        ListTypeItem<UpdateReferralType> updateReferralCollection = referral.getUpdateReferralCollection();
        updateReferralCollection.add(GenericTypeItem.from(UUID.randomUUID().toString(), updateReferralType));
        referral.setUpdateReferralCollection(updateReferralCollection);
        updateOriginalReferral(caseData, userFullName);
    }

    private static void updateOriginalReferral(CaseData caseData, String userFullName) {
        ReferralType referral = caseData.getReferralCollection()
                .get(Integer.parseInt(caseData.getSelectReferral().getValue().getCode()) - 1).getValue();

        referral.setReferCaseTo(caseData.getUpdateReferCaseTo());
        referral.setIsUrgent(caseData.getUpdateIsUrgent());
        referral.setReferralSubject(caseData.getUpdateReferralSubject());
        referral.setReferralSubjectSpecify(caseData.getUpdateReferralSubjectSpecify());
        referral.setReferralDetails(caseData.getUpdateReferralDetails());
        referral.setReferralDocument(caseData.getUpdateReferralDocument());
        referral.setReferralInstruction(caseData.getUpdateReferralInstruction());
        referral.setReferralDate(Helper.getCurrentDate());
        referral.setReferentEmail(caseData.getUpdateReferentEmail());
        referral.setReferredBy(userFullName);
    }

    /**
     * Formats data needed for Referral PDF Document.
     * @param caseData the case in which we extract the referral type
     * @return stringified json data for pdf document
     */
    public static String getDocumentRequest(CaseData caseData, String accessKey) throws JsonProcessingException {
        ReferralTypeData data;
        if (caseData.getReferentEmail() != null || caseData.getSelectReferral() == null) {
            data = newReferralRequest(caseData);
        } else {
            data = existingReferralRequest(caseData);
        }
        ReferralTypeDocument document = ReferralTypeDocument.builder()
                .accessKey(accessKey)
                .outputName(REF_OUTPUT_NAME)
                .templateName(REF_SUMMARY_TEMPLATE_NAME)
                .data(data).build();
        return OBJECT_MAPPER.writeValueAsString(document);
    }

    /**
     * Formats data needed for Referral PDF Document.
     * @param caseData the case in which we extract the referral type
     * @return stringified json data for pdf document
     */
    public static TornadoDocument getDocumentRequest(MultipleData caseData, CaseData leadCase, String accessKey) {
        ReferralTypeData data;
        if (caseData.getReferentEmail() != null || caseData.getSelectReferral() == null) {
            data = newReferralRequest(caseData, leadCase);
        } else {
            data = existingReferralRequest(caseData, leadCase);
        }

        return TornadoDocument.<ReferralTypeData>builder()
            .accessKey(accessKey)
            .outputName(REF_OUTPUT_NAME)
            .templateName(REF_SUMMARY_TEMPLATE_NAME)
            .data(data).build();
    }

    /**
     * Creates a new referral using the caseData temporary variables.
     * @param caseData contains the temporary variables
     * @return a referral object which can then be mapped into the pdf doc
     */
    private static ReferralTypeData newReferralRequest(CaseData caseData) {
        String caseNumber = StringUtils.isBlank(caseData.getMultipleReference())
                ? caseData.getEthosCaseReference()
                : caseData.getMultipleReference();
        return ReferralTypeData.builder()
                .caseNumber(defaultIfEmpty(caseNumber, null))
                .referralDate(Helper.getCurrentDate())
                .referredBy(defaultIfEmpty(caseData.getReferredBy(), null))
                .referCaseTo(defaultIfEmpty(caseData.getReferCaseTo(), null))
                .referentEmail(defaultIfEmpty(caseData.getReferentEmail(), null))
                .urgent(defaultIfEmpty(caseData.getIsUrgent(), null))
                .nextHearingDate(getNearestHearingToReferral(caseData, "None"))
                .referralSubject(defaultIfEmpty(caseData.getReferralSubject(), null))
                .referralDetails(defaultIfEmpty(caseData.getReferralDetails(), null))
                .referralDocument(caseData.getReferralDocument())
                .referralInstruction(defaultIfEmpty(caseData.getReferralInstruction(), null))
                .referralStatus(ReferralStatus.AWAITING_INSTRUCTIONS).build();
    }

    /**
     * Creates a new referral using the caseData temporary variables.
     * @param caseData contains the temporary variables
     * @return a referral object which can then be mapped into the pdf doc
     */
    private static ReferralTypeData newReferralRequest(MultipleData caseData, CaseData leadCase) {
        String caseNumber = caseData.getMultipleReference();
        return ReferralTypeData.builder()
                .caseNumber(defaultIfEmpty(caseNumber, null))
                .referralDate(Helper.getCurrentDate())
                .referredBy(defaultIfEmpty(caseData.getReferredBy(), null))
                .referCaseTo(defaultIfEmpty(caseData.getReferCaseTo(), null))
                .referentEmail(defaultIfEmpty(caseData.getReferentEmail(), null))
                .urgent(defaultIfEmpty(caseData.getIsUrgent(), null))
                .nextHearingDate(getNearestHearingToReferral(leadCase, "None"))
                .referralSubject(defaultIfEmpty(caseData.getReferralSubject(), null))
                .referralDetails(defaultIfEmpty(caseData.getReferralDetails(), null))
                .referralDocument(caseData.getReferralDocument())
                .referralInstruction(defaultIfEmpty(caseData.getReferralInstruction(), null))
                .referralStatus(ReferralStatus.AWAITING_INSTRUCTIONS).build();
    }

    /**
     * Creates a referral using the existing selected Referral.
     * @param caseData contains selected referral
     * @return a referral object which can then be mapped into the pdf doc
     */
    private static ReferralTypeData existingReferralRequest(CaseData caseData) {
        ReferralType referral = getSelectedReferral(caseData);
        return ReferralTypeData.builder()
                .caseNumber(defaultIfEmpty(caseData.getEthosCaseReference(), null))
                .referralDate(Helper.getCurrentDate())
                .referredBy(defaultIfEmpty(referral.getReferredBy(), null))
                .referCaseTo(defaultIfEmpty(referral.getReferCaseTo(), null))
                .referentEmail(defaultIfEmpty(referral.getReferentEmail(), null))
                .urgent(defaultIfEmpty(referral.getIsUrgent(), null))
                .nextHearingDate(getNearestHearingToReferral(caseData, "None"))
                .referralSubject(defaultIfEmpty(referral.getReferralSubject(), null))
                .referralDetails(defaultIfEmpty(referral.getReferralDetails(), null))
                .referralDocument(referral.getReferralDocument())
                .referralInstruction(defaultIfEmpty(referral.getReferralInstruction(), null))
                .referralReplyCollection(referral.getReferralReplyCollection())
                .referralStatus(referral.getReferralStatus())
                .referralReplyCollection(referral.getReferralReplyCollection()).build();
    }

    /**
     * Creates a referral using the existing selected Referral.
     * @param caseData contains selected referral
     * @return a referral object which can then be mapped into the pdf doc
     */
    private static ReferralTypeData existingReferralRequest(MultipleData caseData, CaseData leadCase) {
        ReferralType referral = getSelectedReferral(caseData);
        return ReferralTypeData.builder()
                .caseNumber(defaultIfEmpty(caseData.getMultipleReference(), null))
                .referralDate(Helper.getCurrentDate())
                .referredBy(defaultIfEmpty(referral.getReferredBy(), null))
                .referCaseTo(defaultIfEmpty(referral.getReferCaseTo(), null))
                .referentEmail(defaultIfEmpty(referral.getReferentEmail(), null))
                .urgent(defaultIfEmpty(referral.getIsUrgent(), null))
                .nextHearingDate(getNearestHearingToReferral(leadCase, "None"))
                .referralSubject(defaultIfEmpty(referral.getReferralSubject(), null))
                .referralDetails(defaultIfEmpty(referral.getReferralDetails(), null))
                .referralDocument(referral.getReferralDocument())
                .referralInstruction(defaultIfEmpty(referral.getReferralInstruction(), null))
                .referralReplyCollection(referral.getReferralReplyCollection())
                .referralStatus(referral.getReferralStatus())
                .referralReplyCollection(referral.getReferralReplyCollection()).build();
    }

    /**
     * Gets the next hearing date from the referral, returns "None" if no suitable hearing date exists.
     * @param caseData contains all the case data
     * @return Returns next hearing date in "dd MMM yyyy" format or "None"
     */
    public static String getNearestHearingToReferral(CaseData caseData, String defaultValue) {
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
     * Resets the case data fields relating to updating a referral so that they won't be auto-populated when
     * updating a new referral.
     * @param caseData contains all the case data
     */
    public static void clearUpdateReferralDataFromCaseData(CaseData caseData) {
        caseData.setUpdateReferCaseTo(null);
        caseData.setUpdateReferentEmail(null);
        caseData.setUpdateIsUrgent(null);
        caseData.setUpdateReferralSubject(null);
        caseData.setUpdateReferralSubjectSpecify(null);
        caseData.setUpdateReferralDetails(null);
        caseData.setUpdateReferralDocument(null);
        caseData.setUpdateReferralInstruction(null);
    }

    /**
     * Create fields for referral dropdown selector.
     * @param caseData contains all the case data
     */
    public static DynamicFixedListType populateSelectReferralDropdown(List<ReferralTypeItem> referrals) {
        if (CollectionUtils.isEmpty(referrals)) {
            return null;
        }

        return DynamicFixedListType.from(referrals.stream()
                .filter(r -> !r.getValue().getReferralStatus().equals(ReferralStatus.CLOSED))
                .map(r -> DynamicValueType.create(
                        r.getValue().getReferralNumber(),
                        r.getValue().getReferralNumber() + " " + r.getValue().getReferralSubject())).toList());
    }

    /**
     * Creates a referral reply and adds it to the referral reply collection.
     * @param caseData contains all the case data
     * @param userFullName The full name of the logged-in user
     */
    public static void createReferralReply(BaseCaseData caseData, String userFullName, boolean waEnabled) {
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

        if (waEnabled) {
            // for Work Allocation DMNs only
            referralReply.setReplyDateTime(Helper.getCurrentDateTime());
            referralReply.setReferralSubject(referral.getReferralSubject());
            referralReply.setReferralNumber(referral.getReferralNumber());
        }

        ReferralReplyTypeItem referralReplyTypeItem = new ReferralReplyTypeItem();
        referralReplyTypeItem.setId(UUID.randomUUID().toString());
        referralReplyTypeItem.setValue(referralReply);

        List<ReferralReplyTypeItem> referralReplyCollection = referral.getReferralReplyCollection();
        referralReplyCollection.add(referralReplyTypeItem);
        referral.setReferralReplyCollection(referralReplyCollection);
        referral.setReferralStatus(ReferralStatus.INSTRUCTIONS_ISSUED);
    }

    /**
     * Resets the case data fields relating to replying to a referral so that they won't be auto populated when
     * creating a new referral.
     * @param caseData contains all the case data
     */
    public static void clearReferralReplyDataFromCaseData(BaseCaseData caseData) {
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
     * Resets the case data fields relating to closing a referral so that they won't be auto-populated when
     * creating a new referral.
     * @param caseData contains all the case data
     */
    public static void clearCloseReferralDataFromCaseData(BaseCaseData caseData) {
        caseData.setSelectReferral(null);
        caseData.setCloseReferralHearingDetails(null);
        caseData.setConfirmCloseReferral(null);
        caseData.setCloseReferralGeneralNotes(null);
    }

    public static void setReferralStatusToClosed(BaseCaseData caseData) {
        ReferralType referral = getSelectedReferral(caseData);
        referral.setReferralStatus(ReferralStatus.CLOSED);
        referral.setCloseReferralGeneralNotes(caseData.getCloseReferralGeneralNotes());
    }

    /**
     * Validates email address by using the Apache Commons validator, returns an error if the email is invalid.
     * @param email Contains email address of the person whom the referral is sent to.
     */
    public static List<String> validateEmail(String email) {
        List<String> errors = new ArrayList<>();
        if (!EmailValidator.getInstance().isValid(email)) {
            errors.add(INVALID_EMAIL_ERROR_MESSAGE);
        }

        return errors;
    }

    /**
     * Generates a map of personalised information that will be used for the
     * placeholder fields in the Referral email template.
     * @param caseData Contains all the case data.
     * @param referralNumber The number of the referral.
     * @param username Name of the user making or replying to this referral.
     * @param linkToExui link to Exui with the caseId as a parameter
     * @param isNew Flag for if this is a new referral.
     */
    public static Map<String, String> buildPersonalisation(CaseData caseData, String referralNumber, boolean isNew,
                                                           String username, String linkToExui) {
        Map<String, String> personalisation = new ConcurrentHashMap<>();
        personalisation.put(CASE_NUMBER, caseData.getEthosCaseReference());
        personalisation.put(EMAIL_FLAG, getEmailFlag(isNew ? caseData.getIsUrgent() : caseData.getIsUrgentReply()));
        personalisation.put(CLAIMANT, caseData.getClaimant());
        personalisation.put(RESPONDENTS, getRespondentNames(caseData));
        personalisation.put(DATE, getNearestHearingToReferral(caseData, "Not set"));
        personalisation.put("body", isNew ? EMAIL_BODY_NEW : EMAIL_BODY_REPLY);
        personalisation.put("refNumber", referralNumber);
        personalisation.put("subject", getReferralSubject(caseData, isNew));
        personalisation.put("username", username);
        personalisation.put("replyReferral", isNew ? REPLY_REFERRAL_REF : REPLY_REFERRAL_REP);
        personalisation.put(LINK_TO_EXUI, linkToExui);
        return personalisation;
    }

    /**
     * Generates a map of personalised information that will be used for the
     * placeholder fields in the Referral email template.
     * @param caseData Contains all the case data.
     * @param referralNumber The number of the referral.
     * @param username Name of the user making or replying to this referral.
     * @param linkToExui link to Exui with the caseId as a parameter
     * @param isNew Flag for if this is a new referral.
     */
    public static Map<String, String> buildPersonalisation(MultipleData caseData, CaseData leadCase,
            String referralNumber, boolean isNew, String username, String linkToExui) {

        Map<String, String> personalisation = new ConcurrentHashMap<>();
        personalisation.put(CASE_NUMBER, caseData.getMultipleReference());
        personalisation.put(EMAIL_FLAG, getEmailFlag(isNew ? caseData.getIsUrgent() : caseData.getIsUrgentReply()));
        personalisation.put(CLAIMANT, leadCase.getClaimant());
        personalisation.put(RESPONDENTS, getRespondentNames(leadCase));
        personalisation.put(DATE, getNearestHearingToReferral(leadCase, "Not set"));
        personalisation.put("body", isNew ? EMAIL_BODY_NEW : EMAIL_BODY_REPLY);
        personalisation.put("refNumber", referralNumber);
        personalisation.put("username", username);
        personalisation.put("replyReferral", isNew ? REPLY_REFERRAL_REF : REPLY_REFERRAL_REP);
        personalisation.put(LINK_TO_EXUI, linkToExui);

        if (isNew) {
            personalisation.put("subject", caseData.getReferralSubject());
        }

        ReferralType selectedReferral = getSelectedReferral(caseData);

        if (selectedReferral == null) {
            throw new NotFoundException("Referral not found");
        }

        personalisation.put("subject", selectedReferral.getReferralSubject());

        return personalisation;
    }

    public static Map<String, String> buildPersonalisationUpdateReferral(CaseDetails detail, String referralNumber,
                                                                         String username, String linkToExui) {
        CaseData caseData = detail.getCaseData();
        Map<String, String> personalisation = new ConcurrentHashMap<>();
        personalisation.put(CASE_NUMBER, caseData.getEthosCaseReference());
        personalisation.put(EMAIL_FLAG, caseData.getUpdateIsUrgent());
        personalisation.put(CLAIMANT, caseData.getClaimant());
        personalisation.put(RESPONDENTS, getRespondentNames(caseData));
        personalisation.put(DATE, getNearestHearingToReferral(caseData, "Not set"));
        personalisation.put("body", EMAIL_BODY_NEW);
        personalisation.put("refNumber", referralNumber);
        personalisation.put("subject", caseData.getUpdateReferralSubject());
        personalisation.put("username", username);
        personalisation.put("replyReferral", REPLY_REFERRAL_REF);
        personalisation.put(LINK_TO_EXUI, linkToExui);
        return personalisation;
    }

    /**
     * Gets errors in document upload.
     * @param documentTypeItems - a list from which referral document items are extracted
     * @param errors list
     */
    public static void addDocumentUploadErrors(List<DocumentTypeItem> documentTypeItems, List<String> errors) {
        for (DocumentTypeItem documentTypeItem : documentTypeItems) {
            if (!Strings.isNullOrEmpty(documentTypeItem.getValue().getShortDescription())
                && documentTypeItem.getValue().getUploadedDocument() == null) {
                errors.add("Short description is added but document is not uploaded.");
            }
        }
    }

    private static String getRespondentNames(CaseData caseData) {
        return caseData.getRespondentCollection().stream()
                .map(o -> o.getValue().getRespondentName())
                .collect(Collectors.joining(", "));
    }

    private static String getReferralSubject(CaseData caseData, boolean isNew) {
        if (isNew) {
            return caseData.getReferralSubject();
        }

        ReferralType selectedReferral = getSelectedReferral(caseData);
        if (selectedReferral == null) {
            throw new NotFoundException("Referral not found");
        }

        return selectedReferral.getReferralSubject();
    }

    private static String getEmailFlag(String isUrgent) {
        return YES.equals(isUrgent) ? "URGENT" : "";
    }

    public static void addReferralDocumentToDocumentCollection(CaseData caseData) {
        ReferralType referral = getSelectedReferral(caseData);
        UploadedDocumentType referralDocument = referral.getReferralSummaryPdf();
        if (ObjectUtils.isEmpty(referralDocument)) {
            return;
        }

        referralDocument.setDocumentFilename(getReferralDocumentName(referral));
        referralDocument.setCategoryId(DocumentCategory.REFERRAL_JUDICIAL_DIRECTION.getId());

        if (CollectionUtils.isEmpty(caseData.getDocumentCollection())) {
            caseData.setDocumentCollection(new ArrayList<>());
        }

        caseData.getDocumentCollection().add(createDocumentTypeItemFromTopLevel(referralDocument,
                DocumentConstants.CASE_MANAGEMENT, DocumentConstants.REFERRAL_JUDICIAL_DIRECTION, null));
        DocumentHelper.setDocumentNumbers(caseData);
    }

    private static String getReferralDocumentName(ReferralType referral) {
        return String.format(REFERRAL_DOCUMENT_NAME, referral.getReferralNumber(), referral.getReferralSubject());
    }

}
