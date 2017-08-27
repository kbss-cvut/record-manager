package cz.cvut.kbss.study.service;

import cz.cvut.kbss.study.model.Institution;
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
     * Gets records of patients treated at the specified institution.
     *
     * @param institution The institution to filter by
     * @return Records of matching patients
     */
    List<PatientRecord> findByInstitution(Institution institution);
}
