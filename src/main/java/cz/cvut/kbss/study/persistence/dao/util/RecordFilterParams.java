package cz.cvut.kbss.study.persistence.dao.util;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Encapsulates {@link cz.cvut.kbss.study.model.PatientRecord} filtering criteria.
 */
public class RecordFilterParams {

    private String institutionKey;

    private LocalDate minModifiedDate = LocalDate.EPOCH;

    private LocalDate maxModifiedDate = LocalDate.now();

    private Set<String> phaseIds = Collections.emptySet();

    public RecordFilterParams() {
    }

    // This one mainly is for test data setup
    public RecordFilterParams(String institutionKey, LocalDate minModifiedDate, LocalDate maxModifiedDate,
                              Set<String> phaseIds) {
        this.institutionKey = institutionKey;
        this.minModifiedDate = minModifiedDate;
        this.maxModifiedDate = maxModifiedDate;
        this.phaseIds = phaseIds;
    }

    public Optional<String> getInstitutionKey() {
        return Optional.ofNullable(institutionKey);
    }

    public void setInstitutionKey(String institutionKey) {
        this.institutionKey = institutionKey;
    }

    public Optional<LocalDate> getMinModifiedDate() {
        return Optional.ofNullable(minModifiedDate);
    }

    public void setMinModifiedDate(LocalDate minModifiedDate) {
        this.minModifiedDate = minModifiedDate;
    }

    public Optional<LocalDate> getMaxModifiedDate() {
        return Optional.ofNullable(maxModifiedDate);
    }

    public void setMaxModifiedDate(LocalDate maxModifiedDate) {
        this.maxModifiedDate = maxModifiedDate;
    }

    public Set<String> getPhaseIds() {
        return phaseIds;
    }

    public void setPhaseIds(Set<String> phaseIds) {
        this.phaseIds = phaseIds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RecordFilterParams that)) {
            return false;
        }
        return Objects.equals(institutionKey, that.institutionKey)
                && Objects.equals(minModifiedDate, that.minModifiedDate)
                && Objects.equals(maxModifiedDate, that.maxModifiedDate)
                && Objects.equals(phaseIds, that.phaseIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(institutionKey, minModifiedDate, maxModifiedDate, phaseIds);
    }

    @Override
    public String toString() {
        return "RecordFilter{" +
                "institutionKey='" + institutionKey + '\'' +
                ", minModifiedDate=" + minModifiedDate +
                ", maxModifiedDate=" + maxModifiedDate +
                ", phaseIds=" + phaseIds +
                '}';
    }
}
