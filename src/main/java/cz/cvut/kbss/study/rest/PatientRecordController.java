package cz.cvut.kbss.study.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.cvut.kbss.study.dto.PatientRecordDto;
import cz.cvut.kbss.study.dto.RecordImportResult;
import cz.cvut.kbss.study.exception.NotFoundException;
import cz.cvut.kbss.study.exception.ValidationException;
import cz.cvut.kbss.study.model.PatientRecord;
import cz.cvut.kbss.study.model.RecordPhase;
import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.model.export.RawRecord;
import cz.cvut.kbss.study.persistence.dao.util.RecordFilterParams;
import cz.cvut.kbss.study.rest.event.PaginatedResultRetrievedEvent;
import cz.cvut.kbss.study.rest.exception.BadRequestException;
import cz.cvut.kbss.study.rest.util.RecordFilterMapper;
import cz.cvut.kbss.study.rest.util.RestUtils;
import cz.cvut.kbss.study.security.SecurityConstants;
import cz.cvut.kbss.study.service.ConfigReader;
import cz.cvut.kbss.study.service.ExcelRecordConverter;
import cz.cvut.kbss.study.service.PatientRecordService;
import cz.cvut.kbss.study.service.UserService;
import cz.cvut.kbss.study.util.ConfigParam;
import cz.cvut.kbss.study.util.Constants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@PreAuthorize("hasRole('" + SecurityConstants.ROLE_USER + "')")
@RequestMapping("/records")
public class PatientRecordController extends BaseController {

    private final PatientRecordService recordService;

    private final ApplicationEventPublisher eventPublisher;
    private final ExcelRecordConverter excelRecordConverter;
    private final RestTemplate restTemplate;
    private final ConfigReader configReader;
    private ObjectMapper objectMapper;
    private final UserService userService;

    public PatientRecordController(PatientRecordService recordService, ApplicationEventPublisher eventPublisher,
                                   ExcelRecordConverter excelRecordConverter, RestTemplate restTemplate,
                                   ConfigReader configReader, ObjectMapper objectMapper,
                                   UserService userService)  {
        this.recordService = recordService;
        this.eventPublisher = eventPublisher;
        this.excelRecordConverter = excelRecordConverter;
        this.restTemplate = restTemplate;
        this.configReader = configReader;
        this.objectMapper = objectMapper;
        this.userService = userService;
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

    @PreAuthorize("hasRole('" + SecurityConstants.ROLE_ADMIN + "') or @securityUtils.isMemberOfInstitution(#institutionKey)")
    @GetMapping(value="availablePhases", produces = MediaType.APPLICATION_JSON_VALUE)
    public Set<RecordPhase> getAvailableRecordPhases(@RequestParam(value = "institution", required = false) String institutionKey){
        return recordService.findAllAvailableRecordsPhases();
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
    public ResponseEntity<String> createRecord(@RequestBody PatientRecord record) {

        if(userService.getCurrentUser().getInstitution() == null)
            throw new ValidationException("record.save-error.user-not-assigned-to-institution",
                    "User is not assigned to any institution.");

        recordService.persist(record);
        if (LOG.isTraceEnabled()) {
            LOG.trace("Patient record {} successfully created.", record);
        }
        final String key = record.getKey();
        final HttpHeaders headers = RestUtils.createLocationHeaderFromCurrentUri("/{key}", key);
        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }

    @PreAuthorize(
        "hasRole('" + SecurityConstants.ROLE_ADMIN + "') or @securityUtils.isMemberOfInstitution(#institutionKey)")
    @PostMapping(value = "/publish", produces = {MediaType.APPLICATION_JSON_VALUE})
    public RecordImportResult publishRecords(
        @RequestParam(name = "institution", required = false) String institutionKey,
        @RequestParam(required = false) MultiValueMap<String, String> params,
        HttpServletRequest request) {

        String onPublishRecordsServiceUrl = configReader.getConfig(ConfigParam.ON_PUBLISH_RECORDS_SERVICE_URL);
        if(onPublishRecordsServiceUrl == null || onPublishRecordsServiceUrl.isBlank()) {
            LOG.info("No publish service url provided, noop.");
            RecordImportResult result = new RecordImportResult(0);
            result.addError("Cannot publish completed records. Publish server not configured.");
            return result;
        }

       // export
        final Page<PatientRecord> result = recordService.findAllFull(RecordFilterMapper.constructRecordFilter(params),
                RestUtils.resolvePaging(params));
        List<PatientRecord> records = result.getContent();

        // Convert the records to JSON
        String recordsJson;
        try {
            recordsJson = objectMapper.writeValueAsString(records);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert records to JSON", e);
        }

        // Create a MultiValueMap to hold the file part
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(recordsJson.getBytes()) {
            @Override
            public String getFilename() {
                return "records.json";
            }
        });

        // Create HttpEntity
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && !authHeader.isBlank()) {
            headers.set(HttpHeaders.AUTHORIZATION, authHeader);
        } else {
            throw new RuntimeException("Authorization header missing in request");
        }
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // Call the import endpoint
        LOG.debug("Publishing records.");
        ResponseEntity<RecordImportResult> responseEntity = restTemplate.postForEntity(
            onPublishRecordsServiceUrl, requestEntity, RecordImportResult.class);

        // TODO make records published

        LOG.debug("Publish server response: ", responseEntity.getBody());
        return responseEntity.getBody();
    }

