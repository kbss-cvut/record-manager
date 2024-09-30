package cz.cvut.kbss.study.rest;

import cz.cvut.kbss.study.dto.PatientRecordDto;
import cz.cvut.kbss.study.exception.NotFoundException;
import cz.cvut.kbss.study.model.Institution;
import cz.cvut.kbss.study.rest.exception.BadRequestException;
import cz.cvut.kbss.study.rest.util.RestUtils;
import cz.cvut.kbss.study.security.SecurityConstants;
import cz.cvut.kbss.study.service.InstitutionService;
import cz.cvut.kbss.study.service.PatientRecordService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;

import static cz.cvut.kbss.study.rest.util.RecordFilterMapper.constructRecordFilter;

@RestController
@PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_USER + "')")
@RequestMapping("/institutions")
public class InstitutionController extends BaseController {

    private final InstitutionService institutionService;

    private final PatientRecordService recordService;
    
    public InstitutionController(InstitutionService institutionService,
                                 PatientRecordService recordService) {
        this.institutionService = institutionService;
        this.recordService = recordService;
    }

    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_ADMIN + "')")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Institution> getAllInstitutions() {
        final List<Institution> institutions = institutionService.findAll();
        institutions.sort(Comparator.comparing(Institution::getName));
        return institutions;
    }

    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_ADMIN + "') " +
     "or hasAuthority('" + SecurityConstants.ROLE_USER + "') and @securityUtils.isMemberOfInstitution(#key)")
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

    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_ADMIN + "') or @securityUtils.isRecordInUsersInstitution(#key)")
    @GetMapping(value = "/{key}/patients", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<PatientRecordDto> getTreatedPatientRecords(@PathVariable("key") String key) {
        final Institution inst = findInternal(key);
        assert inst != null;
        return recordService.findAll(constructRecordFilter("institution", key), Pageable.unpaged()).getContent();
    }

    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_ADMIN + "')")
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

    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_ADMIN + "')")
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

    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_ADMIN + "')")
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
