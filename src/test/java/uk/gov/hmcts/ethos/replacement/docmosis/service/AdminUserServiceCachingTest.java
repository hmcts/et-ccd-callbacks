package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AdminUserServiceCachingTest.TestConfig.class)
@TestPropertySource(properties = {
    "etcos.system.username=test-user",
    "etcos.system.password=test-pass"
})
class AdminUserServiceCachingTest {

    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private UserIdamService userIdamService;

    @Autowired
    private CacheManager cacheManager;

    @Configuration
    @EnableCaching
    static class TestConfig {
        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager("adminUserToken");
        }

        @Bean
        UserIdamService userIdamService() {
            return mock(UserIdamService.class);
        }

        @Bean
        AdminUserService adminUserService(UserIdamService userIdamService) {
            return new AdminUserService(userIdamService);
        }

        // Ensures @Value fields in AdminUserService are resolved from @TestPropertySource
        @Bean
        static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
            return new PropertySourcesPlaceholderConfigurer();
        }
    }

    @BeforeEach
    void setUp() {
        Cache cache = cacheManager.getCache("adminUserToken");
        if (cache != null) {
            cache.clear();
        }
        reset(userIdamService);
    }

    @Test
    void shouldCacheAdminUserToken() {
        when(userIdamService.getAccessToken("test-user", "test-pass")).thenReturn("tok-123");

        String token1 = adminUserService.getAdminUserToken();
        String token2 = adminUserService.getAdminUserToken();

        assertThat(token1).isEqualTo("Bearer tok-123");
        assertThat(token2).isEqualTo("Bearer tok-123");
        verify(userIdamService, times(1)).getAccessToken("test-user", "test-pass");
        verifyNoMoreInteractions(userIdamService);
    }

    @Test
    void shouldEvictCacheWhenEmptyAdminUserTokenCalled() {
        when(userIdamService.getAccessToken("test-user", "test-pass")).thenReturn("tok-abc");

        // Populate and use cache
        adminUserService.getAdminUserToken();
        adminUserService.getAdminUserToken();
        verify(userIdamService, times(1)).getAccessToken("test-user", "test-pass");

        // Evict cache via the annotated method
        adminUserService.emptyAdminUserToken();

        // Next call should trigger another fetch
        String tokenAfterEvict = adminUserService.getAdminUserToken();
        assertThat(tokenAfterEvict).isEqualTo("Bearer tok-abc");
        verify(userIdamService, times(2)).getAccessToken("test-user", "test-pass");
        verifyNoMoreInteractions(userIdamService);
    }

}

