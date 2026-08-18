package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.caseflags.CaseFlagReferenceDataDetail;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.caseflags.CaseFlagReferenceDataGroup;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.caseflags.CaseFlagReferenceDataResponse;
import uk.gov.hmcts.ethos.replacement.docmosis.rdprofessional.OrganisationClient;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.EXTERNAL;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.INTERNAL;

class CaseFlagsReferenceDataServiceTest {
    private static final String USER_TOKEN = "user-token";
    private static final String SERVICE_TOKEN = "service-token";
    private static final String HMCTS_SERVICE_ID = "BHA1";

    @Test
    void getPartyFlagVisibilities_shouldFlattenReferenceDataByCodeNativeCodeAndName() {
        OrganisationClient client = mock(OrganisationClient.class);
        AuthTokenGenerator authTokenGenerator = mock(AuthTokenGenerator.class);
        CaseFlagsReferenceDataService service =
                new CaseFlagsReferenceDataService(client, authTokenGenerator, HMCTS_SERVICE_ID);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        when(client.getCaseFlags(USER_TOKEN, SERVICE_TOKEN, HMCTS_SERVICE_ID, "PARTY", "Y", "N"))
                .thenReturn(new CaseFlagReferenceDataResponse(List.of(new CaseFlagReferenceDataGroup(List.of(
                        flag("Reasonable adjustment", false, "CATGRY", "RA0001", List.of(
                                flag("Documents in a specified colour", true, "RA0010", "RA0010", List.of()),
                                flag("Guidance on how to complete forms", false, "RA0017", "RA0017", List.of())
                        ))
                )))));

        Map<String, String> actual = service.getPartyFlagVisibilities(USER_TOKEN);

        assertEquals(INTERNAL, actual.get("ra0001"));
        assertEquals(EXTERNAL, actual.get("ra0010"));
        assertEquals(EXTERNAL, actual.get("documents in a specified colour"));
        assertEquals(INTERNAL, actual.get("ra0017"));
        assertEquals(INTERNAL, actual.get("guidance on how to complete forms"));
        verify(client).getCaseFlags(USER_TOKEN, SERVICE_TOKEN, HMCTS_SERVICE_ID, "PARTY", "Y", "N");
    }

    @Test
    void getPartyFlagVisibilities_shouldIgnoreAmbiguousNames() {
        OrganisationClient client = mock(OrganisationClient.class);
        AuthTokenGenerator authTokenGenerator = mock(AuthTokenGenerator.class);
        CaseFlagsReferenceDataService service =
                new CaseFlagsReferenceDataService(client, authTokenGenerator, HMCTS_SERVICE_ID);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        when(client.getCaseFlags(USER_TOKEN, SERVICE_TOKEN, HMCTS_SERVICE_ID, "PARTY", "Y", "N"))
                .thenReturn(new CaseFlagReferenceDataResponse(List.of(new CaseFlagReferenceDataGroup(List.of(
                        flag("Other", true, "OT0001", "OT0001", List.of()),
                        flag("Other", false, "OT0002", "OT0002", List.of())
                )))));

        Map<String, String> actual = service.getPartyFlagVisibilities(USER_TOKEN);

        assertEquals("", actual.get("other"));
        assertEquals(EXTERNAL, actual.get("ot0001"));
        assertEquals(INTERNAL, actual.get("ot0002"));
    }

    private static CaseFlagReferenceDataDetail flag(
            String name,
            boolean externallyAvailable,
            String flagCode,
            String nativeFlagCode,
            List<CaseFlagReferenceDataDetail> childFlags) {
        return new CaseFlagReferenceDataDetail(name, externallyAvailable, flagCode, nativeFlagCode, childFlags);
    }
}
