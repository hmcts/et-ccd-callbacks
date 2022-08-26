package uk.gov.hmcts.ethos.replacement.docmosis.reports.sessiondays;

import org.apache.commons.collections4.CollectionUtils;
import org.elasticsearch.common.Strings;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ecm.common.model.reports.sessiondays.SessionDaysCaseData;
import uk.gov.hmcts.ecm.common.model.reports.sessiondays.SessionDaysSubmitEvent;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DateListedType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.Judge;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.JudgeEmploymentStatus;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReportHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.ReportParams;
import uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.JudgeService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.Math.round;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_HEARD;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.OLD_DATE_TIME_PATTERN;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.ReportCommonMethods.getHearingDurationInMinutes;

@SuppressWarnings({"PMD.ConfusingTernary", "PDM.CyclomaticComplexity", "PMD.AvoidInstantiatingObjectsInLoops",
    "PMD.GodClass", "PMD.CognitiveComplexity", "PMD.InsufficientStringBufferDeclaration",
    "PMD.LiteralsFirstInComparisons", "PMD.UnnecessaryFullyQualifiedName", "PMD.LawOfDemeter"})
public final class SessionDaysReport {

    public static final String ONE_HOUR = "One Hour";
    public static final String HALF_DAY = "Half Day";
    public static final String FULL_DAY = "Full Day";
    public static final String NONE = "None";
    private static final int ZERO = 0;
    private static final int SIXTY = 60;
    private static final int HUNDRED_EIGHTY = 180;
    private final SessionDaysReportDataSource reportDataSource;
    private final JudgeService judgeService;
    private ReportParams params;

    public SessionDaysReport(SessionDaysReportDataSource reportDataSource, JudgeService judgeService) {
        this.reportDataSource = reportDataSource;
        this.judgeService = judgeService;
    }

    public SessionDaysReportData generateReport(ReportParams params) {
        this.params = params;
        var submitEvents = getCases();
        var reportData = initReport();
        if (CollectionUtils.isNotEmpty(submitEvents)) {
            executeReport(submitEvents, reportData);
        }
        return reportData;
    }

    private SessionDaysReportData initReport() {
        var reportSummary = new SessionDaysReportSummary(
                ReportHelper.getReportOffice(params.getCaseTypeId(), params.getManagingOffice()));
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
        return reportDataSource.getData(UtilHelper.getListingCaseTypeId(params.getCaseTypeId()),
                params.getManagingOffice(), params.getDateFrom(), params.getDateTo());
    }

    private void executeReport(List<SessionDaysSubmitEvent> submitEvents, SessionDaysReportData sessionDaysReportData) {
        setReportData(submitEvents, sessionDaysReportData);
    }

    private boolean sessionExists(String judgeName, String date, List<List<String>> sessionsList) {
        String dateFormatted = LocalDateTime.parse(date, OLD_DATE_TIME_PATTERN).toLocalDate().toString();
        if (!Strings.isNullOrEmpty(judgeName) && !Strings.isNullOrEmpty(dateFormatted)) {
            List<String> judgeDate = List.of(judgeName, dateFormatted);
            if (sessionsList.contains(judgeDate)) {
                return true;
            } else {
                sessionsList.add(judgeDate);
                return false;
            }
        }
        return true;
    }

    private void setReportData(List<SessionDaysSubmitEvent> submitEvents, SessionDaysReportData reportData) {
        List<SessionDaysReportSummary2> sessionDaysReportSummary2List = new ArrayList<>();
        List<SessionDaysReportDetail> sessionDaysReportDetailList = new ArrayList<>();
        List<List<String>> sessionsList = new ArrayList<>();
        for (SessionDaysSubmitEvent submitEvent : submitEvents) {
            var caseData = submitEvent.getCaseData();
            setCaseReportSummaries(caseData, reportData.getReportSummary(),
                    sessionDaysReportSummary2List, sessionsList);
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
                                        List<SessionDaysReportSummary2> sessionDaysReportSummary2List,
                                        List<List<String>> sessionsList) {
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
                        if (!sessionExists(judgeName, dateListedTypeItem.getValue().getListedDate(), sessionsList)) {
                            setReportSummariesFields(judgeStatus, reportSummary, reportSummary2);
                        }
                    }
                }
            }
        }
    }

    private void setReportSummariesFields(JudgeEmploymentStatus judgeStatus, SessionDaysReportSummary reportSummary,
                                          SessionDaysReportSummary2 reportSummary2) {
        int ft;
        int pt;
        int ot;
        int ft2;
        int pt2;
        int ot2;
        int total2;
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

    private JudgeEmploymentStatus getJudgeStatus(String judgeName) {
        List<Judge> judges = new ArrayList<>();
        if (!isNullOrEmpty(params.getManagingOffice())
                && TribunalOffice.isEnglandWalesOffice(params.getManagingOffice())) {
            judges = judgeService.getJudges(TribunalOffice.valueOfOfficeName(params.getManagingOffice()));
        } else {
            for (var office : TribunalOffice.SCOTLAND_OFFICES) {
                judges.addAll(judgeService.getJudges(office));
            }
        }
        if (CollectionUtils.isNotEmpty(judges)) {
            Optional<Judge> judge = judges.stream().filter(n -> n.getName().equals(judgeName)).findFirst();
            if (judge.isPresent()) {
                return judge.get().getEmploymentStatus();
            }
        }
        return null;
    }

    private boolean isHearingStatusValid(DateListedTypeItem dateListedTypeItem) {
        return HEARING_STATUS_HEARD.equals(dateListedTypeItem.getValue().getHearingStatus());
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
                        setJudgeType(judgeStatus, reportDetail);
                        reportDetail.setCaseReference(caseData.getEthosCaseReference());
                        reportDetail.setHearingNumber(hearingTypeItem.getValue().getHearingNumber());
                        reportDetail.setHearingType(hearingTypeItem.getValue().getHearingType());
                        reportDetail.setHearingSitAlone("Sit Alone".equals(
                                hearingTypeItem.getValue().getHearingSitAlone()) ? "Y" : "");
                        setTelCon(hearingTypeItem, reportDetail);
                        String duration = getHearingDurationInMinutes(dateListedTypeItem);
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

    private void setJudgeType(JudgeEmploymentStatus judgeStatus, SessionDaysReportDetail reportDetail) {
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

    private String getSessionType(long duration) {
        if (duration > ZERO && duration < SIXTY) {
            return ONE_HOUR;
        } else if (duration >= SIXTY && duration <= HUNDRED_EIGHTY) {
            return HALF_DAY;
        } else if (duration > HUNDRED_EIGHTY) {
            return FULL_DAY;
        } else {
            return NONE;
        }
    }

}
