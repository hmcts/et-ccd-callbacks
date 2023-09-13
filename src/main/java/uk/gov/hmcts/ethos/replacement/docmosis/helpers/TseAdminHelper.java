package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections4.CollectionUtils;
import uk.gov.hmcts.ecm.common.exceptions.DocumentManagementException;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.documents.TseAdminDecisionRecordData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.documents.TseAdminReplyData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.documents.TseReplyData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.documents.TseReplyDocument;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DocumentManagementService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.TornadoService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLOSED_STATE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.DOCGEN_ERROR;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.DocumentHelper.createDocumentTypeItem;

public final class TseAdminHelper {
    private static final String TSE_ADMIN_REPLY_OUTPUT_NAME = "%s Reply.pdf";
    private static final String TSE_ADMIN_REPLY_TEMPLATE_NAME = "EM-TRB-EGW-ENG-Admin-Tse-Reply.docx";
    private static final String TSE_ADMIN_DECISION_TEMPLATE_NAME = "EM-TRB-EGW-ENG-Admin-Tse-Decision.docx";
    private static final String CASE_MANAGEMENT_ORDER = "Case management order";

    private TseAdminHelper() {
        // Sonar Lint: Utility classes should not have public constructors
    }

    /**
     * Create fields for application dropdown selector.
     * @param caseData contains all the case data
     */
    public static DynamicFixedListType populateSelectApplicationAdminDropdown(CaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getGenericTseApplicationCollection())) {
            return null;
        }

        return DynamicFixedListType.from(caseData.getGenericTseApplicationCollection().stream()
            .filter(r -> r.getValue().getStatus() != null && !CLOSED_STATE.equals(r.getValue().getStatus()))
            .map(r -> DynamicValueType.create(r.getValue().getNumber(),
                r.getValue().getNumber() + " - " + r.getValue().getType())
            )
            .collect(Collectors.toList()));
    }

    public static DocumentTypeItem getDocumentTypeItem(DocumentManagementService docService,
                                                       TornadoService tornadoService, CaseDetails caseDetails,
                                                       String userToken, String typeOfDocument,
                                                       String typeOfCorrespondence) {
        if (caseDetails == null) {
            return null;
        }
        DocumentInfo eventDocInfo = getDocumentInfo(tornadoService, userToken, caseDetails, typeOfDocument);
        String resTseSelectApp = caseDetails.getCaseData().getResTseSelectApplication();
        return createDocumentTypeItem(docService.addDocumentToDocumentField(eventDocInfo),
                typeOfCorrespondence, resTseSelectApp);
    }

    private static DocumentInfo getDocumentInfo(TornadoService tornadoService, String userToken,
                                                CaseDetails caseDetails, String typeOfDocument) {
        CaseData caseData = caseDetails.getCaseData();
        try {
            return tornadoService.generateEventDocument(caseData, userToken, caseDetails.getCaseTypeId(),
                    typeOfDocument);
        } catch (Exception e) {
            throw new DocumentManagementException(String.format(DOCGEN_ERROR, caseData.getEthosCaseReference()), e);
        }
    }

    +
    public static String getReplyDocumentRequest(CaseData caseData, String accessKey) throws JsonProcessingException {
        GenericTseApplicationType selectedApplication = getTseAdminSelectedApplicationType(caseData);
        assert selectedApplication != null;

        TseReplyData data = createDataForTseReply(caseData, selectedApplication);
        TseReplyDocument document = TseReplyDocument.builder()
                .accessKey(accessKey)
                .outputName(String.format(TSE_ADMIN_REPLY_OUTPUT_NAME, selectedApplication.getType()))
                .templateName(TSE_ADMIN_REPLY_TEMPLATE_NAME)
                .data(data).build();
        return new ObjectMapper().writeValueAsString(document);
    }

    public static String getAdminDecisionDocumentRequest(CaseData caseData, String accessKey) throws
            JsonProcessingException {
        GenericTseApplicationType selectedApplication = getTseAdminSelectedApplicationType(caseData);
        assert selectedApplication != null;

        TseReplyData data = createDataForTseAdminDecision(caseData, selectedApplication);
        TseReplyDocument document = TseReplyDocument.builder()
                .accessKey(accessKey)
                .outputName(String.format(TSE_ADMIN_REPLY_OUTPUT_NAME, selectedApplication.getType()))
                .templateName(TSE_ADMIN_DECISION_TEMPLATE_NAME)
                .data(data).build();
        return new ObjectMapper().writeValueAsString(document);
    }

    private static List<GenericTypeItem<DocumentType>> getSupportDocListForDecision(CaseData caseData) {
        if (CASE_MANAGEMENT_ORDER.equals(caseData.getTseAdminTypeOfDecision())
                && caseData.getTseAdminResponseRequiredYesDoc() != null) {
            return caseData.getTseAdminResponseRequiredYesDoc();
        } else {
            return caseData.getTseAdminResponseRequiredNoDoc();
        }
    }

    private static TseReplyData createDataForTseAdminDecision(CaseData caseData,
                                                              GenericTseApplicationType application) {
        List<GenericTypeItem<DocumentType>> supportDocList = getSupportDocListForDecision(caseData);
        return TseAdminDecisionRecordData.builder()
                .caseNumber(defaultIfEmpty(caseData.getEthosCaseReference(), null))
                .respondentParty(application.getApplicant())
                .type(defaultIfEmpty(application.getType(), null))
                .responseDate(UtilHelper.formatCurrentDate(LocalDate.now()))
                .tseAdminDecision(defaultIfEmpty(caseData.getTseAdminDecision(), null))
                .tseAdminDecisionMadeBy(defaultIfEmpty(caseData.getTseAdminDecisionMadeBy(), null))
                .tseAdminTypeOfDecision(defaultIfEmpty(caseData.getTseAdminTypeOfDecision(), null))
                .notificationTitle(defaultIfEmpty(caseData.getTseAdminEnterNotificationTitle(), null))
                .tseAdminAdditionalInformation(defaultIfEmpty(caseData.getTseAdminAdditionalInformation(),
                        null))
                .tseAdminDecisionMadeByFullName(defaultIfEmpty(caseData.getTseAdminDecisionMadeByFullName(),
                        null))
                .tseAdminIsResponseRequired(defaultIfEmpty(caseData.getTseAdminIsResponseRequired(), null))
                .tseAdminSelectPartyNotify(defaultIfEmpty(caseData.getTseAdminSelectPartyNotify(), null))
                .tseAdminSelectPartyRespond(defaultIfEmpty(caseData.getTseAdminSelectPartyRespond(), null))
                .supportingYesNo(hasSupportingDocs(supportDocList))
                .documentCollection(getUploadedDocList(supportDocList))
                .build();
    }

    public static GenericTseApplicationType getTseAdminSelectedApplicationType(CaseData caseData) {
        return caseData.getGenericTseApplicationCollection().stream()
                .filter(item -> item.getValue().getNumber().equals(caseData.getTseAdminSelectApplication()
                        .getSelectedCode()))
                .findFirst()
                .map(GenericTseApplicationTypeItem::getValue)
                .orElse(null);
    }

    private static TseReplyData createDataForTseReply(CaseData caseData, GenericTseApplicationType application) {
        String selectedCmoRespondent = caseData.getTseAdmReplyCmoSelectPartyRespond();
        return TseAdminReplyData.builder()
                .caseNumber(defaultIfEmpty(caseData.getEthosCaseReference(), null))
                .respondentParty(application.getApplicant())
                .type(defaultIfEmpty(application.getType(), null))
                .responseDate(UtilHelper.formatCurrentDate(LocalDate.now()))
                .response(defaultIfEmpty(application.getDetails(), null))
                .supportingYesNo(hasSupportingDocs(caseData.getTseAdmReplyAddDocument()))
                .documentCollection(getUploadedDocList(caseData.getTseAdmReplyAddDocument()))
                .copy(defaultIfEmpty(application.getCopyToOtherPartyYesOrNo(), null))
                .responseTitle(defaultIfEmpty(caseData.getTseAdmReplyEnterResponseTitle(), null))
                .responseAdditionalInfo(defaultIfEmpty(caseData.getTseAdmReplyAdditionalInformation(), null))
                .isCmoOrRequest(defaultIfEmpty(caseData.getTseAdmReplyIsCmoOrRequest(), null))
                .cmoMadeBy(defaultIfEmpty(caseData.getTseAdmReplyCmoMadeBy(), null))
                .cmoEnterFullName(defaultIfEmpty(caseData.getTseAdmReplyCmoEnterFullName(), null))
                .cmoIsResponseRequired(defaultIfEmpty(caseData.getTseAdmReplyCmoIsResponseRequired(), null))
                .cmoSelectPartyRespond(defaultIfEmpty(selectedCmoRespondent, null))
                .requestMadeBy(defaultIfEmpty(caseData.getTseAdmReplyRequestMadeBy(), null))
                .requestEnterFullName(defaultIfEmpty(caseData.getTseAdmReplyRequestEnterFullName(), null))
                .requestIsResponseRequired(defaultIfEmpty(caseData.getTseAdmReplyRequestIsResponseRequired(),
                        null))
                .requestSelectPartyRespond(defaultIfEmpty(caseData.getTseAdmReplyRequestSelectPartyRespond(),
                        null))
                .selectPartyNotify(defaultIfEmpty(caseData.getTseAdmReplySelectPartyNotify(), null))
                .build();
    }

    private static List<GenericTypeItem<DocumentType>> getUploadedDocList(
            List<GenericTypeItem<DocumentType>> docTypeList) {
        if (docTypeList == null) {
            return null;
        }

        List<GenericTypeItem<DocumentType>> genericDocTypeList = new ArrayList<>();
        docTypeList.forEach(doc -> {
            GenericTypeItem<DocumentType> genTypeItems = new GenericTypeItem<>();
            DocumentType docType = new DocumentType();
            docType.setUploadedDocument(doc.getValue().getUploadedDocument());
            genTypeItems.setId(doc.getId() != null ? doc.getId() : UUID.randomUUID().toString());
            genTypeItems.setValue(docType);
            genericDocTypeList.add(genTypeItems);
        });

        return genericDocTypeList;
    }

    private static String hasSupportingDocs(List<GenericTypeItem<DocumentType>> supportDocList) {
        return (supportDocList != null && !supportDocList.isEmpty())  ? "Yes" : "No";
    }

}
