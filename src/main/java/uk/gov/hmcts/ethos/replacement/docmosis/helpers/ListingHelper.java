package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.DateListedType;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;
import uk.gov.hmcts.et.common.model.listing.ListingData;
import uk.gov.hmcts.et.common.model.listing.items.ListingTypeItem;
import uk.gov.hmcts.et.common.model.listing.types.ListingType;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.SPACE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.BROUGHT_FORWARD_REPORT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CASES_COMPLETED_REPORT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CASE_SOURCE_LOCAL_REPORT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMS_ACCEPTED_REPORT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMS_BY_HEARING_VENUE_REPORT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.FILE_EXTENSION;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARINGS_BY_HEARING_TYPE_REPORT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_DOC_ETCL;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_DOC_IT56;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_DOC_IT57;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_ETCL_PRESS_LIST;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_ETCL_PUBLIC;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_ETCL_STAFF;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.IT56_TEMPLATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.IT57_TEMPLATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.LIVE_CASELOAD_REPORT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MEMBER_DAYS_REPORT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NEW_DATE_PATTERN;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NEW_LINE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NEW_TIME_PATTERN;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.OLD_DATE_TIME_PATTERN;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.OLD_DATE_TIME_PATTERN2;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.OUTPUT_FILE_NAME;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.PRESS_LIST_CAUSE_LIST_RANGE_TEMPLATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.PRESS_LIST_CAUSE_LIST_SINGLE_TEMPLATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.PUBLIC_CASE_CAUSE_LIST_ROOM_TEMPLATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.PUBLIC_CASE_CAUSE_LIST_TEMPLATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RANGE_HEARING_DATE_TYPE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_LISTING_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SERVING_CLAIMS_REPORT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SESSION_DAYS_REPORT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.STAFF_CASE_CAUSE_LIST_ROOM_TEMPLATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.STAFF_CASE_CAUSE_LIST_TEMPLATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TIME_TO_FIRST_HEARING_REPORT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.DocumentHelper.getHearingDuration;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.DocumentHelper.getRespondentAddressET3;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.nullCheck;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultiplesScheduleHelper.NOT_ALLOCATED;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.Constants.CASES_AWAITING_JUDGMENT_REPORT;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.Constants.ECC_REPORT;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.Constants.HEARINGS_TO_JUDGEMENTS_REPORT;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.Constants.NO_CHANGE_IN_CURRENT_POSITION_REPORT;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.Constants.RESPONDENTS_REPORT;

@Slf4j
public final class ListingHelper {

    private static final String ROOM_NOT_ALLOCATED = "* Not Allocated";
    private static final String NO_DOCUMENT_FOUND = "No document found";
    private static final int NUMBER_CHAR_PARSING_DATE = 20;
    private static final String LISTING_NEWLINE = "\"listing\":[\n";
    private static final String ARRAY_ELEMENT_CLOSING_NEWLINE = "}],\n";
    static final List<String> REPORTS = Arrays.asList(BROUGHT_FORWARD_REPORT, CLAIMS_ACCEPTED_REPORT,
        LIVE_CASELOAD_REPORT, CASES_COMPLETED_REPORT, CASES_AWAITING_JUDGMENT_REPORT, TIME_TO_FIRST_HEARING_REPORT,
        SERVING_CLAIMS_REPORT, CASE_SOURCE_LOCAL_REPORT, HEARINGS_TO_JUDGEMENTS_REPORT,
            HEARINGS_BY_HEARING_TYPE_REPORT, NO_CHANGE_IN_CURRENT_POSITION_REPORT,
            MEMBER_DAYS_REPORT, RESPONDENTS_REPORT, SESSION_DAYS_REPORT, ECC_REPORT, CLAIMS_BY_HEARING_VENUE_REPORT);
    private static final List<String> SCOTLAND_HEARING_LIST = List.of("Reading Day", "Deliberation Day",
            "Members meeting", "In Chambers");
    public static final DateTimeFormatter CAUSE_LIST_DATE_TIME_PATTERN = DateTimeFormatter.ofPattern("dd MMMM yyyy");
    public static final String RULE_49_APPLIES = "Order made pursuant to Rule 49";
    //hearing venue name label for Newcastle
    public static final String NEWCASTLE_CFT = "Newcastle CFT";
    public static final String NEWCASTLE_CFCTC = "Newcastle CFCTC";
    public static final String TEESSIDE_MAGS = "Teesside Mags";
    public static final String TEESSIDE_JUSTICE_CENTRE = "Teesside Justice Centre";

    private ListingHelper() {
    }

