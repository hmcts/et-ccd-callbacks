package uk.gov.hmcts.et.common.model.ccd;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import uk.gov.hmcts.ccd.sdk.api.HasAccessControl;
import uk.gov.hmcts.ccd.sdk.api.HasRole;
import uk.gov.hmcts.ccd.sdk.api.Permission;

import static uk.gov.hmcts.ccd.sdk.api.Permission.CRU;
import static uk.gov.hmcts.ccd.sdk.api.Permission.CRUD;
import static uk.gov.hmcts.et.common.model.ccd.ListingRole.EMPLOYMENT_API;
import static uk.gov.hmcts.et.common.model.ccd.ListingRole.ENGLAND_WALES_CASEWORKER;
import static uk.gov.hmcts.et.common.model.ccd.ListingRole.ENGLAND_WALES_JUDGE;
import static uk.gov.hmcts.et.common.model.ccd.ListingRole.SCOTLAND_CASEWORKER;
import static uk.gov.hmcts.et.common.model.ccd.ListingRole.SCOTLAND_JUDGE;

public class ListingAccess implements HasAccessControl {
    @Override
    public SetMultimap<HasRole, Permission> getGrants() {
        SetMultimap<HasRole, Permission> grants = HashMultimap.create();
        grants.putAll(ENGLAND_WALES_CASEWORKER, CRU);
        grants.putAll(ENGLAND_WALES_JUDGE, CRU);
        grants.putAll(SCOTLAND_CASEWORKER, CRU);
        grants.putAll(SCOTLAND_JUDGE, CRU);
        grants.putAll(EMPLOYMENT_API, CRUD);
        return grants;
    }
}
