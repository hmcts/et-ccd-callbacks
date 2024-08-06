package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.exceptions.DocumentManagementException;
import uk.gov.hmcts.ecm.common.helpers.DocumentHelper;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.citizenhub.ClaimantTse;
import uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.ClaimantTellSomethingElseHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.TSEApplicationTypeData;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants.APPLICATION_COMPLETE_RULE92_ANSWERED_NO;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants.APPLICATION_COMPLETE_RULE92_ANSWERED_YES_RESP_OFFLINE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants.APPLICATION_COMPLETE_RULE92_ANSWERED_YES_RESP_ONLINE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.ClaimantTellSomethingElseHelper.claimantSelectApplicationToType;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.DOCGEN_ERROR;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.DocumentHelper.createDocumentTypeItemFromTopLevel;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.TornadoService.CLAIMANT_TSE_FILE_NAME;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClaimantTellSomethingElseService {

    private final DocumentManagementService documentManagementService;
    private final TornadoService tornadoService;

    public List<String> validateGiveDetails(CaseData caseData) {
        List<String> errors = new ArrayList<>();
        TSEApplicationTypeData selectedAppData =
                ClaimantTellSomethingElseHelper.getSelectedApplicationType(caseData);
        if (selectedAppData.getUploadedTseDocument() == null && isNullOrEmpty(selectedAppData.getSelectedTextBox())) {
            errors.add(TSEConstants.GIVE_DETAIL_MISSING);
        }
        return errors;
    }

    public void populateClaimantTse(CaseData caseData) {
        ClaimantTse claimantTse = new ClaimantTse();
        claimantTse.setContactApplicationType(caseData.getClaimantTseSelectApplication());
        claimantTse.setCopyToOtherPartyYesOrNo(caseData.getClaimantTseRule92());
        claimantTse.setCopyToOtherPartyText(caseData.getClaimantTseRule92AnsNoGiveDetails());

        TSEApplicationTypeData selectedAppData =
                ClaimantTellSomethingElseHelper.getSelectedApplicationType(caseData);
        claimantTse.setContactApplicationText(selectedAppData.getSelectedTextBox());
        claimantTse.setContactApplicationFile(selectedAppData.getUploadedTseDocument());
        caseData.setClaimantTse(claimantTse);
    }

    public void generateAndAddApplicationPdf(CaseData caseData, String userToken, String caseTypeId) {
        try {
            if (isEmpty(caseData.getDocumentCollection())) {
                caseData.setDocumentCollection(new ArrayList<>());
            }

            String selectApplicationType = claimantSelectApplicationToType(caseData.getClaimantTseSelectApplication());
            String applicationDocMapping =
                    DocumentHelper.claimantApplicationTypeToDocType(selectApplicationType);
            String topLevel = DocumentHelper.getTopLevelDocument(applicationDocMapping);
            DocumentInfo documentInfo =
                    tornadoService.generateEventDocument(caseData, userToken, caseTypeId, CLAIMANT_TSE_FILE_NAME);
            caseData.setDocMarkUp(documentInfo.getMarkUp().replace("Document", "Download a copy of your application"));
            caseData.getDocumentCollection().add(createDocumentTypeItemFromTopLevel(
                    documentManagementService.addDocumentToDocumentField(documentInfo),
                    topLevel,
                    applicationDocMapping,
                    caseData.getClaimantTseSelectApplication()
            ));

        } catch (Exception e) {
            throw new DocumentManagementException(String.format(DOCGEN_ERROR, caseData.getEthosCaseReference()), e);
        }
    }

    public String buildApplicationCompleteResponse(CaseData caseData) {
        List<GenericTseApplicationTypeItem> tseApplicationCollection =
                caseData.getGenericTseApplicationCollection();
        GenericTseApplicationTypeItem latestTSEApplication =
                tseApplicationCollection.get(tseApplicationCollection.size() - 1);

        String ansRule92 = latestTSEApplication.getValue().getCopyToOtherPartyYesOrNo();
        String tseRespNotAvailability = caseData.getClaimantTseRespNotAvailable();
        String body;
        if (YES.equals(ansRule92)) {
            if (YES.equals(tseRespNotAvailability)) {
                body = String.format(APPLICATION_COMPLETE_RULE92_ANSWERED_YES_RESP_OFFLINE,
                        caseData.getDocMarkUp());
            } else {
                body = String.format(APPLICATION_COMPLETE_RULE92_ANSWERED_YES_RESP_ONLINE,
                        UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 7), caseData.getDocMarkUp());
            }
        } else {
            body = String.format(APPLICATION_COMPLETE_RULE92_ANSWERED_NO, caseData.getDocMarkUp());
        }
        return body;
    }
}