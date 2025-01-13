package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.webjars.NotFoundException;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.ecm.common.model.helper.DocumentCategory;
import uk.gov.hmcts.ecm.common.model.helper.DocumentConstants;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.ListTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.ReferralReplyTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.ReferralTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;
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
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.CASE_NUMBER;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.CLAIMANT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.DATE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.EMAIL_FLAG;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.LINK_TO_EXUI;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.RESPONDENTS;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.EMPTY_STRING;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.MONTH_STRING_DATE_FORMAT;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.DocumentHelper.createDocumentTypeItemFromTopLevel;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.HearingsHelper.getHearingVenue;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.HearingsHelper.mapEarliest;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.MarkdownHelper.createTwoColumnTable;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.MarkdownHelper.detailsWrapper;

@Slf4j
public final class ReferralHelper {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String TRUE = "True";
    private static final String FALSE = "False";
    private static final String JUDGE_ROLE_ENG = "caseworker-employment-etjudge-englandwales";
    private static final String JUDGE_ROLE_SCOT = "caseworker-employment-etjudge-scotland";
    private static final String DOCUMENT_LINK = "<a href=\"%s\" download>%s</a><br>";
    private static final String REF_OUTPUT_NAME = "Referral Summary.pdf";
    private static final String REF_SUMMARY_TEMPLATE_NAME = "EM-TRB-EGW-ENG-00067.docx";
    private static final String INVALID_EMAIL_ERROR_MESSAGE = "The email address entered is invalid.";
    private static final String EMAIL_BODY_NEW = "You have a new referral on this case.";
    private static final String EMAIL_BODY_REPLY = "You have a reply to a referral on this case.";
    private static final String REPLY_REFERRAL_REP = "Reply by";
    private static final String REPLY_REFERRAL_REF = "Referred by";
    private static final String REFERRAL_DOCUMENT_NAME = "Referral %s - %s.pdf";
    private static final String NOT_SET = "Not set";
    private static final String REF_NUMBER = "refNumber";
    private static final String SUBJECT = "subject";
    private static final String USERNAME = "username";
    private static final String REPLY_REFERRAL = "replyReferral";
    private static final String EMAIL_ADDRESS = "Email address";
    private static final String URGENT = "Urgent";
    private static final String REFERRAL_SUBJECT = "Referral subject";
    private static final String DOCUMENTS = "Documents";
    private static final String CLOSE_PRE_TAG = "</pre>";
    private static final String BREAKS = "\r\n";

