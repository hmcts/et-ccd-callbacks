package uk.gov.hmcts.ethos.replacement.docmosis;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.rse.ccd.lib.Database;
import uk.gov.hmcts.rse.ccd.lib.test.CftlibTest;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RetainAndDisposeCftlibTest extends CftlibTest {

    private static final String CASE_TYPE = "ET_EnglandWales";
    private static final String CREATE_EVENT = "INITIATE_CASE_DRAFT";

    @Autowired
    private CoreCaseDataApi ccdApi;

    @Autowired
    private IdamClient idamClient;

    @Autowired
    private JdbcTemplate jdbc;

    @MockitoBean
    private VerifyTokenService verifyTokenService;

    private String idamToken;
    private String s2sToken;
    private String userId;

    @BeforeAll
    void authenticate() {
        when(verifyTokenService.verifyTokenSignature(anyString())).thenReturn(true);
        idamToken = idamClient.getAccessToken("admin@hmcts.net", "password");
        s2sToken = generateDummyS2SToken("et_cos");
        userId = idamClient.getUserInfo(idamToken).getUid();
    }

    @Test
    void disposesAnExpiredDraftOnlyAfterItIsMissingFromCcd() throws Exception {
        var startEvent = ccdApi.startForCaseworker(
            idamToken,
            s2sToken,
            userId,
            "EMPLOYMENT",
            CASE_TYPE,
            CREATE_EVENT
        );
        var content = CaseDataContent.builder()
            .data(Map.of())
            .event(Event.builder().id(CREATE_EVENT).build())
            .eventToken(startEvent.getToken())
            .build();
        CaseDetails createdCase = ccdApi.submitForCaseworker(
            idamToken,
            s2sToken,
            userId,
            "EMPLOYMENT",
            CASE_TYPE,
            false,
            content
        );
        long reference = createdCase.getId();

        jdbc.update(
            "update ccd.case_data set created_date = current_timestamp - interval '366 days' where reference = ?",
            reference
        );

        await()
            .atMost(Duration.ofSeconds(30))
            .pollInterval(Duration.ofSeconds(1))
            .untilAsserted(() -> {
                Map<String, Object> retainedCase = jdbc.queryForMap(
                    "select state, resolved_ttl::text as resolved_ttl from ccd.case_data where reference = ?",
                    reference
                );
                assertThat(retainedCase.get("state")).isEqualTo("PendingDisposal");
                assertThat(retainedCase.get("resolved_ttl")).isEqualTo(LocalDate.now().toString());
            });

        CaseDetails disposedCase = ccdApi.getCase(idamToken, s2sToken, Long.toString(reference));
        assertThat(disposedCase.getState()).isEqualTo("PendingDisposal");

        List<String> events = jdbc.queryForList(
            """
                select event_id
                from ccd.case_event
                where case_data_id = (select id from ccd.case_data where reference = ?)
                order by created_date
                """,
            String.class,
            reference
        );
        assertThat(events).containsSubsequence(CREATE_EVENT, "MarkForDisposal", "ConfirmDisposal");

        jdbc.update(
            "update ccd.case_data set resolved_ttl = current_date - 1 where reference = ?",
            reference
        );

        await()
            .pollDelay(Duration.ofSeconds(4))
            .atMost(Duration.ofSeconds(6))
            .untilAsserted(() -> assertThat(localCaseCount(reference)).isOne());

        try (var connection = cftlib().getConnection(Database.Datastore);
             var statement = connection.prepareStatement("delete from case_data where reference = ?")) {
            statement.setLong(1, reference);
            assertThat(statement.executeUpdate()).isOne();
        }

        await()
            .atMost(Duration.ofSeconds(30))
            .pollInterval(Duration.ofSeconds(1))
            .untilAsserted(() -> assertThat(localCaseCount(reference)).isZero());
    }

    private int localCaseCount(long reference) {
        return jdbc.queryForObject(
            "select count(*) from ccd.case_data where reference = ?",
            Integer.class,
            reference
        );
    }
}
