package cz.cvut.kbss.study.model.qam;

import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import cz.cvut.kbss.study.model.AbstractEntity;
import cz.cvut.kbss.study.model.Vocabulary;

import java.util.Date;

@OWLClass(iri = Vocabulary.s_c_comment)
public class Comment extends AbstractEntity {

    @OWLDataProperty(iri = Vocabulary.s_p_has_comment_value)
    private String value;

    @OWLDataProperty(iri = Vocabulary.s_p_has_timestamp)
    private Date timestamp;

    public Comment(String value, Date created) {
        this.value = value;
        this.timestamp = created;
    }

    public Comment() {
    }

    public Comment(Comment other) {
        this.value = other.value;
        this.timestamp = other.timestamp;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "value='" + value + '\'' +
                ", created=" + timestamp +
                '}';
    }

}
