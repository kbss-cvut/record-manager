package cz.cvut.kbss.study.persistence.dao;

import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.kbss.study.model.Vocabulary;
import cz.cvut.kbss.study.model.export.NamedItem;
import cz.cvut.kbss.study.model.export.Path;
import org.springframework.stereotype.Repository;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class CodeListValuesDao {

    private final EntityManager em;

    public CodeListValuesDao(EntityManager em) {
        this.em = em;
    }



    public List<NamedItem> findClassificationOfOccurrence() {
        return findItems(URI.create("http://vfn.cz/ontologies/ava-study/model/classification-of-occurrence"));
    }


    public List<NamedItem> findConsequence() {
        return findItems(URI.create("http://vfn.cz/ontologies/ava-study/model/consequence"));
    }


    public List<NamedItem> findFailureAscertainmentCircumstances() {
        return findItems(URI.create("http://vfn.cz/ontologies/ava-study/model/failure-ascertainment-circumstances"));
    }


    public List<NamedItem> findFailureCause() {
        return findItems(URI.create("http://vfn.cz/ontologies/ava-study/model/failure-cause"));
    }


    public List<NamedItem> findFhaEvent() {
        return findItems(URI.create("http://vfn.cz/ontologies/ava-study/model/fha-event"));
    }


    public List<NamedItem> findMission() {
        return findItems(URI.create("http://vfn.cz/ontologies/ava-study/model/mission"));
    }


    public List<NamedItem> findRepair() {
        return findItems(URI.create("http://vfn.cz/ontologies/ava-study/model/repair"));
    }


    public List<NamedItem> findRepeatedFailure() {
        return findItems(URI.create("http://vfn.cz/ontologies/ava-study/model/repeated-failure"));
    }

    public List<NamedItem> findInstitutions(){
        return em.createNativeQuery("""
                PREFIX rm: <http://onto.fel.cvut.cz/ontologies/record-manager/>
                PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
    
                SELECT ?uri (str(?_name) as ?name) {
                    ?uri a ?type.
                    ?uri rdfs:label ?_name
                }
                """, NamedItem.class.getSimpleName())
                .setParameter("type", URI.create(Vocabulary.s_c_institution))
                .getResultList();
    }

    public List<NamedItem> findItems(URI type){
        return em.createNativeQuery("""
                PREFIX rm: <http://onto.fel.cvut.cz/ontologies/record-manager/>
                PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
    
                SELECT ?uri (str(?_name) as ?name) {
                    SERVICE <repository:record-manager-formgen> { 
                        ?uri a ?type.
                        ?uri rdfs:label ?_name
                    }
                }
                """, NamedItem.class.getSimpleName())
                .setParameter("type", type)
                .getResultList();
    }

    public List<NamedItem> findItems(Collection<URI> items){
        String queryString = """
                PREFIX rm: <http://onto.fel.cvut.cz/ontologies/record-manager/>
                PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
    
                SELECT ?uri (str(?_name) as ?name) {
                    SERVICE <repository:record-manager-formgen> { 
                        ?uri rdfs:label ?_name
                    }
                }VALUES ?uri {
                    ###items###
                }
                """;
        if(items != null) {
            queryString = queryString.replaceFirst(
                    "###items###",
                    items.stream().map(u -> "<%s>".formatted(u)).collect(Collectors.joining("\n"))
                    );
        }
        return em.createNativeQuery(queryString, NamedItem.class.getSimpleName())
                .getResultList();
    }



    public List<NamedItem> findAircraft(){
        return em.createNativeQuery("""
                PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
                PREFIX form: <http://onto.fel.cvut.cz/ontologies/form/>
                
                SELECT ?uri ?name {
                    SERVICE <repository:record-manager-formgen> {
                        ?uri a form:form-template.
                        ?uri rdfs:label ?name.
                    }
                }
                """, NamedItem.class.getSimpleName()).getResultList();
    }

    public List<NamedItem> findAircraftParts(){
        return em.createNativeQuery("""
                PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
                PREFIX form: <http://onto.fel.cvut.cz/ontologies/form/>
                
                SELECT ?uri ?name {
                    SERVICE <repository:record-manager-formgen> {
                        ?uri a form:form-template.
                        ?uri rdfs:label ?name.
                    }
                }
                """, NamedItem.class.getSimpleName()).getResultList();
    }

    public List<Path> getBroaderPath(Collection<URI> elements){
        String queryString = """
                PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
                SELECT ?l1 ?l2 ?l3 ?l4 ?l5 (?p as ?uri) {
                    SERVICE <repository:record-manager-formgen> {
                        OPTIONAL{
                            ?p skos:broader+ ?l1.
                            FILTER NOT EXISTS{
                                ?l1 skos:broader ?l0 .
                            }
                            OPTIONAL{
                                ?l2 skos:broader ?l1 .
                                ?p skos:broader+ ?l2.
                                OPTIONAL{
                                    ?l3 skos:broader ?l2 .
                                    ?p skos:broader+ ?l3.
                                    OPTIONAL{
                                        ?l4 skos:broader ?l3 .
                                        ?p skos:broader+ ?l4.
                                        OPTIONAL{
                                            ?l5 skos:broader ?l4 .
                                            ?p skos:broader+ ?l5.
                                        }
                                    }
                                }
                            }
                        }
                    }
                }VALUES ?p {
                    %s
                }
                """;
        queryString = queryString.formatted(elements.stream().map(u -> "<%s>".formatted(u.toString()))
                .collect(Collectors.joining("\n")));
        return em.createNativeQuery(queryString, Path.class.getSimpleName())
                .getResultList();
    }

}
