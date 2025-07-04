package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.exceptions.CaseCreationException;
import uk.gov.hmcts.ecm.common.exceptions.DocumentManagementException;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DateListedType;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;
import uk.gov.hmcts.et.common.model.listing.ListingData;
import uk.gov.hmcts.et.common.model.listing.ListingDetails;
import uk.gov.hmcts.et.common.model.listing.items.ListingTypeItem;
import uk.gov.hmcts.et.common.model.listing.types.ListingType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.ListingHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.ListingVenueHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReportHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.bfaction.BfActionReport;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.bfaction.BfActionReportData;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.casescompleted.CasesCompletedReport;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.casesourcelocalreport.CaseSourceLocalReport;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.claimsbyhearingvenue.ClaimsByHearingVenueReportData;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.memberdays.MemberDaysReport;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.servingclaims.ServingClaimsReport;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.timetofirsthearing.TimeToFirstHearingReport;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.ExcelReportDocumentInfoService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.VenueService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ALL_VENUES;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.BROUGHT_FORWARD_REPORT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CASES_COMPLETED_REPORT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CASE_SOURCE_LOCAL_REPORT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMS_ACCEPTED_REPORT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMS_BY_HEARING_VENUE_REPORT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_LISTING_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_DOC_ETCL;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_ETCL_STAFF;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_POSTPONED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_SETTLED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_WITHDRAWN;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_TYPE_JUDICIAL_MEDIATION;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_TYPE_JUDICIAL_MEDIATION_TCC;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_TYPE_PERLIMINARY_HEARING;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_TYPE_PERLIMINARY_HEARING_CM;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_TYPE_PERLIMINARY_HEARING_CM_TCC;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_TYPE_PRIVATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.LIVE_CASELOAD_REPORT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MAX_ES_SIZE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MEMBER_DAYS_REPORT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.OLD_DATE_TIME_PATTERN2;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RANGE_HEARING_DATE_TYPE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_LISTING_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SERVING_CLAIMS_REPORT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TIME_TO_FIRST_HEARING_REPORT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.ListingHelper.CAUSE_LIST_DATE_TIME_PATTERN;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReportHelper.CASES_SEARCHED;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.Constants.ELASTICSEARCH_FIELD_HEARING_LISTED_DATE;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.Constants.ELASTICSEARCH_FIELD_HEARING_LOCATION;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.Constants.ELASTICSEARCH_FIELD_HEARING_VENUE_DAY_SCOTLAND;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.Constants.ELASTICSEARCH_FIELD_MANAGING_OFFICE_KEYWORD;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.DefaultValuesReaderService.ALL_OFFICES;

@RequiredArgsConstructor
@Slf4j
@Service("listingService")
public class ListingService {

    private final TornadoService tornadoService;
    private final CcdClient ccdClient;
    private final CasesCompletedReport casesCompletedReport;
    private final TimeToFirstHearingReport timeToFirstHearingReport;
    private final ServingClaimsReport servingClaimsReport;
    private final CaseSourceLocalReport caseSourceLocalReport;
    private final ExcelReportDocumentInfoService excelReportDocumentInfoService;
    private final VenueService venueService;
    private final BfActionReport bfActionReport;
    private static final String MISSING_DOCUMENT_NAME = "Missing document name";
    private static final String MESSAGE = "Failed to generate document for case id : ";
    public static final String ELASTICSEARCH_FIELD_HEARING_VENUE_SCOTLAND =
            "data.hearingCollection.value.Hearing_venue_Scotland";
    public static final String HEARING_STATUS_VACATED = "Vacated";
    public static final String ELASTICSEARCH_FIELD_MANAGING_OFFICE = "data.managingOffice";

    public ListingData listingCaseCreation(ListingDetails listingDetails) {

        ListingData listingData = listingDetails.getCaseData();

        if (listingData.getHearingDocType() != null) {
            listingData.setDocumentName(listingData.getHearingDocType());
        } else if (listingData.getReportType() != null) {
            listingData.setDocumentName(listingData.getReportType());
        } else {
            listingData.setDocumentName(MISSING_DOCUMENT_NAME);
        }

        return listingData;
    }

