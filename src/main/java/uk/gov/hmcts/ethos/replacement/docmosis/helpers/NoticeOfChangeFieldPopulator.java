package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantIndType;
import uk.gov.hmcts.et.common.model.ccd.types.NoticeOfChangeAnswers;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationPolicy;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.SolicitorRole;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.NoticeOfChangeFieldPopulator.NoticeOfChangeAnswersPopulationStrategy.BLANK;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.NoticeOfChangeFieldPopulator.NoticeOfChangeAnswersPopulationStrategy.POPULATE;

@Component
@RequiredArgsConstructor
public class NoticeOfChangeFieldPopulator {
    private final RespondentPolicyConverter policyConverter;
    private final NoticeOfChangeAnswersConverter answersConverter;

    public Map<String, Object> generate(CaseData caseData) {
        return generate(caseData, POPULATE);
    }

    /**
     * Generates a map of all respondent organisation policy and notice of change elements that
     * need to be added to the case.
     * @param caseData case data
     * @param strategy determines whether to build blank notice of change answers or not
     * @return map of organisation policy and notice of change case elements
     */
    public Map<String, Object> generate(CaseData caseData,
                                        NoticeOfChangeAnswersPopulationStrategy strategy) {
        Map<String, Object> data = new ConcurrentHashMap<>();

        List<RespondentSumTypeItem> respondentType = caseData.getRespondentCollection();

        int numElements = respondentType.size();

        List<SolicitorRole> solicitorRoles = Arrays.asList(SolicitorRole.values());

        for (int i = 0; i < solicitorRoles.size(); i++) {
            SolicitorRole solicitorRole = solicitorRoles.get(i);

            Optional<RepresentedTypeRItem> solicitorContainer = i < numElements
                ? getRepItem(caseData.getRepCollection(), respondentType.get(i))
                : Optional.empty();

            Optional<RespondentSumTypeItem> respondent = i < numElements
                ? Optional.of(respondentType.get(i))
                : Optional.empty();

            OrganisationPolicy organisationPolicy = policyConverter.generate(
                solicitorRole, solicitorContainer
            );

            data.put(String.format(SolicitorRole.POLICY_FIELD_TEMPLATE, i), organisationPolicy);

            Optional<NoticeOfChangeAnswers> possibleAnswer = populateAnswer(
                strategy, respondent, caseData.getClaimantIndType()
            );

            if (possibleAnswer.isPresent()) {
                data.put(String.format(SolicitorRole.NOC_ANSWERS_TEMPLATE, i), possibleAnswer.get());
            }
        }

        return data;
    }

    private Optional<RepresentedTypeRItem> getRepItem(List<RepresentedTypeRItem> repCollection,
                                                      RespondentSumTypeItem respondent) {
        return emptyIfNull(repCollection).stream()
            .filter(representedTypeRItem ->
                    NocRespondentHelper.validateRespondent(respondent, representedTypeRItem.getValue()))
            .findFirst();
    }

    private Optional<NoticeOfChangeAnswers> populateAnswer(NoticeOfChangeAnswersPopulationStrategy strategy,
                                                           Optional<RespondentSumTypeItem> respondentRepresentative,
                                                           ClaimantIndType claimant) {
        if (BLANK == strategy) {
            return Optional.of(NoticeOfChangeAnswers.builder().build());
        }

        return respondentRepresentative.map(rep -> answersConverter.generateForSubmission(rep, claimant));
    }

    public enum NoticeOfChangeAnswersPopulationStrategy {
        POPULATE, BLANK
    }

}
