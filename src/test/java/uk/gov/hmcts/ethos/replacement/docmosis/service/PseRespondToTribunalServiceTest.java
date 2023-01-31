package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.PseResponseType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CaseDataBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_TITLE;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
class PseRespondToTribunalServiceTest {
    private PseRespondToTribunalService pseRespondToTribService;
    private CaseData caseData;

    private static final String RESPONSE = "Some Response";
    private static final String YES = "Yes";
    private static final String NO = "No";
    private static final String RULE92_YES = "I do want to copy";
    private static final String RULE92_NO = "I do not want to copy";

    private static final String RULE92_NO_DETAILS = "Rule 92 Reasons";

    private static final String EXPECTED_TABLE_MARKDOWN = "|Hearing, case management order or request | |\r\n"
        + "|--|--|\r\n"
        + "|Notification | [ToDo: Dependency on RET-2949]|\r\n"
        + "|Hearing | [ToDo: Dependency on RET-2949]|\r\n"
        + "|Date sent | [ToDo: Dependency on RET-2949]|\r\n"
        + "|Sent by | [ToDo: Dependency on RET-2949]|\r\n"
        + "|Case management order or request? | [ToDo: Dependency on RET-2949]|\r\n"
        + "|Response due | [ToDo: Dependency on RET-2949]|\r\n"
        + "|Party or parties to respond | [ToDo: Dependency on RET-2949]|\r\n"
        + "|Additional information | [ToDo: Dependency on RET-2949]|\r\n"
        + "|Description | [ToDo: Dependency on RET-2949]|\r\n"
        + "|Document | [ToDo: Dependency on RET-2949]|\r\n"
        + "|Case management order made by | [ToDo: Dependency on RET-2949]|\r\n"
        + "|Name | [ToDo: Dependency on RET-2949]|\r\n"
        + "|Sent to | [ToDo: Dependency on RET-2949]|\r\n"
        + "\r\n"
        + "\r\n"
        + "|Response [ToDo: Dependency on RET-2928] | |\r\n"
        + "|--|--|\r\n"
        + "|Response from | [ToDo: Dependency on RET-2928]|\r\n"
        + "|Response date | [ToDo: Dependency on RET-2928]|\r\n"
        + "|What's your response to the tribunal? | [ToDo: Dependency on RET-2928]|\r\n"
        + "|Supporting material | [ToDo: Dependency on RET-2928]|\r\n"
        + "|Do you want to copy this correspondence to the other party to satisfy the Rules of Procedure? "
        + "| [ToDo: Dependency on RET-2928]|\r\n"
        + "\r\n";

    @BeforeEach
    void setUp() {
        pseRespondToTribService = new PseRespondToTribunalService();

        caseData = CaseDataBuilder.builder().build();
    }

    @Test
    void initialOrdReqDetailsTableMarkUp_hasOrderRequests() {
        assertThat(pseRespondToTribService.initialOrdReqDetailsTableMarkUp(caseData), is(EXPECTED_TABLE_MARKDOWN));
    }

    @ParameterizedTest
    @MethodSource("inputList")
    void validateInput_CountErrors(String responseText, String supportingMaterial, int expectedErrorCount) {
        caseData.setPseRespondentOrdReqResponseText(responseText);
        caseData.setPseRespondentOrdReqHasSupportingMaterial(supportingMaterial);

        List<String> errors = pseRespondToTribService.validateInput(caseData);

        assertEquals(expectedErrorCount, errors.size());
    }

    private static Stream<Arguments> inputList() {
        return Stream.of(
            Arguments.of(null, null, 1),
            Arguments.of(null, NO, 1),
            Arguments.of(null, YES, 0),
            Arguments.of(RESPONSE, null, 0),
            Arguments.of(RESPONSE, NO, 0),
            Arguments.of(RESPONSE, YES, 0)
        );
    }

