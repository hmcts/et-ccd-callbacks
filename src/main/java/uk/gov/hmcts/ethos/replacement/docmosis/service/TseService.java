package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.*;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.TseRespondType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.citizenhub.ClaimantTse;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MarkdownHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.RespondentTellSomethingElseHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.TseHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.IntWrapper;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.RespondentTSEApplicationTypeData;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.*;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TableMarkupConstants.STRING_BR;

@Slf4j
@Service
@RequiredArgsConstructor
public class TseService {
    private static final String RULE92_QUESTION =
            "Do you want to copy this correspondence to the other party to satisfy the Rules of Procedure?";
    private static final String RULE92_DETAILS =
            "Details of why you do not want to inform the other party";
    public static final String WHATS_YOUR_RESPONSE = "What's your response to the %s's application";
    private final DocumentManagementService documentManagementService;

    /**
     * Creates a new TSE collection if it doesn't exist.
     * Create a new application in the list and assign the TSE data from CaseData to it.
     * At last, clears the existing TSE data from CaseData to ensure fields will be empty when user
     * starts a new application in the same case.
     *
     * @param caseData   contains all the case data.
     * @param isClaimant create a claimant application or a respondent application
     */

    public void createApplication(CaseData caseData, boolean isClaimant) {
        if (CollectionUtils.isEmpty(caseData.getGenericTseApplicationCollection())) {
            caseData.setGenericTseApplicationCollection(new ArrayList<>());
        }

        GenericTseApplicationType application = new GenericTseApplicationType();

        application.setDate(UtilHelper.formatCurrentDate(LocalDate.now()));
        application.setDueDate(UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 7));
        application.setResponsesCount("0");
        application.setNumber(String.valueOf(getNextApplicationNumber(caseData)));
        application.setStatus(OPEN_STATE);

        if (isClaimant) {
            addClaimantData(caseData, application);
        } else {
            addRespondentData(caseData, application);
        }

        GenericTseApplicationTypeItem tseApplicationTypeItem = new GenericTseApplicationTypeItem();
        tseApplicationTypeItem.setId(UUID.randomUUID().toString());
        tseApplicationTypeItem.setValue(application);

        List<GenericTseApplicationTypeItem> tseApplicationCollection = caseData.getGenericTseApplicationCollection();
        tseApplicationCollection.add(tseApplicationTypeItem);

