package cz.cvut.kbss.study.rest;

import cz.cvut.kbss.study.exception.NotFoundException;
import cz.cvut.kbss.study.model.Clinic;
import cz.cvut.kbss.study.rest.exception.BadRequestException;
import cz.cvut.kbss.study.rest.util.RestUtils;
import cz.cvut.kbss.study.service.ClinicService;
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
@RequestMapping("/clinics")
public class ClinicController extends BaseController {

    @Autowired
    private ClinicService clinicService;

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Clinic> getAllClinics() {
        final List<Clinic> clinics = clinicService.findAll();
        Collections.sort(clinics, (a, b) -> a.getName().compareTo(b.getName()));
        return clinics;
    }

    @RequestMapping(value = "/{key}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Clinic findByKey(@PathVariable("key") String key) {
        return findInternal(key);
    }

    private Clinic findInternal(String key) {
        final Clinic result = clinicService.findByKey(key);
        if (result == null) {
            throw NotFoundException.create("Clinic", key);
        }
        return result;
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Void> createClinic(@RequestBody Clinic clinic) {
        clinicService.persist(clinic);
        if (LOG.isTraceEnabled()) {
            LOG.trace("Clinic {} successfully created.", clinic);
        }
        final String key = clinic.getKey();
        final HttpHeaders headers = RestUtils.createLocationHeaderFromCurrentUri("/{key}", key);
        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/{key}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateClinic(@PathVariable("key") String key, @RequestBody Clinic clinic) {
        if (!key.equals(clinic.getKey())) {
            throw new BadRequestException("The passed clinic's key is different from the specified one.");
        }
        final Clinic original = findInternal(key);
        assert original != null;
        clinicService.update(clinic);
        if (LOG.isTraceEnabled()) {
            LOG.trace("Clinic {} successfully updated.", clinic);
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/{key}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteClinic(@PathVariable("key") String key) {
        final Clinic toRemove = findInternal(key);
        clinicService.remove(toRemove);
        if (LOG.isTraceEnabled()) {
            LOG.trace("Clinic {} successfully removed.", toRemove);
        }
    }
}