    public static ListingType getListingTypeFromCaseData(ListingData listingData, CaseData caseData,
                                                         HearingType hearingType, DateListedType dateListedType,
                                                         int index, int hearingCollectionSize) {
        ListingType listingType = new ListingType();

        try {
            log.info("started getListingTypeFromCaseData");
            listingType.setElmoCaseReference(caseData.getEthosCaseReference());
            String listedDate = dateListedType.getListedDate();
            listingType.setCauseListDate(getCauseListDate(listedDate));
            listingType.setCauseListTime(getCauseListTime(listedDate));

            listingType.setJurisdictionCodesList(BulkHelper.getJurCodesCollectionWithHide(
                    caseData.getJurCodesCollection()));
            listingType.setHearingType(getHearingType(hearingType));
            listingType.setPositionType(getPositionType(caseData));

            listingType.setHearingJudgeName(hearingType.hasHearingJudge()
                    ? hearingType.getJudge().getSelectedLabel()
                    : SPACE);
            listingType.setAdditionalJudge(hearingType.hasAdditionalHearingJudge()
                    ? hearingType.getAdditionalJudge().getSelectedLabel()
                    : SPACE);
            listingType.setHearingEEMember(hearingType.hasHearingEmployeeMember()
                    ? hearingType.getHearingEEMember().getSelectedLabel()
                    : SPACE);
            listingType.setHearingERMember(hearingType.hasHearingEmployerMember()
                    ? hearingType.getHearingERMember().getSelectedLabel()
                    : SPACE);
            listingType.setHearingClerk(dateListedType.getHearingClerk() != null
                    ? dateListedType.getHearingClerk().getSelectedLabel()
                    : SPACE);
            listingType.setHearingPanel(
                    isNullOrEmpty(hearingType.getHearingSitAlone()) ? SPACE : hearingType.getHearingSitAlone());
            listingType.setHearingFormat(isNotEmpty(hearingType.getHearingFormat())
                    ? String.join(", ", hearingType.getHearingFormat())
                    : SPACE);
            listingType.setJudicialMediation(
                    isNullOrEmpty(hearingType.getJudicialMediation()) || NO.equals(hearingType.getJudicialMediation())
                    ? SPACE
                    : hearingType.getJudicialMediation());
            
            listingType.setCauseListVenue(getVenueFromDateListedType(dateListedType));
            
            listingType.setHearingRoom(getHearingRoom(dateListedType));
            
            listingType.setHearingNotes(
                    isNullOrEmpty(hearingType.getHearingNotes()) ? SPACE : hearingType.getHearingNotes());
            
            listingType.setHearingDay(index + 1 + " of " + hearingCollectionSize);
            listingType.setEstHearingLength(isNullOrEmpty(getHearingDuration(hearingType)) ? SPACE
                    : getHearingDuration(hearingType));

            listingType.setHearingReadingDeliberationMembersChambers(isNullOrEmpty(
                    dateListedType.getHearingTypeReadingDeliberation()) || !SCOTLAND_HEARING_LIST.contains(
                    dateListedType.getHearingTypeReadingDeliberation()) ? SPACE :
                    dateListedType.getHearingTypeReadingDeliberation());

            log.info("End getListingTypeFromCaseData");
            return getClaimantRespondentDetails(listingType, listingData, caseData);

        } catch (Exception ex) {
            log.error("ListingData: " + listingData);
            log.error("CaseData: " + caseData);
            log.error("HearingType: " + hearingType);
            log.error("DateListedType: " + dateListedType);
            log.error("index: " + index);
            log.error("hearingCollectionSize: " + hearingCollectionSize);
            return listingType;
        }
    }

    private static String getPositionType(CaseData caseData) {
        return isNullOrEmpty(caseData.getPositionType()) ? SPACE : caseData.getPositionType();
    }

    private static String getHearingType(HearingType hearingType) {
        return isNullOrEmpty(hearingType.getHearingType()) ? SPACE :
                hearingType.getHearingType();
    }

    private static String getCauseListTime(String listedDate) {
        return isNullOrEmpty(listedDate) ? SPACE : UtilHelper.formatLocalTime(listedDate);
    }

    private static String getCauseListDate(String listedDate) {
        return isNullOrEmpty(listedDate) ? SPACE :
                LocalDate.parse(listedDate, OLD_DATE_TIME_PATTERN).format(CAUSE_LIST_DATE_TIME_PATTERN);
    }

    private static String getHearingRoom(DateListedType dateListedType) {
        for (Method m: dateListedType.getClass().getDeclaredMethods()) {
            if (m.getName().startsWith("getHearingRoom")) {
                try {
                    DynamicFixedListType room = (DynamicFixedListType)m.invoke(dateListedType);
                    if (room != null) {
                        return room.getValue().getCode();
                    }
                } catch (IllegalAccessException | InvocationTargetException e) {
                    log.error("Error getting hearing room:", e);
                }
            }
        }
        return SPACE;
    }

    private static ListingType getClaimantRespondentDetails(ListingType listingType, ListingData listingData,
                                                            CaseData caseData) {
        listingType.setClaimantTown(SPACE);
        listingType.setRespondentTown(SPACE);
        listingType.setRespondentOthers(SPACE);
        listingType.setClaimantRepresentative(SPACE);
        listingType.setRespondentRepresentative(SPACE);

        boolean isPublicType = isPublicType(listingData);
        boolean isPressListType = isPressListType(listingData);
        boolean rule49d = isRule49d(caseData);
        boolean rule49b = isRule49b(caseData);
        if (rule49b && isPublicType || rule49d && isPublicType) {
            listingType.setClaimantName(SPACE);
            listingType.setRespondent(SPACE);
        } else if (rule49b && isPressListType || rule49d && isPressListType) {
            listingType.setClaimantName(RULE_49_APPLIES);
            listingType.setRespondent(RULE_49_APPLIES);
        } else {
            if (isNullOrEmpty(caseData.getClaimantCompany())) {
                listingType.setClaimantName(getClaimantName(caseData));
            } else {
                listingType.setClaimantName(caseData.getClaimantCompany());
            }
            listingType.setClaimantTown(getClaimantTown(caseData));
            listingType.setRespondent(getRespondent(caseData));
            listingType.setRespondentTown(getRespondentTown(caseData));
            listingType.setRespondentOthers(
                    isNullOrEmpty(getRespOthersName(caseData)) ? SPACE : getRespOthersName(caseData));
            listingType.setClaimantRepresentative(getClaimantRepresentative(caseData));
            listingType.setRespondentRepresentative(getRespondentRepresentative(caseData));
        }
        return listingType;
    }

