package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import uk.gov.hmcts.ecm.common.service.PostcodeToOfficeService;

class Et1ReppedServiceTest {

    @InjectMocks
    private Et1ReppedService et1ReppedService;
    @Mock
    private PostcodeToOfficeService postcodeToOfficeService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void validatePostcode() {
    }
}