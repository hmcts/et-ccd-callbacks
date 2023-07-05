package uk.gov.hmcts.ethos.replacement.docmosis.reports.casesourcelocalreport;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.listing.ListingData;
import uk.gov.hmcts.et.common.model.listing.ListingDetails;
import uk.gov.hmcts.et.common.model.listing.items.AdhocReportTypeItem;
import uk.gov.hmcts.et.common.model.listing.types.AdhocReportType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReportHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.ET1_ONLINE_CASE_SOURCE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.FLAG_ECC;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MANUALLY_CREATED_POSITION;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MIGRATION_CASE_SOURCE;

@Service
@Slf4j
public class CaseSourceLocalReport {

    public ListingData generateReportData(ListingDetails listingDetails, List<SubmitEvent> submitEvents) {
        executeReport(listingDetails, submitEvents);
        listingDetails.getCaseData().clearReportFields();
        return listingDetails.getCaseData();
    }

    private void executeReport(ListingDetails listingDetails, List<SubmitEvent> submitEvents) {
        log.info(String.format("Case source local report case type id %s search results: %d",
                listingDetails.getCaseTypeId(), submitEvents.size()));
        populateLocalReportSummary(listingDetails, submitEvents);
    }

    private void populateLocalReportSummary(ListingDetails listingDetails, List<SubmitEvent> submitEvents) {
        ListingData listingData = listingDetails.getCaseData();
        collectionUtilsIsEmpty(listingData);
        AdhocReportType adhocReportType = listingData.getLocalReportsSummary().get(0).getValue();
        adhocReportType.setReportOffice(ReportHelper.getReportOffice(listingDetails.getCaseTypeId(),
                listingData.getManagingOffice()));

        int manuallyCreatedCases = countCasesBySource(submitEvents, MANUALLY_CREATED_POSITION);
        int et1OnlineCases = countCasesBySource(submitEvents, ET1_ONLINE_CASE_SOURCE);
        int eccCases = countCasesBySource(submitEvents, FLAG_ECC);
        int migrationCases = countCasesBySource(submitEvents, MIGRATION_CASE_SOURCE);
        int totalCases = manuallyCreatedCases + et1OnlineCases + eccCases + migrationCases;

        float manuallyCreatedPercent = calculatePercentage(manuallyCreatedCases, totalCases);
        float et1OnlinePercent = calculatePercentage(et1OnlineCases, totalCases);
        float eccPercent = calculatePercentage(eccCases, totalCases);
        float migrationCasesPercent = calculatePercentage(migrationCases, totalCases);

        setReportTypeProperties(adhocReportType, manuallyCreatedCases, et1OnlineCases,
                migrationCases, eccCases, totalCases, manuallyCreatedPercent, et1OnlinePercent,
                migrationCasesPercent, eccPercent);
    }

    private int countCasesBySource(List<SubmitEvent> submitEvents, String caseSource) {
        int count = 0;
        for (SubmitEvent submitEvent : submitEvents) {
            if (caseSource.equals(submitEvent.getCaseData().getCaseSource())) {
                count++;
            }
        }
        return count;
    }

    private float calculatePercentage(int cases, int totalCases) {
        return (totalCases != 0) ? ((float) cases / totalCases) * 100 : 0;
    }

    private void setReportTypeProperties(AdhocReportType adhocReportType, int manuallyCreatedCases,
                                         int et1OnlineCases, int migrationCases, int eccCases,
                                         int totalCases, float manuallyCreatedPercent,
                                         float et1OnlinePercent, float migrationCasesPercent,
                                         float eccPercent) {
        adhocReportType.setManuallyCreatedTotalCases(String.valueOf(manuallyCreatedCases));
        adhocReportType.setEt1OnlineTotalCases(String.valueOf(et1OnlineCases));
        adhocReportType.setMigratedTotalCases(String.valueOf(migrationCases));
        adhocReportType.setEccTotalCases(String.valueOf(eccCases));
        adhocReportType.setTotalCases(String.valueOf(totalCases));

        adhocReportType.setManuallyCreatedTotalCasesPercent(String.format("%.2f", manuallyCreatedPercent));
        adhocReportType.setEt1OnlineTotalCasesPercent(String.format("%.2f", et1OnlinePercent));
        adhocReportType.setMigratedTotalCasesPercent(String.format("%.2f", migrationCasesPercent));
        adhocReportType.setEccTotalCasesPercent(String.format("%.2f", eccPercent));
    }

    private static void collectionUtilsIsEmpty(ListingData listingData) {
        if (CollectionUtils.isEmpty(listingData.getLocalReportsSummary())) {
            AdhocReportTypeItem adhocReportTypeItem = new AdhocReportTypeItem();
            AdhocReportType reportType = new AdhocReportType();
            adhocReportTypeItem.setId(UUID.randomUUID().toString());
            adhocReportTypeItem.setValue(reportType);
            List<AdhocReportTypeItem> newSummary = new ArrayList<>();
            newSummary.add(adhocReportTypeItem);
            listingData.setLocalReportsSummary(newSummary);
        }
    }

}
