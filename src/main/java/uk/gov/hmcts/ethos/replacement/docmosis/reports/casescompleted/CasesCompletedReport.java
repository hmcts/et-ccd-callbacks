package uk.gov.hmcts.ethos.replacement.docmosis.reports.casescompleted;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.items.JurCodesTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DateListedType;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;
import uk.gov.hmcts.et.common.model.ccd.types.JurCodesType;
import uk.gov.hmcts.et.common.model.listing.ListingData;
import uk.gov.hmcts.et.common.model.listing.ListingDetails;
import uk.gov.hmcts.et.common.model.listing.items.AdhocReportTypeItem;
import uk.gov.hmcts.et.common.model.listing.types.AdhocReportType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReportHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLOSED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CONCILIATION_TRACK_FAST_TRACK;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CONCILIATION_TRACK_NO_CONCILIATION;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CONCILIATION_TRACK_NUMBER_FOUR;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CONCILIATION_TRACK_NUMBER_ONE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CONCILIATION_TRACK_NUMBER_THREE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CONCILIATION_TRACK_NUMBER_TWO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CONCILIATION_TRACK_OPEN_TRACK;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CONCILIATION_TRACK_STANDARD_TRACK;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_TYPE_JUDICIAL_HEARING;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_TYPE_PERLIMINARY_HEARING;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_TYPE_PERLIMINARY_HEARING_CM;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_TYPE_PERLIMINARY_HEARING_CM_TCC;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.JURISDICTION_OUTCOME_DISMISSED_AT_HEARING;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.JURISDICTION_OUTCOME_SUCCESSFUL_AT_HEARING;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.JURISDICTION_OUTCOME_UNSUCCESSFUL_AT_HEARING;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.POSITION_TYPE_CASE_INPUT_IN_ERROR;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.POSITION_TYPE_CASE_TRANSFERRED_OTHER_COUNTRY;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.POSITION_TYPE_CASE_TRANSFERRED_SAME_COUNTRY;

@Service
@Slf4j
@SuppressWarnings({"PMD.ConfusingTernary", "PMD.ExcessiveImports"})
public class CasesCompletedReport {
    static final String ZERO = "0";
    static final String ZERO_DECIMAL = "0.00";
    static final String COMPLETED_PER_SESSION_FORMAT = "%.2f";

    static final List<String> INVALID_POSITION_TYPES = Arrays.asList(
            POSITION_TYPE_CASE_INPUT_IN_ERROR,
            POSITION_TYPE_CASE_TRANSFERRED_SAME_COUNTRY,
            POSITION_TYPE_CASE_TRANSFERRED_OTHER_COUNTRY);

    static final List<String> VALID_JURISDICTION_OUTCOMES = Arrays.asList(
            JURISDICTION_OUTCOME_SUCCESSFUL_AT_HEARING,
            JURISDICTION_OUTCOME_UNSUCCESSFUL_AT_HEARING,
            JURISDICTION_OUTCOME_DISMISSED_AT_HEARING);

    static final List<String> VALID_HEARING_TYPES = Arrays.asList(
            HEARING_TYPE_JUDICIAL_HEARING,
            HEARING_TYPE_PERLIMINARY_HEARING,
            HEARING_TYPE_PERLIMINARY_HEARING_CM,
            HEARING_TYPE_PERLIMINARY_HEARING_CM_TCC);

    public ListingData generateReportData(ListingDetails listingDetails, List<SubmitEvent> submitEvents) {
        initReport(listingDetails);

        if (CollectionUtils.isNotEmpty(submitEvents)) {
            executeReport(listingDetails, submitEvents);
        }

        listingDetails.getCaseData().clearReportFields();
        return listingDetails.getCaseData();
    }

