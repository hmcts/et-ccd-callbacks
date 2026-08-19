package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.exceptions.DocumentManagementException;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.JurCodesTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.VettingJurCodesTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.AdditionalCaseInfoType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantHearingPreference;
import uk.gov.hmcts.et.common.model.ccd.types.JurCodesType;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.et.common.model.ccd.types.VettingJurisdictionCodesType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.JurisdictionCodeHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.jpaservice.JpaVenueService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.IntWrapper;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.JurisdictionCodeConstants.JUR_CODE_CONCILIATION_TRACK_OP;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.JurisdictionCodeConstants.JUR_CODE_CONCILIATION_TRACK_SH;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.JurisdictionCodeConstants.JUR_CODE_CONCILIATION_TRACK_ST;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.JurisdictionCodeConstants.TRACK_NO;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.JurisdictionCodeConstants.TRACK_OPEN;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.JurisdictionCodeConstants.TRACK_SHORT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.JurisdictionCodeConstants.TRACK_STANDARD;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.ACAS_DOC_TYPE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.BEFORE_LABEL_ACAS;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.BEFORE_LABEL_ACAS_OPEN_TAB;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.BEFORE_LABEL_ET1;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.BEFORE_LABEL_ET1_ATTACHMENT;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.BEFORE_LABEL_TEMPLATE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.CLAIMANT_AND_RESPONDENT_ADDRESSES;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.CLAIMANT_AND_RESPONDENT_ADDRESSES_WITHOUT_WORK_ADDRESS;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.CLAIMANT_DETAILS_COMPANY;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.CLAIMANT_DETAILS_PERSON;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.COMPANY;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.DOCGEN_ERROR;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.ERA_ASSESSMENT_HEADER;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.ERROR_EXISTING_JUR_CODE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.ERROR_SELECTED_JUR_CODE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.ET1_ATTACHMENT_DOC_TYPE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.ET1_DOC_TYPE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.FIVE_ACAS_DOC_TYPE_ITEMS_COUNT;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.JUR_CODE_HTML;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.ONE_RESPONDENT_COUNT;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.RESPONDENT_ACAS_DETAILS;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.RESPONDENT_ACAS_DETAILS_WITH_ERA;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.RESPONDENT_DETAILS;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.TRACK_ALLOCATION_HTML;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.TRIBUNAL_ENGLAND;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.TRIBUNAL_LOCATION_LABEL;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.TRIBUNAL_OFFICE_LOCATION;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.TRIBUNAL_SCOTLAND;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.addressIsEmpty;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.EmploymentRightsActService.NOT_APPLICABLE;

@Slf4j
@Service
@RequiredArgsConstructor
public class Et1VettingService {

    public static final String ADDRESS_NOT_ENTERED = "Address not entered";

    private final TornadoService tornadoService;
    private final JpaVenueService jpaVenueService;
    private final EmploymentRightsActService employmentRightsActService;

    /**
     * Update et1VettingBeforeYouStart.
     * @param caseDetails Get caseId and Update caseData
     */
    public void initialiseEt1Vetting(CaseDetails caseDetails) {
        caseDetails.getCaseData().setEt1VettingBeforeYouStart(initialBeforeYouStart(caseDetails));
        caseDetails.getCaseData().setEt1VettingClaimantDetailsMarkUp(
            getInitialClaimantDetailsMarkUp(caseDetails.getCaseData()));
        caseDetails.getCaseData().setEt1VettingRespondentDetailsMarkUp(
            initialRespondentDetailsMarkUp(caseDetails.getCaseData()));
        populateRespondentAcasDetailsMarkUp(caseDetails.getCaseData());
        populateEraAssessmentMarkUp(caseDetails.getCaseData());
        initialEt1ReasonableAdjustments(caseDetails);
    }

    private static void initialEt1ReasonableAdjustments(CaseDetails caseDetails) {
        CaseData caseData = caseDetails.getCaseData();
        ClaimantHearingPreference hearingPreference = caseData.getClaimantHearingPreference();
        Optional.ofNullable(hearingPreference)
                .map(ClaimantHearingPreference::getReasonableAdjustments)
                .ifPresent(reasonableAdjustment -> {
                    caseData.setEt1ReasonableAdjustmentsQuestion(YES.equals(reasonableAdjustment) ? YES : NO);
                    caseData.setEt1ReasonableAdjustmentsTextArea(hearingPreference.getReasonableAdjustmentsDetail());
                });
    }

