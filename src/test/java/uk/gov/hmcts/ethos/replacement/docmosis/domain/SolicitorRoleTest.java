package uk.gov.hmcts.ethos.replacement.docmosis.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import java.util.ArrayList;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.assertThat;

class SolicitorRoleTest {
    private static final String RESPONDENT_NAME = "Harry Johnson";
    private static final String RESPONDENT_NAME_TWO = "Jane Green";
    private static final String RESPONDENT_NAME_THREE = "Bad Company Inc";
    private static final String RESPONDENT_REF = "7277";
    private static final String RESPONDENT_REF_TWO = "6887";
    private static final String RESPONDENT_REF_THREE = "9292";
    private static final String RESPONDENT_EMAIL = "h.johnson@corp.co.uk";
    private static final String RESPONDENT_EMAIL_TWO = "j.green@corp.co.uk";
    private static final String RESPONDENT_EMAIL_THREE = "info@corp.co.uk";
    private static final String RESPONDENT_REP_ID = "1111-2222-3333-1111";
    private static final String RESPONDENT_REP_ID_TWO = "1111-2222-3333-1112";
    private static final String RESPONDENT_REP_ID_THREE = "1111-2222-3333-1113";
    private static final String RESPONDENT_REP_NAME = "Legal One";
    private static final String RESPONDENT_REP_NAME_TWO = "Legal Two";
    private static final String RESPONDENT_REP_NAME_THREE = "Legal Three";

    private CaseData caseData;

    @BeforeEach
    void setUp() {

        caseData = new CaseData();

        // Respondent
        caseData.setRespondentCollection(new ArrayList<>());

        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(RespondentSumType.builder().respondentName(RESPONDENT_NAME)
            .respondentEmail(RESPONDENT_EMAIL)
            .responseReference(RESPONDENT_REF)
            .build());
        caseData.getRespondentCollection().add(respondentSumTypeItem);

        respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(RespondentSumType.builder().respondentName(RESPONDENT_NAME_TWO)
            .respondentEmail(RESPONDENT_EMAIL_TWO)
            .responseReference(RESPONDENT_REF_TWO)
            .build());
        caseData.getRespondentCollection().add(respondentSumTypeItem);

        respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(RespondentSumType.builder().respondentName(RESPONDENT_NAME_THREE)
            .respondentEmail(RESPONDENT_REF_THREE)
            .responseReference(RESPONDENT_EMAIL_THREE)
            .build());
        caseData.getRespondentCollection().add(respondentSumTypeItem);

        // Respondent Representative
        caseData.setRepCollection(new ArrayList<>());
        RepresentedTypeR representedType =
            RepresentedTypeR.builder()
                .nameOfRepresentative(RESPONDENT_REP_NAME)
                .respRepName(RESPONDENT_NAME)
                .build();
        RepresentedTypeRItem representedTypeRItem = new RepresentedTypeRItem();
        representedTypeRItem.setId(RESPONDENT_REP_ID);
        representedTypeRItem.setValue(representedType);
        caseData.getRepCollection().add(representedTypeRItem);

        representedType =
            RepresentedTypeR.builder()
                .nameOfRepresentative(RESPONDENT_REP_NAME_TWO)
                .respRepName(RESPONDENT_NAME_TWO)
                .build();
        representedTypeRItem = new RepresentedTypeRItem();
        representedTypeRItem.setId(RESPONDENT_REP_ID_TWO);
        representedTypeRItem.setValue(representedType);
        caseData.getRepCollection().add(representedTypeRItem);

        representedType =
            RepresentedTypeR.builder()
                .nameOfRepresentative(RESPONDENT_REP_NAME_THREE)
                .respRepName(RESPONDENT_NAME_THREE)
                .build();
        representedTypeRItem = new RepresentedTypeRItem();
        representedTypeRItem.setId(RESPONDENT_REP_ID_THREE);
        representedTypeRItem.setValue(representedType);
        caseData.getRepCollection().add(representedTypeRItem);
    }

    @ParameterizedTest
    @MethodSource("typeToEnumSource")
    void shouldConvertTypeStringToSolicitorRole(String label, SolicitorRole expectedSolicitorRole) {
        assertThat(SolicitorRole.from(label)).isPresent().hasValue(expectedSolicitorRole);
    }

    private static Stream<Arguments> typeToEnumSource() {
        return Stream.of(
            Arguments.of("[SOLICITORA]", SolicitorRole.SOLICITORA),
            Arguments.of("[SOLICITORB]", SolicitorRole.SOLICITORB),
            Arguments.of("[SOLICITORC]", SolicitorRole.SOLICITORC),
            Arguments.of("[SOLICITORD]", SolicitorRole.SOLICITORD),
            Arguments.of("[SOLICITORE]", SolicitorRole.SOLICITORE),
            Arguments.of("[SOLICITORF]", SolicitorRole.SOLICITORF),
            Arguments.of("[SOLICITORG]", SolicitorRole.SOLICITORG),
            Arguments.of("[SOLICITORH]", SolicitorRole.SOLICITORH),
            Arguments.of("[SOLICITORI]", SolicitorRole.SOLICITORI),
            Arguments.of("[SOLICITORJ]", SolicitorRole.SOLICITORJ)
        );
    }

    @Test
    void shouldReturnTheCorrectRespondentRepresentativeItem() {
        assertThat(SolicitorRole.from("[SOLICITORA]")
            .flatMap(solicitorRole -> solicitorRole.getRepresentationItem(caseData))
            .map(representedTypeRItem -> representedTypeRItem.getValue().getRespondentName()))
            .isPresent().hasValue(RESPONDENT_NAME);

        assertThat(SolicitorRole.from("[SOLICITORB]")
            .flatMap(solicitorRole -> solicitorRole.getRepresentationItem(caseData))
            .map(representedTypeRItem -> representedTypeRItem.getValue().getRespondentName()))
            .isPresent().hasValue(
                RESPONDENT_NAME_TWO);

        assertThat(SolicitorRole.from("[SOLICITORC]")
            .flatMap(solicitorRole -> solicitorRole.getRepresentationItem(caseData))
            .map(representedTypeRItem -> representedTypeRItem.getValue().getRespondentName()))
            .isPresent().hasValue(
                RESPONDENT_NAME_THREE);
    }
}