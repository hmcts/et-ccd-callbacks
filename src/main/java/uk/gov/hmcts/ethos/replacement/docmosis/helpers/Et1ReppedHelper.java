package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantHearingPreference;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantIndType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantOtherType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantRequestType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantWorkAddressType;
import uk.gov.hmcts.et.common.model.ccd.types.CreateRespondentType;
import uk.gov.hmcts.et.common.model.ccd.types.NewEmploymentType;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.constants.ET1ReppedConstants;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ecm.common.helpers.UtilHelper.listingFormatLocalDate;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.INDIVIDUAL_TYPE_CLAIMANT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET1ReppedConstants.CLAIM_DETAILS_MISSING;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET1ReppedConstants.COMPLETED;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET1ReppedConstants.INDIVIDUAL;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET1ReppedConstants.MONTHS;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET1ReppedConstants.NOTICE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET1ReppedConstants.NOT_COMPLETED;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET1ReppedConstants.NOT_SURE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET1ReppedConstants.NO_LONGER_WORKING;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET1ReppedConstants.ORGANISATION;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET1ReppedConstants.OTHER;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET1ReppedConstants.PAY_PERIODS;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET1ReppedConstants.RESPONDENT_PREAMBLE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET1ReppedConstants.SECTION_COMPLETE_LABEL;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET1ReppedConstants.TAB_PRE_LABEL;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET1ReppedConstants.TITLES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET1ReppedConstants.WEEKS;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET1ReppedConstants.WORKING;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.ClaimantHearingPreferencesValidator.PHONE_PREFERENCE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.ClaimantHearingPreferencesValidator.VIDEO_PREFERENCE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.EMPTY_STRING;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.UNEXPECTED_VALUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.getFirstListItem;

@Slf4j
public final class Et1ReppedHelper {

    private Et1ReppedHelper() {
        super();
        // Access through static methods
    }

    /**
     * Sets the create draft data for the ET1 Repped journey.
     * @param caseData the case data
     */
    public static void setCreateDraftData(CaseData caseData, String caseId) {
        caseData.setEt1ReppedSectionOne(NO);
        caseData.setEt1ReppedSectionTwo(NO);
        caseData.setEt1ReppedSectionThree(NO);
        setEt1Statuses(caseData, caseId);
    }

    /**
     * Sets the statuses for the ET1 Repped journey.
     * @param caseData the case data
     */
    public static void setEt1Statuses(CaseData caseData, String caseId) {
        caseData.setEt1ClaimStatuses(TAB_PRE_LABEL + et1ClaimStatus(caseData, caseId));
    }

    private static String et1ClaimStatus(CaseData caseData, String caseId) {
        String formatted = ET1ReppedConstants.LABEL.formatted(
                caseId,
                sectionCompleted(caseData.getEt1ReppedSectionOne()),
                listingFormatLocalDate(caseData.getEt1SectionOneDateCompleted()),
                caseId,
                sectionCompleted(caseData.getEt1ReppedSectionTwo()),
                listingFormatLocalDate(caseData.getEt1SectionTwoDateCompleted()),
                caseId,
                sectionCompleted(caseData.getEt1ReppedSectionThree()),
                listingFormatLocalDate(caseData.getEt1SectionThreeDateCompleted()));
        String downloadSection = anySectionCompleted(caseData)
                ? ET1ReppedConstants.DOWNLOAD_DRAFT_LABEL.formatted(caseId,
                listingFormatLocalDate(caseData.getDownloadDraftEt1Date()))
                : "";
        String submitSection = allSectionsCompleted(caseData)
                ? ET1ReppedConstants.ET1_SUBMIT_AVAILABLE.formatted(caseId)
                : ET1ReppedConstants.ET1_SUBMIT_UNAVAILABLE;
        return formatted + downloadSection + submitSection;

    }