    /**
     * This method populates the hearing venue. The office is determined from the previous screen where the user
     * picks the tribunal office the case should be listed in. If they choose a different office to the current one,
     * they should see the venues for that office
     * @param caseData holds all the casedata
     */
    public void populateHearingVenue(CaseData caseData) {
        if (DynamicFixedListType.getSelectedLabel(caseData.getRegionalOfficeList()).isPresent()) {
            caseData.setEt1TribunalRegion(caseData.getRegionalOfficeList().getSelectedLabel());
        } else {
            caseData.setEt1TribunalRegion(caseData.getManagingOffice());
        }
        caseData.setEt1HearingVenues(getHearingVenuesList(caseData.getEt1TribunalRegion()));
        if (caseData.getSuggestedHearingVenues() != null
                && caseData.getSuggestedHearingVenues().getValue() != null
                && caseData.getEt1HearingVenues()
                .isValidCodeForList(caseData.getSuggestedHearingVenues().getValue().getCode())) {
            caseData.getEt1HearingVenues().setValue(caseData.getSuggestedHearingVenues().getValue());
        }
    }

    public void clearEt1FieldsFromCaseData(CaseData caseData) {
        caseData.setEt1VettingBeforeYouStart(null);
        caseData.setEt1VettingRespondentDetailsMarkUp(null);
        caseData.setEt1VettingRespondentAcasDetails1(null);
        caseData.setEt1VettingRespondentAcasDetails2(null);
        caseData.setEt1VettingRespondentAcasDetails3(null);
        caseData.setEt1VettingRespondentAcasDetails4(null);
        caseData.setEt1VettingRespondentAcasDetails5(null);
        caseData.setEt1VettingRespondentAcasDetails6(null);
        caseData.setExistingJurisdictionCodes(null);
        caseData.setEt1VettingClaimantDetailsMarkUp(null);
        caseData.setTrackAllocation(null);
        caseData.setEt1AddressDetails(null);
        caseData.setTribunalCorrespondenceAddress(null);
        caseData.setRegionalOffice(null);
        caseData.setEt1VettingEraAssessmentMarkUp(null);
    }

    /**
     * Populates hearing venues for suggestedHearingVenues from the managing office.
     * @param caseData data on the case.
     */
    public void populateSuggestedHearingVenues(CaseData caseData) {
        DynamicFixedListType hearingVenuesList = getHearingVenuesList(caseData.getManagingOffice());
        DynamicFixedListType suggestedHearingVenues = caseData.getSuggestedHearingVenues();
        if (suggestedHearingVenues != null) {
            hearingVenuesList.setValue(suggestedHearingVenues.getValue());
        }
        caseData.setSuggestedHearingVenues(hearingVenuesList);
    }

    /**
     * Prepare wordings to be displayed in et1VettingBeforeYouStart.
     * Check an uploaded document in documentCollection
     *  For ET1 form
     *  - get and display ET1 form
     *  For Acas cert
     *  - get and count the number of Acas certs
     *  - if 0 Acas certs, hide the Acas link
     *  - if 1-5 Acas cert(s), display one or multi Acas link(s)
     *  - if 6 or more Acas certs, display a link to case doc tab
     * @param caseDetails Get caseId and documentCollection
     * @return et1VettingBeforeYouStart
     */
    private String initialBeforeYouStart(CaseDetails caseDetails) {

        String et1Display = "";
        String acasDisplay = "";
        String et1Attachment = "";
        IntWrapper acasCount = new IntWrapper(0);

        List<DocumentTypeItem> documentCollection = caseDetails.getCaseData().getDocumentCollection();
        if (documentCollection != null) {
            et1Display = documentCollection
                    .stream()
                    .filter(d -> defaultIfEmpty(d.getValue().getTypeOfDocument(), "")
                            .equals(ET1_DOC_TYPE))
                    .map(d -> String.format(BEFORE_LABEL_ET1, createDocLinkBinary(d)))
                    .collect(Collectors.joining());
            acasDisplay = documentCollection
                    .stream()
                    .filter(d -> defaultIfEmpty(d.getValue().getTypeOfDocument(), "")
                            .equals(ACAS_DOC_TYPE))
                    .map(d -> String.format(
                             BEFORE_LABEL_ACAS, createDocLinkBinary(d), acasCount.incrementAndReturnValue()))
                    .collect(Collectors.joining());
            et1Attachment = documentCollection
                    .stream()
                    .filter(d -> defaultIfEmpty(d.getValue().getTypeOfDocument(), "")
                            .equals(ET1_ATTACHMENT_DOC_TYPE))
                    .map(d -> String.format(BEFORE_LABEL_ET1_ATTACHMENT,
                            createDocLinkBinary(d), d.getValue().getUploadedDocument().getDocumentFilename()))
                    .collect(Collectors.joining());
        }

        if (acasCount.getValue() > FIVE_ACAS_DOC_TYPE_ITEMS_COUNT) {
            acasDisplay = String.format(BEFORE_LABEL_ACAS_OPEN_TAB, caseDetails.getCaseId());
        }

        return String.format(BEFORE_LABEL_TEMPLATE, et1Display, acasDisplay, et1Attachment);
    }

