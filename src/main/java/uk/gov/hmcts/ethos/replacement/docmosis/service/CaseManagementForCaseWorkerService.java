package uk.gov.hmcts.ethos.replacement.docmosis.service;

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestClientResponseException;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.exceptions.CaseCreationException;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.ccd.CaseAccessPin;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.EccCounterClaimTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DateListedType;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.EccCounterClaimType;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.et.common.model.generic.BaseCaseData;
import uk.gov.hmcts.et.common.model.multiples.SubmitMultipleEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.CitizenRole;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ClaimantSolicitorRole;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.DefendantRole;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.ECCHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.FlagsImageHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.MultipleCasesSendingService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.multiples.MultipleReferenceService;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.util.Collections.singletonMap;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ABOUT_TO_SUBMIT_EVENT_CALLBACK;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.DEFAULT_FLAGS_IMAGE_FILE_NAME;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.EMPLOYMENT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ET3_DUE_DATE_FROM_SERVING_DATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.FLAG_ECC;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_LISTED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.INDIVIDUAL_TYPE_CLAIMANT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MID_EVENT_CALLBACK;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.OLD_DATE_TIME_PATTERN;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.ACAS_DOC_TYPE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.EMPTY_STRING;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.ET1_ATTACHMENT_DOC_TYPE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.ET1_DOC_TYPE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.nullCheck;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.TribunalOfficesService.UNASSIGNED_OFFICE;

@Slf4j
@Service
public class CaseManagementForCaseWorkerService {
    private final CaseRetrievalForCaseWorkerService caseRetrievalForCaseWorkerService;
    private final CcdClient ccdClient;
    private final ClerkService clerkService;
    private final FeatureToggleService featureToggleService;
    private final String hmctsServiceId;
    private final AdminUserService adminUserService;
    private final CaseManagementLocationService caseManagementLocationService;
    private final MultipleReferenceService multipleReferenceService;
    private final MultipleCasesSendingService multipleCasesSendingService;

    private static final String MISSING_CLAIMANT = "Missing claimant";
    private static final String MISSING_RESPONDENT = "Missing respondent";
    private static final String MESSAGE = "Failed to link ECC case for case id : ";
    private static final String CASE_NOT_FOUND_MESSAGE = "Case Reference Number not found.";
    public static final String LISTED_DATE_ON_WEEKEND_MESSAGE = "A hearing date you have entered "
            + "falls on a weekend. You cannot list this case on a weekend. Please amend the date of Hearing ";
    public static final String HMCTS_SERVICE_ID = "HMCTSServiceId";
    public static final String ORGANISATION = "Organisation";

    public static final String SUPPLEMENTARY_DATA_ERROR = "Call to Supplementary Data API failed for %s";

    public static final String CASE_MANAGEMENT_LABEL = "Employment Tribunals";
    public static final String CASE_MANAGEMENT_CODE = "Employment";
    private static final String EMPLOYMENT_JURISDICTION = "EMPLOYMENT";
    public static final String ET3_RESPONSE_RECEIVED_INITIAL_VALUE = "1";
    private final String ccdGatewayBaseUrl;
    private final List<String> caseTypeIdsToCheck = List.of("ET_EnglandWales", "ET_Scotland", "Bristol",
            "Leeds", "LondonCentral", "LondonEast", "LondonSouth", "Manchester", "MidlandsEast", "MidlandsWest",
            "Newcastle", "Scotland", "Wales", "Watford");
    private static final int EXPIRY_PERIOD = 180;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Autowired
    public CaseManagementForCaseWorkerService(CaseRetrievalForCaseWorkerService caseRetrievalForCaseWorkerService,
                                              CcdClient ccdClient,
                                              ClerkService clerkService,
                                              FeatureToggleService featureToggleService,
                                              @Value("${hmcts_service_id}") String hmctsServiceId,
                                              AdminUserService adminUserService,
                                              CaseManagementLocationService caseManagementLocationService,
                                              MultipleReferenceService multipleReferenceService,
                                              @Value("${ccd_gateway_base_url}") String ccdGatewayBaseUrl,
                                              MultipleCasesSendingService multipleCasesSendingService) {
        this.caseRetrievalForCaseWorkerService = caseRetrievalForCaseWorkerService;
        this.ccdClient = ccdClient;
        this.clerkService = clerkService;
        this.featureToggleService = featureToggleService;
        this.hmctsServiceId = hmctsServiceId;
        this.adminUserService = adminUserService;
        this.caseManagementLocationService = caseManagementLocationService;
        this.multipleReferenceService = multipleReferenceService;
        this.ccdGatewayBaseUrl = ccdGatewayBaseUrl;
        this.multipleCasesSendingService = multipleCasesSendingService;
    }

