package uk.gov.hmcts.ethos.replacement.docmosis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.joda.time.LocalDate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestClientResponseException;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.exceptions.CaseCreationException;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.EccCounterClaimTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.CasePreAcceptType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantIndType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantType;
import uk.gov.hmcts.et.common.model.ccd.types.DateListedType;
import uk.gov.hmcts.et.common.model.ccd.types.EccCounterClaimType;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.FlagsImageHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.InternalException;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ABOUT_TO_SUBMIT_EVENT_CALLBACK;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ET3_DUE_DATE_FROM_SERVING_DATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.FLAG_ECC;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_LISTED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MID_EVENT_CALLBACK;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SINGLE_CASE_TYPE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SUBMITTED_CALLBACK;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementForCaseWorkerService.LISTED_DATE_ON_WEEKEND_MESSAGE;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.InternalException.ERROR_MESSAGE;

@SuppressWarnings({"PMD.LawOfDemeter", "PMD.NcssCount", "PMD.AvoidInstantiatingObjectsInLoops",
    "PMD.UseProperClassLoader", "PMD.TooManyMethods", "PMD.ExcessiveImports", "PMD.ExcessivePublicCount",
                   "PMD.TooManyFields", "PMD.CyclomaticComplexity"})
@ExtendWith(SpringExtension.class)
class CaseManagementForCaseWorkerServiceTest {

    private static final String AUTH_TOKEN = "Bearer eyJhbGJbpjciOiJIUzI1NiJ9";
    public static final String UNASSIGNED_OFFICE = "Unassigned";

    @InjectMocks
    private CaseManagementForCaseWorkerService caseManagementForCaseWorkerService;
    private CCDRequest scotlandCcdRequest1;
    private CCDRequest scotlandCcdRequest2;
    private CCDRequest scotlandCcdRequest3;
    private CCDRequest scotlandCcdRequest5;
    private CCDRequest ccdRequest10;
    private CCDRequest ccdRequest11;
    private CCDRequest ccdRequest12;
    private CCDRequest ccdRequest13;
    private CCDRequest ccdRequest14;
    private CCDRequest ccdRequest15;
    private CCDRequest ccdRequest21;
    private CCDRequest manchesterCcdRequest;
    private SubmitEvent submitEvent;

    @MockBean
    private CaseRetrievalForCaseWorkerService caseRetrievalForCaseWorkerService;
    @MockBean
    private CcdClient ccdClient;
    @MockBean
    private ClerkService clerkService;
    @MockBean
    private AuthTokenGenerator serviceAuthTokenGenerator;
    private String hmctsServiceId = "BHA1";

    @BeforeEach
    void setUp() throws Exception {
        scotlandCcdRequest1 = new CCDRequest();
        CaseDetails caseDetailsScot1 = generateCaseDetails("caseDetailsScotTest1.json");
        scotlandCcdRequest1.setCaseDetails(caseDetailsScot1);

        scotlandCcdRequest2 = new CCDRequest();
        CaseDetails caseDetailsScot2 = generateCaseDetails("caseDetailsScotTest2.json");
        scotlandCcdRequest2.setCaseDetails(caseDetailsScot2);

        scotlandCcdRequest3 = new CCDRequest();
        CaseDetails caseDetailsScot3 = generateCaseDetails("caseDetailsScotTest3.json");
        scotlandCcdRequest3.setCaseDetails(caseDetailsScot3);

        scotlandCcdRequest5 = new CCDRequest();
        CaseDetails caseDetailsScot5 = generateCaseDetails("caseDetailsScotTest5.json");
        scotlandCcdRequest5.setCaseDetails(caseDetailsScot5);

        ccdRequest10 = new CCDRequest();
        CaseDetails caseDetails10 = generateCaseDetails("caseDetailsTest10.json");
        ccdRequest10.setCaseDetails(caseDetails10);

        ccdRequest11 = new CCDRequest();
        CaseDetails caseDetails11 = generateCaseDetails("caseDetailsTest11.json");
        ccdRequest11.setCaseDetails(caseDetails11);

        ccdRequest12 = new CCDRequest();
        CaseDetails caseDetails12 = generateCaseDetails("caseDetailsTest12.json");
        ccdRequest12.setCaseDetails(caseDetails12);

        ccdRequest13 = new CCDRequest();
        CaseDetails caseDetails13 = generateCaseDetails("caseDetailsTest13.json");
        ccdRequest13.setCaseDetails(caseDetails13);

        ccdRequest14 = new CCDRequest();
        CaseDetails caseDetails14 = generateCaseDetails("caseDetailsTest14.json");
        ccdRequest14.setCaseDetails(caseDetails14);

        ccdRequest15 = new CCDRequest();
        CaseDetails caseDetails15 = generateCaseDetails("caseDetailsTest15.json");
        ccdRequest15.setCaseDetails(caseDetails15);

        ccdRequest21 = new CCDRequest();
        CaseDetails caseDetails21 = generateCaseDetails("caseDetailsTest21.json");
        ccdRequest21.setCaseDetails(caseDetails21);

        manchesterCcdRequest = new CCDRequest();
        CaseData caseData = new CaseData();
        CasePreAcceptType casePreAcceptType = new CasePreAcceptType();
        casePreAcceptType.setCaseAccepted(YES);
        caseData.setPreAcceptCase(casePreAcceptType);
        caseData.setCaseRefECC("11111");

        EccCounterClaimTypeItem eccCounterClaimTypeItem = new EccCounterClaimTypeItem();
        EccCounterClaimType counterClaimType = new EccCounterClaimType();
        counterClaimType.setCounterClaim("72632632");
        eccCounterClaimTypeItem.setId(UUID.randomUUID().toString());
        eccCounterClaimTypeItem.setValue(counterClaimType);
        CaseDetails manchesterCaseDetails = new CaseDetails();
        caseData.setEccCases(List.of(eccCounterClaimTypeItem));
        caseData.setRespondentECC(createRespondentECC());
        manchesterCaseDetails.setCaseData(caseData);
        manchesterCaseDetails.setCaseId("123456");
        manchesterCaseDetails.setCaseTypeId(ENGLANDWALES_CASE_TYPE_ID);
        manchesterCaseDetails.setJurisdiction("TRIBUNALS");
        manchesterCcdRequest.setCaseDetails(manchesterCaseDetails);

        submitEvent = new SubmitEvent();
        CaseData submitCaseData = new CaseData();
        submitCaseData.setRespondentCollection(createRespondentCollection(true));
        submitCaseData.setClaimantIndType(createClaimantIndType());
        submitCaseData.setRepresentativeClaimantType(createRepresentedTypeC());
        submitCaseData.setRepCollection(createRepCollection(false));
        submitCaseData.setClaimantRepresentedQuestion(YES);
        Address address = new Address();
        address.setAddressLine1("AddressLine1");
        address.setAddressLine2("AddressLine2");
        address.setAddressLine3("AddressLine3");
        address.setPostTown("Manchester");
        address.setCountry("UK");
        address.setPostCode("L1 122");
        ClaimantType claimantType = new ClaimantType();
        claimantType.setClaimantAddressUK(address);
        submitCaseData.setClaimantType(claimantType);
        submitEvent.setState("Accepted");
        submitEvent.setCaseId(123);
        submitEvent.setCaseData(submitCaseData);

        caseManagementForCaseWorkerService = new CaseManagementForCaseWorkerService(
                caseRetrievalForCaseWorkerService, ccdClient, clerkService, serviceAuthTokenGenerator,
                hmctsServiceId);
    }

