package uk.gov.hmcts.ethos.replacement.docmosis.reports.respondentsreport;

import org.apache.commons.collections4.CollectionUtils;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.ecm.common.model.reports.respondentsreport.RespondentsReportCaseData;
import uk.gov.hmcts.ecm.common.model.reports.respondentsreport.RespondentsReportSubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReportHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.ReportParams;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RespondentsReport {

    private final RespondentsReportDataSource reportDataSource;

    public RespondentsReport(RespondentsReportDataSource reportDataSource) {
        this.reportDataSource = reportDataSource;
    }

    public RespondentsReportData generateReport(ReportParams params) {
        List<RespondentsReportSubmitEvent> submitEvents = getCases(params);
        String reportOffice = ReportHelper.getReportOffice(params.getCaseTypeId(), params.getManagingOffice());
        RespondentsReportData reportData = initReport(reportOffice);
        if (CollectionUtils.isNotEmpty(submitEvents)) {
            executeReport(reportData, submitEvents);
        }
        return reportData;
    }

    private RespondentsReportData initReport(String office) {
        RespondentsReportSummary reportSummary = new RespondentsReportSummary();
        reportSummary.setOffice(office);
        reportSummary.setTotalCasesWithMoreThanOneRespondent("0");
        return new RespondentsReportData(reportSummary);
    }

    private List<RespondentsReportSubmitEvent> getCases(ReportParams params) {
        String caseTypeId = UtilHelper.getListingCaseTypeId(params.getCaseTypeId());
        return reportDataSource.getData(caseTypeId, params.getManagingOffice(), params.getDateFrom(),
                params.getDateTo());
    }

    private void executeReport(RespondentsReportData respondentReportData,
                               List<RespondentsReportSubmitEvent> submitEvents) {
        int moreThan1Resp = (int) submitEvents.stream()
                .filter(s -> CollectionUtils.isNotEmpty(s.getCaseData().getRespondentCollection())
               && s.getCaseData().getRespondentCollection().size() > 1).count();

        respondentReportData.getReportSummary().setTotalCasesWithMoreThanOneRespondent(String.valueOf(moreThan1Resp));
        respondentReportData.addReportDetail(getReportDetail(submitEvents));

    }

    private List<RespondentsReportDetail> getReportDetail(List<RespondentsReportSubmitEvent> submitEvents) {
        List<RespondentsReportDetail> respondentsReportDetailList = new ArrayList<>();
        for (RespondentsReportSubmitEvent submitEvent : submitEvents) {
            RespondentsReportCaseData caseData = submitEvent.getCaseData();
            if (hasMultipleRespondents(caseData)) {

                for (RespondentSumTypeItem r : caseData.getRespondentCollection()) {
                    RespondentsReportDetail detail = new RespondentsReportDetail();
                    detail.setCaseNumber(caseData.getEthosCaseReference());
                    detail.setRespondentName(r.getValue().getRespondentName());
                    String rep = getRepresentative(r.getValue().getRespondentName(), caseData);
                    detail.setRepresentativeName(rep);
                    detail.setRepresentativeHasMoreThanOneRespondent(
                        isRepresentativeRepresentingMoreThanOneRespondent(rep, caseData) ? "Y" : "N");
                    respondentsReportDetailList.add(detail);
                }
            }
        }
        return respondentsReportDetailList;
    }

    private boolean hasMultipleRespondents(RespondentsReportCaseData caseData) {
        return CollectionUtils.size(caseData.getRespondentCollection()) > 1;
    }

    private boolean isRepresentativeRepresentingMoreThanOneRespondent(String rep, RespondentsReportCaseData caseData) {
        int count = 0;
        if (CollectionUtils.isNotEmpty(caseData.getRepCollection())) {
            for (RepresentedTypeRItem repItem : caseData.getRepCollection()) {
                if (repItem.getValue().getNameOfRepresentative().equals(rep)) {
                    for (RespondentSumTypeItem respItem : caseData.getRespondentCollection()) {
                        if (respItem.getValue().getRespondentName().equals(repItem.getValue().getRespRepName())) {
                            count++;
                        }
                    }
                }
            }
        }
        return count > 1;
    }

    private String getRepresentative(String respName, RespondentsReportCaseData caseData) {
        if (CollectionUtils.isNotEmpty(caseData.getRepCollection())) {
            Optional<RepresentedTypeRItem> rep = caseData.getRepCollection().stream()
                    .filter(a -> a.getValue().getRespRepName().equals(respName)).findFirst();
            if (rep.isPresent()) {
                return rep.get().getValue().getNameOfRepresentative();
            }
        }
        return "N/A";
    }
}
