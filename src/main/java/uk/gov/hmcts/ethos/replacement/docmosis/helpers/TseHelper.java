package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.TseRespondentReplyTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.TseRespondentReplyType;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NEW_DATE_PATTERN;

@Slf4j
public class TseHelper {
    public static final String INTRO = "The respondent has applied to <b>%s</b>.</br>%s</br> If you have any "
        + "objections or responses to their application you must send them to the tribunal as soon as possible and by "
        + "%s at the latest.</br></br>If you need more time to respond, you may request more time from the tribunal. If"
        + " you do not respond or request more time to respond, the tribunal will consider the application without your"
        + " response.";
    public static final String TABLE = "| | |\r\n"
        + "|--|--|\r\n"
        + "|Application date | %s\r\n"
        + "|Details of the application | %s\r\n"
        + "Application file upload | %s";
    public static final String GROUP_B = "You do not need to respond to this application.<br>";
    public static final List<String> GROUP_B_TYPES = List.of("Change my personal details", "Consider a decision "
        + "afresh", "Reconsider a judgment", "Withdraw my claim");
    public static final String OPEN = "Open";
    public static final String CLOSED = "Closed";

    private TseHelper() {
        // Access through static methods
    }

    /**
     * Create fields for application dropdown selector.
     * @param caseData contains all the case data
     */
    public static DynamicFixedListType populateSelectApplicationDropdown(CaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getGenericTseApplicationCollection())) {
            return null;
        }

        return DynamicFixedListType.from(caseData.getGenericTseApplicationCollection().stream()
            .filter(r -> r.getValue().getRespondentReply() == null
                && r.getValue().getStatus() != null
                && !r.getValue().getStatus().equals(CLOSED)
            ).map(r -> DynamicValueType.create(
                r.getValue().getNumber(),
                r.getValue().getNumber() + " " + r.getValue().getType())
            ).collect(Collectors.toList()));
    }

    /**
     * Sets the data for the second page of the response journey.
     * @param caseData contains all the case data
     */
    public static void setDataForRespondingToApplication(CaseData caseData) {
        List<GenericTseApplicationTypeItem> applications = caseData.getGenericTseApplicationCollection();
        if (CollectionUtils.isEmpty(applications)) {
            return;
        }

        GenericTseApplicationType genericTseApplicationType = getSelectedApplication(caseData);

        LocalDate date = LocalDate.parse(genericTseApplicationType.getDate(), NEW_DATE_PATTERN);

        caseData.setTseResponseIntro(
            String.format(
                INTRO,
                genericTseApplicationType.getType(),
                GROUP_B_TYPES.contains(genericTseApplicationType.getType()) ? GROUP_B : "",
                UtilHelper.formatCurrentDatePlusDays(date, 7)
            )
        );

        String document = "N/A";

        if (genericTseApplicationType.getDocumentUpload() != null) {
            Pattern pattern = Pattern.compile("^.+?/documents/");
            Matcher matcher = pattern.matcher(genericTseApplicationType.getDocumentUpload().getDocumentBinaryUrl());
            String documentLink = matcher.replaceFirst("");
            String documentName = genericTseApplicationType.getDocumentUpload().getDocumentFilename();
            document = String.format("<a href=\"/documents/%s\" target=\"_blank\">%s</a>", documentLink, documentName);
        }

        caseData.setTseResponseTable(
            String.format(
                TABLE,
                genericTseApplicationType.getDate(),
                isNullOrEmpty(genericTseApplicationType.getDetails()) ? "N/A" : genericTseApplicationType.getDetails(),
                document
            )
        );

        if (genericTseApplicationType.getRespondentReply() == null) {
            return;
        }

        TseRespondentReplyType respondentReply = genericTseApplicationType.getRespondentReply().get(0).getValue();
        if (respondentReply == null) {
            return;
        }

        caseData.setTseResponseHasSupportingMaterial(respondentReply.getHasSupportingMaterial());
        caseData.setTseResponseText(respondentReply.getResponse());
        caseData.setTseResponseSupportingMaterial(respondentReply.getSupportingMaterial());
        caseData.setTseResponseCopyToOtherParty(respondentReply.getCopyToOtherParty());
        caseData.setTseResponseCopyNoGiveDetails(respondentReply.getCopyNoGiveDetails());
    }

    /**
     * Saves the data on the reply page onto the application object.
     * @param caseData contains all the case data
     */
    public static void saveReplyToApplication(CaseData caseData) {
        List<GenericTseApplicationTypeItem> applications = caseData.getGenericTseApplicationCollection();
        if (CollectionUtils.isEmpty(applications)) {
            return;
        }

        TseRespondentReplyType tseRespondentReplyType = new TseRespondentReplyType();
        tseRespondentReplyType.setResponse(caseData.getTseResponseText());
        tseRespondentReplyType.setSupportingMaterial(caseData.getTseResponseSupportingMaterial());
        tseRespondentReplyType.setHasSupportingMaterial(caseData.getTseResponseHasSupportingMaterial());
        tseRespondentReplyType.setFrom("Respondent");
        tseRespondentReplyType.setDate(UtilHelper.formatCurrentDate(LocalDate.now()));
        tseRespondentReplyType.setCopyToOtherParty(caseData.getTseResponseCopyToOtherParty());
        tseRespondentReplyType.setCopyNoGiveDetails(caseData.getTseResponseCopyNoGiveDetails());
        TseRespondentReplyTypeItem tseRespondentReplyTypeItem = new TseRespondentReplyTypeItem();
        tseRespondentReplyTypeItem.setId(UUID.randomUUID().toString());
        tseRespondentReplyTypeItem.setValue(tseRespondentReplyType);
        GenericTseApplicationType genericTseApplicationType = getSelectedApplication(caseData);
        genericTseApplicationType.setRespondentReply(List.of(tseRespondentReplyTypeItem));
        // TODO: This will need changing when we support admin decisions
        genericTseApplicationType.setResponsesCount("1");
    }

    /**
     * Clears fields that are used when responding to an application.
     * @param caseData contains all the case data
     */
    public static void resetReplyToApplicationPage(CaseData caseData) {
        caseData.setTseResponseText(null);
        caseData.setTseResponseIntro(null);
        caseData.setTseResponseTable(null);
        caseData.setTseResponseHasSupportingMaterial(null);
        caseData.setTseResponseSupportingMaterial(null);
        caseData.setTseResponseCopyToOtherParty(null);
        caseData.setTseResponseCopyNoGiveDetails(null);
    }

    private static GenericTseApplicationType getSelectedApplication(CaseData caseData) {
        return caseData.getGenericTseApplicationCollection()
            .get(Integer.parseInt(caseData.getTseRespondSelectApplication().getValue().getCode()) - 1).getValue();
    }
}