    private static String getClaimantRepresentative(CaseData caseData) {
        return caseData.getRepresentativeClaimantType() != null
                && caseData.getRepresentativeClaimantType().getNameOfOrganisation() != null
                ? caseData.getRepresentativeClaimantType().getNameOfOrganisation()
                : SPACE;
    }

    private static String getRespondentRepresentative(CaseData caseData) {
        return isNotEmpty(caseData.getRepCollection())
                && caseData.getRepCollection().get(0).getValue() != null
                && caseData.getRepCollection().get(0).getValue().getNameOfOrganisation() != null
                ? caseData.getRepCollection().get(0).getValue().getNameOfOrganisation()
                : SPACE;
    }

    private static String getRespondentTown(CaseData caseData) {
        return isNotEmpty(caseData.getRespondentCollection())
            && caseData.getRespondentCollection().get(0).getValue() != null
            && getRespondentAddressET3(caseData.getRespondentCollection().get(0).getValue()) != null
            && getRespondentAddressET3(caseData.getRespondentCollection().get(0).getValue()).getPostTown() != null
            ? getRespondentAddressET3(caseData.getRespondentCollection().get(0).getValue()).getPostTown()
            : SPACE;
    }

    private static String getRespondent(CaseData caseData) {
        return isNotEmpty(caseData.getRespondentCollection())
                && caseData.getRespondentCollection().get(0).getValue() != null
                ? caseData.getRespondentCollection().get(0).getValue().getRespondentName()
                : SPACE;
    }

    private static String getClaimantTown(CaseData caseData) {
        return caseData.getClaimantType() != null
                && caseData.getClaimantType().getClaimantAddressUK() != null
                && caseData.getClaimantType().getClaimantAddressUK().getPostTown() != null
                ? caseData.getClaimantType().getClaimantAddressUK().getPostTown()
                : SPACE;
    }

    private static String getClaimantName(CaseData caseData) {
        return caseData.getClaimantIndType() != null
                && caseData.getClaimantIndType().getClaimantLastName() != null
                ? caseData.getClaimantIndType().claimantFullName()
                : SPACE;
    }

    private static boolean isRule49b(CaseData caseData) {
        // Rule 49 was previously Rule 50
        return caseData.getRestrictedReporting() != null
                && caseData.getRestrictedReporting().getRule503b() != null
                && caseData.getRestrictedReporting().getRule503b().equals(YES);
    }

    private static boolean isRule49d(CaseData caseData) {
        // Rule 49 was previously Rule 50
        return caseData.getRestrictedReporting() != null
                && caseData.getRestrictedReporting().getImposed() != null
                && caseData.getRestrictedReporting().getImposed().equals(YES);
    }

    private static boolean isPressListType(ListingData listingData) {
        return listingData.getHearingDocType() != null
                && listingData.getHearingDocType().equals(HEARING_DOC_ETCL)
                && listingData.getHearingDocETCL().equals(HEARING_ETCL_PRESS_LIST);
    }

    private static boolean isPublicType(ListingData listingData) {
        return listingData.getHearingDocType() != null
                && listingData.getHearingDocType().equals(HEARING_DOC_ETCL)
                && listingData.getHearingDocETCL().equals(HEARING_ETCL_PUBLIC);
    }

    public static StringBuilder buildListingDocumentContent(ListingData listingData, String accessKey,
                                                            String templateName, UserDetails userDetails,
                                                            String caseType) {
        StringBuilder sb = new StringBuilder(150);

        // Start building the instruction
        sb.append("{\n\"accessKey\":\"").append(accessKey).append(NEW_LINE)
                .append("\"templateName\":\"")
                .append(templateName).append(FILE_EXTENSION).append(NEW_LINE).append("\"outputName\":\"")
                .append(OUTPUT_FILE_NAME).append(NEW_LINE).append("\"data\":{\n")
                .append(getCourtListingData(listingData)).append(getLogo(caseType))
                .append("\"Office_name\":\"")
                .append(listingData.getManagingOffice()).append(NEW_LINE).append("\"Hearing_location\":\"")
                .append(ListingVenueHelper.getListingVenueLabel(listingData)).append(NEW_LINE)
                .append(getListingDate(listingData));

        String userName = nullCheck(userDetails.getFirstName() + SPACE + userDetails.getLastName());
        sb.append("\"Clerk\":\"").append(nullCheck(userName)).append(NEW_LINE);

        if (listingData.getListingCollection() != null && !listingData.getListingCollection().isEmpty()) {
            sortListingCollection(listingData, templateName);
            sb.append(getDocumentData(listingData, templateName, caseType));
        }

        sb.append("\"Today_date\":\"").append(UtilHelper.formatCurrentDate(LocalDate.now())).append("\"\n}\n}\n");
        return sb;
    }

    private static StringBuilder getLogo(String caseType) {
        StringBuilder sb = new StringBuilder();
        if (caseType.equals(SCOTLAND_LISTING_CASE_TYPE_ID)) {
            sb.append("\"listing_logo\":\"[userImage:schmcts.png]").append(NEW_LINE);
        } else {
            sb.append("\"listing_logo\":\"[userImage:enhmcts.png]").append(NEW_LINE);
        }
        return sb;
    }

