package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import org.springframework.stereotype.Component;

import java.util.List;

import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.AmendCaseDetailsConfig.FieldKind.COMPLEX;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.AmendCaseDetailsConfig.FieldKind.MANDATORY;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.AmendCaseDetailsConfig.FieldKind.OPTIONAL;

@Component
public class EnglandWalesAmendCaseDetailsConfig extends AmendCaseDetailsConfig<EnglandWalesCaseData> {

    public EnglandWalesAmendCaseDetailsConfig() {
        super(
            EtUserRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES,
            EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES,
            false,
            false,
            false,
            amendCaseDetailsFields(),
            amendCaseDetailsClosedFields()
        );
    }

    private static List<FieldSpec> amendCaseDetailsFields() {
        List<FieldSpec> multipleFields = multipleFields(1, 1, 10, "multipleFlag=\"No\"");
        return List.of(
            ttl(2, 2, 3),
            receiptDate(1, 1),
            field("preAcceptCase", COMPLEX, 1, 1, 2),
            field("claimServedDate", OPTIONAL, 1, 1, 3),
            field("et3DueDate", OPTIONAL, 1, 1, 4).show("claimServedDate!=\"\""),
            field("feeGroupReference", MANDATORY, 1, 1, 5),
            field("clerkResponsible", OPTIONAL, 1, 1, 6),
            field("positionType", MANDATORY, 1, 1, 7),
            field("fileLocation", OPTIONAL, 1, 1, 8),
            field("conciliationTrack", MANDATORY, 1, 1, 9),
            multipleFields.get(0),
            multipleFields.get(1),
            multipleFields.get(2),
            multipleFields.get(3),
            multipleFields.get(4),
            field("caseNotes", OPTIONAL, 2, 2, 1),
            field("additionalCaseInfo", OPTIONAL, 2, 2, 2),
            field("suggestedHearingVenues", OPTIONAL, 1, 1, 14).noPageColumn()
        );
    }

    private static List<FieldSpec> amendCaseDetailsClosedFields() {
        List<FieldSpec> multipleFields = multipleFields(1, 1, 7, "multipleFlag=\"No\"");
        return List.of(
            ttl(2, 2, 3),
            receiptDate(1, 1),
            field("preAcceptCase", COMPLEX, 1, 1, 2),
            field("feeGroupReference", MANDATORY, 1, 1, 3),
            field("clerkResponsible", OPTIONAL, 1, 1, 4),
            field("fileLocation", OPTIONAL, 1, 1, 5),
            field("conciliationTrack", MANDATORY, 1, 1, 6),
            multipleFields.get(0),
            multipleFields.get(1),
            multipleFields.get(2),
            multipleFields.get(3),
            multipleFields.get(4),
            field("caseNotes", OPTIONAL, 2, 2, 1),
            field("additionalCaseInfo", OPTIONAL, 2, 2, 2)
        );
    }
}