    public CaseData processListingSingleCasesRequest(CaseDetails caseDetails) {
        CaseData caseData = caseDetails.getCaseData();
        List<ListingTypeItem> listingTypeItems = new ArrayList<>();
        String caseTypeId = getCaseTypeId(caseData);
        if (isNotEmpty(caseData.getHearingCollection())) {
            for (HearingTypeItem hearingTypeItem : caseData.getHearingCollection()) {
                if (hearingTypeItem.getValue().getHearingDateCollection() != null) {
                    log.info("Processing listing single cases");
                    listingTypeItems.addAll(getListingTypeItems(hearingTypeItem,
                            caseData.getPrintHearingDetails(), caseData, caseTypeId));
                }
            }
        }
        caseData.setPrintHearingCollection(caseData.getPrintHearingDetails());
        caseData.getPrintHearingCollection().setListingCollection(listingTypeItems);

        return caseData;
    }

    public ListingData setManagingOfficeAndCourtAddressFromCaseData(CaseData caseData) {
        ListingData listingData = caseData.getPrintHearingCollection();
        listingData.setTribunalCorrespondenceAddress(caseData.getTribunalCorrespondenceAddress());
        listingData.setTribunalCorrespondenceTelephone(caseData.getTribunalCorrespondenceTelephone());
        listingData.setTribunalCorrespondenceFax(caseData.getTribunalCorrespondenceFax());
        listingData.setTribunalCorrespondenceEmail(caseData.getTribunalCorrespondenceEmail());
        listingData.setTribunalCorrespondenceDX(caseData.getTribunalCorrespondenceDX());
        listingData.setManagingOffice(caseData.getManagingOffice());
        return listingData;
    }

    public ListingData processListingHearingsRequest(ListingDetails listingDetails, String authToken) {
        if (SCOTLAND_LISTING_CASE_TYPE_ID.equals(listingDetails.getCaseTypeId())) {
            populateScottishVenues(listingDetails.getCaseData());
        }

        try {
            List<SubmitEvent> submitEvents = getListingHearingsSearch(listingDetails, authToken);
            if (isNotEmpty(submitEvents)) {
                log.info(CASES_SEARCHED + "{}", submitEvents.size());
                List<ListingTypeItem> listingTypeItems = new ArrayList<>();
                for (SubmitEvent submitEvent : submitEvents) {
                    if (isNotEmpty(submitEvent.getCaseData().getHearingCollection())) {
                        addListingTypeItems(submitEvent, listingTypeItems, listingDetails);
                    }
                }
                listingTypeItems.sort(Comparator.comparing(o -> LocalDate.parse(o.getValue().getCauseListDate(),
                        CAUSE_LIST_DATE_TIME_PATTERN)));
                listingDetails.getCaseData().setListingCollection(listingTypeItems);
            }
            listingDetails.getCaseData().clearReportFields();
            return listingDetails.getCaseData();
        } catch (Exception ex) {
            throw new CaseCreationException(MESSAGE + listingDetails.getCaseId() + ex.getMessage());
        }
    }

    private void populateScottishVenues(ListingData listingData) {
        if (TribunalOffice.ABERDEEN.getOfficeName().equals(listingData.getManagingOffice())) {
            listingData.setVenueAberdeen(listingData.getListingVenue());
        } else if (TribunalOffice.DUNDEE.getOfficeName().equals(listingData.getManagingOffice())) {
            listingData.setVenueDundee(listingData.getListingVenue());
        } else if (TribunalOffice.EDINBURGH.getOfficeName().equals(listingData.getManagingOffice())) {
            listingData.setVenueEdinburgh(listingData.getListingVenue());
        } else if (TribunalOffice.GLASGOW.getOfficeName().equals(listingData.getManagingOffice())) {
            listingData.setVenueGlasgow(listingData.getListingVenue());
        }
    }

