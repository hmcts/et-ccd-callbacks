package uk.gov.hmcts.ethos.replacement.docmosis.reports.nochangeincurrentposition;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.listing.ListingDetails;
import uk.gov.hmcts.et.common.model.multiples.SubmitMultipleEvent;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ACCEPTED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MULTIPLE_CASE_TYPE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.OLD_DATE_TIME_PATTERN2;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.REJECTED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_LISTING_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SINGLE_CASE_TYPE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SUBMITTED_STATE;

@Slf4j
public class NoPositionChangeReport {
    private final NoPositionChangeDataSource noPositionChangeDataSource;
    private final String reportDate;
    public static final List<String> VALID_CASE_STATES = List.of(ACCEPTED_STATE, REJECTED_STATE, SUBMITTED_STATE);

    public NoPositionChangeReport(NoPositionChangeDataSource noPositionChangeDataSource, String reportDate) {
        this.noPositionChangeDataSource = noPositionChangeDataSource;
        this.reportDate = reportDate;
    }

    public NoPositionChangeReportData runReport(ListingDetails listingDetails) {
        var caseTypeId = listingDetails.getCaseTypeId();
        var managingOffice = isNullOrEmpty(listingDetails.getCaseData().getManagingOffice())
                ? TribunalOffice.GLASGOW.getOfficeName()
                : listingDetails.getCaseData().getManagingOffice();

        var submitEvents = getCases(caseTypeId, managingOffice);
        var reportData = initReport(SCOTLAND_LISTING_CASE_TYPE_ID.equals(caseTypeId)
            ? TribunalOffice.SCOTLAND.getOfficeName()
            : managingOffice);

        log.info("Retrieved No Change In Current Position report case data");
        if (CollectionUtils.isEmpty(submitEvents)) {
            return reportData;
        }

        submitEvents.removeIf(submitEvent -> !VALID_CASE_STATES.contains(submitEvent.getState()));

        var multipleIds = submitEvents.parallelStream()
                .filter(se -> se.getCaseData().getCaseType().equals(MULTIPLE_CASE_TYPE)
                        && StringUtils.isNotBlank(se.getCaseData().getMultipleReference()))
                .map(e -> e.getCaseData().getMultipleReference())
                .distinct()
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(multipleIds)) {
            var submitMultipleEvents = getMultipleCases(caseTypeId, multipleIds);
            populateData(reportData, caseTypeId, submitEvents, submitMultipleEvents);
        } else {
            populateData(reportData, caseTypeId, submitEvents);
        }

        return reportData;
    }

    private NoPositionChangeReportData initReport(String caseTypeId) {
        var reportSummary = new NoPositionChangeReportSummary(caseTypeId);
        return new NoPositionChangeReportData(reportSummary, reportDate);
    }

    private List<NoPositionChangeSubmitEvent> getCases(String caseTypeId, String managingOffice) {
        return noPositionChangeDataSource.getData(
                UtilHelper.getListingCaseTypeId(caseTypeId), reportDate, managingOffice);
    }

    private List<SubmitMultipleEvent> getMultipleCases(String casTypeId, List<String> multipleCaseIds) {
        var multipleCaseTypeId = UtilHelper.getListingCaseTypeId(casTypeId) + "_Multiple";
        return noPositionChangeDataSource.getMultiplesData(multipleCaseTypeId, multipleCaseIds);
    }

    private void populateData(NoPositionChangeReportData reportData,
                              String caseTypeId, List<NoPositionChangeSubmitEvent> submitEvents) {
        populateData(reportData, caseTypeId, submitEvents, new ArrayList<>());
    }

    private void populateData(NoPositionChangeReportData reportData,
                              String caseTypeId, List<NoPositionChangeSubmitEvent> submitEvents,
                              List<SubmitMultipleEvent> submitMultipleEvents) {
        log.info(String.format("No change in current position case type id %s search results: %d",
                caseTypeId, submitEvents.size()));

        for (var submitEvent : submitEvents) {
            var caseData = submitEvent.getCaseData();
            if (!isValidCase(caseData)) {
                continue;
            }

            if (caseData.getCaseType().equals(SINGLE_CASE_TYPE)) {
                var reportDetailSingle = new NoPositionChangeReportDetailSingle();
                reportDetailSingle.setCaseReference(caseData.getEthosCaseReference());
                reportDetailSingle.setCurrentPosition(caseData.getCurrentPosition());
                reportDetailSingle.setDateToPosition(caseData.getDateToPosition());

                var year = LocalDate.parse(caseData.getReceiptDate(), OLD_DATE_TIME_PATTERN2).getYear();
                reportDetailSingle.setYear(String.valueOf(year));

                var hasMultipleRespondents = CollectionUtils.isNotEmpty(caseData.getRespondentCollection())
                        && caseData.getRespondentCollection().size() > 1;
                var respondent = caseData.getRespondent() + (hasMultipleRespondents ? " & Others" : "");
                reportDetailSingle.setRespondent(respondent);

                reportData.addReportDetailsSingle(reportDetailSingle);
            } else if (caseData.getCaseType().equals(MULTIPLE_CASE_TYPE)) {
                var multipleCase = submitMultipleEvents.stream()
                        .filter(sme -> sme.getCaseData().getMultipleReference().equals(caseData.getMultipleReference()))
                        .findFirst();

                var reportDetailMultiple = new NoPositionChangeReportDetailMultiple();
                reportDetailMultiple.setCaseReference(caseData.getEthosCaseReference());
                reportDetailMultiple.setCurrentPosition(caseData.getCurrentPosition());
                reportDetailMultiple.setDateToPosition(caseData.getDateToPosition());
                reportDetailMultiple.setMultipleName(multipleCase.isPresent()
                        ? multipleCase.get().getCaseData().getMultipleName() : "");

                var year = LocalDate.parse(caseData.getReceiptDate(), OLD_DATE_TIME_PATTERN2).getYear();
                reportDetailMultiple.setYear(String.valueOf(year));

                reportData.addReportDetailsMultiple(reportDetailMultiple);
            }
        }

        reportData.getReportSummary().setTotalCases(
                String.valueOf(reportData.getReportDetailsSingle().size()
                        + reportData.getReportDetailsMultiple().size()));
        reportData.getReportSummary().setTotalSingleCases(
                String.valueOf(reportData.getReportDetailsSingle().size()));
        reportData.getReportSummary().setTotalMultipleCases(
                String.valueOf(reportData.getReportDetailsMultiple().size()));
    }

    private boolean isValidCase(NoPositionChangeCaseData caseData) {
        if (caseData == null) {
            return false;
        }

        if (StringUtils.isBlank(caseData.getCurrentPosition())
                || StringUtils.isBlank(caseData.getDateToPosition())) {
            return false;
        }

        var reportDateMinus3Months = LocalDate.parse(reportDate, OLD_DATE_TIME_PATTERN2).minusMonths(3).plusDays(1);
        var dateToPosition = LocalDate.parse(caseData.getDateToPosition(), OLD_DATE_TIME_PATTERN2);
        return dateToPosition.isBefore(reportDateMinus3Months);
    }
}
