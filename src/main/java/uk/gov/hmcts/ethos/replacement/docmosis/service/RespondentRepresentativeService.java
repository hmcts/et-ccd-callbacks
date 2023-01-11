package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.AuditEvent;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ChangeOrganisationRequest;
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.SolicitorRole;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NoticeOfChangeFieldPopulator;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.et.common.model.ccd.types.ChangeOrganisationApprovalStatus.APPROVED;

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

    @Value("${etcos.system.username}")
    private String systemUserName;

    @Value("${etcos.system.password}")
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

    /**
     * Gets the case data before and after and checks respondent org policies for differences.
     * For each difference creates a change organisation request to remove old organisation and add new.
     * For each change request trigger the updateRepresentation event against CCD
     * @param caseId case id of case being modified
     * @param caseData case date before the update event occurred
     * @param caseDataBefore case data after the update event occurred
     */
    public void updateRepresentativesAccess(String caseId, CaseData caseData, CaseData caseDataBefore) {

        List<ChangeOrganisationRequest> changeRequests = getRepresentationChanges(caseData.getRepCollection(),
            caseDataBefore.getRepCollection()) ;

        log.info("{} representation changes detected", changeRequests.size());

        for (ChangeOrganisationRequest changeRequest : changeRequests) {
            log.info("About to apply representation change {}", changeRequest);

//            coreCaseDataService.triggerEvent(caseId, "updateRepresentation",
//                Map.of("changeOrganisationRequestField", changeRequest));

            log.info("Representation change applied {}", changeRequest);
        }

    }
    public List<ChangeOrganisationRequest> getRepresentationChanges(List<RepresentedTypeRItem>  after,
                                                                    List<RepresentedTypeRItem>  before) {

        final List<RepresentedTypeRItem> newRespondents = defaultIfNull(after, new ArrayList<>());
        final List<RepresentedTypeRItem> oldRespondents = defaultIfNull(before, new ArrayList<>());

        final Map<UUID, Organisation> newRespondentsOrganisations = organisationByRespondentId(newRespondents);
        final Map<UUID, Organisation> oldRespondentsOrganisations = organisationByRespondentId(oldRespondents);

        final List<ChangeOrganisationRequest> changeRequests = new ArrayList<>();

        for (int i = 0; i < newRespondents.size(); i++) {

            SolicitorRole solicitorRole = Arrays.asList(SolicitorRole.values()).get(i);
            UUID respondentId = newRespondents.get(i).getValue().getId();

            Organisation newOrganisation = newRespondentsOrganisations.get(respondentId);
            Organisation oldOrganisation = oldRespondentsOrganisations.get(respondentId);

            if (!Objects.equals(newOrganisation, oldOrganisation)) {
                changeRequests.add(changeRequest(newOrganisation, oldOrganisation, solicitorRole));
            }
        }

        return changeRequests;
    }

    private Map<UUID, Organisation> organisationByRespondentId(List<RepresentedTypeRItem> respondents) {
        return respondents.stream().collect(
            HashMap::new,
            (container, respondent) -> container.put(respondent.getValue().getId(), getOrganisation(respondent)),
            HashMap::putAll
        );
    }

    private Organisation getOrganisation(RepresentedTypeRItem respondent) {
        return Optional.ofNullable(respondent)
            .map(respItem -> respItem.getValue().getRespondentOrganisation())
            .filter(org -> isNotEmpty(org.getOrganisationID()))
            .orElse(null);
    }

    private ChangeOrganisationRequest changeRequest(Organisation newOrganisation,
                                                    Organisation oldOrganisation,
                                                    SolicitorRole solicitorRole) {

        DynamicFixedListType roleItem = new DynamicFixedListType();
        DynamicValueType dynamicValueType = new DynamicValueType();
        dynamicValueType.setCode(solicitorRole.getCaseRoleLabel());
        dynamicValueType.setLabel(solicitorRole.getCaseRoleLabel());
        roleItem.setValue(dynamicValueType);


        return ChangeOrganisationRequest.builder()
            .approvalStatus(APPROVED)
            .requestTimestamp(LocalDateTime.now())
            .caseRoleId(roleItem)
            .organisationToRemove(oldOrganisation)
            .organisationToAdd(newOrganisation)
            .build();
    }

    public RespondentSumType getRespondent(String respName, CaseData caseData) {
        return caseData.getRespondentCollection().stream()
            .filter(respondent -> respondent.getValue().getRespondentName().equals(respName))
            .findFirst().map(RespondentSumTypeItem::getValue)
            .orElse(new RespondentSumType());
    }
}
