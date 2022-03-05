package uk.gov.hmcts.ethos.replacement.docmosis.reports.sessiondays;

import com.microsoft.azure.servicebus.primitives.StringUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.elasticsearch.common.Strings;
import uk.gov.hmcts.ecm.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.ecm.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.ecm.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.ecm.common.model.ccd.types.DateListedType;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ecm.common.model.reports.sessiondays.SessionDaysCaseData;
import uk.gov.hmcts.ecm.common.model.reports.sessiondays.SessionDaysSubmitEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.Judge;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.JudgeEmploymentStatus;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReportHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.JudgeService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.Math.round;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_HEARD;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.OLD_DATE_TIME_PATTERN;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.memberdays.MemberDaysReport.OLD_DATE_TIME_PATTERN3;

public class SessionDaysReport {
    public static final String ONE_HOUR = "One Hour";
    public static final String HALF_DAY = "Half Day";
    public static final String FULL_DAY = "Full Day";
    public static final String NONE = "None";

    private final SessionDaysReportDataSource reportDataSource;
    private final JudgeService judgeService;
    private SessionDaysReportParams params;

    public SessionDaysReport(SessionDaysReportDataSource reportDataSource, JudgeService judgeService) {
        this.reportDataSource = reportDataSource;
        this.judgeService = judgeService;
    }

    public SessionDaysReportData generateReport(SessionDaysReportParams params) {
        this.params = params;
        var submitEvents = getCases();
        var reportData = initReport();
        if (CollectionUtils.isNotEmpty(submitEvents)) {
            executeReport(submitEvents, reportData);
        }
        return reportData;
    }

    private SessionDaysReportData initReport() {
        var reportSummary = new SessionDaysReportSummary(params.getManagingOffice());
        reportSummary.setFtSessionDaysTotal("0");
        reportSummary.setPtSessionDaysTotal("0");
        reportSummary.setOtherSessionDaysTotal("0");
        reportSummary.setSessionDaysTotal("0");
        reportSummary.setPtSessionDaysPerCent("0.0");
        return new SessionDaysReportData(reportSummary);
    }

    public List<DateListedTypeItem> filterValidHearingDates(List<DateListedTypeItem> dateListedTypeItems) {
        return dateListedTypeItems.stream()
                .filter(x -> isHearingDateInRange(x.getValue().getListedDate()))
                .collect(Collectors.toList());
    }

    private boolean isHearingDateInRange(String dateListed) {
        var hearingListedDate = LocalDate.parse(ReportHelper.getFormattedLocalDate(dateListed));
        var hearingDatesFrom = LocalDate.parse(ReportHelper.getFormattedLocalDate(params.getDateFrom()));
        var hearingDatesTo = LocalDate.parse(ReportHelper.getFormattedLocalDate(params.getDateTo()));
        return  (hearingListedDate.isEqual(hearingDatesFrom) ||  hearingListedDate.isAfter(hearingDatesFrom))
                && (hearingListedDate.isEqual(hearingDatesTo) || hearingListedDate.isBefore(hearingDatesTo));
    }

    private void initReportSummary2(SessionDaysReportSummary2 reportSummary2) {
        reportSummary2.setPtSessionDays("0");
        reportSummary2.setOtherSessionDays("0");
        reportSummary2.setFtSessionDays("0");
        reportSummary2.setSessionDaysTotalDetail("0");
    }

    private List<SessionDaysSubmitEvent> getCases() {
        return reportDataSource.getData(params.getCaseTypeId(), params.getDateFrom(), params.getDateTo());
    }

    private void executeReport(List<SessionDaysSubmitEvent> submitEvents, SessionDaysReportData sessionDaysReportData) {
        setReportData(submitEvents, sessionDaysReportData);
    }