    private void addListingTypeItems(SubmitEvent submitEvent,
                                     List<ListingTypeItem> listingTypeItems,
                                     ListingDetails listingDetails) {
        for (HearingTypeItem hearingTypeItem : submitEvent.getCaseData().getHearingCollection()) {
            if (isNotEmpty(hearingTypeItem.getValue().getHearingDateCollection())) {
                listingTypeItems.addAll(getListingTypeItems(hearingTypeItem,
                        listingDetails.getCaseData(),
                        submitEvent.getCaseData(),
                        UtilHelper.getListingCaseTypeId(listingDetails.getCaseTypeId())));
            }
        }
    }

    private List<SubmitEvent> getListingHearingsSearch(ListingDetails listingDetails, String authToken)
            throws IOException {
        ListingData listingData = listingDetails.getCaseData();
        Map.Entry<String, String> entry =
                ListingVenueHelper.getListingVenueToSearch(listingData).entrySet().iterator().next();
        String venueToSearchMapping = entry.getKey();
        String venueToSearch = entry.getValue();

        boolean dateRange = listingData.getHearingDateType().equals(RANGE_HEARING_DATE_TYPE);
        String dateFrom = dateRange ? listingData.getListingDateFrom() : listingData.getListingDate();
        String dateTo = dateRange ? listingData.getListingDateTo() : listingData.getListingDate();

        if (ALL_VENUES.equals(venueToSearch)) {
            venueToSearch = listingData.getManagingOffice();
            venueToSearchMapping = getFieldNameForVenueToSearch(listingDetails.getCaseTypeId());
        }

        return ccdClient.buildAndGetElasticSearchRequest(authToken,
                UtilHelper.getListingCaseTypeId(listingDetails.getCaseTypeId()),
                getESQuery(dateFrom, dateTo, venueToSearchMapping, venueToSearch, listingData.getManagingOffice()));
    }

    private String getFieldNameForVenueToSearch(String caseTypeId) {
        return SCOTLAND_CASE_TYPE_ID.equals(UtilHelper.getListingCaseTypeId(caseTypeId))
                ? ELASTICSEARCH_FIELD_HEARING_VENUE_SCOTLAND
                : ELASTICSEARCH_FIELD_MANAGING_OFFICE;
    }

    private String getESQuery(String dateFrom, String dateTo, String key, String venue, String managingOffice) {
        BoolQueryBuilder boolQueryBuilder = boolQuery()
                .filter(new RangeQueryBuilder(ELASTICSEARCH_FIELD_HEARING_LISTED_DATE).gte(dateFrom).lte(dateTo));

        if (!ALL_OFFICES.equals(managingOffice)) {
            if (TribunalOffice.isEnglandWalesOffice(managingOffice)) {
                boolQueryBuilder.filter(
                        new TermsQueryBuilder(ELASTICSEARCH_FIELD_MANAGING_OFFICE_KEYWORD, managingOffice));
            } else if (TribunalOffice.isScotlandOffice(managingOffice)) {
                boolQueryBuilder.must(
                        new MatchQueryBuilder(ELASTICSEARCH_FIELD_HEARING_VENUE_DAY_SCOTLAND, managingOffice));
            }
        }

        if (!managingOffice.equals(venue)) {
            if (TribunalOffice.isEnglandWalesOffice(managingOffice)) {
                boolQueryBuilder.must(new MatchQueryBuilder(key, venue));
            } else if (TribunalOffice.isScotlandOffice(managingOffice)) {
                String scotlandField = ELASTICSEARCH_FIELD_HEARING_LOCATION + managingOffice + ".value.code";
                boolQueryBuilder.must(new MatchQueryBuilder(scotlandField, venue));
            }
        }

        return new SearchSourceBuilder()
                .size(MAX_ES_SIZE)
                .query(boolQueryBuilder).toString();
    }

    public String getSelectedOfficeForPrintLists(CaseData caseData) {
        if (caseData.getPrintHearingDetails().getListingVenue() != null) {
            return caseData.getManagingOffice();
        } else if (!isNullOrEmpty(caseData.getPrintHearingDetails().getListingVenueScotland())) {
            return caseData.getPrintHearingDetails().getListingVenueScotland();
        } else {
            throw new IllegalStateException("Unable to get selected office from "
                    + "printing details for case reference " + caseData.getEthosCaseReference());
        }
    }

