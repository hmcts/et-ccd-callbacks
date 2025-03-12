package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.TseAdminRecordDecisionTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.TseRespondTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentTse;
import uk.gov.hmcts.et.common.model.ccd.types.TseAdminRecordDecisionType;
import uk.gov.hmcts.et.common.model.ccd.types.TseRespondType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.citizenhub.ClaimantTse;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.DocumentHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MarkdownHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.RespondentTellSomethingElseHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.TseHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.IntWrapper;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.TSEApplicationTypeData;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Stream;

import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ADMIN;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.IN_PROGRESS;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NOT_STARTED_YET;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.OPEN_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TRIBUNAL;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants.CLAIMANT_REP_TITLE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants.RESPONDENT_REP_TITLE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.ClaimantTellSomethingElseHelper.claimantSelectApplicationToType;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.MarkdownHelper.createTwoColumnTable;

@Slf4j
@Service
@RequiredArgsConstructor
public class TseService {
    public static final String WHATS_YOUR_RESPONSE = "What's your response to the %s's application";

    static final String[] MD_TABLE_EMPTY_LINE = {"", ""};

    private static final String RULE92_QUESTION =
            "Do you want to copy this correspondence to the other party to satisfy the Rules of Procedure?";
    private static final String RULE92_DETAILS =
            "Details of why you do not want to inform the other party";

    private final DocumentManagementService documentManagementService;

    /**
     * Creates a new TSE collection if it doesn't exist.
     * Create a new application in the list and assign the TSE data from CaseData to it.
     * At last, clears the existing TSE data from CaseData to ensure fields will be empty when user
     * starts a new application in the same case.
     *
     * @param caseData   contains all the case data.
     * @param userType create an application for the claimant, respondent or claimant representative
     */

    public void createApplication(CaseData caseData, String userType) {
        if (isEmpty(caseData.getGenericTseApplicationCollection())) {
            caseData.setGenericTseApplicationCollection(new ArrayList<>());
        }

        GenericTseApplicationType application = new GenericTseApplicationType();

        application.setDate(UtilHelper.formatCurrentDate(LocalDate.now()));
        application.setDueDate(UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 7));
        application.setResponsesCount("0");
        application.setNumber(String.valueOf(getNextApplicationNumber(caseData)));
        log.info("RespondentTse RespondentIdamId: {}", caseData.getRespondentTse().getRespondentIdamId());
        application.setApplicantIdamId(caseData.getRespondentTse().getRespondentIdamId());
        application.setStatus(OPEN_STATE);

        switch (userType) {
            case CLAIMANT_TITLE:
                addClaimantData(caseData, application);
                break;
            case RESPONDENT_TITLE:
                addRespondentData(caseData, application);
                break;
            case RESPONDENT_REP_TITLE:
                addRespondentRepData(caseData, application);
                break;
            case CLAIMANT_REP_TITLE:
                addClaimantRepresentativeData(caseData, application);
                break;
            default:
                throw new IllegalArgumentException("Unexpected user type: " + userType);
        }

        GenericTseApplicationTypeItem tseApplicationTypeItem = new GenericTseApplicationTypeItem();
        tseApplicationTypeItem.setId(UUID.randomUUID().toString());
        tseApplicationTypeItem.setValue(application);

        List<GenericTseApplicationTypeItem> tseApplicationCollection = caseData.getGenericTseApplicationCollection();
        tseApplicationCollection.add(tseApplicationTypeItem);

