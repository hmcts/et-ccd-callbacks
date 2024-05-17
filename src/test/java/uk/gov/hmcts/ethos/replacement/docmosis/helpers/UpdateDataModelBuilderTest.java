package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.model.servicebus.datamodel.UpdateDataModel;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.JudgementTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.JurCodesTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.JudgementType;
import uk.gov.hmcts.et.common.model.ccd.types.JurCodesType;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SELECT_NONE_VALUE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@ExtendWith(SpringExtension.class)
class UpdateDataModelBuilderTest {
    CaseData caseData;

    private static final String CLAIMANT_REPRESENTATIVE_NAME = "Ruth Powers";
    private static final String JURISDICTION = "DSO";
    private static final String RESPONDENT_NAME = "Mindy Simmons";
    private static final String JUDGEMENT_ID = "1234-5678-9012";
    private static final String JUDGEMENT_TYPE = "Case Management";
    private static final String RESPONDENT_REPRESENTATIVE_ID = "5555-6666-7777-8888";
    private static final String RESPONDENT_REPRESENTATIVE_NAME = "Lionel Hutz";
    private static final String UNKNOWN_VALUE = "Unknown";

    @BeforeEach
    public void setup() {
        caseData = new CaseData();

        // Claimant Representative Name
        RepresentedTypeC representedTypeC = new RepresentedTypeC();
        representedTypeC.setNameOfRepresentative(CLAIMANT_REPRESENTATIVE_NAME);
        caseData.setRepresentativeClaimantType(representedTypeC);

        // Jurisdiction
        caseData.setJurCodesCollection(new ArrayList<>());
        JurCodesTypeItem item = new JurCodesTypeItem();
        JurCodesType jurCodesType = new JurCodesType();
        jurCodesType.setJuridictionCodesList(JURISDICTION);
        item.setValue(jurCodesType);
        caseData.getJurCodesCollection().add(item);

        // Respondent
        caseData.setRespondentCollection(new ArrayList<>());
        RespondentSumType respondentSumType = new RespondentSumType();
        respondentSumType.setRespondentName(RESPONDENT_NAME);
        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(respondentSumType);
        caseData.getRespondentCollection().add(respondentSumTypeItem);

        // Judgement
        caseData.setJudgementCollection(new ArrayList<>());
        JudgementType judgementType = new JudgementType();
        judgementType.setJudgementType(JUDGEMENT_TYPE);
        JudgementTypeItem judgementTypeItem = new JudgementTypeItem();
        judgementTypeItem.setId(JUDGEMENT_ID);
        judgementTypeItem.setValue(judgementType);
        caseData.getJudgementCollection().add(judgementTypeItem);

        // Respondent Representative
        caseData.setRepCollection(new ArrayList<>());
        RepresentedTypeR representedType =
            RepresentedTypeR.builder()
                .nameOfRepresentative(RESPONDENT_REPRESENTATIVE_NAME).build();
        RepresentedTypeRItem representedTypeRItem = new RepresentedTypeRItem();
        representedTypeRItem.setId(RESPONDENT_REPRESENTATIVE_ID);
        representedTypeRItem.setValue(representedType);
        caseData.getRepCollection().add(representedTypeRItem);
    }

    @Test
    void testNoClaimantRepresentativeSelected() {
        MultipleData multipleData = createMultipleData(
                SELECT_NONE_VALUE, SELECT_NONE_VALUE, SELECT_NONE_VALUE, SELECT_NONE_VALUE, SELECT_NONE_VALUE);
        UpdateDataModel updateDataModel = UpdateDataModelBuilder.build(multipleData, caseData);
        assertNull(updateDataModel.getRepresentativeClaimantType());
    }

    @Test
    void testClaimantRepresentativeSelected() {
        MultipleData multipleData = createMultipleData(
                CLAIMANT_REPRESENTATIVE_NAME, SELECT_NONE_VALUE,
                SELECT_NONE_VALUE, SELECT_NONE_VALUE, SELECT_NONE_VALUE);
        UpdateDataModel updateDataModel = UpdateDataModelBuilder.build(multipleData, caseData);
        assertEquals(CLAIMANT_REPRESENTATIVE_NAME,
                updateDataModel.getRepresentativeClaimantType().getNameOfRepresentative());
    }

