package uk.gov.hmcts.ethos.replacement.docmosis.service.messagehandler;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.ecm.common.model.servicebus.CreateUpdatesMsg;
import uk.gov.hmcts.et.common.model.bulk.items.CaseIdTypeItem;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantIndType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantType;
import uk.gov.hmcts.et.common.model.ccd.types.multiples.AdditionalClaimant;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.SubmitMultipleEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultiplesHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementForCaseWorkerService;
import uk.gov.hmcts.reform.et.syaapi.service.NotificationService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.netty.util.internal.StringUtil.isNullOrEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ET1_ONLINE_CASE_SOURCE;

/**
 * Creates the child CCD cases for the additional claimants of a multiple, and the multiple shell.
 *
 * <p>Each child case is a copy of the lead case with the claimant identity fields
 * ({@link ClaimantIndType} and {@link ClaimantType}) overridden with the supplied
 * {@link AdditionalClaimant}.  Fields specific to a multiple
 * (e.g. the additional-claimant spreadsheet, {@code addClaimantMethod}) are deliberately
 * not copied to the child cases.
 */
@Slf4j
@Service
public class CreateMultiplesService {

    private final CcdClient ccdClient;
    private final CaseManagementForCaseWorkerService caseManagementForCaseWorkerService;
    private final NotificationService notificationService;

    @Autowired
    public CreateMultiplesService(CcdClient ccdClient,
                                  CaseManagementForCaseWorkerService caseManagementForCaseWorkerService,
                                  NotificationService notificationService) {
        this.ccdClient = ccdClient;
        this.caseManagementForCaseWorkerService = caseManagementForCaseWorkerService;
        this.notificationService = notificationService;
    }

    /**
     * Retrieves the lead (parent) case that child cases are copied from.
     *
     * @param accessToken      admin access token
     * @param createUpdatesMsg the create-updates message carrying the lead ethos case reference
     * @return the lead case, or {@code null} if none is found
     * @throws IOException if CCD communication fails
     */
    public SubmitEvent retrieveLeadCase(String accessToken, CreateUpdatesMsg createUpdatesMsg) throws IOException {
        List<SubmitEvent> submitEvents = ccdClient.retrieveCasesElasticSearch(
                accessToken,
                createUpdatesMsg.getCaseTypeId(),
                createUpdatesMsg.getEthosCaseRefCollection());
        return CollectionUtils.isEmpty(submitEvents) ? null : submitEvents.getFirst();
    }

    /**
     * Copies the lead case and creates a new CCD case whose claimant details are overridden with
     * those of the supplied {@link AdditionalClaimant}.
     *
     * @param leadSubmitEvent    the lead case retrieved from CCD
     * @param accessToken        admin access token
     * @param createUpdatesMsg   the create-updates message carrying the shared case metadata
     * @param additionalClaimant the claimant whose details override the lead case
     * @return the ethos case reference of the created child case, or {@code null} if not created
     * @throws IOException if CCD communication fails
     */
    public String createCase(SubmitEvent leadSubmitEvent, String accessToken, CreateUpdatesMsg createUpdatesMsg,
                             AdditionalClaimant additionalClaimant) throws IOException {

        if (ObjectUtils.isEmpty(additionalClaimant)) {
            return null;
        }

        String leadCaseId = String.valueOf(leadSubmitEvent.getCaseId());
        log.info("Creating additional claimant case from lead case {}", leadCaseId);

        CaseData newCaseData = copyAndOverrideClaimant(leadSubmitEvent.getCaseData(), additionalClaimant);

        CaseDetails newCaseDetails = new CaseDetails();
        newCaseDetails.setCaseTypeId(createUpdatesMsg.getCaseTypeId());
        newCaseDetails.setJurisdiction(createUpdatesMsg.getJurisdiction());
        newCaseDetails.setCaseData(newCaseData);

        CCDRequest ccdRequest = ccdClient.startMultipleCaseCreation(accessToken, newCaseDetails);
        SubmitEvent createdCase = ccdClient.submitCaseCreation(
                accessToken,
                newCaseDetails,
                ccdRequest,
                "Case created as part of multiple - lead case " + leadSubmitEvent.getCaseData().getEthosCaseReference()
        );

        if (ObjectUtils.isEmpty(createdCase)) {
            log.warn("CCD returned no case when creating additional claimant case from lead {}", leadCaseId);
            return null;
        }

        log.info("Setting HMCTS service ID for new case {}", createdCase.getCaseId());
        ccdRequest = ccdClient.startEventForCase(accessToken, createUpdatesMsg.getCaseTypeId(),
                createUpdatesMsg.getJurisdiction(), String.valueOf(createdCase.getCaseId()));
        caseManagementForCaseWorkerService.setHmctsServiceIdSupplementary(ccdRequest.getCaseDetails());
        log.info("Successfully set HMCTS service ID for new case {}", createdCase.getCaseId());

        return Optional.ofNullable(createdCase.getCaseData())
                .map(CaseData::getEthosCaseReference)
                .orElse(null);
    }

