package cz.cvut.kbss.study.model.export;

import cz.cvut.kbss.jopa.model.annotations.*;
import cz.cvut.kbss.study.model.AbstractEntity;

@SparqlResultSetMappings(value =
@SparqlResultSetMapping(name = "NamedItem", entities = {
        @EntityResult(entityClass = NamedItem.class)
})
)
@OWLClass(iri = "http://named-item")
public class NamedItem extends AbstractEntity {
    @OWLDataProperty(iri = "http://name")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
