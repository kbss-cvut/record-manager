package cz.cvut.kbss.study.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "cz.cvut.kbss.study.persistence")
public class PersistenceConfig {
}
