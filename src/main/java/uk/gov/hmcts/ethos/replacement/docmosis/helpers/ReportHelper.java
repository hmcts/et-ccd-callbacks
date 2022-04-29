package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.items.BFActionTypeItem;
import uk.gov.hmcts.et.common.model.listing.ListingData;
import uk.gov.hmcts.et.common.model.listing.ListingDetails;
import uk.gov.hmcts.et.common.model.listing.items.AdhocReportTypeItem;
import uk.gov.hmcts.et.common.model.listing.items.BFDateTypeItem;
import uk.gov.hmcts.et.common.model.listing.types.AdhocReportType;
import uk.gov.hmcts.et.common.model.listing.types.BFDateType;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MULTIPLE_CASE_TYPE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.OLD_DATE_TIME_PATTERN;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.OLD_DATE_TIME_PATTERN2;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.POSITION_TYPE_CASE_CLOSED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.POSITION_TYPE_CASE_INPUT_IN_ERROR;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.POSITION_TYPE_CASE_TRANSFERRED_OTHER_COUNTRY;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.POSITION_TYPE_CASE_TRANSFERRED_SAME_COUNTRY;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.POSITION_TYPE_REJECTED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RANGE_HEARING_DATE_TYPE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SINGLE_CASE_TYPE;

@Slf4j
public class ReportHelper {

    public static final String CASES_SEARCHED = "Cases searched: ";
    private static final String SPACE = " ";
    private static final String DATE_TIME_SEPARATOR = "T";
    private static final String MILLISECOND_PART = ".000";

    private ReportHelper() {
    }

    public static boolean validateMatchingDate(ListingData listingData, String matchingDate) {
        boolean dateRange = listingData.getHearingDateType().equals(RANGE_HEARING_DATE_TYPE);
        if (!dateRange) {
            String dateToSearch = listingData.getListingDate();
            return ListingHelper.getMatchingDateBetween(dateToSearch, "", matchingDate, false);
        } else {
            String dateToSearchFrom = listingData.getListingDateFrom();
            String dateToSearchTo = listingData.getListingDateTo();
            return ListingHelper.getMatchingDateBetween(dateToSearchFrom, dateToSearchTo, matchingDate, true);
        }
    }

    public static ListingData processBroughtForwardDatesRequest(ListingDetails listingDetails,
                                                                List<SubmitEvent> submitEvents) {
        if (submitEvents != null && !submitEvents.isEmpty()) {
            log.info(CASES_SEARCHED + submitEvents.size());
            List<BFDateTypeItem> bfDateTypeItems = new ArrayList<>();
            for (SubmitEvent submitEvent : submitEvents) {
                addBfDateTypeItems(submitEvent, listingDetails, bfDateTypeItems);
            }
            listingDetails.getCaseData().setBfDateCollection(bfDateTypeItems);
        }

        listingDetails.getCaseData().clearReportFields();
        return listingDetails.getCaseData();
    }

    private static void addBfDateTypeItems(
            SubmitEvent submitEvent,
            ListingDetails listingDetails,
            List<BFDateTypeItem> bfDateTypeItems) {
        if (submitEvent.getCaseData().getBfActions() != null
                && !submitEvent.getCaseData().getBfActions().isEmpty()) {
            for (BFActionTypeItem bfActionTypeItem : submitEvent.getCaseData().getBfActions()) {
                var bfDateTypeItem = getBFDateTypeItem(bfActionTypeItem,
                        listingDetails.getCaseData(), submitEvent.getCaseData());
                if (bfDateTypeItem.getValue() != null) {
                    bfDateTypeItems.add(bfDateTypeItem);
                }
            }
        }
    }

    public static String getFormattedLocalDate(String date) {
        if (date == null || date.length() < 10) {
            return null;
        }

        try {
            if (date.contains(DATE_TIME_SEPARATOR) && date.endsWith(MILLISECOND_PART)) {
                return LocalDateTime.parse(date, OLD_DATE_TIME_PATTERN).toLocalDate().toString();
            } else if (date.contains(DATE_TIME_SEPARATOR)) {
                return LocalDate.parse(date.split(DATE_TIME_SEPARATOR)[0], OLD_DATE_TIME_PATTERN2).toString();
            } else if (date.contains(SPACE)) {
                return LocalDate.parse(date.split(SPACE)[0], OLD_DATE_TIME_PATTERN2).toString();
            }
            return LocalDate.parse(date, OLD_DATE_TIME_PATTERN2).toString();
        } catch (DateTimeException e) {
            log.warn(String.format("Unable to parse %s to LocalDate in getFormattedLocalDate "
                + "method of ReportHelper", date), e);
            return null;
        }
    }