    public void caseDataDefaults(CaseData caseData) {
        claimantDefaults(caseData);
        respondentDefaults(caseData);
        struckOutDefaults(caseData);
        dateToCurrentPosition(caseData);
        flagsImageFileNameDefaults(caseData);
        setGlobalSearchDefaults(caseData);
        setWorkAllocationDefaults(caseData);
    }

    public void setGlobalSearchDefaults(CaseData caseData) {
        if (!featureToggleService.isGlobalSearchEnabled()) {
            return;
        }
        setCaseNameHmctsInternal(caseData);
        caseManagementLocationService.setCaseManagementLocation(caseData);
        setCaseManagementCategory(caseData);
    }

    public void setWorkAllocationDefaults(CaseData caseData) {
        if (!featureToggleService.isWorkAllocationEnabled()) {
            return;
        }
        setHmctsCaseCategory(caseData);
    }

    public void claimantDefaults(CaseData caseData) {
        String claimantTypeOfClaimant = caseData.getClaimantTypeOfClaimant();

        if (isNullOrEmpty(claimantTypeOfClaimant)) {
            caseData.setClaimant(MISSING_CLAIMANT);
        } else {
            if (claimantTypeOfClaimant.equals(INDIVIDUAL_TYPE_CLAIMANT)) {
                String claimantFirstNames = nullCheck(caseData.getClaimantIndType().getClaimantFirstNames());
                String claimantLastName = nullCheck(caseData.getClaimantIndType().getClaimantLastName());
                caseData.setClaimant(claimantFirstNames + " " + claimantLastName);
            } else {
                caseData.setClaimant(nullCheck(caseData.getClaimantCompany()));
            }
        }

        if (featureToggleService.isHmcEnabled()) {
            caseData.setClaimantId(UUID.randomUUID().toString());
        }
        addClaimantDocuments(caseData);
    }

    public void addClaimantDocuments(BaseCaseData caseData) {
        List<DocumentTypeItem> documentCollection = caseData.getDocumentCollection();
        List<DocumentTypeItem> claimantDocumentCollection = new ArrayList<>();
        List<String> claimantDocs = List.of(ET1_DOC_TYPE, ET1_ATTACHMENT_DOC_TYPE, ACAS_DOC_TYPE);
        if (documentCollection != null) {
            for (DocumentTypeItem documentTypeItem : documentCollection) {
                DocumentType documentType = documentTypeItem.getValue();
                if (claimantDocs.contains(defaultIfEmpty(documentType.getStartingClaimDocuments(), ""))) {
                    claimantDocumentCollection.add(documentTypeItem);
                }
            }
            if (isNotEmpty(claimantDocumentCollection)) {
                caseData.setClaimantDocumentCollection(claimantDocumentCollection);
            }
        }
    }

    private void respondentDefaults(CaseData caseData) {
        if (caseData.getRespondentCollection() != null && !caseData.getRespondentCollection().isEmpty()) {
            RespondentSumType respondentSumType = caseData.getRespondentCollection().get(0).getValue();
            caseData.setRespondent(nullCheck(respondentSumType.getRespondentName()));
            for (RespondentSumTypeItem respondentSumTypeItem : caseData.getRespondentCollection()) {
                checkExtensionRequired(respondentSumTypeItem);
                checkResponseReceived(respondentSumTypeItem);
                checkResponseAddress(respondentSumTypeItem);
                checkResponseContinue(respondentSumTypeItem);
                clearRespondentTypeFields(respondentSumTypeItem);
            }
        } else {
            caseData.setRespondent(MISSING_RESPONDENT);
        }
    }

    private void checkResponseAddress(RespondentSumTypeItem respondentSumTypeItem) {
        if (respondentSumTypeItem.getValue().getResponseReceived().equals(NO)
                && respondentSumTypeItem.getValue().getResponseRespondentAddress() != null) {
            resetResponseRespondentAddress(respondentSumTypeItem);
        }
    }