    private void initReport(ListingDetails listingDetails) {
        AdhocReportType adhocReportType = new AdhocReportType();

        adhocReportType.setCasesCompletedHearingTotal(ZERO);
        adhocReportType.setSessionDaysTotal(ZERO);
        adhocReportType.setCompletedPerSessionTotal(ZERO_DECIMAL);
        adhocReportType.setConNoneCasesCompletedHearing(ZERO);
        adhocReportType.setConNoneSessionDays(ZERO);
        adhocReportType.setConNoneCompletedPerSession(ZERO_DECIMAL);
        adhocReportType.setConFastCasesCompletedHearing(ZERO);
        adhocReportType.setConFastSessionDays(ZERO);
        adhocReportType.setConFastCompletedPerSession(ZERO_DECIMAL);
        adhocReportType.setConStdCasesCompletedHearing(ZERO);
        adhocReportType.setConStdSessionDays(ZERO);
        adhocReportType.setConStdCompletedPerSession(ZERO_DECIMAL);
        adhocReportType.setConOpenCasesCompletedHearing(ZERO);
        adhocReportType.setConOpenSessionDays(ZERO);
        adhocReportType.setConOpenCompletedPerSession(ZERO_DECIMAL);

        String managingOffice = listingDetails.getCaseData().getManagingOffice();
        String reportOffice = ReportHelper.getReportOffice(listingDetails.getCaseTypeId(), managingOffice);
        adhocReportType.setReportOffice(reportOffice);

        ListingData listingData = listingDetails.getCaseData();
        listingData.setLocalReportsDetailHdr(adhocReportType);
        listingData.setLocalReportsDetail(new ArrayList<>());
    }

    private void executeReport(ListingDetails listingDetails, List<SubmitEvent> submitEvents) {
        log.info(String.format("Cases Completed report case type id %s search results: %d",
                listingDetails.getCaseTypeId(), submitEvents.size()));

        AdhocReportType localReportsDetailHdr = listingDetails.getCaseData().getLocalReportsDetailHdr();
        List<AdhocReportTypeItem> localReportsDetailList = listingDetails.getCaseData().getLocalReportsDetail();

        for (SubmitEvent submitEvent : submitEvents) {
            if (isValidCaseForCasesCompletedReport(submitEvent)) {
                AdhocReportTypeItem localReportsDetailItem =
                        getCasesCompletedDetailItem(listingDetails, submitEvent.getCaseData());
                if (localReportsDetailItem != null) {
                    updateCasesCompletedDetailHdr(localReportsDetailItem, localReportsDetailHdr);
                    localReportsDetailList.add(localReportsDetailItem);
                }
            }
        }
    }

    private boolean isValidCaseForCasesCompletedReport(SubmitEvent submitEvent) {
        return CLOSED_STATE.equals(submitEvent.getState())
                && isCaseWithHearings(submitEvent.getCaseData())
                && isValidPositionType(submitEvent.getCaseData())
                && isValidJurisdictionOutcome(submitEvent.getCaseData());
    }

    private boolean isValidPositionType(CaseData caseData) {
        if (caseData.getPositionType() != null) {
            return !INVALID_POSITION_TYPES.contains(caseData.getPositionType());
        } else {
            return true;
        }
    }

    private boolean isCaseWithHearings(CaseData caseData) {
        return CollectionUtils.isNotEmpty(caseData.getHearingCollection());
    }

    private boolean isValidJurisdictionOutcome(CaseData caseData) {
        if (CollectionUtils.isNotEmpty(caseData.getJurCodesCollection())) {
            for (JurCodesTypeItem jurCodesTypeItem : caseData.getJurCodesCollection()) {
                JurCodesType jurCodesType = jurCodesTypeItem.getValue();
                if (VALID_JURISDICTION_OUTCOMES.contains(jurCodesType.getJudgmentOutcome())) {
                    return true;
                }
            }
            return false;
        } else {
            return true;
        }
    }

    private HearingSession getLatestDisposedHearingSession(ListingData listingData, CaseData caseData) {
        SessionDays sessionDays = new SessionDays(listingData, caseData);
        return sessionDays.getLatestDisposedHearingSession();
    }

    private AdhocReportTypeItem getCasesCompletedDetailItem(ListingDetails listingDetails, CaseData caseData) {
        HearingSession hearingSession = getLatestDisposedHearingSession(listingDetails.getCaseData(), caseData);
        if (hearingSession != null) {
            AdhocReportType reportDetail = createReportDetail(caseData, hearingSession);
            AdhocReportTypeItem adhocReportTypeItem = new AdhocReportTypeItem();
            adhocReportTypeItem.setValue(reportDetail);
            return adhocReportTypeItem;
        } else {
            return null;
        }
    }

