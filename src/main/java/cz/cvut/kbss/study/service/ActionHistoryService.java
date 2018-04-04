package cz.cvut.kbss.study.service;

import cz.cvut.kbss.study.model.ActionHistory;
import cz.cvut.kbss.study.model.User;

import java.util.List;

public interface ActionHistoryService extends BaseService<ActionHistory> {

    List<ActionHistory> findAllByOrderAsc(int pageNumber);

    ActionHistory findByKey(String key);

    List<ActionHistory> findByType(String type, int pageNumber);

    List<ActionHistory> findByAuthor(User author, int pageNumber);
}
