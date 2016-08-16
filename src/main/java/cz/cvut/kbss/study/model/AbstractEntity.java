package cz.cvut.kbss.study.model;

import cz.cvut.kbss.jopa.model.annotations.Id;
import cz.cvut.kbss.jopa.model.annotations.MappedSuperclass;
import cz.cvut.kbss.study.model.util.HasUri;

import java.io.Serializable;
import java.net.URI;

/**
 * Abstract superclass for all entities with generated identifier.
 */
@MappedSuperclass
public abstract class AbstractEntity implements Serializable, HasUri {

    @Id(generated = true)
    protected URI uri;

    @Override
    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }
}
