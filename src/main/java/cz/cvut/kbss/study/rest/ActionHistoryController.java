package cz.cvut.kbss.study.rest;

import cz.cvut.kbss.study.model.ActionHistory;
import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.rest.util.RestUtils;
import cz.cvut.kbss.study.security.SecurityConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import cz.cvut.kbss.study.service.ActionHistoryService;

import java.util.List;

@RestController
@RequestMapping("/action-history")
public class ActionHistoryController extends BaseController {

    @Autowired
    private ActionHistoryService actionHistoryService;

    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void create(@RequestBody ActionHistory actionHistory) {
        actionHistoryService.persist(actionHistory);
        if (LOG.isTraceEnabled()) {
            LOG.trace("User {} successfully registered.", actionHistory);
        }
    }

    @PreAuthorize("hasRole('" + SecurityConstants.ROLE_ADMIN + "')")
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ActionHistory> getActions() {
        return actionHistoryService.findAll();
    }

}
