package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.Nullable;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.documents.Et3VettingData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.documents.Et3VettingDocument;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_LISTED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.OLD_DATE_TIME_PATTERN;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

/**
 * ET3 vetting helper provides methods to assist with the ET3 vetting pages
 * this includes formatting markdown and querying the state of the ET3 response.
 * This also includes part of the document generation for the ET3 Vetting process. It creates the data needed by
 * Docmosis in order to generate a document.
 */
@Slf4j
@SuppressWarnings({"PMD.TooManyMethods", "PMD.TooManyFields", "PMD.AvoidDuplicateLiterals",
    "PMD.UnnecessaryAnnotationValueElement", "PMD.ExcessivePublicCount", "PMD.ExcessiveClassLength",
    "PMD.GodClass", "PMD.ConfusingTernary", "PDM.CyclomaticComplexity",
    "PMD.ClassWithOnlyPrivateConstructorsShouldBeFinal", "PMD.ClassNamingConventions",
    "PMD.AvoidInstantiatingObjectsInLoops", "PMD.CognitiveComplexity", "PMD.PrematureDeclaration",
    "PMD.LinguisticNaming", "PMD.InsufficientStringBufferDeclaration", "PMD.ConsecutiveLiteralAppends",
    "PMD.LiteralsFirstInComparisons", "PMD.UnnecessaryFullyQualifiedName", "PMD.LawOfDemeter"})
public class Et3VettingHelper {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String TEMPLATE_NAME = "EM-TRB-EGW-ENG-01145.docx";
    private static final String OUTPUT_NAME = "ET3 Processing.pdf";

    static final String NO_RESPONDENTS_FOUND_ERROR = "No respondents found for case %s";
    static final String NO_CLAIM_SERVED_DATE = "Cannot proceed as there is no claim served date";
    static final String NO_ET3_RESPONSE = "Cannot process as there is no ET3 Response";
    static final String ET3_TABLE_DATA =
        "| <h2>Dates</h2>| |\r\n"
        + "|--|--|\r\n"
        + "|ET1 served| %s|\r\n"
        + "|ET3 due| %s|\r\n"
        + "|Extension| %s|\r\n"
        + "|ET3 received| %s|";

    private static final String ET3_HEARING_TABLE =
        "| <h2>Hearing details</h2>| | \r\n"
        + "|--|--|\r\n"
        + "|Date| %s|\r\n"
        + "|Hearing Type| %s|\r\n"
        + "|Hearing Length| %s|\r\n"
        + "|Hearing Format| %s|\r\n"
        + "|Sit Alone/Full Panel| %s|\r\n"
        + "|Track| %s|";

    private static final String ET3_TRIBUNAL_TABLE =
        "| <h2>Tribunal location</h2>| | \r\n"
            + "|--|--|\r\n"
            + "|Tribunal| %s|\r\n"
            + "|Office| %s|";
    private static final String RESPONDENT_DETAILS = "<h2>Respondent</h2>"
        + "<pre>Name &#09&#09&#09&#09&#09&#09&nbsp; %s"
        + "<br><br>Contact address &#09&#09 %s</pre><hr>";
    private static final String BR_WITH_TAB = "<br>&#09&#09&#09&#09&#09&#09&#09&#09&#09";
    private static final int ET3_RESPONSE_WINDOW = 28;
    private static final String NONE = "None";
    private static final String NONE_GIVEN = "None Given";
    private static final String CASE_NOT_LISTED = "<h2>Hearing details</h2>The case has not been listed<hr>";