    /**
     * Creates a new multiple "shell" case (the {@code _Multiple} case type) grouping the lead case
     * and the newly created child cases.  A fresh multiple is always created - the lead single case
     * is only linked as the first member of the multiple, it is never converted into the shell.
     *
     * <p>The multiple reference and managing office are populated explicitly here because, for the
     * ET1-online source, the {@code createMultiple} callback does not generate them.  Without a
     * reference / managing office the created multiple is invalid and does not show up.
     *
     * @param accessToken      admin access token
     * @param createUpdatesMsg the create-updates message carrying the lead ethos reference and metadata
     * @param leadSubmitEvent  the lead case, used for the managing office and lead ethos reference
     * @param childEthosRefs   ethos case references of the created child cases
     * @return the created multiple, or {@code null} if creation failed
     * @throws IOException if CCD communication fails
     */
    public SubmitMultipleEvent createMultipleShell(String accessToken, CreateUpdatesMsg createUpdatesMsg,
                                                   SubmitEvent leadSubmitEvent,
                                                   List<String> childEthosRefs,
                                                   Map<Integer, AdditionalClaimant> failedCases) throws IOException {

        CaseData leadCaseData = Optional.ofNullable(leadSubmitEvent)
                .map(SubmitEvent::getCaseData)
                .orElse(null);
        String leadCaseRef = Optional.ofNullable(leadCaseData)
                .map(CaseData::getEthosCaseReference)
                .orElseGet(() -> firstEthosRef(createUpdatesMsg.getEthosCaseRefCollection()).orElse(null));

        List<CaseIdTypeItem> caseIdCollection = new ArrayList<>();
        if (ObjectUtils.isNotEmpty(leadCaseRef)) {
            caseIdCollection.add(MultiplesHelper.createCaseIdTypeItem(leadCaseRef));
        }
        if (ObjectUtils.isNotEmpty(childEthosRefs)) {
            for (String childRef : childEthosRefs) {
                if (ObjectUtils.isNotEmpty(childRef)) {
                    caseIdCollection.add(MultiplesHelper.createCaseIdTypeItem(childRef));
                }
            }
        }
        String multipleName = Optional.ofNullable(leadCaseData).map(CaseData::getRespondent).orElse(null);
        String managingOffice = Optional.ofNullable(leadCaseData).map(CaseData::getManagingOffice).orElse(null);
        MultipleData multipleData = new MultipleData();
        multipleData.setMultipleName(multipleName);
        multipleData.setMultipleSource(ET1_ONLINE_CASE_SOURCE);
        multipleData.setCaseIdCollection(caseIdCollection);
        multipleData.setLeadCase(leadCaseRef);
        multipleData.setManagingOffice(managingOffice);

        String jurisdiction = createUpdatesMsg.getJurisdiction();
        String multipleCaseTypeId = UtilHelper.getBulkCaseTypeId(createUpdatesMsg.getCaseTypeId());
        log.info("Creating new multiple shell {} for lead case {} with {} case(s)",
            multipleCaseTypeId, leadCaseRef, caseIdCollection.size());
        String addClaimantMethod = Optional.ofNullable(leadCaseData).map(CaseData::getAddClaimantMethod).orElse(null);

        handleFailedCases(failedCases, multipleData, addClaimantMethod);
        CCDRequest ccdRequest = ccdClient.startCaseMultipleCreation(accessToken, multipleCaseTypeId, jurisdiction);
        SubmitMultipleEvent createdMultiple = ccdClient.submitMultipleCreation(
            accessToken, multipleData, multipleCaseTypeId, jurisdiction, ccdRequest);

        if (ObjectUtils.isNotEmpty(createdMultiple)) {
            if (ObjectUtils.isNotEmpty(failedCases)) {
                sendEmailForFailedCases(failedCases, createdMultiple);
            }
            log.info("Created multiple shell case {}", createdMultiple.getCaseId());
        } else {
            String ccdId = Optional.ofNullable(leadCaseData).map(CaseData::getCcdID).orElse(null);
            long feeGroupReference = Optional.ofNullable(leadCaseData)
                    .map(CaseData::getFeeGroupReference)
                    .filter(ObjectUtils::isNotEmpty)
                    .map(Long::parseLong)
                    .orElse(0L);
            log.info("Error creating multiple shell case for {}", ccdId);
            notificationService.sendFailedMultiplesShellCreationEmail(
                    leadCaseRef,
                    feeGroupReference);
        }
        return createdMultiple;
    }

