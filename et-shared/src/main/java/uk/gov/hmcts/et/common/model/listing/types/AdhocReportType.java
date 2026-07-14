package uk.gov.hmcts.et.common.model.listing.types;

import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;
import uk.gov.hmcts.et.common.model.ccd.EnglandWalesDefinition;
import uk.gov.hmcts.et.common.model.ccd.ScotlandDefinition;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import uk.gov.hmcts.et.common.model.listing.items.ReportET4TypeItem;
import uk.gov.hmcts.et.common.model.listing.items.ReportListingsTypeItem;
import uk.gov.hmcts.et.common.model.listing.items.ReportRespondentTypeItem;

import java.util.List;

@ComplexType(name = "adhocReport", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class AdhocReportType {

    @CCD(label = "By Date")
    @JsonProperty("reportDate")
    private String reportDate;
    @CCD(label = "Office")
    @JsonProperty("reportOffice")
    private String reportOffice;
    @CCD(label = "Receipt Date")
    @JsonProperty("receiptDate")
    private String receiptDate;
    @CCD(label = "Hearing Date")
    @JsonProperty("hearingDate")
    private String hearingDate;
    @CCD(label = "By Date")
    @JsonProperty("date")
    private String date;
    @CCD(label = "Full Days")
    @JsonProperty("full")
    private String full;
    @CCD(label = "Half Days")
    @JsonProperty("half")
    private String half;
    @CCD(label = "Minutes")
    @JsonProperty("mins")
    private String mins;
    @CCD(label = "Total")
    @JsonProperty("total")
    private String total;
    @CCD(label = "Employee Member")
    @JsonProperty("eeMember")
    private String eeMember;
    @CCD(label = "Employer Member")
    @JsonProperty("erMember")
    private String erMember;
    @CCD(label = "Case Number")
    @JsonProperty("caseReference")
    private String caseReference;
    @CCD(label = "Multiple Reference")
    @JsonProperty("multipleRef")
    private String multipleRef;
    @CCD(label = "Multiple / Sub")
    @JsonProperty("multSub")
    private String multSub;
    @CCD(label = "Hearing Number")
    @JsonProperty("hearingNumber")
    private String hearingNumber;
    @CCD(label = "Hearing Type")
    @JsonProperty("hearingType")
    private String hearingType;
    @CCD(label = "Telephone Conference")
    @JsonProperty("hearingTelConf")
    private String hearingTelConf;
    @CCD(label = "Duration (minutes)")
    @JsonProperty("hearingDuration")
    private String hearingDuration;
    @CCD(label = "Hearing Clerk")
    @JsonProperty("hearingClerk")
    private String hearingClerk;
    @CCD(label = "Clerk")
    @JsonProperty("clerk")
    private String clerk;
    @CCD(label = "Panel Type")
    @JsonProperty("hearingSitAlone")
    private String hearingSitAlone;
    @CCD(label = "Judge")
    @JsonProperty("hearingJudge")
    private String hearingJudge;
    @CCD(label = "Full time or part time")
    @JsonProperty("judgeType")
    private String judgeType;
    @CCD(label = "Date Sent")
    @JsonProperty("judgementDateSent")
    private String judgementDateSent;
    @CCD(label = "Position")
    @JsonProperty("position")
    private String position;
    @CCD(label = "Date To Position")
    @JsonProperty("dateToPosition")
    private String dateToPosition;
    @CCD(label = "Physical Location")
    @JsonProperty("fileLocation")
    private String fileLocation;
    @CCD(label = "Physical Location", includeInProfiles = ScotlandDefinition.class)
    @JsonProperty("fileLocationGlasgow")
    private String fileLocationGlasgow;
    @CCD(label = "Physical Location", includeInProfiles = ScotlandDefinition.class)
    @JsonProperty("fileLocationAberdeen")
    private String fileLocationAberdeen;
    @CCD(label = "Physical Location", includeInProfiles = ScotlandDefinition.class)
    @JsonProperty("fileLocationDundee")
    private String fileLocationDundee;
    @CCD(label = "Physical Location", includeInProfiles = ScotlandDefinition.class)
    @JsonProperty("fileLocationEdinburgh")
    private String fileLocationEdinburgh;
    @CCD(label = "Cases Completed at Hearing")
    @JsonProperty("casesCompletedHearingTotal")
    private String casesCompletedHearingTotal;
    @CCD(label = "Cases Completed at Hearing")
    @JsonProperty("casesCompletedHearing")
    private String casesCompletedHearing;
    @CCD(label = "Session Type")
    @JsonProperty("sessionType")
    private String sessionType;
    @CCD(label = "Session Days")
    @JsonProperty("sessionDays")
    private String sessionDays;
    @CCD(label = "Total Session Days")
    @JsonProperty("sessionDaysTotal")
    private String sessionDaysTotal;
    @CCD(label = "Total Session Days")
    @JsonProperty("sessionDaysTotalDetail")
    private String sessionDaysTotalDetail;
    @CCD(label = "Completed Per Session Day")
    @JsonProperty("completedPerSession")
    private String completedPerSession;
    @CCD(label = "Completed Per Session Day")
    @JsonProperty("completedPerSessionTotal")
    private String completedPerSessionTotal;
    @CCD(label = "FTC Session Days")
    @JsonProperty("ftSessionDays")
    private String ftSessionDays;
    @CCD(label = "FTC Session Days")
    @JsonProperty("ftSessionDaysTotal")
    private String ftSessionDaysTotal;
    @CCD(label = "PTC Session Days")
    @JsonProperty("ptSessionDays")
    private String ptSessionDays;
    @CCD(label = "PTC Session Days")
    @JsonProperty("ptSessionDaysTotal")
    private String ptSessionDaysTotal;
    @CCD(label = "PTC Session Days")
    @JsonProperty("ptSessionDaysPerCent")
    private String ptSessionDaysPerCent;
    @CCD(label = "Other Session Days")
    @JsonProperty("otherSessionDaysTotal")
    private String otherSessionDaysTotal;
    @CCD(label = "Other Session Days")
    @JsonProperty("otherSessionDays")
    private String otherSessionDays;
    @CCD(label = "Conciliation Track")
    @JsonProperty("conciliationTrack")
    private String conciliationTrack;
    @CCD(label = "Number")
    @JsonProperty("conciliationTrackNo")
    private String conciliationTrackNo;
    @CCD(label = "No Conciliation")
    @JsonProperty("ConNoneCasesCompletedHearing")
    private String conNoneCasesCompletedHearing;
    @CCD(label = "No Conciliation")
    @JsonProperty("ConNoneSessionDays")
    private String conNoneSessionDays;
    @CCD(label = "No Conciliation")
    @JsonProperty("ConNoneCompletedPerSession")
    private String conNoneCompletedPerSession;
    @CCD(label = "Fast Track")
    @JsonProperty("ConFastCasesCompletedHearing")
    private String conFastCasesCompletedHearing;
    @CCD(label = "Fast Track")
    @JsonProperty("ConFastSessionDays")
    private String conFastSessionDays;
    @CCD(label = "Fast Track")
    @JsonProperty("ConFastCompletedPerSession")
    private String conFastCompletedPerSession;
    @CCD(label = "Standard Track")
    @JsonProperty("ConStdCasesCompletedHearing")
    private String conStdCasesCompletedHearing;
    @CCD(label = "Standard Track")
    @JsonProperty("ConStdSessionDays")
    private String conStdSessionDays;
    @CCD(label = "Standard Track")
    @JsonProperty("ConStdCompletedPerSession")
    private String conStdCompletedPerSession;
    @CCD(label = "Open Track")
    @JsonProperty("ConOpenCasesCompletedHearing")
    private String conOpenCasesCompletedHearing;
    @CCD(label = "Open Track")
    @JsonProperty("ConOpenSessionDays")
    private String conOpenSessionDays;
    @CCD(label = "Open Track")
    @JsonProperty("ConOpenCompletedPerSession")
    private String conOpenCompletedPerSession;
    @CCD(label = "Total Cases")
    @JsonProperty("totalCases")
    private String totalCases;
    @CCD(label = "Total")
    @JsonProperty("Total26wk")
    private String total26wk;
    @CCD(label = "%")
    @JsonProperty("Total26wkPerCent")
    private String total26wkPerCent;
    @CCD(label = "Total")
    @JsonProperty("Totalx26wk")
    private String totalx26wk;
    @CCD(label = "%")
    @JsonProperty("Totalx26wkPerCent")
    private String totalx26wkPerCent;
    @CCD(id = "wk4Total", label = "Total", includeInProfiles = EnglandWalesDefinition.class)
    @CCD(id = "4wkTotal", label = "Total", includeInProfiles = ScotlandDefinition.class)
    @JsonProperty("Total4wk")
    private String total4wk;
    @CCD(id = "wk4TotalPerCent", label = "%", includeInProfiles = EnglandWalesDefinition.class)
    @CCD(id = "4wkTotalPerCent", label = "%", includeInProfiles = ScotlandDefinition.class)
    @JsonProperty("Total4wkPerCent")
    private String total4wkPerCent;
    @CCD(id = "x4wkTotal", label = "Total")
    @JsonProperty("Totalx4wk")
    private String totalx4wk;
    @CCD(id = "x4wkTotalPerCent", label = "%")
    @JsonProperty("Totalx4wkPerCent")
    private String totalx4wkPerCent;
    @CCD(label = "Respondent Name")
    @JsonProperty("respondentName")
    private String respondentName;
    @CCD(label = "Actioned")
    @JsonProperty("actioned")
    private String actioned;
    @CCD(label = "BF Date")
    @JsonProperty("bfDate")
    private String bfDate;
    @CCD(label = "Cleared?")
    @JsonProperty("bfDateCleared")
    private String bfDateCleared;
    @CCD(label = "Reserved")
    @JsonProperty("reservedHearing")
    private String reservedHearing;
    @CCD(label = "CM")
    @JsonProperty("hearingCM")
    private String hearingCM;
    @CCD(label = "costs")
    @JsonProperty("costs")
    private String costs;
    @CCD(label = "Interloc")
    @JsonProperty("hearingInterloc")
    private String hearingInterloc;
    @CCD(label = "PH/PHR")
    @JsonProperty("hearingPH")
    private String hearingPH;
    @CCD(label = "Prelim")
    @JsonProperty("hearingPrelim")
    private String hearingPrelim;
    @CCD(label = "Stage")
    @JsonProperty("stage")
    private String stage;
    @CCD(label = "Stage1")
    @JsonProperty("hearingStage1")
    private String hearingStage1;
    @CCD(label = "Stage2")
    @JsonProperty("hearingStage2")
    private String hearingStage2;
    @CCD(label = "Full")
    @JsonProperty("hearingFull")
    private String hearingFull;
    @CCD(label = "Hearing")
    @JsonProperty("hearing")
    private String hearing;
    @CCD(label = "Remedy")
    @JsonProperty("remedy")
    private String remedy;
    @CCD(label = "Review")
    @JsonProperty("review")
    private String review;
    @CCD(label = "Reconsider")
    @JsonProperty("reconsider")
    private String reconsider;
    @CCD(label = "Sub split")
    @JsonProperty("subSplit")
    private String subSplit;
    @CCD(label = "Lead?")
    @JsonProperty("leadCase")
    private String leadCase;
    @CCD(label = "Received Date")
    @JsonProperty("et3ReceivedDate")
    private String et3ReceivedDate;
    @CCD(label = "JM")
    @JsonProperty("judicialMediation")
    private String judicialMediation;
    @CCD(label = "Case Type")
    @JsonProperty("caseType")
    private String caseType;
    @CCD(label = "Singles")
    @JsonProperty("singlesTotal")
    private String singlesTotal;
    @CCD(label = "Multiples")
    @JsonProperty("multiplesTotal")
    private String multiplesTotal;
    @CCD(label = "Date of Acceptance")
    @JsonProperty("dateOfAcceptance")
    private String dateOfAcceptance;
    @CCD(label = " ")
    @JsonProperty("respondentET3")
    private List<ReportRespondentTypeItem> respondentET3;
    @CCD(label = " ")
    @JsonProperty("respondentET4")
    private List<ReportET4TypeItem> respondentET4;
    @CCD(label = " ")
    @JsonProperty("listingHistory")
    private List<ReportListingsTypeItem> listingHistory;
    @CCD(label = "No Conciliation")
    @JsonProperty("ConNoneTotal")
    private String conNoneTotal;
    @CCD(label = "Fast Track")
    @JsonProperty("ConStdTotal")
    private String conStdTotal;
    @CCD(label = "Standard Track")
    @JsonProperty("ConFastTotal")
    private String conFastTotal;
    @CCD(label = "Open Track")
    @JsonProperty("ConOpenTotal")
    private String conOpenTotal;
    @CCD(label = "No Conciliation")
    @JsonProperty("ConNone26wkTotal")
    private String conNone26wkTotal;
    @CCD(label = "Fast Track")
    @JsonProperty("ConStd26wkTotal")
    private String conStd26wkTotal;
    @CCD(label = "Standard Track")
    @JsonProperty("ConFast26wkTotal")
    private String conFast26wkTotal;
    @CCD(label = "Open Track")
    @JsonProperty("ConOpen26wkTotal")
    private String conOpen26wkTotal;
    @CCD(label = "No Conciliation")
    @JsonProperty("ConNone26wkTotalPerCent")
    private String conNone26wkTotalPerCent;
    @CCD(label = "Fast Track")
    @JsonProperty("ConStd26wkTotalPerCent")
    private String conStd26wkTotalPerCent;
    @CCD(label = "Standard Track")
    @JsonProperty("ConFast26wkTotalPerCent")
    private String conFast26wkTotalPerCent;
    @CCD(label = "Open Track")
    @JsonProperty("ConOpen26wkTotalPerCent")
    private String conOpen26wkTotalPerCent;
    @CCD(label = "No Conciliation")
    @JsonProperty("xConNone26wkTotal")
    private String notConNone26wkTotal;
    @CCD(label = "Fast Track")
    @JsonProperty("xConStd26wkTotal")
    private String notConStd26wkTotal;
    @CCD(label = "Standard Track")
    @JsonProperty("xConFast26wkTotal")
    private String notConFast26wkTotal;
    @CCD(label = "Open Track")
    @JsonProperty("xConOpen26wkTotal")
    private String notConOpen26wkTotal;
    @CCD(label = "No Conciliation")
    @JsonProperty("xConNone26wkTotalPerCent")
    private String notConNone26wkTotalPerCent;
    @CCD(label = "Fast Track")
    @JsonProperty("xConStd26wkTotalPerCent")
    private String notConStd26wkTotalPerCent;
    @CCD(label = "Standard Track")
    @JsonProperty("xConFast26wkTotalPerCent")
    private String notConFast26wkTotalPerCent;
    @CCD(label = "Open Track")
    @JsonProperty("xConOpen26wkTotalPerCent")
    private String notConOpen26wkTotalPerCent;
    @CCD(label = "Delayed days for First Hearing", includeInProfiles = EnglandWalesDefinition.class)
    @CCD(label = "Delayed Days For First Hearing", includeInProfiles = ScotlandDefinition.class)
    @JsonProperty("delayedDaysForFirstHearing")
    private String delayedDaysForFirstHearing;

    @CCD(label = "Total")
    @JsonProperty("claimServedDay1Total")
    private String claimServedDay1Total;
    @CCD(label = "%")
    @JsonProperty("claimServedDay1Percent")
    private String claimServedDay1Percent;

    @CCD(label = "Total")
    @JsonProperty("claimServedDay2Total")
    private String claimServedDay2Total;
    @CCD(label = "%")
    @JsonProperty("claimServedDay2Percent")
    private String claimServedDay2Percent;

    @CCD(label = "Total")
    @JsonProperty("claimServedDay3Total")
    private String claimServedDay3Total;
    @CCD(label = "%")
    @JsonProperty("claimServedDay3Percent")
    private String claimServedDay3Percent;

    @CCD(label = "Total")
    @JsonProperty("claimServedDay4Total")
    private String claimServedDay4Total;
    @CCD(label = "%")
    @JsonProperty("claimServedDay4Percent")
    private String claimServedDay4Percent;

    @CCD(label = "Total")
    @JsonProperty("claimServedDay5Total")
    private String claimServedDay5Total;
    @CCD(label = "%")
    @JsonProperty("claimServedDay5Percent")
    private String claimServedDay5Percent;

    @CCD(label = "Total")
    @JsonProperty("claimServed6PlusDaysTotal")
    private String claimServed6PlusDaysTotal;
    @CCD(label = "%")
    @JsonProperty("claimServed6PlusDaysPercent")
    private String claimServed6PlusDaysPercent;

    @CCD(label = "Total Claims")
    @JsonProperty("claimServedTotal")
    private String claimServedTotal;

    @CCD(label = "claim Served Items list")
    @JsonProperty("claimServedItems")
    private List<ClaimServedTypeItem> claimServedItems;

    @CCD(label = "Manually created cases")
    @JsonProperty("manuallyCreatedTotalCases")
    private String manuallyCreatedTotalCases;
    @CCD(label = "ET1 online cases")
    @JsonProperty("et1OnlineTotalCases")
    private String et1OnlineTotalCases;
    @CCD(label = "Ecc total cases")
    @JsonProperty("eccTotalCases")
    private String eccTotalCases;
    @CCD(label = "Migration cases")
    @JsonProperty("migratedTotalCases")
    private String migratedTotalCases;

    @CCD(label = "Manually created cases percent")
    @JsonProperty("manuallyCreatedTotalCasesPercent")
    private String manuallyCreatedTotalCasesPercent;
    @CCD(label = "ET1 online cases percent")
    @JsonProperty("et1OnlineTotalCasesPercent")
    private String et1OnlineTotalCasesPercent;
    @CCD(label = "ecc total cases percent")
    @JsonProperty("eccTotalCasesPercent")
    private String eccTotalCasesPercent;
    @CCD(label = "Migration cases percent")
    @JsonProperty("migratedTotalCasesPercent")
    private String migratedTotalCasesPercent;

}
