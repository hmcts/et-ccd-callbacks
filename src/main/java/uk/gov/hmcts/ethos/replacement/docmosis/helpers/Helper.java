package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.ecm.common.model.labels.LabelPayloadES;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.ccd.SignificantItem;
import uk.gov.hmcts.et.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.JurCodesTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.CorrespondenceScotType;
import uk.gov.hmcts.et.common.model.ccd.types.CorrespondenceType;
import uk.gov.hmcts.et.common.model.ccd.types.DateListedType;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.BF_ACTION_ACAS;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.BF_ACTION_CASE_LISTED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.BF_ACTION_CASE_PAPERS;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.BF_ACTION_CASE_TRANSFERRED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.BF_ACTION_DRAFT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.BF_ACTION_ENQUIRY_ISSUED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.BF_ACTION_ENQUIRY_RECEIVED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.BF_ACTION_EXHIBITS;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.BF_ACTION_INTERLOCUTORY;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.BF_ACTION_IT3_RECEIVED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.BF_ACTION_OTHER_ACTION;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.BF_ACTION_POSTPONEMENT_REQUESTED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.BF_ACTION_REFER_CHAIRMAN;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.BF_ACTION_REPLY_TO_ENQUIRY;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.BF_ACTION_STRIKING_OUT_WARNING;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CASE_CLOSED_POSITION;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_POSTPONED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.INDIVIDUAL_TYPE_CLAIMANT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.OLD_DATE_TIME_PATTERN;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.COMPANY;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.MONTH_STRING_DATE_FORMAT;

@Slf4j
public final class Helper {