    private String createDocLinkBinary(DocumentTypeItem documentTypeItem) {
        String documentBinaryUrl = documentTypeItem.getValue().getUploadedDocument().getDocumentBinaryUrl();
        return documentBinaryUrl.substring(documentBinaryUrl.indexOf("/documents/"));
    }

    /**
     * Prepare wordings to be displayed in et1VettingClaimantDetailsMarkUp
     * for the type of current claimant, i.e., Person or Company.
     * @param caseData Get ClaimantIndType and ClaimantType
     * @return et1VettingClaimantDetailsMarkUp
     */
    private String getInitialClaimantDetailsMarkUp(CaseData caseData) {
        if (COMPANY.equals(caseData.getClaimantTypeOfClaimant())) {
            return String.format(CLAIMANT_DETAILS_COMPANY,
                caseData.getClaimantCompany() != null
                    ? caseData.getClaimantCompany()
                    : "Company name not specified.",
                toAddressWithTab(caseData.getClaimantType().getClaimantAddressUK()));
        }

        if (caseData.getClaimantIndType() == null) {
            return String.format(CLAIMANT_DETAILS_PERSON, "Name not specified.",
                toAddressWithTab(caseData.getClaimantType().getClaimantAddressUK()));
        }

        String firstName = defaultIfEmpty(caseData.getClaimantIndType().getClaimantFirstNames(), "");
        String lastName = defaultIfEmpty(caseData.getClaimantIndType().getClaimantLastName(), "");
        String fullName = (firstName + " " + lastName).trim();
        return String.format(CLAIMANT_DETAILS_PERSON,
            fullName.isEmpty() ? "Name not specified." : fullName,
            toAddressWithTab(caseData.getClaimantType().getClaimantAddressUK()));
    }

    /**
     * Prepare wordings to be displayed in et1VettingRespondentDetailsMarkUp.
     * @param caseData Get RespondentCollection
     * @return et1VettingRespondentDetailsMarkUp
     */
    private String initialRespondentDetailsMarkUp(CaseData caseData) {
        if (caseData.getRespondentCollection().size() == ONE_RESPONDENT_COUNT) {
            RespondentSumType respondentSumType = caseData.getRespondentCollection().getFirst().getValue();
            return String.format(RESPONDENT_DETAILS, "",
                    respondentSumType.getRespondentName(),
                    toAddressWithTab(respondentSumType.getRespondentAddress()));
        } else {
            IntWrapper count = new IntWrapper(0);
            return caseData.getRespondentCollection()
                    .stream()
                    .map(r -> String.format(RESPONDENT_DETAILS,
                            count.incrementAndReturnValue(),
                            r.getValue().getRespondentName(),
                            toAddressWithTab(r.getValue().getRespondentAddress())))
                    .collect(Collectors.joining());
        }
    }

    private void populateRespondentAcasDetailsMarkUp(CaseData caseData) {
        List<RespondentSumTypeItem> respondentList = caseData.getRespondentCollection();
        if (CollectionUtils.isEmpty(respondentList)) {
            return;
        }
        int count = Math.min(respondentList.size(), 6);
        IntStream.range(0, count).forEach(i -> {
            RespondentSumType respondent = respondentList.get(i).getValue();
            int respondentNumber = i + 1;
            String markUp = generateRespondentAndAcasDetails(caseData, respondent, respondentNumber);
            setRespondentAcasDetailsMarkUp(caseData, respondentNumber, markUp);
        });
    }