        // todo implement try catch for concurrent modification
        caseData.setGenericTseApplicationCollection(tseApplicationCollection);
    }

    /**
     * Remove item from TseApplicationStoredCollection.
     *
     * @param caseData contains all the case data.
     */
    public void removeStoredApplication(CaseData caseData) {
        String applicationId = caseData.getClaimantTse().getStoredApplicationId();
        if (caseData.getTseApplicationStoredCollection() != null && applicationId != null) {
            caseData.getTseApplicationStoredCollection().removeIf(item -> item.getId().equals(applicationId));
        }
    }

    /**
     * Clears the existing TSE data from CaseData to ensure fields will be empty when user
     * starts a new application in the same case.
     *
     * @param caseData contains all the case data.
     */
    public void clearApplicationData(CaseData caseData) {
        clearClaimantTseDataFromCaseData(caseData);
        clearRespondentTseDataFromCaseData(caseData);
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
    }

    private void addRespondentData(CaseData caseData, GenericTseApplicationType application) {
        application.setApplicant(RESPONDENT_TITLE);

        RespondentTse respondentTse = caseData.getRespondentTse();
        application.setType(respondentTse.getContactApplicationType());
        application.setDetails(respondentTse.getContactApplicationText());
        application.setDocumentUpload(respondentTse.getContactApplicationFile());
        application.setCopyToOtherPartyYesOrNo(respondentTse.getCopyToOtherPartyYesOrNo());
        application.setCopyToOtherPartyText(respondentTse.getCopyToOtherPartyText());
        application.setApplicationState(IN_PROGRESS);
    }

    private void addClaimantRepresentativeData(CaseData caseData, GenericTseApplicationType application) {
        addClaimantData(caseData, application);
        application.setApplicant(CLAIMANT_REP_TITLE);
        application.setType(caseData.getClaimantTseSelectApplication());
        addSupportingMaterialToDocumentCollection(caseData, application, true);
    }

    private void addRespondentRepData(CaseData caseData, GenericTseApplicationType application) {
        application.setApplicant(RESPONDENT_REP_TITLE);
        assignDataToFieldsFromApplicationType(application, caseData);
        application.setType(caseData.getResTseSelectApplication());
        application.setCopyToOtherPartyYesOrNo(caseData.getResTseCopyToOtherPartyYesOrNo());
        application.setCopyToOtherPartyText(caseData.getResTseCopyToOtherPartyTextArea());
        application.setApplicationState(NOT_STARTED_YET);
        addSupportingMaterialToDocumentCollection(caseData, application, false);
    }

    private void addSupportingMaterialToDocumentCollection(CaseData caseData, GenericTseApplicationType application,
                                                           boolean isClaimantRep) {
        if (application.getDocumentUpload() != null) {
            if (isEmpty(caseData.getDocumentCollection())) {
                caseData.setDocumentCollection(new ArrayList<>());
            }
            String applicationDoc;
            if (isClaimantRep) {
                String selectApplicationType =
                        claimantSelectApplicationToType(caseData.getClaimantTseSelectApplication());
                applicationDoc = uk.gov.hmcts.ecm.common.helpers.DocumentHelper.claimantApplicationTypeToDocType(
                        selectApplicationType);
            } else {
                applicationDoc = uk.gov.hmcts.ecm.common.helpers.DocumentHelper.respondentApplicationToDocType(
                        application.getType());

            }
            String topLevel = uk.gov.hmcts.ecm.common.helpers.DocumentHelper.getTopLevelDocument(applicationDoc);
            String extension = FilenameUtils.getExtension(application.getDocumentUpload().getDocumentFilename());
            application.getDocumentUpload().setDocumentFilename("Application %s - %s - Attachment.%s".formatted(
                    application.getNumber(),
                    applicationDoc,
                    extension
            ));

            caseData.getDocumentCollection().add(DocumentHelper.createDocumentTypeItemFromTopLevel(
                    application.getDocumentUpload(), topLevel, applicationDoc, application.getType()
            ));

        }
    }

    private void assignDataToFieldsFromApplicationType(GenericTseApplicationType respondentTseType, CaseData caseData) {
        TSEApplicationTypeData selectedAppData =
                RespondentTellSomethingElseHelper.getSelectedApplicationType(caseData);
        respondentTseType.setDetails(selectedAppData.getSelectedTextBox());
        respondentTseType.setDocumentUpload(selectedAppData.getUploadedTseDocument());
    }

    private void clearRespondentTseDataFromCaseData(CaseData caseData) {
        caseData.setRespondentTse(null);
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

    private void clearClaimantTseDataFromCaseData(CaseData caseData) {
        caseData.setClaimantTse(null);
        caseData.setClaimantTseSelectApplication(null);
        caseData.setClaimantTseRule92(null);
        caseData.setClaimantTseRule92AnsNoGiveDetails(null);
        caseData.setClaimantTseRespNotAvailable(null);

        caseData.setClaimantTseTextBox1(null);
        caseData.setClaimantTseTextBox2(null);
        caseData.setClaimantTseTextBox3(null);
        caseData.setClaimantTseTextBox4(null);
        caseData.setClaimantTseTextBox5(null);
        caseData.setClaimantTseTextBox6(null);
        caseData.setClaimantTseTextBox7(null);
        caseData.setClaimantTseTextBox8(null);
        caseData.setClaimantTseTextBox9(null);
        caseData.setClaimantTseTextBox10(null);
        caseData.setClaimantTseTextBox11(null);
        caseData.setClaimantTseTextBox12(null);
        caseData.setClaimantTseTextBox13(null);

        caseData.setClaimantTseDocument1(null);
        caseData.setClaimantTseDocument2(null);
        caseData.setClaimantTseDocument3(null);
        caseData.setClaimantTseDocument4(null);
        caseData.setClaimantTseDocument5(null);
        caseData.setClaimantTseDocument6(null);
        caseData.setClaimantTseDocument7(null);
        caseData.setClaimantTseDocument8(null);
        caseData.setClaimantTseDocument9(null);
        caseData.setClaimantTseDocument10(null);
        caseData.setClaimantTseDocument11(null);
        caseData.setClaimantTseDocument12(null);
        caseData.setClaimantTseDocument13(null);
    }

    /**
     * Gets the number a new TSE application should be labelled as.
     *
     * @param caseData contains all the case data
     */
    public static int getNextApplicationNumber(CaseData caseData) {
        if (isEmpty(caseData.getGenericTseApplicationCollection())) {
            return 1;
        }
        return caseData.getGenericTseApplicationCollection().size() + 1;
    }

    /**
     * Builds a two column Markdown table with both application details and all responses.
     * @param caseData parent object for all case data
     * @param authToken user token for getting document metadata
     * @param isRespondentView is respondent or their representatives viewing this application
     * @return two column Markdown table string
     */
    public String formatViewApplication(CaseData caseData, String authToken, boolean isRespondentView) {
        GenericTseApplicationType application;
        if (caseData.getTseAdminSelectApplication() != null) {
            application = TseHelper.getAdminSelectedApplicationType(caseData);
        } else if (caseData.getTseViewApplicationSelect() != null) {
            application = TseHelper.getViewSelectedApplicationType(caseData);
        } else {
            throw new IllegalStateException("Selected application is null");
        }

        List<String[]> applicationTable = getApplicationDetailsRows(application, authToken, true);
        List<String[]> responses = formatApplicationResponses(application, authToken, isRespondentView);
        List<String[]> decisions = formatApplicationDecisions(application, authToken);

        return createTwoColumnTable(new String[]{"Application", ""},
            Stream.of(applicationTable, responses, decisions).flatMap(Collection::stream).toList());
    }

    List<String[]> getApplicationDetailsRows(GenericTseApplicationType application, String authToken, boolean rule92) {
        UploadedDocumentType document = application.getDocumentUpload();
        String supportingMaterial = documentManagementService.displayDocNameTypeSizeLink(document, authToken);

        List<String[]> rows = new ArrayList<>(List.of(
                new String[]{"Applicant", application.getApplicant()},
                new String[]{"Type of application", application.getType()},
                new String[]{"Application date", application.getDate()},
                new String[]{"What do you want to tell or ask the tribunal?", application.getDetails()},
                new String[]{"Supporting material", supportingMaterial}
        ));

        if (rule92) {
            rows.add(new String[]{RULE92_QUESTION, application.getCopyToOtherPartyYesOrNo()});
            rows.add(new String[]{RULE92_DETAILS, application.getCopyToOtherPartyText()});
        }

        return rows;
    }

    /**
     * Formats all responses for an application into a two column Markdown table.
     * @param application the application that owns the responses
     * @param authToken user token for getting document metadata
     * @param isRespondentView determines if it is the Respondent viewing the responses
     * @return Two column Markdown table string of all responses
     */
    public List<String[]> formatApplicationResponses(GenericTseApplicationType application, String authToken,
                                             boolean isRespondentView) {
        List<TseRespondTypeItem> respondCollection = application.getRespondCollection();
        if (isEmpty(respondCollection)) {
            return Collections.emptyList();
        }

        IntWrapper respondCount = new IntWrapper(0);
        String applicant = application.getApplicant().toLowerCase(Locale.ENGLISH);

        return respondCollection.stream()
                .map(TseRespondTypeItem::getValue)
                .map(o -> ADMIN.equals(o.getFrom())
                        ? formatAdminReply(o, respondCount.incrementAndReturnValue(), authToken)
                        : formatNonAdminReply(o, respondCount.incrementAndReturnValue(), applicant,
                            authToken, isRespondentView)
                ).flatMap(Collection::stream)
            .toList();
    }

    private List<String[]> getSingleDecisionMarkdown(TseAdminRecordDecisionType decision, String authToken) {
        List<String[]> rows = new ArrayList<>(List.of(
            MD_TABLE_EMPTY_LINE,
            MD_TABLE_EMPTY_LINE,
            new String[]{"Decision", ""},
            new String[]{"Notification", decision.getEnterNotificationTitle()},
            new String[]{"Decision", decision.getDecision()},
            new String[]{"Decision details", decision.getDecisionDetails()},
            new String[]{"Date", decision.getDate()},
            new String[]{"Sent by", TRIBUNAL},
            new String[]{"Type of decision", decision.getTypeOfDecision()}
        ));
        rows.addAll(addDocumentsRows(decision.getResponseRequiredDoc(), authToken));
        rows.addAll(List.of(
            new String[]{"Additional information", decision.getAdditionalInformation()},
            new String[]{"Decision made by", decision.getDecisionMadeBy()},
            new String[]{"Name", decision.getDecisionMadeByFullName()},
            new String[]{"Sent to", decision.getSelectPartyNotify()}
        ));

        return rows;
    }

    private List<String[]> formatApplicationDecisions(GenericTseApplicationType application, String authToken) {
        List<TseAdminRecordDecisionTypeItem> adminDecision = application.getAdminDecision();
        if (adminDecision == null) {
            return Collections.emptyList();
        }

        return adminDecision.stream()
            .sorted(Comparator.comparing((TseAdminRecordDecisionTypeItem d) -> d.getValue().getDate()).reversed())
            .limit(2)
            .map(d -> getSingleDecisionMarkdown(d.getValue(), authToken))
            .flatMap(Collection::stream)
            .toList();
    }

    /**
     * Formats an admin response into a two column Markdown table.
     *
     * @param reply the admin response to format
     * @param count an arbitrary number representing the position of this response
     * @param authToken user token for getting document metadata
     * @return Two columned Markdown table detailing the admin response
     */
    List<String[]> formatAdminReply(TseRespondType reply, int count, String authToken) {
        List<String[]> rows = new ArrayList<>(List.of(
                MD_TABLE_EMPTY_LINE,
                MD_TABLE_EMPTY_LINE,
                new String[]{"Response " + count, ""},
                new String[]{"Response", reply.getEnterResponseTitle()},
                new String[]{"Date", reply.getDate()},
                new String[]{"Sent by", "Tribunal"},
                new String[]{"Case management order or request?", reply.getIsCmoOrRequest()},
                new String[]{"Is a response required?", reply.getIsResponseRequired()},
                new String[]{"Party or parties to respond", reply.getSelectPartyRespond()},
                new String[]{"Additional information", reply.getAdditionalInformation()}
        ));
        rows.addAll(addDocumentsRows(reply.getAddDocument(), authToken));
        rows.addAll(List.of(
                new String[]{"Case management order made by", reply.getCmoMadeBy()},
                new String[]{"Request made by", reply.getRequestMadeBy()},
                new String[]{"Full name", reply.getMadeByFullName()},
                new String[]{"Sent to", reply.getSelectPartyNotify()}
        ));

        return rows;
    }

    /**
     * Formats an admin response into a two column Markdown table.
     * If the responses are being viewed by Respondent then they will be filtered by Rule92
     * @param reply the admin response to format
     * @param count an arbitrary number representing the position of this response
     * @param authToken user token for getting document metadata
     * @param isRespondentView determines if it is the Respondent viewing the responses
     * @return Two columned Markdown table detailing the admin response
     */
    private List<String[]> formatNonAdminReply(TseRespondType reply, int count, String applicant, String authToken,
                                       boolean isRespondentView) {
        String from = reply.getFrom();
        String copyToOtherParty = reply.getCopyToOtherParty();
        if (isRespondentView && CLAIMANT_TITLE.equals(from) && NO.equals(copyToOtherParty)) {
            return Collections.emptyList();
        }

        List<String[]> rows = new ArrayList<>(List.of(
                MD_TABLE_EMPTY_LINE,
                MD_TABLE_EMPTY_LINE,
                new String[]{"Response " + count, ""},
                new String[]{"Response from", from},
                new String[]{"Response date", reply.getDate()},
                new String[]{String.format(WHATS_YOUR_RESPONSE, applicant), reply.getResponse()}
        ));
        rows.addAll(addDocumentsRows(reply.getSupportingMaterial(), authToken));
        rows.addAll(List.of(
                new String[]{RULE92_QUESTION, copyToOtherParty},
                new String[]{RULE92_DETAILS, reply.getCopyNoGiveDetails()}
        ));

        return rows;
    }

    /**
     * Returns a list of rows for multiple documents for use in a two columned Markdown table.
     * @return A list of String arrays, one string array for each document's name and another for the short description
     */
    List<String[]> addDocumentsRows(List<GenericTypeItem<DocumentType>> documents, String authToken) {
        if (isEmpty(documents)) {
            return Collections.emptyList();
        }
        return documents.stream().flatMap(o -> addDocumentRow(o.getValue(), authToken).stream()).toList();
    }

    /**
     * Returns two rows of two columns for a document representing its name and description.
     * @param document Document data
     * @param authToken user token for getting document metadata
     * @return A list of String arrays representing the two columned rows
     */
    List<String[]> addDocumentRow(DocumentType document, String authToken) {
        UploadedDocumentType uploadedDocument = document.getUploadedDocument();
        String nameTypeSizeLink = documentManagementService.displayDocNameTypeSizeLink(uploadedDocument, authToken);
        return MarkdownHelper.addRowsForDocument(document, nameTypeSizeLink);
    }

    /**
     * Returns a formatted document name for a TSE application.
     * @param caseData parent object for all case data
     * @return formatted document name
     */
    public String getTseDocumentName(CaseData caseData) {
        return String.format("Application %d - %s.pdf",
                getNextApplicationNumber(caseData) - 1,
                caseData.getResTseSelectApplication());
    }

    public String getClaimantTseDocumentName(CaseData caseData) {
        return String.format("Application %d - %s.pdf",
                getNextApplicationNumber(caseData) - 1,
                caseData.getClaimantTseSelectApplication());
    }
}
