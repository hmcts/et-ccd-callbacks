package uk.gov.hmcts.ethos.replacement.docmosis.reports.casesawaitingjudgment;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.ecm.common.model.helper.Constants;
import uk.gov.hmcts.ecm.common.model.reports.casesawaitingjudgment.CaseData;
import uk.gov.hmcts.ecm.common.model.reports.casesawaitingjudgment.CasesAwaitingJudgmentSubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DateListedType;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;
import uk.gov.hmcts.et.common.model.listing.ListingDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReportHelper;

import java.time.Clock;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLOSED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_HEARD;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MULTIPLE_CASE_TYPE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.OLD_DATE_TIME_PATTERN;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.OLD_DATE_TIME_PATTERN2;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.casesawaitingjudgment.ReportDetail.NO_MULTIPLE_REFERENCE;

@Slf4j
@SuppressWarnings({"PMD.AvoidInstantiatingObjectsInLoops", "PMD.InsufficientStringBufferDeclaration",
    "PMD.FieldNamingConventions", "PMD.LawOfDemeter", "PMD.ExcessiveImports"})
public final class CasesAwaitingJudgmentReport {

    static final Collection<String> VALID_POSITION_TYPES = List.of(
            "Draft with members",
            "Heard awaiting judgment being sent to the parties",
            "Awaiting judgment being sent to the parties, other",
            "Awaiting chairman's notes of evidence",
            "Awaiting draft judgment from chairman",
            "Draft judgment received, awaiting typing",
            "Draft judgment typed, to chairman for amendment",
            "Revised draft received, awaiting typing",
            "Fair copy, to chairman for signature",
            "Signed fair copy received",
            "Judgment photocopied, awaiting being sent to the parties",
            "Awaiting written reasons"
    );

    private final ReportDataSource reportDataSource;
    private final Clock clock;

    static class HeardHearing {
        String listedDate;
        String hearingNumber;
        String hearingType;
        String judge;
    }

    public CasesAwaitingJudgmentReport(ReportDataSource reportDataSource) {
        this(reportDataSource, Clock.systemDefaultZone());
    }

    public CasesAwaitingJudgmentReport(ReportDataSource reportDataSource, Clock clock) {
        this.reportDataSource = reportDataSource;
        this.clock = clock;
    }

    public CasesAwaitingJudgmentReportData runReport(ListingDetails listingDetails) {
        String managingOffice = listingDetails.getCaseData().getManagingOffice();
        String caseTypeId = listingDetails.getCaseTypeId();
        List<CasesAwaitingJudgmentSubmitEvent> submitEvents = getCases(caseTypeId, managingOffice);

        String reportOffice = ReportHelper.getReportOffice(caseTypeId, managingOffice);
        CasesAwaitingJudgmentReportData reportData = initReport(reportOffice);

        populateData(reportData, submitEvents);

        return reportData;
    }

    private CasesAwaitingJudgmentReportData initReport(String owningOffice) {
        ReportSummary reportSummary = new ReportSummary(owningOffice);
        return new CasesAwaitingJudgmentReportData(reportSummary);
    }

    private List<CasesAwaitingJudgmentSubmitEvent> getCases(String caseTypeId, String owningOffice) {
        return reportDataSource.getData(UtilHelper.getListingCaseTypeId(caseTypeId), owningOffice);
    }

    private void populateData(CasesAwaitingJudgmentReportData reportData,
                              List<CasesAwaitingJudgmentSubmitEvent> submitEvents) {
        for (CasesAwaitingJudgmentSubmitEvent submitEvent : submitEvents) {
            if (!isValidCase(submitEvent)) {
                continue;
            }

            ReportDetail reportDetail = new ReportDetail();
            CaseData caseData = submitEvent.getCaseData();
            log.info("Adding case {} to Cases Awaiting Judgment report", caseData.getEthosCaseReference());

            HeardHearing heardHearing = getLatestHeardHearing(caseData.getHearingCollection());
            LocalDate today = LocalDate.now(clock);
            LocalDate listedDate = LocalDate.parse(heardHearing.listedDate, OLD_DATE_TIME_PATTERN);

            reportDetail.setPositionType(caseData.getPositionType());
            reportDetail.setWeeksSinceHearing(getWeeksSinceHearing(listedDate, today));
            reportDetail.setDaysSinceHearing(getDaysSinceHearing(listedDate, today));
            reportDetail.setCaseNumber(caseData.getEthosCaseReference());
            if (MULTIPLE_CASE_TYPE.equals(caseData.getEcmCaseType())) {
                reportDetail.setMultipleReference(caseData.getMultipleReference());
            } else {
                reportDetail.setMultipleReference(NO_MULTIPLE_REFERENCE);
            }

            reportDetail.setHearingNumber(heardHearing.hearingNumber);
            reportDetail.setHearingType(heardHearing.hearingType);
            reportDetail.setLastHeardHearingDate(formatDate(OLD_DATE_TIME_PATTERN, heardHearing.listedDate));
            reportDetail.setJudge(heardHearing.judge);
            reportDetail.setCurrentPosition(caseData.getCurrentPosition());
            reportDetail.setDateToPosition(formatDate(OLD_DATE_TIME_PATTERN2, caseData.getDateToPosition()));
            reportDetail.setConciliationTrack(caseData.getConciliationTrack());

            reportData.addReportDetail(reportDetail);
        }

        sortReportDetails(reportData);
        addReportSummary(reportData);
    }

