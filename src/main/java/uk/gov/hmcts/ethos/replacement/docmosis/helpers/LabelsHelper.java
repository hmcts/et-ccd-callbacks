package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.ecm.common.model.labels.LabelPayloadES;
import uk.gov.hmcts.ecm.common.model.labels.LabelPayloadEvent;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.AddressLabelTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.AddressLabelType;
import uk.gov.hmcts.et.common.model.ccd.types.AddressLabelsAttributesType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantIndType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantType;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ADDRESS_LABELS_COPIES_ERROR;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ADDRESS_LABELS_COPIES_LESS_10_ERROR;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ADDRESS_LABELS_LABELS_LIMIT_ERROR;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ADDRESS_LABELS_SELECT_ERROR;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ADDRESS_LABELS_TEMPLATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_ADDRESS_LABEL;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_REP;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_REP_ADDRESS_LABEL;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.INDIVIDUAL_TYPE_CLAIMANT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MULTIPLE_CASE_TYPE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.REF;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENTS_ADDRESS__LABEL;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENTS_REPS_ADDRESS__LABEL;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_REP;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SINGLE_CASE_TYPE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TEL;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.getActiveRespondents;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.getActiveRespondentsLabels;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.nullCheck;

@Slf4j
public final class LabelsHelper {

    public static final int MAX_NUMBER_LABELS = 2000;
    public static final String ADDRESS_LABELS_RESULT_SELECTION_ERROR =
            "There are no address labels associated with your selection";

    private LabelsHelper() {
    }

    public static AddressLabelTypeItem getClaimantAddressLabelData(LabelPayloadES labelPayloadES,
                                                                   String printClaimantLabel) {

        return getClaimantAddressLabel(labelPayloadES.getClaimantTypeOfClaimant(),
                labelPayloadES.getClaimantIndType(),
                labelPayloadES.getClaimantType(),
                labelPayloadES.getClaimantCompany(),
                labelPayloadES.getEthosCaseReference(),
                printClaimantLabel);

    }

    public static AddressLabelTypeItem getClaimantAddressLabelCaseData(CaseData caseData, String printClaimantLabel) {

        return getClaimantAddressLabel(caseData.getClaimantTypeOfClaimant(),
                caseData.getClaimantIndType(),
                caseData.getClaimantType(),
                caseData.getClaimantCompany(),
                caseData.getEthosCaseReference(),
                printClaimantLabel);

    }

    private static AddressLabelTypeItem getClaimantAddressLabel(String claimantTypeOfClaimant,
                                                               ClaimantIndType claimantIndType,
                                                               ClaimantType claimantType,
                                                               String claimantCompany,
                                                               String ethosCaseReference,
                                                               String printClaimantLabel) {

        AddressLabelType addressLabelType = new AddressLabelType();

        addressLabelType.setPrintLabel(printClaimantLabel);

        if (claimantTypeOfClaimant != null && claimantTypeOfClaimant.equals(INDIVIDUAL_TYPE_CLAIMANT)) {
            if (claimantIndType != null) {
                addressLabelType.setFullName(CLAIMANT + nullCheck(claimantIndType.claimantFullName()));
                addressLabelType.setLabelEntityName01(nullCheck(claimantIndType.claimantFullName()));
                addressLabelType.setLabelEntityName02("");
            }
        } else {
            addressLabelType.setFullName(CLAIMANT + nullCheck(claimantCompany));
            addressLabelType.setLabelEntityName01("");
            addressLabelType.setLabelEntityName02(nullCheck(claimantCompany));
        }

        if (claimantType != null) {
            setEntityAddress(addressLabelType, claimantType.getClaimantAddressUK());
            setEntityTelephone(addressLabelType, claimantType.getClaimantPhoneNumber());
            setEntityFax(addressLabelType, claimantType.getClaimantMobileNumber());
        } else {
            addressLabelType.setFullAddress("");
            addressLabelType.setLabelEntityAddress(new Address());
            addressLabelType.setLabelEntityTelephone("");
            addressLabelType.setLabelEntityFax("");
        }

        addressLabelType.setLabelEntityReference(REF);
        addressLabelType.setLabelCaseReference(ethosCaseReference);

        AddressLabelTypeItem addressLabelTypeItem = new AddressLabelTypeItem();

        addressLabelTypeItem.setId(UUID.randomUUID().toString());
        addressLabelTypeItem.setValue(addressLabelType);

        return addressLabelTypeItem;

    }

