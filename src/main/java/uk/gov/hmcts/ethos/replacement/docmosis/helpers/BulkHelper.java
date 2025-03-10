package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.bulk.BulkData;
import uk.gov.hmcts.et.common.model.bulk.BulkDetails;
import uk.gov.hmcts.et.common.model.bulk.items.CaseIdTypeItem;
import uk.gov.hmcts.et.common.model.bulk.items.MultipleTypeItem;
import uk.gov.hmcts.et.common.model.bulk.items.SearchTypeItem;
import uk.gov.hmcts.et.common.model.bulk.items.SubMultipleTypeItem;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.MultipleType;
import uk.gov.hmcts.et.common.model.bulk.types.SearchType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.items.JurCodesTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.JurCodesType;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.FILE_EXTENSION;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.LIST_CASES;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.LIST_CASES_CONFIG;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MULTIPLE_SCHEDULE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MULTIPLE_SCHEDULE_CONFIG;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MULTIPLE_SCHEDULE_DETAILED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MULTIPLE_SCHEDULE_DETAILED_CONFIG;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NEW_LINE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.OUTPUT_FILE_NAME;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.PENDING_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SUBMITTED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.nullCheck;

@Slf4j
public final class BulkHelper {

    private static final String JURISDICTION_OUTCOME_ACAS_CONCILIATED_SETTLEMENT = "Acas conciliated settlement";
    private static final String JURISDICTION_OUTCOME_WITHDRAWN_OR_PRIVATE_SETTLEMENT =
            "Withdrawn or private settlement";
    private static final String JURISDICTION_OUTCOME_INPUT_IN_ERROR = "Input in error";
    private static final String JURISDICTION_OUTCOME_DISMISSED_ON_WITHDRAWAL = "Dismissed on withdrawal";
    static final List<String> HIDE_JURISDICTION_OUTCOME = Arrays.asList(
            JURISDICTION_OUTCOME_ACAS_CONCILIATED_SETTLEMENT,
            JURISDICTION_OUTCOME_WITHDRAWN_OR_PRIVATE_SETTLEMENT,
            JURISDICTION_OUTCOME_INPUT_IN_ERROR,
            JURISDICTION_OUTCOME_DISMISSED_ON_WITHDRAWAL);

    private BulkHelper() {
    }

    public static BulkDetails getMultipleCollection(BulkDetails bulkDetails,
                                                    List<MultipleTypeItem> multipleTypeItemList) {
        if (multipleTypeItemList != null && !multipleTypeItemList.isEmpty()) {
            bulkDetails.getCaseData().setMultipleCollectionCount(String.valueOf(multipleTypeItemList.size()));
            bulkDetails.getCaseData().setMultipleCollection(multipleTypeItemList);
        } else {
            bulkDetails.getCaseData().setMultipleCollection(new ArrayList<>());
            bulkDetails.getCaseData().setMultipleCollectionCount(null);
        }
        bulkDetails.getCaseData().setCaseIdCollection(BulkHelper.getCaseIdTypeItems(bulkDetails,
                BulkHelper.getMultipleCaseIds(bulkDetails)));
        return bulkDetails;
    }

    public static BulkDetails clearSearchCollection(BulkDetails bulkDetails) {
        bulkDetails.getCaseData().setSearchCollection(new ArrayList<>());
        bulkDetails.getCaseData().setSearchCollectionCount(null);
        return bulkDetails;
    }

    private static void setClaimantSurnameM(CaseData caseData, MultipleType multipleType) {
        if (caseData.getClaimantIndType() != null && caseData.getClaimantIndType().getClaimantLastName() != null) {
            multipleType.setClaimantSurnameM(caseData.getClaimantIndType().getClaimantLastName());
        } else {
            multipleType.setClaimantSurnameM(" ");
        }
    }

    private static void setClaimantAddressLine1M(CaseData caseData, MultipleType multipleType) {
        if (caseData.getClaimantType() != null && caseData.getClaimantType().getClaimantAddressUK() != null
                && caseData.getClaimantType().getClaimantAddressUK().getAddressLine1() != null) {
            multipleType.setClaimantAddressLine1M(caseData.getClaimantType().getClaimantAddressUK().getAddressLine1());
        } else {
            multipleType.setClaimantAddressLine1M(" ");
        }
    }

