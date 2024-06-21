package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.ecm.common.model.helper.SchedulePayload;
import uk.gov.hmcts.et.common.model.bulk.items.CaseIdTypeItem;
import uk.gov.hmcts.et.common.model.bulk.types.CaseType;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleObject;
import uk.gov.hmcts.et.common.model.multiples.items.CaseMultipleTypeItem;
import uk.gov.hmcts.et.common.model.multiples.items.SubMultipleTypeItem;
import uk.gov.hmcts.et.common.model.multiples.types.SubMultipleType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.ET1_ONLINE_CASE_SOURCE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MANUALLY_CREATED_POSITION;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MIGRATION_CASE_SOURCE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.et.common.model.multiples.MultipleConstants.HEADER_1;
import static uk.gov.hmcts.et.common.model.multiples.MultipleConstants.HEADER_2;
import static uk.gov.hmcts.et.common.model.multiples.MultipleConstants.HEADER_3;
import static uk.gov.hmcts.et.common.model.multiples.MultipleConstants.HEADER_4;
import static uk.gov.hmcts.et.common.model.multiples.MultipleConstants.HEADER_5;
import static uk.gov.hmcts.et.common.model.multiples.MultipleConstants.HEADER_6;

@Slf4j
public final class MultiplesHelper {

    public static final List<String> HEADERS = new ArrayList<>(Arrays.asList(
            HEADER_1, HEADER_2, HEADER_3, HEADER_4, HEADER_5, HEADER_6));
    public static final String SELECT_ALL = "All";
    public static final String MULTIPLE_SUFFIX = "_Multiple";

    private MultiplesHelper() {
    }

    public static List<String> getCaseIds(MultipleData multipleData) {
        if (collectionHasValues(multipleData.getCaseIdCollection())) {
            return multipleData.getCaseIdCollection().stream()
                    .filter(key -> key.getId() != null && !key.getId().equals("null"))
                    .map(caseId -> caseId.getValue().getEthosCaseReference())
                    .distinct()
                    .toList();

        } else {
            return new ArrayList<>();
        }
    }

    public static List<String> getCaseIdsFromCollection(List<CaseIdTypeItem> caseIdCollection) {
        if (collectionIsEmpty(caseIdCollection)) {
            return new ArrayList<>();
        }

        return caseIdCollection.stream()
                .filter(key -> key.getId() != null && !key.getId().equals("null"))
                .map(caseId -> caseId.getValue().getEthosCaseReference())
                .distinct()
                .toList();
    }

    // MID EVENTS COLLECTIONS HAVE KEY AS NULL BUT WITH VALUES!
    public static List<String> getCaseIdsForMidEvent(List<CaseIdTypeItem> caseIdCollection) {
        if (collectionIsEmpty(caseIdCollection)) {
            return new ArrayList<>();
        }

        return caseIdCollection.stream()
                .filter(caseId -> caseId.getValue().getEthosCaseReference() != null)
                .map(caseId -> caseId.getValue().getEthosCaseReference())
                .distinct()
                .toList();
    }

    public static List<CaseIdTypeItem> filterDuplicatedAndEmptyCaseIds(MultipleData multipleData) {

        if (collectionHasValues(multipleData.getCaseIdCollection())) {

            return multipleData.getCaseIdCollection().stream()
                    .filter(caseId ->
                            caseId.getValue().getEthosCaseReference() != null
                                    && !caseId.getValue().getEthosCaseReference().trim().isEmpty())
                    .filter(distinctByValue(CaseIdTypeItem::getValue))
                    .distinct()
                    .toList();

        } else {

            return new ArrayList<>();

        }
    }

    private static <T> Predicate<T> distinctByValue(Function<? super T, Object> keyExtractor) {

        Map<Object, Boolean> map = new ConcurrentHashMap<>();

        return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;

    }

    public static String getLeadFromCaseIds(MultipleData multipleData) {

        List<String> caseIds = getCaseIds(multipleData);

        if (caseIds.isEmpty()) {

            return "";

        } else {

            return caseIds.get(0);

        }

    }

