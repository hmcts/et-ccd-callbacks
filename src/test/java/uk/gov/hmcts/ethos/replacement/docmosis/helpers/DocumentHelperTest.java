package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.ecm.common.model.helper.DefaultValues;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.AddressLabelsAttributesType;
import uk.gov.hmcts.et.common.model.ccd.types.CorrespondenceScotType;
import uk.gov.hmcts.et.common.model.ccd.types.CorrespondenceType;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VenueAddressReaderService;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ADDRESS_LABELS_TEMPLATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.ACAS_CERTIFICATE;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.ET1;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.ET1_ATTACHMENT;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.LEGACY_DOCUMENT_NAMES;

@ExtendWith(SpringExtension.class)
class DocumentHelperTest {

    private static final String DUMMY_CASE_TYPE_ID = "dummy case type id";
    private static final String MANCHESTER_VENUE_ADDRESS =
            "Manchester Employment Tribunals, Alexandra House, 14-22 The Parsonage, Manchester, M3 2JA";
    private static final String GLASGOW_VENUE_ADDRESS =
            "Glasgow Tribunal Centre, Atlantic Quay, 20 York Street, Glasgow, G2 8GT";
    private static final String ABERDEEN_VENUE_ADDRESS = "Ground Floor, AB1, 48 Huntly Street, Aberdeen, AB10 1SH";
    private CaseDetails caseDetails1;
    private CaseDetails caseDetails2;
    private CaseDetails caseDetails3;
    private CaseDetails caseDetails4;
    private CaseDetails caseDetails5;
    private CaseDetails caseDetails6;
    private CaseDetails caseDetails7;
    private CaseDetails caseDetails8;
    private CaseDetails caseDetails9;
    private CaseDetails caseDetails10;
    private CaseDetails caseDetails12;
    private CaseDetails caseDetails13;
    private CaseDetails caseDetails14;
    private CaseDetails caseDetails15;
    private CaseDetails caseDetails20;
    private CaseDetails caseDetailsEmpty;
    private CaseDetails caseDetailsScot1;
    private CaseDetails caseDetailsScot2;
    private CaseDetails caseDetailsScot3;
    private CaseDetails caseDetailsScot4;
    private UserDetails userDetails;
    private VenueAddressReaderService venueAddressReaderService;

    @BeforeEach
    void setUp() throws Exception {
        caseDetails1 = generateCaseDetails("caseDetailsTest1.json");
        caseDetails2 = generateCaseDetails("caseDetailsTest2.json");
        caseDetails3 = generateCaseDetails("caseDetailsTest3.json");
        caseDetails4 = generateCaseDetails("caseDetailsTest4.json");
        caseDetails5 = generateCaseDetails("caseDetailsTest5.json");
        caseDetails6 = generateCaseDetails("caseDetailsTest6.json");
        caseDetails7 = generateCaseDetails("caseDetailsTest7.json");
        caseDetails8 = generateCaseDetails("caseDetailsTest8.json");
        caseDetails9 = generateCaseDetails("caseDetailsTest9.json");
        caseDetails10 = generateCaseDetails("caseDetailsTest10.json");
        caseDetails12 = generateCaseDetails("caseDetailsTest12.json");
        caseDetails13 = generateCaseDetails("caseDetailsTest13.json");
        caseDetails14 = generateCaseDetails("caseDetailsTest14.json");
        caseDetails15 = generateCaseDetails("caseDetailsTest15.json");
        caseDetails20 = generateCaseDetails("caseDetailsTest20.json");
        caseDetailsScot1 = generateCaseDetails("caseDetailsScotTest1.json");
        caseDetailsScot2 = generateCaseDetails("caseDetailsScotTest2.json");
        caseDetailsScot3 = generateCaseDetails("caseDetailsScotTest3.json");
        caseDetailsScot4 = generateCaseDetails("caseDetailsScotTest4.json");

        caseDetailsEmpty = new CaseDetails();
        caseDetailsEmpty.setCaseData(new CaseData());
        userDetails = HelperTest.getUserDetails();

        mockVenueAddressReaderService();
    }

    private void mockVenueAddressReaderService() {
        venueAddressReaderService = mock(VenueAddressReaderService.class);
        when(venueAddressReaderService.getVenueAddress(any(), any(), any())).thenReturn(MANCHESTER_VENUE_ADDRESS);
    }