    public static ListingData processClaimsAcceptedRequest(ListingDetails listingDetails,
                                                           List<SubmitEvent> submitEvents) {
        var localReportsDetailHdr = new AdhocReportType();
        localReportsDetailHdr.setReportOffice(getReportOffice(
                UtilHelper.getListingCaseTypeId(listingDetails.getCaseTypeId()),
                listingDetails.getCaseData().getManagingOffice()));
        if (CollectionUtils.isNotEmpty(submitEvents)) {
            log.info(CASES_SEARCHED + submitEvents.size());
            var totalCases = 0;
            var totalSingles = 0;
            var totalMultiples = 0;
            List<AdhocReportTypeItem> localReportsDetailList = new ArrayList<>();
            for (SubmitEvent submitEvent : submitEvents) {
                AdhocReportTypeItem localReportsDetailItem =
                        getClaimsAcceptedDetailItem(listingDetails, submitEvent.getCaseData());
                if (localReportsDetailItem.getValue() != null) {
                    totalCases++;
                    if (localReportsDetailItem.getValue().getCaseType().equals(SINGLE_CASE_TYPE)) {
                        totalSingles++;
                    } else {
                        totalMultiples++;
                    }
                    localReportsDetailList.add(localReportsDetailItem);
                }
            }
            localReportsDetailHdr.setTotal(Integer.toString(totalCases));
            localReportsDetailHdr.setSinglesTotal(Integer.toString(totalSingles));
            localReportsDetailHdr.setMultiplesTotal(Integer.toString(totalMultiples));
            listingDetails.getCaseData().setLocalReportsDetail(localReportsDetailList);
        }
        listingDetails.getCaseData().setLocalReportsDetailHdr(localReportsDetailHdr);
        listingDetails.getCaseData().clearReportFields();
        return listingDetails.getCaseData();
    }

    public static ListingData processLiveCaseloadRequest(ListingDetails listingDetails,
                                                         List<SubmitEvent> submitEvents) {
        if (submitEvents != null && !submitEvents.isEmpty()) {
            log.info(CASES_SEARCHED + submitEvents.size());
            List<AdhocReportTypeItem> localReportsDetailList = new ArrayList<>();

            for (SubmitEvent submitEvent : submitEvents) {
                AdhocReportTypeItem localReportsDetailItem =
                        getLiveCaseloadDetailItem(listingDetails, submitEvent.getCaseData());
                if (localReportsDetailItem.getValue() != null) {
                    localReportsDetailList.add(localReportsDetailItem);
                }
            }

            var localReportsDetailHdr = new AdhocReportType();
            localReportsDetailHdr.setReportOffice(listingDetails.getCaseData().getManagingOffice());
            listingDetails.getCaseData().setLocalReportsDetailHdr(localReportsDetailHdr);
            listingDetails.getCaseData().setLocalReportsDetail(localReportsDetailList);

            var localReportsSummaryHdr = new AdhocReportType();
            var singlesTotal = getSinglesTotal(localReportsDetailList);
            var multiplesTotal = getMultiplesTotal(localReportsDetailList);
            var total = singlesTotal + multiplesTotal;

            localReportsSummaryHdr.setSinglesTotal(String.valueOf(singlesTotal));
            localReportsSummaryHdr.setMultiplesTotal(String.valueOf(multiplesTotal));
            localReportsSummaryHdr.setTotal(String.valueOf(total));
            listingDetails.getCaseData().setLocalReportsSummaryHdr(localReportsSummaryHdr);
        }

        listingDetails.getCaseData().clearReportFields();
        return listingDetails.getCaseData();
    }