    private static StringBuilder getDocumentData(ListingData listingData, String templateName, String caseType) {
        if (Arrays.asList(IT56_TEMPLATE, IT57_TEMPLATE)
                .contains(templateName)) {
            return getCaseCauseList(listingData, caseType);
        } else if (Arrays.asList(PUBLIC_CASE_CAUSE_LIST_TEMPLATE, STAFF_CASE_CAUSE_LIST_TEMPLATE)
                .contains(templateName)) {
            return getCaseCauseListByDate(listingData, caseType);
        } else if (Arrays.asList(PRESS_LIST_CAUSE_LIST_RANGE_TEMPLATE, PRESS_LIST_CAUSE_LIST_SINGLE_TEMPLATE)
                .contains(templateName)) {
            return getListByRoomOrVenue(new ArrayList<>(), listingData, caseType, false);
        } else if (Arrays.asList(PUBLIC_CASE_CAUSE_LIST_ROOM_TEMPLATE, STAFF_CASE_CAUSE_LIST_ROOM_TEMPLATE)
                .contains(templateName)) {
            return getCaseCauseListByRoom(listingData, caseType);
        } else {
            return new StringBuilder();
        }
    }

    private static boolean isEmptyHearingDate(ListingType listingType) {
        if (listingType.getCauseListDate() != null) {
            return isNullOrEmpty(listingType.getCauseListDate());
        }
        return true;
    }

    private static TreeMap<String, List<ListingTypeItem>> getListHearingsByDate(ListingData listingData) {
        return listingData.getListingCollection()
                .stream()
                .filter(listingTypeItem -> !isEmptyHearingDate(listingTypeItem.getValue()))
                .collect(Collectors.groupingBy(listingTypeItem -> listingTypeItem.getValue().getCauseListDate(),
                    () -> new TreeMap<>(getDateComparator()), toList()));
    }

    private static Iterator<Map.Entry<String, List<ListingTypeItem>>> getEntriesByDate(StringBuilder sb,
                                                                                       ListingData listingData) {
        TreeMap<String, List<ListingTypeItem>> sortedMap = getListHearingsByDate(listingData);
        sb.append("\"listing_date\":[\n");
        return new TreeMap<>(sortedMap).entrySet().iterator();
    }

    private static StringBuilder getCaseCauseListByDate(ListingData listingData, String caseType) {
        StringBuilder sb = new StringBuilder(40);
        Iterator<Map.Entry<String, List<ListingTypeItem>>> entries = getEntriesByDate(sb, listingData);
        while (entries.hasNext()) {
            Map.Entry<String, List<ListingTypeItem>> listingEntry = entries.next();
            sb.append("{\"date\":\"").append(listingEntry.getKey()).append(NEW_LINE);
            sb.append("\"case_total\":\"").append(listingEntry.getValue().size()).append(NEW_LINE);
            sb.append(LISTING_NEWLINE);
            setListingEntry(listingData, caseType, sb, entries, listingEntry);
        }
        return sb;
    }

    private static StringBuilder getCaseCauseListByRoom(ListingData listingData, String caseType) {
        StringBuilder sb = new StringBuilder();
        Iterator<Map.Entry<String, List<ListingTypeItem>>> entries = getEntriesByDate(sb, listingData);
        while (entries.hasNext()) {
            Map.Entry<String, List<ListingTypeItem>> listingEntry = entries.next();
            sb.append("{\"date\":\"").append(listingEntry.getKey()).append(NEW_LINE);
            sb.append(getListByRoomOrVenue(listingEntry.getValue(), listingData, caseType, true));
            if (entries.hasNext()) {
                sb.append("},\n");
            } else {
                sb.append(ARRAY_ELEMENT_CLOSING_NEWLINE);
            }
        }
        return sb;
    }

    public static StringBuilder getListingDate(ListingData listingData) {
        StringBuilder sb = new StringBuilder();
        if (RANGE_HEARING_DATE_TYPE.equals(listingData.getHearingDateType())) {
            sb.append("\"Listed_date_from\":\"")
                    .append(UtilHelper.listingFormatLocalDate(listingData.getListingDateFrom())).append(NEW_LINE)
                    .append("\"Listed_date_to\":\"")
                    .append(UtilHelper.listingFormatLocalDate(listingData.getListingDateTo())).append(NEW_LINE);
        } else {
            sb.append("\"Listed_date\":\"")
                    .append(UtilHelper.listingFormatLocalDate(listingData.getListingDate())).append(NEW_LINE);
        }
        return sb;
    }

    private static boolean isEmptyHearingRoom(ListingType listingType) {
        if (listingType.getHearingRoom() != null) {
            return isNullOrEmpty(listingType.getHearingRoom());
        }
        return true;
    }

    private static TreeMap<String, List<ListingTypeItem>> getListHearingsByRoomWithNotAllocated(
            List<ListingTypeItem> listingSubCollection) {
        TreeMap<String, List<ListingTypeItem>> sortedMap = listingSubCollection
                .stream()
                .filter(listingTypeItem -> !isEmptyHearingRoom(listingTypeItem.getValue()))
                .collect(Collectors.groupingBy(listingTypeItem -> listingTypeItem.getValue().getHearingRoom(),
                    () -> new TreeMap<>(getVenueComparator()), toList()));
        List<ListingTypeItem> notAllocated = listingSubCollection
                .stream()
                .filter(listingTypeItem -> isEmptyHearingRoom(listingTypeItem.getValue()))
                .sorted(getVenueComparatorListingTypeItem())
                .toList();
        if (!notAllocated.isEmpty()) {
            sortedMap.computeIfAbsent(ROOM_NOT_ALLOCATED, k -> new ArrayList<>()).addAll(notAllocated);
        }
        return sortedMap;
    }

    private static boolean isEmptyHearingVenue(ListingType listingType) {
        if (listingType.getCauseListVenue() != null) {
            return isNullOrEmpty(listingType.getCauseListVenue());
        }
        return true;
    }

