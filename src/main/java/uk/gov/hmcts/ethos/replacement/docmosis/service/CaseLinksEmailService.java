package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.ethos.replacement.docmosis.config.NotificationProperties;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NotificationHelper;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;

@Slf4j
@Service
@RequiredArgsConstructor
public class CaseLinksEmailService {
    public static final String CASE_NUMBER = "caseNumber";
    public static final String CASE_LINK = "linkToManageCase";
    private final NotificationProperties notificationProperties;
    private final EmailService emailService;
    @Value("${caselinks.linked.template.id}")
    private String caseLinkedTemplateId;
    @Value("${caselinks.unlinked.template.id}")
    private String caseUnlinkedTemplateId;

    /**
     * Called after submitting case linking update.
     * Sends email to claimant and respondents that are unrepresented
     *
     * @param caseDetails holds the request and case data
     * @param isLinking   determines if the case is being linked or unlinked
     */
    public void sendCaseLinkingEmails(CaseDetails caseDetails, boolean isLinking) {
        CaseData caseData = caseDetails.getCaseData();
        String templateId = isLinking ? caseLinkedTemplateId : caseUnlinkedTemplateId;

        if (NO.equals(caseData.getClaimantRepresentedQuestion())) {
            Map<String, Object> claimantPersonalisation = Map.of(
                    CASE_NUMBER, caseData.getEthosCaseReference(),
                    CASE_LINK, "To manage your case, go to "
                            + notificationProperties.getCitizenLinkWithCaseId(caseDetails.getCaseId())
            );

            emailService.sendEmail(templateId,
                    caseData.getClaimantType().getClaimantEmailAddress(),
                    claimantPersonalisation);
        }

        Map<String, Object> respondentPersonalisation = Map.of(
                CASE_NUMBER, caseData.getEthosCaseReference(),
                CASE_LINK, notificationProperties.getExuiLinkWithCaseId(caseDetails.getCaseId())
        );

        List<RespondentSumTypeItem> respondents = caseData.getRespondentCollection();
        for (RespondentSumTypeItem respondent : respondents) {
            String respondentEmail =
                    NotificationHelper.getEmailAddressForUnrepresentedRespondent(caseData, respondent.getValue());
            if (respondentEmail != null) {
                emailService.sendEmail(templateId, respondentEmail, respondentPersonalisation);
            }
        }
    }
}
