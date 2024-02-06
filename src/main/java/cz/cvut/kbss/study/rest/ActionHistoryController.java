package cz.cvut.kbss.study.rest;

import cz.cvut.kbss.study.exception.NotFoundException;
import cz.cvut.kbss.study.model.ActionHistory;
import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.rest.event.PaginatedResultRetrievedEvent;
import cz.cvut.kbss.study.rest.util.RestUtils;
import cz.cvut.kbss.study.security.SecurityConstants;
import cz.cvut.kbss.study.service.ActionHistoryService;
import cz.cvut.kbss.study.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/history")
public class ActionHistoryController extends BaseController {

    private final ActionHistoryService actionHistoryService;

    private final UserService userService;

    private final ApplicationEventPublisher eventPublisher;

    public ActionHistoryController(ActionHistoryService actionHistoryService,
                                   UserService userService, ApplicationEventPublisher eventPublisher) {
        this.actionHistoryService = actionHistoryService;
        this.userService = userService;
        this.eventPublisher = eventPublisher;
    }

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
                                          @RequestParam MultiValueMap<String, String> params,
                                          UriComponentsBuilder uriBuilder, HttpServletResponse response) {
        User author = null;
        if (authorUsername != null) {
            try {
                author = userService.findByUsername(authorUsername);
            } catch (Exception e) {
                return Collections.emptyList();
            }
        }
        final Page<ActionHistory> result = actionHistoryService.findAllWithParams(type, author, RestUtils.resolvePaging(params));
        eventPublisher.publishEvent(new PaginatedResultRetrievedEvent(this, uriBuilder, response, result));
        return result.getContent();
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