package uk.gov.hmcts.ethos.replacement.docmosis.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.Et1SubmissionService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.EMPLOYMENT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.DocumentHelper.setDocumentNumbers;

@Component
@RequiredArgsConstructor
@Slf4j
public class AcasCertificateTask {
    private final Et1SubmissionService et1SubmissionService;
    private final AdminUserService adminUserService;
    private final CcdClient ccdClient;
    private final FeatureToggleService featureToggleService;

    @Value("${cron.caseTypeId}")
    private String caseTypeIdsString;

    @Value("${cron.maxCasesPerSearch}")
    private int maxCases;

    @Scheduled(cron = "${cron.acasCertSubmission}")
    public void generateAcasCertificates() {
        if (!featureToggleService.isAcasCertificatePostSubmissionEnabled()) {
            return;
        }
        String query = buildQuery();
        String adminUserToken = adminUserService.getAdminUserToken();
        String[] caseTypeIds = caseTypeIdsString.split(",");

        for (String caseTypeId : caseTypeIds) {
            try {
                List<SubmitEvent> cases = ccdClient.buildAndGetElasticSearchRequest(adminUserToken, caseTypeId, query);
                log.info("{} - Acas certificate task - Retrieved {} cases", caseTypeId, cases.size());
                while (!cases.isEmpty()) {
                    cases.forEach(c -> triggerEventForCase(adminUserToken, c, caseTypeId));
                    cases = ccdClient.buildAndGetElasticSearchRequest(adminUserToken, caseTypeId, query);
                    log.info("{} - Acas certificate task - Retrieved {} cases", caseTypeId, cases.size());
                }
            } catch (IOException e) {
                log.error("Error retrieving cases for case type ID {}: {}", caseTypeId, e.getMessage(), e);
            }
        }
    }

    private void triggerEventForCase(String adminUserToken, SubmitEvent submitEvent, String caseTypeId) {
        try {
            CCDRequest ccdRequest = ccdClient.startEventForCase(adminUserToken, caseTypeId, EMPLOYMENT,
                    String.valueOf(submitEvent.getCaseId()), "UPDATE_CASE_SUBMITTED");
            CaseDetails caseDetails = ccdRequest.getCaseDetails();
            CaseData caseData = caseDetails.getCaseData();
            if (caseData.getAcasCertificateRequired() == null || !YES.equals(caseData.getAcasCertificateRequired())) {
                return;
            }
            List<DocumentTypeItem> acasCertificates =
                    et1SubmissionService.retrieveAndAddAcasCertificates(caseData, adminUserToken, caseTypeId);
            if (isNotEmpty(acasCertificates)) {
                if (isEmpty(caseData.getDocumentCollection())) {
                    caseData.setDocumentCollection(new ArrayList<>());
                }
                caseData.getDocumentCollection().addAll(acasCertificates);
                setDocumentNumbers(caseData);
                if (isEmpty(caseData.getClaimantDocumentCollection())) {
                    caseData.setClaimantDocumentCollection(new ArrayList<>());
                }
                caseData.getClaimantDocumentCollection().addAll(acasCertificates);
            }
            caseData.setAcasCertificateRequired(NO);
            ccdClient.submitEventForCase(adminUserToken, caseData, caseTypeId,
                    EMPLOYMENT, ccdRequest, String.valueOf(submitEvent.getCaseId()));
            log.info("Added Acas certificate for case {}", submitEvent.getCaseId());
        } catch (Exception e) {
            log.error("Error triggering event for case ID {}: {}", submitEvent.getCaseId(), e.getMessage(), e);
        }
    }

    private String buildQuery() {
        return new SearchSourceBuilder()
                .size(maxCases)
                .query(new BoolQueryBuilder()
                        .must(new TermsQueryBuilder("state.keyword", "Submitted"))
                        .must(new TermQueryBuilder("data.acasCertificateRequired", YES))
                ).toString();
    }

}
