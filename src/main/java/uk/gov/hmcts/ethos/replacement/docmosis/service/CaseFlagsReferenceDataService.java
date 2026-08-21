package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.caseflags.CaseFlagReferenceDataDetail;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.caseflags.CaseFlagReferenceDataGroup;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.caseflags.CaseFlagReferenceDataResponse;
import uk.gov.hmcts.ethos.replacement.docmosis.rdprofessional.OrganisationClient;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.EXTERNAL;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.INTERNAL;

@Service
public class CaseFlagsReferenceDataService {
    private static final String PARTY_FLAG_TYPE = "PARTY";
    private static final String WELSH_REQUIRED = "Y";
    private static final String AVAILABLE_EXTERNAL_FLAG = "N";

    private final OrganisationClient organisationClient;
    private final AuthTokenGenerator authTokenGenerator;
    private final String hmctsServiceId;

    public CaseFlagsReferenceDataService(
            OrganisationClient organisationClient,
            AuthTokenGenerator authTokenGenerator,
            @Value("${hmcts_service_id:BHA1}") String hmctsServiceId) {
        this.organisationClient = organisationClient;
        this.authTokenGenerator = authTokenGenerator;
        this.hmctsServiceId = hmctsServiceId;
    }

    public Map<String, String> getPartyFlagVisibilities(String userToken) {
        CaseFlagReferenceDataResponse response = organisationClient.getCaseFlags(
                userToken,
                authTokenGenerator.generate(),
                hmctsServiceId,
                PARTY_FLAG_TYPE,
                WELSH_REQUIRED,
                AVAILABLE_EXTERNAL_FLAG
        );

        return visibilityByFlagCodeOrName(response);
    }

    Map<String, String> visibilityByFlagCodeOrName(CaseFlagReferenceDataResponse response) {
        Map<String, String> visibilityByKey = new HashMap<>();
        if (response == null || response.getFlags() == null) {
            return visibilityByKey;
        }

        response.getFlags().stream()
                .map(CaseFlagReferenceDataGroup::getFlagDetails)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .forEach(flag -> addFlagVisibility(visibilityByKey, flag));
        return visibilityByKey;
    }

    private static void addFlagVisibility(Map<String, String> visibilityByKey, CaseFlagReferenceDataDetail flag) {
        if (flag == null) {
            return;
        }

        String visibility = Boolean.TRUE.equals(flag.getExternallyAvailable()) ? EXTERNAL : INTERNAL;
        putVisibility(visibilityByKey, flag.getFlagCode(), visibility);
        putVisibility(visibilityByKey, flag.getNativeFlagCode(), visibility);
        putVisibility(visibilityByKey, flag.getName(), visibility);

        if (flag.getChildFlags() != null) {
            flag.getChildFlags().forEach(childFlag -> addFlagVisibility(visibilityByKey, childFlag));
        }
    }

    private static void putVisibility(Map<String, String> visibilityByKey, String key, String visibility) {
        String normalisedKey = normaliseCaseFlagReferenceKey(key);
        if (StringUtils.isBlank(normalisedKey)) {
            return;
        }

        visibilityByKey.merge(
                normalisedKey,
                visibility,
                (existing, candidate) -> Objects.equals(existing, candidate) ? existing : StringUtils.EMPTY
        );
    }

    public static String normaliseCaseFlagReferenceKey(String value) {
        return StringUtils.trimToEmpty(value).toLowerCase(Locale.ROOT);
    }
}
