package uk.gov.hmcts.ethos.replacement.docmosis.service.noc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignment;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NocNotificationHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseAccessService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EmailNotificationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EmailService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.RespondentUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.noc.ClaimantRepresentativeUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.noc.RespondentRepresentativeUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ecm.common.constants.PdfMapperConstants.STRING_EMPTY;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.MISSING_REP_CLAIMANT_TYPE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.MISSING_REP_TYPE_R_ITEM;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.NOC_TYPE_REMOVAL;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.REMOVE_ORGANISATION;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_FAILED_TO_GET_CASE_ASSIGNMENTS_BY_ID;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_CLAIMANT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_ORGANISATION;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_RESPONDENT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_TO_REMOVED_REPRESENTATIVE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_TO_UNREPRESENTED_PARTY;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.LEGAL_REP_NAME;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.LEGAL_REP_ORG;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.LINK_TO_CIT_UI;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.PARTY_NAME;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.STRING_COMMA_WITH_SPACE;

@Slf4j
@Service
@RequiredArgsConstructor
public class NocRemoveRepresentationService {

    private final NocCcdService nocCcdService;
    private final NocNotificationService nocNotificationService;
    private final EmailService emailService;
    private final CaseAccessService caseAccessService;
    private final EmailNotificationService emailNotificationService;
    private final NocRespondentRepresentativeService nocRespondentRepresentativeService;

    @Value("${template.nocNotification.org-admin-not-representing}")
    private String nocOrgAdminNotRepresentingTemplateId;
    @Value("${template.nocNotification.noc-legal-rep-no-longer-assigned}")
    private String nocLegalRepNoLongerAssignedTemplateId;
    @Value("${template.nocNotification.noc-citizen-no-longer-represented}")
    private String nocCitizenNoLongerRepresentedTemplateId;
    @Value("${template.nocNotification.noc-other-party-not-represented}")
    private String nocOtherPartyNotRepresentedTemplateId;

    /**
     * Revoke claimant legal rep and send email notifications to related parties.
     * @param caseDetails the case details of the case to revoke claimant legal rep
     * @param userToken the user token of the requester
     */
    public void revokeClaimantLegalRep(CaseDetails caseDetails, String userToken) {
        // get existing rep and organisation details for sending emails
        RepresentedTypeC existingClaimantRep = caseDetails.getCaseData().getRepresentativeClaimantType();
        if (existingClaimantRep == null) {
            throw new IllegalStateException(MISSING_REP_CLAIMANT_TYPE + caseDetails.getCaseId());
        }
        final String orgName = existingClaimantRep.getNameOfOrganisation();
        final String orgEmailAddress = nocNotificationService.findClaimantRepOrgSuperUserEmail(existingClaimantRep);
        final String repName = existingClaimantRep.getNameOfRepresentative();
        final String repEmailAddress = existingClaimantRep.getRepresentativeEmailAddress();
        final String partyName = caseDetails.getCaseData().getClaimant();

        // revoke claimant legal rep
        nocCcdService.revokeClaimantRepresentation(userToken, caseDetails);
        ClaimantRepresentativeUtils.markClaimantAsUnrepresented(caseDetails.getCaseData());

        // send email to organisation admin
        sendEmailToOrgAdmin(caseDetails, orgEmailAddress, repName);
        // send email to removed legal rep
        sendEmailToRemovedLegalRep(caseDetails, repEmailAddress);
        // send email to unrepresented party, i.e. claimant
        sendEmailToUnrepresentedClaimant(caseDetails, orgName);
        // send email to other party, i.e. respondents
        sendEmailToOtherPartyRespondent(caseDetails, null, partyName);
    }

    private void sendEmailToOrgAdmin(
        CaseDetails caseDetails,
        String emailToSend,
        String repName
    ) {
        if (isNullOrEmpty(emailToSend)) {
            return;
        }

        Map<String, String> personalisation =
            NocNotificationHelper.buildPreviousRespondentSolicitorPersonalisation(caseDetails.getCaseData());
        personalisation.put(LEGAL_REP_NAME, repName);

        try {
            emailService.sendEmail(
                nocOrgAdminNotRepresentingTemplateId,
                emailToSend,
                personalisation);
        } catch (Exception e) {
            log.warn(
                WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_ORGANISATION,
                caseDetails.getCaseId(),
                e.getMessage());
        }
    }

    private void sendEmailToRemovedLegalRep(CaseDetails caseDetails, String emailToSend) {
        try {
            Map<String, String> personalisation =
                NocNotificationHelper.buildPreviousRespondentSolicitorPersonalisation(caseDetails.getCaseData());
            emailService.sendEmail(
                nocLegalRepNoLongerAssignedTemplateId,
                emailToSend,
                personalisation);
        } catch (Exception e) {
            log.warn(
                WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_TO_REMOVED_REPRESENTATIVE,
                caseDetails.getCaseId(),
                e.getMessage());
        }
    }

