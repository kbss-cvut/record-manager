package cz.cvut.kbss.study.dto;

import cz.cvut.kbss.jopa.model.annotations.*;
import cz.cvut.kbss.study.model.*;
import cz.cvut.kbss.study.model.util.HasOwlKey;

import java.net.URI;
import java.util.Date;

@OWLClass(iri = Vocabulary.s_c_patient_record)
public class PatientRecordDto extends AbstractEntity implements HasOwlKey {

    @ParticipationConstraints(nonEmpty = true)
    @OWLDataProperty(iri = Vocabulary.s_p_key)
    private String key;

    @OWLDataProperty(iri = Vocabulary.s_p_has_form_template)
    private String formTemplate;

    @ParticipationConstraints(nonEmpty = true)
    @OWLAnnotationProperty(iri = Vocabulary.s_p_label)
    private String localName;

    @ParticipationConstraints(nonEmpty = true)
    @OWLObjectProperty(iri = Vocabulary.s_p_has_author)
    private URI author;

    @OWLDataProperty(iri = Vocabulary.s_p_created)
    private Date dateCreated;

    @OWLDataProperty(iri = Vocabulary.s_p_modified)
    private Date lastModified;

    @OWLObjectProperty(iri = Vocabulary.s_p_has_last_editor)
    private URI lastModifiedBy;

    @OWLObjectProperty(iri = Vocabulary.s_p_was_treated_at, fetch = FetchType.EAGER)
    private Institution institution;

    @Enumerated(EnumType.OBJECT_ONE_OF)
    @OWLObjectProperty(iri = Vocabulary.s_p_has_phase)
    private RecordPhase phase;

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public void setKey(String key) {
        this.key = key;
    }

    public String getLocalName() {
        return localName;
    }

    public void setLocalName(String localName) {
        this.localName = localName;
    }

    public URI getAuthor() {
        return author;
    }

    public void setAuthor(URI author) {
        this.author = author;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public URI getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(URI lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public String getFormTemplate() {
        return formTemplate;
    }

    public void setFormTemplate(String formTemplate) {
        this.formTemplate = formTemplate;
    }

    public RecordPhase getPhase() {
        return phase;
    }

    public void setPhase(RecordPhase phase) {
        this.phase = phase;
    }

    @Override
    public String toString() {
        return "PatientRecordDto{" +
                "localName=" + localName +
                "dateCreated=" + dateCreated +
                ", institution=" + institution +
                ", phase=" + phase +
                "} " + super.toString();
    }
}