    @Test
    void caseDataDefaultsClaimantIndividual() {
        CaseData caseData = scotlandCcdRequest1.getCaseDetails().getCaseData();
        caseManagementForCaseWorkerService.caseDataDefaults(caseData);
        assertEquals("Anton Juliet Rodriguez", caseData.getClaimant());
    }

    @Test
    void caseDataDefaultsResponseReceived() {
        CaseData caseData = scotlandCcdRequest1.getCaseDetails().getCaseData();
        caseManagementForCaseWorkerService.caseDataDefaults(caseData);
        for (RespondentSumTypeItem respondentSumTypeItem : caseData.getRespondentCollection()) {
            assertEquals(NO, respondentSumTypeItem.getValue().getResponseReceived());
        }
    }

    @Test
    void caseDataDefaultsResetResponseRespondentAddress() {
        CaseData caseData = scotlandCcdRequest1.getCaseDetails().getCaseData();
        for (RespondentSumTypeItem respondentSumTypeItem : caseData.getRespondentCollection()) {
            respondentSumTypeItem.getValue().setResponseReceived(null);
            respondentSumTypeItem.getValue().setResponseRespondentAddress(new Address());
            respondentSumTypeItem.getValue().getResponseRespondentAddress().setAddressLine1("Address1");
            respondentSumTypeItem.getValue().getResponseRespondentAddress().setAddressLine2("Address2");
            respondentSumTypeItem.getValue().getResponseRespondentAddress().setAddressLine3("Address3");
            respondentSumTypeItem.getValue().getResponseRespondentAddress().setCounty("County");
            respondentSumTypeItem.getValue().getResponseRespondentAddress().setPostTown("PostTown");
            respondentSumTypeItem.getValue().getResponseRespondentAddress().setCountry("Country");
            respondentSumTypeItem.getValue().getResponseRespondentAddress().setPostCode("PostCode");

        }
        caseManagementForCaseWorkerService.caseDataDefaults(caseData);
        for (RespondentSumTypeItem respondentSumTypeItem : caseData.getRespondentCollection()) {
            assertEquals("", respondentSumTypeItem.getValue().getResponseRespondentAddress().getAddressLine1());
            assertEquals("", respondentSumTypeItem.getValue().getResponseRespondentAddress().getAddressLine2());
            assertEquals("", respondentSumTypeItem.getValue().getResponseRespondentAddress().getAddressLine3());
            assertEquals("", respondentSumTypeItem.getValue().getResponseRespondentAddress().getCountry());
            assertEquals("", respondentSumTypeItem.getValue().getResponseRespondentAddress().getCounty());
            assertEquals("", respondentSumTypeItem.getValue().getResponseRespondentAddress().getPostCode());
            assertEquals("", respondentSumTypeItem.getValue().getResponseRespondentAddress().getPostTown());
        }
    }

    @Test
    void caseDataDefaultsResponseReceivedDoesNotChange() {
        CaseData caseData = scotlandCcdRequest1.getCaseDetails().getCaseData();
        caseData.getRespondentCollection().get(0).getValue().setResponseReceived(YES);
        caseManagementForCaseWorkerService.caseDataDefaults(caseData);
        assertEquals(YES, caseData.getRespondentCollection().get(0).getValue().getResponseReceived());
        for (RespondentSumTypeItem respondentSumTypeItem : caseData.getRespondentCollection()) {
            if (!respondentSumTypeItem.equals(caseData.getRespondentCollection().get(0))) {
                assertEquals(NO, respondentSumTypeItem.getValue().getResponseReceived());
            }
        }
    }

    @Test
    void caseDataDefaultsClaimantCompany() {
        CaseData caseData = scotlandCcdRequest2.getCaseDetails().getCaseData();
        caseManagementForCaseWorkerService.caseDataDefaults(caseData);
        assertEquals("Orlando LTD", caseData.getClaimant());
    }

    @Test
    void caseDataDefaultsClaimantMissing() {
        CaseData caseData = scotlandCcdRequest2.getCaseDetails().getCaseData();
        caseData.setClaimantTypeOfClaimant(null);
        caseManagementForCaseWorkerService.caseDataDefaults(caseData);
        assertEquals("Missing claimant", caseData.getClaimant());
    }

    @Test
    void caseDataDefaultsRespondentAvailable() {
        CaseData caseData = scotlandCcdRequest1.getCaseDetails().getCaseData();
        caseManagementForCaseWorkerService.caseDataDefaults(caseData);
        assertEquals("Antonio Vazquez", caseData.getRespondent());
    }

    @Test
    void caseDataDefaultsRespondentMissing() {
        CaseData caseData = scotlandCcdRequest2.getCaseDetails().getCaseData();
        caseManagementForCaseWorkerService.caseDataDefaults(caseData);
        assertEquals("Missing respondent", caseData.getRespondent());
    }

    @Test
    void caseDataDefaultsStruckOutYESandNulltoNO() {
        CaseData caseData = scotlandCcdRequest1.getCaseDetails().getCaseData();

        caseManagementForCaseWorkerService.caseDataDefaults(caseData);

        assertEquals(3, caseData.getRespondentCollection().size());

        assertEquals("Antonio Vazquez", caseData.getRespondentCollection().get(0).getValue().getRespondentName());
        assertEquals(NO, caseData.getRespondentCollection().get(0).getValue().getResponseStruckOut());
        assertEquals("Juan Garcia", caseData.getRespondentCollection().get(1).getValue().getRespondentName());
        assertEquals(YES, caseData.getRespondentCollection().get(1).getValue().getResponseStruckOut());
        assertEquals("Roberto Dondini", caseData.getRespondentCollection().get(2).getValue().getRespondentName());
        assertEquals(NO, caseData.getRespondentCollection().get(2).getValue().getResponseStruckOut());
    }