    private void sendEmailToUnrepresentedClaimant(CaseDetails caseDetails, String orgName) {
        CaseData caseData = caseDetails.getCaseData();
        if (ObjectUtils.isEmpty(caseData.getClaimantType())
            || StringUtils.isBlank(caseData.getClaimantType().getClaimantEmailAddress())) {
            return;
        }
        String emailToSend = caseData.getClaimantType().getClaimantEmailAddress();
        String linkToCitUI = emailService.getCitizenCaseLink(caseDetails.getCaseId());

        sendEmailToUnrepresentedParty(caseDetails, emailToSend, orgName, linkToCitUI);
    }

    private void sendEmailToUnrepresentedParty(
        CaseDetails caseDetails,
        String emailToSend,
        String orgName,
        String linkToCitUI
    ) {
        Map<String, String> personalisation =
            NocNotificationHelper.buildPreviousRespondentSolicitorPersonalisation(caseDetails.getCaseData());
        personalisation.put(LEGAL_REP_ORG, orgName);
        personalisation.put(LINK_TO_CIT_UI, linkToCitUI);

        try {
            emailService.sendEmail(
                nocCitizenNoLongerRepresentedTemplateId,
                emailToSend,
                personalisation);
        } catch (Exception e) {
            log.warn(
                WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_TO_UNREPRESENTED_PARTY,
                caseDetails.getCaseId(),
                e.getMessage());
        }
    }

    private void sendEmailToOtherPartyRespondent(
        CaseDetails caseDetails,
        List<String> respondentIdInRevokeList,
        String partyName
    ) {
        List<CaseUserAssignment> caseUserAssignments =
            caseAccessService.getCaseUserAssignmentsById(caseDetails.getCaseId());
        if (caseUserAssignments == null || caseUserAssignments.isEmpty()) {
            log.warn(WARNING_FAILED_TO_GET_CASE_ASSIGNMENTS_BY_ID,
                caseDetails.getCaseId());
            return;
        }

        // send email to respondent legal rep
        emailNotificationService.getRespondentSolicitorEmails(caseUserAssignments).forEach(email -> {
                String linkToCitUI = emailService.getExuiCaseLink(caseDetails.getCaseId());
                sendEmailToEachRespondent(caseDetails, email, partyName, linkToCitUI);
            }
        );

        // send email to respondent not rep or revoke
        List<RespondentSumTypeItem> respondentToEmail =
            getRespondentCollectionToEmail(caseDetails.getCaseData(), respondentIdInRevokeList);
        respondentToEmail.forEach(respondentSumTypeItem -> {
            RespondentSumType respondent = respondentSumTypeItem.getValue();
            String email = StringUtils.isNotBlank(respondent.getResponseRespondentEmail())
                ? respondent.getResponseRespondentEmail()
                : respondent.getRespondentEmail();
            String linkToCitUI = emailService.getSyrCaseLink(caseDetails.getCaseId(), respondentSumTypeItem.getId());
            sendEmailToEachRespondent(caseDetails, email, partyName, linkToCitUI);
        });
    }

    private void sendEmailToEachRespondent(
        CaseDetails caseDetails,
        String emailToSend,
        String partyName,
        String linkToCitUI
    ) {
        try {
            Map<String, String> personalisation =
                NocNotificationHelper.buildPreviousRespondentSolicitorPersonalisation(caseDetails.getCaseData());
            personalisation.put(PARTY_NAME, partyName);
            personalisation.put(LINK_TO_CIT_UI, linkToCitUI);

            emailService.sendEmail(
                nocOtherPartyNotRepresentedTemplateId,
                emailToSend,
                personalisation
            );
        } catch (Exception e) {
            log.warn(
                WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_RESPONDENT,
                caseDetails.getCaseId(),
                e.getMessage()
            );
        }
    }

