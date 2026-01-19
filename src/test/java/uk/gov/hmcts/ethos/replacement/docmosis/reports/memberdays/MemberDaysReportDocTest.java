package uk.gov.hmcts.ethos.replacement.docmosis.reports.memberdays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.listing.ListingData;

import java.text.DecimalFormat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NEW_LINE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SINGLE_HEARING_DATE_TYPE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.nullCheck;

@ExtendWith(SpringExtension.class)
class MemberDaysReportDocTest {

    MemberDaysReportDoc memberDaysReportDoc;
    MemberDaysReportData listingData;
    MemberDaysReport memberDaysReport;
    MemberDaysReportDetail detailItem;

    @BeforeEach
    void setUp() {
        memberDaysReport = new MemberDaysReport();
        detailItem = new MemberDaysReportDetail();
        listingData = new MemberDaysReportData();
        memberDaysReportDoc = new MemberDaysReportDoc();
    }

    @Test
    void shouldThrowException() {
        ListingData nonMemberDaysReportDocListingData = new ListingData();
        assertThrows(IllegalStateException.class, () ->
                memberDaysReportDoc.getReportDocPart(nonMemberDaysReportDocListingData)
        );
    }

    @Test
    void shouldReturnCorrectReportPartialWithDetails() {
        detailItem = new MemberDaysReportDetail();
        detailItem.setHearingDate("15 September 2021");
        detailItem.setEmployeeMember("EE Member");
        detailItem.setEmployerMember("ER Member");
        detailItem.setCaseReference("1800003/2021");
        detailItem.setHearingNumber("33");
        detailItem.setHearingType("Preliminary Hearing");
        detailItem.setHearingClerk("Tester Clerk");
        detailItem.setHearingDuration("420");
        listingData.getReportDetails().add(detailItem);

        StringBuilder expectedDetailRowContent = new StringBuilder(339);
        expectedDetailRowContent.append("\"Listed_date\":\"").append(NEW_LINE)
                .append("\"Report_Office\":\"").append(NEW_LINE)
                .append("\"Total_Full_Days\":\"").append(NEW_LINE)
                .append("\"Total_Half_Days\":\"").append(NEW_LINE)
                .append("\"Total_Days\":\"").append(NEW_LINE)
                .append("\"memberDaySummaryItems\":[\n],\n")
                .append("\"reportDetails\":[\n{\n")
                .append("\"Detail_Hearing_Date\":\"").append(nullCheck(detailItem.getHearingDate())).append(NEW_LINE)
                .append("\"Employee_Member\":\"").append(nullCheck(detailItem.getEmployeeMember())).append(NEW_LINE)
                .append("\"Employer_Member\":\"").append(nullCheck(detailItem.getEmployerMember())).append(NEW_LINE)
                .append("\"Case_Reference\":\"").append(nullCheck(detailItem.getCaseReference())).append(NEW_LINE)
                .append("\"Hearing_Number\":\"").append(nullCheck(detailItem.getHearingNumber())).append(NEW_LINE)
                .append("\"Hearing_Type\":\"").append(nullCheck(detailItem.getHearingType())).append(NEW_LINE)
                .append("\"Hearing_Clerk\":\"").append(nullCheck(detailItem.getHearingClerk())).append(NEW_LINE);

        double durationInMinutes = Double.parseDouble(detailItem.getHearingDuration());
        expectedDetailRowContent.append("\"Hearing_Duration\":\"")
                .append(nullCheck(String.valueOf(new DecimalFormat("#").format(durationInMinutes))))
                .append("\"\n}],\n");

        StringBuilder resultListingData = memberDaysReportDoc.getReportDocPart(listingData);
        assertFalse(resultListingData.toString().isEmpty());
        assertEquals(expectedDetailRowContent.toString(), resultListingData.toString());
    }