    public static AddressLabelTypeItem getClaimantRepAddressLabelData(LabelPayloadES labelPayloadES,
                                                                      String printClaimantRepLabel) {

        if (labelPayloadES.getRepresentativeClaimantType() != null
                && labelPayloadES.getClaimantRepresentedQuestion().equals(YES)) {
            return getClaimantRepAddressLabel(labelPayloadES.getRepresentativeClaimantType(),
                    labelPayloadES.getEthosCaseReference(), printClaimantRepLabel);
        }

        return null;
    }

    public static AddressLabelTypeItem getClaimantRepAddressLabelCaseData(CaseData caseData,
                                                                          String printClaimantRepLabel) {

        if (caseData.getRepresentativeClaimantType() != null && caseData.getClaimantRepresentedQuestion().equals(YES)) {
            return getClaimantRepAddressLabel(caseData.getRepresentativeClaimantType(),
                    caseData.getEthosCaseReference(), printClaimantRepLabel);
        }

        return null;

    }

    private static AddressLabelTypeItem getClaimantRepAddressLabel(RepresentedTypeC representedTypeC,
                                                                   String ethosCaseReference,
                                                                   String printClaimantRepLabel) {

        AddressLabelType addressLabelType = new AddressLabelType();

        addressLabelType.setPrintLabel(printClaimantRepLabel);

        addressLabelType.setFullName(CLAIMANT_REP + nullCheck(representedTypeC.getNameOfRepresentative()));
        addressLabelType.setLabelEntityName01(nullCheck(representedTypeC.getNameOfRepresentative()));
        addressLabelType.setLabelEntityName02(nullCheck(representedTypeC.getNameOfOrganisation()));
        setEntityAddress(addressLabelType, representedTypeC.getRepresentativeAddress());
        setEntityTelephone(addressLabelType, representedTypeC.getRepresentativePhoneNumber());
        setEntityFax(addressLabelType, representedTypeC.getRepresentativeMobileNumber());

        if (isNullOrEmpty(nullCheck(representedTypeC.getRepresentativeReference()))) {
            addressLabelType.setLabelEntityReference(REF);
        } else {
            addressLabelType.setLabelEntityReference(
                    REF + nullCheck(representedTypeC.getRepresentativeReference()));
        }

        addressLabelType.setLabelCaseReference(ethosCaseReference);

        AddressLabelTypeItem addressLabelTypeItem = new AddressLabelTypeItem();

        addressLabelTypeItem.setId(UUID.randomUUID().toString());
        addressLabelTypeItem.setValue(addressLabelType);

        return addressLabelTypeItem;

    }

    public static List<AddressLabelTypeItem> getRespondentsAddressLabelsData(LabelPayloadES labelPayloadES,
                                                                             String printRespondentsLabels) {

        List<AddressLabelTypeItem> labelTypeItemList = new ArrayList<>();
        if (labelPayloadES.getRespondentCollection() != null && !labelPayloadES.getRespondentCollection().isEmpty()) {
            List<RespondentSumTypeItem> activeRespondents = getActiveRespondentsLabels(labelPayloadES);
            if (!activeRespondents.isEmpty()) {
                labelTypeItemList = getRespondentsAddressLabels(activeRespondents,
                        labelPayloadES.getEthosCaseReference(), printRespondentsLabels);
            }
        }

        return labelTypeItemList;

    }

    public static List<AddressLabelTypeItem> getRespondentsAddressLabelsCaseData(CaseData caseData,
                                                                                 String printRespondentsLabels) {

        List<AddressLabelTypeItem> labelTypeItemList = new ArrayList<>();
        if (caseData.getRespondentCollection() != null && !caseData.getRespondentCollection().isEmpty()) {
            List<RespondentSumTypeItem> activeRespondents = getActiveRespondents(caseData);
            if (!activeRespondents.isEmpty()) {
                labelTypeItemList = getRespondentsAddressLabels(activeRespondents,
                        caseData.getEthosCaseReference(), printRespondentsLabels);
            }
        }

        return labelTypeItemList;

    }

