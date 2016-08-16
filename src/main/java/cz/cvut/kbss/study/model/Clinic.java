package cz.cvut.kbss.study.model;

import cz.cvut.kbss.jopa.CommonVocabulary;
import cz.cvut.kbss.jopa.model.annotations.*;
import cz.cvut.kbss.study.model.util.HasOwlKey;

import java.util.Date;
import java.util.Set;

public class Clinic extends AbstractEntity implements HasOwlKey {

    @ParticipationConstraints(nonEmpty = true)
    @OWLDataProperty(iri = Vocabulary.s_p_key)
    private String key;

    @ParticipationConstraints(nonEmpty = true)
    @OWLAnnotationProperty(iri = CommonVocabulary.RDFS_LABEL)
    private String name;

    @OWLDataProperty(iri = Vocabulary.s_p_mbox)
    private String emailAddress;

    @OWLDataProperty(iri = Vocabulary.s_p_created)
    private Date dateCreated;

    @Inferred
    @OWLObjectProperty(iri = Vocabulary.s_p_has_member, readOnly = true)
    private Set<User> members;

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    /**
     * Gets date when this clinic's account was created.
     */
    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Set<User> getMembers() {
        return members;
    }

    public void setMembers(Set<User> members) {
        this.members = members;
    }

    @Override
    public String toString() {
        return "Clinic{" +
                "name='" + name + '\'' +
                "} " + super.toString();
    }
}
