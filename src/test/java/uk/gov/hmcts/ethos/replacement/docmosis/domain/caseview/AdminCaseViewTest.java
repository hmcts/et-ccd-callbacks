package uk.gov.hmcts.ethos.replacement.docmosis.domain.caseview;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.AdminData;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ADMIN_CASE_TYPE_ID;

class AdminCaseViewTest {

    @Test
    void returnsConfiguredCaseTypeAndCase() {
        AdminCaseView caseView = new AdminCaseView();
        AdminData adminData = new AdminData();

        assertEquals(Set.of(ADMIN_CASE_TYPE_ID), caseView.caseTypeIds());
        assertSame(adminData, caseView.getCase(null, adminData));
    }
}