    private void setReportData(List<SessionDaysSubmitEvent> submitEvents, SessionDaysReportData reportData) {
        List<SessionDaysReportSummary2> sessionDaysReportSummary2List = new ArrayList<>();
        List<SessionDaysReportDetail> sessionDaysReportDetailList = new ArrayList<>();
        for (SessionDaysSubmitEvent submitEvent : submitEvents) {
            var caseData = submitEvent.getCaseData();
            setCaseReportSummaries(caseData, reportData.getReportSummary(), sessionDaysReportSummary2List);
            setReportDetail(caseData, sessionDaysReportDetailList);
        }
        sessionDaysReportSummary2List.sort(Comparator.comparing(SessionDaysReportSummary2::getDate));
        sessionDaysReportDetailList.sort(Comparator.comparing(SessionDaysReportDetail::getHearingDate));
        int ft = Integer.parseInt(reportData.getReportSummary().getFtSessionDaysTotal());
        int pt = Integer.parseInt(reportData.getReportSummary().getPtSessionDaysTotal());
        int ot = Integer.parseInt(reportData.getReportSummary().getOtherSessionDaysTotal());
        int total = ft + pt + ot;
        long ptPercent = total > 0 ? round(((double)pt * 100) / total) : 0;
        reportData.getReportSummary().setSessionDaysTotal(String.valueOf(total));
        reportData.getReportSummary().setPtSessionDaysPerCent(String.valueOf(ptPercent));
        reportData.addReportSummary2List(sessionDaysReportSummary2List);
        reportData.addReportDetail(sessionDaysReportDetailList);
    }

    private boolean areDatesEqual(String date1, String date2) {
        var date2Formatted =  LocalDateTime.parse(date2, OLD_DATE_TIME_PATTERN).toLocalDate().toString();
        return date1.equals(date2Formatted);

    }

    private SessionDaysReportSummary2 getReportSummary2Item(
            DateListedType dateListedType, List<SessionDaysReportSummary2> sessionDaysReportSummary2List) {
        Optional<SessionDaysReportSummary2> item = sessionDaysReportSummary2List.stream()
                .filter(i -> !Strings.isNullOrEmpty(i.getDate())
                && areDatesEqual(i.getDate(), dateListedType.getListedDate())).findFirst();
        if (item.isPresent()) {
            return item.get();
        }
        SessionDaysReportSummary2 summary2 = new SessionDaysReportSummary2();
        initReportSummary2(summary2);

        summary2.setDate(LocalDateTime.parse(
                dateListedType.getListedDate(), OLD_DATE_TIME_PATTERN).toLocalDate().toString());
        sessionDaysReportSummary2List.add(summary2);
        return summary2;
    }

