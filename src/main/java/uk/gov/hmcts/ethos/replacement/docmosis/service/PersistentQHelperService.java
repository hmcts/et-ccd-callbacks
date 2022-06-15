package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.PersistentQHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.servicebus.CreateUpdatesBusSender;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service("persistentQHelperService")
public class PersistentQHelperService {

    private final CreateUpdatesBusSender createUpdatesBusSender;
    private final UserService userService;

    public void sendCreationEventToSingles(String userToken, String caseTypeId, String jurisdiction,
                                           List<String> errors, List<String> ethosCaseRefCollection, String officeCT,
                                           String positionTypeCT, String ccdGatewayBaseUrl,
                                           String reasonForCT, String multipleRef, String confirmation,
                                           String multipleReferenceLinkMarkUp, boolean transferSameCountry,
                                           String sourceEthosCaseReference) {

        String username = userService.getUserDetails(userToken).getEmail();
        var dataModel = PersistentQHelper.getCreationSingleDataModel(ccdGatewayBaseUrl, officeCT, positionTypeCT,
                reasonForCT, transferSameCountry, sourceEthosCaseReference);

        PersistentQHelper.sendSingleUpdatesPersistentQ(caseTypeId,
                jurisdiction,
                username,
                ethosCaseRefCollection,
                dataModel,
                errors,
                multipleRef,
                confirmation,
                createUpdatesBusSender,
                String.valueOf(ethosCaseRefCollection.size()),
                multipleReferenceLinkMarkUp
        );

    }

    public void sendTransferToEcmEvent(String userToken, String caseTypeId, String jurisdiction,
                                       List<String> errors, List<String> ethosCaseRefCollection, String officeCT,
                                       String positionTypeCT, String ccdGatewayBaseUrl,
                                       String reasonForCT, String confirmation, String sourceEthosCaseReference) {
        String username = userService.getUserDetails(userToken).getEmail();
        var dataModel = PersistentQHelper.getTransferToEcmModel(ccdGatewayBaseUrl, officeCT, positionTypeCT,
                reasonForCT, sourceEthosCaseReference);

        PersistentQHelper.sendSingleUpdatesPersistentQ(caseTypeId,
                jurisdiction,
                username,
                ethosCaseRefCollection,
                dataModel,
                errors,
                null,
                confirmation,
                createUpdatesBusSender,
                String.valueOf(ethosCaseRefCollection.size()),
                null
        );

    }
}