    private List<ListingTypeItem> getListingTypeItems(HearingTypeItem hearingTypeItem,
                                                      ListingData listingData,
                                                      CaseData caseData,
                                                      String caseTypeId) {
        List<ListingTypeItem> listingTypeItems = new ArrayList<>();
        if (isHearingTypeValid(listingData, hearingTypeItem)) {
            int hearingDateCollectionSize = hearingTypeItem.getValue().getHearingDateCollection().size();
            for (int i = 0; i < hearingDateCollectionSize; i++) {
                DateListedTypeItem dateListedTypeItem = hearingTypeItem.getValue().getHearingDateCollection().get(i);
                boolean isListingVenueValid = false;
                try {
                    isListingVenueValid = isListingVenueValid(listingData, dateListedTypeItem,
                            caseTypeId, caseData.getEthosCaseReference());
                } catch (Exception e) {
                    log.error("Unable to get venue code for case reference {}: {}",
                            caseData.getEthosCaseReference(), e.getMessage());
                }
                boolean isListingDateValid = isListingDateValid(listingData, dateListedTypeItem);
                boolean isListingStatusValid = true;
                if (!showAllHearingType(listingData)) {
                    isListingStatusValid = isListingStatusValid(dateListedTypeItem);
                }
                if (!isListingVenueValid || !isListingDateValid || !isListingStatusValid) {
                    continue;
                }
                ListingTypeItem listingTypeItem = new ListingTypeItem();
                ListingType listingType = ListingHelper.getListingTypeFromCaseData(
                        listingData, caseData, hearingTypeItem.getValue(), dateListedTypeItem.getValue(),
                        i, hearingDateCollectionSize);
                listingTypeItem.setId(String.valueOf(dateListedTypeItem.getId()));
                listingTypeItem.setValue(listingType);
                listingTypeItems.add(listingTypeItem);
            }
        }
        return listingTypeItems;
    }

    private String getCaseTypeId(CaseData caseData) {
        return TribunalOffice.getCaseTypeId(getSelectedOfficeForPrintLists(caseData));
    }

    public ListingData getDateRangeReport(ListingDetails listingDetails,
                                          String authToken,
                                          String userName) throws IOException {
        clearListingFields(listingDetails.getCaseData());
        List<SubmitEvent> submitEvents = getDateRangeReportSearch(listingDetails, authToken);
        log.info("Number of cases found: {}", submitEvents.size());
        return switch (listingDetails.getCaseData().getReportType()) {
            case BROUGHT_FORWARD_REPORT -> bfActionReport.runReport(listingDetails,
                    submitEvents, userName);
            case CLAIMS_ACCEPTED_REPORT -> ReportHelper.processClaimsAcceptedRequest(listingDetails, submitEvents);
            case LIVE_CASELOAD_REPORT -> ReportHelper.processLiveCaseloadRequest(listingDetails, submitEvents);
            case CASES_COMPLETED_REPORT -> casesCompletedReport.generateReportData(listingDetails, submitEvents);
            case TIME_TO_FIRST_HEARING_REPORT ->
                    timeToFirstHearingReport.generateReportData(listingDetails, submitEvents);
            case SERVING_CLAIMS_REPORT -> servingClaimsReport.generateReportData(listingDetails, submitEvents);
            case CASE_SOURCE_LOCAL_REPORT -> caseSourceLocalReport.generateReportData(listingDetails, submitEvents);
            case MEMBER_DAYS_REPORT -> new MemberDaysReport().runReport(listingDetails, submitEvents);
            default -> listingDetails.getCaseData();
        };
    }

    private void clearListingFields(ListingData listingData) {
        listingData.setLocalReportsSummary(null);
        listingData.setLocalReportsSummaryHdr(null);
        listingData.setLocalReportsSummaryHdr2(null);
        listingData.setLocalReportsSummary2(null);
        listingData.setLocalReportsDetailHdr(null);
        listingData.setLocalReportsDetail(null);
    }