    private static List<AddressLabelTypeItem> getRespondentsAddressLabels(List<RespondentSumTypeItem> activeRespondents,
                                                                         String ethosCaseReference,
                                                                         String printRespondentsLabels) {

        List<AddressLabelTypeItem> labelTypeItemList = new ArrayList<>();

        activeRespondents.forEach(activeRespondent -> {
            AddressLabelType addressLabelType = new AddressLabelType();
            RespondentSumType respondentSumType = activeRespondent.getValue();
            addressLabelType.setPrintLabel(printRespondentsLabels);
            addressLabelType.setFullName(RESPONDENT + nullCheck(respondentSumType.getRespondentName()));
            addressLabelType.setLabelEntityName01(nullCheck(respondentSumType.getRespondentName()));
            addressLabelType.setLabelEntityName02("");
            setEntityAddress(addressLabelType, DocumentHelper.getRespondentAddressET3(respondentSumType));
            setEntityTelephone(addressLabelType, respondentSumType.getRespondentPhone1());
            setEntityFax(addressLabelType, respondentSumType.getRespondentPhone2());
            addressLabelType.setLabelEntityReference(REF);
            addressLabelType.setLabelCaseReference(ethosCaseReference);
            AddressLabelTypeItem addressLabelTypeItem = new AddressLabelTypeItem();
            addressLabelTypeItem.setId(UUID.randomUUID().toString());
            addressLabelTypeItem.setValue(addressLabelType);
            labelTypeItemList.add(addressLabelTypeItem);
        });

        return labelTypeItemList;

    }

    public static List<AddressLabelTypeItem> getRespondentsRepsAddressLabelsData(LabelPayloadES labelPayloadES,
                                                                                 String printRespondentsRepsLabels) {

        List<AddressLabelTypeItem> labelTypeItemList = new ArrayList<>();
        if (labelPayloadES.getRepCollection() != null && !labelPayloadES.getRepCollection().isEmpty()) {
            labelTypeItemList = getRespondentsRepsAddressLabels(labelPayloadES.getRepCollection(),
                    labelPayloadES.getEthosCaseReference(), printRespondentsRepsLabels);

        }

        return labelTypeItemList;

    }

    public static List<AddressLabelTypeItem> getRespondentsRepsAddressLabelsCaseData(
            CaseData caseData, String printRespondentsRepsLabels) {

        List<AddressLabelTypeItem> labelTypeItemList = new ArrayList<>();
        if (caseData.getRepCollection() != null && !caseData.getRepCollection().isEmpty()) {
            labelTypeItemList = getRespondentsRepsAddressLabels(caseData.getRepCollection(),
                    caseData.getEthosCaseReference(), printRespondentsRepsLabels);

        }

        return labelTypeItemList;

    }

    private static List<AddressLabelTypeItem> getRespondentsRepsAddressLabels(List<RepresentedTypeRItem> repCollection,
                                                                             String ethosCaseReference,
                                                                             String printRespondentsRepsLabels) {

        List<AddressLabelTypeItem> labelTypeItemList = new ArrayList<>();

        repCollection.forEach(representedTypeRItem -> {
            AddressLabelType addressLabelType = new AddressLabelType();
            RepresentedTypeR representedTypeR = representedTypeRItem.getValue();
            addressLabelType.setPrintLabel(printRespondentsRepsLabels);
            addressLabelType.setFullName(RESPONDENT_REP + nullCheck(representedTypeR.getNameOfRepresentative()));
            addressLabelType.setLabelEntityName01(nullCheck(representedTypeR.getNameOfRepresentative()));
            addressLabelType.setLabelEntityName02(nullCheck(representedTypeR.getNameOfOrganisation()));
            setEntityAddress(addressLabelType, representedTypeR.getRepresentativeAddress());
            setEntityTelephone(addressLabelType, representedTypeR.getRepresentativePhoneNumber());
            setEntityFax(addressLabelType, representedTypeR.getRepresentativeMobileNumber());
            if (isNullOrEmpty(nullCheck(representedTypeR.getRepresentativeReference()))) {
                addressLabelType.setLabelEntityReference(REF);
            } else {
                addressLabelType.setLabelEntityReference(
                        REF + nullCheck(representedTypeR.getRepresentativeReference()));
            }
            addressLabelType.setLabelCaseReference(ethosCaseReference);
            AddressLabelTypeItem addressLabelTypeItem = new AddressLabelTypeItem();
            addressLabelTypeItem.setId(UUID.randomUUID().toString());
            addressLabelTypeItem.setValue(addressLabelType);
            labelTypeItemList.add(addressLabelTypeItem);
        });

        return labelTypeItemList;

    }

