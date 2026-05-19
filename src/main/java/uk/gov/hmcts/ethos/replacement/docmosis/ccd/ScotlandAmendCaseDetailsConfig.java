package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import org.springframework.stereotype.Component;

import java.util.List;

import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.AmendCaseDetailsConfig.FieldKind.COMPLEX;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.AmendCaseDetailsConfig.FieldKind.MANDATORY;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.AmendCaseDetailsConfig.FieldKind.OPTIONAL;

@Component
public class ScotlandAmendCaseDetailsConfig extends AmendCaseDetailsConfig<ScotlandCaseData> {

    public ScotlandAmendCaseDetailsConfig() {
        super(
            EtUserRole.CASEWORKER_EMPLOYMENT_SCOTLAND,
            EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND,
            true,
            true,
            true,
            amendCaseDetailsFields(),
            amendCaseDetailsClosedFields()
        );
    }

    private static List<FieldSpec> amendCaseDetailsFields() {
        List<FieldSpec> fileLocations = scotlandFileLocations(2, 2, 8);
        List<FieldSpec> multipleFields = multipleFields(2, 2, 13, "multipleFlag=\"No\"");
        return List.of(
            ttl(3, 23, 3),
            field("managingOffice", MANDATORY, 1, 1, 1).callback("${ET_COS_URL}/initialiseAmendCaseDetails"),
            field("allocatedOffice", MANDATORY, 1, 1, 2),
            receiptDate(2, 2),
            field("preAcceptCase", COMPLEX, 2, 2, 2),
            field("claimServedDate", OPTIONAL, 1, 1, 3),
            field("et3DueDate", OPTIONAL, 1, 1, 4),
            field("feeGroupReference", MANDATORY, 2, 2, 5),
            field("clerkResponsible", OPTIONAL, 2, 2, 6),
            field("positionType", MANDATORY, 2, 2, 7),
            fileLocations.get(0),
            fileLocations.get(1).withPageFieldDisplayOrder(9),
            fileLocations.get(2).withPageFieldDisplayOrder(10),
            fileLocations.get(3).withPageFieldDisplayOrder(11),
            field("conciliationTrack", MANDATORY, 2, 2, 12),
            multipleFields.get(0),
            multipleFields.get(1),
            multipleFields.get(2),
            multipleFields.get(3),
            multipleFields.get(4),
            field("caseNotes", OPTIONAL, 2, 2, 18),
            field("additionalCaseInfo", OPTIONAL, 3, 3, 1),
            field("suggestedHearingVenues", OPTIONAL, 2, 2, 18).noPageColumn()
        );
    }

    private static List<FieldSpec> amendCaseDetailsClosedFields() {
        List<FieldSpec> fileLocations = scotlandFileLocations(1, 1, 7);
        List<FieldSpec> multipleFields = multipleFields(1, 1, 9, "feeGroupReference=\"dummy\"");
        return List.of(
            ttl(2, 2, 3),
            receiptDate(1, 1),
            field("preAcceptCase", COMPLEX, 1, 1, 2),
            field("feeGroupReference", MANDATORY, 1, 1, 3),
            field("managingOffice", MANDATORY, 1, 1, 4),
            field("allocatedOffice", MANDATORY, 1, 1, 5),
            field("clerkResponsible", OPTIONAL, 1, 1, 6),
            fileLocations.get(0),
            fileLocations.get(1),
            fileLocations.get(2),
            fileLocations.get(3),
            field("conciliationTrack", MANDATORY, 1, 1, 8),
            multipleFields.get(0),
            multipleFields.get(1),
            multipleFields.get(2),
            multipleFields.get(3),
            multipleFields.get(4),
            field("caseNotes", OPTIONAL, 1, 1, 14),
            field("additionalCaseInfo", OPTIONAL, 2, 2, 1)
        );
    }
}
