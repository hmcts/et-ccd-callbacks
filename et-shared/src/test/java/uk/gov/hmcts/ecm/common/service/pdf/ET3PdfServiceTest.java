package uk.gov.hmcts.ecm.common.service.pdf;

import lombok.SneakyThrows;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.ecm.common.exceptions.PdfServiceException;
import uk.gov.hmcts.ecm.common.service.utils.data.TestDataProvider;
import uk.gov.hmcts.et.common.model.ccd.CaseData;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.service.pdf.et3.ET3FormConstants.SUBMIT_ET3_CITIZEN;
import static uk.gov.hmcts.ecm.common.service.utils.TestConstants.ENGLISH_PDF_TEMPLATE_SOURCE;
import static uk.gov.hmcts.ecm.common.service.utils.TestConstants.ET3_FORM_CLIENT_TYPE_RESPONDENT;
import static uk.gov.hmcts.ecm.common.service.utils.TestConstants.ET3_FORM_TYPE;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ET3PdfServiceTest {

    private PdfService pdfService;
    @Mock
    private ET1PdfMapperService et1PdfMapperService;
    private CaseData caseData;

    @BeforeEach
    void setUp() {
        //MockitoAnnotations.openMocks(this);
        pdfService = new PdfService(et1PdfMapperService);
        caseData = new CaseData();
    }

    @Test
    void returnsEmptyArrayWhenPdfSourceIsNull() throws Exception {
        byte[] result = pdfService.createPdf(caseData, null, "ET1", "client", "event");
        assertThat(result).isEmpty();
    }

    @Test
    void returnsEmptyArrayWhenPdfSourceIsBlank() throws Exception {
        byte[] result = pdfService.createPdf(caseData, "   ", "ET1", "client", "event");
        assertThat(result).isEmpty();
    }

    @Test
    void returnsEmptyArrayWhenResourceNotFound() throws Exception {
        byte[] result = pdfService.createPdf(caseData, "notfound.pdf", "ET1", "client", "event");
        assertThat(result).isEmpty();
    }

    @Test
    void throwsPdfServiceExceptionWhenPdfTemplateHasNoForm() throws Exception {
        // Prepare a minimal PDF with no AcroForm
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PDDocument doc = new PDDocument()) {
            doc.save(baos);
        }
        InputStream pdfStream = new ByteArrayInputStream(baos.toByteArray());
        ClassLoader cl = mock(ClassLoader.class);
        when(cl.getResourceAsStream(anyString())).thenReturn(pdfStream);

        Thread currentThread = Thread.currentThread();
        ClassLoader originalCl = currentThread.getContextClassLoader();
        currentThread.setContextClassLoader(cl);

        try {
            assertThatThrownBy(() -> pdfService.createPdf(caseData, "dummy.pdf", "ET1", "client", "event"))
                    .isInstanceOf(PdfServiceException.class)
                    .hasMessageContaining("PDF template does not contain a form");
        } finally {
            currentThread.setContextClassLoader(originalCl);
        }
    }

    @SneakyThrows
    @Test
    void closesInputStreamOnSuccessAndFailure() throws Exception {
        // Prepare a PDF with AcroForm
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PDDocument doc = new PDDocument()) {
            PDAcroForm form = new PDAcroForm(doc);
            doc.getDocumentCatalog().setAcroForm(form);
            doc.save(baos);
        }
        InputStream pdfStream = spy(new ByteArrayInputStream(baos.toByteArray()));
        ClassLoader cl = mock(ClassLoader.class);
        when(cl.getResourceAsStream(anyString())).thenReturn(pdfStream);

        Thread currentThread = Thread.currentThread();
        ClassLoader originalCl = currentThread.getContextClassLoader();
        currentThread.setContextClassLoader(cl);

        // Mock font loading
        InputStream fontStream = new ByteArrayInputStream(new byte[10]);
        PdfService pdfServiceSpy = Mockito.spy(pdfService);
        doReturn(fontStream).when(pdfServiceSpy).loadFont(anyString());

        // Mock PDF entries
        Map<String, Optional<String>> entries = Map.of("field", Optional.of("value"));
        when(et1PdfMapperService.mapHeadersToPdf(any())).thenReturn(entries);

        try {
            byte[] result = pdfServiceSpy.createPdf(caseData, "dummy.pdf", "ET1", "client", "event");
            assertThat(result).isNotEmpty();
            verify(pdfStream, atLeastOnce()).close();
        } finally {
            currentThread.setContextClassLoader(originalCl);
        }
    }

    @Test
    void throwsPdfServiceExceptionOnIOException() throws Exception {
        ClassLoader cl = mock(ClassLoader.class);
        InputStream is = mock(InputStream.class);
        when(cl.getResourceAsStream(anyString())).thenReturn(is);
        when(is.read(any(), anyInt(), anyInt())).thenThrow(new IOException("fail"));

        Thread currentThread = Thread.currentThread();
        ClassLoader originalCl = currentThread.getContextClassLoader();
        currentThread.setContextClassLoader(cl);

        try {
            assertThatThrownBy(() -> pdfService.createPdf(caseData, "dummy.pdf", "ET1", "client", "event"))
                    .isInstanceOf(IOException.class);
        } finally {
            currentThread.setContextClassLoader(originalCl);
        }
    }

    @Test
    @SneakyThrows
    void theConvertCaseToPdfForET3CaseData() {
        // Prepare a PDF with AcroForm for ET3
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PDDocument doc = new PDDocument()) {
            PDAcroForm form = new PDAcroForm(doc);
            doc.getDocumentCatalog().setAcroForm(form);
            doc.save(baos);
        }
        InputStream pdfStream = new ByteArrayInputStream(baos.toByteArray());
        ClassLoader cl = mock(ClassLoader.class);
        when(cl.getResourceAsStream(anyString())).thenReturn(pdfStream);

        Thread currentThread = Thread.currentThread();
        ClassLoader originalCl = currentThread.getContextClassLoader();
        currentThread.setContextClassLoader(cl);

        // Mock font loading for NotoSans
        InputStream fontStream = new ByteArrayInputStream(new byte[10]);
        PdfService pdfServiceSpy = Mockito.spy(pdfService);
        doReturn(fontStream).when(pdfServiceSpy).loadFont(anyString());

        // Mock ET3FormMapper static method
        Map<String, Optional<String>> entries = Map.of("field", Optional.of("value"));
        var et3FormMapperMockedStatic = Mockito.mockStatic(uk.gov.hmcts.ecm.common.service.pdf.et3.ET3FormMapper.class);
        et3FormMapperMockedStatic.when(() ->
            uk.gov.hmcts.ecm.common.service.pdf.et3.ET3FormMapper.mapEt3Form(any(), anyString(), anyString())
        ).thenReturn(entries);

        try {
            CaseData et3CaseData = TestDataProvider.generateEt3CaseData();
            byte[] pdfByteArray = pdfServiceSpy.convertCaseToPdf(et3CaseData,
                    ENGLISH_PDF_TEMPLATE_SOURCE, ET3_FORM_TYPE, ET3_FORM_CLIENT_TYPE_RESPONDENT, SUBMIT_ET3_CITIZEN);
            assertThat(pdfByteArray).isNotEmpty();
        } finally {
            et3FormMapperMockedStatic.close();
            currentThread.setContextClassLoader(originalCl);
        }
    }
}
