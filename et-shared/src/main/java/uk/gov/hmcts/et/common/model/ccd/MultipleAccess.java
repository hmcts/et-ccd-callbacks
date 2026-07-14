package uk.gov.hmcts.et.common.model.ccd;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import uk.gov.hmcts.ccd.sdk.api.HasAccessControl;
import uk.gov.hmcts.ccd.sdk.api.HasRole;
import uk.gov.hmcts.ccd.sdk.api.Permission;

public abstract class MultipleAccess implements HasAccessControl {

    private final Object[][] grants;

    protected MultipleAccess(Object[]... grants) {
        this.grants = grants;
    }

    protected static Object[] grant(MultipleRole role, Permission... permissions) {
        return new Object[] {role, permissions};
    }

    @Override
    public SetMultimap<HasRole, Permission> getGrants() {
        SetMultimap<HasRole, Permission> result = HashMultimap.create();
        for (Object[] grant : grants) {
            result.putAll((MultipleRole) grant[0], java.util.List.of((Permission[]) grant[1]));
        }
        return result;
    }

    public static final class Access01 extends MultipleAccess {
        public Access01() {
            super(
                    grant(
                            MultipleRole.EMPLOYMENT_API,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(
                            MultipleRole.ENGLAND_WALES_CASEWORKER,
                            Permission.C,
                            Permission.R,
                            Permission.U),
                    grant(MultipleRole.EMPLOYMENT_JUDGE, Permission.R),
                    grant(MultipleRole.ACAS_API, Permission.R));
        }
    }

    public static final class Access02 extends MultipleAccess {
        public Access02() {
            super(
                    grant(
                            MultipleRole.EMPLOYMENT_API,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(
                            MultipleRole.ENGLAND_WALES_CASEWORKER,
                            Permission.C,
                            Permission.R,
                            Permission.U),
                    grant(
                            MultipleRole.ENGLAND_WALES_JUDGE,
                            Permission.C,
                            Permission.R,
                            Permission.U),
                    grant(MultipleRole.EMPLOYMENT_JUDGE, Permission.R),
                    grant(MultipleRole.EMPLOYMENT_CASEWORKER, Permission.R),
                    grant(MultipleRole.CITIZEN, Permission.C, Permission.R, Permission.U),
                    grant(MultipleRole.ACAS_API, Permission.R));
        }
    }

    public static final class Access03 extends MultipleAccess {
        public Access03() {
            super(
                    grant(
                            MultipleRole.EMPLOYMENT_API,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(
                            MultipleRole.ENGLAND_WALES_CASEWORKER,
                            Permission.C,
                            Permission.R,
                            Permission.U),
                    grant(
                            MultipleRole.ENGLAND_WALES_JUDGE,
                            Permission.C,
                            Permission.R,
                            Permission.U),
                    grant(MultipleRole.EMPLOYMENT_JUDGE, Permission.R),
                    grant(MultipleRole.EMPLOYMENT_CASEWORKER, Permission.R),
                    grant(MultipleRole.CITIZEN, Permission.C, Permission.R, Permission.U));
        }
    }

    public static final class Access04 extends MultipleAccess {
        public Access04() {
            super(
                    grant(
                            MultipleRole.EMPLOYMENT_API,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(
                            MultipleRole.ENGLAND_WALES_CASEWORKER,
                            Permission.C,
                            Permission.R,
                            Permission.U),
                    grant(
                            MultipleRole.ENGLAND_WALES_JUDGE,
                            Permission.C,
                            Permission.R,
                            Permission.U),
                    grant(MultipleRole.EMPLOYMENT_JUDGE, Permission.R),
                    grant(MultipleRole.EMPLOYMENT_CASEWORKER, Permission.R),
                    grant(MultipleRole.ACAS_API, Permission.R));
        }
    }

    public static final class Access05 extends MultipleAccess {
        public Access05() {
            super(
                    grant(
                            MultipleRole.EMPLOYMENT_API,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(
                            MultipleRole.ENGLAND_WALES_CASEWORKER,
                            Permission.C,
                            Permission.R,
                            Permission.U),
                    grant(
                            MultipleRole.ENGLAND_WALES_JUDGE,
                            Permission.C,
                            Permission.R,
                            Permission.U),
                    grant(MultipleRole.EMPLOYMENT_JUDGE, Permission.R),
                    grant(MultipleRole.LEGAL_REP, Permission.R),
                    grant(MultipleRole.WA_TASK_CONFIGURATION, Permission.R),
                    grant(MultipleRole.CITIZEN, Permission.C, Permission.R, Permission.U),
                    grant(MultipleRole.ACAS_API, Permission.R));
        }
    }

