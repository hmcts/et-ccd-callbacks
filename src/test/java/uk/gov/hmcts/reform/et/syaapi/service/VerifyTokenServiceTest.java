package uk.gov.hmcts.reform.et.syaapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VerifyTokenServiceTest {

    @Mock
    private JwtDecoder jwtDecoder;

    private VerifyTokenService verifyTokenService;

    @BeforeEach
    void setUp() {
        verifyTokenService = new VerifyTokenService(jwtDecoder);
    }

    @Test
    void verifyTokenSignature_validToken_returnsTrue() {
        when(jwtDecoder.decode(anyString())).thenReturn(mock(Jwt.class));
        assertTrue(verifyTokenService.verifyTokenSignature("Bearer validtoken"));
    }

    @Test
    void verifyTokenSignature_invalidToken_returnsFalse() {
        when(jwtDecoder.decode(anyString())).thenThrow(new JwtException("bad token"));
        assertFalse(verifyTokenService.verifyTokenSignature("Bearer badtoken"));
    }

    @Test
    void verifyTokenSignature_nullToken_returnsFalse() {
        assertFalse(verifyTokenService.verifyTokenSignature(null));
    }
}