    private void checkResponseContinue(RespondentSumTypeItem respondentSumTypeItem) {
        if (isNullOrEmpty(respondentSumTypeItem.getValue().getResponseContinue())) {
            respondentSumTypeItem.getValue().setResponseContinue(YES);
        }
    }

    private void checkResponseReceived(RespondentSumTypeItem respondentSumTypeItem) {
        if (respondentSumTypeItem.getValue().getResponseReceived() == null) {
            respondentSumTypeItem.getValue().setResponseReceived(NO);
        }
    }

    private void checkExtensionRequired(RespondentSumTypeItem respondentSumTypeItem) {
        if (isNullOrEmpty(respondentSumTypeItem.getValue().getExtensionRequested())) {
            respondentSumTypeItem.getValue().setExtensionRequested(NO);
        }
    }

    private void resetResponseRespondentAddress(RespondentSumTypeItem respondentSumTypeItem) {
        if (!isNullOrEmpty(respondentSumTypeItem.getValue().getResponseRespondentAddress().getAddressLine1())) {
            respondentSumTypeItem.getValue().getResponseRespondentAddress().setAddressLine1("");
        }
        if (!isNullOrEmpty(respondentSumTypeItem.getValue().getResponseRespondentAddress().getAddressLine2())) {
            respondentSumTypeItem.getValue().getResponseRespondentAddress().setAddressLine2("");
        }
        if (!isNullOrEmpty(respondentSumTypeItem.getValue().getResponseRespondentAddress().getAddressLine3())) {
            respondentSumTypeItem.getValue().getResponseRespondentAddress().setAddressLine3("");
        }
        if (!isNullOrEmpty(respondentSumTypeItem.getValue().getResponseRespondentAddress().getCountry())) {
            respondentSumTypeItem.getValue().getResponseRespondentAddress().setCountry("");
        }
        if (!isNullOrEmpty(respondentSumTypeItem.getValue().getResponseRespondentAddress().getCounty())) {
            respondentSumTypeItem.getValue().getResponseRespondentAddress().setCounty("");
        }
        if (!isNullOrEmpty(respondentSumTypeItem.getValue().getResponseRespondentAddress().getPostCode())) {
            respondentSumTypeItem.getValue().getResponseRespondentAddress().setPostCode("");
        }
        if (!isNullOrEmpty(respondentSumTypeItem.getValue().getResponseRespondentAddress().getPostTown())) {
            respondentSumTypeItem.getValue().getResponseRespondentAddress().setPostTown("");
        }
    }

    private void struckOutDefaults(CaseData caseData) {
        if (caseData.getRespondentCollection() != null && !caseData.getRespondentCollection().isEmpty()) {
            for (RespondentSumTypeItem respondentSumTypeItem : caseData.getRespondentCollection()) {
                if (respondentSumTypeItem.getValue().getResponseStruckOut() == null) {
                    respondentSumTypeItem.getValue().setResponseStruckOut(NO);
                }
            }
        }
    }

    private void flagsImageFileNameDefaults(CaseData caseData) {
        if (isNullOrEmpty(caseData.getFlagsImageFileName())) {
            caseData.setFlagsImageFileName(DEFAULT_FLAGS_IMAGE_FILE_NAME);
        }
    }

    public void dateToCurrentPosition(CaseData caseData) {
        if (!isNullOrEmpty(caseData.getPositionType()) && positionChanged(caseData)) {
            caseData.setDateToPosition(LocalDate.now().toString());
            caseData.setCurrentPosition(caseData.getPositionType());
        }
    }

    public void setEt3ResponseDueDate(CaseData caseData) {
        if (!isNullOrEmpty(caseData.getClaimServedDate()) && isNullOrEmpty(caseData.getEt3DueDate())) {
            caseData.setEt3DueDate(LocalDate.parse(
                    caseData.getClaimServedDate()).plusDays(ET3_DUE_DATE_FROM_SERVING_DATE).toString());
        }
    }

    /**
     * Update Work Allocation specific field.
     *
     * @param errors - if there are errors do not update the field
     * @param caseData - the caseData contains the values for the case
     */
    public void updateWorkAllocationField(List<String> errors, CaseData caseData) {
        if (errors.isEmpty()
                && featureToggleService.isWorkAllocationEnabled()
                && !isEmpty(caseData.getRespondentCollection())) {
            updateResponseReceivedCounter(caseData.getRespondentCollection());
        }
    }

