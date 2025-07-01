package uk.gov.hmcts.ethos.replacement.docmosis.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Getter
public enum SolicitorRole {
    SOLICITORA("[SOLICITORA]", 0),
    SOLICITORB("[SOLICITORB]", 1),
    SOLICITORC("[SOLICITORC]", 2),
    SOLICITORD("[SOLICITORD]", 3),
    SOLICITORE("[SOLICITORE]", 4),
    SOLICITORF("[SOLICITORF]", 5),
    SOLICITORG("[SOLICITORG]", 6),
    SOLICITORH("[SOLICITORH]", 7),
    SOLICITORI("[SOLICITORI]", 8),
    SOLICITORJ("[SOLICITORJ]", 9);

    private final String caseRoleLabel;
    private final int index;

    public static final String POLICY_FIELD_TEMPLATE = "respondentOrganisationPolicy%d";
    public static final String NOC_ANSWERS_TEMPLATE = "noticeOfChangeAnswers%d";
    public static final String CASE_FIELD = "repCollection";

    public static Optional<SolicitorRole> from(String label) {
        return Arrays.stream(SolicitorRole.values())
            .filter(role -> role.caseRoleLabel.equals(label))
            .findFirst();
    }

    public Optional<RespondentSumTypeItem> getRepresentationItem(CaseData caseData) {
        List<RespondentSumTypeItem> parties = caseData.getRespondentCollection();
        if (this.index < parties.size()) {
            return Optional.of(parties.get(this.index));
        } else {
            return Optional.empty();
        }
    }
}
