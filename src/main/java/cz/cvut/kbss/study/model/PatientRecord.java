package cz.cvut.kbss.study.model;

import cz.cvut.kbss.jopa.model.annotations.*;
import cz.cvut.kbss.study.model.qam.Question;
import cz.cvut.kbss.study.model.util.HasOwlKey;

import java.util.Date;

@OWLClass(iri = Vocabulary.s_c_patient_record)
public class PatientRecord extends AbstractEntity implements HasOwlKey {

    @ParticipationConstraints(nonEmpty = true)
    @OWLDataProperty(iri = Vocabulary.s_p_key)
    private String key;

    @ParticipationConstraints(nonEmpty = true)
    @OWLAnnotationProperty(iri = Vocabulary.s_p_label)
    private String localName;

    @OWLDataProperty(iri = Vocabulary.s_p_is_complete)
    private Boolean complete;

    @ParticipationConstraints(nonEmpty = true)
    @OWLObjectProperty(iri = Vocabulary.s_p_has_author, fetch = FetchType.EAGER)
    private User author;

    @OWLDataProperty(iri = Vocabulary.s_p_created)
    private Date dateCreated;

    @OWLDataProperty(iri = Vocabulary.s_p_modified)
    private Date lastModified;

    @OWLObjectProperty(iri = Vocabulary.s_p_has_last_editor, fetch = FetchType.EAGER)
    private User lastModifiedBy;

    @OWLObjectProperty(iri = Vocabulary.s_p_was_treated_at, fetch = FetchType.EAGER)
    private Clinic clinic;

    @OWLObjectProperty(iri = Vocabulary.s_p_has_question, cascade = {CascadeType.MERGE,
            CascadeType.REMOVE}, fetch = FetchType.EAGER)
    private Question question;

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

    public Boolean isComplete() {
        return complete;
    }

    public void setComplete(Boolean complete) {
        this.complete = complete;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
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

    public User getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(User lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public Clinic getClinic() {
        return clinic;
    }

    public void setClinic(Clinic clinic) {
        this.clinic = clinic;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    @Override
    public String toString() {
        return "PatientRecord{" +
                "localName=" + localName +
                "dateCreated=" + dateCreated +
                ", clinic=" + clinic +
                "} " + super.toString();
    }
}
