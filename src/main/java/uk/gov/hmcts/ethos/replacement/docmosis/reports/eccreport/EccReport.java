package uk.gov.hmcts.ethos.replacement.docmosis.reports.eccreport;

import org.apache.commons.collections4.CollectionUtils;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ecm.common.model.reports.eccreport.EccReportSubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.items.EccCounterClaimTypeItem;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.ReportParams;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_LISTING_CASE_TYPE_ID;

public class EccReport {

    private final EccReportDataSource reportDataSource;

    public EccReport(EccReportDataSource reportDataSource) {
        this.reportDataSource = reportDataSource;
    }

    public EccReportData generateReport(ReportParams params) {
        var submitEvents = getCases(params);
        var office = getOffice(params);
        var reportData = initReport(office);

        if (CollectionUtils.isNotEmpty(submitEvents)) {
            executeReport(reportData, submitEvents);
        }
        return reportData;
    }

    private String getOffice(ReportParams params) {
        return ENGLANDWALES_LISTING_CASE_TYPE_ID.equals(params.getCaseTypeId())
                ? params.getManagingOffice() : TribunalOffice.SCOTLAND.getOfficeName();
    }

    private EccReportData initReport(String office) {
        return new EccReportData(office);
    }

    private List<EccReportSubmitEvent> getCases(ReportParams params) {
        var caseTypeId = UtilHelper.getListingCaseTypeId(params.getCaseTypeId());
        return reportDataSource.getData(new ReportParams(caseTypeId, params.getManagingOffice(), params.getDateFrom(),
                params.getDateTo()));
    }

    private void executeReport(EccReportData eccReportData,
                               List<EccReportSubmitEvent> submitEvents) {
        eccReportData.addReportDetail(getReportDetail(submitEvents));
    }

    private List<EccReportDetail> getReportDetail(List<EccReportSubmitEvent> submitEvents) {
        var eccReportDetailList = new ArrayList<EccReportDetail>();
        for (EccReportSubmitEvent submitEvent : submitEvents) {
            var eccReportDetail = new EccReportDetail();
            var caseData = submitEvent.getCaseData();
            if (CollectionUtils.isNotEmpty(caseData.getEccCases())
                    && CollectionUtils.isNotEmpty(caseData.getRespondentCollection())) {
                eccReportDetail.setState(submitEvent.getState());
                eccReportDetail.setDate(caseData.getReceiptDate());
                eccReportDetail.setCaseNumber(caseData.getEthosCaseReference());
                eccReportDetail.setEccCasesCount(String.valueOf(caseData.getEccCases().size()));
                eccReportDetail.setEccCaseList(getEccCases(caseData.getEccCases()));
                eccReportDetail.setRespondentsCount(String.valueOf(caseData.getRespondentCollection().size()));
                eccReportDetailList.add(eccReportDetail);
            }

        }
        eccReportDetailList.sort(Comparator.comparing(EccReportDetail::getCaseNumber));
        return eccReportDetailList;
    }

    private String getEccCases(List<EccCounterClaimTypeItem> eccItems) {
        StringBuilder eccCasesList = new StringBuilder();
        for (EccCounterClaimTypeItem eccItem : eccItems) {
            eccCasesList.append(eccItem.getValue().getCounterClaim()).append("\n");
        }
        return eccCasesList.toString().trim();
    }

}