    private static boolean anySectionCompleted(CaseData caseData) {
        return !isNullOrEmpty(caseData.getEt1ReppedSectionOne())
               && YES.equalsIgnoreCase(caseData.getEt1ReppedSectionOne())
               || !isNullOrEmpty(caseData.getEt1ReppedSectionTwo())
               && YES.equalsIgnoreCase(caseData.getEt1ReppedSectionTwo())
               || !isNullOrEmpty(caseData.getEt1ReppedSectionThree())
               && YES.equalsIgnoreCase(caseData.getEt1ReppedSectionThree());
    }

    /**
     * Sets the ET1 Repped section statuses once each section have been completed.
     * @param ccdRequest the ccd request
     */
    public static void setEt1SectionStatuses(CCDRequest ccdRequest) {
        String eventId = ccdRequest.getEventId();
        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        switch (eventId) {
            case "et1SectionOne" -> {
                setInitialSectionOneData(caseData);
                caseData.setEt1ReppedSectionOne(YES);
                caseData.setEt1SectionOneDateCompleted(LocalDate.now().toString());
            }
            case "et1SectionTwo" -> {
                setInitialSectionTwoData(caseData);
                caseData.setEt1ReppedSectionTwo(YES);
                caseData.setEt1SectionTwoDateCompleted(LocalDate.now().toString());
            }
            case "et1SectionThree" -> {
                caseData.setEt1ReppedSectionThree(YES);
                caseData.setEt1SectionThreeDateCompleted(LocalDate.now().toString());
            }
            default -> throw new IllegalArgumentException(UNEXPECTED_VALUE + eventId);
        }
        setEt1Statuses(caseData, ccdRequest.getCaseDetails().getCaseId());
    }

    private static String sectionCompleted(String section) {
        return isNullOrEmpty(section) || NO.equalsIgnoreCase(section)
                ? NOT_COMPLETED
                : COMPLETED;
    }

    public static boolean allSectionsCompleted(CaseData caseData) {
        return !isNullOrEmpty(caseData.getEt1ReppedSectionOne())
               && YES.equalsIgnoreCase(caseData.getEt1ReppedSectionOne())
               && !isNullOrEmpty(caseData.getEt1ReppedSectionTwo())
               && YES.equalsIgnoreCase(caseData.getEt1ReppedSectionTwo())
               && !isNullOrEmpty(caseData.getEt1ReppedSectionThree())
               && YES.equalsIgnoreCase(caseData.getEt1ReppedSectionThree());
    }

    /**
     * Validates the options selected for the ET1 Repped journey.
     * @param options the options
     * @return a list of error messages
     */
    public static List<String> validateSingleOption(List<String> options) {
        if (CollectionUtils.isNotEmpty(options) && options.size() > 1) {
            return List.of(ET1ReppedConstants.MULTIPLE_OPTION_ERROR);
        }
        return List.of();
    }

    /**
     * Generates the preamble for the additional respondent.
     * @param caseData the case data
     */
    public static void generateRespondentPreamble(CaseData caseData) {
        caseData.setAddAdditionalRespondentPreamble(RESPONDENT_PREAMBLE.formatted(getRespondentNameFromType(
                caseData.getRespondentType(),
                caseData.getRespondentFirstName(),
                caseData.getRespondentLastName(),
                caseData.getRespondentOrganisationName())));
    }

    /**
     * Generates the preamble for the claimant work address.
     * @param caseData the case data
     */
    public static void generateWorkAddressLabel(CaseData caseData) {
        if (caseData.getRespondentAddress() == null) {
            throw new NullPointerException("Respondent address is null");
        }
        caseData.setDidClaimantWorkAtSameAddressPreamble(caseData.getRespondentAddress().toString());
    }

    private static void setInitialSectionOneData(CaseData caseData) {
        if (isNullOrEmpty(caseData.getClaimantFirstName()) || isNullOrEmpty(caseData.getClaimantLastName())) {
            throw new NullPointerException("Claimant name is null or empty");
        }
        caseData.setClaimant(caseData.getClaimantFirstName() + " " + caseData.getClaimantLastName());
        claimantInformation(caseData);
    }