    private static void setRespondentSurnameM(CaseData caseData, MultipleType multipleType) {
        if (caseData.getRespondentCollection() != null && !caseData.getRespondentCollection().isEmpty()) {
            RespondentSumType respondentSumType = caseData.getRespondentCollection().get(0).getValue();
            multipleType.setRespondentSurnameM(respondentSumType.getRespondentName());
        } else {
            multipleType.setRespondentSurnameM(" ");
        }
    }

    private static void setClaimantPostCodeM(CaseData caseData, MultipleType multipleType) {
        if (caseData.getClaimantType() != null && caseData.getClaimantType().getClaimantAddressUK() != null
                && caseData.getClaimantType().getClaimantAddressUK().getPostCode() != null) {
            multipleType.setClaimantPostCodeM(caseData.getClaimantType().getClaimantAddressUK().getPostCode());
        } else {
            multipleType.setClaimantPostCodeM(" ");
        }
    }

    private static MultipleType getMultipleTypeFromCaseData(CaseData caseData) {
        MultipleType multipleType = new MultipleType();
        multipleType.setEthosCaseReferenceM(Optional.ofNullable(caseData.getEthosCaseReference()).orElse(" "));
        if (caseData.getClerkResponsible() != null && caseData.getClerkResponsible().getValue() != null) {
            multipleType.setClerkRespM(caseData.getClerkResponsible().getSelectedLabel());
        } else {
            multipleType.setClerkRespM(" ");
        }
        setClaimantSurnameM(caseData, multipleType);
        setClaimantAddressLine1M(caseData, multipleType);
        setClaimantPostCodeM(caseData, multipleType);
        setRespondentSurnameM(caseData, multipleType);
        if (caseData.getRespondentCollection() != null && !caseData.getRespondentCollection().isEmpty()
                && caseData.getRespondentCollection().get(0).getValue().getRespondentAddress() != null
                && caseData.getRespondentCollection().get(0).getValue()
                .getRespondentAddress().getAddressLine1() != null) {
            RespondentSumType respondentSumType = caseData.getRespondentCollection().get(0).getValue();
            multipleType.setRespondentAddressLine1M(respondentSumType.getRespondentAddress().getAddressLine1());
        } else {
            multipleType.setRespondentAddressLine1M(" ");
        }
        if (caseData.getRespondentCollection() != null && !caseData.getRespondentCollection().isEmpty()
                && caseData.getRespondentCollection().get(0).getValue().getRespondentAddress() != null
                && caseData.getRespondentCollection().get(0).getValue().getRespondentAddress().getPostCode() != null) {
            RespondentSumType respondentSumType = caseData.getRespondentCollection().get(0).getValue();
            multipleType.setRespondentPostCodeM(respondentSumType.getRespondentAddress().getPostCode());
        } else {
            multipleType.setRespondentPostCodeM(" ");
        }
        if (caseData.getRepresentativeClaimantType() != null
                && caseData.getRepresentativeClaimantType().getNameOfRepresentative() != null) {
            multipleType.setClaimantRepM(caseData.getRepresentativeClaimantType().getNameOfRepresentative());
            multipleType.setClaimantRepOrgM(caseData.getRepresentativeClaimantType().getNameOfOrganisation());
        } else {
            multipleType.setClaimantRepM(" ");
            multipleType.setClaimantRepOrgM(" ");
        }
        if (caseData.getRepCollection() != null
                && !caseData.getRepCollection().isEmpty()
                && caseData.getRepCollection().get(0).getValue() != null) {
            multipleType.setRespondentRepM(caseData.getRepCollection().get(0).getValue().getNameOfRepresentative());
            multipleType.setRespondentRepOrgM(caseData.getRepCollection().get(0).getValue().getNameOfOrganisation());
        } else {
            multipleType.setRespondentRepM(" ");
            multipleType.setRespondentRepOrgM(" ");
        }

        multipleType.setFileLocM(DynamicFixedListType.getSelectedLabel(caseData.getFileLocation()).orElse(" "));
        multipleType.setReceiptDateM(Optional.ofNullable(caseData.getReceiptDate()).orElse(" "));
        multipleType.setPositionTypeM(Optional.ofNullable(caseData.getPositionType()).orElse(" "));
        multipleType.setFeeGroupReferenceM(Optional.ofNullable(caseData.getFeeGroupReference()).orElse(" "));
        multipleType.setJurCodesCollectionM(getJurCodesCollection(caseData.getJurCodesCollection()));
        multipleType.setSubMultipleM(" ");
        multipleType.setSubMultipleTitleM(" ");
        multipleType.setCurrentPositionM(Optional.ofNullable(caseData.getPositionType()).orElse(" "));
        multipleType.setFlag1M(Optional.ofNullable(caseData.getFlag1()).orElse(" "));
        multipleType.setFlag2M(Optional.ofNullable(caseData.getFlag2()).orElse(" "));
        multipleType.setEqpm(Optional.ofNullable(caseData.getEqp()).orElse(" "));
        multipleType.setLeadClaimantM(Optional.ofNullable(caseData.getLeadClaimant()).orElse(NO));
        return multipleType;
    }