    private Et3VettingHelper() {
        //Access through static methods
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    /**
     * This method will create and populate the dynamicList needed for the user to choose the respondent. It
     * initially checks to see if the collection is empty and returns an error to the user to let them know. If it is
     * not empty, the code continues and calls upon an existing helper class which creates the list using the
     * respondent collection
     * @param caseData contains all the case data
     * @return will either return a list which contains an error message if no respondents were found or will return an
     *         empty list showing that there were no errors
     */
    public static List<String> populateRespondentDynamicList(CaseData caseData) {
        List<RespondentSumTypeItem> respondentCollection = caseData.getRespondentCollection();
        if (CollectionUtils.isEmpty(respondentCollection)) {
            return Arrays.asList(String.format(NO_RESPONDENTS_FOUND_ERROR, caseData.getEthosCaseReference()));
        }

        List<DynamicValueType> dynamicRespondentList =
            DynamicListHelper.createDynamicRespondentName(respondentCollection);
        DynamicFixedListType dynamicFixedListType = new DynamicFixedListType();
        dynamicFixedListType.setListItems(dynamicRespondentList);
        caseData.setEt3ChooseRespondent(dynamicFixedListType);
        return new ArrayList<>();
    }

    /**
     * Sets the "Et3NameAddressRespondent" property on CaseData to the HTML representation of the respondent's name
     * and address in table form. Will log an error if no respondents are found and use "None Given" if name and/or
     * address data are unavailable
     * @param caseData contains all the case data
     */
    public static void getRespondentNameAndAddress(CaseData caseData) {
        String respondentName = caseData.getEt3ChooseRespondent().getSelectedLabel();
        List<RespondentSumTypeItem> respondentCollection = caseData.getRespondentCollection();

        if (CollectionUtils.isEmpty(respondentCollection))  {
            log.error(String.format(NO_RESPONDENTS_FOUND_ERROR, caseData.getEthosCaseReference()));
            return;
        }

        Optional<RespondentSumTypeItem> respondentSumTypeOptional = respondentCollection
            .stream()
            .filter(r -> respondentExistsAndEt3Received(respondentName, r.getValue()))
            .findFirst();

        respondentSumTypeOptional.ifPresent(
            respondentSumTypeItem -> setRespondentNameAddress(caseData, respondentSumTypeItem));
    }

    private static void setRespondentNameAddress(CaseData caseData, RespondentSumTypeItem respondentSumTypeItem) {
        String respondentName = isNullOrEmpty(respondentSumTypeItem.getValue().getResponseRespondentName())
            ? NONE_GIVEN
            : respondentSumTypeItem.getValue().getResponseRespondentName();

        Address address = respondentSumTypeItem.getValue().getResponseRespondentAddress();

        String respondentAddress = address == null || isNullOrEmpty(address.toString())
            ? NONE_GIVEN
            : address.toAddressHtml();

        caseData.setEt3NameAddressRespondent(
            String.format(
                RESPONDENT_DETAILS,
                respondentName,
                address == null ? NONE_GIVEN : formatAddressToHtml(address)
            )
        );

        caseData.setEt3DoWeHaveRespondentsName(respondentName.equals(NONE_GIVEN)
            ? NO
            : YES);

        caseData.setEt3DoWeHaveRespondentsAddress(respondentAddress.equals(NONE_GIVEN)
            ? NO
            : YES);
    }

    /**
     * Formats an Address into HTML for ExUI to display. It's expected that an address will always have a Line 1,
     * Post Town and Postcode.
     * @param address Address object to format
     * @return String representing address in HTML form.
     */
    public static String formatAddressToHtml(Address address) {
        StringBuilder addressBuilder = new StringBuilder();
        addressBuilder.append(address.getAddressLine1());
        if (!Strings.isNullOrEmpty(address.getAddressLine2())) {
            addressBuilder.append(BR_WITH_TAB).append(address.getAddressLine2());
        }
        if (!Strings.isNullOrEmpty(address.getAddressLine3())) {
            addressBuilder.append(BR_WITH_TAB).append(address.getAddressLine3());
        }
        addressBuilder.append(BR_WITH_TAB).append(address.getPostTown())
            .append(BR_WITH_TAB).append(address.getPostCode());
        return addressBuilder.toString();
    }

    /**
     * Formats the case data into the table that is rendered on the "Is there an ET3 Response?" page.
     * @param caseData The case data containing the ET3 response
     * @return A string containing markdown for a table, will change content depending on if/when the ET3 response
     *         has been submitted
     * */
    public static String getEt3DatesInMarkdown(CaseData caseData) {
        return String.format(
            ET3_TABLE_DATA,
            findEt1ServedDate(caseData.getClaimServedDate()),
            findEt3DueDate(caseData.getClaimServedDate()),
            findEt3ExtensionDate(caseData),
            findEt3ReceivedDate(caseData)
        );
    }

    /**
     * Check if an ET3 response has been submitted on a given case data.
     * @param caseData The case data to check
     * @return True if ET3 submitted, False if not submitted or if the respondent collection is empty
     */
    public static boolean isThereAnEt3Response(CaseData caseData) {
        List<RespondentSumTypeItem> respondentCollection = caseData.getRespondentCollection();

        if (CollectionUtils.isEmpty(respondentCollection)) {
            log.error(String.format(NO_RESPONDENTS_FOUND_ERROR, caseData.getEthosCaseReference()));

            return false;
        }

        return respondentCollection
            .stream()
            .anyMatch(r -> respondentExistsAndEt3Received(
                caseData.getEt3ChooseRespondent().getSelectedLabel(), r.getValue()));

    }

    /**
     * This method is used to calculate whether the response was received on time. It takes the claimServedDate and adds
     * a set number of days to calculate the ET3 due date. It then looks at the respondent to see when the response was
     * received as well as checking to see if an extension was request and granted. If they were granted an extension,
     * a new due date is applied from the extension date
     * @param caseData this contains data from the case
     */
    public static List<String> calculateResponseTime(CaseData caseData) {
        List<String> errors = new ArrayList<>();

        if (!responseInTimePreValidationCheck(caseData, errors)) {
            return errors;
        }

        List<RespondentSumTypeItem> respondentCollection = caseData.getRespondentCollection();

        Optional<RespondentSumTypeItem> respondentSumTypeOptional = respondentCollection
            .stream()
            .filter(r -> respondentExistsAndEt3Received(
                caseData.getEt3ChooseRespondent().getSelectedLabel(), r.getValue()))
            .findFirst();

        respondentSumTypeOptional.ifPresent(respondent -> setResponseInTime(caseData, respondent.getValue()));

        return errors;
    }

    private static void setResponseInTime(CaseData caseData, RespondentSumType respondentSumType) {
        LocalDate et3DueDate = LocalDate.parse(caseData.getClaimServedDate()).plusDays(ET3_RESPONSE_WINDOW);
        LocalDate et3ReceivedDate = LocalDate.parse(respondentSumType.getResponseReceivedDate());
        if (respondentExtensionExists(respondentSumType)) {
            et3DueDate = LocalDate.parse(respondentSumType.getExtensionDate());
        }

        if (et3ReceivedDate.isAfter(et3DueDate)) {
            caseData.setEt3ResponseInTime(NO);
        } else {
            caseData.setEt3ResponseInTime(YES);
        }
    }

    private static boolean responseInTimePreValidationCheck(CaseData caseData, List<String> errors) {
        if (isNullOrEmpty(caseData.getClaimServedDate())) {
            errors.add(NO_CLAIM_SERVED_DATE);
        }
        if (NO.equals(findEt3ReceivedDate(caseData))) {
            errors.add(NO_ET3_RESPONSE);
        }
        return errors.isEmpty();
    }

    private static String findEt3DueDate(String et3DueDate) {
        return isNullOrEmpty(et3DueDate)
            ? "Cannot find ET3 Due Date"
            : UtilHelper.formatCurrentDatePlusDays(LocalDate.parse(et3DueDate), ET3_RESPONSE_WINDOW);
    }

    private static String findEt1ServedDate(String date) {
        return isNullOrEmpty(date)
            ? "Cannot find ET1 Served Date"
            : UtilHelper.listingFormatLocalDate(date);
    }

    private static String findEt3ExtensionDate(CaseData caseData) {
        List<RespondentSumTypeItem> respondentCollection = caseData.getRespondentCollection();

        if (CollectionUtils.isEmpty(respondentCollection)) {
            log.error(String.format(NO_RESPONDENTS_FOUND_ERROR, caseData.getEthosCaseReference()));
            return NONE;
        }

        String respondentName = caseData.getEt3ChooseRespondent().getSelectedLabel();
        for (RespondentSumTypeItem respondentSumTypeItem : respondentCollection) {
            RespondentSumType respondent = respondentSumTypeItem.getValue();
            if (respondentName.equals(respondent.getRespondentName())
                && respondentExtensionExists(respondent))  {
                return UtilHelper.listingFormatLocalDate(respondent.getExtensionDate());
            }
        }

        return NONE;
    }

    private static String findEt3ReceivedDate(CaseData caseData) {
        List<RespondentSumTypeItem> respondentCollection = caseData.getRespondentCollection();

        if (CollectionUtils.isEmpty(respondentCollection)) {
            log.error(String.format(NO_RESPONDENTS_FOUND_ERROR, caseData.getEthosCaseReference()));
            return NO;
        }

        String respondentName = caseData.getEt3ChooseRespondent().getSelectedLabel();
        for (RespondentSumTypeItem respondentSumTypeItem : respondentCollection) {
            String date = getEt3ReceivedDateForRespondent(respondentName, respondentSumTypeItem);
            if (date != null) {
                return date;
            }
        }

        return NO;
    }

    @Nullable
    private static String getEt3ReceivedDateForRespondent(String respondentName, RespondentSumTypeItem respondent) {
        if (respondentExistsAndEt3Received(respondentName, respondent.getValue()))  {
            String date = UtilHelper.listingFormatLocalDate(respondent.getValue().getResponseReceivedDate());
            if (!isNullOrEmpty(date)) {
                return date;
            }
        }
        return null;
    }

    private static boolean respondentExistsAndEt3Received(String respondentName, RespondentSumType respondent) {
        return respondentName.equals(respondent.getRespondentName())
            && YES.equals(respondent.getResponseReceived());
    }

    private static boolean respondentExtensionExists(RespondentSumType respondent) {
        return YES.equals(respondent.getExtensionRequested()) && YES.equals(respondent.getExtensionGranted());
    }

    /**
     * Finds listed hearings for a case and sets the hearing details for ExUI. Will display a table with the earliest
     * hearing date and track type or static text saying that there are no listings for the case.
     * @param caseData data for the current case
     */
    public static void setHearingListedForExUi(CaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getHearingCollection())) {
            log.info(String.format("No hearings for case %s", caseData.getEthosCaseReference()));
            caseData.setEt3HearingDetails(CASE_NOT_LISTED);
            caseData.setEt3IsCaseListedForHearing(NO);
            return;
        }

