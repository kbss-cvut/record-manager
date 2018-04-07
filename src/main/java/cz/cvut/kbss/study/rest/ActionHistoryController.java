package cz.cvut.kbss.study.rest;

import cz.cvut.kbss.study.exception.NotFoundException;
import cz.cvut.kbss.study.model.ActionHistory;
import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.security.SecurityConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import cz.cvut.kbss.study.service.ActionHistoryService;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/history")
public class ActionHistoryController extends BaseController {

    @Autowired
    private ActionHistoryService actionHistoryService;

    @Autowired
    private UserController userController;

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
    public List<ActionHistory> getActions(@RequestParam(value = "author", required = false) String authorUsername,
                                          @RequestParam(value = "type", required = false) String type,
                                          @RequestParam(value = "page") int pageNumber) {
        List<ActionHistory> actions;
        if (authorUsername != null && type != null) {
            actions = getByTypeAndAuthor(type, authorUsername, pageNumber);
        } else if (authorUsername != null) {
            actions = getByAuthor(authorUsername, pageNumber);
        } else if (type != null) {
            actions = actionHistoryService.findByType(type, pageNumber);
        } else {
            actions = actionHistoryService.findAllByOrderAsc(pageNumber);
        }
        return actions;
    }

    @PreAuthorize("hasRole('" + SecurityConstants.ROLE_ADMIN + "')")
    @RequestMapping(method = RequestMethod.GET, value = "/{key}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ActionHistory getByKey(@PathVariable("key") String key) {
        final ActionHistory action = actionHistoryService.findByKey(key);
        if (action == null) {
            throw NotFoundException.create("ActionHistory", key);
        }
        return action;
    }

    private List<ActionHistory> getByAuthor(String authorUsername, int pageNumber) {
        assert authorUsername != null;
        final User author;
        try {
            author = userController.getByUsername(authorUsername);
        } catch (Exception e) {
            return Collections.emptyList();
        }
        return actionHistoryService.findByAuthor(author, pageNumber);
    }

    private List<ActionHistory> getByTypeAndAuthor(String type, String authorUsername, int pageNumber) {
        assert authorUsername != null;
        final User author;
        try {
            author = userController.getByUsername(authorUsername);
        } catch (Exception e) {
            return Collections.emptyList();
        }
        return actionHistoryService.findByTypeAndAuthor(type, author, pageNumber);
    }
}