    @Test
    void testUnknownSelectedClaimantRepresentativeIsIgnored() {
        MultipleData multipleData = createMultipleData(
                UNKNOWN_VALUE, SELECT_NONE_VALUE, SELECT_NONE_VALUE, SELECT_NONE_VALUE, SELECT_NONE_VALUE);
        UpdateDataModel updateDataModel = UpdateDataModelBuilder.build(multipleData, caseData);
        assertNull(updateDataModel.getRepresentativeClaimantType());
    }

    @Test
    void testNoJurisdictionSelected() {
        MultipleData multipleData = createMultipleData(
                SELECT_NONE_VALUE, SELECT_NONE_VALUE, SELECT_NONE_VALUE, SELECT_NONE_VALUE, SELECT_NONE_VALUE);
        UpdateDataModel updateDataModel = UpdateDataModelBuilder.build(multipleData, caseData);
        assertNull(updateDataModel.getJurCodesType());
    }

    @Test
    void testJurisdictionSelected() {
        MultipleData multipleData = createMultipleData(
                SELECT_NONE_VALUE, JURISDICTION, SELECT_NONE_VALUE, SELECT_NONE_VALUE, SELECT_NONE_VALUE);
        UpdateDataModel updateDataModel = UpdateDataModelBuilder.build(multipleData, caseData);
        assertEquals(JURISDICTION, updateDataModel.getJurCodesType().getJuridictionCodesList());
    }

    @Test
    void testUnknownSelectedJurisdictionIsIgnored() {
        MultipleData multipleData = createMultipleData(
                SELECT_NONE_VALUE, UNKNOWN_VALUE, SELECT_NONE_VALUE, SELECT_NONE_VALUE, SELECT_NONE_VALUE);
        UpdateDataModel updateDataModel = UpdateDataModelBuilder.build(multipleData, caseData);
        assertNull(updateDataModel.getJurCodesType());
    }

    @Test
    void testNoRespondentSelected() {
        MultipleData multipleData = createMultipleData(
                SELECT_NONE_VALUE, SELECT_NONE_VALUE, SELECT_NONE_VALUE, SELECT_NONE_VALUE, SELECT_NONE_VALUE);
        UpdateDataModel updateDataModel = UpdateDataModelBuilder.build(multipleData, caseData);
        assertNull(updateDataModel.getRespondentSumType());
    }

    @Test
    void testRespondentSelected() {
        MultipleData multipleData = createMultipleData(
                SELECT_NONE_VALUE, SELECT_NONE_VALUE, RESPONDENT_NAME, SELECT_NONE_VALUE, SELECT_NONE_VALUE);
        UpdateDataModel updateDataModel = UpdateDataModelBuilder.build(multipleData, caseData);
        assertEquals(RESPONDENT_NAME, updateDataModel.getRespondentSumType().getRespondentName());
    }

    @Test
    void testUnknownSelectedRespondentIsIgnored() {
        MultipleData multipleData = createMultipleData(
                SELECT_NONE_VALUE, SELECT_NONE_VALUE, UNKNOWN_VALUE, SELECT_NONE_VALUE, SELECT_NONE_VALUE);
        UpdateDataModel updateDataModel = UpdateDataModelBuilder.build(multipleData, caseData);
        assertNull(updateDataModel.getRespondentSumType());
    }

    @Test
    void testNoJudgementSelected() {
        MultipleData multipleData = createMultipleData(
                SELECT_NONE_VALUE, SELECT_NONE_VALUE, SELECT_NONE_VALUE, SELECT_NONE_VALUE, SELECT_NONE_VALUE);
        UpdateDataModel updateDataModel = UpdateDataModelBuilder.build(multipleData, caseData);
        assertNull(updateDataModel.getJudgementType());
    }

