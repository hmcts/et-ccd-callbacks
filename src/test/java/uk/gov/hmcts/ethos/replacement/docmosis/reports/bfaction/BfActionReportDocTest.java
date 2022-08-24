package uk.gov.hmcts.ethos.replacement.docmosis.reports.bfaction;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.et.common.model.listing.items.BFDateTypeItem;
import uk.gov.hmcts.et.common.model.listing.types.BFDateType;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.BROUGHT_FORWARD_REPORT;

public class BfActionReportDocTest {
    private BfActionReportDoc bfActionReportDoc;
    private String bfActionReportDocTestDateRangeResource;
    private String bfActionReportDocTestSingleDateResource;

    @Before
    public void setUp() throws Exception {
        bfActionReportDoc = new BfActionReportDoc();
        bfActionReportDocTestDateRangeResource =
            getBfActionDocTestFileContent("bfActionReportDocTestDateRangeResource.json");
        bfActionReportDocTestSingleDateResource =
            getBfActionDocTestFileContent("bfActionReportDocTestSingleDateResource.json");
    }

    @Test
    public void shouldReturnCorrectReportPartialWithDetailsForDateRange() {
        BfActionReportData bfActionReportDataDateRange = new BfActionReportData();
        bfActionReportDataDateRange.setOffice("Leeds");
        bfActionReportDataDateRange.setDocumentName(BROUGHT_FORWARD_REPORT);
        bfActionReportDataDateRange.setReportType(BROUGHT_FORWARD_REPORT);
        bfActionReportDataDateRange.setListingDateFrom("2019-12-08");
        bfActionReportDataDateRange.setListingDateTo("2019-12-20");

        BFDateType bfDateType = new BFDateType();
        bfDateType.setCaseReference("1800522/2020");
        bfDateType.setBroughtForwardAction("Case papers prepared");
        bfDateType.setBroughtForwardEnteredDate("2019-11-20");
        bfDateType.setBroughtForwardDate("2019-12-09");
        bfDateType.setBroughtForwardDateReason("test reason one");
        BFDateTypeItem bfDateTypeItem = new BFDateTypeItem();
        bfDateTypeItem.setId("234");
        bfDateTypeItem.setValue(bfDateType);
        BFDateType bfDateType2 = new BFDateType();
        bfDateType2.setCaseReference("1800876/2020");
        bfDateType2.setBroughtForwardAction("Interlocutory order requested");
        bfDateType2.setBroughtForwardEnteredDate("2019-11-20");
        bfDateType2.setBroughtForwardDate("2019-12-15");
        bfDateType2.setBroughtForwardDateReason("test reason two");
        BFDateTypeItem bfDateTypeItem2 = new BFDateTypeItem();
        bfDateTypeItem2.setId("654");
        bfDateTypeItem2.setValue(bfDateType2);

        List<BFDateTypeItem> bfDateTypeItems = new ArrayList<>();
        bfDateTypeItems.add(bfDateTypeItem);
        bfDateTypeItems.add(bfDateTypeItem2);
        bfActionReportDataDateRange.setBfDateCollection(bfDateTypeItems);
        var resultListingData = bfActionReportDoc.getReportDocPart(bfActionReportDataDateRange);
        assertFalse(resultListingData.toString().isEmpty());
        assertEquals(bfActionReportDocTestDateRangeResource, resultListingData.toString());
    }

    @Test
    public void shouldReturnCorrectReportPartialWithDetailsForSingleDate() {
        BfActionReportData bfActionReportDataSingleDate = new BfActionReportData();
        bfActionReportDataSingleDate.setOffice("Leeds");
        bfActionReportDataSingleDate.setDocumentName(BROUGHT_FORWARD_REPORT);
        bfActionReportDataSingleDate.setReportType(BROUGHT_FORWARD_REPORT);
        bfActionReportDataSingleDate.setListingDateFrom(null);
        bfActionReportDataSingleDate.setListingDateTo(null);
        bfActionReportDataSingleDate.setListingDate("2019-06-18");

        BFDateType bfDateType = new BFDateType();
        bfDateType.setCaseReference("1800909/2020");
        bfDateType.setBroughtForwardAction("Application of letter to ACAS/RPO");
        bfDateType.setBroughtForwardEnteredDate("2019-11-20");
        bfDateType.setBroughtForwardDate("2019-06-18");
        bfDateType.setBroughtForwardDateReason("test");
        BFDateTypeItem bfDateTypeItem = new BFDateTypeItem();
        bfDateTypeItem.setId("7895");
        bfDateTypeItem.setValue(bfDateType);
        BFDateType bfDateType2 = new BFDateType();
        bfDateType2.setCaseReference("1800888/2020");
        bfDateType2.setBroughtForwardAction("Interlocutory order requested");
        bfDateType2.setBroughtForwardEnteredDate("2019-11-20");
        bfDateType2.setBroughtForwardDate("2019-06-18");
        bfDateType2.setBroughtForwardDateReason("test reason two");
        BFDateTypeItem bfDateTypeItem2 = new BFDateTypeItem();
        bfDateTypeItem2.setId("654");
        bfDateTypeItem2.setValue(bfDateType2);

        List<BFDateTypeItem> bfDateTypeItems = new ArrayList<>();
        bfDateTypeItems.add(bfDateTypeItem);
        bfDateTypeItems.add(bfDateTypeItem2);
        bfActionReportDataSingleDate.setBfDateCollection(bfDateTypeItems);
        var resultListingData = bfActionReportDoc.getReportDocPart(bfActionReportDataSingleDate);
        assertFalse(resultListingData.toString().isEmpty());
        assertEquals(bfActionReportDocTestSingleDateResource, resultListingData.toString());
    }

    private String getBfActionDocTestFileContent(String jsonFileName) throws Exception {
        String json = new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(getClass().getClassLoader()
            .getResource(jsonFileName)).toURI())));
        // returns the content by excluding the opening and closing curly brackets
        return json.substring(1, (json.length() - 1));
    }
}