    private void setRespondentAcasDetailsMarkUp(CaseData caseData, int respondentNumber, String markUp) {
        switch (respondentNumber) {
            case 1:
                caseData.setEt1VettingRespondentAcasDetails1(markUp);
                break;
            case 2:
                caseData.setEt1VettingRespondentAcasDetails2(markUp);
                break;
            case 3:
                caseData.setEt1VettingRespondentAcasDetails3(markUp);
                break;
            case 4:
                caseData.setEt1VettingRespondentAcasDetails4(markUp);
                break;
            case 5:
                caseData.setEt1VettingRespondentAcasDetails5(markUp);
                break;
            case 6:
                caseData.setEt1VettingRespondentAcasDetails6(markUp);
                break;
            default:
                break;
        }
    }

    private String generateRespondentAndAcasDetails(CaseData caseData, RespondentSumType respondent,
                                                    int respondentNumber) {
        if (caseData == null || respondent == null) {
            return "";
        }

        String acasStatus = respondent.getRespondentAcas() == null
            ? "No certificate provided."
            : String.format("%s", respondent.getRespondentAcas());

        if (!employmentRightsActService.isEraOctober2026(caseData)) {
            return String.format(RESPONDENT_ACAS_DETAILS, respondentNumber, respondent.getRespondentName(),
                toAddressWithTab(respondent.getRespondentAddress()), acasStatus);
        }

        String dateOfLastEvent = getDateOfLastEvent(caseData);
        String acasReceiptDate = respondent.getAcasCertificateReceiptDate();
        String acasIssueDate = respondent.getAcasCertificateIssueDate();
        String receiptDate = caseData.getReceiptDate();

        String effectiveElapsedTime = calculateEffectiveElapsedTime(receiptDate, dateOfLastEvent,
                acasReceiptDate, acasIssueDate);

        return String.format(RESPONDENT_ACAS_DETAILS_WITH_ERA, respondentNumber, respondent.getRespondentName(),
            toAddressWithTab(respondent.getRespondentAddress()), acasStatus,
            formatDisplayDate(dateOfLastEvent),
            formatDisplayDate(acasReceiptDate),
            formatDisplayDate(acasIssueDate),
            formatDisplayDate(receiptDate),
            effectiveElapsedTime != null ? effectiveElapsedTime : "Not calculated");
    }

    private String getDateOfLastEvent(CaseData caseData) {
        if (caseData == null) {
            return null;
        }
        if (caseData.getClaimantOtherType() != null
                && !isNullOrEmpty(caseData.getClaimantOtherType().getDateOfLastEvent())) {
            return caseData.getClaimantOtherType().getDateOfLastEvent();
        }
        return null;
    }

