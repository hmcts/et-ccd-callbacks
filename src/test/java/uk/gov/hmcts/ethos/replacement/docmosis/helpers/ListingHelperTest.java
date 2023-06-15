package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantIndType;
import uk.gov.hmcts.et.common.model.ccd.types.DateListedType;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.et.common.model.ccd.types.RestrictedReportingType;
import uk.gov.hmcts.et.common.model.listing.ListingData;
import uk.gov.hmcts.et.common.model.listing.ListingDetails;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.BROUGHT_FORWARD_REPORT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CASES_COMPLETED_REPORT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CASE_SOURCE_LOCAL_REPORT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMS_ACCEPTED_REPORT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_LISTING_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_DOC_ETCL;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_DOC_IT56;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_DOC_IT57;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_ETCL_PRESS_LIST;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_ETCL_PUBLIC;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_ETCL_STAFF;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.IT56_TEMPLATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.IT57_TEMPLATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.LIVE_CASELOAD_REPORT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MEMBER_DAYS_REPORT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.PRESS_LIST_CAUSE_LIST_RANGE_TEMPLATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.PRESS_LIST_CAUSE_LIST_SINGLE_TEMPLATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.PUBLIC_CASE_CAUSE_LIST_ROOM_TEMPLATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.PUBLIC_CASE_CAUSE_LIST_TEMPLATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RANGE_HEARING_DATE_TYPE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_LISTING_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SERVING_CLAIMS_REPORT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SINGLE_HEARING_DATE_TYPE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.STAFF_CASE_CAUSE_LIST_ROOM_TEMPLATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.STAFF_CASE_CAUSE_LIST_TEMPLATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TIME_TO_FIRST_HEARING_REPORT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ecm.common.model.helper.TribunalOffice.GLASGOW;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.Constants.CASES_AWAITING_JUDGMENT_REPORT;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.Constants.HEARINGS_TO_JUDGEMENTS_REPORT;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.Constants.NO_CHANGE_IN_CURRENT_POSITION_REPORT;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.Constants.RESPONDENTS_REPORT;

class ListingHelperTest {

    private ListingDetails listingDetails;
    private ListingDetails listingDetails2;
    private ListingDetails listingDetails3;
    private ListingDetails listingDetails4;
    private UserDetails userDetails;

    @BeforeEach
    public void setUp() throws Exception {
        listingDetails = generateListingDetails("listingDetailsTest1.json");
        listingDetails2 = generateListingDetails("listingDetailsTest2.json");
        listingDetails3 = generateListingDetails("listingDetailsTest3.json");
        listingDetails4 = generateListingDetails("listingDetailsTest4.json");
        userDetails = HelperTest.getUserDetails();
    }

