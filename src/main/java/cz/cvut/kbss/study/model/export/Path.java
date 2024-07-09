package cz.cvut.kbss.study.model.export;


import cz.cvut.kbss.jopa.model.annotations.*;
import cz.cvut.kbss.study.model.AbstractEntity;

import java.net.URI;

@SparqlResultSetMappings(value =
@SparqlResultSetMapping(name = "Path", entities = {
        @EntityResult(entityClass = Path.class)
})
)
@OWLClass(iri = "http://path")
public class Path extends AbstractEntity {

    @OWLDataProperty(iri = "http://l1")
    protected URI l1;
    @OWLDataProperty(iri = "http://l2")
    protected URI l2;
    @OWLDataProperty(iri = "http://l3")
    protected URI l3;
    @OWLDataProperty(iri = "http://l4")
    protected URI l4;
    @OWLDataProperty(iri = "http://l5")
    protected URI l5;

    public URI getL1() {
        return l1;
    }

    public void setL1(URI l1) {
        this.l1 = l1;
    }

    public URI getL2() {
        return l2;
    }

    public void setL2(URI l2) {
        this.l2 = l2;
    }

    public URI getL3() {
        return l3;
    }

    public void setL3(URI l3) {
        this.l3 = l3;
    }

    public URI getL4() {
        return l4;
    }

    public void setL4(URI l4) {
        this.l4 = l4;
    }

    public URI getL5() {
        return l5;
    }

    public void setL5(URI l5) {
        this.l5 = l5;
    }
}