        // todo implement try catch for concurrent modification
        caseData.setGenericTseApplicationCollection(tseApplicationCollection);
    }

    private void addClaimantData(CaseData caseData, GenericTseApplicationType application) {
        application.setApplicant(CLAIMANT_TITLE);

        ClaimantTse claimantTse = caseData.getClaimantTse();
        application.setType(ClaimantTse.APP_TYPE_MAP.get(claimantTse.getContactApplicationType()));
        application.setDetails(claimantTse.getContactApplicationText());
        application.setDocumentUpload(claimantTse.getContactApplicationFile());
        application.setCopyToOtherPartyYesOrNo(claimantTse.getCopyToOtherPartyYesOrNo());
        application.setCopyToOtherPartyText(claimantTse.getCopyToOtherPartyText());
        application.setApplicationState(IN_PROGRESS);

        caseData.setClaimantTse(null);
    }

    private void addRespondentData(CaseData caseData, GenericTseApplicationType application) {
        application.setApplicant(RESPONDENT_TITLE);
        assignDataToFieldsFromApplicationType(application, caseData);
        application.setType(caseData.getResTseSelectApplication());
        application.setCopyToOtherPartyYesOrNo(caseData.getResTseCopyToOtherPartyYesOrNo());
        application.setCopyToOtherPartyText(caseData.getResTseCopyToOtherPartyTextArea());
        application.setApplicationState(NOT_STARTED_YET);
        addSupportingMaterialToDocumentCollection(caseData, application);

        clearRespondentTseDataFromCaseData(caseData);
    }

    private void addSupportingMaterialToDocumentCollection(CaseData caseData, GenericTseApplicationType application) {
        if (application.getDocumentUpload() != null) {
            DocumentTypeItem documentTypeItem = DocumentTypeItem.fromUploadedDocument(application.getDocumentUpload());
            documentTypeItem.getValue().setTypeOfDocument("Respondent correspondence");
            documentTypeItem.getValue().setShortDescription("Application supporting material");

            if (isEmpty(caseData.getDocumentCollection())) {
                caseData.setDocumentCollection(new ArrayList<>());
            }
            caseData.getDocumentCollection().add(documentTypeItem);
        }
    }

    private void assignDataToFieldsFromApplicationType(GenericTseApplicationType respondentTseType, CaseData caseData) {
        RespondentTSEApplicationTypeData selectedAppData =
                RespondentTellSomethingElseHelper.getSelectedApplicationType(caseData);
        respondentTseType.setDetails(selectedAppData.getSelectedTextBox());
        respondentTseType.setDocumentUpload(selectedAppData.getResTseDocument());
    }

    private void clearRespondentTseDataFromCaseData(CaseData caseData) {
        caseData.setResTseSelectApplication(null);
        caseData.setResTseCopyToOtherPartyYesOrNo(null);
        caseData.setResTseCopyToOtherPartyTextArea(null);

        caseData.setResTseTextBox1(null);
        caseData.setResTseTextBox2(null);
        caseData.setResTseTextBox3(null);
        caseData.setResTseTextBox4(null);
        caseData.setResTseTextBox5(null);
        caseData.setResTseTextBox6(null);
        caseData.setResTseTextBox7(null);
        caseData.setResTseTextBox8(null);
        caseData.setResTseTextBox9(null);
        caseData.setResTseTextBox10(null);
        caseData.setResTseTextBox11(null);
        caseData.setResTseTextBox12(null);

        caseData.setResTseDocument1(null);
        caseData.setResTseDocument2(null);
        caseData.setResTseDocument3(null);
        caseData.setResTseDocument4(null);
        caseData.setResTseDocument5(null);
        caseData.setResTseDocument6(null);
        caseData.setResTseDocument7(null);
        caseData.setResTseDocument8(null);
        caseData.setResTseDocument9(null);
        caseData.setResTseDocument10(null);
        caseData.setResTseDocument11(null);
        caseData.setResTseDocument12(null);
    }

    /**
     * Gets the number a new TSE application should be labelled as.
     *
     * @param caseData contains all the case data
     */
    private static int getNextApplicationNumber(CaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getGenericTseApplicationCollection())) {
            return 1;
        }
        return caseData.getGenericTseApplicationCollection().size() + 1;
    }

    public String getDocNameAndSizeLinks(List<GenericTypeItem<DocumentType>> documents, String authToken) {
        if (CollectionUtils.isEmpty(documents)) {
            return "";
        }

        return documents.stream()
                .map(GenericTypeItem::getValue)
                .map(o -> formatDocumentForTwoColumnTable(o, authToken))
                .collect(Collectors.joining(""));
    }

    public String formatDocumentForTwoColumnTable(DocumentType document, String authToken) {
        UploadedDocumentType uploadedDocument = document.getUploadedDocument();
        String nameTypeSizeLink = documentManagementService.displayDocNameTypeSizeLink(uploadedDocument, authToken);

        return MarkdownHelper.createTwoColumnRows(List.of(
                new String[]{"Document", nameTypeSizeLink},
                new String[]{"Description", document.getShortDescription()})
        );
    }
    public List<String[]> addDocumentRow(DocumentType document, String authToken) {
        UploadedDocumentType uploadedDocument = document.getUploadedDocument();
        String nameTypeSizeLink = documentManagementService.displayDocNameTypeSizeLink(uploadedDocument, authToken);

        return List.of(
                new String[]{"Document", nameTypeSizeLink},
                new String[]{"Description", document.getShortDescription()}
        );
    }
    public List<String[]> addDocumentRows(List<GenericTypeItem<DocumentType>> documents, String authToken) {
        if (CollectionUtils.isEmpty(documents)) {
            return Collections.emptyList();
        }

        return documents.stream()
                .flatMap(o -> addDocumentRow(o.getValue(), authToken).stream())
                .collect(Collectors.toList());
    }

    /**
     * Format Admin response markup.
     *
     * @param reply Respond as TseRespondType
     * @param respondCount Respond count as incrementAndReturnValue()
     * @param authToken authorisation token for caller
     * @return Markup String
     */
    public String formatAdminReply(TseRespondType reply, int respondCount, String authToken) {
        List<String[]> rows = new ArrayList<>();

        rows.addAll(List.of(
                new String[]{"Response", reply.getEnterResponseTitle()},
                new String[]{"Date", reply.getDate()},
                new String[]{"Sent by", "Tribunal"},
                new String[]{"Case management order or request?", reply.getIsCmoOrRequest()},
                new String[]{"Is a response required?", reply.getIsResponseRequired()},
                new String[]{"Party or parties to respond", reply.getSelectPartyRespond()},
                new String[]{"Additional information", reply.getAdditionalInformation()}
        ));
        rows.addAll(addDocumentRows(reply.getAddDocument(), authToken));
        rows.addAll(List.of(
                new String[]{"Case management order made by", reply.getCmoMadeBy()},
                new String[]{"Request made by", reply.getRequestMadeBy()},
                new String[]{"Full name", reply.getMadeByFullName()},
                new String[]{"Sent to", reply.getSelectPartyNotify()}
        ));

        return MarkdownHelper.createTwoColumnTable(new String[] {"Response " + respondCount, ""}, rows) + "\r\n";
    }

    public String formatApplicationResponses(GenericTseApplicationType application, String authToken) {
        List<TseRespondTypeItem> respondCollection = application.getRespondCollection();

        if (CollectionUtils.isEmpty(respondCollection)) {
            return "";
        }

        IntWrapper respondCount = new IntWrapper(0);
        String applicant = application.getApplicant().toLowerCase(Locale.ENGLISH);

        return application.getRespondCollection().stream()
                .map(TseRespondTypeItem::getValue)
                .map(o -> ADMIN.equals(o.getFrom())
                        ? formatAdminReply(o, respondCount.incrementAndReturnValue(), authToken)
                        : formatReply(o, respondCount.incrementAndReturnValue(), applicant, authToken)
                ).collect(Collectors.joining());
    }

    private String formatReply(TseRespondType reply, int count, String applicant, String authToken) {
        List<String[]> rows = new ArrayList<>();

        rows.addAll(List.of(
                new String[]{"Response from", reply.getFrom()},
                new String[]{"Response date", reply.getDate()},
                new String[]{String.format(WHATS_YOUR_RESPONSE, applicant), reply.getResponse()}
        ));
        rows.addAll(addDocumentRows(reply.getSupportingMaterial(), authToken));
        rows.addAll(List.of(
                new String[]{RULE92_QUESTION, reply.getCopyToOtherParty()},
                new String[]{RULE92_DETAILS, reply.getCopyNoGiveDetails()}
        ));

        return MarkdownHelper.createTwoColumnTable(new String[] {"Response " + count, ""}, rows) + "\r\n";
    }

    public String formatViewApplication(CaseData caseData, String authToken) {
        List<GenericTseApplicationTypeItem> applications = caseData.getGenericTseApplicationCollection();
        GenericTseApplicationType application = TseHelper.getSelectedApplication(caseData);

        if (CollectionUtils.isEmpty(applications) || application == null) {
            return "";
        }

        UploadedDocumentType document = application.getDocumentUpload();
        String supportingMaterial = documentManagementService.displayDocNameTypeSizeLink(document, authToken);

        String applicationTable = MarkdownHelper.createTwoColumnTable(new String[]{"Application", ""}, List.of(
                new String[]{"Applicant", application.getApplicant()},
                new String[]{"Type of application", application.getType()},
                new String[]{"Application date", application.getDate()},
                new String[]{"What do you want to tell or ask the tribunal?", application.getDetails()},
                new String[]{"Supporting material", supportingMaterial},
                new String[]{RULE92_QUESTION, application.getCopyToOtherPartyYesOrNo()},
                new String[]{RULE92_DETAILS, application.getCopyToOtherPartyText()}
        ));

        String responses = formatApplicationResponses(application, authToken);

        return applicationTable + "\r\n" + responses;
    }
}