    @Test
    void shouldReturnCorrectReportPartialWithSummary() {

        detailItem = new MemberDaysReportDetail();
        detailItem.setHearingDate("15 September 2021");
        detailItem.setEmployeeMember("EE Member");
        detailItem.setEmployeeMember("ER Member");
        detailItem.setCaseReference("1800003/2021");
        detailItem.setHearingNumber("33");
        detailItem.setHearingType("Preliminary Hearing");
        detailItem.setHearingClerk("Tester Clerk");
        detailItem.setHearingDuration("420");
        listingData.getReportDetails().add(detailItem);
        listingData.setHearingDateType(SINGLE_HEARING_DATE_TYPE);
        listingData.setListingDate("2021-09-15");

        StringBuilder expectedDetailRowContent = new StringBuilder(548);
        expectedDetailRowContent.append("\"Listed_date\":\"15 September 2021").append(NEW_LINE)
                .append("\"Report_Office\":\"").append(NEW_LINE)
                .append("\"Total_Full_Days\":\"").append(NEW_LINE)
                .append("\"Total_Half_Days\":\"").append(NEW_LINE)
                .append("\"Total_Days\":\"").append(NEW_LINE);

        MemberDaySummaryItem memberDaySummaryItem = new MemberDaySummaryItem();
        memberDaySummaryItem.setHearingDate("15 September 2021");
        memberDaySummaryItem.setFullDays("2");
        memberDaySummaryItem.setHalfDays("0");
        memberDaySummaryItem.setTotalDays("2");

        listingData.getMemberDaySummaryItems().add(memberDaySummaryItem);

        expectedDetailRowContent.append("\"memberDaySummaryItems\":[\n{\n\"Hearing_Date\":\"15 September 2021")
                .append(NEW_LINE).append("\"Full_Days\":\"2").append(NEW_LINE)
                .append("\"Half_Days\":\"0").append(NEW_LINE)
                .append("\"Total_Days\":\"2\"\n}],\n")
                .append("\"reportDetails\":[\n{\n\"Detail_Hearing_Date\":\"")
                .append(nullCheck(detailItem.getHearingDate())).append(NEW_LINE)
                .append("\"Employee_Member\":\"").append(nullCheck(detailItem.getEmployeeMember())).append(NEW_LINE)
                .append("\"Employer_Member\":\"").append(nullCheck(detailItem.getEmployerMember())).append(NEW_LINE)
                .append("\"Case_Reference\":\"").append(nullCheck(detailItem.getCaseReference())).append(NEW_LINE)
                .append("\"Hearing_Number\":\"").append(nullCheck(detailItem.getHearingNumber())).append(NEW_LINE)
                .append("\"Hearing_Type\":\"").append(nullCheck(detailItem.getHearingType())).append(NEW_LINE)
                .append("\"Hearing_Clerk\":\"").append(nullCheck(detailItem.getHearingClerk())).append(NEW_LINE)
                .append("\"Hearing_Duration\":\"")
                .append(nullCheck(String.valueOf(new DecimalFormat("#")
                        .format(Double.parseDouble(detailItem.getHearingDuration())))))
                .append("\"\n}],\n");
        StringBuilder resultListingData = memberDaysReportDoc.getReportDocPart(listingData);
        assertFalse(resultListingData.toString().isEmpty());
        assertEquals(expectedDetailRowContent.toString(), resultListingData.toString());
    }

    @Test
    void shouldReturnCorrectReportPartialWithSummaryHeader() {
        detailItem = new MemberDaysReportDetail();
        detailItem.setHearingDate("15 September 2021");
        detailItem.setEmployeeMember("EE Member");
        detailItem.setEmployeeMember("ER Member");
        detailItem.setCaseReference("1800003/2021");
        detailItem.setHearingNumber("33");
        detailItem.setHearingType("Preliminary Hearing");
        detailItem.setHearingClerk("Tester Clerk");
        detailItem.setHearingDuration("420");

        MemberDaySummaryItem memberDaySummaryItem = new MemberDaySummaryItem();
        memberDaySummaryItem.setHearingDate("15 September 2021");
        memberDaySummaryItem.setFullDays("2");
        memberDaySummaryItem.setHalfDays("0");
        memberDaySummaryItem.setTotalDays("2");

        listingData.getReportDetails().add(detailItem);
        listingData.setHearingDateType(SINGLE_HEARING_DATE_TYPE);
        listingData.setListingDate("2021-09-18");
        listingData.setOffice("MukeraCity");
        listingData.setHalfDaysTotal("0");
        listingData.setFullDaysTotal("2");
        listingData.setTotalDays("2.0");
        listingData.getMemberDaySummaryItems().add(memberDaySummaryItem);
        String expectedDetailRowContent = "\"Listed_date\":\"18 September 2021" + NEW_LINE
                + "\"Report_Office\":\"MukeraCity" + NEW_LINE
                + "\"Total_Full_Days\":\"2" + NEW_LINE
                + "\"Total_Half_Days\":\"0" + NEW_LINE
                + "\"Total_Days\":\"2.0" + NEW_LINE
                + "\"memberDaySummaryItems\":[\n{\n\"Hearing_Date\":\"15 September 2021" + NEW_LINE
                + "\"Full_Days\":\"2" + NEW_LINE
                + "\"Half_Days\":\"0" + NEW_LINE
                + "\"Total_Days\":\"2\"\n}],\n"
                + "\"reportDetails\":[\n{\n"
                + "\"Detail_Hearing_Date\":\"" + nullCheck(detailItem.getHearingDate()) + NEW_LINE
                + "\"Employee_Member\":\"" + nullCheck(detailItem.getEmployeeMember()) + NEW_LINE
                + "\"Employer_Member\":\"" + nullCheck(detailItem.getEmployerMember()) + NEW_LINE
                + "\"Case_Reference\":\"" + nullCheck(detailItem.getCaseReference()) + NEW_LINE
                + "\"Hearing_Number\":\"" + nullCheck(detailItem.getHearingNumber()) + NEW_LINE
                + "\"Hearing_Type\":\"" + nullCheck(detailItem.getHearingType()) + NEW_LINE
                + "\"Hearing_Clerk\":\"" + nullCheck(detailItem.getHearingClerk()) + NEW_LINE
                + "\"Hearing_Duration\":\"" + nullCheck(String.valueOf(new DecimalFormat("#")
                        .format(Double.parseDouble(detailItem.getHearingDuration()))))
                + "\"\n}],\n";
        StringBuilder resultListingData = memberDaysReportDoc.getReportDocPart(listingData);
        assertFalse(resultListingData.toString().isEmpty());
        assertEquals(expectedDetailRowContent, resultListingData.toString());
    }
}