    private static void setInitialSectionTwoData(CaseData caseData) {
        caseData.setRespondent(getRespondentNameFromType(caseData.getRespondentType(),
                caseData.getRespondentFirstName(), caseData.getRespondentLastName(),
                caseData.getRespondentOrganisationName()));
    }

    public static String getSectionCompleted(CaseData caseData, String caseId) {
        return SECTION_COMPLETE_LABEL + et1ClaimStatus(caseData, caseId);
    }

    private static String getRespondentNameFromType(String respondentType, String firstName, String lastName,
                                                    String organisationName) {
        if (INDIVIDUAL.equals(respondentType)) {
            return firstName + " " + lastName;
        } else if (ORGANISATION.equals(respondentType)) {
            return organisationName;
        } else {
            return "";
        }
    }

    /**
     * Adds the data stored in the toplevel fields to the different objects used.
     * @param caseData the case data
     */
    public static void setEt1SubmitData(CaseData caseData) {
        addClaimantInformation(caseData);
        addRespondentInformation(caseData);
        addClaimDetails(caseData);
    }

    private static void addClaimDetails(CaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getEt1SectionThreeTypeOfClaim())) {
            return;
        }
        caseData.setTypesOfClaim(caseData.getEt1SectionThreeTypeOfClaim());
        ClaimantRequestType claimantRequestType = new ClaimantRequestType();
        claimantRequestType.setClaimDescription(caseData.getEt1SectionThreeClaimDetails());
        claimantRequestType.setClaimDescriptionDocument(caseData.getEt1SectionThreeDocumentUpload());
        caseData.getTypesOfClaim().forEach(claimType -> {
            switch (claimType) {
                case "discrimination" -> claimantRequestType.setDiscriminationClaims(
                        caseData.getDiscriminationTypesOfClaim());
                case "payRelated" -> claimantRequestType.setPayClaims(caseData.getPayTypesOfClaim());
                case "whistleBlowing" -> {
                    claimantRequestType.setWhistleblowing(getFirstListItem(caseData.getWhistleblowingYesNo()));
                    if (YES.equals(claimantRequestType.getWhistleblowing())) {
                        claimantRequestType.setWhistleblowingAuthority(caseData.getWhistleblowingRegulator());
                    }
                }
                case "otherTypesOfClaims" -> claimantRequestType.setOtherClaim(caseData.getOtherTypeOfClaimDetails());
                default -> {
                    // Do nothing for unmatched characters
                }
            }
        });
        if (CollectionUtils.isNotEmpty(caseData.getClaimSuccessful())) {
            claimantRequestType.setClaimOutcome(caseData.getClaimSuccessful());
            claimantRequestType.getClaimOutcome().forEach(claimOutcome -> {
                if ("compensation".equals(claimOutcome)) {
                    claimantRequestType.setClaimantCompensationText(caseData.getCompensationDetails());
                } else if ("tribunal".equals(claimOutcome)) {
                    claimantRequestType.setClaimantTribunalRecommendation(caseData.getTribunalRecommendationDetails());
                }
            });
        }
        claimantRequestType.setLinkedCases(getFirstListItem(caseData.getLinkedCasesYesNo()));
        claimantRequestType.setLinkedCasesDetail(caseData.getLinkedCasesDetails());
        caseData.setClaimantRequests(claimantRequestType);
    }

    private static void addRespondentInformation(CaseData caseData) {
        RespondentSumType respondentSumType = new RespondentSumType();
        respondentSumType.setRespondentName(getRespondentNameFromType(caseData.getRespondentType(),
                caseData.getRespondentFirstName(), caseData.getRespondentLastName(),
                caseData.getRespondentOrganisationName()));
        respondentSumType.setRespondentType(caseData.getRespondentType());
        if (INDIVIDUAL.equals(caseData.getRespondentType())) {
            respondentSumType.setRespondentFirstName(caseData.getRespondentFirstName());
            respondentSumType.setRespondentLastName(caseData.getRespondentLastName());
        } else if (ORGANISATION.equals(caseData.getRespondentType())) {
            respondentSumType.setRespondentOrganisation(caseData.getRespondentOrganisationName());
        }
        respondentSumType.setRespondentAddress(caseData.getRespondentAddress());
        respondentSumType.setRespondentAcasQuestion(caseData.getRespondentAcasYesNo());
        respondentSumType.setRespondentAcas(caseData.getRespondentAcasNumber());
        respondentSumType.setRespondentAcasNo(caseData.getRespondentNoAcasCertificateReason());
        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(respondentSumType);
        respondentSumTypeItem.setId(String.valueOf(UUID.randomUUID()));
        caseData.setRespondentCollection(new ArrayList<>());
        caseData.getRespondentCollection().add(respondentSumTypeItem);
        List<RespondentSumTypeItem> additionalRespondents = addAdditionalRespondents(caseData);
        if (CollectionUtils.isNotEmpty(additionalRespondents)) {
            caseData.getRespondentCollection().addAll(additionalRespondents);
        }
    }

    private static List<RespondentSumTypeItem> addAdditionalRespondents(CaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getEt1ReppedRespondentCollection())) {
            return new ArrayList<>();
        }
        return caseData.getEt1ReppedRespondentCollection().stream()
                .map(Et1ReppedHelper::getRespondentSumTypeItem)
                .toList();
    }

    @NotNull
    private static RespondentSumTypeItem getRespondentSumTypeItem(
            GenericTypeItem<CreateRespondentType> createRespondentType) {
        CreateRespondentType respondentDetails = createRespondentType.getValue();
        RespondentSumType respondentSumType = new RespondentSumType();
        respondentSumType.setRespondentName(getRespondentNameFromType(respondentDetails.getRespondentType(),
                respondentDetails.getRespondentFirstName(), respondentDetails.getRespondentLastName(),
                respondentDetails.getRespondentOrganisation()));
        respondentSumType.setRespondentType(respondentDetails.getRespondentType());
        if (INDIVIDUAL.equals(respondentDetails.getRespondentType())) {
            respondentSumType.setRespondentFirstName(respondentDetails.getRespondentFirstName());
            respondentSumType.setRespondentLastName(respondentDetails.getRespondentLastName());
        } else if (ORGANISATION.equals(respondentDetails.getRespondentType())) {
            respondentSumType.setRespondentOrganisation(respondentDetails.getRespondentOrganisation());

        } else {
            throw new IllegalArgumentException(UNEXPECTED_VALUE + respondentDetails.getRespondentType());
        }
        respondentSumType.setRespondentAddress(respondentDetails.getRespondentAddress());
        respondentSumType.setRespondentAcasQuestion(respondentDetails.getRespondentAcasQuestion());
        respondentSumType.setRespondentAcas(respondentDetails.getRespondentAcas());
        respondentSumType.setRespondentAcasNo(respondentDetails.getRespondentAcasNo());
        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(respondentSumType);
        respondentSumTypeItem.setId(String.valueOf(UUID.randomUUID()));
        return respondentSumTypeItem;
    }

    private static void addClaimantInformation(CaseData caseData) {
        caseData.setClaimantIndType(claimantInformation(caseData));
        caseData.setClaimantTypeOfClaimant(INDIVIDUAL_TYPE_CLAIMANT);
        caseData.setClaimantType(getClaimantType(caseData));
        caseData.setClaimantHearingPreference(getClaimantHearingPreference(caseData));
        caseData.setClaimantOtherType(claimantOtherType(caseData));
        caseData.setNewEmploymentType(claimantNewEmployment(caseData));
        caseData.setClaimantWorkAddress(claimantWorkAddress(caseData));
    }

    private static ClaimantWorkAddressType claimantWorkAddress(CaseData caseData) {
        ClaimantWorkAddressType claimantWorkAddressType = new ClaimantWorkAddressType();
        if (YES.equals(caseData.getDidClaimantWorkAtSameAddress())) {
            caseData.setClaimantWorkAddressQuestion(YES);
            claimantWorkAddressType.setClaimantWorkAddress(caseData.getRespondentAddress());
        } else if (NO.equals(caseData.getDidClaimantWorkAtSameAddress())) {
            caseData.setClaimantWorkAddressQuestion(NO);
            return caseData.getClaimantWorkAddress();
        }
        return null;
    }

    private static NewEmploymentType claimantNewEmployment(CaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getClaimantNewJob())) {
            return null;
        }
        NewEmploymentType newEmploymentType = new NewEmploymentType();
        newEmploymentType.setNewJob(getFirstListItem(caseData.getClaimantNewJob()));
        newEmploymentType.setNewlyEmployedFrom(caseData.getClaimantNewJobStartDate());
        newEmploymentType.setNewPayBeforeTax(formatPay(caseData.getClaimantNewJobPayBeforeTax()));
        newEmploymentType.setNewJobPayInterval(getFirstListItem(caseData.getClaimantNewJobPayPeriod()));
        return newEmploymentType;
    }

    private static ClaimantOtherType claimantOtherType(CaseData caseData) {
        ClaimantOtherType claimantOtherType = new ClaimantOtherType();
        if (CollectionUtils.isEmpty(caseData.getDidClaimantWorkForOrg())) {
            return claimantOtherType;
        }
        claimantOtherType.setPastEmployer(getFirstListItem(caseData.getDidClaimantWorkForOrg()));
        if (NO.equals(claimantOtherType.getPastEmployer())) {
            return claimantOtherType;
        }
        claimantOtherType.setStillWorking(getFirstListItem(caseData.getClaimantStillWorking()));
        claimantOtherType.setClaimantOccupation(caseData.getClaimantJobTitle());
        claimantOtherType.setClaimantEmployedFrom(caseData.getClaimantStartDate());
        claimantOtherType.setClaimantEmployedTo(caseData.getClaimantEndDate());
        if (claimantOtherType.getStillWorking() != null) {
            switch (claimantOtherType.getStillWorking()) {
                case WORKING -> claimantStillWorkingNoticePeriod(caseData, claimantOtherType);
                case NOTICE -> claimantNoticePeriod(caseData, claimantOtherType);
                case NO_LONGER_WORKING -> claimantNoLongerWorking(caseData, claimantOtherType);
                default -> {
                    // Do nothing for unmatched values
                }
            }
        }
        claimantOtherType.setClaimantAverageWeeklyHours(caseData.getClaimantAverageWeeklyWorkHours());
        claimantOtherType.setClaimantPayBeforeTax(formatPay(caseData.getClaimantPayBeforeTax()));
        claimantOtherType.setClaimantPayAfterTax(formatPay(caseData.getClaimantPayAfterTax()));
        claimantOtherType.setClaimantPayCycle(CollectionUtils.isEmpty(caseData.getClaimantPayType())
                                              || caseData.getClaimantPayType().get(0).equals(
                NOT_SURE)
                ? EMPTY_STRING
                : PAY_PERIODS.get(getFirstListItem(caseData.getClaimantPayType())));
        claimantOtherType.setClaimantPensionContribution(
                CollectionUtils.isEmpty(caseData.getClaimantPensionContribution())
                || caseData.getClaimantPensionContribution().get(0).equals(NOT_SURE)
                ? EMPTY_STRING
                : caseData.getClaimantPensionContribution().get(0));
        claimantOtherType.setClaimantPensionWeeklyContribution(formatPay(caseData.getClaimantWeeklyPension()));
        claimantOtherType.setClaimantBenefits(getFirstListItem(caseData.getClaimantEmployeeBenefits()));
        claimantOtherType.setClaimantBenefitsDetail(caseData.getClaimantBenefits());
        return claimantOtherType;
    }

    private static void claimantNoLongerWorking(CaseData caseData, ClaimantOtherType claimantOtherType) {
        if (CollectionUtils.isEmpty(caseData.getClaimantNoLongerWorkingQuestion())) {
            return;
        }
        claimantOtherType.setClaimantNoticePeriod(YES);
        if (CollectionUtils.isNotEmpty(caseData.getClaimantNoLongerWorking())) {
            if (caseData.getClaimantNoLongerWorking().get(0).equals(WEEKS)) {
                claimantOtherType.setClaimantNoticePeriodUnit(WEEKS);
                claimantOtherType.setClaimantNoticePeriodDuration(caseData.getClaimantNoLongerWorkingWeeks());
            } else if (caseData.getClaimantNoLongerWorking().get(0).equals(MONTHS)) {
                claimantOtherType.setClaimantNoticePeriodUnit(MONTHS);
                claimantOtherType.setClaimantNoticePeriodDuration(caseData.getClaimantNoLongerWorkingMonths());
            }
        }
    }

    private static void claimantNoticePeriod(CaseData caseData, ClaimantOtherType claimantOtherType) {
        claimantOtherType.setClaimantEmployedNoticePeriod(caseData.getClaimantWorkingNoticePeriodEndDate());
        if (CollectionUtils.isEmpty(caseData.getClaimantWorkingNoticePeriod())) {
            return;
        }
        if (caseData.getClaimantWorkingNoticePeriod().get(0).equals(WEEKS)) {
            claimantOtherType.setClaimantNoticePeriod(YES);
            claimantOtherType.setClaimantNoticePeriodUnit(WEEKS);
            claimantOtherType.setClaimantNoticePeriodDuration(caseData.getClaimantWorkingNoticePeriodWeeks());
        } else if (caseData.getClaimantWorkingNoticePeriod().get(0).equals(MONTHS)) {
            claimantOtherType.setClaimantNoticePeriod(YES);
            claimantOtherType.setClaimantNoticePeriodUnit(MONTHS);
            claimantOtherType.setClaimantNoticePeriodDuration(caseData.getClaimantWorkingNoticePeriodMonths());
        }
    }

    private static void claimantStillWorkingNoticePeriod(CaseData caseData, ClaimantOtherType claimantOtherType) {
        if (CollectionUtils.isEmpty(caseData.getClaimantStillWorkingNoticePeriod())) {
            return;
        }
        if (caseData.getClaimantStillWorkingNoticePeriod().get(0).equals(WEEKS)) {
            claimantOtherType.setClaimantNoticePeriod(YES);
            claimantOtherType.setClaimantNoticePeriodUnit(WEEKS);
            claimantOtherType.setClaimantNoticePeriodDuration(caseData.getClaimantStillWorkingNoticePeriodWeeks());
        } else if (caseData.getClaimantStillWorkingNoticePeriod().get(0).equals(MONTHS)) {
            claimantOtherType.setClaimantNoticePeriod(YES);
            claimantOtherType.setClaimantNoticePeriodUnit(MONTHS);
            claimantOtherType.setClaimantNoticePeriodDuration(caseData.getClaimantStillWorkingNoticePeriodMonths());
        } else if (caseData.getClaimantStillWorkingNoticePeriod().get(0).equals(NO)) {
            claimantOtherType.setClaimantNoticePeriod(NO);
        }
    }

    @NotNull
    private static ClaimantType getClaimantType(CaseData caseData) {
        ClaimantType claimantType = new ClaimantType();
        claimantType.setClaimantAddressUK(caseData.getClaimantContactAddress());
        return claimantType;
    }

    private static ClaimantIndType claimantInformation(CaseData caseData) {
        ClaimantIndType claimantIndType = new ClaimantIndType();
        claimantIndType.setClaimantFirstNames(caseData.getClaimantFirstName());
        claimantIndType.setClaimantLastName(caseData.getClaimantLastName());
        claimantIndType.setClaimantDateOfBirth(caseData.getClaimantDateOfBirth());
        claimantIndType.setClaimantSex(getFirstListItem(caseData.getClaimantSex()));
        if (isNullOrEmpty(caseData.getClaimantPreferredTitle())) {
            return claimantIndType;
        }
        claimantIndType.setClaimantPreferredTitle(TITLES.contains(caseData.getClaimantPreferredTitle())
                ? caseData.getClaimantPreferredTitle()
                : OTHER);
        if (claimantIndType.getClaimantPreferredTitle().equals(OTHER)) {
            claimantIndType.setClaimantTitleOther(caseData.getClaimantPreferredTitle());
        }
        return claimantIndType;
    }

    private static ClaimantHearingPreference getClaimantHearingPreference(CaseData caseData) {
        ClaimantHearingPreference claimantHearingPreference = new ClaimantHearingPreference();
        claimantHearingPreference.setHearingPreferences(hearingPreferenceMapping(caseData.getClaimantAttendHearing()));
        claimantHearingPreference.setReasonableAdjustments(
                reasonableAdjustmentsMapping(caseData.getClaimantSupportQuestion()));
        claimantHearingPreference.setReasonableAdjustmentsDetail(caseData.getClaimantSupportQuestionReason());
        claimantHearingPreference.setHearingLanguage(getFirstListItem(caseData.getClaimantHearingContactLanguage()));
        return claimantHearingPreference;
    }

    private static String reasonableAdjustmentsMapping(List<String> claimantSupportQuestion) {
        return CollectionUtils.isEmpty(claimantSupportQuestion)
               || !claimantSupportQuestion.get(0).equals(YES)
                  && !claimantSupportQuestion.get(0).equals(NO)
                ? null
                : claimantSupportQuestion.get(0);
    }

    private static List<String> hearingPreferenceMapping(List<String> claimantHearingPreferences) {
        List<String> hearingPreferencesMapped = new ArrayList<>();
        if (CollectionUtils.isEmpty(claimantHearingPreferences)) {
            return hearingPreferencesMapped;
        }
        claimantHearingPreferences.forEach(claimantHearingPreference -> {
            if (claimantHearingPreference.equals(VIDEO_PREFERENCE)) {
                hearingPreferencesMapped.add(VIDEO_PREFERENCE);
            } else if (claimantHearingPreference.equals(PHONE_PREFERENCE)) {
                hearingPreferencesMapped.add(PHONE_PREFERENCE);
            }
        });
        return hearingPreferencesMapped;
    }

    /**
     * Clears the ET1 Repped creation fields.
     * @param caseData the case data
     */
    public static void clearEt1ReppedCreationFields(CaseData caseData) {
        caseData.setEt1ReppedTriageAddress(null);
        caseData.setEt1ReppedTriageYesNo(null);
        caseData.setEt1ClaimStatuses(null);
        caseData.setEt1ReppedSectionOne(null);
        caseData.setEt1ReppedSectionTwo(null);
        caseData.setEt1ReppedSectionThree(null);
        caseData.setEt1SectionOneDateCompleted(null);
        caseData.setEt1SectionTwoDateCompleted(null);
        caseData.setEt1SectionThreeDateCompleted(null);
        caseData.setClaimantFirstName(null);
        caseData.setClaimantLastName(null);
        caseData.setClaimantDateOfBirth(null);
        caseData.setClaimantSex(null);
        caseData.setClaimantPreferredTitle(null);
        caseData.setClaimantContactAddress(null);
        caseData.setRepresentativeAttendHearing(null);
        caseData.setClaimantAttendHearing(null);
        caseData.setClaimantSupportQuestion(null);
        caseData.setClaimantSupportQuestionReason(null);
        caseData.setRepresentativeContactPreference(null);
        caseData.setContactPreferencePostReason(null);
        caseData.setRepresentativePhoneNumber(null);
        caseData.setRepresentativeReferenceNumber(null);
        caseData.setDidClaimantWorkForOrg(null);
        caseData.setClaimantStillWorking(null);
        caseData.setClaimantJobTitle(null);
        caseData.setClaimantStartDate(null);
        caseData.setClaimantEndDate(null);
        caseData.setClaimantStillWorkingNoticePeriod(null);
        caseData.setClaimantStillWorkingNoticePeriodWeeks(null);
        caseData.setClaimantStillWorkingNoticePeriodMonths(null);
        caseData.setClaimantWorkingNoticePeriod(null);
        caseData.setClaimantWorkingNoticePeriodEndDate(null);
        caseData.setClaimantWorkingNoticePeriodWeeks(null);
        caseData.setClaimantWorkingNoticePeriodMonths(null);
        caseData.setClaimantNoLongerWorkingQuestion(null);
        caseData.setClaimantNoLongerWorking(null);
        caseData.setClaimantNoLongerWorkingWeeks(null);
        caseData.setClaimantNoLongerWorkingMonths(null);
        caseData.setClaimantAverageWeeklyWorkHours(null);
        caseData.setClaimantPayBeforeTax(null);
        caseData.setClaimantPayAfterTax(null);
        caseData.setClaimantPayType(null);
        caseData.setClaimantPensionContribution(null);
        caseData.setClaimantWeeklyPension(null);
        caseData.setClaimantEmployeeBenefits(null);
        caseData.setClaimantBenefits(null);
        caseData.setClaimantNewJob(null);
        caseData.setClaimantNewJobStartDate(null);
        caseData.setClaimantNewJobPayBeforeTax(null);
        caseData.setClaimantNewJobPayPeriod(null);
        caseData.setRespondentType(null);
        caseData.setRespondentOrganisationName(null);
        caseData.setRespondentFirstName(null);
        caseData.setRespondentLastName(null);
        caseData.setRespondentAddress(null);
        caseData.setDidClaimantWorkAtSameAddressPreamble(null);
        caseData.setDidClaimantWorkAtSameAddress(null);
        caseData.setClaimantWorkAddressYes(null);
        caseData.setRespondentAcasYesNo(null);
        caseData.setRespondentAcasNumber(null);
        caseData.setRespondentNoAcasCertificateReason(null);
        caseData.setAddAdditionalRespondentPreamble(null);
        caseData.setEt1ReppedRespondentCollection(null);
        caseData.setEt1SectionThreeClaimDetails(null);
        caseData.setEt1SectionThreeDocumentUpload(null);
        caseData.setEt1SectionThreeTypeOfClaim(null);
        caseData.setDiscriminationTypesOfClaim(null);
        caseData.setPayTypesOfClaim(null);
        caseData.setWhistleblowingYesNo(null);
        caseData.setWhistleblowingRegulator(null);
        caseData.setOtherTypeOfClaimDetails(null);
        caseData.setClaimSuccessful(null);
        caseData.setCompensationDetails(null);
        caseData.setTribunalRecommendationDetails(null);
        caseData.setLinkedCasesYesNo(null);
        caseData.setLinkedCasesDetails(null);
        caseData.setAddAdditionalRespondent(null);
        caseData.setEt3ResponseHearingPanelPreference(null);
        caseData.setEt3ResponseHearingPanelPreferenceReason(null);
    }

    /**
     * Validates the grounds for the ET1 Repped journey.
     * @param caseData the case data
     * @return a list of error messages
     */
    public static List<String> validateGrounds(CaseData caseData) {
        if (ObjectUtils.isEmpty(caseData.getEt1SectionThreeDocumentUpload())
            && isNullOrEmpty(caseData.getEt1SectionThreeClaimDetails())) {
            return Collections.singletonList(CLAIM_DETAILS_MISSING);
        }
        return Collections.emptyList();
    }

    private static String formatPay(String pay) {
        if (isNullOrEmpty(pay)) {
            return null;
        } else if (pay.length() < 3) {
            return pay;
        } else {
            return pay.substring(0, pay.length() - 2);
        }
    }
}
