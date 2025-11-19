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
import org.mockito.MockedStatic;
import org.mockito.Mockito;
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
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.HearingsHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.MultipleCasesSendingService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.multiples.MultipleReferenceService;

import java.io.IOException;
import java.net.URISyntaxException;
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
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.EMPLOYMENT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_BULK_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ET3_DUE_DATE_FROM_SERVING_DATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_LISTED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SINGLE_CASE_TYPE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.ACAS_DOC_TYPE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.EMPTY_STRING;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.ET1_ATTACHMENT_DOC_TYPE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.ET1_DOC_TYPE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementForCaseWorkerService.LISTED_DATE_ON_WEEKEND_MESSAGE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementForCaseWorkerService.ORGANISATION;

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
    void setUp() throws URISyntaxException, IOException {

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
                caseRetrievalForCaseWorkerService, ccdClient, featureToggleService, HMCTS_SERVICE_ID,
                adminUserService, caseManagementLocationService, multipleReferenceService, ccdGatewayBaseUrl,
                multipleCasesSendingService);
    }

    private void setScotlandCaseRequests() throws URISyntaxException, IOException {
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

    private void setGenericCaseRequests() throws URISyntaxException, IOException {
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
        assertThat(caseData.getClaimantDocumentCollection()).hasSize(3);
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
        caseData.getRespondentCollection().getFirst().getValue().setResponseReceived(YES);
        caseManagementForCaseWorkerService.caseDataDefaults(caseData);
        assertThat(caseData.getRespondentCollection().getFirst().getValue().getResponseReceived()).isEqualTo(YES);
        for (RespondentSumTypeItem respondentSumTypeItem : caseData.getRespondentCollection()) {
            if (!respondentSumTypeItem.equals(caseData.getRespondentCollection().getFirst())) {
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

        assertThat(caseData.getRespondentCollection()).hasSize(3);
        assertThat(caseData.getRespondentCollection().getFirst().getValue().getRespondentName())
                .isEqualTo("Antonio Vazquez");
        assertThat(caseData.getRespondentCollection().getFirst().getValue().getResponseStruckOut()).isEqualTo(NO);
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
        assertThat(caseData.getRespondentCollection()).hasSize(1);
        assertThat(caseData.getRespondentCollection().getFirst().getValue().getRespondentName())
                .isEqualTo("Antonio Vazquez");
        assertThat(caseData.getRespondentCollection().getFirst().getValue().getResponseStruckOut()).isEqualTo(NO);
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
        assertThat(caseData.getDateToPosition()).isEqualTo(LocalDate.now().toString());

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
        assertThat(caseData.getDateToPosition()).isEqualTo(LocalDate.now().toString());
    }

    @Test
    void struckOutRespondentEmpty() {
        CaseData caseData = caseManagementForCaseWorkerService.struckOutRespondents(scotlandCcdRequest5);
        assertThat(caseData.getRespondentCollection()).isEmpty();
    }

    @Test
    void struckOutRespondentFirstToLast() {
        CaseData caseData = caseManagementForCaseWorkerService.struckOutRespondents(scotlandCcdRequest1);

        assertThat(caseData.getRespondentCollection()).hasSize(3);

        assertThat(caseData.getRespondentCollection().getFirst().getValue().getRespondentName())
                .isEqualTo("Antonio Vazquez");
        assertThat(caseData.getRespondentCollection().getFirst().getValue().getResponseStruckOut()).isEqualTo(NO);
        assertThat(caseData.getRespondentCollection().get(1).getValue().getRespondentName())
                .isEqualTo("Roberto Dondini");
        assertThat(caseData.getRespondentCollection().get(1).getValue().getResponseStruckOut()).isEqualTo(NO);
        assertThat(caseData.getRespondentCollection().get(2).getValue().getRespondentName()).isEqualTo("Juan Garcia");
        assertThat(caseData.getRespondentCollection().get(2).getValue().getResponseStruckOut()).isEqualTo(YES);
    }

    @Test
    void struckOutRespondentRespAddressLinesEmpty() {
        scotlandCcdRequest1.getCaseDetails().getCaseData().getRespondentCollection().getFirst().getValue()
                .setResponseRespondentAddress(new Address());
        scotlandCcdRequest1.getCaseDetails().getCaseData().getRespondentCollection().getFirst().getValue()
                .getResponseRespondentAddress().setAddressLine1("");
        scotlandCcdRequest1.getCaseDetails().getCaseData().getRespondentCollection().getFirst().getValue()
                .getResponseRespondentAddress().setAddressLine2("");
        scotlandCcdRequest1.getCaseDetails().getCaseData().getRespondentCollection().getFirst().getValue()
                .getResponseRespondentAddress().setAddressLine3("");
        scotlandCcdRequest1.getCaseDetails().getCaseData().getRespondentCollection().getFirst().getValue()
                .getResponseRespondentAddress().setCountry("");
        scotlandCcdRequest1.getCaseDetails().getCaseData().getRespondentCollection().getFirst().getValue()
                .getResponseRespondentAddress().setCounty("");
        scotlandCcdRequest1.getCaseDetails().getCaseData().getRespondentCollection().getFirst().getValue()
                .getResponseRespondentAddress().setPostCode("");
        scotlandCcdRequest1.getCaseDetails().getCaseData().getRespondentCollection().getFirst().getValue()
                .getResponseRespondentAddress().setPostTown("");
        CaseData caseData = caseManagementForCaseWorkerService.struckOutRespondents(scotlandCcdRequest1);
        assertThat(caseData.getRespondentCollection()).hasSize(3);
        assertThat(caseData.getRespondentCollection().getFirst().getValue().getResponseRespondentAddress()
                .getAddressLine1()).isEmpty();
        assertThat(caseData.getRespondentCollection().getFirst().getValue().getResponseRespondentAddress()
                .getAddressLine2()).isEmpty();
        assertThat(caseData.getRespondentCollection().getFirst().getValue().getResponseRespondentAddress()
                .getAddressLine3()).isEmpty();
        assertThat(caseData.getRespondentCollection().getFirst().getValue().getResponseRespondentAddress().getCountry())
                .isEmpty();
        assertThat(caseData.getRespondentCollection().getFirst().getValue().getResponseRespondentAddress().getCounty())
                .isEmpty();
        assertThat(caseData.getRespondentCollection().getFirst().getValue().getResponseRespondentAddress()
                .getPostCode()).isEmpty();
        assertThat(caseData.getRespondentCollection().getFirst().getValue().getResponseRespondentAddress()
                .getPostTown()).isEmpty();
    }

    @Test
    void struckOutRespondentUnchanged() {
        CaseData caseData = caseManagementForCaseWorkerService.struckOutRespondents(scotlandCcdRequest3);
        assertThat(caseData.getRespondentCollection()).hasSize(1);
        assertThat(caseData.getRespondentCollection().getFirst().getValue().getRespondentName())
                .isEqualTo("Antonio Vazquez");
    }

    @Test
    void continuingRespondentFirstToLast() {
        CaseData caseData = caseManagementForCaseWorkerService.continuingRespondent(scotlandCcdRequest1);
        assertThat(caseData.getRespondentCollection()).hasSize(3);
        assertThat(caseData.getRespondentCollection().getFirst().getValue().getRespondentName())
                .isEqualTo("Antonio Vazquez");
        assertThat(caseData.getRespondentCollection().getFirst().getValue().getResponseContinue()).isEqualTo(YES);
        assertThat(caseData.getRespondentCollection().get(1).getValue().getRespondentName()).isEqualTo("Juan Garcia");
        assertThat(caseData.getRespondentCollection().get(1).getValue().getResponseContinue()).isEqualTo(YES);
        assertThat(caseData.getRespondentCollection().get(2).getValue().getRespondentName())
                .isEqualTo("Roberto Dondini");
        assertThat(caseData.getRespondentCollection().get(2).getValue().getResponseContinue()).isEqualTo(NO);
    }

    @Test
    void continuingRespondentNull() {
        CaseData caseData = caseManagementForCaseWorkerService.continuingRespondent(scotlandCcdRequest3);
        assertThat(caseData.getRespondentCollection()).hasSize(1);
        assertThat(caseData.getRespondentCollection().getFirst().getValue().getResponseContinue()).isEqualTo(YES);
    }

    @Test
    void continuingRespondentEmpty() {
        CaseData caseData = caseManagementForCaseWorkerService.continuingRespondent(scotlandCcdRequest5);
        assertThat(caseData.getRespondentCollection()).isEmpty();
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

    private CaseDetails generateCaseDetails(String jsonFileName) throws URISyntaxException, IOException {
        String json = new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(Thread.currentThread()
            .getContextClassLoader().getResource(jsonFileName)).toURI())));
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, CaseDetails.class);
    }

    @Test
    void amendHearingNonScotland() {
        CaseData caseData = ccdRequest13.getCaseDetails().getCaseData();
        caseManagementForCaseWorkerService.amendHearing(caseData, ENGLANDWALES_CASE_TYPE_ID);
        assertThat(caseData.getHearingCollection().getFirst().getValue().getHearingDateCollection().getFirst()
                .getValue().getHearingStatus()).isEqualTo(HEARING_STATUS_LISTED);
        assertThat(caseData.getHearingCollection().get(1).getValue().getHearingDateCollection().getFirst().getValue()
                .getHearingStatus()).isEqualTo(HEARING_STATUS_LISTED);
        assertThat(caseData.getHearingCollection().get(2).getValue().getHearingDateCollection().getFirst().getValue()
                .getHearingStatus()).isEqualTo(HEARING_STATUS_LISTED);
        assertThat(caseData.getHearingCollection().get(2).getValue().getHearingDateCollection().getFirst().getValue()
                .getHearingStatus()).isEqualTo(HEARING_STATUS_LISTED);
        assertThat(caseData.getHearingCollection().getFirst().getValue().getHearingDateCollection().getFirst()
                .getValue().getHearingVenueDay().getSelectedLabel()).isEqualTo("Manchester");
        assertThat(caseData.getHearingCollection().getFirst().getValue().getHearingDateCollection().getFirst()
                .getValue().getHearingTimingStart()).isEqualTo("2019-11-01T12:11:00.000");
        assertThat(caseData.getHearingCollection().getFirst().getValue().getHearingDateCollection().getFirst()
                .getValue().getHearingTimingFinish()).isEqualTo("2019-11-01T12:11:00.000");
    }

    @Test
    void amendHearing_sortsHearingDateBranchesChronologically() {

        HearingType hearingType1 = new HearingType();
        hearingType1.setHearingNumber("1");
        List<DateListedTypeItem> hearing1Dates = new ArrayList<>();
        hearing1Dates.add(listing("2024-01-12T10:00:00.000"));
        hearing1Dates.add(listing("2024-01-10T10:00:00.000"));
        hearing1Dates.add(listing("2024-01-11T10:00:00.000"));
        hearingType1.setHearingDateCollection(hearing1Dates);
        HearingTypeItem hearing1 = new HearingTypeItem();
        hearing1.setValue(hearingType1);

        HearingType hearingType2 = new HearingType();
        hearingType2.setHearingNumber("2");
        List<DateListedTypeItem> hearing2Dates = new ArrayList<>();
        hearing2Dates.add(listing("2025-03-05T14:00:00.000"));
        hearing2Dates.add(listing("2025-03-01T09:00:00.000"));
        hearingType2.setHearingDateCollection(hearing2Dates);
        HearingTypeItem hearing2 = new HearingTypeItem();
        hearing2.setValue(hearingType2);

        // Build a minimal case with two hearings, each with unsorted listings
        CaseData caseData = new CaseData();
        caseData.setHearingCollection(List.of(hearing1, hearing2));

        // Act
        caseManagementForCaseWorkerService.amendHearing(caseData, ENGLANDWALES_CASE_TYPE_ID);

        // Assert: hearing1 sorted 10th, 11th, 12th
        List<DateListedTypeItem> sorted1 =
                caseData.getHearingCollection().getFirst().getValue().getHearingDateCollection();
        assertThat(sorted1.getFirst().getValue().getListedDate()).isEqualTo("2024-01-10T10:00:00.000");
        assertThat(sorted1.get(1).getValue().getListedDate()).isEqualTo("2024-01-11T10:00:00.000");
        assertThat(sorted1.get(2).getValue().getListedDate()).isEqualTo("2024-01-12T10:00:00.000");

        // Assert: hearing2 sorted 1st then 5th
        List<DateListedTypeItem> sorted2 = caseData.getHearingCollection().get(1).getValue().getHearingDateCollection();
        assertThat(sorted2.getFirst().getValue().getListedDate()).isEqualTo("2025-03-01T09:00:00.000");
        assertThat(sorted2.get(1).getValue().getListedDate()).isEqualTo("2025-03-05T14:00:00.000");

        // Status should default to LISTED after amend
        assertThat(sorted1.getFirst().getValue().getHearingStatus()).isEqualTo(HEARING_STATUS_LISTED);
        assertThat(sorted2.getFirst().getValue().getHearingStatus()).isEqualTo(HEARING_STATUS_LISTED);
    }

    @Test
    void amendHearing_sortsMixedDateFormatsAndNullsLast() {
        // Given a hearing with mixed date formats and invalid entries
        List<DateListedTypeItem> items = new ArrayList<>();
        DateListedTypeItem oldFmt = listing("2025-01-01T09:00:00.000");
        DateListedTypeItem isoNoMillis = listing("2024-12-31T09:00:00");
        DateListedTypeItem nullDate = new DateListedTypeItem();
        nullDate.setValue(new DateListedType());
        DateListedTypeItem invalid = listing("not-a-date");
        DateListedTypeItem empty = listing("");

        // Deliberately add in non-chronological order
        items.add(oldFmt);
        items.add(isoNoMillis);
        items.add(empty);
        items.add(invalid);
        items.add(nullDate);
        HearingType hearingType = new HearingType();
        hearingType.setHearingDateCollection(items);
        HearingTypeItem hearing = new HearingTypeItem();
        hearing.setValue(hearingType);

        CaseData caseData = new CaseData();
        caseData.setHearingCollection(List.of(hearing));

        // When
        caseManagementForCaseWorkerService.amendHearing(caseData, ENGLANDWALES_CASE_TYPE_ID);

        // Then: valid dates sorted ascending, null/invalid at the end preserving stable order
        List<DateListedTypeItem> sorted =
                caseData.getHearingCollection().getFirst().getValue().getHearingDateCollection();
        assertThat(sorted.get(0).getValue().getListedDate()).isEqualTo("2024-12-31T09:00:00");
        assertThat(sorted.get(1).getValue().getListedDate()).isEqualTo("2025-01-01T09:00:00.000");
        assertThat(sorted.get(2).getValue().getListedDate()).isEmpty();
        assertThat(sorted.get(3).getValue().getListedDate()).isEqualTo("not-a-date");
        assertThat(sorted.get(4).getValue().getListedDate()).isNull();

        // And status defaulted to LISTED for processed items
        assertThat(sorted.get(0).getValue().getHearingStatus()).isEqualTo(HEARING_STATUS_LISTED);
        assertThat(sorted.get(1).getValue().getHearingStatus()).isEqualTo(HEARING_STATUS_LISTED);
    }

    @Test
    void amendHearing_respectsFractionalSecondsOrder() {
        // Given two entries at the same second, one with millis, one without
        HearingType hearingType = new HearingType();
        List<DateListedTypeItem> items = new ArrayList<>();
        DateListedTypeItem laterNanos = listing("2025-03-01T09:00:00.123");
        DateListedTypeItem base = listing("2025-03-01T09:00:00");
        // Add out of order to verify sort
        items.add(laterNanos);
        items.add(base);
        hearingType.setHearingDateCollection(items);
        HearingTypeItem hearing = new HearingTypeItem();
        hearing.setValue(hearingType);
        CaseData caseData = new CaseData();
        caseData.setHearingCollection(List.of(hearing));

        // When
        caseManagementForCaseWorkerService.amendHearing(caseData, ENGLANDWALES_CASE_TYPE_ID);

        // Then: base second precedes the nanos variant after sort
        List<DateListedTypeItem> sorted =
                caseData.getHearingCollection().getFirst().getValue().getHearingDateCollection();
        assertThat(sorted.get(0).getValue().getListedDate()).isEqualTo("2025-03-01T09:00:00");
        assertThat(sorted.get(1).getValue().getListedDate()).isEqualTo("2025-03-01T09:00:00.123");
    }

    private static DateListedTypeItem listing(String listedDate) {
        DateListedType dateListedType = new DateListedType();
        dateListedType.setListedDate(listedDate);
        DateListedTypeItem item = new DateListedTypeItem();
        item.setValue(dateListedType);
        return item;
    }

    @Test
    void amendHearingEmptyHearingCollection() {
        CaseData caseData = ccdRequest21.getCaseDetails().getCaseData();
        caseData.setHearingCollection(new ArrayList<>());
        caseManagementForCaseWorkerService.amendHearing(caseData, ENGLANDWALES_CASE_TYPE_ID);
        assertThat(caseData.getHearingCollection()).isEmpty();
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
        caseData.getHearingCollection().getFirst().getValue().setHearingDateCollection(null);
        caseManagementForCaseWorkerService.amendHearing(caseData, ENGLANDWALES_CASE_TYPE_ID);
        assertThat(caseData.getHearingCollection().getFirst().getValue().getHearingDateCollection()).isNull();
    }

    @Test
    void amendHearingEmptyHearingDateCollection() {
        CaseData caseData = ccdRequest21.getCaseDetails().getCaseData();
        caseData.getHearingCollection().getFirst().getValue().setHearingDateCollection(new ArrayList<>());
        caseManagementForCaseWorkerService.amendHearing(caseData, ENGLANDWALES_CASE_TYPE_ID);
        assertThat(caseData.getHearingCollection().getFirst().getValue().getHearingDateCollection()).isEmpty();
    }

    @Test
    void midEventAmendHearingEmptyHearingCollection() {
        CaseData caseData = ccdRequest21.getCaseDetails().getCaseData();
        caseData.setHearingCollection(new ArrayList<>());
        List<String> errors = new ArrayList<>();
        caseManagementForCaseWorkerService.midEventAmendHearing(caseData, errors);
        assertThat(caseData.getHearingCollection()).isEmpty();
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
        caseData.getHearingCollection().getFirst().getValue().setHearingDateCollection(null);
        List<String> errors = new ArrayList<>();
        caseManagementForCaseWorkerService.midEventAmendHearing(caseData, errors);
        assertThat(caseData.getHearingCollection().getFirst().getValue().getHearingDateCollection()).isNull();
    }

    @Test
    void midEventAmendHearingEmptyHearingDateCollection() {
        CaseData caseData = ccdRequest21.getCaseDetails().getCaseData();
        caseData.getHearingCollection().getFirst().getValue().setHearingDateCollection(new ArrayList<>());
        List<String> errors = new ArrayList<>();
        caseManagementForCaseWorkerService.midEventAmendHearing(caseData, errors);
        assertThat(caseData.getHearingCollection().getFirst().getValue().getHearingDateCollection()).isEmpty();
    }

    @Test
    void midEventAmendHearingDateOnWeekend() {
        CaseData caseData = ccdRequest13.getCaseDetails().getCaseData();
        List<String> errors = new ArrayList<>();
        caseData.getHearingCollection().getFirst()
                .getValue().getHearingDateCollection()
                .getFirst().getValue().setListedDate("2022-03-19T12:11:00.000");
        String hearingNumber = caseData.getHearingCollection().getFirst().getValue().getHearingNumber();
        caseManagementForCaseWorkerService.midEventAmendHearing(caseData, errors);
        assertThat(errors).isNotEmpty();
        assertThat(errors.getFirst()).isEqualTo(LISTED_DATE_ON_WEEKEND_MESSAGE + hearingNumber);
    }

    @ParameterizedTest
    @CsvSource({"Listed, Yes", " , Yes"})
    void midEventAmendHearingDateInPast(String hearingStatus, String warning) {
        CaseData caseData = ccdRequest13.getCaseDetails().getCaseData();
        List<String> errors = new ArrayList<>();
        DateListedType dateListedType = caseData.getHearingCollection().getFirst()
                .getValue().getHearingDateCollection()
                .getFirst().getValue();
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
        assertThat(errors).isEmpty();
    }

    @Test
    void amendMidEventHearingDateMondayMorning() {
        CaseData caseData = createCaseWithHearingDate("2022-03-21T00:00:00.000");
        List<String> errors = new ArrayList<>();
        caseManagementForCaseWorkerService.midEventAmendHearing(caseData, errors);
        assertThat(errors).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
        "' 1 ', false",
        "'-1', true",
        "'0', true",
        "'Test', true",
        "'1.5', false",
        "'-1.5', true"
    })
    void amendMidEventHearingEstLengthNum(String input, boolean expectError) {
        CaseData caseData = createCaseWithHearingDate("2022-03-21T00:00:00.000");
        caseData.getHearingCollection().getFirst().getValue().setHearingEstLengthNum(input);
        List<String> errors = new ArrayList<>();

        caseManagementForCaseWorkerService.midEventAmendHearing(caseData, errors);

        if (expectError) {
            assertThat(errors).hasSize(1);
            String hearingNumber = caseData.getHearingCollection().getFirst().getValue().getHearingNumber();
            assertThat(errors.getFirst()).isEqualTo(
                "The estimated hearing length for hearing " + hearingNumber + " must be greater than 0.");
        } else {
            assertThat(errors).isEmpty();
        }
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
        hearingType.setHearingEstLengthNum("1");
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
        assertThat(caseData.getHearingCollection().getFirst().getValue().getHearingDateCollection().getFirst()
                .getValue().getHearingStatus()).isEqualTo(HEARING_STATUS_LISTED);
        assertThat(caseData.getHearingCollection().getFirst().getValue().getHearingDateCollection().getFirst()
                .getValue().getHearingAberdeen().getSelectedLabel()).isEqualTo(TribunalOffice.ABERDEEN.getOfficeName());
        assertThat(caseData.getHearingCollection().getFirst().getValue().getHearingDateCollection().getFirst()
                .getValue().getHearingGlasgow()).isNull();
        assertThat(caseData.getHearingCollection().get(1).getValue().getHearingDateCollection().getFirst().getValue()
                .getHearingGlasgow().getSelectedLabel()).isEqualTo(TribunalOffice.GLASGOW.getOfficeName());
        assertThat(caseData.getHearingCollection().get(1).getValue().getHearingDateCollection().getFirst().getValue()
                .getHearingAberdeen()).isNull();
        assertThat(caseData.getHearingCollection().get(2).getValue().getHearingDateCollection().getFirst().getValue()
                .getHearingEdinburgh().getSelectedLabel()).isEqualTo(TribunalOffice.EDINBURGH.getOfficeName());
        assertThat(caseData.getHearingCollection().getFirst().getValue().getHearingDateCollection().getFirst()
                .getValue().getHearingGlasgow()).isNull();
        final String dundee = TribunalOffice.DUNDEE.getOfficeName();
        assertThat(caseData.getHearingCollection().get(3).getValue().getHearingDateCollection().getFirst().getValue()
                .getHearingDundee().getSelectedLabel()).isEqualTo(dundee);
        assertThat(caseData.getHearingCollection().get(3).getValue().getHearingDateCollection().getFirst().getValue()
                .getHearingVenueDayScotland()).isEqualTo(dundee);
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
        submitEvent = new SubmitEvent();
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

        assertThat(caseData.getRespondentCollection().getFirst().getValue().getResponseReceivedCount()).isNull();
    }

    @Test
    void updateWorkAllocationField_FeatureFlagFalse() {
        List<String> errors = new ArrayList<>();
        CaseData caseData = scotlandCcdRequest1.getCaseDetails().getCaseData();

        when(featureToggleService.isWorkAllocationEnabled()).thenReturn(false);

        caseManagementForCaseWorkerService.updateWorkAllocationField(errors, caseData);

        assertThat(caseData.getRespondentCollection().getFirst().getValue().getResponseReceivedCount()).isNull();
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

        assertThat(caseData.getRespondentCollection().getFirst().getValue().getResponseReceivedCount()).isNull();
    }

    @Test
    void updateWorkAllocationField_NoResponseReceived() {
        List<String> errors = new ArrayList<>();
        CaseData caseData = scotlandCcdRequest1.getCaseDetails().getCaseData();
        caseData.getRespondentCollection().getFirst().getValue().setResponseReceived(NO);

        caseManagementForCaseWorkerService.updateWorkAllocationField(errors, caseData);

        assertThat(caseData.getRespondentCollection().getFirst().getValue().getResponseReceivedCount()).isNull();
    }

    @Test
    void updateWorkAllocationField_ResponseReceived_FirstTime() {
        List<String> errors = new ArrayList<>();
        CaseData caseData = scotlandCcdRequest1.getCaseDetails().getCaseData();
        caseData.getRespondentCollection().getFirst().getValue().setResponseReceived(YES);

        caseManagementForCaseWorkerService.updateWorkAllocationField(errors, caseData);

        assertThat(caseData.getRespondentCollection().getFirst().getValue().getResponseReceivedCount())
                .isEqualTo("1");
    }

    @Test
    void updateWorkAllocationField_ResponseReceived_NthTime() {
        List<String> errors = new ArrayList<>();
        CaseData caseData = scotlandCcdRequest1.getCaseDetails().getCaseData();
        caseData.getRespondentCollection().getFirst().getValue().setResponseReceived(YES);
        caseData.getRespondentCollection().getFirst().getValue().setResponseReceivedCount("1");

        caseManagementForCaseWorkerService.updateWorkAllocationField(errors, caseData);

        assertThat(caseData.getRespondentCollection().getFirst().getValue().getResponseReceivedCount()).isEqualTo("2");
    }

    @ParameterizedTest
    @MethodSource("respondentEccReplyCounterTestCases")
    void updateWorkAllocationField_RespondentEccReplyCounter(String eccReplyValue,
                                                             String initialCount, String expectedCount) {
        List<String> errors = new ArrayList<>();
        CaseData caseData = scotlandCcdRequest1.getCaseDetails().getCaseData();
        caseData.getRespondentCollection().getFirst().getValue().setRespondentEccReply(eccReplyValue);
        caseData.getRespondentCollection().getFirst().getValue().setRespondentEccReplyCount(initialCount);

        caseManagementForCaseWorkerService.updateWorkAllocationField(errors, caseData);

        assertThat(caseData.getRespondentCollection().getFirst().getValue().getRespondentEccReplyCount())
                .isEqualTo(expectedCount);
    }

    private static Stream<Arguments> respondentEccReplyCounterTestCases() {
        return Stream.of(

                Arguments.of(null, null, null),
                Arguments.of("", null, null),
                Arguments.of(null, "1", "1"),
                Arguments.of("", "1", "1"),
                Arguments.of(null, "2", "2"),
                Arguments.of(YES, null, "1"),
                Arguments.of(NO, null, "1"),
                Arguments.of(YES, "2", "3"),
                Arguments.of(NO, "1", "2")
        );
    }

    @Test
    void updateWorkAllocationField_MultipleRespondents_EccReply() {
        CaseData caseData = scotlandCcdRequest1.getCaseDetails().getCaseData();
        
        caseData.getRespondentCollection().get(0).getValue().setRespondentEccReply(null);
        caseData.getRespondentCollection().get(1).getValue().setRespondentEccReply("");
        caseData.getRespondentCollection().get(2).getValue().setRespondentEccReply(YES);
        
        caseData.getRespondentCollection().get(0).getValue().setRespondentEccReplyCount(null);
        caseData.getRespondentCollection().get(1).getValue().setRespondentEccReplyCount("1");
        caseData.getRespondentCollection().get(2).getValue().setRespondentEccReplyCount(null);

        List<String> errors = new ArrayList<>();
        caseManagementForCaseWorkerService.updateWorkAllocationField(errors, caseData);
        
        assertThat(caseData.getRespondentCollection().get(0).getValue().getRespondentEccReplyCount())
                .isNull();
        assertThat(caseData.getRespondentCollection().get(1).getValue().getRespondentEccReplyCount())
                .isEqualTo("1");
        assertThat(caseData.getRespondentCollection().get(2).getValue().getRespondentEccReplyCount())
                .isEqualTo("1");
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
                caseData.getHearingCollection().getFirst().getValue().getHearingDateCollection();
        dateListedTypeItems.add(dateListedTypeItem);
        caseData.getHearingCollection().getFirst().getValue().setHearingDateCollection(dateListedTypeItems);
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
        caseData.getRespondentCollection().getFirst().getValue().setExtensionRequested(YES);
        caseManagementForCaseWorkerService.caseDataDefaults(caseData);
        assertThat(caseData.getRespondentCollection().getFirst().getValue().getExtensionRequested()).isEqualTo(YES);
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

    @ParameterizedTest
    @MethodSource("respondentEccTestCases")
    void testUpdateListOfRespondentsWithAnEcc(
            List<RespondentSumTypeItem> respondentCollection, String expected) {
        CaseData caseData = new CaseData();
        caseData.setRespondentCollection(respondentCollection);

        caseManagementForCaseWorkerService.updateListOfRespondentsWithAnEcc(caseData);

        assertThat(caseData.getRespondentsWithEcc()).isEqualTo(expected);
    }

    private static Stream<Arguments> respondentEccTestCases() {
        RespondentSumType respondentYes = new RespondentSumType();
        respondentYes.setRespondentName("Resp1");
        respondentYes.setRespondentEcc(YES);

        RespondentSumType respondentNo = new RespondentSumType();
        respondentNo.setRespondentName("Resp2");
        respondentNo.setRespondentEcc(NO);

        RespondentSumTypeItem respondentSumTypeItemYes = new RespondentSumTypeItem();
        respondentSumTypeItemYes.setValue(respondentYes);

        RespondentSumTypeItem respondentSumTypeItemNo = new RespondentSumTypeItem();
        respondentSumTypeItemNo.setValue(respondentNo);

        return Stream.of(
                Arguments.of(
                        List.of(respondentSumTypeItemYes, respondentSumTypeItemNo),
                        "Resp1"
                ),
                Arguments.of(
                        List.of(respondentSumTypeItemNo),
                        ""
                )
        );
    }

    @Test
    void setNextEarliestListedHearing_shouldHandleNullCaseData() {
        CaseData caseData = null;

        assertDoesNotThrow(() -> caseManagementForCaseWorkerService.setNextEarliestListedHearing(caseData));
    }

    @ParameterizedTest
    @MethodSource("provideCaseDataForSetNextEarliestListedHearing")
    void setNextEarliestListedHearing_handlesVariousCaseDataScenarios(CaseData caseData, boolean shouldInvokeHelper) {
        try (MockedStatic<HearingsHelper> mockedStatic = Mockito.mockStatic(HearingsHelper.class)) {
            // Act
            caseManagementForCaseWorkerService.setNextEarliestListedHearing(caseData);

            // Assert
            if (shouldInvokeHelper) {
                mockedStatic.verify(() -> HearingsHelper.setEtInitialConsiderationListedHearingType(caseData),
                        times(1));
            } else {
                mockedStatic.verify(() -> HearingsHelper.setEtInitialConsiderationListedHearingType(caseData), never());
            }
        }
    }

    private static Stream<Arguments> provideCaseDataForSetNextEarliestListedHearing() {
        CaseData validCaseData = new CaseData();
        CaseData nullCaseData = null;

        return Stream.of(
                Arguments.of(validCaseData, true),
                Arguments.of(nullCaseData, false)
        );
    }


    @ParameterizedTest
    @MethodSource("provideHearingCollectionsForNextListedDate")
    void setNextListedDate_handlesVariousHearingCollections(List<HearingTypeItem> hearingCollection,
                                                            String expectedNextListedDate) {
        CaseData caseData = new CaseData();
        caseData.setHearingCollection(hearingCollection);

        caseManagementForCaseWorkerService.setNextListedDate(caseData);

        assertThat(caseData.getNextListedDate()).isEqualTo(expectedNextListedDate);
    }

    private static Stream<Arguments> provideHearingCollectionsForNextListedDate() {
        HearingTypeItem hearingWithFutureDate = createHearingTypeItem("2026-01-01T10:00:00");
        HearingTypeItem hearingWithPastDate = createHearingTypeItem("2020-01-01T10:00:00");
        HearingTypeItem hearingWithCurrentDate = createHearingTypeItem(LocalDateTime.now().toString());
        HearingTypeItem hearingWithInvalidDate = createHearingTypeItem("not-a-date");

        return Stream.of(
                Arguments.of(List.of(hearingWithFutureDate, hearingWithPastDate), "2026-01-01"),
                Arguments.of(List.of(hearingWithPastDate, hearingWithCurrentDate), ""),
                Arguments.of(List.of(hearingWithInvalidDate), ""),
                Arguments.of(new ArrayList<>(), null),
                Arguments.of(null, null)
        );
    }

    private static HearingTypeItem createHearingTypeItem(String listedDate) {
        DateListedType dateListedType = new DateListedType();
        dateListedType.setListedDate(listedDate);
        DateListedTypeItem dateListedTypeItem = new DateListedTypeItem();
        dateListedTypeItem.setValue(dateListedType);
        dateListedTypeItem.getValue().setHearingStatus(HEARING_STATUS_LISTED);

        HearingType hearingType = new HearingType();
        hearingType.setHearingDateCollection(List.of(dateListedTypeItem));
        HearingTypeItem hearingTypeItem = new HearingTypeItem();
        hearingTypeItem.setValue(hearingType);

        return hearingTypeItem;
    }

}
