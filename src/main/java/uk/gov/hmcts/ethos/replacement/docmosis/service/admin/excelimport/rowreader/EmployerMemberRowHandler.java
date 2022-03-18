package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.excelimport.rowreader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.CourtWorkerType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.CourtWorkerRepository;

@Component
@StaffRowHandler
public class EmployerMemberRowHandler extends AbstractCourtWorkerRowHandler {
    static final String EMPLOYER_MEMBER_ROW_ID = "TRIB_fl_ERMember";

    @Autowired
    public EmployerMemberRowHandler(CourtWorkerRepository courtWorkerRepository) {
        super(courtWorkerRepository, EMPLOYER_MEMBER_ROW_ID, CourtWorkerType.EMPLOYER_MEMBER);
    }
}