    public static String getLeadFromCaseMultipleCollection(MultipleData multipleData) {

        List<CaseMultipleTypeItem> caseMultipleTypeItemList = multipleData.getCaseMultipleCollection();

        if (caseMultipleTypeItemList.isEmpty()) {

            return "";

        } else {

            return caseMultipleTypeItemList.get(0).getValue().getEthosCaseRef();

        }

    }

    public static void addLeadToCaseIds(MultipleData multipleData, String leadCase) {

        CaseIdTypeItem caseIdTypeItem = createCaseIdTypeItem(leadCase);

        if (multipleData.getCaseIdCollection() == null) {

            multipleData.setCaseIdCollection(new ArrayList<>(Collections.singletonList(caseIdTypeItem)));

        } else {

            multipleData.getCaseIdCollection().add(0, caseIdTypeItem);

        }

    }

    public static CaseIdTypeItem createCaseIdTypeItem(String ethosCaseReference) {

        CaseType caseType = new CaseType();
        caseType.setEthosCaseReference(ethosCaseReference);
        CaseIdTypeItem caseIdTypeItem = new CaseIdTypeItem();
        caseIdTypeItem.setId(UUID.randomUUID().toString());
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
        multipleData.setSubMultiple(null);

        multipleData.setFileLocation(null);
        multipleData.setFileLocationGlasgow(null);
        multipleData.setFileLocationAberdeen(null);
        multipleData.setFileLocationDundee(null);
        multipleData.setFileLocationEdinburgh(null);
        multipleData.setClerkResponsible(null);
        multipleData.setPositionType(null);
        multipleData.setReceiptDate(null);
        multipleData.setHearingStage(null);
        multipleData.setNotes(null);

        multipleData.setBatchUpdateCase(null);
        multipleData.setBatchUpdateType(null);

        multipleData.setMoveCases(null);
        multipleData.setSubMultipleAction(null);
        multipleData.setScheduleDocName(null);

        multipleData.setBatchUpdateRespondent(null);
        multipleData.setBatchUpdateJurisdiction(null);
        multipleData.setBatchUpdateClaimantRep(null);
        multipleData.setBatchUpdateJudgment(null);
        multipleData.setBatchUpdateRespondentRep(null);

        multipleData.setCorrespondenceType(null);
        multipleData.setCorrespondenceScotType(null);
        multipleData.setAddressLabelsSelectionTypeMSL(null);
        multipleData.setAddressLabelCollection(null);
        multipleData.setAddressLabelsAttributesType(null);

        multipleData.setPreAcceptCase(null);

        multipleData.setOfficeMultipleCT(null);
        multipleData.setPositionTypeCT(null);
        multipleData.setBatchCaseStayed(null);
    }

    public static SubMultipleTypeItem createSubMultipleTypeItem(String subMultipleReference, String subMultipleName) {

        SubMultipleType subMultipleType = new SubMultipleType();
        subMultipleType.setSubMultipleName(subMultipleName);
        subMultipleType.setSubMultipleRef(subMultipleReference);

        SubMultipleTypeItem subMultipleTypeItem = new SubMultipleTypeItem();
        subMultipleTypeItem.setId(subMultipleReference);
        subMultipleTypeItem.setValue(subMultipleType);

        return subMultipleTypeItem;

    }

    public static void addSubMultipleTypeToCase(MultipleData multipleData, SubMultipleTypeItem subMultipleTypeItem) {

        List<SubMultipleTypeItem> subMultipleTypeItems = multipleData.getSubMultipleCollection();

        if (subMultipleTypeItems != null) {

            subMultipleTypeItems.add(subMultipleTypeItem);

        } else {

            subMultipleTypeItems = new ArrayList<>(Collections.singletonList(subMultipleTypeItem));

        }

        multipleData.setSubMultipleCollection(subMultipleTypeItems);

    }

