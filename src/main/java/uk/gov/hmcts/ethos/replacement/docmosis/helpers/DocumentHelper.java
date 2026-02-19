package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.ecm.common.model.helper.DefaultValues;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.AddressLabelTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.AddressLabelType;
import uk.gov.hmcts.et.common.model.ccd.types.AddressLabelsAttributesType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantIndType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantType;
import uk.gov.hmcts.et.common.model.ccd.types.CorrespondenceScotType;
import uk.gov.hmcts.et.common.model.ccd.types.CorrespondenceType;
import uk.gov.hmcts.et.common.model.ccd.types.DateListedType;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.et.common.model.generic.BaseCaseData;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VenueAddressReaderService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ADDRESS_LABELS_PAGE_SIZE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ADDRESS_LABELS_TEMPLATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.COMPANY_TYPE_CLAIMANT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ECC_DOCUMENT_ENG_TEMPLATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ECC_DOCUMENT_SCOT_TEMPLATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.FILE_EXTENSION;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_LISTED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.LABEL;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.LBL;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NEW_LINE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.OUTPUT_FILE_NAME;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem.fromUploadedDocument;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.nullCheck;

@Slf4j
public final class DocumentHelper {

    public static final String USER_IMAGE = "[userImage:";
    public static final String ENHMCTS_PNG = "enhmcts.png]";
    public static final String SCHMCTS_PNG = "schmcts.png]";
    public static final String I_SCOT = "\"iScot";
    public static final String CLAIMANT_FULL_NAME = "\"claimant_full_name\":\"";
    public static final String CLAIMANT = "\"Claimant\":\"";
    public static final String RESPONDENT = "\"Respondent\":\"";
    public static final String RESPONDENT_FULL_NAME = "\"respondent_full_name\":\"";
    public static final String RESPONDENT_OR_REP_FULL_NAME = "\"respondent_or_rep_full_name\":\"";
    public static final String RESPONDENT_REP_ORG = "\"respondent_rep_organisation\":\"";
    public static final String RESP_ADDRESS = "\"resp_address\":\"";
    public static final String RESP_OTHERS = "\"resp_others\":\"";
    public static final String COLON = "\":\"";
    public static final String HEARING_DATE = "\"Hearing_date\":\"";
    public static final String HEARING_TIME = "\"Hearing_time\":\"";
    public static final String HEARING_DATE_TIME = "\"Hearing_date_time\":\"";
    public static final String CLAIMANT_OR_REP_FULL_NAME = "\"claimant_or_rep_full_name\":\"";
    public static final String CLAIMANT_REP_ORG = "\"claimant_rep_organisation\":\"";
    private static final Double DOUBLE_ONE = 1d;
    private static final List<String> EW_ECC_REJECTION_SECTIONS = List.of("3.1", "3.2", "3.3", "3.4", "3.5",
        "3.6", "3.7", "3.22", "3.23");

    private DocumentHelper() {
    }

    /**
     * Builds JSON content for Employment Tribunal documents using Docmosis templates.
    * <p>
     * Creates structured data for various ET documents including correspondence, address labels,
     * and ECC (Employer's Contract Claim) documents. Handles both England/Wales and Scotland
     * jurisdictions with appropriate branding.
     *
     * @param caseData Case data with claimant and respondent details
     * @param accessKey Docmosis access key for authentication
     * @param userDetails User details for clerk name in output
     * @param caseTypeId Case jurisdiction ("England_Wales" or "Scotland")
     * @param correspondenceType England/Wales correspondence config (nullable)
     * @param correspondenceScotType Scotland correspondence config (nullable)
     * @param multipleData Multiple case data for bulk operations (nullable)
     * @param allocatedCourtAddress Court address details (nullable)
     * @param venueAddressReaderService Service to resolve hearing venue addresses
     *
     * @return StringBuilder with JSON content ready for Docmosis processing
     */
    public static StringBuilder buildDocumentContent(CaseData caseData, String accessKey,
                                                     UserDetails userDetails, String caseTypeId,
                                                     CorrespondenceType correspondenceType,
                                                     CorrespondenceScotType correspondenceScotType,
                                                     MultipleData multipleData,
                                                     DefaultValues allocatedCourtAddress,
                                                     VenueAddressReaderService venueAddressReaderService) {
        StringBuilder sb = new StringBuilder(260);
        String templateName = getTemplateName(correspondenceType, correspondenceScotType);

        sb.append("{\n\"accessKey\":\"").append(accessKey).append(NEW_LINE).append("\"templateName\":\"")
                .append(templateName).append(FILE_EXTENSION).append(NEW_LINE).append("\"outputName\":\"")
                .append(OUTPUT_FILE_NAME).append(NEW_LINE).append("\"data\":{\n");

        if (templateName.equals(ADDRESS_LABELS_TEMPLATE) && multipleData == null) {
            sb.append(getAddressLabelsDataSingleCase(caseData));
        } else if (templateName.equals(ADDRESS_LABELS_TEMPLATE)) {
            sb.append(getAddressLabelsDataMultipleCase(multipleData));
        } else {
            if (isEccDocumentTemplate(caseTypeId, correspondenceType, correspondenceScotType)) {
                processECCData(caseData, correspondenceType, correspondenceScotType, caseTypeId, sb);
            } else {
                sb.append(getClaimantData(caseData)).append(getRespondentData(caseData));
            }

            sb.append(getHearingData(caseData, caseTypeId, correspondenceType,
                            correspondenceScotType, venueAddressReaderService))
                .append(getCorrespondenceData(correspondenceType))
                .append(getCorrespondenceScotData(correspondenceScotType))
                .append(getCourtData(caseData, allocatedCourtAddress));
        }

        sb.append("\"i").append(getEWSectionName(correspondenceType).replace(".", "_")
                .replace(" ", "_"))
                .append("_enhmcts\":\"").append(USER_IMAGE).append(ENHMCTS_PNG).append(NEW_LINE).append("\"i")
                .append(getEWSectionName(correspondenceType).replace(".", "_")
                .replace(" ", "_"))
                .append("_enhmcts1\":\"").append(USER_IMAGE).append(ENHMCTS_PNG).append(NEW_LINE).append("\"i")
                .append(getEWSectionName(correspondenceType).replace(".", "_")
                .replace(" ", "_"))
                .append("_enhmcts2\":\"").append(USER_IMAGE).append(ENHMCTS_PNG).append(NEW_LINE).append(I_SCOT)
                .append(getScotSectionName(correspondenceScotType).replace(".", "_")
                .replace(" ", "_"))
                .append("_schmcts\":\"").append(USER_IMAGE).append(SCHMCTS_PNG).append(NEW_LINE).append(I_SCOT)
                .append(getScotSectionName(correspondenceScotType).replace(".", "_")
                .replace(" ", "_"))
                .append("_schmcts1\":\"").append(USER_IMAGE).append(SCHMCTS_PNG).append(NEW_LINE).append(I_SCOT)
                .append(getScotSectionName(correspondenceScotType).replace(".", "_")
                .replace(" ", "_"))
                .append("_schmcts2\":\"").append(USER_IMAGE).append(SCHMCTS_PNG).append(NEW_LINE);

        String userName = nullCheck(userDetails.getFirstName() + " " + userDetails.getLastName());
        sb.append("\"Clerk\":\"").append(nullCheck(userName)).append(NEW_LINE).append("\"Today_date\":\"")
                .append(UtilHelper.formatCurrentDate(LocalDate.now())).append(NEW_LINE).append("\"TodayPlus28Days\":\"")
                .append(UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 28)).append(NEW_LINE)
                .append("\"Case_No\":\"").append(nullCheck(caseData.getEthosCaseReference())).append(NEW_LINE)
                .append("\"submission_reference\":\"").append(nullCheck(caseData.getFeeGroupReference()))
                .append(NEW_LINE).append("}\n}\n");

