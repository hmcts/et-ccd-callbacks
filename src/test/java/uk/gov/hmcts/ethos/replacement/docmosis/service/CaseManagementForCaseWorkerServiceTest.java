package uk.gov.hmcts.ethos.replacement.docmosis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.util.Pair;
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
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.EccCounterClaimTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.CasePreAcceptType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantIndType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantType;
import uk.gov.hmcts.et.common.model.ccd.types.DateListedType;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.EccCounterClaimType;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.et.common.model.ccd.types.RestrictedReportingType;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.SubmitMultipleEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.FlagsImageHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.MultipleCasesSendingService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.multiples.MultipleReferenceService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.InternalException;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ABOUT_TO_SUBMIT_EVENT_CALLBACK;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.EMPLOYMENT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_BULK_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ET3_DUE_DATE_FROM_SERVING_DATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.FLAG_ECC;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_LISTED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MID_EVENT_CALLBACK;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SINGLE_CASE_TYPE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SUBMITTED_CALLBACK;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.ACAS_DOC_TYPE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.EMPTY_STRING;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.ET1_ATTACHMENT_DOC_TYPE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.ET1_DOC_TYPE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementForCaseWorkerService.LISTED_DATE_ON_WEEKEND_MESSAGE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementForCaseWorkerService.ORGANISATION;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.InternalException.ERROR_MESSAGE;

@ExtendWith(SpringExtension.class)
class CaseManagementForCaseWorkerServiceTest {

    private static final String AUTH_TOKEN = "Bearer eyJhbGJbpjciOiJIUzI1NiJ9";
    public static final String UNASSIGNED_OFFICE = "Unassigned";
    private static final String HMCTS_SERVICE_ID = "BHA1";

    @Value("${ccd_gateway_base_url}")
    private String ccdGatewayBaseUrl;

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
    private CCDRequest ccdRequest22;
    private CCDRequest manchesterCcdRequest;
    private SubmitEvent submitEvent;

    @MockBean
    private CaseRetrievalForCaseWorkerService caseRetrievalForCaseWorkerService;
    @MockBean
    private CcdClient ccdClient;
    @MockBean
    private ClerkService clerkService;
    @MockBean
    private FeatureToggleService featureToggleService;
    @MockBean
    private AdminUserService adminUserService;
    @MockBean
    private CaseManagementLocationService caseManagementLocationService;
    @MockBean
    private MultipleReferenceService multipleReferenceService;
    @MockBean
    private MultipleCasesSendingService multipleCasesSendingService;

    @BeforeEach
    @SneakyThrows
    void setUp() {

        setScotlandCaseRequests();
        setGenericCaseRequests();
        setManchesterCCDRequest();
        setSubmitEvent();

        when(featureToggleService.isGlobalSearchEnabled()).thenReturn(true);
        when(featureToggleService.isWorkAllocationEnabled()).thenReturn(true);
        when(featureToggleService.isHmcEnabled()).thenReturn(true);
        when(featureToggleService.isWorkAllocationEnabled()).thenReturn(true);
        when(adminUserService.getAdminUserToken()).thenReturn(AUTH_TOKEN);
        caseManagementForCaseWorkerService = new CaseManagementForCaseWorkerService(
                caseRetrievalForCaseWorkerService, ccdClient, clerkService, featureToggleService, HMCTS_SERVICE_ID,
                adminUserService, caseManagementLocationService, multipleReferenceService, ccdGatewayBaseUrl,
                multipleCasesSendingService);
    }

    private void setScotlandCaseRequests() throws Exception {
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

    }

    private void setGenericCaseRequests() throws Exception {
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

        ccdRequest22 = new CCDRequest();
        CaseDetails caseDetails22 = generateCaseDetails("caseDetailsTest22.json");
        ccdRequest22.setCaseDetails(caseDetails22);

        CCDRequest ccdRequest2 = new CCDRequest();
        CaseDetails caseDetails2 = generateCaseDetails("caseDetailsTest2.json");
        ccdRequest2.setCaseDetails(caseDetails2);
    }

    private void setManchesterCCDRequest() {
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
        caseData.setEccCases(List.of(eccCounterClaimTypeItem));
        caseData.setRespondentECC(createRespondentECC());
        CaseDetails manchesterCaseDetails = new CaseDetails();
        manchesterCaseDetails.setCaseData(caseData);
        manchesterCaseDetails.setCaseId("123456");
        manchesterCaseDetails.setCaseTypeId(ENGLANDWALES_CASE_TYPE_ID);
        manchesterCaseDetails.setJurisdiction("TRIBUNALS");
        manchesterCcdRequest.setCaseDetails(manchesterCaseDetails);
    }

    private void setSubmitEvent() {
        submitEvent = new SubmitEvent();
        CaseData submitCaseData = new CaseData();
        submitCaseData.setRespondentCollection(createRespondentCollection(true));
        submitCaseData.setClaimantIndType(createClaimantIndType());
        submitCaseData.setRepresentativeClaimantType(createRepresentedTypeC());
        submitCaseData.setRepCollection(createRepCollection());
        submitCaseData.setClaimantRepresentedQuestion(YES);
        ClaimantType claimantType = new ClaimantType();
        claimantType.setClaimantAddressUK(getAddress());
        submitCaseData.setClaimantType(claimantType);
        submitEvent.setState("Accepted");
        submitEvent.setCaseId(123);
        submitEvent.setCaseData(submitCaseData);
    }

    private static Address getAddress() {
        Address address = new Address();
        address.setAddressLine1("AddressLine1");
        address.setAddressLine2("AddressLine2");
        address.setAddressLine3("AddressLine3");
        address.setPostTown("Manchester");
        address.setCountry("UK");
        address.setPostCode("L1 122");
        return address;
    }

    @Test
    void caseDataDefaultsClearRespondentTypeFields() {
        CaseData caseData = ccdRequest22.getCaseDetails().getCaseData();
        caseManagementForCaseWorkerService.caseDataDefaults(caseData);
        for (RespondentSumTypeItem respondentSumTypeItem : caseData.getRespondentCollection()) {
            if (respondentSumTypeItem.getValue().getRespondentType().equals(ORGANISATION)) {
                assertThat(respondentSumTypeItem.getValue().getRespondentFirstName()).isEmpty();
                assertThat(respondentSumTypeItem.getValue().getRespondentLastName()).isEmpty();
            } else {
                assertThat(respondentSumTypeItem.getValue().getRespondentOrganisation()).isEmpty();
            }
        }
    }

    @Test
    void caseDataDefaultsCaseManagementCategory() {
        CaseData caseData = scotlandCcdRequest1.getCaseDetails().getCaseData();
        caseManagementForCaseWorkerService.caseDataDefaults(caseData);
        assertThat(caseData.getCaseManagementCategory().getSelectedCode()).isEqualTo("Employment Tribunals");
        assertThat(caseData.getCaseManagementCategory().getSelectedLabel()).isEqualTo("Employment");
    }

    @Test
    void caseDataDefaultsCaseNameHmctsInternal() {
        CaseData caseData = scotlandCcdRequest1.getCaseDetails().getCaseData();
        caseManagementForCaseWorkerService.caseDataDefaults(caseData);
        assertThat(caseData.getCaseNameHmctsInternal()).isEqualTo("Anton Juliet Rodriguez vs Antonio Vazquez");
    }

    @Test
    void caseDataDefaultsHmctsCaseCategory() {
        CaseData caseData = scotlandCcdRequest1.getCaseDetails().getCaseData();
        caseManagementForCaseWorkerService.caseDataDefaults(caseData);
        assertThat(caseData.getHmctsCaseCategory()).isEqualTo("Employment Tribunals");
    }

