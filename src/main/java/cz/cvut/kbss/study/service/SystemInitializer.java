package cz.cvut.kbss.study.service;

import cz.cvut.kbss.study.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class SystemInitializer {

    private static final String ADMIN_USERNAME = "admin";

    @Autowired
    private UserService userService;

    @PostConstruct
    private void initializeSystem() {
        addDefaultAdministrator();
    }

    private void addDefaultAdministrator() {
        if (userService.findByUsername(ADMIN_USERNAME) == null) {
            final User admin = new User();
            admin.setFirstName("Admin");
            admin.setLastName("Administratorowitch");
            admin.setUsername(ADMIN_USERNAME);
            admin.setPassword("5y5t3mAdm1n.");
            userService.persist(admin);
        }
    }
}