    @ParameterizedTest
    @MethodSource("createRespondentResponses")
    void addRespondentResponseToJON(String response, String hasSupportingMaterial,
                                                int supportingDocsSize, String copyOtherParty, String copyDetails) {
        caseData.setPseRespondentOrdReqResponseText(response);
        caseData.setPseRespondentOrdReqHasSupportingMaterial(hasSupportingMaterial);
        caseData.setPseRespondentOrdReqCopyToOtherParty(copyOtherParty);
        caseData.setPseRespondentOrdReqCopyNoGiveDetails(copyDetails);

        if (supportingDocsSize > 0) {
            List<DocumentTypeItem> supportingMaterials = new ArrayList<>();
            for (int i = 0; i < supportingDocsSize; i++) {
                supportingMaterials.add(createDocumentType(Integer.toString(i)));
            }
            caseData.setPseRespondentOrdReqUploadDocument(supportingMaterials);
        }

        pseRespondToTribService.addRespondentResponseToJON(caseData);

        PseResponseType savedResponse = caseData.getPseOrdReqResponses().get(0).getValue();

        assertEquals(RESPONDENT_TITLE, savedResponse.getFrom());
        assertEquals(response, savedResponse.getResponse());
        assertEquals(hasSupportingMaterial, savedResponse.getHasSupportingMaterial());
        assertEquals(copyOtherParty, savedResponse.getCopyToOtherParty());
        assertEquals(copyDetails, savedResponse.getCopyNoGiveDetails());

        if (supportingDocsSize > 0) {
            assertEquals(savedResponse.getSupportingMaterial().size(), supportingDocsSize);
        } else {
            assertNull(savedResponse.getSupportingMaterial());
        }
    }

    private static Stream<Arguments> createRespondentResponses() {
        return Stream.of(
            Arguments.of(RESPONSE, YES, 2, RULE92_YES, null),
            Arguments.of(RESPONSE, YES, 1, RULE92_NO, RULE92_NO_DETAILS),
            Arguments.of(RESPONSE, NO, 0, RULE92_YES, null),
            Arguments.of(RESPONSE, NO, 0, RULE92_NO, RULE92_NO_DETAILS)
        );
    }

    private DocumentTypeItem createDocumentType(String id) {
        DocumentType documentType = new DocumentType();
        documentType.setUploadedDocument(new UploadedDocumentType());
        documentType.getUploadedDocument().setDocumentBinaryUrl("binaryUrl/documents/");
        documentType.getUploadedDocument().setDocumentFilename("testFileName");

        DocumentTypeItem document = new DocumentTypeItem();
        document.setId(id);
        document.setValue(documentType);

        return document;
    }

    @Test
    void clearRespondentResponse() {
        DynamicFixedListType newType = new DynamicFixedListType("Hello World");
        caseData.setPseRespondentSelectOrderOrRequest(newType);
        caseData.setPseRespondentOrdReqTableMarkUp(EXPECTED_TABLE_MARKDOWN);
        caseData.setPseRespondentOrdReqResponseText(RESPONSE);
        caseData.setPseRespondentOrdReqHasSupportingMaterial(YES);
        caseData.setPseRespondentOrdReqUploadDocument(
            new ArrayList<>(Collections.singletonList(createDocumentType("documentId"))));
        caseData.setPseRespondentOrdReqCopyToOtherParty(RULE92_NO);
        caseData.setPseRespondentOrdReqCopyNoGiveDetails(RULE92_NO_DETAILS);

        pseRespondToTribService.clearRespondentResponse(caseData);

        assertNull(caseData.getPseRespondentSelectOrderOrRequest());
        assertNull(caseData.getPseRespondentOrdReqTableMarkUp());
        assertNull(caseData.getPseRespondentOrdReqResponseText());
        assertNull(caseData.getPseRespondentOrdReqHasSupportingMaterial());
        assertNull(caseData.getPseRespondentOrdReqUploadDocument());
        assertNull(caseData.getPseRespondentOrdReqCopyToOtherParty());
        assertNull(caseData.getPseRespondentOrdReqCopyNoGiveDetails());
    }
}
