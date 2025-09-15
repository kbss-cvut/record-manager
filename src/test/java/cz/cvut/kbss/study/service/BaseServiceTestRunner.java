package cz.cvut.kbss.study.service;

import cz.cvut.kbss.study.environment.TransactionalTestRunner;
import cz.cvut.kbss.study.environment.config.TestPersistenceConfig;
import cz.cvut.kbss.study.environment.config.TestServiceConfig;
import cz.cvut.kbss.study.environment.generator.Generator;
import cz.cvut.kbss.study.model.Institution;
import cz.cvut.kbss.study.model.RoleGroup;
import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.persistence.ConfigDataApplicationContextInitializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TestServiceConfig.class, TestPersistenceConfig.class}, initializers = {ConfigDataApplicationContextInitializer.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public abstract class BaseServiceTestRunner extends TransactionalTestRunner {

    protected User admin;

    protected User user;

    protected RoleGroup adminRoleGroup;

    protected RoleGroup userRoleGroup;

    protected Institution institution;

    @BeforeEach
    public void setUp() {
        this.institution = Generator.generateInstitution();
        this.adminRoleGroup = Generator.generateAdminRoleGroup();
        this.userRoleGroup = Generator.generateRoleGroupWithRoles();
        this.admin = Generator.generateUser(institution, adminRoleGroup);
        this.user = Generator.generateUser(institution, this.userRoleGroup);
    }
}
