package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.excelimport.rowreader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.CourtWorkerType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.CourtWorkerRepository;

@Component
@StaffRowHandler
public class EmployeeMemberRowHandler extends AbstractCourtWorkerRowHandler {
    static final String EMPLOYEE_MEMBER_ROW_ID = "TRIB_fl_EEMember";

    @Autowired
    public EmployeeMemberRowHandler(CourtWorkerRepository courtWorkerRepository) {
        super(courtWorkerRepository, EMPLOYEE_MEMBER_ROW_ID, CourtWorkerType.EMPLOYEE_MEMBER);
    }
}
