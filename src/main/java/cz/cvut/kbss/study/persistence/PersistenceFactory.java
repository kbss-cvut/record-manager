package cz.cvut.kbss.study.persistence;

import cz.cvut.kbss.jopa.Persistence;
import cz.cvut.kbss.jopa.model.EntityManagerFactory;
import cz.cvut.kbss.jopa.model.JOPAPersistenceProvider;
import cz.cvut.kbss.ontodriver.config.OntoDriverProperties;
import cz.cvut.kbss.study.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static cz.cvut.kbss.jopa.model.JOPAPersistenceProperties.*;
import static cz.cvut.kbss.ontodriver.config.OntoDriverProperties.DATA_SOURCE_PASSWORD;
import static cz.cvut.kbss.ontodriver.config.OntoDriverProperties.DATA_SOURCE_USERNAME;
import static cz.cvut.kbss.study.util.ConfigParam.DRIVER;
import static cz.cvut.kbss.study.util.ConfigParam.REPOSITORY_URL;

/**
 * Sets up persistence and provides {@link EntityManagerFactory} as Spring bean.
 */
@Configuration
@PropertySource("classpath:config.properties")
public class PersistenceFactory {

    private static final String USERNAME_PROPERTY = "username";
    private static final String PASSWORD_PROPERTY = "password";

    private static final Map<String, String> DEFAULT_PARAMS = initParams();

    private final Environment environment;

    private EntityManagerFactory emf;

    public PersistenceFactory(Environment environment) {
        this.environment = environment;
    }

    @Bean
    @Primary
    public EntityManagerFactory getEntityManagerFactory() {
        return emf;
    }

    @PostConstruct
    private void init() {
        final Map<String, String> properties = new HashMap<>(DEFAULT_PARAMS);
        properties.put(ONTOLOGY_PHYSICAL_URI_KEY, environment.getProperty(REPOSITORY_URL.toString()));
        properties.put(DATA_SOURCE_CLASS, environment.getProperty(DRIVER.toString()));
        if (environment.getProperty(USERNAME_PROPERTY) != null) {
            properties.put(DATA_SOURCE_USERNAME, environment.getProperty(USERNAME_PROPERTY));
            properties.put(DATA_SOURCE_PASSWORD, environment.getProperty(PASSWORD_PROPERTY));
        }
        this.emf = Persistence.createEntityManagerFactory("fertilitySavingStudyPU", properties);
    }

    @PreDestroy
    private void close() {
        emf.close();
    }

    private static Map<String, String> initParams() {
        final Map<String, String> map = new HashMap<>();
        map.put(OntoDriverProperties.ONTOLOGY_LANGUAGE, Constants.PU_LANGUAGE);
        map.put(SCAN_PACKAGE, "cz.cvut.kbss.study");
        map.put(JPA_PERSISTENCE_PROVIDER, JOPAPersistenceProvider.class.getName());
        return map;
    }

    static Map<String, String> getDefaultParams() {
        return Collections.unmodifiableMap(DEFAULT_PARAMS);
    }
}
