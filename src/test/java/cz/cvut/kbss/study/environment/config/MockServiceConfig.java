package cz.cvut.kbss.study.environment.config;

import cz.cvut.kbss.study.persistence.dao.ActionHistoryDao;
import cz.cvut.kbss.study.service.ActionHistoryService;
import cz.cvut.kbss.study.service.ConfigReader;
import cz.cvut.kbss.study.service.InstitutionService;
import cz.cvut.kbss.study.service.RecordService;
import cz.cvut.kbss.study.service.StatisticsService;
import cz.cvut.kbss.study.service.UserService;
import cz.cvut.kbss.study.service.security.UserDetailsService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.Mockito.mock;

@TestConfiguration
@ComponentScan(basePackages = "cz.cvut.kbss.study.service")
public class MockServiceConfig {

    @Bean
    public RecordService recordService() { return mock(RecordService.class); }

    @Bean
    public UserService userService() {
        return mock(UserService.class);
    }

    @Bean
    public InstitutionService institutionService() {
        return mock(InstitutionService.class);
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return mock(UserDetailsService.class);
    }

    @Bean
    public ActionHistoryService actionHistoryService() {
        return mock(ActionHistoryService.class);
    }

    @Bean
    public StatisticsService statisticsService() {
        return mock(StatisticsService.class);
    }

    @Bean
    public ActionHistoryDao actionHistoryDao() {
        return mock(ActionHistoryDao.class);
    }

    @Bean
    public ConfigReader configReader() {
        return mock(ConfigReader.class);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}