    private void updateResponseReceivedCounter(List<RespondentSumTypeItem> respondentCollection) {
        RespondentSumType firstRespondent = respondentCollection.get(0).getValue();
        if (YES.equals(firstRespondent.getResponseReceived())) {
            firstRespondent.setResponseReceivedCount(
                    StringUtils.isBlank(firstRespondent.getResponseReceivedCount())
                            ? ET3_RESPONSE_RECEIVED_INITIAL_VALUE
                            : Integer.toString(Integer.parseInt(firstRespondent.getResponseReceivedCount()) + 1)
            );
        }
    }

    public void setNextListedDate(CaseData caseData) {
        List<String> dates = new ArrayList<>();
        String nextListedDate = "";

        if (isNotEmpty(caseData.getHearingCollection())) {
            for (HearingTypeItem hearingTypeItem : caseData.getHearingCollection()) {
                dates.addAll(getListedDates(hearingTypeItem));
            }
            for (String date : dates) {
                LocalDateTime parsedDate = LocalDateTime.parse(date);
                if (EMPTY_STRING.equals(nextListedDate) && parsedDate.isAfter(LocalDateTime.now())
                        || parsedDate.isAfter(LocalDateTime.now())
                        && parsedDate.isBefore(LocalDateTime.parse(nextListedDate))) {
                    nextListedDate = date;
                }
            }
            caseData.setNextListedDate(nextListedDate.split("T")[0]);
        }
    }

    public void setNextListedDateOnMultiple(CaseDetails details) throws IOException {
        CaseData caseData = details.getCaseData();
        if (StringUtils.isEmpty(caseData.getMultipleReference()) || !YES.equals(caseData.getLeadClaimant())) {
            return;
        }

        String adminToken = adminUserService.getAdminUserToken();
        String multipleCaseTypeId = details.getCaseTypeId() + "_Multiple";
        SubmitMultipleEvent multiple = multipleReferenceService.getMultipleByReference(
            adminToken,
            multipleCaseTypeId,
            caseData.getMultipleReference()
        );

        var multipleData = multiple.getCaseData();

        multipleData.setNextListedDate(caseData.getNextListedDate());

        multipleCasesSendingService.sendUpdateToMultiple(
            adminToken, 
            multipleCaseTypeId, 
            EMPLOYMENT, 
            multipleData, 
            String.valueOf(multiple.getCaseId())
        );
    }

    private List<String> getListedDates(HearingTypeItem hearingTypeItem) {
        HearingType hearingType = hearingTypeItem.getValue();
        List<String> dates = new ArrayList<>();
        if (isNotEmpty(hearingType.getHearingDateCollection())) {
            for (DateListedTypeItem dateListedTypeItem : hearingType.getHearingDateCollection()) {
                DateListedType dateListedType = dateListedTypeItem.getValue();
                if (HEARING_STATUS_LISTED.equals(dateListedType.getHearingStatus())
                        && !isNullOrEmpty(dateListedType.getListedDate())) {
                    dates.add(dateListedType.getListedDate());
                }
            }
        }
        return dates;
    }

    public void setMigratedCaseLinkDetails(String authToken, CaseDetails caseDetails) {
        // get a target case data using the source case data and elastic search query
        Pair<String, List<SubmitEvent>> caseRefAndCaseDataPair =
                caseRetrievalForCaseWorkerService.transferSourceCaseRetrievalESRequest(
                        caseDetails.getCaseId(), authToken, caseTypeIdsToCheck);
        if (caseRefAndCaseDataPair == null
                || caseRefAndCaseDataPair.getFirst().isEmpty()
                || caseRefAndCaseDataPair.getSecond().isEmpty()) {
            return;
        }

        String sourceCaseTypeId = caseRefAndCaseDataPair.getFirst();
        SubmitEvent submitEvent = caseRefAndCaseDataPair.getSecond().get(0);
        log.info("SubmitEvent retrieved from ES for the update target case: {} with source case type of {}.",
                submitEvent.getCaseId(), sourceCaseTypeId);
        String sourceCaseId = String.valueOf(submitEvent.getCaseId());
        String ethosCaseReference = caseRetrievalForCaseWorkerService.caseRefRetrievalRequest(authToken,
                caseDetails.getCaseTypeId(), EMPLOYMENT_JURISDICTION, sourceCaseId);
        log.info("Source Case reference is retrieved via retrieveTransferredCaseReference: {}.", ethosCaseReference);
        if (ethosCaseReference != null) {
            caseDetails.getCaseData().setTransferredCaseLink("<a target=\"_blank\" href=\""
                    + String.format("%s/cases/case-details/%s", ccdGatewayBaseUrl, sourceCaseId) + "\">"
                    + ethosCaseReference + "</a>");
        }
    }

