package cz.cvut.kbss.study.rest;

import cz.cvut.kbss.study.dto.PatientRecordDto;
import cz.cvut.kbss.study.dto.RecordImportResult;
import cz.cvut.kbss.study.exception.NotFoundException;
import cz.cvut.kbss.study.model.Institution;
import cz.cvut.kbss.study.model.PatientRecord;
import cz.cvut.kbss.study.rest.exception.BadRequestException;
import cz.cvut.kbss.study.rest.util.RecordFilterMapper;
import cz.cvut.kbss.study.rest.util.RestUtils;
import cz.cvut.kbss.study.security.SecurityConstants;
import cz.cvut.kbss.study.service.InstitutionService;
import cz.cvut.kbss.study.service.PatientRecordService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@PreAuthorize("hasRole('" + SecurityConstants.ROLE_USER + "')")
@RequestMapping("/records")
public class PatientRecordController extends BaseController {

    private final PatientRecordService recordService;

    private final InstitutionService institutionService;

    public PatientRecordController(PatientRecordService recordService, InstitutionService institutionService) {
        this.recordService = recordService;
        this.institutionService = institutionService;
    }

    @PreAuthorize("hasRole('" + SecurityConstants.ROLE_ADMIN + "') or @securityUtils.isMemberOfInstitution(#institutionKey)")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<PatientRecordDto> getRecords(
            @RequestParam(value = "institution", required = false) String institutionKey) {
        return institutionKey != null ? recordService.findByInstitution(getInstitution(institutionKey)) :
               recordService.findAllRecords();
    }

    private Institution getInstitution(String institutionKey) {
        final Institution institution = institutionService.findByKey(institutionKey);
        if (institution == null) {
            throw NotFoundException.create("Institution", institutionKey);
        }
        return institution;
    }

    @PreAuthorize(
            "hasRole('" + SecurityConstants.ROLE_ADMIN + "') or @securityUtils.isMemberOfInstitution(#institutionKey)")
    @GetMapping(value = "/export", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<PatientRecord> exportRecords(
            @RequestParam(name = "institutionKey", required = false) String institutionKey,
            @RequestParam MultiValueMap<String, String> params) {
        return recordService.findAllFull(RecordFilterMapper.constructRecordFilter(params));
    }

    @PreAuthorize("hasRole('" + SecurityConstants.ROLE_ADMIN + "') or @securityUtils.isRecordInUsersInstitution(#key)")
    @GetMapping(value = "/{key}", produces = MediaType.APPLICATION_JSON_VALUE)
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

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
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

    @PostMapping(value = "/import", consumes = MediaType.APPLICATION_JSON_VALUE)
    public RecordImportResult importRecords(@RequestBody List<PatientRecord> records) {
        final RecordImportResult result = recordService.importRecords(records);
        LOG.trace("Records imported with result: {}.", result);
        return result;
    }

    @PutMapping(value = "/{key}", consumes = MediaType.APPLICATION_JSON_VALUE)
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

    @DeleteMapping(value = "/{key}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeRecord(@PathVariable("key") String key) {
        final PatientRecord toRemove = findInternal(key);
        recordService.remove(toRemove);
        if (LOG.isTraceEnabled()) {
            LOG.trace("Patient record {} successfully removed.", toRemove);
        }
    }
}