    /**
     * Populates the ERA Limitation Assessment Markdown panel and sets the default response state for et1VettingEra.
     *
     * <p>If the claim is submitted before 1st October 2026 or the ERA feature flag is disabled,
     * {@code et1VettingEra} is set to "Not applicable" and the ERA assessment markdown is set to {@code null}.
     *
     * <p>If the ERA feature is active, each respondent's Effective Elapsed Time is evaluated.
     * If at least one respondent has an Effective Elapsed Time greater than 3 months and less than or equal to
     * 6 months, the markdown panel is populated listing the triggering respondent(s) and their effective elapsed time.
     * In this case, {@code et1VettingEra} is reset to {@code null} if unselected or previously "Not applicable",
     * requiring mandatory selection by the caseworker.
     *
     * <p>If no respondents fall within the 3 to 6 month window, {@code et1VettingEra} defaults to "Not applicable"
     * and the markdown panel is set to {@code null}.
     *
     * @param caseData the case data containing claim and respondent details
     */
    private void populateEraAssessmentMarkUp(CaseData caseData) {
        if (!employmentRightsActService.isEraOctober2026(caseData)) {
            caseData.setEt1VettingEra(NOT_APPLICABLE);
            caseData.setEt1VettingEraAssessmentMarkUp(null);
            return;
        }

        List<RespondentSumTypeItem> respondentList = caseData.getRespondentCollection();
        if (CollectionUtils.isEmpty(respondentList)) {
            caseData.setEt1VettingEra(NOT_APPLICABLE);
            caseData.setEt1VettingEraAssessmentMarkUp(null);
            return;
        }

        String dateOfLastEvent = getDateOfLastEvent(caseData);
        String receiptDate = caseData.getReceiptDate();
        if (isNullOrEmpty(dateOfLastEvent) || isNullOrEmpty(receiptDate)) {
            caseData.setEt1VettingEra(NOT_APPLICABLE);
            caseData.setEt1VettingEraAssessmentMarkUp(null);
            return;
        }

        int count = Math.min(respondentList.size(), 6);
        StringBuilder triggeringRespondents = new StringBuilder();

        for (int i = 0; i < count; i++) {
            RespondentSumType respondent = respondentList.get(i).getValue();
            int respondentNumber = i + 1;
            String acasReceiptDate = respondent.getAcasCertificateReceiptDate();
            String acasIssueDate = respondent.getAcasCertificateIssueDate();

            Period period = calculateEffectiveElapsedPeriod(receiptDate, dateOfLastEvent,
                acasReceiptDate, acasIssueDate);
            if (period != null && isBetweenThreeAndSixMonths(period)) {
                String elapsedTimeStr = formatPeriod(period.getYears() * 12 + period.getMonths(), period.getDays());
                triggeringRespondents.append(String.format("• Respondent %d - %s%n%n", respondentNumber,
                    elapsedTimeStr));
            }
        }

        if (!triggeringRespondents.isEmpty()) {
            String markUp = ERA_ASSESSMENT_HEADER + triggeringRespondents;
            caseData.setEt1VettingEraAssessmentMarkUp(markUp);
            if (isNullOrEmpty(caseData.getEt1VettingEra()) || NOT_APPLICABLE.equals(caseData.getEt1VettingEra())) {
                caseData.setEt1VettingEra(null);
            }
        } else {
            caseData.setEt1VettingEraAssessmentMarkUp(null);
            caseData.setEt1VettingEra(NOT_APPLICABLE);
        }
    }

    private static boolean isBetweenThreeAndSixMonths(Period period) {
        int totalMonths = period.getYears() * 12 + period.getMonths();
        int totalDays = period.getDays();
        boolean isGreaterThanThreeMonths = totalMonths > 3 || (totalMonths == 3 && totalDays > 0);
        boolean isLessThanOrEqualToSixMonths = totalMonths < 6 || (totalMonths == 6 && totalDays == 0);
        return isGreaterThanThreeMonths && isLessThanOrEqualToSixMonths;
    }

    /**
     * Calculates the Effective Elapsed Period between the Date of Last Event and the ET1 Receipt Date,
     * adjusted for any Acas Early Conciliation period.
     */
    public static Period calculateEffectiveElapsedPeriod(String receiptDateStr, String dateOfLastEventStr,
                                                         String acasReceiptDateStr, String acasIssueDateStr) {
        if (isNullOrEmpty(receiptDateStr) || isNullOrEmpty(dateOfLastEventStr)) {
            return null;
        }
        try {
            LocalDate receiptDate = LocalDate.parse(receiptDateStr);
            LocalDate dateOfLastEvent = LocalDate.parse(dateOfLastEventStr);

            long acasDays = 0;
            if (!isNullOrEmpty(acasReceiptDateStr) && !isNullOrEmpty(acasIssueDateStr)) {
                LocalDate acasReceiptDate = LocalDate.parse(acasReceiptDateStr);
                LocalDate acasIssueDate = LocalDate.parse(acasIssueDateStr);
                if (acasReceiptDate.equals(acasIssueDate)) {
                    acasDays = 1;
                } else if (acasIssueDate.isAfter(acasReceiptDate)) {
                    acasDays = ChronoUnit.DAYS.between(acasReceiptDate, acasIssueDate);
                }
            }

            LocalDate adjustedReceiptDate = receiptDate.minusDays(acasDays);
            if (adjustedReceiptDate.isBefore(dateOfLastEvent)) {
                return Period.ZERO;
            }

            return Period.between(dateOfLastEvent, adjustedReceiptDate);
        } catch (Exception e) {
            log.error("Error calculating effective elapsed period", e);
            return null;
        }
    }