    @Test
    void caseDataDefaultsClaimantIndividual() {
        CaseData caseData = scotlandCcdRequest1.getCaseDetails().getCaseData();
        caseManagementForCaseWorkerService.caseDataDefaults(caseData);
        assertThat(caseData.getClaimant()).isEqualTo("Anton Juliet Rodriguez");
    }

    @Test
    void caseDataDefaultsClaimantDocs() {
        DocumentTypeItem et1Doc = DocumentTypeItem.builder()
                .id(UUID.randomUUID().toString())
                .value(DocumentType.builder()
                        .startingClaimDocuments(ET1_DOC_TYPE)
                        .build())
                .build();
        DocumentTypeItem et1Attachment = DocumentTypeItem.builder()
                .id(UUID.randomUUID().toString())
                .value(DocumentType.builder()
                        .startingClaimDocuments(ET1_ATTACHMENT_DOC_TYPE)
                        .build())
                .build();
        DocumentTypeItem acas = DocumentTypeItem.builder()
                .id(UUID.randomUUID().toString())
                .value(DocumentType.builder()
                        .startingClaimDocuments(ACAS_DOC_TYPE)
                        .build())
                .build();

        DocumentTypeItem hiddenClaimantDoc = DocumentTypeItem.builder()
                .value(DocumentType.builder()
                        .startingClaimDocuments("ET1 Vetting")
                        .build())
                .build();
        CaseData caseData = scotlandCcdRequest1.getCaseDetails().getCaseData();
        caseData.setDocumentCollection(List.of(et1Doc, et1Attachment, acas, hiddenClaimantDoc));
        caseManagementForCaseWorkerService.caseDataDefaults(caseData);
        assertThat(caseData.getClaimantDocumentCollection().size()).isEqualTo(3);
        assertThat(caseData.getClaimantDocumentCollection().stream().noneMatch(d -> "ET1 Vetting".equals(
                d.getValue().getStartingClaimDocuments()))).isTrue();
    }

    @Test
    void caseDataDefaultsResponseReceived() {
        CaseData caseData = scotlandCcdRequest1.getCaseDetails().getCaseData();
        caseManagementForCaseWorkerService.caseDataDefaults(caseData);
        for (RespondentSumTypeItem respondentSumTypeItem : caseData.getRespondentCollection()) {
            assertThat(respondentSumTypeItem.getValue().getResponseReceived()).isEqualTo(NO);
        }
    }

    @Test
    void caseDataDefaultsResponseReceivedDoesNotChange() {
        CaseData caseData = scotlandCcdRequest1.getCaseDetails().getCaseData();
        caseData.getRespondentCollection().get(0).getValue().setResponseReceived(YES);
        caseManagementForCaseWorkerService.caseDataDefaults(caseData);
        assertThat(caseData.getRespondentCollection().get(0).getValue().getResponseReceived()).isEqualTo(YES);
        for (RespondentSumTypeItem respondentSumTypeItem : caseData.getRespondentCollection()) {
            if (!respondentSumTypeItem.equals(caseData.getRespondentCollection().get(0))) {
                assertThat(respondentSumTypeItem.getValue().getResponseReceived()).isEqualTo(NO);
            }
        }
    }

    @Test
    void caseDataDefaultsClaimantCompany() {
        CaseData caseData = scotlandCcdRequest2.getCaseDetails().getCaseData();
        caseManagementForCaseWorkerService.caseDataDefaults(caseData);
        assertThat(caseData.getClaimant()).isEqualTo("Orlando LTD");
    }

    @Test
    void caseDataDefaultsClaimantMissing() {
        CaseData caseData = scotlandCcdRequest2.getCaseDetails().getCaseData();
        caseData.setClaimantTypeOfClaimant(null);
        caseManagementForCaseWorkerService.caseDataDefaults(caseData);
        assertThat(caseData.getClaimant()).isEqualTo("Missing claimant");
    }

    @Test
    void caseDataDefaultsRespondentAvailable() {
        CaseData caseData = scotlandCcdRequest1.getCaseDetails().getCaseData();
        caseManagementForCaseWorkerService.caseDataDefaults(caseData);
        assertThat(caseData.getRespondent()).isEqualTo("Antonio Vazquez");
    }

    @Test
    void caseDataDefaultsRespondentMissing() {
        CaseData caseData = scotlandCcdRequest2.getCaseDetails().getCaseData();
        caseManagementForCaseWorkerService.caseDataDefaults(caseData);
        assertThat(caseData.getRespondent()).isEqualTo("Missing respondent");
    }

    @Test
    void caseDataDefaultsStruckOutYESandNulltoNO() {
        CaseData caseData = scotlandCcdRequest1.getCaseDetails().getCaseData();

        caseManagementForCaseWorkerService.caseDataDefaults(caseData);

        assertThat(caseData.getRespondentCollection().size()).isEqualTo(3);
        assertThat(caseData.getRespondentCollection().get(0).getValue().getRespondentName())
                .isEqualTo("Antonio Vazquez");
        assertThat(caseData.getRespondentCollection().get(0).getValue().getResponseStruckOut()).isEqualTo(NO);
        assertThat(caseData.getRespondentCollection().get(1).getValue().getRespondentName())
                .isEqualTo("Juan Garcia");
        assertThat(caseData.getRespondentCollection().get(1).getValue().getResponseStruckOut()).isEqualTo(YES);
        assertThat(caseData.getRespondentCollection().get(2).getValue().getRespondentName())
                .isEqualTo("Roberto Dondini");
        assertThat(caseData.getRespondentCollection().get(2).getValue().getResponseStruckOut()).isEqualTo(NO);
    }

    @Test
    void caseDataDefaultsStruckOutUnchanged() {
        CaseData caseData = scotlandCcdRequest3.getCaseDetails().getCaseData();
        caseManagementForCaseWorkerService.caseDataDefaults(caseData);
        assertThat(caseData.getRespondentCollection().size()).isEqualTo(1);
        assertThat(caseData.getRespondentCollection().get(0).getValue().getRespondentName())
                .isEqualTo("Antonio Vazquez");
        assertThat(caseData.getRespondentCollection().get(0).getValue().getResponseStruckOut()).isEqualTo(NO);
    }

    @Test
    void caseDataDefaultsFlagsImageFileNameNull() {
        CaseData caseData = manchesterCcdRequest.getCaseDetails().getCaseData();
        caseManagementForCaseWorkerService.caseDataDefaults(caseData);
        assertThat(caseData.getFlagsImageAltText()).isNull();
        assertThat(caseData.getFlagsImageFileName()).isEqualTo("EMP-TRIB-0000000.jpg");
    }

    @Test
    void caseDataDefaultsFlagsImageFileNameEmpty() {
        CaseData caseData = ccdRequest10.getCaseDetails().getCaseData();
        caseManagementForCaseWorkerService.caseDataDefaults(caseData);
        assertThat(caseData.getFlagsImageAltText()).isNull();
        assertThat(caseData.getFlagsImageFileName()).isEqualTo("EMP-TRIB-0000000.jpg");
    }

    @Test
    void dateToCurrentPositionChanged() {
        CaseData caseData = scotlandCcdRequest1.getCaseDetails().getCaseData();
        caseManagementForCaseWorkerService.dateToCurrentPosition(caseData);
        assertThat(caseData.getPositionType()).isEqualTo(caseData.getCurrentPosition());
        assertThat(LocalDate.now().toString()).isEqualTo(caseData.getDateToPosition());
    }

    @Test
    void dateToCurrentPositionUnChanged() {
        CaseData caseData = scotlandCcdRequest2.getCaseDetails().getCaseData();
        caseManagementForCaseWorkerService.dateToCurrentPosition(caseData);
        assertThat(caseData.getPositionType()).isEqualTo(caseData.getCurrentPosition());
        assertThat(caseData.getDateToPosition()).isEqualTo("2019-11-15");
    }

