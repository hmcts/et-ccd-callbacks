package uk.gov.hmcts.reform.et.syaapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.ccd.sdk.ActorAttribution;
import uk.gov.hmcts.ccd.sdk.SystemEventAction;
import uk.gov.hmcts.ccd.sdk.SystemEventExecutionContext;
import uk.gov.hmcts.ccd.sdk.SystemEventExecutionResult;
import uk.gov.hmcts.ccd.sdk.SystemEventExecutor;
import uk.gov.hmcts.ccd.sdk.SystemEventResult;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CitizenSystemEventServiceTest {

    private static final String AUTHORIZATION = "Bearer citizen";
    private static final String S2S_TOKEN = "s2s";
    private static final String CASE_ID = "1646225213651590";
    private static final long CASE_REFERENCE = 1_646_225_213_651_590L;
    private static final String CASE_TYPE = "ET_EnglandWales";

    @Mock
    private IdamClient idamClient;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private CoreCaseDataApi ccdApiClient;
    @Mock
    private SystemEventExecutor systemEventExecutor;

    private CitizenSystemEventService service;

    @BeforeEach
    void setUp() {
        service = new CitizenSystemEventService(idamClient, authTokenGenerator, ccdApiClient, systemEventExecutor);
    }

    @Test
    void readsTheCaseAsTheCitizen() {
        UserInfo citizen = citizen("Jane", "Doe");
        CaseDetails caseDetails = caseDetails(CASE_TYPE);
        when(idamClient.getUserInfo(AUTHORIZATION)).thenReturn(citizen);
        when(authTokenGenerator.generate()).thenReturn(S2S_TOKEN);
        when(ccdApiClient.getCase(AUTHORIZATION, S2S_TOKEN, CASE_ID)).thenReturn(caseDetails);

        CitizenSystemEventService.CitizenCase result = service.readCase(AUTHORIZATION, CASE_ID, CASE_TYPE);

        assertThat(result.user()).isSameAs(citizen);
        assertThat(result.caseDetails()).isSameAs(caseDetails);
    }

    @Test
    void rejectsAUserWithoutTheCitizenRole() {
        when(idamClient.getUserInfo(AUTHORIZATION)).thenReturn(
            UserInfo.builder().uid("caseworker").roles(List.of("caseworker-employment")).build());

        assertThatThrownBy(() -> service.readCase(AUTHORIZATION, CASE_ID, CASE_TYPE))
            .isInstanceOfSatisfying(ResponseStatusException.class,
                e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN));
        verifyNoInteractions(ccdApiClient);
    }

    @Test
    void rejectsACaseOfAnotherCaseType() {
        when(idamClient.getUserInfo(AUTHORIZATION)).thenReturn(citizen("Jane", "Doe"));
        when(authTokenGenerator.generate()).thenReturn(S2S_TOKEN);
        when(ccdApiClient.getCase(AUTHORIZATION, S2S_TOKEN, CASE_ID)).thenReturn(caseDetails("ET_Scotland"));

        assertThatThrownBy(() -> service.readCase(AUTHORIZATION, CASE_ID, CASE_TYPE))
            .isInstanceOfSatisfying(ResponseStatusException.class,
                e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void recordsTheWriteAsASystemEventAttributedToTheCitizen() {
        List<Long> written = new ArrayList<>();
        when(systemEventExecutor.execute(anyLong(), any(ActorAttribution.class), any(UUID.class), any()))
            .thenAnswer(invocation -> {
                SystemEventAction action = invocation.getArgument(3);
                SystemEventResult result = action.execute(new SystemEventExecutionContext(
                    invocation.getArgument(0), invocation.getArgument(2), CASE_TYPE, "Accepted"));
                assertThat(result).isEqualTo(SystemEventResult.withoutStateTransition("EVENT", "Event name"));
                return new SystemEventExecutionResult(1L, SystemEventExecutionResult.Outcome.EXECUTED);
            });

        service.record(citizenCase("Jane", "Doe"), "EVENT", "Event name", written::add);

        assertThat(written).containsExactly(CASE_REFERENCE);
        verify(systemEventExecutor).execute(eq(CASE_REFERENCE), eq(new ActorAttribution("citizen-id", "Jane", "Doe")),
            any(UUID.class), any());
    }

    @Test
    void usesANewIdempotencyKeyForEachRequest() {
        when(systemEventExecutor.execute(anyLong(), any(ActorAttribution.class), any(UUID.class), any()))
            .thenReturn(new SystemEventExecutionResult(1L, SystemEventExecutionResult.Outcome.EXECUTED));

        service.record(citizenCase("Jane", "Doe"), "EVENT", "Event name", reference -> { });
        service.record(citizenCase("Jane", "Doe"), "EVENT", "Event name", reference -> { });

        ArgumentCaptor<UUID> keys = ArgumentCaptor.forClass(UUID.class);
        verify(systemEventExecutor, times(2))
            .execute(anyLong(), any(ActorAttribution.class), keys.capture(), any());
        assertThat(keys.getAllValues()).doesNotHaveDuplicates();
    }

    @Test
    void fallsBackToTheIdamSubjectForMissingNames() {
        when(systemEventExecutor.execute(anyLong(), any(ActorAttribution.class), any(UUID.class), any()))
            .thenReturn(new SystemEventExecutionResult(1L, SystemEventExecutionResult.Outcome.EXECUTED));

        service.record(citizenCase(null, " "), "EVENT", "Event name", reference -> { });

        verify(systemEventExecutor).execute(eq(CASE_REFERENCE),
            eq(new ActorAttribution("citizen-id", "jane@example.com", "jane@example.com")), any(UUID.class), any());
    }

    private static CitizenSystemEventService.CitizenCase citizenCase(String givenName, String familyName) {
        return new CitizenSystemEventService.CitizenCase(citizen(givenName, familyName), caseDetails(CASE_TYPE));
    }

    private static UserInfo citizen(String givenName, String familyName) {
        return UserInfo.builder()
            .uid("citizen-id")
            .sub("jane@example.com")
            .givenName(givenName)
            .familyName(familyName)
            .roles(List.of("citizen"))
            .build();
    }

    private static CaseDetails caseDetails(String caseTypeId) {
        return CaseDetails.builder().id(CASE_REFERENCE).caseTypeId(caseTypeId).build();
    }
}
