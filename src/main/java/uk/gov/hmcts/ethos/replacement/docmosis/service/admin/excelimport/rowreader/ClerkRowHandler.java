package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.excelimport.rowreader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.CourtWorkerType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.CourtWorkerRepository;

@Component
@StaffRowHandler
public class ClerkRowHandler extends AbstractCourtWorkerRowHandler {
    static final String CLERK_ROW_ID = "TRIB_fl_Clerks";

    @Autowired
    public ClerkRowHandler(CourtWorkerRepository courtWorkerRepository) {
        super(courtWorkerRepository, CLERK_ROW_ID, CourtWorkerType.CLERK);
    }
}