    @Test
    void caseDataDefaultsStruckOutUnchanged() {
        CaseData caseData = scotlandCcdRequest3.getCaseDetails().getCaseData();

        caseManagementForCaseWorkerService.caseDataDefaults(caseData);

        assertEquals(1, caseData.getRespondentCollection().size());

        assertEquals("Antonio Vazquez", caseData
                .getRespondentCollection().get(0).getValue().getRespondentName());
        assertEquals(NO, caseData.getRespondentCollection()
                .get(0).getValue().getResponseStruckOut());
    }

    @Test
    void caseDataDefaultsFlagsImageFileNameNull() {
        CaseData caseData = manchesterCcdRequest.getCaseDetails().getCaseData();
        caseManagementForCaseWorkerService.caseDataDefaults(caseData);
        assertNull(caseData.getFlagsImageAltText());
        assertEquals("EMP-TRIB-0000000.jpg", caseData.getFlagsImageFileName());
    }

    @Test
    void caseDataDefaultsFlagsImageFileNameEmpty() {
        CaseData caseData = ccdRequest10.getCaseDetails().getCaseData();
        caseManagementForCaseWorkerService.caseDataDefaults(caseData);
        assertNull(caseData.getFlagsImageAltText());
        assertEquals("EMP-TRIB-0000000.jpg", caseData.getFlagsImageFileName());
    }

    @Test
    void dateToCurrentPositionChanged() {
        CaseData caseData = scotlandCcdRequest1.getCaseDetails().getCaseData();
        caseManagementForCaseWorkerService.dateToCurrentPosition(caseData);
        assertEquals(caseData.getCurrentPosition(), caseData.getPositionType());
        assertEquals(caseData.getDateToPosition(), LocalDate.now().toString());
    }

    @Test
    void dateToCurrentPositionUnChanged() {
        CaseData caseData = scotlandCcdRequest2.getCaseDetails().getCaseData();
        caseManagementForCaseWorkerService.dateToCurrentPosition(caseData);
        assertEquals(caseData.getCurrentPosition(), caseData.getPositionType());
        assertEquals("2019-11-15", caseData.getDateToPosition());
    }

    @Test
    void dateToCurrentPositionNullPositionType() {
        CaseData caseData = scotlandCcdRequest3.getCaseDetails().getCaseData();
        caseData.setPositionType(null);
        caseManagementForCaseWorkerService.dateToCurrentPosition(caseData);
        assertNull(caseData.getPositionType());
        assertNull(caseData.getDateToPosition());
    }

    @Test
    void dateToCurrentPositionNullCurrentPosition() {
        CaseData caseData = scotlandCcdRequest3.getCaseDetails().getCaseData();
        caseManagementForCaseWorkerService.dateToCurrentPosition(caseData);
        assertEquals(caseData.getCurrentPosition(), caseData.getPositionType());
        assertEquals(caseData.getDateToPosition(), LocalDate.now().toString());
    }

    @Test
    void struckOutRespondentEmpty() {
        CaseData caseData = caseManagementForCaseWorkerService.struckOutRespondents(scotlandCcdRequest5);
        assertEquals(0, caseData.getRespondentCollection().size());
    }

    @Test
    void struckOutRespondentFirstToLast() {
        CaseData caseData = caseManagementForCaseWorkerService.struckOutRespondents(scotlandCcdRequest1);

        assertEquals(3, caseData.getRespondentCollection().size());

        assertEquals("Antonio Vazquez", caseData
                .getRespondentCollection().get(0).getValue().getRespondentName());
        assertEquals(NO, caseData.getRespondentCollection()
                .get(0).getValue().getResponseStruckOut());
        assertEquals("Roberto Dondini", caseData
                .getRespondentCollection()
                .get(1).getValue().getRespondentName());
        assertEquals(NO, caseData.getRespondentCollection()
                .get(1).getValue().getResponseStruckOut());
        assertEquals("Juan Garcia", caseData.getRespondentCollection()
                .get(2).getValue().getRespondentName());
        assertEquals(YES, caseData.getRespondentCollection()
                .get(2).getValue().getResponseStruckOut());
    }

    @Test
    void struckOutRespondentRespAddressLinesEmpty() {
        scotlandCcdRequest1.getCaseDetails().getCaseData().getRespondentCollection().get(0).getValue()
                .setResponseRespondentAddress(new Address());
        scotlandCcdRequest1.getCaseDetails().getCaseData().getRespondentCollection().get(0).getValue()
                .getResponseRespondentAddress().setAddressLine1("");
        scotlandCcdRequest1.getCaseDetails().getCaseData().getRespondentCollection().get(0).getValue()
                .getResponseRespondentAddress().setAddressLine2("");
        scotlandCcdRequest1.getCaseDetails().getCaseData().getRespondentCollection().get(0).getValue()
                .getResponseRespondentAddress().setAddressLine3("");
        scotlandCcdRequest1.getCaseDetails().getCaseData().getRespondentCollection().get(0).getValue()
                .getResponseRespondentAddress().setCountry("");
        scotlandCcdRequest1.getCaseDetails().getCaseData().getRespondentCollection().get(0).getValue()
                .getResponseRespondentAddress().setCounty("");
        scotlandCcdRequest1.getCaseDetails().getCaseData().getRespondentCollection().get(0).getValue()
                .getResponseRespondentAddress().setPostCode("");
        scotlandCcdRequest1.getCaseDetails().getCaseData().getRespondentCollection().get(0).getValue()
                .getResponseRespondentAddress().setPostTown("");
        CaseData caseData = caseManagementForCaseWorkerService.struckOutRespondents(scotlandCcdRequest1);
        assertEquals(3, caseData.getRespondentCollection().size());
        assertEquals("", caseData
                .getRespondentCollection().get(0).getValue().getResponseRespondentAddress().getAddressLine1());
        assertEquals("", caseData
                .getRespondentCollection().get(0).getValue().getResponseRespondentAddress().getAddressLine2());
        assertEquals("", caseData
                .getRespondentCollection().get(0).getValue().getResponseRespondentAddress().getAddressLine3());
        assertEquals("", caseData
                .getRespondentCollection().get(0).getValue().getResponseRespondentAddress().getCountry());
        assertEquals("", caseData
                .getRespondentCollection().get(0).getValue().getResponseRespondentAddress().getCounty());
        assertEquals("", caseData
                .getRespondentCollection().get(0).getValue().getResponseRespondentAddress().getPostCode());
        assertEquals("", caseData
                .getRespondentCollection().get(0).getValue().getResponseRespondentAddress().getPostTown());
    }

    @Test
    void struckOutRespondentUnchanged() {
        CaseData caseData = caseManagementForCaseWorkerService.struckOutRespondents(scotlandCcdRequest3);

        assertEquals(1, caseData.getRespondentCollection().size());

        assertEquals("Antonio Vazquez",
                caseData.getRespondentCollection().get(0).getValue().getRespondentName());
    }