    private void setCaseReportSummaries(SessionDaysCaseData caseData,
                                        SessionDaysReportSummary reportSummary,
                                        List<SessionDaysReportSummary2> sessionDaysReportSummary2List) {
        int ft;
        int pt;
        int ot;
        int ft2 = 0;
        int pt2 = 0;
        int ot2 = 0;
        int total2 = 0;
        for (HearingTypeItem hearingTypeItem : getHearings(caseData)) {
            var dates = hearingTypeItem.getValue().getHearingDateCollection();
            dates = filterValidHearingDates(dates);
            if (CollectionUtils.isNotEmpty(dates)) {
                for (DateListedTypeItem dateListedTypeItem : dates) {
                    if (isHearingStatusValid(dateListedTypeItem)) {
                        String judgeName = getJudgeName(hearingTypeItem.getValue().getJudge());
                        JudgeEmploymentStatus judgeStatus = getJudgeStatus(judgeName);
                        SessionDaysReportSummary2 reportSummary2 = getReportSummary2Item(
                                dateListedTypeItem.getValue(), sessionDaysReportSummary2List);
                        if (judgeStatus != null) {
                            switch (judgeStatus) {
                                case SALARIED:
                                    ft = Integer.parseInt(reportSummary.getFtSessionDaysTotal()) + 1;
                                    reportSummary.setFtSessionDaysTotal(String.valueOf(ft));
                                    ft2 = Integer.parseInt(reportSummary2.getFtSessionDays()) + 1;
                                    reportSummary2.setFtSessionDays(String.valueOf(ft2));
                                    total2 = Integer.parseInt(reportSummary2.getSessionDaysTotalDetail()) + 1;
                                    reportSummary2.setSessionDaysTotalDetail(String.valueOf(total2));
                                    break;
                                case FEE_PAID:
                                    pt = Integer.parseInt(reportSummary.getPtSessionDaysTotal()) + 1;
                                    reportSummary.setPtSessionDaysTotal(String.valueOf(pt));
                                    pt2 = Integer.parseInt(reportSummary2.getPtSessionDays()) + 1;
                                    reportSummary2.setPtSessionDays(String.valueOf(pt2));
                                    total2 = Integer.parseInt(reportSummary2.getSessionDaysTotalDetail()) + 1;
                                    reportSummary2.setSessionDaysTotalDetail(String.valueOf(total2));
                                    break;
                                case UNKNOWN:
                                    ot = Integer.parseInt(reportSummary.getOtherSessionDaysTotal()) + 1;
                                    reportSummary.setOtherSessionDaysTotal(String.valueOf(ot));
                                    ot2 = Integer.parseInt(reportSummary2.getOtherSessionDays()) + 1;
                                    reportSummary2.setOtherSessionDays(String.valueOf(ot2));
                                    total2 = Integer.parseInt(reportSummary2.getSessionDaysTotalDetail()) + 1;
                                    reportSummary2.setSessionDaysTotalDetail(String.valueOf(total2));
                                    break;
                                default:
                                    break;
                            }
                        } else {
                            ot = Integer.parseInt(reportSummary.getOtherSessionDaysTotal()) + 1;
                            reportSummary.setOtherSessionDaysTotal(String.valueOf(ot));
                            ot2 = Integer.parseInt(reportSummary2.getOtherSessionDays()) + 1;
                            reportSummary2.setOtherSessionDays(String.valueOf(ot2));
                            total2 = Integer.parseInt(reportSummary2.getSessionDaysTotalDetail()) + 1;
                            reportSummary2.setSessionDaysTotalDetail(String.valueOf(total2));
                        }
                    }
                }
            }
        }
    }

    private boolean isHearingStatusValid(DateListedTypeItem dateListedTypeItem) {
        return HEARING_STATUS_HEARD.equals(dateListedTypeItem.getValue().getHearingStatus());
    }

    private JudgeEmploymentStatus getJudgeStatus(String judgeName) {
        List<Judge> judges = judgeService.getJudges(TribunalOffice.valueOfOfficeName(params.getManagingOffice()));
        if (CollectionUtils.isNotEmpty(judges)) {
            Optional<Judge> judge = judges.stream().filter(n -> n.getName().equals(judgeName)).findFirst();
            if (judge.isPresent()) {
                return judge.get().getEmploymentStatus();
            }
        }
        return null;
    }

    private List<HearingTypeItem> getHearings(SessionDaysCaseData caseData) {
        var hearings = caseData.getHearingCollection();
        if (hearings == null) {
            return Collections.emptyList();
        }
        return hearings;
    }

