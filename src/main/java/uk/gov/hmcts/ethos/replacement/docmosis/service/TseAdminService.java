package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.IntWrapper;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
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

    /**
     * Initial Application and Respond details table.
     * @param caseData contains all the case data
     */
    public void initialTseAdminTableMarkUp(CaseData caseData) {
        GenericTseApplicationTypeItem applicationTypeItem = getApplication(caseData);
        if (applicationTypeItem != null) {
            GenericTseApplicationType applicationType = applicationTypeItem.getValue();
            String appDetails = initialTseAdminAppDetails(applicationType);
            String responseDetails = initialTseAdminRespondDetails(applicationType);
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

    private String initialTseAdminAppDetails(GenericTseApplicationType applicationType) {
        return String.format(
                APP_DETAILS,
                applicationType.getApplicant(),
                applicationType.getType(),
                applicationType.getDate(),
                "Give details",
                applicationType.getDetails(),
                populateDocWithInfoAndLink(applicationType.getDocumentUpload())
        );
    }

    private String populateDocWithInfoAndLink(UploadedDocumentType document) {
        if (document == null) {
            return "";
        }
        Pattern pattern = Pattern.compile("^.+?/documents/");
        Matcher matcher = pattern.matcher(document.getDocumentBinaryUrl());
        String documentLink = matcher.replaceFirst("");
        String documentName = document.getDocumentFilename();
        String documentFileType = "File type";
        String documentFileSize = "File size";
        return String.format(FILE_DISPLAY, documentLink, documentName, documentFileType, documentFileSize);
    }

    private String initialTseAdminRespondDetails(GenericTseApplicationType applicationType) {
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
                        populateListDocWithInfoAndLink(respondent.getValue().getSupportingMaterial())))
                .collect(Collectors.joining());
    }

    private String populateListDocWithInfoAndLink(List<DocumentTypeItem> supportingMaterial) {
        if (supportingMaterial == null) {
            return "";
        }
        return supportingMaterial.stream()
                .map(documentTypeItem ->
                        populateDocWithInfoAndLink(documentTypeItem.getValue().getUploadedDocument()) + STRING_BR)
                .collect(Collectors.joining());
    }

}
