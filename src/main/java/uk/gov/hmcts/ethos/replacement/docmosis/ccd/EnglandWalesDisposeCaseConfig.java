package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.Event.EventBuilder;
import uk.gov.hmcts.ccd.sdk.api.FieldCollection.FieldCollectionBuilder;

@Component
public class EnglandWalesDisposeCaseConfig extends DisposeCaseConfig<EnglandWalesCaseData> {

    public EnglandWalesDisposeCaseConfig() {
        super(
            EtUserRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES,
            EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES,
            40,
            "Yes",
            "${ET_COS_URL}/aboutToStartDisposal"
        );
    }

    @Override
    protected void addFields(
        FieldCollectionBuilder<EnglandWalesCaseData, EtState,
            EventBuilder<EnglandWalesCaseData, EtUserRole, EtState>> fields
    ) {
        addField(fields, "positionType", FieldKind.READ_ONLY, 1, 1, 1, null, null);
        addField(fields, "clerkResponsible", FieldKind.MANDATORY, 1, 1, 2, null, null);
        addField(fields, "fileLocation", FieldKind.OPTIONAL, 1, 1, 3, null, null);
        addField(fields, "caseNotes", FieldKind.OPTIONAL, 1, 1, 4, null, null);
        addField(fields, "currentPosition", FieldKind.READ_ONLY, 1, 1, 5, "positionType =\"dummy\"", null);
        addField(fields, "additionalCaseInfo", FieldKind.OPTIONAL, 1, 1, 6, "positionType =\"dummy\"", null);
        addTtlField(fields, 1, 1, 6);
    }
}
