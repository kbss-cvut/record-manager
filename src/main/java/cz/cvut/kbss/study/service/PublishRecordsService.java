package cz.cvut.kbss.study.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.cvut.kbss.study.config.WebAppConfig;
import cz.cvut.kbss.study.dto.RecordImportResult;
import cz.cvut.kbss.study.model.Record;
import cz.cvut.kbss.study.model.RecordPhase;
import cz.cvut.kbss.study.persistence.dao.util.RecordFilterParams;
import cz.cvut.kbss.study.service.security.SecurityUtils;
import cz.cvut.kbss.study.util.ConfigParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashSet;
import java.util.List;

@Service
public class PublishRecordsService {

    private static final Logger LOG = LoggerFactory.getLogger(PublishRecordsService.class);

    private final RecordService recordService;
    private final ObjectMapper objectMapper;
    private final SecurityUtils securityUtils;
    private final RestTemplate restTemplate;
    private final ConfigReader configReader;

    public PublishRecordsService(RecordService recordService, SecurityUtils securityUtils, RestTemplate restTemplate, ConfigReader configReader) {
        this.recordService = recordService;
        this.objectMapper = WebAppConfig.createJsonObjectMapper();
        this.securityUtils = securityUtils;
        this.restTemplate = restTemplate;
        this.configReader = configReader;
    }

    public RecordImportResult publishRecords(RecordFilterParams filters, Pageable pageSpec){
        String onPublishRecordsServiceUrl = configReader.getConfig(ConfigParam.ON_PUBLISH_RECORDS_SERVICE_URL);
        if(onPublishRecordsServiceUrl == null || onPublishRecordsServiceUrl.isBlank()) {
            LOG.warn("No publish service url configured, noop.");
            RecordImportResult result = new RecordImportResult(0);
            result.addError("Cannot publish completed records. Publish server not configured.");
            return result;
        }

        filters.setPhaseIds(new HashSet<>());
        filters.getPhaseIds().add(RecordPhase.completed.getIri());

        final Page<Record> result = recordService.findAllFull(filters, pageSpec);
        List<Record> records = result.getContent();

        ResponseEntity<RecordImportResult> responseEntity = executePublishRequest(onPublishRecordsServiceUrl, records);

        LOG.debug("Publish server response: {}", responseEntity.getBody());
        RecordImportResult importResult = responseEntity.getBody();
        if(importResult != null && importResult.getImportedRecords() != null)
            recordService.setPhase(importResult.getImportedRecords(), RecordPhase.published);
        return importResult;
    }

    protected ResponseEntity<RecordImportResult> executePublishRequest(String onPublishRecordsServiceUrl, List<Record> records){
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
        String authHeader = securityUtils.getPublishToken();
        if (authHeader != null && !authHeader.isBlank()) {
            headers.setBearerAuth(authHeader);
        } else {
            throw new SecurityException("Could not retrieve publish token.");
        }
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // Call the import endpoint
        LOG.debug("Publishing records.");
        return restTemplate.postForEntity(
                onPublishRecordsServiceUrl, requestEntity, RecordImportResult.class);
    }
}
