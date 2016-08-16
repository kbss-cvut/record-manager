package cz.cvut.kbss.study.service;

import cz.cvut.kbss.study.model.Clinic;

public interface ClinicService extends BaseService<Clinic> {

    /**
     * Finds a clinic with the specified key.
     *
     * @param key Clinic identifier
     * @return Matching clinic or {@code null}
     */
    Clinic findByKey(String key);

    /**
     * Finds a clinic with the specified name.
     *
     * @param name Clinic name
     * @return Matching clinic or {@code null}
     */
    Clinic findByName(String name);
}