    private ListingDetails generateListingDetails(String jsonFileName) throws Exception {
        String json = new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(getClass().getClassLoader()
                .getResource(jsonFileName)).toURI())));
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, ListingDetails.class);
    }

    @Test
    void buildCaseCauseListByRoom() {
        String expected = "{\n"
                + "\"accessKey\":\"\",\n"
                + "\"templateName\":\"EM-TRB-SCO-ENG-00214.docx\",\n"
                + "\"outputName\":\"document.docx\",\n"
                + "\"data\":{\n"
                + "\"Court_addressLine1\":\"Manchester Employment Tribunal\",\n"
                + "\"Court_addressLine2\":\"Alexandra House\",\n"
                + "\"Court_addressLine3\":\"14-22 The Parsonage\",\n"
                + "\"Court_town\":\"Manchester\",\n"
                + "\"Court_county\":\"\",\n"
                + "\"Court_postCode\":\"M3 2JA\",\n"
                + "\"Court_fullAddress\":\"Manchester Employment Tribunal, Alexandra House, 14-22 The Parsonage, "
                + "Manchester, M3 2JA\",\n"
                + "\"Court_telephone\":\"03577131270\",\n"
                + "\"Court_fax\":\"07577126570\",\n"
                + "\"Court_DX\":\"123456\",\n"
                + "\"Court_Email\":\"ManchesterOfficeET@hmcts.gov.uk\",\n"
                + "\"listing_logo\":\"[userImage:enhmcts.png]\",\n"
                + "\"Office_name\":\"Manchester\",\n"
                + "\"Hearing_location\":\"Manchester\",\n"
                + "\"Listed_date\":\"12 October 2020\",\n"
                + "\"Clerk\":\"Mike Jordan\",\n"
                + "\"listing_date\":[\n"
                + "{\"date\":\"11 February 2020\",\n"
                + "\"location\":[\n"
                + "{\"Hearing_room\":\"Tribunal 2\",\n"
                + "\"listing\":[\n"
                + "{\"Judge\":\"Ms AM Judge\",\n"
                + "\"Court_addressLine1\":\"Manchester Employment Tribunal\",\n"
                + "\"Court_addressLine2\":\"Alexandra House\",\n"
                + "\"Court_addressLine3\":\"14-22 The Parsonage\",\n"
                + "\"Court_town\":\"Manchester\",\n"
                + "\"Court_county\":\"\",\n"
                + "\"Court_postCode\":\"M3 2JA\",\n"
                + "\"Court_fullAddress\":\"Manchester Employment Tribunal, Alexandra House, 14-22 The Parsonage, "
                + "Manchester, M3 2JA\",\n"
                + "\"Court_telephone\":\"03577131270\",\n"
                + "\"Court_fax\":\"07577126570\",\n"
                + "\"Court_DX\":\"123456\",\n"
                + "\"Court_Email\":\"ManchesterOfficeET@hmcts.gov.uk\",\n"
                + "\"listing_logo\":\"[userImage:enhmcts.png]\",\n"
                + "\"ERMember\":\" \",\n"
                + "\"EEMember\":\" \",\n"
                + "\"Case_No\":\"1112\",\n"
                + "\"Hearing_type\":\"Hearing\",\n"
                + "\"Jurisdictions\":\"ADG, DCD\",\n"
                + "\"Hearing_date\":\"11 February 2020\",\n"
                + "\"Hearing_date_time\":\"11 February 2020 at 00:00\",\n"
                + "\"Hearing_time\":\"00:00\",\n"
                + "\"Hearing_duration\":\"12 Days\",\n"
                + "\"Hearing_clerk\":\"Andrew Pearl\",\n"
                + "\"Claimant\":\"Mr s sdfs\",\n"
                + "\"claimant_town\":\"claimantTown1\",\n"
                + "\"claimant_representative\":\"Rep2\",\n"
                + "\"Respondent\":\"sdf2\",\n"
                + "\"resp_others\":\"Mark Taylor\\nTony Jones\",\n"
                + "\"respondent_town\":\"respondentTown1\",\n"
                + "\"Hearing_location\":\"Manchester\",\n"
                + "\"Hearing_room\":\"Tribunal 2\",\n"
                + "\"Hearing_dayofdays\":\"2 of 3\",\n"
                + "\"Hearing_panel\":\"\",\n"
                + "\"Hearing_notes\":\"Notes2\",\n"
                + "\"Judicial_mediation\":\"\",\n"
                + "\"Reading_deliberation_day\":\"\",\n"
                + "\"Hearing_format\":\"\",\n"
                + "\"respondent_representative\":\"Org2\"}]\n"
                + "}],\n"
                + "},\n"
                + "{\"date\":\"12 October 2020\",\n"
                + "\"location\":[\n"
                + "{\"Hearing_room\":\"* Not Allocated\",\n"
                + "\"listing\":[\n"
                + "{\"Judge\":\"Ms AM Aspden\",\n"
                + "\"Court_addressLine1\":\"Manchester Employment Tribunal\",\n"
                + "\"Court_addressLine2\":\"Alexandra House\",\n"
                + "\"Court_addressLine3\":\"14-22 The Parsonage\",\n"
                + "\"Court_town\":\"Manchester\",\n"
                + "\"Court_county\":\"\",\n"
                + "\"Court_postCode\":\"M3 2JA\",\n"
                + "\"Court_fullAddress\":\"Manchester Employment Tribunal, Alexandra House, 14-22 The Parsonage, "
                + "Manchester, M3 2JA\",\n"
                + "\"Court_telephone\":\"03577131270\",\n"
                + "\"Court_fax\":\"07577126570\",\n"
                + "\"Court_DX\":\"123456\",\n"
                + "\"Court_Email\":\"ManchesterOfficeET@hmcts.gov.uk\",\n"
                + "\"listing_logo\":\"[userImage:enhmcts.png]\",\n"
                + "\"ERMember\":\" \",\n"
                + "\"EEMember\":\" \",\n"
                + "\"Case_No\":\"1112\",\n"
                + "\"Hearing_type\":\"Hearing\",\n"
                + "\"Jurisdictions\":\"ADG, COM\",\n"
                + "\"Hearing_date\":\"12 October 2020\",\n"
                + "\"Hearing_date_time\":\"12 October 2020 at 00:00\",\n"
                + "\"Hearing_time\":\"00:00\",\n"
                + "\"Hearing_duration\":\"12 Days\",\n"
                + "\"Hearing_clerk\":\"Anne Fox\",\n"
                + "\"Claimant\":\"Mr s sdfs\",\n"
                + "\"claimant_town\":\"claimantTown\",\n"
                + "\"claimant_representative\":\"Rep\",\n"
                + "\"Respondent\":\"sdf\",\n"
                + "\"resp_others\":\"Mark Taylor\\nTony Jones\\nSteve Thomas\",\n"
                + "\"respondent_town\":\"respondentTown\",\n"
                + "\"Hearing_location\":\"Manchester\",\n"
                + "\"Hearing_room\":\"\",\n"
                + "\"Hearing_dayofdays\":\"1 of 3\",\n"
                + "\"Hearing_panel\":\"Panel\",\n"
                + "\"Hearing_notes\":\"Notes with  -  new line\",\n"
                + "\"Judicial_mediation\":\"\",\n"
                + "\"Reading_deliberation_day\":\"\",\n"
                + "\"Hearing_format\":\"\",\n"
                + "\"respondent_representative\":\"Org\"}]\n"
                + "}],\n"
                + "},\n"
                + "{\"date\":\"14 December 2020\",\n"
                + "\"location\":[\n"
                + "{\"Hearing_room\":\"Tribunal 4\",\n"
                + "\"listing\":[\n"
                + "{\"Judge\":\"Another Judge\",\n"
                + "\"Court_addressLine1\":\"Manchester Employment Tribunal\",\n"
                + "\"Court_addressLine2\":\"Alexandra House\",\n"
                + "\"Court_addressLine3\":\"14-22 The Parsonage\",\n"
                + "\"Court_town\":\"Manchester\",\n"
                + "\"Court_county\":\"\",\n"
                + "\"Court_postCode\":\"M3 2JA\",\n"
                + "\"Court_fullAddress\":\"Manchester Employment Tribunal, Alexandra House, 14-22 The Parsonage, "
                + "Manchester, M3 2JA\",\n"
                + "\"Court_telephone\":\"03577131270\",\n"
                + "\"Court_fax\":\"07577126570\",\n"
                + "\"Court_DX\":\"123456\",\n"
                + "\"Court_Email\":\"ManchesterOfficeET@hmcts.gov.uk\",\n"
                + "\"listing_logo\":\"[userImage:enhmcts.png]\",\n"
                + "\"ERMember\":\" \",\n"
                + "\"EEMember\":\" \",\n"
                + "\"Case_No\":\"1112\",\n"
                + "\"Hearing_type\":\"Preliminary Hearing (CM)\",\n"
                + "\"Jurisdictions\":\"ADG, COM\",\n"
                + "\"Hearing_date\":\"14 December 2020\",\n"
                + "\"Hearing_date_time\":\"14 December 2020 at 00:00\",\n"
                + "\"Hearing_time\":\"00:00\",\n"
                + "\"Hearing_duration\":\"3 Minutes\",\n"
                + "\"Hearing_clerk\":\"Juan Fox\",\n"
                + "\"Claimant\":\"Mr s Dominguez\",\n"
                + "\"claimant_town\":\"\",\n"
                + "\"claimant_representative\":\"Representative\",\n"
                + "\"Respondent\":\"Respondent\",\n"
                + "\"resp_others\":\"\",\n"
                + "\"respondent_town\":\"\",\n"
                + "\"Hearing_location\":\"Manchester\",\n"
                + "\"Hearing_room\":\"Tribunal 4\",\n"
                + "\"Hearing_dayofdays\":\"2 of 3\",\n"
                + "\"Hearing_panel\":\"Panel\",\n"
                + "\"Hearing_notes\":\"Notes4\",\n"
                + "\"Judicial_mediation\":\"\",\n"
                + "\"Reading_deliberation_day\":\"\",\n"
                + "\"Hearing_format\":\"\",\n"
                + "\"respondent_representative\":\"Organization\"},\n"
                + "{\"Judge\":\"Judge For Tribunal4\",\n"
                + "\"Court_addressLine1\":\"Manchester Employment Tribunal\",\n"
                + "\"Court_addressLine2\":\"Alexandra House\",\n"
                + "\"Court_addressLine3\":\"14-22 The Parsonage\",\n"
                + "\"Court_town\":\"Manchester\",\n"
                + "\"Court_county\":\"\",\n"
                + "\"Court_postCode\":\"M3 2JA\",\n"
                + "\"Court_fullAddress\":\"Manchester Employment Tribunal, Alexandra House, 14-22 The Parsonage, "
                + "Manchester, M3 2JA\",\n"
                + "\"Court_telephone\":\"03577131270\",\n"
                + "\"Court_fax\":\"07577126570\",\n"
                + "\"Court_DX\":\"123456\",\n"
                + "\"Court_Email\":\"ManchesterOfficeET@hmcts.gov.uk\",\n"
                + "\"listing_logo\":\"[userImage:enhmcts.png]\",\n"
                + "\"ERMember\":\" \",\n"
                + "\"EEMember\":\" \",\n"
                + "\"Case_No\":\"1112\",\n"
                + "\"Hearing_type\":\"Preliminary Hearing (CM)\",\n"
                + "\"Jurisdictions\":\"ADG, COM\",\n"
                + "\"Hearing_date\":\"14 December 2020\",\n"
                + "\"Hearing_date_time\":\"14 December 2020 at 01:00\",\n"
                + "\"Hearing_time\":\"01:00\",\n"
                + "\"Hearing_duration\":\"12 Minutes\",\n"
                + "\"Hearing_clerk\":\"Anne Fox\",\n"
                + "\"Claimant\":\"Mr s sdfs\",\n"
                + "\"claimant_town\":\"claimantTown2\",\n"
                + "\"claimant_representative\":\"Rep\",\n"
                + "\"Respondent\":\"sdf\",\n"
                + "\"resp_others\":\"Mark Taylor\",\n"
                + "\"respondent_town\":\"respondentTown2\",\n"
                + "\"Hearing_location\":\"Manchester\",\n"
                + "\"Hearing_room\":\"Tribunal 4\",\n"
                + "\"Hearing_dayofdays\":\"2 of 3\",\n"
                + "\"Hearing_panel\":\"\",\n"
                + "\"Hearing_notes\":\"Notes3\",\n"
                + "\"Judicial_mediation\":\"\",\n"
                + "\"Reading_deliberation_day\":\"\",\n"
                + "\"Hearing_format\":\"\",\n"
                + "\"respondent_representative\":\"Org\"}]\n"
                + "}],\n"
                + "}],\n"
                + "\"Today_date\":\"" + UtilHelper.formatCurrentDate(LocalDate.now()) + "\"\n"
                + "}\n"
                + "}\n";
        listingDetails.getCaseData().setManagingOffice(TribunalOffice.MANCHESTER.getOfficeName());
        listingDetails.getCaseData().getListingCollection().get(0).getValue().setHearingNotes("Notes with \n new line");
        assertEquals(expected, ListingHelper.buildListingDocumentContent(listingDetails.getCaseData(),
                "", PUBLIC_CASE_CAUSE_LIST_ROOM_TEMPLATE, userDetails,
                ENGLANDWALES_LISTING_CASE_TYPE_ID).toString());
    }

    @Test
    void buildCaseCauseListWithNoDocument() {
        String expected = "{\n"
                + "\"accessKey\":\"\",\n"
                + "\"templateName\":\".docx\",\n"
                + "\"outputName\":\"document.docx\",\n"
                + "\"data\":{\n"
                + "\"Court_addressLine1\":\"Manchester Employment Tribunal\",\n"
                + "\"Court_addressLine2\":\"Alexandra House\",\n"
                + "\"Court_addressLine3\":\"14-22 The Parsonage\",\n"
                + "\"Court_town\":\"Manchester\",\n"
                + "\"Court_county\":\"\",\n"
                + "\"Court_postCode\":\"M3 2JA\",\n"
                + "\"Court_fullAddress\":\"Manchester Employment Tribunal, Alexandra House, 14-22 The Parsonage, "
                + "Manchester, M3 2JA\",\n"
                + "\"Court_telephone\":\"03577131270\",\n"
                + "\"Court_fax\":\"07577126570\",\n"
                + "\"Court_DX\":\"123456\",\n"
                + "\"Court_Email\":\"ManchesterOfficeET@hmcts.gov.uk\",\n"
                + "\"listing_logo\":\"[userImage:enhmcts.png]\",\n"
                + "\"Office_name\":\"London Central\",\n"
                + "\"Hearing_location\":\"Manchester\",\n"
                + "\"Listed_date\":\"12 October 2020\",\n"
                + "\"Clerk\":\"Mike Jordan\",\n"
                + "\"Today_date\":\"" + UtilHelper.formatCurrentDate(LocalDate.now()) + "\"\n"
                + "}\n"
                + "}\n";
        listingDetails.getCaseData().setManagingOffice(TribunalOffice.LONDON_CENTRAL.getOfficeName());
        assertEquals(expected, ListingHelper.buildListingDocumentContent(listingDetails.getCaseData(),
                "", "", userDetails, ENGLANDWALES_LISTING_CASE_TYPE_ID).toString());
    }

    @Test
    void buildCaseCauseListWithNoDocumentAndRangeAndScotland() {
        listingDetails.getCaseData().setHearingDateType(RANGE_HEARING_DATE_TYPE);
        listingDetails.getCaseData().setListingDateFrom("2020-01-02");
        listingDetails.getCaseData().setListingDateTo("2020-03-01");
        listingDetails.getCaseData().setManagingOffice(TribunalOffice.SCOTLAND.getOfficeName());
        String expected = "{\n"
                + "\"accessKey\":\"\",\n"
                + "\"templateName\":\".docx\",\n"
                + "\"outputName\":\"document.docx\",\n"
                + "\"data\":{\n"
                + "\"Court_addressLine1\":\"Manchester Employment Tribunal\",\n"
                + "\"Court_addressLine2\":\"Alexandra House\",\n"
                + "\"Court_addressLine3\":\"14-22 The Parsonage\",\n"
                + "\"Court_town\":\"Manchester\",\n"
                + "\"Court_county\":\"\",\n"
                + "\"Court_postCode\":\"M3 2JA\",\n"
                + "\"Court_fullAddress\":\"Manchester Employment Tribunal, Alexandra House, 14-22 The Parsonage, "
                + "Manchester, M3 2JA\",\n"
                + "\"Court_telephone\":\"03577131270\",\n"
                + "\"Court_fax\":\"07577126570\",\n"
                + "\"Court_DX\":\"123456\",\n"
                + "\"Court_Email\":\"ManchesterOfficeET@hmcts.gov.uk\",\n"
                + "\"listing_logo\":\"[userImage:schmcts.png]\",\n"
                + "\"Office_name\":\"Scotland\",\n"
                + "\"Hearing_location\":\"Manchester\",\n"
                + "\"Listed_date_from\":\"2 January 2020\",\n"
                + "\"Listed_date_to\":\"1 March 2020\",\n"                + "\"Clerk\":\"Mike Jordan\",\n"
                + "\"Today_date\":\"" + UtilHelper.formatCurrentDate(LocalDate.now()) + "\"\n"
                + "}\n"
                + "}\n";
        assertEquals(expected, ListingHelper.buildListingDocumentContent(listingDetails.getCaseData(),
                "", "", userDetails, SCOTLAND_LISTING_CASE_TYPE_ID).toString());
    }

    @Test
    void buildCaseCauseList() {
        String expected = "{\n"
                + "\"accessKey\":\"\",\n"
                + "\"templateName\":\"EM-TRB-SCO-ENG-00212.docx\",\n"
                + "\"outputName\":\"document.docx\",\n"
                + "\"data\":{\n"
                + "\"Court_addressLine1\":\"Manchester Employment Tribunal\",\n"
                + "\"Court_addressLine2\":\"Alexandra House\",\n"
                + "\"Court_addressLine3\":\"14-22 The Parsonage\",\n"
                + "\"Court_town\":\"Manchester\",\n"
                + "\"Court_county\":\"\",\n"
                + "\"Court_postCode\":\"M3 2JA\",\n"
                + "\"Court_fullAddress\":\"Manchester Employment Tribunal, Alexandra House, 14-22 The Parsonage, "
                + "Manchester, M3 2JA\",\n"
                + "\"Court_telephone\":\"03577131270\",\n"
                + "\"Court_fax\":\"07577126570\",\n"
                + "\"Court_DX\":\"123456\",\n"
                + "\"Court_Email\":\"ManchesterOfficeET@hmcts.gov.uk\",\n"
                + "\"listing_logo\":\"[userImage:enhmcts.png]\",\n"
                + "\"Office_name\":\"Manchester\",\n"
                + "\"Hearing_location\":\"Manchester\",\n"
                + "\"Listed_date\":\"12 October 2020\",\n"
                + "\"Clerk\":\"Mike Jordan\",\n"
                + "\"listing_date\":[\n"
                + "{\"date\":\"11 February 2020\",\n"
                + "\"case_total\":\"1\",\n"
                + "\"listing\":[\n"
                + "{\"Judge\":\"Ms AM Judge\",\n"
                + "\"Court_addressLine1\":\"Manchester Employment Tribunal\",\n"
                + "\"Court_addressLine2\":\"Alexandra House\",\n"
                + "\"Court_addressLine3\":\"14-22 The Parsonage\",\n"
                + "\"Court_town\":\"Manchester\",\n"
                + "\"Court_county\":\"\",\n"
                + "\"Court_postCode\":\"M3 2JA\",\n"
                + "\"Court_fullAddress\":\"Manchester Employment Tribunal, Alexandra House, 14-22 The Parsonage, "
                + "Manchester, M3 2JA\",\n"
                + "\"Court_telephone\":\"03577131270\",\n"
                + "\"Court_fax\":\"07577126570\",\n"
                + "\"Court_DX\":\"123456\",\n"
                + "\"Court_Email\":\"ManchesterOfficeET@hmcts.gov.uk\",\n"
                + "\"listing_logo\":\"[userImage:enhmcts.png]\",\n"
                + "\"ERMember\":\" \",\n"
                + "\"EEMember\":\" \",\n"
                + "\"Case_No\":\"1112\",\n"
                + "\"Hearing_type\":\"Hearing\",\n"
                + "\"Jurisdictions\":\"ADG, DCD\",\n"
                + "\"Hearing_date\":\"11 February 2020\",\n"
                + "\"Hearing_date_time\":\"11 February 2020 at 00:00\",\n"
                + "\"Hearing_time\":\"00:00\",\n"
                + "\"Hearing_duration\":\"12 Days\",\n"
                + "\"Hearing_clerk\":\"Andrew Pearl\",\n"
                + "\"Claimant\":\"Mr s sdfs\",\n"
                + "\"claimant_town\":\"claimantTown1\",\n"
                + "\"claimant_representative\":\"Rep2\",\n"
                + "\"Respondent\":\"sdf2\",\n"
                + "\"resp_others\":\"Mark Taylor\\nTony Jones\",\n"
                + "\"respondent_town\":\"respondentTown1\",\n"
                + "\"Hearing_location\":\"Manchester\",\n"
                + "\"Hearing_room\":\"Tribunal 2\",\n"
                + "\"Hearing_dayofdays\":\"2 of 3\",\n"
                + "\"Hearing_panel\":\"\",\n"
                + "\"Hearing_notes\":\"Notes2\",\n"
                + "\"Judicial_mediation\":\"\",\n"
                + "\"Reading_deliberation_day\":\"\",\n"
                + "\"Hearing_format\":\"\",\n"
                + "\"respondent_representative\":\"Org2\"}]\n"
                + "},\n"
                + "{\"date\":\"12 October 2020\",\n"
                + "\"case_total\":\"1\",\n"
                + "\"listing\":[\n"
                + "{\"Judge\":\"Ms AM Aspden\",\n"
                + "\"Court_addressLine1\":\"Manchester Employment Tribunal\",\n"
                + "\"Court_addressLine2\":\"Alexandra House\",\n"
                + "\"Court_addressLine3\":\"14-22 The Parsonage\",\n"
                + "\"Court_town\":\"Manchester\",\n"
                + "\"Court_county\":\"\",\n"
                + "\"Court_postCode\":\"M3 2JA\",\n"
                + "\"Court_fullAddress\":\"Manchester Employment Tribunal, Alexandra House, 14-22 The Parsonage, "
                + "Manchester, M3 2JA\",\n"
                + "\"Court_telephone\":\"03577131270\",\n"
                + "\"Court_fax\":\"07577126570\",\n"
                + "\"Court_DX\":\"123456\",\n"
                + "\"Court_Email\":\"ManchesterOfficeET@hmcts.gov.uk\",\n"
                + "\"listing_logo\":\"[userImage:enhmcts.png]\",\n"
                + "\"ERMember\":\" \",\n"
                + "\"EEMember\":\" \",\n"
                + "\"Case_No\":\"1112\",\n"
                + "\"Hearing_type\":\"Hearing\",\n"
                + "\"Jurisdictions\":\"ADG, COM\",\n"
                + "\"Hearing_date\":\"12 October 2020\",\n"
                + "\"Hearing_date_time\":\"12 October 2020 at 00:00\",\n"
                + "\"Hearing_time\":\"00:00\",\n"
                + "\"Hearing_duration\":\"12 Days\",\n"
                + "\"Hearing_clerk\":\"Anne Fox\",\n"
                + "\"Claimant\":\"Mr s sdfs\",\n"
                + "\"claimant_town\":\"claimantTown\",\n"
                + "\"claimant_representative\":\"Rep\",\n"
                + "\"Respondent\":\"sdf\",\n"
                + "\"resp_others\":\"Mark Taylor\\nTony Jones\\nSteve Thomas\",\n"
                + "\"respondent_town\":\"respondentTown\",\n"
                + "\"Hearing_location\":\"Manchester\",\n"
                + "\"Hearing_room\":\"\",\n"
                + "\"Hearing_dayofdays\":\"1 of 3\",\n"
                + "\"Hearing_panel\":\"Panel\",\n"
                + "\"Hearing_notes\":\"Notes1\",\n"
                + "\"Judicial_mediation\":\"\",\n"
                + "\"Reading_deliberation_day\":\"\",\n"
                + "\"Hearing_format\":\"\",\n"
                + "\"respondent_representative\":\"Org\"}]\n"
                + "},\n"
                + "{\"date\":\"14 December 2020\",\n"
                + "\"case_total\":\"2\",\n"
                + "\"listing\":[\n"
                + "{\"Judge\":\"Another Judge\",\n"
                + "\"Court_addressLine1\":\"Manchester Employment Tribunal\",\n"
                + "\"Court_addressLine2\":\"Alexandra House\",\n"
                + "\"Court_addressLine3\":\"14-22 The Parsonage\",\n"
                + "\"Court_town\":\"Manchester\",\n"
                + "\"Court_county\":\"\",\n"
                + "\"Court_postCode\":\"M3 2JA\",\n"
                + "\"Court_fullAddress\":\"Manchester Employment Tribunal, Alexandra House, 14-22 The Parsonage, "
                + "Manchester, M3 2JA\",\n"
                + "\"Court_telephone\":\"03577131270\",\n"
                + "\"Court_fax\":\"07577126570\",\n"
                + "\"Court_DX\":\"123456\",\n"
                + "\"Court_Email\":\"ManchesterOfficeET@hmcts.gov.uk\",\n"
                + "\"listing_logo\":\"[userImage:enhmcts.png]\",\n"
                + "\"ERMember\":\" \",\n"
                + "\"EEMember\":\" \",\n"
                + "\"Case_No\":\"1112\",\n"
                + "\"Hearing_type\":\"Preliminary Hearing (CM)\",\n"
                + "\"Jurisdictions\":\"ADG, COM\",\n"
                + "\"Hearing_date\":\"14 December 2020\",\n"
                + "\"Hearing_date_time\":\"14 December 2020 at 00:00\",\n"
                + "\"Hearing_time\":\"00:00\",\n"
                + "\"Hearing_duration\":\"3 Minutes\",\n"
                + "\"Hearing_clerk\":\"Juan Fox\",\n"
                + "\"Claimant\":\"Mr s Dominguez\",\n"
                + "\"claimant_town\":\"\",\n"
                + "\"claimant_representative\":\"Representative\",\n"
                + "\"Respondent\":\"Respondent\",\n"
                + "\"resp_others\":\"\",\n"
                + "\"respondent_town\":\"\",\n"
                + "\"Hearing_location\":\"Manchester\",\n"
                + "\"Hearing_room\":\"Tribunal 4\",\n"
                + "\"Hearing_dayofdays\":\"2 of 3\",\n"
                + "\"Hearing_panel\":\"Panel\",\n"
                + "\"Hearing_notes\":\"Notes4\",\n"
                + "\"Judicial_mediation\":\"\",\n"
                + "\"Reading_deliberation_day\":\"\",\n"
                + "\"Hearing_format\":\"\",\n"
                + "\"respondent_representative\":\"Organization\"},\n"
                + "{\"Judge\":\"Judge For Tribunal4\",\n"
                + "\"Court_addressLine1\":\"Manchester Employment Tribunal\",\n"
                + "\"Court_addressLine2\":\"Alexandra House\",\n"
                + "\"Court_addressLine3\":\"14-22 The Parsonage\",\n"
                + "\"Court_town\":\"Manchester\",\n"
                + "\"Court_county\":\"\",\n"
                + "\"Court_postCode\":\"M3 2JA\",\n"
                + "\"Court_fullAddress\":\"Manchester Employment Tribunal, Alexandra House, 14-22 The Parsonage, "
                + "Manchester, M3 2JA\",\n"
                + "\"Court_telephone\":\"03577131270\",\n"
                + "\"Court_fax\":\"07577126570\",\n"
                + "\"Court_DX\":\"123456\",\n"
                + "\"Court_Email\":\"ManchesterOfficeET@hmcts.gov.uk\",\n"
                + "\"listing_logo\":\"[userImage:enhmcts.png]\",\n"
                + "\"ERMember\":\" \",\n"
                + "\"EEMember\":\" \",\n"
                + "\"Case_No\":\"1112\",\n"
                + "\"Hearing_type\":\"Preliminary Hearing (CM)\",\n"
                + "\"Jurisdictions\":\"ADG, COM\",\n"
                + "\"Hearing_date\":\"14 December 2020\",\n"
                + "\"Hearing_date_time\":\"14 December 2020 at 01:00\",\n"
                + "\"Hearing_time\":\"01:00\",\n"
                + "\"Hearing_duration\":\"12 Minutes\",\n"
                + "\"Hearing_clerk\":\"Anne Fox\",\n"
                + "\"Claimant\":\"Mr s sdfs\",\n"
                + "\"claimant_town\":\"claimantTown2\",\n"
                + "\"claimant_representative\":\"Rep\",\n"
                + "\"Respondent\":\"sdf\",\n"
                + "\"resp_others\":\"Mark Taylor\",\n"
                + "\"respondent_town\":\"respondentTown2\",\n"
                + "\"Hearing_location\":\"Manchester\",\n"
                + "\"Hearing_room\":\"Tribunal 4\",\n"
                + "\"Hearing_dayofdays\":\"2 of 3\",\n"
                + "\"Hearing_panel\":\"\",\n"
                + "\"Hearing_notes\":\"Notes3\",\n"
                + "\"Judicial_mediation\":\"\",\n"
                + "\"Reading_deliberation_day\":\"\",\n"
                + "\"Hearing_format\":\"\",\n"
                + "\"respondent_representative\":\"Org\"}]\n"
                + "}],\n"
                + "\"Today_date\":\"" + UtilHelper.formatCurrentDate(LocalDate.now()) + "\"\n"
                + "}\n"
                + "}\n";
        listingDetails.getCaseData().setManagingOffice(TribunalOffice.MANCHESTER.getOfficeName());
        assertEquals(expected, ListingHelper.buildListingDocumentContent(listingDetails.getCaseData(), "",
                PUBLIC_CASE_CAUSE_LIST_TEMPLATE, userDetails, ENGLANDWALES_LISTING_CASE_TYPE_ID).toString());
    }

    @Test
    void buildCaseCauseList2() {
        String expected = "{\n"
                + "\"accessKey\":\"\",\n"
                + "\"templateName\":\"EM-TRB-SCO-ENG-00212.docx\",\n"
                + "\"outputName\":\"document.docx\",\n"
                + "\"data\":{\n"
                + "\"Court_addressLine1\":\"Manchester Employment Tribunal\",\n"
                + "\"Court_addressLine2\":\"Alexandra House\",\n"
                + "\"Court_addressLine3\":\"14-22 The Parsonage\",\n"
                + "\"Court_town\":\"Manchester\",\n"
                + "\"Court_county\":\"\",\n"
                + "\"Court_postCode\":\"M3 2JA\",\n"
                + "\"Court_fullAddress\":\"Manchester Employment Tribunal, Alexandra House, 14-22 The Parsonage, "
                + "Manchester, M3 2JA\",\n"
                + "\"Court_telephone\":\"03577131270\",\n"
                + "\"Court_fax\":\"07577126570\",\n"
                + "\"Court_DX\":\"123456\",\n"
                + "\"Court_Email\":\"ManchesterOfficeET@hmcts.gov.uk\",\n"
                + "\"listing_logo\":\"[userImage:enhmcts.png]\",\n"
                + "\"Office_name\":\"Manchester\",\n"
                + "\"Hearing_location\":\"Manchester\",\n"
                + "\"Listed_date\":\"12 October 2020\",\n"
                + "\"Clerk\":\"Mike Jordan\",\n"
                + "\"listing_date\":[\n"
                + "{\"date\":\"16 January 2020\",\n"
                + "\"case_total\":\"1\",\n"
                + "\"listing\":[\n"
                + "{\"Judge\":\"\",\n"
                + "\"Court_addressLine1\":\"Manchester Employment Tribunal\",\n"
                + "\"Court_addressLine2\":\"Alexandra House\",\n"
                + "\"Court_addressLine3\":\"14-22 The Parsonage\",\n"
                + "\"Court_town\":\"Manchester\",\n"
                + "\"Court_county\":\"\",\n"
                + "\"Court_postCode\":\"M3 2JA\",\n"
                + "\"Court_fullAddress\":\"Manchester Employment Tribunal, Alexandra House, 14-22 The Parsonage, "
                + "Manchester, M3 2JA\",\n"
                + "\"Court_telephone\":\"03577131270\",\n"
                + "\"Court_fax\":\"07577126570\",\n"
                + "\"Court_DX\":\"123456\",\n"
                + "\"Court_Email\":\"ManchesterOfficeET@hmcts.gov.uk\",\n"
                + "\"listing_logo\":\"[userImage:enhmcts.png]\",\n"
                + "\"ERMember\":\" \",\n"
                + "\"EEMember\":\" \",\n"
                + "\"Case_No\":\"1112\",\n"
                + "\"Hearing_type\":\"Preliminary Hearing (CM)\",\n"
                + "\"Jurisdictions\":\"ADG, COM\",\n"
                + "\"Hearing_date\":\"16 January 2020\",\n"
                + "\"Hearing_date_time\":\"16 January 2020 at 00:00\",\n"
                + "\"Hearing_time\":\"00:00\",\n"
                + "\"Hearing_duration\":\"12 Minutes\",\n"
                + "\"Hearing_clerk\":\"Anne Fox\",\n"
                + "\"Claimant\":\"Mr s sdfs\",\n"
                + "\"claimant_town\":\"claimantTown2\",\n"
                + "\"claimant_representative\":\"Rep\",\n"
                + "\"Respondent\":\"sdf\",\n"
                + "\"resp_others\":\"Mark Taylor\",\n"
                + "\"respondent_town\":\"respondentTown2\",\n"
                + "\"Hearing_location\":\"Manchester\",\n"
                + "\"Hearing_room\":\"Tribunal 4\",\n"
                + "\"Hearing_dayofdays\":\"2 of 3\",\n"
                + "\"Hearing_panel\":\"\",\n"
                + "\"Hearing_notes\":\"Notes3\",\n"
                + "\"Judicial_mediation\":\"\",\n"
                + "\"Reading_deliberation_day\":\"\",\n"
                + "\"Hearing_format\":\"\",\n"
                + "\"respondent_representative\":\"Org\"}]\n"
                + "},\n"
                + "{\"date\":\"12 October 2020\",\n"
                + "\"case_total\":\"1\",\n"
                + "\"listing\":[\n"
                + "{\"Judge\":\"\",\n"
                + "\"Court_addressLine1\":\"Manchester Employment Tribunal\",\n"
                + "\"Court_addressLine2\":\"Alexandra House\",\n"
                + "\"Court_addressLine3\":\"14-22 The Parsonage\",\n"
                + "\"Court_town\":\"Manchester\",\n"
                + "\"Court_county\":\"\",\n"
                + "\"Court_postCode\":\"M3 2JA\",\n"
                + "\"Court_fullAddress\":\"Manchester Employment Tribunal, Alexandra House, 14-22 The Parsonage, "
                + "Manchester, M3 2JA\",\n"
                + "\"Court_telephone\":\"03577131270\",\n"
                + "\"Court_fax\":\"07577126570\",\n"
                + "\"Court_DX\":\"123456\",\n"
                + "\"Court_Email\":\"ManchesterOfficeET@hmcts.gov.uk\",\n"
                + "\"listing_logo\":\"[userImage:enhmcts.png]\",\n"
                + "\"ERMember\":\" \",\n"
                + "\"EEMember\":\" \",\n"
                + "\"Case_No\":\"1112\",\n"
                + "\"Hearing_type\":\"Hearing\",\n"
                + "\"Jurisdictions\":\"ADG, COM\",\n"
                + "\"Hearing_date\":\"12 October 2020\",\n"
                + "\"Hearing_date_time\":\"12 October 2020 at 00:00\",\n"
                + "\"Hearing_time\":\"00:00\",\n"
                + "\"Hearing_duration\":\"12 Days\",\n"
                + "\"Hearing_clerk\":\"Anne Fox\",\n"
                + "\"Claimant\":\"Mr s sdfs\",\n"
                + "\"claimant_town\":\"claimantTown\",\n"
                + "\"claimant_representative\":\"Rep\",\n"
                + "\"Respondent\":\"sdf\",\n"
                + "\"resp_others\":\"Mark Taylor\\nTony Jones\\nSteve Thomas\",\n"
                + "\"respondent_town\":\"respondentTown\",\n"
                + "\"Hearing_location\":\"Manchester\",\n"
                + "\"Hearing_room\":\"Tribunal 2\",\n"
                + "\"Hearing_dayofdays\":\"1 of 3\",\n"
                + "\"Hearing_panel\":\"Panel\",\n"
                + "\"Hearing_notes\":\"Notes1\",\n"
                + "\"Judicial_mediation\":\"\",\n"
                + "\"Reading_deliberation_day\":\"\",\n"
                + "\"Hearing_format\":\"\",\n"
                + "\"respondent_representative\":\"Org\"}]\n"
                + "},\n"
                + "{\"date\":\"14 October 2020\",\n"
                + "\"case_total\":\"1\",\n"
                + "\"listing\":[\n"
                + "{\"Judge\":\"\",\n"
                + "\"Court_addressLine1\":\"Manchester Employment Tribunal\",\n"
                + "\"Court_addressLine2\":\"Alexandra House\",\n"
                + "\"Court_addressLine3\":\"14-22 The Parsonage\",\n"
                + "\"Court_town\":\"Manchester\",\n"
                + "\"Court_county\":\"\",\n"
                + "\"Court_postCode\":\"M3 2JA\",\n"
                + "\"Court_fullAddress\":\"Manchester Employment Tribunal, Alexandra House, 14-22 The Parsonage, "
                + "Manchester, M3 2JA\",\n"
                + "\"Court_telephone\":\"03577131270\",\n"
                + "\"Court_fax\":\"07577126570\",\n"
                + "\"Court_DX\":\"123456\",\n"
                + "\"Court_Email\":\"ManchesterOfficeET@hmcts.gov.uk\",\n"
                + "\"listing_logo\":\"[userImage:enhmcts.png]\",\n"
                + "\"ERMember\":\" \",\n"
                + "\"EEMember\":\" \",\n"
                + "\"Case_No\":\"1112\",\n"
                + "\"Hearing_type\":\"Hearing\",\n"
                + "\"Jurisdictions\":\"ADG, DCD\",\n"
                + "\"Hearing_date\":\"14 October 2020\",\n"
                + "\"Hearing_date_time\":\"14 October 2020 at 00:00\",\n"
                + "\"Hearing_time\":\"00:00\",\n"
                + "\"Hearing_duration\":\"12 Days\",\n"
                + "\"Hearing_clerk\":\"Andrew Pearl\",\n"
                + "\"Claimant\":\"Mr s sdfs\",\n"
                + "\"claimant_town\":\"claimantTown1\",\n"
                + "\"claimant_representative\":\"Rep2\",\n"
                + "\"Respondent\":\"sdf2\",\n"
                + "\"resp_others\":\"Mark Taylor\\nTony Jones\",\n"
                + "\"respondent_town\":\"respondentTown1\",\n"
                + "\"Hearing_location\":\"Leeds\",\n"
                + "\"Hearing_room\":\"Tribunal 2\",\n"
                + "\"Hearing_dayofdays\":\"2 of 3\",\n"
                + "\"Hearing_panel\":\"\",\n"
                + "\"Hearing_notes\":\"Notes2\",\n"
                + "\"Judicial_mediation\":\"\",\n"
                + "\"Reading_deliberation_day\":\"\",\n"
                + "\"Hearing_format\":\"\",\n"
                + "\"respondent_representative\":\"Org2\"}]\n"
                + "}],\n"
                + "\"Today_date\":\"" + UtilHelper.formatCurrentDate(LocalDate.now()) + "\"\n"
                + "}\n"
                + "}\n";
        listingDetails2.getCaseData().setManagingOffice(TribunalOffice.MANCHESTER.getOfficeName());
        assertEquals(expected, ListingHelper.buildListingDocumentContent(listingDetails2.getCaseData(), "",
                PUBLIC_CASE_CAUSE_LIST_TEMPLATE, userDetails, ENGLANDWALES_LISTING_CASE_TYPE_ID).toString());
    }

    @Test
    void buildCaseCauseIt56() {
        String expected = "{\n"
                + "\"accessKey\":\"\",\n"
                + "\"templateName\":\"EM-TRB-SCO-ENG-00210.docx\",\n"
                + "\"outputName\":\"document.docx\",\n"
                + "\"data\":{\n"
                + "\"Court_addressLine1\":\"Manchester Employment Tribunal\",\n"
                + "\"Court_addressLine2\":\"Alexandra House\",\n"
                + "\"Court_addressLine3\":\"14-22 The Parsonage\",\n"
                + "\"Court_town\":\"Manchester\",\n"
                + "\"Court_county\":\"\",\n"
                + "\"Court_postCode\":\"M3 2JA\",\n"
                + "\"Court_fullAddress\":\"Manchester Employment Tribunal, Alexandra House, 14-22 The Parsonage, "
                + "Manchester, M3 2JA\",\n"
                + "\"Court_telephone\":\"03577131270\",\n"
                + "\"Court_fax\":\"07577126570\",\n"
                + "\"Court_DX\":\"123456\",\n"
                + "\"Court_Email\":\"ManchesterOfficeET@hmcts.gov.uk\",\n"
                + "\"listing_logo\":\"[userImage:enhmcts.png]\",\n"
                + "\"Office_name\":\"Manchester\",\n"
                + "\"Hearing_location\":\"Manchester\",\n"
                + "\"Listed_date\":\"12 October 2020\",\n"
                + "\"Clerk\":\"Mike Jordan\",\n"
                + "\"listing\":[\n"
                + "{\"Judge\":\"\",\n"
                + "\"Court_addressLine1\":\"Manchester Employment Tribunal\",\n"
                + "\"Court_addressLine2\":\"Alexandra House\",\n"
                + "\"Court_addressLine3\":\"14-22 The Parsonage\",\n"
                + "\"Court_town\":\"Manchester\",\n"
                + "\"Court_county\":\"\",\n"
                + "\"Court_postCode\":\"M3 2JA\",\n"
                + "\"Court_fullAddress\":\"Manchester Employment Tribunal, Alexandra House, 14-22 The Parsonage, "
                + "Manchester, M3 2JA\",\n"
                + "\"Court_telephone\":\"03577131270\",\n"
                + "\"Court_fax\":\"07577126570\",\n"
                + "\"Court_DX\":\"123456\",\n"
                + "\"Court_Email\":\"ManchesterOfficeET@hmcts.gov.uk\",\n"
                + "\"listing_logo\":\"[userImage:enhmcts.png]\",\n"
                + "\"ERMember\":\" \",\n"
                + "\"EEMember\":\" \",\n"
                + "\"Case_No\":\"1112\",\n"
                + "\"Hearing_type\":\"Preliminary Hearing (CM)\",\n"
                + "\"Jurisdictions\":\"ADG, COM\",\n"
                + "\"Hearing_date\":\"16 January 2020\",\n"
                + "\"Hearing_date_time\":\"16 January 2020 at 00:00\",\n"
                + "\"Hearing_time\":\"00:00\",\n"
                + "\"Hearing_duration\":\"12 Minutes\",\n"
                + "\"Hearing_clerk\":\"Anne Fox\",\n"
                + "\"Claimant\":\"Mr s sdfs\",\n"
                + "\"claimant_town\":\"claimantTown2\",\n"
                + "\"claimant_representative\":\"Rep\",\n"
                + "\"Respondent\":\"sdf\",\n"
                + "\"resp_others\":\"Mark Taylor\",\n"
                + "\"respondent_town\":\"respondentTown2\",\n"
                + "\"Hearing_location\":\"Manchester\",\n"
                + "\"Hearing_room\":\"Tribunal 4\",\n"
                + "\"Hearing_dayofdays\":\"2 of 3\",\n"
                + "\"Hearing_panel\":\"\",\n"
                + "\"Hearing_notes\":\"Notes3\",\n"
                + "\"Judicial_mediation\":\"\",\n"
                + "\"Reading_deliberation_day\":\"\",\n"
                + "\"Hearing_format\":\"\",\n"
                + "\"respondent_representative\":\"Org\"},\n"
                + "{\"Judge\":\"\",\n"
                + "\"Court_addressLine1\":\"Manchester Employment Tribunal\",\n"
                + "\"Court_addressLine2\":\"Alexandra House\",\n"
                + "\"Court_addressLine3\":\"14-22 The Parsonage\",\n"
                + "\"Court_town\":\"Manchester\",\n"
                + "\"Court_county\":\"\",\n"
                + "\"Court_postCode\":\"M3 2JA\",\n"
                + "\"Court_fullAddress\":\"Manchester Employment Tribunal, Alexandra House, 14-22 The Parsonage, "
                + "Manchester, M3 2JA\",\n"
                + "\"Court_telephone\":\"03577131270\",\n"
                + "\"Court_fax\":\"07577126570\",\n"
                + "\"Court_DX\":\"123456\",\n"
                + "\"Court_Email\":\"ManchesterOfficeET@hmcts.gov.uk\",\n"
                + "\"listing_logo\":\"[userImage:enhmcts.png]\",\n"
                + "\"ERMember\":\" \",\n"
                + "\"EEMember\":\" \",\n"
                + "\"Case_No\":\"1112\",\n"
                + "\"Hearing_type\":\"Hearing\",\n"
                + "\"Jurisdictions\":\"ADG, COM\",\n"
                + "\"Hearing_date\":\"12 October 2020\",\n"
                + "\"Hearing_date_time\":\"12 October 2020 at 00:00\",\n"
                + "\"Hearing_time\":\"00:00\",\n"
                + "\"Hearing_duration\":\"12 Days\",\n"
                + "\"Hearing_clerk\":\"Anne Fox\",\n"
                + "\"Claimant\":\"Mr s sdfs\",\n"
                + "\"claimant_town\":\"claimantTown\",\n"
                + "\"claimant_representative\":\"Rep\",\n"
                + "\"Respondent\":\"sdf\",\n"
                + "\"resp_others\":\"Mark Taylor\\nTony Jones\\nSteve Thomas\",\n"
                + "\"respondent_town\":\"respondentTown\",\n"
                + "\"Hearing_location\":\"Manchester\",\n"
                + "\"Hearing_room\":\"Tribunal 2\",\n"
                + "\"Hearing_dayofdays\":\"1 of 3\",\n"
                + "\"Hearing_panel\":\"Panel\",\n"
                + "\"Hearing_notes\":\"Notes1\",\n"
                + "\"Judicial_mediation\":\"\",\n"
                + "\"Reading_deliberation_day\":\"\",\n"
                + "\"Hearing_format\":\"\",\n"
                + "\"respondent_representative\":\"Org\"},\n"
                + "{\"Judge\":\"\",\n"
                + "\"Court_addressLine1\":\"Manchester Employment Tribunal\",\n"
                + "\"Court_addressLine2\":\"Alexandra House\",\n"
                + "\"Court_addressLine3\":\"14-22 The Parsonage\",\n"
                + "\"Court_town\":\"Manchester\",\n"
                + "\"Court_county\":\"\",\n"
                + "\"Court_postCode\":\"M3 2JA\",\n"
                + "\"Court_fullAddress\":\"Manchester Employment Tribunal, Alexandra House, 14-22 The Parsonage, "
                + "Manchester, M3 2JA\",\n"
                + "\"Court_telephone\":\"03577131270\",\n"
                + "\"Court_fax\":\"07577126570\",\n"
                + "\"Court_DX\":\"123456\",\n"
                + "\"Court_Email\":\"ManchesterOfficeET@hmcts.gov.uk\",\n"
                + "\"listing_logo\":\"[userImage:enhmcts.png]\",\n"
                + "\"ERMember\":\" \",\n"
                + "\"EEMember\":\" \",\n"
                + "\"Case_No\":\"1112\",\n"
                + "\"Hearing_type\":\"Hearing\",\n"
                + "\"Jurisdictions\":\"ADG, DCD\",\n"
                + "\"Hearing_date\":\"14 October 2020\",\n"
                + "\"Hearing_date_time\":\"14 October 2020 at 00:00\",\n"
                + "\"Hearing_time\":\"00:00\",\n"
                + "\"Hearing_duration\":\"12 Days\",\n"
                + "\"Hearing_clerk\":\"Andrew Pearl\",\n"
                + "\"Claimant\":\"Mr s sdfs\",\n"
                + "\"claimant_town\":\"claimantTown1\",\n"
                + "\"claimant_representative\":\"Rep2\",\n"
                + "\"Respondent\":\"sdf2\",\n"
                + "\"resp_others\":\"Mark Taylor\\nTony Jones\",\n"
                + "\"respondent_town\":\"respondentTown1\",\n"
                + "\"Hearing_location\":\"Leeds\",\n"
                + "\"Hearing_room\":\"Tribunal 2\",\n"
                + "\"Hearing_dayofdays\":\"2 of 3\",\n"
                + "\"Hearing_panel\":\"\",\n"
                + "\"Hearing_notes\":\"Notes2\",\n"
                + "\"Judicial_mediation\":\"\",\n"
                + "\"Reading_deliberation_day\":\"\",\n"
                + "\"Hearing_format\":\"\",\n"
                + "\"respondent_representative\":\"Org2\"}],\n"
                + "\"Today_date\":\"" + UtilHelper.formatCurrentDate(LocalDate.now()) + "\"\n"
                + "}\n"
                + "}\n";
        listingDetails2.getCaseData().setManagingOffice(TribunalOffice.MANCHESTER.getOfficeName());
        assertEquals(expected, ListingHelper.buildListingDocumentContent(listingDetails2.getCaseData(), "",
                IT56_TEMPLATE, userDetails, ENGLANDWALES_LISTING_CASE_TYPE_ID).toString());
    }

    @Test
    void buildCaseCauseListPressList() {
        String expected = "{\n"
                + "\"accessKey\":\"\",\n"
                + "\"templateName\":\"EM-TRB-SCO-ENG-00217.docx\",\n"
                + "\"outputName\":\"document.docx\",\n"
                + "\"data\":{\n"
                + "\"Court_addressLine1\":\"Manchester Employment Tribunal\",\n"
                + "\"Court_addressLine2\":\"Alexandra House\",\n"
                + "\"Court_addressLine3\":\"14-22 The Parsonage\",\n"
                + "\"Court_town\":\"Manchester\",\n"
                + "\"Court_county\":\"\",\n"
                + "\"Court_postCode\":\"M3 2JA\",\n"
                + "\"Court_fullAddress\":\"Manchester Employment Tribunal, Alexandra House, 14-22 The Parsonage, "
                + "Manchester, M3 2JA\",\n"
                + "\"Court_telephone\":\"03577131270\",\n"
                + "\"Court_fax\":\"07577126570\",\n"
                + "\"Court_DX\":\"123456\",\n"
                + "\"Court_Email\":\"ManchesterOfficeET@hmcts.gov.uk\",\n"
                + "\"listing_logo\":\"[userImage:enhmcts.png]\",\n"
                + "\"Office_name\":\"Manchester\",\n"
                + "\"Hearing_location\":\"Glasgow COET\",\n"
                + "\"Listed_date\":\"12 October 2020\",\n"
                + "\"Clerk\":\"Mike Jordan\",\n"
                + "\"location\":[\n"
                + "{\"Hearing_venue\":\"Manchester\",\n"
                + "\"listing\":[\n"
                + "{\"Judge\":\"\",\n"
                + "\"Court_addressLine1\":\"Manchester Employment Tribunal\",\n"
                + "\"Court_addressLine2\":\"Alexandra House\",\n"
                + "\"Court_addressLine3\":\"14-22 The Parsonage\",\n"
                + "\"Court_town\":\"Manchester\",\n"
                + "\"Court_county\":\"\",\n"
                + "\"Court_postCode\":\"M3 2JA\",\n"
                + "\"Court_fullAddress\":\"Manchester Employment Tribunal, Alexandra House, 14-22 The Parsonage, "
                + "Manchester, M3 2JA\",\n"
                + "\"Court_telephone\":\"03577131270\",\n"
                + "\"Court_fax\":\"07577126570\",\n"
                + "\"Court_DX\":\"123456\",\n"
                + "\"Court_Email\":\"ManchesterOfficeET@hmcts.gov.uk\",\n"
                + "\"listing_logo\":\"[userImage:enhmcts.png]\",\n"
                + "\"ERMember\":\" \",\n"
                + "\"EEMember\":\" \",\n"
                + "\"Case_No\":\"1112\",\n"
                + "\"Hearing_type\":\"Preliminary Hearing (CM)\",\n"
                + "\"Jurisdictions\":\"ADG, COM\",\n"
                + "\"Hearing_date\":\"16 January 2020\",\n"
                + "\"Hearing_date_time\":\"16 January 2020 at 00:00\",\n"
                + "\"Hearing_time\":\"00:00\",\n"
                + "\"Hearing_duration\":\"12 Minutes\",\n"
                + "\"Hearing_clerk\":\"Anne Fox\",\n"
                + "\"Claimant\":\"Mr s sdfs\",\n"
                + "\"claimant_town\":\"claimantTown2\",\n"
                + "\"claimant_representative\":\"Rep\",\n"
                + "\"Respondent\":\"sdf\",\n"
                + "\"resp_others\":\"Mark Taylor\",\n"
                + "\"respondent_town\":\"respondentTown2\",\n"
                + "\"Hearing_location\":\"Manchester\",\n"
                + "\"Hearing_room\":\"Tribunal 4\",\n"
                + "\"Hearing_dayofdays\":\"2 of 3\",\n"
                + "\"Hearing_panel\":\"\",\n"
                + "\"Hearing_notes\":\"Notes3\",\n"
                + "\"Judicial_mediation\":\"\",\n"
                + "\"Reading_deliberation_day\":\"\",\n"
                + "\"Hearing_format\":\"\",\n"
                + "\"respondent_representative\":\"Org\"},\n"
                + "{\"Judge\":\"\",\n"
                + "\"Court_addressLine1\":\"Manchester Employment Tribunal\",\n"
                + "\"Court_addressLine2\":\"Alexandra House\",\n"
                + "\"Court_addressLine3\":\"14-22 The Parsonage\",\n"
                + "\"Court_town\":\"Manchester\",\n"
                + "\"Court_county\":\"\",\n"
                + "\"Court_postCode\":\"M3 2JA\",\n"
                + "\"Court_fullAddress\":\"Manchester Employment Tribunal, Alexandra House, 14-22 The Parsonage, "
                + "Manchester, M3 2JA\",\n"
                + "\"Court_telephone\":\"03577131270\",\n"
                + "\"Court_fax\":\"07577126570\",\n"
                + "\"Court_DX\":\"123456\",\n"
                + "\"Court_Email\":\"ManchesterOfficeET@hmcts.gov.uk\",\n"
                + "\"listing_logo\":\"[userImage:enhmcts.png]\",\n"
                + "\"ERMember\":\" \",\n"
                + "\"EEMember\":\" \",\n"
                + "\"Case_No\":\"1112\",\n"
                + "\"Hearing_type\":\"Hearing\",\n"
                + "\"Jurisdictions\":\"ADG, DCD\",\n"
                + "\"Hearing_date\":\"16 January 2020\",\n"
                + "\"Hearing_date_time\":\"16 January 2020 at 02:00\",\n"
                + "\"Hearing_time\":\"02:00\",\n"
                + "\"Hearing_duration\":\"12 Days\",\n"
                + "\"Hearing_clerk\":\"Andrew Pearl\",\n"
                + "\"Claimant\":\"Mr s sdfs\",\n"
                + "\"claimant_town\":\"claimantTown1\",\n"
                + "\"claimant_representative\":\"Rep2\",\n"
                + "\"Respondent\":\"sdf2\",\n"
                + "\"resp_others\":\"Mark Taylor\\nTony Jones\",\n"
                + "\"respondent_town\":\"respondentTown1\",\n"
                + "\"Hearing_location\":\"Manchester\",\n"
                + "\"Hearing_room\":\"Tribunal 2\",\n"
                + "\"Hearing_dayofdays\":\"2 of 3\",\n"
                + "\"Hearing_panel\":\"\",\n"
                + "\"Hearing_notes\":\"Notes2\",\n"
                + "\"Judicial_mediation\":\"\",\n"
                + "\"Reading_deliberation_day\":\"\",\n"
                + "\"Hearing_format\":\"\",\n"
                + "\"respondent_representative\":\"Org2\"}]\n"
                + "},\n"
                + "{\"Hearing_venue\":\"Not_Allocated\",\n"
                + "\"listing\":[\n"
                + "{\"Judge\":\"\",\n"
                + "\"Court_addressLine1\":\"Manchester Employment Tribunal\",\n"
                + "\"Court_addressLine2\":\"Alexandra House\",\n"
                + "\"Court_addressLine3\":\"14-22 The Parsonage\",\n"
                + "\"Court_town\":\"Manchester\",\n"
                + "\"Court_county\":\"\",\n"
                + "\"Court_postCode\":\"M3 2JA\",\n"
                + "\"Court_fullAddress\":\"Manchester Employment Tribunal, Alexandra House, 14-22 The Parsonage, "
                + "Manchester, M3 2JA\",\n"
                + "\"Court_telephone\":\"03577131270\",\n"
                + "\"Court_fax\":\"07577126570\",\n"
                + "\"Court_DX\":\"123456\",\n"
                + "\"Court_Email\":\"ManchesterOfficeET@hmcts.gov.uk\",\n"
                + "\"listing_logo\":\"[userImage:enhmcts.png]\",\n"
                + "\"ERMember\":\" \",\n"
                + "\"EEMember\":\" \",\n"
                + "\"Case_No\":\"1112\",\n"
                + "\"Hearing_type\":\"Hearing\",\n"
                + "\"Jurisdictions\":\"ADG, COM\",\n"
                + "\"Hearing_date\":\"16 January 2020\",\n"
                + "\"Hearing_date_time\":\"16 January 2020 at 03:00\",\n"
                + "\"Hearing_time\":\"03:00\",\n"
                + "\"Hearing_duration\":\"12 Days\",\n"
                + "\"Hearing_clerk\":\"Anne Fox\",\n"
                + "\"Claimant\":\"Mr s sdfs\",\n"
                + "\"claimant_town\":\"claimantTown\",\n"
                + "\"claimant_representative\":\"Rep\",\n"
                + "\"Respondent\":\"sdf\",\n"
                + "\"resp_others\":\"Mark Taylor\\nTony Jones\\nSteve Thomas\",\n"
                + "\"respondent_town\":\"respondentTown\",\n"
                + "\"Hearing_location\":\"\",\n"
                + "\"Hearing_room\":\"Tribunal 2\",\n"
                + "\"Hearing_dayofdays\":\"1 of 3\",\n"
                + "\"Hearing_panel\":\"Panel\",\n"
                + "\"Hearing_notes\":\"Notes1\",\n"
                + "\"Judicial_mediation\":\"\",\n"
                + "\"Reading_deliberation_day\":\"\",\n"
                + "\"Hearing_format\":\"\",\n"
                + "\"respondent_representative\":\"Org\"}]\n"
                + "}],\n"
                + "\"Today_date\":\"" + UtilHelper.formatCurrentDate(LocalDate.now()) + "\"\n"
                + "}\n"
                + "}\n";
        listingDetails3.getCaseData().setManagingOffice(TribunalOffice.MANCHESTER.getOfficeName());
        assertEquals(expected, ListingHelper.buildListingDocumentContent(listingDetails3.getCaseData(), "",
                PRESS_LIST_CAUSE_LIST_SINGLE_TEMPLATE, userDetails, ENGLANDWALES_LISTING_CASE_TYPE_ID).toString());
    }

    @Test
    void getListingCaseTypeId() {
        assertEquals(ENGLANDWALES_CASE_TYPE_ID,
                UtilHelper.getListingCaseTypeId(ENGLANDWALES_LISTING_CASE_TYPE_ID));
        assertEquals(SCOTLAND_CASE_TYPE_ID,
                UtilHelper.getListingCaseTypeId(SCOTLAND_LISTING_CASE_TYPE_ID));
    }

    @Test
    @Disabled // Some extra properties on this
    public void getListingTypeFromSubmitData() throws Exception {
        CaseData caseData = new CaseData();
        ClaimantIndType claimantIndType = new ClaimantIndType();
        claimantIndType.setClaimantLastName("Rodriguez");
        caseData.setClaimantIndType(claimantIndType);
        RespondentSumType respondentSumType = new RespondentSumType();
        respondentSumType.setRespondentName("Juan Pedro");
        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(respondentSumType);
        caseData.setRespondentCollection(new ArrayList<>(Collections.singleton(respondentSumTypeItem)));
        DateListedType dateListedType = new DateListedType();
        dateListedType.setHearingClerk(new DynamicFixedListType("Clerk"));
        dateListedType.setHearingRoom(new DynamicFixedListType("Tribunal 4"));
        dateListedType.setHearingEdinburgh(new DynamicFixedListType("EdinburghVenue"));
        dateListedType.setHearingVenueDay(new DynamicFixedListType("Edinburgh"));
        dateListedType.setListedDate("2019-12-12T12:11:00.000");
        DateListedTypeItem dateListedTypeItem = new DateListedTypeItem();
        dateListedTypeItem.setId("123");
        dateListedTypeItem.setValue(dateListedType);
        HearingType hearingType = new HearingType();
        hearingType.setHearingDateCollection(new ArrayList<>(Collections.singleton(dateListedTypeItem)));
        hearingType.setHearingVenue(new DynamicFixedListType(TribunalOffice.ABERDEEN.getOfficeName()));
        hearingType.setHearingEstLengthNum("2");
        hearingType.setHearingEstLengthNumType("hours");
        hearingType.setHearingFormat(List.of("Telephone"));
        hearingType.setHearingSitAlone("Sit Alone");
        hearingType.setJudicialMediation(YES);
        String expected = "ListingType(causeListDate=12 December 2019, causeListTime=12:11, "
                + "causeListVenue=Edinburgh, elmoCaseReference=null, "
                + "jurisdictionCodesList= , hearingType= , positionType= , hearingJudgeName= , "
                + "hearingEEMember= , hearingERMember= , hearingClerk=Clerk, "
                + "hearingDay=2 of 3, claimantName=Rodriguez, claimantTown= , claimantRepresentative= , "
                + "respondent=Juan Pedro, respondentTown= , "
                + "respondentRepresentative= , estHearingLength=2 hours, hearingPanel= , hearingRoom=Tribunal 4, "
                + "respondentOthers= , hearingNotes= , judicialMediation= , hearingFormat=Telephone, "
                + "hearingReadingDeliberationMembersChambers= )";
        ListingDetails listingDetails = generateListingDetails("listingDetailsTest1.json");
        assertEquals(expected, ListingHelper.getListingTypeFromCaseData(listingDetails.getCaseData(), caseData,
                hearingType, dateListedType, 1, 3).toString());

        dateListedType.setHearingRoom(new DynamicFixedListType("Tribunal 5"));
        dateListedType.setHearingEdinburgh(null);
        dateListedType.setHearingVenueDay(new DynamicFixedListType(TribunalOffice.DUNDEE.getOfficeName()));
        dateListedType.setHearingRoom(new DynamicFixedListType("Tribunal 5"));
        expected = "ListingType(causeListDate=12 December 2019, causeListTime=12:11, causeListVenue=Dundee, "
                + "elmoCaseReference=null, "
                + "jurisdictionCodesList= , hearingType= , positionType= , hearingJudgeName= , hearingEEMember= , "
                + "hearingERMember= , hearingClerk=Clerk, "
                + "hearingDay=2 of 3, claimantName=Rodriguez, claimantTown= , claimantRepresentative= , "
                + "respondent=Juan Pedro, respondentTown= , "
                + "respondentRepresentative= , estHearingLength=2 hours, hearingPanel= , hearingRoom=Tribunal 5, "
                + "respondentOthers= , hearingNotes= , judicialMediation= , hearingFormat=Telephone, "
                + "hearingReadingDeliberationMembersChambers= )";
        assertEquals(expected, ListingHelper.getListingTypeFromCaseData(listingDetails.getCaseData(), caseData,
                hearingType, dateListedType, 1, 3).toString());

        dateListedType.setHearingRoom(new DynamicFixedListType("Tribunal 5"));
        dateListedType.setHearingDundee(null);
        dateListedType.setHearingVenueDay(new DynamicFixedListType(GLASGOW.getOfficeName()));
        dateListedType.setHearingGlasgow(new DynamicFixedListType("GlasgowVenue"));
        expected = "ListingType(causeListDate=12 December 2019, causeListTime=12:11, causeListVenue=Glasgow, "
                + "elmoCaseReference=null, jurisdictionCodesList= , hearingType= , positionType= , hearingJudgeName= , "
                + "hearingEEMember= , hearingERMember= , hearingClerk=Clerk, "
                + "hearingDay=2 of 3, claimantName=Rodriguez, claimantTown= , claimantRepresentative= , "
                + "respondent=Juan Pedro, respondentTown= , respondentRepresentative= , estHearingLength=2 hours, "
                + "hearingPanel= , hearingRoom=Tribunal 5, respondentOthers= , hearingNotes= , judicialMediation= , "
                + "hearingFormat=Telephone, hearingReadingDeliberationMembersChambers= )";
        assertEquals(expected, ListingHelper.getListingTypeFromCaseData(listingDetails.getCaseData(), caseData,
                hearingType, dateListedType, 1, 3).toString());

        dateListedType.setHearingRoom(new DynamicFixedListType("Tribunal 7"));
        dateListedType.setHearingGlasgow(null);
        dateListedType.setHearingVenueDay(new DynamicFixedListType(TribunalOffice.EDINBURGH.getOfficeName()));
        dateListedType.setHearingEdinburgh(new DynamicFixedListType("EdinburghVenue"));
        expected = "ListingType(causeListDate=12 December 2019, causeListTime=12:11, causeListVenue=Edinburgh, "
                + "elmoCaseReference=null, jurisdictionCodesList= , hearingType= , positionType= , hearingJudgeName= , "
                + "hearingEEMember= , hearingERMember= , hearingClerk=Clerk, hearingDay=2 of 3, "
                + "claimantName=Rodriguez, claimantTown= , claimantRepresentative= , respondent=Juan Pedro, "
                + "respondentTown= , respondentRepresentative= , estHearingLength=2 hours, hearingPanel= , "
                + "hearingRoom=Tribunal 7, respondentOthers= , hearingNotes= , judicialMediation= , "
                + "hearingFormat=Telephone, hearingReadingDeliberationMembersChambers= )";

        assertEquals(expected, ListingHelper.getListingTypeFromCaseData(listingDetails.getCaseData(), caseData,
                hearingType, dateListedType, 1, 3).toString());

        CaseData caseDataRule50 = new CaseData();
        RestrictedReportingType restrictedReportingType = new RestrictedReportingType();
        restrictedReportingType.setRule503b(YES);
        caseDataRule50.setRestrictedReporting(restrictedReportingType);
        ListingData listingDataPublic = listingDetails.getCaseData();
        listingDataPublic.setHearingDocETCL(HEARING_ETCL_PUBLIC);
        dateListedType.setHearingTypeReadingDeliberation("Neither");
        expected = "ListingType(causeListDate=12 December 2019, causeListTime=12:11, causeListVenue=Edinburgh, "
                + "elmoCaseReference=null, jurisdictionCodesList= , hearingType= , positionType= , hearingJudgeName= , "
                + "hearingEEMember= , hearingERMember= , hearingClerk=Clerk, hearingDay=2 of 3, claimantName= , "
                + "claimantTown= , claimantRepresentative= , respondent= , respondentTown= , "
                + "respondentRepresentative= , estHearingLength=2 hours, hearingPanel= , hearingRoom=Tribunal 7, "
                + "respondentOthers= , hearingNotes= , judicialMediation= , hearingFormat=Telephone,"
                + " hearingReadingDeliberationMembersChambers= )";
        assertEquals(expected, ListingHelper.getListingTypeFromCaseData(listingDataPublic, caseDataRule50,
                hearingType, dateListedType, 1, 3).toString());
        ListingData listingDataPressList = listingDetails.getCaseData();
        listingDataPressList.setHearingDocETCL(HEARING_ETCL_PRESS_LIST);
        caseDataRule50.setManagingOffice(TribunalOffice.DUNDEE.getOfficeName());
        dateListedType.setHearingTypeReadingDeliberation("Reading Day");
        expected = "ListingType(causeListDate=12 December 2019, causeListTime=12:11, causeListVenue=Edinburgh, "
                + "elmoCaseReference=null, " + "jurisdictionCodesList= , hearingType= , positionType= , "
                + "hearingJudgeName= , hearingEEMember= , hearingERMember= , hearingClerk=Clerk, "
                + "hearingDay=2 of 3, claimantName=Order made pursuant to Rule 50, claimantTown= , "
                + "claimantRepresentative= , " + "respondent=Order made pursuant to Rule 50, respondentTown= , "
                + "respondentRepresentative= , estHearingLength=2 hours, " + "hearingPanel= ,"
                + " hearingRoom=Tribunal 7, respondentOthers= , hearingNotes= , judicialMediation= ,"
                + " hearingFormat=Telephone, hearingReadingDeliberationMembersChambers=Reading Day)";
        assertEquals(expected, ListingHelper.getListingTypeFromCaseData(listingDataPressList,
                caseDataRule50, hearingType, dateListedType, 1, 3).toString());

        dateListedType.setHearingRoom(null);
        dateListedType.setHearingVenueDay(null);
        dateListedType.setHearingVenueDayScotland(TribunalOffice.EDINBURGH.getOfficeName());

        expected = "ListingType(causeListDate=12 December 2019, causeListTime=12:11, causeListVenue=EdinburghVenue, "
                + "elmoCaseReference=null, " + "jurisdictionCodesList= , hearingType= , "
                + "positionType= , hearingJudgeName= "
                + ", hearingEEMember= , hearingERMember= , hearingClerk=Clerk, " + "hearingDay=2 of 3,"
                + " claimantName=Rodriguez, claimantTown= , claimantRepresentative= , "
                + "respondent=Juan Pedro, respondentTown= , "
                + "respondentRepresentative= , estHearingLength=2 hours, hearingPanel= , hearingRoom= , "
                + "respondentOthers= , hearingNotes= , judicialMediation= , hearingFormat=Telephone,"
                + " hearingReadingDeliberationMembersChambers=Reading Day)";
        assertEquals(expected, ListingHelper.getListingTypeFromCaseData(listingDetails.getCaseData(),
                caseData, hearingType, dateListedType, 1, 3).toString());

        dateListedType.setHearingVenueDayScotland(GLASGOW.getOfficeName());
        dateListedType.setHearingEdinburgh(null);
        dateListedType.setHearingGlasgow(new DynamicFixedListType("GlasgowVenue"));

        expected = "ListingType(causeListDate=12 December 2019, causeListTime=12:11,"
                + " causeListVenue=GlasgowVenue, elmoCaseReference=null, "
                + "jurisdictionCodesList= , hearingType= , positionType= , hearingJudgeName= , "
                + "hearingEEMember= , hearingERMember= , hearingClerk=Clerk, "
                + "hearingDay=2 of 3, claimantName=Rodriguez, claimantTown= , claimantRepresentative= , "
                + "respondent=Juan Pedro, respondentTown= , "
                + "respondentRepresentative= , estHearingLength=2 hours, hearingPanel= , hearingRoom= , "
                + "respondentOthers= , hearingNotes= , judicialMediation= , hearingFormat=Telephone, "
                + "hearingReadingDeliberationMembersChambers=Reading Day)";
        assertEquals(expected, ListingHelper.getListingTypeFromCaseData(
                listingDetails.getCaseData(), caseData, hearingType, dateListedType, 1, 3).toString());

        dateListedType.setHearingVenueDayScotland(TribunalOffice.ABERDEEN.getOfficeName());
        dateListedType.setHearingGlasgow(null);
        dateListedType.setHearingAberdeen(new DynamicFixedListType("AberdeenVenue"));

        expected = "ListingType(causeListDate=12 December 2019, causeListTime=12:11, causeListVenue=AberdeenVenue,"
                + " elmoCaseReference=null, " + "jurisdictionCodesList= , hearingType= , positionType= , "
                + "hearingJudgeName= , hearingEEMember= , " + "hearingERMember= , hearingClerk=Clerk, "
                + "hearingDay=2 of 3, claimantName=Rodriguez, claimantTown= ,"
                + " claimantRepresentative= , respondent=Juan Pedro, "
                + "respondentTown= , " + "respondentRepresentative= , estHearingLength=2 hours,"
                + " hearingPanel= ,"
                + " hearingRoom= , respondentOthers= , hearingNotes= , judicialMediation= , "
                + "hearingFormat=Telephone, "
                + "hearingReadingDeliberationMembersChambers=Reading Day)";
        assertEquals(expected, ListingHelper.getListingTypeFromCaseData(
                listingDetails.getCaseData(),
                caseData,
                hearingType,
                dateListedType,
                1,
                3)
                .toString());

        dateListedType.setHearingVenueDayScotland(TribunalOffice.DUNDEE.getOfficeName());
        dateListedType.setHearingAberdeen(null);
        dateListedType.setHearingDundee(new DynamicFixedListType("DundeeVenue"));

        expected = "ListingType(causeListDate=12 December 2019, causeListTime=12:11, causeListVenue=DundeeVenue,"
                + " elmoCaseReference=null, " + "jurisdictionCodesList= , hearingType= ,"
                + " positionType= , hearingJudgeName= , "
                + "hearingEEMember= , hearingERMember= , hearingClerk=Clerk, "
                + "hearingDay=2 of 3, claimantName=Rodriguez,"
                + " claimantTown= , claimantRepresentative= , respondent=Juan Pedro,"
                + " respondentTown= , "
                + "respondentRepresentative= , estHearingLength=2 hours, hearingPanel= , hearingRoom= , "
                + "respondentOthers= , hearingNotes= , judicialMediation= , hearingFormat=Telephone, "
                + "hearingReadingDeliberationMembersChambers=Reading Day)";
        assertEquals(expected, ListingHelper.getListingTypeFromCaseData(
                listingDetails.getCaseData(),
                caseData,
                hearingType,
                dateListedType,
                1,
                3)
                .toString());
    }

    @Test
    void getListingDateBetween() {
        String dateToSearchFrom = "2020-01-02";
        String dateToSearchTo = "2020-03-01";
        String dateToSearch = "2020-01-02T00:00:00.000";
        boolean isBetween = ListingHelper.getListingDateBetween(dateToSearchFrom, dateToSearchTo, dateToSearch);
        assertTrue(isBetween);

        dateToSearchFrom = "2020-01-02";
        dateToSearchTo = "";
        dateToSearch = "2020-01-02T10:00:00.000";
        boolean isEqual = ListingHelper.getListingDateBetween(dateToSearchFrom, dateToSearchTo, dateToSearch);
        assertTrue(isEqual);

        dateToSearchFrom = "2020-01-02";
        dateToSearchTo = "";
        dateToSearch = "2020-01-03T10:00:00.000";
        isEqual = ListingHelper.getListingDateBetween(dateToSearchFrom, dateToSearchTo, dateToSearch);
        assertFalse(isEqual);
    }

    @Test
    void getMatchingDateBetween() {
        String dateToSearchFrom = "2020-01-11";
        String dateToSearchTo = "2020-01-20";
        String dateToSearch = "2020-01-15";
        boolean isBetween = ListingHelper.getMatchingDateBetween(dateToSearchFrom, dateToSearchTo, dateToSearch, true);
        assertTrue(isBetween);

        dateToSearchFrom = "2020-01-11";
        dateToSearchTo = "2020-01-20";
        dateToSearch = "2020-01-25";
        isBetween = ListingHelper.getMatchingDateBetween(dateToSearchFrom, dateToSearchTo, dateToSearch, true);
        assertFalse(isBetween);

        dateToSearchFrom = "2020-01-11";
        dateToSearchTo = "";
        dateToSearch = "2020-01-11";
        boolean isEqual = ListingHelper.getMatchingDateBetween(dateToSearchFrom, dateToSearchTo, dateToSearch, false);
        assertTrue(isEqual);

        dateToSearchFrom = "2020-01-11";
        dateToSearchTo = "";
        dateToSearch = "2020-01-12";
        isEqual = ListingHelper.getMatchingDateBetween(dateToSearchFrom, dateToSearchTo, dateToSearch, false);
        assertFalse(isEqual);
    }

    @Test
    void getListingDocName() {
        ListingData listingData = new ListingData();
        listingData.setHearingDocType(HEARING_DOC_ETCL);
        listingData.setHearingDocETCL(HEARING_ETCL_STAFF);
        listingData.setRoomOrNoRoom(NO);
        assertEquals(STAFF_CASE_CAUSE_LIST_TEMPLATE, ListingHelper.getListingDocName(listingData));
        listingData.setRoomOrNoRoom(YES);
        assertEquals(STAFF_CASE_CAUSE_LIST_ROOM_TEMPLATE, ListingHelper.getListingDocName(listingData));
        listingData.setHearingDocETCL(HEARING_ETCL_PUBLIC);
        listingData.setRoomOrNoRoom(NO);
        assertEquals(PUBLIC_CASE_CAUSE_LIST_TEMPLATE, ListingHelper.getListingDocName(listingData));
        listingData.setRoomOrNoRoom(YES);
        assertEquals(PUBLIC_CASE_CAUSE_LIST_ROOM_TEMPLATE, ListingHelper.getListingDocName(listingData));
        listingData.setHearingDocETCL(HEARING_ETCL_PRESS_LIST);
        listingData.setHearingDateType(RANGE_HEARING_DATE_TYPE);
        assertEquals(PRESS_LIST_CAUSE_LIST_RANGE_TEMPLATE, ListingHelper.getListingDocName(listingData));
        listingData.setHearingDateType(SINGLE_HEARING_DATE_TYPE);
        assertEquals(PRESS_LIST_CAUSE_LIST_SINGLE_TEMPLATE, ListingHelper.getListingDocName(listingData));
        listingData.setHearingDocType(HEARING_DOC_IT56);
        assertEquals(IT56_TEMPLATE, ListingHelper.getListingDocName(listingData));
        listingData.setHearingDocType(HEARING_DOC_IT57);
        assertEquals(IT57_TEMPLATE, ListingHelper.getListingDocName(listingData));
        listingData.setHearingDocType(null);
        listingData.setReportType(BROUGHT_FORWARD_REPORT);
        assertEquals("EM-TRB-SCO-ENG-00218", ListingHelper.getListingDocName(listingData));
        listingData.setReportType(CLAIMS_ACCEPTED_REPORT);
        assertEquals("EM-TRB-SCO-ENG-00219", ListingHelper.getListingDocName(listingData));
        listingData.setReportType(LIVE_CASELOAD_REPORT);
        assertEquals("EM-TRB-SCO-ENG-00220", ListingHelper.getListingDocName(listingData));
        listingData.setReportType(CASES_COMPLETED_REPORT);
        assertEquals("EM-TRB-SCO-ENG-00221", ListingHelper.getListingDocName(listingData));
        listingData.setReportType(CASES_AWAITING_JUDGMENT_REPORT);
        assertEquals("EM-TRB-SCO-ENG-00749", ListingHelper.getListingDocName(listingData));
        listingData.setReportType(TIME_TO_FIRST_HEARING_REPORT);
        assertEquals("EM-TRB-SCO-ENG-00751", ListingHelper.getListingDocName(listingData));
        listingData.setReportType(SERVING_CLAIMS_REPORT);
        assertEquals("EM-TRB-SCO-ENG-00781", ListingHelper.getListingDocName(listingData));
        listingData.setReportType(CASE_SOURCE_LOCAL_REPORT);
        assertEquals("EM-TRB-SCO-ENG-00783", ListingHelper.getListingDocName(listingData));
        listingData.setReportType(HEARINGS_TO_JUDGEMENTS_REPORT);
        assertEquals("EM-TRB-SCO-ENG-00786", ListingHelper.getListingDocName(listingData));
        listingData.setReportType(NO_CHANGE_IN_CURRENT_POSITION_REPORT);
        assertEquals("EM-TRB-SCO-ENG-00794", ListingHelper.getListingDocName(listingData));
        listingData.setReportType(MEMBER_DAYS_REPORT);
        assertEquals("EM-TRB-SCO-ENG-00800", ListingHelper.getListingDocName(listingData));
        listingData.setReportType(RESPONDENTS_REPORT);
        assertEquals("EM-TRB-SCO-ENG-00815", ListingHelper.getListingDocName(listingData));
        listingData.setReportType("");
        assertEquals("No document found", ListingHelper.getListingDocName(listingData));
    }

    @Test
    void getRespondentOthersWithLineBreaksForMultipleRespondents() {
        String expected = "Mark Taylor\\nTony Jones\\nSteve Thomas";
        String actual = ListingHelper.getRespondentOthersWithLineBreaks(
                listingDetails.getCaseData().getListingCollection().get(0).getValue());

        assertEquals(expected, actual);
    }

    @Test
    void getRespondentOthersWithLineBreaksForTwoRespondents() {
        String expected = "Mark Taylor\\nTony Jones";
        String actual = ListingHelper.getRespondentOthersWithLineBreaks(
                listingDetails.getCaseData().getListingCollection().get(1).getValue());

        assertEquals(expected, actual);
    }

    @Test
    void getRespondentOthersWithLineBreaksForSingleRespondent() {
        String expected = "Mark Taylor";
        String actual = ListingHelper.getRespondentOthersWithLineBreaks(
                listingDetails.getCaseData().getListingCollection().get(2).getValue());

        assertEquals(expected, actual);
    }

    @Test
    void getVenueCodeFromDateListedTypeEWTest() {
        String office = "Leeds";
        DateListedTypeItem dateListedTypeItem = setDateListedTimeItem(false, office);
        String result = ListingHelper.getVenueCodeFromDateListedType(dateListedTypeItem.getValue());
        assertThat(result)
                .isEqualTo(office);
    }

    @ParameterizedTest
    @CsvSource({"Glasgow", "Aberdeen", "Edinburgh", "Dundee"})
    public void getVenueCodeFromDateListedTypeScotlandTest(String office) {
        DateListedTypeItem dateListedTypeItem = setDateListedTimeItem(true, office);
        String result = ListingHelper.getVenueCodeFromDateListedType(dateListedTypeItem.getValue());
        assertThat(result)
                .isEqualTo(office);
    }

    private DateListedTypeItem setDateListedTimeItem(boolean scotland, String office) {
        DateListedTypeItem dateListedTypeItem = new DateListedTypeItem();
        dateListedTypeItem.setId(UUID.randomUUID().toString());
        DateListedType dateListedType = new DateListedType();
        if (!scotland) {
            dateListedType.setHearingVenueDay(new DynamicFixedListType(office));
        } else {
            dateListedType.setHearingVenueDayScotland(office);
            final TribunalOffice tribunalOffice = TribunalOffice.valueOfOfficeName(office);
            switch (tribunalOffice) {
                case GLASGOW:
                    dateListedType.setHearingGlasgow(new DynamicFixedListType("Glasgow"));
                    break;
                case ABERDEEN:
                    dateListedType.setHearingAberdeen(new DynamicFixedListType("Aberdeen"));
                    break;
                case DUNDEE:
                    dateListedType.setHearingDundee(new DynamicFixedListType("Dundee"));
                    break;
                case EDINBURGH:
                    dateListedType.setHearingEdinburgh(new DynamicFixedListType("Edinburgh"));
                    break;
                default:
                    break;
            }
        }
        dateListedTypeItem.setValue(dateListedType);
        return dateListedTypeItem;
    }

    @Test
    void getRespondentOthersWithLineBreaksForNoRespondents() {
        String expected = "";
        String actual = ListingHelper.getRespondentOthersWithLineBreaks(listingDetails.getCaseData()
                .getListingCollection().get(3).getValue());

        assertEquals(expected, actual);
    }

    @Test
    void listingRangeValid() {
        List<String> errors = new ArrayList<>();
        ListingHelper.isListingRangeValid(listingDetails.getCaseData(), errors);

        assertEquals(0, errors.size());
    }

    @Test
    void listingRangeInvalid() {
        List<String> errors = new ArrayList<>();
        ListingHelper.isListingRangeValid(listingDetails4.getCaseData(), errors);

        assertEquals(1, errors.size());
        assertEquals("Date range is limited to a max of 31 days", errors.get(0));
    }
}