    private AdhocReportType createReportDetail(CaseData caseData, HearingSession hearingSession) {
        DateListedType latestSession = hearingSession.getDateListedType();
        HearingType hearingType = hearingSession.getHearingType();

        AdhocReportType adhocReportType = new AdhocReportType();
        adhocReportType.setCaseReference(caseData.getEthosCaseReference());
        adhocReportType.setPosition(caseData.getPositionType());
        adhocReportType.setConciliationTrack(getConciliationTrack(caseData));
        adhocReportType.setConciliationTrackNo(getConciliationTrackNumber(caseData.getConciliationTrack()));
        adhocReportType.setSessionDays(String.valueOf(hearingSession.getSessionDays()));
        adhocReportType.setHearingNumber(hearingType.getHearingNumber());
        adhocReportType.setHearingDate(latestSession.getListedDate());
        adhocReportType.setHearingType(hearingType.getHearingType());
        if (hearingType.hasHearingJudge()) {
            adhocReportType.setHearingJudge(hearingType.getJudge().getSelectedLabel());
        }
        if (latestSession.hasHearingClerk()) {
            adhocReportType.setHearingClerk(latestSession.getHearingClerk().getSelectedLabel());
        }

        return adhocReportType;
    }

    private String getConciliationTrack(CaseData caseData) {
        return StringUtils.isNotBlank(caseData.getConciliationTrack())
                ? caseData.getConciliationTrack() : CONCILIATION_TRACK_NO_CONCILIATION;
    }

    private String getConciliationTrackNumber(String conciliationTrack) {
        if (CONCILIATION_TRACK_NO_CONCILIATION.equals(conciliationTrack)) {
            return CONCILIATION_TRACK_NUMBER_ONE;
        } else if (CONCILIATION_TRACK_FAST_TRACK.equals(conciliationTrack)) {
            return CONCILIATION_TRACK_NUMBER_TWO;
        } else if (CONCILIATION_TRACK_STANDARD_TRACK.equals(conciliationTrack)) {
            return CONCILIATION_TRACK_NUMBER_THREE;
        } else if (CONCILIATION_TRACK_OPEN_TRACK.equals(conciliationTrack)) {
            return CONCILIATION_TRACK_NUMBER_FOUR;
        } else {
            return CONCILIATION_TRACK_NUMBER_ONE;
        }
    }

    private void updateCasesCompletedDetailHdr(AdhocReportTypeItem localReportsDetailItem,
                                                      AdhocReportType localReportsDetailHdr) {
        AdhocReportType adhocReportType = localReportsDetailItem.getValue();

        switch (adhocReportType.getConciliationTrackNo()) {
            case CONCILIATION_TRACK_NUMBER_ONE:
                handleTrackOne(adhocReportType, localReportsDetailHdr);
                break;
            case CONCILIATION_TRACK_NUMBER_TWO:
                handleTrackTwo(adhocReportType, localReportsDetailHdr);
                break;
            case CONCILIATION_TRACK_NUMBER_THREE:
                handleTrackThree(adhocReportType, localReportsDetailHdr);
                break;
            case CONCILIATION_TRACK_NUMBER_FOUR:
                handleTrackFour(adhocReportType, localReportsDetailHdr);
                break;
            default:
                return;
        }

        updateTotals(adhocReportType, localReportsDetailHdr);
    }

    private void handleTrackOne(AdhocReportType adhocReportType, AdhocReportType localReportsDetailHdr) {
        int completedAtHearingPerTrack = Integer.parseInt(localReportsDetailHdr.getConNoneCasesCompletedHearing());
        int sessionDaysTakenPerTrack = Integer.parseInt(localReportsDetailHdr.getConNoneSessionDays());

        completedAtHearingPerTrack++;
        sessionDaysTakenPerTrack += Integer.parseInt(adhocReportType.getSessionDays());
        double completedPerSessionDayPerTrack = (double)completedAtHearingPerTrack / sessionDaysTakenPerTrack;

        localReportsDetailHdr.setConNoneCasesCompletedHearing(Integer.toString(completedAtHearingPerTrack));
        localReportsDetailHdr.setConNoneSessionDays(Integer.toString(sessionDaysTakenPerTrack));
        localReportsDetailHdr.setConNoneCompletedPerSession(String.format(Locale.ROOT, COMPLETED_PER_SESSION_FORMAT,
                completedPerSessionDayPerTrack));
    }

