package uk.gov.hmcts.ethos.replacement.docmosis.domain.caseview;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CaseView;
import uk.gov.hmcts.ccd.sdk.CaseViewRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.AdminData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.caseview.state.AdminCaseState;

import java.util.Set;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.ADMIN_CASE_TYPE_ID;

@Component
public class AdminCaseView implements CaseView<AdminData, AdminCaseState> {
    @Override
    public Set<String> caseTypeIds() {
        return Set.of(ADMIN_CASE_TYPE_ID);
    }

    @Override
    public AdminData getCase(CaseViewRequest<AdminCaseState> request, AdminData blobCase) {
        return blobCase;
    }
}
