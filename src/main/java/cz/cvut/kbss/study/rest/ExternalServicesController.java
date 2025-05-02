package cz.cvut.kbss.study.rest;

import cz.cvut.kbss.study.exception.NotFoundException;
import cz.cvut.kbss.study.model.PatientRecord;
import cz.cvut.kbss.study.model.RecordPhase;
import cz.cvut.kbss.study.service.PatientRecordService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/external")
public class ExternalServicesController {
    private final PatientRecordService recordService;

    public ExternalServicesController(PatientRecordService recordService) {
        this.recordService = recordService;
    }

    @GetMapping(value = "/open/{key}")
    public void openRecord(@PathVariable("key") String recordId) {
        PatientRecord record = findInternal(recordId);
        record.setPhase(RecordPhase.open);
        recordService.updateFromExternal(record);
    }

    private PatientRecord findInternal(String key) {
        final PatientRecord record = recordService.findByKey(key);
        if (record == null) {
            throw NotFoundException.create("PatientRecord", key);
        }
        return record;
    }
}