    private void handleFailedCases(Map<Integer, AdditionalClaimant> failedCases,
                                   MultipleData multipleData, String addClaimantMethod) {
        if (failedCases.isEmpty()) {
            return;
        }

        boolean isSpreadsheetUpload = !"manually".equals(addClaimantMethod);
        String source = isSpreadsheetUpload ? "spreadsheet upload" : "manual entry";

        String failedClaimantDetails = failedCases.entrySet().stream()
                .map(entry -> buildFailedClaimantLine(entry.getKey(), entry.getValue(), isSpreadsheetUpload))
                .collect(Collectors.joining("\n"));

        multipleData.setMultipleNote(
                "Additional claimant cases failed to create for the following claimant(s), added via "
                        + source + ":\n" + failedClaimantDetails);
    }

    private void sendEmailForFailedCases(Map<Integer, AdditionalClaimant> failedCases,
                                   SubmitMultipleEvent createdMultiple) {

        notificationService.sendFailedAdditionalClaimantsEmail(
                createdMultiple.getCaseData().getLeadEthosCaseRef(),
                createdMultiple.getCaseData().getMultipleReference(),
                createdMultiple.getCaseId());
        log.error("Multiple {} created with {} failed additional claimant(s)",
                createdMultiple.getCaseData().getMultipleReference(), failedCases.size());
    }

    private static String buildFailedClaimantLine(int index, AdditionalClaimant claimant, boolean isSpreadsheetUpload) {
        String location = isSpreadsheetUpload
                ? "Row " + (index + 1)
                : "Additional claimant " + (index + 1);

        if (ObjectUtils.isEmpty(claimant)) {
            return "- " + location + ": no claimant data supplied";
        }

        String firstName = isNullOrEmpty(claimant.getFirstName())
                ? "[First name not provided]" : claimant.getFirstName();
        String lastName = isNullOrEmpty(claimant.getLastName())
                ? "[Last name not provided]" : claimant.getLastName();

        return "- " + location + " (" + firstName + " " + lastName + ")";
    }

    private static Optional<String> firstEthosRef(List<String> ethosRefs) {
        return ethosRefs == null || ethosRefs.isEmpty()
                ? Optional.empty()
                : Optional.ofNullable(ethosRefs.getFirst());
    }

    /**
     * Converts a DOB string to ISO format ({@code YYYY-MM-DD}) expected by CCD.
     * Accepts {@code DD/MM/YYYY} (frontend / spreadsheet format) and passes through
     * values already in {@code YYYY-MM-DD}.  Returns {@code null} for blank input so
     * callers can skip setting the field and avoid CCD validation errors.
     */
    private String convertDobToIso(String dob) {
        if (ObjectUtils.isEmpty(dob)) {
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
        newCaseData.setClaimantRequests(leadCaseData.getClaimantRequests());

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