    @Test
    void continuingRespondentFirstToLast() {
        CaseData caseData = caseManagementForCaseWorkerService.continuingRespondent(scotlandCcdRequest1);

        assertEquals(3, caseData.getRespondentCollection().size());

        assertEquals("Antonio Vazquez",
                caseData.getRespondentCollection().get(0).getValue().getRespondentName());
        assertEquals(YES, caseData.getRespondentCollection().get(0).getValue().getResponseContinue());
        assertEquals("Juan Garcia",
                caseData.getRespondentCollection().get(1).getValue().getRespondentName());
        assertEquals(YES, caseData.getRespondentCollection().get(1).getValue().getResponseContinue());
        assertEquals("Roberto Dondini",
                caseData.getRespondentCollection().get(2).getValue().getRespondentName());
        assertEquals(NO, caseData.getRespondentCollection().get(2).getValue().getResponseContinue());
    }

    @Test
    void continuingRespondentNull() {
        CaseData caseData = caseManagementForCaseWorkerService.continuingRespondent(scotlandCcdRequest3);
        assertEquals(1, caseData.getRespondentCollection().size());
        assertEquals(YES, caseData.getRespondentCollection().get(0).getValue().getResponseContinue());
    }

    @Test
    void continuingRespondentEmpty() {
        CaseData caseData = caseManagementForCaseWorkerService.continuingRespondent(scotlandCcdRequest5);
        assertEquals(0, caseData.getRespondentCollection().size());
    }

    @Test
    void buildFlagsImageFileNameForNullFlagsTypes() {
        CaseDetails caseDetails = ccdRequest11.getCaseDetails();
        FlagsImageHelper.buildFlagsImageFileName(caseDetails);
        assertEquals("", caseDetails.getCaseData().getFlagsImageAltText());
        //assertEquals("EMP-TRIB-00000000000.jpg", caseDetails.getCaseData().getFlagsImageFileName());
    }

    @Test
    void buildFlagsImageFileNameForNullFlagsFields() {
        CaseDetails caseDetails = ccdRequest12.getCaseDetails();
        FlagsImageHelper.buildFlagsImageFileName(caseDetails);
        assertEquals("", caseDetails.getCaseData().getFlagsImageAltText());
        //assertEquals("EMP-TRIB-00000000000.jpg", caseDetails.getCaseData().getFlagsImageFileName());
    }

    @Test
    void buildFlagsImageFileNameForEmptyFlagsFields() {
        CaseDetails caseDetails = ccdRequest13.getCaseDetails();
        FlagsImageHelper.buildFlagsImageFileName(caseDetails);
        assertEquals("", caseDetails.getCaseData().getFlagsImageAltText());
        //assertEquals("EMP-TRIB-00000000000.jpg", caseDetails.getCaseData().getFlagsImageFileName());
    }

    @Test
    void buildFlagsImageFileNameForFalseFlagsFields() {
        CaseDetails caseDetails = ccdRequest14.getCaseDetails();
        FlagsImageHelper.buildFlagsImageFileName(caseDetails);
        assertEquals("", caseDetails.getCaseData().getFlagsImageAltText());
        //assertEquals("EMP-TRIB-00000000000.jpg", caseDetails.getCaseData().getFlagsImageFileName());
    }

    @Test
    void buildFlagsImageFileNameForTrueFlagsFields() {
        CaseDetails caseDetails = ccdRequest15.getCaseDetails();
        FlagsImageHelper.buildFlagsImageFileName(caseDetails);
        String expected = "<font color='DarkRed' size='5'> DO NOT POSTPONE </font>"
                + "<font size='5'> - </font>"
                + "<font color='Green' size='5'> LIVE APPEAL </font>"
                + "<font size='5'> - </font>"
                + "<font color='Red' size='5'> RULE 50(3)b </font>"
                + "<font size='5'> - </font>"
                + "<font color='LightBlack' size='5'> REPORTING </font>"
                + "<font size='5'> - </font>"
                + "<font color='Orange' size='5'> SENSITIVE </font>"
                + "<font size='5'> - </font>"
                + "<font color='Purple' size='5'> RESERVED </font>"
                + "<font size='5'> - </font>"
                + "<font color='Olive' size='5'> ECC </font>"
                + "<font size='5'> - </font>"
                + "<font color='SlateGray' size='5'> DIGITAL FILE </font>"
                + "<font size='5'> - </font><font color='DarkSlateBlue' size='5'> REASONABLE ADJUSTMENT </font>";
        assertEquals(expected, caseDetails.getCaseData().getFlagsImageAltText());
        //assertEquals("EMP-TRIB-01111111110.jpg", caseDetails.getCaseData().getFlagsImageFileName());
    }

    @Test
    void buildFlagsImageFileNameForTrueFlagsFieldsScotland() {
        CaseDetails caseDetails = scotlandCcdRequest3.getCaseDetails();
        FlagsImageHelper.buildFlagsImageFileName(caseDetails);
        String expected = "<font color='DeepPink' size='5'> WITH OUTSTATION </font>";
        assertEquals(expected, caseDetails.getCaseData().getFlagsImageAltText());
        //assertEquals("EMP-TRIB-10000000000.jpg", caseDetails.getCaseData().getFlagsImageFileName());
    }