    @Test
    void dateToCurrentPositionNullPositionType() {
        CaseData caseData = scotlandCcdRequest3.getCaseDetails().getCaseData();
        caseData.setPositionType(null);
        caseManagementForCaseWorkerService.dateToCurrentPosition(caseData);
        assertThat(caseData.getPositionType()).isNull();
        assertThat(caseData.getDateToPosition()).isNull();
    }

    @Test
    void dateToCurrentPositionNullCurrentPosition() {
        CaseData caseData = scotlandCcdRequest3.getCaseDetails().getCaseData();
        caseManagementForCaseWorkerService.dateToCurrentPosition(caseData);
        assertThat(caseData.getPositionType()).isEqualTo(caseData.getCurrentPosition());
        assertThat(LocalDate.now().toString()).isEqualTo(caseData.getDateToPosition());
    }

    @Test
    void struckOutRespondentEmpty() {
        CaseData caseData = caseManagementForCaseWorkerService.struckOutRespondents(scotlandCcdRequest5);
        assertThat(caseData.getRespondentCollection().size()).isEqualTo(0);
    }

    @Test
    void struckOutRespondentFirstToLast() {
        CaseData caseData = caseManagementForCaseWorkerService.struckOutRespondents(scotlandCcdRequest1);

        assertThat(caseData.getRespondentCollection().size()).isEqualTo(3);

        assertThat(caseData.getRespondentCollection().get(0).getValue().getRespondentName())
                .isEqualTo("Antonio Vazquez");
        assertThat(caseData.getRespondentCollection().get(0).getValue().getResponseStruckOut()).isEqualTo(NO);
        assertThat(caseData.getRespondentCollection().get(1).getValue().getRespondentName())
                .isEqualTo("Roberto Dondini");
        assertThat(caseData.getRespondentCollection().get(1).getValue().getResponseStruckOut()).isEqualTo(NO);
        assertThat(caseData.getRespondentCollection().get(2).getValue().getRespondentName()).isEqualTo("Juan Garcia");
        assertThat(caseData.getRespondentCollection().get(2).getValue().getResponseStruckOut()).isEqualTo(YES);
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
        assertThat(caseData.getRespondentCollection().size()).isEqualTo(3);
        assertThat(caseData.getRespondentCollection().get(0).getValue().getResponseRespondentAddress()
                .getAddressLine1()).isEmpty();
        assertThat(caseData.getRespondentCollection().get(0).getValue().getResponseRespondentAddress()
                .getAddressLine2()).isEmpty();
        assertThat(caseData.getRespondentCollection().get(0).getValue().getResponseRespondentAddress()
                .getAddressLine3()).isEmpty();
        assertThat(caseData.getRespondentCollection().get(0).getValue().getResponseRespondentAddress().getCountry())
                .isEmpty();
        assertThat(caseData.getRespondentCollection().get(0).getValue().getResponseRespondentAddress().getCounty())
                .isEmpty();
        assertThat(caseData.getRespondentCollection().get(0).getValue().getResponseRespondentAddress().getPostCode())
                .isEmpty();
        assertThat(caseData.getRespondentCollection().get(0).getValue().getResponseRespondentAddress().getPostTown())
                .isEmpty();
    }

    @Test
    void struckOutRespondentUnchanged() {
        CaseData caseData = caseManagementForCaseWorkerService.struckOutRespondents(scotlandCcdRequest3);
        assertThat(caseData.getRespondentCollection().size()).isEqualTo(1);
        assertThat(caseData.getRespondentCollection().get(0).getValue().getRespondentName())
                .isEqualTo("Antonio Vazquez");
    }

    @Test
    void continuingRespondentFirstToLast() {
        CaseData caseData = caseManagementForCaseWorkerService.continuingRespondent(scotlandCcdRequest1);
        assertThat(caseData.getRespondentCollection().size()).isEqualTo(3);
        assertThat(caseData.getRespondentCollection().get(0).getValue().getRespondentName())
                .isEqualTo("Antonio Vazquez");
        assertThat(caseData.getRespondentCollection().get(0).getValue().getResponseContinue()).isEqualTo(YES);
        assertThat(caseData.getRespondentCollection().get(1).getValue().getRespondentName()).isEqualTo("Juan Garcia");
        assertThat(caseData.getRespondentCollection().get(1).getValue().getResponseContinue()).isEqualTo(YES);
        assertThat(caseData.getRespondentCollection().get(2).getValue().getRespondentName())
                .isEqualTo("Roberto Dondini");
        assertThat(caseData.getRespondentCollection().get(2).getValue().getResponseContinue()).isEqualTo(NO);
    }

    @Test
    void continuingRespondentNull() {
        CaseData caseData = caseManagementForCaseWorkerService.continuingRespondent(scotlandCcdRequest3);
        assertThat(caseData.getRespondentCollection().size()).isEqualTo(1);
        assertThat(caseData.getRespondentCollection().get(0).getValue().getResponseContinue()).isEqualTo(YES);
    }

    @Test
    void continuingRespondentEmpty() {
        CaseData caseData = caseManagementForCaseWorkerService.continuingRespondent(scotlandCcdRequest5);
        assertThat(caseData.getRespondentCollection().size()).isEqualTo(0);
    }

    @Test
    void buildFlagsImageFileNameForNullFlagsTypes() {
        CaseDetails caseDetails = ccdRequest11.getCaseDetails();
        FlagsImageHelper.buildFlagsImageFileName(caseDetails);
        assertThat(caseDetails.getCaseData().getFlagsImageAltText()).isEmpty();
    }

    @Test
    void buildFlagsImageFileNameForNullFlagsFields() {
        CaseDetails caseDetails = ccdRequest12.getCaseDetails();
        FlagsImageHelper.buildFlagsImageFileName(caseDetails);
        assertThat(caseDetails.getCaseData().getFlagsImageAltText()).isEmpty();
    }

    @Test
    void buildFlagsImageFileNameForEmptyFlagsFields() {
        CaseDetails caseDetails = ccdRequest13.getCaseDetails();
        FlagsImageHelper.buildFlagsImageFileName(caseDetails);
        assertThat(caseDetails.getCaseData().getFlagsImageAltText()).isEmpty();
    }

    @Test
    void buildFlagsImageFileNameForFalseFlagsFields() {
        CaseDetails caseDetails = ccdRequest14.getCaseDetails();
        FlagsImageHelper.buildFlagsImageFileName(caseDetails);
        assertThat(caseDetails.getCaseData().getFlagsImageAltText()).isEmpty();
    }

    @Test
    void buildFlagsImageFileNameForTrueFlagsFields() {
        CaseDetails caseDetails = ccdRequest15.getCaseDetails();
        FlagsImageHelper.buildFlagsImageFileName(caseDetails);
        String expected = "<font color='DarkRed' size='5'> DO NOT POSTPONE </font>"
                + "<font size='5'> - </font>"
                + "<font color='Green' size='5'> LIVE APPEAL </font>"
                + "<font size='5'> - </font>"
                + "<font color='Red' size='5'> RULE 49(3)b </font>"
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
                + "<font size='5'> - </font>"
                + "<font color='DarkSlateBlue' size='5'> REASONABLE ADJUSTMENT </font>";
        assertThat(caseDetails.getCaseData().getFlagsImageAltText()).isEqualTo(expected);
    }

    @Test
    void buildFlagsImageFileNameForTrueFlagsFieldsScotland() {
        CaseDetails caseDetails = scotlandCcdRequest3.getCaseDetails();
        FlagsImageHelper.buildFlagsImageFileName(caseDetails);
        String expected = "<font color='DeepPink' size='5'> WITH OUTSTATION </font>";
        assertThat(caseDetails.getCaseData().getFlagsImageAltText()).isEqualTo(expected);
    }