    @PostMapping(value = "/import/json", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public RecordImportResult importRecordsJson(@RequestPart("file") MultipartFile file,
                                            @RequestParam(name = "phase", required = false) String phase) {

        List<PatientRecord> records;

        if(file.isEmpty())
            throw new IllegalArgumentException("Cannot import records, missing input file");
        try {
            records =  objectMapper.readValue(file.getBytes(), new TypeReference<List<PatientRecord>>(){});
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse JSON content", e);
        }
        return importRecords(records, phase);
    }

    @PostMapping(value = "/import/excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public RecordImportResult importRecordsExcel(
        @RequestPart("file") MultipartFile file,
        @RequestParam(name = "phase", required = false) String phase) {

        List<PatientRecord> records;

        if(file.isEmpty())
            throw new IllegalArgumentException("Cannot import records, missing input file");

        String excelImportServiceUrl = configReader.getConfig(ConfigParam.EXCEL_IMPORT_SERVICE_URL);

        if (excelImportServiceUrl == null)
            throw new IllegalArgumentException("Cannot import XLS, excelImportServiceUrl is not configured");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("files", file.getResource());

        String request = UriComponentsBuilder.fromHttpUrl(excelImportServiceUrl)
            .queryParam("datasetResource", "@%s".formatted(file.getOriginalFilename()))
            .toUriString();

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<List<PatientRecord>> responseEntity = restTemplate.exchange(
                URI.create(request),
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<List<PatientRecord>>() {}
        );
        records = responseEntity.getBody();
        return importRecords(records, phase);
    }

    @PostMapping(value = "/import/tsv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public RecordImportResult importRecordsTsv(
        @RequestPart("file") MultipartFile file,
        @RequestParam(name = "phase", required = false) String phase) {

        List<PatientRecord> records;

        if(file.isEmpty())
            throw new IllegalArgumentException("Cannot import records, missing input file");

        String excelImportServiceUrl = configReader.getConfig(ConfigParam.EXCEL_IMPORT_SERVICE_URL);

        if (excelImportServiceUrl == null)
            throw new IllegalArgumentException("Cannot import TSV, excelImportServiceUrl is not configured");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("files", file.getResource());

        String request = UriComponentsBuilder.fromHttpUrl(excelImportServiceUrl)
            .queryParam("datasetResource", "@%s".formatted(file.getOriginalFilename()))
            .toUriString();

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<byte[]> responseEntity = restTemplate.postForEntity(
            URI.create(request),
            requestEntity,
            byte[].class
        );

        LOG.info("Import finished with status {}", responseEntity.getStatusCode());
        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            byte[] responseBody = responseEntity.getBody();
            LOG.debug("Response body length is {}", responseBody.length);
        }

        return new RecordImportResult();
    }

    public RecordImportResult importRecords(List<PatientRecord> records, String phase) {
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

        onUpdateRecord(record.getUri());
    }

    private void onUpdateRecord(URI uri){
        Objects.nonNull(uri);
        String onRecordUpdateServiceUrl = Optional.ofNullable(configReader)
                .map(r -> r.getConfig(ConfigParam.ON_UPDATE_RECORD_SERVICE_URL))
                .orElse(null);

        if(onRecordUpdateServiceUrl == null || onRecordUpdateServiceUrl.isBlank()) {
            LOG.debug("No onRecordUpdateServiceUrl service url provided, noop.");
            return;
        }

        LOG.debug("calling onRecordUpdateServiceUrl: {} with parameter {}", onRecordUpdateServiceUrl, uri);
        String requestUrl = UriComponentsBuilder.fromHttpUrl(onRecordUpdateServiceUrl)
                .queryParam("record", uri)
                .toUriString();
        restTemplate.getForObject(requestUrl,String.class);
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
