package cz.cvut.kbss.study;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class RecordManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(RecordManagerApplication.class, args);
    }
}
