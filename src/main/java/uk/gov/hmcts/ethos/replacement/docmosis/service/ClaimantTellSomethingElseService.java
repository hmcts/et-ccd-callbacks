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
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.ClaimantTellSomethingElseHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.TSEApplicationTypeData;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
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
    private static final String MISSING_DETAILS = "Please upload a document or provide details in the text box.";

    private static final String APPLICATION_COMPLETE_RULE92_ANSWERED_NO = "<hr>"
            + "<h3>What happens next</h3>"
            + "<p>The tribunal will consider all correspondence and let you know what happens next.</p>"
            + "<hr>";

    private static final String APPLICATION_COMPLETE_RULE92_ANSWERED_YES_RESP_OFFLINE = "<hr>"
            + "<h3>What happens next</h3>"
            + "<p>You must submit your application after copying the correspondence to the other party.</p>"
            + "<p>To copy this correspondence to the other party, you must send it to them by post or email. "
            + "You must include all supporting documents.</p>";

    private static final String APPLICATION_COMPLETE_RULE92_ANSWERED_YES_RESP_ONLINE = "<hr>"
            + "<h3>What happens next</h3>"
            + "<p>You have sent a copy of your application to the respondent. They will have until %s to respond.</p>"
            + "<p>If they do respond, they are expected to copy their response to you.</p>"
            + "<p>You may be asked to supply further information. "
            + "The tribunal will consider all correspondence and let you know what happens next.</p>"
            + "<hr>";

    public List<String> validateGiveDetails(CaseData caseData) {
        List<String> errors = new ArrayList<>();
        TSEApplicationTypeData selectedAppData =
                ClaimantTellSomethingElseHelper.getSelectedApplicationType(caseData);
        if (selectedAppData.getUploadedTseDocument() == null && isNullOrEmpty(selectedAppData.getSelectedTextBox())) {
            errors.add(MISSING_DETAILS);
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
                    caseData.getResTseSelectApplication()
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
        String isRespOffline = caseData.getClaimantTseRespNotAvailable();
        String body;
        if (YES.equals(ansRule92)) {
            if (YES.equals(isRespOffline)) {
                body = APPLICATION_COMPLETE_RULE92_ANSWERED_YES_RESP_OFFLINE;
                body = body + caseData.getDocMarkUp() + "</br>";
            } else {
                body = String.format(APPLICATION_COMPLETE_RULE92_ANSWERED_YES_RESP_ONLINE,
                        UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 7));
            }
        } else {
            body = APPLICATION_COMPLETE_RULE92_ANSWERED_NO;
        }
        return body;
    }
}