    public CaseData struckOutRespondents(CCDRequest ccdRequest) {
        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        if (caseData.getRespondentCollection() != null && !caseData.getRespondentCollection().isEmpty()) {
            List<RespondentSumTypeItem> activeRespondent = new ArrayList<>();
            List<RespondentSumTypeItem> struckRespondent = new ArrayList<>();
            for (RespondentSumTypeItem respondentSumTypeItem : caseData.getRespondentCollection()) {
                RespondentSumType respondentSumType = respondentSumTypeItem.getValue();
                if (respondentSumType.getResponseStruckOut() != null) {
                    if (respondentSumType.getResponseStruckOut().equals(YES)) {
                        struckRespondent.add(respondentSumTypeItem);
                    } else {
                        activeRespondent.add(respondentSumTypeItem);
                    }
                } else {
                    respondentSumType.setResponseStruckOut(NO);
                    activeRespondent.add(respondentSumTypeItem);
                }
            }
            caseData.setRespondentCollection(Stream.concat(activeRespondent.stream(),
                    struckRespondent.stream()).toList());
            respondentDefaults(caseData);
        }
        return caseData;
    }

    public CaseData continuingRespondent(CCDRequest ccdRequest) {
        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        if (isEmpty(caseData.getRepCollection())) {
            List<RespondentSumTypeItem> continuingRespondent = new ArrayList<>();
            List<RespondentSumTypeItem> notContinuingRespondent = new ArrayList<>();
            for (RespondentSumTypeItem respondentSumTypeItem : caseData.getRespondentCollection()) {
                RespondentSumType respondentSumType = respondentSumTypeItem.getValue();
                if (YES.equals(respondentSumType.getResponseContinue())) {
                    continuingRespondent.add(respondentSumTypeItem);
                } else if (NO.equals(respondentSumType.getResponseContinue())) {
                    notContinuingRespondent.add(respondentSumTypeItem);
                } else {
                    respondentSumType.setResponseContinue(YES);
                    continuingRespondent.add(respondentSumTypeItem);
                }
            }
            caseData.setRespondentCollection(Stream.concat(continuingRespondent.stream(),
                    notContinuingRespondent.stream()).toList());
            respondentDefaults(caseData);
        }
        return caseData;
    }

    private boolean positionChanged(CaseData caseData) {
        return isNullOrEmpty(caseData.getCurrentPosition())
                || !caseData.getPositionType().equals(caseData.getCurrentPosition());
    }

    public void amendHearing(CaseData caseData, String caseTypeId) {
        if (!isEmpty(caseData.getHearingCollection())) {
            for (HearingTypeItem hearingTypeItem : caseData.getHearingCollection()) {
                HearingType hearingType = hearingTypeItem.getValue();
                if (!isEmpty(hearingTypeItem.getValue().getHearingDateCollection())) {
                    for (DateListedTypeItem dateListedTypeItem
                            : hearingTypeItem.getValue().getHearingDateCollection()) {
                        DateListedType dateListedType = dateListedTypeItem.getValue();
                        if (dateListedType.getHearingStatus() == null) {
                            dateListedType.setHearingStatus(HEARING_STATUS_LISTED);
                            dateListedType.setHearingTimingStart(dateListedType.getListedDate());
                            dateListedType.setHearingTimingFinish(dateListedType.getListedDate());
                        }
                        populateHearingVenueFromHearingLevelToDayLevel(dateListedType, hearingType, caseTypeId);
                    }
                }
            }
        }
    }

