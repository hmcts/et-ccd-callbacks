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
        if (CollectionUtils.isEmpty(listingData.getLocalReportsSummary())) {
            AdhocReportTypeItem adhocReportTypeItem = new AdhocReportTypeItem();
            AdhocReportType reportType = new AdhocReportType();
            adhocReportTypeItem.setId(UUID.randomUUID().toString());
            adhocReportTypeItem.setValue(reportType);
            List<AdhocReportTypeItem> newSummary = new ArrayList<>();
            newSummary.add(adhocReportTypeItem);
            listingData.setLocalReportsSummary(newSummary);
        }
        AdhocReportType adhocReportType = listingData.getLocalReportsSummary().get(0).getValue();
        adhocReportType.setReportOffice(ReportHelper.getReportOffice(listingDetails.getCaseTypeId(),
                listingData.getManagingOffice()));
        int manuallyCreatedCases = 0;
        int et1OnlineCases = 0;
        int eccCases = 0;
        int migrationCases = 0;

        for (SubmitEvent submitEvent : submitEvents) {
            if (ET1_ONLINE_CASE_SOURCE.equals(submitEvent.getCaseData().getCaseSource())) {
                et1OnlineCases = et1OnlineCases + 1;
            }

            if (MIGRATION_CASE_SOURCE.equals(submitEvent.getCaseData().getCaseSource())) {
                migrationCases = migrationCases + 1;
            }

            if (FLAG_ECC.equals(submitEvent.getCaseData().getCaseSource())) {
                eccCases = eccCases + 1;
            }

            if (MANUALLY_CREATED_POSITION.equals(submitEvent.getCaseData().getCaseSource())) {
                manuallyCreatedCases = manuallyCreatedCases + 1;
            }
        }
        int totalCases = manuallyCreatedCases + et1OnlineCases + eccCases + migrationCases;

        float manuallyCreatedPercent = (totalCases == 0) ? 0 : ((float) manuallyCreatedCases / totalCases) * 100;

        float et1OnlinePercent = (totalCases == 0) ? 0 : ((float) et1OnlineCases / totalCases) * 100;

        float eccPercent = (totalCases == 0) ? 0 : ((float) eccCases / totalCases) * 100;

        float migrationCasesPercent = (totalCases == 0) ? 0 : ((float) migrationCases / totalCases) * 100;

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

}
