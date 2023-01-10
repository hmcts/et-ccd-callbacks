package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.TseAdminReplyTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.TseAdminReplyType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.IntWrapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.TseHelper.getSelectedApplicationTypeItem;

@Slf4j
@Service
@RequiredArgsConstructor
public class TseAdmReplyService {

    private final DocumentManagementService documentManagementService;

    private static final String APP_DETAILS = "| | |\r\n"
            + "|--|--|\r\n"
            + "|Applicant | %s|\r\n"
            + "|Type of application | %s|\r\n"
            + "|Application date | %s|\r\n"
            + "|%s | %s|\r\n"
            + "|Supporting material | %s|\r\n"
            + "|Do you want to copy this correspondence to the other party to satisfy the Rules of Procedure? | %s|\r\n"
            + "\r\n";
    private static final String RESPONSE_DETAILS = "|Response %s | |\r\n"
            + "|--|--|\r\n"
            + "|Response from | %s|\r\n"
            + "|Response date | %s|\r\n"
            + "|What’s your response to the %s’s application? | %s|\r\n"
            + "|Supporting material | %s|\r\n"
            + "|Do you want to copy this correspondence to the other party to satisfy the Rules of Procedure? | %s|\r\n"
            + "\r\n";
    private static final String STRING_BR = "<br>";
    private static final String APPLICATION_QUESTION = "Give details";
    private static final String COPY_TO_OTHER_PARTY_YES = "I confirm I want to copy";
    private static final String COPY_TO_OTHER_PARTY_NO = "I do not want to copy";

    /**
     * Initial Application and Respond details table.
     * @param caseData contains all the case data
     * @param authToken the caller's bearer token used to verify the caller
     */
    public String initialTseAdmReplyTableMarkUp(CaseData caseData, String authToken) {
        GenericTseApplicationTypeItem applicationTypeItem = getSelectedApplicationTypeItem(caseData);
        if (applicationTypeItem != null) {
            return initialAppDetails(applicationTypeItem.getValue(), authToken)
                    + initialRespondDetails(applicationTypeItem.getValue(), authToken);
        }
        return null;
    }

    private String initialAppDetails(GenericTseApplicationType applicationType, String authToken) {
        return String.format(
                APP_DETAILS,
                applicationType.getApplicant(),
                applicationType.getType(),
                applicationType.getDate(),
                APPLICATION_QUESTION,
                applicationType.getDetails(),
                documentManagementService.displayDocNameTypeSizeLink(applicationType.getDocumentUpload(), authToken),
                displayCopyToOtherPartyYesOrNo(applicationType.getCopyToOtherPartyYesOrNo())
        );
    }

    private String initialRespondDetails(GenericTseApplicationType applicationType, String authToken) {
        if (applicationType.getRespondentReply() == null) {
            return "";
        }
        IntWrapper respondCount = new IntWrapper(0);
        return applicationType.getRespondentReply().stream()
                .map(respondent -> String.format(
                        RESPONSE_DETAILS,
                        respondCount.incrementAndReturnValue(),
                        respondent.getValue().getFrom(),
                        respondent.getValue().getDate(),
                        respondent.getValue().getFrom().toLowerCase(),
                        respondent.getValue().getResponse(),
                        populateListDocWithInfoAndLink(respondent.getValue().getSupportingMaterial(), authToken),
                        displayCopyToOtherPartyYesOrNo(respondent.getValue().getCopyToOtherParty())))
                .findFirst()
                .orElse(null);
    }

    private String populateListDocWithInfoAndLink(List<DocumentTypeItem> supportingMaterial, String authToken) {
        if (supportingMaterial == null) {
            return "";
        }
        return supportingMaterial.stream()
                .map(documentTypeItem ->
                        documentManagementService.displayDocNameTypeSizeLink(
                                documentTypeItem.getValue().getUploadedDocument(), authToken) + STRING_BR)
                .collect(Collectors.joining());
    }

    private String displayCopyToOtherPartyYesOrNo(String copyToOtherPartyYesOrNo) {
        if (COPY_TO_OTHER_PARTY_YES.equals(copyToOtherPartyYesOrNo)) {
            return YES;
        } else if (COPY_TO_OTHER_PARTY_NO.equals(copyToOtherPartyYesOrNo)) {
            return NO;
        } else {
            return null;
        }
    }

    /**
     * Save Tse Admin Record a Decision data to the application object.
     * @param caseData in which the case details are extracted from
     */
    public void saveTseAdmReplyDataFromCaseData(CaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getGenericTseApplicationCollection())) {
            return;
        }

        GenericTseApplicationTypeItem applicationTypeItem = getSelectedApplicationTypeItem(caseData);
        if (applicationTypeItem != null) {

            GenericTseApplicationType genericTseApplicationType = applicationTypeItem.getValue();
            if (CollectionUtils.isEmpty(genericTseApplicationType.getAdminReply())) {
                genericTseApplicationType.setAdminReply(new ArrayList<>());
            }

            genericTseApplicationType.getAdminReply().add(
                    TseAdminReplyTypeItem.builder()
                            .id(UUID.randomUUID().toString())
                            .value(
                                    TseAdminReplyType.builder()
                                            .date(UtilHelper.formatCurrentDate(LocalDate.now()))
                                            .enterResponseTitle(caseData.getTseAdmReplyEnterResponseTitle())
                                            .additionalInformation(caseData.getTseAdmReplyAdditionalInformation())
                                            .addDocument(getDocumentMandatoryOrOptional(caseData))
                                            .isCmoOrRequest(caseData.getTseAdmReplyIsCmoOrRequest())
                                            .cmoMadeBy(caseData.getTseAdmReplyCmoMadeBy())
                                            .requestMadeBy(caseData.getTseAdmReplyRequestMadeBy())
                                            .enterFullName(caseData.getTseAdmReplyEnterFullName())
                                            .isResponseRequired(caseData.getTseAdmReplyIsResponseRequired())
                                            .selectPartyRespond(caseData.getTseAdmReplySelectPartyRespond())
                                            .selectPartyNotify(caseData.getTseAdmReplySelectPartyNotify())
                                            .build()
                            ).build());
        }
    }

    private UploadedDocumentType getDocumentMandatoryOrOptional(CaseData caseData) {
        if (YES.equals(caseData.getTseAdmReplyIsResponseRequired())) {
            return caseData.getTseAdmReplyAddDocumentMandatory();
        }
        return caseData.getTseAdmReplyAddDocumentOptional();
    }

    /**
     * Clear Tse Admin Record a Decision Interface data from caseData.
     * @param caseData in which the case details are extracted from
     */
    public void clearTseAdmReplyDataFromCaseData(CaseData caseData) {
        caseData.setTseAdminSelectApplication(null);
        caseData.setTseAdmReplyTableMarkUp(null);
        caseData.setTseAdmReplyEnterResponseTitle(null);
        caseData.setTseAdmReplyAdditionalInformation(null);
        caseData.setTseAdmReplyAddDocumentMandatory(null);
        caseData.setTseAdmReplyAddDocumentOptional(null);
        caseData.setTseAdmReplyIsCmoOrRequest(null);
        caseData.setTseAdmReplyCmoMadeBy(null);
        caseData.setTseAdmReplyRequestMadeBy(null);
        caseData.setTseAdmReplyEnterFullName(null);
        caseData.setTseAdmReplyIsResponseRequired(null);
        caseData.setTseAdmReplySelectPartyRespond(null);
        caseData.setTseAdmReplySelectPartyNotify(null);
    }

}