    public static final class Access06 extends MultipleAccess {
        public Access06() {
            super(
                    grant(
                            MultipleRole.EMPLOYMENT_API,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(
                            MultipleRole.ENGLAND_WALES_CASEWORKER,
                            Permission.C,
                            Permission.R,
                            Permission.U),
                    grant(
                            MultipleRole.ENGLAND_WALES_JUDGE,
                            Permission.C,
                            Permission.R,
                            Permission.U),
                    grant(MultipleRole.EMPLOYMENT_JUDGE, Permission.R),
                    grant(MultipleRole.LEGAL_REP, Permission.R),
                    grant(MultipleRole.ACAS_API, Permission.R));
        }
    }

    public static final class Access07 extends MultipleAccess {
        public Access07() {
            super(
                    grant(
                            MultipleRole.EMPLOYMENT_API,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(
                            MultipleRole.ENGLAND_WALES_CASEWORKER,
                            Permission.C,
                            Permission.R,
                            Permission.U),
                    grant(
                            MultipleRole.ENGLAND_WALES_JUDGE,
                            Permission.C,
                            Permission.R,
                            Permission.U),
                    grant(MultipleRole.EMPLOYMENT_JUDGE, Permission.R),
                    grant(MultipleRole.WA_TASK_CONFIGURATION, Permission.R),
                    grant(MultipleRole.CITIZEN, Permission.C, Permission.R, Permission.U),
                    grant(MultipleRole.ACAS_API, Permission.R));
        }
    }

    public static final class Access08 extends MultipleAccess {
        public Access08() {
            super(
                    grant(
                            MultipleRole.EMPLOYMENT_API,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(
                            MultipleRole.ENGLAND_WALES_CASEWORKER,
                            Permission.C,
                            Permission.R,
                            Permission.U),
                    grant(
                            MultipleRole.ENGLAND_WALES_JUDGE,
                            Permission.C,
                            Permission.R,
                            Permission.U),
                    grant(MultipleRole.EMPLOYMENT_JUDGE, Permission.R),
                    grant(MultipleRole.CITIZEN, Permission.C, Permission.R, Permission.U),
                    grant(MultipleRole.ACAS_API, Permission.R));
        }
    }

    public static final class Access09 extends MultipleAccess {
        public Access09() {
            super(
                    grant(
                            MultipleRole.EMPLOYMENT_API,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(
                            MultipleRole.ENGLAND_WALES_CASEWORKER,
                            Permission.C,
                            Permission.R,
                            Permission.U),
                    grant(
                            MultipleRole.ENGLAND_WALES_JUDGE,
                            Permission.C,
                            Permission.R,
                            Permission.U),
                    grant(MultipleRole.EMPLOYMENT_JUDGE, Permission.R),
                    grant(MultipleRole.ACAS_API, Permission.R));
        }
    }

    public static final class Access10 extends MultipleAccess {
        public Access10() {
            super(
                    grant(
                            MultipleRole.EMPLOYMENT_API,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(
                            MultipleRole.ENGLAND_WALES_CASEWORKER,
                            Permission.C,
                            Permission.R,
                            Permission.U),
                    grant(
                            MultipleRole.ENGLAND_WALES_JUDGE,
                            Permission.C,
                            Permission.R,
                            Permission.U),
                    grant(MultipleRole.EMPLOYMENT_JUDGE, Permission.R));
        }
    }

