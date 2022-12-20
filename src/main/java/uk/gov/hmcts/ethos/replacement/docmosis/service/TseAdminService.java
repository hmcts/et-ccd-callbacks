package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.DocumentDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.IntWrapper;

import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

}
