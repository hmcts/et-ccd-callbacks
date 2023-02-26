package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.*;
import uk.gov.hmcts.et.common.model.ccd.types.TseAdminRecordDecisionType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.IntWrapper;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.*;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.TseHelper.*;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings({"squid:S1192", "PMD.AvoidInstantiatingObjectsInLoops", "PMD.ExcessiveImports"})
public class TseViewAppService {

    private final DocumentManagementService documentManagementService;

    private static final String RESPONSE_APP_DETAILS = "| | |\r\n"
            + "|--|--|\r\n"
            + "|%s application | %s|\r\n"
            + "|Application date | %s|\r\n"
            + "|Give details | %s|\r\n"
            + "|Supporting material | %s|\r\n"
            + "\r\n";


    private static final String STRING_BR = "<br>";

    /**
     * Initial Application and Respond details table.
     * @param caseData contains all the case data
     */
    public void setApplicationResponsesTable(CaseData caseData, String authToken) {
        GenericTseApplicationType applicationTypeItem = getChosenApplication(caseData);
        if (applicationTypeItem != null) {
            caseData.setTseApplicationSummary(initialTseAdminAppDetails(applicationTypeItem, authToken)
                + initialTseAdminRespondDetails(applicationTypeItem, authToken));
        }
    }

    private String initialTseAdminAppDetails(GenericTseApplicationType applicationType, String authToken) {
        return String.format(
            RESPONSE_APP_DETAILS,
            applicationType.getApplicant(),
            applicationType.getType(),
            applicationType.getDate(),
            applicationType.getDetails(),
            documentManagementService.displayDocNameTypeSizeLink(applicationType.getDocumentUpload(), authToken)
        );
    }

    private String initialTseAdminRespondDetails(GenericTseApplicationType applicationType, String authToken) {
        if (CollectionUtils.isEmpty(applicationType.getRespondCollection())) {
            return "";
        }
        IntWrapper respondCount = new IntWrapper(0);
        return applicationType.getRespondCollection().stream()
            .map(replyItem ->
                ADMIN.equals(replyItem.getValue().getFrom())
                    ? formatAdminReply(
                        replyItem.getValue(),
                        respondCount.incrementAndReturnValue(),
//                        documentManagementService.displayDocNameTypeSizeLink(
//                            replyItem.getValue().getAddDocument(), authToken)
                        populateListDocWithInfoAndLink(replyItem)
                )
                    : formatLegalRepReplyOrClaimantForDecision(
                        replyItem.getValue(),
                        respondCount.incrementAndReturnValue(),
                        // "Case reference 1674211483380412 not found for requested document."))
                        populateListDocWithInfoAndLink(replyItem)))
            .collect(Collectors.joining(""));
    }

    private String populateListDocWithInfoAndLink(TseRespondTypeItem response) {
//        if (CollectionUtils.isEmpty(supportingMaterial)) {
//            return "";
//        }
        log.info("about to stream supporting material");
//        return supportingMaterial.stream()
//            .map(documentTypeItem ->{
//
//               return documentManagementService.displayDocNameTypeSizeLink(
//                    documentTypeItem.getValue().getUploadedDocument(), authToken) + STRING_BR;
//            })
//            .collect(Collectors.joining());
        log.info("document fetching.... ");
        ArrayList<String> links = getDocumentUrls(response);
        AtomicInteger j = new AtomicInteger(1);
        log.info("links is " + links);

        String result = "";
        if (links != null) {
            result = links.stream().map(link -> {
                if (j.get() == 1) {
                    j.getAndIncrement();
                    // return "|Supporting Material |" + link + "\r\n";
                    return link + STRING_BR;
                }
               //  return "| |" + link + "\r\n";
                return link + STRING_BR;
            }).collect(Collectors.joining(""));
        }
        log.info("resulting tbale str is", result);
        return result;
    }

    public static ArrayList<String> getDocumentUrls(TseRespondTypeItem tseRespondType){
        if (tseRespondType.getValue().getSupportingMaterial() != null) {
            Pattern pattern = Pattern.compile("^.+?/documents/");

            ArrayList<String> links = tseRespondType.getValue().getSupportingMaterial().stream()
                    .map(doc -> {
                        Matcher matcher = pattern.matcher(doc.getValue().getUploadedDocument().getDocumentBinaryUrl());
                        String documentLink = matcher.replaceFirst("");
                        String documentName = doc.getValue().getUploadedDocument().getDocumentFilename();
                        return String.format("<a href=\"/documents/%s\" target=\"_blank\">%s</a>", documentLink, documentName);
                    }).collect(Collectors.toCollection(ArrayList::new));
            return links;
        }
        return null;
    }

    private UploadedDocumentType getResponseRequiredDocYesOrNo(CaseData caseData) {
        if (YES.equals(caseData.getTseAdminIsResponseRequired())) {
            return caseData.getTseAdminResponseRequiredYesDoc();
        }
        return caseData.getTseAdminResponseRequiredNoDoc();
    }

}
