package cz.cvut.kbss.study.service;

import cz.cvut.kbss.study.dto.PatientRecordDto;
import cz.cvut.kbss.study.dto.RecordImportResult;
import cz.cvut.kbss.study.model.PatientRecord;
import cz.cvut.kbss.study.model.RecordPhase;
import cz.cvut.kbss.study.model.export.RawRecord;
import cz.cvut.kbss.study.persistence.dao.util.RecordFilterParams;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

public interface PatientRecordService extends BaseService<PatientRecord> {

    /**
     * Finds a record with the specified key.
     *
     * @param key Record identifier
     * @return Matching patient record or {@code null}
     */
    PatientRecord findByKey(String key);

    /**
     * Gets records corresponding to the specified filtering, paging, and sorting criteria.
     *
     * @param filters  Record filtering criteria
     * @param pageSpec Specification of page and sorting to retrieve
     * @return List of matching record DTOs
     * @see #findAllFull(RecordFilterParams, Pageable)
     */
    Page<PatientRecordDto> findAll(RecordFilterParams filters, Pageable pageSpec);

    /**
     * Gets records corresponding to the specified filtering, paging, and sorting criteria.
     *
     * @param filters  Record filtering criteria
     * @param pageSpec Specification of page and sorting to retrieve
     * @return List of matching records
     * @see #findAll(RecordFilterParams, Pageable)
     */
    Page<PatientRecord> findAllFull(RecordFilterParams filters, Pageable pageSpec);

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
     * current user is set as the record's author. Also, if the current user is not an admin, the phase of all the
     * imported records is set to {@link RecordPhase#open}, for admin, the phase of the records is retained.
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
     * @param targetPhase Phase to be set to all imported records.
     * @return Instance representing the import result
     * @throws cz.cvut.kbss.study.exception.RecordAuthorNotFoundException Thrown when importing a record whose author
     *                                                                    does not exist in this application instance's
     *                                                                    repository
     */
    RecordImportResult importRecords(List<PatientRecord> records, RecordPhase targetPhase);

    /**
     *
     * @param filters Record filtering criteria
     * @param pageSpec Specification of page and sorting to retrieve
     * @return List of matching records
     */
    Page<RawRecord> exportRecords(RecordFilterParams filters, Pageable pageSpec);



    /**
     * Retrieves a set of all distinct phases that records can be in.
     * <p>
     * This method provides a way to get a comprehensive list of the various phases
     * associated with records. Phases typically represent different stages or statuses
     * that a record might go through in its lifecycle.
     *
     * @return Set of all available record phases
     */
    Set<RecordPhase> findAllAvailableRecordsPhases();


}
