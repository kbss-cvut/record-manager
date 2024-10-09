package cz.cvut.kbss.study.persistence.dao;

import cz.cvut.kbss.study.environment.generator.Generator;
import cz.cvut.kbss.study.model.*;
import cz.cvut.kbss.study.persistence.BaseDaoTestRunner;
import cz.cvut.kbss.study.util.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ActionHistoryDaoTest extends BaseDaoTestRunner {

    @Autowired
    private UserDao userDao;

    @Autowired
    private InstitutionDao institutionDao;

    @Autowired
    ActionHistoryDao actionHistoryDao;

    @Autowired
    RoleGroupDao roleGroupDao;

    private static final String LOAD_SUCCESS = "LOAD_SUCCESS";
    private static final String LOAD_ERROR = "LOAD_ERROR";
    private static final String LOAD_PENDING = "LOAD_PENDING";

    private RoleGroup roleGroupAdmin;

    @BeforeEach
    public void setUp() {
        this.roleGroupAdmin = Generator.generateRoleGroupWithRoles(Role.administrator);
        transactional(() -> roleGroupDao.persist(this.roleGroupAdmin));
    }

    @Test
    public void findByKeyReturnsActionWithPayload() {
        Institution institution = Generator.generateInstitution();

        User user = Generator.generateUser(institution, this.roleGroupAdmin);
        ActionHistory action = Generator.generateActionHistory(user);

        transactional(() -> {
            institutionDao.persist(institution);
            userDao.persist(user);
            actionHistoryDao.persist(action);
        });

        final ActionHistory result = actionHistoryDao.findByKey(action.getKey());

        assertNotNull(result);
        assertNotNull(result.getPayload());
    }

    @Test
    public void findAllWithParamsWithoutParamsReturnsAllActions() {
        Institution institution = Generator.generateInstitution();
        User user1 = Generator.generateUser(institution, this.roleGroupAdmin);
        User user2 = Generator.generateUser(institution, this.roleGroupAdmin);
        ActionHistory action1 = Generator.generateActionHistory(user1);
        ActionHistory action2 = Generator.generateActionHistory(user1);
        ActionHistory action3 = Generator.generateActionHistory(user2);

        transactional(() -> {
            institutionDao.persist(institution);
            userDao.persist(List.of(user1, user2));
            actionHistoryDao.persist(List.of(action1, action2, action3));
        });

        Page<ActionHistory> actionsList = actionHistoryDao.findAllWithParams(null, null, PageRequest.of(0, Constants.DEFAULT_PAGE_SIZE));

        assertEquals(3, actionsList.getNumberOfElements());
    }

    @Test
    public void findAllWithParamsWithAuthorReturnsAuthorsActions() {
        Institution institution = Generator.generateInstitution();
        User user1 = Generator.generateUser(institution, this.roleGroupAdmin);
        User user2 = Generator.generateUser(institution, this.roleGroupAdmin);
        User user3 = Generator.generateUser(institution, this.roleGroupAdmin);
        ActionHistory action1 = Generator.generateActionHistory(user1);
        ActionHistory action2 = Generator.generateActionHistory(user1);
        ActionHistory action3 = Generator.generateActionHistory(user2);

        transactional(() -> {
            institutionDao.persist(institution);
            userDao.persist(List.of(user1, user2, user3));
            actionHistoryDao.persist(List.of(action1, action2, action3));
        });

        Page<ActionHistory> actionsList1 = actionHistoryDao.findAllWithParams(null, user1, PageRequest.of(0, Constants.DEFAULT_PAGE_SIZE));
        Page<ActionHistory> actionsList2 = actionHistoryDao.findAllWithParams(null, user2, PageRequest.of(0, Constants.DEFAULT_PAGE_SIZE));
        Page<ActionHistory> actionsList3 = actionHistoryDao.findAllWithParams(null, user3, PageRequest.of(0, Constants.DEFAULT_PAGE_SIZE));

        assertEquals(2, actionsList1.getNumberOfElements());
        assertEquals(1, actionsList2.getNumberOfElements());
        assertEquals(0, actionsList3.getNumberOfElements());
    }

    @Test
    public void findAllWithParamsWithTypeReturnsActionsWithExactType() {
        Institution institution = Generator.generateInstitution();
        User user = Generator.generateUser(institution, this.roleGroupAdmin);
        ActionHistory action1 = Generator.generateActionHistory(user);
        action1.setType(LOAD_SUCCESS);
        ActionHistory action2 = Generator.generateActionHistory(user);
        action2.setType(LOAD_SUCCESS);
        ActionHistory action3 = Generator.generateActionHistory(user);
        action3.setType(LOAD_ERROR);

        transactional(() -> {
            institutionDao.persist(institution);
            userDao.persist(user);
            actionHistoryDao.persist(List.of(action1, action2, action3));
        });

        Page<ActionHistory> actionsList1 = actionHistoryDao.findAllWithParams(LOAD_SUCCESS, null, PageRequest.of(0, Constants.DEFAULT_PAGE_SIZE));
        Page<ActionHistory> actionsList2 = actionHistoryDao.findAllWithParams(LOAD_ERROR, null, PageRequest.of(0, Constants.DEFAULT_PAGE_SIZE));
        Page<ActionHistory> actionsList3 = actionHistoryDao.findAllWithParams(LOAD_PENDING, null, PageRequest.of(0, Constants.DEFAULT_PAGE_SIZE));

        assertEquals(2, actionsList1.getNumberOfElements());
        assertEquals(1, actionsList2.getNumberOfElements());
        assertEquals(0, actionsList3.getNumberOfElements());
    }

    @Test
    public void findAllWithParamsWithTypeReturnsActionsWithTypeContained() {
        Institution institution = Generator.generateInstitution();
        User user = Generator.generateUser(institution, this.roleGroupAdmin);
        ActionHistory action1 = Generator.generateActionHistory(user);
        action1.setType(LOAD_SUCCESS);
        ActionHistory action2 = Generator.generateActionHistory(user);
        action2.setType(LOAD_SUCCESS);
        ActionHistory action3 = Generator.generateActionHistory(user);

        transactional(() -> {
            institutionDao.persist(institution);
            userDao.persist(user);
            actionHistoryDao.persist(List.of(action1, action2, action3));
        });

        Page<ActionHistory> actionsList = actionHistoryDao.findAllWithParams("LOAD", null, PageRequest.of(0, Constants.DEFAULT_PAGE_SIZE));

        assertEquals(2, actionsList.getNumberOfElements());
    }

    @Test
    public void findAllWithParamsReturnsMatchingActions() {
        Institution institution = Generator.generateInstitution();
        User user1 = Generator.generateUser(institution, this.roleGroupAdmin);
        User user2 = Generator.generateUser(institution, this.roleGroupAdmin);
        ActionHistory action1 = Generator.generateActionHistory(user1);
        action1.setType(LOAD_SUCCESS);
        ActionHistory action2 = Generator.generateActionHistory(user1);
        action2.setType(LOAD_SUCCESS);
        ActionHistory action3 = Generator.generateActionHistory(user2);
        action3.setType(LOAD_ERROR);

        transactional(() -> {
            institutionDao.persist(institution);
            userDao.persist(List.of(user1, user2));
            actionHistoryDao.persist(List.of(action1, action2, action3));
        });

        Page<ActionHistory> actionsList1 = actionHistoryDao.findAllWithParams(LOAD_SUCCESS, user1, PageRequest.of(0, Constants.DEFAULT_PAGE_SIZE));
        Page<ActionHistory> actionsList2 = actionHistoryDao.findAllWithParams(LOAD_SUCCESS, user2, PageRequest.of(0, Constants.DEFAULT_PAGE_SIZE));
        Page<ActionHistory> actionsList3 = actionHistoryDao.findAllWithParams(LOAD_ERROR, user2, PageRequest.of(0, Constants.DEFAULT_PAGE_SIZE));
        Page<ActionHistory> actionsList4 = actionHistoryDao.findAllWithParams("LOAD", user2, PageRequest.of(0, Constants.DEFAULT_PAGE_SIZE));

        assertEquals(2, actionsList1.getNumberOfElements());
        assertEquals(0, actionsList2.getNumberOfElements());
        assertEquals(1, actionsList3.getNumberOfElements());
        assertEquals(1, actionsList4.getNumberOfElements());
    }

    @Test
    void findAllReturnsActionsOnMatchingPage() {
        Institution institution = Generator.generateInstitution();
        User user = Generator.generateUser(institution, this.roleGroupAdmin);
        final List<ActionHistory> allActions = IntStream.range(0, 10).mapToObj(i -> Generator.generateActionHistory(user)).toList();
        transactional(() -> {
            institutionDao.persist(institution);
            userDao.persist(user);
            actionHistoryDao.persist(allActions);
        });

        final PageRequest pageSpec = PageRequest.of(2, allActions.size() / 2);
        final Page<ActionHistory> result = actionHistoryDao.findAllWithParams(null, null, pageSpec);
        assertEquals(allActions.subList((int) pageSpec.getOffset(), allActions.size()), result.getContent());
    }
}
