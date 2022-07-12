package uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum JurisdictionCode {
    ADG("Suffer a detriment and/or dismissal resulting from a failure to allow an employee to be accompanied"
        + " or to accompany a fellow employee at a disciplinary/grievance hearing"),
    ADT("Discriminatory terms or rules"),
    ADTST("Application for a declaration that the inclusion of discriminatory terms/rules within certain "
        + "agreements or rules causes the aforesaid to be invalid"),
    APA("Application by an employee, their representative or trade union for a protective award as a "
        + "result of an employer's failure to consult over a redundancy situation"),
    AWR("Suffered less favourable treatment and/or dismissal as an agency worker, than a directly "
        + "recruited employee including being denied equal pay after 12 weeks (formerly Swedish derogation contract)"),
    CCP("Failure of the employer to consult with an employee representative or trade union about a proposed "
        + "contracting out of a pension scheme"),
    COM("Application or complaint by the EHRC in respect of discriminatory advertisements or instructions "
        + "or pressure to discriminate (including preliminary action before a claim to the county court)"),
    EAP("Failure to provide equal pay for equal value work"),
    FCT("Failure of the employer to consult with an employee rep. or trade union or a transferor with a "
        + "transferee about a proposed transfer and failure to notify employee liability information"),
    FLW("Suffer a detriment and/or dismissal for claiming under the flexible working regulations or be "
        + "subject to a breach of procedure"),
    FML("Failure to pay remuneration whilst suspended from work for health and safety reasons whilst pregnant"
        + " or on mat. leave"),
    FPA("Application by an employee that an employer has failed to pay a protected award as ordered by a "
        + "tribunal"),
    FTC("Failure to provide a written statement of terms and conditions and any subsequent changes to those "
        + "terms"),
    FTE("Suffered less favourable treatment and/or dismissal as a fixed term employee, than a full-time "
        + "employee or, on becoming permanent, failed to receive a written statement of confirmation from employer"),
    FTO("Failure to allow time off for trade union activities or duties, for ante-natal care or for public "
        + "duties"),
    FTP("Failure to provide a  guarantee payment"),
    FTR("Failure to pay remuneration whilst suspended for medical reasons"),
    FTS("Failure to allow time off to seek work during a redundancy situation"),
    FTU("Failure of an employer to comply with an award by a tribunal following a finding that the employer "
        + "had previously failed to consult about a proposed transfer of an undertaking"),
    FT1("Failure to allow or to pay for time off for care of dependants, union learning representatives "
        + "duties, pension scheme trustee duties, employee representatives duties, young person studying/training "
        + "and European Works Council duties"),
    FWP("Failure to provide a written pay statement or an adequate pay statement"),
    FWS("Failure to provide a written statement of reasons for dismissal or the contents of the statement "
        + "are disputed"),
    HAS("Appeal against an enforcement, improvement or prohibition notice imposed by the HSE or Environmental"
        + " Health Inspector, or by the Environment Agency"),
    HSD("Failure to pay for or allow time off to carry out Safety Rep duties or undertake training"),
    HSR("Suffer a detriment, dismissal or redundancy for health and safety reasons"),
    IRF("Application for interim relief"),
    ISV("Failure by the SOS to make an insolvency payment in lieu of wages and/or redundancy"),
    LEV("Appeal against the levy assessment of an Industrial Training Board"),
    LSO("Loss of office as a result of the reorganisation of a statutory body"),
    MWA("Appeal against an enforcement or penalty notice issued by Her Majesty's Revenue & Customs"),
    MWD("Suffer a detriment and/or dismissal related to failure to pay the minimum wage or allow access to "
        + "records"),
    NNA("Appeal against an unlawful act on a notice issued by EHRC"),
    PAC("Failure of the employer to comply with a certificate of exemption or to deduct funds from employees "
        + "pay in order to contribute to a trade union political fund"),
    PAY("Failure of the employer to prevent unauthorised or excessive deductions in the form of union "
        + "subscriptions"),
    PEN("Failure of the Secretary of State to pay unpaid contributions to a pensions scheme following an "
        + "application for payment to be made"),
    PLD("Suffer a detriment and/or dismissal due to requesting or taking paternity, adoption, parental "
        + "bereavement leave or time off to assist a dependant"),
    PTE("Suffer less favourable treatment and/or dismissal as a result of being a part time employee by "
        + "comparison to a full-time employee"),
    RPTS("Failure of the SOS to pay a redundancy payment following an application to the NI fund"),
    RTR("Appeal against an improvement notice imposed by a VOSA inspector"),
    RTRST("Suffer a detriment or dismissal as a result of doing work under another contract or arrangement  "
        + "(zero hours contract exclusivity clause)"),
    SUN("Suffer a detriment and/or dismissal for refusing to work on a Sunday"),
    TPE("Suffer less favourable treatment and/or dismissal as a temp. employee than a full time employee"),
    TT("Suffer a detriment and/or dismissal for requesting time to train. Failure of employer to follow "
        + "correct procedures/reject request based on incorrect facts"),
    TUE("Suffer discrimination in obtaining employment due to membership or non-membership of a trade union;"
        + " or refused employment or suffered a detriment for a reason related to a blacklist"),
    TUM("Suffer a detriment and/or dismissal relating to being, not being or proposing to become a trade "
        + "union member"),
    TUR("(a)  Failure of the employer to consult or report about training in relation to a bargaining unit (b)"
        + "  Suffered a detriment on grounds related to recognition of a trade union for collective bargaining"),
    TUS("Suffer discrimination in obtaining the services of an employment agency due to membership or "
        + "non-membership of a trade union; or refused employment agency services or suffered a detriment for a "
        + "reason related to a blacklist"),
    TXCST("Suffered a detriment and/or dismissal due to exercising rights under the Tax Credits Act (JST to be "
        + "notified if a claim is received)"),
    TXC("Appeal against \"Failure to pay an Employment Tribunal award Penalty\""),
    UDC("Unfair dismissal after exercising or claiming a statutory right"),
    UIA("Unfair dismissal in connection to a lock out, strike or other industrial action"),
    WTA("Appeal by a person who has been served with an improvement or prohibition notice under the Working "
        + "Time Regulations 1998"),
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
    WTR("Failure to limit weekly or night working time, or to ensure rest breaks"),
    DRB("Discrimination, including indirect discrimination, discrimination based on association or perception,"
        + " harassment or victimisation on grounds of religion or belief");

    @Getter
    public final String description;
}