    private static void setEntityAddress(AddressLabelType addressLabelType, Address entityAddress) {

        if (entityAddress != null) {
            addressLabelType.setFullAddress(getFullAddressOneLine(entityAddress).toString());
            addressLabelType.setLabelEntityAddress(entityAddress);

        } else {
            addressLabelType.setFullAddress("");
            addressLabelType.setLabelEntityAddress(new Address());
        }

    }

    private static void setEntityTelephone(AddressLabelType addressLabelType, String telephone) {

        if (isNullOrEmpty(nullCheck(telephone))) {
            addressLabelType.setLabelEntityTelephone("");
        } else {
            addressLabelType.setLabelEntityTelephone(TEL + nullCheck(telephone));
        }
    }

    private static void setEntityFax(AddressLabelType addressLabelType, String fax) {

        if (isNullOrEmpty(nullCheck(fax))) {
            addressLabelType.setLabelEntityFax("");
        } else {
            addressLabelType.setLabelEntityFax(TEL + nullCheck(fax));
        }
    }

    private static StringBuilder getFullAddressOneLine(Address address) {

        StringBuilder sb = new StringBuilder();
        sb.append(nullCheck(address.getAddressLine1()))
                .append(isNullOrEmpty(nullCheck(address.getAddressLine2())) || sb.isEmpty() ? "" : ", ")
                .append(nullCheck(address.getAddressLine2()))
                .append(isNullOrEmpty(nullCheck(address.getAddressLine3())) || sb.isEmpty() ? "" : ", ")
                .append(nullCheck(address.getAddressLine3()))
                .append(isNullOrEmpty(nullCheck(address.getPostTown())) || sb.isEmpty() ? "" : ", ")
                .append(nullCheck(address.getPostTown()))
                .append(isNullOrEmpty(nullCheck(address.getCounty())) || sb.isEmpty() ? "" : ", ")
                .append(nullCheck(address.getCounty()))
                .append(isNullOrEmpty(nullCheck(address.getPostCode())) || sb.isEmpty() ? "" : ", ")
                .append(nullCheck(address.getPostCode()))
                .append(isNullOrEmpty(nullCheck(address.getCountry())) || sb.isEmpty() ? "" : ", ")
                .append(nullCheck(address.getCountry())).append(sb.isEmpty() ? "" : ".");

        return sb;

    }

    public static List<AddressLabelTypeItem> customiseSelectedAddressesMultiples(
            List<LabelPayloadEvent> labelPayloadEvents,
            MultipleData multipleData) {

        if (multipleData.getAddressLabelsSelectionTypeMSL() != null
                && !multipleData.getAddressLabelsSelectionTypeMSL().isEmpty()) {

            return new ArrayList<>(getAddressLabelTypeItems(labelPayloadEvents,
                    multipleData.getAddressLabelsSelectionTypeMSL()));

        } else {
            return Collections.emptyList();
        }

    }

