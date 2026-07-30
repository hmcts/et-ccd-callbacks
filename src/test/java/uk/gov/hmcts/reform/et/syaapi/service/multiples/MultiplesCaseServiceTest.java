package uk.gov.hmcts.reform.et.syaapi.service.multiples;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.et.common.model.multiples.SubmitMultipleEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.service.multiples.MultipleReferenceService;
import uk.gov.hmcts.reform.et.syaapi.service.AdminUserService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MultiplesCaseServiceTest {

    private static final String ADMIN_TOKEN = "adminToken";
    private static final String MULTIPLE_REFERENCE = "6000008";

    @Mock
    private AdminUserService adminUserService;

    @Mock
    private MultipleReferenceService multipleReferenceService;

    @Mock
    private CcdClient ccdClient;

    private MultiplesCaseService multiplesCaseService;

    @BeforeEach
    void setUp() {
        multiplesCaseService = new MultiplesCaseService(adminUserService, multipleReferenceService, ccdClient);
        when(adminUserService.getAdminUserToken()).thenReturn(ADMIN_TOKEN);
    }

    @Test
    void shouldAppendMultipleSuffixToSingleCaseTypeIdWhenSearching() throws Exception {
        SubmitMultipleEvent expected = new SubmitMultipleEvent();
        when(multipleReferenceService.getMultipleByReference(ADMIN_TOKEN, "ET_Scotland_Multiple", MULTIPLE_REFERENCE))
                .thenReturn(expected);

        SubmitMultipleEvent result =
                multiplesCaseService.getMultipleCaseByCaseReference("ET_Scotland", MULTIPLE_REFERENCE);

        assertThat(result).isSameAs(expected);
        verify(multipleReferenceService).getMultipleByReference(
                ADMIN_TOKEN, "ET_Scotland_Multiple", MULTIPLE_REFERENCE);
    }

    @Test
    void shouldNotDoubleAppendMultipleSuffixWhenAlreadyPresent() throws Exception {
        SubmitMultipleEvent expected = new SubmitMultipleEvent();
        when(multipleReferenceService.getMultipleByReference(ADMIN_TOKEN, "ET_Scotland_Multiple", MULTIPLE_REFERENCE))
                .thenReturn(expected);

        SubmitMultipleEvent result =
                multiplesCaseService.getMultipleCaseByCaseReference("ET_Scotland_Multiple", MULTIPLE_REFERENCE);

        assertThat(result).isSameAs(expected);
        verify(multipleReferenceService).getMultipleByReference(
                ADMIN_TOKEN, "ET_Scotland_Multiple", MULTIPLE_REFERENCE);
    }
}
