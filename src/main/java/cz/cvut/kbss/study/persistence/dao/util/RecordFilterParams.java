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

    private String author;

    private Set<String> institutionKeys = Collections.emptySet();

    private LocalDate minModifiedDate = LocalDate.EPOCH;

    private LocalDate maxModifiedDate = LocalDate.now();

    private Set<String> formTemplateIds = Collections.emptySet();

    private Set<String> phaseIds = Collections.emptySet();

    public RecordFilterParams() {
    }

    public RecordFilterParams(Set<String> institutionKeys) {
        this.institutionKeys = institutionKeys;
    }


    public RecordFilterParams(String author, Set<String> institutionKeys, LocalDate minModifiedDate, LocalDate maxModifiedDate,
                              Set<String> phaseIds, Set<String> formTemplateIds) {
        this.author = author;
        this.institutionKeys = institutionKeys;
        this.minModifiedDate = minModifiedDate;
        this.maxModifiedDate = maxModifiedDate;
        this.phaseIds = phaseIds;
        this.formTemplateIds = formTemplateIds;
    }

    public Optional<String> getAuthor() { return Optional.ofNullable(author); }

    public void setAuthor(String author) { this.author = author; }

    public Set<String> getInstitutionKeys() {
        return institutionKeys;
    }

    public void setInstitutionKeys(Set<String> institutionKeys) {
        this.institutionKeys = institutionKeys;
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

    public Set<String> getFormTemplateIds() {
        return formTemplateIds;
    }

    public void setFormTemplateIds(Set<String> formTemplateIds) {
        this.formTemplateIds = formTemplateIds;
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
        return  Objects.equals(this.author, that.author)
                && Objects.equals(institutionKeys, that.institutionKeys)
                && Objects.equals(minModifiedDate, that.minModifiedDate)
                && Objects.equals(maxModifiedDate, that.maxModifiedDate)
                && Objects.equals(formTemplateIds, that.formTemplateIds)
                && Objects.equals(phaseIds, that.phaseIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(author, institutionKeys, minModifiedDate, maxModifiedDate, formTemplateIds, phaseIds);
    }

    @Override
    public String toString() {
        return "RecordFilter{" +
                "author='" + author + '\'' +
                ", institutionKeys='" + institutionKeys + '\'' +
                ", minModifiedDate=" + minModifiedDate +
                ", maxModifiedDate=" + maxModifiedDate +
                ", formTemplateIds=" + formTemplateIds +
                ", phaseIds=" + phaseIds +
                '}';
    }
}
