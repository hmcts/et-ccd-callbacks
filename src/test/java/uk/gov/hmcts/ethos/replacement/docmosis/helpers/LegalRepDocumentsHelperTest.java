package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.TseRespondTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.TseRespondType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.APP_FOR_A_WITNESS_ORDER_C;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.APP_TO_AMEND_RESPONSE;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.APP_TO_VARY_OR_REVOKE_AN_ORDER_C;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.CLAIMANT_CORRESPONDENCE;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.CLAIM_REJECTED;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.CONTACT_THE_TRIBUNAL_C;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.COT3;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.ET1;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.ET1_VETTING;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.ET3;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.ET3_PROCESSING;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.INITIAL_CONSIDERATION;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.OTHER;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.REFERRAL_JUDICIAL_DIRECTION;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.REJECTION_OF_CLAIM;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.TRIBUNAL_CASE_FILE;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.WITHDRAWAL_OF_ALL_OR_PART_CLAIM;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.DocumentFixtures.getDocumentTypeItem;

class LegalRepDocumentsHelperTest {

    private CaseData caseData;

    @BeforeEach
    void setUp() {
        caseData = new CaseData();
    }

    @Test
    void setLegalRepVisibleDocuments() {
        caseData.setDocumentCollection(List.of(
            getDocumentTypeItem(ET1, ET1),
            getDocumentTypeItem(ET3, ET3),
            getDocumentTypeItem("VisibleDoc", null),
            // Hidden documents
            getDocumentTypeItem(ET1_VETTING, ET1_VETTING),
            getDocumentTypeItem(ET3_PROCESSING, ET3_PROCESSING),
            getDocumentTypeItem(INITIAL_CONSIDERATION, INITIAL_CONSIDERATION),
            getDocumentTypeItem(APP_FOR_A_WITNESS_ORDER_C, APP_FOR_A_WITNESS_ORDER_C),
            getDocumentTypeItem(REFERRAL_JUDICIAL_DIRECTION, REFERRAL_JUDICIAL_DIRECTION),
            getDocumentTypeItem(COT3, COT3),
            getDocumentTypeItem(OTHER, OTHER),
            getDocumentTypeItem(CONTACT_THE_TRIBUNAL_C, CONTACT_THE_TRIBUNAL_C),
            getDocumentTypeItem(TRIBUNAL_CASE_FILE, TRIBUNAL_CASE_FILE),
            getDocumentTypeItem(REJECTION_OF_CLAIM, REJECTION_OF_CLAIM),
            getDocumentTypeItem(CLAIM_REJECTED, CLAIM_REJECTED),
            getDocumentTypeItem(APP_TO_AMEND_RESPONSE, APP_TO_AMEND_RESPONSE),
            getDocumentTypeItem(APP_TO_VARY_OR_REVOKE_AN_ORDER_C, APP_TO_VARY_OR_REVOKE_AN_ORDER_C),
            getDocumentTypeItem(WITHDRAWAL_OF_ALL_OR_PART_CLAIM, WITHDRAWAL_OF_ALL_OR_PART_CLAIM)
            )
        );

        LegalRepDocumentsHelper.setLegalRepVisibleDocuments(caseData);

        assertNotNull(caseData.getLegalRepDocumentsMarkdown());
        assertTrue(caseData.getLegalRepDocumentsMarkdown().contains("|ET1|"));
        assertTrue(caseData.getLegalRepDocumentsMarkdown().contains("|ET3|"));
        assertFalse(caseData.getLegalRepDocumentsMarkdown().contains("|4|"));
        assertFalse(caseData.getLegalRepDocumentsMarkdown().contains("|ET1 Vetting|"));
        assertFalse(caseData.getLegalRepDocumentsMarkdown().contains("|ET3 Processing|"));
    }

    @Test
    void hidesRule92NoDocuments() {
        DocumentTypeItem doc1 = getDocumentTypeItem("Application 1 - Doc 1", CLAIMANT_CORRESPONDENCE);
        DocumentTypeItem doc2 = getDocumentTypeItem("Application 2 - Doc 1", CLAIMANT_CORRESPONDENCE);
        DocumentTypeItem doc3 = getDocumentTypeItem("Application 2 - Doc 2", CLAIMANT_CORRESPONDENCE);
        caseData.setDocumentCollection(List.of(doc1, doc2, doc3));

        GenericTseApplicationTypeItem claimantApp = GenericTseApplicationTypeItem.builder()
            .value(GenericTseApplicationType.builder()
                .applicant(CLAIMANT_TITLE)
                .copyToOtherPartyYesOrNo(NO)
                .documentUpload(doc1.getValue().getUploadedDocument())
                .build())
            .build();

        List<TseRespondTypeItem> tseRespondTypeItems = List.of(
            TseRespondTypeItem.builder()
                .value(TseRespondType.builder()
                    .from(CLAIMANT_TITLE)
                    .copyToOtherParty(NO)
                    .supportingMaterial(List.of(doc2, doc3))
                    .build())
            .build()
        );

        GenericTseApplicationTypeItem claimantResponses = GenericTseApplicationTypeItem.builder()
            .value(GenericTseApplicationType.builder()
                .documentUpload(doc1.getValue().getUploadedDocument())
                .respondCollection(tseRespondTypeItems)
                .build())
            .build();

        caseData.setGenericTseApplicationCollection(List.of(claimantApp, claimantResponses));

        LegalRepDocumentsHelper.setLegalRepVisibleDocuments(caseData);

        assertNull(caseData.getLegalRepDocumentsMarkdown());
    }
}
