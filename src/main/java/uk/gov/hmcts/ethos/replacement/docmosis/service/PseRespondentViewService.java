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

import static org.apache.commons.lang3.StringUtils.defaultString;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.BOTH_PARTIES;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_ONLY;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_ONLY;

@Slf4j
@Service
@RequiredArgsConstructor
public class PseRespondentViewService {

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
    public DynamicFixedListType populateSelectDropdownView(CaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getSendNotificationCollection())) {
            return null;
        }

        return DynamicFixedListType.from(caseData.getSendNotificationCollection().stream()
            .filter(this::isNotifyRespondent)
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

    public String generateViewNotificationsMarkdown(CaseData caseData) {
        List<SendNotificationTypeItem> notifications = caseData.getSendNotificationCollection();
        if (CollectionUtils.isEmpty(notifications)) {
            return String.format(TABLE_COLUMNS_MARKDOWN, "");
        }

        String tableRows = notifications.stream()
                .filter(o -> !CLAIMANT_ONLY.equals(o.getValue().getSendNotificationNotify()))
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
                defaultString(notification.getSendNotificationResponseTribunal(), "No"),
                "0"
        );
    }
}
