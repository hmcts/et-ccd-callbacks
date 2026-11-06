package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.ccd.sdk.ActorAttribution;
import uk.gov.hmcts.ccd.sdk.SystemEventExecutionResult;
import uk.gov.hmcts.ccd.sdk.SystemEventExecutor;
import uk.gov.hmcts.ccd.sdk.SystemEventResult;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.UUID;
import java.util.function.LongConsumer;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

/**
 * Records a citizen's change to data et-cos holds outside the case blob, such as hub link statuses or notification
 * views, without a CCD start/submit round trip.
 *
 * <p>The case is read through CCD as the citizen, so CCD still decides whether they can access it. The write then
 * runs in-process as a system event that names the citizen in the case history.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CitizenSystemEventService {

    static final String CITIZEN_ROLE = "citizen";

    private final IdamClient idamClient;
    private final AuthTokenGenerator authTokenGenerator;
    private final CoreCaseDataApi ccdApiClient;
    private final SystemEventExecutor systemEventExecutor;

    /**
     * Reads a case as a citizen, failing unless they hold the citizen role and CCD lets them read the case.
     *
     * @param authorization the citizen's bearer token
     * @param caseId        the case reference
     * @param caseTypeId    the case type the caller expects the case to have
     * @return the citizen together with the case as CCD returned it to them
     */
    public CitizenCase readCase(String authorization, String caseId, String caseTypeId) {
        UserInfo user = idamClient.getUserInfo(authorization);
        if (user.getRoles() == null || !user.getRoles().contains(CITIZEN_ROLE)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only citizens can make this change");
        }
        CaseDetails caseDetails = ccdApiClient.getCase(authorization, authTokenGenerator.generate(), caseId);
        if (caseDetails == null || !caseDetails.getCaseTypeId().equals(caseTypeId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Case not found for the given case type");
        }
        return new CitizenCase(user, caseDetails);
    }

    /**
     * Runs a write to et-cos tables as a system event attributed to the citizen.
     *
     * <p>The write runs in the event's transaction, which holds the lock on the case. Each call is a new event: the
     * callers' writes are idempotent, so a client retry only adds another history entry.
     *
     * @param citizenCase the citizen and case from {@link #readCase}
     * @param eventId     the event id recorded in the case history
     * @param eventName   the event name shown in the case history
     * @param write       the write, given the case reference
     */
    public void record(CitizenCase citizenCase, String eventId, String eventName, LongConsumer write) {
        UserInfo user = citizenCase.user();
        ActorAttribution actor = new ActorAttribution(
            user.getUid(),
            defaultIfBlank(user.getGivenName(), user.getSub()),
            defaultIfBlank(user.getFamilyName(), user.getSub())
        );
        long caseReference = citizenCase.caseDetails().getId();
        SystemEventExecutionResult result = systemEventExecutor.execute(
            caseReference,
            actor,
            UUID.randomUUID(),
            context -> {
                write.accept(context.caseReference());
                return SystemEventResult.withoutStateTransition(eventId, eventName);
            }
        );
        log.info("Recorded {} as event {} for case {}", eventId, result.eventId(), caseReference);
    }

    /**
     * A citizen and the case CCD returned to them.
     */
    public record CitizenCase(UserInfo user, CaseDetails caseDetails) {
    }
}
