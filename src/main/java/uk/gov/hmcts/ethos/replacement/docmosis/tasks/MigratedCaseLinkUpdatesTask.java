package uk.gov.hmcts.ethos.replacement.docmosis.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.ExistsQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseRetrievalForCaseWorkerService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.exceptions.CaseDuplicateSearchException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.ACCEPTED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLOSED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.REJECTED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SUBMITTED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TRANSFERRED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.VETTED_STATE;

@Component
@RequiredArgsConstructor
@Slf4j
public class MigratedCaseLinkUpdatesTask {
    private final AdminUserService adminUserService;
    private final CaseRetrievalForCaseWorkerService caseRetrievalForCaseWorkerService;
    private final CcdClient ccdClient;
    private final FeatureToggleService featureToggleService;
    private final List<String> validStates = List.of(TRANSFERRED_STATE, ACCEPTED_STATE, REJECTED_STATE,
            SUBMITTED_STATE, VETTED_STATE, CLOSED_STATE);
    private static final String EVENT_ID = "migrateCaseLinkDetails";
    @Value("${cron.caseTypeId}")
    private String caseTypeIdsString;

    @Value("${cron.maxCasesPerSearch}")
    private int maxCases;
    private static final int TWO = 2;

    @Scheduled(cron = "${cron.updateTransferredCaseLinks}")
    public void updateTransferredCaseLinks() {

        if (!featureToggleService.isUpdateTransferredCaseLinksEnabled()) {
            return;
        }

        String query = buildStartQuery();
        String adminUserToken = adminUserService.getAdminUserToken();
        String[] caseTypeIds = caseTypeIdsString.split(",");

        List.of(caseTypeIds).forEach(caseTypeId -> {
            try {
                //Get transferred cases by case type
                List<SubmitEvent> transferredCases = ccdClient.buildAndGetElasticSearchRequest(adminUserToken,
                        caseTypeId, query);

                for (SubmitEvent submitEvent : transferredCases) {
                    if (!featureToggleService.isUpdateTransferredCaseLinksEnabled()) {
                        return;
                    }

                    //find possible duplicate cases by ethos reference
                    //list of pairs of 'case type id' and 'list of submit events'
                    List<Pair<String, List<SubmitEvent>>> listOfPairs = findCaseByEthosReference(
                            adminUserToken, submitEvent.getCaseData().getEthosCaseReference());

                    for (Pair<String, List<SubmitEvent>> pair : listOfPairs) {
                        List<SubmitEvent> duplicates = pair.getRight();
                        if (duplicates != null && haveSameCheckedFieldValues(duplicates)) {
                            //update valid matching duplicates by triggering event for case
                            String sourceCaseTypeId = pair.getLeft();
                            triggerEventForCase(adminUserToken, submitEvent, duplicates, caseTypeId, sourceCaseTypeId);
                        }
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        });
    }

    // Checked field values : ethos ref, claimant, submission ref(i.e. FeeGroupReference),
    // and date of receipt
    private boolean haveSameCheckedFieldValues(List<SubmitEvent> duplicates) {
        SubmitEvent sourceCaseData = duplicates.get(0);
        SubmitEvent targetCaseData = duplicates.get(1);
        if (sourceCaseData.getCaseData() == null || targetCaseData.getCaseData() == null) {
            return false;
        }

        boolean checkResult = sourceCaseData.getCaseData().getEthosCaseReference().equals(
                targetCaseData.getCaseData().getEthosCaseReference())
                && sourceCaseData.getCaseData().getClaimant().equals(
                        targetCaseData.getCaseData().getClaimant())
                && sourceCaseData.getCaseData().getFeeGroupReference().equals(
                        targetCaseData.getCaseData().getFeeGroupReference())
                && sourceCaseData.getCaseData().getReceiptDate().equals(targetCaseData.getCaseData().getReceiptDate());
        log.info("The haveSameCheckedFieldValues method result is {} ", checkResult);
        return checkResult;
    }

    public  List<Pair<String, List<SubmitEvent>>> findCaseByEthosReference(String adminUserToken,
                                                                           String ethosReference) {
        String followUpQuery = buildFollowUpQuery(ethosReference);
        String[] caseTypeIds = caseTypeIdsString.split(",");
        List<Pair<String, List<SubmitEvent>>> pairsList = new ArrayList<>();

        //search for duplicates in all case types and group the result by case type id
        List.of(caseTypeIds).forEach(sourceCaseTypeId -> {
            try {
                //for each transferred case, get duplicates by ethos ref
                List<SubmitEvent> duplicateCases = ccdClient.buildAndGetElasticSearchRequest(adminUserToken,
                        sourceCaseTypeId, followUpQuery);
                if (duplicateCases.size() == TWO) {
                    pairsList.add(Pair.of(sourceCaseTypeId, duplicateCases));
                }
            } catch (IOException exception) {
                String errorMessage = String.format(
                        "Error searching for duplicates by Ethos reference for case type: %s", sourceCaseTypeId);
                log.info(errorMessage, exception);
                throw new CaseDuplicateSearchException(exception.getMessage(), exception);
            }
        });

        return pairsList;
    }

    public void triggerEventForCase(String adminUserToken, SubmitEvent targetSubmitEvent,
                                     List<SubmitEvent> duplicates, String targetCaseTypeId, String sourceCaseTypeId) {
        try {
            //get the source case details from the duplicates list
            SubmitEvent sourceCaseData = duplicates.stream()
                .filter(submitEvent -> !submitEvent.getCaseData().getCcdID().equals(
                        targetSubmitEvent.getCaseData().getCcdID())).findFirst().orElse(null);

            if (sourceCaseData == null || sourceCaseData.getCaseData() == null) {
                log.info("In triggerEventForCase method: Source case not found in duplicates list");
                return;
            }

            log.info("In triggerEventForCase method: Updating case {} with source case {}",
                    targetSubmitEvent.getCaseId(), sourceCaseData.getCaseData().getCcdID());

            CCDRequest returnedRequest = ccdClient.startEventForCase(adminUserToken, targetCaseTypeId,
                    "EMPLOYMENT", String.valueOf(targetSubmitEvent.getCaseId()), EVENT_ID);
            CaseDetails targetCaseDetails = returnedRequest.getCaseDetails();

            // update target's two fields that will be used for link construction by triggering
            // the migrateCaseLinkDetails event for case
            targetCaseDetails.getCaseData().setTransferredCaseLinkSourceCaseId(
                    String.valueOf(sourceCaseData.getCaseId()));
            targetCaseDetails.getCaseData().setTransferredCaseLinkSourceCaseTypeId(sourceCaseTypeId);

            ccdClient.submitEventForCase(adminUserToken, targetCaseDetails.getCaseData(),
                    targetCaseDetails.getCaseTypeId(), targetCaseDetails.getJurisdiction(),
                    returnedRequest, targetCaseDetails.getCaseId());
        } catch (Exception e) {
            log.info("An exception occurred while trying to update target two fields. \nMessage: {}", e.getMessage());
        }
    }

    private String buildStartQuery() {
        return new SearchSourceBuilder()
                .size(maxCases)
                .query(new BoolQueryBuilder()
                        .must(new TermsQueryBuilder("state.keyword", validStates.get(0)))
                        .mustNot(new ExistsQueryBuilder("data.transferredCaseLink"))
                )
                .fetchSource(new String[]{"reference"}, null)
                .sort("reference.keyword", SortOrder.ASC)
                .toString();
    }

    private String buildFollowUpQuery(String ethosCaseReference) {
        return new SearchSourceBuilder()
                .size(maxCases)
                .query(new BoolQueryBuilder()
                        .must(new TermsQueryBuilder("state.keyword", validStates))
                        .must(new TermsQueryBuilder("data.ethosCaseReference", ethosCaseReference))
                )
                .fetchSource(new String[]{"reference"}, null)
                .sort("reference.keyword", SortOrder.ASC)
                .toString();
    }
}
