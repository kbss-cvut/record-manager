package cz.cvut.kbss.study.persistence.dao;

import cz.cvut.kbss.study.model.ActionHistory;
import org.springframework.stereotype.Repository;

@Repository
public class ActionHistoryDao extends OwlKeySupportingDao<ActionHistory>{

    public ActionHistoryDao() {
        super(ActionHistory.class);
    }

}
