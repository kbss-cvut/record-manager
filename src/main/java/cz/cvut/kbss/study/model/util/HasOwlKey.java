package cz.cvut.kbss.study.model.util;

/**
 * Marker interface for entities identified by OWL key.
 */
public interface HasOwlKey {

    String getKey();

    void setKey(String key);
}
