package uk.gov.hmcts.ethos.replacement.docmosis.service.messagehandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.ecm.common.model.servicebus.UpdateCaseMsg;
import uk.gov.hmcts.ecm.common.model.servicebus.datamodel.CreationSingleDataModel;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.SINGLE_CASE_TYPE;

/**
 * Service for reading single cases and routing updates.
 * Migrated from et-message-handler.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class SingleReadingService {

    private final CcdClient ccdClient;
    private final AdminUserService userService;
    private final SingleUpdateService singleUpdateService;
    private final SingleTransferService singleTransferService;

    public void sendUpdateToSingleLogic(UpdateCaseMsg updateCaseMsg) throws IOException {
        String accessToken = userService.getAdminUserToken();
        List<SubmitEvent> submitEvents = retrieveSingleCase(accessToken, updateCaseMsg);

        if (CollectionUtils.isNotEmpty(submitEvents)) {
            if (updateCaseMsg.getDataModelParent() instanceof CreationSingleDataModel) {
                singleTransferService.sendTransferred(submitEvents.getFirst(), accessToken, updateCaseMsg);
            } else {
                singleUpdateService.sendUpdate(submitEvents.getFirst(), accessToken, updateCaseMsg);
            }
        } else {
            log.warn("No submit events found for msg id {} with case reference {}", updateCaseMsg.getMsgId(),
                     updateCaseMsg.getEthosCaseReference());
        }
    }

    private List<SubmitEvent> retrieveSingleCase(String accessToken, UpdateCaseMsg updateCaseMsg) throws IOException {
        Objects.requireNonNull(updateCaseMsg.getEthosCaseReference(), "No ethosCaseReference found");
        String caseType = updateCaseMsg.getMultipleRef().equals(SINGLE_CASE_TYPE)
            ? updateCaseMsg.getCaseTypeId()
            : UtilHelper.getCaseTypeId(updateCaseMsg.getCaseTypeId());

        return ccdClient.retrieveCasesElasticSearch(
            accessToken,
            caseType,
            new ArrayList<>(Collections.singletonList(updateCaseMsg.getEthosCaseReference())));
    }
}
