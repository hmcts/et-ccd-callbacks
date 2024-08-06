package uk.gov.hmcts.ethos.replacement.docmosis.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.index.query.WildcardQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;

import java.util.Arrays;
import java.util.List;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.ACCEPTED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.EMPLOYMENT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.REJECTED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SUBMITTED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.VETTED_STATE;

@Component
@RequiredArgsConstructor
@Slf4j
public class PartySpacingTask {
    private final AdminUserService adminUserService;
    private final CcdClient ccdClient;
    private final FeatureToggleService featureToggleService;

    private static final String CLAIMANT_FIRST_NAMES_KEYWORD = "data.claimantIndType.claimant_first_names.keyword";
    private static final String CLAIMANT_LAST_NAME_KEYWORD = "data.claimantIndType.claimant_last_name.keyword";
    private static final String RESPONDENT_NAME_KEYWORD = "data.respondentCollection.value.respondent_name.keyword";
    private static final String CLAIMANT_REP_NAME = "data.representativeClaimantType.name_of_representative.keyword";
    private static final String RESPONDENT_REP_NAME = "data.repCollection.value.name_of_representative.keyword";
    private final List<String> validStates = List.of(SUBMITTED_STATE, VETTED_STATE, ACCEPTED_STATE, REJECTED_STATE);

    @Value("${cron.caseTypeId}")
    private String caseTypeIdsString;

    @Value("${cron.maxCasesPerSearch}")
    private int maxCases;

    @Scheduled(cron = "${cron.partySpacing}")
    public void refactorPartySpacing() {
        if (!featureToggleService.isPartySpacingCronEnabled()) {
            return;
        }

        String query = buildQuery();
        String adminUserToken = adminUserService.getAdminUserToken();
        String[] caseTypeIds = caseTypeIdsString.split(",");

        Arrays.stream(caseTypeIds).forEach(caseTypeId -> {
            try {
                List<SubmitEvent> cases = ccdClient.buildAndGetElasticSearchRequest(adminUserToken, caseTypeId, query);
                log.info("{} - Party spacing task - Retrieved {} cases", caseTypeId, cases.size());
                while (!cases.isEmpty()) {
                    if (featureToggleService.isPartySpacingCronEnabled()) {
                        cases.forEach(submitEvent -> triggerEventForCase(adminUserToken, submitEvent, caseTypeId));
                    }
                    cases = ccdClient.buildAndGetElasticSearchRequest(adminUserToken, caseTypeId, query);
                    log.info("{} - Party spacing task - Retrieved {} cases", caseTypeId, cases.size());
                }
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        });
    }

    private void triggerEventForCase(String adminUserToken, SubmitEvent submitEvent, String caseTypeId) {
        try {
            CCDRequest ccdRequest = ccdClient.startEventForCase(adminUserToken, caseTypeId, EMPLOYMENT,
                    String.valueOf(submitEvent.getCaseId()), "UPDATE_CASE_SUBMITTED");

            CaseDetails caseDetails = ccdRequest.getCaseDetails();
            CaseData caseData = caseDetails.getCaseData();
            Helper.removeSpacesFromPartyNames(caseData);

            ccdClient.submitEventForCase(adminUserToken, caseData, caseTypeId,
                    caseDetails.getJurisdiction(), ccdRequest, String.valueOf(submitEvent.getCaseId()));
            log.info("Removed spaces in party names for case {}", submitEvent.getCaseId());
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    /**
     * Builds the query to search for cases with spaces in party names.
     *
     * @return the query
     */
    private String buildQuery() {
        return new SearchSourceBuilder()
                .size(maxCases)
                .query(new BoolQueryBuilder()
                        .must(new TermsQueryBuilder("state.keyword", validStates))
                        // Not looking for claimant company as not onboarded yet
                        .should(new WildcardQueryBuilder(CLAIMANT_FIRST_NAMES_KEYWORD, "* "))
                        .should(new WildcardQueryBuilder(CLAIMANT_FIRST_NAMES_KEYWORD, " *"))
                        .should(new WildcardQueryBuilder(CLAIMANT_LAST_NAME_KEYWORD, "* "))
                        .should(new WildcardQueryBuilder(CLAIMANT_LAST_NAME_KEYWORD, " *"))
                        .should(new WildcardQueryBuilder(RESPONDENT_NAME_KEYWORD, "* "))
                        .should(new WildcardQueryBuilder(RESPONDENT_NAME_KEYWORD, " *"))
                        .should(new WildcardQueryBuilder(CLAIMANT_REP_NAME, " *"))
                        .should(new WildcardQueryBuilder(CLAIMANT_REP_NAME, "* "))
                        .should(new WildcardQueryBuilder(RESPONDENT_REP_NAME, "* "))
                        .should(new WildcardQueryBuilder(RESPONDENT_REP_NAME, " *"))
                ).toString();
    }
}