    private boolean isValidCase(CasesAwaitingJudgmentSubmitEvent submitEvent) {
        if (CLOSED_STATE.equals(submitEvent.getState())) {
            return false;
        }

        CaseData caseData = submitEvent.getCaseData();
        if (!isValidPositionType(caseData.getPositionType())) {
            return false;
        }

        if (!isCaseWithValidHearing(caseData)) {
            return false;
        }

        return isCaseAwaitingJudgment(caseData);
    }

    private boolean isValidPositionType(String positionType) {
        return StringUtils.isNotBlank(positionType) && VALID_POSITION_TYPES.contains(positionType);
    }

    private boolean isCaseWithValidHearing(CaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getHearingCollection())) {
            return false;
        }

        for (HearingTypeItem hearingTypeItem : caseData.getHearingCollection()) {
            if (isValidHearing(hearingTypeItem)) {
                return true;
            }

        }

        return false;
    }

    private boolean isValidHearing(HearingTypeItem hearingTypeItem) {
        HearingType hearingType = hearingTypeItem.getValue();
        if (hearingType == null || CollectionUtils.isEmpty(hearingType.getHearingDateCollection())) {
            return false;
        }

        for (DateListedTypeItem dateListedItemType : hearingType.getHearingDateCollection()) {
            if (Constants.HEARING_STATUS_HEARD.equals(dateListedItemType.getValue().getHearingStatus())) {
                return true;
            }
        }

        return false;
    }

    private boolean isCaseAwaitingJudgment(CaseData caseData) {
        return CollectionUtils.isEmpty(caseData.getJudgementCollection());
    }

    private void sortReportDetails(CasesAwaitingJudgmentReportData reportData) {
        Comparator<ReportDetail> comparator = Comparator.comparingLong(ReportDetail::getDaysSinceHearing).reversed();
        reportData.getReportDetails().sort(comparator);
    }

    private void addReportSummary(CasesAwaitingJudgmentReportData reportData) {
        HashMap<String, Integer> positionTypeCounts = new HashMap<>();
        reportData.getReportDetails().forEach(rd -> positionTypeCounts.merge(rd.getPositionType(), 1,
            Integer::sum));

        List<PositionTypeSummary> positionTypes = reportData.getReportSummary().getPositionTypes();
        positionTypeCounts.forEach((k, v) -> positionTypes.add(new PositionTypeSummary(k, v)));

        Comparator<PositionTypeSummary> comparator = Comparator.comparingInt(PositionTypeSummary::getPositionTypeCount);
        positionTypes.sort(comparator);
    }

    private HeardHearing getLatestHeardHearing(List<HearingTypeItem> hearings) {
        ArrayList<HeardHearing> heardHearings = new ArrayList<>();
        for (HearingTypeItem hearingTypeItem : hearings) {
            HearingType hearingType = hearingTypeItem.getValue();
            for (DateListedTypeItem dateListedTypeItem : hearingType.getHearingDateCollection()) {
                DateListedType dateListedType = dateListedTypeItem.getValue();
                if (HEARING_STATUS_HEARD.equals(dateListedType.getHearingStatus())) {
                    HeardHearing heardHearing = new HeardHearing();
                    heardHearing.listedDate = dateListedType.getListedDate();
                    heardHearing.hearingNumber = hearingType.getHearingNumber();
                    heardHearing.hearingType = hearingType.getHearingType();
                    if (hearingType.hasHearingJudge()) {
                        heardHearing.judge = hearingType.getJudge().getSelectedLabel();
                    }

                    heardHearings.add(heardHearing);
                }
            }
        }

        return Collections.max(heardHearings, Comparator.comparing(h -> h.listedDate));
    }

    private long getWeeksSinceHearing(LocalDate listedDate, LocalDate today) {
        return ChronoUnit.WEEKS.between(listedDate, today);
    }

    private long getDaysSinceHearing(LocalDate listedDate, LocalDate today) {
        return ChronoUnit.DAYS.between(listedDate, today);
    }

    private String formatDate(DateTimeFormatter sourceFormatter, String sourceDate) {
        if (StringUtils.isBlank(sourceDate)) {
            return sourceDate;
        }
        try {
            TemporalAccessor date = sourceFormatter.parse(sourceDate);
            DateTimeFormatter targetFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return targetFormatter.format(date);
        } catch (DateTimeException e) {
            log.warn(String.format("Unable to parse %s", sourceDate), e);
            return sourceDate;
        }
    }
}
