package cz.cvut.kbss.study.service;

import cz.cvut.kbss.study.model.ActionHistory;
import cz.cvut.kbss.study.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ActionHistoryService extends BaseService<ActionHistory> {

    ActionHistory findByKey(String key);

    Page<ActionHistory> findAllWithParams(String type, User author, Pageable pageSpec);
}