    /**
     * Calculates the Effective Elapsed Time between the Date of Last Event and the ET1 Receipt Date,
     * adjusted for any Acas Early Conciliation period.
     *
     * <p>The formula applied is:
     * <pre>
     * Effective Elapsed Time = (ET1 Receipt Date - Date of Last Event)
     *                          - (Date Acas Certificate Issued - Date Received by Acas)
     * </pre>
     *
     * <p>Calendar dates are used for period calculations rather than fixed day-to-month conversions.
     * If Acas certificate dates are present, the number of days between Acas receipt and certificate issue
     * is subtracted from the ET1 receipt date prior to computing the period from the Date of Last Event.
     * If the Acas receipt date and issue date are the same, the Acas conciliation period is treated as 1 day.
     *
     * @param receiptDateStr the ET1 receipt date (YYYY-MM-DD format)
     * @param dateOfLastEventStr the date of the last event (YYYY-MM-DD format)
     * @param acasReceiptDateStr the date conciliation request was received by Acas (YYYY-MM-DD format, optional)
     * @param acasIssueDateStr the date Acas certificate was issued (YYYY-MM-DD format, optional)
     * @return a formatted string representing the effective elapsed time (e.g. "4 months", "2 months 20 days",
     *         "15 days", "0 days"), or {@code null} if required dates are missing or invalid
     */
    public static String calculateEffectiveElapsedTime(String receiptDateStr, String dateOfLastEventStr,
                                                       String acasReceiptDateStr, String acasIssueDateStr) {
        Period period = calculateEffectiveElapsedPeriod(receiptDateStr, dateOfLastEventStr,
                acasReceiptDateStr, acasIssueDateStr);
        if (period == null) {
            return null;
        }
        int months = (period.getYears() * 12) + period.getMonths();
        int days = period.getDays();
        return formatPeriod(months, days);
    }

    private static String formatPeriod(int months, int days) {
        if (months == 0 && days == 0) {
            return "0 days";
        }
        StringBuilder sb = new StringBuilder();
        if (months > 0) {
            sb.append(months).append(months == 1 ? " month" : " months");
        }
        if (days > 0) {
            if (months > 0) {
                sb.append(" ");
            }
            sb.append(days).append(days == 1 ? " day" : " days");
        }
        return sb.toString();
    }

    private String formatDisplayDate(String dateStr) {
        if (isNullOrEmpty(dateStr)) {
            return "-";
        }
        try {
            LocalDate date = LocalDate.parse(dateStr);
            return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception e) {
            return dateStr;
        }
    }

    /**
     * Generate the Existing Jurisdiction Code list in HTML.
     */
    public String generateJurisdictionCodesHtml(List<JurCodesTypeItem> jurisdictionCodes) {
        StringBuilder sb = new StringBuilder();
        for (JurCodesTypeItem codeItem : jurisdictionCodes) {
            JurisdictionCodeHelper.populateCodeNameAndDescriptionHtml(
                    sb, codeItem.getValue().getJuridictionCodesList());
        }
        return String.format(JUR_CODE_HTML, sb);
    }

    /**
     * Validates the jurisdiction codes added by the caseworker in the vettingJurisdictionCodeCollection
     * to ensure that existing codes cannot be re-added and that each code is unique, with no duplicate entries.
     *
     * @return a list of validation errors
     */
    public List<String> validateJurisdictionCodes(CaseData caseData) {
        List<String> errors = new ArrayList<>();
        List<VettingJurCodesTypeItem> jurisdictionCodesList = caseData.getVettingJurisdictionCodeCollection();

        if (CollectionUtils.isNotEmpty(jurisdictionCodesList)) {
            // Check if the jurisdiction codes already exist in the jurisdictionCodesCollection
            if (CollectionUtils.isNotEmpty(caseData.getJurCodesCollection())) {
                List<String> existingCodes = caseData.getJurCodesCollection().stream()
                        .map(existingCode -> existingCode.getValue().getJuridictionCodesList())
                        .toList();

                jurisdictionCodesList.stream()
                        .filter(codesTypeItem -> existingCodes.contains(
                                codesTypeItem.getValue().getEt1VettingJurCodeList()))
                        .forEach(c -> errors.add(String.format(ERROR_EXISTING_JUR_CODE,
                                c.getValue().getEt1VettingJurCodeList())));
            }

            // Check if a jurisdiction code has been added more than once
            // First, get a list of all the Et1 Vetting Jurisdiction codes
            List<String> et1VettingCodeList = jurisdictionCodesList.stream()
                    .map(c -> c.getValue().getEt1VettingJurCodeList())
                    .toList();
            // Then, check if any code has been added more than once
            et1VettingCodeList.stream()
                    .filter(code -> Collections.frequency(et1VettingCodeList, code) > 1)
                    .collect(Collectors.toSet())
                    .forEach(code -> errors.add(String.format(ERROR_SELECTED_JUR_CODE, code)));
        }

        return errors;
    }