    @Test
    void testJudgementSelected() {
        MultipleData multipleData = createMultipleData(
                SELECT_NONE_VALUE, SELECT_NONE_VALUE, SELECT_NONE_VALUE, JUDGEMENT_ID, SELECT_NONE_VALUE);
        UpdateDataModel updateDataModel = UpdateDataModelBuilder.build(multipleData, caseData);
        assertEquals(JUDGEMENT_TYPE, updateDataModel.getJudgementType().getJudgementType());
    }

    @Test
    void testUnknownSelectedJudgementIsIgnored() {
        MultipleData multipleData = createMultipleData(
                SELECT_NONE_VALUE, SELECT_NONE_VALUE, SELECT_NONE_VALUE, UNKNOWN_VALUE, SELECT_NONE_VALUE);
        UpdateDataModel updateDataModel = UpdateDataModelBuilder.build(multipleData, caseData);
        assertNull(updateDataModel.getJudgementType());
    }

    @Test
    void testNoRespondentRepresentativeSelected() {
        MultipleData multipleData = createMultipleData(
                SELECT_NONE_VALUE, SELECT_NONE_VALUE, SELECT_NONE_VALUE, SELECT_NONE_VALUE, SELECT_NONE_VALUE);
        UpdateDataModel updateDataModel = UpdateDataModelBuilder.build(multipleData, caseData);
        assertNull(updateDataModel.getRepresentedType());
    }

    @Test
    void testRespondentRepresentativeSelected() {
        MultipleData multipleData = createMultipleData(
                SELECT_NONE_VALUE, SELECT_NONE_VALUE, SELECT_NONE_VALUE,
                SELECT_NONE_VALUE, RESPONDENT_REPRESENTATIVE_ID);
        UpdateDataModel updateDataModel = UpdateDataModelBuilder.build(multipleData, caseData);
        assertEquals(RESPONDENT_REPRESENTATIVE_NAME, updateDataModel.getRepresentedType().getNameOfRepresentative());
    }

    @Test
    void testUnknownSelectedRespondentRepresentativeIsIgnored() {
        MultipleData multipleData = createMultipleData(
                SELECT_NONE_VALUE, SELECT_NONE_VALUE, SELECT_NONE_VALUE,
                SELECT_NONE_VALUE, UNKNOWN_VALUE);
        UpdateDataModel updateDataModel = UpdateDataModelBuilder.build(multipleData, caseData);
        assertNull(updateDataModel.getRepresentedType());
    }

    @Test
    void build_withBatchCaseStayed() {
        MultipleData multipleData = createMultipleData(
                SELECT_NONE_VALUE, SELECT_NONE_VALUE, SELECT_NONE_VALUE,
                SELECT_NONE_VALUE, SELECT_NONE_VALUE);
        multipleData.setBatchCaseStayed(YES);
        UpdateDataModel updateDataModel = UpdateDataModelBuilder.build(multipleData, caseData);
        assertEquals(YES, updateDataModel.getBatchCaseStayed());
    }

    private MultipleData createMultipleData(String claimantRepresentative,
                                            String jurisdiction,
                                            String respondent,
                                            String judgement,
                                            String respondentRepresentative) {
        MultipleData multipleData = new MultipleData();
        multipleData.setBatchUpdateClaimantRep(new DynamicFixedListType(claimantRepresentative));
        multipleData.setBatchUpdateJurisdiction(new DynamicFixedListType(jurisdiction));
        multipleData.setBatchUpdateRespondent(new DynamicFixedListType(respondent));
        multipleData.setBatchUpdateJudgment(new DynamicFixedListType(judgement));
        multipleData.setBatchUpdateRespondentRep(new DynamicFixedListType(respondentRepresentative));

        return multipleData;
    }

    @Test
    void testSubMultiple() {
        MultipleData multipleData = createMultipleData(
                SELECT_NONE_VALUE, SELECT_NONE_VALUE, SELECT_NONE_VALUE, JUDGEMENT_ID, SELECT_NONE_VALUE);
        caseData.setSubMultipleName("SubMultiple");
        UpdateDataModel updateDataModel = UpdateDataModelBuilder.build(multipleData, caseData);
        assertEquals("SubMultiple", updateDataModel.getSubMultiple());
    }
}
