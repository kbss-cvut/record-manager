package cz.cvut.kbss.study.model;

import cz.cvut.kbss.jopa.model.annotations.CascadeType;
import cz.cvut.kbss.jopa.model.annotations.EnumType;
import cz.cvut.kbss.jopa.model.annotations.Enumerated;
import cz.cvut.kbss.jopa.model.annotations.FetchType;
import cz.cvut.kbss.jopa.model.annotations.Id;
import cz.cvut.kbss.jopa.model.annotations.OWLAnnotationProperty;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import cz.cvut.kbss.jopa.model.annotations.OWLObjectProperty;
import cz.cvut.kbss.jopa.model.annotations.ParticipationConstraints;
import cz.cvut.kbss.study.model.qam.Question;
import cz.cvut.kbss.study.model.util.HasOwlKey;
import cz.cvut.kbss.study.model.util.HasUri;

import java.io.Serializable;
import java.net.URI;
import java.util.Date;

@OWLClass(iri = Vocabulary.s_c_patient_record)
public class PatientRecord implements Serializable, HasOwlKey, HasUri {

    @Id
    private URI uri;

    @ParticipationConstraints(nonEmpty = true)
    @OWLDataProperty(iri = Vocabulary.s_p_key)
    private String key;

    @ParticipationConstraints(nonEmpty = true)
    @OWLAnnotationProperty(iri = Vocabulary.s_p_label)
    private String localName;

    @ParticipationConstraints(nonEmpty = true)
    @OWLObjectProperty(iri = Vocabulary.s_p_record_manager_has_author, fetch = FetchType.EAGER)
    private User author;

    @OWLDataProperty(iri = Vocabulary.s_p_created)
    private Date dateCreated;

    @OWLDataProperty(iri = Vocabulary.s_p_modified)
    private Date lastModified;

    @OWLObjectProperty(iri = Vocabulary.s_p_has_last_editor, fetch = FetchType.EAGER)
    private User lastModifiedBy;

    @OWLObjectProperty(iri = Vocabulary.s_p_was_treated_at, fetch = FetchType.EAGER)
    private Institution institution;

    @OWLDataProperty(iri = Vocabulary.s_p_has_form_template)
    private String formTemplate;

    @OWLDataProperty(iri = Vocabulary.s_p_has_form_template_version)
    private String formTemplateVersion;

    @OWLObjectProperty(iri = Vocabulary.s_p_has_question, cascade = {CascadeType.MERGE,
                                                                     CascadeType.REMOVE}, fetch = FetchType.EAGER)
    private Question question;

    @Enumerated(EnumType.OBJECT_ONE_OF)
    @OWLObjectProperty(iri = Vocabulary.s_p_has_phase, cascade = {CascadeType.MERGE}, fetch = FetchType.EAGER)
    private RecordPhase phase;

   @OWLDataProperty(iri = Vocabulary.s_p_reject_reason)
    private String rejectReason;

    @Override
    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

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

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public String getFormTemplate() {
        return formTemplate;
    }

    public void setFormTemplate(String formTemplate) {
        this.formTemplate = formTemplate;
    }

    public String getFormTemplateVersion() {
        return formTemplateVersion;
    }

    public void setFormTemplateVersion(String formTemplateVersion) {
        this.formTemplateVersion = formTemplateVersion;
    }

    public RecordPhase getPhase() {
        return phase;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }


    public void setPhase(RecordPhase phase) {
        this.phase = phase;
    }

    @Override
    public String toString() {
        return "PatientRecord{<" + uri +
                ">, localName=" + localName +
                ", dateCreated=" + dateCreated +
                ", institution=" + institution +
                ", phase=" + phase +
                "}";
    }
}
