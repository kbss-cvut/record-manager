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
}