    private List<SubmitEvent> getDateRangeReportSearch(ListingDetails listingDetails, String authToken)
            throws IOException {
        ListingData listingData = listingDetails.getCaseData();

        String dateSearchFrom;
        String dateSearchTo;
        if (RANGE_HEARING_DATE_TYPE.equals(listingData.getHearingDateType())) {
            dateSearchFrom = LocalDate.parse(listingData.getListingDateFrom(), OLD_DATE_TIME_PATTERN2).toString();
            dateSearchTo = LocalDate.parse(listingData.getListingDateTo(), OLD_DATE_TIME_PATTERN2).toString();
        } else {
            dateSearchFrom = LocalDate.parse(listingData.getListingDate(), OLD_DATE_TIME_PATTERN2).toString();
            dateSearchTo = dateSearchFrom;
        }

        String caseTypeId = UtilHelper.getListingCaseTypeId(listingDetails.getCaseTypeId());
        TribunalOffice tribunalOffice = listingData.getManagingOffice() != null
                ? TribunalOffice.valueOfOfficeName(listingData.getManagingOffice()) : null;
        return ccdClient.retrieveCasesGenericReportElasticSearch(authToken, caseTypeId, tribunalOffice, dateSearchFrom,
                dateSearchTo, listingData.getReportType());
    }

    private boolean areAllVenuesSelected(ListingData listingData,
                                         DateListedTypeItem dateListedTypeItem,
                                         String caseTypeId) {
        if (listingData.hasListingVenue()
                && ALL_VENUES.equals(listingData.getListingVenue().getSelectedCode())) {
            return caseTypeId.equals(ENGLANDWALES_CASE_TYPE_ID)
                    || caseTypeId.equals(SCOTLAND_CASE_TYPE_ID)
                    && (listingData.getManagingOffice()
                    .equals(dateListedTypeItem.getValue().getHearingVenueDayScotland())
                    || ALL_OFFICES.equals(listingData.getManagingOffice()));
        }
        return false;
    }

    public boolean isListingVenueValid(ListingData listingData,
                                       DateListedTypeItem dateListedTypeItem,
                                       String caseTypeId,
                                       String caseReference) {
        String venueSearched;
        String venueToSearch = ListingVenueHelper.getListingVenue(listingData);
        if (areAllVenuesSelected(listingData, dateListedTypeItem, caseTypeId)) {
            return true;
        } else {

            if (ListingVenueHelper.isAllScottishVenues(listingData)) {
                venueSearched = dateListedTypeItem.getValue().hasHearingVenue()
                        ? dateListedTypeItem.getValue().getHearingVenueDayScotland()
                        : " ";
            } else {
                try {
                    venueSearched = ListingHelper.getVenueCodeFromDateListedType(dateListedTypeItem.getValue());
                } catch (IllegalStateException ex) {
                    log.error("Unable to get venue code for case reference {}", caseReference, ex);
                    return false;
                }

            }
            return venueSearched.trim().equals(venueToSearch.trim());
        }
    }

    private boolean isListingDateValid(ListingData listingData, DateListedTypeItem dateListedTypeItem) {
        boolean dateRange = listingData.getHearingDateType().equals(RANGE_HEARING_DATE_TYPE);
        String dateListed = isNullOrEmpty(dateListedTypeItem.getValue().getListedDate()) ? "" :
                dateListedTypeItem.getValue().getListedDate();
        if (dateRange) {
            String dateToSearchFrom = listingData.getListingDateFrom();
            String dateToSearchTo = listingData.getListingDateTo();
            return ListingHelper.getListingDateBetween(dateToSearchFrom, dateToSearchTo, dateListed);
        } else {
            String dateToSearch = listingData.getListingDate();
            return ListingHelper.getListingDateBetween(dateToSearch, "", dateListed);
        }
    }

    private boolean isListingStatusValid(DateListedTypeItem dateListedTypeItem) {
        DateListedType dateListedType = dateListedTypeItem.getValue();

        if (dateListedType.getHearingStatus() != null) {
            List<String> invalidHearingStatuses = Arrays.asList(HEARING_STATUS_SETTLED,
                    HEARING_STATUS_WITHDRAWN, HEARING_STATUS_POSTPONED, HEARING_STATUS_VACATED);
            return invalidHearingStatuses.stream().noneMatch(str -> str.equals(dateListedType.getHearingStatus()));
        } else {
            return true;
        }
    }

