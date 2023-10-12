package cz.cvut.kbss.study.persistence.dao;

import cz.cvut.kbss.study.environment.generator.Generator;
import cz.cvut.kbss.study.model.ActionHistory;
import cz.cvut.kbss.study.model.Institution;
import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.persistence.BaseDaoTestRunner;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ActionHistoryDaoTest extends BaseDaoTestRunner {

    @Autowired
    private UserDao userDao;

    @Autowired
    private InstitutionDao institutionDao;

    @Autowired
    ActionHistoryDao actionHistoryDao;

    private final String LOAD_SUCCESS = "LOAD_SUCCESS";
    private final String LOAD_ERROR = "LOAD_ERROR";
    private final String LOAD_PENDING = "LOAD_PENDING";

    @Test
    public void findByKeyReturnsActionWithPayload() {
        Institution institution = Generator.generateInstitution();
        institutionDao.persist(institution);

        User user = Generator.generateUser(institution);
        userDao.persist(user);

        ActionHistory action = Generator.generateActionHistory(user);
        actionHistoryDao.persist(action);

        action = actionHistoryDao.findByKey(action.getKey());

        assertNotNull(action);
        assertNotNull(action.getPayload());
    }

    @Test
    public void findAllWithParamsWithoutParamsReturnsAllActions() {
        Institution institution = Generator.generateInstitution();
        institutionDao.persist(institution);

        User user1 = Generator.generateUser(institution);
        userDao.persist(user1);
        User user2 = Generator.generateUser(institution);
        userDao.persist(user2);

        ActionHistory action1 = Generator.generateActionHistory(user1);
        ActionHistory action2 = Generator.generateActionHistory(user1);
        ActionHistory action3 = Generator.generateActionHistory(user2);
        actionHistoryDao.persist(action1);
        actionHistoryDao.persist(action2);
        actionHistoryDao.persist(action3);

        List<ActionHistory> actionsList = actionHistoryDao.findAllWithParams(null, null, 1);

        assertEquals(3, actionsList.size());
    }

    @Test
    public void findAllWithParamsWithAuthorReturnsAuthorsActions() {
        Institution institution = Generator.generateInstitution();
        institutionDao.persist(institution);

        User user1 = Generator.generateUser(institution);
        userDao.persist(user1);
        User user2 = Generator.generateUser(institution);
        userDao.persist(user2);
        User user3 = Generator.generateUser(institution);
        userDao.persist(user3);

        ActionHistory action1 = Generator.generateActionHistory(user1);
        ActionHistory action2 = Generator.generateActionHistory(user1);
        ActionHistory action3 = Generator.generateActionHistory(user2);
        actionHistoryDao.persist(action1);
        actionHistoryDao.persist(action2);
        actionHistoryDao.persist(action3);

        List<ActionHistory> actionsList1 = actionHistoryDao.findAllWithParams(null, user1, 1);
        List<ActionHistory> actionsList2 = actionHistoryDao.findAllWithParams(null, user2, 1);
        List<ActionHistory> actionsList3 = actionHistoryDao.findAllWithParams(null, user3, 1);

        assertEquals(2, actionsList1.size());
        assertEquals(1, actionsList2.size());
        assertEquals(0, actionsList3.size());
    }

    @Test
    public void findAllWithParamsWithTypeReturnsActionsWithExactType() {
        Institution institution = Generator.generateInstitution();
        institutionDao.persist(institution);

        User user = Generator.generateUser(institution);
        userDao.persist(user);

        ActionHistory action1 = Generator.generateActionHistory(user);
        action1.setType(LOAD_SUCCESS);
        ActionHistory action2 = Generator.generateActionHistory(user);
        action2.setType(LOAD_SUCCESS);
        ActionHistory action3 = Generator.generateActionHistory(user);
        action3.setType(LOAD_ERROR);
        actionHistoryDao.persist(action1);
        actionHistoryDao.persist(action2);
        actionHistoryDao.persist(action3);

        List<ActionHistory> actionsList1 = actionHistoryDao.findAllWithParams(LOAD_SUCCESS, null, 1);
        List<ActionHistory> actionsList2 = actionHistoryDao.findAllWithParams(LOAD_ERROR, null, 1);
        List<ActionHistory> actionsList3 = actionHistoryDao.findAllWithParams(LOAD_PENDING, null, 1);

        assertEquals(2, actionsList1.size());
        assertEquals(1, actionsList2.size());
        assertEquals(0, actionsList3.size());
    }

    @Test
    public void findAllWithParamsWithTypeReturnsActionsWithTypeContained() {
        Institution institution = Generator.generateInstitution();
        institutionDao.persist(institution);

        User user = Generator.generateUser(institution);
        userDao.persist(user);

        ActionHistory action1 = Generator.generateActionHistory(user);
        action1.setType(LOAD_SUCCESS);
        ActionHistory action2 = Generator.generateActionHistory(user);
        action2.setType(LOAD_SUCCESS);
        ActionHistory action3 = Generator.generateActionHistory(user);

        actionHistoryDao.persist(action1);
        actionHistoryDao.persist(action2);
        actionHistoryDao.persist(action3);

        List<ActionHistory> actionsList = actionHistoryDao.findAllWithParams("LOAD", null, 1);

        assertEquals(2, actionsList.size());
    }

    @Test
    public void findAllWithParamsReturnsMatchingActions() {
        Institution institution = Generator.generateInstitution();
        institutionDao.persist(institution);

        User user1 = Generator.generateUser(institution);
        userDao.persist(user1);
        User user2 = Generator.generateUser(institution);
        userDao.persist(user2);

        ActionHistory action1 = Generator.generateActionHistory(user1);
        action1.setType(LOAD_SUCCESS);
        ActionHistory action2 = Generator.generateActionHistory(user1);
        action2.setType(LOAD_SUCCESS);
        ActionHistory action3 = Generator.generateActionHistory(user2);
        action3.setType(LOAD_ERROR);

        actionHistoryDao.persist(action1);
        actionHistoryDao.persist(action2);
        actionHistoryDao.persist(action3);

        List<ActionHistory> actionsList1 = actionHistoryDao.findAllWithParams(LOAD_SUCCESS, user1, 1);
        List<ActionHistory> actionsList2 = actionHistoryDao.findAllWithParams(LOAD_SUCCESS, user2, 1);
        List<ActionHistory> actionsList3 = actionHistoryDao.findAllWithParams(LOAD_ERROR, user2, 1);
        List<ActionHistory> actionsList4 = actionHistoryDao.findAllWithParams("LOAD", user2, 1);

        assertEquals(2, actionsList1.size());
        assertEquals(0, actionsList2.size());
        assertEquals(1, actionsList3.size());
        assertEquals(1, actionsList4.size());
    }
}
