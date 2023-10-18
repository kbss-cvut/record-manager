package cz.cvut.kbss.study.environment.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.cvut.kbss.study.environment.util.Environment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@TestConfiguration
@ComponentScan(basePackages = "cz.cvut.kbss.study.security")
public class TestSecurityConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return Environment.getObjectMapper();
    }
}
