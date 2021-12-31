package uk.gov.hmcts.ethos.replacement.docmosis.service.casetransfer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ethos.replacement.docmosis.service.PersistentQHelperService;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;

@Service
public class CaseTransferEventService {

    @Value("${ccd_gateway_base_url}")
    private String ccdGatewayBaseUrl;

    private final PersistentQHelperService persistentQHelperService;

    public CaseTransferEventService(PersistentQHelperService persistentQHelperService) {
        this.persistentQHelperService = persistentQHelperService;
    }

    public List<String> transfer(CaseTransferEventParams params) {
        var errors = new ArrayList<String>();

        persistentQHelperService.sendCreationEventToSingles(
                params.getUserToken(),
                params.getCaseTypeId(),
                params.getJurisdiction(),
                errors,
                List.of(params.getEthosCaseReference()),
                params.getNewManagingOffice(),
                params.getPositionType(),
                ccdGatewayBaseUrl,
                params.getReason(),
                params.getEcmCaseType(),
                NO,
                null,
                params.isTransferSameCountry(),
                params.getSourceEthosCaseReference()
        );

        return errors;
    }
}
