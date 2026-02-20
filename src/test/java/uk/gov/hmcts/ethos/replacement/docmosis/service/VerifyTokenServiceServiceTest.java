package uk.gov.hmcts.ethos.replacement.docmosis.service;

import ch.qos.logback.classic.Level;
import com.nimbusds.jose.proc.JWSVerifierFactory;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.test.utils.LoggerTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
class VerifyTokenServiceServiceTest {

    private static final String INVALID_USER_TOKEN = "Bearer "
            + "eyJ0eXAiOiJKV1QiLCJ6aXAiOiJOT05FIiwia2lkIjoiYi9PNk92VnYxK3krV2dySDVVa"
            + "TlXVGlvTHQwPSIsImFsZyI6IlJTMjU2In0"
            + ".eyJzdWIiOiJzc2NzLWNpdGl6ZW40QGhtY3RzLm5ldCIsImF1dGhfbGV2ZWwiOjAsI"
            + "mF1ZGl0VHJhY2tpbmdJZCI6Ijc1YzEyMTk3LWFjYm"
            + "YtNDg2Zi1iNDI5LTJlYWEwZjMyNWVkMCIsImlzcyI6Imh0dHA6Ly9mci1hbTo4MDgwL2"
            + "9wZW5hbS9vYXV0aDIvaG1jdHMiLCJ0b2tlbk5hbWU"
            + "iOiJhY2Nlc3NfdG9rZW4iLCJ0b2tlbl90eXBlIjoiQmVhcmVyIiwiYXV0aEdyYW50SWQi"
            + "OiIwMGZhYThiNy03OWY5LTRiZWQtODI1OS0zZDE0M"
            + "DEzOGYzZjIiLCJhdWQiOiJzc2NzIiwibmJmIjoxNTc4NTAwNDU0LCJncmFudF90eXBlIjo"
            + "iYXV0aG9yaXphdGlvbl9jb2RlIiwic2NvcGUiOlsi"
            + "b3BlbmlkIiwicHJvZmlsZSIsInJvbGVzIl0sImF1dGhfdGltZSI6MTU3ODUwMDQ1MTAwMCwicmVhbG"
            + "0iOiIvaG1jdHMiLCJleHAiOjE1Nzg1Mjky"
            + "NTQsImlhdCI6MTU3ODUwMDQ1NCwiZXhwaXJlc19pbiI6Mjg4MDAsImp0aSI6ImNkMTgxODM3LTdlM"
            + "mUtNDY1Ny05ZTgwLTk4NWE3ZjVmZDMzYiJ9."
            + "SZOd981fC1bdMWehXKsUl0B9vEXRr7-NBKl6IaFIoS573rNjKgcIzChMaxcmc-anOxJqgF8Lan7RdM"
            + "CIb4Y-zGG3TzfGAG7elpmXJVsogPKCWJlGF"
            + "CJm_wU-h_cqAcL2llgqnNkkms43lgvyfIdiXv3J-00qBHzMy3jG5mLOE5YZet1LKf3IiRNZxI5Vx6L"
            + "2Afdox1jiKGQGGt2bNx7-rcYS8VVVZI-ovo7"
            + "lbbWU6Mi5lWI19q2AS9jGcK5U4hcIU06JzoWGsh-Ob1xkq7VtJKyrOSiUth-SjY5PqQzjvpuEO8Mr"
            + "LWTI0sCaWRHbmbF0bHICGO17bQ42_PfTHgza4A";

    private static final String EXPECTED_ERROR_INVALID_TOKEN = "Invalid Token " + INVALID_USER_TOKEN;

    @Mock
    JWSVerifierFactory jwsVerifierFactory;

    @InjectMocks
    private VerifyTokenService verifyTokenService;

    @BeforeEach
    void setUp() {
        verifyTokenService = new VerifyTokenService();
        LoggerTestUtils.initializeLogger(VerifyTokenService.class);
        ReflectionTestUtils.setField(verifyTokenService, "idamJwkUrl", "http://idam-api:5000/jwks");
    }

    @Test
    void verifyTokenSignature() {
        assertThat(verifyTokenService.verifyTokenSignature(INVALID_USER_TOKEN)).isFalse();
    }

    @Test
    @SneakyThrows
    void theIsTokenSignatureValid() {
        // when token is invalid should return false and log invalid token error
        assertThat(verifyTokenService.isTokenSignatureValid(INVALID_USER_TOKEN)).isFalse();
        LoggerTestUtils.checkLog(Level.ERROR, LoggerTestUtils.INTEGER_THREE, EXPECTED_ERROR_INVALID_TOKEN);
    }
}