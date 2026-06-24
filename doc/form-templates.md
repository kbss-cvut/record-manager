# Form templates

Record Manager exposes available form templates through
`GET /formGen/formTemplates` (`FormGenController` → `FormGenService.getFormTemplates()`).
That endpoint runs the SPARQL `CONSTRUCT` query
[`src/main/resources/query/findFormTemplates.rq`](../src/main/resources/query/findFormTemplates.rq)
against the form-gen repository (`formGenRepositoryUrl`). This document describes the RDF
pattern a resource must follow to be recognized as a form template, and how to validate
TTL files against it.

## Form template RDF pattern

A valid form template is a named graph whose IRI is the template IRI, containing a
resource (the same IRI) typed `form:form-template` with a label and a comment:

```trig
@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
@prefix form: <http://onto.fel.cvut.cz/ontologies/form/> .

<http://example.org/my-template> {
    <http://example.org/my-template> a form:form-template ;
        rdfs:label   "My template" ;
        rdfs:comment "Description of the template" .
}
```

## Requirements

A resource is recognized as a form template only when **all** of the following hold:

1. **It lives in a named graph whose IRI equals the template IRI.** The query matches
   `GRAPH ?formTemplate { ?formTemplate ... }` — the graph name and the subject are the
   same. Triples in the default graph or in a differently named graph are not seen.
2. **It is typed `form:form-template`** (`http://onto.fel.cvut.cz/ontologies/form/form-template`).
3. **It has an `rdfs:label`** (`http://www.w3.org/2000/01/rdf-schema#label`).
4. **It has an `rdfs:comment`** (`http://www.w3.org/2000/01/rdf-schema#comment`).

The query's `WHERE` clause requires the type, label, and comment **conjunctively**, so a
candidate **missing either the label or the comment is silently dropped** from the result
— it does not appear even when correctly typed. The form-gen repository must also be a
quad store (it must preserve named graphs) and expose a SPARQL endpoint that accepts a
`query` parameter.

## Validating a TTL file

Use the reusable SHACL shape [`doc/form-template-shape.ttl`](form-template-shape.ttl).
For example, with [pySHACL](https://github.com/RDFLib/pySHACL):

```bash
pyshacl -s doc/form-template-shape.ttl -f human your-templates.ttl
```

The shape targets `form:form-template` resources and reports a violation when the
`rdfs:label` or `rdfs:comment` is missing. Resources not typed as templates are ignored,
so the shape can be run against arbitrary TTL files.

**Named-graph caveat:** SHACL validates the merged data graph, not graph names, so the
shape can only enforce the triple-level pattern (requirements 2–4). The
graph-IRI-equals-subject-IRI rule (requirement 1) must be verified separately — for
example by running the authoritative query
[`findFormTemplates.rq`](../src/main/resources/query/findFormTemplates.rq) against the
loaded data and checking that the template is returned.