    public void midEventAmendHearing(CaseData caseData, List<String> errors) {
        caseData.setListedDateInPastWarning(NO);
        if (!isEmpty(caseData.getHearingCollection())) {
            for (HearingTypeItem hearingTypeItem : caseData.getHearingCollection()) {
                if (!isEmpty(hearingTypeItem.getValue().getHearingDateCollection())) {
                    for (DateListedTypeItem dateListedTypeItem
                            : hearingTypeItem.getValue().getHearingDateCollection()) {
                        addHearingsOnWeekendError(dateListedTypeItem, errors,
                                hearingTypeItem.getValue().getHearingNumber());
                        addHearingsInPastWarning(dateListedTypeItem, caseData);
                    }
                }
            }
        }
    }

    public void setScotlandAllocatedOffice(String caseTypeId, CaseData caseData) {
        if (!SCOTLAND_CASE_TYPE_ID.equals(caseTypeId)) {
            return;
        }

        if (caseData.getManagingOffice() != null && !UNASSIGNED_OFFICE.equals(caseData.getManagingOffice())) {
            caseData.setAllocatedOffice(TribunalOffice.GLASGOW.getOfficeName());
        }
    }

    private void addHearingsOnWeekendError(DateListedTypeItem dateListedTypeItem, List<String> errors,
                                           String hearingNumber) {
        LocalDate date = LocalDateTime.parse(
                dateListedTypeItem.getValue().getListedDate(), OLD_DATE_TIME_PATTERN).toLocalDate();
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        if (SUNDAY.equals(dayOfWeek)
                || SATURDAY.equals(dayOfWeek)) {
            errors.add(LISTED_DATE_ON_WEEKEND_MESSAGE + hearingNumber);
        }
    }

    private void addHearingsInPastWarning(DateListedTypeItem dateListedTypeItem, CaseData caseData) {
        LocalDate date = LocalDateTime.parse(
                dateListedTypeItem.getValue().getListedDate(), OLD_DATE_TIME_PATTERN).toLocalDate();
        if ((isNullOrEmpty(dateListedTypeItem.getValue().getHearingStatus())
                || HEARING_STATUS_LISTED.equals(dateListedTypeItem.getValue().getHearingStatus()))
                && date.isBefore(LocalDate.now())) {
            caseData.setListedDateInPastWarning(YES);
        }
    }

    private void populateHearingVenueFromHearingLevelToDayLevel(DateListedType dateListedType,
                                                                HearingType hearingType,
                                                                String caseTypeId) {
        switch (caseTypeId) {
            case ENGLANDWALES_CASE_TYPE_ID:
                populateHearingVenueEnglandWales(dateListedType, hearingType);
                break;
            case SCOTLAND_CASE_TYPE_ID:
                populateHearingVenueScotland(dateListedType, hearingType);
                break;
            default:
                throw new IllegalArgumentException("Unexpected case type id " + caseTypeId);
        }
    }

    private void populateHearingVenueEnglandWales(DateListedType dateListedType, HearingType hearingType) {
        if (!dateListedType.hasHearingVenue()) {
            dateListedType.setHearingVenueDay(hearingType.getHearingVenue());
        }
    }

    private void populateHearingVenueScotland(DateListedType dateListedType, HearingType hearingType) {
        dateListedType.setHearingVenueDayScotland(hearingType.getHearingVenueScotland());
        dateListedType.setHearingGlasgow(null);
        dateListedType.setHearingAberdeen(null);
        dateListedType.setHearingDundee(null);
        dateListedType.setHearingEdinburgh(null);

        String hearingVenue = hearingType.getHearingVenueScotland();
        if (TribunalOffice.GLASGOW.getOfficeName().equals(hearingVenue)) {
            dateListedType.setHearingGlasgow(hearingType.getHearingGlasgow());
            hearingType.setHearingAberdeen(null);
            hearingType.setHearingDundee(null);
            hearingType.setHearingEdinburgh(null);
        } else if (TribunalOffice.ABERDEEN.getOfficeName().equals(hearingVenue)) {
            dateListedType.setHearingAberdeen(hearingType.getHearingAberdeen());
            hearingType.setHearingGlasgow(null);
            hearingType.setHearingDundee(null);
            hearingType.setHearingEdinburgh(null);
        } else if (TribunalOffice.DUNDEE.getOfficeName().equals(hearingVenue)) {
            dateListedType.setHearingDundee(hearingType.getHearingDundee());
            hearingType.setHearingGlasgow(null);
            hearingType.setHearingAberdeen(null);
            hearingType.setHearingEdinburgh(null);
        } else if (TribunalOffice.EDINBURGH.getOfficeName().equals(hearingVenue)) {
            dateListedType.setHearingEdinburgh(hearingType.getHearingEdinburgh());
            hearingType.setHearingGlasgow(null);
            hearingType.setHearingAberdeen(null);
            hearingType.setHearingDundee(null);
        }
    }

