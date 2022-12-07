package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.AuditEvent;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
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
public class RespondentRepresentativeService {
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
     * @param caseData case data
     * @return updated case
     */
    public CaseData updateRepresentation(CaseData caseData) throws IOException {
        Map<String, Object> caseDataAsMap = caseConverter.toMap(caseData);
        Map<String, Object> repCollection = updateRepresentationMap(caseData);
        caseDataAsMap.putAll(repCollection);
        return  caseConverter.convert(caseDataAsMap, CaseData.class);
    }

    private Map<String, Object> updateRepresentationMap(CaseData caseData) throws IOException {

        String accessToken = "";
            //idamApi.getAccessToken(systemUserName, systemUserPassword);

        Optional<AuditEvent>  auditEvent =
                auditEventService.getLatestAuditEventByName(accessToken, caseData.getCcdID(), "nocRequest");

        Optional<UserDetails> userDetails = auditEvent
            .map(event -> userService.getUserDetailsById(accessToken, event.getUserId()));

        final ChangeOrganisationRequest change = caseData.getChangeOrganisationRequestField();

        if (isEmpty(change) || isEmpty(change.getCaseRoleId()) || isEmpty(change.getOrganisationToAdd())) {
            throw new IllegalStateException("Invalid or missing ChangeOrganisationRequest: " + change);
        }

        final SolicitorRole role = SolicitorRole.from(change.getCaseRoleId().getSelectedCode()).orElseThrow();

        RepresentedTypeR container = caseData.getRepCollection().get(role.getIndex()).getValue();

        String userName = null;
        String userEmail = null;

        if (userDetails.isPresent()) {
            userName = String.join(" ", userDetails.get().getFirstName(), userDetails.get().getLastName());
            userEmail = userDetails.get().getEmail();
        }

        RepresentedTypeR addedSolicitor = RepresentedTypeR.builder()
            .nameOfRepresentative(userName)
            .representativeEmailAddress(userEmail)
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
