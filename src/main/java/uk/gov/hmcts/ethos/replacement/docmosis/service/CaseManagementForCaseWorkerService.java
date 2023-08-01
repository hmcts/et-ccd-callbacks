package uk.gov.hmcts.ethos.replacement.docmosis.service;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.exceptions.CaseCreationException;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.EccCounterClaimTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DateListedType;
import uk.gov.hmcts.et.common.model.ccd.types.EccCounterClaimType;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.config.NotificationProperties;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.ECCHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.FlagsImageHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ABOUT_TO_SUBMIT_EVENT_CALLBACK;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.DEFAULT_FLAGS_IMAGE_FILE_NAME;
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
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.nullCheck;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.TribunalOfficesService.UNASSIGNED_OFFICE;

@Slf4j
@Service("caseManagementForCaseWorkerService")
public class CaseManagementForCaseWorkerService {

    private final CaseRetrievalForCaseWorkerService caseRetrievalForCaseWorkerService;
    private final CcdClient ccdClient;
    private final ClerkService clerkService;
    private final AuthTokenGenerator serviceAuthTokenGenerator;
    private final String hmctsServiceId;
    private final NotificationProperties notificationProperties;

    private static final String MISSING_CLAIMANT = "Missing claimant";
    private static final String MISSING_RESPONDENT = "Missing respondent";
    private static final String MESSAGE = "Failed to link ECC case for case id : ";
    private static final String CASE_NOT_FOUND_MESSAGE = "Case Reference Number not found.";
    public static final String LISTED_DATE_ON_WEEKEND_MESSAGE = "A hearing date you have entered "
            + "falls on a weekend. You cannot list this case on a weekend. Please amend the date of Hearing ";
    public static final String HMCTS_SERVICE_ID = "HMCTSServiceId";
    public static final String DOCUMENTS_TAB = "#Documents";
    public static final String INDIVIDUAL = "Individual";
    public static final String ORGANISATION = "Organisation";

    @Autowired
    public CaseManagementForCaseWorkerService(CaseRetrievalForCaseWorkerService caseRetrievalForCaseWorkerService,
                                              CcdClient ccdClient, ClerkService clerkService,
                                              AuthTokenGenerator serviceAuthTokenGenerator,
                                              @Value("${hmcts_service_id}") String hmctsServiceId,
                                              NotificationProperties notificationProperties) {
        this.caseRetrievalForCaseWorkerService = caseRetrievalForCaseWorkerService;
        this.ccdClient = ccdClient;
        this.clerkService = clerkService;
        this.serviceAuthTokenGenerator = serviceAuthTokenGenerator;
        this.hmctsServiceId = hmctsServiceId;
        this.notificationProperties = notificationProperties;
    }

    public void caseDataDefaults(CaseData caseData) {
        claimantDefaults(caseData);
        respondentDefaults(caseData);
        struckOutDefaults(caseData);
        dateToCurrentPosition(caseData);
        flagsImageFileNameDefaults(caseData);
    }

