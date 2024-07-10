package cz.cvut.kbss.study.rest;

import cz.cvut.kbss.study.dto.PatientRecordDto;
import cz.cvut.kbss.study.dto.RecordImportResult;
import cz.cvut.kbss.study.exception.NotFoundException;
import cz.cvut.kbss.study.model.PatientRecord;
import cz.cvut.kbss.study.model.RecordPhase;
import cz.cvut.kbss.study.model.export.RawRecord;
import cz.cvut.kbss.study.persistence.dao.util.RecordFilterParams;
import cz.cvut.kbss.study.rest.event.PaginatedResultRetrievedEvent;
import cz.cvut.kbss.study.rest.exception.BadRequestException;
import cz.cvut.kbss.study.rest.util.RecordFilterMapper;
import cz.cvut.kbss.study.rest.util.RestUtils;
import cz.cvut.kbss.study.security.SecurityConstants;
import cz.cvut.kbss.study.service.ExcelRecordConverter;
import cz.cvut.kbss.study.service.PatientRecordService;
import cz.cvut.kbss.study.util.Constants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.collections4.EnumerationUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Stream;

@RestController
@PreAuthorize("hasRole('" + SecurityConstants.ROLE_USER + "')")
@RequestMapping("/records")
public class PatientRecordController extends BaseController {

    private final PatientRecordService recordService;

    private final ApplicationEventPublisher eventPublisher;
    private final ExcelRecordConverter excelRecordConverter;

    public PatientRecordController(PatientRecordService recordService, ApplicationEventPublisher eventPublisher, ExcelRecordConverter excelRecordConverter) {
        this.recordService = recordService;
        this.eventPublisher = eventPublisher;
        this.excelRecordConverter = excelRecordConverter;
    }

    @PreAuthorize("hasRole('" + SecurityConstants.ROLE_ADMIN + "') or @securityUtils.isMemberOfInstitution(#institutionKey)")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<PatientRecordDto> getRecords(
            @RequestParam(value = "institution", required = false) String institutionKey,
            @RequestParam MultiValueMap<String, String> params,
            UriComponentsBuilder uriBuilder, HttpServletResponse response) {
        final Page<PatientRecordDto> result = recordService.findAll(RecordFilterMapper.constructRecordFilter(params),
                                                                    RestUtils.resolvePaging(params));
        eventPublisher.publishEvent(new PaginatedResultRetrievedEvent(this, uriBuilder, response, result));
        return result.getContent();
    }

    @PreAuthorize(
            "hasRole('" + SecurityConstants.ROLE_ADMIN + "') or @securityUtils.isMemberOfInstitution(#institutionKey)")
    @GetMapping(value = "/export", produces = {MediaType.APPLICATION_JSON_VALUE, Constants.MEDIA_TYPE_EXCEL})
    public ResponseEntity<?> exportRecords(
            @RequestParam(name = "institution", required = false) String institutionKey,
            @RequestParam(required = false) MultiValueMap<String, String> params,
            UriComponentsBuilder uriBuilder, HttpServletRequest request, HttpServletResponse response) {

        MediaType exportType = Stream.of(
                        Optional.ofNullable(params).map(p -> p.get(Constants.EXPORT_TYPE_PARAM)),
                        Optional.of(Collections.list(request.getHeaders(HttpHeaders.ACCEPT)))
                )
                .map(o -> o.filter(l -> !l.isEmpty()))
                .filter(Optional::isPresent)
                .map(o -> o.map(l -> l.stream().flatMap(s ->
                                MediaType.parseMediaTypes(s).stream()
                                        .filter(RestUtils::isSupportedExportType)
                        ).max(Comparator.comparing(MediaType::getQualityValue)).orElse(null)))
                .filter(Optional::isPresent)
                .map(o -> o.orElse(null))
                .findFirst()
                .orElse(MediaType.APPLICATION_JSON)
                .removeQualityValue();

        return switch (exportType.toString()){
            case Constants.MEDIA_TYPE_EXCEL ->  exportRecordsExcel(params, uriBuilder, response);
            case MediaType.APPLICATION_JSON_VALUE -> exportRecordsAsJson(params, uriBuilder, response);
            default -> throw new IllegalArgumentException("Unsupported export type: " + exportType);
        };
    }

    protected ResponseEntity<List<PatientRecord>> exportRecordsAsJson(
            MultiValueMap<String, String> params,
            UriComponentsBuilder uriBuilder, HttpServletResponse response){
        final Page<PatientRecord> result = recordService.findAllFull(RecordFilterMapper.constructRecordFilter(params),
                RestUtils.resolvePaging(params));
        eventPublisher.publishEvent(new PaginatedResultRetrievedEvent(this, uriBuilder, response, result));
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(result.getContent());
    }

    public ResponseEntity<InputStreamResource> exportRecordsExcel(MultiValueMap<String, String> params,
                                                                  UriComponentsBuilder uriBuilder, HttpServletResponse response){
        RecordFilterParams filterParams = new RecordFilterParams();
        filterParams.setMinModifiedDate(null);
        filterParams.setMaxModifiedDate(null);
        RecordFilterMapper.constructRecordFilter(filterParams, params);

        Page<RawRecord> result = recordService.exportRecords(filterParams, RestUtils.resolvePaging(params));

        InputStream stream = excelRecordConverter.convert(result.getContent());
        eventPublisher.publishEvent(new PaginatedResultRetrievedEvent(this, uriBuilder, response, result));
        ContentDisposition contentDisposition = ContentDisposition.attachment().filename("export.xlsx").build();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(Constants.MEDIA_TYPE_EXCEL))
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .body(new InputStreamResource(stream));
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
    public RecordImportResult importRecords(@RequestBody List<PatientRecord> records,
                                            @RequestParam(name = "phase", required = false) String phase) {
        final RecordImportResult importResult;
        if (phase != null) {
            final RecordPhase targetPhase = RecordPhase.fromIriOrName(phase);
            importResult = recordService.importRecords(records, targetPhase);
        } else {
            importResult = recordService.importRecords(records);
        }
        LOG.trace("Records imported with result: {}.", importResult);
        return importResult;
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