    /**
     * Add the jurisdiction codes that have been added by the caseworker to jurCodesCollection.
     * Set the Track Allocation field which default the longest track for a claim based on the jurisdiction codes
     */
    public String populateEt1TrackAllocationHtml(CaseData caseData) {
        if (CollectionUtils.isNotEmpty(caseData.getVettingJurisdictionCodeCollection())) {
            caseData.getVettingJurisdictionCodeCollection()
                    .forEach(codeItem -> addJurCodeToExistingCollection(caseData, codeItem.getValue()));
        }

        if (caseData.getJurCodesCollection().stream()
            .anyMatch(c -> JUR_CODE_CONCILIATION_TRACK_OP.contains(c.getValue().getJuridictionCodesList()))) {
            caseData.setTrackType(TRACK_OPEN);
            return String.format(TRACK_ALLOCATION_HTML, TRACK_OPEN);
        } else if (caseData.getJurCodesCollection().stream()
            .anyMatch(c -> JUR_CODE_CONCILIATION_TRACK_ST.contains(c.getValue().getJuridictionCodesList()))) {
            caseData.setTrackType(TRACK_STANDARD);
            return String.format(TRACK_ALLOCATION_HTML, TRACK_STANDARD);
        } else if (caseData.getJurCodesCollection().stream()
            .anyMatch(c -> JUR_CODE_CONCILIATION_TRACK_SH.contains(c.getValue().getJuridictionCodesList()))) {
            caseData.setTrackType(TRACK_SHORT);
            return String.format(TRACK_ALLOCATION_HTML, TRACK_SHORT);
        } else {
            caseData.setTrackType(TRACK_NO);
            return String.format(TRACK_ALLOCATION_HTML, TRACK_NO);
        }
    }

    /**
     * Populates tribunal office location and regional office label/list based on managing office location.
     */
    public void populateTribunalOfficeFields(CaseData caseData) {
        String managingOffice = caseData.getManagingOffice();
        String tribunalLocation = TribunalOffice.isScotlandOffice(managingOffice)
            ? TRIBUNAL_SCOTLAND : TRIBUNAL_ENGLAND;
        caseData.setTribunalAndOfficeLocation(String
            .format(TRIBUNAL_OFFICE_LOCATION, tribunalLocation, managingOffice));
        caseData.setRegionalOffice(String.format(TRIBUNAL_LOCATION_LABEL, tribunalLocation));
        caseData.setRegionalOfficeList(populateRegionalOfficeList(tribunalLocation, managingOffice));
    }

    private DynamicFixedListType populateRegionalOfficeList(String tribunalLocation, String managingOffice) {
        List<TribunalOffice> tribunalOffices = tribunalLocation.equals(TRIBUNAL_ENGLAND)
            ? TribunalOffice.ENGLANDWALES_OFFICES : TribunalOffice.SCOTLAND_OFFICES;

        return DynamicFixedListType.from(tribunalOffices.stream()
            .filter(tribunalOffice -> !tribunalOffice.getOfficeName().equals(managingOffice))
            .map(tribunalOffice ->
                DynamicValueType.create(tribunalOffice.getOfficeName(), tribunalOffice.getOfficeName()))
            .toList());
    }

    public String toAddressWithTab(Address address) {
        if (addressIsEmpty(address)) {
            return ADDRESS_NOT_ENTERED;
        }
        StringBuilder claimantAddressStr = new StringBuilder();
        claimantAddressStr.append(defaultIfEmpty(address.getAddressLine1(), ""));
        if (!isNullOrEmpty(address.getAddressLine2())) {
            claimantAddressStr.append("<br>").append(address.getAddressLine2());
        }
        if (!isNullOrEmpty(address.getAddressLine3())) {
            claimantAddressStr.append("<br>").append(address.getAddressLine3());
        }
        if (!isNullOrEmpty(address.getPostTown())) {
            claimantAddressStr.append("<br>").append(address.getPostTown());
        }
        if (!isNullOrEmpty(address.getPostCode())) {
            claimantAddressStr.append("<br>").append(address.getPostCode());
        }
        return claimantAddressStr.toString();
    }

