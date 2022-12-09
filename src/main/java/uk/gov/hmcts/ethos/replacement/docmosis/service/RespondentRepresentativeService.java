package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.AuditEvent;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ChangeOrganisationRequest;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.SolicitorRole;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NoticeOfChangeFieldPopulator;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;

@Service
@RequiredArgsConstructor
@Slf4j
public class RespondentRepresentativeService {
    public static final String BEARER = "Bearer";
    public static final String NOC_REQUEST = "nocRequest";
    private final NoticeOfChangeFieldPopulator noticeOfChangeFieldPopulator;

    private final UserService userService;

    private final CaseConverter caseConverter;

    private final AuditEventService auditEventService;

    @Value("${aac.system.username}")
    private String systemUserName;

    @Value("${aac.system.password}")
    private String systemUserPassword;

    /**
     * Add respondent organisation policy and notice of change answer fields to the case data.
     * @param caseData case data
     * @return modified case data
     */
    public CaseData prepopulateOrgPolicyAndNoc(CaseData caseData) {
        Map<String, Object> caseDataAsMap = caseConverter.toMap(caseData);
        Map<String, Object> generatedContent =
            noticeOfChangeFieldPopulator.generate(caseData);
        caseDataAsMap.putAll(generatedContent);
        return  caseConverter.convert(caseDataAsMap, CaseData.class);
    }

    /**
     * Replace the organisation policy and relevant respondent representative mapping with
     * new respondent representative details.
     * @param caseDetails containing case data with change organisation request field
     * @return updated case
     */
    public CaseData updateRepresentation(CaseDetails caseDetails) throws IOException {
        CaseData caseData = caseDetails.getCaseData();
        Map<String, Object> caseDataAsMap = caseConverter.toMap(caseData);
        Map<String, Object> repCollection = updateRepresentationMap(caseData, caseDetails.getCaseId());
        caseDataAsMap.putAll(repCollection);
        return  caseConverter.convert(caseDataAsMap, CaseData.class);
    }

    private Map<String, Object> updateRepresentationMap(CaseData caseData, String caseId) throws IOException {

        final ChangeOrganisationRequest change = caseData.getChangeOrganisationRequestField();

        if (isEmpty(change) || isEmpty(change.getCaseRoleId()) || isEmpty(change.getOrganisationToAdd())) {
            throw new IllegalStateException("Invalid or missing ChangeOrganisationRequest: " + change);
        }

        String accessToken = String.join(" ", BEARER, userService.getAccessToken(systemUserName, systemUserPassword));

        Optional<AuditEvent> auditEvent =
            auditEventService.getLatestAuditEventByName(accessToken, caseId, NOC_REQUEST);

        Optional<UserDetails> userDetails = auditEvent
            .map(event -> userService.getUserDetailsById(accessToken, event.getUserId()));

        final SolicitorRole role = SolicitorRole.from(change.getCaseRoleId().getSelectedCode()).orElseThrow();

        RepresentedTypeR container = caseData.getRepCollection().get(role.getIndex()).getValue();

        RepresentedTypeR addedSolicitor = RepresentedTypeR.builder()
            .nameOfRepresentative(userDetails
                .map(user -> String.join(" ", user.getFirstName(), user.getLastName()))
                .orElse(null))
            .representativeEmailAddress(userDetails.map(UserDetails::getEmail).orElse(null))
            .respondentOrganisation(change.getOrganisationToAdd())
            .respRepName(container.getRespRepName())
            .myHmctsYesNo("Yes")
            .build();

        caseData.getRepCollection().get(role.getIndex()).setValue(addedSolicitor);

        return Map.of(SolicitorRole.CASE_FIELD, caseData.getRepCollection());

    }

    public RespondentSumType getRespondent(String respName, CaseData caseData) {
        return caseData.getRespondentCollection().stream()
            .filter(respondent -> respondent.getValue().getRespondentName().equals(respName))
            .findFirst().map(RespondentSumTypeItem::getValue)
            .orElse(new RespondentSumType());
    }
}
