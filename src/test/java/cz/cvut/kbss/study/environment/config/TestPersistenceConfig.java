package cz.cvut.kbss.study.environment.config;

import cz.cvut.kbss.study.persistence.TestFormGenPersistenceFactory;
import cz.cvut.kbss.study.persistence.TestPersistenceFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

@Configuration
@ComponentScan(basePackages = {"cz.cvut.kbss.study.persistence.dao"})
@Import({TestPersistenceFactory.class,
    TestFormGenPersistenceFactory.class})
public class TestPersistenceConfig {
}