    private CaseDetails generateCaseDetails(String jsonFileName) throws Exception {
        String json = new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(getClass().getClassLoader()
                .getResource(jsonFileName)).toURI())));
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, CaseDetails.class);
    }

    @Test
    void amendHearingNonScotland() {
        CaseData caseData = ccdRequest13.getCaseDetails().getCaseData();
        caseManagementForCaseWorkerService.amendHearing(caseData, ENGLANDWALES_CASE_TYPE_ID);
        assertEquals(HEARING_STATUS_LISTED, caseData.getHearingCollection().get(0).getValue()
                .getHearingDateCollection().get(0).getValue().getHearingStatus());
        assertEquals(HEARING_STATUS_LISTED, caseData.getHearingCollection().get(1).getValue()
                .getHearingDateCollection().get(0).getValue().getHearingStatus());
        assertEquals(HEARING_STATUS_LISTED, caseData.getHearingCollection().get(2).getValue()
                .getHearingDateCollection().get(0).getValue().getHearingStatus());
        assertEquals(HEARING_STATUS_LISTED, caseData.getHearingCollection().get(2).getValue()
                .getHearingDateCollection().get(0).getValue().getHearingStatus());
        assertEquals("Manchester", caseData.getHearingCollection().get(0).getValue()
                .getHearingDateCollection().get(0).getValue().getHearingVenueDay().getSelectedLabel());
        assertEquals("2019-11-01T12:11:00.000", caseData.getHearingCollection().get(0).getValue()
                .getHearingDateCollection().get(0).getValue().getHearingTimingStart());
        assertEquals("2019-11-01T12:11:00.000", caseData.getHearingCollection().get(0).getValue()
                .getHearingDateCollection().get(0).getValue().getHearingTimingFinish());
    }

    @Test
    void amendHearingEmptyHearingCollection() {
        CaseData caseData = ccdRequest21.getCaseDetails().getCaseData();
        caseData.setHearingCollection(new ArrayList<>());
        caseManagementForCaseWorkerService.amendHearing(caseData, ENGLANDWALES_CASE_TYPE_ID);
        assertEquals(0, caseData.getHearingCollection().size());
    }

    @Test
    void amendHearingCaseTypeIdSingle() {
        CaseData caseData = ccdRequest21.getCaseDetails().getCaseData();
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            caseManagementForCaseWorkerService.amendHearing(caseData, SINGLE_CASE_TYPE);
        });
        assertEquals("Unexpected case type id " + SINGLE_CASE_TYPE, exception.getMessage());
    }

    @Test
    void amendHearingNullHearingCollection() {
        CaseData caseData = ccdRequest21.getCaseDetails().getCaseData();
        caseData.setHearingCollection(null);
        caseManagementForCaseWorkerService.amendHearing(caseData, ENGLANDWALES_CASE_TYPE_ID);
        assertNull(caseData.getHearingCollection());
    }

    @Test
    void amendHearingNullHearingDateCollection() {
        CaseData caseData = ccdRequest21.getCaseDetails().getCaseData();
        caseData.getHearingCollection().get(0).getValue().setHearingDateCollection(null);
        caseManagementForCaseWorkerService.amendHearing(caseData, ENGLANDWALES_CASE_TYPE_ID);
        assertNull(caseData.getHearingCollection().get(0).getValue().getHearingDateCollection());
    }

    @Test
    void amendHearingEmptyHearingDateCollection() {
        CaseData caseData = ccdRequest21.getCaseDetails().getCaseData();
        caseData.getHearingCollection().get(0).getValue().setHearingDateCollection(new ArrayList<>());
        caseManagementForCaseWorkerService.amendHearing(caseData, ENGLANDWALES_CASE_TYPE_ID);
        assertEquals(0, caseData.getHearingCollection().get(0).getValue().getHearingDateCollection().size());
    }

    @Test
    void midEventAmendHearingEmptyHearingCollection() {
        CaseData caseData = ccdRequest21.getCaseDetails().getCaseData();
        caseData.setHearingCollection(new ArrayList<>());
        List<String> errors = new ArrayList<>();
        caseManagementForCaseWorkerService.midEventAmendHearing(caseData, errors);
        assertEquals(0, caseData.getHearingCollection().size());
    }

    @Test
    void midEventAmendHearingNullHearingCollection() {
        CaseData caseData = ccdRequest21.getCaseDetails().getCaseData();
        caseData.setHearingCollection(null);
        List<String> errors = new ArrayList<>();
        caseManagementForCaseWorkerService.midEventAmendHearing(caseData, errors);
        assertNull(caseData.getHearingCollection());
    }

    @Test
    void midEventAmendHearingNullHearingDateCollection() {
        CaseData caseData = ccdRequest21.getCaseDetails().getCaseData();
        caseData.getHearingCollection().get(0).getValue().setHearingDateCollection(null);
        List<String> errors = new ArrayList<>();
        caseManagementForCaseWorkerService.midEventAmendHearing(caseData, errors);
        assertNull(caseData.getHearingCollection().get(0).getValue().getHearingDateCollection());
    }

    @Test
    void midEventAmendHearingEmptyHearingDateCollection() {
        CaseData caseData = ccdRequest21.getCaseDetails().getCaseData();
        caseData.getHearingCollection().get(0).getValue().setHearingDateCollection(new ArrayList<>());
        List<String> errors = new ArrayList<>();
        caseManagementForCaseWorkerService.midEventAmendHearing(caseData, errors);
        assertEquals(0, caseData.getHearingCollection().get(0).getValue().getHearingDateCollection().size());
    }

    @Test
    void midEventAmendHearingDateOnWeekend() {
        CaseData caseData = ccdRequest13.getCaseDetails().getCaseData();
        List<String> errors = new ArrayList<>();
        caseData.getHearingCollection().get(0)
                .getValue().getHearingDateCollection()
                .get(0).getValue().setListedDate("2022-03-19T12:11:00.000");
        String hearingNumber = caseData.getHearingCollection().get(0).getValue().getHearingNumber();
        caseManagementForCaseWorkerService.midEventAmendHearing(caseData, errors);
        assertFalse(errors.isEmpty());
        assertEquals(LISTED_DATE_ON_WEEKEND_MESSAGE + hearingNumber, errors.get(0));
    }

    @ParameterizedTest
    @CsvSource({"Listed, Yes", " , Yes"})
     void midEventAmendHearingDateInPast(String hearingStatus, String warning) {
        CaseData caseData = ccdRequest13.getCaseDetails().getCaseData();
        List<String> errors = new ArrayList<>();
        DateListedType dateListedType = caseData.getHearingCollection().get(0)
                .getValue().getHearingDateCollection()
                .get(0).getValue();
        dateListedType.setListedDate("2022-03-19T12:11:00.000");
        dateListedType.setHearingStatus(hearingStatus);
        caseManagementForCaseWorkerService.midEventAmendHearing(caseData, errors);
        assertEquals(warning, caseData.getListedDateInPastWarning());
    }

    @Test
    void amendMidEventHearingDateFridayNight() {
        CaseData caseData = createCaseWithHearingDate("2022-03-18T23:59:00.000");
        List<String> errors = new ArrayList<>();
        caseManagementForCaseWorkerService.midEventAmendHearing(caseData, errors);
        assertTrue(errors.isEmpty());
    }

    @Test
    void amendMidEventHearingDateMondayMorning() {
        CaseData caseData = createCaseWithHearingDate("2022-03-21T00:00:00.000");
        List<String> errors = new ArrayList<>();
        caseManagementForCaseWorkerService.midEventAmendHearing(caseData, errors);
        assertTrue(errors.isEmpty());
    }

    private CaseData createCaseWithHearingDate(String date) {
        HearingTypeItem hearing = new HearingTypeItem();
        hearing.setId(UUID.randomUUID().toString());
        DateListedTypeItem dateListedTypeItem = new DateListedTypeItem();
        DateListedType dateListedType = new DateListedType();
        dateListedType.setListedDate(date);
        dateListedTypeItem.setId(UUID.randomUUID().toString());
        dateListedTypeItem.setValue(dateListedType);
        HearingType hearingType = new HearingType();
        hearingType.setHearingDateCollection(Collections.singletonList(dateListedTypeItem));
        hearing.setValue(hearingType);
        List<HearingTypeItem> hearings = new ArrayList<>();
        hearings.add(hearing);
        CaseData caseData = new CaseData();
        caseData.setHearingCollection(hearings);
        return caseData;
    }

    @Test
    void amendHearingScotland() {
        CaseData caseData = scotlandCcdRequest3.getCaseDetails().getCaseData();
        caseManagementForCaseWorkerService.amendHearing(caseData, SCOTLAND_CASE_TYPE_ID);
        assertEquals(HEARING_STATUS_LISTED, caseData.getHearingCollection().get(0).getValue()
                .getHearingDateCollection().get(0).getValue().getHearingStatus());

        assertEquals(TribunalOffice.ABERDEEN.getOfficeName(), caseData.getHearingCollection().get(0).getValue()
                .getHearingDateCollection().get(0).getValue().getHearingAberdeen().getSelectedLabel());
        assertNull(caseData.getHearingCollection().get(0).getValue()
                .getHearingDateCollection().get(0).getValue().getHearingGlasgow());

        assertEquals(TribunalOffice.GLASGOW.getOfficeName(), caseData.getHearingCollection().get(1).getValue()
                .getHearingDateCollection().get(0).getValue().getHearingGlasgow().getSelectedLabel());
        assertNull(caseData.getHearingCollection().get(1).getValue()
                .getHearingDateCollection().get(0).getValue().getHearingAberdeen());

        assertEquals(TribunalOffice.EDINBURGH.getOfficeName(), caseData.getHearingCollection().get(2).getValue()
                .getHearingDateCollection().get(0).getValue().getHearingEdinburgh().getSelectedLabel());
        assertNull(caseData.getHearingCollection().get(0).getValue()
                .getHearingDateCollection().get(0).getValue().getHearingGlasgow());

        final String dundee = TribunalOffice.DUNDEE.getOfficeName();
        assertEquals(dundee, caseData.getHearingCollection().get(3).getValue()
                .getHearingDateCollection().get(0).getValue().getHearingDundee().getSelectedLabel());
        assertEquals(dundee, caseData.getHearingCollection().get(3).getValue()
                .getHearingDateCollection().get(0).getValue().getHearingVenueDayScotland());
    }

    @Test
    void midRespondentECC() {
        when(caseRetrievalForCaseWorkerService.casesRetrievalESRequest(
                isA(String.class), eq(AUTH_TOKEN), isA(String.class), isA(List.class)))
                .thenReturn(new ArrayList(Collections.singleton(submitEvent)));
        assertEquals(1, caseManagementForCaseWorkerService.createECC(
                manchesterCcdRequest.getCaseDetails(), AUTH_TOKEN,
                new ArrayList<>(), MID_EVENT_CALLBACK).getRespondentECC().getListItems().size());
    }

    @Test
    void midRespondentECCWithStruckOut() {
        CaseData caseData = new CaseData();
        caseData.setRespondentCollection(createRespondentCollection(false));
        submitEvent.setCaseData(caseData);
        when(caseRetrievalForCaseWorkerService.casesRetrievalESRequest(
                isA(String.class), eq(AUTH_TOKEN), isA(String.class), isA(List.class)))
                .thenReturn(new ArrayList(Collections.singleton(submitEvent)));
        assertEquals(2, caseManagementForCaseWorkerService.createECC(
                manchesterCcdRequest.getCaseDetails(), AUTH_TOKEN,
                new ArrayList<>(), MID_EVENT_CALLBACK).getRespondentECC().getListItems().size());
    }

    @Test
    void midRespondentECCEmpty() {
        when(caseRetrievalForCaseWorkerService.casesRetrievalESRequest(
                isA(String.class), eq(AUTH_TOKEN), isA(String.class), isA(List.class)))
                .thenReturn(null);
        List<String> errors = new ArrayList<>();
        caseManagementForCaseWorkerService.createECC(
                manchesterCcdRequest.getCaseDetails(), AUTH_TOKEN, errors, MID_EVENT_CALLBACK);
        assertEquals("[Case Reference Number not found.]", errors.toString());
    }

    @Test
    void midRespondentECCWithNoRespondentECC() {
        when(caseRetrievalForCaseWorkerService.casesRetrievalESRequest(
                isA(String.class), eq(AUTH_TOKEN), isA(String.class), isA(List.class)))
                .thenReturn(new ArrayList(Collections.singleton(submitEvent)));
        manchesterCcdRequest.getCaseDetails().getCaseData().setRespondentECC(null);
        assertEquals(1, caseManagementForCaseWorkerService.createECC(
                manchesterCcdRequest.getCaseDetails(), AUTH_TOKEN,
                new ArrayList<>(), MID_EVENT_CALLBACK).getRespondentECC().getListItems().size());
    }

    @Test
    void createECC() {
        when(caseRetrievalForCaseWorkerService.casesRetrievalESRequest(
                isA(String.class), eq(AUTH_TOKEN), isA(String.class), isA(List.class)))
                .thenReturn(new ArrayList(Collections.singleton(submitEvent)));
        CaseData casedata = caseManagementForCaseWorkerService.createECC(
                manchesterCcdRequest.getCaseDetails(), AUTH_TOKEN,
                new ArrayList<>(), ABOUT_TO_SUBMIT_EVENT_CALLBACK);
        assertEquals("11111", casedata.getCaseRefECC());
        assertEquals(FLAG_ECC, casedata.getCaseSource());
        assertTrue(casedata.getJurCodesCollection().get(0).getId().matches(
                "[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}"));
    }

    @Test
    void linkOriginalCaseECC() {
        when(caseRetrievalForCaseWorkerService.casesRetrievalESRequest(
                isA(String.class), eq(AUTH_TOKEN), isA(String.class), isA(List.class)))
                .thenReturn(new ArrayList(Collections.singleton(submitEvent)));
        assertEquals("11111", caseManagementForCaseWorkerService.createECC(
                manchesterCcdRequest.getCaseDetails(), AUTH_TOKEN,
                new ArrayList<>(), SUBMITTED_CALLBACK).getCaseRefECC());
    }

    @Test
    void linkOriginalCaseECCCounterClaims() {
        when(caseRetrievalForCaseWorkerService.casesRetrievalESRequest(
                isA(String.class), eq(AUTH_TOKEN), isA(String.class), isA(List.class)))
                .thenReturn(new ArrayList(Collections.singleton(submitEvent)));
        assertEquals("72632632", caseManagementForCaseWorkerService.createECC(
                manchesterCcdRequest.getCaseDetails(), AUTH_TOKEN,
                new ArrayList<>(), SUBMITTED_CALLBACK).getEccCases().get(0).getValue().getCounterClaim());
        EccCounterClaimTypeItem c1 = new EccCounterClaimTypeItem();
        EccCounterClaimType counterClaimType1 = new EccCounterClaimType();
        EccCounterClaimType counterClaimType2 = new EccCounterClaimType();
        counterClaimType1.setCounterClaim("72632632");
        counterClaimType2.setCounterClaim("63467343");
        c1.setId(UUID.randomUUID().toString());
        EccCounterClaimTypeItem c2 = new EccCounterClaimTypeItem();
        c2.setId(UUID.randomUUID().toString());
        c1.setValue(counterClaimType1);
        c2.setValue(counterClaimType2);
        manchesterCcdRequest.getCaseDetails().getCaseData().setEccCases(Arrays.asList(c1, c2));
        when(caseRetrievalForCaseWorkerService.casesRetrievalESRequest(
                isA(String.class), eq(AUTH_TOKEN), isA(String.class), isA(List.class)))
                .thenReturn(new ArrayList(Collections.singleton(submitEvent)));
        assertEquals(c1.getValue().getCounterClaim(), caseManagementForCaseWorkerService.createECC(
                manchesterCcdRequest.getCaseDetails(), AUTH_TOKEN,
                new ArrayList<>(), SUBMITTED_CALLBACK).getEccCases().get(0).getValue().getCounterClaim());
        assertEquals(c2.getValue().getCounterClaim(), caseManagementForCaseWorkerService.createECC(
                manchesterCcdRequest.getCaseDetails(), AUTH_TOKEN,
                new ArrayList<>(), SUBMITTED_CALLBACK).getEccCases().get(1).getValue().getCounterClaim());
    }

    @Test()
    void linkOriginalCaseECCException() throws IOException {
        when(caseRetrievalForCaseWorkerService.casesRetrievalESRequest(
                isA(String.class), eq(AUTH_TOKEN), isA(String.class), isA(List.class)))
                .thenReturn(new ArrayList(Collections.singleton(submitEvent)));
        when(ccdClient.submitEventForCase(
                anyString(), any(), anyString(), anyString(), any(), anyString()))
                .thenThrow(new InternalException(ERROR_MESSAGE));
        Exception exception = assertThrows(Exception.class, () -> {
            caseManagementForCaseWorkerService.createECC(manchesterCcdRequest.getCaseDetails(), AUTH_TOKEN,
                    new ArrayList<>(), SUBMITTED_CALLBACK);
        });
        Assertions.assertNotNull(exception);
    }

    @Test
    void createECCFromClosedCaseWithoutET3() {
        submitEvent.setState("Closed");
        submitEvent.getCaseData().getRespondentCollection().get(0).getValue().setResponseReceived(NO);
        when(caseRetrievalForCaseWorkerService.casesRetrievalESRequest(
                isA(String.class), eq(AUTH_TOKEN), isA(String.class), isA(List.class)))
                .thenReturn(new ArrayList(Collections.singleton(submitEvent)));
        List<String> errors = new ArrayList<>();
        CaseData caseData = caseManagementForCaseWorkerService.createECC(
                manchesterCcdRequest.getCaseDetails(), AUTH_TOKEN, errors, MID_EVENT_CALLBACK);
        assertNull(caseData.getRespondentECC().getListItems());
        assertEquals(2, errors.size());
        submitEvent.setState("Accepted");
        submitEvent.getCaseData().getRespondentCollection().get(0).getValue().setResponseReceived(YES);
    }

    @Test
    void respondentExtension_defaultValueNo() {
        CaseData caseData = scotlandCcdRequest1.getCaseDetails().getCaseData();
        caseManagementForCaseWorkerService.caseDataDefaults(caseData);
        for (RespondentSumTypeItem respondentSumTypeItem : caseData.getRespondentCollection()) {
            assertThat(respondentSumTypeItem.getValue().getExtensionRequested(), is(NO));
        }
    }

    @Test
    void setEt3ResponseDueDate_Serving_Date_Plus_28() {
        CaseData caseData = scotlandCcdRequest1.getCaseDetails().getCaseData();
        LocalDate localDate = LocalDate.now();
        String expectedEt3DueDate = localDate.plusDays(ET3_DUE_DATE_FROM_SERVING_DATE).toString();
        caseData.setClaimServedDate(localDate.toString());
        caseManagementForCaseWorkerService.setEt3ResponseDueDate(caseData);
        assertEquals(expectedEt3DueDate, caseData.getEt3DueDate());
    }

    @Test
    void setEt3ResponseDueDate_Serving_Date_Empty() {
        CaseData caseData = scotlandCcdRequest1.getCaseDetails().getCaseData();
        caseData.setClaimServedDate("");
        caseManagementForCaseWorkerService.setEt3ResponseDueDate(caseData);
        assertNull(caseData.getEt3DueDate());
    }

    @Test
    void respondentExtension_doesNotOverideExistingValue() {
        CaseData caseData = scotlandCcdRequest3.getCaseDetails().getCaseData();
        caseData.getRespondentCollection().get(0).getValue().setExtensionRequested(YES);
        caseManagementForCaseWorkerService.caseDataDefaults(caseData);
        assertThat(caseData.getRespondentCollection().get(0).getValue().getExtensionRequested(), is(YES));
    }

    @Test
    void testNotSetScotlandAllocatedOfficeWhenCaseTypeIdNotScotland() {
        CaseData caseData = new CaseData();
        caseData.setManagingOffice(UNASSIGNED_OFFICE);
        caseManagementForCaseWorkerService.setScotlandAllocatedOffice(ENGLANDWALES_CASE_TYPE_ID, caseData);
        assertNull(caseData.getAllocatedOffice());
    }

    @Test
    void testSetScotlandAllocatedOfficeManagingOfficeUnassigned() {
        CaseData caseData = new CaseData();
        caseData.setManagingOffice(UNASSIGNED_OFFICE);
        caseManagementForCaseWorkerService.setScotlandAllocatedOffice(SCOTLAND_CASE_TYPE_ID, caseData);
        assertNull(caseData.getAllocatedOffice());
    }

    @Test
    void testSetScotlandAllocatedOfficeManagingOfficeNull() {
        CaseData caseData = new CaseData();
        caseData.setManagingOffice(null);
        caseManagementForCaseWorkerService.setScotlandAllocatedOffice(SCOTLAND_CASE_TYPE_ID, caseData);
        assertNull(caseData.getAllocatedOffice());
    }

    @Test
    void testSetScotlandAllocatedOfficeManagingOfficeGlasgow() {
        CaseData caseData = new CaseData();
        caseData.setManagingOffice(TribunalOffice.GLASGOW.getOfficeName());
        String expectedAllocatedOffice = TribunalOffice.GLASGOW.getOfficeName();
        String expectedManagingOffice = TribunalOffice.GLASGOW.getOfficeName();
        caseManagementForCaseWorkerService.setScotlandAllocatedOffice(SCOTLAND_CASE_TYPE_ID, caseData);
        assertEquals(expectedAllocatedOffice, caseData.getAllocatedOffice());
        assertEquals(expectedManagingOffice, caseData.getManagingOffice());
    }

    @Test
    void setHmctsServiceIdSupplementary_success() throws IOException {
        Map<String, Object> payload = Map.of("supplementary_data_updates", Map.of("$set", Map.of("HMCTSServiceId",
                hmctsServiceId)));
        CaseDetails caseDetails = ccdRequest10.getCaseDetails();
        String token = ccdRequest10.getToken();
        when(ccdClient.setSupplementaryData(eq(token), eq(payload), eq(ccdRequest10.getCaseDetails().getCaseId())))
                .thenReturn(ResponseEntity.ok().build());

        caseManagementForCaseWorkerService.setHmctsServiceIdSupplementary(caseDetails, token);
        verify(ccdClient, times(1)).setSupplementaryData(eq(token), eq(payload), 
            eq(ccdRequest10.getCaseDetails().getCaseId()));
    }

    @Test
    void setHmctsServiceIdSupplementary_noResponse() throws IOException {
        Map<String, Object> payload = Map.of("supplementary_data_updates", Map.of("$set", Map.of("HMCTSServiceId",
                hmctsServiceId)));
        CaseDetails caseDetails = ccdRequest10.getCaseDetails();
        String token = ccdRequest10.getToken();
        when(ccdClient.setSupplementaryData(eq(token), eq(payload), eq(ccdRequest10.getCaseDetails().getCaseId())))
                .thenReturn(null);

        Exception e = assertThrows(CaseCreationException.class,
                () -> caseManagementForCaseWorkerService.setHmctsServiceIdSupplementary(caseDetails, token));
        assertEquals("Call to Supplementary Data API failed for 123456789", e.getMessage());
    }

    @Test
    void setHmctsServiceIdSupplementary_failedResponse() throws IOException {
        Map<String, Object> payload = Map.of("supplementary_data_updates", Map.of("$set", Map.of("HMCTSServiceId",
                hmctsServiceId)));
        CaseDetails caseDetails = ccdRequest10.getCaseDetails();
        String token = ccdRequest10.getToken();
        when(ccdClient.setSupplementaryData(eq(token), eq(payload), eq(ccdRequest10.getCaseDetails().getCaseId())))
                .thenThrow(new RestClientResponseException("call failed", 400, "Bad Request", null, null, null));

        Exception e = assertThrows(CaseCreationException.class,
                () -> caseManagementForCaseWorkerService.setHmctsServiceIdSupplementary(caseDetails, token));
        assertEquals("Call to Supplementary Data API failed for 123456789 with call failed", e.getMessage());
    }

    private List<RespondentSumTypeItem> createRespondentCollection(boolean single) {
        RespondentSumTypeItem respondentSumTypeItem1 = createRespondentSumType(
                "RespondentName1", false);
        if (single) {
            return new ArrayList<>(Collections.singletonList(respondentSumTypeItem1));
        }

        RespondentSumTypeItem respondentSumTypeItem2 = createRespondentSumType(
                "RespondentName2", false);
        RespondentSumTypeItem respondentSumTypeItem3 = createRespondentSumType(
                "RespondentName3", true);

        return new ArrayList<>(
                Arrays.asList(respondentSumTypeItem1, respondentSumTypeItem2, respondentSumTypeItem3));
    }

    private RespondentSumTypeItem createRespondentSumType(String respondentName, boolean struckOut) {
        RespondentSumType respondentSumType = new RespondentSumType();
        respondentSumType.setRespondentName(respondentName);
        if (struckOut) {
            respondentSumType.setResponseStruckOut(YES);
        }
        respondentSumType.setResponseReceived(YES);
        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setId("111");
        respondentSumTypeItem.setValue(respondentSumType);
        return respondentSumTypeItem;
    }

    private ClaimantIndType createClaimantIndType() {
        ClaimantIndType claimantIndType = new ClaimantIndType();
        claimantIndType.setClaimantLastName("ClaimantSurname");
        claimantIndType.setClaimantFirstNames("ClaimantName");
        claimantIndType.setClaimantTitle("Mr");
        return claimantIndType;
    }

    private RepresentedTypeC createRepresentedTypeC() {
        RepresentedTypeC representativeClaimantType = new RepresentedTypeC();
        representativeClaimantType.setNameOfRepresentative("Claimant Rep Name");
        representativeClaimantType.setNameOfOrganisation("Claimant Rep Org");
        representativeClaimantType.setRepresentativeReference("Claimant Rep Ref");
        return representativeClaimantType;
    }

    private List<RepresentedTypeRItem> createRepCollection(boolean single) {
        RepresentedTypeRItem representedTypeRItem1 = createRepresentedTypeR(
                "", "RepresentativeNameAAA");
        RepresentedTypeRItem representedTypeRItem2 = createRepresentedTypeR(
                "dummy", "RepresentativeNameBBB");
        RepresentedTypeRItem representedTypeRItem3 = createRepresentedTypeR(
                "RespondentName1", "RepresentativeNameCCC");
        if (single) {
            return new ArrayList<>(Collections.singletonList(representedTypeRItem1));
        } else {
            return new ArrayList<>(Arrays.asList(
                    representedTypeRItem1, representedTypeRItem2, representedTypeRItem3));
        }
    }

    private RepresentedTypeRItem createRepresentedTypeR(String respondentName, String representativeName) {
        RepresentedTypeR representedTypeR = RepresentedTypeR.builder()
            .respRepName(respondentName)
            .nameOfRepresentative(representativeName).build();
        RepresentedTypeRItem representedTypeRItem = new RepresentedTypeRItem();
        representedTypeRItem.setId("111");
        representedTypeRItem.setValue(representedTypeR);
        return representedTypeRItem;
    }

    private DynamicFixedListType createRespondentECC() {
        DynamicFixedListType respondentECC = new DynamicFixedListType();
        DynamicValueType dynamicValueType = new DynamicValueType();
        dynamicValueType.setCode("RespondentName1");
        dynamicValueType.setLabel("RespondentName1");
        respondentECC.setValue(dynamicValueType);
        return respondentECC;
    }

}
