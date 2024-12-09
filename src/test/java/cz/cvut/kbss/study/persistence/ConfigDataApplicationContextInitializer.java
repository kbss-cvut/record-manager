package cz.cvut.kbss.study.persistence;

import org.springframework.boot.DefaultBootstrapContext;
import org.springframework.boot.DefaultPropertiesPropertySource;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.boot.env.RandomValuePropertySource;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

public class ConfigDataApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    public ConfigDataApplicationContextInitializer() {
    }

    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        RandomValuePropertySource.addToEnvironment(environment);
        DefaultBootstrapContext bootstrapContext = new DefaultBootstrapContext();
        ConfigDataEnvironmentPostProcessor.applyTo(environment, applicationContext, bootstrapContext, new String[0]);
        bootstrapContext.close(applicationContext);
        DefaultPropertiesPropertySource.moveToEnd(environment);
    }
}