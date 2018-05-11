package cz.cvut.kbss.study.environment.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "cz.cvut.kbss.study.security")
public class TestSecurityConfig {
}