    public static List<MultipleTypeItem> getMultipleTypeListBySubmitEventList(List<SubmitEvent> submitEvents,
                                                                              String multipleReference) {
        List<MultipleTypeItem> multipleTypeItemList = new ArrayList<>();
        for (SubmitEvent submitEvent : submitEvents) {
            CaseData caseData = submitEvent.getCaseData();
            MultipleType multipleType = getMultipleTypeFromCaseData(caseData);
            multipleType.setCaseIDM(String.valueOf(submitEvent.getCaseId()));
            multipleType.setMultipleReferenceM(Optional.ofNullable(multipleReference).orElse(" "));
            multipleType.setStateM(getSubmitEventState(submitEvent));

            MultipleTypeItem multipleTypeItem = new MultipleTypeItem();
            multipleTypeItem.setId(String.valueOf(submitEvent.getCaseId()));
            multipleTypeItem.setValue(multipleType);
            multipleTypeItemList.add(multipleTypeItem);
        }
        return multipleTypeItemList;
    }

    private static String getSubmitEventState(SubmitEvent submitEvent) {
        String state = submitEvent.getState();
        if (state != null) {
            if (state.equals(PENDING_STATE)) {
                return SUBMITTED_STATE;
            } else {
                return state;
            }
        } else {
            return " ";
        }
    }

    public static SearchType getSearchTypeFromMultipleType(MultipleType multipleType) {
        SearchType searchType = new SearchType();
        searchType.setCaseIDS(multipleType.getCaseIDM());
        searchType.setEthosCaseReferenceS(multipleType.getEthosCaseReferenceM());
        searchType.setLeadClaimantS(multipleType.getLeadClaimantM());
        searchType.setClerkRespS(multipleType.getClerkRespM());
        searchType.setClaimantSurnameS(multipleType.getClaimantSurnameM());
        searchType.setRespondentSurnameS(multipleType.getRespondentSurnameM());
        searchType.setClaimantRepS(multipleType.getClaimantRepM());
        searchType.setRespondentRepS(multipleType.getRespondentRepM());
        searchType.setFileLocS(multipleType.getFileLocM());
        searchType.setReceiptDateS(multipleType.getReceiptDateM());
        searchType.setPositionTypeS(multipleType.getPositionTypeM());
        searchType.setFeeGroupReferenceS(multipleType.getFeeGroupReferenceM());
        searchType.setJurCodesCollectionS(multipleType.getJurCodesCollectionM());
        searchType.setStateS(multipleType.getStateM());
        searchType.setCurrentPositionS(multipleType.getCurrentPositionM());
        searchType.setClaimantAddressLine1S(multipleType.getClaimantAddressLine1M());
        searchType.setClaimantPostCodeS(multipleType.getClaimantPostCodeM());
        searchType.setRespondentAddressLine1S(multipleType.getRespondentAddressLine1M());
        searchType.setRespondentPostCodeS(multipleType.getRespondentPostCodeM());
        searchType.setFlag1S(multipleType.getFlag1M());
        searchType.setFlag2S(multipleType.getFlag2M());
        searchType.setEqps(multipleType.getEqpm());
        searchType.setRespondentRepOrgS(multipleType.getRespondentRepOrgM());
        searchType.setClaimantRepOrgS(multipleType.getClaimantRepOrgM());
        return searchType;
    }

