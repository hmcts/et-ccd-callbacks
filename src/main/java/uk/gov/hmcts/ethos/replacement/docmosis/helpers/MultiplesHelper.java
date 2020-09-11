package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.ecm.common.model.bulk.items.CaseIdTypeItem;
import uk.gov.hmcts.ecm.common.model.bulk.types.CaseType;
import uk.gov.hmcts.ecm.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.ecm.common.model.multiples.MultipleData;
import uk.gov.hmcts.ecm.common.model.multiples.MultipleObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.ecm.common.model.multiples.MultipleConstants.*;

@Slf4j
public class MultiplesHelper {

    public static List<String> HEADERS = new ArrayList<>(Arrays.asList(HEADER_1, HEADER_2, HEADER_3, HEADER_4, HEADER_5, HEADER_6));
    public static String SELECT_ALL = "All";

    public static List<String> getCaseIds(MultipleData multipleData) {

        if (multipleData.getCaseIdCollection() != null
                && !multipleData.getCaseIdCollection().isEmpty()) {

            return multipleData.getCaseIdCollection().stream()
                    .filter(key -> key.getId() != null && !key.getId().equals("null"))
                    .map(caseId -> caseId.getValue().getEthosCaseReference())
                    .distinct()
                    .collect(Collectors.toList());

        } else {

            return new ArrayList<>();

        }
    }

    public static String getLeadFromCaseIds(MultipleData multipleData) {

        List<String> caseIds = getCaseIds(multipleData);

        if (caseIds.isEmpty()) {

            return "";

        } else {

            return caseIds.get(0);

        }

    }

    public static void removeCaseIds(MultipleData multipleData, List<String> multipleObjectsFiltered) {

        List<CaseIdTypeItem> newCaseIdCollection = new ArrayList<>();

        if (multipleData.getCaseIdCollection() != null
                && !multipleData.getCaseIdCollection().isEmpty()) {

            newCaseIdCollection = multipleData.getCaseIdCollection().stream()
                    .filter(key -> key.getId() != null && !key.getId().equals("null"))
                    .filter(caseId -> !multipleObjectsFiltered.contains(caseId.getValue().getEthosCaseReference()))
                    .distinct()
                    .collect(Collectors.toList());

        }

        multipleData.setCaseIdCollection(newCaseIdCollection);

    }

    public static void addCaseIds(MultipleData multipleData, List<String> multipleObjectsFiltered) {

        List<CaseIdTypeItem> caseIdCollectionToAdd = new ArrayList<>();

        for (String ethosCaseReference : multipleObjectsFiltered) {

            caseIdCollectionToAdd.add(createCaseIdTypeItem(ethosCaseReference));
        }

        multipleData.getCaseIdCollection().addAll(caseIdCollectionToAdd);

    }

    public static CaseIdTypeItem createCaseIdTypeItem(String ethosCaseReference) {

        CaseType caseType = new CaseType();
        caseType.setEthosCaseReference(ethosCaseReference);
        CaseIdTypeItem caseIdTypeItem = new CaseIdTypeItem();
        caseIdTypeItem.setId(ethosCaseReference);
        caseIdTypeItem.setValue(caseType);

        return caseIdTypeItem;
    }

    public static MultipleObject createMultipleObject(String ethosCaseReference, String subMultiple) {

        return MultipleObject.builder()
                .ethosCaseRef(ethosCaseReference)
                .subMultiple(subMultiple)
                .flag1("")
                .flag2("")
                .flag3("")
                .flag4("")
                .build();
    }

    public static String getExcelBinaryUrl(MultipleData multipleData) {
        return multipleData.getCaseImporterFile().getUploadedDocument().getDocumentBinaryUrl();
    }

    public static void resetMidFields(MultipleData multipleData) {

        multipleData.setFlag1(null);
        multipleData.setFlag2(null);
        multipleData.setFlag3(null);
        multipleData.setFlag4(null);

        multipleData.setManagingOffice(null);
        multipleData.setFileLocation(null);
        multipleData.setFileLocationGlasgow(null);
        multipleData.setFileLocationAberdeen(null);
        multipleData.setFileLocationDundee(null);
        multipleData.setFileLocationEdinburgh(null);
        multipleData.setClerkResponsible(null);
        multipleData.setPositionType(null);
        multipleData.setReceiptDate(null);
        multipleData.setHearingStage(null);

        multipleData.setBatchUpdateCase(null);
        multipleData.setBatchUpdateType(null);

        multipleData.setMoveCases(null);

    }

    public static DynamicValueType getDynamicValue(String value) {

        DynamicValueType dynamicValueType = new DynamicValueType();
        dynamicValueType.setCode(value);
        dynamicValueType.setLabel(value);

        return dynamicValueType;

    }
}