    public static List<String> generateSubMultipleStringCollection(MultipleData multipleData) {

        if (collectionHasValues(multipleData.getSubMultipleCollection())) {

            return multipleData.getSubMultipleCollection().stream()
                    .map(subMultipleTypeItem -> subMultipleTypeItem.getValue().getSubMultipleName())
                    .distinct()
                    .toList();

        } else {

            return new ArrayList<>();

        }

    }

    public static String generateMarkUp(String ccdGatewayBaseUrl, String caseId, String ethosCaseRef) {

        String url = ccdGatewayBaseUrl + "/cases/case-details/" + caseId;

        return "<a target=\"_blank\" href=\"" + url + "\">" + ethosCaseRef + "</a>";

    }

    public static String generateExcelDocumentName(MultipleData multipleData) {

        return multipleData.getMultipleName() + "-" + multipleData.getMultipleReference() + ".xlsx";

    }

    public static String getCurrentLead(String leadCaseLink) {

        return leadCaseLink != null && !leadCaseLink.isEmpty()
                ? leadCaseLink.substring(leadCaseLink.indexOf('>') + 1).replace("</a>", "")
                : "";

    }

    public static void updatePayloadMultiple(MultipleData multipleData) {

        if (multipleData.getMultipleSource().equals(ET1_ONLINE_CASE_SOURCE)
                && multipleData.getPreAcceptCase() == null) {
            multipleData.setPreAcceptDone(NO);
        }
        if ((multipleData.getMultipleSource().equals(MANUALLY_CREATED_POSITION)
                || multipleData.getMultipleSource().equals(MIGRATION_CASE_SOURCE))
                && multipleData.getPreAcceptCase() == null) {
            multipleData.setPreAcceptDone(YES);
        }
    }

    public static SortedMap<String, SortedMap<String, Object>> createCollectionOrderedByCaseRef(List<?> list) {
        SortedMap<String, SortedMap<String, Object>> orderedCollection = new TreeMap<>();

        for (Object item : list) {
            String ethosCaseRef;
            if (item instanceof String) {
                ethosCaseRef = (String) item;
            } else if (item instanceof MultipleObject) {
                ethosCaseRef = ((MultipleObject) item).getEthosCaseRef();
            } else if (item instanceof SchedulePayload) {
                ethosCaseRef = ((SchedulePayload) item).getEthosCaseRef();
            } else {
                log.info("unrecognised input object type: {}", item.getClass());
                break;
            }

            addObjectToCollectionOrderedByCaseRef(orderedCollection, item, ethosCaseRef);
        }

        return orderedCollection;
    }

    public static void addObjectToCollectionOrderedByCaseRef(SortedMap<String, SortedMap<String, Object>> collection,
                                                             Object item, String ethosCaseRef) {
        String[] caseRefParts = ethosCaseRef.split("/");

        if (collection.containsKey(caseRefParts[1])) {
            collection.get(caseRefParts[1]).put(caseRefParts[0], item);
        } else {
            collection.put(caseRefParts[1], new TreeMap<>(Map.of(caseRefParts[0], item)));
        }
    }

    public static byte[] writeExcelFileToByteArray(XSSFWorkbook workbook) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {
            workbook.write(bos);
            workbook.close();
        } catch (IOException e) {
            log.error("Error generating the excel");
            throw new RuntimeException("Error generating the excel", e);
        }

        return bos.toByteArray();
    }

    public static String removeMultipleSuffix(String caseTypeId) {
        return caseTypeId.replace(MULTIPLE_SUFFIX, "");
    }

    public static String appendMultipleSuffix(String caseTypeId) {
        return caseTypeId + MULTIPLE_SUFFIX;
    }

    public static String getListingMultipleCaseTypeId(String caseTypeId) {
        return UtilHelper.getListingCaseTypeId(caseTypeId) + MULTIPLE_SUFFIX;
    }

    private static <T> boolean collectionHasValues(List<T> collection) {
        return collection != null && !collection.isEmpty();
    }
    private static <T> boolean collectionIsEmpty(List<T> collection) {
        return !collectionHasValues(collection);
    }
}
