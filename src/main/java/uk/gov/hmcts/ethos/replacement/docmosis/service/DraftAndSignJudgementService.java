package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.types.DraftAndSignJudgement;

import java.time.LocalDate;
import java.util.Map;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.SPACE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.CASE_NUMBER;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.CLAIMANT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.LINK_TO_DOCUMENT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.REASON_FOR_PUBLICATION;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.RESPONDENT_NAMES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.SUBMITTED_DATE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.USER_NAME;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.getRespondentNames;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.nullCheck;

@Slf4j
@Service
@RequiredArgsConstructor
public class DraftAndSignJudgementService {

    private final EmailService emailService;
    private final UserIdamService userIdamService;

    @Value("${template.draftAndSignJudgement.forFindCaseLaw}")
    private String forFindCaseLawEmailTemplateId;
    @Value("${email.forFindCaseLaw}")
    private String emailAddressToSend;

    public void checkAndSendEmails(CaseDetails caseDetails, String userToken) {
        CaseData caseData = caseDetails.getCaseData();
        DraftAndSignJudgement judgement = caseData.getDraftAndSignJudgement();
        UserDetails userDetails = userIdamService.getUserDetails(userToken);

        if (isNotEmpty(judgement)
            && YES.equals(judgement.getIsJudgement())
            && YES.equals(judgement.getShouldForPublication())) {
            emailService.sendEmail(
                forFindCaseLawEmailTemplateId,
                emailAddressToSend,
                prepareEmailContent(caseDetails, judgement, userDetails)
            );
        }
    }

    private Map<String, Object> prepareEmailContent(CaseDetails caseDetails, DraftAndSignJudgement judgement, UserDetails userDetails) {
        CaseData caseData = caseDetails.getCaseData();
        String userName = nullCheck(userDetails.getFirstName() + SPACE + userDetails.getLastName());
        return Map.of(
            CASE_NUMBER, caseData.getEthosCaseReference(),
            CLAIMANT, caseData.getClaimant(),
            RESPONDENT_NAMES, getRespondentNames(caseData),
            REASON_FOR_PUBLICATION, judgement.getShouldForPublicationReason(),
            SUBMITTED_DATE, UtilHelper.formatCurrentDate(LocalDate.now()),
            USER_NAME, userName,
            LINK_TO_DOCUMENT, "link"
        );
    }
}