        String hearingDate = findHearingDate(caseData.getHearingCollection());

        if (CASE_NOT_LISTED.equals(hearingDate)) {
            caseData.setEt3HearingDetails(CASE_NOT_LISTED);
            caseData.setEt3IsCaseListedForHearing(NO);
            return;
        }

        String track = isNullOrEmpty(caseData.getConciliationTrack())
            ? "Track could not be found"
            : caseData.getConciliationTrack();

        HearingType hearing = HearingsHelper.findHearingByListedDate(caseData, hearingDate);

        caseData.setEt3HearingDetails(String.format(
                ET3_HEARING_TABLE,
                LocalDateTime.parse(hearingDate, OLD_DATE_TIME_PATTERN)
                        .format(DateTimeFormatter.ofPattern("EEEE d MMMM y")),
                hearing.getHearingType(),
                hearing.getHearingEstLengthNum() + " " + hearing.getHearingEstLengthNumType(),
                String.join(", ", hearing.getHearingFormat()),
                hearing.getHearingSitAlone(),
                track));
        caseData.setEt3IsCaseListedForHearing(YES);
    }

    private static String findHearingDate(List<HearingTypeItem> hearingCollection) {
        List<String> hearingDates = new ArrayList<>();
        hearingCollection.forEach(item -> addListedDates(hearingDates, item.getValue().getHearingDateCollection()));
        if (CollectionUtils.isEmpty(hearingDates)) {
            return CASE_NOT_LISTED;
        }
        return hearingDates.get(0);
    }

    private static void addListedDates(List<String> hearingDates, List<DateListedTypeItem> hearingCollection) {
        hearingCollection.stream()
            .filter(item -> HEARING_STATUS_LISTED.equals(item.getValue().getHearingStatus()))
            .forEach(item -> hearingDates.add(item.getValue().getListedDate()));
        Collections.sort(hearingDates);
    }

    /**
     * Creates a table for ExUI representing the case's current tribunal and office.
     * @param caseData data for the current case
     */
    public static void transferApplication(CaseData caseData) {
        String managingOffice = caseData.getManagingOffice();
        String tribunalOffice = TribunalOffice.isEnglandWalesOffice(managingOffice)
            ? "England & Wales"
            : TribunalOffice.SCOTLAND.getOfficeName();

        caseData.setEt3TribunalLocation(String.format(ET3_TRIBUNAL_TABLE, tribunalOffice, managingOffice));
    }

    /**
     * This method generates the request which will be sent to Docmosis to generate the document.
     * @param caseData where the data is stored
     * @param userToken token to access tornado
     * @return a string which contains a JSON payload which contains the data needed to generate the document
     * @throws JsonProcessingException if the JSON cannot be correctly generated
     */
    public static String getDocumentRequest(CaseData caseData, String userToken) throws JsonProcessingException {
        Et3VettingData et3VettingData = Et3VettingData.builder()
                .ethosCaseReference(caseData.getEthosCaseReference())
                .et3IsThereAnEt3Response(defaultIfEmpty(caseData.getEt3IsThereAnEt3Response(), null))
                .et3NoEt3Response(defaultIfEmpty(caseData.getEt3NoEt3Response(), null))
                .et3GeneralNotes(defaultIfEmpty(caseData.getEt3GeneralNotes(), null))
                .et3IsThereACompaniesHouseSearchDocument(
                        defaultIfEmpty(caseData.getEt3IsThereACompaniesHouseSearchDocument(), null))
                .et3GeneralNotesCompanyHouse(defaultIfEmpty(caseData.getEt3GeneralNotesCompanyHouse(), null))
                .et3IsThereAnIndividualSearchDocument(
                        defaultIfEmpty(caseData.getEt3IsThereAnIndividualSearchDocument(), null))
                .et3GeneralNotesIndividualInsolvency(
                        defaultIfEmpty(caseData.getEt3GeneralNotesIndividualInsolvency(), null))
                .et3LegalIssue(defaultIfEmpty(caseData.getEt3LegalIssue(), null))
                .et3LegalIssueGiveDetails(defaultIfEmpty(caseData.getEt3LegalIssueGiveDetails(), null))
                .et3GeneralNotesLegalEntity(defaultIfEmpty(caseData.getEt3GeneralNotesLegalEntity(), null))
                .et3ResponseInTime(defaultIfEmpty(caseData.getEt3ResponseInTime(), null))
                .et3ResponseInTimeDetails(defaultIfEmpty(caseData.getEt3ResponseInTimeDetails(), null))
                .et3DoWeHaveRespondentsName(defaultIfEmpty(caseData.getEt3DoWeHaveRespondentsName(), null))
                .et3GeneralNotesRespondentName(defaultIfEmpty(caseData.getEt3GeneralNotesRespondentName(), null))
                .et3DoesRespondentsNameMatch(defaultIfEmpty(caseData.getEt3DoesRespondentsNameMatch(), null))
                .et3RespondentNameMismatchDetails(defaultIfEmpty(caseData.getEt3RespondentNameMismatchDetails(), null))
                .et3GeneralNotesRespondentNameMatch(
                        defaultIfEmpty(caseData.getEt3GeneralNotesRespondentNameMatch(), null))
                .et3DoWeHaveRespondentsAddress(defaultIfEmpty(caseData.getEt3DoWeHaveRespondentsAddress(), null))
                .et3DoesRespondentsAddressMatch(defaultIfEmpty(caseData.getEt3DoesRespondentsAddressMatch(), null))
                .et3RespondentAddressMismatchDetails(
                        defaultIfEmpty(caseData.getEt3RespondentAddressMismatchDetails(), null))
                .et3GeneralNotesRespondentAddress(defaultIfEmpty(caseData.getEt3GeneralNotesRespondentAddress(), null))
                .et3GeneralNotesAddressMatch(defaultIfEmpty(caseData.getEt3GeneralNotesAddressMatch(), null))
                .et3IsCaseListedForHearing(defaultIfEmpty(caseData.getEt3IsCaseListedForHearing(), null))
                .et3IsCaseListedForHearingDetails(defaultIfEmpty(caseData.getEt3IsCaseListedForHearingDetails(), null))
                .et3GeneralNotesCaseListed(defaultIfEmpty(caseData.getEt3GeneralNotesCaseListed(), null))
                .et3IsThisLocationCorrect(defaultIfEmpty(caseData.getEt3IsThisLocationCorrect(), null))
                .et3GeneralNotesTransferApplication(
                        defaultIfEmpty(caseData.getEt3GeneralNotesTransferApplication(), null))
                .et3RegionalOffice(defaultIfEmpty(caseData.getEt3RegionalOffice(), null))
                .et3WhyWeShouldChangeTheOffice(defaultIfEmpty(caseData.getEt3WhyWeShouldChangeTheOffice(), null))
                .et3ContestClaim(defaultIfEmpty(caseData.getEt3ContestClaim(), null))
                .et3ContestClaimGiveDetails(defaultIfEmpty(caseData.getEt3ContestClaimGiveDetails(), null))
                .et3GeneralNotesContestClaim(defaultIfEmpty(caseData.getEt3GeneralNotesContestClaim(), null))
                .et3ContractClaimSection7(defaultIfEmpty(caseData.getEt3ContractClaimSection7(), null))
                .et3ContractClaimSection7Details(defaultIfEmpty(caseData.getEt3ContractClaimSection7Details(), null))
                .et3GeneralNotesContractClaimSection7(
                        defaultIfEmpty(caseData.getEt3GeneralNotesContractClaimSection7(), null))
                .et3Rule26(defaultIfEmpty(caseData.getEt3Rule26(), null))
                .et3Rule26Details(defaultIfEmpty(caseData.getEt3Rule26Details(), null))
                .et3SuggestedIssuesStrikeOut(defaultIfEmpty(caseData.getEt3SuggestedIssuesStrikeOut(), null))
                .et3SuggestedIssueInterpreters(defaultIfEmpty(caseData.getEt3SuggestedIssueInterpreters(), null))
                .et3SuggestedIssueJurisdictional(defaultIfEmpty(caseData.getEt3SuggestedIssueJurisdictional(), null))
                .et3SuggestedIssueAdjustments(defaultIfEmpty(caseData.getEt3SuggestedIssueAdjustments(), null))
                .et3SuggestedIssueRule50(defaultIfEmpty(caseData.getEt3SuggestedIssueRule50(), null))
                .et3SuggestedIssueTimePoints(defaultIfEmpty(caseData.getEt3SuggestedIssueTimePoints(), null))
                .et3GeneralNotesRule26(defaultIfEmpty(caseData.getEt3GeneralNotesRule26(), null))
                .et3AdditionalInformation(defaultIfEmpty(caseData.getEt3AdditionalInformation(), null))
                .build();

        Et3VettingDocument et3VettingDocument = Et3VettingDocument.builder()
                .accessKey(userToken)
                .outputName(OUTPUT_NAME)
                .templateName(TEMPLATE_NAME)
                .et3VettingData(et3VettingData)
                .build();

        return OBJECT_MAPPER.writeValueAsString(et3VettingDocument);

    }

}