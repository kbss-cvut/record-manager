package cz.cvut.kbss.study.persistence;

import cz.cvut.kbss.jopa.Persistence;
import cz.cvut.kbss.jopa.model.EntityManagerFactory;
import cz.cvut.kbss.jopa.model.JOPAPersistenceProperties;
import cz.cvut.kbss.jopa.model.JOPAPersistenceProvider;
import cz.cvut.kbss.ontodriver.rdf4j.config.Rdf4jOntoDriverProperties;
import cz.cvut.kbss.study.util.ConfigParam;
import cz.cvut.kbss.study.util.Constants;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.Map;

import static cz.cvut.kbss.ontodriver.config.OntoDriverProperties.DATA_SOURCE_PASSWORD;
import static cz.cvut.kbss.ontodriver.config.OntoDriverProperties.DATA_SOURCE_USERNAME;

@Configuration
@PropertySource("classpath:application.properties")
@Profile("test")
public class TestPersistenceFactory {

    private static final String URL_PROPERTY = "test." + ConfigParam.REPOSITORY_URL;
    private static final String DRIVER_PROPERTY = "test." + ConfigParam.PERSISTENCE_DRIVER;
    private static final String USERNAME_PROPERTY = "test.username";
    private static final String PASSWORD_PROPERTY = "test.password";

    @Autowired
    private Environment environment;

    private EntityManagerFactory emf;

    @Bean
    @Primary
    public EntityManagerFactory getEntityManagerFactory() {
        return emf;
    }

    @PostConstruct
    private void init() {
        final Map<String, String> properties = getDefaultProperties();
        properties.put(JOPAPersistenceProperties.ONTOLOGY_PHYSICAL_URI_KEY, environment.getProperty(URL_PROPERTY));
        properties.put(JOPAPersistenceProperties.DATA_SOURCE_CLASS, environment.getProperty(DRIVER_PROPERTY));
        if (environment.getProperty(USERNAME_PROPERTY) != null) {
            properties.put(DATA_SOURCE_USERNAME, environment.getProperty(USERNAME_PROPERTY));
            properties.put(DATA_SOURCE_PASSWORD, environment.getProperty(PASSWORD_PROPERTY));
        }
        this.emf = Persistence.createEntityManagerFactory("studyTestPU", properties);
    }

    @PreDestroy
    private void close() {
        if (emf.isOpen()) {
            emf.close();
        }
    }

    static Map<String, String> getDefaultProperties() {
        final Map<String, String> properties = new HashMap<>();
        properties.put(JOPAPersistenceProperties.LANG, Constants.PU_LANGUAGE);
        properties.put(JOPAPersistenceProperties.SCAN_PACKAGE, "cz.cvut.kbss.study");
        properties.put(Rdf4jOntoDriverProperties.USE_VOLATILE_STORAGE, Boolean.TRUE.toString());
        properties.put(Rdf4jOntoDriverProperties.USE_INFERENCE, Boolean.FALSE.toString());
        properties.put(JOPAPersistenceProperties.JPA_PERSISTENCE_PROVIDER, JOPAPersistenceProvider.class.getName());
        return properties;
    }
}