    public static MultipleType getMultipleTypeFromSubmitEvent(SubmitEvent submitEvent) {
        CaseData caseData = submitEvent.getCaseData();
        MultipleType multipleType = getMultipleTypeFromCaseData(caseData);
        multipleType.setCaseIDM(String.valueOf(submitEvent.getCaseId()));
        multipleType.setMultipleReferenceM(isNullOrEmpty(
                caseData.getMultipleReference()) ? " " : caseData.getMultipleReference());
        multipleType.setStateM(isNullOrEmpty(submitEvent.getState()) ? " " : submitEvent.getState());
        return multipleType;
    }

    public static MultipleTypeItem getMultipleTypeItemFromSubmitEvent(SubmitEvent submitEvent,
                                                                      String multipleReference) {
        MultipleTypeItem multipleTypeItem = new MultipleTypeItem();
        multipleTypeItem.setId(String.valueOf(submitEvent.getCaseId()));
        MultipleType multipleType = BulkHelper.getMultipleTypeFromSubmitEvent(submitEvent);
        multipleType.setMultipleReferenceM(multipleReference);
        multipleTypeItem.setValue(multipleType);
        return multipleTypeItem;
    }

    static String getJurCodesCollection(List<JurCodesTypeItem> jurCodesTypeItems) {
        if (jurCodesTypeItems != null) {
            return jurCodesTypeItems.stream()
                    .map(jurCodesTypeItem -> jurCodesTypeItem.getValue().getJuridictionCodesList())
                    .distinct()
                    .collect(Collectors.joining(", "));
        } else {
            return " ";
        }
    }

    static String getJurCodesCollectionWithHide(List<JurCodesTypeItem> jurCodesTypeItems) {
        if (CollectionUtils.isNotEmpty(jurCodesTypeItems)) {
            return StringUtils.defaultIfEmpty(
                    jurCodesTypeItems.stream()
                        .filter(jurCodesTypeItem ->
                                !HIDE_JURISDICTION_OUTCOME.contains(jurCodesTypeItem.getValue().getJudgmentOutcome()))
                        .map(jurCodesTypeItem -> jurCodesTypeItem.getValue().getJuridictionCodesList())
                        .distinct()
                        .collect(Collectors.joining(", ")),
                    " ");
        } else {
            return " ";
        }
    }

    public static List<String> getCaseIds(BulkDetails bulkDetails) {
        if (bulkDetails.getCaseData().getCaseIdCollection() != null
                && !bulkDetails.getCaseData().getCaseIdCollection().isEmpty()) {
            return bulkDetails.getCaseData().getCaseIdCollection().stream()
                    .filter(key -> key.getId() != null && !key.getId().equals("null"))
                    .map(caseId -> caseId.getValue().getEthosCaseReference())
                    .distinct()
                    .toList();
        } else {
            return new ArrayList<>();
        }
    }

    public static List<String> getEthosRefNumsFromSearchCollection(List<SearchTypeItem> searchTypeItems) {
        return searchTypeItems.stream()
                .filter(key -> key.getId() != null)
                .map(caseId -> caseId.getValue().getEthosCaseReferenceS())
                .distinct()
                .toList();
    }

    private static List<CaseIdTypeItem> getCaseIdTypeItems(BulkDetails bulkDetails, List<String> multipleTypeItems) {
        return bulkDetails.getCaseData().getCaseIdCollection() != null
                ? bulkDetails.getCaseData().getCaseIdCollection().stream()
                .filter(p -> p.getValue().getEthosCaseReference() != null)
                .filter(distinctByKey(p -> p.getValue().getEthosCaseReference()))
                .filter(p -> multipleTypeItems.contains(p.getValue().getEthosCaseReference()))
                .toList()
                : new ArrayList<>();
    }

    public static List<String> getMultipleCaseIds(BulkDetails bulkDetails) {
        return bulkDetails.getCaseData().getMultipleCollection() != null
                ? bulkDetails.getCaseData().getMultipleCollection().stream()
                .map(caseId -> caseId.getValue().getEthosCaseReferenceM())
                .distinct()
                .toList()
                : new ArrayList<>();
    }

    private static List<String> getJurCodesValues(List<JurCodesTypeItem> jurCodesTypeItems) {
        return jurCodesTypeItems != null && !jurCodesTypeItems.isEmpty()
                ? jurCodesTypeItems.stream()
                .map(jurCodesTypeItem -> jurCodesTypeItem.getValue().getJuridictionCodesList())
                .distinct()
                .toList()
                : new ArrayList<>();
    }