    private List<RespondentSumTypeItem> getRespondentCollectionToEmail(
        CaseData caseData,
        List<String> respondentIdInRevokeList
    ) {
        Set<String> respondentIdWithRep = caseData.getRepCollection().stream()
            .map(RepresentedTypeRItem::getValue)
            .filter(Objects::nonNull)
            .map(RepresentedTypeR::getRespondentId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        return caseData.getRespondentCollection().stream()
            .filter(item -> item.getId() != null)
            .filter(item -> !respondentIdWithRep.contains(item.getId()))
            .filter(item -> !respondentIdInRevokeList.contains(item.getId()))
            .collect(Collectors.toList());
    }

    /**
     * About to start event to check if more than 1 representative from the organisation.
     * @param caseDetails the case details of the case to revoke respondent legal rep
     * @param userToken the user token of the requester
     * @return return Yes if more than 1 representative from the organisation, else return No
     */
    public String isMoreThanOneRespondent(CaseDetails caseDetails, String userToken) {
        // get list of RepresentedTypeRItem that represented by this legal rep
        List<RepresentedTypeRItem> currentRepList =
            nocRespondentRepresentativeService.findRepresentativesByToken(userToken, caseDetails);
        if (CollectionUtils.isEmpty(currentRepList)) {
            return NO;
        }

        // get the organisation id for this legal rep
        String orgId = getFirstRepOrganisationId(currentRepList);
        if (isNullOrEmpty(orgId)) {
            return NO;
        }

        // get all legal reps who are under the same organisation
        List<RepresentedTypeRItem> orgRepList =
            RespondentRepresentativeUtils.findRepresentativesByOrganisationId(
                caseDetails.getCaseData(),
                orgId
            );

        // compare and see if other legal reps involved in this case
        return orgRepList.size() > currentRepList.size()
            ? YES
            : NO;
    }

    private String getFirstRepOrganisationId(List<RepresentedTypeRItem> currentRepList) {
        if (CollectionUtils.isEmpty(currentRepList)) {
            return null;
        }

        // assume all items are belongs to the same legal rep, get the first one
        RepresentedTypeR currentRep = currentRepList.getFirst().getValue();
        if (currentRep.getRespondentOrganisation() == null
            || currentRep.getRespondentOrganisation().getOrganisationID() == null) {
            return null;
        }

        // return the organisation id
        return currentRep.getRespondentOrganisation().getOrganisationID();
    }

    /**
     * Revoke respondent legal rep and send email notifications to related parties.
     * @param caseDetails the case details of the case to revoke respondent legal rep
     * @param userToken the user token of the requester
     */
    public void revokeRespondentLegalRep(CaseDetails caseDetails, String userToken) {
        // get a list of RepresentedTypeRItem to be revoked
        List<RepresentedTypeRItem> repListToRevoke = getRespondentRepListToRevoke(caseDetails, userToken);
        if (CollectionUtils.isEmpty(repListToRevoke)) {
            throw new IllegalStateException(MISSING_REP_TYPE_R_ITEM + caseDetails.getCaseId());
        }

        // get existing rep and organisation details for sending emails
        final String orgName = getRespondentEmailOrgName(repListToRevoke);
        final String orgEmailAddress =
            nocNotificationService.resolveRespondentRepresentativeOrganisationSuperuserEmail(
                caseDetails,
                repListToRevoke.getFirst(),
                NOC_TYPE_REMOVAL
            );
        final String repName = getRespondentEmailRepName(repListToRevoke);
        final List<String> repEmailAddress = getRespondentEmailRepEmailAddress(repListToRevoke);
        final String partyName = getRespondentEmailPartyName(repListToRevoke);
        final List<String> respondentIdInRevokeList = getRespondentIdInRepList(repListToRevoke);

        // revoke respondent legal rep
        nocRespondentRepresentativeService.revokeAndRemoveRespondentRepresentatives(
            caseDetails,
            repListToRevoke
        );

        // send email to organisation admin
        sendEmailToOrgAdmin(caseDetails, orgEmailAddress, repName);
        // send email to removed legal rep
        sendEmailToListOfRemovedLegalRep(caseDetails, repEmailAddress);
        // send email to unrepresented party, i.e. this respondent
        sendEmailToUnrepresentedRespondent(caseDetails, repListToRevoke, orgName);
        // send email to claimant
        sendEmailToOtherPartyClaimant(caseDetails, partyName);
        // send email to other respondent
        sendEmailToOtherPartyRespondent(caseDetails, respondentIdInRevokeList, partyName);
    }

    private List<RepresentedTypeRItem> getRespondentRepListToRevoke(CaseDetails caseDetails, String userToken) {
        List<RepresentedTypeRItem> currentRepList =
            nocRespondentRepresentativeService.findRepresentativesByToken(userToken, caseDetails);
        if (CollectionUtils.isEmpty(currentRepList)) {
            throw new IllegalStateException(MISSING_REP_TYPE_R_ITEM + caseDetails.getCaseId());
        }

        if (YES.equals(caseDetails.getCaseData().getNocRemoveRepIsMoreThanOneFlag())
            && REMOVE_ORGANISATION.equals(caseDetails.getCaseData().getNocRemoveRepOption())) {
            String orgId = getFirstRepOrganisationId(currentRepList);
            return RespondentRepresentativeUtils.findRepresentativesByOrganisationId(
                    caseDetails.getCaseData(),
                    orgId
                );
        }

        return currentRepList;
    }

    private String getRespondentEmailOrgName(List<RepresentedTypeRItem> repList) {
        return repList.stream()
            .map(RepresentedTypeRItem::getValue)
            .filter(Objects::nonNull)
            .map(RepresentedTypeR::getNameOfOrganisation)
            .filter(name -> !isNullOrEmpty(name))
            .findFirst()
            .orElse(STRING_EMPTY);
    }

    private String getRespondentEmailRepName(List<RepresentedTypeRItem> repList) {
        return repList.stream()
            .map(RepresentedTypeRItem::getValue)
            .filter(Objects::nonNull)
            .map(RepresentedTypeR::getNameOfRepresentative)
            .filter(name -> !isNullOrEmpty(name))
            .distinct()
            .collect(Collectors.joining(STRING_COMMA_WITH_SPACE));
    }

    private List<String> getRespondentEmailRepEmailAddress(List<RepresentedTypeRItem> repList) {
        return repList.stream()
            .map(RepresentedTypeRItem::getValue)
            .filter(Objects::nonNull)
            .map(RepresentedTypeR::getRepresentativeEmailAddress)
            .filter(email -> !isNullOrEmpty(email))
            .distinct()
            .toList();
    }

    private String getRespondentEmailPartyName(List<RepresentedTypeRItem> currentRepList) {
        List<String> respondentNames = currentRepList.stream()
            .map(RepresentedTypeRItem::getValue)
            .filter(Objects::nonNull)
            .map(RepresentedTypeR::getRespRepName)
            .filter(Objects::nonNull)
            .toList();
        return String.join(STRING_COMMA_WITH_SPACE, respondentNames);
    }

    private List<String> getRespondentIdInRepList(List<RepresentedTypeRItem> repList) {
        return repList.stream()
            .map(RepresentedTypeRItem::getValue)
            .filter(Objects::nonNull)
            .map(RepresentedTypeR::getRespondentId)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private void sendEmailToListOfRemovedLegalRep(CaseDetails caseDetails, List<String> repEmailAddress) {
        repEmailAddress.forEach(email -> sendEmailToRemovedLegalRep(caseDetails, email));
    }

    private void sendEmailToUnrepresentedRespondent(
        CaseDetails caseDetails,
        List<RepresentedTypeRItem> repListToRevoke,
        String orgName
    ) {
        for (RepresentedTypeRItem representative : repListToRevoke) {
            // find respondent for this RepresentedTypeRItem
            RespondentSumTypeItem respondent = RespondentRepresentativeUtils.findRespondentByRepresentative(
                caseDetails.getCaseData(), representative);
            if (respondent == null || !RespondentUtils.isValidRespondent(respondent)) {
                continue;
            }

            // personalize email address and link
            String respondentEmailAddress = respondent.getValue().getRespondentEmail();
            String linkToCitUI = emailService.getSyrCaseLink(caseDetails.getCaseId(), respondent.getId());

            // send email to unrepresented respondent
            sendEmailToUnrepresentedParty(caseDetails, respondentEmailAddress, orgName, linkToCitUI);
        }
    }

    private void sendEmailToOtherPartyClaimant(CaseDetails caseDetails, String partyName) {
        // check if claimant is represented
        RepresentedTypeC representativeClaimantType = caseDetails.getCaseData().getRepresentativeClaimantType();
        boolean isClaimantRepresented = representativeClaimantType != null;

        // get email address of claimant or claimant legal rep
        String emailToSend = isClaimantRepresented
            ? representativeClaimantType.getRepresentativeEmailAddress()
            : caseDetails.getCaseData().getClaimantType().getClaimantEmailAddress();
        if (isNullOrEmpty(emailToSend)) {
            return;
        }

        // get email personalisation
        Map<String, String> personalisation =
            NocNotificationHelper.buildPreviousRespondentSolicitorPersonalisation(caseDetails.getCaseData());
        personalisation.put(PARTY_NAME, partyName);
        personalisation.put(LINK_TO_CIT_UI, isClaimantRepresented
            ? emailService.getExuiCaseLink(caseDetails.getCaseId())
            : emailService.getCitizenCaseLink(caseDetails.getCaseId()));

        // send email to claimant or claimant legal rep
        try {
            emailService.sendEmail(
                nocOtherPartyNotRepresentedTemplateId,
                emailToSend,
                personalisation
            );
        } catch (Exception e) {
            log.warn(
                WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_CLAIMANT,
                caseDetails.getCaseId(),
                e.getMessage()
            );
        }
    }
}
