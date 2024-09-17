package cz.cvut.kbss.study.dto;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents the result of importing records to this instance.
 */
public class RecordImportResult {

    /**
     * Total number of processed records.
     */
    private int totalCount;

    /**
     * Number of successfully imported records.
     */
    private int importedCount;

    private Set<String> importedRecords;

    /**
     * Errors that occurred during import.
     */
    private List<String> errors;

    public RecordImportResult() {
    }

    public RecordImportResult(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getImportedCount() {
        return importedCount;
    }

    public void setImportedCount(int importedCount) {
        this.importedCount = importedCount;
    }


    public Set<String> getImportedRecords() {
        return importedRecords;
    }

    public void setImportedRecords(Set<String> importedRecords) {
        this.importedRecords = importedRecords;
    }

    public void addImportedRecord(String recordIRI) {
        if(recordIRI == null)
            return;
        if(importedRecords == null)
            importedRecords = new HashSet<>();
        importedRecords.add(recordIRI);
        importedCount = importedRecords.size();
    }

    public void incrementImportedCount() {
        this.importedCount++;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public void addError(String error) {
        if (this.errors == null) {
            this.errors = new ArrayList<>();
        }
        errors.add(error);
    }

    @Override
    public String toString() {
        return "RecordImportResult{" +
                "totalCount=" + totalCount +
                ", importedCount=" + importedCount +
                (errors != null ? ", errors=" + errors : "") +
                '}';
    }
}