    public static boolean containsAllJurCodes(List<JurCodesTypeItem> jurCodesTypeItems1,
                                              List<JurCodesTypeItem> jurCodesTypeItems2) {
        if (jurCodesTypeItems1 != null && !jurCodesTypeItems1.isEmpty()) {
            return getJurCodesValues(jurCodesTypeItems2).containsAll(getJurCodesValues(jurCodesTypeItems1));
        }
        return false;
    }

    public static List<JurCodesTypeItem> getJurCodesListFromString(String jurCodesStringList) {
        List<JurCodesTypeItem> jurCodesTypeItems = new ArrayList<>();
        if (jurCodesStringList != null && !jurCodesStringList.trim().equals("")) {
            List<String> codes = new ArrayList<>(Arrays.asList(jurCodesStringList.split(", ")));
            jurCodesTypeItems = codes.stream()
                    .map(code -> {
                        JurCodesType jurCodesType = new JurCodesType();
                        jurCodesType.setJuridictionCodesList(code);
                        JurCodesTypeItem jurCodesTypeItem = new JurCodesTypeItem();
                        jurCodesTypeItem.setValue(jurCodesType);
                        jurCodesTypeItem.setId(code);
                        return jurCodesTypeItem;
                    })
                    .toList();
        }
        return jurCodesTypeItems;
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
        Map<Object, Boolean> map = new ConcurrentHashMap<>();
        return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    public static StringBuilder buildScheduleDocumentContent(BulkData bulkData, String accessKey) {
        return new StringBuilder().append("{\n").append("\"accessKey\":\"").append(accessKey).append(NEW_LINE)
                .append("\"templateName\":\"").append(BulkHelper.getScheduleDocName(bulkData.getScheduleDocName()))
                .append(FILE_EXTENSION).append(NEW_LINE).append("\"outputName\":\"").append(OUTPUT_FILE_NAME)
                .append(NEW_LINE).append("\"data\":{\n\"Multiple_No\":\"").append(bulkData.getMultipleReference())
                .append(NEW_LINE).append("\"Multiple_title\":\"").append(bulkData.getBulkCaseTitle()).append(NEW_LINE)
                .append(getDocumentData(bulkData)).append("\"Today_date\":\"")
                .append(UtilHelper.formatCurrentDate(LocalDate.now())).append("\"\n}\n}\n");
    }

    private static StringBuilder getDocumentData(BulkData bulkData) {
        if (LIST_CASES_CONFIG.equals(bulkData.getScheduleDocName())) {
            return getScheduleBySubMultipleData(bulkData);
        } else if (Arrays.asList(MULTIPLE_SCHEDULE_CONFIG, MULTIPLE_SCHEDULE_DETAILED_CONFIG)
                .contains(bulkData.getScheduleDocName())) {
            return getScheduleData(bulkData.getSearchCollection());
        } else {
            return new StringBuilder();
        }
    }

    private static StringBuilder getScheduleData(List<SearchTypeItem> searchTypeItems) {
        StringBuilder sb = new StringBuilder(25);
        sb.append("\"multiple\":[\n");
        for (int i = 0; i < searchTypeItems.size(); i++) {
            sb.append(getMultipleTypeRow(searchTypeItems.get(i).getValue()));
            if (i != searchTypeItems.size() - 1) {
                sb.append(",\n");
            }
        }
        sb.append("],\n");
        return sb;
    }

    private static StringBuilder getMultipleTypeRow(SearchType searchType) {
        StringBuilder sb = new StringBuilder(220);
        sb.append("{\"Claimant\":\"").append(nullCheck(searchType.getClaimantSurnameS())).append(NEW_LINE)
                .append("\"Current_position\":\"").append(nullCheck(searchType.getCurrentPositionS())).append(NEW_LINE)
                .append("\"Case_No\":\"").append(nullCheck(searchType.getEthosCaseReferenceS())).append(NEW_LINE)
                .append("\"claimant_full_name\":\"").append(nullCheck(searchType.getClaimantSurnameS()))
                .append(NEW_LINE).append("\"claimant_addressLine1\":\"")
                .append(nullCheck(searchType.getClaimantAddressLine1S())).append(NEW_LINE)
                .append("\"claimant_postCode\":\"").append(nullCheck(searchType.getClaimantPostCodeS()))
                .append(NEW_LINE).append("\"respondent_full_name\":\"")
                .append(nullCheck(searchType.getRespondentSurnameS())).append(NEW_LINE)
                .append("\"respondent_addressLine1\":\"").append(nullCheck(searchType.getRespondentAddressLine1S()))
                .append(NEW_LINE).append("\"respondent_postCode\":\"")
                .append(nullCheck(searchType.getRespondentPostCodeS())).append("\"}");
        return sb;
    }

    private static StringBuilder getScheduleBySubMultipleData(BulkData bulkData) {
        StringBuilder sb = new StringBuilder();
        Map<String, List<SearchType>> multipleMap = getSearchedCasesBySubMultipleRefMap(bulkData);
        if (!multipleMap.isEmpty()) {
            sb.append("\"subMultiple\":[\n");
            Iterator<Map.Entry<String, List<SearchType>>> entries = new TreeMap<>(multipleMap).entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry<String, List<SearchType>> subMultipleEntry = entries.next();
                sb.append("{\"SubMultiple_No\":\"").append(subMultipleEntry.getKey()).append(NEW_LINE)
                        .append("\"SubMultiple_title\":\"")
                        .append(getSubMultipleTitle(subMultipleEntry.getKey(), bulkData)).append(NEW_LINE)
                        .append("\"multiple\":[\n");
                for (int i = 0; i < subMultipleEntry.getValue().size(); i++) {
                    sb.append(getMultipleTypeRow(subMultipleEntry.getValue().get(i)));
                    if (i != subMultipleEntry.getValue().size() - 1) {
                        sb.append(",\n");
                    }
                }
                sb.append("]\n");
                if (entries.hasNext()) {
                    sb.append("},\n");
                } else {
                    sb.append("}],\n");
                }
            }
        }
        return sb;
    }

