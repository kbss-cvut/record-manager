package cz.cvut.kbss.study.service;

import cz.cvut.kbss.study.model.Institution;
import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.model.Vocabulary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class SystemInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(SystemInitializer.class);

    private static final String ADMIN_USERNAME = "admin";
    private static final String INSTITUTION_NAME = "admin_institution";

    private final UserService userService;

    private final InstitutionService institutionService;

    public SystemInitializer(UserService userService,
                             InstitutionService institutionService) {
        this.userService = userService;
        this.institutionService = institutionService;
    }

    @PostConstruct
    private void initializeSystem() {
        addDefaultInstitution();
        addDefaultAdministrator();
    }

    private void addDefaultInstitution() {
        if (institutionService.findByName(INSTITUTION_NAME) == null) {
            final Institution institution = new Institution();
            institution.setName(INSTITUTION_NAME);
            institutionService.persist(institution);
        }
    }

    private void addDefaultAdministrator() {
        if (userService.findByUsername(ADMIN_USERNAME) == null) {
            final User admin = new User();
            admin.setFirstName("Admin");
            admin.setLastName("Administratorowitch");
            admin.setEmailAddress("admin@admin.org");
            admin.setUsername(ADMIN_USERNAME);
            admin.setPassword("5y5t3mAdm1n.");
            admin.setInstitution(institutionService.findByName(INSTITUTION_NAME));
            admin.setIsInvited(true);
            admin.getTypes().add(Vocabulary.s_c_administrator);
            LOG.debug("Persisting default administrator {}", admin);
            userService.persist(admin);
        }
    }
}
