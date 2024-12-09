package cz.cvut.kbss.study.persistence;

import cz.cvut.kbss.jopa.Persistence;
import cz.cvut.kbss.jopa.model.EntityManagerFactory;
import cz.cvut.kbss.jopa.model.JOPAPersistenceProperties;
import cz.cvut.kbss.study.util.ConfigParam;
import java.util.Map;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

@Configuration
@EnableConfigurationProperties(cz.cvut.kbss.study.util.Configuration.class)
@Profile("test")
public class TestFormGenPersistenceFactory {

    @Autowired
    private cz.cvut.kbss.study.util.Configuration config;

    private EntityManagerFactory emf;

    @Bean(name = "formGen")
    public EntityManagerFactory getEntityManagerFactory() {
        return emf;
    }

    @PostConstruct
    private void init() {
        final Map<String, String> properties = TestPersistenceFactory.getDefaultProperties();
        properties.put(JOPAPersistenceProperties.ONTOLOGY_PHYSICAL_URI_KEY, config.getFormGenRepositoryUrl());
        properties.put(JOPAPersistenceProperties.DATA_SOURCE_CLASS, config.getPersistenceDriver());
        this.emf = Persistence.createEntityManagerFactory("formGenTestPU", properties);
    }

    @PreDestroy
    private void close() {
        if (emf.isOpen()) {
            emf.close();
        }
    }
}
