package cz.cvut.kbss.study.persistence;

import cz.cvut.kbss.jopa.Persistence;
import cz.cvut.kbss.jopa.model.EntityManagerFactory;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.Map;

import static cz.cvut.kbss.jopa.model.JOPAPersistenceProperties.CACHE_ENABLED;
import static cz.cvut.kbss.jopa.model.JOPAPersistenceProperties.DATA_SOURCE_CLASS;
import static cz.cvut.kbss.jopa.model.JOPAPersistenceProperties.ONTOLOGY_PHYSICAL_URI_KEY;
import static cz.cvut.kbss.study.util.ConfigParam.DRIVER;
import static cz.cvut.kbss.study.util.ConfigParam.FORM_GEN_REPOSITORY_URL;

@Configuration
@PropertySource("classpath:config.properties")
public class FormGenPersistenceFactory {

    private final Environment environment;

    private EntityManagerFactory emf;

    public FormGenPersistenceFactory(Environment environment) {
        this.environment = environment;
    }

    @Bean(name = "formGen")
    public EntityManagerFactory getEntityManagerFactory() {
        return emf;
    }

    @PostConstruct
    private void init() {
        final Map<String, String> properties = new HashMap<>(PersistenceFactory.getDefaultParams());
        properties.put(ONTOLOGY_PHYSICAL_URI_KEY, environment.getProperty(FORM_GEN_REPOSITORY_URL.toString()));
        properties.put(DATA_SOURCE_CLASS, environment.getProperty(DRIVER.toString()));
        properties.put(CACHE_ENABLED, Boolean.FALSE.toString());
        this.emf = Persistence.createEntityManagerFactory("formGenPU", properties);
    }

    @PreDestroy
    private void close() {
        emf.close();
    }
}
