package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationTypeItem;

import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_ONLY;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_ONLY;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_TITLE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.PseHelper.formatOrdReqDetails;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.PseHelper.formatRespondDetails;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.PseHelper.getPartyNotifications;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.PseHelper.getSelectedNotificationWithCode;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProvideSomethingElseViewService {

    private static final String TABLE_COLUMNS_MARKDOWN =
            "| No | Subject | To party | Date sent | Notification | Response due | Number of responses |\r\n"
                    + "|:---------|:---------|:---------|:---------|:---------|:---------|:---------|\r\n"
                    + "%s\r\n";

    private static final String TABLE_ROW_MARKDOWN = "|%s|%s|%s|%s|%s|%s|%s|\r\n";

    /**
     * Create fields for application dropdown selector.
     * Only populate when
     * - SendNotificationNotify = RESPONDENT_ONLY or BOTH_PARTIES
     * @param caseData contains all the case data
     */
    public DynamicFixedListType populateSelectDropdownView(CaseData caseData, String party) {
        if (CollectionUtils.isEmpty(caseData.getSendNotificationCollection())) {
            return null;
        }

        return DynamicFixedListType.from(caseData.getSendNotificationCollection().stream()
            .filter(notification -> getPartyNotifications(notification, party))
            .map(r ->
                DynamicValueType.create(
                    r.getValue().getNumber(),
                    r.getValue().getNumber() + " - " + r.getValue().getSendNotificationTitle()
                )
            )
            .toList());
    }

    public String generateViewNotificationsMarkdown(CaseData caseData, String partySelection) {
        String filter = switch (partySelection) {
            case RESPONDENT_TITLE -> CLAIMANT_ONLY;
            case CLAIMANT_TITLE -> RESPONDENT_ONLY;
            default -> throw new IllegalArgumentException("Invalid party selection");
        };
        List<SendNotificationTypeItem> notifications = caseData.getSendNotificationCollection();
        if (CollectionUtils.isEmpty(notifications)) {
            return String.format(TABLE_COLUMNS_MARKDOWN, "");
        }

        String tableRows = notifications.stream()
                .filter(o -> !filter.equals(o.getValue().getSendNotificationNotify()))
                .map(this::viewNotificationsFormatRow)
                .collect(Collectors.joining());

        return String.format(TABLE_COLUMNS_MARKDOWN, tableRows);
    }

    private String viewNotificationsFormatRow(SendNotificationTypeItem sendNotificationTypeItem) {
        SendNotificationType notification = sendNotificationTypeItem.getValue();

        return String.format(TABLE_ROW_MARKDOWN,
                notification.getNumber(),
                String.join(", ", notification.getSendNotificationSubject()),
                notification.getSendNotificationNotify(),
                notification.getDate(),
                notification.getSendNotificationTitle(),
                defaultIfEmpty(notification.getSendNotificationResponseTribunal(), "No"),
                "0"
        );
    }

    /**
     * Initial Application and Respond details table.
     * @param caseData contains all the case data
     */
    public String initialOrdReqDetailsTableMarkUp(CaseData caseData, String party) {
        SendNotificationTypeItem sendNotificationTypeItem;
        switch (party) {
            case RESPONDENT_TITLE -> {
                if (caseData.getPseRespondentSelectJudgmentOrderNotification() == null) {
                    return "";
                }
                sendNotificationTypeItem = getSelectedNotificationWithCode(caseData,
                        caseData.getPseRespondentSelectJudgmentOrderNotification().getSelectedCode());
            }
            case CLAIMANT_TITLE -> {
                if (caseData.getClaimantSelectNotification() == null) {
                    return "";
                }
                sendNotificationTypeItem = getSelectedNotificationWithCode(caseData,
                        caseData.getClaimantSelectNotification().getSelectedCode());
            }
            default -> throw new IllegalArgumentException("Invalid party selection");
        }

        if (sendNotificationTypeItem == null) {
            return "";
        }
        SendNotificationType sendNotificationType = sendNotificationTypeItem.getValue();
        return formatOrdReqDetails(sendNotificationType)
            + formatRespondDetails(sendNotificationType);
    }

}
