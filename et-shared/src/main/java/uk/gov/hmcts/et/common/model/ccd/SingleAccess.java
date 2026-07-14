package uk.gov.hmcts.et.common.model.ccd;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import uk.gov.hmcts.ccd.sdk.api.HasAccessControl;
import uk.gov.hmcts.ccd.sdk.api.HasRole;
import uk.gov.hmcts.ccd.sdk.api.Permission;

public abstract class SingleAccess implements HasAccessControl {
    private final Object[][] grants;

    protected SingleAccess(Object[]... grants) {
        this.grants = grants;
    }

    protected static Object[] grant(SingleRole role, String crud) {
        return new Object[] {role, crud};
    }

    @Override
    public SetMultimap<HasRole, Permission> getGrants() {
        SetMultimap<HasRole, Permission> result = HashMultimap.create();
        for (Object[] grant : grants) {
            for (char permission : ((String) grant[1]).toCharArray()) {
                result.put((SingleRole) grant[0], Permission.valueOf(String.valueOf(permission)));
            }
        }
        return result;
    }

    public static final class Access001 extends SingleAccess {
        public Access001() {
            super(grant(SingleRole.CLAIMANTSOLICITOR, "CRU"));
        }
    }

    public static final class Access002 extends SingleAccess {
        public Access002() {
            super(
                    grant(SingleRole.CLAIMANTSOLICITOR, "CRUD"),
                    grant(SingleRole.CREATOR, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"));
        }
    }

    public static final class Access003 extends SingleAccess {
        public Access003() {
            super(
                    grant(SingleRole.CLAIMANTSOLICITOR, "CRUD"),
                    grant(SingleRole.SOLICITORA, "CRU"),
                    grant(SingleRole.SOLICITORB, "CRU"),
                    grant(SingleRole.SOLICITORC, "CRU"),
                    grant(SingleRole.SOLICITORD, "CRU"),
                    grant(SingleRole.SOLICITORE, "CRU"),
                    grant(SingleRole.SOLICITORF, "CRU"),
                    grant(SingleRole.SOLICITORG, "CRU"),
                    grant(SingleRole.SOLICITORH, "CRU"),
                    grant(SingleRole.SOLICITORI, "CRU"),
                    grant(SingleRole.SOLICITORJ, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access004 extends SingleAccess {
        public Access004() {
            super(
                    grant(SingleRole.CLAIMANTSOLICITOR, "CRUD"),
                    grant(SingleRole.SOLICITORA, "CRUD"),
                    grant(SingleRole.SOLICITORB, "CRUD"),
                    grant(SingleRole.SOLICITORC, "CRUD"),
                    grant(SingleRole.SOLICITORD, "CRUD"),
                    grant(SingleRole.SOLICITORE, "CRUD"),
                    grant(SingleRole.SOLICITORF, "CRUD"),
                    grant(SingleRole.SOLICITORG, "CRUD"),
                    grant(SingleRole.SOLICITORH, "CRUD"),
                    grant(SingleRole.SOLICITORI, "CRUD"),
                    grant(SingleRole.SOLICITORJ, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"));
        }
    }

    public static final class Access005 extends SingleAccess {
        public Access005() {
            super(
                    grant(SingleRole.CLAIMANTSOLICITOR, "CRUD"),
                    grant(SingleRole.SOLICITORA, "D"),
                    grant(SingleRole.SOLICITORB, "D"),
                    grant(SingleRole.SOLICITORC, "D"),
                    grant(SingleRole.SOLICITORD, "D"),
                    grant(SingleRole.SOLICITORE, "D"),
                    grant(SingleRole.SOLICITORF, "D"),
                    grant(SingleRole.SOLICITORG, "D"),
                    grant(SingleRole.SOLICITORH, "D"),
                    grant(SingleRole.SOLICITORI, "D"),
                    grant(SingleRole.SOLICITORJ, "D"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"));
        }
    }

    public static final class Access006 extends SingleAccess {
        public Access006() {
            super(
                    grant(SingleRole.CLAIMANTSOLICITOR, "CRUD"),
                    grant(SingleRole.CASEWORKER_CAA, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CITIZEN, "CRUD"));
        }
    }

    public static final class Access007 extends SingleAccess {
        public Access007() {
            super(
                    grant(SingleRole.CLAIMANTSOLICITOR, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"));
        }
    }

    public static final class Access008 extends SingleAccess {
        public Access008() {
            super(grant(SingleRole.CLAIMANTSOLICITOR, "CRUD"));
        }
    }

    public static final class Access009 extends SingleAccess {
        public Access009() {
            super(
                    grant(SingleRole.CLAIMANTSOLICITOR, "CUD"),
                    grant(SingleRole.SOLICITORA, "CUD"),
                    grant(SingleRole.SOLICITORB, "CUD"),
                    grant(SingleRole.SOLICITORC, "CUD"),
                    grant(SingleRole.SOLICITORD, "CUD"),
                    grant(SingleRole.SOLICITORE, "CUD"),
                    grant(SingleRole.SOLICITORF, "CUD"),
                    grant(SingleRole.SOLICITORG, "CUD"),
                    grant(SingleRole.SOLICITORH, "CUD"),
                    grant(SingleRole.SOLICITORI, "CUD"),
                    grant(SingleRole.SOLICITORJ, "CUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "CUD"),
                    grant(SingleRole.CITIZEN, "CRUD"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access010 extends SingleAccess {
        public Access010() {
            super(
                    grant(SingleRole.CLAIMANTSOLICITOR, "CUD"),
                    grant(SingleRole.SOLICITORA, "CUD"),
                    grant(SingleRole.SOLICITORB, "CUD"),
                    grant(SingleRole.SOLICITORC, "CUD"),
                    grant(SingleRole.SOLICITORD, "CUD"),
                    grant(SingleRole.SOLICITORE, "CUD"),
                    grant(SingleRole.SOLICITORF, "CUD"),
                    grant(SingleRole.SOLICITORG, "CUD"),
                    grant(SingleRole.SOLICITORH, "CUD"),
                    grant(SingleRole.SOLICITORI, "CUD"),
                    grant(SingleRole.SOLICITORJ, "CUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "CUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRU"),
                    grant(SingleRole.CITIZEN, "CRUD"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access011 extends SingleAccess {
        public Access011() {
            super(
                    grant(SingleRole.CLAIMANTSOLICITOR, "D"),
                    grant(SingleRole.SOLICITORA, "CRU"),
                    grant(SingleRole.SOLICITORB, "CRU"),
                    grant(SingleRole.SOLICITORC, "CRU"),
                    grant(SingleRole.SOLICITORD, "CRU"),
                    grant(SingleRole.SOLICITORE, "CRU"),
                    grant(SingleRole.SOLICITORF, "CRU"),
                    grant(SingleRole.SOLICITORG, "CRU"),
                    grant(SingleRole.SOLICITORH, "CRU"),
                    grant(SingleRole.SOLICITORI, "CRU"),
                    grant(SingleRole.SOLICITORJ, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access012 extends SingleAccess {
        public Access012() {
            super(
                    grant(SingleRole.CLAIMANTSOLICITOR, "D"),
                    grant(SingleRole.SOLICITORA, "CRUD"),
                    grant(SingleRole.SOLICITORB, "CRUD"),
                    grant(SingleRole.SOLICITORC, "CRUD"),
                    grant(SingleRole.SOLICITORD, "CRUD"),
                    grant(SingleRole.SOLICITORE, "CRUD"),
                    grant(SingleRole.SOLICITORF, "CRUD"),
                    grant(SingleRole.SOLICITORG, "CRUD"),
                    grant(SingleRole.SOLICITORH, "CRUD"),
                    grant(SingleRole.SOLICITORI, "CRUD"),
                    grant(SingleRole.SOLICITORJ, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"));
        }
    }

    public static final class Access013 extends SingleAccess {
        public Access013() {
            super(
                    grant(SingleRole.CREATOR, "CRUD"),
                    grant(SingleRole.DEFENDANT, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRUD"));
        }
    }

    public static final class Access014 extends SingleAccess {
        public Access014() {
            super(
                    grant(SingleRole.CREATOR, "CRUD"),
                    grant(SingleRole.DEFENDANT, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRUD"));
        }
    }

    public static final class Access015 extends SingleAccess {
        public Access015() {
            super(
                    grant(SingleRole.CREATOR, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access016 extends SingleAccess {
        public Access016() {
            super(
                    grant(SingleRole.DEFENDANT, "CRUD"),
                    grant(SingleRole.CASEWORKER_APPROVER, "CRUD"),
                    grant(SingleRole.CASEWORKER_CAA, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "CRUD"));
        }
    }

    public static final class Access017 extends SingleAccess {
        public Access017() {
            super(
                    grant(SingleRole.DEFENDANT, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "CRU"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access018 extends SingleAccess {
        public Access018() {
            super(
                    grant(SingleRole.DEFENDANT, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"));
        }
    }

    public static final class Access019 extends SingleAccess {
        public Access019() {
            super(
                    grant(SingleRole.SOLICITORA, "CRU"),
                    grant(SingleRole.SOLICITORB, "CRU"),
                    grant(SingleRole.SOLICITORC, "CRU"),
                    grant(SingleRole.SOLICITORD, "CRU"),
                    grant(SingleRole.SOLICITORE, "CRU"),
                    grant(SingleRole.SOLICITORF, "CRU"),
                    grant(SingleRole.SOLICITORG, "CRU"),
                    grant(SingleRole.SOLICITORH, "CRU"),
                    grant(SingleRole.SOLICITORI, "CRU"),
                    grant(SingleRole.SOLICITORJ, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRU"),
                    grant(SingleRole.CITIZEN, "CRU"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access020 extends SingleAccess {
        public Access020() {
            super(
                    grant(SingleRole.SOLICITORA, "CRU"),
                    grant(SingleRole.SOLICITORB, "CRU"),
                    grant(SingleRole.SOLICITORC, "CRU"),
                    grant(SingleRole.SOLICITORD, "CRU"),
                    grant(SingleRole.SOLICITORE, "CRU"),
                    grant(SingleRole.SOLICITORF, "CRU"),
                    grant(SingleRole.SOLICITORG, "CRU"),
                    grant(SingleRole.SOLICITORH, "CRU"),
                    grant(SingleRole.SOLICITORI, "CRU"),
                    grant(SingleRole.SOLICITORJ, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRU"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access021 extends SingleAccess {
        public Access021() {
            super(
                    grant(SingleRole.SOLICITORA, "CRU"),
                    grant(SingleRole.SOLICITORB, "CRU"),
                    grant(SingleRole.SOLICITORC, "CRU"),
                    grant(SingleRole.SOLICITORD, "CRU"),
                    grant(SingleRole.SOLICITORE, "CRU"),
                    grant(SingleRole.SOLICITORF, "CRU"),
                    grant(SingleRole.SOLICITORG, "CRU"),
                    grant(SingleRole.SOLICITORH, "CRU"),
                    grant(SingleRole.SOLICITORI, "CRU"),
                    grant(SingleRole.SOLICITORJ, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRU"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access022 extends SingleAccess {
        public Access022() {
            super(
                    grant(SingleRole.SOLICITORA, "R"),
                    grant(SingleRole.SOLICITORB, "CRU"),
                    grant(SingleRole.SOLICITORC, "CRU"),
                    grant(SingleRole.SOLICITORD, "CRU"),
                    grant(SingleRole.SOLICITORE, "CRU"),
                    grant(SingleRole.SOLICITORF, "CRU"),
                    grant(SingleRole.SOLICITORG, "CRU"),
                    grant(SingleRole.SOLICITORH, "CRU"),
                    grant(SingleRole.SOLICITORI, "CRU"),
                    grant(SingleRole.SOLICITORJ, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "R"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access023 extends SingleAccess {
        public Access023() {
            super(
                    grant(SingleRole.SOLICITORA, "R"),
                    grant(SingleRole.SOLICITORB, "R"),
                    grant(SingleRole.SOLICITORC, "R"),
                    grant(SingleRole.SOLICITORD, "R"),
                    grant(SingleRole.SOLICITORE, "R"),
                    grant(SingleRole.SOLICITORF, "R"),
                    grant(SingleRole.SOLICITORG, "R"),
                    grant(SingleRole.SOLICITORH, "R"),
                    grant(SingleRole.SOLICITORI, "R"),
                    grant(SingleRole.SOLICITORJ, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "R"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access024 extends SingleAccess {
        public Access024() {
            super(
                    grant(SingleRole.CASEWORKER_APPROVER, "CRU"),
                    grant(SingleRole.CASEWORKER_CAA, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRU"));
        }
    }

    public static final class Access025 extends SingleAccess {
        public Access025() {
            super(
                    grant(SingleRole.CASEWORKER_APPROVER, "CRUD"),
                    grant(SingleRole.CASEWORKER_CAA, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "CRUD"),
                    grant(SingleRole.CITIZEN, "R"));
        }
    }

    public static final class Access026 extends SingleAccess {
        public Access026() {
            super(
                    grant(SingleRole.CASEWORKER_APPROVER, "CRUD"),
                    grant(SingleRole.CASEWORKER_CAA, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "CRUD"));
        }
    }

    public static final class Access027 extends SingleAccess {
        public Access027() {
            super(
                    grant(SingleRole.CASEWORKER_CAA, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "R"),
                    grant(SingleRole.CASEWORKER_WA_TASK_CONFIGURATION, "R"),
                    grant(SingleRole.CITIZEN, "CRU"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access028 extends SingleAccess {
        public Access028() {
            super(
                    grant(SingleRole.CASEWORKER_CAA, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRU"),
                    grant(SingleRole.CASEWORKER_WA_TASK_CONFIGURATION, "R"),
                    grant(SingleRole.CITIZEN, "CRU"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access029 extends SingleAccess {
        public Access029() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "D"),
                    grant(SingleRole.CASEWORKER_WA_TASK_CONFIGURATION, "R"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access030 extends SingleAccess {
        public Access030() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "D"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access031 extends SingleAccess {
        public Access031() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRUD"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access032 extends SingleAccess {
        public Access032() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRUD"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access033 extends SingleAccess {
        public Access033() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "CRUD"),
                    grant(SingleRole.CASEWORKER_WA_TASK_CONFIGURATION, "R"),
                    grant(SingleRole.CITIZEN, "CRUD"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access034 extends SingleAccess {
        public Access034() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "CRUD"),
                    grant(SingleRole.CITIZEN, "CRUD"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access035 extends SingleAccess {
        public Access035() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "D"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access036 extends SingleAccess {
        public Access036() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "D"));
        }
    }

    public static final class Access037 extends SingleAccess {
        public Access037() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRUD"));
        }
    }

    public static final class Access038 extends SingleAccess {
        public Access038() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "D"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRUD"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access039 extends SingleAccess {
        public Access039() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "D"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRUD"));
        }
    }

    public static final class Access040 extends SingleAccess {
        public Access040() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRUD"));
        }
    }

    public static final class Access041 extends SingleAccess {
        public Access041() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRU"),
                    grant(SingleRole.CASEWORKER_WA_TASK_CONFIGURATION, "R"),
                    grant(SingleRole.CITIZEN, "CRUD"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access042 extends SingleAccess {
        public Access042() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRU"),
                    grant(SingleRole.CITIZEN, "CRUD"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access043 extends SingleAccess {
        public Access043() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "D"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRUD"));
        }
    }

    public static final class Access044 extends SingleAccess {
        public Access044() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "D"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access045 extends SingleAccess {
        public Access045() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "D"));
        }
    }

    public static final class Access046 extends SingleAccess {
        public Access046() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "D"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRU"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access047 extends SingleAccess {
        public Access047() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "D"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRU"));
        }
    }

    public static final class Access048 extends SingleAccess {
        public Access048() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "CRU"),
                    grant(SingleRole.CITIZEN, "CRU"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access049 extends SingleAccess {
        public Access049() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "CRU"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access050 extends SingleAccess {
        public Access050() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "CRUD"),
                    grant(SingleRole.CITIZEN, "CRU"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access051 extends SingleAccess {
        public Access051() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "D"),
                    grant(SingleRole.CASEWORKER_WA_TASK_CONFIGURATION, "R"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access052 extends SingleAccess {
        public Access052() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "D"),
                    grant(SingleRole.CITIZEN, "CRU"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access053 extends SingleAccess {
        public Access053() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "D"),
                    grant(SingleRole.CITIZEN, "CRU"));
        }
    }

    public static final class Access054 extends SingleAccess {
        public Access054() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "D"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access055 extends SingleAccess {
        public Access055() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "R"),
                    grant(SingleRole.CASEWORKER_WA_TASK_CONFIGURATION, "R"),
                    grant(SingleRole.CITIZEN, "CRU"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access056 extends SingleAccess {
        public Access056() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "R"),
                    grant(SingleRole.CITIZEN, "CRU"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access057 extends SingleAccess {
        public Access057() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRU"),
                    grant(SingleRole.CITIZEN, "CRU"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access058 extends SingleAccess {
        public Access058() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRU"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access059 extends SingleAccess {
        public Access059() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRU"));
        }
    }

    public static final class Access060 extends SingleAccess {
        public Access060() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "CRU"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access061 extends SingleAccess {
        public Access061() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "CRU"));
        }
    }

    public static final class Access062 extends SingleAccess {
        public Access062() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "D"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access063 extends SingleAccess {
        public Access063() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "D"));
        }
    }

    public static final class Access064 extends SingleAccess {
        public Access064() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "CRU"),
                    grant(SingleRole.CITIZEN, "CRU"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access065 extends SingleAccess {
        public Access065() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "CRU"),
                    grant(SingleRole.CITIZEN, "CRUD"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access066 extends SingleAccess {
        public Access066() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "CRUD"),
                    grant(SingleRole.CASEWORKER_WA_TASK_CONFIGURATION, "R"),
                    grant(SingleRole.CITIZEN, "CRU"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access067 extends SingleAccess {
        public Access067() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "D"),
                    grant(SingleRole.CITIZEN, "CRU"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access068 extends SingleAccess {
        public Access068() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "D"),
                    grant(SingleRole.CITIZEN, "CRUD"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access069 extends SingleAccess {
        public Access069() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "D"),
                    grant(SingleRole.CITIZEN, "CRUD"));
        }
    }

    public static final class Access070 extends SingleAccess {
        public Access070() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "D"),
                    grant(SingleRole.CITIZEN, "R"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access071 extends SingleAccess {
        public Access071() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "D"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access072 extends SingleAccess {
        public Access072() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "D"));
        }
    }

    public static final class Access073 extends SingleAccess {
        public Access073() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "R"),
                    grant(SingleRole.CITIZEN, "CRUD"));
        }
    }

    public static final class Access074 extends SingleAccess {
        public Access074() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "R"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access075 extends SingleAccess {
        public Access075() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "R"));
        }
    }

    public static final class Access076 extends SingleAccess {
        public Access076() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRUD"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access077 extends SingleAccess {
        public Access077() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRUD"));
        }
    }

    public static final class Access078 extends SingleAccess {
        public Access078() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "D"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access079 extends SingleAccess {
        public Access079() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRUD"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access080 extends SingleAccess {
        public Access080() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRUD"));
        }
    }

    public static final class Access081 extends SingleAccess {
        public Access081() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRUD"),
                    grant(SingleRole.CASEWORKER_WA_TASK_CONFIGURATION, "R"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access082 extends SingleAccess {
        public Access082() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRUD"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access083 extends SingleAccess {
        public Access083() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRUD"));
        }
    }

    public static final class Access084 extends SingleAccess {
        public Access084() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRUD"));
        }
    }

    public static final class Access085 extends SingleAccess {
        public Access085() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRU"),
                    grant(SingleRole.CITIZEN, "CRU"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access086 extends SingleAccess {
        public Access086() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRU"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access087 extends SingleAccess {
        public Access087() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRU"),
                    grant(SingleRole.CITIZEN, "CRU"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access088 extends SingleAccess {
        public Access088() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "D"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRU"),
                    grant(SingleRole.CASEWORKER_WA_TASK_CONFIGURATION, "R"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access089 extends SingleAccess {
        public Access089() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "D"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRU"),
                    grant(SingleRole.CITIZEN, "CRU"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access090 extends SingleAccess {
        public Access090() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "D"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRU"),
                    grant(SingleRole.CITIZEN, "CRU"));
        }
    }

    public static final class Access091 extends SingleAccess {
        public Access091() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "D"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRU"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access092 extends SingleAccess {
        public Access092() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRU"),
                    grant(SingleRole.CASEWORKER_WA_TASK_CONFIGURATION, "R"),
                    grant(SingleRole.CITIZEN, "CRU"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access093 extends SingleAccess {
        public Access093() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRU"),
                    grant(SingleRole.CITIZEN, "CRU"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access094 extends SingleAccess {
        public Access094() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRU"),
                    grant(SingleRole.CITIZEN, "CRU"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access095 extends SingleAccess {
        public Access095() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRU"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access096 extends SingleAccess {
        public Access096() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRU"));
        }
    }

    public static final class Access097 extends SingleAccess {
        public Access097() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRUD"),
                    grant(SingleRole.CITIZEN, "CRU"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access098 extends SingleAccess {
        public Access098() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRUD"),
                    grant(SingleRole.CITIZEN, "CRUD"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access099 extends SingleAccess {
        public Access099() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRUD"),
                    grant(SingleRole.CITIZEN, "CRUD"));
        }
    }

    public static final class Access100 extends SingleAccess {
        public Access100() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRUD"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access101 extends SingleAccess {
        public Access101() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRUD"),
                    grant(SingleRole.CASEWORKER_WA_TASK_CONFIGURATION, "R"),
                    grant(SingleRole.CITIZEN, "CRU"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access102 extends SingleAccess {
        public Access102() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "D"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRUD"),
                    grant(SingleRole.CITIZEN, "CRUD"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access103 extends SingleAccess {
        public Access103() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "D"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRUD"),
                    grant(SingleRole.CITIZEN, "CRUD"));
        }
    }

    public static final class Access104 extends SingleAccess {
        public Access104() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "D"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRUD"),
                    grant(SingleRole.CITIZEN, "R"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access105 extends SingleAccess {
        public Access105() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "D"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRUD"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access106 extends SingleAccess {
        public Access106() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "D"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRUD"));
        }
    }

    public static final class Access107 extends SingleAccess {
        public Access107() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRUD"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access108 extends SingleAccess {
        public Access108() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRUD"));
        }
    }

    public static final class Access109 extends SingleAccess {
        public Access109() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRUD"));
        }
    }

    public static final class Access110 extends SingleAccess {
        public Access110() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "R"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access111 extends SingleAccess {
        public Access111() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "D"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRU"),
                    grant(SingleRole.CITIZEN, "CRU"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access112 extends SingleAccess {
        public Access112() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "CRU"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access113 extends SingleAccess {
        public Access113() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "CRU"));
        }
    }

    public static final class Access114 extends SingleAccess {
        public Access114() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"));
        }
    }

    public static final class Access115 extends SingleAccess {
        public Access115() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "D"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access116 extends SingleAccess {
        public Access116() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "D"));
        }
    }

    public static final class Access117 extends SingleAccess {
        public Access117() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "R"));
        }
    }

    public static final class Access118 extends SingleAccess {
        public Access118() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "R"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access119 extends SingleAccess {
        public Access119() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "D"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "R"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access120 extends SingleAccess {
        public Access120() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "D"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "R"));
        }
    }

    public static final class Access121 extends SingleAccess {
        public Access121() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "R"));
        }
    }

    public static final class Access122 extends SingleAccess {
        public Access122() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"));
        }
    }

    public static final class Access123 extends SingleAccess {
        public Access123() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRU"));
        }
    }

    public static final class Access124 extends SingleAccess {
        public Access124() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CITIZEN, "CRU"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access125 extends SingleAccess {
        public Access125() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CITIZEN, "CRU"));
        }
    }

    public static final class Access126 extends SingleAccess {
        public Access126() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRUD"));
        }
    }

    public static final class Access127 extends SingleAccess {
        public Access127() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRU"));
        }
    }

    public static final class Access128 extends SingleAccess {
        public Access128() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRU"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access129 extends SingleAccess {
        public Access129() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRU"));
        }
    }

    public static final class Access130 extends SingleAccess {
        public Access130() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRU"));
        }
    }

    public static final class Access131 extends SingleAccess {
        public Access131() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRU"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access132 extends SingleAccess {
        public Access132() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRU"));
        }
    }

    public static final class Access133 extends SingleAccess {
        public Access133() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRU"));
        }
    }

    public static final class Access134 extends SingleAccess {
        public Access134() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "CRU"));
        }
    }

    public static final class Access135 extends SingleAccess {
        public Access135() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "CRU"),
                    grant(SingleRole.CASEWORKER_WA_TASK_CONFIGURATION, "R"),
                    grant(SingleRole.CITIZEN, "CRU"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access136 extends SingleAccess {
        public Access136() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "D"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access137 extends SingleAccess {
        public Access137() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "R"),
                    grant(SingleRole.CITIZEN, "CRU"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access138 extends SingleAccess {
        public Access138() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "R"),
                    grant(SingleRole.CITIZEN, "CRU"));
        }
    }

    public static final class Access139 extends SingleAccess {
        public Access139() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRU"),
                    grant(SingleRole.CASEWORKER_WA_TASK_CONFIGURATION, "R"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access140 extends SingleAccess {
        public Access140() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRU"),
                    grant(SingleRole.CASEWORKER_WA_TASK_CONFIGURATION, "R"));
        }
    }

    public static final class Access141 extends SingleAccess {
        public Access141() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRU"),
                    grant(SingleRole.CITIZEN, "CRU"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access142 extends SingleAccess {
        public Access142() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRU"),
                    grant(SingleRole.CITIZEN, "CRU"));
        }
    }

    public static final class Access143 extends SingleAccess {
        public Access143() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRU"),
                    grant(SingleRole.CITIZEN, "CRUD"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access144 extends SingleAccess {
        public Access144() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRU"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access145 extends SingleAccess {
        public Access145() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRU"));
        }
    }

    public static final class Access146 extends SingleAccess {
        public Access146() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "R"));
        }
    }

    public static final class Access147 extends SingleAccess {
        public Access147() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "D"),
                    grant(SingleRole.CITIZEN, "CRU"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access148 extends SingleAccess {
        public Access148() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRU"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access149 extends SingleAccess {
        public Access149() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "R"));
        }
    }

    public static final class Access150 extends SingleAccess {
        public Access150() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRU"));
        }
    }

    public static final class Access151 extends SingleAccess {
        public Access151() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRUD"),
                    grant(SingleRole.GS_PROFILE, "R"));
        }
    }

    public static final class Access152 extends SingleAccess {
        public Access152() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRUD"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access153 extends SingleAccess {
        public Access153() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRUD"));
        }
    }

    public static final class Access154 extends SingleAccess {
        public Access154() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "R"),
                    grant(SingleRole.CASEWORKER_WA_TASK_CONFIGURATION, "R"),
                    grant(SingleRole.CITIZEN, "CRUD"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access155 extends SingleAccess {
        public Access155() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "R"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access156 extends SingleAccess {
        public Access156() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRUD"),
                    grant(SingleRole.CITIZEN, "CRUD"));
        }
    }

    public static final class Access157 extends SingleAccess {
        public Access157() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRUD"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access158 extends SingleAccess {
        public Access158() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRUD"));
        }
    }

    public static final class Access159 extends SingleAccess {
        public Access159() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRUD"),
                    grant(SingleRole.CASEWORKER_WA_TASK_CONFIGURATION, "R"));
        }
    }

    public static final class Access160 extends SingleAccess {
        public Access160() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRUD"));
        }
    }

    public static final class Access161 extends SingleAccess {
        public Access161() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "CRUD"),
                    grant(SingleRole.CITIZEN, "CRUD"),
                    grant(SingleRole.TTL_PROFILE, "CRUD"));
        }
    }

    public static final class Access162 extends SingleAccess {
        public Access162() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRUD"),
                    grant(SingleRole.CASEWORKER_WA_TASK_CONFIGURATION, "R"));
        }
    }

    public static final class Access163 extends SingleAccess {
        public Access163() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRUD"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access164 extends SingleAccess {
        public Access164() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRUD"));
        }
    }

    public static final class Access165 extends SingleAccess {
        public Access165() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRU"),
                    grant(SingleRole.CASEWORKER_WA_TASK_CONFIGURATION, "R"),
                    grant(SingleRole.CITIZEN, "CRU"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access166 extends SingleAccess {
        public Access166() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "D"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRU"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access167 extends SingleAccess {
        public Access167() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRU"),
                    grant(SingleRole.CASEWORKER_WA_TASK_CONFIGURATION, "R"),
                    grant(SingleRole.CITIZEN, "CRU"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access168 extends SingleAccess {
        public Access168() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRU"),
                    grant(SingleRole.CITIZEN, "CRU"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access169 extends SingleAccess {
        public Access169() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRU"),
                    grant(SingleRole.CITIZEN, "CRU"));
        }
    }

    public static final class Access170 extends SingleAccess {
        public Access170() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRU"),
                    grant(SingleRole.CASEWORKER_WA_TASK_CONFIGURATION, "R"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access171 extends SingleAccess {
        public Access171() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRU"),
                    grant(SingleRole.CASEWORKER_WA_TASK_CONFIGURATION, "R"));
        }
    }

    public static final class Access172 extends SingleAccess {
        public Access172() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRU"),
                    grant(SingleRole.CITIZEN, "CRU"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access173 extends SingleAccess {
        public Access173() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRU"),
                    grant(SingleRole.CITIZEN, "CRU"));
        }
    }

    public static final class Access174 extends SingleAccess {
        public Access174() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRU"),
                    grant(SingleRole.CITIZEN, "CRUD"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access175 extends SingleAccess {
        public Access175() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRU"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access176 extends SingleAccess {
        public Access176() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRU"));
        }
    }

    public static final class Access177 extends SingleAccess {
        public Access177() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRUD"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access178 extends SingleAccess {
        public Access178() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRUD"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access179 extends SingleAccess {
        public Access179() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRUD"));
        }
    }

    public static final class Access180 extends SingleAccess {
        public Access180() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRU"));
        }
    }

    public static final class Access181 extends SingleAccess {
        public Access181() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "D"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRU"),
                    grant(SingleRole.CITIZEN, "CRU"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access182 extends SingleAccess {
        public Access182() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRU"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access183 extends SingleAccess {
        public Access183() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRUD"),
                    grant(SingleRole.CASEWORKER_WA_TASK_CONFIGURATION, "R"));
        }
    }

    public static final class Access184 extends SingleAccess {
        public Access184() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRUD"));
        }
    }

    public static final class Access185 extends SingleAccess {
        public Access185() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "R"),
                    grant(SingleRole.CASEWORKER_WA_TASK_CONFIGURATION, "R"));
        }
    }

    public static final class Access186 extends SingleAccess {
        public Access186() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRU"));
        }
    }

    public static final class Access187 extends SingleAccess {
        public Access187() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "CRU"),
                    grant(SingleRole.CITIZEN, "CRUD"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access188 extends SingleAccess {
        public Access188() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "CRU"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access189 extends SingleAccess {
        public Access189() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "CRU"));
        }
    }

    public static final class Access190 extends SingleAccess {
        public Access190() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRUD"),
                    grant(SingleRole.CITIZEN, "CRUD"),
                    grant(SingleRole.TTL_PROFILE, "CRUD"));
        }
    }

    public static final class Access191 extends SingleAccess {
        public Access191() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "CRUD"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access192 extends SingleAccess {
        public Access192() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "CRUD"));
        }
    }

    public static final class Access193 extends SingleAccess {
        public Access193() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "R"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access194 extends SingleAccess {
        public Access194() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRU"));
        }
    }

    public static final class Access195 extends SingleAccess {
        public Access195() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRUD"),
                    grant(SingleRole.GS_PROFILE, "R"));
        }
    }

    public static final class Access196 extends SingleAccess {
        public Access196() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_RAS_VALIDATION, "R"),
                    grant(SingleRole.CASEWORKER_WA_TASK_CONFIGURATION, "R"),
                    grant(SingleRole.GS_PROFILE, "R"));
        }
    }

    public static final class Access197 extends SingleAccess {
        public Access197() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CASEWORKER_WA_TASK_CONFIGURATION, "R"),
                    grant(SingleRole.GS_PROFILE, "R"));
        }
    }

    public static final class Access198 extends SingleAccess {
        public Access198() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CITIZEN, "CRUD"),
                    grant(SingleRole.GS_PROFILE, "R"));
        }
    }

    public static final class Access199 extends SingleAccess {
        public Access199() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"),
                    grant(SingleRole.CITIZEN, "CRUD"));
        }
    }

    public static final class Access200 extends SingleAccess {
        public Access200() {
            super(grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"));
        }
    }

    public static final class Access201 extends SingleAccess {
        public Access201() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "CRU"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access202 extends SingleAccess {
        public Access202() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "R"));
        }
    }

    public static final class Access203 extends SingleAccess {
        public Access203() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "R"));
        }
    }

    public static final class Access204 extends SingleAccess {
        public Access204() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRU"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access205 extends SingleAccess {
        public Access205() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "R"));
        }
    }

    public static final class Access206 extends SingleAccess {
        public Access206() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "R"));
        }
    }

    public static final class Access207 extends SingleAccess {
        public Access207() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_API, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "R"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access208 extends SingleAccess {
        public Access208() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRUD"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "R"));
        }
    }

    public static final class Access209 extends SingleAccess {
        public Access209() {
            super(grant(SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES, "CRUD"));
        }
    }

    public static final class Access210 extends SingleAccess {
        public Access210() {
            super(
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "CRU"),
                    grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRU"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access211 extends SingleAccess {
        public Access211() {
            super(grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES, "R"));
        }
    }

    public static final class Access212 extends SingleAccess {
        public Access212() {
            super(grant(SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, "R"));
        }
    }

    public static final class Access213 extends SingleAccess {
        public Access213() {
            super(grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "CRU"));
        }
    }

    public static final class Access214 extends SingleAccess {
        public Access214() {
            super(grant(SingleRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, "R"));
        }
    }

    public static final class Access215 extends SingleAccess {
        public Access215() {
            super(grant(SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRUD"));
        }
    }

    public static final class Access216 extends SingleAccess {
        public Access216() {
            super(
                    grant(SingleRole.CASEWORKER_ET_PCQEXTRACTOR, "R"),
                    grant(SingleRole.ET_ACAS_API, "R"));
        }
    }

    public static final class Access217 extends SingleAccess {
        public Access217() {
            super(grant(SingleRole.CASEWORKER_ET_PCQEXTRACTOR, "R"));
        }
    }

    public static final class Access218 extends SingleAccess {
        public Access218() {
            super(
                    grant(SingleRole.CASEWORKER_RAS_VALIDATION, "R"),
                    grant(SingleRole.CASEWORKER_WA_TASK_CONFIGURATION, "R"),
                    grant(SingleRole.GS_PROFILE, "R"));
        }
    }

    public static final class Access219 extends SingleAccess {
        public Access219() {
            super(grant(SingleRole.CITIZEN, "CRUD"));
        }
    }
}