    private boolean showAllHearingType(ListingData listingData) {
        return !isNullOrEmpty(listingData.getHearingDocType())
                && !isNullOrEmpty(listingData.getHearingDocETCL())
                && listingData.getHearingDocType().equals(HEARING_DOC_ETCL)
                && listingData.getHearingDocETCL().equals(HEARING_ETCL_STAFF)
                && !isNullOrEmpty(listingData.getShowAll())
                && listingData.getShowAll().equals(YES);
    }

    private boolean isHearingTypeValid(ListingData listingData, HearingTypeItem hearingTypeItem) {
        if (!isNullOrEmpty(listingData.getHearingDocType())
                && !isNullOrEmpty(listingData.getHearingDocETCL())
                && listingData.getHearingDocType().equals(HEARING_DOC_ETCL)
                && !listingData.getHearingDocETCL().equals(HEARING_ETCL_STAFF)) {

            HearingType hearingType = hearingTypeItem.getValue();

            if (hearingType.getHearingType() != null) {
                if (hearingType.getHearingType().equals(HEARING_TYPE_PERLIMINARY_HEARING)
                        && hearingType.getHearingPublicPrivate() != null
                        && hearingType.getHearingPublicPrivate().equals(HEARING_TYPE_PRIVATE)) {
                    return false;
                } else {
                    List<String> invalidHearingTypes = Arrays.asList(HEARING_TYPE_JUDICIAL_MEDIATION,
                            HEARING_TYPE_JUDICIAL_MEDIATION_TCC, HEARING_TYPE_PERLIMINARY_HEARING_CM,
                            HEARING_TYPE_PERLIMINARY_HEARING_CM_TCC);
                    return invalidHearingTypes.stream().noneMatch(str -> str.equals(hearingType.getHearingType()));
                }
            }
        }
        return true;
    }

    public DocumentInfo processHearingDocument(ListingData listingData, String caseTypeId, String authToken) {
        try {
            if (CLAIMS_BY_HEARING_VENUE_REPORT.equals(listingData.getReportType())) {
                return excelReportDocumentInfoService.generateClaimsByHearingVenueExcelReportDocumentInfo(
                        (ClaimsByHearingVenueReportData)listingData, caseTypeId, authToken);
            }
            if (BROUGHT_FORWARD_REPORT.equals(listingData.getReportType())) {
                return excelReportDocumentInfoService.generateBfExcelReportDocumentInfo(
                        (BfActionReportData)listingData, caseTypeId, authToken);
            }
            return tornadoService.listingGeneration(authToken, listingData, caseTypeId);
        } catch (Exception ex) {
            throw new DocumentManagementException(MESSAGE + caseTypeId, ex);
        }
    }

    public void dynamicVenueListing(String caseTypeId, ListingData listingData) {
        if (SCOTLAND_LISTING_CASE_TYPE_ID.equals(caseTypeId)
                || ENGLANDWALES_LISTING_CASE_TYPE_ID.equals(caseTypeId)) {
            dynamicVenueList(listingData);
        } else {
            throw new IllegalArgumentException(String.format("Unexpected CaseType : %s", caseTypeId));
        }
    }

    private void dynamicVenueList(ListingData listingData) {
        List<DynamicValueType> listItems = new ArrayList<>();
        listItems.add(DynamicValueType.create(ALL_VENUES, ALL_VENUES));
        if (!ALL_VENUES.equals(listingData.getManagingOffice())) {
            listItems.addAll(venueService.getVenues(TribunalOffice.valueOfOfficeName(
                    listingData.getManagingOffice())));
        }
        DynamicFixedListType dynamicListingVenues = new DynamicFixedListType();
        dynamicListingVenues.setListItems(listItems);
        listingData.setListingVenue(dynamicListingVenues);
        if (ALL_VENUES.equals(listingData.getManagingOffice())) {
            listingData.getListingVenue().setValue(listingData.getListingVenue().getListItems().get(0));
        }
    }
}
