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

    protected static Object[] crud(MultipleRole role) {
        return grant(role, Permission.C, Permission.R, Permission.U, Permission.D);
    }

    protected static Object[] cru(MultipleRole role) {
        return grant(role, Permission.C, Permission.R, Permission.U);
    }

    protected static Object[] read(MultipleRole role) {
        return grant(role, Permission.R);
    }

    protected static Object[] delete(MultipleRole role) {
        return grant(role, Permission.D);
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
                    crud(MultipleRole.EMPLOYMENT_API),
                    cru(MultipleRole.ENGLAND_WALES_CASEWORKER),
                    read(MultipleRole.EMPLOYMENT_JUDGE),
                    read(MultipleRole.ACAS_API));
        }
    }

    public static final class Access02 extends MultipleAccess {
        public Access02() {
            super(
                    crud(MultipleRole.EMPLOYMENT_API),
                    cru(MultipleRole.ENGLAND_WALES_CASEWORKER),
                    cru(MultipleRole.ENGLAND_WALES_JUDGE),
                    read(MultipleRole.EMPLOYMENT_JUDGE),
                    read(MultipleRole.EMPLOYMENT_CASEWORKER),
                    cru(MultipleRole.CITIZEN),
                    read(MultipleRole.ACAS_API));
        }
    }

    public static final class Access03 extends MultipleAccess {
        public Access03() {
            super(
                    crud(MultipleRole.EMPLOYMENT_API),
                    cru(MultipleRole.ENGLAND_WALES_CASEWORKER),
                    cru(MultipleRole.ENGLAND_WALES_JUDGE),
                    read(MultipleRole.EMPLOYMENT_JUDGE),
                    read(MultipleRole.EMPLOYMENT_CASEWORKER),
                    cru(MultipleRole.CITIZEN));
        }
    }

    public static final class Access04 extends MultipleAccess {
        public Access04() {
            super(
                    crud(MultipleRole.EMPLOYMENT_API),
                    cru(MultipleRole.ENGLAND_WALES_CASEWORKER),
                    cru(MultipleRole.ENGLAND_WALES_JUDGE),
                    read(MultipleRole.EMPLOYMENT_JUDGE),
                    read(MultipleRole.EMPLOYMENT_CASEWORKER),
                    read(MultipleRole.ACAS_API));
        }
    }

    public static final class Access05 extends MultipleAccess {
        public Access05() {
            super(
                    crud(MultipleRole.EMPLOYMENT_API),
                    cru(MultipleRole.ENGLAND_WALES_CASEWORKER),
                    cru(MultipleRole.ENGLAND_WALES_JUDGE),
                    read(MultipleRole.EMPLOYMENT_JUDGE),
                    read(MultipleRole.LEGAL_REP),
                    read(MultipleRole.WA_TASK_CONFIGURATION),
                    cru(MultipleRole.CITIZEN),
                    read(MultipleRole.ACAS_API));
        }
    }

    public static final class Access06 extends MultipleAccess {
        public Access06() {
            super(
                    crud(MultipleRole.EMPLOYMENT_API),
                    cru(MultipleRole.ENGLAND_WALES_CASEWORKER),
                    cru(MultipleRole.ENGLAND_WALES_JUDGE),
                    read(MultipleRole.EMPLOYMENT_JUDGE),
                    read(MultipleRole.LEGAL_REP),
                    read(MultipleRole.ACAS_API));
        }
    }

    public static final class Access07 extends MultipleAccess {
        public Access07() {
            super(
                    crud(MultipleRole.EMPLOYMENT_API),
                    cru(MultipleRole.ENGLAND_WALES_CASEWORKER),
                    cru(MultipleRole.ENGLAND_WALES_JUDGE),
                    read(MultipleRole.EMPLOYMENT_JUDGE),
                    read(MultipleRole.WA_TASK_CONFIGURATION),
                    cru(MultipleRole.CITIZEN),
                    read(MultipleRole.ACAS_API));
        }
    }

    public static final class Access08 extends MultipleAccess {
        public Access08() {
            super(
                    crud(MultipleRole.EMPLOYMENT_API),
                    cru(MultipleRole.ENGLAND_WALES_CASEWORKER),
                    cru(MultipleRole.ENGLAND_WALES_JUDGE),
                    read(MultipleRole.EMPLOYMENT_JUDGE),
                    cru(MultipleRole.CITIZEN),
                    read(MultipleRole.ACAS_API));
        }
    }

    public static final class Access09 extends MultipleAccess {
        public Access09() {
            super(
                    crud(MultipleRole.EMPLOYMENT_API),
                    cru(MultipleRole.ENGLAND_WALES_CASEWORKER),
                    cru(MultipleRole.ENGLAND_WALES_JUDGE),
                    read(MultipleRole.EMPLOYMENT_JUDGE),
                    read(MultipleRole.ACAS_API));
        }
    }

    public static final class Access10 extends MultipleAccess {
        public Access10() {
            super(
                    crud(MultipleRole.EMPLOYMENT_API),
                    cru(MultipleRole.ENGLAND_WALES_CASEWORKER),
                    cru(MultipleRole.ENGLAND_WALES_JUDGE),
                    read(MultipleRole.EMPLOYMENT_JUDGE));
        }
    }

    public static final class Access11 extends MultipleAccess {
        public Access11() {
            super(
                    crud(MultipleRole.EMPLOYMENT_API),
                    cru(MultipleRole.ENGLAND_WALES_CASEWORKER),
                    cru(MultipleRole.ENGLAND_WALES_JUDGE),
                    crud(MultipleRole.LEGAL_REP),
                    cru(MultipleRole.CITIZEN));
        }
    }

    public static final class Access12 extends MultipleAccess {
        public Access12() {
            super(
                    crud(MultipleRole.EMPLOYMENT_API),
                    cru(MultipleRole.ENGLAND_WALES_CASEWORKER),
                    cru(MultipleRole.ENGLAND_WALES_JUDGE),
                    read(MultipleRole.LEGAL_REP),
                    read(MultipleRole.WA_TASK_CONFIGURATION),
                    cru(MultipleRole.CITIZEN));
        }
    }

    public static final class Access13 extends MultipleAccess {
        public Access13() {
            super(
                    crud(MultipleRole.EMPLOYMENT_API),
                    cru(MultipleRole.ENGLAND_WALES_CASEWORKER),
                    cru(MultipleRole.ENGLAND_WALES_JUDGE),
                    read(MultipleRole.LEGAL_REP),
                    cru(MultipleRole.CITIZEN));
        }
    }

    public static final class Access14 extends MultipleAccess {
        public Access14() {
            super(
                    crud(MultipleRole.EMPLOYMENT_API),
                    cru(MultipleRole.ENGLAND_WALES_CASEWORKER),
                    cru(MultipleRole.ENGLAND_WALES_JUDGE),
                    cru(MultipleRole.CITIZEN));
        }
    }

    public static final class Access15 extends MultipleAccess {
        public Access15() {
            super(
                    crud(MultipleRole.EMPLOYMENT_API),
                    cru(MultipleRole.ENGLAND_WALES_CASEWORKER),
                    cru(MultipleRole.ENGLAND_WALES_JUDGE));
        }
    }

    public static final class Access16 extends MultipleAccess {
        public Access16() {
            super(
                    crud(MultipleRole.EMPLOYMENT_API),
                    cru(MultipleRole.ENGLAND_WALES_CASEWORKER),
                    crud(MultipleRole.ENGLAND_WALES_JUDGE),
                    cru(MultipleRole.LEGAL_REP));
        }
    }

    public static final class Access17 extends MultipleAccess {
        public Access17() {
            super(
                    crud(MultipleRole.EMPLOYMENT_API),
                    cru(MultipleRole.ENGLAND_WALES_CASEWORKER),
                    crud(MultipleRole.ENGLAND_WALES_JUDGE));
        }
    }

    public static final class Access18 extends MultipleAccess {
        public Access18() {
            super(
                    crud(MultipleRole.EMPLOYMENT_API),
                    cru(MultipleRole.ENGLAND_WALES_CASEWORKER),
                    read(MultipleRole.ENGLAND_WALES_JUDGE),
                    read(MultipleRole.EMPLOYMENT_JUDGE));
        }
    }

    public static final class Access19 extends MultipleAccess {
        public Access19() {
            super(
                    crud(MultipleRole.EMPLOYMENT_API),
                    crud(MultipleRole.ENGLAND_WALES_CASEWORKER),
                    crud(MultipleRole.ENGLAND_WALES_JUDGE),
                    cru(MultipleRole.EMPLOYMENT_JUDGE),
                    cru(MultipleRole.EMPLOYMENT_CASEWORKER),
                    read(MultipleRole.ACAS_API));
        }
    }

    public static final class Access20 extends MultipleAccess {
        public Access20() {
            super(
                    crud(MultipleRole.EMPLOYMENT_API),
                    crud(MultipleRole.ENGLAND_WALES_CASEWORKER),
                    crud(MultipleRole.ENGLAND_WALES_JUDGE),
                    cru(MultipleRole.EMPLOYMENT_JUDGE),
                    read(MultipleRole.EMPLOYMENT_CASEWORKER),
                    read(MultipleRole.ACAS_API));
        }
    }

    public static final class Access21 extends MultipleAccess {
        public Access21() {
            super(
                    crud(MultipleRole.EMPLOYMENT_API),
                    crud(MultipleRole.ENGLAND_WALES_CASEWORKER),
                    crud(MultipleRole.ENGLAND_WALES_JUDGE),
                    cru(MultipleRole.EMPLOYMENT_JUDGE),
                    read(MultipleRole.EMPLOYMENT_CASEWORKER));
        }
    }

    public static final class Access22 extends MultipleAccess {
        public Access22() {
            super(
                    crud(MultipleRole.EMPLOYMENT_API),
                    crud(MultipleRole.ENGLAND_WALES_CASEWORKER),
                    crud(MultipleRole.ENGLAND_WALES_JUDGE),
                    cru(MultipleRole.EMPLOYMENT_JUDGE),
                    delete(MultipleRole.LEGAL_REP),
                    cru(MultipleRole.EMPLOYMENT_CASEWORKER),
                    read(MultipleRole.ACAS_API));
        }
    }

    public static final class Access23 extends MultipleAccess {
        public Access23() {
            super(
                    crud(MultipleRole.EMPLOYMENT_API),
                    crud(MultipleRole.ENGLAND_WALES_CASEWORKER),
                    crud(MultipleRole.ENGLAND_WALES_JUDGE),
                    cru(MultipleRole.EMPLOYMENT_JUDGE),
                    delete(MultipleRole.LEGAL_REP),
                    read(MultipleRole.EMPLOYMENT_CASEWORKER),
                    read(MultipleRole.ACAS_API));
        }
    }

    public static final class Access24 extends MultipleAccess {
        public Access24() {
            super(
                    crud(MultipleRole.EMPLOYMENT_API),
                    crud(MultipleRole.ENGLAND_WALES_CASEWORKER),
                    crud(MultipleRole.ENGLAND_WALES_JUDGE),
                    cru(MultipleRole.EMPLOYMENT_JUDGE),
                    delete(MultipleRole.LEGAL_REP),
                    read(MultipleRole.EMPLOYMENT_CASEWORKER));
        }
    }

    public static final class Access25 extends MultipleAccess {
        public Access25() {
            super(
                    crud(MultipleRole.EMPLOYMENT_API),
                    crud(MultipleRole.ENGLAND_WALES_CASEWORKER),
                    crud(MultipleRole.ENGLAND_WALES_JUDGE),
                    crud(MultipleRole.EMPLOYMENT_JUDGE),
                    crud(MultipleRole.EMPLOYMENT_CASEWORKER));
        }
    }

    public static final class Access26 extends MultipleAccess {
        public Access26() {
            super(
                    crud(MultipleRole.EMPLOYMENT_API),
                    crud(MultipleRole.ENGLAND_WALES_CASEWORKER),
                    crud(MultipleRole.ENGLAND_WALES_JUDGE),
                    crud(MultipleRole.EMPLOYMENT_JUDGE),
                    delete(MultipleRole.LEGAL_REP),
                    crud(MultipleRole.EMPLOYMENT_CASEWORKER));
        }
    }

    public static final class Access27 extends MultipleAccess {
        public Access27() {
            super(
                    crud(MultipleRole.EMPLOYMENT_API),
                    crud(MultipleRole.ENGLAND_WALES_CASEWORKER),
                    crud(MultipleRole.ENGLAND_WALES_JUDGE),
                    read(MultipleRole.EMPLOYMENT_JUDGE));
        }
    }

    public static final class Access28 extends MultipleAccess {
        public Access28() {
            super(
                    crud(MultipleRole.EMPLOYMENT_API),
                    read(MultipleRole.EMPLOYMENT_JUDGE),
                    cru(MultipleRole.SCOTLAND_CASEWORKER),
                    read(MultipleRole.ACAS_API));
        }
    }

    public static final class Access29 extends MultipleAccess {
        public Access29() {
            super(
                    crud(MultipleRole.EMPLOYMENT_API),
                    cru(MultipleRole.SCOTLAND_JUDGE),
                    read(MultipleRole.EMPLOYMENT_JUDGE),
                    read(MultipleRole.LEGAL_REP),
                    cru(MultipleRole.SCOTLAND_CASEWORKER),
                    read(MultipleRole.WA_TASK_CONFIGURATION),
                    cru(MultipleRole.CITIZEN),
                    read(MultipleRole.ACAS_API));
        }
    }

    public static final class Access30 extends MultipleAccess {
        public Access30() {
            super(
                    crud(MultipleRole.EMPLOYMENT_API),
                    cru(MultipleRole.SCOTLAND_JUDGE),
                    read(MultipleRole.EMPLOYMENT_JUDGE),
                    read(MultipleRole.LEGAL_REP),
                    cru(MultipleRole.SCOTLAND_CASEWORKER),
                    read(MultipleRole.ACAS_API));
        }
    }

    public static final class Access31 extends MultipleAccess {
        public Access31() {
            super(
                    crud(MultipleRole.EMPLOYMENT_API),
                    cru(MultipleRole.SCOTLAND_JUDGE),
                    read(MultipleRole.EMPLOYMENT_JUDGE),
                    cru(MultipleRole.SCOTLAND_CASEWORKER),
                    read(MultipleRole.EMPLOYMENT_CASEWORKER),
                    cru(MultipleRole.CITIZEN),
                    read(MultipleRole.ACAS_API));
        }
    }

    public static final class Access32 extends MultipleAccess {
        public Access32() {
            super(
                    crud(MultipleRole.EMPLOYMENT_API),
                    cru(MultipleRole.SCOTLAND_JUDGE),
                    read(MultipleRole.EMPLOYMENT_JUDGE),
                    cru(MultipleRole.SCOTLAND_CASEWORKER),
                    read(MultipleRole.EMPLOYMENT_CASEWORKER),
                    cru(MultipleRole.CITIZEN));
        }
    }

    public static final class Access33 extends MultipleAccess {
        public Access33() {
            super(
                    crud(MultipleRole.EMPLOYMENT_API),
                    cru(MultipleRole.SCOTLAND_JUDGE),
                    read(MultipleRole.EMPLOYMENT_JUDGE),
                    cru(MultipleRole.SCOTLAND_CASEWORKER),
                    read(MultipleRole.EMPLOYMENT_CASEWORKER),
                    read(MultipleRole.ACAS_API));
        }
    }

    public static final class Access34 extends MultipleAccess {
        public Access34() {
            super(
                    crud(MultipleRole.EMPLOYMENT_API),
                    cru(MultipleRole.SCOTLAND_JUDGE),
                    read(MultipleRole.EMPLOYMENT_JUDGE),
                    cru(MultipleRole.SCOTLAND_CASEWORKER),
                    read(MultipleRole.EMPLOYMENT_CASEWORKER));
        }
    }

    public static final class Access35 extends MultipleAccess {
        public Access35() {
            super(
                    crud(MultipleRole.EMPLOYMENT_API),
                    cru(MultipleRole.SCOTLAND_JUDGE),
                    read(MultipleRole.EMPLOYMENT_JUDGE),
                    cru(MultipleRole.SCOTLAND_CASEWORKER),
                    read(MultipleRole.WA_TASK_CONFIGURATION),
                    read(MultipleRole.ACAS_API));
        }
    }

    public static final class Access36 extends MultipleAccess {
        public Access36() {
            super(
                    crud(MultipleRole.EMPLOYMENT_API),
                    cru(MultipleRole.SCOTLAND_JUDGE),
                    read(MultipleRole.EMPLOYMENT_JUDGE),
                    cru(MultipleRole.SCOTLAND_CASEWORKER),
                    cru(MultipleRole.CITIZEN),
                    read(MultipleRole.ACAS_API));
        }
    }

    public static final class Access37 extends MultipleAccess {
        public Access37() {
            super(
                    crud(MultipleRole.EMPLOYMENT_API),
                    cru(MultipleRole.SCOTLAND_JUDGE),
                    read(MultipleRole.EMPLOYMENT_JUDGE),
                    cru(MultipleRole.SCOTLAND_CASEWORKER));
        }
    }

    public static final class Access38 extends MultipleAccess {
        public Access38() {
            super(
                    crud(MultipleRole.EMPLOYMENT_API),
                    cru(MultipleRole.SCOTLAND_JUDGE),
                    crud(MultipleRole.LEGAL_REP),
                    cru(MultipleRole.SCOTLAND_CASEWORKER),
                    cru(MultipleRole.CITIZEN));
        }
    }

    public static final class Access39 extends MultipleAccess {
        public Access39() {
            super(
                    crud(MultipleRole.EMPLOYMENT_API),
                    cru(MultipleRole.SCOTLAND_JUDGE),
                    read(MultipleRole.LEGAL_REP),
                    cru(MultipleRole.SCOTLAND_CASEWORKER),
                    read(MultipleRole.WA_TASK_CONFIGURATION),
                    cru(MultipleRole.CITIZEN));
        }
    }

    public static final class Access40 extends MultipleAccess {
        public Access40() {
            super(
                    crud(MultipleRole.EMPLOYMENT_API),
                    cru(MultipleRole.SCOTLAND_JUDGE),
                    read(MultipleRole.LEGAL_REP),
                    cru(MultipleRole.SCOTLAND_CASEWORKER),
                    cru(MultipleRole.CITIZEN));
        }
    }

    public static final class Access41 extends MultipleAccess {
        public Access41() {
            super(
                    crud(MultipleRole.EMPLOYMENT_API),
                    cru(MultipleRole.SCOTLAND_JUDGE),
                    cru(MultipleRole.SCOTLAND_CASEWORKER),
                    cru(MultipleRole.CITIZEN));
        }
    }

    public static final class Access42 extends MultipleAccess {
        public Access42() {
            super(
                    crud(MultipleRole.EMPLOYMENT_API),
                    cru(MultipleRole.SCOTLAND_JUDGE),
                    cru(MultipleRole.SCOTLAND_CASEWORKER));
        }
    }

    public static final class Access43 extends MultipleAccess {
        public Access43() {
            super(
                    crud(MultipleRole.EMPLOYMENT_API),
                    crud(MultipleRole.SCOTLAND_JUDGE),
                    cru(MultipleRole.EMPLOYMENT_JUDGE),
                    delete(MultipleRole.LEGAL_REP),
                    crud(MultipleRole.SCOTLAND_CASEWORKER),
                    cru(MultipleRole.EMPLOYMENT_CASEWORKER),
                    read(MultipleRole.ACAS_API));
        }
    }

    public static final class Access44 extends MultipleAccess {
        public Access44() {
            super(
                    crud(MultipleRole.EMPLOYMENT_API),
                    crud(MultipleRole.SCOTLAND_JUDGE),
                    cru(MultipleRole.EMPLOYMENT_JUDGE),
                    delete(MultipleRole.LEGAL_REP),
                    crud(MultipleRole.SCOTLAND_CASEWORKER),
                    read(MultipleRole.EMPLOYMENT_CASEWORKER),
                    read(MultipleRole.ACAS_API));
        }
    }

    public static final class Access45 extends MultipleAccess {
        public Access45() {
            super(
                    crud(MultipleRole.EMPLOYMENT_API),
                    crud(MultipleRole.SCOTLAND_JUDGE),
                    cru(MultipleRole.EMPLOYMENT_JUDGE),
                    delete(MultipleRole.LEGAL_REP),
                    crud(MultipleRole.SCOTLAND_CASEWORKER),
                    read(MultipleRole.EMPLOYMENT_CASEWORKER));
        }
    }

    public static final class Access46 extends MultipleAccess {
        public Access46() {
            super(
                    crud(MultipleRole.EMPLOYMENT_API),
                    crud(MultipleRole.SCOTLAND_JUDGE),
                    cru(MultipleRole.EMPLOYMENT_JUDGE),
                    crud(MultipleRole.SCOTLAND_CASEWORKER),
                    cru(MultipleRole.EMPLOYMENT_CASEWORKER),
                    read(MultipleRole.ACAS_API));
        }
    }

    public static final class Access47 extends MultipleAccess {
        public Access47() {
            super(
                    crud(MultipleRole.EMPLOYMENT_API),
                    crud(MultipleRole.SCOTLAND_JUDGE),
                    cru(MultipleRole.EMPLOYMENT_JUDGE),
                    crud(MultipleRole.SCOTLAND_CASEWORKER),
                    read(MultipleRole.EMPLOYMENT_CASEWORKER),
                    read(MultipleRole.ACAS_API));
        }
    }

    public static final class Access48 extends MultipleAccess {
        public Access48() {
            super(
                    crud(MultipleRole.EMPLOYMENT_API),
                    crud(MultipleRole.SCOTLAND_JUDGE),
                    cru(MultipleRole.EMPLOYMENT_JUDGE),
                    crud(MultipleRole.SCOTLAND_CASEWORKER),
                    read(MultipleRole.EMPLOYMENT_CASEWORKER));
        }
    }

    public static final class Access49 extends MultipleAccess {
        public Access49() {
            super(
                    crud(MultipleRole.EMPLOYMENT_API),
                    crud(MultipleRole.SCOTLAND_JUDGE),
                    crud(MultipleRole.EMPLOYMENT_JUDGE),
                    delete(MultipleRole.LEGAL_REP),
                    crud(MultipleRole.SCOTLAND_CASEWORKER),
                    crud(MultipleRole.EMPLOYMENT_CASEWORKER));
        }
    }

    public static final class Access50 extends MultipleAccess {
        public Access50() {
            super(
                    crud(MultipleRole.EMPLOYMENT_API),
                    crud(MultipleRole.SCOTLAND_JUDGE),
                    crud(MultipleRole.EMPLOYMENT_JUDGE),
                    crud(MultipleRole.SCOTLAND_CASEWORKER),
                    crud(MultipleRole.EMPLOYMENT_CASEWORKER));
        }
    }

    public static final class Access51 extends MultipleAccess {
        public Access51() {
            super(
                    crud(MultipleRole.EMPLOYMENT_API),
                    crud(MultipleRole.SCOTLAND_JUDGE),
                    cru(MultipleRole.LEGAL_REP),
                    cru(MultipleRole.SCOTLAND_CASEWORKER));
        }
    }

    public static final class Access52 extends MultipleAccess {
        public Access52() {
            super(
                    crud(MultipleRole.EMPLOYMENT_API),
                    crud(MultipleRole.SCOTLAND_JUDGE),
                    cru(MultipleRole.SCOTLAND_CASEWORKER));
        }
    }

    public static final class Access53 extends MultipleAccess {
        public Access53() {
            super(
                    crud(MultipleRole.EMPLOYMENT_API),
                    read(MultipleRole.SCOTLAND_JUDGE),
                    read(MultipleRole.EMPLOYMENT_JUDGE),
                    cru(MultipleRole.SCOTLAND_CASEWORKER));
        }
    }

    public static final class Access54 extends MultipleAccess {
        public Access54() {
            super(
                    crud(MultipleRole.EMPLOYMENT_API),
                    read(MultipleRole.RAS_VALIDATION),
                    read(MultipleRole.WA_TASK_CONFIGURATION),
                    read(MultipleRole.GS_PROFILE));
        }
    }

    public static final class Access55 extends MultipleAccess {
        public Access55() {
            super(crud(MultipleRole.EMPLOYMENT_API));
        }
    }

    public static final class Access56 extends MultipleAccess {
        public Access56() {
            super(
                    read(MultipleRole.EMPLOYMENT_API),
                    read(MultipleRole.ENGLAND_WALES_CASEWORKER),
                    read(MultipleRole.ENGLAND_WALES_JUDGE),
                    read(MultipleRole.EMPLOYMENT_JUDGE),
                    read(MultipleRole.EMPLOYMENT_CASEWORKER));
        }
    }

    public static final class Access57 extends MultipleAccess {
        public Access57() {
            super(
                    read(MultipleRole.EMPLOYMENT_API),
                    read(MultipleRole.ENGLAND_WALES_CASEWORKER),
                    read(MultipleRole.ENGLAND_WALES_JUDGE),
                    read(MultipleRole.EMPLOYMENT_JUDGE));
        }
    }

    public static final class Access58 extends MultipleAccess {
        public Access58() {
            super(
                    read(MultipleRole.EMPLOYMENT_API),
                    read(MultipleRole.SCOTLAND_JUDGE),
                    read(MultipleRole.EMPLOYMENT_JUDGE),
                    read(MultipleRole.SCOTLAND_CASEWORKER),
                    read(MultipleRole.EMPLOYMENT_CASEWORKER));
        }
    }

    public static final class Access59 extends MultipleAccess {
        public Access59() {
            super(
                    read(MultipleRole.EMPLOYMENT_API),
                    read(MultipleRole.SCOTLAND_JUDGE),
                    read(MultipleRole.EMPLOYMENT_JUDGE),
                    read(MultipleRole.SCOTLAND_CASEWORKER));
        }
    }

    public static final class Access60 extends MultipleAccess {
        public Access60() {
            super(
                    read(MultipleRole.EMPLOYMENT_API),
                    read(MultipleRole.SCOTLAND_JUDGE),
                    read(MultipleRole.SCOTLAND_CASEWORKER),
                    read(MultipleRole.CITIZEN));
        }
    }

    public static final class Access61 extends MultipleAccess {
        public Access61() {
            super(
                    read(MultipleRole.EMPLOYMENT_API),
                    delete(MultipleRole.LEGAL_REP),
                    read(MultipleRole.ACAS_API));
        }
    }
}
