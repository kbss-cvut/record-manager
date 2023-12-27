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
    public static RecordPhase fromString(String iri) {
        for (RecordPhase p : values()) {
            if (p.getIri().equals(iri)) {
                return p;
            }
        }
        throw new IllegalArgumentException("Unknown record phase identifier '" + iri + "'.");
    }
}
