package uk.gov.hmcts.reform.et.syaapi.service.multiples;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantIndType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantType;
import uk.gov.hmcts.et.common.model.ccd.types.multiples.AdditionalClaimant;
import uk.gov.hmcts.et.common.model.multiples.SubmitMultipleEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultiplesHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.multiples.MultipleReferenceService;
import uk.gov.hmcts.reform.et.syaapi.service.AdminUserService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides read and write access to multiples cases stored by ET.
 */

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.TooManyMethods"})
public class MultiplesCaseService {

    private final AdminUserService adminUserService;
    private final MultipleReferenceService multipleReferenceService;
    private final CcdClient ccdClient;

    /**
     * Retrieves multiple case details by case type and case reference using an admin token.
     * Used to fetch the multiple shell case associated with a lead case.
     *
     * @param caseTypeId the case type identifier
     * @param caseId the CCD case ID of the multiple case (from {@code parentMultipleCaseId} on the lead case)
     * @return the {@link SubmitMultipleEvent} for the multiple case
     */
    public SubmitMultipleEvent getMultipleCaseByCaseReference(String caseTypeId, String caseId) throws IOException {
        String multipleCaseTypeId = caseTypeId != null && caseTypeId.endsWith(MultiplesHelper.MULTIPLE_SUFFIX)
                ? caseTypeId
                : MultiplesHelper.appendMultipleSuffix(caseTypeId);
        log.info("Fetching multiple case by caseTypeId {} and caseId: {}", multipleCaseTypeId, caseId);
        String adminToken = adminUserService.getAdminUserToken();
        return multipleReferenceService.getMultipleByReference(adminToken, multipleCaseTypeId, caseId);
    }

    /**
     * Retrieves every single case belonging to a multiple, identified by matching
     * {@code caseData.multipleReference} against the multiple's reference, and maps each into an
     * {@link AdditionalClaimant}.
     *
     * <p>Uses an admin token internally, so this is not scoped to any particular respondent's case access.
     *
     * @param caseTypeId        the base (single-case) case type id, e.g. {@code ENGLANDWALES_CASE_TYPE_ID} -
     *                           NOT the multiple-suffixed type
     * @param multipleReference the multiple reference to match against {@code caseData.multipleReference}
     * @return additional claimants for every matching case; empty if none found
     */
    public List<AdditionalClaimant> getAdditionalClaimantsByMultipleReference(
            String caseTypeId, String multipleReference) {

        if (StringUtils.isBlank(multipleReference)) {
            log.warn("Skipping additional claimants ES search - multipleReference is blank");
            return List.of();
        }

        List<SubmitEvent> submitEvents;
        try {
            String adminToken = adminUserService.getAdminUserToken();
            submitEvents = ccdClient.retrieveCasesByMultipleReferenceElasticSearch(
                    adminToken, caseTypeId, multipleReference);
        } catch (Exception ex) {
            log.error("Error retrieving cases for multipleReference {}", multipleReference, ex);
            throw new RuntimeException("Error retrieving cases for multiple reference", ex);
        }

        if (CollectionUtils.isEmpty(submitEvents)) {
            log.info("No cases found for multipleReference {}", multipleReference);
            return List.of();
        }

        List<AdditionalClaimant> additionalClaimants = new ArrayList<>();
        for (SubmitEvent submitEvent : submitEvents) {
            if (submitEvent == null) {
                continue;
            }
            AdditionalClaimant claimant = toAdditionalClaimant(submitEvent.getCaseData());
            if (claimant != null) {
                additionalClaimants.add(claimant);
            }
        }
        return additionalClaimants;
    }

    /**
     * Maps a single case's {@link CaseData} into an {@link AdditionalClaimant}.
     *
     * @param caseData the case data to map
     * @return the mapped claimant, or {@code null} if caseData is null
     */
    private AdditionalClaimant toAdditionalClaimant(CaseData caseData) {
        if (caseData == null) {
            return null;
        }
        ClaimantIndType indType = caseData.getClaimantIndType();
        ClaimantType claimantType = caseData.getClaimantType();

        return AdditionalClaimant.builder()
                .firstName(indType != null ? indType.getClaimantFirstNames() : null)
                .lastName(indType != null ? indType.getClaimantLastName() : null)
                .email(claimantType != null ? claimantType.getClaimantEmailAddress() : null)
                .address(claimantType != null ? claimantType.getClaimantAddressUK() : null)
                .build();
    }
}
