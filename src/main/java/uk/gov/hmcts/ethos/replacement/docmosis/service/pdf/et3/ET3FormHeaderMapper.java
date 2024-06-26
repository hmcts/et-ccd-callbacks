package uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3;

import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;

import java.util.Optional;
import java.util.concurrent.ConcurrentMap;

import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.DATE_FORMAT_DD_MM_YYYY_DASH;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.DATE_FORMAT_YYYY_MM_DD_DASH;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_HEADER_FIELD_CASE_NUMBER;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_HEADER_FIELD_DATE_RECEIVED;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_HEADER_FIELD_RTF;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_HEADER_VALUE_ADDITIONAL_DOCUMENT_EXISTS;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_HEADER_VALUE_ADDITIONAL_DOCUMENT_NOT_EXISTS;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.util.ET3FormUtil.formatDate;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.util.ET3FormUtil.putPdfTextField;

public final class ET3FormHeaderMapper {

    private ET3FormHeaderMapper() {
        // Add a private constructor to hide the implicit public one.
    }

    /**
     * Maps header values (case number, case received date, RFT) with PDF input fields.
     * @param caseData case data that is received by case number
     * @param respondentSumType respondent data selected by representative of respondent
     * @param pdfFields print fields that is created in ET3FormMapper
     */
    public static void mapHeader(CaseData caseData,
                                 RespondentSumType respondentSumType,
                                 ConcurrentMap<String, Optional<String>> pdfFields) {

        putPdfTextField(pdfFields, TXT_PDF_HEADER_FIELD_CASE_NUMBER, caseData.getEthosCaseReference());
        putPdfTextField(pdfFields, TXT_PDF_HEADER_FIELD_DATE_RECEIVED, formatDate(caseData.getReceiptDate(),
                DATE_FORMAT_YYYY_MM_DD_DASH, DATE_FORMAT_DD_MM_YYYY_DASH));
        putPdfTextField(pdfFields, TXT_PDF_HEADER_FIELD_RTF,
                isEmpty(respondentSumType.getEt3ResponseContestClaimDocument())
                && isEmpty(respondentSumType.getEt3ResponseEmployerClaimDocument())
                && isEmpty(respondentSumType.getEt3ResponseRespondentSupportDocument())
                ? TXT_PDF_HEADER_VALUE_ADDITIONAL_DOCUMENT_NOT_EXISTS
                : TXT_PDF_HEADER_VALUE_ADDITIONAL_DOCUMENT_EXISTS);
    }

}
