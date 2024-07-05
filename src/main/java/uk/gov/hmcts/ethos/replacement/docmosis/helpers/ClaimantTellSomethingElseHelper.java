package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.documents.ClaimantTellSomethingElseData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.documents.ClaimantTellSomethingElseDocument;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.TSEApplicationTypeData;

import java.time.LocalDate;

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_TITLE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants.CLAIMANT_TSE_WITHDRAW_CLAIM;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.TornadoService.CLAIMANT_TSE_FILE_NAME;

public final class ClaimantTellSomethingElseHelper {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String CLAIMANT_TSE_TEMPLATE_NAME = "EM-TRB-EGW-ENG-02822.docx";

    private ClaimantTellSomethingElseHelper() {
    }

    public static TSEApplicationTypeData getSelectedApplicationType(CaseData caseData) {
        if (caseData.getClaimantTseSelectApplication().equals(CLAIMANT_TSE_WITHDRAW_CLAIM)) {
            return new TSEApplicationTypeData(
                    caseData.getClaimantTseDocument13(), caseData.getClaimantTseTextBox13());
        }
        throw new IllegalArgumentException(String.format("Unexpected application type %s",
                caseData.getResTseSelectApplication()));
    }

    public static String getDocumentRequest(CaseData caseData, String accessKey)
            throws JsonProcessingException {

        TSEApplicationTypeData selectedAppData = getSelectedApplicationType(caseData);
        GenericTseApplicationTypeItem lastApp = getCurrentGenericTseApplicationTypeItem(caseData);

        ClaimantTellSomethingElseData data = ClaimantTellSomethingElseData.builder()
                .claimTseApplicant(CLAIMANT_TITLE)
                .caseNumber(defaultIfEmpty(caseData.getEthosCaseReference(), null))
                .claimTseSelectApplication(defaultIfEmpty(caseData.getClaimantTseSelectApplication(), null))
                .claimTseApplicationDate(lastApp != null && StringUtils.isNotBlank(lastApp.getValue().getDate())
                        ? lastApp.getValue().getDate() : UtilHelper.formatCurrentDate(LocalDate.now()))
                .claimTseDocument(getDocumentName(selectedAppData))
                .claimTseTextBox(getTextBoxDetails(selectedAppData))
                .claimTseCopyToOtherPartyYesOrNo(defaultIfEmpty(caseData.getClaimantTseRule92(), null))
                .claimTseCopyToOtherPartyTextArea(defaultIfEmpty(caseData.getClaimantTseRule92AnsNoGiveDetails(), null))
                .build();

        ClaimantTellSomethingElseDocument document = ClaimantTellSomethingElseDocument.builder()
                .accessKey(accessKey)
                .outputName(CLAIMANT_TSE_FILE_NAME)
                .templateName(CLAIMANT_TSE_TEMPLATE_NAME)
                .data(data).build();

        return OBJECT_MAPPER.writeValueAsString(document);
    }

    private static String getDocumentName(TSEApplicationTypeData selectedAppData) {
        if (selectedAppData == null || selectedAppData.getUploadedTseDocument() == null) {
            return null;
        }

        return selectedAppData.getUploadedTseDocument().getDocumentFilename();
    }

    private static String getTextBoxDetails(TSEApplicationTypeData selectedAppData) {
        if (selectedAppData == null) {
            return "";
        }

        return selectedAppData.getSelectedTextBox();
    }

    private static GenericTseApplicationTypeItem getCurrentGenericTseApplicationTypeItem(CaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getGenericTseApplicationCollection())) {
            return null;
        }

        return caseData.getGenericTseApplicationCollection()
                .get(caseData.getGenericTseApplicationCollection().size() - 1);
    }
}
