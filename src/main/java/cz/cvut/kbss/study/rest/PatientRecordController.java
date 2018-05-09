package cz.cvut.kbss.study.rest;

import cz.cvut.kbss.study.dto.PatientRecordDto;
import cz.cvut.kbss.study.exception.NotFoundException;
import cz.cvut.kbss.study.model.Institution;
import cz.cvut.kbss.study.model.PatientRecord;
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

import java.util.List;

@RestController
@PreAuthorize("hasRole('" + SecurityConstants.ROLE_USER + "')")
@RequestMapping("/records")
public class PatientRecordController extends BaseController {

    @Autowired
    private PatientRecordService recordService;

    @Autowired
    private InstitutionService institutionService;

    @PreAuthorize("hasRole('" + SecurityConstants.ROLE_ADMIN + "') or @securityUtils.isMemberOfInstitution(#institutionKey)")
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<PatientRecordDto> getRecords(@RequestParam(value = "institution", required = false) String institutionKey) {
        return institutionKey != null ? findByInstitution(institutionKey) : recordService.findAllRecords();
    }

    private List<PatientRecordDto> findByInstitution(String institutionKey) {
        final Institution institution = institutionService.findByKey(institutionKey);
        if (institution == null) {
            throw NotFoundException.create("Institution", institutionKey);
        }
        return recordService.findByInstitution(institution);
    }

    @PreAuthorize("hasRole('" + SecurityConstants.ROLE_ADMIN + "') or @securityUtils.isRecordInUsersInstitution(#key)")
    @RequestMapping(value = "/{key}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public PatientRecord getRecord(@PathVariable("key") String key) {
        return findInternal(key);
    }

    private PatientRecord findInternal(String key) {
        final PatientRecord record = recordService.findByKey(key);
        if (record == null) {
            throw NotFoundException.create("PatientRecord", key);
        }
        return record;
    }

    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Void> createRecord(@RequestBody PatientRecord record) {
        recordService.persist(record);
        if (LOG.isTraceEnabled()) {
            LOG.trace("Patient record {} successfully created.", record);
        }
        final String key = record.getKey();
        final HttpHeaders headers = RestUtils.createLocationHeaderFromCurrentUri("/{key}", key);
        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/{key}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateRecord(@PathVariable("key") String key, @RequestBody PatientRecord record) {
        if (!key.equals(record.getKey())) {
            throw new BadRequestException("The passed record's key is different from the specified one.");
        }
        final PatientRecord original = findInternal(key);
        assert original != null;
        recordService.update(record);
        if (LOG.isTraceEnabled()) {
            LOG.trace("Patient record {} successfully updated.", record);
        }
    }

    @RequestMapping(value = "/{key}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeRecord(@PathVariable("key") String key) {
        final PatientRecord toRemove = findInternal(key);
        recordService.remove(toRemove);
        if (LOG.isTraceEnabled()) {
            LOG.trace("Patient record {} successfully removed.", toRemove);
        }
    }
}