    private static long getSinglesTotal(List<AdhocReportTypeItem> localReportsDetailList) {
        long singlesTotal = 0;
        if (CollectionUtils.isNotEmpty(localReportsDetailList)) {
            singlesTotal = localReportsDetailList.stream().distinct()
                    .filter(reportItem -> SINGLE_CASE_TYPE.equals(reportItem.getValue().getCaseType())).count();
        }
        return singlesTotal;
    }

    private static long getMultiplesTotal(List<AdhocReportTypeItem> localReportsDetailList) {
        long multiplesTotal = 0;
        if (CollectionUtils.isNotEmpty(localReportsDetailList)) {
            multiplesTotal = localReportsDetailList.stream().distinct()
                    .filter(reportItem -> MULTIPLE_CASE_TYPE.equals(reportItem.getValue().getCaseType())).count();
        }
        return multiplesTotal;
    }

    private static BFDateTypeItem getBFDateTypeItem(BFActionTypeItem bfActionTypeItem,
                                                    ListingData listingData, CaseData caseData) {
        var bfDateTypeItem = new BFDateTypeItem();
        var bfActionType = bfActionTypeItem.getValue();
        if (!isNullOrEmpty(bfActionType.getBfDate()) && isNullOrEmpty(bfActionType.getCleared())) {
            boolean matchingDateIsValid = validateMatchingDate(listingData, bfActionType.getBfDate());
            boolean clerkResponsibleIsValid = validateClerkResponsible(listingData, caseData);
            if (matchingDateIsValid && clerkResponsibleIsValid) {
                var bfDateType = new BFDateType();
                bfDateType.setCaseReference(caseData.getEthosCaseReference());
                if (!Strings.isNullOrEmpty(bfActionType.getAllActions())) {
                    bfDateType.setBroughtForwardAction(bfActionType.getAllActions());
                } else if (!Strings.isNullOrEmpty(bfActionType.getCwActions())) {
                    bfDateType.setBroughtForwardAction(bfActionType.getCwActions());
                }
                bfDateType.setBroughtForwardDate(bfActionType.getBfDate());
                bfDateType.setBroughtForwardDateCleared(bfActionType.getCleared());
                bfDateType.setBroughtForwardDateReason(bfActionType.getNotes());
                bfDateTypeItem.setId(String.valueOf(bfActionTypeItem.getId()));
                bfDateTypeItem.setValue(bfDateType);
            }
        }
        return bfDateTypeItem;
    }

    private static AdhocReportTypeItem getClaimsAcceptedDetailItem(ListingDetails listingDetails, CaseData caseData) {
        var adhocReportTypeItem = new AdhocReportTypeItem();
        var listingData = listingDetails.getCaseData();
        if (caseData.getPreAcceptCase() != null && caseData.getPreAcceptCase().getDateAccepted() != null) {
            boolean matchingDateIsValid =
                    validateMatchingDate(listingData, caseData.getPreAcceptCase().getDateAccepted());
            if (matchingDateIsValid) {
                var adhocReportType = new AdhocReportType();
                adhocReportType.setCaseType(caseData.getEcmCaseType());
                getCommonReportDetailFields(listingDetails, caseData, adhocReportType);
                adhocReportTypeItem.setValue(adhocReportType);
            }
        }
        return adhocReportTypeItem;
    }

    private static AdhocReportTypeItem getLiveCaseloadDetailItem(ListingDetails listingDetails, CaseData caseData) {
        var adhocReportTypeItem = new AdhocReportTypeItem();
        var listingData = listingDetails.getCaseData();
        if (caseData.getPreAcceptCase() != null && caseData.getPreAcceptCase().getDateAccepted() != null) {
            boolean matchingDateIsValid =
                    validateMatchingDate(listingData, caseData.getPreAcceptCase().getDateAccepted());
            boolean liveCaseloadIsValid = liveCaseloadIsValid(caseData);
            if (matchingDateIsValid && liveCaseloadIsValid) {
                var adhocReportType = new AdhocReportType();
                adhocReportType.setReportOffice(getTribunalOffice(listingDetails, caseData));
                // TODO : hearingCollection.Hearing_stage implementation
                getCommonReportDetailFields(listingDetails, caseData, adhocReportType);
                adhocReportTypeItem.setValue(adhocReportType);
            }
        }
        return adhocReportTypeItem;
    }