    public static final class Access11 extends MultipleAccess {
        public Access11() {
            super(
                    grant(
                            MultipleRole.EMPLOYMENT_API,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(
                            MultipleRole.ENGLAND_WALES_CASEWORKER,
                            Permission.C,
                            Permission.R,
                            Permission.U),
                    grant(
                            MultipleRole.ENGLAND_WALES_JUDGE,
                            Permission.C,
                            Permission.R,
                            Permission.U),
                    grant(
                            MultipleRole.LEGAL_REP,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(MultipleRole.CITIZEN, Permission.C, Permission.R, Permission.U));
        }
    }

    public static final class Access12 extends MultipleAccess {
        public Access12() {
            super(
                    grant(
                            MultipleRole.EMPLOYMENT_API,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(
                            MultipleRole.ENGLAND_WALES_CASEWORKER,
                            Permission.C,
                            Permission.R,
                            Permission.U),
                    grant(
                            MultipleRole.ENGLAND_WALES_JUDGE,
                            Permission.C,
                            Permission.R,
                            Permission.U),
                    grant(MultipleRole.LEGAL_REP, Permission.R),
                    grant(MultipleRole.WA_TASK_CONFIGURATION, Permission.R),
                    grant(MultipleRole.CITIZEN, Permission.C, Permission.R, Permission.U));
        }
    }

    public static final class Access13 extends MultipleAccess {
        public Access13() {
            super(
                    grant(
                            MultipleRole.EMPLOYMENT_API,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(
                            MultipleRole.ENGLAND_WALES_CASEWORKER,
                            Permission.C,
                            Permission.R,
                            Permission.U),
                    grant(
                            MultipleRole.ENGLAND_WALES_JUDGE,
                            Permission.C,
                            Permission.R,
                            Permission.U),
                    grant(MultipleRole.LEGAL_REP, Permission.R),
                    grant(MultipleRole.CITIZEN, Permission.C, Permission.R, Permission.U));
        }
    }

    public static final class Access14 extends MultipleAccess {
        public Access14() {
            super(
                    grant(
                            MultipleRole.EMPLOYMENT_API,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(
                            MultipleRole.ENGLAND_WALES_CASEWORKER,
                            Permission.C,
                            Permission.R,
                            Permission.U),
                    grant(
                            MultipleRole.ENGLAND_WALES_JUDGE,
                            Permission.C,
                            Permission.R,
                            Permission.U),
                    grant(MultipleRole.CITIZEN, Permission.C, Permission.R, Permission.U));
        }
    }

    public static final class Access15 extends MultipleAccess {
        public Access15() {
            super(
                    grant(
                            MultipleRole.EMPLOYMENT_API,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(
                            MultipleRole.ENGLAND_WALES_CASEWORKER,
                            Permission.C,
                            Permission.R,
                            Permission.U),
                    grant(
                            MultipleRole.ENGLAND_WALES_JUDGE,
                            Permission.C,
                            Permission.R,
                            Permission.U));
        }
    }

    public static final class Access16 extends MultipleAccess {
        public Access16() {
            super(
                    grant(
                            MultipleRole.EMPLOYMENT_API,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(
                            MultipleRole.ENGLAND_WALES_CASEWORKER,
                            Permission.C,
                            Permission.R,
                            Permission.U),
                    grant(
                            MultipleRole.ENGLAND_WALES_JUDGE,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(MultipleRole.LEGAL_REP, Permission.C, Permission.R, Permission.U));
        }
    }

    public static final class Access17 extends MultipleAccess {
        public Access17() {
            super(
                    grant(
                            MultipleRole.EMPLOYMENT_API,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(
                            MultipleRole.ENGLAND_WALES_CASEWORKER,
                            Permission.C,
                            Permission.R,
                            Permission.U),
                    grant(
                            MultipleRole.ENGLAND_WALES_JUDGE,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D));
        }
    }

    public static final class Access18 extends MultipleAccess {
        public Access18() {
            super(
                    grant(
                            MultipleRole.EMPLOYMENT_API,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(
                            MultipleRole.ENGLAND_WALES_CASEWORKER,
                            Permission.C,
                            Permission.R,
                            Permission.U),
                    grant(MultipleRole.ENGLAND_WALES_JUDGE, Permission.R),
                    grant(MultipleRole.EMPLOYMENT_JUDGE, Permission.R));
        }
    }

    public static final class Access19 extends MultipleAccess {
        public Access19() {
            super(
                    grant(
                            MultipleRole.EMPLOYMENT_API,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(
                            MultipleRole.ENGLAND_WALES_CASEWORKER,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(
                            MultipleRole.ENGLAND_WALES_JUDGE,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(MultipleRole.EMPLOYMENT_JUDGE, Permission.C, Permission.R, Permission.U),
                    grant(
                            MultipleRole.EMPLOYMENT_CASEWORKER,
                            Permission.C,
                            Permission.R,
                            Permission.U),
                    grant(MultipleRole.ACAS_API, Permission.R));
        }
    }

    public static final class Access20 extends MultipleAccess {
        public Access20() {
            super(
                    grant(
                            MultipleRole.EMPLOYMENT_API,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(
                            MultipleRole.ENGLAND_WALES_CASEWORKER,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(
                            MultipleRole.ENGLAND_WALES_JUDGE,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(MultipleRole.EMPLOYMENT_JUDGE, Permission.C, Permission.R, Permission.U),
                    grant(MultipleRole.EMPLOYMENT_CASEWORKER, Permission.R),
                    grant(MultipleRole.ACAS_API, Permission.R));
        }
    }

    public static final class Access21 extends MultipleAccess {
        public Access21() {
            super(
                    grant(
                            MultipleRole.EMPLOYMENT_API,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(
                            MultipleRole.ENGLAND_WALES_CASEWORKER,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(
                            MultipleRole.ENGLAND_WALES_JUDGE,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(MultipleRole.EMPLOYMENT_JUDGE, Permission.C, Permission.R, Permission.U),
                    grant(MultipleRole.EMPLOYMENT_CASEWORKER, Permission.R));
        }
    }

    public static final class Access22 extends MultipleAccess {
        public Access22() {
            super(
                    grant(
                            MultipleRole.EMPLOYMENT_API,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(
                            MultipleRole.ENGLAND_WALES_CASEWORKER,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(
                            MultipleRole.ENGLAND_WALES_JUDGE,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(MultipleRole.EMPLOYMENT_JUDGE, Permission.C, Permission.R, Permission.U),
                    grant(MultipleRole.LEGAL_REP, Permission.D),
                    grant(
                            MultipleRole.EMPLOYMENT_CASEWORKER,
                            Permission.C,
                            Permission.R,
                            Permission.U),
                    grant(MultipleRole.ACAS_API, Permission.R));
        }
    }

    public static final class Access23 extends MultipleAccess {
        public Access23() {
            super(
                    grant(
                            MultipleRole.EMPLOYMENT_API,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(
                            MultipleRole.ENGLAND_WALES_CASEWORKER,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(
                            MultipleRole.ENGLAND_WALES_JUDGE,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(MultipleRole.EMPLOYMENT_JUDGE, Permission.C, Permission.R, Permission.U),
                    grant(MultipleRole.LEGAL_REP, Permission.D),
                    grant(MultipleRole.EMPLOYMENT_CASEWORKER, Permission.R),
                    grant(MultipleRole.ACAS_API, Permission.R));
        }
    }

    public static final class Access24 extends MultipleAccess {
        public Access24() {
            super(
                    grant(
                            MultipleRole.EMPLOYMENT_API,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(
                            MultipleRole.ENGLAND_WALES_CASEWORKER,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(
                            MultipleRole.ENGLAND_WALES_JUDGE,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(MultipleRole.EMPLOYMENT_JUDGE, Permission.C, Permission.R, Permission.U),
                    grant(MultipleRole.LEGAL_REP, Permission.D),
                    grant(MultipleRole.EMPLOYMENT_CASEWORKER, Permission.R));
        }
    }

    public static final class Access25 extends MultipleAccess {
        public Access25() {
            super(
                    grant(
                            MultipleRole.EMPLOYMENT_API,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(
                            MultipleRole.ENGLAND_WALES_CASEWORKER,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(
                            MultipleRole.ENGLAND_WALES_JUDGE,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(
                            MultipleRole.EMPLOYMENT_JUDGE,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(
                            MultipleRole.EMPLOYMENT_CASEWORKER,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D));
        }
    }

    public static final class Access26 extends MultipleAccess {
        public Access26() {
            super(
                    grant(
                            MultipleRole.EMPLOYMENT_API,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(
                            MultipleRole.ENGLAND_WALES_CASEWORKER,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(
                            MultipleRole.ENGLAND_WALES_JUDGE,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(
                            MultipleRole.EMPLOYMENT_JUDGE,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(MultipleRole.LEGAL_REP, Permission.D),
                    grant(
                            MultipleRole.EMPLOYMENT_CASEWORKER,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D));
        }
    }

    public static final class Access27 extends MultipleAccess {
        public Access27() {
            super(
                    grant(
                            MultipleRole.EMPLOYMENT_API,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(
                            MultipleRole.ENGLAND_WALES_CASEWORKER,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(
                            MultipleRole.ENGLAND_WALES_JUDGE,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(MultipleRole.EMPLOYMENT_JUDGE, Permission.R));
        }
    }

    public static final class Access28 extends MultipleAccess {
        public Access28() {
            super(
                    grant(
                            MultipleRole.EMPLOYMENT_API,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(MultipleRole.EMPLOYMENT_JUDGE, Permission.R),
                    grant(
                            MultipleRole.SCOTLAND_CASEWORKER,
                            Permission.C,
                            Permission.R,
                            Permission.U),
                    grant(MultipleRole.ACAS_API, Permission.R));
        }
    }

    public static final class Access29 extends MultipleAccess {
        public Access29() {
            super(
                    grant(
                            MultipleRole.EMPLOYMENT_API,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(MultipleRole.SCOTLAND_JUDGE, Permission.C, Permission.R, Permission.U),
                    grant(MultipleRole.EMPLOYMENT_JUDGE, Permission.R),
                    grant(MultipleRole.LEGAL_REP, Permission.R),
                    grant(
                            MultipleRole.SCOTLAND_CASEWORKER,
                            Permission.C,
                            Permission.R,
                            Permission.U),
                    grant(MultipleRole.WA_TASK_CONFIGURATION, Permission.R),
                    grant(MultipleRole.CITIZEN, Permission.C, Permission.R, Permission.U),
                    grant(MultipleRole.ACAS_API, Permission.R));
        }
    }

    public static final class Access30 extends MultipleAccess {
        public Access30() {
            super(
                    grant(
                            MultipleRole.EMPLOYMENT_API,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(MultipleRole.SCOTLAND_JUDGE, Permission.C, Permission.R, Permission.U),
                    grant(MultipleRole.EMPLOYMENT_JUDGE, Permission.R),
                    grant(MultipleRole.LEGAL_REP, Permission.R),
                    grant(
                            MultipleRole.SCOTLAND_CASEWORKER,
                            Permission.C,
                            Permission.R,
                            Permission.U),
                    grant(MultipleRole.ACAS_API, Permission.R));
        }
    }

    public static final class Access31 extends MultipleAccess {
        public Access31() {
            super(
                    grant(
                            MultipleRole.EMPLOYMENT_API,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(MultipleRole.SCOTLAND_JUDGE, Permission.C, Permission.R, Permission.U),
                    grant(MultipleRole.EMPLOYMENT_JUDGE, Permission.R),
                    grant(
                            MultipleRole.SCOTLAND_CASEWORKER,
                            Permission.C,
                            Permission.R,
                            Permission.U),
                    grant(MultipleRole.EMPLOYMENT_CASEWORKER, Permission.R),
                    grant(MultipleRole.CITIZEN, Permission.C, Permission.R, Permission.U),
                    grant(MultipleRole.ACAS_API, Permission.R));
        }
    }

    public static final class Access32 extends MultipleAccess {
        public Access32() {
            super(
                    grant(
                            MultipleRole.EMPLOYMENT_API,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(MultipleRole.SCOTLAND_JUDGE, Permission.C, Permission.R, Permission.U),
                    grant(MultipleRole.EMPLOYMENT_JUDGE, Permission.R),
                    grant(
                            MultipleRole.SCOTLAND_CASEWORKER,
                            Permission.C,
                            Permission.R,
                            Permission.U),
                    grant(MultipleRole.EMPLOYMENT_CASEWORKER, Permission.R),
                    grant(MultipleRole.CITIZEN, Permission.C, Permission.R, Permission.U));
        }
    }

    public static final class Access33 extends MultipleAccess {
        public Access33() {
            super(
                    grant(
                            MultipleRole.EMPLOYMENT_API,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(MultipleRole.SCOTLAND_JUDGE, Permission.C, Permission.R, Permission.U),
                    grant(MultipleRole.EMPLOYMENT_JUDGE, Permission.R),
                    grant(
                            MultipleRole.SCOTLAND_CASEWORKER,
                            Permission.C,
                            Permission.R,
                            Permission.U),
                    grant(MultipleRole.EMPLOYMENT_CASEWORKER, Permission.R),
                    grant(MultipleRole.ACAS_API, Permission.R));
        }
    }

    public static final class Access34 extends MultipleAccess {
        public Access34() {
            super(
                    grant(
                            MultipleRole.EMPLOYMENT_API,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(MultipleRole.SCOTLAND_JUDGE, Permission.C, Permission.R, Permission.U),
                    grant(MultipleRole.EMPLOYMENT_JUDGE, Permission.R),
                    grant(
                            MultipleRole.SCOTLAND_CASEWORKER,
                            Permission.C,
                            Permission.R,
                            Permission.U),
                    grant(MultipleRole.EMPLOYMENT_CASEWORKER, Permission.R));
        }
    }

    public static final class Access35 extends MultipleAccess {
        public Access35() {
            super(
                    grant(
                            MultipleRole.EMPLOYMENT_API,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(MultipleRole.SCOTLAND_JUDGE, Permission.C, Permission.R, Permission.U),
                    grant(MultipleRole.EMPLOYMENT_JUDGE, Permission.R),
                    grant(
                            MultipleRole.SCOTLAND_CASEWORKER,
                            Permission.C,
                            Permission.R,
                            Permission.U),
                    grant(MultipleRole.WA_TASK_CONFIGURATION, Permission.R),
                    grant(MultipleRole.ACAS_API, Permission.R));
        }
    }

    public static final class Access36 extends MultipleAccess {
        public Access36() {
            super(
                    grant(
                            MultipleRole.EMPLOYMENT_API,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(MultipleRole.SCOTLAND_JUDGE, Permission.C, Permission.R, Permission.U),
                    grant(MultipleRole.EMPLOYMENT_JUDGE, Permission.R),
                    grant(
                            MultipleRole.SCOTLAND_CASEWORKER,
                            Permission.C,
                            Permission.R,
                            Permission.U),
                    grant(MultipleRole.CITIZEN, Permission.C, Permission.R, Permission.U),
                    grant(MultipleRole.ACAS_API, Permission.R));
        }
    }

    public static final class Access37 extends MultipleAccess {
        public Access37() {
            super(
                    grant(
                            MultipleRole.EMPLOYMENT_API,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(MultipleRole.SCOTLAND_JUDGE, Permission.C, Permission.R, Permission.U),
                    grant(MultipleRole.EMPLOYMENT_JUDGE, Permission.R),
                    grant(
                            MultipleRole.SCOTLAND_CASEWORKER,
                            Permission.C,
                            Permission.R,
                            Permission.U));
        }
    }

    public static final class Access38 extends MultipleAccess {
        public Access38() {
            super(
                    grant(
                            MultipleRole.EMPLOYMENT_API,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(MultipleRole.SCOTLAND_JUDGE, Permission.C, Permission.R, Permission.U),
                    grant(
                            MultipleRole.LEGAL_REP,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(
                            MultipleRole.SCOTLAND_CASEWORKER,
                            Permission.C,
                            Permission.R,
                            Permission.U),
                    grant(MultipleRole.CITIZEN, Permission.C, Permission.R, Permission.U));
        }
    }

    public static final class Access39 extends MultipleAccess {
        public Access39() {
            super(
                    grant(
                            MultipleRole.EMPLOYMENT_API,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(MultipleRole.SCOTLAND_JUDGE, Permission.C, Permission.R, Permission.U),
                    grant(MultipleRole.LEGAL_REP, Permission.R),
                    grant(
                            MultipleRole.SCOTLAND_CASEWORKER,
                            Permission.C,
                            Permission.R,
                            Permission.U),
                    grant(MultipleRole.WA_TASK_CONFIGURATION, Permission.R),
                    grant(MultipleRole.CITIZEN, Permission.C, Permission.R, Permission.U));
        }
    }

    public static final class Access40 extends MultipleAccess {
        public Access40() {
            super(
                    grant(
                            MultipleRole.EMPLOYMENT_API,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(MultipleRole.SCOTLAND_JUDGE, Permission.C, Permission.R, Permission.U),
                    grant(MultipleRole.LEGAL_REP, Permission.R),
                    grant(
                            MultipleRole.SCOTLAND_CASEWORKER,
                            Permission.C,
                            Permission.R,
                            Permission.U),
                    grant(MultipleRole.CITIZEN, Permission.C, Permission.R, Permission.U));
        }
    }

    public static final class Access41 extends MultipleAccess {
        public Access41() {
            super(
                    grant(
                            MultipleRole.EMPLOYMENT_API,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(MultipleRole.SCOTLAND_JUDGE, Permission.C, Permission.R, Permission.U),
                    grant(
                            MultipleRole.SCOTLAND_CASEWORKER,
                            Permission.C,
                            Permission.R,
                            Permission.U),
                    grant(MultipleRole.CITIZEN, Permission.C, Permission.R, Permission.U));
        }
    }

    public static final class Access42 extends MultipleAccess {
        public Access42() {
            super(
                    grant(
                            MultipleRole.EMPLOYMENT_API,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(MultipleRole.SCOTLAND_JUDGE, Permission.C, Permission.R, Permission.U),
                    grant(
                            MultipleRole.SCOTLAND_CASEWORKER,
                            Permission.C,
                            Permission.R,
                            Permission.U));
        }
    }

    public static final class Access43 extends MultipleAccess {
        public Access43() {
            super(
                    grant(
                            MultipleRole.EMPLOYMENT_API,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(
                            MultipleRole.SCOTLAND_JUDGE,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(MultipleRole.EMPLOYMENT_JUDGE, Permission.C, Permission.R, Permission.U),
                    grant(MultipleRole.LEGAL_REP, Permission.D),
                    grant(
                            MultipleRole.SCOTLAND_CASEWORKER,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(
                            MultipleRole.EMPLOYMENT_CASEWORKER,
                            Permission.C,
                            Permission.R,
                            Permission.U),
                    grant(MultipleRole.ACAS_API, Permission.R));
        }
    }

    public static final class Access44 extends MultipleAccess {
        public Access44() {
            super(
                    grant(
                            MultipleRole.EMPLOYMENT_API,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(
                            MultipleRole.SCOTLAND_JUDGE,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(MultipleRole.EMPLOYMENT_JUDGE, Permission.C, Permission.R, Permission.U),
                    grant(MultipleRole.LEGAL_REP, Permission.D),
                    grant(
                            MultipleRole.SCOTLAND_CASEWORKER,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(MultipleRole.EMPLOYMENT_CASEWORKER, Permission.R),
                    grant(MultipleRole.ACAS_API, Permission.R));
        }
    }

    public static final class Access45 extends MultipleAccess {
        public Access45() {
            super(
                    grant(
                            MultipleRole.EMPLOYMENT_API,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(
                            MultipleRole.SCOTLAND_JUDGE,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(MultipleRole.EMPLOYMENT_JUDGE, Permission.C, Permission.R, Permission.U),
                    grant(MultipleRole.LEGAL_REP, Permission.D),
                    grant(
                            MultipleRole.SCOTLAND_CASEWORKER,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(MultipleRole.EMPLOYMENT_CASEWORKER, Permission.R));
        }
    }

    public static final class Access46 extends MultipleAccess {
        public Access46() {
            super(
                    grant(
                            MultipleRole.EMPLOYMENT_API,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(
                            MultipleRole.SCOTLAND_JUDGE,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(MultipleRole.EMPLOYMENT_JUDGE, Permission.C, Permission.R, Permission.U),
                    grant(
                            MultipleRole.SCOTLAND_CASEWORKER,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(
                            MultipleRole.EMPLOYMENT_CASEWORKER,
                            Permission.C,
                            Permission.R,
                            Permission.U),
                    grant(MultipleRole.ACAS_API, Permission.R));
        }
    }

    public static final class Access47 extends MultipleAccess {
        public Access47() {
            super(
                    grant(
                            MultipleRole.EMPLOYMENT_API,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(
                            MultipleRole.SCOTLAND_JUDGE,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(MultipleRole.EMPLOYMENT_JUDGE, Permission.C, Permission.R, Permission.U),
                    grant(
                            MultipleRole.SCOTLAND_CASEWORKER,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(MultipleRole.EMPLOYMENT_CASEWORKER, Permission.R),
                    grant(MultipleRole.ACAS_API, Permission.R));
        }
    }

    public static final class Access48 extends MultipleAccess {
        public Access48() {
            super(
                    grant(
                            MultipleRole.EMPLOYMENT_API,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(
                            MultipleRole.SCOTLAND_JUDGE,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(MultipleRole.EMPLOYMENT_JUDGE, Permission.C, Permission.R, Permission.U),
                    grant(
                            MultipleRole.SCOTLAND_CASEWORKER,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(MultipleRole.EMPLOYMENT_CASEWORKER, Permission.R));
        }
    }

    public static final class Access49 extends MultipleAccess {
        public Access49() {
            super(
                    grant(
                            MultipleRole.EMPLOYMENT_API,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(
                            MultipleRole.SCOTLAND_JUDGE,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(
                            MultipleRole.EMPLOYMENT_JUDGE,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(MultipleRole.LEGAL_REP, Permission.D),
                    grant(
                            MultipleRole.SCOTLAND_CASEWORKER,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(
                            MultipleRole.EMPLOYMENT_CASEWORKER,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D));
        }
    }

    public static final class Access50 extends MultipleAccess {
        public Access50() {
            super(
                    grant(
                            MultipleRole.EMPLOYMENT_API,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(
                            MultipleRole.SCOTLAND_JUDGE,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(
                            MultipleRole.EMPLOYMENT_JUDGE,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(
                            MultipleRole.SCOTLAND_CASEWORKER,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(
                            MultipleRole.EMPLOYMENT_CASEWORKER,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D));
        }
    }

    public static final class Access51 extends MultipleAccess {
        public Access51() {
            super(
                    grant(
                            MultipleRole.EMPLOYMENT_API,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(
                            MultipleRole.SCOTLAND_JUDGE,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(MultipleRole.LEGAL_REP, Permission.C, Permission.R, Permission.U),
                    grant(
                            MultipleRole.SCOTLAND_CASEWORKER,
                            Permission.C,
                            Permission.R,
                            Permission.U));
        }
    }

    public static final class Access52 extends MultipleAccess {
        public Access52() {
            super(
                    grant(
                            MultipleRole.EMPLOYMENT_API,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(
                            MultipleRole.SCOTLAND_JUDGE,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(
                            MultipleRole.SCOTLAND_CASEWORKER,
                            Permission.C,
                            Permission.R,
                            Permission.U));
        }
    }

    public static final class Access53 extends MultipleAccess {
        public Access53() {
            super(
                    grant(
                            MultipleRole.EMPLOYMENT_API,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(MultipleRole.SCOTLAND_JUDGE, Permission.R),
                    grant(MultipleRole.EMPLOYMENT_JUDGE, Permission.R),
                    grant(
                            MultipleRole.SCOTLAND_CASEWORKER,
                            Permission.C,
                            Permission.R,
                            Permission.U));
        }
    }

    public static final class Access54 extends MultipleAccess {
        public Access54() {
            super(
                    grant(
                            MultipleRole.EMPLOYMENT_API,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D),
                    grant(MultipleRole.RAS_VALIDATION, Permission.R),
                    grant(MultipleRole.WA_TASK_CONFIGURATION, Permission.R),
                    grant(MultipleRole.GS_PROFILE, Permission.R));
        }
    }

    public static final class Access55 extends MultipleAccess {
        public Access55() {
            super(
                    grant(
                            MultipleRole.EMPLOYMENT_API,
                            Permission.C,
                            Permission.R,
                            Permission.U,
                            Permission.D));
        }
    }

    public static final class Access56 extends MultipleAccess {
        public Access56() {
            super(
                    grant(MultipleRole.EMPLOYMENT_API, Permission.R),
                    grant(MultipleRole.ENGLAND_WALES_CASEWORKER, Permission.R),
                    grant(MultipleRole.ENGLAND_WALES_JUDGE, Permission.R),
                    grant(MultipleRole.EMPLOYMENT_JUDGE, Permission.R),
                    grant(MultipleRole.EMPLOYMENT_CASEWORKER, Permission.R));
        }
    }

    public static final class Access57 extends MultipleAccess {
        public Access57() {
            super(
                    grant(MultipleRole.EMPLOYMENT_API, Permission.R),
                    grant(MultipleRole.ENGLAND_WALES_CASEWORKER, Permission.R),
                    grant(MultipleRole.ENGLAND_WALES_JUDGE, Permission.R),
                    grant(MultipleRole.EMPLOYMENT_JUDGE, Permission.R));
        }
    }

    public static final class Access58 extends MultipleAccess {
        public Access58() {
            super(
                    grant(MultipleRole.EMPLOYMENT_API, Permission.R),
                    grant(MultipleRole.SCOTLAND_JUDGE, Permission.R),
                    grant(MultipleRole.EMPLOYMENT_JUDGE, Permission.R),
                    grant(MultipleRole.SCOTLAND_CASEWORKER, Permission.R),
                    grant(MultipleRole.EMPLOYMENT_CASEWORKER, Permission.R));
        }
    }

    public static final class Access59 extends MultipleAccess {
        public Access59() {
            super(
                    grant(MultipleRole.EMPLOYMENT_API, Permission.R),
                    grant(MultipleRole.SCOTLAND_JUDGE, Permission.R),
                    grant(MultipleRole.EMPLOYMENT_JUDGE, Permission.R),
                    grant(MultipleRole.SCOTLAND_CASEWORKER, Permission.R));
        }
    }

    public static final class Access60 extends MultipleAccess {
        public Access60() {
            super(
                    grant(MultipleRole.EMPLOYMENT_API, Permission.R),
                    grant(MultipleRole.SCOTLAND_JUDGE, Permission.R),
                    grant(MultipleRole.SCOTLAND_CASEWORKER, Permission.R),
                    grant(MultipleRole.CITIZEN, Permission.R));
        }
    }

    public static final class Access61 extends MultipleAccess {
        public Access61() {
            super(
                    grant(MultipleRole.EMPLOYMENT_API, Permission.R),
                    grant(MultipleRole.LEGAL_REP, Permission.D),
                    grant(MultipleRole.ACAS_API, Permission.R));
        }
    }
}