    private static TreeMap<String, List<ListingTypeItem>> getListHearingsByVenueWithNotAllocated(
            ListingData listingData) {
        TreeMap<String, List<ListingTypeItem>> sortedMap = listingData.getListingCollection()
                .stream()
                .filter(listingTypeItem -> !isEmptyHearingVenue(listingTypeItem.getValue()))
                .collect(Collectors.groupingBy(listingTypeItem -> listingTypeItem.getValue().getCauseListVenue(),
                    () -> new TreeMap<>(getVenueComparator()), toList()));
        List<ListingTypeItem> notAllocated = listingData.getListingCollection()
                .stream()
                .filter(listingTypeItem -> isEmptyHearingVenue(listingTypeItem.getValue()))
                .sorted(getDateComparatorListingTypeItem().thenComparing(getTimeComparatorListingTypeItem()))
                .toList();
        if (isNotEmpty(notAllocated)) {
            sortedMap.computeIfAbsent(NOT_ALLOCATED, k -> new ArrayList<>()).addAll(notAllocated);
        }
        return sortedMap;
    }

    private static StringBuilder getListByRoomOrVenue(List<ListingTypeItem> collection, ListingData listingData,
                                                      String caseType, boolean byRoom) {
        StringBuilder sb = new StringBuilder(30);
        TreeMap<String, List<ListingTypeItem>> sortedMap = byRoom
                ? getListHearingsByRoomWithNotAllocated(collection)
                : getListHearingsByVenueWithNotAllocated(listingData);
        sb.append("\"location\":[\n");
        Iterator<Map.Entry<String, List<ListingTypeItem>>> entries = new TreeMap<>(sortedMap).entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<String, List<ListingTypeItem>> listingEntry = entries.next();
            String hearingRoomOrVenue = byRoom ? "Hearing_room" : "Hearing_venue";
            sb.append("{\"").append(hearingRoomOrVenue).append("\":\"").append(listingEntry.getKey()).append(NEW_LINE)
                    .append(LISTING_NEWLINE);
            setListingEntry(listingData, caseType, sb, entries, listingEntry);
        }
        return sb;
    }

    private static void setListingEntry(ListingData listingData, String caseType, StringBuilder sb,
                                        Iterator<Map.Entry<String, List<ListingTypeItem>>> entries,
                                        Map.Entry<String, List<ListingTypeItem>> listingEntry) {
        for (int i = 0; i < listingEntry.getValue().size(); i++) {
            sb.append(getListingTypeRow(listingEntry.getValue().get(i).getValue(), caseType, listingData));
            if (i != listingEntry.getValue().size() - 1) {
                sb.append(",\n");
            }
        }
        sb.append("]\n");
        if (entries.hasNext()) {
            sb.append("},\n");
        } else {
            sb.append(ARRAY_ELEMENT_CLOSING_NEWLINE);
        }
    }

    private static StringBuilder getCaseCauseList(ListingData listingData, String caseType) {
        List<ListingTypeItem> listingTypeItems = listingData.getListingCollection();
        StringBuilder sb = new StringBuilder();
        sb.append(LISTING_NEWLINE);
        for (int i = 0; i < listingTypeItems.size(); i++) {
            sb.append(getListingTypeRow(listingTypeItems.get(i).getValue(), caseType, listingData));
            if (i != listingTypeItems.size() - 1) {
                sb.append(",\n");
            }
        }
        sb.append("],\n");
        return sb;
    }

    private static StringBuilder getListingTypeRow(ListingType listingType, String caseType, ListingData listingData) {
        StringBuilder sb = new StringBuilder(550);
        sb.append("{\"Judge\":\"").append(nullCheck(extractHearingJudgeName(listingType)))
                .append(NEW_LINE)
                .append(getCourtListingData(listingData)).append(getLogo(caseType)).append("\"ERMember\":\"")
                .append(nullCheck(listingType.getHearingERMember())).append(NEW_LINE).append("\"EEMember\":\"")
                .append(nullCheck(listingType.getHearingEEMember())).append(NEW_LINE).append("\"Case_No\":\"")
                .append(nullCheck(listingType.getElmoCaseReference())).append(NEW_LINE).append("\"Hearing_type\":\"")
                .append(nullCheck(listingType.getHearingType())).append(NEW_LINE).append("\"Jurisdictions\":\"")
                .append(nullCheck(listingType.getJurisdictionCodesList())).append(NEW_LINE)
                .append("\"Hearing_date\":\"").append(nullCheck(listingType.getCauseListDate())).append(NEW_LINE)
                .append("\"Hearing_date_time\":\"").append(nullCheck(listingType.getCauseListDate())).append(" at ")
                .append(nullCheck(listingType.getCauseListTime())).append(NEW_LINE).append("\"Hearing_time\":\"")
                .append(nullCheck(listingType.getCauseListTime())).append(NEW_LINE).append("\"Hearing_duration\":\"")
                .append(nullCheck(listingType.getEstHearingLength())).append(NEW_LINE).append("\"Hearing_clerk\":\"")
                .append(nullCheck(listingType.getHearingClerk())).append(NEW_LINE).append("\"Claimant\":\"")
                .append(nullCheck(listingType.getClaimantName())).append(NEW_LINE).append("\"claimant_town\":\"")
                .append(nullCheck(listingType.getClaimantTown())).append(NEW_LINE)
                .append("\"claimant_representative\":\"").append(nullCheck(listingType.getClaimantRepresentative()))
                .append(NEW_LINE).append("\"Respondent\":\"").append(nullCheck(listingType.getRespondent()))
                .append(NEW_LINE)
                .append("\"resp_others\":\"").append(nullCheck(getRespondentOthersWithLineBreaks(listingType)))
                .append(NEW_LINE).append("\"respondent_town\":\"").append(nullCheck(listingType.getRespondentTown()))
                .append(NEW_LINE).append("\"Hearing_location\":\"").append(nullCheck(listingType.getCauseListVenue()))
                .append(NEW_LINE).append("\"Hearing_room\":\"").append(nullCheck(listingType.getHearingRoom()))
                .append(NEW_LINE).append("\"Hearing_dayofdays\":\"").append(nullCheck(listingType.getHearingDay()))
                .append(NEW_LINE).append("\"Hearing_panel\":\"").append(nullCheck(listingType.getHearingPanel()))
                .append(NEW_LINE).append("\"Hearing_notes\":\"").append(nullCheck(extractHearingNotes(listingType)))
                .append(NEW_LINE).append("\"Judicial_mediation\":\"")
                .append(nullCheck(listingType.getJudicialMediation())).append(NEW_LINE)
                .append("\"Reading_deliberation_day\":\"")
                .append(nullCheck(listingType.getHearingReadingDeliberationMembersChambers())).append(NEW_LINE)
                .append("\"Hearing_format\":\"").append(nullCheck(listingType.getHearingFormat())).append(NEW_LINE)
                .append("\"respondent_representative\":\"").append(nullCheck(listingType.getRespondentRepresentative()))
                .append("\"}");
        return sb;
    }

    private static String extractHearingNotes(ListingType listingType) {
        if (!isNullOrEmpty(listingType.getHearingNotes())) {
            return listingType.getHearingNotes().replaceAll("\n", " - ");
        }
        return "";
    }

    private static String extractHearingJudgeName(ListingType listingType) {
        String judge = "";
        if (listingType.getHearingJudgeName() != null) {
            judge = listingType.getHearingJudgeName().substring(listingType.getHearingJudgeName().indexOf('_') + 1);
            if (listingType.getAdditionalJudge() != null) {
                return String.join(" & ", judge,
                        listingType.getAdditionalJudge().substring(listingType.getAdditionalJudge().indexOf('_') + 1));
            }
            return judge;
        }
        return judge;
    }

    public static String getRespondentOthersWithLineBreaks(ListingType listingType) {
        return nullCheck(listingType.getRespondentOthers()).replace(", ", "\\n");
    }

    private static StringBuilder getCourtListingData(ListingData listingData) {
        StringBuilder sb = new StringBuilder(80);
        if (listingData.getTribunalCorrespondenceAddress() != null) {
            sb.append("\"Court_addressLine1\":\"")
                    .append(nullCheck(listingData.getTribunalCorrespondenceAddress().getAddressLine1()))
                    .append(NEW_LINE).append("\"Court_addressLine2\":\"")
                    .append(nullCheck(listingData.getTribunalCorrespondenceAddress().getAddressLine2()))
                    .append(NEW_LINE).append("\"Court_addressLine3\":\"")
                    .append(nullCheck(listingData.getTribunalCorrespondenceAddress().getAddressLine3()))
                    .append(NEW_LINE).append("\"Court_town\":\"")
                    .append(nullCheck(listingData.getTribunalCorrespondenceAddress().getPostTown())).append(NEW_LINE)
                    .append("\"Court_county\":\"")
                    .append(nullCheck(listingData.getTribunalCorrespondenceAddress().getCounty())).append(NEW_LINE)
                    .append("\"Court_postCode\":\"")
                    .append(nullCheck(listingData.getTribunalCorrespondenceAddress().getPostCode())).append(NEW_LINE)
                    .append("\"Court_fullAddress\":\"")
                    .append(nullCheck(listingData.getTribunalCorrespondenceAddress().toString())).append(NEW_LINE);
        }
        sb.append("\"Court_telephone\":\"").append(nullCheck(listingData.getTribunalCorrespondenceTelephone()))
                .append(NEW_LINE).append("\"Court_fax\":\"")
                .append(nullCheck(listingData.getTribunalCorrespondenceFax())).append(NEW_LINE)
                .append("\"Court_DX\":\"").append(nullCheck(listingData.getTribunalCorrespondenceDX())).append(NEW_LINE)
                .append("\"Court_Email\":\"").append(nullCheck(listingData.getTribunalCorrespondenceEmail()))
                .append(NEW_LINE);
        return sb;
    }

    public static String getListingDocName(ListingData listingData) {
        if (listingData.getHearingDocType() != null) {
            return getHearingDocTemplateName(listingData);
        } else if (listingData.getReportType() != null) {
            return getReportDocTemplateName(listingData.getReportType());
        }
        return NO_DOCUMENT_FOUND;
    }

    public static String getVenueFromDateListedType(DateListedType dateListedType) {
        // EnglandWales
        if (dateListedType.hasHearingVenue()) {
            switch (dateListedType.getHearingVenueDay().getValue().getLabel()) {
                case NEWCASTLE_CFT -> {
                    return NEWCASTLE_CFCTC;
                }
                case TEESSIDE_MAGS -> {
                    return TEESSIDE_JUSTICE_CENTRE;
                }
                default -> {
                    return dateListedType.getHearingVenueDay().getValue().getLabel();
                }
            }

        }

        // Scotland
        String hearingVenueScotland = dateListedType.getHearingVenueDayScotland();
        final TribunalOffice tribunalOffice = TribunalOffice.valueOfOfficeName(hearingVenueScotland);
        switch (tribunalOffice) {
            case GLASGOW -> {
                if (dateListedType.hasHearingGlasgow()) {
                    return dateListedType.getHearingGlasgow().getSelectedLabel();
                }
            }
            case ABERDEEN -> {
                if (dateListedType.hasHearingAberdeen()) {
                    return dateListedType.getHearingAberdeen().getSelectedLabel();
                }
            }
            case DUNDEE -> {
                if (dateListedType.hasHearingDundee()) {
                    return dateListedType.getHearingDundee().getSelectedLabel();
                }
            }
            case EDINBURGH -> {
                if (dateListedType.hasHearingEdinburgh()) {
                    return dateListedType.getHearingEdinburgh().getSelectedLabel();
                }
            }
            default -> {
                return SPACE;
            }
        }

        return SPACE;
    }

    /**
     * Returns Venue code from DateListedType's hearingVenue dynamic fixed list.
     * This returns the dynamicFixedListCodes for the hearingVenues to be compared
     * with the ones which needs to be in report.
     * @param dateListedType Hearing date listed type to get the hearing venue
     */
    public static String getVenueCodeFromDateListedType(DateListedType dateListedType) {
        // EnglandWales
        if (dateListedType.hasHearingVenue()) {
            return dateListedType.getHearingVenueDay().getValue().getCode();
        }

        // Scotland
        String hearingVenueScotland = dateListedType.getHearingVenueDayScotland();
        final TribunalOffice tribunalOffice = TribunalOffice.valueOfOfficeName(hearingVenueScotland);
        switch (tribunalOffice) {
            case GLASGOW:
                if (dateListedType.hasHearingGlasgow()) {
                    return dateListedType.getHearingGlasgow().getSelectedCode();
                }
                break;
            case ABERDEEN:
                if (dateListedType.hasHearingAberdeen()) {
                    return dateListedType.getHearingAberdeen().getSelectedCode();
                }
                break;
            case DUNDEE:
                if (dateListedType.hasHearingDundee()) {
                    return dateListedType.getHearingDundee().getSelectedCode();
                }
                break;
            case EDINBURGH:
                if (dateListedType.hasHearingEdinburgh()) {
                    return dateListedType.getHearingEdinburgh().getSelectedCode();
                }
                break;
            default:
                break;
        }
        throw new IllegalStateException();
    }

    private static String getRespOthersName(CaseData caseData) {
        if (isEmpty(caseData.getRespondentCollection())) {
            return SPACE;
        }
        return String.join(", ", caseData.getRespondentCollection()
                .stream()
                .skip(1)
                .filter(respondentSumTypeItem -> respondentSumTypeItem.getValue().getResponseStruckOut().equals(NO))
                .map(respondentSumTypeItem -> respondentSumTypeItem.getValue().getRespondentName())
                .toList());
    }

    public static String addMillisToDateToSearch(String dateToSearch) {
        if (dateToSearch.length() < NUMBER_CHAR_PARSING_DATE) {
            return dateToSearch.concat(".000");
        }
        return dateToSearch;
    }

    public static boolean getListingDateBetween(String dateToSearchFrom, String dateToSearchTo, String dateToSearch) {
        LocalDate localDateFrom = LocalDate.parse(dateToSearchFrom, OLD_DATE_TIME_PATTERN2);
        LocalDate localDate = LocalDate.parse(addMillisToDateToSearch(dateToSearch), OLD_DATE_TIME_PATTERN);
        if (dateToSearchTo.isEmpty()) {
            return localDateFrom.isEqual(localDate);
        } else {
            LocalDate localDateTo = LocalDate.parse(dateToSearchTo, OLD_DATE_TIME_PATTERN2);
            return !localDate.isBefore(localDateFrom) && !localDate.isAfter(localDateTo);
        }
    }

    public static boolean getMatchingDateBetween(String dateToSearchFrom, String dateToSearchTo,
                                                 String dateToSearch, boolean dateRange) {
        LocalDate localDate = LocalDate.parse(dateToSearch, OLD_DATE_TIME_PATTERN2);
        LocalDate localDateFrom = LocalDate.parse(dateToSearchFrom, OLD_DATE_TIME_PATTERN2);
        if (dateRange) {
            LocalDate localDateTo = LocalDate.parse(dateToSearchTo, OLD_DATE_TIME_PATTERN2);
            return !localDate.isBefore(localDateFrom) && !localDate.isAfter(localDateTo);
        } else {
            return localDateFrom.isEqual(localDate);
        }
    }

    public static boolean isListingRangeValid(ListingData listingData, List<String> errors) {
        if (listingData.getHearingDateType().equals(RANGE_HEARING_DATE_TYPE)) {
            LocalDate localDateFrom = LocalDate.parse(listingData.getListingDateFrom(), OLD_DATE_TIME_PATTERN2);
            LocalDate localDateTo = LocalDate.parse(listingData.getListingDateTo(), OLD_DATE_TIME_PATTERN2);
            long noOfDaysBetween = ChronoUnit.DAYS.between(localDateFrom, localDateTo);
            if (localDateFrom.isBefore(localDateTo) && noOfDaysBetween <= 31) {
                return true;
            } else {
                errors.add("Date range is limited to a max of 31 days");
                return false;
            }
        }
        return true;
    }

    private static Comparator<String> getDateComparator() {
        return Comparator.comparing(causeListDate -> causeListDate != null
                        ? LocalDate.parse(causeListDate, NEW_DATE_PATTERN)
                        : null,
                Comparator.nullsLast(Comparator.naturalOrder()));
    }

    private static Comparator<String> getVenueComparator() {
        return Comparator.comparing(causeListVenue -> causeListVenue,
                Comparator.nullsLast(Comparator.naturalOrder()));
    }

    private static Comparator<ListingTypeItem> getVenueComparatorListingTypeItem() {
        return Comparator.comparing(s -> s.getValue().getCauseListVenue(),
                Comparator.nullsLast(Comparator.naturalOrder()));
    }

    private static Comparator<ListingTypeItem> getDateComparatorListingTypeItem() {
        return Comparator.comparing(s -> s.getValue().getCauseListDate() != null
                        ? LocalDate.parse(s.getValue().getCauseListDate(), NEW_DATE_PATTERN)
                        : null,
                Comparator.nullsLast(Comparator.naturalOrder()));
    }

    private static Comparator<ListingTypeItem> getTimeComparatorListingTypeItem() {
        return Comparator.comparing(s -> s.getValue().getCauseListTime() != null
                        ? LocalTime.parse(s.getValue().getCauseListTime(), NEW_TIME_PATTERN)
                        : null,
                Comparator.nullsLast(Comparator.naturalOrder()));
    }

    private static void sortListingCollection(ListingData listingData, String templateName) {

        log.info("Sorting hearings");
        if (Arrays.asList(PRESS_LIST_CAUSE_LIST_RANGE_TEMPLATE, PRESS_LIST_CAUSE_LIST_SINGLE_TEMPLATE)
                .contains(templateName)) {

            listingData.getListingCollection()
                    .sort(getVenueComparatorListingTypeItem()
                            .thenComparing(getDateComparatorListingTypeItem())
                            .thenComparing(getTimeComparatorListingTypeItem()));
        } else {

            listingData.getListingCollection()
                    .sort(getDateComparatorListingTypeItem()
                            .thenComparing(getVenueComparatorListingTypeItem())
                            .thenComparing(getTimeComparatorListingTypeItem()));
        }
    }

    public static boolean isReportType(String reportType) {
        return REPORTS.contains(reportType);
    }

    private static String getReportDocTemplateName(String reportType) {
        return switch (reportType) {
            case BROUGHT_FORWARD_REPORT -> "EM-TRB-SCO-ENG-00218";
            case CLAIMS_ACCEPTED_REPORT -> "EM-TRB-SCO-ENG-00219";
            case LIVE_CASELOAD_REPORT -> "EM-TRB-SCO-ENG-00220";
            case CASES_COMPLETED_REPORT -> "EM-TRB-SCO-ENG-00221";
            case CASES_AWAITING_JUDGMENT_REPORT -> "EM-TRB-SCO-ENG-00749";
            case TIME_TO_FIRST_HEARING_REPORT -> "EM-TRB-SCO-ENG-00751";
            case SERVING_CLAIMS_REPORT -> "EM-TRB-SCO-ENG-00781";
            case CASE_SOURCE_LOCAL_REPORT -> "EM-TRB-SCO-ENG-00783";
            case HEARINGS_BY_HEARING_TYPE_REPORT -> "EM-TRB-SCO-ENG-00785";
            case HEARINGS_TO_JUDGEMENTS_REPORT -> "EM-TRB-SCO-ENG-00786";
            case NO_CHANGE_IN_CURRENT_POSITION_REPORT -> "EM-TRB-SCO-ENG-00794";
            case MEMBER_DAYS_REPORT -> "EM-TRB-SCO-ENG-00800";
            case RESPONDENTS_REPORT -> "EM-TRB-SCO-ENG-00815";
            case SESSION_DAYS_REPORT -> "EM-TRB-SCO-ENG-00817";
            case ECC_REPORT -> "EM-TRB-SCO-ENG-00818";
            default -> NO_DOCUMENT_FOUND;
        };
    }

    private static String getHearingDocTemplateName(ListingData listingData) {
        String roomOrNoRoom = isNullOrEmpty(listingData.getRoomOrNoRoom()) ? "" : listingData.getRoomOrNoRoom();
        if (listingData.getHearingDocType().equals(HEARING_DOC_ETCL)
                && listingData.getHearingDocETCL().equals(HEARING_ETCL_STAFF)
                && roomOrNoRoom.equals(NO)) {
            return STAFF_CASE_CAUSE_LIST_TEMPLATE;
        } else if (listingData.getHearingDocType().equals(HEARING_DOC_ETCL)
                && listingData.getHearingDocETCL().equals(HEARING_ETCL_STAFF)
                && roomOrNoRoom.equals(YES)) {
            return STAFF_CASE_CAUSE_LIST_ROOM_TEMPLATE;
        } else if (listingData.getHearingDocType().equals(HEARING_DOC_ETCL)
                && listingData.getHearingDocETCL().equals(HEARING_ETCL_PUBLIC)
                && roomOrNoRoom.equals(NO)) {
            return PUBLIC_CASE_CAUSE_LIST_TEMPLATE;
        } else if (listingData.getHearingDocType().equals(HEARING_DOC_ETCL)
                && listingData.getHearingDocETCL().equals(HEARING_ETCL_PUBLIC)
                && roomOrNoRoom.equals(YES)) {
            return PUBLIC_CASE_CAUSE_LIST_ROOM_TEMPLATE;
        } else if (listingData.getHearingDocType().equals(HEARING_DOC_ETCL)
                && listingData.getHearingDocETCL().equals(HEARING_ETCL_PRESS_LIST)
                && listingData.getHearingDateType().equals(RANGE_HEARING_DATE_TYPE)) {
            return PRESS_LIST_CAUSE_LIST_RANGE_TEMPLATE;
        } else if (listingData.getHearingDocType().equals(HEARING_DOC_ETCL)
                && listingData.getHearingDocETCL().equals(HEARING_ETCL_PRESS_LIST)
                && !listingData.getHearingDateType().equals(RANGE_HEARING_DATE_TYPE)) {
            return PRESS_LIST_CAUSE_LIST_SINGLE_TEMPLATE;
        } else if (listingData.getHearingDocType().equals(HEARING_DOC_IT56)) {
            return IT56_TEMPLATE;
        } else if (listingData.getHearingDocType().equals(HEARING_DOC_IT57)) {
            return IT57_TEMPLATE;
        }
        return NO_DOCUMENT_FOUND;
    }
}

