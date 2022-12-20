package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.apache.commons.io.FileUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.DocumentDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.IntWrapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TseAdminService {

    private static final String APP_DETAILS = "| | |\r\n"
            + "|--|--|\r\n"
            + "|%s application | %s|\r\n"
            + "|Application date | %s|\r\n"
            + "|%s | %s|\r\n"
            + "|Supporting material | %s|\r\n"
            + "\r\n";

    private static final String RESPONSE_DETAILS = "|Response %s | |\r\n"
            + "|--|--|\r\n"
            + "|Response from | %s|\r\n"
            + "|Response date | %s|\r\n"
            + "|Details | %s|\r\n"
            + "|Supporting material | %s|\r\n"
            + "\r\n";

    private final EmailService emailService;

    @Value("${tse.admin.template.id}")
    private String emailTemplateId;

    private static final String BOTH = "Both parties";
    private static final String CLAIMANT_ONLY = "Claimant only";
    private static final String RESPONDENT_ONLY = "Respondent only";
    private static final String FILE_DISPLAY = "<a href=\"/documents/%s\" target=\"_blank\">%s (%s, %s)</a>";
    private static final String STRING_BR = "<br>";

    private final DocumentManagementService documentManagementService;

    /**
     * Initial Application and Respond details table.
     * @param caseData contains all the case data
     */
    public void initialTseAdminTableMarkUp(CaseData caseData, String authToken) {
        GenericTseApplicationTypeItem applicationTypeItem = getApplication(caseData);
        if (applicationTypeItem != null) {
            GenericTseApplicationType applicationType = applicationTypeItem.getValue();
            String appDetails = initialTseAdminAppDetails(applicationType, authToken);
            String responseDetails = initialTseAdminRespondDetails(applicationType, authToken);
            caseData.setTseAdminTableMarkUp(appDetails + responseDetails);
        }
    }

    private GenericTseApplicationTypeItem getApplication(CaseData caseData) {
        String selectedAppId = caseData.getTseAdminSelectApplication().getSelectedCode();
        return caseData.getGenericTseApplicationCollection().stream()
                .filter(genericTseApplicationTypeItem ->
                        genericTseApplicationTypeItem.getValue().getNumber().equals(selectedAppId))
                .findFirst()
                .orElse(null);
    }

    private String initialTseAdminAppDetails(GenericTseApplicationType applicationType, String authToken) {
        return String.format(
                APP_DETAILS,
                applicationType.getApplicant(),
                applicationType.getType(),
                applicationType.getDate(),
                "Give details",
                applicationType.getDetails(),
                populateDocWithInfoAndLink(applicationType.getDocumentUpload(), authToken)
        );
    }

    private String populateDocWithInfoAndLink(UploadedDocumentType document, String authToken) {
        if (document == null) {
            return "";
        }
        Pattern pattern = Pattern.compile("^.+?/documents/");
        Matcher matcher = pattern.matcher(document.getDocumentBinaryUrl());
        String documentLink = matcher.replaceFirst("");

        int lastIndexDot = document.getDocumentFilename().lastIndexOf('.');
        String documentName = document.getDocumentFilename().substring(0, lastIndexDot);
        String documentType = document.getDocumentFilename().substring(lastIndexDot + 1).toUpperCase();

        ResponseEntity<DocumentDetails> documentDetails =
                documentManagementService.getDocumentDetails(authToken,
                        UUID.fromString(documentManagementService.getDocumentUUID(document.getDocumentUrl())));
        if (documentDetails != null && documentDetails.getBody() != null) {
            return String.format(FILE_DISPLAY,
                    documentLink,
                    documentName,
                    documentType,
                    FileUtils.byteCountToDisplaySize(Long.parseLong(documentDetails.getBody().getSize())));
        }
        return String.format(FILE_DISPLAY, documentLink, documentName, documentType, "");
    }

    private String initialTseAdminRespondDetails(GenericTseApplicationType applicationType, String authToken) {
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
                        respondent.getValue().getResponse(),
                        populateListDocWithInfoAndLink(respondent.getValue().getSupportingMaterial(), authToken)))
                .collect(Collectors.joining());
    }

    private String populateListDocWithInfoAndLink(List<DocumentTypeItem> supportingMaterial, String authToken) {
        if (supportingMaterial == null) {
            return "";
        }
        return supportingMaterial.stream()
                .map(documentTypeItem ->
                        populateDocWithInfoAndLink(
                                documentTypeItem.getValue().getUploadedDocument(), authToken) + STRING_BR)
                .collect(Collectors.joining());
    }

    /**
     * Uses {@link EmailService} to generate an email.
     * @param caseData in which the case details are extracted from
     */
    public void sendRecordADecisionEmails(CaseData caseData) {
        String caseNumber = caseData.getEthosCaseReference();

        Map<String, String> emailsToSend = new HashMap<>();

        // if respondent only or both parties: send Respondents Decision Emails
        if (RESPONDENT_ONLY.equals(caseData.getTseAdminSelectPartyNotify()) || BOTH.equals(caseData.getTseAdminSelectPartyNotify())) {
            for (RespondentSumTypeItem respondentSumTypeItem: caseData.getRespondentCollection()) {
                if (respondentSumTypeItem.getValue().getRespondentEmail() != null) {
                    emailsToSend.put(respondentSumTypeItem.getValue().getRespondentEmail(),
                        respondentSumTypeItem.getValue().getRespondentName());
                }
            }
        }

        // if claimant only or both parties: send Claimant Decision Email
        if (CLAIMANT_ONLY.equals(caseData.getTseAdminSelectPartyNotify()) || BOTH.equals(caseData.getTseAdminSelectPartyNotify())) {
            String claimantEmail = caseData.getClaimantType().getClaimantEmailAddress();
            String claimantName = caseData.getClaimantIndType().getClaimantFirstNames()
                + " " + caseData.getClaimantIndType().getClaimantLastName();

            if (claimantEmail != null) {
                emailsToSend.put(claimantEmail, claimantName);
            }
        }

        for (Map.Entry<String, String> emailRecipient : emailsToSend.entrySet()) {
            emailService.sendEmail(
                emailTemplateId,
                emailRecipient.getKey(),
                buildPersonalisation(caseNumber, emailRecipient.getValue()));
        }
    }

    private Map<String, String> buildPersonalisation(String caseNumber, String name) {
        Map<String, String> personalisation = new ConcurrentHashMap<>();
        personalisation.put("caseNumber", caseNumber);
        personalisation.put("name", name);
        return personalisation;
    }

}
