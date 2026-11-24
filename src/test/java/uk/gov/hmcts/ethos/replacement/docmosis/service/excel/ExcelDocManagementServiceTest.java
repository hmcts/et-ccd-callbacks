package uk.gov.hmcts.ethos.replacement.docmosis.service.excel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.multiples.CaseImporterFile;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.HelperTest;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultipleUtil;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultiplesHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultiplesScheduleHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DocumentManagementService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserIdamService;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.excel.ExcelDocManagementService.APPLICATION_EXCEL_VALUE;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class ExcelDocManagementServiceTest {

    @Mock
    private DocumentManagementService documentManagementService;
    @Mock
    private ExcelCreationService excelCreationService;
    @Mock
    private UserIdamService userIdamService;
    @Mock
    private ScheduleCreationService scheduleCreationService;
    @InjectMocks
    private ExcelDocManagementService excelDocManagementService;

    private MultipleDetails multipleDetails;
    private String userToken;
    private String caseTypeId;
    private byte[] bytes;

    @BeforeEach
    public void setUp() {
        multipleDetails = new MultipleDetails();
        multipleDetails.setCaseData(MultipleUtil.getMultipleData());
        UserDetails userDetails = HelperTest.getUserDetails();
        when(userIdamService.getUserDetails(anyString())).thenReturn(userDetails);
        userToken = "authString";
        bytes = "Bytes to return".getBytes();
        caseTypeId = multipleDetails.getCaseTypeId();
    }

    @Test
    void uploadExcelDocument() {
        URI uri = URI.create("http://google.com");
        when(documentManagementService.uploadDocument(userToken,
                bytes,
                MultiplesHelper.generateExcelDocumentName(multipleDetails.getCaseData()), APPLICATION_EXCEL_VALUE,
                multipleDetails.getCaseTypeId()))
                .thenReturn(uri);
        excelDocManagementService.uploadExcelDocument(userToken,
                multipleDetails,
                bytes);
        verify(documentManagementService, times(1)).uploadDocument(userToken,
                bytes,
                MultiplesHelper.generateExcelDocumentName(multipleDetails.getCaseData()), APPLICATION_EXCEL_VALUE,
                multipleDetails.getCaseTypeId());
        verifyNoMoreInteractions(documentManagementService);
    }

    @Test
    void downloadExcelDocument() throws IOException {
        String binaryUrl = "http://127.0.0.1:3453/documents/20d8a494-4232-480a-aac3-23ad0746c07b/binary";
        when(documentManagementService.downloadFile(userToken,
                binaryUrl))
                .thenReturn(MultipleUtil.getUploadedDocument());
        assertNotNull(excelDocManagementService.downloadExcelDocument(userToken, binaryUrl));
    }

    @Test
    void generateAndUploadExcel() {
        URI uri = URI.create("http://google.com");
        List<String> multipleCollection = new ArrayList<>(
                Arrays.asList("245000/2020", "245001/2020", "245002/2020"));
        List<String> subMultipleCollection = MultiplesHelper
                .generateSubMultipleStringCollection(multipleDetails.getCaseData());
        when(documentManagementService.uploadDocument(userToken,
                bytes,
                MultiplesHelper.generateExcelDocumentName(multipleDetails.getCaseData()),
                APPLICATION_EXCEL_VALUE, multipleDetails.getCaseTypeId()))
                .thenReturn(uri);
        when(excelCreationService.writeExcel(multipleCollection,
                subMultipleCollection,
                multipleDetails.getCaseData().getLeadCase(), userToken, caseTypeId))
                .thenReturn(bytes);
        excelDocManagementService.generateAndUploadExcel(multipleCollection,
                userToken, multipleDetails);
        verify(documentManagementService, times(1)).uploadDocument(userToken,
                bytes,
                MultiplesHelper.generateExcelDocumentName(multipleDetails.getCaseData()), APPLICATION_EXCEL_VALUE,
                multipleDetails.getCaseTypeId());
        verifyNoMoreInteractions(documentManagementService);
        verify(excelCreationService, times(1)).writeExcel(multipleCollection,
                subMultipleCollection,
                multipleDetails.getCaseData().getLeadCase(), userToken, caseTypeId);
        verifyNoMoreInteractions(excelCreationService);
        assertEquals("3", multipleDetails.getCaseData().getCaseCounter());
    }

    @Test
    void regenerationReplacesSelectionCriteriaFileLink() {
        MultipleData data = multipleDetails.getCaseData();
        data.setMultipleName("Mrs Test Auto");
        data.setMultipleReference("6000007");
        final CaseImporterFile originalFile = data.getCaseImporterFile();
        List<String> cases = List.of("245000/2020");
        String filename = "Mrs Test Auto-6000007.xlsx";
        ReflectionTestUtils.setField(excelDocManagementService, "ccdCaseDocumentUrl", "http://case-document-am");
        when(excelCreationService.writeExcel(cases,
                MultiplesHelper.generateSubMultipleStringCollection(data), data.getLeadCase(), userToken, caseTypeId))
                .thenReturn(bytes);
        when(documentManagementService.uploadDocument(userToken, bytes, filename, APPLICATION_EXCEL_VALUE, caseTypeId))
                .thenReturn(URI.create("http://dm-store/documents/regenerated"));

        excelDocManagementService.generateAndUploadExcel(cases, userToken, multipleDetails);

        assertNotSame(originalFile, data.getCaseImporterFile());
        assertEquals(filename, data.getCaseImporterFile().getUploadedDocument().getDocumentFilename());
        assertEquals("http://case-document-am/documents/regenerated",
                data.getCaseImporterFile().getUploadedDocument().getDocumentUrl());
        assertEquals("http://case-document-am/documents/regenerated/binary",
                data.getCaseImporterFile().getUploadedDocument().getDocumentBinaryUrl());
        assertEquals("1", data.getCaseCounter());
    }

    @Test
    void generateAndUploadExcelEmptySubMultipleCollection() {
        URI uri = URI.create("http://google.com");
        List<String> multipleCollection = new ArrayList<>(
                Arrays.asList("245000/2020", "245001/2020", "245002/2020"));
        multipleDetails.getCaseData().setSubMultipleCollection(null);
        when(documentManagementService.uploadDocument(userToken,
                bytes,
                MultiplesHelper.generateExcelDocumentName(
                        multipleDetails.getCaseData()), APPLICATION_EXCEL_VALUE,
                multipleDetails.getCaseTypeId()))
                .thenReturn(uri);
        when(excelCreationService.writeExcel(multipleCollection,
                new ArrayList<>(),
                multipleDetails.getCaseData().getLeadCase(), userToken, caseTypeId))
                .thenReturn(bytes);
        excelDocManagementService.generateAndUploadExcel(multipleCollection,
                userToken, multipleDetails);
        verify(documentManagementService, times(1)).uploadDocument(userToken,
                bytes,
                MultiplesHelper.generateExcelDocumentName(
                        multipleDetails.getCaseData()), APPLICATION_EXCEL_VALUE,
                multipleDetails.getCaseTypeId());
        verifyNoMoreInteractions(documentManagementService);
        verify(excelCreationService, times(1)).writeExcel(
                multipleCollection,
                new ArrayList<>(),
                multipleDetails.getCaseData().getLeadCase(), userToken, caseTypeId);
        verifyNoMoreInteractions(excelCreationService);
    }

    @Test
    void writeAndUploadScheduleDocument() {
        URI uri = URI.create("http://google.com");
        when(scheduleCreationService.writeSchedule(multipleDetails.getCaseData(),
                new ArrayList<>(),
                new TreeMap<>()))
                .thenReturn(bytes);
        when(documentManagementService.uploadDocument(userToken,
                bytes,
                MultiplesScheduleHelper.generateScheduleDocumentName(
                        multipleDetails.getCaseData()), APPLICATION_EXCEL_VALUE,
                multipleDetails.getCaseTypeId()))
                .thenReturn(uri);
        excelDocManagementService.writeAndUploadScheduleDocument(userToken, new TreeMap<>(),
                multipleDetails, new ArrayList<>());
        verify(documentManagementService, times(1)).uploadDocument(userToken,
                bytes,
                MultiplesScheduleHelper.generateScheduleDocumentName(
                        multipleDetails.getCaseData()), APPLICATION_EXCEL_VALUE,
                multipleDetails.getCaseTypeId());
        verify(scheduleCreationService, times(1))
                .writeSchedule(multipleDetails.getCaseData(),
                        new ArrayList<>(),
                        new TreeMap<>());
        verifyNoMoreInteractions(excelCreationService);
    }

}