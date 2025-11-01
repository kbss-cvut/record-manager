package cz.cvut.kbss.study.persistence.dao;

import cz.cvut.kbss.jopa.exceptions.NoResultException;
import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.kbss.jopa.model.descriptors.Descriptor;
import cz.cvut.kbss.jopa.model.descriptors.EntityDescriptor;
import cz.cvut.kbss.jopa.model.metamodel.EntityType;
import cz.cvut.kbss.jopa.model.query.Query;
import cz.cvut.kbss.jopa.model.query.TypedQuery;
import cz.cvut.kbss.ontodriver.model.LangString;
import cz.cvut.kbss.study.dto.RecordDto;
import cz.cvut.kbss.study.exception.PersistenceException;
import cz.cvut.kbss.study.exception.ValidationException;
import cz.cvut.kbss.study.model.*;
import cz.cvut.kbss.study.model.Record;
import cz.cvut.kbss.study.model.export.RawRecord;
import cz.cvut.kbss.study.persistence.dao.util.QuestionSaver;
import cz.cvut.kbss.study.persistence.dao.util.RecordFilterParams;
import cz.cvut.kbss.study.persistence.dao.util.RecordSort;
import cz.cvut.kbss.study.util.Constants;
import cz.cvut.kbss.study.util.IdentificationUtils;
import cz.cvut.kbss.study.util.Utils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.net.URI;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class RecordDao extends OwlKeySupportingDao<Record> {

    public static final String FIND_ALL_RAW_RECORDS = "find-raw-records.sparql";
    public static final String RECORDS_CLAUSE_TEMPLATE_VAR = "###RECORD_CLAUSE###";

    public RecordDao(EntityManager em) {
        super(Record.class, em);
    }

    @Override
    public Record find(URI uri) {
        Objects.requireNonNull(uri);
        try {
            return em.find(Record.class, uri, getDescriptor(uri));
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public Record findByKey(String key) {
        Objects.requireNonNull(key);
        try {
            return em.createQuery("SELECT r FROM " + Record.class.getSimpleName() + " r WHERE r.key = :key",
                                  type)
                     .setParameter("key", key, Constants.PU_LANGUAGE)
                     .setDescriptor(getDescriptor(key)).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public void persist(Record entity) {
        Objects.requireNonNull(entity);
        if (entity.getKey() == null) {
            entity.setKey(IdentificationUtils.generateKey());
        }
        if (entity.getUri() == null) {
            entity.setUri(generateRecordUriFromKey(entity.getKey()));
        }
        try {
            final Descriptor descriptor = getDescriptor(entity.getUri());
            em.persist(entity, descriptor);
            final QuestionSaver questionSaver = new QuestionSaver(descriptor);
            questionSaver.persistIfNecessary(entity.getQuestion(), em);
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    private Descriptor getDescriptor(String recordKey) {
        return getDescriptor(generateRecordUriFromKey(recordKey));
    }

    private Descriptor getDescriptor(URI ctx) {
        final EntityDescriptor descriptor = new EntityDescriptor(ctx);
        final EntityType<Record> et = em.getMetamodel().entity(Record.class);
        descriptor.addAttributeContext(et.getAttribute("author"), null);
        descriptor.addAttributeContext(et.getAttribute("lastModifiedBy"), null);
        descriptor.addAttributeContext(et.getAttribute("institution"), null);
        return descriptor;
    }

    public static URI generateRecordUriFromKey(String recordKey) {
        return URI.create(Vocabulary.s_c_record + "/" + Objects.requireNonNull(recordKey));
    }

    @Override
    public void update(Record entity) {
        Objects.requireNonNull(entity);
        final Descriptor descriptor = getDescriptor(entity.getUri());
        final Record orig = em.find(Record.class, entity.getUri(), descriptor);
        assert orig != null;
        orig.setQuestion(null);
        em.merge(entity, descriptor);
        // Evict cached instances loaded from the default context
        em.getEntityManagerFactory().getCache().evict(Record.class, entity.getUri(), null);
        em.getEntityManagerFactory().getCache().evict(RecordDto.class, entity.getUri(), null);
    }

    public void updateStatus(URI entityUri, RecordPhase targetPhase){
        Record entity = find(entityUri);
        if(entity == null)
             return;
        entity.setPhase(targetPhase);
        em.getEntityManagerFactory().getCache().evict(Record.class, entity.getUri(), null);
        em.getEntityManagerFactory().getCache().evict(RecordDto.class, entity.getUri(), null);
    }

    public List<RecordDto> findAllRecords() {
        return em.createNativeQuery("SELECT ?x WHERE { ?x a ?type . }", RecordDto.class)
                 .setParameter("type", typeUri)
                 .getResultList();
    }

    /**
     * Gets records of the specified institution.
     *
     * @param institution The institution to filter by
     * @return Records
     */
    public List<RecordDto> findByInstitution(Institution institution) {
        Objects.requireNonNull(institution);
        return em.createNativeQuery("SELECT ?r WHERE { ?r a ?type ; ?treatedAt ?institution . }",
                                    RecordDto.class)
                 .setParameter("type", typeUri)
                 .setParameter("treatedAt", URI.create(Vocabulary.s_p_was_treated_at))
                 .setParameter("institution", institution.getUri())
                 .getResultList();
    }

    /**
     * Gets records created by specified author.
     *
     * @param author The author to filter by
     * @return Records
     */
    public List<Record> findByAuthor(User author) {
        Objects.requireNonNull(author);
        return em.createNativeQuery("SELECT ?r WHERE { ?r a ?type ; ?createdBy ?author . }", Record.class)
                 .setParameter("type", typeUri)
                 .setParameter("createdBy", URI.create(Vocabulary.s_p_has_author))
                 .setParameter("author", author.getUri()).getResultList();
    }

    public int getNumberOfProcessedRecords() {
        return ((BigInteger) em.createNativeQuery(
                                       "SELECT (count(?p) as ?recordsCount) WHERE { ?p a ?record . }")
                               .setParameter("record", URI.create(Vocabulary.s_c_record))
                               .getSingleResult()
        ).intValue();
    }

    /**
     * Ensure that local name of provided record is unique within its organization.
     *
     * @param entity The local name to be checked for uniqueness
     */
    public void requireUniqueNonEmptyLocalName(Record entity) {
        Objects.requireNonNull(entity.getInstitution());
        if (entity.getLocalName() == null || entity.getLocalName().isEmpty()) {
            throw new ValidationException("error.record.localNameOfRecordIsEmpty",
                                          "Local name of record is empty for entity " + entity);
        }
        boolean unique = findByInstitution(entity.getInstitution()).stream()
                                                                   .filter(pr -> (entity.getFormTemplate() != null) && entity.getFormTemplate()
                                                                                                                             .equals(pr.getFormTemplate()))
                                                                   .filter(pr -> pr.getLocalName()
                                                                                   .equals(entity.getLocalName()))
                                                                   .allMatch(pr -> pr.getUri().equals(entity.getUri()));
        if (!unique) {
            throw new ValidationException("error.record.localNameOfRecordIsNotUnique",
                                          "Local name of record is not unique for entity " + entity);
        }
        em.clear();
    }

    /**
     * Retrieves DTOs of records matching the specified filtering criteria.
     * <p>
     * Note that since the record modification is tracked by a timestamp and the filter uses dates, this method uses
     * beginning of the min date and end of the max date.
     * <p>
     * The returned page contains also information about total number of matching records.
     *
     * @param filters  Record filtering criteria
     * @param pageSpec Specification of page and sorting
     * @return Page with matching records
     * @see #findAllRecordsFull(RecordFilterParams, Pageable)
     */
    public Page<RecordDto> findAllRecords(RecordFilterParams filters, Pageable pageSpec) {
        Objects.requireNonNull(filters);
        Objects.requireNonNull(pageSpec);
        return findRecords(filters, pageSpec, RecordDto.class);
    }

    /**
     * Retrieves records matching the specified filtering criteria.
     * <p>
     * Note that since the record modification is tracked by a timestamp and the filter uses dates, this method uses
     * beginning of the min date and end of the max date.
     * <p>
     * The returned page contains also information about total number of matching records.
     *
     * @param filters  Record filtering criteria
     * @param pageSpec Specification of page and sorting
     * @return Page with matching records
     * @see #findAllRecords(RecordFilterParams, Pageable)
     */
    public Page<Record> findAllRecordsFull(RecordFilterParams filters, Pageable pageSpec) {
        Objects.requireNonNull(filters);
        Objects.requireNonNull(pageSpec);
        return findRecords(filters, pageSpec, Record.class);
    }

    private <T> Page<T> findRecords(RecordFilterParams filters, Pageable pageSpec, Class<T> resultClass) {
        final Map<String, Object> queryParams = new HashMap<>();
        final String whereClause = constructWhereClause(filters, queryParams);
        final String queryString = "SELECT ?r WHERE " + whereClause + resolveOrderBy(pageSpec.getSortOr(RecordSort.defaultSort()));
        final TypedQuery<T> query = em.createNativeQuery(queryString, resultClass);
        setQueryParameters(query, queryParams);
        if (pageSpec.isPaged()) {
            query.setFirstResult((int) pageSpec.getOffset());
            query.setMaxResults(pageSpec.getPageSize());
        }
        final List<T> records = query.getResultList();
        final TypedQuery<Integer> countQuery = em.createNativeQuery("SELECT (COUNT(?r) as ?cnt) WHERE " + whereClause, Integer.class);
        setQueryParameters(countQuery, queryParams);
        final Integer totalCount = countQuery.getSingleResult();
        return new PageImpl<>(records, pageSpec, totalCount);
    }

    public Set<RecordPhase> findUsedRecordPhases(){
        return em.createNativeQuery("SELECT ?phase WHERE { ?record ?hasPhase ?phase } ", String.class)
                .setParameter("hasPhase", URI.create(Vocabulary.s_p_has_phase))
                .getResultList()
                .stream()
                .map(RecordPhase::fromIri)
                .collect(Collectors.toSet());

    }

    public Page<RawRecord> findAllRecordsRaw(RecordFilterParams filters, Pageable pageSpec){
        final Map<String, Object> queryParams = new HashMap<>();
        final String whereClause = constructWhereClauseWithGraphs(filters, queryParams);

        final String queryStringNoPaging = Utils.loadQuery(FIND_ALL_RAW_RECORDS)
                .replaceFirst(RECORDS_CLAUSE_TEMPLATE_VAR, whereClause);
        final String queryString = queryStringNoPaging + (pageSpec.isPaged()
                ? resolveOrderBy(pageSpec.getSortOr(RecordSort.defaultSort()))
                : ""
        );

        Query query = em.createNativeQuery(queryString,  RawRecord.class.getSimpleName());
        queryParams.forEach(query::setParameter);

        if (pageSpec.isPaged()) {
            query.setFirstResult((int) pageSpec.getOffset());
            query.setMaxResults(pageSpec.getPageSize());
        }
        setQueryParameters(query, queryParams);
        List<RawRecord> result = query.getResultList();
        Integer totalCount = result.size();
        if(pageSpec.isPaged()){
            TypedQuery<Integer> countQuery = em.createNativeQuery(
                    "SELECT (COUNT(?r) as ?cnt) WHERE {%s}".formatted(whereClause),
                    Integer.class);

            setQueryParameters(countQuery, queryParams);
            totalCount = countQuery.getSingleResult();
        }

        return new PageImpl<>(result, pageSpec, totalCount);
    }

    private void setQueryParameters(Query query, Map<String, Object> queryParams) {
        query
                .setParameter("type", typeUri)
                .setParameter("hasAuthor", URI.create(Vocabulary.s_p_has_author))
                .setParameter("hasUsername", URI.create(Vocabulary.s_p_accountName))
                .setParameter("hasPhase", URI.create(Vocabulary.s_p_has_phase))
                .setParameter("hasFormTemplate", URI.create(Vocabulary.s_p_has_form_template))
                .setParameter("hasInstitution", URI.create(Vocabulary.s_p_was_treated_at))
                .setParameter("hasKey", URI.create(Vocabulary.s_p_key))
                .setParameter("hasCreatedDate", URI.create(Vocabulary.s_p_created))
                .setParameter("hasLastModified", URI.create(Vocabulary.s_p_modified));
        queryParams.forEach(query::setParameter);
    }

    private static String constructWhereClause(RecordFilterParams filters, Map<String, Object> queryParams) {
        // Could not use Criteria API because it does not support OPTIONAL
        String whereClause = "{" +
                "?r a ?type ; " +
                "?hasAuthor ?author ; " +
                "?hasCreatedDate ?created ; " +
                "?hasInstitution ?institution . " +
                "?institution ?hasKey ?institutionKey ." +
                "?author ?hasUsername ?username ." +
                "OPTIONAL { ?r ?hasPhase ?phase . } " +
                "OPTIONAL { ?r ?hasFormTemplate ?formTemplate . } " +
                "OPTIONAL { ?r ?hasLastModified ?lastModified . } " +
                "BIND (COALESCE(?lastModified, ?created) AS ?date) ";
        whereClause += mapParamsToQuery(filters, queryParams);
        whereClause += "}";
        return whereClause;
    }

    private static String constructWhereClauseWithGraphs(RecordFilterParams filters, Map<String, Object> queryParams) {
        // Could not use Criteria API because it does not support OPTIONAL
        String whereClause = "{GRAPH ?r{" +
                "?r a ?type ; " +
                "?hasCreatedDate ?created ; " +
                "?hasInstitution ?institution . " +
                "OPTIONAL { ?r ?hasPhase ?phase . } " +
                "OPTIONAL { ?r ?hasFormTemplate ?formTemplate . } " +
                "OPTIONAL { ?r ?hasLastModified ?lastModified . } " +
                "BIND (COALESCE(?lastModified, ?created) AS ?date) ";
        whereClause += mapParamsToQuery(filters, queryParams);
        whereClause += "}" +
                "GRAPH ?institutionGraph{" +
                "?institution ?hasKey ?institutionKey ." +
                "}}";

        queryParams.put("institutionGraph", URI.create(Vocabulary.s_c_institution + "s"));

        return whereClause;
    }

    private static String mapParamsToQuery(RecordFilterParams filterParams, Map<String, Object> queryParams) {
        final List<String> filters = new ArrayList<>();
        filterParams.getAuthor().ifPresent(author -> queryParams.put("username",  new LangString(author, Constants.PU_LANGUAGE)));
        if(!filterParams.getInstitutionKeys().isEmpty()) {
            filters.add("FILTER (?institutionKey in (?institutionKeys))");
            queryParams.put("institutionKeys", filterParams.getInstitutionKeys().stream().map(key -> new LangString(key, Constants.PU_LANGUAGE)).collect(Collectors.toList()));
        }
        filterParams.getMinModifiedDate().ifPresent(date -> {
            filters.add("FILTER (?date >= ?minDate)");
            queryParams.put("minDate", date.atStartOfDay(ZoneOffset.UTC).toInstant());
        });
        filterParams.getMaxModifiedDate().ifPresent(date -> {
            filters.add("FILTER (?date < ?maxDate)");
            queryParams.put("maxDate", date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant());
        });
        if (!filterParams.getPhaseIds().isEmpty()) {
            filters.add("FILTER (?phase in (?phases))");
            queryParams.put("phases",
                            filterParams.getPhaseIds().stream().map(URI::create).collect(Collectors.toList()));
        }
        if (!filterParams.getFormTemplateIds().isEmpty()) {
            filters.add("FILTER (?formTemplate in (?formTemplates))");
            queryParams.put("formTemplates",
                filterParams.getFormTemplateIds().stream().map(id -> new LangString(id, Constants.PU_LANGUAGE)).collect(Collectors.toList()));
        }
        return String.join(" ", filters);
    }

    private static String resolveOrderBy(Sort sort) {
        if (sort.isUnsorted()) {
            return "";
        }
        final StringBuilder sb = new StringBuilder(" ORDER BY");
        for (Sort.Order o : sort) {
            if (!RecordSort.SORTING_PROPERTIES.contains(o.getProperty())) {
                throw new IllegalArgumentException("Unsupported record sorting property '" + o.getProperty() + "'.");
            }
            sb.append(' ');
            sb.append(o.isAscending() ? "ASC(" : "DESC(");
            sb.append('?').append(o.getProperty()).append(')');
        }
        return sb.toString();
    }
}