    public static final String HEARING_CREATION_NUMBER_ERROR = "A new hearing can only "
            + "be added from the List Hearing menu item";
    public static final String HEARING_CREATION_DAY_ERROR = "A new day for a hearing can "
            + "only be added from the List Hearing menu item";
    private static final String MY_HMCTS = "MyHMCTS";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    private Helper() {
        OBJECT_MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    public static String nullCheck(String value) {
        Optional<String> opt = Optional.ofNullable(value);
        if (opt.isPresent()) {
            return value.replaceAll("\"", "'");
        } else {
            return "";
        }
    }

    public static SignificantItem generateSignificantItem(DocumentInfo documentInfo, List<String> errors) {
        log.info("generateSignificantItem for document: " + documentInfo);
        if (documentInfo == null) {
            errors.add("Error processing document");
            return new SignificantItem();
        } else {
            return SignificantItem.builder()
                    .url(documentInfo.getUrl())
                    .description(documentInfo.getDescription())
                    .type(SignificantItemType.DOCUMENT.name())
                    .build();
        }
    }

    private static List<DynamicValueType> createDynamicRespondentAddressFixedList(
            List<RespondentSumTypeItem> respondentCollection) {
        List<DynamicValueType> listItems = new ArrayList<>();
        if (respondentCollection != null) {
            for (RespondentSumTypeItem respondentSumTypeItem : respondentCollection) {
                DynamicValueType dynamicValueType = new DynamicValueType();
                RespondentSumType respondentSumType = respondentSumTypeItem.getValue();
                dynamicValueType.setCode(respondentSumType.getRespondentName());
                dynamicValueType.setLabel(respondentSumType.getRespondentName() + " - "
                        + respondentSumType.getRespondentAddress().toString());
                listItems.add(dynamicValueType);
            }
        }
        return listItems;
    }

    public static CaseData midRespondentAddress(CaseData caseData) {
        List<DynamicValueType> listItems = createDynamicRespondentAddressFixedList(caseData.getRespondentCollection());
        if (!listItems.isEmpty()) {
            if (caseData.getClaimantWorkAddressQRespondent() != null) {
                caseData.getClaimantWorkAddressQRespondent().setListItems(listItems);
            } else {
                DynamicFixedListType dynamicFixedListType = new DynamicFixedListType();
                dynamicFixedListType.setListItems(listItems);
                caseData.setClaimantWorkAddressQRespondent(dynamicFixedListType);
            }
            //Default dynamic list
            caseData.getClaimantWorkAddressQRespondent().setValue(listItems.get(0));
        }
        return caseData;
    }

    public static List<RespondentSumTypeItem> getActiveRespondents(CaseData caseData) {

        List<RespondentSumTypeItem> activeRespondents = new ArrayList<>();

        if (caseData.getRespondentCollection() != null && !caseData.getRespondentCollection().isEmpty()) {
            activeRespondents = caseData.getRespondentCollection()
                    .stream()
                    .filter(respondentSumTypeItem -> respondentSumTypeItem.getValue().getResponseStruckOut() == null
                            || respondentSumTypeItem.getValue().getResponseStruckOut().equals(NO))
                    .toList();

            if (caseData.getRespondentCollection().size() == 1
                    && YES.equals(caseData.getRespondentCollection().get(0).getValue().getResponseStruckOut())
                    && YES.equals(caseData.getRespondentCollection().get(0).getValue().getResponseReceived())
                    && !isNullOrEmpty(caseData.getRespondentCollection().get(0)
                    .getValue().getResponseReceivedDate())) {
                return caseData.getRespondentCollection();
            }
        }
        return activeRespondents;
    }

    public static List<RespondentSumTypeItem> getActiveRespondentsLabels(LabelPayloadES labelPayloadES) {

        List<RespondentSumTypeItem> activeRespondents = new ArrayList<>();

        if (labelPayloadES.getRespondentCollection() != null && !labelPayloadES.getRespondentCollection().isEmpty()) {
            activeRespondents = labelPayloadES.getRespondentCollection()
                    .stream()
                    .filter(respondentSumTypeItem -> respondentSumTypeItem.getValue().getResponseStruckOut() == null
                            || respondentSumTypeItem.getValue().getResponseStruckOut().equals(NO))
                    .toList();
        }

        return activeRespondents;
    }

    public static String getDocumentName(CorrespondenceType correspondenceType,
                                         CorrespondenceScotType correspondenceScotType) {
        String ewSection = DocumentHelper.getEWSectionName(correspondenceType);
        String sectionName = ewSection.isEmpty()
                ? DocumentHelper.getScotSectionName(correspondenceScotType) : ewSection;
        return DocumentHelper.getTemplateName(correspondenceType, correspondenceScotType) + "_" + sectionName;
    }

    private static List<DynamicValueType> createDynamicRespondentNameList(
            List<RespondentSumTypeItem> respondentCollection) {
        List<DynamicValueType> listItems = new ArrayList<>();
        if (respondentCollection != null) {
            for (RespondentSumTypeItem respondentSumTypeItem : respondentCollection) {
                RespondentSumType respondentSumType = respondentSumTypeItem.getValue();
                if (respondentSumType.getResponseStruckOut() == null
                        || respondentSumType.getResponseStruckOut().equals(NO)) {
                    DynamicValueType dynamicValueType = new DynamicValueType();
                    dynamicValueType.setCode(respondentSumType.getRespondentName());
                    dynamicValueType.setLabel(respondentSumType.getRespondentName());
                    listItems.add(dynamicValueType);
                }
            }
        }
        return listItems;
    }

    public static void midRespondentECC(CaseData caseData, CaseData originalCaseData) {
        List<DynamicValueType> listItems = createDynamicRespondentNameList(originalCaseData.getRespondentCollection());
        if (!listItems.isEmpty()) {
            if (caseData.getRespondentECC() != null) {
                caseData.getRespondentECC().setListItems(listItems);
            } else {
                DynamicFixedListType dynamicFixedListType = new DynamicFixedListType();
                dynamicFixedListType.setListItems(listItems);
                caseData.setRespondentECC(dynamicFixedListType);
            }
            //Default dynamic list
            caseData.getRespondentECC().setValue(listItems.get(0));
        }
    }

    public static List<DynamicValueType> getDefaultBfListItems() {
        return new ArrayList<>(Arrays.asList(
                DynamicListHelper.getDynamicValue(BF_ACTION_ACAS),
                DynamicListHelper.getDynamicValue(BF_ACTION_CASE_LISTED),
                DynamicListHelper.getDynamicValue(BF_ACTION_CASE_PAPERS),
                DynamicListHelper.getDynamicValue(BF_ACTION_CASE_TRANSFERRED),
                DynamicListHelper.getDynamicValue(BF_ACTION_DRAFT),
                DynamicListHelper.getDynamicValue(BF_ACTION_ENQUIRY_ISSUED),
                DynamicListHelper.getDynamicValue(BF_ACTION_ENQUIRY_RECEIVED),
                DynamicListHelper.getDynamicValue(BF_ACTION_EXHIBITS),
                DynamicListHelper.getDynamicValue(BF_ACTION_INTERLOCUTORY),
                DynamicListHelper.getDynamicValue(BF_ACTION_IT3_RECEIVED),
                DynamicListHelper.getDynamicValue(BF_ACTION_OTHER_ACTION),
                DynamicListHelper.getDynamicValue(BF_ACTION_POSTPONEMENT_REQUESTED),
                DynamicListHelper.getDynamicValue(BF_ACTION_REFER_CHAIRMAN),
                DynamicListHelper.getDynamicValue(BF_ACTION_REPLY_TO_ENQUIRY),
                DynamicListHelper.getDynamicValue(BF_ACTION_STRIKING_OUT_WARNING)));
    }

    public static void updatePositionTypeToClosed(CaseData caseData) {
        caseData.setPositionType(CASE_CLOSED_POSITION);
        caseData.setCurrentPosition(CASE_CLOSED_POSITION);
    }

    public static void updatePostponedDate(CaseData caseData) {

        if (caseData.getHearingCollection() != null) {
            for (HearingTypeItem hearingTypeItem : caseData.getHearingCollection()) {

                if (hearingTypeItem.getValue() != null
                    && hearingTypeItem.getValue().getHearingDateCollection() != null) {
                    for (DateListedTypeItem dateListedTypeItem
                            : hearingTypeItem.getValue().getHearingDateCollection()) {

                        DateListedType dateListedType = dateListedTypeItem.getValue();
                        if (dateListedType != null) {
                            if (isHearingStatusPostponed(dateListedType)
                                && dateListedType.getPostponedDate() == null) {
                                dateListedType.setPostponedDate(UtilHelper.formatCurrentDate2(LocalDate.now()));
                            }
                            if (dateListedType.getPostponedDate() != null
                                && (!isHearingStatusPostponed(dateListedType)
                                    || dateListedType.getHearingStatus() == null)) {
                                dateListedType.setPostponedDate(null);
                            }
                        }
                    }
                }
            }
        }

    }

    private static boolean isHearingStatusPostponed(DateListedType dateListedType) {
        return dateListedType.getHearingStatus() != null
                && HEARING_STATUS_POSTPONED.equals(dateListedType.getHearingStatus());
    }

    public static List<String> getJurCodesCollection(List<JurCodesTypeItem> jurCodesCollection) {

        return jurCodesCollection != null
                ? jurCodesCollection.stream()
                .map(jurCodesTypeItem -> jurCodesTypeItem.getValue().getJuridictionCodesList())
                .toList()
                : new ArrayList<>();
    }

    /**
     * Creates an object of targetClassType that contains the properties with common names from the sourceObject passed.
     * @param sourceObject The object to copy values from
     * @param targetClassType The new object type
     * @return A new object that has a subset of data from the source object dependent on the class passed
     */
    public static Object intersectProperties(Object sourceObject, Class<?> targetClassType) {
        return OBJECT_MAPPER.convertValue(sourceObject, targetClassType);
    }

    /**
     * Gives current date in string format.
     * @return current date in "dd MMM yyy" format
     */
    public static String getCurrentDate() {
        return new SimpleDateFormat(MONTH_STRING_DATE_FORMAT).format(new Date());
    }

    /**
     * Gives current datetime in string format.
     * @return current datetime in "yyyy-MM-dd'T'HH:mm:ss.SSS" format
     */
    public static String getCurrentDateTime() {
        return LocalDateTime.now().format(OLD_DATE_TIME_PATTERN);
    }

    public static String getRespondentNames(CaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getRespondentCollection())) {
            return "";
        }
        return caseData.getRespondentCollection().stream()
            .filter(o -> o.getValue() != null && o.getValue().getRespondentName() != null)
            .map(o -> o.getValue().getRespondentName())
            .collect(Collectors.joining(", "));
    }

    public static Matcher getDocumentMatcher(String url) {
        Pattern pattern = Pattern.compile("^.+?/documents/");
        return pattern.matcher(url);
    }

    /**
     * Generates and returns a link for an UploadedDocumentType.
     * Creates an HTML anchor element link to an uploaded document which will open in a new tab.
     * @return a string anchor tag linking to the document
     */
    public static String createLinkForUploadedDocument(UploadedDocumentType document) {
        if (document == null) {
            return "";
        }
        Matcher matcher = Helper.getDocumentMatcher(document.getDocumentBinaryUrl());
        String documentLink = matcher.replaceFirst("");
        String documentName = document.getDocumentFilename();
        return String.format("<a href=\"/documents/%s\" target=\"_blank\">%s</a>", documentLink, documentName);
    }

    public static String extractLink(String input) {
        Pattern pattern = Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}/binary");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            return matcher.group();
        }
        return null; // No UUID found
    }

    /**
     * Validate if the other party (Claimant/Citizen) is a system user.
     * Non system user Claimant refers to the cases that have been transferred from legacy ECM or a paper based claim
     * which a caseworker would manually create in ExUI.
     * @param caseData in which the case details are extracted from
     * @return errors Error message
     */
    public static boolean isClaimantNonSystemUser(CaseData caseData) {
        if (caseData != null) {
            // TODO rework this logic when working on Claimant Gaps
            return (caseData.getEt1OnlineSubmission() == null && caseData.getHubLinksStatuses() == null)
                    || YES.equals(defaultIfNull(caseData.getMigratedFromEcm(), NO));
        }
        return true;
    }

    /**
     * Checks if the respondent is a non-system user.
     * A non-system user respondent refers to the cases that have been
     * transferred from legacy ECM or a paper based claim
     * which a caseworker would manually create in ExUI.
     *
     * @param caseData The case data to check for respondent user type.
     * @return true if the respondent is a non-system user, false otherwise.
     */
    public static boolean isRespondentSystemUser(CaseData caseData) {
        if (caseData != null) {
            List<RepresentedTypeRItem> repCollection = caseData.getRepCollection();
            return !CollectionUtils.isEmpty(repCollection)
                    && repCollection.stream().anyMatch(rep -> YES.equals(rep.getValue().getMyHmctsYesNo()));
        }
        return true;
    }

    /**
     * Gets the last item in a list.
     * @param <T> the type of the elements in the list
     * @param list the list to get the last item from
     * @return the last element in the list
     */
    public static <T> T getLast(List<T> list) {
        return CollectionUtils.isEmpty(list) ? null : list.get(list.size() - 1);
    }

    /**
     * Return the first item in a list or null if the list is empty.
     * @param list the list to get the first item from
     * @return the first item in the list or null if the list is empty
     */
    public static String getFirstListItem(List<String> list) {
        return CollectionUtils.isEmpty(list) ? null : list.get(0);
    }

    public static boolean isRepresentedClaimantWithMyHmctsCase(CaseData caseData) {
        return MY_HMCTS.equals(caseData.getCaseSource())
               && YES.equals(caseData.getClaimantRepresentedQuestion())
               && ObjectUtils.isNotEmpty(caseData.getRepresentativeClaimantType())
               && ObjectUtils.isNotEmpty(caseData.getRepresentativeClaimantType().getMyHmctsOrganisation());
    }

    /**
     * Removes leading and trailing spaces from party names.
     * @param caseData the case data to remove spaces from
     */
    public static void removeSpacesFromPartyNames(CaseData caseData) {
        removeSpacesFromClaimant(caseData);
        removeSpacesFromClaimantRepresentative(caseData);
        removeSpacesFromRespondent(caseData);
        removeSpacesFromRespondentRepresentative(caseData);
    }

    private static void removeSpacesFromRespondentRepresentative(CaseData caseData) {
        if (ObjectUtils.isNotEmpty(caseData.getRepCollection())) {
            caseData.getRepCollection().stream()
                    .filter(ObjectUtils::isNotEmpty)
                    .map(RepresentedTypeRItem::getValue)
                    .filter(r -> ObjectUtils.isNotEmpty(r) && !isNullOrEmpty(r.getNameOfRepresentative()))
                    .forEach(r -> r.setNameOfRepresentative(r.getNameOfRepresentative().trim()));
        }
    }

    private static void removeSpacesFromRespondent(CaseData caseData) {
        if (CollectionUtils.isNotEmpty(caseData.getRespondentCollection())) {
            caseData.getRespondentCollection().stream()
                    .filter(ObjectUtils::isNotEmpty)
                    .map(RespondentSumTypeItem::getValue)
                    .filter(r -> ObjectUtils.isNotEmpty(r) && !isNullOrEmpty(r.getRespondentName()))
                    .forEach(r -> r.setRespondentName(r.getRespondentName().trim()));
        }
    }

    private static void removeSpacesFromClaimantRepresentative(CaseData caseData) {
        if (ObjectUtils.isNotEmpty(caseData.getRepresentativeClaimantType())
            && !isNullOrEmpty(caseData.getRepresentativeClaimantType().getNameOfRepresentative())) {
            caseData.getRepresentativeClaimantType().setNameOfRepresentative(
                    caseData.getRepresentativeClaimantType().getNameOfRepresentative().trim());
        }
    }

    private static void removeSpacesFromClaimant(CaseData caseData) {
        if (COMPANY.equals(caseData.getClaimantTypeOfClaimant()) && !isNullOrEmpty(caseData.getClaimantCompany())) {
            caseData.setClaimantCompany(caseData.getClaimantCompany().trim());
        } else if (INDIVIDUAL_TYPE_CLAIMANT.equals(caseData.getClaimantTypeOfClaimant())
                   && ObjectUtils.isNotEmpty(caseData.getClaimantIndType())
                   && !isNullOrEmpty(caseData.getClaimantIndType().getClaimantFirstNames())
                   && !isNullOrEmpty(caseData.getClaimantIndType().getClaimantLastName())) {
            caseData.getClaimantIndType().setClaimantFirstNames(
                    caseData.getClaimantIndType().getClaimantFirstNames().trim());
            caseData.getClaimantIndType().setClaimantLastName(
                    caseData.getClaimantIndType().getClaimantLastName().trim());
        }
    }

    public static boolean addressIsEmpty(Address address) {
        return address == null
                || isNullOrEmpty(address.getAddressLine1())
                && isNullOrEmpty(address.getAddressLine2())
                && isNullOrEmpty(address.getAddressLine3())
                && isNullOrEmpty(address.getPostTown())
                && isNullOrEmpty(address.getPostCode())
                && isNullOrEmpty(address.getCountry())
                && isNullOrEmpty(address.getCounty());
    }
}
