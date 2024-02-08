package cz.cvut.kbss.study.model;

import cz.cvut.kbss.jopa.model.annotations.Individual;

public enum RecordPhase {
    @Individual(iri = Vocabulary.s_c_open_record_phase)
    open(Vocabulary.s_c_open_record_phase),
    @Individual(iri = Vocabulary.s_c_valid_record_phase)
    valid(Vocabulary.s_c_valid_record_phase),
    @Individual(iri = Vocabulary.s_c_completed_record_phase)
    completed(Vocabulary.s_c_completed_record_phase),
    @Individual(iri = Vocabulary.s_c_published_record_phase)
    published(Vocabulary.s_c_published_record_phase),
    @Individual(iri = Vocabulary.s_c_rejected_record_phase)
    rejected(Vocabulary.s_c_rejected_record_phase);

    private final String iri;

    RecordPhase(String iri) {
        this.iri = iri;
    }

    public String getIri() {
        return iri;
    }

    /**
     * Returns {@link RecordPhase} with the specified IRI.
     *
     * @param iri record phase identifier
     * @return matching {@code RecordPhase}
     * @throws IllegalArgumentException When no matching phase is found
     */
    public static RecordPhase fromIri(String iri) {
        for (RecordPhase p : values()) {
            if (p.getIri().equals(iri)) {
                return p;
            }
        }
        throw new IllegalArgumentException("Unknown record phase identifier '" + iri + "'.");
    }

    /**
     * Returns {@link RecordPhase} with the specified constant name.
     *
     * @param name record phase name
     * @return matching {@code RecordPhase}
     * @throws IllegalArgumentException When no matching phase is found
     */
    public static RecordPhase fromName(String name) {
        for (RecordPhase p : values()) {
            if (p.name().equalsIgnoreCase(name)) {
                return p;
            }
        }
        throw new IllegalArgumentException("Unknown record phase '" + name + "'.");
    }

    /**
     * Returns a {@link RecordPhase} with the specified IRI or constant name.
     * <p>
     * This function first tries to find the enum constant by IRI. If it is not found, constant name matching is
     * attempted.
     *
     * @param identification Constant IRI or name to find match by
     * @return matching {@code RecordPhase}
     * @throws IllegalArgumentException When no matching phase is found
     */
    public static RecordPhase fromIriOrName(String identification) {
        try {
            return fromIri(identification);
        } catch (IllegalArgumentException e) {
            return fromName(identification);
        }
    }
}