    private static Map<String, List<SearchType>> getSearchedCasesBySubMultipleRefMap(BulkData bulkData) {
        Map<String, List<SearchType>> multipleMap = new ConcurrentHashMap<>();
        for (SearchTypeItem searchTypeItem : bulkData.getSearchCollection()) {
            if (bulkData.getMultipleCollection() != null) {
                for (MultipleTypeItem multipleTypeItem : bulkData.getMultipleCollection()) {
                    if (searchTypeItem.getValue().getEthosCaseReferenceS()
                            .equals(multipleTypeItem.getValue().getEthosCaseReferenceM())
                            && multipleTypeItem.getValue().getSubMultipleM() != null
                            && !multipleTypeItem.getValue().getSubMultipleM().equals(" ")) {
                        multipleMap.computeIfAbsent(multipleTypeItem.getValue()
                                .getSubMultipleM(), k -> new ArrayList<>()).add(searchTypeItem.getValue());
                    }
                }
            }
        }
        return multipleMap;
    }

    public static String getSubMultipleTitle(String subMultipleRef, BulkData bulkData) {
        Optional<SubMultipleTypeItem> subMultipleTypeItem = bulkData.getSubMultipleCollection().stream()
                .filter(subMultiple -> subMultiple.getValue().getSubMultipleRefT().equals(subMultipleRef))
                .findFirst();
        if (subMultipleTypeItem.isPresent()) {
            return subMultipleTypeItem.get().getValue().getSubMultipleNameT();
        }
        return " ";
    }

    public static String getScheduleDocName(String scheduleDocName) {
        if (scheduleDocName.equals(LIST_CASES_CONFIG)) {
            return LIST_CASES;
        } else if (scheduleDocName.equals(MULTIPLE_SCHEDULE_CONFIG)) {
            return MULTIPLE_SCHEDULE;
        } else {
            return MULTIPLE_SCHEDULE_DETAILED;
        }
    }

    public static List<SubmitEvent> calculateLeadCase(List<SubmitEvent> submitEvents, List<String> caseIds) {
        for (String caseId : caseIds) {
            int index = submitEvents.stream()
                    .map(submitEvent -> submitEvent.getCaseData().getEthosCaseReference())
                    .toList()
                    .indexOf(caseId);
            if (index != -1) {
                SubmitEvent submitEvent = submitEvents.get(index);
                log.info("setLeadClaimant is set to Yes for case: "
                        + submitEvent.getCaseData().getEthosCaseReference());
                submitEvent.getCaseData().setLeadClaimant(YES);
                submitEvents.remove(index);
                submitEvents.add(0, submitEvent);
                return submitEvents;
            }
        }
        return submitEvents;
    }
}
