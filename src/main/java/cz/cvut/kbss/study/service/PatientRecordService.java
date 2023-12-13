package cz.cvut.kbss.study.service;

import cz.cvut.kbss.study.dto.PatientRecordDto;
import cz.cvut.kbss.study.model.Institution;
import cz.cvut.kbss.study.model.PatientRecord;
import cz.cvut.kbss.study.model.User;

import java.time.LocalDate;
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
     * Finds all records that were created or modified in the specified date interval.
     * <p>
     * In contrast to {@link #findAll()}, this method returns full records, not DTOs.
     *
     * @param minDate Minimum date of modification of returned records, inclusive
     * @param maxDate Maximum date of modification of returned records, inclusive
     * @return List of matching records
     * @see #findAllFull(Institution, LocalDate, LocalDate)
     * @see #findAllRecords()
     */
    List<PatientRecord> findAllFull(LocalDate minDate, LocalDate maxDate);

    /**
     * Finds all records that were created or modified at the specified institution in the specified date interval.
     * <p>
     * In contrast to {@link #findByInstitution(Institution)}, this method returns full records, not DTOs.
     *
     * @param institution Institution with which the records are associated
     * @param minDate     Minimum date of modification of returned records, inclusive
     * @param maxDate     Maximum date of modification of returned records, inclusive
     * @return List of matching records
     * @see #findAllFull(LocalDate, LocalDate)
     * @see #findByInstitution(Institution)
     */
    List<PatientRecord> findAllFull(Institution institution, LocalDate minDate, LocalDate maxDate);
}
