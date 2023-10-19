package cz.cvut.kbss.study.service.security;

import cz.cvut.kbss.study.environment.generator.Generator;
import cz.cvut.kbss.study.environment.util.Environment;
import cz.cvut.kbss.study.model.Institution;
import cz.cvut.kbss.study.model.PatientRecord;
import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.service.BaseServiceTestRunner;
import cz.cvut.kbss.study.service.InstitutionService;
import cz.cvut.kbss.study.service.PatientRecordService;
import cz.cvut.kbss.study.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class SecurityUtilsTest extends BaseServiceTestRunner {

    @Autowired
    private SecurityUtils securityUtils;

    @Autowired
    private UserService userService;

    @Autowired
    private InstitutionService institutionService;

    @Autowired
    private PatientRecordService patientRecordService;

    private User user;
    public static final String USERNAME = "halsey";
    public static final String PASSWORD = "john117";

    @BeforeEach
    public void setUp() {
        Institution institution = Generator.generateInstitution();
        institutionService.persist(institution);
        this.user = Generator.getUser(USERNAME, PASSWORD, "John", "Johnie", "Johnie@gmail.com", institutionService.findByName(institution.getName()));
        user.generateUri();
        userService.persist(user);
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void getCurrentUserReturnsCurrentlyLoggedInUser() {
        Environment.setCurrentUser(user);
        final User result = securityUtils.getCurrentUser();
        assertEquals(user, result);
    }

    @Test
    public void isMemberOfInstitutionReturnsMembershipStatusTrue() {
        Environment.setCurrentUser(user);
        assertTrue(securityUtils.isMemberOfInstitution(user.getInstitution().getKey()));
    }

    @Test
    public void isMemberOfInstitutionReturnsMembershipStatusFalse() {
        Environment.setCurrentUser(user);
        assertFalse(securityUtils.isMemberOfInstitution("nonExistingInstitutionKey"));
    }

    @Test
    public void areFromSameInstitutionReturnsMembershipStatusTrue() {
        Environment.setCurrentUser(user);

        User userFromSameInstitution = Generator.generateUser(user.getInstitution());
        userService.persist(userFromSameInstitution);

        assertTrue(securityUtils.areFromSameInstitution(userFromSameInstitution.getUsername()));
    }

    @Test
    public void areFromSameInstitutionReturnsMembershipStatusFalse() {
        Environment.setCurrentUser(user);

        Institution institutionAnother = Generator.generateInstitution();
        institutionService.persist(institutionAnother);

        User userFromAnotherInstitution = Generator.generateUser(institutionAnother);
        userService.persist(userFromAnotherInstitution);
        assertFalse(securityUtils.areFromSameInstitution(userFromAnotherInstitution.getUsername()));
    }

    @Test
    public void isRecordInUsersInstitutionReturnsMembershipStatusTrueForUser() {
        Environment.setCurrentUser(user);

        PatientRecord record = Generator.generatePatientRecord(user);

        patientRecordService.persist(record);
        assertTrue(securityUtils.isRecordInUsersInstitution(record.getKey()));
    }

    @Test
    public void isRecordInUsersInstitutionReturnsMembershipStatusFalse() {
        Environment.setCurrentUser(user);

        Institution institutionAnother = Generator.generateInstitution();
        institutionService.persist(institutionAnother);

        User userFromAnotherInstitution = Generator.generateUser(institutionAnother);
        userService.persist(userFromAnotherInstitution);

        PatientRecord record = Generator.generatePatientRecord(userFromAnotherInstitution);
        patientRecordService.persist(record);

        Environment.setCurrentUser(userFromAnotherInstitution);

        assertFalse(securityUtils.isRecordInUsersInstitution(record.getKey()));
    }
}