    private static List<AddressLabelTypeItem> getAddressLabelTypeItems(List<LabelPayloadEvent> labelPayloadEvents,
                                                                List<String> addressLabelsSelectionTypeMSL) {

        List<AddressLabelTypeItem> addressLabelTypeItems = new ArrayList<>();

        for (LabelPayloadEvent labelPayloadEvent : labelPayloadEvents) {

            if (addressLabelsSelectionTypeMSL.contains(CLAIMANT_ADDRESS_LABEL)) {

                log.info("Adding: CLAIMANT_ADDRESS_LABEL");

                addressLabelTypeItems.add(LabelsHelper.getClaimantAddressLabelData(
                        labelPayloadEvent.getLabelPayloadES(), YES));

            }

            if (addressLabelsSelectionTypeMSL.contains(CLAIMANT_REP_ADDRESS_LABEL)) {

                log.info("Adding: CLAIMANT_REP_ADDRESS_LABEL");

                AddressLabelTypeItem addressLabelTypeItem =
                        LabelsHelper.getClaimantRepAddressLabelData(labelPayloadEvent.getLabelPayloadES(), YES);
                if (addressLabelTypeItem != null) {
                    addressLabelTypeItems.add(addressLabelTypeItem);
                }

            }

            if (addressLabelsSelectionTypeMSL.contains(RESPONDENTS_ADDRESS__LABEL)) {

                log.info("Adding: RESPONDENTS_ADDRESS__LABEL");

                List<AddressLabelTypeItem> addressLabelTypeItemsAux =
                        LabelsHelper.getRespondentsAddressLabelsData(labelPayloadEvent.getLabelPayloadES(), YES);
                if (!addressLabelTypeItemsAux.isEmpty()) {
                    addressLabelTypeItems.addAll(addressLabelTypeItemsAux);
                }
            }

            if (addressLabelsSelectionTypeMSL.contains(RESPONDENTS_REPS_ADDRESS__LABEL)) {

                log.info("Adding: RESPONDENTS_REPS_ADDRESS__LABEL");

                List<AddressLabelTypeItem> addressLabelTypeItemsAux =
                        LabelsHelper.getRespondentsRepsAddressLabelsData(labelPayloadEvent.getLabelPayloadES(), YES);
                if (!addressLabelTypeItemsAux.isEmpty()) {
                    addressLabelTypeItems.addAll(addressLabelTypeItemsAux);
                }
            }

        }

        return addressLabelTypeItems;

    }

    public static List<AddressLabelTypeItem> getSelectedAddressLabelsMultiple(MultipleData multipleData) {

        List<AddressLabelTypeItem> selectedAddressLabels = new ArrayList<>();

        if (multipleData.getAddressLabelCollection() != null && !multipleData.getAddressLabelCollection().isEmpty()) {
            selectedAddressLabels = multipleData.getAddressLabelCollection()
                    .stream()
                    .filter(addressLabelTypeItem -> addressLabelTypeItem.getValue().getFullName() != null
                            || addressLabelTypeItem.getValue().getFullAddress() != null)
                    .toList();
        }

        return selectedAddressLabels;
    }

    public static void validateNumberOfSelectedLabels(MultipleData multipleData, List<String> errors) {
        String templateName = DocumentHelper.getTemplateName(multipleData.getCorrespondenceType(),
                multipleData.getCorrespondenceScotType());
        if (errors.isEmpty()
                && templateName.equals(ADDRESS_LABELS_TEMPLATE)
                && Integer.parseInt(multipleData.getAddressLabelsAttributesType().getNumberOfSelectedLabels()) == 0) {
            errors.add(ADDRESS_LABELS_RESULT_SELECTION_ERROR);
        }
    }

    public static List<String> midValidateAddressLabelsErrors(AddressLabelsAttributesType addressLabelsAttributesType,
                                                              String caseType) {

        List<String> errors = new ArrayList<>();

        if (caseType.equals(SINGLE_CASE_TYPE)
                && Integer.parseInt(addressLabelsAttributesType.getNumberOfSelectedLabels()) == 0) {
            errors.add(ADDRESS_LABELS_SELECT_ERROR);

        } else if (addressLabelsAttributesType.getNumberOfCopies().contains(".")) {
            errors.add(ADDRESS_LABELS_COPIES_ERROR);

        } else if (caseType.equals(SINGLE_CASE_TYPE)
                && Integer.parseInt(addressLabelsAttributesType.getNumberOfCopies()) > 10) {
            errors.add(ADDRESS_LABELS_COPIES_LESS_10_ERROR);

        } else if (caseType.equals(MULTIPLE_CASE_TYPE) && reachLimitOfTotalNumberLabels(addressLabelsAttributesType)) {
            errors.add(ADDRESS_LABELS_LABELS_LIMIT_ERROR + " of " + MAX_NUMBER_LABELS);

        }

        return errors;

    }

    private static boolean reachLimitOfTotalNumberLabels(AddressLabelsAttributesType addressLabelsAttributesType) {

        int selectedLabels = Integer.parseInt(addressLabelsAttributesType.getNumberOfSelectedLabels());
        int numberOfCopies = Integer.parseInt(addressLabelsAttributesType.getNumberOfCopies());
        return selectedLabels * numberOfCopies > MAX_NUMBER_LABELS;

    }

}
