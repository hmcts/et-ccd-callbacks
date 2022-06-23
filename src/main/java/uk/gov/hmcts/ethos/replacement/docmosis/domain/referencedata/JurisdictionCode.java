package uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum JurisdictionCode {
    BOC("(a) Claim of an employee for breach of contract of employment (b) Employer contract claim"),
    UDL("Unfair dismissal on grounds of capability, conduct or some other general reason including the "
             + "result of a transfer of an undertaking"),
    PID("Suffered a detriment and/or dismissal due to exercising rights under the Public Interest Disclosure"
            + " Act"),
    DAG("Discrimination, including indirect discrimination, harassment or victimisation or discrimination based "
             + "on association or perception on grounds of age"),
    DDA("Suffered a detriment, discrimination, including indirect discrimination, and discrimination based on "
             + "association or perception, harassment, victimisation and/or dismissal on grounds of disability or "
            + "failure of employer to make reasonable adjustments"),
    DOD("Suffered a detriment and/or dismissal resulting from requiring time off for other (non-work but not "
             + "Health and Safety) duties, study, training or seeking work"),
    RRD("Discrimination, including indirect discrimination, discrimination based on association or perception,"
             + " harassment or victimisation on grounds of race or ethnic origin"),
    SXD("Discrimination, including indirect discrimination, discrimination based on association or perception,"
             + " harassment or victimisation on grounds of sex, marriage and civil partnership or gender reassignment"),
    DSO("Discrimination, including indirect discrimination, discrimination based on association or perception,"
             + " harassment or victimisation on grounds of sexual orientation"),
    MAT("Suffer a detriment and/or dismissal on grounds of pregnancy, childbirth or maternity "),
    RPT("Failure to pay a redundancy payment"),
    WA("Failure of employer to pay or unauthorised deductions have been made (including Parental Bereavement Pay)"),
    WTRAL("Complaint by a worker that employer has failed to allow them to take or to pay them for statutory "
             + "annual leave entitlement"),
    DRB("Discrimination, including indirect discrimination, discrimination based on association or perception,"
             + " harassment or victimisation on grounds of religion or belief");

    @Getter
    public final String description;
}
