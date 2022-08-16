package uk.gov.hmcts.ethos.replacement.docmosis.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.webjars.NotFoundException;
import uk.gov.hmcts.ecm.common.exceptions.DocumentManagementException;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.Et3VettingType;

import java.util.Optional;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.intersectProperties;

@Slf4j
@Service()
@RequiredArgsConstructor
public class Et3VettingService {

    private final DocumentManagementService documentManagementService;
    private final TornadoService tornadoService;

    private static final String DOCGEN_ERROR = "Failed to generate document for case id: %s";

    /**
     * Moves ET3 Vetting related fields off of CaseData and onto the relevant respondent.
     * @param caseData The object containing case data
     */
    public void saveEt3VettingToRespondent(CaseData caseData, DocumentInfo documentInfo) {
        String respondentName = caseData.getEt3ChooseRespondent().getSelectedLabel();
        RespondentSumTypeItem respondent = getRespondentForCase(respondentName, caseData);

        respondent.getValue().setEt3Vetting(copyEt3FieldsFromCaseDataToRespondent(caseData));
        respondent.getValue().getEt3Vetting().setEt3VettingDocument(
                documentManagementService.addDocumentToDocumentField(documentInfo));
        updateValuesOnObject(caseData, new Et3VettingType());

        respondent.getValue().setEt3VettingCompleted(YES);
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
                .filter(o -> respondentName.equals(o.getValue().getRespondentName()))
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

    public DocumentInfo generateEt1VettingDocument(CaseData caseData, String userToken, String caseTypeId) {
        try {
            return tornadoService.generateEventDocument(caseData, userToken,
                    caseTypeId, "ET3 Processing.pdf");
        } catch (Exception e) {
            throw new DocumentManagementException(String.format(DOCGEN_ERROR, caseData.getEthosCaseReference()), e);
        }
    }
}
