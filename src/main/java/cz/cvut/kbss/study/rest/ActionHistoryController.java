package cz.cvut.kbss.study.rest;

import cz.cvut.kbss.study.exception.NotFoundException;
import cz.cvut.kbss.study.model.ActionHistory;
import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.security.SecurityConstants;
import cz.cvut.kbss.study.service.ActionHistoryService;
import cz.cvut.kbss.study.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/history")
public class ActionHistoryController extends BaseController {

    @Autowired
    private ActionHistoryService actionHistoryService;

    @Autowired
    private UserService userService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@RequestBody ActionHistory actionHistory) {
        actionHistoryService.persist(actionHistory);
        if (LOG.isTraceEnabled()) {
            LOG.trace("Action {} successfully created.", actionHistory.getKey());
        }
    }

    @PreAuthorize("hasRole('" + SecurityConstants.ROLE_ADMIN + "')")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ActionHistory> getActions(@RequestParam(value = "author", required = false) String authorUsername,
                                          @RequestParam(value = "type", required = false) String type,
                                          @RequestParam(value = "page") int pageNumber) {
        User author = null;
        if (authorUsername != null) {
            try {
                author = userService.findByUsername(authorUsername);
            } catch (Exception e) {
                return Collections.emptyList();
            }
        }
        return actionHistoryService.findAllWithParams(type, author, pageNumber);
    }

    @PreAuthorize("hasRole('" + SecurityConstants.ROLE_ADMIN + "')")
    @GetMapping(value = "/{key}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ActionHistory getByKey(@PathVariable("key") String key) {
        final ActionHistory action = actionHistoryService.findByKey(key);
        if (action == null) {
            throw NotFoundException.create("ActionHistory", key);
        }
        return action;
    }

}