    private void addJurCodeToExistingCollection(CaseData caseData, VettingJurisdictionCodesType code) {
        JurCodesType newCode = new JurCodesType();
        newCode.setJuridictionCodesList(code.getEt1VettingJurCodeList());
        JurCodesTypeItem codesTypeItem = new JurCodesTypeItem();
        codesTypeItem.setValue(newCode);
        codesTypeItem.setId(UUID.randomUUID().toString());
        caseData.getJurCodesCollection().add(codesTypeItem);
    }

    public DynamicFixedListType getHearingVenuesList(String office) {
        List<DynamicValueType> venueList = jpaVenueService.getVenues(TribunalOffice.valueOfOfficeName(office));
        return DynamicFixedListType.from(venueList);
    }

    public String getAddressesHtml(CaseData caseData) {
        String claimantName = buildClaimantFullName(caseData);
        String respondentName = caseData.getRespondentCollection().getFirst().getValue().getRespondentName();
        if (caseData.getClaimantWorkAddressQuestion() != null
                && caseData.getClaimantWorkAddress() != null
                && NO.equals(caseData.getClaimantWorkAddressQuestion())) {
            return String.format(CLAIMANT_AND_RESPONDENT_ADDRESSES,
                    claimantName,
                    toAddressWithTab(caseData.getClaimantType().getClaimantAddressUK()),
                    toAddressWithTab(ObjectUtils.isEmpty(caseData.getClaimantWorkAddress().getClaimantWorkAddress())
                            ? new Address()
                            : caseData.getClaimantWorkAddress().getClaimantWorkAddress()),
                    respondentName,
                    toAddressWithTab(caseData.getRespondentCollection().getFirst().getValue().getRespondentAddress()));
        } else {
            return String.format(CLAIMANT_AND_RESPONDENT_ADDRESSES_WITHOUT_WORK_ADDRESS,
                    claimantName,
                    toAddressWithTab(caseData.getClaimantType().getClaimantAddressUK()),
                    respondentName,
                    toAddressWithTab(caseData.getRespondentCollection().getFirst().getValue().getRespondentAddress())
            );
        }
    }

    private String buildClaimantFullName(CaseData caseData) {
        if (COMPANY.equals(caseData.getClaimantTypeOfClaimant())) {
            return defaultIfEmpty(caseData.getClaimantCompany(), "");
        }
        if (caseData.getClaimantIndType() == null) {
            return "";
        }
        String firstName = defaultIfEmpty(caseData.getClaimantIndType().getClaimantFirstNames(), "");
        String lastName = defaultIfEmpty(caseData.getClaimantIndType().getClaimantLastName(), "");
        return (firstName + " " + lastName).trim();
    }

    /**
     * This calls the Tornado service to generate the PDF for the ET1 Vetting journey.
     * @param caseData gets the casedata
     * @param userToken user authentication token
     * @param caseTypeId reference which casetype the document will be uploaded to
     * @return DocumentInfo, which contains the url and markup for the uploaded document
     */
    public DocumentInfo generateEt1VettingDocument(CaseData caseData, String userToken, String caseTypeId) {
        try {
            return tornadoService.generateEventDocument(caseData, userToken,
                    caseTypeId, "ET1 Vetting.pdf");
        } catch (Exception e) {
            throw new DocumentManagementException(String.format(DOCGEN_ERROR, caseData.getEthosCaseReference()), e);
        }
    }

    public void setEraFields(CaseData caseData) {
        if (!employmentRightsActService.isEraOctober2026(caseData)) {
            caseData.setEt1VettingEra(NOT_APPLICABLE);
        }
        caseData.setEt1VettingEraAssessmentMarkUp(null);
        if (ObjectUtils.isEmpty(caseData.getAdditionalCaseInfoType())) {
            caseData.setAdditionalCaseInfoType(new AdditionalCaseInfoType());
        }

        if (YES.equals(caseData.getEt1VettingEra())) {
            caseData.getAdditionalCaseInfoType().setEra(YES);
        } else if (NO.equals(caseData.getEt1VettingEra()) || NOT_APPLICABLE.equals(caseData.getEt1VettingEra())) {
            caseData.getAdditionalCaseInfoType().setEra(NO);
        }

    }
}
