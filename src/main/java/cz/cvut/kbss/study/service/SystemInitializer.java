package cz.cvut.kbss.study.service;

import cz.cvut.kbss.study.model.Institution;
import cz.cvut.kbss.study.model.Role;
import cz.cvut.kbss.study.model.RoleGroup;
import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.model.Vocabulary;
import cz.cvut.kbss.study.util.Constants;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@ConditionalOnProperty(prefix = "security", name = "provider", havingValue = "internal", matchIfMissing = true)
@Service
public class SystemInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(SystemInitializer.class);

    private static final String ADMIN_USERNAME = "admin";
    private static final String INSTITUTION_NAME = "admin_institution";
    private static final String ADMIN_ROLE_GROUP_NAME = "admin-role-group";

    private final UserService userService;

    private final InstitutionService institutionService;

    private final RoleGroupService roleGroupService;

    public SystemInitializer(UserService userService,
                             InstitutionService institutionService,
                             RoleGroupService roleGroupService) {
        this.userService = userService;
        this.institutionService = institutionService;
        this.roleGroupService = roleGroupService;
    }

    @PostConstruct
    private void initializeSystem() {
        if (noAdminExists()) {
            addAdminRoleGroup();
            addDefaultInstitution();
            addDefaultAdministrator();
        }
    }

    private boolean noAdminExists() {
        return userService.findAll().stream()
                .noneMatch(user -> user.getRoleGroup() != null && user.getRoleGroup().getRoles().contains(Role.administrator));
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
            admin.setRoleGroup(roleGroupService.findByName(ADMIN_ROLE_GROUP_NAME));
            LOG.debug("Persisting default administrator {}", admin);
            userService.persist(admin);
        }
    }

    private void addAdminRoleGroup() {
        if (roleGroupService.findByName(ADMIN_ROLE_GROUP_NAME) == null) {
            final RoleGroup roleGroup = new RoleGroup();
            roleGroup.setName(ADMIN_ROLE_GROUP_NAME);
            roleGroup.addRole(Role.administrator);
            roleGroup.generateUri();
            roleGroupService.persist(roleGroup);
        }
    }
}
