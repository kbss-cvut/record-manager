package cz.cvut.kbss.study.model.qam;

import cz.cvut.kbss.jopa.model.annotations.*;
import cz.cvut.kbss.study.model.AbstractEntity;
import cz.cvut.kbss.study.model.Vocabulary;

import java.util.HashSet;
import java.util.Set;

@OWLClass(iri = Vocabulary.s_c_question_template)
public class QuestionTemplate extends AbstractEntity {

    @OWLDataProperty(iri = Vocabulary.s_p_label)
    private String label;

    @OWLDataProperty(iri = Vocabulary.s_p_has_origin_path_id)
    private String originPathId;

    @OWLObjectProperty(iri = Vocabulary.s_p_has_preceding_template, cascade = {CascadeType.MERGE}, fetch = FetchType.EAGER)
    private Set<QuestionTemplate> precedingTemplate = new HashSet<>();
}
