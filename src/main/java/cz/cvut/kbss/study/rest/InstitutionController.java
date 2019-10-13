package cz.cvut.kbss.study.rest;

import cz.cvut.kbss.study.dto.PatientRecordDto;
import cz.cvut.kbss.study.exception.NotFoundException;
import cz.cvut.kbss.study.model.Institution;
import cz.cvut.kbss.study.rest.exception.BadRequestException;
import cz.cvut.kbss.study.rest.util.RestUtils;
import cz.cvut.kbss.study.security.SecurityConstants;
import cz.cvut.kbss.study.service.InstitutionService;
import cz.cvut.kbss.study.service.PatientRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@PreAuthorize("hasRole('" + SecurityConstants.ROLE_USER + "')")
@RequestMapping("/institutions")
public class InstitutionController extends BaseController {

    @Autowired
    private InstitutionService institutionService;

    @Autowired
    private PatientRecordService recordService;

    @PreAuthorize("hasRole('" + SecurityConstants.ROLE_ADMIN + "')")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Institution> getAllInstitutions() {
        final List<Institution> institutions = institutionService.findAll();
        Collections.sort(institutions, (a, b) -> a.getName().compareTo(b.getName()));
        return institutions;
    }

    @PreAuthorize("hasRole('" + SecurityConstants.ROLE_ADMIN + "') " +
     "or hasRole('" + SecurityConstants.ROLE_USER + "') and @securityUtils.isMemberOfInstitution(#key)")
    @GetMapping(value = "/{key}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Institution findByKey(@PathVariable("key") String key) {
        return findInternal(key);
    }

    private Institution findInternal(String key) {
        final Institution result = institutionService.findByKey(key);
        if (result == null) {
            throw NotFoundException.create("Institution", key);
        }
        return result;
    }

    @PreAuthorize("hasRole('" + SecurityConstants.ROLE_ADMIN + "') or @securityUtils.isRecordInUsersInstitution(#key)")
    @GetMapping(value = "/{key}/patients", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<PatientRecordDto> getTreatedPatientRecords(@PathVariable("key") String key) {
        final Institution institution = findInternal(key);
        return recordService.findByInstitution(institution);
    }

    @PreAuthorize("hasRole('" + SecurityConstants.ROLE_ADMIN + "')")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Void> createInstitution(@RequestBody Institution institution) {
        institutionService.persist(institution);
        if (LOG.isTraceEnabled()) {
            LOG.trace("Institution {} successfully created.", institution);
        }
        final String key = institution.getKey();
        final HttpHeaders headers = RestUtils.createLocationHeaderFromCurrentUri("/{key}", key);
        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('" + SecurityConstants.ROLE_ADMIN + "')")
    @PutMapping(value = "/{key}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateInstitution(@PathVariable("key") String key, @RequestBody Institution institution) {
        if (!key.equals(institution.getKey())) {
            throw new BadRequestException("The passed institution's key is different from the specified one.");
        }
        final Institution original = findInternal(key);
        assert original != null;
        institutionService.update(institution);
        if (LOG.isTraceEnabled()) {
            LOG.trace("Institution {} successfully updated.", institution);
        }
    }

    @PreAuthorize("hasRole('" + SecurityConstants.ROLE_ADMIN + "')")
    @DeleteMapping(value = "/{key}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteInstitution(@PathVariable("key") String key) {
        final Institution toRemove = findInternal(key);
        institutionService.remove(toRemove);
        if (LOG.isTraceEnabled()) {
            LOG.trace("Institution {} successfully removed.", toRemove);
        }
    }
}
