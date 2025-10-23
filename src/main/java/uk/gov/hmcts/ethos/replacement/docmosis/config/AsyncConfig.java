package uk.gov.hmcts.ethos.replacement.docmosis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class AsyncConfig {

    @Bean(name = "etAsyncExecutor", destroyMethod = "shutdown")
    public ExecutorService etAsyncExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
