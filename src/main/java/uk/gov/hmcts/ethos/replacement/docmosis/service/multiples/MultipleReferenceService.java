package uk.gov.hmcts.ethos.replacement.docmosis.service.multiples;

import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.exceptions.CaseCreationException;
import uk.gov.hmcts.ecm.common.helpers.ESHelper;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.ListTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.SubCaseLegalRepDetails;
import uk.gov.hmcts.et.common.model.multiples.MultipleCaseSearchResult;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.SubmitMultipleEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.MultipleRefEnglandWalesRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.MultipleRefScotlandRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultiplesHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.MultipleCasesSendingService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_BULK_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_BULK_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@Slf4j
@RequiredArgsConstructor
@Service("multipleReferenceService")
public class MultipleReferenceService {

    private final MultipleRefEnglandWalesRepository multipleRefEnglandWalesRepository;
    private final MultipleRefScotlandRepository multipleRefScotlandRepository;
    private final RestTemplate restTemplate;
    private final CcdClient ccdClient;
    private final MultipleCasesSendingService multipleCasesSendingService;
    private final AdminUserService adminUserService;

    @Value("${ccd.data-store-api-url}")
    private String ccdDataStoreUrl;

    private static final String NOT_MULTIPLE_ERROR = "The Case (%s) is not a Multiple";
    private static final String MISSING_MULTIPLE_REFERENCE_ERROR = "The Case (%s) is missing a Multiple Reference";
    private static final String SEARCH_CASES_FORMAT = "%s/searchCases?ctid=%s";
    private static final String ADD_USER_ERROR = "Call to add legal rep to Multiple Case failed for %s";

    public synchronized String createReference(String caseTypeId) {
        return switch (caseTypeId) {
            case ENGLANDWALES_BULK_CASE_TYPE_ID -> multipleRefEnglandWalesRepository.ethosMultipleCaseRefGen();
            case SCOTLAND_BULK_CASE_TYPE_ID -> multipleRefScotlandRepository.ethosMultipleCaseRefGen();
            default -> throw new IllegalArgumentException(
                    String.format("Unable to create case reference: unexpected caseTypeId %s", caseTypeId));
        };
    }

    public List<String> validateSubcaseIsOfMultiple(CaseData caseData) {
        List<String> errors = new ArrayList<>();
        if (!YES.equals(caseData.getMultipleFlag())) {
            errors.add(String.format(NOT_MULTIPLE_ERROR, caseData.getEthosCaseReference()));
        }
        if (caseData.getMultipleReference() == null || caseData.getMultipleReference().isBlank()) {
            errors.add(String.format(MISSING_MULTIPLE_REFERENCE_ERROR, caseData.getEthosCaseReference()));
        }
        return errors;
    }

    public void addLegalRepToMultiple(CaseDetails caseDetails, String userToAddId) throws IOException {
        String adminUserToken = adminUserService.getAdminUserToken();
        String caseType = MultiplesHelper.appendMultipleSuffix(caseDetails.getCaseTypeId());
        String multipleRef = caseDetails.getCaseData().getMultipleReference();
        SubmitMultipleEvent multiShell = getMultipleByReference(adminUserToken, caseType, multipleRef);

        String caseId = caseDetails.getCaseId();
        String multipleId = String.valueOf(multiShell.getCaseId());
        MultipleData multipleShell = multiShell.getCaseData();
        if (multipleId.equals("0") || multipleShell == null) {
            log.warn("Add Respondent Representative to Multiple failed. "
                    + "Multiple not found for case {}, with MultipleReference {}", caseId, multipleRef);
            return;
        }

        String jurisdiction = caseDetails.getJurisdiction();
        addUserToMultiple(adminUserToken, jurisdiction, caseType, multipleId, userToAddId);

        String caseRef = caseDetails.getCaseData().getEthosCaseReference();
        updateMultipleLegalRepCollection(
                adminUserToken, caseType, jurisdiction, multipleShell, multipleId, caseRef, userToAddId);
    }

    public SubmitMultipleEvent getMultipleByReference(String adminUserToken,
                                                      String caseType,
                                                      String multipleReference) throws IOException {
        String getUrl = String.format(SEARCH_CASES_FORMAT, ccdDataStoreUrl, caseType);
        String requestBody = ESHelper.getBulkSearchQuery(multipleReference);

        HttpEntity<String> request =
                new HttpEntity<>(
                        requestBody,
                        ccdClient.buildHeaders(adminUserToken)
                );

        ResponseEntity<MultipleCaseSearchResult> response;

        try {
            response = restTemplate
                    .exchange(
                            getUrl,
                            HttpMethod.POST,
                            request,
                            MultipleCaseSearchResult.class
                    );
        } catch (RestClientResponseException exception) {
            log.error("Error from ccd - {}", exception.getMessage());
            throw exception;
        }

        MultipleCaseSearchResult resultBody = response.getBody();

        if (resultBody != null && CollectionUtils.isNotEmpty(resultBody.getCases())) {
            return resultBody.getCases().get(0);
        }

        return new SubmitMultipleEvent();
    }

    public void addUserToMultiple(String adminUserToken,
                               String jurisdiction,
                               String caseType,
                               String multipleId,
                               String userToAddId) throws IOException {
        Map<String, String> payload = Maps.newHashMap();
        payload.put("id", userToAddId);

        String errorMessage = String.format(ADD_USER_ERROR, multipleId);

        try {
            ResponseEntity<Object> response =
                    ccdClient.addUserToMultiple(
                            adminUserToken,
                            jurisdiction,
                            caseType,
                            multipleId,
                            payload);

            if (response == null) {
                throw new CaseCreationException(errorMessage);
            }

            log.info("Http status received from CCD addUserToMultiple API; {}", response.getStatusCodeValue());
        } catch (RestClientResponseException e) {
            throw (CaseCreationException)
                    new CaseCreationException(String.format("%s with %s", errorMessage, e.getMessage())).initCause(e);
        }
    }

    private void updateMultipleLegalRepCollection(
            String userToken,
            String caseTypeId,
            String jurisdiction,
            MultipleData multiDataToUpdate,
            String multipleId,
            String caseRef,
            String legalRepId) {

        ListTypeItem<SubCaseLegalRepDetails> legalRepCollection = multiDataToUpdate.getLegalRepCollection();
        if (legalRepCollection == null) {
            legalRepCollection = new ListTypeItem<>();
            multiDataToUpdate.setLegalRepCollection(legalRepCollection);
        }

        boolean caseExists = false;
        for (GenericTypeItem<SubCaseLegalRepDetails> details : legalRepCollection) {
            if (details.getValue().getCaseReference().equals(caseRef)) {
                caseExists = true;
                if (details.getValue().getLegalRepIds().stream().noneMatch(
                        item -> legalRepId.equals(item.getValue()))) {
                    GenericTypeItem<String> legalRepList = GenericTypeItem.from(legalRepId);
                    details.getValue().getLegalRepIds().add(legalRepList);
                } else {
                    log.info("Legal Rep already exists in collection");
                    return;
                }
                break;
            }
        }

        if (!caseExists) {
            ListTypeItem<String> newLegalRepList = ListTypeItem.from(legalRepId);
            GenericTypeItem<SubCaseLegalRepDetails> newDetails =
                    GenericTypeItem.from(new SubCaseLegalRepDetails(caseRef, newLegalRepList));
            legalRepCollection.add(newDetails);
        }

        multiDataToUpdate.setLegalRepCollection(legalRepCollection);
        multipleCasesSendingService.sendUpdateToMultiple(
                userToken, caseTypeId, jurisdiction, multiDataToUpdate, multipleId);
    }
}
