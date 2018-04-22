package cz.cvut.kbss.study.service;

import cz.cvut.kbss.study.model.ActionHistory;
import cz.cvut.kbss.study.model.User;

import java.util.List;

public interface ActionHistoryService extends BaseService<ActionHistory> {

    ActionHistory findByKey(String key);

    List <ActionHistory> findAllWithParams(String type, User author, int pageNumber);
}
