package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.ListTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.CaseLink;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseLinksHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NotificationHelper;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.NotificationHelper.getRespondentRepresentative;

@Slf4j
@Service
@RequiredArgsConstructor
public class CaseLinksEmailService {
    public static final String CASE_NUMBER = "caseNumber";
    public static final String CASE_LINK = "linkToManageCase";
    public static final String CASE_TITLE = "caseTitle";
    private final CaseRetrievalForCaseWorkerService caseRetrievalForCaseWorkerService;
    private final EmailService emailService;
    @Value("${template.case-links.linked}")
    private String caseLinkedTemplateId;
    @Value("${template.case-links.unlinked}")
    private String caseUnlinkedTemplateId;
    @Value("${template.case-links.linked-rep}")
    private String caseLinkedLegalRepTemplateId;
    @Value("${template.case-links.unlinked-rep}")
    private String caseUnlinkedLegalRepTemplateId;

    /**
     * Called on about to submit case linking update.
     * Sends email to claimant and respondents that are unrepresented
     *
     * @param ccdRequest holds the request and case data
     * @param userToken  auth token
     * @param isLinking  determines if a case is being linked or unlinked
     */
    public void sendMailWhenCaseLinkForHearing(CCDRequest ccdRequest, String userToken, boolean isLinking) {
        SubmitEvent currentCase = caseRetrievalForCaseWorkerService.caseRetrievalRequest(userToken,
                ccdRequest.getCaseDetails().getCaseTypeId(), ccdRequest.getCaseDetails().getJurisdiction(),
                ccdRequest.getCaseDetails().getCaseId());
        ListTypeItem<CaseLink> caseLinksBeforeSubmit = currentCase.getCaseData().getCaseLinks();
        ListTypeItem<CaseLink> caseLinksAfterSubmit = ccdRequest.getCaseDetails().getCaseData().getCaseLinks();

        if (isLinking) {
            sendCaseLinkingEmails(ccdRequest.getCaseDetails(), caseLinksBeforeSubmit, caseLinksAfterSubmit, true);
        } else {
            // When removing case links we reverse the order of the lists passed to this function
            sendCaseLinkingEmails(ccdRequest.getCaseDetails(), caseLinksAfterSubmit, caseLinksBeforeSubmit, false);
        }
    }

    private void sendCaseLinkingEmails(CaseDetails caseDetails,
                                       ListTypeItem<CaseLink> list1,
                                       ListTypeItem<CaseLink> list2,
                                       Boolean isLinking) {
        boolean isLinkedForHearing;
        if (list1 == null || list1.isEmpty()) {
            isLinkedForHearing = CaseLinksHelper.isLinkedForHearing(list2);
        } else {
            List<GenericTypeItem<CaseLink>> diff = list2.stream()
                    .filter(element -> !list1.contains(element))
                    .toList();
            isLinkedForHearing = CaseLinksHelper.isLinkedForHearing(diff);
        }
        if (isLinkedForHearing) {
            sendEmails(caseDetails, isLinking);
        }
    }

    private void sendEmails(CaseDetails caseDetails, boolean isLinking) {
        CaseData caseData = caseDetails.getCaseData();
        String templateId = isLinking ? caseLinkedTemplateId : caseUnlinkedTemplateId;
        String caseId = caseDetails.getCaseId();

        sendEmailToClaimant(caseId, caseData, templateId);

        List<RespondentSumTypeItem> respondents = caseData.getRespondentCollection();
        for (RespondentSumTypeItem respondent : respondents) {
            RepresentedTypeR representative = getRespondentRepresentative(caseData, respondent.getValue());
            if (representative != null) {
                sendEmailToLegalRep(caseId, representative, caseData, isLinking);
            } else {
                sendEmailToUnrepresentedRespondent(respondent, caseData, templateId);
            }
        }
    }

    private void sendEmailToClaimant(String caseId, CaseData caseData, String templateId) {
        if (NO.equals(caseData.getClaimantRepresentedQuestion())) {
            Map<String, Object> claimantPersonalisation = Map.of(
                    CASE_NUMBER, caseData.getEthosCaseReference(),
                    CASE_LINK, "To manage your case, go to "
                            + emailService.getCitizenCaseLink(caseId)
            );
            emailService.sendEmail(templateId,
                    caseData.getClaimantType().getClaimantEmailAddress(),
                    claimantPersonalisation);
        }
    }

    private void sendEmailToLegalRep(String caseId,
                                     RepresentedTypeR representative,
                                     CaseData caseData,
                                     boolean isLinking) {
        String legalRepEmail = representative.getRepresentativeEmailAddress();
        String claimant = caseData.getClaimantIndType().getClaimantFirstNames()
                + " "
                + caseData.getClaimantIndType().getClaimantLastName();
        String respondents = Helper.getRespondentNames(caseData);
        if (legalRepEmail != null) {
            String repEmailTemplateId = isLinking ? caseLinkedLegalRepTemplateId : caseUnlinkedLegalRepTemplateId;
            Map<String, Object> repPersonalisation = Map.of(
                    CASE_TITLE, claimant + " vs " + respondents,
                    CASE_NUMBER, caseData.getEthosCaseReference(),
                    CASE_LINK, emailService.getExuiCaseLink(caseId)
            );
            emailService.sendEmail(repEmailTemplateId, legalRepEmail, repPersonalisation);
        }
    }

    private void sendEmailToUnrepresentedRespondent(RespondentSumTypeItem respondent,
                                                    CaseData caseData,
                                                    String templateId) {
        String respondentEmail =
                NotificationHelper.getEmailAddressForRespondent(caseData, respondent.getValue());
        if (respondentEmail != null) {
            Map<String, Object> respondentPersonalisation = Map.of(
                    CASE_NUMBER, caseData.getEthosCaseReference(),
                    CASE_LINK, ""
            );
            emailService.sendEmail(templateId, respondentEmail, respondentPersonalisation);
        }
    }
}
