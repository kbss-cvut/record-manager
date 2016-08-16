package cz.cvut.kbss.study.service;

import cz.cvut.kbss.study.model.Clinic;
import cz.cvut.kbss.study.model.PatientRecord;

import java.util.List;

public interface PatientRecordService extends BaseService<PatientRecord> {

    /**
     * Finds a record with the specified key.
     *
     * @param key Record identifier
     * @return Matching patient record or {@code null}
     */
    PatientRecord findByKey(String key);

    /**
     * Gets records of patients treated at the specified clinic.
     *
     * @param clinic The clinic to filter by
     * @return Records of matching patients
     */
    List<PatientRecord> findByClinic(Clinic clinic);
}
