package cz.cvut.kbss.study.model;

import cz.cvut.kbss.jopa.model.annotations.Individual;

public enum RecordPhase {
    @Individual(iri = Vocabulary.s_c_open_record_state)
    open(Vocabulary.s_c_open_record_state),
    @Individual(iri = Vocabulary.s_c_valid_record_state)
    valid(Vocabulary.s_c_valid_record_state),
    @Individual(iri = Vocabulary.s_c_completed_record_state)
    completed(Vocabulary.s_c_completed_record_state),
    @Individual(iri = Vocabulary.s_c_published_record_state)
    published(Vocabulary.s_c_published_record_state);

    private final String iri;

    RecordPhase(String iri) {
        this.iri = iri;
    }

    public String getIri() {
        return iri;
    }
}
