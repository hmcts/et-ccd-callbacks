package uk.gov.hmcts.ethos.replacement.docmosis.reports.nochangeincurrentposition;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.listing.ListingDetails;
import uk.gov.hmcts.et.common.model.multiples.SubmitMultipleEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReportHelper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.ACCEPTED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MULTIPLE_CASE_TYPE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.OLD_DATE_TIME_PATTERN2;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.REJECTED_STATE;
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
        String caseTypeId = listingDetails.getCaseTypeId();
        String managingOffice = listingDetails.getCaseData().getManagingOffice();
        String reportOffice = ReportHelper.getReportOffice(listingDetails.getCaseTypeId(), managingOffice);

        List<NoPositionChangeSubmitEvent> submitEvents = getCases(caseTypeId, managingOffice);
        NoPositionChangeReportData reportData = initReport(reportOffice);

        log.info("Retrieved No Change In Current Position report case data");
        if (CollectionUtils.isEmpty(submitEvents)) {
            return reportData;
        }

        submitEvents.removeIf(submitEvent -> !VALID_CASE_STATES.contains(submitEvent.getState()));

        List<String> multipleIds = submitEvents.parallelStream()
                .filter(se -> se.getCaseData().getCaseType().equals(MULTIPLE_CASE_TYPE)
                        && StringUtils.isNotBlank(se.getCaseData().getMultipleReference()))
                .map(e -> e.getCaseData().getMultipleReference())
                .distinct()
                .toList();

        if (CollectionUtils.isNotEmpty(multipleIds)) {
            List<SubmitMultipleEvent> submitMultipleEvents = getMultipleCases(caseTypeId, multipleIds);
            populateData(reportData, caseTypeId, submitEvents, submitMultipleEvents);
        } else {
            populateData(reportData, caseTypeId, submitEvents);
        }

        return reportData;
    }

    private NoPositionChangeReportData initReport(String caseTypeId) {
        NoPositionChangeReportSummary reportSummary = new NoPositionChangeReportSummary(caseTypeId);
        return new NoPositionChangeReportData(reportSummary, reportDate);
    }

    private List<NoPositionChangeSubmitEvent> getCases(String caseTypeId, String managingOffice) {
        return noPositionChangeDataSource.getData(
                UtilHelper.getListingCaseTypeId(caseTypeId), reportDate, managingOffice);
    }

    private List<SubmitMultipleEvent> getMultipleCases(String casTypeId, List<String> multipleCaseIds) {
        String multipleCaseTypeId = UtilHelper.getListingCaseTypeId(casTypeId) + "_Multiple";
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

        for (NoPositionChangeSubmitEvent submitEvent : submitEvents) {
            NoPositionChangeCaseData caseData = submitEvent.getCaseData();
            if (!isValidCase(caseData)) {
                continue;
            }

            if (caseData.getCaseType().equals(SINGLE_CASE_TYPE)) {
                NoPositionChangeReportDetailSingle reportDetailSingle = new NoPositionChangeReportDetailSingle();
                reportDetailSingle.setCaseReference(caseData.getEthosCaseReference());
                reportDetailSingle.setCurrentPosition(caseData.getCurrentPosition());
                reportDetailSingle.setDateToPosition(caseData.getDateToPosition());

                int year = LocalDate.parse(caseData.getReceiptDate(), OLD_DATE_TIME_PATTERN2).getYear();
                reportDetailSingle.setYear(String.valueOf(year));

                boolean hasMultipleRespondents = CollectionUtils.isNotEmpty(caseData.getRespondentCollection())
                        && caseData.getRespondentCollection().size() > 1;
                String respondent = caseData.getRespondent() + (hasMultipleRespondents ? " & Others" : "");
                reportDetailSingle.setRespondent(respondent);

                reportData.addReportDetailsSingle(reportDetailSingle);
            } else if (caseData.getCaseType().equals(MULTIPLE_CASE_TYPE)) {
                Optional<SubmitMultipleEvent> multipleCase = submitMultipleEvents.stream()
                        .filter(sme -> sme.getCaseData().getMultipleReference().equals(caseData.getMultipleReference()))
                        .findFirst();

                NoPositionChangeReportDetailMultiple reportDetailMultiple = new NoPositionChangeReportDetailMultiple();
                reportDetailMultiple.setCaseReference(caseData.getEthosCaseReference());
                reportDetailMultiple.setCurrentPosition(caseData.getCurrentPosition());
                reportDetailMultiple.setDateToPosition(caseData.getDateToPosition());
                reportDetailMultiple.setMultipleName(multipleCase.isPresent()
                        ? multipleCase.get().getCaseData().getMultipleName() : "");

                int year = LocalDate.parse(caseData.getReceiptDate(), OLD_DATE_TIME_PATTERN2).getYear();
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

        LocalDate reportDateMinus3Months = LocalDate.parse(reportDate, OLD_DATE_TIME_PATTERN2)
            .minusMonths(3).plusDays(1);
        LocalDate dateToPosition = LocalDate.parse(caseData.getDateToPosition(), OLD_DATE_TIME_PATTERN2);
        return dateToPosition.isBefore(reportDateMinus3Months);
    }
}
