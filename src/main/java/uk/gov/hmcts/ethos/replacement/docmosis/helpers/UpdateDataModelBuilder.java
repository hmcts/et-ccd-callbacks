package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import uk.gov.hmcts.ecm.common.model.servicebus.datamodel.UpdateDataModel;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.JudgementTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.JurCodesTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.ListTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DynamicListType;
import uk.gov.hmcts.et.common.model.ccd.types.JudgementType;
import uk.gov.hmcts.et.common.model.ccd.types.JurCodesType;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;

import java.util.List;
import java.util.Optional;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

public final class UpdateDataModelBuilder {

    private UpdateDataModelBuilder() {
        // Access through static methods
    }

    public static UpdateDataModel build(MultipleData multipleData, CaseData caseData) {
        return UpdateDataModel.builder()
                .managingOffice(multipleData.getManagingOffice())
                .fileLocation(DynamicFixedListType.getSelectedValue(multipleData.getFileLocation()).orElse(null))
                .fileLocationGlasgow(DynamicFixedListType.getSelectedValue(
                        multipleData.getFileLocationGlasgow()).orElse(null))
                .fileLocationAberdeen(DynamicFixedListType.getSelectedValue(
                        multipleData.getFileLocationAberdeen()).orElse(null))
                .fileLocationDundee(DynamicFixedListType.getSelectedValue(
                        multipleData.getFileLocationDundee()).orElse(null))
                .fileLocationEdinburgh(DynamicFixedListType.getSelectedValue(
                        multipleData.getFileLocationEdinburgh()).orElse(null))
                .clerkResponsible(DynamicFixedListType.getSelectedValue(
                        multipleData.getClerkResponsible()).orElse(null))
                .positionType(multipleData.getPositionType())
                .receiptDate(multipleData.getReceiptDate())
                .hearingStage(multipleData.getHearingStage())
                .subMultiple(getSubMultipleName(caseData))
                .isRespondentRepRemovalUpdate(multipleData.getBatchRemoveRespondentRep())
                .isClaimantRepRemovalUpdate(multipleData.getBatchRemoveClaimantRep())

                .representativeClaimantType(getRepresentativeClaimantType(multipleData, caseData))
                .jurCodesList(getJurCodesList(multipleData.getBatchUpdateJurisdictionList(), caseData))
                .respondentSumType(getRespondentSumType(multipleData, caseData))
                .judgementType(getJudgementType(multipleData, caseData))
                .representedType(getRespondentRepType(multipleData, caseData))
                .isFixCase(getFixCase(multipleData))
                .batchCaseStayed(multipleData.getBatchCaseStayed())
                .build();
    }

    private static String getFixCase(MultipleData multipleData) {
        if (isNullOrEmpty(multipleData.getIsFixCase())
            || NO.equals(multipleData.getIsFixCase())) {
            return NO;
        } else {
            return YES;
        }
    }

    private static String getSubMultipleName(CaseData caseData) {
        if (caseData == null || isNullOrEmpty(caseData.getSubMultipleName())) {
            return null;
        } else {
            return caseData.getSubMultipleName();
        }
    }

    private static List<JurCodesType> getJurCodesList(ListTypeItem<DynamicListType> batchUpdateJurisdictionsList,
        CaseData caseData) {
        if (batchUpdateJurisdictionsList == null) {
            return null;
        }
        return batchUpdateJurisdictionsList
               .map(jurCode -> getJurCodesType(jurCode.getDynamicList(), caseData))
               .toList();
    }

    private static JurCodesType getJurCodesType(DynamicFixedListType batchUpdateJurisdiction, CaseData caseData) {
        if (caseData == null) {
            return null;
        }

        if (batchUpdateJurisdiction == null) {
            return null;
        }

        DynamicValueType jurisdictions = batchUpdateJurisdiction.getValue();
        List<JurCodesTypeItem> jurCodesCollection = caseData.getJurCodesCollection();

        if (jurisdictions == null || jurCodesCollection == null) {
            return null;
        }

        String jurCodeToSearch = jurisdictions.getLabel();
        Optional<JurCodesTypeItem> jurCodesTypeItemOptional =
                jurCodesCollection.stream()
                        .filter(o -> o.getValue().getJuridictionCodesList().equals(jurCodeToSearch))
                        .findAny();

        if (jurCodesTypeItemOptional.isPresent()) {
            return jurCodesTypeItemOptional.get().getValue();
        }

        return null;
    }

    private static RespondentSumType getRespondentSumType(MultipleData multipleData, CaseData caseData) {
        if (caseData == null) {
            return null;
        }

        List<RespondentSumTypeItem> respondentCollection = caseData.getRespondentCollection();

        if (multipleData.getBatchUpdateRespondent().getValue() != null
                && respondentCollection != null) {
            String respondentToSearch = multipleData.getBatchUpdateRespondent().getValue().getLabel();
            Optional<RespondentSumTypeItem> respondentSumTypeItemOptional =
                    respondentCollection.stream()
                            .filter(respondentSumTypeItem ->
                                    respondentSumTypeItem.getValue().getRespondentName().equals(respondentToSearch))
                            .findAny();

            if (respondentSumTypeItemOptional.isPresent()) {
                return respondentSumTypeItemOptional.get().getValue();
            }
        }

        return null;
    }

    private static JudgementType getJudgementType(MultipleData multipleData, CaseData caseData) {
        if (caseData == null) {
            return null;
        }

        List<JudgementTypeItem> judgementCollection = caseData.getJudgementCollection();

        if (multipleData.getBatchUpdateJudgment().getValue() != null
                && judgementCollection != null) {
            String judgementIdToSearch = multipleData.getBatchUpdateJudgment().getValue().getCode();
            Optional<JudgementTypeItem> judgementTypeItemOptional =
                    judgementCollection.stream()
                            .filter(judgementTypeItem ->
                                    judgementTypeItem.getId().equals(judgementIdToSearch))
                            .findAny();

            if (judgementTypeItemOptional.isPresent()) {
                return judgementTypeItemOptional.get().getValue();
            }
        }

        return null;
    }

    public static RepresentedTypeR getRespondentRepType(MultipleData multipleData, CaseData caseData) {
        if (caseData == null) {
            return null;
        }
        List<RepresentedTypeRItem> repCollection = caseData.getRepCollection();

        if (multipleData.getBatchUpdateRespondentRep().getValue() != null
                && repCollection != null) {
            String respondentRepIdToSearch = multipleData.getBatchUpdateRespondentRep().getValue().getCode();
            Optional<RepresentedTypeRItem> representedTypeRItemOptional =
                    repCollection.stream()
                            .filter(representedTypeRItem ->
                                    representedTypeRItem.getId().equals(respondentRepIdToSearch))
                            .findAny();

            if (representedTypeRItemOptional.isPresent()) {
                return representedTypeRItemOptional.get().getValue();
            }
        }

        return null;
    }

    private static RepresentedTypeC getRepresentativeClaimantType(MultipleData multipleData, CaseData caseData) {
        if (caseData == null) {
            return null;
        }

        RepresentedTypeC representedTypeC = caseData.getRepresentativeClaimantType();
        if (multipleData.getBatchUpdateClaimantRep() != null && representedTypeC != null) {
            String claimantRepresentative = multipleData.getBatchUpdateClaimantRep().getValue().getCode();

            if (claimantRepresentative.equals(representedTypeC.getNameOfRepresentative())) {
                return representedTypeC;
            }
        }

        return null;
    }
}
