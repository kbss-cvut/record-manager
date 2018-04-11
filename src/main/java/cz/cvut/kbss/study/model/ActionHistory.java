package cz.cvut.kbss.study.model;

import cz.cvut.kbss.jopa.model.annotations.*;
import cz.cvut.kbss.study.model.util.HasOwlKey;

import java.net.URI;
import java.util.Date;

@OWLClass(iri = Vocabulary.s_c_action_history)
public class ActionHistory extends AbstractEntity implements HasOwlKey {

    @ParticipationConstraints(nonEmpty = true)
    @OWLDataProperty(iri = Vocabulary.s_p_key)
    private String key;

    @OWLObjectProperty(iri = Vocabulary.s_p_has_owner, fetch = FetchType.EAGER)
    private User author;

    @ParticipationConstraints(nonEmpty = true)
    @OWLDataProperty(iri = Vocabulary.s_p_created)
    private Date timestamp;

    @ParticipationConstraints(nonEmpty = true)
    @OWLDataProperty(iri = Vocabulary.s_p_label)
    private String type;

    @OWLDataProperty(iri = Vocabulary.s_p_has_data_value, fetch = FetchType.LAZY)
    private String payload;

    @Override
    public String toString() {
        return null;
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public void setKey(String key) {
        this.key = key;
    }
}