    private void setReportDetail(SessionDaysCaseData caseData, List<SessionDaysReportDetail> reportDetailList) {
        for (HearingTypeItem hearingTypeItem : getHearings(caseData)) {
            var dates = hearingTypeItem.getValue().getHearingDateCollection();
            dates = filterValidHearingDates(dates);
            if (CollectionUtils.isNotEmpty(dates)) {
                for (DateListedTypeItem dateListedTypeItem : dates) {
                    if (isHearingStatusValid(dateListedTypeItem)) {
                        SessionDaysReportDetail reportDetail = new SessionDaysReportDetail();
                        reportDetail.setHearingDate(LocalDateTime.parse(
                                dateListedTypeItem.getValue().getListedDate(), OLD_DATE_TIME_PATTERN)
                                .toLocalDate().toString());

                        String judgeName = getJudgeName(hearingTypeItem.getValue().getJudge());

                        reportDetail.setHearingJudge(
                                Strings.isNullOrEmpty(judgeName)
                                        ? "* Not Allocated" : judgeName);
                        JudgeEmploymentStatus judgeStatus = getJudgeStatus(judgeName);

                        if (judgeStatus != null) {
                            switch (judgeStatus) {
                                case SALARIED:
                                    reportDetail.setJudgeType("FTC");
                                    break;
                                case FEE_PAID:
                                    reportDetail.setJudgeType("PTC");
                                    break;
                                case UNKNOWN:
                                    reportDetail.setJudgeType("UNKNOWN");
                                    break;
                                default:
                                    break;
                            }
                        }
                        reportDetail.setCaseReference(caseData.getEthosCaseReference());
                        reportDetail.setHearingNumber(hearingTypeItem.getValue().getHearingNumber());
                        reportDetail.setHearingType(hearingTypeItem.getValue().getHearingType());
                        reportDetail.setHearingSitAlone("Sit Alone".equals(
                                hearingTypeItem.getValue().getHearingSitAlone()) ? "Y" : "");
                        setTelCon(hearingTypeItem, reportDetail);
                        String duration = calculateDuration(dateListedTypeItem);
                        reportDetail.setHearingDuration(duration);
                        reportDetail.setSessionType(getSessionType(Long.parseLong(duration)));
                        if (dateListedTypeItem.getValue().hasHearingClerk()) {
                            reportDetail.setHearingClerk(dateListedTypeItem.getValue().getHearingClerk()
                                    .getSelectedLabel());
                        }
                        reportDetailList.add(reportDetail);
                    }
                }
            }
        }
    }

    private String getJudgeName(DynamicFixedListType dynamicFixedListType) {
        if (dynamicFixedListType != null) {
            return dynamicFixedListType.getSelectedLabel();
        } else {
            return null;
        }
    }

    private void setTelCon(HearingTypeItem hearingTypeItem, SessionDaysReportDetail reportDetail) {
        var telConf = CollectionUtils.isNotEmpty(hearingTypeItem.getValue().getHearingFormat())
                && hearingTypeItem.getValue().getHearingFormat().contains("Telephone") ? "Y" : "";
        reportDetail.setHearingTelConf(telConf);

    }

    private LocalDateTime convertHearingTime(String dateToConvert) {
        return dateToConvert.endsWith(".000")
                ? LocalDateTime.parse(dateToConvert, OLD_DATE_TIME_PATTERN)
                : LocalDateTime.parse(dateToConvert, OLD_DATE_TIME_PATTERN3);
    }

    private String calculateDuration(DateListedTypeItem c) {
        var dateListedType = c.getValue();
        long duration = 0;
        long breakDuration = 0;

        var hearingTimingBreak = dateListedType.getHearingTimingBreak();
        var hearingTimingResume = dateListedType.getHearingTimingResume();
        //If there was a break and resumption during the hearing
        if (!StringUtil.isNullOrEmpty(hearingTimingBreak)
                && !StringUtil.isNullOrEmpty(hearingTimingResume)) {
            var hearingBreak = convertHearingTime(hearingTimingBreak);
            var hearingResume = convertHearingTime(hearingTimingResume);
            breakDuration = ChronoUnit.MINUTES.between(hearingBreak, hearingResume);
        }

        var hearingTimingStart = dateListedType.getHearingTimingStart();
        var hearingTimingFinish = dateListedType.getHearingTimingFinish();
        if (!StringUtil.isNullOrEmpty(hearingTimingStart)
                && !StringUtil.isNullOrEmpty(hearingTimingFinish)) {
            var hearingStartTime = convertHearingTime(hearingTimingStart);
            var hearingEndTime = convertHearingTime(hearingTimingFinish);
            long startToEndDiffInMinutes = ChronoUnit.MINUTES.between(hearingStartTime, hearingEndTime);
            duration = startToEndDiffInMinutes - breakDuration;
        }

        return String.valueOf(duration);
    }

    private String getSessionType(long duration) {
        if (duration > 0 && duration < 60) {
            return ONE_HOUR;
        } else if (duration >= 60 && duration <= 180) {
            return HALF_DAY;
        } else if (duration > 180) {
            return FULL_DAY;
        } else {
            return NONE;
        }
    }

}