    private void handleTrackTwo(AdhocReportType adhocReportType, AdhocReportType localReportsDetailHdr) {
        int completedAtHearingPerTrack = Integer.parseInt(localReportsDetailHdr.getConFastCasesCompletedHearing());
        int sessionDaysTakenPerTrack = Integer.parseInt(localReportsDetailHdr.getConFastSessionDays());

        completedAtHearingPerTrack++;
        sessionDaysTakenPerTrack += Integer.parseInt(adhocReportType.getSessionDays());
        double completedPerSessionDayPerTrack = (double)completedAtHearingPerTrack / sessionDaysTakenPerTrack;

        localReportsDetailHdr.setConFastCasesCompletedHearing(Integer.toString(completedAtHearingPerTrack));
        localReportsDetailHdr.setConFastSessionDays(Integer.toString(sessionDaysTakenPerTrack));
        localReportsDetailHdr.setConFastCompletedPerSession(String.format(Locale.ROOT, COMPLETED_PER_SESSION_FORMAT,
                completedPerSessionDayPerTrack));
    }

    private void handleTrackThree(AdhocReportType adhocReportType, AdhocReportType localReportsDetailHdr) {
        int completedAtHearingPerTrack = Integer.parseInt(localReportsDetailHdr.getConStdCasesCompletedHearing());
        int sessionDaysTakenPerTrack = Integer.parseInt(localReportsDetailHdr.getConStdSessionDays());

        completedAtHearingPerTrack++;
        sessionDaysTakenPerTrack += Integer.parseInt(adhocReportType.getSessionDays());
        double completedPerSessionDayPerTrack = (double)completedAtHearingPerTrack / sessionDaysTakenPerTrack;

        localReportsDetailHdr.setConStdCasesCompletedHearing(Integer.toString(completedAtHearingPerTrack));
        localReportsDetailHdr.setConStdSessionDays(Integer.toString(sessionDaysTakenPerTrack));
        localReportsDetailHdr.setConStdCompletedPerSession(String.format(Locale.ROOT, COMPLETED_PER_SESSION_FORMAT,
                completedPerSessionDayPerTrack));
    }

    private void handleTrackFour(AdhocReportType adhocReportType, AdhocReportType localReportsDetailHdr) {
        int completedAtHearingPerTrack = Integer.parseInt(localReportsDetailHdr.getConOpenCasesCompletedHearing());
        int sessionDaysTakenPerTrack = Integer.parseInt(localReportsDetailHdr.getConOpenSessionDays());

        completedAtHearingPerTrack++;
        sessionDaysTakenPerTrack += Integer.parseInt(adhocReportType.getSessionDays());
        double completedPerSessionDayPerTrack = (double)completedAtHearingPerTrack / sessionDaysTakenPerTrack;

        localReportsDetailHdr.setConOpenCasesCompletedHearing(Integer.toString(completedAtHearingPerTrack));
        localReportsDetailHdr.setConOpenSessionDays(Integer.toString(sessionDaysTakenPerTrack));
        localReportsDetailHdr.setConOpenCompletedPerSession(String.format(Locale.ROOT, COMPLETED_PER_SESSION_FORMAT,
                completedPerSessionDayPerTrack));
    }

    private void updateTotals(AdhocReportType adhocReportType, AdhocReportType localReportsDetailHdr) {
        int completedAtHearingTotal = Integer.parseInt(localReportsDetailHdr.getCasesCompletedHearingTotal());
        int sessionDaysTakenTotal = Integer.parseInt(localReportsDetailHdr.getSessionDaysTotal());

        completedAtHearingTotal++;
        sessionDaysTakenTotal += Integer.parseInt(adhocReportType.getSessionDays());
        double completedPerSessionDayTotal = (double)completedAtHearingTotal / sessionDaysTakenTotal;

        localReportsDetailHdr.setCasesCompletedHearingTotal(Integer.toString(completedAtHearingTotal));
        localReportsDetailHdr.setSessionDaysTotal(Integer.toString(sessionDaysTakenTotal));
        localReportsDetailHdr.setCompletedPerSessionTotal(String.format(Locale.ROOT, COMPLETED_PER_SESSION_FORMAT,
                completedPerSessionDayTotal));
    }
}
