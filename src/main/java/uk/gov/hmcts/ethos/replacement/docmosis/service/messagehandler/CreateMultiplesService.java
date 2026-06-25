package uk.gov.hmcts.ethos.replacement.docmosis.service.messagehandler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.model.servicebus.UpdateCaseMsg;
import uk.gov.hmcts.ecm.common.model.servicebus.datamodel.CreateMultiplesDataModel;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantIndType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantType;
import uk.gov.hmcts.et.common.model.ccd.types.multiples.AdditionalClaimant;

import java.io.IOException;

/**
 * Creates a new CCD case for a single additional claimant within a multiple.
 *
 * <p>The new case is a copy of the lead case with the claimant identity fields
 * ({@link ClaimantIndType} and {@link ClaimantType}) overridden with the details
 * supplied in the {@link CreateMultiplesDataModel}.  Fields specific to a multiple
 * (e.g. the additional-claimant spreadsheet, {@code addClaimantMethod}) are deliberately
 * not copied to the child cases.
 */
@Slf4j
@Service
public class CreateMultiplesService {

    private final CcdClient ccdClient;

    @Autowired
    public CreateMultiplesService(CcdClient ccdClient) {
        this.ccdClient = ccdClient;
    }

    /**
     * Copies the lead case and creates a new CCD case whose claimant details are
     * overridden with those carried by the {@link CreateMultiplesDataModel}.
     *
     * @param leadSubmitEvent the lead case retrieved from CCD
     * @param accessToken     admin access token
     * @param updateCaseMsg   the update-case message carrying the {@link CreateMultiplesDataModel}
     * @throws IOException if CCD communication fails
     */
    public void createCase(SubmitEvent leadSubmitEvent, String accessToken, UpdateCaseMsg updateCaseMsg)
            throws IOException {

        CreateMultiplesDataModel dataModel = (CreateMultiplesDataModel) updateCaseMsg.getDataModelParent();
        AdditionalClaimant additionalClaimant = dataModel.getAdditionalClaimant();

        if (additionalClaimant == null) {
            log.warn("CreateMultiplesDataModel has no additionalClaimant - skipping case creation for msgId={}",
                    updateCaseMsg.getMsgId());
            return;
        }

        String leadCaseId = String.valueOf(leadSubmitEvent.getCaseId());
        log.info("Creating additional claimant case from lead case {}", leadCaseId);

        CaseData newCaseData = copyAndOverrideClaimant(leadSubmitEvent.getCaseData(), additionalClaimant);

        CaseDetails newCaseDetails = new CaseDetails();
        newCaseDetails.setCaseTypeId(updateCaseMsg.getCaseTypeId());
        newCaseDetails.setJurisdiction(updateCaseMsg.getJurisdiction());
        newCaseDetails.setCaseData(newCaseData);

        CCDRequest ccdRequest = ccdClient.startCaseCreation(accessToken, newCaseDetails);
        SubmitEvent createdCase = ccdClient.submitCaseCreation(
                accessToken,
                newCaseDetails,
                ccdRequest,
                "Case created as part of multiple - lead case " + leadSubmitEvent.getCaseData().getEthosCaseReference()
        );

        if (createdCase != null) {
            log.info("Successfully created additional claimant case {} from lead case {}",
                    createdCase.getCaseId(), leadCaseId);
        }
    }

    /**
     * Converts a DOB string to ISO format ({@code YYYY-MM-DD}) expected by CCD.
     * Accepts {@code DD/MM/YYYY} (frontend / spreadsheet format) and passes through
     * values already in {@code YYYY-MM-DD}.  Returns {@code null} for blank input so
     * callers can skip setting the field and avoid CCD validation errors.
     */
    private String convertDobToIso(String dob) {
        if (dob == null || dob.isBlank()) {
            return null;
        }
        // DD/MM/YYYY → YYYY-MM-DD
        if (dob.matches("\\d{2}/\\d{2}/\\d{4}")) {
            String[] parts = dob.split("/");
            return parts[2] + "-" + parts[1] + "-" + parts[0];
        }
        // Already YYYY-MM-DD or unknown format - return as-is
        return dob;
    }

    private CaseData copyAndOverrideClaimant(CaseData leadCaseData, AdditionalClaimant additionalClaimant) {
        CaseData newCaseData = new CaseData();

        // Copy all shared case fields from the lead case
        newCaseData.setEcmCaseType(leadCaseData.getEcmCaseType());
        newCaseData.setClaimantTypeOfClaimant(leadCaseData.getClaimantTypeOfClaimant());
        newCaseData.setClaimantCompany(leadCaseData.getClaimantCompany());
        newCaseData.setClaimantOtherType(leadCaseData.getClaimantOtherType());
        newCaseData.setClaimantWorkAddressQuestion(leadCaseData.getClaimantWorkAddressQuestion());
        newCaseData.setClaimantWorkAddressQRespondent(leadCaseData.getClaimantWorkAddressQRespondent());
        newCaseData.setClaimantWorkAddress(leadCaseData.getClaimantWorkAddress());
        newCaseData.setClaimantRepresentedQuestion(leadCaseData.getClaimantRepresentedQuestion());
        newCaseData.setRepresentativeClaimantType(leadCaseData.getRepresentativeClaimantType());
        newCaseData.setRespondentCollection(leadCaseData.getRespondentCollection());
        newCaseData.setRepCollection(leadCaseData.getRepCollection());
        newCaseData.setJurCodesCollection(leadCaseData.getJurCodesCollection());
        newCaseData.setReceiptDate(leadCaseData.getReceiptDate());
        newCaseData.setPositionType(leadCaseData.getPositionType());
        newCaseData.setManagingOffice(leadCaseData.getManagingOffice());
        newCaseData.setCaseSource(leadCaseData.getCaseSource());
        newCaseData.setAdditionalCaseInfoType(leadCaseData.getAdditionalCaseInfoType());
        newCaseData.setDocumentCollection(leadCaseData.getDocumentCollection());
        newCaseData.setTypesOfClaim(leadCaseData.getTypesOfClaim());
        newCaseData.setConciliationTrack(leadCaseData.getConciliationTrack());
        newCaseData.setPreAcceptCase(leadCaseData.getPreAcceptCase());
        newCaseData.setMultipleReference(leadCaseData.getMultipleReference());

        // Override claimant identity fields with those of the additional claimant
        ClaimantIndType claimantIndType = new ClaimantIndType();
        claimantIndType.setClaimantTitle(additionalClaimant.getTitle());
        claimantIndType.setClaimantFirstNames(additionalClaimant.getFirstName());
        claimantIndType.setClaimantLastName(additionalClaimant.getLastName());
        // CCD requires YYYY-MM-DD; the frontend stores DOB as DD/MM/YYYY
        String dob = convertDobToIso(additionalClaimant.getDob());
        if (dob != null) {
            claimantIndType.setClaimantDateOfBirth(dob);
        }
        newCaseData.setClaimantIndType(claimantIndType);

        ClaimantType claimantType = new ClaimantType();
        claimantType.setClaimantEmailAddress(additionalClaimant.getEmail());
        claimantType.setClaimantAddressUK(additionalClaimant.getAddress());
        newCaseData.setClaimantType(claimantType);

        return newCaseData;
    }
}