    // Below needed to fix a typo issue introduced when referrals went live
    public static final String PARTY_NOT_RESPONDED_COMPLIED = "Party not responded/complied";
    public static final String PARTY_NOT_RESPONDED_COMPILED = "Party not responded/compiled";

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
        String hearingDetails = populateHearingDetails(caseData);
        String referralDetails = createTwoColumnTable(new String[]{"Referral", EMPTY_STRING}, Stream.of(
                populateReferralDetails(caseData, caseData)).flatMap(Collection::stream).toList()) + BREAKS;
        String referralUpdateDetails = populateUpdateDetails(caseData, caseData);
        String referralReplyDetails = populateReplyDetails(caseData, caseData);
        return hearingDetails + referralDetails + referralUpdateDetails + referralReplyDetails;
    }

    /**
     * Populates Hearing, Referral and Replies details. For judges only hearing and referral details will be displayed.
     */
    public static String populateHearingReferralDetails(MultipleData caseData, CaseData leadCase) {
        String hearingDetails = populateHearingDetails(leadCase);
        String referralDetails = createTwoColumnTable(new String[]{"Referral", EMPTY_STRING}, Stream.of(
                populateReferralDetails(caseData, leadCase)).flatMap(Collection::stream).toList()) + BREAKS;
        String referralUpdateDetails = populateUpdateDetails(caseData, leadCase);
        String referralReplyDetails = populateReplyDetails(caseData, leadCase);
        return hearingDetails + referralDetails + referralUpdateDetails + referralReplyDetails;
    }

    private static String populateUpdateDetails(BaseCaseData referralCase, CaseData leadCase) {
        ReferralType referralType = getSelectedReferral(referralCase);
        ListTypeItem<UpdateReferralType> updateCollection = referralType.getUpdateReferralCollection();
        if (CollectionUtils.isEmpty(updateCollection)) {
            return "";
        }

        AtomicInteger count = new AtomicInteger();
        boolean singleUpdate = updateCollection.size() == 1;
        return detailsWrapper("Referral Updates", updateCollection.stream()
                .map(GenericTypeItem::getValue)
                .map(updateItem -> new ArrayList<>(
                List.of(new String[]{"<pre>Referred to", updateItem.getUpdateReferCaseTo()},
                        new String[]{EMAIL_ADDRESS, defaultIfEmpty(updateItem.getUpdateReferentEmail(), "-")},
                        new String[]{URGENT, updateItem.getUpdateIsUrgent()},
                        new String[]{"Next hearing date", getNearestHearingToReferral(leadCase, "None")},
                        new String[]{REFERRAL_SUBJECT, updateItem.getUpdateReferralSubject()},
                        new String[]{"Details of the referral",
                                formatReferralDetails(updateItem.getUpdateReferralDetails())},
                        new String[]{DOCUMENTS, createDocLinkFromCollection(updateItem.getUpdateReferralDocument())},
                        new String[]{"Recommended instructions",
                                formatReferralDetails(updateItem.getUpdateReferralInstruction())},
                        new String[]{"Updated by", updateItem.getUpdateReferredBy()},
                        new String[]{"Updated date", updateItem.getUpdateReferralDate() + CLOSE_PRE_TAG})))
                .map(updateRow -> createTwoColumnTable(
                        new String[]{"Update %s".formatted(singleUpdate ? "1" : count.incrementAndGet()), EMPTY_STRING},
                        Stream.of(updateRow).flatMap(Collection::stream).toList()) + BREAKS)
                .collect(Collectors.joining()));
    }

    public static void populateUpdateReferralDetails(BaseCaseData caseData) {
        ReferralType referral = getSelectedReferral(caseData);
        caseData.setUpdateReferCaseTo(referral.getReferCaseTo());
        caseData.setUpdateReferralSubject(PARTY_NOT_RESPONDED_COMPLIED.equals(referral.getReferralSubject())
                ? PARTY_NOT_RESPONDED_COMPILED
                : referral.getReferralSubject());
        caseData.setUpdateReferralDetails(referral.getReferralDetails());
        caseData.setUpdateReferentEmail(referral.getReferentEmail());
        caseData.setUpdateIsUrgent(referral.getIsUrgent());
        caseData.setUpdateReferralInstruction(referral.getReferralInstruction());
        caseData.setUpdateReferralSubjectSpecify(referral.getReferralSubjectSpecify());
        caseData.setUpdateReferralDocument(referral.getReferralDocument());
    }

    /**
     * Formats the hearing details into HTML for ExUI to display. It's expected that there are at least one hearing
     * already created before this event is started. Hearing details should contain the hearing date, hearing
     * type and the track type for each hearing.
     */
    public static String populateHearingDetails(CaseData caseData) {
        List<HearingTypeItem> hearingCollection = caseData.getHearingCollection();
        if (CollectionUtils.isEmpty(hearingCollection)) {
            log.info("No hearings on populateHearingDetails for {}", caseData.getEthosCaseReference());
            return "";
        }

        StringBuilder sb = new StringBuilder();

        hearingCollection.forEach(hearing -> {
            HearingType hearingType = hearing.getValue();
            DateListedTypeItem hearingDates = mapEarliest(hearing);
            if (ObjectUtils.isNotEmpty(hearingDates)) {
                ArrayList<String[]> hearingDetail = new ArrayList<>(List.of(
                        new String[]{"<pre>Hearing type", hearingType.getHearingType()},
                        new String[]{"Hearing venue", defaultIfEmpty(getHearingVenue(hearingType), "-")},
                        new String[]{"Date", UtilHelper.formatLocalDate(hearingDates.getValue().getListedDate())},
                        new String[]{"Estimated hearing length",
                                hearingType.getHearingEstLengthNum() + " " + hearingType.getHearingEstLengthNumType()
                                + CLOSE_PRE_TAG}));
                sb.append(createTwoColumnTable(
                        new String[]{"Hearing %s".formatted(hearingType.getHearingNumber()), EMPTY_STRING},
                        Stream.of(hearingDetail)
                                .flatMap(Collection::stream)
                                .toList()))
                        .append(BREAKS);
            }
        });
        if (isNullOrEmpty(sb.toString())) {
            return EMPTY_STRING;
        }
        return detailsWrapper("Hearings", sb.toString());
    }

    private static List<String[]> populateReferralDetails(BaseCaseData referralCase, CaseData leadCase) {
        ReferralType referral = getSelectedReferral(referralCase);
        String referralDocLink = "-";
        if (CollectionUtils.isNotEmpty(referral.getReferralDocument())) {
            referralDocLink = referral.getReferralDocument().stream()
                    .map(ReferralHelper::getReferralDocLink)
                    .collect(Collectors.joining());

        }

        return new ArrayList<>(
                List.of(new String[]{"<pre>Referred by", referral.getReferredBy()},
                        new String[]{"Referred to", referral.getReferCaseTo()},
                        new String[]{EMAIL_ADDRESS, defaultIfEmpty(referral.getReferentEmail(), "-")},
                        new String[]{URGENT, referral.getIsUrgent()},
                        new String[]{"Referral date", referral.getReferralDate()},
                        new String[]{"Next hearing date", getNearestHearingToReferral(leadCase, "None")},
                        new String[]{REFERRAL_SUBJECT, referral.getReferralSubject()},
                        new String[]{"Details of the referral", formatReferralDetails(referral.getReferralDetails())},
                        new String[]{DOCUMENTS, referralDocLink},
                        new String[]{"Recommended instructions",
                                formatReferralDetails(referral.getReferralInstruction()) + CLOSE_PRE_TAG}));
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
                && !isNullOrEmpty(documentTypeItem.getValue()
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

        return detailsWrapper("Referral Replies", replyCollection.stream()
                .map(ReferralReplyTypeItem::getValue)
                .map(reply -> new ArrayList<>(
                List.of(new String[]{"<pre>Reply by", reply.getReplyBy()},
                        new String[]{"Reply to", reply.getDirectionTo()},
                        new String[]{EMAIL_ADDRESS, defaultIfEmpty(reply.getReplyToEmailAddress(), "-")},
                        new String[]{URGENT, reply.getIsUrgentReply()},
                        new String[]{"Referral date", reply.getReplyDate()},
                        new String[]{"Hearing date", getNearestHearingToReferral(leadCase, "None")},
                        new String[]{REFERRAL_SUBJECT, reply.getReferralSubject()},
                        new String[]{"Directions", formatReferralDetails(reply.getDirectionDetails())},
                        new String[]{DOCUMENTS, createDocLinkFromCollection(reply.getReplyDocument())},
                        new String[]{"General Notes",
                                formatReferralDetails(reply.getReplyGeneralNotes()) + CLOSE_PRE_TAG})))
                .map(replyRows -> createTwoColumnTable(
                        new String[]{"Reply %s".formatted(singleReply ? "1" : count.incrementAndGet()), EMPTY_STRING},
                        Stream.of(replyRows)
                                .flatMap(Collection::stream)
                                .toList()) + BREAKS)
                .collect(Collectors.joining()));
    }

    private static String createDocLinkFromCollection(List<DocumentTypeItem> docItem) {
        if (docItem == null) {
            return "-";
        }

        return docItem.stream()
            .map(ReferralHelper::getReferralDocLink)
            .collect(Collectors.joining());
    }

    private static String createDocLinkBinary(DocumentTypeItem documentTypeItem) {
        String documentBinaryUrl = documentTypeItem.getValue().getUploadedDocument().getDocumentBinaryUrl();
        if (!isNullOrEmpty(documentBinaryUrl) && documentBinaryUrl.contains("/documents/")) {
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
    public static void createReferral(BaseCaseData caseData, String userFullName,
                                      UploadedDocumentType documentInfo, String nextHearingDate) {
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

        referralType.setReferralHearingDate(nextHearingDate);
        referralType.setReferralSummaryPdf(documentInfo);

        ReferralTypeItem referralTypeItem = new ReferralTypeItem();
        referralTypeItem.setId(UUID.randomUUID().toString());
        referralTypeItem.setValue(referralType);

        List<ReferralTypeItem> referralCollection = caseData.getReferralCollection();
        referralCollection.add(referralTypeItem);
        caseData.setReferralCollection(referralCollection);
    }

    /**
     * Creates a referral and adds it to the referral collection.
     * @param caseData contains all the case data
     * @param userFullName Full name of the logged-in user
     */
    public static void createReferral(CaseData caseData, String userFullName,
                                      UploadedDocumentType documentInfo) {
        String nextHearingDate = getNearestHearingToReferral(caseData, "None");
        createReferral(caseData, userFullName, documentInfo, nextHearingDate);
    }

    public static boolean isValidReferralStatus(BaseCaseData caseData) {
        ReferralType referral = caseData.getReferralCollection()
                .get(Integer.parseInt(caseData.getSelectReferral().getValue().getCode()) - 1).getValue();
        return ReferralStatus.AWAITING_INSTRUCTIONS.equals(referral.getReferralStatus());
    }

    /**
     * Updates a referral and adds it to the update referral collection.
     * @param caseData contains all the case data
     * @param userFullName Full name of the logged-in user
     */
    public static void updateReferral(BaseCaseData caseData, String userFullName, String nextHearingDate) {
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
        updateReferralType.setUpdateReferralHearingDate(nextHearingDate);
        ListTypeItem<UpdateReferralType> updateReferralCollection = referral.getUpdateReferralCollection();
        updateReferralCollection.add(GenericTypeItem.from(UUID.randomUUID().toString(), updateReferralType));
        referral.setUpdateReferralCollection(updateReferralCollection);
    }

    /**
     * Formats data needed for Referral PDF Document.
     * @param caseData the case in which we extract the referral type
     * @return stringified json data for pdf document
     */
    public static String getDocumentRequest(CaseData caseData, String accessKey) throws JsonProcessingException {
        ReferralTypeData data;
        if (caseData.getSelectReferral() == null) {
            data = newReferralRequest(caseData);
        } else {
            ReferralType referral = getSelectedReferral(caseData);
            data = rebuildReferral(caseData.getHearingCollection(), caseData.getEthosCaseReference(), referral);
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
    public static TornadoDocument<ReferralTypeData> getDocumentRequest(MultipleData caseData, CaseData leadCase,
                                                                       String accessKey) {
        ReferralTypeData data;
        if (caseData.getSelectReferral() == null) {
            data = newReferralRequest(caseData, leadCase);
        } else {
            ReferralType referral = getSelectedReferral(caseData);
            data = rebuildReferral(leadCase.getHearingCollection(), caseData.getMultipleReference(), referral);
        }

        return TornadoDocument.<ReferralTypeData>builder()
            .accessKey(accessKey)
            .outputName(REF_OUTPUT_NAME)
            .templateName(REF_SUMMARY_TEMPLATE_NAME)
            .data(data)
            .build();
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
     * @param hearings the list of hearings on a case
     * @param id the case id
     * @param referral the referral to rebuild
     * @return a referral object which can then be mapped into the pdf doc
     */
    private static ReferralTypeData rebuildReferral(List<HearingTypeItem> hearings, String id, ReferralType referral) {
        return ReferralTypeData.builder()
                .caseNumber(defaultIfEmpty(id, null))
                .referralDate(defaultIfEmpty(referral.getReferralDate(), null))
                .referredBy(defaultIfEmpty(referral.getReferredBy(), null))
                .referCaseTo(defaultIfEmpty(referral.getReferCaseTo(), null))
                .referentEmail(defaultIfEmpty(referral.getReferentEmail(), null))
                .urgent(defaultIfEmpty(referral.getIsUrgent(), null))
                .nextHearingDate(getNearestHearingToReferral(hearings, "None"))
                .referralSubject(defaultIfEmpty(referral.getReferralSubject(), null))
                .referralDetails(defaultIfEmpty(referral.getReferralDetails(), null))
                .referralDocument(referral.getReferralDocument())
                .referralInstruction(defaultIfEmpty(referral.getReferralInstruction(), null))
                .referralReplyCollection(referral.getReferralReplyCollection())
                .referralStatus(referral.getReferralStatus())
                .updateReferralCollection(referral.getUpdateReferralCollection())
                .build();
    }

    /**
     * Gets the next hearing date from the referral, returns "None" if no suitable hearing date exists.
     * @param hearingCollection list of hearings
     * @return Returns next hearing date in "dd MMM yyyy" format or "None"
     */
    public static String getNearestHearingToReferral(List<HearingTypeItem> hearingCollection, String defaultValue) {
        String earliestFutureHearingDate = HearingsHelper.getEarliestFutureHearingDate(hearingCollection);

        if (earliestFutureHearingDate == null) {
            return defaultValue;
        }

        try {
            Date hearingStartDate =
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.ENGLISH).parse(earliestFutureHearingDate);
            return new SimpleDateFormat(MONTH_STRING_DATE_FORMAT, Locale.ENGLISH).format(hearingStartDate);
        } catch (ParseException e) {
            log.info("Failed to parse hearing date when creating new referral");
            return defaultValue;
        }
    }

    /**
     * Gets the next hearing date from the referral, returns "None" if no suitable hearing date exists.
     * @param caseData contains all the case data
     * @return Returns next hearing date in "dd MMM yyyy" format or "None"
     */
    public static String getNearestHearingToReferral(CaseData caseData, String defaultValue) {
        return getNearestHearingToReferral(caseData.getHearingCollection(), defaultValue);
    }

    /**
     * Resets the case data fields relating to creating a referral so that they won't be autopopulated when
     * creating a new referral.
     * @param caseData contains all the case data
     */
    public static void clearReferralDataFromCaseData(BaseCaseData caseData) {
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
        caseData.setUpdateReferCaseTo(null);
        caseData.setUpdateReferentEmail(null);
        caseData.setUpdateIsUrgent(null);
        caseData.setUpdateReferralSubject(null);
        caseData.setUpdateReferralSubjectSpecify(null);
        caseData.setUpdateReferralDetails(null);
        caseData.setUpdateReferralDocument(null);
        caseData.setUpdateReferralInstruction(null);
        caseData.setSelectReferral(null);
    }

    /**
     * Create fields for referral dropdown selector.
     */
    public static DynamicFixedListType populateSelectReferralDropdown(List<ReferralTypeItem> referrals) {
        if (CollectionUtils.isEmpty(referrals)) {
            return null;
        }

        return DynamicFixedListType.from(referrals.stream()
                .filter(r -> !r.getValue().getReferralStatus().equals(ReferralStatus.CLOSED))
                .map(r -> DynamicValueType.create(
                        r.getValue().getReferralNumber(),
                        r.getValue().getReferralNumber() + " - " + r.getValue().getReferralSubject()))
                .toList());
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
     * Resets the case data fields relating to replying to a referral so that they won't be autopopulated when
     * creating a new referral.
     * @param caseData contains all the case data
     */
    public static void clearReferralReplyDataFromCaseData(BaseCaseData caseData) {
        caseData.setSelectReferral(null);
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
     * Resets the case data fields relating to closing a referral so that they won't be autopopulated when
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
        personalisation.put(DATE, getNearestHearingToReferral(caseData, NOT_SET));
        personalisation.put("body", isNew ? EMAIL_BODY_NEW : EMAIL_BODY_REPLY);
        personalisation.put(REF_NUMBER, referralNumber);
        personalisation.put(SUBJECT, defaultIfEmpty(getReferralSubject(caseData, isNew), ""));
        personalisation.put(USERNAME, username);
        personalisation.put(REPLY_REFERRAL, isNew ? REPLY_REFERRAL_REF : REPLY_REFERRAL_REP);
        personalisation.put(LINK_TO_EXUI, linkToExui);
        return personalisation;
    }

    /**
     * Generates a map of personalised information that will be used for the
     * placeholder fields in the Referral email template.
     * @param caseData Contains all the case data.
     * @param referralNumber The number of the referral.
     * @param username Name of the user making or replying to this referral.
     * @param exuiLink link to Exui with the caseId as a parameter
     * @param isNew Flag for if this is a new referral.
     */
    public static Map<String, String> buildPersonalisation(MultipleData caseData, CaseData leadCase,
            String referralNumber, boolean isNew, String username, String exuiLink) {

        Map<String, String> personalisation = new ConcurrentHashMap<>();
        personalisation.put(CASE_NUMBER, caseData.getMultipleReference());
        personalisation.put(CLAIMANT, leadCase.getClaimant());
        personalisation.put(RESPONDENTS, getRespondentNames(leadCase));
        personalisation.put(DATE, getNearestHearingToReferral(leadCase, NOT_SET));
        personalisation.put(REF_NUMBER, referralNumber);
        personalisation.put(USERNAME, username);
        personalisation.put(REPLY_REFERRAL, isNew ? REPLY_REFERRAL_REF : REPLY_REFERRAL_REP);
        personalisation.put(LINK_TO_EXUI, exuiLink);

        if (isNew) {
            personalisation.put("body", EMAIL_BODY_NEW);
            personalisation.put(SUBJECT, caseData.getReferralSubject());
            personalisation.put(EMAIL_FLAG, getEmailFlag(caseData.getIsUrgent()));
            return personalisation;
        }

        ReferralType selectedReferral = getSelectedReferral(caseData);

        if (selectedReferral == null) {
            throw new NotFoundException("Referral not found");
        }

        personalisation.put(SUBJECT, selectedReferral.getReferralSubject());
        personalisation.put(EMAIL_FLAG, getEmailFlag(caseData.getIsUrgentReply()));
        personalisation.put("body", EMAIL_BODY_REPLY);

        return personalisation;
    }

    /**
     * Gets errors in document upload.
     * @param documentTypeItems - a list from which referral document items are extracted
     * @param errors list
     */
    public static void addDocumentUploadErrors(List<DocumentTypeItem> documentTypeItems, List<String> errors) {
        for (DocumentTypeItem documentTypeItem : documentTypeItems) {
            if (!isNullOrEmpty(documentTypeItem.getValue().getShortDescription())
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

    public static void addReferralDocumentToDocumentCollection(BaseCaseData caseData) {
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

    private static String formatReferralDetails(String text) {
        return isNullOrEmpty(text) ? "-" : text.replace("\n", "<br>");
    }

    public static String setReferralSubject(String referralSubject) {
        if (isNullOrEmpty(referralSubject)) {
            return null;
        }
        switch (referralSubject) {
            case PARTY_NOT_RESPONDED_COMPILED -> {
                return PARTY_NOT_RESPONDED_COMPLIED;
            }
            case "Rule 21" -> {
                return  "Rule 22";
            }
            case "Rule 50 application" -> {
                return "Rule 49 Application";
            }
            default -> {
                return referralSubject;
            }
        }
    }

}