        return sb;
    }

    private static void processECCData(CaseData caseData, CorrespondenceType correspondenceType,
                                       CorrespondenceScotType correspondenceScotType,
                                       String caseTypeId, StringBuilder sb) {
        boolean isEnglandWales = ENGLANDWALES_CASE_TYPE_ID.equals(caseTypeId);

        boolean isLetterAddressedToClaimant = isEnglandWales
                ? correspondenceType != null
                && YES.equals(correspondenceType.getFlipRespondentAndClaimantValues())
                : correspondenceScotType != null
                && YES.equals(correspondenceScotType.getFlipRespondentAndClaimantValues());

        String selectedRespondentId = null;

        if (isEnglandWales && correspondenceType != null
                && correspondenceType.getDynamicRespondentsWithEcc() != null
                && correspondenceType.getDynamicRespondentsWithEcc().getValue() != null) {
            selectedRespondentId = correspondenceType.getDynamicRespondentsWithEcc().getValue().getCode();
        } else if (!isEnglandWales && correspondenceScotType != null
                && correspondenceScotType.getDynamicRespondentsWithEcc() != null
                && correspondenceScotType.getDynamicRespondentsWithEcc().getValue() != null) {
            selectedRespondentId = correspondenceScotType.getDynamicRespondentsWithEcc().getValue().getCode();
        }

        sb.append(isLetterAddressedToClaimant
                ? getClaimantEccWithRespondentData(caseData, selectedRespondentId)
                .append(getRespondentEccWithClaimantData(caseData, selectedRespondentId))
                : getClaimantData(caseData)
                .append(getSelectedRespondentEccData(caseData, selectedRespondentId)));
    }

    /**
     * Helper method to build address fields with a given prefix.
     * @param address the address to process
     * @param prefix the field name prefix (e.g., "claimant", "respondent")
     * @return StringBuilder with formatted address fields
     */
    private static StringBuilder buildAddressFields(Address address, String prefix) {
        StringBuilder sb = new StringBuilder(170);
        sb.append(prefix).append("addressLine1\":\"")
                .append(nullCheck(address.getAddressLine1())).append(NEW_LINE)
                .append(prefix).append("addressLine2\":\"")
                .append(nullCheck(address.getAddressLine2())).append(NEW_LINE)
                .append(prefix).append("addressLine3\":\"")
                .append(nullCheck(address.getAddressLine3())).append(NEW_LINE)
                .append(prefix).append("town\":\"")
                .append(nullCheck(address.getPostTown())).append(NEW_LINE)
                .append(prefix).append("county\":\"")
                .append(nullCheck(address.getCounty())).append(NEW_LINE)
                .append(prefix).append("postCode\":\"")
                .append(nullCheck(address.getPostCode())).append(NEW_LINE);
        return sb;
    }

    private static StringBuilder getClaimantAddressUK(Address address) {
        return buildAddressFields(address, "\"claimant_");
    }

    /**
     * Helper method to extract the claimant's full name from case data.
     * @param caseData the case data
     * @return the claimant's full name or empty string if not available
     */
    private static String extractClaimantFullName(CaseData caseData) {
        return Optional.ofNullable(caseData.getClaimantTypeOfClaimant())
                .filter(COMPANY_TYPE_CLAIMANT::equals)
                .map(type -> caseData.getClaimantCompany())
                .orElseGet(() -> Optional.ofNullable(caseData.getClaimantIndType())
                        .map(ClaimantIndType::claimantFullName).orElse(""));
    }

    private static StringBuilder getClaimantOrRepAddressUK(Address address) {
        return buildAddressFields(address, "\"claimant_or_rep_");
    }

    private static StringBuilder getClaimantData(CaseData caseData) {
        RepresentedTypeC representative = caseData.getRepresentativeClaimantType();
        boolean hasRepresentative = representative != null
                && caseData.getClaimantRepresentedQuestion() != null
                && YES.equals(caseData.getClaimantRepresentedQuestion());

        String claimantName = extractClaimantFullName(caseData);
        Address claimantAddress = Optional.ofNullable(caseData.getClaimantType())
                .map(ClaimantType::getClaimantAddressUK)
                .orElse(new Address());

        boolean isCompanyClaimant = COMPANY_TYPE_CLAIMANT.equals(caseData.getClaimantTypeOfClaimant());
        boolean hasIndividualClaimant = caseData.getClaimantIndType() != null;

        StringBuilder sb = new StringBuilder();

        if (hasRepresentative) {
            appendClaimantRepresentativeData(sb, representative, claimantName);
        } else {
            appendClaimantData(sb, claimantName, claimantAddress, isCompanyClaimant, hasIndividualClaimant);
        }

        sb.append(getClaimantAddressUK(claimantAddress));
        return sb;
    }

    /**
     * Appends claimant representative information to the StringBuilder.
     */
    private static void appendClaimantRepresentativeData(StringBuilder sb,
                                                         RepresentedTypeC representative, String claimantName) {
        sb.append(CLAIMANT_OR_REP_FULL_NAME)
                .append(nullCheck(representative.getNameOfRepresentative()))
                .append(NEW_LINE)
                .append(CLAIMANT_REP_ORG)
                .append(nullCheck(representative.getNameOfOrganisation()))
                .append(NEW_LINE);

        sb.append(getClaimantOrRepAddressUK(
                Optional.ofNullable(representative.getRepresentativeAddress()).orElse(new Address())));

        sb.append("\"claimant_reference\":\"")
                .append(nullCheck(representative.getRepresentativeReference()))
                .append(NEW_LINE);

        sb.append(CLAIMANT_FULL_NAME).append(nullCheck(claimantName)).append(NEW_LINE)
                .append(CLAIMANT).append(nullCheck(claimantName)).append(NEW_LINE);
    }

    /**
     * Appends claimant information (no representative) to the StringBuilder.
     */
    private static void appendClaimantData(StringBuilder sb, String claimantName, Address claimantAddress,
                                           boolean isCompanyClaimant, boolean hasIndividualClaimant) {
        if (isCompanyClaimant) {
            appendClaimantNameFields(sb, claimantName, claimantName, claimantName);
        } else if (hasIndividualClaimant) {
            appendClaimantNameFields(sb, claimantName, claimantName, claimantName);
        } else {
            appendEmptyClaimantFields(sb);
        }

        sb.append(getClaimantOrRepAddressUK(claimantAddress));
    }

    /**
     * Appends claimant name fields with potentially different values.
     */
    private static void appendClaimantNameFields(StringBuilder sb, String orRepFullName,
                                                String fullName, String claimantField) {
        sb.append(CLAIMANT_OR_REP_FULL_NAME).append(nullCheck(orRepFullName)).append(NEW_LINE)
                .append(CLAIMANT_FULL_NAME).append(nullCheck(fullName)).append(NEW_LINE)
                .append(CLAIMANT).append(nullCheck(claimantField)).append(NEW_LINE);
    }

    /**
     * Appends empty claimant fields when no claimant data is available.
     */
    private static void appendEmptyClaimantFields(StringBuilder sb) {
        sb.append(CLAIMANT_OR_REP_FULL_NAME).append(NEW_LINE)
                .append(CLAIMANT_FULL_NAME).append(NEW_LINE)
                .append(CLAIMANT).append(NEW_LINE)
                .append(CLAIMANT_REP_ORG).append(NEW_LINE);
    }

    /**
     * Helper method to find a respondent by ID from the collection.
     * @param respondentCollection the respondent collection
     * @param respondentId the respondent ID to find
     * @return Optional containing the found respondent item
     */
    private static Optional<RespondentSumTypeItem> findRespondentById(
            List<RespondentSumTypeItem> respondentCollection, String respondentId) {
        return Optional.ofNullable(respondentCollection)
                .orElseGet(ArrayList::new)
                .stream()
                .filter(item -> respondentId != null && respondentId.equals(item.getId()))
                .findFirst();
    }

    /**
     * Internal method to get respondent address data using claimant field names - for ECC flipped scenarios.
     *
     * @param caseData             The case data
     * @param selectedRespondentId The specific respondent ID to use as claimant (null for regular claimant data)
     * @return StringBuilder containing respondent data in claimant fields
     */
    static StringBuilder getClaimantEccWithRespondentData(CaseData caseData, String selectedRespondentId) {
        if (selectedRespondentId == null) {
            return new StringBuilder();
        }

        Optional<RespondentSumTypeItem> selectedRespondentItem = findRespondentById(
                caseData.getRespondentCollection(), selectedRespondentId);

        String originalClaimantName = extractClaimantFullName(caseData);
        StringBuilder sb = new StringBuilder();

        if (selectedRespondentItem.isEmpty()) {
            appendEmptyRespondentAsClaimantRep(sb, originalClaimantName);
            sb.append(getClaimantAddressUK(new Address()));
            return sb;
        }

        RespondentSumType respondent = selectedRespondentItem.get().getValue();
        String respondentName = nullCheck(respondent.getRespondentName());
        Address respondentAddress = getRespondentAddressET3(respondent);

        Optional<RepresentedTypeRItem> representative = findRespondentRepresentativeByName(
                caseData.getRepCollection(), respondentName);

        if (representative.isPresent()) {
            appendRespondentRepAsClaimantRep(sb, representative.get(), originalClaimantName);
        } else if (!respondentName.isEmpty()) {
            appendRespondentAsClaimantRep(sb, respondentName, respondentAddress, originalClaimantName);
        } else {
            appendEmptyRespondentAsClaimantRep(sb, originalClaimantName);
        }

        sb.append(getClaimantAddressUK(respondentAddress));
        return sb;
    }

    /**
     * Finds a respondent representative by matching the respondent name.
     */
    private static Optional<RepresentedTypeRItem> findRespondentRepresentativeByName(
            List<RepresentedTypeRItem> representatives, String respondentName) {
        if (CollectionUtils.isEmpty(representatives) || respondentName.isEmpty()) {
            return Optional.empty();
        }

        return representatives.stream()
                .filter(rep -> rep.getValue().getRespRepName().equals(respondentName))
                .findFirst();
    }

    /**
     * Appends respondent representative information using claimant field names.
     */
    private static void appendRespondentRepAsClaimantRep(StringBuilder sb,
                                                         RepresentedTypeRItem representativeItem,
                                                         String originalClaimantName) {
        RepresentedTypeR representative = representativeItem.getValue();

        sb.append(CLAIMANT_OR_REP_FULL_NAME)
                .append(nullCheck(representative.getNameOfRepresentative()))
                .append(NEW_LINE)
                .append(CLAIMANT_REP_ORG)
                .append(nullCheck(representative.getNameOfOrganisation()))
                .append(NEW_LINE);

        sb.append(getClaimantOrRepAddressUK(
                Optional.ofNullable(representative.getRepresentativeAddress()).orElse(new Address())));

        sb.append("\"claimant_reference\":\"")
                .append(nullCheck(representative.getRepresentativeReference()))
                .append(NEW_LINE);

        appendOriginalClaimantName(sb, originalClaimantName);
    }

    /**
     * Appends respondent information using claimant field names (no representative).
     */
    private static void appendRespondentAsClaimantRep(StringBuilder sb, String respondentName,
                                                      Address respondentAddress, String originalClaimantName) {
        sb.append(CLAIMANT_OR_REP_FULL_NAME).append(respondentName).append(NEW_LINE);
        sb.append(getClaimantOrRepAddressUK(respondentAddress));
        appendOriginalClaimantName(sb, originalClaimantName);
    }

    /**
     * Appends empty respondent data using claimant field names (respondent not found).
     */
    private static void appendEmptyRespondentAsClaimantRep(StringBuilder sb, String originalClaimantName) {
        sb.append(CLAIMANT_OR_REP_FULL_NAME).append(NEW_LINE);
        sb.append(getClaimantOrRepAddressUK(new Address()));
        appendOriginalClaimantName(sb, originalClaimantName);
        sb.append(CLAIMANT_REP_ORG).append(NEW_LINE);
    }

    /**
     * Appends the original claimant name to claimant fields.
     */
    private static void appendOriginalClaimantName(StringBuilder sb, String claimantName) {
        sb.append(CLAIMANT_FULL_NAME).append(nullCheck(claimantName)).append(NEW_LINE)
                .append(CLAIMANT).append(nullCheck(claimantName)).append(NEW_LINE);
    }

    private static StringBuilder getRespondentData(CaseData caseData) {
        return getRespondentDataInternal(caseData, null);
    }

    private static StringBuilder getSelectedRespondentEccData(CaseData caseData, String selectedRespondentId) {
        return getRespondentDataInternal(caseData, selectedRespondentId);
    }

    /**
     * Internal method to get respondent data - shared by getRespondentData and getRespondentEccData.
     * @param caseData The case data
     * @param selectedRespondentId The specific respondent ID to select (null for first valid respondent)
     * @return StringBuilder containing respondent data
     */
    static StringBuilder getRespondentDataInternal(CaseData caseData, String selectedRespondentId) {
        List<RespondentSumTypeItem> respondentCollection = Optional.ofNullable(caseData.getRespondentCollection())
                .orElse(new ArrayList<>());

        if (respondentCollection.isEmpty()) {
            log.info("No respondents present for case: {}", caseData.getEthosCaseReference());
            return buildEmptyRespondentData();
        }

        RespondentSumType respondent;
        boolean isRespondentValid;
        
        if (selectedRespondentId != null) {
            Optional<RespondentSumTypeItem> selectedItem =
                    findRespondentById(respondentCollection, selectedRespondentId);
            if (selectedItem.isPresent()) {
                respondent = selectedItem.get().getValue();
                isRespondentValid = isValidRespondent(respondent);
                if (!isRespondentValid) {
                    log.info("Selected respondent {} is invalid for case: {}",
                            selectedRespondentId, caseData.getEthosCaseReference());
                }
            } else {
                log.info("No respondent found with ID: {} for case: {}",
                        selectedRespondentId, caseData.getEthosCaseReference());
                respondent = new RespondentSumType();
                isRespondentValid = false;
            }
        } else {
            Optional<RespondentSumType> validRespondent = respondentCollection.stream()
                    .map(RespondentSumTypeItem::getValue)
                    .filter(DocumentHelper::isValidRespondent)
                    .findFirst();
            
            if (validRespondent.isPresent()) {
                respondent = validRespondent.get();
                isRespondentValid = true;
            } else {
                log.error("No valid respondent found for case: {}", caseData.getEthosCaseReference());
                respondent = new RespondentSumType();
                isRespondentValid = false;
            }
        }

        Optional<RepresentedTypeRItem> representative = findRespondentRepresentative(
                caseData.getRepCollection(), respondent, isRespondentValid);

        StringBuilder sb = new StringBuilder();

        if (representative.isPresent()) {
            appendRepresentativeData(sb, representative.get().getValue());
        } else {
            appendRespondentData(sb, respondent, isRespondentValid, respondentCollection);
        }

        appendRespondentDetails(sb, respondent, isRespondentValid, caseData, respondentCollection);
        return sb;
    }

    /**
     * Helper method to check if a respondent is valid (not struck out and response continues).
     */
    private static boolean isValidRespondent(RespondentSumType respondent) {
        boolean responseContinue = isNullOrEmpty(respondent.getResponseContinue())
                || YES.equals(respondent.getResponseContinue());
        boolean responseNotStruckOut = isNullOrEmpty(respondent.getResponseStruckOut())
                || NO.equals(respondent.getResponseStruckOut());
        return responseContinue && responseNotStruckOut;
    }

    /**
     * Finds the representative for a given respondent.
     */
    private static Optional<RepresentedTypeRItem> findRespondentRepresentative(
            List<RepresentedTypeRItem> representativeCollection, RespondentSumType respondent, boolean isValid) {
        if (CollectionUtils.isEmpty(representativeCollection) || !isValid) {
            return Optional.empty();
        }

        return representativeCollection.stream()
                .filter(rep -> rep.getValue().getRespRepName().equals(respondent.getRespondentName()))
                .findFirst();
    }

    /**
     * Builds empty respondent data structure.
     */
    private static StringBuilder buildEmptyRespondentData() {
        StringBuilder sb = new StringBuilder();
        sb.append(RESPONDENT_OR_REP_FULL_NAME).append(NEW_LINE)
                .append(RESPONDENT_REP_ORG).append(NEW_LINE)
                .append(getRespondentOrRepAddressUK(new Address()))
                .append(RESPONDENT_FULL_NAME).append(NEW_LINE)
                .append(getRespondentAddressUK(new Address()))
                .append(RESPONDENT).append(NEW_LINE)
                .append(RESP_OTHERS).append(NEW_LINE)
                .append(RESP_ADDRESS).append(NEW_LINE);
        return sb;
    }

    /**
     * Appends representative data to the StringBuilder.
     */
    private static void appendRepresentativeData(StringBuilder sb, RepresentedTypeR representative) {
        sb.append(RESPONDENT_OR_REP_FULL_NAME)
                .append(nullCheck(representative.getNameOfRepresentative()))
                .append(NEW_LINE);

        sb.append(getRespondentOrRepAddressUK(
                Optional.ofNullable(representative.getRepresentativeAddress()).orElse(new Address())));

        sb.append("\"respondent_reference\":\"")
                .append(nullCheck(representative.getRepresentativeReference()))
                .append(NEW_LINE)
                .append(RESPONDENT_REP_ORG)
                .append(nullCheck(representative.getNameOfOrganisation()))
                .append(NEW_LINE);
    }

    /**
     * Appends respondent data (when no representative) to the StringBuilder.
     */
    private static void appendRespondentData(StringBuilder sb, RespondentSumType respondent, boolean isValid,
                                             List<RespondentSumTypeItem> respondentCollection) {
        boolean hasValidData = !respondentCollection.isEmpty() && isValid;
        String respondentName = hasValidData
                ? nullCheck(respondent.getRespondentName())
                : "";
        Address respondentAddress = hasValidData
                ? getRespondentAddressET3(respondent)
                : new Address();

        sb.append(RESPONDENT_OR_REP_FULL_NAME)
                .append(respondentName)
                .append(NEW_LINE);

        if (!hasValidData) {
            sb.append(RESPONDENT_REP_ORG).append(NEW_LINE);
        }

        sb.append(getRespondentOrRepAddressUK(respondentAddress));
    }

    /**
     * Appends detailed respondent information to the StringBuilder.
     */
    private static void appendRespondentDetails(StringBuilder sb, RespondentSumType respondent, boolean isValid,
                                              CaseData caseData, List<RespondentSumTypeItem> respondentCollection) {
        if (respondentCollection.isEmpty()) {
            return;
        }

        String respondentName = isValid ? nullCheck(respondent.getRespondentName()) : "";
        sb.append(RESPONDENT_FULL_NAME).append(respondentName).append(NEW_LINE);

        if (isValid) {
            sb.append(getRespondentAddressUK(getRespondentAddressET3(respondent)));

            boolean hasMultipleRespondents = respondentCollection.size() > 1;
            String respondentDisplay = hasMultipleRespondents
                    ? "1. " + respondentName + ","
                    : respondentName;
            sb.append(RESPONDENT).append(respondentDisplay).append(NEW_LINE);
        } else {
            sb.append(RESPONDENT).append(NEW_LINE);
        }

        sb.append(getRespOthersName(caseData))
                .append(getRespAddress(caseData));
    }

    /**
     * Internal method to get claimant data using respondent field names - for ECC flipped scenarios.
     * @param caseData The case data
     * @param selectedRespondentId The specific respondent ID to use as claimant (null for regular claimant data)
     * @return StringBuilder containing claimant data in respondent fields
     */
    static StringBuilder getRespondentEccWithClaimantData(CaseData caseData, String selectedRespondentId) {
        if (selectedRespondentId == null) {
            return new StringBuilder();
        }

        String respondentName = findRespondentById(caseData.getRespondentCollection(), selectedRespondentId)
                .map(item -> nullCheck(item.getValue().getRespondentName()))
                .orElse("");

        RepresentedTypeC representative = caseData.getRepresentativeClaimantType();
        boolean hasRepresentative = representative != null
                && caseData.getClaimantRepresentedQuestion() != null
                && YES.equals(caseData.getClaimantRepresentedQuestion());

        String claimantName = extractClaimantFullName(caseData);
        Address claimantAddress = Optional.ofNullable(caseData.getClaimantType())
                .map(ClaimantType::getClaimantAddressUK)
                .orElse(new Address());

        StringBuilder sb = new StringBuilder();

        if (hasRepresentative) {
            appendClaimantRepAsRespondentRep(sb, representative, respondentName);
        } else {
            appendClaimantAsRespondentRep(sb, claimantName, claimantAddress, respondentName);
        }

        appendClaimantRepAddressAsRespondentRepAddress(sb, caseData, respondentName);
        appendEccFlippedFooter(sb);

        return sb;
    }

    /**
     * Appends claimant representative information using respondent field names.
     */
    private static void appendClaimantRepAsRespondentRep(StringBuilder sb,
                                                         RepresentedTypeC representative,
                                                         String respondentName) {
        sb.append(RESPONDENT_OR_REP_FULL_NAME)
                .append(nullCheck(representative.getNameOfRepresentative()))
                .append(NEW_LINE);

        sb.append(getRespondentOrRepAddressUK(
                Optional.ofNullable(representative.getRepresentativeAddress()).orElse(new Address())));

        sb.append("\"respondent_reference\":\"")
                .append(nullCheck(representative.getRepresentativeReference()))
                .append(NEW_LINE)
                .append(RESPONDENT_REP_ORG)
                .append(nullCheck(representative.getNameOfOrganisation()))
                .append(NEW_LINE);

        sb.append(RESPONDENT_FULL_NAME).append(respondentName).append(NEW_LINE);
    }

    /**
     * Appends claimant information using respondent field names (no representative).
     */
    private static void appendClaimantAsRespondentRep(StringBuilder sb, String claimantName,
                                                      Address claimantAddress, String respondentName) {
        if (claimantName.isEmpty()) {
            sb.append(RESPONDENT_OR_REP_FULL_NAME).append(NEW_LINE)
                    .append(RESPONDENT_REP_ORG).append(NEW_LINE);
        } else {
            sb.append(RESPONDENT_OR_REP_FULL_NAME).append(claimantName).append(NEW_LINE)
                    .append(RESPONDENT_FULL_NAME).append(respondentName).append(NEW_LINE);
        }

        sb.append(getRespondentOrRepAddressUK(claimantAddress));
    }

    /**
     * Appends claimant address information using respondent address fields.
     */
    private static void appendClaimantRepAddressAsRespondentRepAddress(StringBuilder sb,
                                                                       CaseData caseData, String respondentName) {
        Address claimantAddress = Optional.ofNullable(caseData.getClaimantType())
                .map(ClaimantType::getClaimantAddressUK)
                .orElse(new Address());

        sb.append(getRespondentAddressUK(claimantAddress))
                .append(RESPONDENT).append(respondentName).append(NEW_LINE);
    }

    /**
     * Appends the footer fields for ECC flipped data.
     */
    private static void appendEccFlippedFooter(StringBuilder sb) {
        sb.append(RESP_OTHERS).append(NEW_LINE)
                .append(RESP_ADDRESS).append(NEW_LINE);
    }

    private static StringBuilder getRespondentAddressUK(Address address) {
        return buildAddressFields(address, "\"respondent_");
    }

    private static StringBuilder getRespondentOrRepAddressUK(Address address) {
        return buildAddressFields(address, "\"respondent_or_rep_");
    }

    private static StringBuilder getRespOthersName(CaseData caseData) {
        StringBuilder sb = new StringBuilder(20);
        AtomicInteger atomicInteger = new AtomicInteger(2);
        List<String> respOthers = IntStream.range(1, caseData.getRespondentCollection().size())
            .mapToObj(idx -> caseData.getRespondentCollection().get(idx))
            .filter(respondentSumTypeItem -> respondentSumTypeItem.getValue().getResponseStruckOut() == null
                || respondentSumTypeItem.getValue().getResponseStruckOut().equals(NO)
                && (respondentSumTypeItem.getValue().getResponseContinue() == null
                || respondentSumTypeItem.getValue().getResponseContinue().equals(YES)))
            .map(respondentSumTypeItem -> atomicInteger.getAndIncrement() + ". "
                                          + respondentSumTypeItem.getValue().getRespondentName())
            .toList();
        sb.append(RESP_OTHERS).append(nullCheck(String.join(", ", respOthers))).append(NEW_LINE);
        return sb;
    }

    private static StringBuilder getRespAddress(CaseData caseData) {
        StringBuilder sb = new StringBuilder(25);
        AtomicInteger atomicInteger = new AtomicInteger(1);
        int size = caseData.getRespondentCollection().size();
        List<String> respAddressList = caseData.getRespondentCollection()
                .stream()
                .filter(respondentSumTypeItem -> respondentSumTypeItem.getValue().getResponseStruckOut() == null
                        || respondentSumTypeItem.getValue().getResponseStruckOut().equals(NO)
                        && (respondentSumTypeItem.getValue().getResponseContinue() == null
                        || YES.equals(respondentSumTypeItem.getValue().getResponseContinue())))
                .map(respondentSumTypeItem -> (size > 1 ? atomicInteger.getAndIncrement() + ". " : "")
                        + getRespondentAddressET3(respondentSumTypeItem.getValue()))
                .toList();
        sb.append(RESP_ADDRESS).append(nullCheck(String.join("\\n", respAddressList)))
                .append(NEW_LINE);
        return sb;
    }

    private static StringBuilder getHearingData(CaseData caseData, String caseTypeId,
                                                CorrespondenceType correspondenceType,
                                                CorrespondenceScotType correspondenceScotType,
                                                VenueAddressReaderService venueAddressReaderService) {
        StringBuilder sb = new StringBuilder();
        //Currently checking collection not the HearingType
        if (caseData.getHearingCollection() != null && !caseData.getHearingCollection().isEmpty()) {
            String correspondenceHearingNumber = getCorrespondenceHearingNumber(
                    correspondenceType, correspondenceScotType);
            HearingType hearingType = getHearingByNumber(caseData.getHearingCollection(), correspondenceHearingNumber);
            if (hearingType.getHearingDateCollection() != null && !hearingType.getHearingDateCollection().isEmpty()) {
                sb.append(HEARING_DATE).append(nullCheck(getHearingDates(hearingType
                        .getHearingDateCollection()))).append(NEW_LINE);
                String hearingDateAndTime = nullCheck(getHearingDatesAndTime(hearingType.getHearingDateCollection()));
                sb.append(HEARING_DATE_TIME).append(hearingDateAndTime).append(NEW_LINE).append(HEARING_TIME)
                        .append(getHearingTime(hearingDateAndTime)).append(NEW_LINE);
            } else {
                sb.append(HEARING_DATE).append(NEW_LINE).append(HEARING_DATE_TIME).append(NEW_LINE).append(HEARING_TIME)
                        .append(NEW_LINE);
            }
            sb.append("\"Hearing_venue\":\"").append(nullCheck(
                            venueAddressReaderService.getVenueAddress(hearingType, caseTypeId,
                                    caseData.getManagingOffice())))
                    .append(NEW_LINE).append("\"Hearing_duration\":\"")
                    .append(nullCheck(getHearingDuration(hearingType))).append(NEW_LINE);
        } else {
            sb.append(HEARING_DATE).append(NEW_LINE).append(HEARING_DATE_TIME).append(NEW_LINE)
                    .append("\"Hearing_venue\":\"").append(NEW_LINE).append("\"Hearing_duration\":\"").append(NEW_LINE)
                    .append(HEARING_TIME).append(NEW_LINE);
        }
        return sb;
    }

    public static String getCorrespondenceHearingNumber(CorrespondenceType correspondenceType,
                                                        CorrespondenceScotType correspondenceScotType) {
        if (correspondenceType != null && correspondenceType.getDynamicHearingNumber() != null) {
            return correspondenceType.getDynamicHearingNumber().getValue().getCode();
        } else if (correspondenceScotType != null && correspondenceScotType.getDynamicHearingNumber() != null) {
            return correspondenceScotType.getDynamicHearingNumber().getValue().getCode();
        } else {
            return null;
        }

    }

    public static HearingType getHearingByNumber(List<HearingTypeItem> hearingCollection,
                                                 String correspondenceHearingNumber) {
        HearingType hearingType = new HearingType();

        for (HearingTypeItem hearingTypeItem : hearingCollection) {
            hearingType = hearingTypeItem.getValue();
            if (hearingType.getHearingNumber() != null
                    && hearingType.getHearingNumber().equals(correspondenceHearingNumber)) {
                break;
            }
        }

        return hearingType;
    }

    private static String getHearingTime(String dateTime) {
        return dateTime.isEmpty() ? "" : dateTime.substring(dateTime.indexOf("at") + 3);
    }

    private static String getHearingDates(List<DateListedTypeItem> hearingDateCollection) {
        StringBuilder sb = new StringBuilder();

        List<String> dateListedList = new ArrayList<>();
        for (DateListedTypeItem dateListedTypeItem : hearingDateCollection) {
            if (dateListedTypeItem.getValue().getHearingStatus() != null
                    && dateListedTypeItem.getValue().getHearingStatus().equals(HEARING_STATUS_LISTED)) {
                dateListedList.add(UtilHelper.formatLocalDate(dateListedTypeItem.getValue().getListedDate()));
            }
        }
        sb.append(String.join(", ", dateListedList));

        return sb.toString();
    }

    private static String getHearingDatesAndTime(List<DateListedTypeItem> hearingDateCollection) {
        StringBuilder sb = new StringBuilder(getHearingDates(hearingDateCollection));
        Iterator<DateListedTypeItem> itr = hearingDateCollection.iterator();
        LocalTime earliestTime = LocalTime.of(23, 59);
        boolean isEmpty = true;

        while (itr.hasNext()) {
            DateListedType dateListedType = itr.next().getValue();
            if (dateListedType.getHearingStatus() != null && dateListedType.getHearingStatus()
                    .equals(HEARING_STATUS_LISTED)) {
                LocalDateTime listedDate = LocalDateTime.parse(dateListedType.getListedDate());
                LocalTime listedTime = LocalTime.of(listedDate.getHour(), listedDate.getMinute());
                earliestTime = listedTime.isBefore(earliestTime) ? listedTime : earliestTime;
                isEmpty = false;
            }
        }
        if (!isEmpty) {
            sb.append(" at ").append(earliestTime.toString());
        }

        return sb.toString();
    }

    public static String getHearingDuration(HearingType hearingType) {
        if (isNullOrEmpty(hearingType.getHearingEstLengthNum())) {
            return "";
        }
        String numType = hearingType.getHearingEstLengthNumType();
        try {
            double tmp = Double.parseDouble(hearingType.getHearingEstLengthNum());
            if (tmp == DOUBLE_ONE) {
                numType = numType.substring(0, numType.length() - 1);
            }
        } catch (NumberFormatException e) {
            log.error(e.toString());
            numType = hearingType.getHearingEstLengthNumType();
        }
        return String.join(" ",
                hearingType.getHearingEstLengthNum(), numType);
    }

    public static String getTemplateName(CorrespondenceType correspondenceType,
                                         CorrespondenceScotType correspondenceScotType) {
        if (correspondenceType != null) {
            return correspondenceType.getTopLevelDocuments();
        } else {
            if (correspondenceScotType != null) {
                return correspondenceScotType.getTopLevelScotDocuments();
            } else {
                return "";
            }
        }
    }

    public static String getEWSectionName(CorrespondenceType correspondenceType) {
        if (correspondenceType != null) {
            return getEWPartDocument(correspondenceType);
        }
        return "";
    }

    public static String getScotSectionName(CorrespondenceScotType correspondenceScotType) {
        if (correspondenceScotType != null) {
            return getScotPartDocument(correspondenceScotType);
        }
        return "";
    }

    private static String getEWPartDocument(CorrespondenceType correspondence) {
        if (correspondence.getPart0Documents() != null) {
            return correspondence.getPart0Documents();
        }
        if (correspondence.getPart1Documents() != null) {
            return correspondence.getPart1Documents();
        }
        if (correspondence.getPart2Documents() != null) {
            return correspondence.getPart2Documents();
        }
        if (correspondence.getPart3Documents() != null) {
            return correspondence.getPart3Documents();
        }
        if (correspondence.getPart4Documents() != null) {
            return correspondence.getPart4Documents();
        }
        if (correspondence.getPart5Documents() != null) {
            return correspondence.getPart5Documents();
        }
        if (correspondence.getPart6Documents() != null) {
            return correspondence.getPart6Documents();
        }
        if (correspondence.getPart7Documents() != null) {
            return correspondence.getPart7Documents();
        }
        if (correspondence.getPart8Documents() != null) {
            return correspondence.getPart8Documents();
        }
        if (correspondence.getPart9Documents() != null) {
            return correspondence.getPart9Documents();
        }
        if (correspondence.getPart10Documents() != null) {
            return correspondence.getPart10Documents();
        }
        if (correspondence.getPart11Documents() != null) {
            return correspondence.getPart11Documents();
        }
        if (correspondence.getPart12Documents() != null) {
            return correspondence.getPart12Documents();
        }
        if (correspondence.getPart13Documents() != null) {
            return correspondence.getPart13Documents();
        }
        if (correspondence.getPart14Documents() != null) {
            return correspondence.getPart14Documents();
        }
        if (correspondence.getPart15Documents() != null) {
            return correspondence.getPart15Documents();
        }
        if (correspondence.getPart16Documents() != null) {
            return correspondence.getPart16Documents();
        }
        if (correspondence.getPart17Documents() != null) {
            return correspondence.getPart17Documents();
        }
        if (correspondence.getPart18Documents() != null) {
            return correspondence.getPart18Documents();
        }
        if (correspondence.getPart20Documents() != null) {
            return correspondence.getPart20Documents();
        }
        return "";
    }

    private static String getScotPartDocument(CorrespondenceScotType correspondenceScotType) {
        if (correspondenceScotType.getPart0ScotDocuments() != null) {
            return correspondenceScotType.getPart0ScotDocuments();
        }
        if (correspondenceScotType.getPart1ScotDocuments() != null) {
            return correspondenceScotType.getPart1ScotDocuments();
        }
        if (correspondenceScotType.getPart2ScotDocuments() != null) {
            return correspondenceScotType.getPart2ScotDocuments();
        }
        if (correspondenceScotType.getPart3ScotDocuments() != null) {
            return correspondenceScotType.getPart3ScotDocuments();
        }
        if (correspondenceScotType.getPart4ScotDocuments() != null) {
            return correspondenceScotType.getPart4ScotDocuments();
        }
        if (correspondenceScotType.getPart5ScotDocuments() != null) {
            return correspondenceScotType.getPart5ScotDocuments();
        }
        if (correspondenceScotType.getPart6ScotDocuments() != null) {
            return correspondenceScotType.getPart6ScotDocuments();
        }
        if (correspondenceScotType.getPart7ScotDocuments() != null) {
            return correspondenceScotType.getPart7ScotDocuments();
        }
        if (correspondenceScotType.getPart8ScotDocuments() != null) {
            return correspondenceScotType.getPart8ScotDocuments();
        }
        if (correspondenceScotType.getPart9ScotDocuments() != null) {
            return correspondenceScotType.getPart9ScotDocuments();
        }
        if (correspondenceScotType.getPart10ScotDocuments() != null) {
            return correspondenceScotType.getPart10ScotDocuments();
        }
        if (correspondenceScotType.getPart11ScotDocuments() != null) {
            return correspondenceScotType.getPart11ScotDocuments();
        }
        if (correspondenceScotType.getPart12ScotDocuments() != null) {
            return correspondenceScotType.getPart12ScotDocuments();
        }
        if (correspondenceScotType.getPart13ScotDocuments() != null) {
            return correspondenceScotType.getPart13ScotDocuments();
        }
        if (correspondenceScotType.getPart14ScotDocuments() != null) {
            return correspondenceScotType.getPart14ScotDocuments();
        }
        if (correspondenceScotType.getPart15ScotDocuments() != null) {
            return correspondenceScotType.getPart15ScotDocuments();
        }
        if (correspondenceScotType.getPart16ScotDocuments() != null) {
            return correspondenceScotType.getPart16ScotDocuments();
        }
        return "";
    }

    private static StringBuilder getCorrespondenceData(CorrespondenceType correspondence) {
        log.info("Correspondence data");
        String sectionName = getEWSectionName(correspondence);
        sectionName = sectionName.replace(".", "_");
        sectionName = sectionName.replace(" ", "_");
        StringBuilder sb = new StringBuilder();
        if (!sectionName.isEmpty()) {
            sb.append("\"t").append(sectionName).append(COLON).append("true").append(NEW_LINE);
        }
        return sb;
    }

    private static StringBuilder getCorrespondenceScotData(CorrespondenceScotType correspondenceScotType) {
        String scotSectionName = getScotSectionName(correspondenceScotType);
        scotSectionName = scotSectionName.replace(".", "_");
        scotSectionName = scotSectionName.replace(" ", "_");
        StringBuilder sb = new StringBuilder();
        if (!scotSectionName.isEmpty()) {
            sb.append("\"t_Scot_").append(scotSectionName).append(COLON).append("true").append(NEW_LINE);
        }
        return sb;
    }

    private static StringBuilder getCourtData(CaseData caseData, DefaultValues allocatedCourtAddress) {
        StringBuilder sb = new StringBuilder();
        if (allocatedCourtAddress != null) {
            sb.append("\"Court_addressLine1\":\"")
                    .append(nullCheck(allocatedCourtAddress.getTribunalCorrespondenceAddressLine1())).append(NEW_LINE)
                    .append("\"Court_addressLine2\":\"")
                    .append(nullCheck(allocatedCourtAddress.getTribunalCorrespondenceAddressLine2())).append(NEW_LINE)
                    .append("\"Court_addressLine3\":\"")
                    .append(nullCheck(allocatedCourtAddress.getTribunalCorrespondenceAddressLine3())).append(NEW_LINE)
                    .append("\"Court_town\":\"")
                    .append(nullCheck(allocatedCourtAddress.getTribunalCorrespondenceTown())).append(NEW_LINE)
                    .append("\"Court_county\":\"").append(NEW_LINE).append("\"Court_postCode\":\"")
                    .append(nullCheck(allocatedCourtAddress.getTribunalCorrespondencePostCode())).append(NEW_LINE)
                    .append("\"Court_telephone\":\"")
                    .append(nullCheck(allocatedCourtAddress.getTribunalCorrespondenceTelephone())).append(NEW_LINE)
                    .append("\"Court_fax\":\"").append(nullCheck(allocatedCourtAddress.getTribunalCorrespondenceFax()))
                    .append(NEW_LINE).append("\"Court_DX\":\"")
                    .append(nullCheck(allocatedCourtAddress.getTribunalCorrespondenceDX())).append(NEW_LINE)
                    .append("\"Court_Email\":\"")
                    .append(nullCheck(allocatedCourtAddress.getTribunalCorrespondenceEmail())).append(NEW_LINE);
        } else {
            if (caseData.getTribunalCorrespondenceAddress() != null) {
                sb.append("\"Court_addressLine1\":\"")
                        .append(nullCheck(caseData.getTribunalCorrespondenceAddress().getAddressLine1()))
                        .append(NEW_LINE).append("\"Court_addressLine2\":\"")
                        .append(nullCheck(caseData.getTribunalCorrespondenceAddress().getAddressLine2()))
                        .append(NEW_LINE).append("\"Court_addressLine3\":\"")
                        .append(nullCheck(caseData.getTribunalCorrespondenceAddress().getAddressLine3()))
                        .append(NEW_LINE).append("\"Court_town\":\"")
                        .append(nullCheck(caseData.getTribunalCorrespondenceAddress().getPostTown())).append(NEW_LINE)
                        .append("\"Court_county\":\"")
                        .append(nullCheck(caseData.getTribunalCorrespondenceAddress().getCounty())).append(NEW_LINE)
                        .append("\"Court_postCode\":\"")
                        .append(nullCheck(caseData.getTribunalCorrespondenceAddress().getPostCode())).append(NEW_LINE);
            }
            sb.append("\"Court_telephone\":\"").append(nullCheck(caseData.getTribunalCorrespondenceTelephone()))
                    .append(NEW_LINE).append("\"Court_fax\":\"")
                    .append(nullCheck(caseData.getTribunalCorrespondenceFax())).append(NEW_LINE)
                    .append("\"Court_DX\":\"").append(nullCheck(caseData.getTribunalCorrespondenceDX()))
                    .append(NEW_LINE).append("\"Court_Email\":\"")
                    .append(nullCheck(caseData.getTribunalCorrespondenceEmail())).append(NEW_LINE);
        }
        return sb;
    }

    @NotNull
    private static StringBuilder getNumberOfCopies(AddressLabelsAttributesType addressLabelsAttributesType,
                                                   List<AddressLabelTypeItem> addressLabelCollection2) {
        int numberOfCopies = Integer.parseInt(addressLabelsAttributesType.getNumberOfCopies());
        int startingLabel = Integer.parseInt(addressLabelsAttributesType.getStartingLabel());
        String showTelFax = addressLabelsAttributesType.getShowTelFax();

        return getAddressLabelsData(numberOfCopies, startingLabel, showTelFax, addressLabelCollection2);
    }

    private static int getPageLabelNumber(int startingLabel, int pageLabel) {
        int pageLabelNumber = pageLabel + 1;

        if (startingLabel > 1) {
            pageLabelNumber += startingLabel - 1;
        }

        if (pageLabelNumber > ADDRESS_LABELS_PAGE_SIZE) {
            int numberOfFullLabelPages = pageLabelNumber / ADDRESS_LABELS_PAGE_SIZE;
            pageLabelNumber = pageLabelNumber % ADDRESS_LABELS_PAGE_SIZE == 0
                    ? ADDRESS_LABELS_PAGE_SIZE
                    : pageLabelNumber - (numberOfFullLabelPages * ADDRESS_LABELS_PAGE_SIZE);
        }
        return pageLabelNumber;
    }

    private static StringBuilder getAddressLabelsDataSingleCase(CaseData caseData) {

        return getNumberOfCopies(caseData.getAddressLabelsAttributesType(), caseData.getAddressLabelCollection());
    }

    private static StringBuilder getAddressLabelsDataMultipleCase(MultipleData multipleData) {

        return getNumberOfCopies(multipleData.getAddressLabelsAttributesType(),
                multipleData.getAddressLabelCollection());

    }

    private static StringBuilder getAddressLabelsData(int numberOfCopies, int startingLabel, String showTelFax,
                                                      List<AddressLabelTypeItem> addressLabelCollection) {

        List<AddressLabelTypeItem> selectedAddressLabelCollection = getSelectedAddressLabels(addressLabelCollection);
        List<AddressLabelTypeItem> copiedAddressLabelCollection =
                getCopiedAddressLabels(selectedAddressLabelCollection, numberOfCopies);

        StringBuilder sb = new StringBuilder(40);
        sb.append("\"address_labels_page\":[\n");

        boolean startingLabelAboveOne = true;

        for (int i = 0; i < copiedAddressLabelCollection.size(); i++) {
            int pageLabelNumber = getPageLabelNumber(startingLabel, i);

            if (pageLabelNumber == 1 || startingLabelAboveOne) {
                startingLabelAboveOne = false;
                sb.append('{');
            }

            String templateLabelNumber = pageLabelNumber < 10
                    ? "0" + pageLabelNumber : String.valueOf(pageLabelNumber);
            sb.append(getAddressLabel(copiedAddressLabelCollection.get(i).getValue(), templateLabelNumber, showTelFax));

            if (pageLabelNumber == ADDRESS_LABELS_PAGE_SIZE || i == copiedAddressLabelCollection.size() - 1) {
                sb.append('}');
            }

            if (i != copiedAddressLabelCollection.size() - 1) {
                sb.append(",\n");
            }
        }
        sb.append("],\n");
        return sb;
    }

    private static StringBuilder getAddressLabel(AddressLabelType addressLabelType,
                                                 String labelNumber, String showTelFax) {
        StringBuilder sb = new StringBuilder(70);
        sb.append('"').append(LABEL).append(labelNumber).append("_Entity_Name_01\":\"")
                .append(nullCheck(addressLabelType.getLabelEntityName01())).append(NEW_LINE).append('"').append(LABEL)
                .append(labelNumber).append("_Entity_Name_02\":\"")
                .append(nullCheck(addressLabelType.getLabelEntityName02())).append(NEW_LINE)
                .append(getAddressLines(addressLabelType, labelNumber))
                .append(getTelFaxLine(addressLabelType, labelNumber, showTelFax)).append('"').append(LBL)
                .append(labelNumber).append("_Eef\":\"").append(nullCheck(addressLabelType.getLabelEntityReference()))
                .append(NEW_LINE).append('"').append(LBL).append(labelNumber).append("_Cef\":\"")
                .append(nullCheck(addressLabelType.getLabelCaseReference())).append('"');
        return sb;
    }

    private static StringBuilder getAddressLines(AddressLabelType addressLabelType, String labelNumber) {
        StringBuilder sb = new StringBuilder();

        int lineNum = 0;
        String addressLine = "";

        if (!isNullOrEmpty(nullCheck(addressLabelType.getLabelEntityAddress().getAddressLine1()))) {
            lineNum++;
            addressLine = nullCheck(addressLabelType.getLabelEntityAddress().getAddressLine1());
            sb.append(getAddressLine(addressLine, labelNumber, lineNum));
        }

        if (!isNullOrEmpty(nullCheck(addressLabelType.getLabelEntityAddress().getAddressLine2()))) {
            lineNum++;
            addressLine = nullCheck(addressLabelType.getLabelEntityAddress().getAddressLine2());
            sb.append(getAddressLine(addressLine, labelNumber, lineNum));
        }

        if (!isNullOrEmpty(nullCheck(addressLabelType.getLabelEntityAddress().getAddressLine3()))) {
            lineNum++;
            addressLine = nullCheck(addressLabelType.getLabelEntityAddress().getAddressLine3());
            sb.append(getAddressLine(addressLine, labelNumber, lineNum));
        }

        if (!isNullOrEmpty(nullCheck(addressLabelType.getLabelEntityAddress().getPostTown()))) {
            lineNum++;
            addressLine = nullCheck(addressLabelType.getLabelEntityAddress().getPostTown());
            sb.append(getAddressLine(addressLine, labelNumber, lineNum));
        }

        if (!isNullOrEmpty(nullCheck(addressLabelType.getLabelEntityAddress().getCounty()))) {
            lineNum++;
            addressLine = nullCheck(addressLabelType.getLabelEntityAddress().getCounty());
            if (lineNum < 5) {
                sb.append(getAddressLine(addressLine, labelNumber, lineNum));
            }
        }

        if (!isNullOrEmpty(nullCheck(addressLabelType.getLabelEntityAddress().getPostCode()))) {
            if (lineNum < 5) {
                lineNum++;
                addressLine = nullCheck(addressLabelType.getLabelEntityAddress().getPostCode());
            } else {
                addressLine += " " + nullCheck(addressLabelType.getLabelEntityAddress().getPostCode());
            }
            sb.append(getAddressLine(addressLine, labelNumber, lineNum));
        }
        return sb;
    }

    private static StringBuilder getAddressLine(String addressLine, String labelNumber, int lineNum) {
        StringBuilder sb = new StringBuilder();
        String lineNumber = "0" + lineNum;
        sb.append('"').append(LABEL).append(labelNumber).append("_Address_Line_").append(lineNumber)
                .append(COLON).append(addressLine).append(NEW_LINE);
        return sb;
    }

    private static StringBuilder getTelFaxLine(AddressLabelType addressLabelType, String labelNumber,
                                               String showTelFax) {
        StringBuilder sb = new StringBuilder();
        if (showTelFax.equals(YES)) {
            String tel = "";
            String fax = "";

            if (!isNullOrEmpty(addressLabelType.getLabelEntityTelephone())) {
                tel = addressLabelType.getLabelEntityTelephone();
            }

            if (isNullOrEmpty(tel)) {
                if (!isNullOrEmpty(addressLabelType.getLabelEntityFax())) {
                    tel = addressLabelType.getLabelEntityFax();
                }
            } else {
                if (!isNullOrEmpty(addressLabelType.getLabelEntityFax())) {
                    fax = addressLabelType.getLabelEntityFax();
                }
            }

            sb.append('"').append(LABEL).append(labelNumber).append("_Telephone\":\"").append(tel).append(NEW_LINE)
                    .append('"').append(LABEL).append(labelNumber).append("_Fax\":\"").append(fax).append(NEW_LINE);
        }
        return sb;
    }

    public static List<AddressLabelTypeItem> getSelectedAddressLabels(
            List<AddressLabelTypeItem> addressLabelCollection) {

        List<AddressLabelTypeItem> selectedAddressLabels = new ArrayList<>();

        if (addressLabelCollection != null && !addressLabelCollection.isEmpty()) {
            selectedAddressLabels = addressLabelCollection
                    .stream()
                    .filter(addressLabelTypeItem -> addressLabelTypeItem.getValue().getPrintLabel() != null
                            && addressLabelTypeItem.getValue().getPrintLabel().equals(YES))
                    .filter(addressLabelTypeItem -> addressLabelTypeItem.getValue().getFullName() != null
                            || addressLabelTypeItem.getValue().getFullAddress() != null)
                    .toList();
        }

        return selectedAddressLabels;
    }

    private static List<AddressLabelTypeItem> getCopiedAddressLabels(List<AddressLabelTypeItem> selectedAddressLabels,
                                                                     int numberOfCopies) {

        List<AddressLabelTypeItem> copiedAddressLabels = new ArrayList<>();
        if (!selectedAddressLabels.isEmpty() && numberOfCopies > 1) {
            selectedAddressLabels.stream().map(AddressLabelTypeItem::getValue)
                    .forEach(addressLabelType ->
                            IntStream.range(0, numberOfCopies).mapToObj(i -> new AddressLabelTypeItem())
                    .forEach(addressLabelTypeItem -> {
                        addressLabelTypeItem.setId(String.valueOf(copiedAddressLabels.size()));
                        addressLabelTypeItem.setValue(addressLabelType);
                        copiedAddressLabels.add(addressLabelTypeItem);
                    }));
        } else {
            return selectedAddressLabels;
        }

        return copiedAddressLabels;
    }

    public static Address getRespondentAddressET3(RespondentSumType respondentSumType) {
        return YES.equals(respondentSumType.getResponseReceived())
                && respondentSumType.getResponseRespondentAddress() != null
                && !isNullOrEmpty(respondentSumType.getResponseRespondentAddress().toString())
                ? respondentSumType.getResponseRespondentAddress()
                : respondentSumType.getRespondentAddress();

    }

    /**
     * Create a new DocumentTypeItem, copy from uploadedDocumentType and update TypeOfDocument.
     * @param uploadedDocumentType UploadedDocumentType to be added
     * @param typeOfDocument String to update TypeOfDocument
     * @param shortDescription short description of the document
     * @return DocumentTypeItem
     */
    public static DocumentTypeItem createDocumentTypeItem(UploadedDocumentType uploadedDocumentType,
                                                          String typeOfDocument, String shortDescription) {
        DocumentTypeItem documentTypeItem = fromUploadedDocument(uploadedDocumentType);
        DocumentType documentType = documentTypeItem.getValue();
        documentType.setTypeOfDocument(typeOfDocument);
        documentType.setShortDescription(shortDescription);
        documentType.setDateOfCorrespondence(LocalDate.now().toString());
        documentType.setTopLevelDocuments(
                uk.gov.hmcts.ecm.common.helpers.DocumentHelper.getTopLevelDocument(typeOfDocument));
        uk.gov.hmcts.ecm.common.helpers.DocumentHelper.setSecondLevelDocumentFromType(documentType, typeOfDocument);
        uk.gov.hmcts.ecm.common.helpers.DocumentHelper.setDocumentTypeForDocument(documentType);
        return documentTypeItem;
    }

    /**
     * Create a new DocumentTypeItem, copy from uploadedDocumentType and update TypeOfDocument.
     * @param uploadedDocumentType UploadedDocumentType to be added
     * @param typeOfDocument String to update TypeOfDocument
     * @return DocumentTypeItem
     */
    public static DocumentTypeItem createDocumentTypeItem(UploadedDocumentType uploadedDocumentType,
                                                          String typeOfDocument) {
        return createDocumentTypeItem(uploadedDocumentType, typeOfDocument, null);
    }

    public static DocumentTypeItem createDocumentTypeItemFromTopLevel(UploadedDocumentType uploadedDocumentType,
                                                          String topLevel,
                                                          String secondLevel) {
        DocumentTypeItem documentTypeItem = fromUploadedDocument(uploadedDocumentType);
        DocumentType documentType = documentTypeItem.getValue();
        documentType.setDateOfCorrespondence(LocalDate.now().toString());
        documentType.setTopLevelDocuments(topLevel);
        uk.gov.hmcts.ecm.common.helpers.DocumentHelper.setSecondLevelDocumentFromType(documentType, secondLevel);
        uk.gov.hmcts.ecm.common.helpers.DocumentHelper.setDocumentTypeForDocument(documentType);
        return documentTypeItem;
    }

    /**
     * Create a new DocumentTypeItem, copy from uploadedDocumentType and update TypeOfDocument.
     * @param uploadedDocumentType UploadedDocumentType to be added
     * @param topLevel top level document
     * @param secondLevel second level document
     * @return DocumentTypeItem
     */
    public static DocumentTypeItem createDocumentTypeItemFromTopLevel(UploadedDocumentType uploadedDocumentType,
                                                          String topLevel,
                                                          String secondLevel,
                                                          String shortDescription) {
        DocumentTypeItem documentTypeItem = fromUploadedDocument(uploadedDocumentType);
        DocumentType documentType = documentTypeItem.getValue();
        documentType.setShortDescription(shortDescription);
        documentType.setDateOfCorrespondence(LocalDate.now().toString());
        documentType.setTopLevelDocuments(topLevel);
        uk.gov.hmcts.ecm.common.helpers.DocumentHelper.setSecondLevelDocumentFromType(documentType, secondLevel);
        uk.gov.hmcts.ecm.common.helpers.DocumentHelper.setDocumentTypeForDocument(documentType);
        return documentTypeItem;
    }

    /**
     * Add document numbers to each of the docs in the case.
     * @param caseData CaseData
     */
    public static void setDocumentNumbers(BaseCaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getDocumentCollection())) {
            return;
        }
        caseData.getDocumentCollection().forEach(documentTypeItem -> {
            DocumentType documentType = documentTypeItem.getValue();
            documentType.setDocNumber(String.valueOf(caseData.getDocumentCollection()
                                                             .indexOf(documentTypeItem) + 1));
        });
    }

    /**
     * Add document to the document collection based on the provided index.
     * @param docTypeItem document type item
     * @param indexToAddString index of the document to be added
     */
    public static void addDocumentToCollectionAtIndex(List<DocumentTypeItem> documentCollection,
                                                      DocumentTypeItem docTypeItem, String indexToAddString) {
        if (StringUtils.isNotEmpty(indexToAddString)) {
            int indexToAdd = Integer.parseInt(indexToAddString);
            if (indexToAdd > 0 && indexToAdd <= documentCollection.size() + 1) {
                documentCollection.add(indexToAdd - 1, docTypeItem);
            } else {
                throw new IllegalArgumentException("The document number is invalid");
            }
        } else {
            documentCollection.add(docTypeItem);
        }
    }

    /**
     * Checks if the document is an ECC template based on the case type and letter type. If the ECC letter is of a
     * rejection letter type, it will return false and use the default logic as opposed to the ECC specific logic.
     *
     * @param caseTypeId     the case type ID
     * @param ewLetterType   the England/Wales letter type
     * @param scotlandLetterType the Scotland letter type
     * @return true if the document is an ECC template, false if it's an ECC rejection letter or any other template
     */
    static boolean isEccDocumentTemplate(String caseTypeId, CorrespondenceType ewLetterType,
                                         CorrespondenceScotType scotlandLetterType) {
        return (SCOTLAND_CASE_TYPE_ID.equals(caseTypeId)
            && isNotEmpty(scotlandLetterType)
            && defaultIfEmpty(scotlandLetterType.getTopLevelScotDocuments(), "").contains(ECC_DOCUMENT_SCOT_TEMPLATE)
            && !defaultIfEmpty(scotlandLetterType.getPart3ScotDocuments(), "").equals("19"))
            || (ENGLANDWALES_CASE_TYPE_ID.equals(caseTypeId)
            && isNotEmpty(ewLetterType)
            && defaultIfEmpty(ewLetterType.getTopLevelDocuments(), "").contains(ECC_DOCUMENT_ENG_TEMPLATE)
            && !EW_ECC_REJECTION_SECTIONS.contains(defaultIfEmpty(ewLetterType.getPart3Documents(), "")));
    }
}
