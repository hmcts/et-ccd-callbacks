package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.*;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.TseAdminRecordDecisionType;
import uk.gov.hmcts.et.common.model.ccd.types.TseRespondType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MarkdownHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.IntWrapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.*;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLOSED_STATE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TableMarkupConstants.ADDITIONAL_INFORMATION;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TableMarkupConstants.CLOSE_APP_DECISION_DETAILS_OTHER;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TableMarkupConstants.DATE_MARKUP;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TableMarkupConstants.DETAILS_OF_THE_APPLICATION;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TableMarkupConstants.NAME_MARKUP;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TableMarkupConstants.SUPPORTING_MATERIAL_TABLE_HEADER;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TableMarkupConstants.TABLE_STRING;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.TseHelper.formatRule92;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.TseHelper.getSelectedApplicationTypeItem;

@Slf4j
@Service
@RequiredArgsConstructor
public class TseAdmCloseService {

    private final DocumentManagementService documentManagementService;
    private final TseService tseService;

    private static final String CLOSE_APP_DETAILS = "| | |\r\n"
        + TABLE_STRING
        + "|Applicant | %s|\r\n"
        + "|Type of application | %s|\r\n"
        + "|Application date | %s|\r\n"
        + "%s" // Details of the application
        + "%s" // Supporting material
        + "%s" // Rule92
        + "\r\n";

    private static final String CLOSE_APP_DECISION_DETAILS = "|Decision | |\r\n"
        + TABLE_STRING
        + "|Notification | %s|\r\n"
        + "|Decision | %s|\r\n"
        + "%s" // Decision details
        + DATE_MARKUP
        + "|Sent by | %s|\r\n"
        + "|Type of decision | %s|\r\n"
        + "%s%s"
        + "|Decision made by | %s|\r\n"
        + NAME_MARKUP
        + "|Sent to | %s|\r\n"
        + "\r\n";

    public String generateCloseApplicationDetailsMarkdown(CaseData caseData, String authToken) {
        if (getSelectedApplicationTypeItem(caseData) == null) {
            return null;
        }
        GenericTseApplicationTypeItem applicationTypeItem = getSelectedApplicationTypeItem(caseData);
        String decisionsMarkdown = "";
        if (applicationTypeItem.getValue().getAdminDecision() != null) {
            // Multiple decisions can be made for the same application but we are only showing the last one for now
            Optional<String> decisionsMarkdownResult = applicationTypeItem.getValue().getAdminDecision()
                .stream()
                .reduce((first, second) -> second)
                .map(d -> String.format(CLOSE_APP_DECISION_DETAILS,
                    d.getValue().getEnterNotificationTitle(),
                    d.getValue().getDecision(),
                    formatDecisionDetails(d.getValue()),
                    d.getValue().getDate(),
                    "Tribunal",
                    d.getValue().getTypeOfDecision(),
                    getAdditionInfoMarkdown(d),
                    getDecisionDocumentLink(d.getValue(), authToken),
                    d.getValue().getDecisionMadeBy(),
                    d.getValue().getDecisionMadeByFullName(),
                    d.getValue().getSelectPartyNotify()));

            if (decisionsMarkdownResult.isPresent()) {
                decisionsMarkdown = decisionsMarkdownResult.get();
            }
        }

        return String.format(
            CLOSE_APP_DETAILS,
            applicationTypeItem.getValue().getApplicant(),
            applicationTypeItem.getValue().getType(),
            applicationTypeItem.getValue().getDate(),
            isBlank(applicationTypeItem.getValue().getDetails())
                ? ""
                : String.format(DETAILS_OF_THE_APPLICATION, applicationTypeItem.getValue().getDetails()),
            getApplicationDocumentLink(applicationTypeItem, authToken),
            formatRule92(applicationTypeItem.getValue().getCopyToOtherPartyYesOrNo(),
                applicationTypeItem.getValue().getCopyToOtherPartyText())
        )
            + tseService.formatApplicationResponses(applicationTypeItem.getValue(), authToken)
            + decisionsMarkdown;

    }

    private String getAdditionInfoMarkdown(TseAdminRecordDecisionTypeItem decision) {
        return decision.getValue().getAdditionalInformation() == null
            ? ""
            : String.format(ADDITIONAL_INFORMATION, decision.getValue().getAdditionalInformation());
    }

    private String formatDecisionDetails(TseAdminRecordDecisionType decision) {
        return isBlank(decision.getDecisionDetails())
            ? ""
            : String.format(CLOSE_APP_DECISION_DETAILS_OTHER, decision.getDecisionDetails());
    }

    private String getDecisionDocumentLink(TseAdminRecordDecisionType decisionType, String authToken) {
        if (decisionType.getResponseRequiredDoc() == null) {
            return "";
        }

        return decisionType.getResponseRequiredDoc().stream()
                .map(GenericTypeItem::getValue)
                .map(o -> tseService.formatDocumentForTwoColumnTable(o, authToken))
                .collect(Collectors.joining(""));
    }

    private String getApplicationDocumentLink(GenericTseApplicationTypeItem applicationTypeItem, String authToken) {
        if (applicationTypeItem.getValue().getDocumentUpload() == null) {
            return "";
        }

        return String.format(SUPPORTING_MATERIAL_TABLE_HEADER,
            documentManagementService.displayDocNameTypeSizeLink(
                applicationTypeItem.getValue().getDocumentUpload(), authToken));
    }

    /**
     * About to Submit Close Application.
     * @param caseData in which the case details are extracted from
     */
    public void aboutToSubmitCloseApplication(CaseData caseData) {
        GenericTseApplicationTypeItem applicationTypeItem = getSelectedApplicationTypeItem(caseData);
        if (applicationTypeItem != null) {
            applicationTypeItem.getValue().setCloseApplicationNotes(caseData.getTseAdminCloseApplicationText());
            applicationTypeItem.getValue().setStatus(CLOSED_STATE);
            caseData.setTseAdminCloseApplicationTable(null);
            caseData.setTseAdminCloseApplicationText(null);
            caseData.setTseAdminSelectApplication(null);
        }
    }

}