    private CaseDetails generateCaseDetails(String jsonFileName) throws Exception {
        String json = new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(Thread.currentThread()
            .getContextClassLoader().getResource(jsonFileName)).toURI())));
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, CaseDetails.class);
    }

    @Test
    void amendHearingNonScotland() {
        CaseData caseData = ccdRequest13.getCaseDetails().getCaseData();
        caseManagementForCaseWorkerService.amendHearing(caseData, ENGLANDWALES_CASE_TYPE_ID);
        assertThat(caseData.getHearingCollection().get(0).getValue().getHearingDateCollection().get(0).getValue()
                .getHearingStatus()).isEqualTo(HEARING_STATUS_LISTED);
        assertThat(caseData.getHearingCollection().get(1).getValue().getHearingDateCollection().get(0).getValue()
                .getHearingStatus()).isEqualTo(HEARING_STATUS_LISTED);
        assertThat(caseData.getHearingCollection().get(2).getValue().getHearingDateCollection().get(0).getValue()
                .getHearingStatus()).isEqualTo(HEARING_STATUS_LISTED);
        assertThat(caseData.getHearingCollection().get(2).getValue().getHearingDateCollection().get(0).getValue()
                .getHearingStatus()).isEqualTo(HEARING_STATUS_LISTED);
        assertThat(caseData.getHearingCollection().get(0).getValue().getHearingDateCollection().get(0).getValue()
                .getHearingVenueDay().getSelectedLabel()).isEqualTo("Manchester");
        assertThat(caseData.getHearingCollection().get(0).getValue().getHearingDateCollection().get(0).getValue()
                .getHearingTimingStart()).isEqualTo("2019-11-01T12:11:00.000");
        assertThat(caseData.getHearingCollection().get(0).getValue().getHearingDateCollection().get(0).getValue()
                .getHearingTimingFinish()).isEqualTo("2019-11-01T12:11:00.000");
    }

    @Test
    void amendHearingEmptyHearingCollection() {
        CaseData caseData = ccdRequest21.getCaseDetails().getCaseData();
        caseData.setHearingCollection(new ArrayList<>());
        caseManagementForCaseWorkerService.amendHearing(caseData, ENGLANDWALES_CASE_TYPE_ID);
        assertThat(caseData.getHearingCollection().size()).isEqualTo(0);
    }

    @Test
    void amendHearingCaseTypeIdSingle() {
        CaseData caseData = ccdRequest21.getCaseDetails().getCaseData();
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                caseManagementForCaseWorkerService.amendHearing(caseData, SINGLE_CASE_TYPE));
        assertThat(exception.getMessage()).isEqualTo("Unexpected case type id " + SINGLE_CASE_TYPE);
    }

    @Test
    void amendHearingNullHearingCollection() {
        CaseData caseData = ccdRequest21.getCaseDetails().getCaseData();
        caseData.setHearingCollection(null);
        caseManagementForCaseWorkerService.amendHearing(caseData, ENGLANDWALES_CASE_TYPE_ID);
        assertThat(caseData.getHearingCollection()).isNull();
    }

    @Test
    void amendHearingNullHearingDateCollection() {
        CaseData caseData = ccdRequest21.getCaseDetails().getCaseData();
        caseData.getHearingCollection().get(0).getValue().setHearingDateCollection(null);
        caseManagementForCaseWorkerService.amendHearing(caseData, ENGLANDWALES_CASE_TYPE_ID);
        assertThat(caseData.getHearingCollection().get(0).getValue().getHearingDateCollection()).isNull();
    }

    @Test
    void amendHearingEmptyHearingDateCollection() {
        CaseData caseData = ccdRequest21.getCaseDetails().getCaseData();
        caseData.getHearingCollection().get(0).getValue().setHearingDateCollection(new ArrayList<>());
        caseManagementForCaseWorkerService.amendHearing(caseData, ENGLANDWALES_CASE_TYPE_ID);
        assertThat(caseData.getHearingCollection().get(0).getValue().getHearingDateCollection().size()).isEqualTo(0);
    }

    @Test
    void midEventAmendHearingEmptyHearingCollection() {
        CaseData caseData = ccdRequest21.getCaseDetails().getCaseData();
        caseData.setHearingCollection(new ArrayList<>());
        List<String> errors = new ArrayList<>();
        caseManagementForCaseWorkerService.midEventAmendHearing(caseData, errors);
        assertThat(caseData.getHearingCollection().size()).isEqualTo(0);
    }

    @Test
    void midEventAmendHearingNullHearingCollection() {
        CaseData caseData = ccdRequest21.getCaseDetails().getCaseData();
        caseData.setHearingCollection(null);
        List<String> errors = new ArrayList<>();
        caseManagementForCaseWorkerService.midEventAmendHearing(caseData, errors);
        assertThat(caseData.getHearingCollection()).isNull();
    }

    @Test
    void midEventAmendHearingNullHearingDateCollection() {
        CaseData caseData = ccdRequest21.getCaseDetails().getCaseData();
        caseData.getHearingCollection().get(0).getValue().setHearingDateCollection(null);
        List<String> errors = new ArrayList<>();
        caseManagementForCaseWorkerService.midEventAmendHearing(caseData, errors);
        assertThat(caseData.getHearingCollection().get(0).getValue().getHearingDateCollection()).isNull();
    }

    @Test
    void midEventAmendHearingEmptyHearingDateCollection() {
        CaseData caseData = ccdRequest21.getCaseDetails().getCaseData();
        caseData.getHearingCollection().get(0).getValue().setHearingDateCollection(new ArrayList<>());
        List<String> errors = new ArrayList<>();
        caseManagementForCaseWorkerService.midEventAmendHearing(caseData, errors);
        assertThat(caseData.getHearingCollection().get(0).getValue().getHearingDateCollection().size()).isEqualTo(0);
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
        assertThat(errors.isEmpty()).isFalse();
        assertThat(errors.get(0)).isEqualTo(LISTED_DATE_ON_WEEKEND_MESSAGE + hearingNumber);
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
        assertThat(caseData.getListedDateInPastWarning()).isEqualTo(warning);
    }

    @Test
    void amendMidEventHearingDateFridayNight() {
        CaseData caseData = createCaseWithHearingDate("2022-03-18T23:59:00.000");
        List<String> errors = new ArrayList<>();
        caseManagementForCaseWorkerService.midEventAmendHearing(caseData, errors);
        assertThat(errors.isEmpty()).isTrue();
    }

    @Test
    void amendMidEventHearingDateMondayMorning() {
        CaseData caseData = createCaseWithHearingDate("2022-03-21T00:00:00.000");
        List<String> errors = new ArrayList<>();
        caseManagementForCaseWorkerService.midEventAmendHearing(caseData, errors);
        assertThat(errors.isEmpty()).isTrue();
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
        assertThat(caseData.getHearingCollection().get(0).getValue().getHearingDateCollection().get(0).getValue()
                .getHearingStatus()).isEqualTo(HEARING_STATUS_LISTED);
        assertThat(caseData.getHearingCollection().get(0).getValue().getHearingDateCollection().get(0).getValue()
                .getHearingAberdeen().getSelectedLabel()).isEqualTo(TribunalOffice.ABERDEEN.getOfficeName());
        assertThat(caseData.getHearingCollection().get(0).getValue().getHearingDateCollection().get(0).getValue()
                .getHearingGlasgow()).isNull();

        assertThat(caseData.getHearingCollection().get(1).getValue().getHearingDateCollection().get(0).getValue()
                .getHearingGlasgow().getSelectedLabel()).isEqualTo(TribunalOffice.GLASGOW.getOfficeName());
        assertThat(caseData.getHearingCollection().get(1).getValue().getHearingDateCollection().get(0).getValue()
                .getHearingAberdeen()).isNull();
        assertThat(caseData.getHearingCollection().get(2).getValue().getHearingDateCollection().get(0).getValue()
                .getHearingEdinburgh().getSelectedLabel()).isEqualTo(TribunalOffice.EDINBURGH.getOfficeName());
        assertThat(caseData.getHearingCollection().get(0).getValue().getHearingDateCollection().get(0).getValue()
                .getHearingGlasgow()).isNull();
        final String dundee = TribunalOffice.DUNDEE.getOfficeName();
        assertThat(caseData.getHearingCollection().get(3).getValue().getHearingDateCollection().get(0).getValue()
                .getHearingDundee().getSelectedLabel()).isEqualTo(dundee);
        assertThat(caseData.getHearingCollection().get(3).getValue().getHearingDateCollection().get(0).getValue()
                .getHearingVenueDayScotland()).isEqualTo(dundee);
    }

    @Test
    void midRespondentECC() {
        when(caseRetrievalForCaseWorkerService.casesRetrievalESRequest(
                anyString(), eq(AUTH_TOKEN), anyString(), anyList()))
                .thenReturn(new ArrayList<>(Collections.singleton(submitEvent)));
        assertThat(caseManagementForCaseWorkerService.createECC(manchesterCcdRequest.getCaseDetails(), AUTH_TOKEN,
                new ArrayList<>(), MID_EVENT_CALLBACK).getRespondentECC().getListItems().size()).isEqualTo(1);
    }

    @Test
    void midRespondentECCWithStruckOut() {
        CaseData caseData = new CaseData();
        caseData.setRespondentCollection(createRespondentCollection(false));
        submitEvent.setCaseData(caseData);
        when(caseRetrievalForCaseWorkerService.casesRetrievalESRequest(
                anyString(), eq(AUTH_TOKEN), anyString(), anyList()))
                .thenReturn(new ArrayList<>(Collections.singleton(submitEvent)));
        assertThat(caseManagementForCaseWorkerService.createECC(manchesterCcdRequest.getCaseDetails(), AUTH_TOKEN,
                new ArrayList<>(), MID_EVENT_CALLBACK).getRespondentECC().getListItems().size()).isEqualTo(2);
    }

    @Test
    void midRespondentECCEmpty() {
        when(caseRetrievalForCaseWorkerService.casesRetrievalESRequest(
                anyString(), eq(AUTH_TOKEN), anyString(), anyList()))
                .thenReturn(null);
        List<String> errors = new ArrayList<>();
        caseManagementForCaseWorkerService.createECC(
                manchesterCcdRequest.getCaseDetails(), AUTH_TOKEN, errors, MID_EVENT_CALLBACK);
        assertThat(errors.toString()).isEqualTo("[Case Reference Number not found.]");
    }

    @Test
    void midRespondentECCWithNoRespondentECC() {
        when(caseRetrievalForCaseWorkerService.casesRetrievalESRequest(
                anyString(), eq(AUTH_TOKEN), anyString(), anyList()))
                .thenReturn(new ArrayList<>(Collections.singleton(submitEvent)));
        manchesterCcdRequest.getCaseDetails().getCaseData().setRespondentECC(null);
        assertThat(caseManagementForCaseWorkerService.createECC(manchesterCcdRequest.getCaseDetails(), AUTH_TOKEN,
                new ArrayList<>(), MID_EVENT_CALLBACK).getRespondentECC().getListItems().size()).isEqualTo(1);
    }

    @Test
    void createECC() {
        when(caseRetrievalForCaseWorkerService.casesRetrievalESRequest(
                anyString(), eq(AUTH_TOKEN), anyString(), anyList()))
                .thenReturn(new ArrayList<>(Collections.singleton(submitEvent)));
        CaseData casedata = caseManagementForCaseWorkerService.createECC(
                manchesterCcdRequest.getCaseDetails(), AUTH_TOKEN,
                new ArrayList<>(), ABOUT_TO_SUBMIT_EVENT_CALLBACK);
        assertThat(casedata.getCaseRefECC()).isEqualTo("11111");
        assertThat(casedata.getCaseSource()).isEqualTo(FLAG_ECC);
        assertThat(casedata.getJurCodesCollection().get(0).getId().matches(
                "[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}")).isTrue();
    }

    @Test
    void linkOriginalCaseECC() {
        when(caseRetrievalForCaseWorkerService.casesRetrievalESRequest(
                anyString(), eq(AUTH_TOKEN), anyString(), anyList()))
                .thenReturn(new ArrayList<>(Collections.singleton(submitEvent)));
        assertThat(caseManagementForCaseWorkerService.createECC(manchesterCcdRequest.getCaseDetails(), AUTH_TOKEN,
                new ArrayList<>(), SUBMITTED_CALLBACK).getCaseRefECC()).isEqualTo("11111");
    }

    @Test
    void linkOriginalCaseECCCounterClaims() {
        when(caseRetrievalForCaseWorkerService.casesRetrievalESRequest(
                anyString(), eq(AUTH_TOKEN), anyString(), anyList()))
                .thenReturn(new ArrayList<>(Collections.singleton(submitEvent)));
        assertThat(caseManagementForCaseWorkerService.createECC(manchesterCcdRequest.getCaseDetails(), AUTH_TOKEN,
                new ArrayList<>(), SUBMITTED_CALLBACK).getEccCases().get(0).getValue().getCounterClaim())
                .isEqualTo("72632632");
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
                anyString(), eq(AUTH_TOKEN), anyString(), anyList()))
                .thenReturn(new ArrayList<>(Collections.singleton(submitEvent)));
        assertThat(caseManagementForCaseWorkerService.createECC(manchesterCcdRequest.getCaseDetails(), AUTH_TOKEN,
                new ArrayList<>(), SUBMITTED_CALLBACK).getEccCases().get(0).getValue().getCounterClaim())
                .isEqualTo(c1.getValue().getCounterClaim());
        assertThat(caseManagementForCaseWorkerService.createECC(manchesterCcdRequest.getCaseDetails(), AUTH_TOKEN,
                new ArrayList<>(), SUBMITTED_CALLBACK).getEccCases().get(1).getValue().getCounterClaim())
                .isEqualTo(c2.getValue().getCounterClaim());
    }

    @Test
    @SneakyThrows
    void linkOriginalCaseECCException() {
        when(caseRetrievalForCaseWorkerService.casesRetrievalESRequest(
                anyString(), eq(AUTH_TOKEN), anyString(), anyList()))
                .thenReturn(new ArrayList<>(Collections.singleton(submitEvent)));
        when(ccdClient.submitEventForCase(
                // doesn't accept any(CCDRequest.class) most probably gets null instead of the class.
                anyString(), any(CaseData.class), anyString(), anyString(), any(), anyString()))
                .thenThrow(new InternalException(ERROR_MESSAGE));
        assertThrows(Exception.class, () ->
                caseManagementForCaseWorkerService.createECC(manchesterCcdRequest.getCaseDetails(), AUTH_TOKEN,
                        new ArrayList<>(), SUBMITTED_CALLBACK)
        );
    }

    @Test
    void createECCFromClosedCaseWithoutET3() {
        submitEvent.setState("Closed");
        submitEvent.getCaseData().getRespondentCollection().get(0).getValue().setResponseReceived(NO);
        when(caseRetrievalForCaseWorkerService.casesRetrievalESRequest(
                anyString(), eq(AUTH_TOKEN), anyString(), anyList()))
                .thenReturn(new ArrayList<>(Collections.singleton(submitEvent)));
        List<String> errors = new ArrayList<>();
        CaseData caseData = caseManagementForCaseWorkerService.createECC(
                manchesterCcdRequest.getCaseDetails(), AUTH_TOKEN, errors, MID_EVENT_CALLBACK);
        assertThat(caseData.getRespondentECC().getListItems()).isNull();
        assertThat(errors.size()).isEqualTo(2);
        submitEvent.setState("Accepted");
        submitEvent.getCaseData().getRespondentCollection().get(0).getValue().setResponseReceived(YES);
    }

    @Test
    void testSetMigratedCaseLinkDetails_Success() {
        String caseDetailsId = "123";
        String caseId = "23_457_865";
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseId(caseDetailsId);
        CaseData caseData = new CaseData();
        caseData.setCcdID(caseId);
        caseDetails.setCaseData(caseData);

        SubmitEvent submitEventSourceCase = new SubmitEvent();
        submitEventSourceCase.setCaseId(123_457_865);
        CaseData sourceCaseData = new CaseData();
        sourceCaseData.setCcdID("123_455");
        submitEventSourceCase.setCaseData(sourceCaseData);
        SubmitEvent fullSourceCase = new SubmitEvent();
        sourceCaseData.setEthosCaseReference("EthosCaseRef");
        fullSourceCase.setCaseData(sourceCaseData);

        SubmitEvent submitEventLocal = new SubmitEvent();
        submitEventLocal.setCaseId(12_345);

        when(caseRetrievalForCaseWorkerService.transferSourceCaseRetrievalESRequest(
                anyString(), anyString(), anyList()))
                .thenReturn(Pair.of("testSourceCaseType", List.of(submitEventSourceCase)));
        when(caseRetrievalForCaseWorkerService.caseRefRetrievalRequest(
                // doesn't accept anyString() as caseTypeId. Most probably it is null on runtime.
                anyString(), any(), anyString(), anyString()))
                .thenReturn(fullSourceCase.getCaseData().getEthosCaseReference());

        caseManagementForCaseWorkerService.setMigratedCaseLinkDetails(AUTH_TOKEN, caseDetails);
        String expectedValue = "<a target=\"_blank\" href=\"" + ccdGatewayBaseUrl + "/cases/case-details/"
                + submitEventSourceCase.getCaseId()
                + "\">EthosCaseRef</a>";
        assertThat(caseDetails.getCaseData().getTransferredCaseLink()).isEqualTo(expectedValue);
    }

    @Test
    void testSetMigratedCaseLinkDetails_When_SubmitEventListIsNull() {
        String caseId = "caseId";
        CaseDetails caseDetails = new CaseDetails();
        CaseData caseData = new CaseData();
        caseData.setCcdID(caseId);
        caseDetails.setCaseData(caseData);

        String authToken = "authToken";
        when(caseRetrievalForCaseWorkerService.transferSourceCaseRetrievalESRequest(
                caseId, authToken, List.of("ET_EnglandWales"))).thenReturn(null);

        caseManagementForCaseWorkerService.setMigratedCaseLinkDetails(authToken, caseDetails);

        assertThat(caseDetails.getCaseData().getTransferredCaseLink()).isNull();
    }

    @Test
    void testSetMigratedCaseLinkDetails_When_SubmitEventListIsEmpty() {
        String caseId = "caseId";
        CaseDetails caseDetails = new CaseDetails();
        CaseData caseData = new CaseData();
        caseData.setCcdID(caseId);
        caseDetails.setCaseData(caseData);
        String authToken = "authToken";
        when(caseRetrievalForCaseWorkerService.transferSourceCaseRetrievalESRequest(
                caseId, authToken, List.of("ET_EnglandWales")))
                .thenReturn(Pair.of(EMPTY_STRING, new ArrayList<>()));

        caseManagementForCaseWorkerService.setMigratedCaseLinkDetails(authToken, caseDetails);

        assertThat(caseDetails.getCaseData().getTransferredCaseLink()).isNull();
    }

    @Test
    void testSetMigratedCaseLinkDetails_When_EthosCaseReferenceIsNull() {
        String caseId = "caseId";
        CaseDetails caseDetails = new CaseDetails();
        CaseData caseData = new CaseData();
        caseData.setCcdID(caseId);
        caseDetails.setCaseData(caseData);

        List<SubmitEvent> submitEventList = new ArrayList<>();
        SubmitEvent submitEvent = new SubmitEvent();
        submitEvent.setCaseId(12_345);
        submitEventList.add(submitEvent);
        SubmitEvent fullSourceCase = new SubmitEvent();
        CaseData sourceCaseData = new CaseData();
        sourceCaseData.setEthosCaseReference(null);
        fullSourceCase.setCaseData(sourceCaseData);
        String authToken = "authToken";

        when(caseRetrievalForCaseWorkerService.transferSourceCaseRetrievalESRequest(
                caseId, authToken, List.of("ET_EnglandWales")))
                .thenReturn(Pair.of("Leeds", submitEventList));
        when(caseRetrievalForCaseWorkerService.caseRetrievalRequest(anyString(), anyString(),
                anyString(), anyString())).thenReturn(fullSourceCase);

        caseManagementForCaseWorkerService.setMigratedCaseLinkDetails(authToken, caseDetails);

        assertThat(caseDetails.getCaseData().getTransferredCaseLink()).isNull();
    }

    @Test
    void respondentExtension_defaultValueNo() {
        CaseData caseData = scotlandCcdRequest1.getCaseDetails().getCaseData();
        caseManagementForCaseWorkerService.caseDataDefaults(caseData);
        for (RespondentSumTypeItem respondentSumTypeItem : caseData.getRespondentCollection()) {
            assertThat(respondentSumTypeItem.getValue().getExtensionRequested()).isEqualTo(NO);
        }
    }

    @Test
    void setEt3ResponseDueDate_Serving_Date_Plus_28() {
        CaseData caseData = scotlandCcdRequest1.getCaseDetails().getCaseData();
        LocalDate localDate = LocalDate.now();
        String expectedEt3DueDate = localDate.plusDays(ET3_DUE_DATE_FROM_SERVING_DATE).toString();
        caseData.setClaimServedDate(localDate.toString());
        caseManagementForCaseWorkerService.setEt3ResponseDueDate(caseData);
        assertThat(caseData.getEt3DueDate()).isEqualTo(expectedEt3DueDate);
    }

    @Test
    void updateWorkAllocationField_Errors() {
        List<String> errors = new ArrayList<>();
        errors.add("Test");
        CaseData caseData = scotlandCcdRequest1.getCaseDetails().getCaseData();

        caseManagementForCaseWorkerService.updateWorkAllocationField(errors, caseData);

        assertThat(caseData.getRespondentCollection().get(0).getValue().getResponseReceivedCount()).isNull();
    }

    @Test
    void updateWorkAllocationField_FeatureFlagFalse() {
        List<String> errors = new ArrayList<>();
        CaseData caseData = scotlandCcdRequest1.getCaseDetails().getCaseData();

        when(featureToggleService.isWorkAllocationEnabled()).thenReturn(false);

        caseManagementForCaseWorkerService.updateWorkAllocationField(errors, caseData);

        assertThat(caseData.getRespondentCollection().get(0).getValue().getResponseReceivedCount()).isNull();
    }

    @Test
    void updateWorkAllocationField_NoRespondents() {
        List<String> errors = new ArrayList<>();
        CaseData caseData = scotlandCcdRequest1.getCaseDetails().getCaseData();
        caseData.setRespondentCollection(null);

        caseManagementForCaseWorkerService.updateWorkAllocationField(errors, caseData);

        assertThat(caseData.getRespondentCollection()).isNull();
    }

    @Test
    void updateWorkAllocationField_ResponseReceivedIsNull() {
        List<String> errors = new ArrayList<>();
        CaseData caseData = scotlandCcdRequest1.getCaseDetails().getCaseData();

        caseManagementForCaseWorkerService.updateWorkAllocationField(errors, caseData);

        assertThat(caseData.getRespondentCollection().get(0).getValue().getResponseReceivedCount()).isNull();
    }

    @Test
    void updateWorkAllocationField_NoResponseReceived() {
        List<String> errors = new ArrayList<>();
        CaseData caseData = scotlandCcdRequest1.getCaseDetails().getCaseData();
        caseData.getRespondentCollection().get(0).getValue().setResponseReceived(NO);

        caseManagementForCaseWorkerService.updateWorkAllocationField(errors, caseData);

        assertThat(caseData.getRespondentCollection().get(0).getValue().getResponseReceivedCount()).isNull();
    }

    @Test
    void updateWorkAllocationField_ResponseReceived_FirstTime() {
        List<String> errors = new ArrayList<>();
        CaseData caseData = scotlandCcdRequest1.getCaseDetails().getCaseData();
        caseData.getRespondentCollection().get(0).getValue().setResponseReceived(YES);

        caseManagementForCaseWorkerService.updateWorkAllocationField(errors, caseData);

        assertThat(caseData.getRespondentCollection().get(0).getValue().getResponseReceivedCount())
                .isEqualTo("1");
    }

    @Test
    void updateWorkAllocationField_ResponseReceived_NthTime() {
        List<String> errors = new ArrayList<>();
        CaseData caseData = scotlandCcdRequest1.getCaseDetails().getCaseData();
        caseData.getRespondentCollection().get(0).getValue().setResponseReceived(YES);
        caseData.getRespondentCollection().get(0).getValue().setResponseReceivedCount("1");

        caseManagementForCaseWorkerService.updateWorkAllocationField(errors, caseData);

        assertThat(caseData.getRespondentCollection().get(0).getValue().getResponseReceivedCount()).isEqualTo("2");
    }

    @Test
    void setNextListedDate() {
        DateListedTypeItem dateListedTypeItem = new DateListedTypeItem();
        dateListedTypeItem.setId(UUID.randomUUID().toString());
        DateListedType dateListedType = new DateListedType();
        dateListedType.setListedDate(LocalDateTime.now().plusDays(2).toDateTime().toLocalDateTime().toString());
        dateListedType.setHearingStatus(HEARING_STATUS_LISTED);
        dateListedTypeItem.setValue(dateListedType);
        CaseData caseData = scotlandCcdRequest1.getCaseDetails().getCaseData();
        List<DateListedTypeItem> dateListedTypeItems =
                caseData.getHearingCollection().get(0).getValue().getHearingDateCollection();
        dateListedTypeItems.add(dateListedTypeItem);
        caseData.getHearingCollection().get(0).getValue().setHearingDateCollection(dateListedTypeItems);
        String expectedNextListedDate = LocalDate.now().plusDays(2).toString();
        caseManagementForCaseWorkerService.setNextListedDate(caseData);
        assertThat(caseData.getNextListedDate()).isEqualTo(expectedNextListedDate);
    }

    @Test
    void setEt3ResponseDueDate_doesNotOverideExistingValue() {
        CaseData caseData = scotlandCcdRequest1.getCaseDetails().getCaseData();
        LocalDate localDate = LocalDate.now();
        caseData.setClaimServedDate(localDate.toString());
        caseData.setEt3DueDate("12/06/2023");
        caseManagementForCaseWorkerService.setEt3ResponseDueDate(caseData);
        assertThat(caseData.getEt3DueDate()).isEqualTo("12/06/2023");
    }

    @Test
    void setEt3ResponseDueDate_Serving_Date_Empty() {
        CaseData caseData = scotlandCcdRequest1.getCaseDetails().getCaseData();
        caseData.setClaimServedDate("");
        caseManagementForCaseWorkerService.setEt3ResponseDueDate(caseData);
        assertThat(caseData.getEt3DueDate()).isNull();
    }

    @Test
    void respondentExtension_doesNotOverideExistingValue() {
        CaseData caseData = scotlandCcdRequest3.getCaseDetails().getCaseData();
        caseData.getRespondentCollection().get(0).getValue().setExtensionRequested(YES);
        caseManagementForCaseWorkerService.caseDataDefaults(caseData);
        assertThat(caseData.getRespondentCollection().get(0).getValue().getExtensionRequested()).isEqualTo(YES);
    }

    @Test
    void testNotSetScotlandAllocatedOfficeWhenCaseTypeIdNotScotland() {
        CaseData caseData = new CaseData();
        caseData.setManagingOffice(UNASSIGNED_OFFICE);
        caseManagementForCaseWorkerService.setScotlandAllocatedOffice(ENGLANDWALES_CASE_TYPE_ID, caseData);
        assertThat(caseData.getAllocatedOffice()).isNull();
    }

    @Test
    void testSetScotlandAllocatedOfficeManagingOfficeUnassigned() {
        CaseData caseData = new CaseData();
        caseData.setManagingOffice(UNASSIGNED_OFFICE);
        caseManagementForCaseWorkerService.setScotlandAllocatedOffice(SCOTLAND_CASE_TYPE_ID, caseData);
        assertThat(caseData.getAllocatedOffice()).isNull();
    }

    @Test
    void testSetScotlandAllocatedOfficeManagingOfficeNull() {
        CaseData caseData = new CaseData();
        caseData.setManagingOffice(null);
        caseManagementForCaseWorkerService.setScotlandAllocatedOffice(SCOTLAND_CASE_TYPE_ID, caseData);
        assertThat(caseData.getAllocatedOffice()).isNull();
    }

    @Test
    void testSetScotlandAllocatedOfficeManagingOfficeGlasgow() {
        CaseData caseData = new CaseData();
        caseData.setManagingOffice(TribunalOffice.GLASGOW.getOfficeName());
        String expectedAllocatedOffice = TribunalOffice.GLASGOW.getOfficeName();
        String expectedManagingOffice = TribunalOffice.GLASGOW.getOfficeName();
        caseManagementForCaseWorkerService.setScotlandAllocatedOffice(SCOTLAND_CASE_TYPE_ID, caseData);
        assertThat(caseData.getAllocatedOffice()).isEqualTo(expectedAllocatedOffice);
        assertThat(caseData.getManagingOffice()).isEqualTo(expectedManagingOffice);
    }

    @Test
    @SneakyThrows
    void setHmctsServiceIdSupplementary_success() {
        Map<String, Object> payload = Map.of("supplementary_data_updates", Map.of("$set", Map.of("HMCTSServiceId",
                HMCTS_SERVICE_ID)));
        CaseDetails caseDetails = ccdRequest10.getCaseDetails();
        when(ccdClient.setSupplementaryData(AUTH_TOKEN, payload, ccdRequest10.getCaseDetails().getCaseId()))
                .thenReturn(ResponseEntity.ok().build());

        caseManagementForCaseWorkerService.setHmctsServiceIdSupplementary(caseDetails);
        verify(ccdClient, times(1)).setSupplementaryData(AUTH_TOKEN, payload,
                ccdRequest10.getCaseDetails().getCaseId());
    }

    @Test
    @SneakyThrows
    void setHmctsServiceIdSupplementary_noResponse() {
        Map<String, Object> payload = Map.of("supplementary_data_updates", Map.of("$set", Map.of("HMCTSServiceId",
                HMCTS_SERVICE_ID)));
        CaseDetails caseDetails = ccdRequest10.getCaseDetails();
        when(ccdClient.setSupplementaryData(AUTH_TOKEN, payload, ccdRequest10.getCaseDetails().getCaseId()))
                .thenReturn(null);

        Exception exception = assertThrows(CaseCreationException.class,
                () -> caseManagementForCaseWorkerService.setHmctsServiceIdSupplementary(caseDetails));
        assertThat(exception.getMessage()).isEqualTo("Call to Supplementary Data API failed for 123456789");
    }

    @Test
    @SneakyThrows
    void setHmctsServiceIdSupplementary_failedResponse() {
        Map<String, Object> payload = Map.of("supplementary_data_updates", Map.of("$set", Map.of("HMCTSServiceId",
                HMCTS_SERVICE_ID)));
        CaseDetails caseDetails = ccdRequest10.getCaseDetails();
        when(ccdClient.setSupplementaryData(AUTH_TOKEN, payload, ccdRequest10.getCaseDetails().getCaseId()))
                .thenThrow(new RestClientResponseException("call failed", 400, "Bad Request", null, null, null));

        Exception exception = assertThrows(CaseCreationException.class,
                () -> caseManagementForCaseWorkerService.setHmctsServiceIdSupplementary(caseDetails));
        assertThat(exception.getMessage())
                .isEqualTo("Call to Supplementary Data API failed for 123456789 with call failed");
    }

    @Test
    @SneakyThrows
    void removeHmctsServiceIdSupplementary_success() {
        Map<String, Object> payload = Map.of("supplementary_data_updates", Map.of("$set", Map.of()));
        CaseDetails caseDetails = ccdRequest10.getCaseDetails();
        when(ccdClient.setSupplementaryData(AUTH_TOKEN, payload, ccdRequest10.getCaseDetails().getCaseId()))
                .thenReturn(ResponseEntity.ok().build());

        caseManagementForCaseWorkerService.removeHmctsServiceIdSupplementary(caseDetails);
        verify(ccdClient, times(1)).setSupplementaryData(AUTH_TOKEN, payload,
                ccdRequest10.getCaseDetails().getCaseId());
    }

    @Test
    @SneakyThrows
    void removeHmctsServiceIdSupplementary_noResponse() {
        Map<String, Object> payload = Map.of("supplementary_data_updates", Map.of("$set", Map.of()));
        CaseDetails caseDetails = ccdRequest10.getCaseDetails();
        when(ccdClient.setSupplementaryData(AUTH_TOKEN, payload, ccdRequest10.getCaseDetails().getCaseId()))
                .thenReturn(null);

        Exception exception = assertThrows(CaseCreationException.class,
                () -> caseManagementForCaseWorkerService.removeHmctsServiceIdSupplementary(caseDetails));
        assertThat(exception.getMessage()).isEqualTo("Call to Supplementary Data API failed for 123456789");
    }

    @Test
    @SneakyThrows
    void removeHmctsServiceIdSupplementary_failedResponse() {
        Map<String, Object> payload = Map.of("supplementary_data_updates", Map.of("$set", Map.of()));
        CaseDetails caseDetails = ccdRequest10.getCaseDetails();
        when(ccdClient.setSupplementaryData(AUTH_TOKEN, payload, ccdRequest10.getCaseDetails().getCaseId()))
                .thenThrow(new RestClientResponseException("call failed", 400, "Bad Request", null, null, null));

        Exception exception = assertThrows(CaseCreationException.class,
                () -> caseManagementForCaseWorkerService.removeHmctsServiceIdSupplementary(caseDetails));
        assertThat(exception.getMessage())
                .isEqualTo("Call to Supplementary Data API failed for 123456789 with call failed");
    }

    @Test
    void testPublicCaseName() {
        CaseData caseData = new CaseData();
        caseData.setClaimant("claimant");
        caseData.setRespondent("respondent");

        caseManagementForCaseWorkerService.setPublicCaseName(caseData);

        assertThat(caseData.getPublicCaseName()).isEqualTo("claimant vs respondent");
    }

    @Test
    void testPublicCaseNameWithRule50() {
        CaseData caseData = new CaseData();
        caseData.setClaimant("Person1");
        caseData.setRespondent("Person2");
        RestrictedReportingType restrictedReportingType = new RestrictedReportingType();
        restrictedReportingType.setRule503b(YES);
        caseData.setRestrictedReporting(restrictedReportingType);

        caseManagementForCaseWorkerService.setPublicCaseName(caseData);

        assertThat(caseData.getPublicCaseName()).isEqualTo(CLAIMANT_TITLE + " vs " + RESPONDENT_TITLE);
    }

    @Test
    void testClaimantDefaultsAddsClaimantIdWhenHmcFlagTruthy() {
        CaseData caseData = new CaseData();
        when(featureToggleService.isHmcEnabled()).thenReturn(true);
        caseManagementForCaseWorkerService.claimantDefaults(caseData);
        assertThat(caseData.getClaimantId()).isNotNull();
    }

    @Test
    void testClaimantDefaultsDoesNotAddClaimantIdWhenHmcFlagFalsy() {
        CaseData caseData = new CaseData();
        when(featureToggleService.isHmcEnabled()).thenReturn(false);
        caseManagementForCaseWorkerService.claimantDefaults(caseData);
        assertThat(caseData.getClaimantId()).isNull();
    }

    @Test
    @SneakyThrows
    void testSetNextListedDateOnMultiple() {
        CaseDetails details = new CaseDetails();
        details.setCaseData(new CaseData());
        details.setCaseTypeId(ENGLANDWALES_CASE_TYPE_ID);
        details.getCaseData().setMultipleReference("6000001");
        String nextListedDate = "2020-03-05";
        details.getCaseData().setNextListedDate(nextListedDate);
        details.getCaseData().setLeadClaimant(YES);

        MultipleData multipleData = MultipleData.builder().build();
        SubmitMultipleEvent event = new SubmitMultipleEvent();
        event.setCaseId(1_716_474_017_962_374L);
        event.setCaseData(multipleData);
        String adminToken = "adminToken";
        when(adminUserService.getAdminUserToken()).thenReturn(adminToken);
        when(multipleReferenceService.getMultipleByReference(anyString(), anyString(), anyString())).thenReturn(event);
        caseManagementForCaseWorkerService.setNextListedDateOnMultiple(details);

        assertThat(multipleData.getNextListedDate()).isEqualTo(nextListedDate);
        multipleData.setNextListedDate(nextListedDate);
        verify(multipleCasesSendingService, times(1))
            .sendUpdateToMultiple(adminToken,
                ENGLANDWALES_BULK_CASE_TYPE_ID,
                EMPLOYMENT,
                multipleData,
                "1716474017962374");
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

    private List<RepresentedTypeRItem> createRepCollection() {
        RepresentedTypeRItem representedTypeRItem1 = createRepresentedTypeR(
                "", "RepresentativeNameAAA");
        RepresentedTypeRItem representedTypeRItem2 = createRepresentedTypeR(
                "dummy", "RepresentativeNameBBB");
        RepresentedTypeRItem representedTypeRItem3 = createRepresentedTypeR(
                "RespondentName1", "RepresentativeNameCCC");
        return new ArrayList<>(Arrays.asList(
                    representedTypeRItem1, representedTypeRItem2, representedTypeRItem3));
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

    @ParameterizedTest
    @MethodSource("individualClaimantNames")
    void testIndividualClaimantNames(String firstName, String lastName,  String expected) {
        CaseData caseData = ccdRequest22.getCaseDetails().getCaseData();
        caseData.getClaimantIndType().setClaimantFirstNames(firstName);
        caseData.getClaimantIndType().setClaimantLastName(lastName);
        caseManagementForCaseWorkerService.caseDataDefaults(caseData);
        assertThat(caseData.getClaimant()).isEqualTo(expected);
    }

    private static Stream<Arguments> individualClaimantNames() {
        return Stream.of(Arguments.of("John", "Doe", "John Doe"),
                Arguments.of("John ", " Doe", "John Doe"),
                Arguments.of(" John", " Doe", "John Doe"),
                Arguments.of(" John ", " Doe ", "John Doe"));
    }
}