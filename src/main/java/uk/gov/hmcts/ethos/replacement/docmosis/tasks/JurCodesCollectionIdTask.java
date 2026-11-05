package uk.gov.hmcts.ethos.replacement.docmosis.tasks;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.impl.PostgresAdvisoryLock;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.JurisdictionCodeHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.sql.DataSource;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.EMPLOYMENT;

@Component
@Slf4j
public class JurCodesCollectionIdTask implements Runnable {
    private static final String LOCK_NAMESPACE = "et-jur-codes-id-fix";
    private static final String LOCK_NAME = "task";
    private static final String FIX_CASE_API = "fixCaseAPI";

    static final String FIND_CASES_MISSING_JUR_CODE_IDS = """
        SELECT reference
        FROM ccd.case_data cd
        WHERE cd.case_type_id = :caseTypeId
          AND cd.id IN (
              SELECT case_data_id
              FROM ccd.case_event
              WHERE event_id = 'submitEt1Draft'
                AND created_date::date >= '2026-09-09'
          )
          AND jsonb_typeof(cd.data->'jurCodesCollection') = 'array'
          AND EXISTS (
              SELECT 1
              FROM jsonb_array_elements(cd.data->'jurCodesCollection') AS elem
              WHERE NULLIF(elem->>'id', '') IS NULL
          )
        LIMIT :maxCases
        """;

    private final AdminUserService adminUserService;
    private final CcdClient ccdClient;
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final PostgresAdvisoryLock advisoryLock;

    @Value("${cron.caseTypeId}")
    private String caseTypeIdsString;

    @Value("${cron.maxCasesPerSearch}")
    private int maxCases;

    @Autowired
    public JurCodesCollectionIdTask(
            AdminUserService adminUserService,
            CcdClient ccdClient,
            NamedParameterJdbcTemplate jdbcTemplate,
            DataSource dataSource) {
        this(adminUserService, ccdClient, jdbcTemplate, new PostgresAdvisoryLock(dataSource));
    }

    JurCodesCollectionIdTask(
            AdminUserService adminUserService,
            CcdClient ccdClient,
            NamedParameterJdbcTemplate jdbcTemplate,
            PostgresAdvisoryLock advisoryLock) {
        this.adminUserService = adminUserService;
        this.ccdClient = ccdClient;
        this.jdbcTemplate = jdbcTemplate;
        this.advisoryLock = advisoryLock;
    }

    @Override
    @Scheduled(cron = "0 0 20 * * ?")
    public void run() {
        boolean acquired = advisoryLock.runIfAcquired(
                LOCK_NAMESPACE,
                LOCK_NAME,
                this::processCasesWithMissingJurCodeIds
        );

        if (!acquired) {
            log.info("JurCodesCollection ID fix task is already running; skipping this invocation");
        }
    }

    private void processCasesWithMissingJurCodeIds() {
        log.info("Checking for cases with missing jurCodesCollection IDs");

        String adminUserToken = adminUserService.getAdminUserToken();
        String[] caseTypeIds = caseTypeIdsString.split(",");

        Arrays.stream(caseTypeIds).forEach(rawCaseTypeId -> {
            String caseTypeId = rawCaseTypeId.strip();
            try {
                List<Long> caseReferences = findCasesWithMissingJurCodeIds(caseTypeId);
                log.info("Found {} cases with missing jurCodesCollection IDs for caseTypeId {}",
                        caseReferences.size(), caseTypeId);
                if (CollectionUtils.isEmpty(caseReferences)) {
                    return;
                }
                caseReferences.forEach(caseReference ->
                        triggerFixCaseEvent(adminUserToken, String.valueOf(caseReference), caseTypeId));
            } catch (Exception ex) {
                log.error(ex.getMessage());
            }
        });
    }

    private List<Long> findCasesWithMissingJurCodeIds(String caseTypeId) {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("caseTypeId", caseTypeId)
                .addValue("maxCases", maxCases);

        return jdbcTemplate.queryForList(FIND_CASES_MISSING_JUR_CODE_IDS, parameters, Long.class);
    }

    private void triggerFixCaseEvent(String adminUserToken, String caseId, String caseTypeId) {
        try {
            CCDRequest returnedRequest = ccdClient.startEventForCase(adminUserToken, caseTypeId,
                    EMPLOYMENT, caseId, FIX_CASE_API);

            CaseData caseData = returnedRequest.getCaseDetails().getCaseData();
            if (!hasMissingJurCodeIds(caseData)) {
                return;
            }
            JurisdictionCodeHelper.populateJurCodesCollectionIds(caseData);
            caseData.setStateAPI(null);

            String jurisdiction = returnedRequest.getCaseDetails().getJurisdiction();
            ccdClient.submitEventForCase(adminUserToken, caseData, caseTypeId,
                    jurisdiction, returnedRequest, caseId);

            log.info("Called {} for {}", FIX_CASE_API, caseId);
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }

    private boolean hasMissingJurCodeIds(CaseData caseData) {
        return caseData != null
                && CollectionUtils.isNotEmpty(caseData.getJurCodesCollection())
                && caseData.getJurCodesCollection().stream()
                        .filter(Objects::nonNull)
                        .anyMatch(item -> isNullOrEmpty(item.getId()));
    }
}