    public CaseData createECC(CaseDetails caseDetails, String authToken, List<String> errors, String callback) {
        CaseData currentCaseData = caseDetails.getCaseData();
        List<SubmitEvent> submitEvents = getCasesES(caseDetails, authToken);
        if (submitEvents != null && !submitEvents.isEmpty()) {
            SubmitEvent submitEvent = submitEvents.get(0);
            if (ECCHelper.validCaseForECC(submitEvent, errors)) {
                switch (callback) {
                    case MID_EVENT_CALLBACK:
                        Helper.midRespondentECC(currentCaseData, submitEvent.getCaseData());
                        currentCaseData.setManagingOffice(submitEvent.getCaseData().getManagingOffice());
                        clerkService.initialiseClerkResponsible(currentCaseData);
                        break;
                    case ABOUT_TO_SUBMIT_EVENT_CALLBACK:
                        ECCHelper.createECCLogic(caseDetails, submitEvent.getCaseData());
                        currentCaseData.setRespondentECC(null);
                        currentCaseData.setCaseSource(FLAG_ECC);
                        break;
                    default:
                        sendUpdateSingleCaseECC(authToken, caseDetails, submitEvent.getCaseData(),
                                String.valueOf(submitEvent.getCaseId()));
                        break;
                }
            }
        } else {
            errors.add(CASE_NOT_FOUND_MESSAGE);
        }
        log.info("Add claimant and respondent defaults");
        claimantDefaults(currentCaseData);
        respondentDefaults(currentCaseData);
        return currentCaseData;
    }

    private List<SubmitEvent> getCasesES(CaseDetails caseDetails, String authToken) {
        return caseRetrievalForCaseWorkerService.casesRetrievalESRequest(caseDetails.getCaseId(), authToken,
                caseDetails.getCaseTypeId(),
                new ArrayList<>(Collections.singleton(caseDetails.getCaseData().getCaseRefECC())));
    }

    private void sendUpdateSingleCaseECC(String authToken, CaseDetails currentCaseDetails,
                                         CaseData originalCaseData, String caseIdToLink) {
        try {
            EccCounterClaimTypeItem eccCounterClaimTypeItem = new EccCounterClaimTypeItem();
            EccCounterClaimType eccCounterClaimType = new EccCounterClaimType();
            eccCounterClaimType.setCounterClaim(currentCaseDetails.getCaseData().getEthosCaseReference());
            eccCounterClaimTypeItem.setId(UUID.randomUUID().toString());
            eccCounterClaimTypeItem.setValue(eccCounterClaimType);
            if (originalCaseData.getEccCases() != null) {
                originalCaseData.getEccCases().add(eccCounterClaimTypeItem);
            } else {
                originalCaseData.setEccCases(
                        new ArrayList<>(Collections.singletonList(eccCounterClaimTypeItem)));
            }
            FlagsImageHelper.buildFlagsImageFileName(currentCaseDetails.getCaseTypeId(), originalCaseData);
            CCDRequest returnedRequest = ccdClient.startEventForCase(authToken, currentCaseDetails.getCaseTypeId(),
                    currentCaseDetails.getJurisdiction(), caseIdToLink);
            ccdClient.submitEventForCase(authToken, originalCaseData, currentCaseDetails.getCaseTypeId(),
                    currentCaseDetails.getJurisdiction(), returnedRequest, caseIdToLink);
        } catch (Exception e) {
            throw (CaseCreationException)new CaseCreationException(
                    MESSAGE + caseIdToLink + e.getMessage()).initCause(e);
        }
    }

