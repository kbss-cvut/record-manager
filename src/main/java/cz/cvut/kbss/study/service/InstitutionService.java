package cz.cvut.kbss.study.service;

import cz.cvut.kbss.study.model.Institution;

public interface InstitutionService extends BaseService<Institution> {

    /**
     * Finds a institution with the specified key.
     *
     * @param key Institution identifier
     * @return Matching institution or {@code null}
     */
    Institution findByKey(String key);

    /**
     * Finds a institution with the specified name.
     *
     * @param name Institution name
     * @return Matching institution or {@code null}
     */
    Institution findByName(String name);
}