    public void claimantDefaults(CaseData caseData) {
        String claimantTypeOfClaimant = caseData.getClaimantTypeOfClaimant();
        if (!isNullOrEmpty(claimantTypeOfClaimant)) {
            if (claimantTypeOfClaimant.equals(INDIVIDUAL_TYPE_CLAIMANT)) {
                String claimantFirstNames = nullCheck(caseData.getClaimantIndType().getClaimantFirstNames());
                String claimantLastName = nullCheck(caseData.getClaimantIndType().getClaimantLastName());
                caseData.setClaimant(claimantFirstNames + " " + claimantLastName);
            } else {
                caseData.setClaimant(nullCheck(caseData.getClaimantCompany()));
            }
        } else {
            caseData.setClaimant(MISSING_CLAIMANT);
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

    public void setHmctsInternalCaseName(CaseData caseData) {
        if (caseData.getClaimant() == null) {
            claimantDefaults(caseData);
        }

        if (caseData.getRespondent() == null) {
            respondentDefaults(caseData);
        }
        
        caseData.setCaseNameHmctsInternal(caseData.getClaimant() + " vs " + caseData.getRespondent());
    }

    public void setCaseDeepLink(CaseData caseData, String caseId) {
        caseData.setCaseDeepLink(notificationProperties.getExuiLinkWithCaseId(caseId) + DOCUMENTS_TAB);
    }

    public void setPublicCaseName(CaseData caseData) {
        if (caseData.getClaimant() == null) {
            claimantDefaults(caseData);
        }

        if (caseData.getRespondent() == null) {
            respondentDefaults(caseData);
        }

        if (caseData.getRestrictedReporting() == null) {
            caseData.setPublicCaseName(caseData.getClaimant() + " vs " + caseData.getRespondent());
        } else {
            caseData.setPublicCaseName(CLAIMANT_TITLE + " vs " + RESPONDENT_TITLE);
        }
    }

    private void checkResponseAddress(RespondentSumTypeItem respondentSumTypeItem) {
        if (respondentSumTypeItem.getValue().getResponseReceived().equals(NO)
                && respondentSumTypeItem.getValue().getResponseRespondentAddress() != null) {
            resetResponseRespondentAddress(respondentSumTypeItem);
        }
    }

    private void checkResponseContinue(RespondentSumTypeItem respondentSumTypeItem) {
        if (Strings.isNullOrEmpty(respondentSumTypeItem.getValue().getResponseContinue())) {
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
        if (!Strings.isNullOrEmpty(respondentSumTypeItem.getValue().getResponseRespondentAddress().getAddressLine1())) {
            respondentSumTypeItem.getValue().getResponseRespondentAddress().setAddressLine1("");
        }
        if (!Strings.isNullOrEmpty(respondentSumTypeItem.getValue().getResponseRespondentAddress().getAddressLine2())) {
            respondentSumTypeItem.getValue().getResponseRespondentAddress().setAddressLine2("");
        }
        if (!Strings.isNullOrEmpty(respondentSumTypeItem.getValue().getResponseRespondentAddress().getAddressLine3())) {
            respondentSumTypeItem.getValue().getResponseRespondentAddress().setAddressLine3("");
        }
        if (!Strings.isNullOrEmpty(respondentSumTypeItem.getValue().getResponseRespondentAddress().getCountry())) {
            respondentSumTypeItem.getValue().getResponseRespondentAddress().setCountry("");
        }
        if (!Strings.isNullOrEmpty(respondentSumTypeItem.getValue().getResponseRespondentAddress().getCounty())) {
            respondentSumTypeItem.getValue().getResponseRespondentAddress().setCounty("");
        }
        if (!Strings.isNullOrEmpty(respondentSumTypeItem.getValue().getResponseRespondentAddress().getPostCode())) {
            respondentSumTypeItem.getValue().getResponseRespondentAddress().setPostCode("");
        }
        if (!Strings.isNullOrEmpty(respondentSumTypeItem.getValue().getResponseRespondentAddress().getPostTown())) {
            respondentSumTypeItem.getValue().getResponseRespondentAddress().setPostTown("");
        }
    }

    private void clearRespondentTypeFields(RespondentSumTypeItem respondentSumTypeItem) {
        RespondentSumType respondentSumType = respondentSumTypeItem.getValue();
        if (respondentSumType != null && respondentSumType.getRespondentType() != null) {
            if (respondentSumType.getRespondentType().equals(ORGANISATION)) {
                respondentSumType.setRespondentFirstName("");
                respondentSumType.setRespondentLastName("");
            } else {
                respondentSumType.setRespondentOrganisation("");
            }
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
        if (CollectionUtils.isEmpty(caseData.getRepCollection())) {
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
        if (!CollectionUtils.isEmpty(caseData.getHearingCollection())) {
            for (HearingTypeItem hearingTypeItem : caseData.getHearingCollection()) {
                HearingType hearingType =  hearingTypeItem.getValue();
                if (!CollectionUtils.isEmpty(hearingTypeItem.getValue().getHearingDateCollection())) {
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
        if (!CollectionUtils.isEmpty(caseData.getHearingCollection())) {
            for (HearingTypeItem hearingTypeItem : caseData.getHearingCollection()) {
                if (!CollectionUtils.isEmpty(hearingTypeItem.getValue().getHearingDateCollection())) {
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
        if ((Strings.isNullOrEmpty(dateListedTypeItem.getValue().getHearingStatus())
                || HEARING_STATUS_LISTED.equals(dateListedTypeItem.getValue().getHearingStatus()))
                && date.isBefore(LocalDate.now())) {
            caseData.setListedDateInPastWarning(YES);
        }
    }

    private void populateHearingVenueFromHearingLevelToDayLevel(DateListedType dateListedType, HearingType hearingType,
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
            throw new CaseCreationException(MESSAGE + caseIdToLink + e.getMessage());
        }
    }

    /**
     * Calls reference data API to add HMCTSServiceId to supplementary_data to a case.
     * @param caseDetails Details on the case
     * @param accessToken authorisation token for reference data api
     */
    public void setHmctsServiceIdSupplementary(CaseDetails caseDetails, String accessToken) throws IOException {
        Map<String, Map<String, Object>> payloadData = Maps.newHashMap();
        payloadData.put("$set", singletonMap(HMCTS_SERVICE_ID, hmctsServiceId));

        Map<String, Object> payload = Maps.newHashMap();
        payload.put("supplementary_data_updates", payloadData);
        String errorMessage = String.format("Call to Supplementary Data API failed for %s", caseDetails.getCaseId());

        try {
            ResponseEntity<Object> response =
                    ccdClient.setSupplementaryData(accessToken, payload, caseDetails.getCaseId());
            if (response == null) {
                throw new CaseCreationException(errorMessage);
            }
            log.info("Http status received from CCD supplementary update API; {}", response.getStatusCodeValue());
        } catch (RestClientResponseException e) {
            throw new CaseCreationException(String.format("%s with %s", errorMessage, e.getMessage()));
        }
    }
}
