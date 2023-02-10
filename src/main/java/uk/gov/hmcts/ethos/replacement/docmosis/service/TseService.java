package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.citizenhub.ClaimantTse;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.RespondentTellSomethingElseHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.RespondentTSEApplicationTypeData;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.IN_PROGRESS;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_TITLE;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings({"PMD.GodClass", "PMD.CyclomaticComplexity", "PMD.LawOfDemeter"})
public class TseService {
    /**
     * Creates a new TSE collection if it doesn't exist.
     * Create a new application in the list and assign the TSE data from CaseData to it.
     * At last, clears the existing TSE data from CaseData to ensure fields will be empty when user
     * starts a new application in the same case.
     * @param caseData contains all the case data.
     * @param isClaimant create a claimant application or a respondent application
     */
    public void createApplication(CaseData caseData, boolean isClaimant) {
        if (CollectionUtils.isEmpty(caseData.getGenericTseApplicationCollection())) {
            caseData.setGenericTseApplicationCollection(new ArrayList<>());
        }

        GenericTseApplicationType application = new GenericTseApplicationType();

        application.setDate(UtilHelper.formatCurrentDate(LocalDate.now()));
        application.setDueDate(UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 7));
        application.setResponsesCount("0");
        application.setNumber(String.valueOf(getNextApplicationNumber(caseData)));
        application.setStatus(IN_PROGRESS);

        if (isClaimant) {
            addClaimantData(caseData, application);
        } else {
            addRespondentData(caseData, application);
        }

        GenericTseApplicationTypeItem tseApplicationTypeItem = new GenericTseApplicationTypeItem();
        tseApplicationTypeItem.setId(UUID.randomUUID().toString());
        tseApplicationTypeItem.setValue(application);

        List<GenericTseApplicationTypeItem> tseApplicationCollection = caseData.getGenericTseApplicationCollection();
        tseApplicationCollection.add(tseApplicationTypeItem);

        // todo implement try catch for concurrent modification
        caseData.setGenericTseApplicationCollection(tseApplicationCollection);
    }

    private void addClaimantData(CaseData caseData, GenericTseApplicationType application) {
        application.setApplicant(CLAIMANT_TITLE);

        ClaimantTse claimantTse = caseData.getClaimantTse();
        application.setType(claimantTse.getContactApplicationType());
        application.setDetails(claimantTse.getContactApplicationText());
        application.setDocumentUpload(claimantTse.getContactApplicationFile());
        application.setCopyToOtherPartyYesOrNo(claimantTse.getCopyToOtherPartyYesOrNo());
        application.setCopyToOtherPartyText(claimantTse.getCopyToOtherPartyText());

        caseData.setClaimantTse(null);
    }

    private void addRespondentData(CaseData caseData, GenericTseApplicationType application) {
        application.setApplicant(RESPONDENT_TITLE);
        assignDataToFieldsFromApplicationType(application, caseData);
        application.setType(caseData.getResTseSelectApplication());
        application.setCopyToOtherPartyYesOrNo(caseData.getResTseCopyToOtherPartyYesOrNo());
        application.setCopyToOtherPartyText(caseData.getResTseCopyToOtherPartyTextArea());

        clearRespondentTseDataFromCaseData(caseData);
    }

    private void assignDataToFieldsFromApplicationType(GenericTseApplicationType respondentTseType, CaseData caseData) {
        RespondentTSEApplicationTypeData selectedAppData =
            RespondentTellSomethingElseHelper.getSelectedApplicationType(caseData);
        if (selectedAppData != null) {
            respondentTseType.setDetails(selectedAppData.getSelectedTextBox());
            respondentTseType.setDocumentUpload(selectedAppData.getResTseDocument());
        }
    }

    private void clearRespondentTseDataFromCaseData(CaseData caseData) {
        caseData.setResTseSelectApplication(null);
        caseData.setResTseCopyToOtherPartyYesOrNo(null);
        caseData.setResTseCopyToOtherPartyTextArea(null);

        caseData.setResTseTextBox1(null);
        caseData.setResTseTextBox2(null);
        caseData.setResTseTextBox3(null);
        caseData.setResTseTextBox4(null);
        caseData.setResTseTextBox5(null);
        caseData.setResTseTextBox6(null);
        caseData.setResTseTextBox7(null);
        caseData.setResTseTextBox8(null);
        caseData.setResTseTextBox9(null);
        caseData.setResTseTextBox10(null);
        caseData.setResTseTextBox11(null);
        caseData.setResTseTextBox12(null);

        caseData.setResTseDocument1(null);
        caseData.setResTseDocument2(null);
        caseData.setResTseDocument3(null);
        caseData.setResTseDocument4(null);
        caseData.setResTseDocument5(null);
        caseData.setResTseDocument6(null);
        caseData.setResTseDocument7(null);
        caseData.setResTseDocument8(null);
        caseData.setResTseDocument9(null);
        caseData.setResTseDocument10(null);
        caseData.setResTseDocument11(null);
        caseData.setResTseDocument12(null);
    }

    /**
     * Gets the number a new TSE application should be labelled as.
     * @param caseData contains all the case data
     */
    private static int getNextApplicationNumber(CaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getGenericTseApplicationCollection())) {
            return 1;
        }
        return caseData.getGenericTseApplicationCollection().size() + 1;
    }
}