    /**
     * Calls reference data API to add HMCTSServiceId to supplementary_data to a case.
     *
     * @param caseDetails Details on the case
     */
    public void setHmctsServiceIdSupplementary(CaseDetails caseDetails) throws IOException {
        if (!featureToggleService.isGlobalSearchEnabled()) {
            return;
        }

        Map<String, Map<String, Object>> payloadData = Maps.newHashMap();
        payloadData.put("$set", singletonMap(HMCTS_SERVICE_ID, hmctsServiceId));

        setSupplementaryData(caseDetails, payloadData);
    }

    /**
     * Calls reference data API to remove HMCTSServiceId from a case's supplementary_data.
     *
     * @param caseDetails Details on the case
     */
    public void removeHmctsServiceIdSupplementary(CaseDetails caseDetails) throws IOException {
        if (!featureToggleService.isGlobalSearchEnabled()) {
            return;
        }
        Map<String, Map<String, Object>> payloadData = Maps.newHashMap();
        payloadData.put("$set", Map.of());

        setSupplementaryData(caseDetails, payloadData);
    }

    private void setSupplementaryData(CaseDetails caseDetails,
                                      Map<String, Map<String, Object>> payloadData) throws IOException {
        Map<String, Object> payload = Maps.newHashMap();
        payload.put("supplementary_data_updates", payloadData);
        String errorMessage = String.format(SUPPLEMENTARY_DATA_ERROR, caseDetails.getCaseId());

        try {
            String adminUserToken = adminUserService.getAdminUserToken();
            ResponseEntity<Object> response =
                    ccdClient.setSupplementaryData(adminUserToken, payload, caseDetails.getCaseId());
            if (response == null) {
                throw new CaseCreationException(errorMessage);
            }
            log.info("Http status received from CCD supplementary update API; {}", response.getStatusCodeValue());
        } catch (RestClientResponseException e) {
            throw new CaseCreationException(String.format("%s with %s", errorMessage, e.getMessage()));
        }
    }

    public void setCaseNameHmctsInternal(CaseData caseData) {
        if (caseData.getClaimant() == null) {
            claimantDefaults(caseData);
        }

        if (caseData.getRespondent() == null) {
            respondentDefaults(caseData);
        }

        caseData.setCaseNameHmctsInternal(caseData.getClaimant() + " vs " + caseData.getRespondent());
    }

    private void setCaseManagementCategory(CaseData caseData) {
        // See RET-4733 for reason of order of DynamicFixedListType.from() values
        caseData.setCaseManagementCategory(
                DynamicFixedListType.from(CASE_MANAGEMENT_LABEL, CASE_MANAGEMENT_CODE, true)
        );
    }

    private void setHmctsCaseCategory(CaseData caseData) {
        caseData.setHmctsCaseCategory(CASE_MANAGEMENT_LABEL);
    }

    public void setPublicCaseName(CaseData caseData) {
        if (ObjectUtils.isEmpty(caseData.getRestrictedReporting())) {
            caseData.setPublicCaseName(caseData.getClaimant() + " vs " + caseData.getRespondent());
        } else {
            caseData.setPublicCaseName(CLAIMANT_TITLE + " vs " + RESPONDENT_TITLE);
        }
    }

    private void clearRespondentTypeFields(RespondentSumTypeItem respondentSumTypeItem) {
        if (!featureToggleService.isHmcEnabled()) {
            return;
        }
        RespondentSumType respondentSumType = respondentSumTypeItem.getValue();
        if (respondentSumType != null && respondentSumType.getRespondentType() != null) {
            if (ORGANISATION.equals(respondentSumType.getRespondentType())) {
                respondentSumType.setRespondentFirstName("");
                respondentSumType.setRespondentLastName("");
            } else {
                respondentSumType.setRespondentOrganisation("");
            }
        }
    }

    /**
     * Sets case access pin to CaseData
     * @param caseData CaseData received from controller
     */
    public void setCaseAccessPin(CaseData caseData) {
        caseData.setCaseAccessPin(new CaseAccessPin.Builder().caseAccessibleRoles(
                List.of(CitizenRole.CITIZEN.getCaseRoleLabel(),
                        DefendantRole.DEFENDANT.getCaseRoleLabel(),
                        ClaimantSolicitorRole.CLAIMANTSOLICITOR.getCaseRoleLabel()))
                        .expiryDate(LocalDateTime.now().plusDays(EXPIRY_PERIOD).toString())
                .build());
    }
}