    private static boolean validateClerkResponsible(ListingData listingData, CaseData caseData) {
        var listingClerkCode = getClerkCode(listingData.getClerkResponsible());
        var caseDataClerkCode = getClerkCode(caseData.getClerkResponsible());
        if (listingClerkCode != null) {
            return listingClerkCode.equals(caseDataClerkCode);
        } else {
            return true;
        }
    }

    private static String getClerkCode(DynamicFixedListType dynamicFixedListType) {
        return dynamicFixedListType != null ? dynamicFixedListType.getSelectedCode() : null;
    }

    private static void getCommonReportDetailFields(ListingDetails listingDetails, CaseData caseData,
                                                    AdhocReportType adhocReportType) {
        adhocReportType.setCaseReference(caseData.getEthosCaseReference());
        adhocReportType.setDateOfAcceptance(caseData.getPreAcceptCase().getDateAccepted());
        adhocReportType.setMultipleRef(caseData.getMultipleReference());
        adhocReportType.setLeadCase(caseData.getLeadClaimant());
        adhocReportType.setPosition(caseData.getCurrentPosition());
        adhocReportType.setDateToPosition(caseData.getDateToPosition());
        adhocReportType.setFileLocation(getFileLocation(listingDetails, caseData));
        if (caseData.getClerkResponsible() != null && caseData.getClerkResponsible().getValue() != null) {
            adhocReportType.setClerk(caseData.getClerkResponsible().getSelectedLabel());
        }
        adhocReportType.setCaseType(caseData.getEcmCaseType());
    }

    private static String getFileLocation(ListingDetails listingDetails, CaseData caseData) {
        String caseTypeId = UtilHelper.getListingCaseTypeId(listingDetails.getCaseTypeId());
        DynamicFixedListType fileLocation = null;
        if (SCOTLAND_CASE_TYPE_ID.equals(caseTypeId)) {
            final TribunalOffice tribunalOffice = TribunalOffice.valueOfOfficeName(caseData.getManagingOffice());
            switch (tribunalOffice) {
                case DUNDEE:
                    fileLocation = caseData.getFileLocationDundee();
                    break;
                case GLASGOW:
                    fileLocation = caseData.getFileLocationGlasgow();
                    break;
                case ABERDEEN:
                    fileLocation = caseData.getFileLocationAberdeen();
                    break;
                case EDINBURGH:
                    fileLocation = caseData.getFileLocationEdinburgh();
                    break;
                default:
                    break;
            }
        } else {
            fileLocation = caseData.getFileLocation();
        }

        return fileLocation != null && fileLocation.getValue() != null ? fileLocation.getSelectedLabel() : null;
    }

    private static String getTribunalOffice(ListingDetails listingDetails, CaseData caseData) {
        String caseTypeId = UtilHelper.getListingCaseTypeId(listingDetails.getCaseTypeId());
        return caseTypeId.equals(SCOTLAND_CASE_TYPE_ID)
            ? caseData.getManagingOffice() : listingDetails.getCaseData().getManagingOffice();
    }

    private static boolean liveCaseloadIsValid(CaseData caseData) {
        if (caseData.getPositionType() != null) {
            List<String> invalidPositionTypes = Arrays.asList(POSITION_TYPE_REJECTED,
                    POSITION_TYPE_CASE_CLOSED,
                    POSITION_TYPE_CASE_INPUT_IN_ERROR,
                    POSITION_TYPE_CASE_TRANSFERRED_SAME_COUNTRY,
                    POSITION_TYPE_CASE_TRANSFERRED_OTHER_COUNTRY);
            return invalidPositionTypes.stream().noneMatch(str -> str.equals(caseData.getPositionType()));
        }
        return true;
    }

    public static String getReportOffice(String caseTypeId, String managingOffice) {
        if (ENGLANDWALES_CASE_TYPE_ID.equals(caseTypeId)) {
            return managingOffice;
        } else if (SCOTLAND_CASE_TYPE_ID.equals(caseTypeId)) {
            return TribunalOffice.SCOTLAND.getOfficeName();
        } else {
            throw new IllegalArgumentException("Unexpected case type id " + caseTypeId);
        }
    }
}
