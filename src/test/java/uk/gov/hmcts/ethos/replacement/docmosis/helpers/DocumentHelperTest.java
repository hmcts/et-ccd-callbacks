package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.ecm.common.model.helper.DefaultValues;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.TseRespondTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.AddressLabelsAttributesType;
import uk.gov.hmcts.et.common.model.ccd.types.CorrespondenceScotType;
import uk.gov.hmcts.et.common.model.ccd.types.CorrespondenceType;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.TseRespondType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VenueAddressReaderService;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ADDRESS_LABELS_TEMPLATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.ACAS_CERTIFICATE;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.ET1;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.ET1_ATTACHMENT;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.LEGACY_DOCUMENT_NAMES;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.DocumentFixtures.getDocumentTypeItem;

@ExtendWith(SpringExtension.class)
class DocumentHelperTest {

    private static final String DUMMY_CASE_TYPE_ID = "dummy case type id";
    private static final String CLOSING_QUOTE_COMMA_AND_NEWLINE = "\",\n";
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
    public void setUp() throws Exception {
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
                + "\"accessKey\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"templateName\":\"EM-TRB-EGW-ENG-00026.docx" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"outputName\":\"document.docx" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"data\":{\n"
                + "\"claimant_or_rep_full_name\":\"RepresentativeNameClaimant" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_rep_organisation\":\"RepresentativeOrganisation" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_addressLine1\":\"56 Block C" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_addressLine2\":\"Ellesmere Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_town\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_county\":\"Lancashire" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_postCode\":\"M3 KJR" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_reference\":\"1111111" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_full_name\":\"Mr A J Rodriguez" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Claimant\":\"Mr A J Rodriguez" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_addressLine1\":\"34" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_addressLine2\":\"Low Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_town\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_county\":\"Lancashire" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_postCode\":\"M3 6gw" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_full_name\":\"Antonio Vazquez" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_addressLine1\":\"11 Small Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_addressLine2\":\"22 House" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_town\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_county\":\"North West" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_postCode\":\"M12 42R" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_full_name\":\"Antonio Vazquez" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_addressLine1\":\"11 Small Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_addressLine2\":\"22 House" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_town\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_county\":\"North West" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_postCode\":\"M12 42R" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Respondent\":\"1. Antonio Vazquez" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"resp_others\":\"2. Juan Garcia\\n3. Mike Jordan" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"resp_address\":\"1. 11 Small Street, 22 House, Manchester, North West, M12 42R, UK\\n2. "
                + "12 Small Street, 24 House, Manchester, North West, M12 4ED, UK\\n3. 11 Small Street, 22 House, "
                + "Manchester, North West, M12 42R, UK" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_date\":\"25 November 2019, 14 December 2019, 28 December 2019"
                + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_date_time\":\"25 November 2019, 14 December 2019, 28 December 2019 at 10:30"
                + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_time\":\"10:30" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_venue\":\"Manchester Employment Tribunals, Alexandra House, 14-22 The Parsonage, "
                + "Manchester, M3 2JA" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_duration\":\"2 days" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"t1_2\":\"true" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_addressLine1\":\"Manchester Employment Tribunal," + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_addressLine2\":\"Alexandra House," + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_addressLine3\":\"14-22 The Parsonage," + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_town\":\"Manchester," + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_county\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_postCode\":\"M3 2JA" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_telephone\":\"03577131270" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_fax\":\"07577126570" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_DX\":\"123456" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_Email\":\"ManchesterOfficeET@hmcts.gov.uk" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"i1_2_enhmcts\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"i1_2_enhmcts1\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"i1_2_enhmcts2\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot_schmcts\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot_schmcts1\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot_schmcts2\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Clerk\":\"Mike Jordan" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Today_date\":\"" + UtilHelper.formatCurrentDate(LocalDate.now())
                + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"TodayPlus28Days\":\"" + UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 28)
                + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Case_No\":\"123456" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"submission_reference\":\"12212121" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "}\n}\n";
        assertEquals(expected, DocumentHelper.buildDocumentContent(caseDetails1.getCaseData(), "",
                userDetails, ENGLANDWALES_CASE_TYPE_ID,
                caseDetails1.getCaseData().getCorrespondenceType(),
                caseDetails1.getCaseData().getCorrespondenceScotType(), null,
                null, venueAddressReaderService).toString());
    }

    @Test
    void buildDocumentContent2_ResponseStruckOut() {
        String expected = "{\n"
                + "\"accessKey\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"templateName\":\"EM-TRB-EGW-ENG-00027.docx" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"outputName\":\"document.docx" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"data\":{\n"
                + "\"claimant_or_rep_full_name\":\"Orlando LTD" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_full_name\":\"Orlando LTD" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Claimant\":\"Orlando LTD" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_addressLine1\":\"34" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_addressLine2\":\"Low Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_town\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_county\":\"Lancashire" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_postCode\":\"M3 6gw" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_addressLine1\":\"34" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_addressLine2\":\"Low Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_town\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_county\":\"Lancashire" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_postCode\":\"M3 6gw" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_full_name\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_rep_organisation\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_addressLine1\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_addressLine2\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_town\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_county\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_postCode\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_full_name\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Respondent\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"resp_others\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"resp_address\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_date\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_date_time\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_time\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_venue\":\"Manchester Employment Tribunals, Alexandra House, 14-22 The Parsonage, "
                + "Manchester, M3 2JA" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_duration\":\"2 days" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"t2_2A\":\"true" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_addressLine1\":\"Manchester Employment Tribunal," + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_addressLine2\":\"Alexandra House," + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_addressLine3\":\"14-22 The Parsonage," + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_town\":\"Manchester," + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_county\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_postCode\":\"M3 2JA" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_telephone\":\"03577131270" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_fax\":\"07577126570" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_DX\":\"123456" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_Email\":\"ManchesterOfficeET@hmcts.gov.uk" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"i2_2A_enhmcts\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"i2_2A_enhmcts1\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"i2_2A_enhmcts2\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot_schmcts\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot_schmcts1\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot_schmcts2\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Clerk\":\"Mike Jordan" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Today_date\":\"" + UtilHelper.formatCurrentDate(LocalDate.now())
                + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"TodayPlus28Days\":\"" + UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 28)
                + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Case_No\":\"123456" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"submission_reference\":\"12212121" + CLOSING_QUOTE_COMMA_AND_NEWLINE
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
                + "\"accessKey\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"templateName\":\"EM-TRB-EGW-ENG-00027.docx" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"outputName\":\"document.docx" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"data\":{\n"
                + "\"claimant_or_rep_full_name\":\"Orlando LTD" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_full_name\":\"Orlando LTD" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Claimant\":\"Orlando LTD" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_addressLine1\":\"34" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_addressLine2\":\"Low Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_town\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_county\":\"Lancashire" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_postCode\":\"M3 6gw" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_addressLine1\":\"34" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_addressLine2\":\"Low Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_town\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_county\":\"Lancashire" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_postCode\":\"M3 6gw" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_full_name\":\"RepresentativeNameRespondent" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_addressLine1\":\"56 Block C" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_addressLine2\":\"Ellesmere Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_town\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_county\":\"Lancashire" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_postCode\":\"M3 KJR" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_reference\":\"1111111" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_rep_organisation\":\"RepresentativeOrganisation" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_full_name\":\"Antonio Vazquez" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_addressLine1\":\"11 Small Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_addressLine2\":\"22 House" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_town\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_county\":\"North West" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_postCode\":\"M12 42R" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Respondent\":\"Antonio Vazquez" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"resp_others\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"resp_address\":\"11 Small Street, 22 House, Manchester, North West, M12 42R, UK"
                + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_date\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_date_time\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_time\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_venue\":\"Manchester Employment Tribunals, Alexandra House, 14-22 The Parsonage, "
                + "Manchester, M3 2JA" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_duration\":\"2 days" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"t2_2A\":\"true" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_addressLine1\":\"Manchester Employment Tribunal," + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_addressLine2\":\"Alexandra House," + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_addressLine3\":\"14-22 The Parsonage," + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_town\":\"Manchester," + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_county\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_postCode\":\"M3 2JA" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_telephone\":\"03577131270" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_fax\":\"07577126570" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_DX\":\"123456" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_Email\":\"ManchesterOfficeET@hmcts.gov.uk" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"i2_2A_enhmcts\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"i2_2A_enhmcts1\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"i2_2A_enhmcts2\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot_schmcts\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot_schmcts1\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot_schmcts2\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Clerk\":\"Mike Jordan" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Today_date\":\"" + UtilHelper.formatCurrentDate(LocalDate.now())
                + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"TodayPlus28Days\":\"" + UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 28)
                + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Case_No\":\"123456" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"submission_reference\":\"12212121" + CLOSING_QUOTE_COMMA_AND_NEWLINE
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
                + "\"accessKey\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"templateName\":\"EM-TRB-EGW-ENG-00028.docx" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"outputName\":\"document.docx" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"data\":{\n"
                + "\"claimant_or_rep_full_name\":\"Mr A J Rodriguez" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_full_name\":\"Mr A J Rodriguez" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Claimant\":\"Mr A J Rodriguez" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_addressLine1\":\"34" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_addressLine2\":\"Low Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_town\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_county\":\"Lancashire" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_postCode\":\"M3 6gw" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_addressLine1\":\"34" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_addressLine2\":\"Low Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_town\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_county\":\"Lancashire" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_postCode\":\"M3 6gw" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_full_name\":\"RepresentativeNameRespondent" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_addressLine1\":\"56 Block C" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_addressLine2\":\"Ellesmere Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_town\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_county\":\"Lancashire" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_postCode\":\"M3 KJR" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_reference\":\"1111111" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_rep_organisation\":\"RepresentativeOrganisation" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_full_name\":\"Antonio Vazquez" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_addressLine1\":\"11 Small Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_addressLine2\":\"22 House" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_town\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_county\":\"North West" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_postCode\":\"M12 42R" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Respondent\":\"Antonio Vazquez" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"resp_others\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"resp_address\":\"11 Small Street, 22 House, Manchester, North West, M12 42R, UK" 
                + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_date\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_date_time\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_venue\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_duration\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_time\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"t3_2\":\"true" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_addressLine1\":\"Manchester Employment Tribunal," + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_addressLine2\":\"Alexandra House," + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_addressLine3\":\"14-22 The Parsonage," + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_town\":\"Manchester," + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_county\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_postCode\":\"M3 2JA" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_telephone\":\"03577131270" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_fax\":\"07577126570" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_DX\":\"123456" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_Email\":\"ManchesterOfficeET@hmcts.gov.uk" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"i3_2_enhmcts\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"i3_2_enhmcts1\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"i3_2_enhmcts2\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot_schmcts\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot_schmcts1\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot_schmcts2\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Clerk\":\"Mike Jordan" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Today_date\":\"" + UtilHelper.formatCurrentDate(LocalDate.now()) 
                + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"TodayPlus28Days\":\"" + UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 28) 
                + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Case_No\":\"123456" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"submission_reference\":\"12212121" + CLOSING_QUOTE_COMMA_AND_NEWLINE
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
                + "\"accessKey\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"templateName\":\"EM-TRB-EGW-ENG-00029.docx" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"outputName\":\"document.docx" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"data\":{\n"
                + "\"claimant_or_rep_full_name\":\"RepresentativeNameClaimant" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_rep_organisation\":\"RepresentativeOrganisation" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_addressLine1\":\"56 Block C" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_addressLine2\":\"Ellesmere Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_town\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_county\":\"Lancashire" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_postCode\":\"M3 KJR" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_reference\":\"1111111" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_full_name\":\"Mr A J Rodriguez" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Claimant\":\"Mr A J Rodriguez" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_addressLine1\":\"34" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_addressLine2\":\"Low Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_town\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_county\":\"Lancashire" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_postCode\":\"M3 6gw" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_full_name\":\"Antonio Vazquez" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_addressLine1\":\"11 Small Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_addressLine2\":\"22 House" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_town\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_county\":\"North West" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_postCode\":\"M12 42R" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_full_name\":\"Antonio Vazquez" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_addressLine1\":\"11 Small Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_addressLine2\":\"22 House" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_town\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_county\":\"North West" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_postCode\":\"M12 42R" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Respondent\":\"Antonio Vazquez" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"resp_others\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"resp_address\":\"11 Small Street, 22 House, Manchester, North West, M12 42R, UK"
                + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_date\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_date_time\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_venue\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_duration\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_time\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"t4_2\":\"true" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_addressLine1\":\"Manchester Employment Tribunal," + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_addressLine2\":\"Alexandra House," + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_addressLine3\":\"14-22 The Parsonage," + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_town\":\"Manchester," + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_county\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_postCode\":\"M3 2JA" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_telephone\":\"03577131270" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_fax\":\"07577126570" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_DX\":\"123456" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_Email\":\"ManchesterOfficeET@hmcts.gov.uk" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"i4_2_enhmcts\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"i4_2_enhmcts1\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"i4_2_enhmcts2\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot_schmcts\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot_schmcts1\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot_schmcts2\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Clerk\":\"Mike Jordan" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Today_date\":\"" + UtilHelper.formatCurrentDate(LocalDate.now()) 
                + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"TodayPlus28Days\":\"" + UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 28) 
                + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Case_No\":\"123456" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"submission_reference\":\"12212121" + CLOSING_QUOTE_COMMA_AND_NEWLINE
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
                + "\"accessKey\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"templateName\":\"EM-TRB-EGW-ENG-00030.docx" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"outputName\":\"document.docx" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"data\":{\n"
                + "\"claimant_or_rep_full_name\":\"RepresentativeNameClaimant" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_rep_organisation\":\"RepresentativeOrganisation" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_addressLine1\":\"56 Block C" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_addressLine2\":\"Ellesmere Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_town\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_county\":\"Lancashire" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_postCode\":\"M3 KJR" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_reference\":\"1111111" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_full_name\":\"Mr A J Rodriguez" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Claimant\":\"Mr A J Rodriguez" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_addressLine1\":\"34" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_addressLine2\":\"Low Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_town\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_county\":\"Lancashire" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_postCode\":\"M3 6gw" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_full_name\":\"RepresentativeNameRespondent1" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_addressLine1\":\"56 Block C" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_addressLine2\":\"Ellesmere Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_town\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_county\":\"Lancashire" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_postCode\":\"M3 KJR" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_reference\":\"3333333333" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_rep_organisation\":\"RepresentativeOrganisation" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_full_name\":\"Antonio Vazquez" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_addressLine1\":\"11 Small Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_addressLine2\":\"22 House" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_town\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_county\":\"North West" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_postCode\":\"M12 42R" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Respondent\":\"1. Antonio Vazquez" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"resp_others\":\"2. Antonio Vazquez2" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"resp_address\":\"1. 11 Small Street, 22 House, Manchester, North West, M12 42R, UK\\n2. 11 Small"
                + " Street, 22 House, Manchester, North West, M12 42R, UK" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_date\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_date_time\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_venue\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_duration\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_time\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"t5_2\":\"true" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_addressLine1\":\"Manchester Employment Tribunal," + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_addressLine2\":\"Alexandra House," + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_addressLine3\":\"14-22 The Parsonage," + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_town\":\"Manchester," + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_county\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_postCode\":\"M3 2JA" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_telephone\":\"03577131270" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_fax\":\"07577126570" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_DX\":\"123456" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_Email\":\"ManchesterOfficeET@hmcts.gov.uk" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"i5_2_enhmcts\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"i5_2_enhmcts1\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"i5_2_enhmcts2\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot_schmcts\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot_schmcts1\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot_schmcts2\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Clerk\":\"Mike Jordan" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Today_date\":\"" + UtilHelper.formatCurrentDate(LocalDate.now())
                + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"TodayPlus28Days\":\"" + UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 28)
                + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Case_No\":\"123456" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"submission_reference\":\"12212121" + CLOSING_QUOTE_COMMA_AND_NEWLINE
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
                + "\"accessKey\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"templateName\":\"EM-TRB-EGW-ENG-00031.docx" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"outputName\":\"document.docx" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"data\":{\n"
                + "\"claimant_or_rep_full_name\":\"RepresentativeNameClaimant" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_rep_organisation\":\"RepresentativeOrganisation" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_addressLine1\":\"56 Block C" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_addressLine2\":\"Ellesmere Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_town\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_county\":\"Lancashire" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_postCode\":\"M3 KJR" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_reference\":\"1111111" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_full_name\":\"Orlando LTD" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Claimant\":\"Orlando LTD" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_addressLine1\":\"34" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_addressLine2\":\"Low Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_town\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_county\":\"Lancashire" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_postCode\":\"M3 6gw" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_full_name\":\"RepresentativeNameRespondent1" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_addressLine1\":\"56 Block C" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_addressLine2\":\"Ellesmere Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_town\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_county\":\"Lancashire" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_postCode\":\"M3 KJR" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_reference\":\"3333333333" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_rep_organisation\":\"RepresentativeOrganisation" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_full_name\":\"Antonio Vazquez" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_addressLine1\":\"11 Small Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_addressLine2\":\"22 House" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_town\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_county\":\"North West" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_postCode\":\"M12 42R" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Respondent\":\"Antonio Vazquez" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"resp_others\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"resp_address\":\"11 Small Street, 22 House, Manchester, North West, M12 42R, UK"
                + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_date\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_date_time\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_venue\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_duration\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_time\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"t6_2\":\"true" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_addressLine1\":\"Manchester Employment Tribunal," + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_addressLine2\":\"Alexandra House," + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_addressLine3\":\"14-22 The Parsonage," + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_town\":\"Manchester," + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_county\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_postCode\":\"M3 2JA" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_telephone\":\"03577131270" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_fax\":\"07577126570" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_DX\":\"123456" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_Email\":\"ManchesterOfficeET@hmcts.gov.uk" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"i6_2_enhmcts\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"i6_2_enhmcts1\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"i6_2_enhmcts2\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot_schmcts\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot_schmcts1\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot_schmcts2\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Clerk\":\"Mike Jordan" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Today_date\":\"" + UtilHelper.formatCurrentDate(LocalDate.now())
                + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"TodayPlus28Days\":\"" + UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 28)
                + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Case_No\":\"123456" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"submission_reference\":\"12212121" + CLOSING_QUOTE_COMMA_AND_NEWLINE
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
                + "\"accessKey\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"templateName\":\"EM-TRB-EGW-ENG-00032.docx" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"outputName\":\"document.docx" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"data\":{\n"
                + "\"claimant_or_rep_full_name\":\"RepresentativeNameClaimant" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_rep_organisation\":\"RepresentativeOrganisation" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_addressLine1\":\"56 Block C" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_addressLine2\":\"Ellesmere Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_town\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_county\":\"Lancashire" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_postCode\":\"M3 KJR" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_reference\":\"1111111" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_full_name\":\"Mr A J Rodriguez" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Claimant\":\"Mr A J Rodriguez" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_addressLine1\":\"34" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_addressLine2\":\"Low Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_town\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_county\":\"Lancashire" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_postCode\":\"M3 6gw" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_full_name\":\"RepresentativeNameRespondent1" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_addressLine1\":\"56 Block C" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_addressLine2\":\"Ellesmere Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_town\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_county\":\"Lancashire" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_postCode\":\"M3 KJR" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_reference\":\"3333333333" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_rep_organisation\":\"RepresentativeOrganisation" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_full_name\":\"Antonio Vazquez" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_addressLine1\":\"11 Small Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_addressLine2\":\"22 House" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_town\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_county\":\"North West" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_postCode\":\"M12 42R" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Respondent\":\"Antonio Vazquez" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"resp_others\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"resp_address\":\"11 Small Street, 22 House, Manchester, North West, M12 42R, UK"
                + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_date\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_date_time\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_time\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_venue\":\"Manchester Employment Tribunals, Alexandra House, 14-22 The Parsonage, "
                + "Manchester, M3 2JA" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_duration\":\"2 days" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"t7_2\":\"true" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_addressLine1\":\"Manchester Employment Tribunal," + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_addressLine2\":\"Alexandra House," + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_addressLine3\":\"14-22 The Parsonage," + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_town\":\"Manchester," + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_county\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_postCode\":\"M3 2JA" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_telephone\":\"03577131270" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_fax\":\"07577126570" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_DX\":\"123456" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_Email\":\"ManchesterOfficeET@hmcts.gov.uk" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"i7_2_enhmcts\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"i7_2_enhmcts1\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"i7_2_enhmcts2\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot_schmcts\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot_schmcts1\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot_schmcts2\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Clerk\":\"Mike Jordan" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Today_date\":\"" + UtilHelper.formatCurrentDate(LocalDate.now()) + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"TodayPlus28Days\":\"" + UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 28)
                + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Case_No\":\"123456" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"submission_reference\":\"12212121" + CLOSING_QUOTE_COMMA_AND_NEWLINE
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
                + "\"accessKey\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"templateName\":\"EM-TRB-EGW-ENG-00033.docx" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"outputName\":\"document.docx" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"data\":{\n"
                + "\"claimant_or_rep_full_name\":\"RepresentativeNameClaimant" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_rep_organisation\":\"RepresentativeOrganisation" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_addressLine1\":\"56 Block C" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_addressLine2\":\"Ellesmere Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_town\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_county\":\"Lancashire" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_postCode\":\"M3 KJR" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_reference\":\"1111111" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_full_name\":\"Mr A J Rodriguez" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Claimant\":\"Mr A J Rodriguez" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_addressLine1\":\"34" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_addressLine2\":\"Low Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_town\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_county\":\"Lancashire" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_postCode\":\"M3 6gw" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_full_name\":\"Antonio Vazquez" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_addressLine1\":\"11 Small Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_addressLine2\":\"22 House" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_town\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_county\":\"North West" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_postCode\":\"M12 42R" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_full_name\":\"Antonio Vazquez" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_addressLine1\":\"11 Small Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_addressLine2\":\"22 House" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_town\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_county\":\"North West" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_postCode\":\"M12 42R" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Respondent\":\"1. Antonio Vazquez" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"resp_others\":\"2. Mikey McCollier" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"resp_address\":\"1. 11 Small Street, 22 House, Manchester, North West, M12 42R, UK\\n2. 1333 "
                + "Small Street, 22222 House, Liverpool, North West, L12 42R, UK" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_date\":\"25 November 2019" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_date_time\":\"25 November 2019 at 12:11" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_time\":\"12:11" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_venue\":\"Manchester Employment Tribunals, Alexandra House, 14-22 The Parsonage, "
                + "Manchester, M3 2JA" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_duration\":\"2 days" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"t10_2\":\"true" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_addressLine1\":\"Manchester Employment Tribunal," + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_addressLine2\":\"Alexandra House," + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_addressLine3\":\"14-22 The Parsonage," + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_town\":\"Manchester," + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_county\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_postCode\":\"M3 2JA" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_telephone\":\"03577131270" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_fax\":\"07577126570" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_DX\":\"123456" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_Email\":\"ManchesterOfficeET@hmcts.gov.uk" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"i10_2_enhmcts\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"i10_2_enhmcts1\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"i10_2_enhmcts2\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot_schmcts\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot_schmcts1\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot_schmcts2\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Clerk\":\"Mike Jordan" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Today_date\":\"" + UtilHelper.formatCurrentDate(LocalDate.now()) + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"TodayPlus28Days\":\"" + UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 28)
                + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Case_No\":\"123456" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"submission_reference\":\"12212121" + CLOSING_QUOTE_COMMA_AND_NEWLINE
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
                + "\"accessKey\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"templateName\":\"EM-TRB-EGW-ENG-00034.docx" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"outputName\":\"document.docx" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"data\":{\n"
                + "\"claimant_or_rep_full_name\":\"RepresentativeNameClaimant" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_rep_organisation\":\"RepresentativeOrganisation" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_addressLine1\":\"56 Block C" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_addressLine2\":\"Ellesmere Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_town\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_county\":\"Lancashire" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_postCode\":\"M3 KJR" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_reference\":\"1111111" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_full_name\":\"Mr A J Rodriguez" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Claimant\":\"Mr A J Rodriguez" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_addressLine1\":\"34" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_addressLine2\":\"Low Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_town\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_county\":\"Lancashire" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_postCode\":\"M3 6gw" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_full_name\":\"Antonio Vazquez" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_addressLine1\":\"11 Small Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_addressLine2\":\"22 House" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_town\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_county\":\"North West" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_postCode\":\"M12 42R" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_full_name\":\"Antonio Vazquez" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_addressLine1\":\"11 Small Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_addressLine2\":\"22 House" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_town\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_county\":\"North West" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_postCode\":\"M12 42R" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Respondent\":\"Antonio Vazquez" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"resp_others\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"resp_address\":\"11 Small Street, 22 House, Manchester, North West, M12 42R, UK"
                + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_date\":\"25 November 2019" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_date_time\":\"25 November 2019 at 12:11" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_time\":\"12:11" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_venue\":\"Manchester Employment Tribunals, Alexandra House, 14-22 The Parsonage, "
                + "Manchester, M3 2JA" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_duration\":\"2 days" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"t9_2\":\"true" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_addressLine1\":\"Manchester Employment Tribunal," + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_addressLine2\":\"Alexandra House," + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_addressLine3\":\"14-22 The Parsonage," + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_town\":\"Manchester," + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_county\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_postCode\":\"M3 2JA" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_telephone\":\"03577131270" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_fax\":\"07577126570" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_DX\":\"123456" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_Email\":\"ManchesterOfficeET@hmcts.gov.uk" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"i9_2_enhmcts\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"i9_2_enhmcts1\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"i9_2_enhmcts2\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot_schmcts\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot_schmcts1\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot_schmcts2\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Clerk\":\"Mike Jordan" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Today_date\":\"" + UtilHelper.formatCurrentDate(LocalDate.now()) + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"TodayPlus28Days\":\"" + UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 28)
                + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Case_No\":\"123456" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"submission_reference\":\"12212121" + CLOSING_QUOTE_COMMA_AND_NEWLINE
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
                + "\"accessKey\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"templateName\":\"EM-TRB-LET-ENG-00544.docx" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"outputName\":\"document.docx" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"data\":{\n"
                + "\"address_labels_page\":[\n"
                + "],\n"
                + "\"i0_1_enhmcts\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"i0_1_enhmcts1\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"i0_1_enhmcts2\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot_schmcts\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot_schmcts1\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot_schmcts2\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Clerk\":\"Mike Jordan" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Today_date\":\"" + UtilHelper.formatCurrentDate(LocalDate.now()) + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"TodayPlus28Days\":\"" + UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 28)
                + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Case_No\":\"123456" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"submission_reference\":\"12212121" + CLOSING_QUOTE_COMMA_AND_NEWLINE
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
                + "\"accessKey\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"templateName\":\"EM-TRB-LET-ENG-00544.docx" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"outputName\":\"document.docx" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"data\":{\n"
                + "\"address_labels_page\":[\n"
                + "{\"Label_01_Entity_Name_01\":\"Claimant Name" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_01_Entity_Name_02\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_01_Address_Line_01\":\"11 Block A" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_01_Address_Line_02\":\"Ellesmere Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_01_Address_Line_03\":\"Address Line 3" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_01_Address_Line_04\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_01_Address_Line_05\":\"Lancashire M1 KJR" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_01_Telephone\":\"07577 136511" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_01_Fax\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_01_Eef\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_01_Cef\":\"1850011/2020" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_02_Entity_Name_01\":\"Claimant Rep" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_02_Entity_Name_02\":\"Claimant Org" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_02_Address_Line_01\":\"22 Block B" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_02_Address_Line_02\":\"Ellesmere Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_02_Address_Line_03\":\"Address Line 3" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_02_Address_Line_04\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_02_Address_Line_05\":\"Lancashire M2 KJR" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_02_Telephone\":\"07577 136722" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_02_Fax\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_02_Eef\":\"OSCA/222/ABC" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_02_Cef\":\"1850022/2020\"}],\n"
                + "\"i0_2_enhmcts\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"i0_2_enhmcts1\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"i0_2_enhmcts2\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot_schmcts\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot_schmcts1\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot_schmcts2\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Clerk\":\"Mike Jordan" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Today_date\":\"" + UtilHelper.formatCurrentDate(LocalDate.now()) + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"TodayPlus28Days\":\"" + UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 28)
                + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Case_No\":\"123456" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"submission_reference\":\"12212121" + CLOSING_QUOTE_COMMA_AND_NEWLINE
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
                + "\"accessKey\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"templateName\":\"EM-TRB-LET-ENG-00544.docx" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"outputName\":\"document.docx" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"data\":{\n"
                + "\"address_labels_page\":[\n"
                + "{\"Label_01_Entity_Name_01\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_01_Entity_Name_02\":\"Claimant Org" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_01_Address_Line_01\":\"11 Block A" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_01_Address_Line_02\":\"M1 KJR" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_01_Eef\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_01_Cef\":\"1850011/2020" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_02_Entity_Name_01\":\"Claimant Rep" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_02_Entity_Name_02\":\"Claimant Org" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_02_Address_Line_01\":\"22 Block B" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_02_Address_Line_02\":\"Address Line 3" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_02_Address_Line_03\":\"Lancashire" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_02_Address_Line_04\":\"M2 KJR" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_02_Eef\":\"OSCA/222/ABC" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_02_Cef\":\"1850022/2020\"}],\n"
                + "\"i0_2_enhmcts\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"i0_2_enhmcts1\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"i0_2_enhmcts2\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot_schmcts\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot_schmcts1\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot_schmcts2\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Clerk\":\"Mike Jordan" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Today_date\":\"" + UtilHelper.formatCurrentDate(LocalDate.now()) + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"TodayPlus28Days\":\"" + UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 28)
                + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Case_No\":\"123456" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"submission_reference\":\"12212121" + CLOSING_QUOTE_COMMA_AND_NEWLINE
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
                + "\"accessKey\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"templateName\":\"EM-TRB-LET-ENG-00544.docx" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"outputName\":\"document.docx" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"data\":{\n"
                + "\"address_labels_page\":[\n"
                + "{\"Label_01_Entity_Name_01\":\"Claimant Name" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_01_Entity_Name_02\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_01_Address_Line_01\":\"11 Block A" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_01_Address_Line_02\":\"Ellesmere Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_01_Address_Line_03\":\"Address Line 3" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_01_Address_Line_04\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_01_Address_Line_05\":\"Lancashire M1 KJR" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_01_Telephone\":\"07577 136511" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_01_Fax\":\"07577 136712" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_01_Eef\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_01_Cef\":\"1850011/2020" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_02_Entity_Name_01\":\"Claimant Name" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_02_Entity_Name_02\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_02_Address_Line_01\":\"11 Block A" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_02_Address_Line_02\":\"Ellesmere Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_02_Address_Line_03\":\"Address Line 3" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_02_Address_Line_04\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_02_Address_Line_05\":\"Lancashire M1 KJR" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_02_Telephone\":\"07577 136511" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_02_Fax\":\"07577 136712" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_02_Eef\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_02_Cef\":\"1850011/2020" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_03_Entity_Name_01\":\"Claimant Rep" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_03_Entity_Name_02\":\"Claimant Org" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_03_Address_Line_01\":\"22 Block B" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_03_Address_Line_02\":\"Ellesmere Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_03_Address_Line_03\":\"Address Line 3" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_03_Address_Line_04\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_03_Address_Line_05\":\"Lancashire M2 KJR" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_03_Telephone\":\"07577 136521" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_03_Fax\":\"07577 136722" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_03_Eef\":\"OSCA/222/ABC" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_03_Cef\":\"1850022/2020" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_04_Entity_Name_01\":\"Claimant Rep" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_04_Entity_Name_02\":\"Claimant Org" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_04_Address_Line_01\":\"22 Block B" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_04_Address_Line_02\":\"Ellesmere Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_04_Address_Line_03\":\"Address Line 3" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_04_Address_Line_04\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_04_Address_Line_05\":\"Lancashire M2 KJR" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_04_Telephone\":\"07577 136521" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_04_Fax\":\"07577 136722" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_04_Eef\":\"OSCA/222/ABC" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_04_Cef\":\"1850022/2020" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_05_Entity_Name_01\":\"Respondent One" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_05_Entity_Name_02\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_05_Address_Line_01\":\"33 Block C" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_05_Address_Line_02\":\"Ellesmere Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_05_Address_Line_03\":\"Address Line 3" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_05_Address_Line_04\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_05_Address_Line_05\":\"Lancashire M3 KJR" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_05_Telephone\":\"07577 136531" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_05_Fax\":\"07577 136732" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_05_Eef\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_05_Cef\":\"1850033/2020" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_06_Entity_Name_01\":\"Respondent One" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_06_Entity_Name_02\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_06_Address_Line_01\":\"33 Block C" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_06_Address_Line_02\":\"Ellesmere Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_06_Address_Line_03\":\"Address Line 3" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_06_Address_Line_04\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_06_Address_Line_05\":\"Lancashire M3 KJR" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_06_Telephone\":\"07577 136531" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_06_Fax\":\"07577 136732" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_06_Eef\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_06_Cef\":\"1850033/2020" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_07_Entity_Name_01\":\"Respondent Two" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_07_Entity_Name_02\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_07_Address_Line_01\":\"44 Block D" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_07_Address_Line_02\":\"Ellesmere Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_07_Address_Line_03\":\"Address Line 3" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_07_Address_Line_04\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_07_Address_Line_05\":\"Lancashire M4 KJR" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_07_Telephone\":\"07577 136541" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_07_Fax\":\"07577 136742" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_07_Eef\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_07_Cef\":\"1850044/2020" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_08_Entity_Name_01\":\"Respondent Two" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_08_Entity_Name_02\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_08_Address_Line_01\":\"44 Block D" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_08_Address_Line_02\":\"Ellesmere Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_08_Address_Line_03\":\"Address Line 3" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_08_Address_Line_04\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_08_Address_Line_05\":\"Lancashire M4 KJR" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_08_Telephone\":\"07577 136541" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_08_Fax\":\"07577 136742" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_08_Eef\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_08_Cef\":\"1850044/2020" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_09_Entity_Name_01\":\"Respondent three" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_09_Entity_Name_02\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_09_Address_Line_01\":\"55 Block E" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_09_Address_Line_02\":\"Ellesmere Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_09_Address_Line_03\":\"Address Line 3" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_09_Address_Line_04\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_09_Address_Line_05\":\"Lancashire M5 KJR" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_09_Telephone\":\"07577 136551" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_09_Fax\":\"07577 136752" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_09_Eef\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_09_Cef\":\"1850055/2020" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_10_Entity_Name_01\":\"Respondent three" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_10_Entity_Name_02\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_10_Address_Line_01\":\"55 Block E" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_10_Address_Line_02\":\"Ellesmere Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_10_Address_Line_03\":\"Address Line 3" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_10_Address_Line_04\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_10_Address_Line_05\":\"Lancashire M5 KJR" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_10_Telephone\":\"07577 136551" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_10_Fax\":\"07577 136752" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_10_Eef\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_10_Cef\":\"1850055/2020" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_11_Entity_Name_01\":\"Respondent Rep" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_11_Entity_Name_02\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_11_Address_Line_01\":\"66 Block F" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_11_Address_Line_02\":\"Ellesmere Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_11_Address_Line_03\":\"Address Line 3" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_11_Address_Line_04\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_11_Address_Line_05\":\"Lancashire M6 KJR" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_11_Telephone\":\"07577 136561" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_11_Fax\":\"07577 136762" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_11_Eef\":\"OSCA/666/ABC" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_11_Cef\":\"1850066/2020" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_12_Entity_Name_01\":\"Respondent Rep" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_12_Entity_Name_02\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_12_Address_Line_01\":\"66 Block F" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_12_Address_Line_02\":\"Ellesmere Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_12_Address_Line_03\":\"Address Line 3" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_12_Address_Line_04\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_12_Address_Line_05\":\"Lancashire M6 KJR" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_12_Telephone\":\"07577 136561" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_12_Fax\":\"07577 136762" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_12_Eef\":\"OSCA/666/ABC" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_12_Cef\":\"1850066/2020\"}],\n"
                + "\"i0_2_enhmcts\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"i0_2_enhmcts1\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"i0_2_enhmcts2\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot_schmcts\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot_schmcts1\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot_schmcts2\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Clerk\":\"Mike Jordan" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Today_date\":\"" + UtilHelper.formatCurrentDate(LocalDate.now()) + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"TodayPlus28Days\":\"" + UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 28)
                + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Case_No\":\"123456" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"submission_reference\":\"12212121" + CLOSING_QUOTE_COMMA_AND_NEWLINE
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
                + "\"accessKey\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"templateName\":\"EM-TRB-LET-ENG-00544.docx" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"outputName\":\"document.docx" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"data\":{\n"
                + "\"address_labels_page\":[\n"
                + "{\"Label_13_Entity_Name_01\":\"Claimant Name" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_13_Entity_Name_02\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_13_Address_Line_01\":\"11 Block A" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_13_Address_Line_02\":\"Ellesmere Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_13_Address_Line_03\":\"Address Line 3" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_13_Address_Line_04\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_13_Address_Line_05\":\"Lancashire M1 KJR" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_13_Telephone\":\"07577 136511" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_13_Fax\":\"07577 136712" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_13_Eef\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_13_Cef\":\"1850011/2020" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_14_Entity_Name_01\":\"Claimant Name" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_14_Entity_Name_02\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_14_Address_Line_01\":\"11 Block A" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_14_Address_Line_02\":\"Ellesmere Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_14_Address_Line_03\":\"Address Line 3" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_14_Address_Line_04\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_14_Address_Line_05\":\"Lancashire M1 KJR" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_14_Telephone\":\"07577 136511" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_14_Fax\":\"07577 136712" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_14_Eef\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_14_Cef\":\"1850011/2020\"},\n"
                + "{\"Label_01_Entity_Name_01\":\"Claimant Name" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_01_Entity_Name_02\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_01_Address_Line_01\":\"11 Block A" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_01_Address_Line_02\":\"Ellesmere Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_01_Address_Line_03\":\"Address Line 3" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_01_Address_Line_04\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_01_Address_Line_05\":\"Lancashire M1 KJR" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_01_Telephone\":\"07577 136511" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_01_Fax\":\"07577 136712" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_01_Eef\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_01_Cef\":\"1850011/2020" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_02_Entity_Name_01\":\"Claimant Rep" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_02_Entity_Name_02\":\"Claimant Org" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_02_Address_Line_01\":\"22 Block B" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_02_Address_Line_02\":\"Ellesmere Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_02_Address_Line_03\":\"Address Line 3" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_02_Address_Line_04\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_02_Address_Line_05\":\"Lancashire M2 KJR" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_02_Telephone\":\"07577 136521" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_02_Fax\":\"07577 136722" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_02_Eef\":\"OSCA/222/ABC" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_02_Cef\":\"1850022/2020" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_03_Entity_Name_01\":\"Claimant Rep" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_03_Entity_Name_02\":\"Claimant Org" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_03_Address_Line_01\":\"22 Block B" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_03_Address_Line_02\":\"Ellesmere Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_03_Address_Line_03\":\"Address Line 3" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_03_Address_Line_04\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_03_Address_Line_05\":\"Lancashire M2 KJR" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_03_Telephone\":\"07577 136521" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_03_Fax\":\"07577 136722" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_03_Eef\":\"OSCA/222/ABC" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_03_Cef\":\"1850022/2020" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_04_Entity_Name_01\":\"Claimant Rep" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_04_Entity_Name_02\":\"Claimant Org" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_04_Address_Line_01\":\"22 Block B" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_04_Address_Line_02\":\"Ellesmere Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_04_Address_Line_03\":\"Address Line 3" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_04_Address_Line_04\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_04_Address_Line_05\":\"Lancashire M2 KJR" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_04_Telephone\":\"07577 136521" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_04_Fax\":\"07577 136722" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_04_Eef\":\"OSCA/222/ABC" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_04_Cef\":\"1850022/2020" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_05_Entity_Name_01\":\"Respondent One" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_05_Entity_Name_02\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_05_Address_Line_01\":\"33 Block C" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_05_Address_Line_02\":\"Ellesmere Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_05_Address_Line_03\":\"Address Line 3" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_05_Address_Line_04\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_05_Address_Line_05\":\"Lancashire M3 KJR" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_05_Telephone\":\"07577 136531" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_05_Fax\":\"07577 136732" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_05_Eef\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_05_Cef\":\"1850033/2020" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_06_Entity_Name_01\":\"Respondent One" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_06_Entity_Name_02\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_06_Address_Line_01\":\"33 Block C" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_06_Address_Line_02\":\"Ellesmere Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_06_Address_Line_03\":\"Address Line 3" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_06_Address_Line_04\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_06_Address_Line_05\":\"Lancashire M3 KJR" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_06_Telephone\":\"07577 136531" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_06_Fax\":\"07577 136732" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_06_Eef\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_06_Cef\":\"1850033/2020" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_07_Entity_Name_01\":\"Respondent One" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_07_Entity_Name_02\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_07_Address_Line_01\":\"33 Block C" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_07_Address_Line_02\":\"Ellesmere Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_07_Address_Line_03\":\"Address Line 3" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_07_Address_Line_04\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_07_Address_Line_05\":\"Lancashire M3 KJR" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_07_Telephone\":\"07577 136531" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_07_Fax\":\"07577 136732" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_07_Eef\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_07_Cef\":\"1850033/2020" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_08_Entity_Name_01\":\"Respondent Two" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_08_Entity_Name_02\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_08_Address_Line_01\":\"44 Block D" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_08_Address_Line_02\":\"Ellesmere Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_08_Address_Line_03\":\"Address Line 3" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_08_Address_Line_04\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_08_Address_Line_05\":\"Lancashire M4 KJR" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_08_Telephone\":\"07577 136541" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_08_Fax\":\"07577 136742" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_08_Eef\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_08_Cef\":\"1850044/2020" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_09_Entity_Name_01\":\"Respondent Two" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_09_Entity_Name_02\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_09_Address_Line_01\":\"44 Block D" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_09_Address_Line_02\":\"Ellesmere Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_09_Address_Line_03\":\"Address Line 3" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_09_Address_Line_04\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_09_Address_Line_05\":\"Lancashire M4 KJR" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_09_Telephone\":\"07577 136541" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_09_Fax\":\"07577 136742" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_09_Eef\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_09_Cef\":\"1850044/2020" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_10_Entity_Name_01\":\"Respondent Two" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_10_Entity_Name_02\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_10_Address_Line_01\":\"44 Block D" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_10_Address_Line_02\":\"Ellesmere Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_10_Address_Line_03\":\"Address Line 3" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_10_Address_Line_04\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_10_Address_Line_05\":\"Lancashire M4 KJR" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_10_Telephone\":\"07577 136541" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_10_Fax\":\"07577 136742" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_10_Eef\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_10_Cef\":\"1850044/2020" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_11_Entity_Name_01\":\"Respondent three" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_11_Entity_Name_02\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_11_Address_Line_01\":\"55 Block E" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_11_Address_Line_02\":\"Ellesmere Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_11_Address_Line_03\":\"Address Line 3" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_11_Address_Line_04\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_11_Address_Line_05\":\"Lancashire M5 KJR" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_11_Telephone\":\"07577 136551" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_11_Fax\":\"07577 136752" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_11_Eef\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_11_Cef\":\"1850055/2020" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_12_Entity_Name_01\":\"Respondent three" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_12_Entity_Name_02\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_12_Address_Line_01\":\"55 Block E" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_12_Address_Line_02\":\"Ellesmere Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_12_Address_Line_03\":\"Address Line 3" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_12_Address_Line_04\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_12_Address_Line_05\":\"Lancashire M5 KJR" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_12_Telephone\":\"07577 136551" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_12_Fax\":\"07577 136752" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_12_Eef\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_12_Cef\":\"1850055/2020" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_13_Entity_Name_01\":\"Respondent three" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_13_Entity_Name_02\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_13_Address_Line_01\":\"55 Block E" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_13_Address_Line_02\":\"Ellesmere Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_13_Address_Line_03\":\"Address Line 3" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_13_Address_Line_04\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_13_Address_Line_05\":\"Lancashire M5 KJR" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_13_Telephone\":\"07577 136551" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_13_Fax\":\"07577 136752" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_13_Eef\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_13_Cef\":\"1850055/2020" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_14_Entity_Name_01\":\"Respondent Rep" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_14_Entity_Name_02\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_14_Address_Line_01\":\"66 Block F" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_14_Address_Line_02\":\"Ellesmere Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_14_Address_Line_03\":\"Address Line 3" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_14_Address_Line_04\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_14_Address_Line_05\":\"Lancashire M6 KJR" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_14_Telephone\":\"07577 136561" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_14_Fax\":\"07577 136762" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_14_Eef\":\"OSCA/666/ABC" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_14_Cef\":\"1850066/2020\"},\n"
                + "{\"Label_01_Entity_Name_01\":\"Respondent Rep" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_01_Entity_Name_02\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_01_Address_Line_01\":\"66 Block F" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_01_Address_Line_02\":\"Ellesmere Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_01_Address_Line_03\":\"Address Line 3" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_01_Address_Line_04\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_01_Address_Line_05\":\"Lancashire M6 KJR" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_01_Telephone\":\"07577 136561" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_01_Fax\":\"07577 136762" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_01_Eef\":\"OSCA/666/ABC" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_01_Cef\":\"1850066/2020" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_02_Entity_Name_01\":\"Respondent Rep" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_02_Entity_Name_02\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_02_Address_Line_01\":\"66 Block F" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_02_Address_Line_02\":\"Ellesmere Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_02_Address_Line_03\":\"Address Line 3" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_02_Address_Line_04\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_02_Address_Line_05\":\"Lancashire M6 KJR" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_02_Telephone\":\"07577 136561" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_02_Fax\":\"07577 136762" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_02_Eef\":\"OSCA/666/ABC" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_02_Cef\":\"1850066/2020\"}],\n"
                + "\"i0_2_enhmcts\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"i0_2_enhmcts1\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"i0_2_enhmcts2\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot_schmcts\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot_schmcts1\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot_schmcts2\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Clerk\":\"Mike Jordan" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Today_date\":\"" + UtilHelper.formatCurrentDate(LocalDate.now()) + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"TodayPlus28Days\":\"" + UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 28)
                + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Case_No\":\"123456" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"submission_reference\":\"12212121" + CLOSING_QUOTE_COMMA_AND_NEWLINE
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
                + "\"accessKey\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"templateName\":\"EM-TRB-EGW-ENG-00043.docx" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"outputName\":\"document.docx" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"data\":{\n"
                + "\"claimant_or_rep_full_name\":\"RepresentativeNameClaimant" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_rep_organisation\":\"RepresentativeOrganisation" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_addressLine1\":\"56 Block C" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_addressLine2\":\"Ellesmere Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_town\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_county\":\"Lancashire" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_postCode\":\"M3 KJR" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_reference\":\"1111111" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_full_name\":\"Mr A J Rodriguez" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Claimant\":\"Mr A J Rodriguez" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_addressLine1\":\"34" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_addressLine2\":\"Low Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_town\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_county\":\"Lancashire" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_postCode\":\"M3 6gw" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_full_name\":\"Antonio Vazquez" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_addressLine1\":\"11 Small Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_addressLine2\":\"22 House" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_town\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_county\":\"North West" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_postCode\":\"M12 42R" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_full_name\":\"Antonio Vazquez" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_addressLine1\":\"11 Small Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_addressLine2\":\"22 House" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_town\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_county\":\"North West" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_postCode\":\"M12 42R" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Respondent\":\"1. Antonio Vazquez" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"resp_others\":\"2. Juan Garcia\\n3. Mike Jordan" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"resp_address\":\"1. 11 Small Street, 22 House, Manchester, North West, M12 42R, "
                + "UK\\n2. 12 Small Street, 24 House, Manchester, North West, M12 4ED, UK\\n3. 11 Small Street, "
                + "22 House, Manchester, North West, M12 42R, UK" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_date\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_date_time\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_time\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_venue\":\"Manchester Employment Tribunals, "
                + "Alexandra House, 14-22 The Parsonage, Manchester, M3 2JA" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_duration\":\"3 days" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"t1_2\":\"true" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_addressLine1\":\"Manchester Employment Tribunal," + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_addressLine2\":\"Alexandra House," + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_addressLine3\":\"14-22 The Parsonage," + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_town\":\"Manchester," + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_county\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_postCode\":\"M3 2JA" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_telephone\":\"03577131270" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_fax\":\"07577126570" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_DX\":\"123456" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_Email\":\"ManchesterOfficeET@hmcts.gov.uk" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"i1_2_enhmcts\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"i1_2_enhmcts1\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"i1_2_enhmcts2\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot_schmcts\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot_schmcts1\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot_schmcts2\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Clerk\":\"Mike Jordan" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Today_date\":\"" + UtilHelper.formatCurrentDate(LocalDate.now()) + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"TodayPlus28Days\":\"" + UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 28)
                + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Case_No\":\"123456" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"submission_reference\":\"12212121" + CLOSING_QUOTE_COMMA_AND_NEWLINE
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
                + "\"accessKey\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"templateName\":\".docx" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"outputName\":\"document.docx" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"data\":{\n"
                + "\"claimant_or_rep_full_name\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_full_name\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Claimant\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_rep_organisation\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_addressLine1\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_addressLine2\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_town\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_county\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_postCode\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_addressLine1\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_addressLine2\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_town\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_county\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_postCode\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_full_name\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_rep_organisation\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_addressLine1\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_addressLine2\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_town\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_county\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_postCode\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_full_name\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_addressLine1\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_addressLine2\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_town\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_county\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_postCode\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Respondent\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"resp_others\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"resp_address\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_date\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_date_time\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_venue\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_duration\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_time\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_telephone\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_fax\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_DX\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_Email\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"i_enhmcts\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"i_enhmcts1\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"i_enhmcts2\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot_schmcts\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot_schmcts1\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot_schmcts2\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Clerk\":\"Mike Jordan" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Today_date\":\"" + UtilHelper.formatCurrentDate(LocalDate.now()) + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"TodayPlus28Days\":\"" + UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 28)
                + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Case_No\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"submission_reference\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
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
                + "\"accessKey\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"templateName\":\"EM-TRB-SCO-ENG-00042.docx" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"outputName\":\"document.docx" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"data\":{\n"
                + "\"claimant_or_rep_full_name\":\"RepresentativeNameClaimant" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_rep_organisation\":\"RepresentativeOrganisation" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_addressLine1\":\"56 Block C" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_addressLine2\":\"Ellesmere Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_town\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_county\":\"Lancashire" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_postCode\":\"M3 KJR" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_reference\":\"1111111" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_full_name\":\"Mr A J Rodriguez" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Claimant\":\"Mr A J Rodriguez" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_addressLine1\":\"34" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_addressLine2\":\"Low Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_town\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_county\":\"Lancashire" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_postCode\":\"M3 6gw" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_full_name\":\"Antonio Vazquez" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_addressLine1\":\"11 Small Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_addressLine2\":\"22 House" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_town\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_county\":\"North West" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_postCode\":\"M12 42R" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_full_name\":\"Antonio Vazquez" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_addressLine1\":\"11 Small Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_addressLine2\":\"22 House" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_town\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_county\":\"North West" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_postCode\":\"M12 42R" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Respondent\":\"1. Antonio Vazquez" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"resp_others\":\"2. Roberto Dondini" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"resp_address\":\"1. 11 Small Street, 22 House, Manchester, North West, M12 42R, UK\\n2. 13 Small"
                + " Street, 26 House, Scotland, North West, SC13 4ED, UK" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_date\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_date_time\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_time\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_venue\":\"Glasgow Tribunal Centre, Atlantic Quay, 20 York Street, Glasgow, G2 8GT"
                + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_duration\":\"2 days" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"t_Scot_7_1\":\"true" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_addressLine1\":\"Eagle Building," + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_addressLine2\":\"215 Bothwell Street," + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_town\":\"Glasgow," + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_county\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_postCode\":\"G2 7TS" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_telephone\":\"03577123270" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_fax\":\"07127126570" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_DX\":\"1234567" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_Email\":\"GlasgowOfficeET@hmcts.gov.uk" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"i_enhmcts\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"i_enhmcts1\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"i_enhmcts2\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot7_1_schmcts\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot7_1_schmcts1\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot7_1_schmcts2\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Clerk\":\"Mike Jordan" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Today_date\":\"" + UtilHelper.formatCurrentDate(LocalDate.now()) + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"TodayPlus28Days\":\"" + UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 28)
                + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Case_No\":\"123456" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"submission_reference\":\"12212121" + CLOSING_QUOTE_COMMA_AND_NEWLINE
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
                + "\"accessKey\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"templateName\":\"EM-TRB-SCO-ENG-00043.docx" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"outputName\":\"document.docx" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"data\":{\n"
                + "\"claimant_or_rep_full_name\":\"Orlando LTD" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_full_name\":\"Orlando LTD" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Claimant\":\"Orlando LTD" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_addressLine1\":\"34" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_addressLine2\":\"Low Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_town\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_county\":\"Lancashire" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_postCode\":\"M3 6gw" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_addressLine1\":\"34" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_addressLine2\":\"Low Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_town\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_county\":\"Lancashire" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_postCode\":\"M3 6gw" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_full_name\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_rep_organisation\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_addressLine1\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_addressLine2\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_town\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_county\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_postCode\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_full_name\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_addressLine1\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_addressLine2\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_town\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_county\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_postCode\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Respondent\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"resp_others\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"resp_address\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_date\":\"25 November 2019" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_date_time\":\"25 November 2019 at 12:11" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_time\":\"12:11" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_venue\":\"Glasgow Tribunal Centre, Atlantic Quay, 20 York Street, Glasgow, G2 8GT"
                + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_duration\":\"2 days" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"t_Scot_24\":\"true" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_addressLine1\":\"Eagle Building," + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_addressLine2\":\"215 Bothwell Street," + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_town\":\"Glasgow," + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_county\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_postCode\":\"G2 7TS" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_telephone\":\"03577123270" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_fax\":\"07127126570" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_DX\":\"1234567" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_Email\":\"GlasgowOfficeET@hmcts.gov.uk" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"i_enhmcts\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"i_enhmcts1\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"i_enhmcts2\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot24_schmcts\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot24_schmcts1\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot24_schmcts2\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Clerk\":\"Mike Jordan" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Today_date\":\"" + UtilHelper.formatCurrentDate(LocalDate.now()) + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"TodayPlus28Days\":\"" + UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 28)
                + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Case_No\":\"123456" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"submission_reference\":\"12212121" + CLOSING_QUOTE_COMMA_AND_NEWLINE
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
                + "\"accessKey\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"templateName\":\"EM-TRB-SCO-ENG-00044.docx" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"outputName\":\"document.docx" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"data\":{\n"
                + "\"claimant_or_rep_full_name\":\"Mr A J Rodriguez" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_full_name\":\"Mr A J Rodriguez" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Claimant\":\"Mr A J Rodriguez" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_addressLine1\":\"34" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_addressLine2\":\"Low Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_town\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_county\":\"Lancashire" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_postCode\":\"M3 6gw" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_addressLine1\":\"34" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_addressLine2\":\"Low Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_town\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_county\":\"Lancashire" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_postCode\":\"M3 6gw" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_full_name\":\"Antonio Vazquez" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_addressLine1\":\"11 Small Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_addressLine2\":\"22 House" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_town\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_county\":\"North West" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_postCode\":\"M12 42R" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_full_name\":\"Antonio Vazquez" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_addressLine1\":\"11 Small Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_addressLine2\":\"22 House" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_town\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_county\":\"North West" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_postCode\":\"M12 42R" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Respondent\":\"Antonio Vazquez" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"resp_others\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"resp_address\":\"11 Small Street, 22 House, Manchester, North West, M12 42R, UK"
                + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_date\":\"1 November 2019" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_date_time\":\"1 November 2019 at 12:11" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_time\":\"12:11" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_venue\":\"Ground Floor, AB1, 48 Huntly Street, Aberdeen, AB10 1SH"
                + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_duration\":\"1 day" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"t_Scot_34\":\"true" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_addressLine1\":\"Eagle Building," + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_addressLine2\":\"215 Bothwell Street," + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_town\":\"Glasgow," + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_county\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_postCode\":\"G2 7TS" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_telephone\":\"03577123270" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_fax\":\"07127126570" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_DX\":\"1234567" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_Email\":\"GlasgowOfficeET@hmcts.gov.uk" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"i_enhmcts\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"i_enhmcts1\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"i_enhmcts2\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot34_schmcts\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot34_schmcts1\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot34_schmcts2\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Clerk\":\"Mike Jordan" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Today_date\":\"" + UtilHelper.formatCurrentDate(LocalDate.now()) + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"TodayPlus28Days\":\"" + UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 28)
                + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Case_No\":\"123456" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"submission_reference\":\"12212121" + CLOSING_QUOTE_COMMA_AND_NEWLINE
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
                + "\"accessKey\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"templateName\":\"EM-TRB-SCO-ENG-00044.docx" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"outputName\":\"document.docx" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"data\":{\n"
                + "\"claimant_or_rep_full_name\":\"Mr A J Rodriguez" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_full_name\":\"Mr A J Rodriguez" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Claimant\":\"Mr A J Rodriguez" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_addressLine1\":\"34" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_addressLine2\":\"Low Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_town\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_county\":\"Lancashire" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_postCode\":\"M3 6gw" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_addressLine1\":\"34" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_addressLine2\":\"Low Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_town\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_county\":\"Lancashire" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_postCode\":\"M3 6gw" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_full_name\":\"Antonio Vazquez" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_addressLine1\":\"11 Small Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_addressLine2\":\"22 House" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_town\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_county\":\"North West" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_postCode\":\"M12 42R" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_full_name\":\"Antonio Vazquez" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_addressLine1\":\"11 Small Street" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_addressLine2\":\"22 House" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_town\":\"Manchester" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_county\":\"North West" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_postCode\":\"M12 42R" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Respondent\":\"Antonio Vazquez" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"resp_others\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"resp_address\":\"11 Small Street, 22 House, Manchester, North West, M12 42R, UK"
                + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_date\":\"1 November 2019" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_date_time\":\"1 November 2019 at 12:11" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_time\":\"12:11" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_venue\":\"Ground Floor, AB1, 48 Huntly Street, Aberdeen, AB10 1SH"
                + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_duration\":\"1 day" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"t_Scot_34\":\"true" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_addressLine1\":\"Aberdeen Address Line1" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_addressLine2\":\"Aberdeen Address Line2" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_town\":\"Aberdeen" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_county\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_postCode\":\"BA 3453" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_telephone\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_fax\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_DX\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_Email\":\"aberdeen@gmail.com" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"i_enhmcts\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"i_enhmcts1\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"i_enhmcts2\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot34_schmcts\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot34_schmcts1\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot34_schmcts2\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Clerk\":\"Mike Jordan" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Today_date\":\"" + UtilHelper.formatCurrentDate(LocalDate.now()) + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"TodayPlus28Days\":\"" + UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 28)
                + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Case_No\":\"123456" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"submission_reference\":\"12212121" + CLOSING_QUOTE_COMMA_AND_NEWLINE
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
                + "\"accessKey\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"templateName\":\"Part_18.docx" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"outputName\":\"document.docx" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"data\":{\n"
                + "\"claimant_or_rep_full_name\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_full_name\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Claimant\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_rep_organisation\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_addressLine1\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_addressLine2\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_town\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_county\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_postCode\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_addressLine1\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_addressLine2\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_town\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_county\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_postCode\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_full_name\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_rep_organisation\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_addressLine1\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_addressLine2\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_town\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_county\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_postCode\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_full_name\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_addressLine1\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_addressLine2\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_town\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_county\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_postCode\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Respondent\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"resp_others\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"resp_address\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_date\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_date_time\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_venue\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_duration\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_time\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"t18A\":\"true" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_telephone\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_fax\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_DX\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_Email\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"i18A_enhmcts\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"i18A_enhmcts1\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"i18A_enhmcts2\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot_schmcts\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot_schmcts1\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot_schmcts2\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Clerk\":\"Mike Jordan" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Today_date\":\"" + UtilHelper.formatCurrentDate(LocalDate.now()) + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"TodayPlus28Days\":\"" + UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 28)
                + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Case_No\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"submission_reference\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
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
                + "\"accessKey\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"templateName\":\"" + topLevel + ".docx" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"outputName\":\"document.docx" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"data\":{\n"
                + "\"claimant_or_rep_full_name\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_full_name\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Claimant\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_rep_organisation\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_addressLine1\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_addressLine2\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_town\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_county\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_or_rep_postCode\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_addressLine1\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_addressLine2\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_town\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_county\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"claimant_postCode\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_full_name\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_rep_organisation\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_addressLine1\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_addressLine2\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_town\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_county\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_or_rep_postCode\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_full_name\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_addressLine1\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_addressLine2\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_addressLine3\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_town\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_county\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"respondent_postCode\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Respondent\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"resp_others\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"resp_address\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_date\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_date_time\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_venue\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_duration\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Hearing_time\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"t_Scot_" + part + "\":\"true" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_telephone\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_fax\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_DX\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Court_Email\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"i_enhmcts\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"i_enhmcts1\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"i_enhmcts2\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot" + part + "_schmcts\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot" + part + "_schmcts1\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot" + part + "_schmcts2\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Clerk\":\"Mike Jordan" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Today_date\":\"" + UtilHelper.formatCurrentDate(LocalDate.now()) + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"TodayPlus28Days\":\"" + UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 28)
                + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Case_No\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"submission_reference\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
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
                + "\"accessKey\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"templateName\":\"EM-TRB-LET-ENG-00544.docx" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"outputName\":\"document.docx" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"data\":{\n"
                + "\"address_labels_page\":[\n"
                + "{\"Label_02_Entity_Name_01\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_02_Entity_Name_02\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_02_Eef\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_02_Cef\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_03_Entity_Name_01\":\"Label Entity1 Name" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_03_Entity_Name_02\":\"Label Entity2 Name" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_03_Address_Line_01\":\"Address Line1" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_03_Address_Line_02\":\"Address Line2" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Label_03_Address_Line_03\":\"M2 45GD" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_03_Eef\":\"" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"lbl_03_Cef\":\"Reference01345\"}],\n"
                + "\"i_enhmcts\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"i_enhmcts1\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"i_enhmcts2\":\"[userImage:enhmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot_schmcts\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot_schmcts1\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"iScot_schmcts2\":\"[userImage:schmcts.png]" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Clerk\":\"Mike Jordan" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Today_date\":\"" + UtilHelper.formatCurrentDate(LocalDate.now()) 
                + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"TodayPlus28Days\":\"" + UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 28)
                + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"Case_No\":\"123456" + CLOSING_QUOTE_COMMA_AND_NEWLINE
                + "\"submission_reference\":\"12212121" + CLOSING_QUOTE_COMMA_AND_NEWLINE
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

    @Nested
    class SetLegalRepVisibleDocuments {
        @Test
        void hidesDocuments() {
            CaseData caseData = new CaseData();

            caseData.setDocumentCollection(List.of(
                getDocumentTypeItem("Visible", "ET1"),
                getDocumentTypeItem("Hidden", "ET1 Vetting"),
                getDocumentTypeItem("Hidden", "ET3 Processing"),
                getDocumentTypeItem("Hidden", "Initial Consideration"),
                getDocumentTypeItem("Hidden", "App for a Witness Order - C"),
                getDocumentTypeItem("Hidden", "Referral/Judicial direction"),
                getDocumentTypeItem("Hidden", "Referral/Judicial Direction"),
                getDocumentTypeItem("Hidden", "COT3"),
                getDocumentTypeItem("Hidden", "Other"),
                getDocumentTypeItem("Hidden", "Contact the tribunal about something else - C"),
                getDocumentTypeItem("Hidden", "Tribunal case file"),
                getDocumentTypeItem("Hidden", "Rejection of Claim"),
                getDocumentTypeItem("Hidden", "Claim rejected"),
                getDocumentTypeItem("Visible", null),
                getDocumentTypeItem("Hidden", "App to amend response"),
                getDocumentTypeItem("Hidden", "App to vary or revoke an order - C"),
                getDocumentTypeItem("Hidden", "Withdrawal of all or part of claim")
                )
            );

            DocumentHelper.setLegalRepVisibleDocuments(caseData);

            List<DocumentTypeItem> legalRepDocuments = caseData.getLegalrepDocumentCollection();

            assertThat(legalRepDocuments).hasSize(2);
            assertThat(legalRepDocuments.get(0).getValue().getUploadedDocument().getDocumentFilename())
                .isEqualTo("Visible");
            assertThat(legalRepDocuments.get(1).getValue().getUploadedDocument().getDocumentFilename())
                .isEqualTo("Visible");
        }

        @Test
        void hidesClaimantRule92NoDocuments() {
            DocumentTypeItem doc1 = getDocumentTypeItem("Tse submit support material", "Claimant correspondence");
            DocumentTypeItem doc2 = getDocumentTypeItem("Tse response document 1", "Claimant correspondence");
            DocumentTypeItem doc3 = getDocumentTypeItem("Tse response document 2", "Claimant correspondence");
            CaseData caseData = new CaseData();
            caseData.setDocumentCollection(List.of(doc1, doc2, doc3));

            GenericTseApplicationTypeItem claimantApp = GenericTseApplicationTypeItem.builder()
                .value(GenericTseApplicationType.builder()
                    .applicant(CLAIMANT_TITLE)
                    .copyToOtherPartyYesOrNo(NO)
                    .documentUpload(doc1.getValue().getUploadedDocument())
                    .build())
                .build();

            List<TseRespondTypeItem> tseRespondTypeItems = List.of(
                TseRespondTypeItem.builder()
                    .value(TseRespondType.builder()
                        .from(CLAIMANT_TITLE)
                        .copyToOtherParty(NO)
                        .supportingMaterial(List.of(doc2, doc3))
                        .build())
                .build()
            );

            GenericTseApplicationTypeItem claimantResponses = GenericTseApplicationTypeItem.builder()
                .value(GenericTseApplicationType.builder()
                    .documentUpload(doc1.getValue().getUploadedDocument())
                    .respondCollection(tseRespondTypeItems)
                    .build())
                .build();

            caseData.setGenericTseApplicationCollection(List.of(claimantApp, claimantResponses));

            DocumentHelper.setLegalRepVisibleDocuments(caseData);

            assertThat(caseData.getLegalrepDocumentCollection()).isEmpty();
        }
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