    private CaseDetails generateCaseDetails(String jsonFileName) throws Exception {
        String json = new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(Thread.currentThread()
            .getContextClassLoader().getResource(jsonFileName)).toURI())));
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, CaseDetails.class);
    }

    @Test
    void buildDocumentContent1() {
        String expected = "{\n"
                + "\"accessKey\":\"\",\n"
                + "\"templateName\":\"EM-TRB-EGW-ENG-00026.docx\",\n"
                + "\"outputName\":\"document.docx\",\n"
                + "\"data\":{\n"
                + "\"claimant_or_rep_full_name\":\"RepresentativeNameClaimant\",\n"
                + "\"claimant_rep_organisation\":\"RepresentativeOrganisation\",\n"
                + "\"claimant_or_rep_addressLine1\":\"56 Block C\",\n"
                + "\"claimant_or_rep_addressLine2\":\"Ellesmere Street\",\n"
                + "\"claimant_or_rep_addressLine3\":\"\",\n"
                + "\"claimant_or_rep_town\":\"Manchester\",\n"
                + "\"claimant_or_rep_county\":\"Lancashire\",\n"
                + "\"claimant_or_rep_postCode\":\"M3 KJR\",\n"
                + "\"claimant_reference\":\"1111111\",\n"
                + "\"claimant_full_name\":\"Mr A J Rodriguez\",\n"
                + "\"Claimant\":\"Mr A J Rodriguez\",\n"
                + "\"claimant_addressLine1\":\"34\",\n"
                + "\"claimant_addressLine2\":\"Low Street\",\n"
                + "\"claimant_addressLine3\":\"\",\n"
                + "\"claimant_town\":\"Manchester\",\n"
                + "\"claimant_county\":\"Lancashire\",\n"
                + "\"claimant_postCode\":\"M3 6gw\",\n"
                + "\"respondent_or_rep_full_name\":\"Antonio Vazquez\",\n"
                + "\"respondent_or_rep_addressLine1\":\"11 Small Street\",\n"
                + "\"respondent_or_rep_addressLine2\":\"22 House\",\n"
                + "\"respondent_or_rep_addressLine3\":\"\",\n"
                + "\"respondent_or_rep_town\":\"Manchester\",\n"
                + "\"respondent_or_rep_county\":\"North West\",\n"
                + "\"respondent_or_rep_postCode\":\"M12 42R\",\n"
                + "\"respondent_full_name\":\"Antonio Vazquez\",\n"
                + "\"respondent_addressLine1\":\"11 Small Street\",\n"
                + "\"respondent_addressLine2\":\"22 House\",\n"
                + "\"respondent_addressLine3\":\"\",\n"
                + "\"respondent_town\":\"Manchester\",\n"
                + "\"respondent_county\":\"North West\",\n"
                + "\"respondent_postCode\":\"M12 42R\",\n"
                + "\"Respondent\":\"1. Antonio Vazquez,\",\n"
                + "\"resp_others\":\"2. Juan Garcia, 3. Mike Jordan\",\n"
                + "\"resp_address\":\"1. 11 Small Street, 22 House, Manchester, North West, M12 42R, UK\\n2. "
                + "12 Small Street, 24 House, Manchester, North West, M12 4ED, UK\\n3. 11 Small Street, 22 House, "
                + "Manchester, North West, M12 42R, UK\",\n"
                + "\"Hearing_date\":\"25 November 2019, 14 December 2019, 28 December 2019\",\n"
                + "\"Hearing_date_time\":\"25 November 2019, 14 December 2019, 28 December 2019 at 10:30\",\n"
                + "\"Hearing_time\":\"10:30\",\n"
                + "\"Hearing_venue\":\"Manchester Employment Tribunals, Alexandra House, 14-22 The Parsonage, "
                + "Manchester, M3 2JA\",\n"
                + "\"Hearing_duration\":\"2 days\",\n"
                + "\"t1_2\":\"true\",\n"
                + "\"Court_addressLine1\":\"Manchester Employment Tribunal,\",\n"
                + "\"Court_addressLine2\":\"Alexandra House,\",\n"
                + "\"Court_addressLine3\":\"14-22 The Parsonage,\",\n"
                + "\"Court_town\":\"Manchester,\",\n"
                + "\"Court_county\":\"\",\n"
                + "\"Court_postCode\":\"M3 2JA\",\n"
                + "\"Court_telephone\":\"03577131270\",\n"
                + "\"Court_fax\":\"07577126570\",\n"
                + "\"Court_DX\":\"123456\",\n"
                + "\"Court_Email\":\"ManchesterOfficeET@hmcts.gov.uk\",\n"
                + "\"i1_2_enhmcts\":\"[userImage:enhmcts.png]\",\n"
                + "\"i1_2_enhmcts1\":\"[userImage:enhmcts.png]\",\n"
                + "\"i1_2_enhmcts2\":\"[userImage:enhmcts.png]\",\n"
                + "\"iScot_schmcts\":\"[userImage:schmcts.png]\",\n"
                + "\"iScot_schmcts1\":\"[userImage:schmcts.png]\",\n"
                + "\"iScot_schmcts2\":\"[userImage:schmcts.png]\",\n"
                + "\"Clerk\":\"Mike Jordan\",\n"
                + "\"Today_date\":\"" + UtilHelper.formatCurrentDate(LocalDate.now()) + "\",\n"
                + "\"TodayPlus28Days\":\"" + UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 28) + "\",\n"
                + "\"Case_No\":\"123456\",\n"
                + "\"submission_reference\":\"12212121\",\n"
                + "}\n"
                + "}\n";
        assertEquals(expected, DocumentHelper.buildDocumentContent(caseDetails1.getCaseData(), "",
                userDetails, ENGLANDWALES_CASE_TYPE_ID,
                caseDetails1.getCaseData().getCorrespondenceType(),
                caseDetails1.getCaseData().getCorrespondenceScotType(), null,
                null, venueAddressReaderService).toString());
    }

    @Test
    void buildDocumentContent2_ResponseStruckOut() {
        String expected = "{\n"
                + "\"accessKey\":\"\",\n"
                + "\"templateName\":\"EM-TRB-EGW-ENG-00027.docx\",\n"
                + "\"outputName\":\"document.docx\",\n"
                + "\"data\":{\n"
                + "\"claimant_or_rep_full_name\":\"Orlando LTD\",\n"
                + "\"claimant_full_name\":\"Orlando LTD\",\n"
                + "\"Claimant\":\"Orlando LTD\",\n"
                + "\"claimant_or_rep_addressLine1\":\"34\",\n"
                + "\"claimant_or_rep_addressLine2\":\"Low Street\",\n"
                + "\"claimant_or_rep_addressLine3\":\"\",\n"
                + "\"claimant_or_rep_town\":\"Manchester\",\n"
                + "\"claimant_or_rep_county\":\"Lancashire\",\n"
                + "\"claimant_or_rep_postCode\":\"M3 6gw\",\n"
                + "\"claimant_addressLine1\":\"34\",\n"
                + "\"claimant_addressLine2\":\"Low Street\",\n"
                + "\"claimant_addressLine3\":\"\",\n"
                + "\"claimant_town\":\"Manchester\",\n"
                + "\"claimant_county\":\"Lancashire\",\n"
                + "\"claimant_postCode\":\"M3 6gw\",\n"
                + "\"respondent_or_rep_full_name\":\"\",\n"
                + "\"respondent_rep_organisation\":\"\",\n"
                + "\"respondent_or_rep_addressLine1\":\"\",\n"
                + "\"respondent_or_rep_addressLine2\":\"\",\n"
                + "\"respondent_or_rep_addressLine3\":\"\",\n"
                + "\"respondent_or_rep_town\":\"\",\n"
                + "\"respondent_or_rep_county\":\"\",\n"
                + "\"respondent_or_rep_postCode\":\"\",\n"
                + "\"respondent_full_name\":\"\",\n"
                + "\"Respondent\":\"\",\n"
                + "\"resp_others\":\"\",\n"
                + "\"resp_address\":\"\",\n"
                + "\"Hearing_date\":\"\",\n"
                + "\"Hearing_date_time\":\"\",\n"
                + "\"Hearing_time\":\"\",\n"
                + "\"Hearing_venue\":\"Manchester Employment Tribunals, Alexandra House, 14-22 The Parsonage, "
                + "Manchester, M3 2JA\",\n"
                + "\"Hearing_duration\":\"2 days\",\n"
                + "\"t2_2A\":\"true\",\n"
                + "\"Court_addressLine1\":\"Manchester Employment Tribunal,\",\n"
                + "\"Court_addressLine2\":\"Alexandra House,\",\n"
                + "\"Court_addressLine3\":\"14-22 The Parsonage,\",\n"
                + "\"Court_town\":\"Manchester,\",\n"
                + "\"Court_county\":\"\",\n"
                + "\"Court_postCode\":\"M3 2JA\",\n"
                + "\"Court_telephone\":\"03577131270\",\n"
                + "\"Court_fax\":\"07577126570\",\n"
                + "\"Court_DX\":\"123456\",\n"
                + "\"Court_Email\":\"ManchesterOfficeET@hmcts.gov.uk\",\n"
                + "\"i2_2A_enhmcts\":\"[userImage:enhmcts.png]\",\n"
                + "\"i2_2A_enhmcts1\":\"[userImage:enhmcts.png]\",\n"
                + "\"i2_2A_enhmcts2\":\"[userImage:enhmcts.png]\",\n"
                + "\"iScot_schmcts\":\"[userImage:schmcts.png]\",\n"
                + "\"iScot_schmcts1\":\"[userImage:schmcts.png]\",\n"
                + "\"iScot_schmcts2\":\"[userImage:schmcts.png]\",\n"
                + "\"Clerk\":\"Mike Jordan\",\n"
                + "\"Today_date\":\"" + UtilHelper.formatCurrentDate(LocalDate.now()) + "\",\n"
                + "\"TodayPlus28Days\":\"" + UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 28) + "\",\n"
                + "\"Case_No\":\"123456\",\n"
                + "\"submission_reference\":\"12212121\",\n"
                + "}\n"
                + "}\n";
        caseDetails2.getCaseData().getRepCollection().get(0).getValue().setRespRepName("Antonio Vazquez");
        assertEquals(expected, DocumentHelper.buildDocumentContent(caseDetails2.getCaseData(), "",
                userDetails, ENGLANDWALES_CASE_TYPE_ID,
                caseDetails2.getCaseData().getCorrespondenceType(),
                caseDetails2.getCaseData().getCorrespondenceScotType(), null,
                null, venueAddressReaderService).toString());
        caseDetails2.getCaseData().getRepCollection().get(0).getValue().setRespRepName("RepresentativeNameRespondent");
    }

    @Test
    void buildDocumentContent2_ResponseNotStruckOut() {
        String expected = "{\n"
                + "\"accessKey\":\"\",\n"
                + "\"templateName\":\"EM-TRB-EGW-ENG-00027.docx\",\n"
                + "\"outputName\":\"document.docx\",\n"
                + "\"data\":{\n"
                + "\"claimant_or_rep_full_name\":\"Orlando LTD\",\n"
                + "\"claimant_full_name\":\"Orlando LTD\",\n"
                + "\"Claimant\":\"Orlando LTD\",\n"
                + "\"claimant_or_rep_addressLine1\":\"34\",\n"
                + "\"claimant_or_rep_addressLine2\":\"Low Street\",\n"
                + "\"claimant_or_rep_addressLine3\":\"\",\n"
                + "\"claimant_or_rep_town\":\"Manchester\",\n"
                + "\"claimant_or_rep_county\":\"Lancashire\",\n"
                + "\"claimant_or_rep_postCode\":\"M3 6gw\",\n"
                + "\"claimant_addressLine1\":\"34\",\n"
                + "\"claimant_addressLine2\":\"Low Street\",\n"
                + "\"claimant_addressLine3\":\"\",\n"
                + "\"claimant_town\":\"Manchester\",\n"
                + "\"claimant_county\":\"Lancashire\",\n"
                + "\"claimant_postCode\":\"M3 6gw\",\n"
                + "\"respondent_or_rep_full_name\":\"RepresentativeNameRespondent\",\n"
                + "\"respondent_or_rep_addressLine1\":\"56 Block C\",\n"
                + "\"respondent_or_rep_addressLine2\":\"Ellesmere Street\",\n"
                + "\"respondent_or_rep_addressLine3\":\"\",\n"
                + "\"respondent_or_rep_town\":\"Manchester\",\n"
                + "\"respondent_or_rep_county\":\"Lancashire\",\n"
                + "\"respondent_or_rep_postCode\":\"M3 KJR\",\n"
                + "\"respondent_reference\":\"1111111\",\n"
                + "\"respondent_rep_organisation\":\"RepresentativeOrganisation\",\n"
                + "\"respondent_full_name\":\"Antonio Vazquez\",\n"
                + "\"respondent_addressLine1\":\"11 Small Street\",\n"
                + "\"respondent_addressLine2\":\"22 House\",\n"
                + "\"respondent_addressLine3\":\"\",\n"
                + "\"respondent_town\":\"Manchester\",\n"
                + "\"respondent_county\":\"North West\",\n"
                + "\"respondent_postCode\":\"M12 42R\",\n"
                + "\"Respondent\":\"Antonio Vazquez\",\n"
                + "\"resp_others\":\"\",\n"
                + "\"resp_address\":\"11 Small Street, 22 House, Manchester, North West, M12 42R, UK\",\n"
                + "\"Hearing_date\":\"\",\n"
                + "\"Hearing_date_time\":\"\",\n"
                + "\"Hearing_time\":\"\",\n"
                + "\"Hearing_venue\":\"Manchester Employment Tribunals, Alexandra House, 14-22 The Parsonage, "
                + "Manchester, M3 2JA\",\n"
                + "\"Hearing_duration\":\"2 days\",\n"
                + "\"t2_2A\":\"true\",\n"
                + "\"Court_addressLine1\":\"Manchester Employment Tribunal,\",\n"
                + "\"Court_addressLine2\":\"Alexandra House,\",\n"
                + "\"Court_addressLine3\":\"14-22 The Parsonage,\",\n"
                + "\"Court_town\":\"Manchester,\",\n"
                + "\"Court_county\":\"\",\n"
                + "\"Court_postCode\":\"M3 2JA\",\n"
                + "\"Court_telephone\":\"03577131270\",\n"
                + "\"Court_fax\":\"07577126570\",\n"
                + "\"Court_DX\":\"123456\",\n"
                + "\"Court_Email\":\"ManchesterOfficeET@hmcts.gov.uk\",\n"
                + "\"i2_2A_enhmcts\":\"[userImage:enhmcts.png]\",\n"
                + "\"i2_2A_enhmcts1\":\"[userImage:enhmcts.png]\",\n"
                + "\"i2_2A_enhmcts2\":\"[userImage:enhmcts.png]\",\n"
                + "\"iScot_schmcts\":\"[userImage:schmcts.png]\",\n"
                + "\"iScot_schmcts1\":\"[userImage:schmcts.png]\",\n"
                + "\"iScot_schmcts2\":\"[userImage:schmcts.png]\",\n"
                + "\"Clerk\":\"Mike Jordan\",\n"
                + "\"Today_date\":\"" + UtilHelper.formatCurrentDate(LocalDate.now()) + "\",\n"
                + "\"TodayPlus28Days\":\"" + UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 28) + "\",\n"
                + "\"Case_No\":\"123456\",\n"
                + "\"submission_reference\":\"12212121\",\n"
                + "}\n"
                + "}\n";
        caseDetails2.getCaseData().getRespondentCollection().get(0).getValue().setResponseStruckOut(NO);
        caseDetails2.getCaseData().getRepCollection().get(0).getValue().setRespRepName("Antonio Vazquez");
        assertEquals(expected, DocumentHelper.buildDocumentContent(caseDetails2.getCaseData(), "",
                userDetails, ENGLANDWALES_CASE_TYPE_ID,
                caseDetails2.getCaseData().getCorrespondenceType(),
                caseDetails2.getCaseData().getCorrespondenceScotType(), null,
                null, venueAddressReaderService).toString());
        caseDetails2.getCaseData().getRepCollection().get(0).getValue().setRespRepName("RepresentativeNameRespondent");
    }

    @Test
    void buildDocumentContent3() {
        String expected = "{\n"
                + "\"accessKey\":\"\",\n"
                + "\"templateName\":\"EM-TRB-EGW-ENG-00028.docx\",\n"
                + "\"outputName\":\"document.docx\",\n"
                + "\"data\":{\n"
                + "\"claimant_or_rep_full_name\":\"Mr A J Rodriguez\",\n"
                + "\"claimant_full_name\":\"Mr A J Rodriguez\",\n"
                + "\"Claimant\":\"Mr A J Rodriguez\",\n"
                + "\"claimant_or_rep_addressLine1\":\"34\",\n"
                + "\"claimant_or_rep_addressLine2\":\"Low Street\",\n"
                + "\"claimant_or_rep_addressLine3\":\"\",\n"
                + "\"claimant_or_rep_town\":\"Manchester\",\n"
                + "\"claimant_or_rep_county\":\"Lancashire\",\n"
                + "\"claimant_or_rep_postCode\":\"M3 6gw\",\n"
                + "\"claimant_addressLine1\":\"34\",\n"
                + "\"claimant_addressLine2\":\"Low Street\",\n"
                + "\"claimant_addressLine3\":\"\",\n"
                + "\"claimant_town\":\"Manchester\",\n"
                + "\"claimant_county\":\"Lancashire\",\n"
                + "\"claimant_postCode\":\"M3 6gw\",\n"
                + "\"respondent_or_rep_full_name\":\"RepresentativeNameRespondent\",\n"
                + "\"respondent_or_rep_addressLine1\":\"56 Block C\",\n"
                + "\"respondent_or_rep_addressLine2\":\"Ellesmere Street\",\n"
                + "\"respondent_or_rep_addressLine3\":\"\",\n"
                + "\"respondent_or_rep_town\":\"Manchester\",\n"
                + "\"respondent_or_rep_county\":\"Lancashire\",\n"
                + "\"respondent_or_rep_postCode\":\"M3 KJR\",\n"
                + "\"respondent_reference\":\"1111111\",\n"
                + "\"respondent_rep_organisation\":\"RepresentativeOrganisation\",\n"
                + "\"respondent_full_name\":\"Antonio Vazquez\",\n"
                + "\"respondent_addressLine1\":\"11 Small Street\",\n"
                + "\"respondent_addressLine2\":\"22 House\",\n"
                + "\"respondent_addressLine3\":\"\",\n"
                + "\"respondent_town\":\"Manchester\",\n"
                + "\"respondent_county\":\"North West\",\n"
                + "\"respondent_postCode\":\"M12 42R\",\n"
                + "\"Respondent\":\"Antonio Vazquez\",\n"
                + "\"resp_others\":\"\",\n"
                + "\"resp_address\":\"11 Small Street, 22 House, Manchester, North West, M12 42R, UK\",\n"
                + "\"Hearing_date\":\"\",\n"
                + "\"Hearing_date_time\":\"\",\n"
                + "\"Hearing_venue\":\"\",\n"
                + "\"Hearing_duration\":\"\",\n"
                + "\"Hearing_time\":\"\",\n"
                + "\"t3_2\":\"true\",\n"
                + "\"Court_addressLine1\":\"Manchester Employment Tribunal,\",\n"
                + "\"Court_addressLine2\":\"Alexandra House,\",\n"
                + "\"Court_addressLine3\":\"14-22 The Parsonage,\",\n"
                + "\"Court_town\":\"Manchester,\",\n"
                + "\"Court_county\":\"\",\n"
                + "\"Court_postCode\":\"M3 2JA\",\n"
                + "\"Court_telephone\":\"03577131270\",\n"
                + "\"Court_fax\":\"07577126570\",\n"
                + "\"Court_DX\":\"123456\",\n"
                + "\"Court_Email\":\"ManchesterOfficeET@hmcts.gov.uk\",\n"
                + "\"i3_2_enhmcts\":\"[userImage:enhmcts.png]\",\n"
                + "\"i3_2_enhmcts1\":\"[userImage:enhmcts.png]\",\n"
                + "\"i3_2_enhmcts2\":\"[userImage:enhmcts.png]\",\n"
                + "\"iScot_schmcts\":\"[userImage:schmcts.png]\",\n"
                + "\"iScot_schmcts1\":\"[userImage:schmcts.png]\",\n"
                + "\"iScot_schmcts2\":\"[userImage:schmcts.png]\",\n"
                + "\"Clerk\":\"Mike Jordan\",\n"
                + "\"Today_date\":\"" + UtilHelper.formatCurrentDate(LocalDate.now()) + "\",\n"
                + "\"TodayPlus28Days\":\"" + UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 28) + "\",\n"
                + "\"Case_No\":\"123456\",\n"
                + "\"submission_reference\":\"12212121\",\n"
                + "}\n"
                + "}\n";
        assertEquals(expected, DocumentHelper.buildDocumentContent(caseDetails3.getCaseData(), "",
                userDetails, ENGLANDWALES_CASE_TYPE_ID,
                caseDetails3.getCaseData().getCorrespondenceType(),
                caseDetails3.getCaseData().getCorrespondenceScotType(),
                null, null, venueAddressReaderService).toString());
    }

    @Test
    void buildDocumentContent4() {
        String expected = "{\n"
                + "\"accessKey\":\"\",\n"
                + "\"templateName\":\"EM-TRB-EGW-ENG-00029.docx\",\n"
                + "\"outputName\":\"document.docx\",\n"
                + "\"data\":{\n"
                + "\"claimant_or_rep_full_name\":\"RepresentativeNameClaimant\",\n"
                + "\"claimant_rep_organisation\":\"RepresentativeOrganisation\",\n"
                + "\"claimant_or_rep_addressLine1\":\"56 Block C\",\n"
                + "\"claimant_or_rep_addressLine2\":\"Ellesmere Street\",\n"
                + "\"claimant_or_rep_addressLine3\":\"\",\n"
                + "\"claimant_or_rep_town\":\"Manchester\",\n"
                + "\"claimant_or_rep_county\":\"Lancashire\",\n"
                + "\"claimant_or_rep_postCode\":\"M3 KJR\",\n"
                + "\"claimant_reference\":\"1111111\",\n"
                + "\"claimant_full_name\":\"Mr A J Rodriguez\",\n"
                + "\"Claimant\":\"Mr A J Rodriguez\",\n"
                + "\"claimant_addressLine1\":\"34\",\n"
                + "\"claimant_addressLine2\":\"Low Street\",\n"
                + "\"claimant_addressLine3\":\"\",\n"
                + "\"claimant_town\":\"Manchester\",\n"
                + "\"claimant_county\":\"Lancashire\",\n"
                + "\"claimant_postCode\":\"M3 6gw\",\n"
                + "\"respondent_or_rep_full_name\":\"Antonio Vazquez\",\n"
                + "\"respondent_or_rep_addressLine1\":\"11 Small Street\",\n"
                + "\"respondent_or_rep_addressLine2\":\"22 House\",\n"
                + "\"respondent_or_rep_addressLine3\":\"\",\n"
                + "\"respondent_or_rep_town\":\"Manchester\",\n"
                + "\"respondent_or_rep_county\":\"North West\",\n"
                + "\"respondent_or_rep_postCode\":\"M12 42R\",\n"
                + "\"respondent_full_name\":\"Antonio Vazquez\",\n"
                + "\"respondent_addressLine1\":\"11 Small Street\",\n"
                + "\"respondent_addressLine2\":\"22 House\",\n"
                + "\"respondent_addressLine3\":\"\",\n"
                + "\"respondent_town\":\"Manchester\",\n"
                + "\"respondent_county\":\"North West\",\n"
                + "\"respondent_postCode\":\"M12 42R\",\n"
                + "\"Respondent\":\"Antonio Vazquez\",\n"
                + "\"resp_others\":\"\",\n"
                + "\"resp_address\":\"11 Small Street, 22 House, Manchester, North West, M12 42R, UK\",\n"
                + "\"Hearing_date\":\"\",\n"
                + "\"Hearing_date_time\":\"\",\n"
                + "\"Hearing_venue\":\"\",\n"
                + "\"Hearing_duration\":\"\",\n"
                + "\"Hearing_time\":\"\",\n"
                + "\"t4_2\":\"true\",\n"
                + "\"Court_addressLine1\":\"Manchester Employment Tribunal,\",\n"
                + "\"Court_addressLine2\":\"Alexandra House,\",\n"
                + "\"Court_addressLine3\":\"14-22 The Parsonage,\",\n"
                + "\"Court_town\":\"Manchester,\",\n"
                + "\"Court_county\":\"\",\n"
                + "\"Court_postCode\":\"M3 2JA\",\n"
                + "\"Court_telephone\":\"03577131270\",\n"
                + "\"Court_fax\":\"07577126570\",\n"
                + "\"Court_DX\":\"123456\",\n"
                + "\"Court_Email\":\"ManchesterOfficeET@hmcts.gov.uk\",\n"
                + "\"i4_2_enhmcts\":\"[userImage:enhmcts.png]\",\n"
                + "\"i4_2_enhmcts1\":\"[userImage:enhmcts.png]\",\n"
                + "\"i4_2_enhmcts2\":\"[userImage:enhmcts.png]\",\n"
                + "\"iScot_schmcts\":\"[userImage:schmcts.png]\",\n"
                + "\"iScot_schmcts1\":\"[userImage:schmcts.png]\",\n"
                + "\"iScot_schmcts2\":\"[userImage:schmcts.png]\",\n"
                + "\"Clerk\":\"Mike Jordan\",\n"
                + "\"Today_date\":\"" + UtilHelper.formatCurrentDate(LocalDate.now()) + "\",\n"
                + "\"TodayPlus28Days\":\"" + UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 28) + "\",\n"
                + "\"Case_No\":\"123456\",\n"
                + "\"submission_reference\":\"12212121\",\n"
                + "}\n"
                + "}\n";
        assertEquals(expected, DocumentHelper.buildDocumentContent(caseDetails4.getCaseData(), "",
                userDetails, ENGLANDWALES_CASE_TYPE_ID,
                caseDetails4.getCaseData().getCorrespondenceType(),
                caseDetails4.getCaseData().getCorrespondenceScotType(),
                null, null, venueAddressReaderService).toString());
    }

    @Test
    void buildDocumentContent5() {
        String expected = "{\n"
                + "\"accessKey\":\"\",\n"
                + "\"templateName\":\"EM-TRB-EGW-ENG-00030.docx\",\n"
                + "\"outputName\":\"document.docx\",\n"
                + "\"data\":{\n"
                + "\"claimant_or_rep_full_name\":\"RepresentativeNameClaimant\",\n"
                + "\"claimant_rep_organisation\":\"RepresentativeOrganisation\",\n"
                + "\"claimant_or_rep_addressLine1\":\"56 Block C\",\n"
                + "\"claimant_or_rep_addressLine2\":\"Ellesmere Street\",\n"
                + "\"claimant_or_rep_addressLine3\":\"\",\n"
                + "\"claimant_or_rep_town\":\"Manchester\",\n"
                + "\"claimant_or_rep_county\":\"Lancashire\",\n"
                + "\"claimant_or_rep_postCode\":\"M3 KJR\",\n"
                + "\"claimant_reference\":\"1111111\",\n"
                + "\"claimant_full_name\":\"Mr A J Rodriguez\",\n"
                + "\"Claimant\":\"Mr A J Rodriguez\",\n"
                + "\"claimant_addressLine1\":\"34\",\n"
                + "\"claimant_addressLine2\":\"Low Street\",\n"
                + "\"claimant_addressLine3\":\"\",\n"
                + "\"claimant_town\":\"Manchester\",\n"
                + "\"claimant_county\":\"Lancashire\",\n"
                + "\"claimant_postCode\":\"M3 6gw\",\n"
                + "\"respondent_or_rep_full_name\":\"RepresentativeNameRespondent1\",\n"
                + "\"respondent_or_rep_addressLine1\":\"56 Block C\",\n"
                + "\"respondent_or_rep_addressLine2\":\"Ellesmere Street\",\n"
                + "\"respondent_or_rep_addressLine3\":\"\",\n"
                + "\"respondent_or_rep_town\":\"Manchester\",\n"
                + "\"respondent_or_rep_county\":\"Lancashire\",\n"
                + "\"respondent_or_rep_postCode\":\"M3 KJR\",\n"
                + "\"respondent_reference\":\"3333333333\",\n"
                + "\"respondent_rep_organisation\":\"RepresentativeOrganisation\",\n"
                + "\"respondent_full_name\":\"Antonio Vazquez\",\n"
                + "\"respondent_addressLine1\":\"11 Small Street\",\n"
                + "\"respondent_addressLine2\":\"22 House\",\n"
                + "\"respondent_addressLine3\":\"\",\n"
                + "\"respondent_town\":\"Manchester\",\n"
                + "\"respondent_county\":\"North West\",\n"
                + "\"respondent_postCode\":\"M12 42R\",\n"
                + "\"Respondent\":\"1. Antonio Vazquez,\",\n"
                + "\"resp_others\":\"2. Antonio Vazquez2\",\n"
                + "\"resp_address\":\"1. 11 Small Street, 22 House, Manchester, North West, M12 42R, UK\\n2. 11 Small"
                + " Street, 22 House, Manchester, North West, M12 42R, UK\",\n"
                + "\"Hearing_date\":\"\",\n"
                + "\"Hearing_date_time\":\"\",\n"
                + "\"Hearing_venue\":\"\",\n"
                + "\"Hearing_duration\":\"\",\n"
                + "\"Hearing_time\":\"\",\n"
                + "\"t5_2\":\"true\",\n"
                + "\"Court_addressLine1\":\"Manchester Employment Tribunal,\",\n"
                + "\"Court_addressLine2\":\"Alexandra House,\",\n"
                + "\"Court_addressLine3\":\"14-22 The Parsonage,\",\n"
                + "\"Court_town\":\"Manchester,\",\n"
                + "\"Court_county\":\"\",\n"
                + "\"Court_postCode\":\"M3 2JA\",\n"
                + "\"Court_telephone\":\"03577131270\",\n"
                + "\"Court_fax\":\"07577126570\",\n"
                + "\"Court_DX\":\"123456\",\n"
                + "\"Court_Email\":\"ManchesterOfficeET@hmcts.gov.uk\",\n"
                + "\"i5_2_enhmcts\":\"[userImage:enhmcts.png]\",\n"
                + "\"i5_2_enhmcts1\":\"[userImage:enhmcts.png]\",\n"
                + "\"i5_2_enhmcts2\":\"[userImage:enhmcts.png]\",\n"
                + "\"iScot_schmcts\":\"[userImage:schmcts.png]\",\n"
                + "\"iScot_schmcts1\":\"[userImage:schmcts.png]\",\n"
                + "\"iScot_schmcts2\":\"[userImage:schmcts.png]\",\n"
                + "\"Clerk\":\"Mike Jordan\",\n"
                + "\"Today_date\":\"" + UtilHelper.formatCurrentDate(LocalDate.now()) + "\",\n"
                + "\"TodayPlus28Days\":\"" + UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 28) + "\",\n"
                + "\"Case_No\":\"123456\",\n"
                + "\"submission_reference\":\"12212121\",\n"
                + "}\n"
                + "}\n";
        assertEquals(expected, DocumentHelper.buildDocumentContent(caseDetails5.getCaseData(), "",
                userDetails, ENGLANDWALES_CASE_TYPE_ID,
                caseDetails5.getCaseData().getCorrespondenceType(),
                caseDetails5.getCaseData().getCorrespondenceScotType(),
                null, null, venueAddressReaderService).toString());
    }

    @Test
    void buildDocumentContent6() {
        String expected = "{\n"
                + "\"accessKey\":\"\",\n"
                + "\"templateName\":\"EM-TRB-EGW-ENG-00031.docx\",\n"
                + "\"outputName\":\"document.docx\",\n"
                + "\"data\":{\n"
                + "\"claimant_or_rep_full_name\":\"RepresentativeNameClaimant\",\n"
                + "\"claimant_rep_organisation\":\"RepresentativeOrganisation\",\n"
                + "\"claimant_or_rep_addressLine1\":\"56 Block C\",\n"
                + "\"claimant_or_rep_addressLine2\":\"Ellesmere Street\",\n"
                + "\"claimant_or_rep_addressLine3\":\"\",\n"
                + "\"claimant_or_rep_town\":\"Manchester\",\n"
                + "\"claimant_or_rep_county\":\"Lancashire\",\n"
                + "\"claimant_or_rep_postCode\":\"M3 KJR\",\n"
                + "\"claimant_reference\":\"1111111\",\n"
                + "\"claimant_full_name\":\"Orlando LTD\",\n"
                + "\"Claimant\":\"Orlando LTD\",\n"
                + "\"claimant_addressLine1\":\"34\",\n"
                + "\"claimant_addressLine2\":\"Low Street\",\n"
                + "\"claimant_addressLine3\":\"\",\n"
                + "\"claimant_town\":\"Manchester\",\n"
                + "\"claimant_county\":\"Lancashire\",\n"
                + "\"claimant_postCode\":\"M3 6gw\",\n"
                + "\"respondent_or_rep_full_name\":\"RepresentativeNameRespondent1\",\n"
                + "\"respondent_or_rep_addressLine1\":\"56 Block C\",\n"
                + "\"respondent_or_rep_addressLine2\":\"Ellesmere Street\",\n"
                + "\"respondent_or_rep_addressLine3\":\"\",\n"
                + "\"respondent_or_rep_town\":\"Manchester\",\n"
                + "\"respondent_or_rep_county\":\"Lancashire\",\n"
                + "\"respondent_or_rep_postCode\":\"M3 KJR\",\n"
                + "\"respondent_reference\":\"3333333333\",\n"
                + "\"respondent_rep_organisation\":\"RepresentativeOrganisation\",\n"
                + "\"respondent_full_name\":\"Antonio Vazquez\",\n"
                + "\"respondent_addressLine1\":\"11 Small Street\",\n"
                + "\"respondent_addressLine2\":\"22 House\",\n"
                + "\"respondent_addressLine3\":\"\",\n"
                + "\"respondent_town\":\"Manchester\",\n"
                + "\"respondent_county\":\"North West\",\n"
                + "\"respondent_postCode\":\"M12 42R\",\n"
                + "\"Respondent\":\"Antonio Vazquez\",\n"
                + "\"resp_others\":\"\",\n"
                + "\"resp_address\":\"11 Small Street, 22 House, Manchester, North West, M12 42R, UK\",\n"
                + "\"Hearing_date\":\"\",\n"
                + "\"Hearing_date_time\":\"\",\n"
                + "\"Hearing_venue\":\"\",\n"
                + "\"Hearing_duration\":\"\",\n"
                + "\"Hearing_time\":\"\",\n"
                + "\"t6_2\":\"true\",\n"
                + "\"Court_addressLine1\":\"Manchester Employment Tribunal,\",\n"
                + "\"Court_addressLine2\":\"Alexandra House,\",\n"
                + "\"Court_addressLine3\":\"14-22 The Parsonage,\",\n"
                + "\"Court_town\":\"Manchester,\",\n"
                + "\"Court_county\":\"\",\n"
                + "\"Court_postCode\":\"M3 2JA\",\n"
                + "\"Court_telephone\":\"03577131270\",\n"
                + "\"Court_fax\":\"07577126570\",\n"
                + "\"Court_DX\":\"123456\",\n"
                + "\"Court_Email\":\"ManchesterOfficeET@hmcts.gov.uk\",\n"
                + "\"i6_2_enhmcts\":\"[userImage:enhmcts.png]\",\n"
                + "\"i6_2_enhmcts1\":\"[userImage:enhmcts.png]\",\n"
                + "\"i6_2_enhmcts2\":\"[userImage:enhmcts.png]\",\n"
                + "\"iScot_schmcts\":\"[userImage:schmcts.png]\",\n"
                + "\"iScot_schmcts1\":\"[userImage:schmcts.png]\",\n"
                + "\"iScot_schmcts2\":\"[userImage:schmcts.png]\",\n"
                + "\"Clerk\":\"Mike Jordan\",\n"
                + "\"Today_date\":\"" + UtilHelper.formatCurrentDate(LocalDate.now()) + "\",\n"
                + "\"TodayPlus28Days\":\"" + UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 28) + "\",\n"
                + "\"Case_No\":\"123456\",\n"
                + "\"submission_reference\":\"12212121\",\n"
                + "}\n"
                + "}\n";
        assertEquals(expected, DocumentHelper.buildDocumentContent(caseDetails6.getCaseData(), "",
                userDetails, ENGLANDWALES_CASE_TYPE_ID,
                caseDetails6.getCaseData().getCorrespondenceType(),
                caseDetails6.getCaseData().getCorrespondenceScotType(),
                null, null, venueAddressReaderService).toString());
    }

    @Test
    void buildDocumentContent7() {
        String expected = "{\n"
                + "\"accessKey\":\"\",\n"
                + "\"templateName\":\"EM-TRB-EGW-ENG-00032.docx\",\n"
                + "\"outputName\":\"document.docx\",\n"
                + "\"data\":{\n"
                + "\"claimant_or_rep_full_name\":\"RepresentativeNameClaimant\",\n"
                + "\"claimant_rep_organisation\":\"RepresentativeOrganisation\",\n"
                + "\"claimant_or_rep_addressLine1\":\"56 Block C\",\n"
                + "\"claimant_or_rep_addressLine2\":\"Ellesmere Street\",\n"
                + "\"claimant_or_rep_addressLine3\":\"\",\n"
                + "\"claimant_or_rep_town\":\"Manchester\",\n"
                + "\"claimant_or_rep_county\":\"Lancashire\",\n"
                + "\"claimant_or_rep_postCode\":\"M3 KJR\",\n"
                + "\"claimant_reference\":\"1111111\",\n"
                + "\"claimant_full_name\":\"Mr A J Rodriguez\",\n"
                + "\"Claimant\":\"Mr A J Rodriguez\",\n"
                + "\"claimant_addressLine1\":\"34\",\n"
                + "\"claimant_addressLine2\":\"Low Street\",\n"
                + "\"claimant_addressLine3\":\"\",\n"
                + "\"claimant_town\":\"Manchester\",\n"
                + "\"claimant_county\":\"Lancashire\",\n"
                + "\"claimant_postCode\":\"M3 6gw\",\n"
                + "\"respondent_or_rep_full_name\":\"RepresentativeNameRespondent1\",\n"
                + "\"respondent_or_rep_addressLine1\":\"56 Block C\",\n"
                + "\"respondent_or_rep_addressLine2\":\"Ellesmere Street\",\n"
                + "\"respondent_or_rep_addressLine3\":\"\",\n"
                + "\"respondent_or_rep_town\":\"Manchester\",\n"
                + "\"respondent_or_rep_county\":\"Lancashire\",\n"
                + "\"respondent_or_rep_postCode\":\"M3 KJR\",\n"
                + "\"respondent_reference\":\"3333333333\",\n"
                + "\"respondent_rep_organisation\":\"RepresentativeOrganisation\",\n"
                + "\"respondent_full_name\":\"Antonio Vazquez\",\n"
                + "\"respondent_addressLine1\":\"11 Small Street\",\n"
                + "\"respondent_addressLine2\":\"22 House\",\n"
                + "\"respondent_addressLine3\":\"\",\n"
                + "\"respondent_town\":\"Manchester\",\n"
                + "\"respondent_county\":\"North West\",\n"
                + "\"respondent_postCode\":\"M12 42R\",\n"
                + "\"Respondent\":\"Antonio Vazquez\",\n"
                + "\"resp_others\":\"\",\n"
                + "\"resp_address\":\"11 Small Street, 22 House, Manchester, North West, M12 42R, UK\",\n"
                + "\"Hearing_date\":\"\",\n"
                + "\"Hearing_date_time\":\"\",\n"
                + "\"Hearing_time\":\"\",\n"
                + "\"Hearing_venue\":\"Manchester Employment Tribunals, Alexandra House, 14-22 The Parsonage, "
                + "Manchester, M3 2JA\",\n"
                + "\"Hearing_duration\":\"2 days\",\n"
                + "\"t7_2\":\"true\",\n"
                + "\"Court_addressLine1\":\"Manchester Employment Tribunal,\",\n"
                + "\"Court_addressLine2\":\"Alexandra House,\",\n"
                + "\"Court_addressLine3\":\"14-22 The Parsonage,\",\n"
                + "\"Court_town\":\"Manchester,\",\n"
                + "\"Court_county\":\"\",\n"
                + "\"Court_postCode\":\"M3 2JA\",\n"
                + "\"Court_telephone\":\"03577131270\",\n"
                + "\"Court_fax\":\"07577126570\",\n"
                + "\"Court_DX\":\"123456\",\n"
                + "\"Court_Email\":\"ManchesterOfficeET@hmcts.gov.uk\",\n"
                + "\"i7_2_enhmcts\":\"[userImage:enhmcts.png]\",\n"
                + "\"i7_2_enhmcts1\":\"[userImage:enhmcts.png]\",\n"
                + "\"i7_2_enhmcts2\":\"[userImage:enhmcts.png]\",\n"
                + "\"iScot_schmcts\":\"[userImage:schmcts.png]\",\n"
                + "\"iScot_schmcts1\":\"[userImage:schmcts.png]\",\n"
                + "\"iScot_schmcts2\":\"[userImage:schmcts.png]\",\n"
                + "\"Clerk\":\"Mike Jordan\",\n"
                + "\"Today_date\":\"" + UtilHelper.formatCurrentDate(LocalDate.now()) + "\",\n"
                + "\"TodayPlus28Days\":\"" + UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 28) + "\",\n"
                + "\"Case_No\":\"123456\",\n"
                + "\"submission_reference\":\"12212121\",\n"
                + "}\n"
                + "}\n";
        assertEquals(expected, DocumentHelper.buildDocumentContent(caseDetails7.getCaseData(), "",
                userDetails, ENGLANDWALES_CASE_TYPE_ID,
                caseDetails7.getCaseData().getCorrespondenceType(),
                caseDetails7.getCaseData().getCorrespondenceScotType(),
                null, null, venueAddressReaderService).toString());
    }

    @Test
    void buildDocumentContent8() {
        String expected = "{\n"
                + "\"accessKey\":\"\",\n"
                + "\"templateName\":\"EM-TRB-EGW-ENG-00033.docx\",\n"
                + "\"outputName\":\"document.docx\",\n"
                + "\"data\":{\n"
                + "\"claimant_or_rep_full_name\":\"RepresentativeNameClaimant\",\n"
                + "\"claimant_rep_organisation\":\"RepresentativeOrganisation\",\n"
                + "\"claimant_or_rep_addressLine1\":\"56 Block C\",\n"
                + "\"claimant_or_rep_addressLine2\":\"Ellesmere Street\",\n"
                + "\"claimant_or_rep_addressLine3\":\"\",\n"
                + "\"claimant_or_rep_town\":\"Manchester\",\n"
                + "\"claimant_or_rep_county\":\"Lancashire\",\n"
                + "\"claimant_or_rep_postCode\":\"M3 KJR\",\n"
                + "\"claimant_reference\":\"1111111\",\n"
                + "\"claimant_full_name\":\"Mr A J Rodriguez\",\n"
                + "\"Claimant\":\"Mr A J Rodriguez\",\n"
                + "\"claimant_addressLine1\":\"34\",\n"
                + "\"claimant_addressLine2\":\"Low Street\",\n"
                + "\"claimant_addressLine3\":\"\",\n"
                + "\"claimant_town\":\"Manchester\",\n"
                + "\"claimant_county\":\"Lancashire\",\n"
                + "\"claimant_postCode\":\"M3 6gw\",\n"
                + "\"respondent_or_rep_full_name\":\"Antonio Vazquez\",\n"
                + "\"respondent_or_rep_addressLine1\":\"11 Small Street\",\n"
                + "\"respondent_or_rep_addressLine2\":\"22 House\",\n"
                + "\"respondent_or_rep_addressLine3\":\"\",\n"
                + "\"respondent_or_rep_town\":\"Manchester\",\n"
                + "\"respondent_or_rep_county\":\"North West\",\n"
                + "\"respondent_or_rep_postCode\":\"M12 42R\",\n"
                + "\"respondent_full_name\":\"Antonio Vazquez\",\n"
                + "\"respondent_addressLine1\":\"11 Small Street\",\n"
                + "\"respondent_addressLine2\":\"22 House\",\n"
                + "\"respondent_addressLine3\":\"\",\n"
                + "\"respondent_town\":\"Manchester\",\n"
                + "\"respondent_county\":\"North West\",\n"
                + "\"respondent_postCode\":\"M12 42R\",\n"
                + "\"Respondent\":\"1. Antonio Vazquez,\",\n"
                + "\"resp_others\":\"2. Mikey McCollier\",\n"
                + "\"resp_address\":\"1. 11 Small Street, 22 House, Manchester, North West, M12 42R, UK\\n2. 1333 "
                + "Small Street, 22222 House, Liverpool, North West, L12 42R, UK\",\n"
                + "\"Hearing_date\":\"25 November 2019\",\n"
                + "\"Hearing_date_time\":\"25 November 2019 at 12:11\",\n"
                + "\"Hearing_time\":\"12:11\",\n"
                + "\"Hearing_venue\":\"Manchester Employment Tribunals, Alexandra House, 14-22 The Parsonage, "
                + "Manchester, M3 2JA\",\n"
                + "\"Hearing_duration\":\"2 days\",\n"
                + "\"t10_2\":\"true\",\n"
                + "\"Court_addressLine1\":\"Manchester Employment Tribunal,\",\n"
                + "\"Court_addressLine2\":\"Alexandra House,\",\n"
                + "\"Court_addressLine3\":\"14-22 The Parsonage,\",\n"
                + "\"Court_town\":\"Manchester,\",\n"
                + "\"Court_county\":\"\",\n"
                + "\"Court_postCode\":\"M3 2JA\",\n"
                + "\"Court_telephone\":\"03577131270\",\n"
                + "\"Court_fax\":\"07577126570\",\n"
                + "\"Court_DX\":\"123456\",\n"
                + "\"Court_Email\":\"ManchesterOfficeET@hmcts.gov.uk\",\n"
                + "\"i10_2_enhmcts\":\"[userImage:enhmcts.png]\",\n"
                + "\"i10_2_enhmcts1\":\"[userImage:enhmcts.png]\",\n"
                + "\"i10_2_enhmcts2\":\"[userImage:enhmcts.png]\",\n"
                + "\"iScot_schmcts\":\"[userImage:schmcts.png]\",\n"
                + "\"iScot_schmcts1\":\"[userImage:schmcts.png]\",\n"
                + "\"iScot_schmcts2\":\"[userImage:schmcts.png]\",\n"
                + "\"Clerk\":\"Mike Jordan\",\n"
                + "\"Today_date\":\"" + UtilHelper.formatCurrentDate(LocalDate.now()) + "\",\n"
                + "\"TodayPlus28Days\":\"" + UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 28) + "\",\n"
                + "\"Case_No\":\"123456\",\n"
                + "\"submission_reference\":\"12212121\",\n"
                + "}\n"
                + "}\n";
        assertEquals(expected, DocumentHelper.buildDocumentContent(caseDetails8.getCaseData(), "",
                userDetails, ENGLANDWALES_CASE_TYPE_ID,
                caseDetails8.getCaseData().getCorrespondenceType(),
                caseDetails8.getCaseData().getCorrespondenceScotType(),
                null, null, venueAddressReaderService).toString());
    }

    @Test
    void buildDocumentContent9() {
        String expected = "{\n"
                + "\"accessKey\":\"\",\n"
                + "\"templateName\":\"EM-TRB-EGW-ENG-00034.docx\",\n"
                + "\"outputName\":\"document.docx\",\n"
                + "\"data\":{\n"
                + "\"claimant_or_rep_full_name\":\"RepresentativeNameClaimant\",\n"
                + "\"claimant_rep_organisation\":\"RepresentativeOrganisation\",\n"
                + "\"claimant_or_rep_addressLine1\":\"56 Block C\",\n"
                + "\"claimant_or_rep_addressLine2\":\"Ellesmere Street\",\n"
                + "\"claimant_or_rep_addressLine3\":\"\",\n"
                + "\"claimant_or_rep_town\":\"Manchester\",\n"
                + "\"claimant_or_rep_county\":\"Lancashire\",\n"
                + "\"claimant_or_rep_postCode\":\"M3 KJR\",\n"
                + "\"claimant_reference\":\"1111111\",\n"
                + "\"claimant_full_name\":\"Mr A J Rodriguez\",\n"
                + "\"Claimant\":\"Mr A J Rodriguez\",\n"
                + "\"claimant_addressLine1\":\"34\",\n"
                + "\"claimant_addressLine2\":\"Low Street\",\n"
                + "\"claimant_addressLine3\":\"\",\n"
                + "\"claimant_town\":\"Manchester\",\n"
                + "\"claimant_county\":\"Lancashire\",\n"
                + "\"claimant_postCode\":\"M3 6gw\",\n"
                + "\"respondent_or_rep_full_name\":\"Antonio Vazquez\",\n"
                + "\"respondent_or_rep_addressLine1\":\"11 Small Street\",\n"
                + "\"respondent_or_rep_addressLine2\":\"22 House\",\n"
                + "\"respondent_or_rep_addressLine3\":\"\",\n"
                + "\"respondent_or_rep_town\":\"Manchester\",\n"
                + "\"respondent_or_rep_county\":\"North West\",\n"
                + "\"respondent_or_rep_postCode\":\"M12 42R\",\n"
                + "\"respondent_full_name\":\"Antonio Vazquez\",\n"
                + "\"respondent_addressLine1\":\"11 Small Street\",\n"
                + "\"respondent_addressLine2\":\"22 House\",\n"
                + "\"respondent_addressLine3\":\"\",\n"
                + "\"respondent_town\":\"Manchester\",\n"
                + "\"respondent_county\":\"North West\",\n"
                + "\"respondent_postCode\":\"M12 42R\",\n"
                + "\"Respondent\":\"Antonio Vazquez\",\n"
                + "\"resp_others\":\"\",\n"
                + "\"resp_address\":\"11 Small Street, 22 House, Manchester, North West, M12 42R, UK\",\n"
                + "\"Hearing_date\":\"25 November 2019\",\n"
                + "\"Hearing_date_time\":\"25 November 2019 at 12:11\",\n"
                + "\"Hearing_time\":\"12:11\",\n"
                + "\"Hearing_venue\":\"Manchester Employment Tribunals, Alexandra House, 14-22 The Parsonage, "
                + "Manchester, M3 2JA\",\n"
                + "\"Hearing_duration\":\"2 days\",\n"
                + "\"t9_2\":\"true\",\n"
                + "\"Court_addressLine1\":\"Manchester Employment Tribunal,\",\n"
                + "\"Court_addressLine2\":\"Alexandra House,\",\n"
                + "\"Court_addressLine3\":\"14-22 The Parsonage,\",\n"
                + "\"Court_town\":\"Manchester,\",\n"
                + "\"Court_county\":\"\",\n"
                + "\"Court_postCode\":\"M3 2JA\",\n"
                + "\"Court_telephone\":\"03577131270\",\n"
                + "\"Court_fax\":\"07577126570\",\n"
                + "\"Court_DX\":\"123456\",\n"
                + "\"Court_Email\":\"ManchesterOfficeET@hmcts.gov.uk\",\n"
                + "\"i9_2_enhmcts\":\"[userImage:enhmcts.png]\",\n"
                + "\"i9_2_enhmcts1\":\"[userImage:enhmcts.png]\",\n"
                + "\"i9_2_enhmcts2\":\"[userImage:enhmcts.png]\",\n"
                + "\"iScot_schmcts\":\"[userImage:schmcts.png]\",\n"
                + "\"iScot_schmcts1\":\"[userImage:schmcts.png]\",\n"
                + "\"iScot_schmcts2\":\"[userImage:schmcts.png]\",\n"
                + "\"Clerk\":\"Mike Jordan\",\n"
                + "\"Today_date\":\"" + UtilHelper.formatCurrentDate(LocalDate.now()) + "\",\n"
                + "\"TodayPlus28Days\":\"" + UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 28) + "\",\n"
                + "\"Case_No\":\"123456\",\n"
                + "\"submission_reference\":\"12212121\",\n"
                + "}\n"
                + "}\n";
        assertEquals(expected, DocumentHelper.buildDocumentContent(caseDetails9.getCaseData(), "",
                userDetails, ENGLANDWALES_CASE_TYPE_ID,
                caseDetails9.getCaseData().getCorrespondenceType(),
                caseDetails9.getCaseData().getCorrespondenceScotType(),
                null, null, venueAddressReaderService).toString());
    }

    @Test
    void buildDocumentContent10() {
        String expected = "{\n"
                + "\"accessKey\":\"\",\n"
                + "\"templateName\":\"EM-TRB-LET-ENG-00544.docx\",\n"
                + "\"outputName\":\"document.docx\",\n"
                + "\"data\":{\n"
                + "\"address_labels_page\":[\n"
                + "],\n"
                + "\"i0_1_enhmcts\":\"[userImage:enhmcts.png]\",\n"
                + "\"i0_1_enhmcts1\":\"[userImage:enhmcts.png]\",\n"
                + "\"i0_1_enhmcts2\":\"[userImage:enhmcts.png]\",\n"
                + "\"iScot_schmcts\":\"[userImage:schmcts.png]\",\n"
                + "\"iScot_schmcts1\":\"[userImage:schmcts.png]\",\n"
                + "\"iScot_schmcts2\":\"[userImage:schmcts.png]\",\n"
                + "\"Clerk\":\"Mike Jordan\",\n"
                + "\"Today_date\":\"" + UtilHelper.formatCurrentDate(LocalDate.now()) + "\",\n"
                + "\"TodayPlus28Days\":\"" + UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 28) + "\",\n"
                + "\"Case_No\":\"123456\",\n"
                + "\"submission_reference\":\"12212121\",\n"
                + "}\n"
                + "}\n";
        assertEquals(expected, DocumentHelper
                .buildDocumentContent(caseDetails10.getCaseData(), "", userDetails, ENGLANDWALES_CASE_TYPE_ID,
                        caseDetails10.getCaseData().getCorrespondenceType(),
                        caseDetails10.getCaseData().getCorrespondenceScotType(),
                        null, null, venueAddressReaderService).toString());
    }

    @Test
    void buildDocumentContent12() {
        String expected = "{\n"
                + "\"accessKey\":\"\",\n"
                + "\"templateName\":\"EM-TRB-LET-ENG-00544.docx\",\n"
                + "\"outputName\":\"document.docx\",\n"
                + "\"data\":{\n"
                + "\"address_labels_page\":[\n"
                + "{\"Label_01_Entity_Name_01\":\"Claimant Name\",\n"
                + "\"Label_01_Entity_Name_02\":\"\",\n"
                + "\"Label_01_Address_Line_01\":\"11 Block A\",\n"
                + "\"Label_01_Address_Line_02\":\"Ellesmere Street\",\n"
                + "\"Label_01_Address_Line_03\":\"Address Line 3\",\n"
                + "\"Label_01_Address_Line_04\":\"Manchester\",\n"
                + "\"Label_01_Address_Line_05\":\"Lancashire M1 KJR\",\n"
                + "\"Label_01_Telephone\":\"07577 136511\",\n"
                + "\"Label_01_Fax\":\"\",\n"
                + "\"lbl_01_Eef\":\"\",\n"
                + "\"lbl_01_Cef\":\"1850011/2020\",\n"
                + "\"Label_02_Entity_Name_01\":\"Claimant Rep\",\n"
                + "\"Label_02_Entity_Name_02\":\"Claimant Org\",\n"
                + "\"Label_02_Address_Line_01\":\"22 Block B\",\n"
                + "\"Label_02_Address_Line_02\":\"Ellesmere Street\",\n"
                + "\"Label_02_Address_Line_03\":\"Address Line 3\",\n"
                + "\"Label_02_Address_Line_04\":\"Manchester\",\n"
                + "\"Label_02_Address_Line_05\":\"Lancashire M2 KJR\",\n"
                + "\"Label_02_Telephone\":\"07577 136722\",\n"
                + "\"Label_02_Fax\":\"\",\n"
                + "\"lbl_02_Eef\":\"OSCA/222/ABC\",\n"
                + "\"lbl_02_Cef\":\"1850022/2020\"}],\n"
                + "\"i0_2_enhmcts\":\"[userImage:enhmcts.png]\",\n"
                + "\"i0_2_enhmcts1\":\"[userImage:enhmcts.png]\",\n"
                + "\"i0_2_enhmcts2\":\"[userImage:enhmcts.png]\",\n"
                + "\"iScot_schmcts\":\"[userImage:schmcts.png]\",\n"
                + "\"iScot_schmcts1\":\"[userImage:schmcts.png]\",\n"
                + "\"iScot_schmcts2\":\"[userImage:schmcts.png]\",\n"
                + "\"Clerk\":\"Mike Jordan\",\n"
                + "\"Today_date\":\"" + UtilHelper.formatCurrentDate(LocalDate.now()) + "\",\n"
                + "\"TodayPlus28Days\":\"" + UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 28) + "\",\n"
                + "\"Case_No\":\"123456\",\n"
                + "\"submission_reference\":\"12212121\",\n"
                + "}\n"
                + "}\n";
        assertEquals(expected, DocumentHelper.buildDocumentContent(caseDetails12.getCaseData(), "",
                userDetails,
                ENGLANDWALES_CASE_TYPE_ID,
                caseDetails12.getCaseData().getCorrespondenceType(),
                caseDetails12.getCaseData().getCorrespondenceScotType(),
                null, null, venueAddressReaderService).toString());
    }

    @Test
    void buildDocumentContent13() {
        String expected = "{\n"
                + "\"accessKey\":\"\",\n"
                + "\"templateName\":\"EM-TRB-LET-ENG-00544.docx\",\n"
                + "\"outputName\":\"document.docx\",\n"
                + "\"data\":{\n"
                + "\"address_labels_page\":[\n"
                + "{\"Label_01_Entity_Name_01\":\"\",\n"
                + "\"Label_01_Entity_Name_02\":\"Claimant Org\",\n"
                + "\"Label_01_Address_Line_01\":\"11 Block A\",\n"
                + "\"Label_01_Address_Line_02\":\"M1 KJR\",\n"
                + "\"lbl_01_Eef\":\"\",\n"
                + "\"lbl_01_Cef\":\"1850011/2020\",\n"
                + "\"Label_02_Entity_Name_01\":\"Claimant Rep\",\n"
                + "\"Label_02_Entity_Name_02\":\"Claimant Org\",\n"
                + "\"Label_02_Address_Line_01\":\"22 Block B\",\n"
                + "\"Label_02_Address_Line_02\":\"Address Line 3\",\n"
                + "\"Label_02_Address_Line_03\":\"Lancashire\",\n"
                + "\"Label_02_Address_Line_04\":\"M2 KJR\",\n"
                + "\"lbl_02_Eef\":\"OSCA/222/ABC\",\n"
                + "\"lbl_02_Cef\":\"1850022/2020\"}],\n"
                + "\"i0_2_enhmcts\":\"[userImage:enhmcts.png]\",\n"
                + "\"i0_2_enhmcts1\":\"[userImage:enhmcts.png]\",\n"
                + "\"i0_2_enhmcts2\":\"[userImage:enhmcts.png]\",\n"
                + "\"iScot_schmcts\":\"[userImage:schmcts.png]\",\n"
                + "\"iScot_schmcts1\":\"[userImage:schmcts.png]\",\n"
                + "\"iScot_schmcts2\":\"[userImage:schmcts.png]\",\n"
                + "\"Clerk\":\"Mike Jordan\",\n"
                + "\"Today_date\":\"" + UtilHelper.formatCurrentDate(LocalDate.now()) + "\",\n"
                + "\"TodayPlus28Days\":\"" + UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 28) + "\",\n"
                + "\"Case_No\":\"123456\",\n"
                + "\"submission_reference\":\"12212121\",\n"
                + "}\n"
                + "}\n";
        assertEquals(expected, DocumentHelper.buildDocumentContent(caseDetails13.getCaseData(), "",
                userDetails, ENGLANDWALES_CASE_TYPE_ID,
                caseDetails13.getCaseData().getCorrespondenceType(),
                caseDetails13.getCaseData().getCorrespondenceScotType(),
                null, null, venueAddressReaderService).toString());
    }

    @Test
    void buildDocumentContent14() {
        String expected = "{\n"
                + "\"accessKey\":\"\",\n"
                + "\"templateName\":\"EM-TRB-LET-ENG-00544.docx\",\n"
                + "\"outputName\":\"document.docx\",\n"
                + "\"data\":{\n"
                + "\"address_labels_page\":[\n"
                + "{\"Label_01_Entity_Name_01\":\"Claimant Name\",\n"
                + "\"Label_01_Entity_Name_02\":\"\",\n"
                + "\"Label_01_Address_Line_01\":\"11 Block A\",\n"
                + "\"Label_01_Address_Line_02\":\"Ellesmere Street\",\n"
                + "\"Label_01_Address_Line_03\":\"Address Line 3\",\n"
                + "\"Label_01_Address_Line_04\":\"Manchester\",\n"
                + "\"Label_01_Address_Line_05\":\"Lancashire M1 KJR\",\n"
                + "\"Label_01_Telephone\":\"07577 136511\",\n"
                + "\"Label_01_Fax\":\"07577 136712\",\n"
                + "\"lbl_01_Eef\":\"\",\n"
                + "\"lbl_01_Cef\":\"1850011/2020\",\n"
                + "\"Label_02_Entity_Name_01\":\"Claimant Name\",\n"
                + "\"Label_02_Entity_Name_02\":\"\",\n"
                + "\"Label_02_Address_Line_01\":\"11 Block A\",\n"
                + "\"Label_02_Address_Line_02\":\"Ellesmere Street\",\n"
                + "\"Label_02_Address_Line_03\":\"Address Line 3\",\n"
                + "\"Label_02_Address_Line_04\":\"Manchester\",\n"
                + "\"Label_02_Address_Line_05\":\"Lancashire M1 KJR\",\n"
                + "\"Label_02_Telephone\":\"07577 136511\",\n"
                + "\"Label_02_Fax\":\"07577 136712\",\n"
                + "\"lbl_02_Eef\":\"\",\n"
                + "\"lbl_02_Cef\":\"1850011/2020\",\n"
                + "\"Label_03_Entity_Name_01\":\"Claimant Rep\",\n"
                + "\"Label_03_Entity_Name_02\":\"Claimant Org\",\n"
                + "\"Label_03_Address_Line_01\":\"22 Block B\",\n"
                + "\"Label_03_Address_Line_02\":\"Ellesmere Street\",\n"
                + "\"Label_03_Address_Line_03\":\"Address Line 3\",\n"
                + "\"Label_03_Address_Line_04\":\"Manchester\",\n"
                + "\"Label_03_Address_Line_05\":\"Lancashire M2 KJR\",\n"
                + "\"Label_03_Telephone\":\"07577 136521\",\n"
                + "\"Label_03_Fax\":\"07577 136722\",\n"
                + "\"lbl_03_Eef\":\"OSCA/222/ABC\",\n"
                + "\"lbl_03_Cef\":\"1850022/2020\",\n"
                + "\"Label_04_Entity_Name_01\":\"Claimant Rep\",\n"
                + "\"Label_04_Entity_Name_02\":\"Claimant Org\",\n"
                + "\"Label_04_Address_Line_01\":\"22 Block B\",\n"
                + "\"Label_04_Address_Line_02\":\"Ellesmere Street\",\n"
                + "\"Label_04_Address_Line_03\":\"Address Line 3\",\n"
                + "\"Label_04_Address_Line_04\":\"Manchester\",\n"
                + "\"Label_04_Address_Line_05\":\"Lancashire M2 KJR\",\n"
                + "\"Label_04_Telephone\":\"07577 136521\",\n"
                + "\"Label_04_Fax\":\"07577 136722\",\n"
                + "\"lbl_04_Eef\":\"OSCA/222/ABC\",\n"
                + "\"lbl_04_Cef\":\"1850022/2020\",\n"
                + "\"Label_05_Entity_Name_01\":\"Respondent One\",\n"
                + "\"Label_05_Entity_Name_02\":\"\",\n"
                + "\"Label_05_Address_Line_01\":\"33 Block C\",\n"
                + "\"Label_05_Address_Line_02\":\"Ellesmere Street\",\n"
                + "\"Label_05_Address_Line_03\":\"Address Line 3\",\n"
                + "\"Label_05_Address_Line_04\":\"Manchester\",\n"
                + "\"Label_05_Address_Line_05\":\"Lancashire M3 KJR\",\n"
                + "\"Label_05_Telephone\":\"07577 136531\",\n"
                + "\"Label_05_Fax\":\"07577 136732\",\n"
                + "\"lbl_05_Eef\":\"\",\n"
                + "\"lbl_05_Cef\":\"1850033/2020\",\n"
                + "\"Label_06_Entity_Name_01\":\"Respondent One\",\n"
                + "\"Label_06_Entity_Name_02\":\"\",\n"
                + "\"Label_06_Address_Line_01\":\"33 Block C\",\n"
                + "\"Label_06_Address_Line_02\":\"Ellesmere Street\",\n"
                + "\"Label_06_Address_Line_03\":\"Address Line 3\",\n"
                + "\"Label_06_Address_Line_04\":\"Manchester\",\n"
                + "\"Label_06_Address_Line_05\":\"Lancashire M3 KJR\",\n"
                + "\"Label_06_Telephone\":\"07577 136531\",\n"
                + "\"Label_06_Fax\":\"07577 136732\",\n"
                + "\"lbl_06_Eef\":\"\",\n"
                + "\"lbl_06_Cef\":\"1850033/2020\",\n"
                + "\"Label_07_Entity_Name_01\":\"Respondent Two\",\n"
                + "\"Label_07_Entity_Name_02\":\"\",\n"
                + "\"Label_07_Address_Line_01\":\"44 Block D\",\n"
                + "\"Label_07_Address_Line_02\":\"Ellesmere Street\",\n"
                + "\"Label_07_Address_Line_03\":\"Address Line 3\",\n"
                + "\"Label_07_Address_Line_04\":\"Manchester\",\n"
                + "\"Label_07_Address_Line_05\":\"Lancashire M4 KJR\",\n"
                + "\"Label_07_Telephone\":\"07577 136541\",\n"
                + "\"Label_07_Fax\":\"07577 136742\",\n"
                + "\"lbl_07_Eef\":\"\",\n"
                + "\"lbl_07_Cef\":\"1850044/2020\",\n"
                + "\"Label_08_Entity_Name_01\":\"Respondent Two\",\n"
                + "\"Label_08_Entity_Name_02\":\"\",\n"
                + "\"Label_08_Address_Line_01\":\"44 Block D\",\n"
                + "\"Label_08_Address_Line_02\":\"Ellesmere Street\",\n"
                + "\"Label_08_Address_Line_03\":\"Address Line 3\",\n"
                + "\"Label_08_Address_Line_04\":\"Manchester\",\n"
                + "\"Label_08_Address_Line_05\":\"Lancashire M4 KJR\",\n"
                + "\"Label_08_Telephone\":\"07577 136541\",\n"
                + "\"Label_08_Fax\":\"07577 136742\",\n"
                + "\"lbl_08_Eef\":\"\",\n"
                + "\"lbl_08_Cef\":\"1850044/2020\",\n"
                + "\"Label_09_Entity_Name_01\":\"Respondent three\",\n"
                + "\"Label_09_Entity_Name_02\":\"\",\n"
                + "\"Label_09_Address_Line_01\":\"55 Block E\",\n"
                + "\"Label_09_Address_Line_02\":\"Ellesmere Street\",\n"
                + "\"Label_09_Address_Line_03\":\"Address Line 3\",\n"
                + "\"Label_09_Address_Line_04\":\"Manchester\",\n"
                + "\"Label_09_Address_Line_05\":\"Lancashire M5 KJR\",\n"
                + "\"Label_09_Telephone\":\"07577 136551\",\n"
                + "\"Label_09_Fax\":\"07577 136752\",\n"
                + "\"lbl_09_Eef\":\"\",\n"
                + "\"lbl_09_Cef\":\"1850055/2020\",\n"
                + "\"Label_10_Entity_Name_01\":\"Respondent three\",\n"
                + "\"Label_10_Entity_Name_02\":\"\",\n"
                + "\"Label_10_Address_Line_01\":\"55 Block E\",\n"
                + "\"Label_10_Address_Line_02\":\"Ellesmere Street\",\n"
                + "\"Label_10_Address_Line_03\":\"Address Line 3\",\n"
                + "\"Label_10_Address_Line_04\":\"Manchester\",\n"
                + "\"Label_10_Address_Line_05\":\"Lancashire M5 KJR\",\n"
                + "\"Label_10_Telephone\":\"07577 136551\",\n"
                + "\"Label_10_Fax\":\"07577 136752\",\n"
                + "\"lbl_10_Eef\":\"\",\n"
                + "\"lbl_10_Cef\":\"1850055/2020\",\n"
                + "\"Label_11_Entity_Name_01\":\"Respondent Rep\",\n"
                + "\"Label_11_Entity_Name_02\":\"\",\n"
                + "\"Label_11_Address_Line_01\":\"66 Block F\",\n"
                + "\"Label_11_Address_Line_02\":\"Ellesmere Street\",\n"
                + "\"Label_11_Address_Line_03\":\"Address Line 3\",\n"
                + "\"Label_11_Address_Line_04\":\"Manchester\",\n"
                + "\"Label_11_Address_Line_05\":\"Lancashire M6 KJR\",\n"
                + "\"Label_11_Telephone\":\"07577 136561\",\n"
                + "\"Label_11_Fax\":\"07577 136762\",\n"
                + "\"lbl_11_Eef\":\"OSCA/666/ABC\",\n"
                + "\"lbl_11_Cef\":\"1850066/2020\",\n"
                + "\"Label_12_Entity_Name_01\":\"Respondent Rep\",\n"
                + "\"Label_12_Entity_Name_02\":\"\",\n"
                + "\"Label_12_Address_Line_01\":\"66 Block F\",\n"
                + "\"Label_12_Address_Line_02\":\"Ellesmere Street\",\n"
                + "\"Label_12_Address_Line_03\":\"Address Line 3\",\n"
                + "\"Label_12_Address_Line_04\":\"Manchester\",\n"
                + "\"Label_12_Address_Line_05\":\"Lancashire M6 KJR\",\n"
                + "\"Label_12_Telephone\":\"07577 136561\",\n"
                + "\"Label_12_Fax\":\"07577 136762\",\n"
                + "\"lbl_12_Eef\":\"OSCA/666/ABC\",\n"
                + "\"lbl_12_Cef\":\"1850066/2020\"}],\n"
                + "\"i0_2_enhmcts\":\"[userImage:enhmcts.png]\",\n"
                + "\"i0_2_enhmcts1\":\"[userImage:enhmcts.png]\",\n"
                + "\"i0_2_enhmcts2\":\"[userImage:enhmcts.png]\",\n"
                + "\"iScot_schmcts\":\"[userImage:schmcts.png]\",\n"
                + "\"iScot_schmcts1\":\"[userImage:schmcts.png]\",\n"
                + "\"iScot_schmcts2\":\"[userImage:schmcts.png]\",\n"
                + "\"Clerk\":\"Mike Jordan\",\n"
                + "\"Today_date\":\"" + UtilHelper.formatCurrentDate(LocalDate.now()) + "\",\n"
                + "\"TodayPlus28Days\":\"" + UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 28) + "\",\n"
                + "\"Case_No\":\"123456\",\n"
                + "\"submission_reference\":\"12212121\",\n"
                + "}\n"
                + "}\n";
        assertEquals(expected, DocumentHelper.buildDocumentContent(caseDetails14.getCaseData(), "",
                userDetails,
                ENGLANDWALES_CASE_TYPE_ID,
                caseDetails14.getCaseData().getCorrespondenceType(),
                caseDetails14.getCaseData().getCorrespondenceScotType(),
                null, null, venueAddressReaderService).toString());
    }

    @Test
    void buildDocumentContent15() {
        String expected = "{\n"
                + "\"accessKey\":\"\",\n"
                + "\"templateName\":\"EM-TRB-LET-ENG-00544.docx\",\n"
                + "\"outputName\":\"document.docx\",\n"
                + "\"data\":{\n"
                + "\"address_labels_page\":[\n"
                + "{\"Label_13_Entity_Name_01\":\"Claimant Name\",\n"
                + "\"Label_13_Entity_Name_02\":\"\",\n"
                + "\"Label_13_Address_Line_01\":\"11 Block A\",\n"
                + "\"Label_13_Address_Line_02\":\"Ellesmere Street\",\n"
                + "\"Label_13_Address_Line_03\":\"Address Line 3\",\n"
                + "\"Label_13_Address_Line_04\":\"Manchester\",\n"
                + "\"Label_13_Address_Line_05\":\"Lancashire M1 KJR\",\n"
                + "\"Label_13_Telephone\":\"07577 136511\",\n"
                + "\"Label_13_Fax\":\"07577 136712\",\n"
                + "\"lbl_13_Eef\":\"\",\n"
                + "\"lbl_13_Cef\":\"1850011/2020\",\n"
                + "\"Label_14_Entity_Name_01\":\"Claimant Name\",\n"
                + "\"Label_14_Entity_Name_02\":\"\",\n"
                + "\"Label_14_Address_Line_01\":\"11 Block A\",\n"
                + "\"Label_14_Address_Line_02\":\"Ellesmere Street\",\n"
                + "\"Label_14_Address_Line_03\":\"Address Line 3\",\n"
                + "\"Label_14_Address_Line_04\":\"Manchester\",\n"
                + "\"Label_14_Address_Line_05\":\"Lancashire M1 KJR\",\n"
                + "\"Label_14_Telephone\":\"07577 136511\",\n"
                + "\"Label_14_Fax\":\"07577 136712\",\n"
                + "\"lbl_14_Eef\":\"\",\n"
                + "\"lbl_14_Cef\":\"1850011/2020\"},\n"
                + "{\"Label_01_Entity_Name_01\":\"Claimant Name\",\n"
                + "\"Label_01_Entity_Name_02\":\"\",\n"
                + "\"Label_01_Address_Line_01\":\"11 Block A\",\n"
                + "\"Label_01_Address_Line_02\":\"Ellesmere Street\",\n"
                + "\"Label_01_Address_Line_03\":\"Address Line 3\",\n"
                + "\"Label_01_Address_Line_04\":\"Manchester\",\n"
                + "\"Label_01_Address_Line_05\":\"Lancashire M1 KJR\",\n"
                + "\"Label_01_Telephone\":\"07577 136511\",\n"
                + "\"Label_01_Fax\":\"07577 136712\",\n"
                + "\"lbl_01_Eef\":\"\",\n"
                + "\"lbl_01_Cef\":\"1850011/2020\",\n"
                + "\"Label_02_Entity_Name_01\":\"Claimant Rep\",\n"
                + "\"Label_02_Entity_Name_02\":\"Claimant Org\",\n"
                + "\"Label_02_Address_Line_01\":\"22 Block B\",\n"
                + "\"Label_02_Address_Line_02\":\"Ellesmere Street\",\n"
                + "\"Label_02_Address_Line_03\":\"Address Line 3\",\n"
                + "\"Label_02_Address_Line_04\":\"Manchester\",\n"
                + "\"Label_02_Address_Line_05\":\"Lancashire M2 KJR\",\n"
                + "\"Label_02_Telephone\":\"07577 136521\",\n"
                + "\"Label_02_Fax\":\"07577 136722\",\n"
                + "\"lbl_02_Eef\":\"OSCA/222/ABC\",\n"
                + "\"lbl_02_Cef\":\"1850022/2020\",\n"
                + "\"Label_03_Entity_Name_01\":\"Claimant Rep\",\n"
                + "\"Label_03_Entity_Name_02\":\"Claimant Org\",\n"
                + "\"Label_03_Address_Line_01\":\"22 Block B\",\n"
                + "\"Label_03_Address_Line_02\":\"Ellesmere Street\",\n"
                + "\"Label_03_Address_Line_03\":\"Address Line 3\",\n"
                + "\"Label_03_Address_Line_04\":\"Manchester\",\n"
                + "\"Label_03_Address_Line_05\":\"Lancashire M2 KJR\",\n"
                + "\"Label_03_Telephone\":\"07577 136521\",\n"
                + "\"Label_03_Fax\":\"07577 136722\",\n"
                + "\"lbl_03_Eef\":\"OSCA/222/ABC\",\n"
                + "\"lbl_03_Cef\":\"1850022/2020\",\n"
                + "\"Label_04_Entity_Name_01\":\"Claimant Rep\",\n"
                + "\"Label_04_Entity_Name_02\":\"Claimant Org\",\n"
                + "\"Label_04_Address_Line_01\":\"22 Block B\",\n"
                + "\"Label_04_Address_Line_02\":\"Ellesmere Street\",\n"
                + "\"Label_04_Address_Line_03\":\"Address Line 3\",\n"
                + "\"Label_04_Address_Line_04\":\"Manchester\",\n"
                + "\"Label_04_Address_Line_05\":\"Lancashire M2 KJR\",\n"
                + "\"Label_04_Telephone\":\"07577 136521\",\n"
                + "\"Label_04_Fax\":\"07577 136722\",\n"
                + "\"lbl_04_Eef\":\"OSCA/222/ABC\",\n"
                + "\"lbl_04_Cef\":\"1850022/2020\",\n"
                + "\"Label_05_Entity_Name_01\":\"Respondent One\",\n"
                + "\"Label_05_Entity_Name_02\":\"\",\n"
                + "\"Label_05_Address_Line_01\":\"33 Block C\",\n"
                + "\"Label_05_Address_Line_02\":\"Ellesmere Street\",\n"
                + "\"Label_05_Address_Line_03\":\"Address Line 3\",\n"
                + "\"Label_05_Address_Line_04\":\"Manchester\",\n"
                + "\"Label_05_Address_Line_05\":\"Lancashire M3 KJR\",\n"
                + "\"Label_05_Telephone\":\"07577 136531\",\n"
                + "\"Label_05_Fax\":\"07577 136732\",\n"
                + "\"lbl_05_Eef\":\"\",\n"
                + "\"lbl_05_Cef\":\"1850033/2020\",\n"
                + "\"Label_06_Entity_Name_01\":\"Respondent One\",\n"
                + "\"Label_06_Entity_Name_02\":\"\",\n"
                + "\"Label_06_Address_Line_01\":\"33 Block C\",\n"
                + "\"Label_06_Address_Line_02\":\"Ellesmere Street\",\n"
                + "\"Label_06_Address_Line_03\":\"Address Line 3\",\n"
                + "\"Label_06_Address_Line_04\":\"Manchester\",\n"
                + "\"Label_06_Address_Line_05\":\"Lancashire M3 KJR\",\n"
                + "\"Label_06_Telephone\":\"07577 136531\",\n"
                + "\"Label_06_Fax\":\"07577 136732\",\n"
                + "\"lbl_06_Eef\":\"\",\n"
                + "\"lbl_06_Cef\":\"1850033/2020\",\n"
                + "\"Label_07_Entity_Name_01\":\"Respondent One\",\n"
                + "\"Label_07_Entity_Name_02\":\"\",\n"
                + "\"Label_07_Address_Line_01\":\"33 Block C\",\n"
                + "\"Label_07_Address_Line_02\":\"Ellesmere Street\",\n"
                + "\"Label_07_Address_Line_03\":\"Address Line 3\",\n"
                + "\"Label_07_Address_Line_04\":\"Manchester\",\n"
                + "\"Label_07_Address_Line_05\":\"Lancashire M3 KJR\",\n"
                + "\"Label_07_Telephone\":\"07577 136531\",\n"
                + "\"Label_07_Fax\":\"07577 136732\",\n"
                + "\"lbl_07_Eef\":\"\",\n"
                + "\"lbl_07_Cef\":\"1850033/2020\",\n"
                + "\"Label_08_Entity_Name_01\":\"Respondent Two\",\n"
                + "\"Label_08_Entity_Name_02\":\"\",\n"
                + "\"Label_08_Address_Line_01\":\"44 Block D\",\n"
                + "\"Label_08_Address_Line_02\":\"Ellesmere Street\",\n"
                + "\"Label_08_Address_Line_03\":\"Address Line 3\",\n"
                + "\"Label_08_Address_Line_04\":\"Manchester\",\n"
                + "\"Label_08_Address_Line_05\":\"Lancashire M4 KJR\",\n"
                + "\"Label_08_Telephone\":\"07577 136541\",\n"
                + "\"Label_08_Fax\":\"07577 136742\",\n"
                + "\"lbl_08_Eef\":\"\",\n"
                + "\"lbl_08_Cef\":\"1850044/2020\",\n"
                + "\"Label_09_Entity_Name_01\":\"Respondent Two\",\n"
                + "\"Label_09_Entity_Name_02\":\"\",\n"
                + "\"Label_09_Address_Line_01\":\"44 Block D\",\n"
                + "\"Label_09_Address_Line_02\":\"Ellesmere Street\",\n"
                + "\"Label_09_Address_Line_03\":\"Address Line 3\",\n"
                + "\"Label_09_Address_Line_04\":\"Manchester\",\n"
                + "\"Label_09_Address_Line_05\":\"Lancashire M4 KJR\",\n"
                + "\"Label_09_Telephone\":\"07577 136541\",\n"
                + "\"Label_09_Fax\":\"07577 136742\",\n"
                + "\"lbl_09_Eef\":\"\",\n"
                + "\"lbl_09_Cef\":\"1850044/2020\",\n"
                + "\"Label_10_Entity_Name_01\":\"Respondent Two\",\n"
                + "\"Label_10_Entity_Name_02\":\"\",\n"
                + "\"Label_10_Address_Line_01\":\"44 Block D\",\n"
                + "\"Label_10_Address_Line_02\":\"Ellesmere Street\",\n"
                + "\"Label_10_Address_Line_03\":\"Address Line 3\",\n"
                + "\"Label_10_Address_Line_04\":\"Manchester\",\n"
                + "\"Label_10_Address_Line_05\":\"Lancashire M4 KJR\",\n"
                + "\"Label_10_Telephone\":\"07577 136541\",\n"
                + "\"Label_10_Fax\":\"07577 136742\",\n"
                + "\"lbl_10_Eef\":\"\",\n"
                + "\"lbl_10_Cef\":\"1850044/2020\",\n"
                + "\"Label_11_Entity_Name_01\":\"Respondent three\",\n"
                + "\"Label_11_Entity_Name_02\":\"\",\n"
                + "\"Label_11_Address_Line_01\":\"55 Block E\",\n"
                + "\"Label_11_Address_Line_02\":\"Ellesmere Street\",\n"
                + "\"Label_11_Address_Line_03\":\"Address Line 3\",\n"
                + "\"Label_11_Address_Line_04\":\"Manchester\",\n"
                + "\"Label_11_Address_Line_05\":\"Lancashire M5 KJR\",\n"
                + "\"Label_11_Telephone\":\"07577 136551\",\n"
                + "\"Label_11_Fax\":\"07577 136752\",\n"
                + "\"lbl_11_Eef\":\"\",\n"
                + "\"lbl_11_Cef\":\"1850055/2020\",\n"
                + "\"Label_12_Entity_Name_01\":\"Respondent three\",\n"
                + "\"Label_12_Entity_Name_02\":\"\",\n"
                + "\"Label_12_Address_Line_01\":\"55 Block E\",\n"
                + "\"Label_12_Address_Line_02\":\"Ellesmere Street\",\n"
                + "\"Label_12_Address_Line_03\":\"Address Line 3\",\n"
                + "\"Label_12_Address_Line_04\":\"Manchester\",\n"
                + "\"Label_12_Address_Line_05\":\"Lancashire M5 KJR\",\n"
                + "\"Label_12_Telephone\":\"07577 136551\",\n"
                + "\"Label_12_Fax\":\"07577 136752\",\n"
                + "\"lbl_12_Eef\":\"\",\n"
                + "\"lbl_12_Cef\":\"1850055/2020\",\n"
                + "\"Label_13_Entity_Name_01\":\"Respondent three\",\n"
                + "\"Label_13_Entity_Name_02\":\"\",\n"
                + "\"Label_13_Address_Line_01\":\"55 Block E\",\n"
                + "\"Label_13_Address_Line_02\":\"Ellesmere Street\",\n"
                + "\"Label_13_Address_Line_03\":\"Address Line 3\",\n"
                + "\"Label_13_Address_Line_04\":\"Manchester\",\n"
                + "\"Label_13_Address_Line_05\":\"Lancashire M5 KJR\",\n"
                + "\"Label_13_Telephone\":\"07577 136551\",\n"
                + "\"Label_13_Fax\":\"07577 136752\",\n"
                + "\"lbl_13_Eef\":\"\",\n"
                + "\"lbl_13_Cef\":\"1850055/2020\",\n"
                + "\"Label_14_Entity_Name_01\":\"Respondent Rep\",\n"
                + "\"Label_14_Entity_Name_02\":\"\",\n"
                + "\"Label_14_Address_Line_01\":\"66 Block F\",\n"
                + "\"Label_14_Address_Line_02\":\"Ellesmere Street\",\n"
                + "\"Label_14_Address_Line_03\":\"Address Line 3\",\n"
                + "\"Label_14_Address_Line_04\":\"Manchester\",\n"
                + "\"Label_14_Address_Line_05\":\"Lancashire M6 KJR\",\n"
                + "\"Label_14_Telephone\":\"07577 136561\",\n"
                + "\"Label_14_Fax\":\"07577 136762\",\n"
                + "\"lbl_14_Eef\":\"OSCA/666/ABC\",\n"
                + "\"lbl_14_Cef\":\"1850066/2020\"},\n"
                + "{\"Label_01_Entity_Name_01\":\"Respondent Rep\",\n"
                + "\"Label_01_Entity_Name_02\":\"\",\n"
                + "\"Label_01_Address_Line_01\":\"66 Block F\",\n"
                + "\"Label_01_Address_Line_02\":\"Ellesmere Street\",\n"
                + "\"Label_01_Address_Line_03\":\"Address Line 3\",\n"
                + "\"Label_01_Address_Line_04\":\"Manchester\",\n"
                + "\"Label_01_Address_Line_05\":\"Lancashire M6 KJR\",\n"
                + "\"Label_01_Telephone\":\"07577 136561\",\n"
                + "\"Label_01_Fax\":\"07577 136762\",\n"
                + "\"lbl_01_Eef\":\"OSCA/666/ABC\",\n"
                + "\"lbl_01_Cef\":\"1850066/2020\",\n"
                + "\"Label_02_Entity_Name_01\":\"Respondent Rep\",\n"
                + "\"Label_02_Entity_Name_02\":\"\",\n"
                + "\"Label_02_Address_Line_01\":\"66 Block F\",\n"
                + "\"Label_02_Address_Line_02\":\"Ellesmere Street\",\n"
                + "\"Label_02_Address_Line_03\":\"Address Line 3\",\n"
                + "\"Label_02_Address_Line_04\":\"Manchester\",\n"
                + "\"Label_02_Address_Line_05\":\"Lancashire M6 KJR\",\n"
                + "\"Label_02_Telephone\":\"07577 136561\",\n"
                + "\"Label_02_Fax\":\"07577 136762\",\n"
                + "\"lbl_02_Eef\":\"OSCA/666/ABC\",\n"
                + "\"lbl_02_Cef\":\"1850066/2020\"}],\n"
                + "\"i0_2_enhmcts\":\"[userImage:enhmcts.png]\",\n"
                + "\"i0_2_enhmcts1\":\"[userImage:enhmcts.png]\",\n"
                + "\"i0_2_enhmcts2\":\"[userImage:enhmcts.png]\",\n"
                + "\"iScot_schmcts\":\"[userImage:schmcts.png]\",\n"
                + "\"iScot_schmcts1\":\"[userImage:schmcts.png]\",\n"
                + "\"iScot_schmcts2\":\"[userImage:schmcts.png]\",\n"
                + "\"Clerk\":\"Mike Jordan\",\n"
                + "\"Today_date\":\"" + UtilHelper.formatCurrentDate(LocalDate.now()) + "\",\n"
                + "\"TodayPlus28Days\":\"" + UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 28) + "\",\n"
                + "\"Case_No\":\"123456\",\n"
                + "\"submission_reference\":\"12212121\",\n"
                + "}\n"
                + "}\n";
        assertEquals(expected, DocumentHelper.buildDocumentContent(caseDetails15.getCaseData(), "",
                userDetails, ENGLANDWALES_CASE_TYPE_ID,
                caseDetails15.getCaseData().getCorrespondenceType(),
                caseDetails15.getCaseData().getCorrespondenceScotType(),
                null, null, venueAddressReaderService).toString());
    }

    @Test
    void buildDocumentContent20() {
        String expected = "{\n"
                + "\"accessKey\":\"\",\n"
                + "\"templateName\":\"EM-TRB-EGW-ENG-00043.docx\",\n"
                + "\"outputName\":\"document.docx\",\n"
                + "\"data\":{\n"
                + "\"claimant_or_rep_full_name\":\"RepresentativeNameClaimant\",\n"
                + "\"claimant_rep_organisation\":\"RepresentativeOrganisation\",\n"
                + "\"claimant_or_rep_addressLine1\":\"56 Block C\",\n"
                + "\"claimant_or_rep_addressLine2\":\"Ellesmere Street\",\n"
                + "\"claimant_or_rep_addressLine3\":\"\",\n"
                + "\"claimant_or_rep_town\":\"Manchester\",\n"
                + "\"claimant_or_rep_county\":\"Lancashire\",\n"
                + "\"claimant_or_rep_postCode\":\"M3 KJR\",\n"
                + "\"claimant_reference\":\"1111111\",\n"
                + "\"claimant_full_name\":\"Mr A J Rodriguez\",\n"
                + "\"Claimant\":\"Mr A J Rodriguez\",\n"
                + "\"claimant_addressLine1\":\"34\",\n"
                + "\"claimant_addressLine2\":\"Low Street\",\n"
                + "\"claimant_addressLine3\":\"\",\n"
                + "\"claimant_town\":\"Manchester\",\n"
                + "\"claimant_county\":\"Lancashire\",\n"
                + "\"claimant_postCode\":\"M3 6gw\",\n"
                + "\"respondent_or_rep_full_name\":\"Antonio Vazquez\",\n"
                + "\"respondent_or_rep_addressLine1\":\"11 Small Street\",\n"
                + "\"respondent_or_rep_addressLine2\":\"22 House\",\n"
                + "\"respondent_or_rep_addressLine3\":\"\",\n"
                + "\"respondent_or_rep_town\":\"Manchester\",\n"
                + "\"respondent_or_rep_county\":\"North West\",\n"
                + "\"respondent_or_rep_postCode\":\"M12 42R\",\n"
                + "\"respondent_full_name\":\"Antonio Vazquez\",\n"
                + "\"respondent_addressLine1\":\"11 Small Street\",\n"
                + "\"respondent_addressLine2\":\"22 House\",\n"
                + "\"respondent_addressLine3\":\"\",\n"
                + "\"respondent_town\":\"Manchester\",\n"
                + "\"respondent_county\":\"North West\",\n"
                + "\"respondent_postCode\":\"M12 42R\",\n"
                + "\"Respondent\":\"1. Antonio Vazquez,\",\n"
                + "\"resp_others\":\"2. Juan Garcia, 3. Mike Jordan\",\n"
                + "\"resp_address\":\"1. 11 Small Street, 22 House, Manchester, North West, M12 42R, "
                + "UK\\n2. 12 Small Street, 24 House, Manchester, North West, M12 4ED, UK\\n3. 11 Small Street, "
                + "22 House, Manchester, North West, M12 42R, UK\",\n"
                + "\"Hearing_date\":\"\",\n"
                + "\"Hearing_date_time\":\"\",\n"
                + "\"Hearing_time\":\"\",\n"
                + "\"Hearing_venue\":\"Manchester Employment Tribunals, "
                + "Alexandra House, 14-22 The Parsonage, Manchester, M3 2JA\",\n"
                + "\"Hearing_duration\":\"3 days\",\n"
                + "\"t1_2\":\"true\",\n"
                + "\"Court_addressLine1\":\"Manchester Employment Tribunal,\",\n"
                + "\"Court_addressLine2\":\"Alexandra House,\",\n"
                + "\"Court_addressLine3\":\"14-22 The Parsonage,\",\n"
                + "\"Court_town\":\"Manchester,\",\n"
                + "\"Court_county\":\"\",\n"
                + "\"Court_postCode\":\"M3 2JA\",\n"
                + "\"Court_telephone\":\"03577131270\",\n"
                + "\"Court_fax\":\"07577126570\",\n"
                + "\"Court_DX\":\"123456\",\n"
                + "\"Court_Email\":\"ManchesterOfficeET@hmcts.gov.uk\",\n"
                + "\"i1_2_enhmcts\":\"[userImage:enhmcts.png]\",\n"
                + "\"i1_2_enhmcts1\":\"[userImage:enhmcts.png]\",\n"
                + "\"i1_2_enhmcts2\":\"[userImage:enhmcts.png]\",\n"
                + "\"iScot_schmcts\":\"[userImage:schmcts.png]\",\n"
                + "\"iScot_schmcts1\":\"[userImage:schmcts.png]\",\n"
                + "\"iScot_schmcts2\":\"[userImage:schmcts.png]\",\n"
                + "\"Clerk\":\"Mike Jordan\",\n"
                + "\"Today_date\":\"" + UtilHelper.formatCurrentDate(LocalDate.now()) + "\",\n"
                + "\"TodayPlus28Days\":\"" + UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 28) + "\",\n"
                + "\"Case_No\":\"123456\",\n"
                + "\"submission_reference\":\"12212121\",\n"
                + "}\n"
                + "}\n";

        assertEquals(expected, DocumentHelper.buildDocumentContent(caseDetails20.getCaseData(), "",
                userDetails, ENGLANDWALES_CASE_TYPE_ID,
                caseDetails20.getCaseData().getCorrespondenceType(),
                caseDetails20.getCaseData().getCorrespondenceScotType(),
                null, null, venueAddressReaderService).toString());
    }

    @Test
    void buildDocumentWithNotContent() {
        String expected = "{\n"
                + "\"accessKey\":\"\",\n"
                + "\"templateName\":\".docx\",\n"
                + "\"outputName\":\"document.docx\",\n"
                + "\"data\":{\n"
                + "\"claimant_or_rep_full_name\":\"\",\n"
                + "\"claimant_full_name\":\"\",\n"
                + "\"Claimant\":\"\",\n"
                + "\"claimant_rep_organisation\":\"\",\n"
                + "\"claimant_or_rep_addressLine1\":\"\",\n"
                + "\"claimant_or_rep_addressLine2\":\"\",\n"
                + "\"claimant_or_rep_addressLine3\":\"\",\n"
                + "\"claimant_or_rep_town\":\"\",\n"
                + "\"claimant_or_rep_county\":\"\",\n"
                + "\"claimant_or_rep_postCode\":\"\",\n"
                + "\"claimant_addressLine1\":\"\",\n"
                + "\"claimant_addressLine2\":\"\",\n"
                + "\"claimant_addressLine3\":\"\",\n"
                + "\"claimant_town\":\"\",\n"
                + "\"claimant_county\":\"\",\n"
                + "\"claimant_postCode\":\"\",\n"
                + "\"respondent_or_rep_full_name\":\"\",\n"
                + "\"respondent_rep_organisation\":\"\",\n"
                + "\"respondent_or_rep_addressLine1\":\"\",\n"
                + "\"respondent_or_rep_addressLine2\":\"\",\n"
                + "\"respondent_or_rep_addressLine3\":\"\",\n"
                + "\"respondent_or_rep_town\":\"\",\n"
                + "\"respondent_or_rep_county\":\"\",\n"
                + "\"respondent_or_rep_postCode\":\"\",\n"
                + "\"respondent_full_name\":\"\",\n"
                + "\"respondent_addressLine1\":\"\",\n"
                + "\"respondent_addressLine2\":\"\",\n"
                + "\"respondent_addressLine3\":\"\",\n"
                + "\"respondent_town\":\"\",\n"
                + "\"respondent_county\":\"\",\n"
                + "\"respondent_postCode\":\"\",\n"
                + "\"Respondent\":\"\",\n"
                + "\"resp_others\":\"\",\n"
                + "\"resp_address\":\"\",\n"
                + "\"Hearing_date\":\"\",\n"
                + "\"Hearing_date_time\":\"\",\n"
                + "\"Hearing_venue\":\"\",\n"
                + "\"Hearing_duration\":\"\",\n"
                + "\"Hearing_time\":\"\",\n"
                + "\"Court_telephone\":\"\",\n"
                + "\"Court_fax\":\"\",\n"
                + "\"Court_DX\":\"\",\n"
                + "\"Court_Email\":\"\",\n"
                + "\"i_enhmcts\":\"[userImage:enhmcts.png]\",\n"
                + "\"i_enhmcts1\":\"[userImage:enhmcts.png]\",\n"
                + "\"i_enhmcts2\":\"[userImage:enhmcts.png]\",\n"
                + "\"iScot_schmcts\":\"[userImage:schmcts.png]\",\n"
                + "\"iScot_schmcts1\":\"[userImage:schmcts.png]\",\n"
                + "\"iScot_schmcts2\":\"[userImage:schmcts.png]\",\n"
                + "\"Clerk\":\"Mike Jordan\",\n"
                + "\"Today_date\":\"" + UtilHelper.formatCurrentDate(LocalDate.now()) + "\",\n"
                + "\"TodayPlus28Days\":\"" + UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 28) + "\",\n"
                + "\"Case_No\":\"\",\n"
                + "\"submission_reference\":\"\",\n"
                + "}\n"
                + "}\n";
        assertEquals(expected, DocumentHelper.buildDocumentContent(caseDetailsEmpty.getCaseData(), "",
                userDetails, "",
                caseDetailsEmpty.getCaseData().getCorrespondenceType(),
                caseDetailsEmpty.getCaseData().getCorrespondenceScotType(),
                null, null, venueAddressReaderService).toString());
    }

    @Test
    void buildDocumentContentScot1() {
        when(venueAddressReaderService.getVenueAddress(any(), any(), any())).thenReturn(GLASGOW_VENUE_ADDRESS);

        String expected = "{\n"
                + "\"accessKey\":\"\",\n"
                + "\"templateName\":\"EM-TRB-SCO-ENG-00042.docx\",\n"
                + "\"outputName\":\"document.docx\",\n"
                + "\"data\":{\n"
                + "\"claimant_or_rep_full_name\":\"RepresentativeNameClaimant\",\n"
                + "\"claimant_rep_organisation\":\"RepresentativeOrganisation\",\n"
                + "\"claimant_or_rep_addressLine1\":\"56 Block C\",\n"
                + "\"claimant_or_rep_addressLine2\":\"Ellesmere Street\",\n"
                + "\"claimant_or_rep_addressLine3\":\"\",\n"
                + "\"claimant_or_rep_town\":\"Manchester\",\n"
                + "\"claimant_or_rep_county\":\"Lancashire\",\n"
                + "\"claimant_or_rep_postCode\":\"M3 KJR\",\n"
                + "\"claimant_reference\":\"1111111\",\n"
                + "\"claimant_full_name\":\"Mr A J Rodriguez\",\n"
                + "\"Claimant\":\"Mr A J Rodriguez\",\n"
                + "\"claimant_addressLine1\":\"34\",\n"
                + "\"claimant_addressLine2\":\"Low Street\",\n"
                + "\"claimant_addressLine3\":\"\",\n"
                + "\"claimant_town\":\"Manchester\",\n"
                + "\"claimant_county\":\"Lancashire\",\n"
                + "\"claimant_postCode\":\"M3 6gw\",\n"
                + "\"respondent_or_rep_full_name\":\"Antonio Vazquez\",\n"
                + "\"respondent_or_rep_addressLine1\":\"11 Small Street\",\n"
                + "\"respondent_or_rep_addressLine2\":\"22 House\",\n"
                + "\"respondent_or_rep_addressLine3\":\"\",\n"
                + "\"respondent_or_rep_town\":\"Manchester\",\n"
                + "\"respondent_or_rep_county\":\"North West\",\n"
                + "\"respondent_or_rep_postCode\":\"M12 42R\",\n"
                + "\"respondent_full_name\":\"Antonio Vazquez\",\n"
                + "\"respondent_addressLine1\":\"11 Small Street\",\n"
                + "\"respondent_addressLine2\":\"22 House\",\n"
                + "\"respondent_addressLine3\":\"\",\n"
                + "\"respondent_town\":\"Manchester\",\n"
                + "\"respondent_county\":\"North West\",\n"
                + "\"respondent_postCode\":\"M12 42R\",\n"
                + "\"Respondent\":\"1. Antonio Vazquez,\",\n"
                + "\"resp_others\":\"2. Roberto Dondini\",\n"
                + "\"resp_address\":\"1. 11 Small Street, 22 House, Manchester, North West, M12 42R, UK\\n2. 13 Small"
                + " Street, 26 House, Scotland, North West, SC13 4ED, UK\",\n"
                + "\"Hearing_date\":\"\",\n"
                + "\"Hearing_date_time\":\"\",\n"
                + "\"Hearing_time\":\"\",\n"
                + "\"Hearing_venue\":\"Glasgow Tribunal Centre, Atlantic Quay, 20 York Street, Glasgow, G2 8GT\",\n"
                + "\"Hearing_duration\":\"2 days\",\n"
                + "\"t_Scot_7_1\":\"true\",\n"
                + "\"Court_addressLine1\":\"Eagle Building,\",\n"
                + "\"Court_addressLine2\":\"215 Bothwell Street,\",\n"
                + "\"Court_addressLine3\":\"\",\n"
                + "\"Court_town\":\"Glasgow,\",\n"
                + "\"Court_county\":\"\",\n"
                + "\"Court_postCode\":\"G2 7TS\",\n"
                + "\"Court_telephone\":\"03577123270\",\n"
                + "\"Court_fax\":\"07127126570\",\n"
                + "\"Court_DX\":\"1234567\",\n"
                + "\"Court_Email\":\"GlasgowOfficeET@hmcts.gov.uk\",\n"
                + "\"i_enhmcts\":\"[userImage:enhmcts.png]\",\n"
                + "\"i_enhmcts1\":\"[userImage:enhmcts.png]\",\n"
                + "\"i_enhmcts2\":\"[userImage:enhmcts.png]\",\n"
                + "\"iScot7_1_schmcts\":\"[userImage:schmcts.png]\",\n"
                + "\"iScot7_1_schmcts1\":\"[userImage:schmcts.png]\",\n"
                + "\"iScot7_1_schmcts2\":\"[userImage:schmcts.png]\",\n"
                + "\"Clerk\":\"Mike Jordan\",\n"
                + "\"Today_date\":\"" + UtilHelper.formatCurrentDate(LocalDate.now()) + "\",\n"
                + "\"TodayPlus28Days\":\"" + UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 28) + "\",\n"
                + "\"Case_No\":\"123456\",\n"
                + "\"submission_reference\":\"12212121\",\n"
                + "}\n"
                + "}\n";
        assertEquals(expected, DocumentHelper.buildDocumentContent(caseDetailsScot1.getCaseData(), "",
                userDetails, SCOTLAND_CASE_TYPE_ID,
                caseDetailsScot1.getCaseData().getCorrespondenceType(),
                caseDetailsScot1.getCaseData().getCorrespondenceScotType(),
                null, null, venueAddressReaderService).toString());
    }

    @Test
    void buildDocumentContentScot2() {
        String expected = "{\n"
                + "\"accessKey\":\"\",\n"
                + "\"templateName\":\"EM-TRB-SCO-ENG-00043.docx\",\n"
                + "\"outputName\":\"document.docx\",\n"
                + "\"data\":{\n"
                + "\"claimant_or_rep_full_name\":\"Orlando LTD\",\n"
                + "\"claimant_full_name\":\"Orlando LTD\",\n"
                + "\"Claimant\":\"Orlando LTD\",\n"
                + "\"claimant_or_rep_addressLine1\":\"34\",\n"
                + "\"claimant_or_rep_addressLine2\":\"Low Street\",\n"
                + "\"claimant_or_rep_addressLine3\":\"\",\n"
                + "\"claimant_or_rep_town\":\"Manchester\",\n"
                + "\"claimant_or_rep_county\":\"Lancashire\",\n"
                + "\"claimant_or_rep_postCode\":\"M3 6gw\",\n"
                + "\"claimant_addressLine1\":\"34\",\n"
                + "\"claimant_addressLine2\":\"Low Street\",\n"
                + "\"claimant_addressLine3\":\"\",\n"
                + "\"claimant_town\":\"Manchester\",\n"
                + "\"claimant_county\":\"Lancashire\",\n"
                + "\"claimant_postCode\":\"M3 6gw\",\n"
                + "\"respondent_or_rep_full_name\":\"\",\n"
                + "\"respondent_rep_organisation\":\"\",\n"
                + "\"respondent_or_rep_addressLine1\":\"\",\n"
                + "\"respondent_or_rep_addressLine2\":\"\",\n"
                + "\"respondent_or_rep_addressLine3\":\"\",\n"
                + "\"respondent_or_rep_town\":\"\",\n"
                + "\"respondent_or_rep_county\":\"\",\n"
                + "\"respondent_or_rep_postCode\":\"\",\n"
                + "\"respondent_full_name\":\"\",\n"
                + "\"respondent_addressLine1\":\"\",\n"
                + "\"respondent_addressLine2\":\"\",\n"
                + "\"respondent_addressLine3\":\"\",\n"
                + "\"respondent_town\":\"\",\n"
                + "\"respondent_county\":\"\",\n"
                + "\"respondent_postCode\":\"\",\n"
                + "\"Respondent\":\"\",\n"
                + "\"resp_others\":\"\",\n"
                + "\"resp_address\":\"\",\n"
                + "\"Hearing_date\":\"25 November 2019\",\n"
                + "\"Hearing_date_time\":\"25 November 2019 at 12:11\",\n"
                + "\"Hearing_time\":\"12:11\",\n"
                + "\"Hearing_venue\":\"Glasgow Tribunal Centre, Atlantic Quay, 20 York Street, Glasgow, G2 8GT\",\n"
                + "\"Hearing_duration\":\"2 days\",\n"
                + "\"t_Scot_24\":\"true\",\n"
                + "\"Court_addressLine1\":\"Eagle Building,\",\n"
                + "\"Court_addressLine2\":\"215 Bothwell Street,\",\n"
                + "\"Court_addressLine3\":\"\",\n"
                + "\"Court_town\":\"Glasgow,\",\n"
                + "\"Court_county\":\"\",\n"
                + "\"Court_postCode\":\"G2 7TS\",\n"
                + "\"Court_telephone\":\"03577123270\",\n"
                + "\"Court_fax\":\"07127126570\",\n"
                + "\"Court_DX\":\"1234567\",\n"
                + "\"Court_Email\":\"GlasgowOfficeET@hmcts.gov.uk\",\n"
                + "\"i_enhmcts\":\"[userImage:enhmcts.png]\",\n"
                + "\"i_enhmcts1\":\"[userImage:enhmcts.png]\",\n"
                + "\"i_enhmcts2\":\"[userImage:enhmcts.png]\",\n"
                + "\"iScot24_schmcts\":\"[userImage:schmcts.png]\",\n"
                + "\"iScot24_schmcts1\":\"[userImage:schmcts.png]\",\n"
                + "\"iScot24_schmcts2\":\"[userImage:schmcts.png]\",\n"
                + "\"Clerk\":\"Mike Jordan\",\n"
                + "\"Today_date\":\"" + UtilHelper.formatCurrentDate(LocalDate.now()) + "\",\n"
                + "\"TodayPlus28Days\":\"" + UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 28) + "\",\n"
                + "\"Case_No\":\"123456\",\n"
                + "\"submission_reference\":\"12212121\",\n"
                + "}\n"
                + "}\n";
        when(venueAddressReaderService.getVenueAddress(any(), any(), any())).thenReturn(GLASGOW_VENUE_ADDRESS);
        assertEquals(expected, DocumentHelper.buildDocumentContent(caseDetailsScot2.getCaseData(), "",
                userDetails, DUMMY_CASE_TYPE_ID,
                caseDetailsScot2.getCaseData().getCorrespondenceType(),
                caseDetailsScot2.getCaseData().getCorrespondenceScotType(),
                null, null, venueAddressReaderService).toString());
    }

    @Test
    void buildDocumentContentScot3() {
        String expected = "{\n"
                + "\"accessKey\":\"\",\n"
                + "\"templateName\":\"EM-TRB-SCO-ENG-00044.docx\",\n"
                + "\"outputName\":\"document.docx\",\n"
                + "\"data\":{\n"
                + "\"claimant_or_rep_full_name\":\"Mr A J Rodriguez\",\n"
                + "\"claimant_full_name\":\"Mr A J Rodriguez\",\n"
                + "\"Claimant\":\"Mr A J Rodriguez\",\n"
                + "\"claimant_or_rep_addressLine1\":\"34\",\n"
                + "\"claimant_or_rep_addressLine2\":\"Low Street\",\n"
                + "\"claimant_or_rep_addressLine3\":\"\",\n"
                + "\"claimant_or_rep_town\":\"Manchester\",\n"
                + "\"claimant_or_rep_county\":\"Lancashire\",\n"
                + "\"claimant_or_rep_postCode\":\"M3 6gw\",\n"
                + "\"claimant_addressLine1\":\"34\",\n"
                + "\"claimant_addressLine2\":\"Low Street\",\n"
                + "\"claimant_addressLine3\":\"\",\n"
                + "\"claimant_town\":\"Manchester\",\n"
                + "\"claimant_county\":\"Lancashire\",\n"
                + "\"claimant_postCode\":\"M3 6gw\",\n"
                + "\"respondent_or_rep_full_name\":\"Antonio Vazquez\",\n"
                + "\"respondent_or_rep_addressLine1\":\"11 Small Street\",\n"
                + "\"respondent_or_rep_addressLine2\":\"22 House\",\n"
                + "\"respondent_or_rep_addressLine3\":\"\",\n"
                + "\"respondent_or_rep_town\":\"Manchester\",\n"
                + "\"respondent_or_rep_county\":\"North West\",\n"
                + "\"respondent_or_rep_postCode\":\"M12 42R\",\n"
                + "\"respondent_full_name\":\"Antonio Vazquez\",\n"
                + "\"respondent_addressLine1\":\"11 Small Street\",\n"
                + "\"respondent_addressLine2\":\"22 House\",\n"
                + "\"respondent_addressLine3\":\"\",\n"
                + "\"respondent_town\":\"Manchester\",\n"
                + "\"respondent_county\":\"North West\",\n"
                + "\"respondent_postCode\":\"M12 42R\",\n"
                + "\"Respondent\":\"Antonio Vazquez\",\n"
                + "\"resp_others\":\"\",\n"
                + "\"resp_address\":\"11 Small Street, 22 House, Manchester, North West, M12 42R, UK\",\n"
                + "\"Hearing_date\":\"1 November 2019\",\n"
                + "\"Hearing_date_time\":\"1 November 2019 at 12:11\",\n"
                + "\"Hearing_time\":\"12:11\",\n"
                + "\"Hearing_venue\":\"Ground Floor, AB1, 48 Huntly Street, Aberdeen, AB10 1SH\",\n"
                + "\"Hearing_duration\":\"1 day\",\n"
                + "\"t_Scot_34\":\"true\",\n"
                + "\"Court_addressLine1\":\"Eagle Building,\",\n"
                + "\"Court_addressLine2\":\"215 Bothwell Street,\",\n"
                + "\"Court_addressLine3\":\"\",\n"
                + "\"Court_town\":\"Glasgow,\",\n"
                + "\"Court_county\":\"\",\n"
                + "\"Court_postCode\":\"G2 7TS\",\n"
                + "\"Court_telephone\":\"03577123270\",\n"
                + "\"Court_fax\":\"07127126570\",\n"
                + "\"Court_DX\":\"1234567\",\n"
                + "\"Court_Email\":\"GlasgowOfficeET@hmcts.gov.uk\",\n"
                + "\"i_enhmcts\":\"[userImage:enhmcts.png]\",\n"
                + "\"i_enhmcts1\":\"[userImage:enhmcts.png]\",\n"
                + "\"i_enhmcts2\":\"[userImage:enhmcts.png]\",\n"
                + "\"iScot34_schmcts\":\"[userImage:schmcts.png]\",\n"
                + "\"iScot34_schmcts1\":\"[userImage:schmcts.png]\",\n"
                + "\"iScot34_schmcts2\":\"[userImage:schmcts.png]\",\n"
                + "\"Clerk\":\"Mike Jordan\",\n"
                + "\"Today_date\":\"" + UtilHelper.formatCurrentDate(LocalDate.now()) + "\",\n"
                + "\"TodayPlus28Days\":\"" + UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 28) + "\",\n"
                + "\"Case_No\":\"123456\",\n"
                + "\"submission_reference\":\"12212121\",\n"
                + "}\n"
                + "}\n";
        when(venueAddressReaderService.getVenueAddress(any(), any(), any())).thenReturn(ABERDEEN_VENUE_ADDRESS);
        assertEquals(expected, DocumentHelper.buildDocumentContent(caseDetailsScot3.getCaseData(), "",
                userDetails, SCOTLAND_CASE_TYPE_ID,
                caseDetailsScot3.getCaseData().getCorrespondenceType(),
                caseDetailsScot3.getCaseData().getCorrespondenceScotType(),
                null, null, venueAddressReaderService).toString());
    }

    @Test
    void buildDocumentContentScot3AllocatedOffice() {
        when(venueAddressReaderService.getVenueAddress(any(), any(), any())).thenReturn(ABERDEEN_VENUE_ADDRESS);
        String expected = "{\n"
                + "\"accessKey\":\"\",\n"
                + "\"templateName\":\"EM-TRB-SCO-ENG-00044.docx\",\n"
                + "\"outputName\":\"document.docx\",\n"
                + "\"data\":{\n"
                + "\"claimant_or_rep_full_name\":\"Mr A J Rodriguez\",\n"
                + "\"claimant_full_name\":\"Mr A J Rodriguez\",\n"
                + "\"Claimant\":\"Mr A J Rodriguez\",\n"
                + "\"claimant_or_rep_addressLine1\":\"34\",\n"
                + "\"claimant_or_rep_addressLine2\":\"Low Street\",\n"
                + "\"claimant_or_rep_addressLine3\":\"\",\n"
                + "\"claimant_or_rep_town\":\"Manchester\",\n"
                + "\"claimant_or_rep_county\":\"Lancashire\",\n"
                + "\"claimant_or_rep_postCode\":\"M3 6gw\",\n"
                + "\"claimant_addressLine1\":\"34\",\n"
                + "\"claimant_addressLine2\":\"Low Street\",\n"
                + "\"claimant_addressLine3\":\"\",\n"
                + "\"claimant_town\":\"Manchester\",\n"
                + "\"claimant_county\":\"Lancashire\",\n"
                + "\"claimant_postCode\":\"M3 6gw\",\n"
                + "\"respondent_or_rep_full_name\":\"Antonio Vazquez\",\n"
                + "\"respondent_or_rep_addressLine1\":\"11 Small Street\",\n"
                + "\"respondent_or_rep_addressLine2\":\"22 House\",\n"
                + "\"respondent_or_rep_addressLine3\":\"\",\n"
                + "\"respondent_or_rep_town\":\"Manchester\",\n"
                + "\"respondent_or_rep_county\":\"North West\",\n"
                + "\"respondent_or_rep_postCode\":\"M12 42R\",\n"
                + "\"respondent_full_name\":\"Antonio Vazquez\",\n"
                + "\"respondent_addressLine1\":\"11 Small Street\",\n"
                + "\"respondent_addressLine2\":\"22 House\",\n"
                + "\"respondent_addressLine3\":\"\",\n"
                + "\"respondent_town\":\"Manchester\",\n"
                + "\"respondent_county\":\"North West\",\n"
                + "\"respondent_postCode\":\"M12 42R\",\n"
                + "\"Respondent\":\"Antonio Vazquez\",\n"
                + "\"resp_others\":\"\",\n"
                + "\"resp_address\":\"11 Small Street, 22 House, Manchester, North West, M12 42R, UK\",\n"
                + "\"Hearing_date\":\"1 November 2019\",\n"
                + "\"Hearing_date_time\":\"1 November 2019 at 12:11\",\n"
                + "\"Hearing_time\":\"12:11\",\n"
                + "\"Hearing_venue\":\"Ground Floor, AB1, 48 Huntly Street, Aberdeen, AB10 1SH\",\n"
                + "\"Hearing_duration\":\"1 day\",\n"
                + "\"t_Scot_34\":\"true\",\n"
                + "\"Court_addressLine1\":\"Aberdeen Address Line1\",\n"
                + "\"Court_addressLine2\":\"Aberdeen Address Line2\",\n"
                + "\"Court_addressLine3\":\"\",\n"
                + "\"Court_town\":\"Aberdeen\",\n"
                + "\"Court_county\":\"\",\n"
                + "\"Court_postCode\":\"BA 3453\",\n"
                + "\"Court_telephone\":\"\",\n"
                + "\"Court_fax\":\"\",\n"
                + "\"Court_DX\":\"\",\n"
                + "\"Court_Email\":\"aberdeen@gmail.com\",\n"
                + "\"i_enhmcts\":\"[userImage:enhmcts.png]\",\n"
                + "\"i_enhmcts1\":\"[userImage:enhmcts.png]\",\n"
                + "\"i_enhmcts2\":\"[userImage:enhmcts.png]\",\n"
                + "\"iScot34_schmcts\":\"[userImage:schmcts.png]\",\n"
                + "\"iScot34_schmcts1\":\"[userImage:schmcts.png]\",\n"
                + "\"iScot34_schmcts2\":\"[userImage:schmcts.png]\",\n"
                + "\"Clerk\":\"Mike Jordan\",\n"
                + "\"Today_date\":\"" + UtilHelper.formatCurrentDate(LocalDate.now()) + "\",\n"
                + "\"TodayPlus28Days\":\"" + UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 28) + "\",\n"
                + "\"Case_No\":\"123456\",\n"
                + "\"submission_reference\":\"12212121\",\n"
                + "}\n"
                + "}\n";
        DefaultValues allocatedCourtAddress = DefaultValues.builder()
                .tribunalCorrespondenceAddressLine1("Aberdeen Address Line1")
                .tribunalCorrespondenceAddressLine2("Aberdeen Address Line2")
                .tribunalCorrespondencePostCode("BA 3453")
                .tribunalCorrespondenceEmail("aberdeen@gmail.com")
                .tribunalCorrespondenceTown("Aberdeen")
                .build();
        assertEquals(expected, DocumentHelper.buildDocumentContent(caseDetailsScot3.getCaseData(), "",
                userDetails, SCOTLAND_CASE_TYPE_ID,
                caseDetailsScot3.getCaseData().getCorrespondenceType(),
                caseDetailsScot3.getCaseData().getCorrespondenceScotType(),
                null, allocatedCourtAddress, venueAddressReaderService).toString());
    }

    @Test
    void buildDocumentContentScot4() throws URISyntaxException, IOException {
        when(venueAddressReaderService.getVenueAddress(any(), any(), any())).thenReturn(ABERDEEN_VENUE_ADDRESS);

        String expectedResult = getExpectedResult();
        expectedResult = expectedResult.replace("current-date", UtilHelper.formatCurrentDate(LocalDate.now()));
        expectedResult = expectedResult.replace("plus28",
                UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 28));
        String actualResult = DocumentHelper.buildDocumentContent(caseDetailsScot4.getCaseData(), "",
                userDetails, SCOTLAND_CASE_TYPE_ID,
                caseDetailsScot4.getCaseData().getCorrespondenceType(),
                caseDetailsScot4.getCaseData().getCorrespondenceScotType(),
                null, null, venueAddressReaderService).toString();
        assertEquals(expectedResult, actualResult.trim());
    }

    @Test
    void buildScotDocumentTemplates() {
        CaseData caseData = new CaseData();
        CorrespondenceScotType correspondenceScotType = new CorrespondenceScotType();
        String topLevel = "Part_3_Scot";
        String part = "32";
        correspondenceScotType.setTopLevelScotDocuments(topLevel);
        correspondenceScotType.setPart3ScotDocuments(part);
        caseData.setCorrespondenceScotType(correspondenceScotType);
        CaseDetails caseDetailsTemplates = new CaseDetails();
        caseDetailsTemplates.setCaseData(caseData);
        assertEquals(getJson(topLevel, part), DocumentHelper.buildDocumentContent(caseDetailsTemplates.getCaseData(),
                "", userDetails, SCOTLAND_CASE_TYPE_ID,
                caseDetailsTemplates.getCaseData().getCorrespondenceType(),
                caseDetailsTemplates.getCaseData().getCorrespondenceScotType(), null,
                null, venueAddressReaderService).toString());
        topLevel = "Part_4_Scot";
        part = "42";
        correspondenceScotType = new CorrespondenceScotType();
        correspondenceScotType.setTopLevelScotDocuments(topLevel);
        correspondenceScotType.setPart4ScotDocuments(part);
        caseData.setCorrespondenceScotType(correspondenceScotType);
        caseDetailsTemplates.setCaseData(caseData);
        assertEquals(getJson(topLevel, part), DocumentHelper.buildDocumentContent(caseDetailsTemplates.getCaseData(),
                "", userDetails, SCOTLAND_CASE_TYPE_ID,
                caseDetailsTemplates.getCaseData().getCorrespondenceType(),
                caseDetailsTemplates.getCaseData().getCorrespondenceScotType(), null,
                null, venueAddressReaderService).toString());
        topLevel = "Part_5_Scot";
        part = "52";
        correspondenceScotType = new CorrespondenceScotType();
        correspondenceScotType.setTopLevelScotDocuments(topLevel);
        correspondenceScotType.setPart5ScotDocuments(part);
        caseData.setCorrespondenceScotType(correspondenceScotType);
        caseDetailsTemplates.setCaseData(caseData);
        assertEquals(getJson(topLevel, part), DocumentHelper.buildDocumentContent(caseDetailsTemplates.getCaseData(),
                "", userDetails, SCOTLAND_CASE_TYPE_ID,
                caseDetailsTemplates.getCaseData().getCorrespondenceType(),
                caseDetailsTemplates.getCaseData().getCorrespondenceScotType(), null,
                null, venueAddressReaderService).toString());
        topLevel = "Part_6_Scot";
        part = "62";
        correspondenceScotType = new CorrespondenceScotType();
        correspondenceScotType.setTopLevelScotDocuments(topLevel);
        correspondenceScotType.setPart6ScotDocuments(part);
        caseData.setCorrespondenceScotType(correspondenceScotType);
        caseDetailsTemplates.setCaseData(caseData);
        assertEquals(getJson(topLevel, part), DocumentHelper.buildDocumentContent(caseDetailsTemplates.getCaseData(),
                "", userDetails, SCOTLAND_CASE_TYPE_ID,
                caseDetailsTemplates.getCaseData().getCorrespondenceType(),
                caseDetailsTemplates.getCaseData().getCorrespondenceScotType(), null,
                null, venueAddressReaderService).toString());
        topLevel = "Part_7_Scot";
        part = "72";
        correspondenceScotType = new CorrespondenceScotType();
        correspondenceScotType.setTopLevelScotDocuments(topLevel);
        correspondenceScotType.setPart7ScotDocuments(part);
        caseData.setCorrespondenceScotType(correspondenceScotType);
        caseDetailsTemplates.setCaseData(caseData);
        assertEquals(getJson(topLevel, part), DocumentHelper.buildDocumentContent(caseDetailsTemplates.getCaseData(),
                "", userDetails, SCOTLAND_CASE_TYPE_ID,
                caseDetailsTemplates.getCaseData().getCorrespondenceType(),
                caseDetailsTemplates.getCaseData().getCorrespondenceScotType(), null,
                null, venueAddressReaderService).toString());
        topLevel = "Part_15_Scot";
        part = "152";
        correspondenceScotType = new CorrespondenceScotType();
        correspondenceScotType.setTopLevelScotDocuments(topLevel);
        correspondenceScotType.setPart15ScotDocuments(part);
        caseData.setCorrespondenceScotType(correspondenceScotType);
        caseDetailsTemplates.setCaseData(caseData);
        assertEquals(getJson(topLevel, part), DocumentHelper.buildDocumentContent(caseDetailsTemplates.getCaseData(),
                "", userDetails, SCOTLAND_CASE_TYPE_ID,
                caseDetailsTemplates.getCaseData().getCorrespondenceType(),
                caseDetailsTemplates.getCaseData().getCorrespondenceScotType(), null,
                null, venueAddressReaderService).toString());
        topLevel = "Part_9_Scot";
        part = "162";
        correspondenceScotType = new CorrespondenceScotType();
        correspondenceScotType.setTopLevelScotDocuments(topLevel);
        correspondenceScotType.setPart9ScotDocuments(part);
        caseData.setCorrespondenceScotType(correspondenceScotType);
        caseDetailsTemplates.setCaseData(caseData);
        assertEquals(getJson(topLevel, part), DocumentHelper.buildDocumentContent(caseDetailsTemplates.getCaseData(),
                "", userDetails, SCOTLAND_CASE_TYPE_ID,
                caseDetailsTemplates.getCaseData().getCorrespondenceType(),
                caseDetailsTemplates.getCaseData().getCorrespondenceScotType(), null,
                null, venueAddressReaderService).toString());
    }

    @Test
    void buildDocumentTemplates() {
        CaseData caseData = new CaseData();
        CorrespondenceType correspondenceType = new CorrespondenceType();
        String topLevel = "Part_18";
        String part = "18A";
        correspondenceType.setTopLevelDocuments(topLevel);
        correspondenceType.setPart18Documents(part);
        caseData.setCorrespondenceType(correspondenceType);
        CaseDetails caseDetailsTemplates = new CaseDetails();
        caseDetailsTemplates.setCaseData(caseData);
        String result = "{\n"
                + "\"accessKey\":\"\",\n"
                + "\"templateName\":\"Part_18.docx\",\n"
                + "\"outputName\":\"document.docx\",\n"
                + "\"data\":{\n"
                + "\"claimant_or_rep_full_name\":\"\",\n"
                + "\"claimant_full_name\":\"\",\n"
                + "\"Claimant\":\"\",\n"
                + "\"claimant_rep_organisation\":\"\",\n"
                + "\"claimant_or_rep_addressLine1\":\"\",\n"
                + "\"claimant_or_rep_addressLine2\":\"\",\n"
                + "\"claimant_or_rep_addressLine3\":\"\",\n"
                + "\"claimant_or_rep_town\":\"\",\n"
                + "\"claimant_or_rep_county\":\"\",\n"
                + "\"claimant_or_rep_postCode\":\"\",\n"
                + "\"claimant_addressLine1\":\"\",\n"
                + "\"claimant_addressLine2\":\"\",\n"
                + "\"claimant_addressLine3\":\"\",\n"
                + "\"claimant_town\":\"\",\n"
                + "\"claimant_county\":\"\",\n"
                + "\"claimant_postCode\":\"\",\n"
                + "\"respondent_or_rep_full_name\":\"\",\n"
                + "\"respondent_rep_organisation\":\"\",\n"
                + "\"respondent_or_rep_addressLine1\":\"\",\n"
                + "\"respondent_or_rep_addressLine2\":\"\",\n"
                + "\"respondent_or_rep_addressLine3\":\"\",\n"
                + "\"respondent_or_rep_town\":\"\",\n"
                + "\"respondent_or_rep_county\":\"\",\n"
                + "\"respondent_or_rep_postCode\":\"\",\n"
                + "\"respondent_full_name\":\"\",\n"
                + "\"respondent_addressLine1\":\"\",\n"
                + "\"respondent_addressLine2\":\"\",\n"
                + "\"respondent_addressLine3\":\"\",\n"
                + "\"respondent_town\":\"\",\n"
                + "\"respondent_county\":\"\",\n"
                + "\"respondent_postCode\":\"\",\n"
                + "\"Respondent\":\"\",\n"
                + "\"resp_others\":\"\",\n"
                + "\"resp_address\":\"\",\n"
                + "\"Hearing_date\":\"\",\n"
                + "\"Hearing_date_time\":\"\",\n"
                + "\"Hearing_venue\":\"\",\n"
                + "\"Hearing_duration\":\"\",\n"
                + "\"Hearing_time\":\"\",\n"
                + "\"t18A\":\"true\",\n"
                + "\"Court_telephone\":\"\",\n"
                + "\"Court_fax\":\"\",\n"
                + "\"Court_DX\":\"\",\n"
                + "\"Court_Email\":\"\",\n"
                + "\"i18A_enhmcts\":\"[userImage:enhmcts.png]\",\n"
                + "\"i18A_enhmcts1\":\"[userImage:enhmcts.png]\",\n"
                + "\"i18A_enhmcts2\":\"[userImage:enhmcts.png]\",\n"
                + "\"iScot_schmcts\":\"[userImage:schmcts.png]\",\n"
                + "\"iScot_schmcts1\":\"[userImage:schmcts.png]\",\n"
                + "\"iScot_schmcts2\":\"[userImage:schmcts.png]\",\n"
                + "\"Clerk\":\"Mike Jordan\",\n"
                + "\"Today_date\":\"" + UtilHelper.formatCurrentDate(LocalDate.now()) + "\",\n"
                + "\"TodayPlus28Days\":\"" + UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 28) + "\",\n"
                + "\"Case_No\":\"\",\n"
                + "\"submission_reference\":\"\",\n"
                + "}\n"
                + "}\n";
        assertEquals(result, DocumentHelper.buildDocumentContent(caseDetailsTemplates.getCaseData(),
                "", userDetails, "",
                caseDetailsTemplates.getCaseData().getCorrespondenceType(),
                caseDetailsTemplates.getCaseData().getCorrespondenceScotType(), null,
                null, venueAddressReaderService).toString());
    }

    private String getJson(String topLevel, String part) {
        return "{\n"
                + "\"accessKey\":\"\",\n"
                + "\"templateName\":\"" + topLevel + ".docx\",\n"
                + "\"outputName\":\"document.docx\",\n"
                + "\"data\":{\n"
                + "\"claimant_or_rep_full_name\":\"\",\n"
                + "\"claimant_full_name\":\"\",\n"
                + "\"Claimant\":\"\",\n"
                + "\"claimant_rep_organisation\":\"\",\n"
                + "\"claimant_or_rep_addressLine1\":\"\",\n"
                + "\"claimant_or_rep_addressLine2\":\"\",\n"
                + "\"claimant_or_rep_addressLine3\":\"\",\n"
                + "\"claimant_or_rep_town\":\"\",\n"
                + "\"claimant_or_rep_county\":\"\",\n"
                + "\"claimant_or_rep_postCode\":\"\",\n"
                + "\"claimant_addressLine1\":\"\",\n"
                + "\"claimant_addressLine2\":\"\",\n"
                + "\"claimant_addressLine3\":\"\",\n"
                + "\"claimant_town\":\"\",\n"
                + "\"claimant_county\":\"\",\n"
                + "\"claimant_postCode\":\"\",\n"
                + "\"respondent_or_rep_full_name\":\"\",\n"
                + "\"respondent_rep_organisation\":\"\",\n"
                + "\"respondent_or_rep_addressLine1\":\"\",\n"
                + "\"respondent_or_rep_addressLine2\":\"\",\n"
                + "\"respondent_or_rep_addressLine3\":\"\",\n"
                + "\"respondent_or_rep_town\":\"\",\n"
                + "\"respondent_or_rep_county\":\"\",\n"
                + "\"respondent_or_rep_postCode\":\"\",\n"
                + "\"respondent_full_name\":\"\",\n"
                + "\"respondent_addressLine1\":\"\",\n"
                + "\"respondent_addressLine2\":\"\",\n"
                + "\"respondent_addressLine3\":\"\",\n"
                + "\"respondent_town\":\"\",\n"
                + "\"respondent_county\":\"\",\n"
                + "\"respondent_postCode\":\"\",\n"
                + "\"Respondent\":\"\",\n"
                + "\"resp_others\":\"\",\n"
                + "\"resp_address\":\"\",\n"
                + "\"Hearing_date\":\"\",\n"
                + "\"Hearing_date_time\":\"\",\n"
                + "\"Hearing_venue\":\"\",\n"
                + "\"Hearing_duration\":\"\",\n"
                + "\"Hearing_time\":\"\",\n"
                + "\"t_Scot_" + part + "\":\"true\",\n"
                + "\"Court_telephone\":\"\",\n"
                + "\"Court_fax\":\"\",\n"
                + "\"Court_DX\":\"\",\n"
                + "\"Court_Email\":\"\",\n"
                + "\"i_enhmcts\":\"[userImage:enhmcts.png]\",\n"
                + "\"i_enhmcts1\":\"[userImage:enhmcts.png]\",\n"
                + "\"i_enhmcts2\":\"[userImage:enhmcts.png]\",\n"
                + "\"iScot" + part + "_schmcts\":\"[userImage:schmcts.png]\",\n"
                + "\"iScot" + part + "_schmcts1\":\"[userImage:schmcts.png]\",\n"
                + "\"iScot" + part + "_schmcts2\":\"[userImage:schmcts.png]\",\n"
                + "\"Clerk\":\"Mike Jordan\",\n"
                + "\"Today_date\":\"" + UtilHelper.formatCurrentDate(LocalDate.now()) + "\",\n"
                + "\"TodayPlus28Days\":\"" + UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 28) + "\",\n"
                + "\"Case_No\":\"\",\n"
                + "\"submission_reference\":\"\",\n"
                + "}\n"
                + "}\n";
    }

    @Test
    void buildDocumentContentMultiples() {
        AddressLabelsAttributesType addressLabelsAttributesType = new AddressLabelsAttributesType();
        addressLabelsAttributesType.setNumberOfCopies("1");
        addressLabelsAttributesType.setStartingLabel("2");
        addressLabelsAttributesType.setShowTelFax("1232312");
        MultipleData multipleData = new MultipleData();
        CorrespondenceType correspondenceType = new CorrespondenceType();
        correspondenceType.setTopLevelDocuments(ADDRESS_LABELS_TEMPLATE);
        multipleData.setCorrespondenceType(correspondenceType);
        multipleData.setAddressLabelsAttributesType(addressLabelsAttributesType);
        multipleData.setAddressLabelCollection(MultipleUtil.getAddressLabelTypeItemList());
        String expected = "{\n"
                + "\"accessKey\":\"\",\n"
                + "\"templateName\":\"EM-TRB-LET-ENG-00544.docx\",\n"
                + "\"outputName\":\"document.docx\",\n"
                + "\"data\":{\n"
                + "\"address_labels_page\":[\n"
                + "{\"Label_02_Entity_Name_01\":\"\",\n"
                + "\"Label_02_Entity_Name_02\":\"\",\n"
                + "\"lbl_02_Eef\":\"\",\n"
                + "\"lbl_02_Cef\":\"\",\n"
                + "\"Label_03_Entity_Name_01\":\"Label Entity1 Name\",\n"
                + "\"Label_03_Entity_Name_02\":\"Label Entity2 Name\",\n"
                + "\"Label_03_Address_Line_01\":\"Address Line1\",\n"
                + "\"Label_03_Address_Line_02\":\"Address Line2\",\n"
                + "\"Label_03_Address_Line_03\":\"M2 45GD\",\n"
                + "\"lbl_03_Eef\":\"\",\n"
                + "\"lbl_03_Cef\":\"Reference01345\"}],\n"
                + "\"i_enhmcts\":\"[userImage:enhmcts.png]\",\n"
                + "\"i_enhmcts1\":\"[userImage:enhmcts.png]\",\n"
                + "\"i_enhmcts2\":\"[userImage:enhmcts.png]\",\n"
                + "\"iScot_schmcts\":\"[userImage:schmcts.png]\",\n"
                + "\"iScot_schmcts1\":\"[userImage:schmcts.png]\",\n"
                + "\"iScot_schmcts2\":\"[userImage:schmcts.png]\",\n"
                + "\"Clerk\":\"Mike Jordan\",\n"
                + "\"Today_date\":\"" + UtilHelper.formatCurrentDate(LocalDate.now()) + "\",\n"
                + "\"TodayPlus28Days\":\"" + UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 28) + "\",\n"
                + "\"Case_No\":\"123456\",\n"
                + "\"submission_reference\":\"12212121\",\n"
                + "}\n"
                + "}\n";
        assertEquals(expected, DocumentHelper.buildDocumentContent(caseDetails2.getCaseData(), "",
                userDetails, ENGLANDWALES_CASE_TYPE_ID,
                multipleData.getCorrespondenceType(), multipleData.getCorrespondenceScotType(),
                multipleData, null, venueAddressReaderService).toString());
    }

    @Test
    void getCorrespondenceHearingNumber() {
        String expectedCorrespondenceHearingNumber = "2";

        assertEquals(expectedCorrespondenceHearingNumber, DocumentHelper.getCorrespondenceHearingNumber(
                caseDetails1.getCaseData().getCorrespondenceType(),
                caseDetails1.getCaseData().getCorrespondenceScotType()));
    }

    @Test
    void getHearingByNumber() {
        String expectedHearingNumber = "2";
        String expectedHearingType = "Single";
        String expectedHearingVenue = "Manchester";

        String correspondenceHearingNumber = "2";

        assertEquals(expectedHearingNumber,
                DocumentHelper.getHearingByNumber(caseDetails1.getCaseData().getHearingCollection(),
                        correspondenceHearingNumber).getHearingNumber());
        assertEquals(expectedHearingType,
                DocumentHelper.getHearingByNumber(caseDetails1.getCaseData().getHearingCollection(),
                        correspondenceHearingNumber).getHearingType());
        assertEquals(expectedHearingVenue,
                DocumentHelper.getHearingByNumber(caseDetails1.getCaseData().getHearingCollection(),
                        correspondenceHearingNumber).getHearingVenue().getSelectedLabel());
    }

    private String getExpectedResult() throws URISyntaxException, IOException {
        String expectedJson = new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(Thread.currentThread()
            .getContextClassLoader().getResource("expectedDocumentContentScot4.json")).toURI())))
                .replace("\r\n", "\n");
        LocalDate currentLocalDate = LocalDate.now();
        LocalDate currentLocalDatePlus28Days = currentLocalDate.plusDays(28);
        return expectedJson.replace("current-date-placeholder",
                        UtilHelper.formatCurrentDate(currentLocalDate))
                .replace("current-date-plus28-placeholder",
                        UtilHelper.formatCurrentDate(currentLocalDatePlus28Days));
    }

    @Test
    void createDocumentTypeItem_createsCorrectly() {
        UploadedDocumentType build = UploadedDocumentType.builder().documentFilename("fileName").documentUrl("url")
            .documentBinaryUrl("binaryUrl").build();
        DocumentTypeItem actual = DocumentHelper.createDocumentTypeItem(
            build, "typeOfDocument", "shortDescription"
        );

        DocumentType expected = DocumentType.builder()
                .typeOfDocument("typeOfDocument")
                .shortDescription("shortDescription")
                .uploadedDocument(build)
                .dateOfCorrespondence(LocalDate.now().toString())
                .topLevelDocuments(LEGACY_DOCUMENT_NAMES)
                .documentType("typeOfDocument")
                .build();

        assertThat(actual.getId()).isNotEmpty();
        assertThat(actual.getValue()).isEqualTo(expected);
    }

    @Test
    void setDocumentNumbers() {
        CaseData caseData = CaseDataBuilder.builder()
                .withDocumentCollection(ET1)
                .withDocumentCollection(ET1_ATTACHMENT)
                .withDocumentCollection(ACAS_CERTIFICATE)
                .build();
        DocumentHelper.setDocumentNumbers(caseData);
        caseData.getDocumentCollection().forEach(d -> assertThat(d.getValue().getDocNumber()).isNotNull());
    }
}
