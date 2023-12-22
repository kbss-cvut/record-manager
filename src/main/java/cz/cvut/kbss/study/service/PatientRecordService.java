package cz.cvut.kbss.study.service;

import cz.cvut.kbss.study.dto.PatientRecordDto;
import cz.cvut.kbss.study.dto.RecordImportResult;
import cz.cvut.kbss.study.model.Institution;
import cz.cvut.kbss.study.model.PatientRecord;
import cz.cvut.kbss.study.model.RecordPhase;
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

    /**
     * Imports the specified records.
     * <p>
     * Only records whose identifiers do not already exist in the repository are imported. Existing records are skipped
     * and the returned object contains a note that the record already exists.
     * <p>
     * This method, in contrast to {@link #importRecords(List, RecordPhase)}, preserves the phase of the imported
     * records.
     * <p>
     * If the current user is an admin, the import procedure retains provenance data of the record. Otherwise, the
     * current user is set as the record's author. Also, if the current user is not an admin, the phase of all
     * the imported records is set to {@link RecordPhase#open}, for admin, the phase of the records is retained.
     *
     * @param records Records to import
     * @return Instance representing the import result
     * @throws cz.cvut.kbss.study.exception.RecordAuthorNotFoundException Thrown when importing a record whose author
     *                                                                    does not exist in this application instance's
     *                                                                    repository
     */
    RecordImportResult importRecords(List<PatientRecord> records);

    /**
     * Imports the specified records and sets them all to the specified phase.
     * <p>
     * Only records whose identifiers do not already exist in the repository are imported. Existing records are skipped
     * and the returned object contains a note that the record already exists.
     * <p>
     * If the current user is an admin, the import procedure retains provenance data of the record. Otherwise, the
     * current user is set as the record's author.
     *
     * @param records Records to import
     * @return Instance representing the import result
     * @throws cz.cvut.kbss.study.exception.RecordAuthorNotFoundException Thrown when importing a record whose author
     *                                                                    does not exist in this application instance's
     *                                                                    repository
     */
    RecordImportResult importRecords(List<PatientRecord> records, RecordPhase targetPhase);
}
