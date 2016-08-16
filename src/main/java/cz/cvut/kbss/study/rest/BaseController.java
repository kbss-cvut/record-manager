package cz.cvut.kbss.study.rest;

import cz.cvut.kbss.study.security.SecurityConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;

@PreAuthorize("hasRole('" + SecurityConstants.ROLE_USER + "')")
public class BaseController {

    protected static final Logger LOG = LoggerFactory.getLogger(BaseController.class);

}
