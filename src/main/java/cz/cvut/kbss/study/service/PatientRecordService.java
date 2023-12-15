package cz.cvut.kbss.study.service;

import cz.cvut.kbss.study.dto.PatientRecordDto;
import cz.cvut.kbss.study.model.Institution;
import cz.cvut.kbss.study.model.PatientRecord;
import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.persistence.dao.util.RecordFilterParams;

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
    List<PatientRecordDto> findByInstitution(Institution institution);

    /**
     * Gets records of patients created by specified author.
     *
     * @param author The author to filter by
     * @return Records of matching patients
     */
    List<PatientRecord> findByAuthor(User author);

    /**
     * Gets records of all patients.
     *
     * @return Records of matching patients
     */
    List<PatientRecordDto> findAllRecords();

    /**
     * Finds all records that match the specified parameters.
     * <p>
     * In contrast to {@link #findAll()}, this method returns full records, not DTOs.
     *
     * @param filterParams Record filtering criteria
     * @return List of matching records
     * @see #findAllRecords()
     */
    List<PatientRecord> findAllFull(RecordFilterParams filterParams);
}
