package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.PseResponseTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.PseResponseType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationTypeItem;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.IntWrapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.BOTH_PARTIES;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_ONLY;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.PseHelper.formatLegalRepReply;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.PseHelper.formatOrdReqDetails;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.PseHelper.getSelectedSendNotificationTypeItem;

@Slf4j
@Service
@RequiredArgsConstructor
public class PseRespondToTribunalService {

    private static final String GIVE_MISSING_DETAIL =
        "Use the text box or supporting materials to give details.";

    private static final String SUBMITTED_BODY = "### What happens next\r\n\r\n"
        + "%s"
        + "The tribunal will consider all correspondence and let you know what happens next.";

    private static final String RULE92_ANSWERED_YES =
        "You have responded to the tribunal and copied your response to the other party.\r\n\r\n";

    /**
     * Create fields for application dropdown selector.
     * @param caseData contains all the case data
     */
    public DynamicFixedListType populateSelectDropdown(CaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getSendNotificationCollection())) {
            return null;
        }

        return DynamicFixedListType.from(caseData.getSendNotificationCollection().stream()
            .filter(r -> isNotifyRespondent(r)
                && isNoRespondentReply(r.getValue().getRespondCollection()))
            .map(r ->
                DynamicValueType.create(
                    r.getValue().getNumber(),
                    r.getValue().getNumber() + " " + r.getValue().getSendNotificationTitle()
                )
            )
            .collect(Collectors.toList()));
    }

    private boolean isNotifyRespondent(SendNotificationTypeItem sendNotificationTypeItem) {
        return RESPONDENT_ONLY.equals(sendNotificationTypeItem.getValue().getSendNotificationNotify())
            || BOTH_PARTIES.equals(sendNotificationTypeItem.getValue().getSendNotificationNotify());
    }

    private boolean isNoRespondentReply(List<PseResponseTypeItem> pseResponseTypeItems) {
        return CollectionUtils.isEmpty(pseResponseTypeItems)
            || pseResponseTypeItems.stream().noneMatch(r -> RESPONDENT_TITLE.equals(r.getValue().getFrom()));
    }

    /**
     * Initial Application and Respond details table.
     * @param caseData contains all the case data
     */
    public String initialOrdReqDetailsTableMarkUp(CaseData caseData) {
        SendNotificationType sendNotificationType =
            getSelectedSendNotificationTypeItem(caseData).getValue();
        return formatOrdReqDetails(sendNotificationType)
            + initialRespondDetails(sendNotificationType);
    }

    private String initialRespondDetails(SendNotificationType sendNotificationType) {
        if (CollectionUtils.isEmpty(sendNotificationType.getRespondCollection())) {
            return "";
        }
        IntWrapper respondCount = new IntWrapper(0);
        return sendNotificationType.getRespondCollection().stream()
            .map(r -> formatLegalRepReply(r.getValue(), respondCount.incrementAndReturnValue()))
            .collect(Collectors.joining(""));
    }

    /**
     * Validate user input.
     * @param caseData contains all the case data
     * @return Error Message List
     */
    public List<String> validateInput(CaseData caseData) {
        List<String> errors = new ArrayList<>();
        if (StringUtils.isEmpty(caseData.getPseRespondentOrdReqResponseText())
            && (StringUtils.isEmpty(caseData.getPseRespondentOrdReqHasSupportingMaterial())
            || NO.equals(caseData.getPseRespondentOrdReqHasSupportingMaterial()))) {
            errors.add(GIVE_MISSING_DETAIL);
        }
        return errors;
    }

    /**
     * Create a new element in the responses list and assign the PSE data from CaseData to it.
     * @param caseData contains all the case data
     */
    public void addRespondentResponseToJON(CaseData caseData) {
        SendNotificationType sendNotificationType = getSelectedSendNotificationTypeItem(caseData).getValue();
        if (CollectionUtils.isEmpty(sendNotificationType.getRespondCollection())) {
            sendNotificationType.setRespondCollection(new ArrayList<>());
        }

        sendNotificationType.getRespondCollection().add(
            PseResponseTypeItem.builder()
                .id(UUID.randomUUID().toString())
                .value(
                    PseResponseType.builder()
                        .from(RESPONDENT_TITLE)
                        .date(UtilHelper.formatCurrentDate(LocalDate.now()))
                        .response(caseData.getPseRespondentOrdReqResponseText())
                        .hasSupportingMaterial(caseData.getPseRespondentOrdReqHasSupportingMaterial())
                        .supportingMaterial(caseData.getPseRespondentOrdReqUploadDocument())
                        .copyToOtherParty(caseData.getPseRespondentOrdReqCopyToOtherParty())
                        .copyNoGiveDetails(caseData.getPseRespondentOrdReqCopyNoGiveDetails())
                        .build()
                ).build());
    }

    /**
     * Clears fields that are used when responding to a JON, so they can be used in subsequent responses to JONs.
     * @param caseData contains all the case data
     */
    public void clearRespondentResponse(CaseData caseData) {
        caseData.setPseRespondentOrdReqTableMarkUp(null);
        caseData.setPseRespondentOrdReqResponseText(null);
        caseData.setPseRespondentOrdReqHasSupportingMaterial(null);
        caseData.setPseRespondentOrdReqUploadDocument(null);
        caseData.setPseRespondentOrdReqCopyToOtherParty(null);
        caseData.setPseRespondentOrdReqCopyNoGiveDetails(null);
    }

    /**
     * Generate Submitted Body String.
     * @param caseData contains all the case data
     * @return Submitted Body String
     */
    public String getSubmittedBody(CaseData caseData) {
        SendNotificationType sendNotificationType = getSelectedSendNotificationTypeItem(caseData).getValue();
        if (sendNotificationType == null || CollectionUtils.isEmpty(sendNotificationType.getRespondCollection())) {
            return SUBMITTED_BODY;
        }

        List<PseResponseTypeItem> respondCollection = sendNotificationType.getRespondCollection();

        PseResponseType response = respondCollection.get(respondCollection.size() - 1).getValue();
        return String.format(SUBMITTED_BODY, YES.equals(response.getCopyToOtherParty()) ? RULE92_ANSWERED_YES : "");
    }

}
