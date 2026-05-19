package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.Event.EventBuilder;
import uk.gov.hmcts.ccd.sdk.api.FieldCollection.FieldCollectionBuilder;

@Component
public class ScotlandDisposeCaseConfig extends DisposeCaseConfig<ScotlandCaseData> {

    public ScotlandDisposeCaseConfig() {
        super(
            EtUserRole.CASEWORKER_EMPLOYMENT_SCOTLAND,
            EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND,
            38,
            "No",
            null
        );
    }

    @Override
    protected void addFields(
        FieldCollectionBuilder<ScotlandCaseData, EtState, EventBuilder<ScotlandCaseData, EtUserRole, EtState>> fields
    ) {
        fields.pageWithCallbackUrl("1", "${ET_COS_URL}/aboutToStartDisposal");
        addField(
            fields,
            "managingOffice",
            FieldKind.MANDATORY,
            1,
            1,
            1,
            null,
            null
        );

        addField(fields, "positionType", FieldKind.READ_ONLY, 2, 2, 1, null, null);
        addField(fields, "clerkResponsible", FieldKind.MANDATORY, 2, 2, 2, null, null);
        addScotlandFileLocation(fields, "Glasgow");
        addScotlandFileLocation(fields, "Aberdeen");
        addScotlandFileLocation(fields, "Dundee");
        addScotlandFileLocation(fields, "Edinburgh");
        addField(fields, "caseNotes", FieldKind.OPTIONAL, 2, 2, 5, null, null);
        addField(fields, "currentPosition", FieldKind.READ_ONLY, 2, 2, 6, "positionType=\"dummy\"", null);
        addField(fields, "additionalCaseInfo", FieldKind.OPTIONAL, 2, 2, 7, "positionType =\"dummy\"", null);
        addTtlField(fields, 2, 2, 8);
    }

    private void addScotlandFileLocation(
        FieldCollectionBuilder<ScotlandCaseData, EtState, EventBuilder<ScotlandCaseData, EtUserRole, EtState>> fields,
        String office
    ) {
        addField(
            fields,
            "fileLocation" + office,
            FieldKind.OPTIONAL,
            2,
            2,
            4,
            "managingOffice=\"" + office + "\"",
            "Yes"
        );
    }
}
