package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

/**
 * ET3 vetting helper provides methods to assist with the ET3 vetting pages
 * this includes formatting markdown and querying the state of the ET3 response.
 */
@Slf4j
public class Et3VettingHelper {

    static final String NO_RESPONDENTS_FOUND_ERROR = "No respondents found for case %s";
    static final String NO_CLAIM_SERVED_DATE = "Cannot proceed as there is no claim served date";
    static final String NO_ET3_RESPONSE = "Cannot process as there is no ET3 Response";
    static final String ET3_TABLE_DATA =
        "| Dates| |\r\n"
        + "|--|--|\r\n"
        + "|ET1 served| %s|\r\n"
        + "|ET3 due| %s|\r\n"
        + "|Extension| %s|\r\n"
        + "|ET3 received| %s|";

    private static final String RESPONDENT_DETAILS = "<h2>Respondent</h2>"
        + "<pre>Name &#09&#09&#09&#09&#09&#09&nbsp; %s"
        + "<br><br>Contact address &#09&#09 %s</pre><hr>";
    private static final String BR_WITH_TAB = "<br>&#09&#09&#09&#09&#09&#09&#09&#09&#09";
    private static final int ET3_RESPONSE_WINDOW = 28;
    private static final String NONE = "None";
    private static final String NONE_GIVEN = "None Given";

    private Et3VettingHelper() {
        //Access through static methods
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
        String respondentName = isNullOrEmpty(respondentSumTypeItem.getValue().getRespondentName())
            ? NONE_GIVEN
            : respondentSumTypeItem.getValue().getRespondentName();

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
        respondentSumTypeOptional.ifPresent(
            respondentSumTypeItem -> setResponseInTime(caseData, respondentSumTypeItem.getValue()));

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
            RespondentSumType respondent = respondentSumTypeItem.getValue();
            if (respondentExistsAndEt3Received(respondentName, respondent))  {
                return UtilHelper.listingFormatLocalDate(respondent.getResponseReceivedDate());
            }
        }

        return NO;
    }

    private static boolean respondentExistsAndEt3Received(String respondentName, RespondentSumType respondent) {
        return respondentName.equals(respondent.getRespondentName())
            && YES.equals(respondent.getResponseReceived());
    }

    private static boolean respondentExtensionExists(RespondentSumType respondent) {
        return YES.equals(respondent.getExtensionRequested()) && YES.equals(respondent.getExtensionGranted());
    }

}