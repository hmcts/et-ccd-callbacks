package uk.gov.hmcts.ethos.replacement.docmosis.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.webjars.NotFoundException;
import uk.gov.hmcts.ecm.common.exceptions.DocumentManagementException;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.Et3VettingType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.DocumentHelper;

import java.util.ArrayList;
import java.util.Optional;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.ET3_PROCESSING;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.RESPONSE_TO_A_CLAIM;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.intersectProperties;

@Slf4j
@Service("et3VettingService")
@RequiredArgsConstructor
public class Et3VettingService {

    private final DocumentManagementService documentManagementService;
    private final TornadoService tornadoService;

    private static final String DOCGEN_ERROR = "Failed to generate document for case id: %s";

    /**
     * Moves ET3 Vetting related fields off of CaseData and onto the relevant respondent. Also saves the document which
     * has been generated onto the respondent
     * @param caseData The object containing case data
     */
    public void saveEt3VettingToRespondent(CaseData caseData, DocumentInfo documentInfo) {
        String respondentName = caseData.getEt3ChooseRespondent().getSelectedLabel();
        RespondentSumTypeItem respondent = getRespondentForCase(respondentName, caseData);

        respondent.getValue().setEt3Vetting(copyEt3FieldsFromCaseDataToRespondent(caseData));
        respondent.getValue().getEt3Vetting().setEt3VettingDocument(
                documentManagementService.addDocumentToDocumentField(documentInfo));
        addEt3VettingCompletedToDoc(caseData, respondent.getValue().getEt3Vetting().getEt3VettingDocument());
        updateValuesOnObject(caseData, new Et3VettingType());

        respondent.getValue().setEt3VettingCompleted(YES);
    }

    private void addEt3VettingCompletedToDoc(CaseData caseData, UploadedDocumentType et3VettingDocument) {
        if (CollectionUtils.isEmpty(caseData.getDocumentCollection())) {
            caseData.setDocumentCollection(new ArrayList<>());
        }
        DocumentTypeItem documentTypeItem = DocumentHelper.createDocumentTypeItemFromTopLevel(et3VettingDocument,
                RESPONSE_TO_A_CLAIM, ET3_PROCESSING, null);
        caseData.getDocumentCollection().add(documentTypeItem);
    }

    /**
     * Restores respondent specific ET3 Vetting related data to the caseData object for editing within ExUI.
     * @param caseData The object containing case data
     */
    public void restoreEt3VettingFromRespondentOntoCaseData(CaseData caseData) {
        String respondentName = caseData.getEt3ChooseRespondent().getSelectedLabel();
        RespondentSumTypeItem respondent = getRespondentForCase(respondentName, caseData);

        Et3VettingType et3Vetting = getEt3VettingTypeForRespondent(respondent);
        DynamicFixedListType et3ChooseRespondent = caseData.getEt3ChooseRespondent();
        updateValuesOnObject(caseData, et3Vetting);
        caseData.setEt3ChooseRespondent(et3ChooseRespondent);
    }

    /**
     * Updates et3 related fields on caseData with values from an Et3VettingType.
     * @param caseData to update
     * @param et3VettingType to bring in values from
     */
    public void updateValuesOnObject(CaseData caseData, Et3VettingType et3VettingType) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.registerModule(new JavaTimeModule());
        try {
            mapper.updateValue(caseData, et3VettingType);
        } catch (JsonMappingException e) {
            log.error(String.format("Failed to restore et3 data for case %s: %s",
                caseData.getEthosCaseReference(), e));
        }
    }

    private RespondentSumTypeItem getRespondentForCase(String respondentName, CaseData caseData) {
        Optional<RespondentSumTypeItem> respondent =
            caseData.getRespondentCollection().stream()
                .filter(o -> respondentName.equals(o.getValue().getRespondentName().trim()))
                .findFirst();

        if (respondent.isEmpty()) {
            throw new NotFoundException(String.format("Failed to look up %s", respondentName));
        }

        return respondent.get();
    }

    private Et3VettingType getEt3VettingTypeForRespondent(RespondentSumTypeItem respondent) {
        Et3VettingType et3VettingType = respondent.getValue().getEt3Vetting();

        if (et3VettingType == null) {
            et3VettingType = new Et3VettingType();
            respondent.getValue().setEt3Vetting(et3VettingType);
        }

        return et3VettingType;
    }

    private Et3VettingType copyEt3FieldsFromCaseDataToRespondent(CaseData caseData) {
        return (Et3VettingType) intersectProperties(caseData, Et3VettingType.class);
    }

    /**
     * This method calls the tornado service to generate the PDF for the ET3 Processing journey.
     * @param caseData where the data is stored
     * @param userToken user authentication token
     * @param caseTypeId reference which caseType the document will be uploaded to
     * @return DocumentInfo which contains the URL and description of the document uploaded to DM Store
     */
    public DocumentInfo generateEt3ProcessingDocument(CaseData caseData, String userToken, String caseTypeId) {
        try {
            return tornadoService.generateEventDocument(caseData, userToken,
                    caseTypeId, "ET3 Processing.pdf");
        } catch (Exception e) {
            throw new DocumentManagementException(String.format(DOCGEN_ERROR, caseData.getEthosCaseReference()), e);
        }
    }
}
