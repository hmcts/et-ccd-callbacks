package uk.gov.hmcts.ethos.replacement.docmosis.service.casetransfer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ethos.replacement.docmosis.service.PersistentQHelperService;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@Service
public class CaseTransferEventService {

    @Value("${ccd_gateway_base_url}")
    private String ccdGatewayBaseUrl;

    private final PersistentQHelperService persistentQHelperService;

    public CaseTransferEventService(PersistentQHelperService persistentQHelperService) {
        this.persistentQHelperService = persistentQHelperService;
    }

    public List<String> transfer(CaseTransferEventParams params) {
        List<String> errors = new ArrayList<>();

        persistentQHelperService.sendCreationEventToSingles(
                params.getUserToken(),
                params.getCaseTypeId(),
                params.getJurisdiction(),
                errors,
                params.getEthosCaseReferences(),
                params.getNewManagingOffice(),
                params.getPositionType(),
                ccdGatewayBaseUrl,
                params.getReason(),
                params.getMultipleReference(),
                params.isConfirmationRequired() ? YES : NO,
                params.getMultipleReferenceLink(),
                params.isTransferSameCountry(),
                params.getSourceEthosCaseReference()
        );

        return errors;
    }

    public List<String> transferToEcm(CaseTransferToEcmParams params) {
        List<String> errors = new ArrayList<>();
        persistentQHelperService.sendTransferToEcmEvent(
                params.getUserToken(),
                params.getCaseTypeId(),
                params.getJurisdiction(),
                errors,
                params.getEthosCaseReferences(),
                params.getNewCaseTypeId(),
                params.getPositionType(),
                ccdGatewayBaseUrl,
                params.getReason(),
                params.isConfirmationRequired() ? YES : NO,
                params.getSourceEthosCaseReference()
        );
        return errors;
    }
}
