package cz.cvut.kbss.study.persistence.dao;

import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.kbss.jopa.model.descriptors.Descriptor;
import cz.cvut.kbss.jopa.model.descriptors.EntityDescriptor;
import cz.cvut.kbss.jopa.model.metamodel.EntityType;
import cz.cvut.kbss.study.dto.PatientRecordDto;
import cz.cvut.kbss.study.environment.generator.Generator;
import cz.cvut.kbss.study.environment.util.Environment;
import cz.cvut.kbss.study.model.Institution;
import cz.cvut.kbss.study.model.PatientRecord;
import cz.cvut.kbss.study.model.RecordPhase;
import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.model.qam.Answer;
import cz.cvut.kbss.study.persistence.BaseDaoTestRunner;
import cz.cvut.kbss.study.persistence.dao.util.QuestionSaver;
import cz.cvut.kbss.study.persistence.dao.util.RecordFilterParams;
import cz.cvut.kbss.study.persistence.dao.util.RecordSort;
import cz.cvut.kbss.study.util.IdentificationUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.net.URI;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.stream.IntStream;

import static cz.cvut.kbss.study.environment.util.ContainsSameEntities.containsSameEntities;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PatientRecordDaoTest extends BaseDaoTestRunner {

    @Autowired
    private EntityManager em;

    @Autowired
    private PatientRecordDao sut;

    @Autowired
    private UserDao userDao;

    @Autowired
    private InstitutionDao institutionDao;

    @Test
    public void findByInstitutionReturnsMatchingRecords() {
        Institution institution = Generator.generateInstitution();
        Institution institutionOther = Generator.generateInstitution();
        User user1 = Generator.generateUser(institution);
        User user2 = Generator.generateUser(institutionOther);
        PatientRecord record1 = Generator.generatePatientRecord(user1);
        PatientRecord record2 = Generator.generatePatientRecord(user1);
        PatientRecord recordOther = Generator.generatePatientRecord(user2);

        transactional(() -> {
            institutionDao.persist(institution);
            institutionDao.persist(institutionOther);
            userDao.persist(user1);
            userDao.persist(user2);
            sut.persist(record1);
            sut.persist(record2);
            sut.persist(recordOther);
        });

        List<PatientRecordDto> records = sut.findByInstitution(institution);

        assertEquals(2, records.size());
        assertEquals(1, records.stream().filter(rs -> record1.getUri().equals(rs.getUri())).count());
        assertEquals(1, records.stream().filter(rs -> record2.getUri().equals(rs.getUri())).count());
    }

    @Test
    public void findAllRecordsReturnAllRecords() {
        Institution institution1 = Generator.generateInstitution();
        Institution institution2 = Generator.generateInstitution();
        User user1 = Generator.generateUser(institution1);
        User user2 = Generator.generateUser(institution2);
        PatientRecord record1 = Generator.generatePatientRecord(user1);
        PatientRecord record2 = Generator.generatePatientRecord(user1);
        PatientRecord record3 = Generator.generatePatientRecord(user2);

        transactional(() -> {
            institutionDao.persist(institution1);
            institutionDao.persist(institution2);
            userDao.persist(user1);
            userDao.persist(user2);
            sut.persist(record1);
            sut.persist(record2);
            sut.persist(record3);
        });

        List<PatientRecordDto> records = sut.findAllRecords();

        assertEquals(3, records.size());
    }

    @Test
    public void getNumberOfProcessedRecords() {
        Institution institution = Generator.generateInstitution();
        User user = Generator.generateUser(institution);
        PatientRecord record1 = Generator.generatePatientRecord(user);
        PatientRecord record2 = Generator.generatePatientRecord(user);
        transactional(() -> {
            institutionDao.persist(institution);
            userDao.persist(user);
            sut.persist(record1);
            sut.persist(record2);
        });

        int numberOfProcessedRecords = sut.getNumberOfProcessedRecords();

        assertEquals(2, numberOfProcessedRecords);
    }

    @Test
    public void findByAuthorReturnsMatchingRecords() {
        Institution institution = Generator.generateInstitution();
        User user1 = Generator.generateUser(institution);
        User user2 = Generator.generateUser(institution);
        PatientRecord record1 = Generator.generatePatientRecord(user1);
        PatientRecord record2 = Generator.generatePatientRecord(user1);
        PatientRecord record3 = Generator.generatePatientRecord(user2);

        transactional(() -> {
            institutionDao.persist(institution);
            userDao.persist(user1);
            userDao.persist(user2);
            sut.persist(record1);
            sut.persist(record2);
            sut.persist(record3);
        });

        List<PatientRecord> records1 = sut.findByAuthor(user1);
        List<PatientRecord> records2 = sut.findByAuthor(user2);

        assertEquals(2, records1.size());
        assertEquals(1, records2.size());
    }

    @Test
    void persistGeneratesIdentifierBeforeSavingRecord() {
        final Institution institution = Generator.generateInstitution();
        institution.setKey(IdentificationUtils.generateKey());
        final User author = Generator.generateUser(institution);
        author.generateUri();
        transactional(() -> {
            em.persist(author);
            em.persist(institution);
        });

        final PatientRecord record = Generator.generatePatientRecord(author);
        record.setUri(null);

        transactional(() -> sut.persist(record));
        assertNotNull(record.getUri());
        final PatientRecord result = em.find(PatientRecord.class, record.getUri());
        assertNotNull(result);
    }

    private User generateAuthorWithInstitution() {
        final Institution institution = Generator.generateInstitution();
        institution.setKey(IdentificationUtils.generateKey());
        final User author = Generator.generateUser(institution);
        author.generateUri();
        transactional(() -> {
            em.persist(author);
            em.persist(institution);
        });
        return author;
    }

    @Test
    void persistSavesRecordWithQuestionAnswerTreeIntoSeparateContext() {
        final User author = generateAuthorWithInstitution();
        final PatientRecord record = Generator.generatePatientRecord(author);
        record.setUri(null);
        record.setQuestion(Generator.generateQuestionAnswerTree());

        transactional(() -> sut.persist(record));

        final Descriptor descriptor = getDescriptor(record);
        final PatientRecord result = em.find(PatientRecord.class, record.getUri(), descriptor);
        assertNotNull(result);
        assertNotNull(result.getQuestion());
    }

    private Descriptor getDescriptor(PatientRecord record) {
        final EntityType<PatientRecord> et = em.getMetamodel().entity(PatientRecord.class);
        final Descriptor descriptor = new EntityDescriptor(PatientRecordDao.generateRecordUriFromKey(record.getKey()));
        descriptor.addAttributeContext(et.getAttribute("author"), null);
        descriptor.addAttributeContext(et.getAttribute("lastModifiedBy"), null);
        descriptor.addAttributeContext(et.getAttribute("institution"), null);
        return descriptor;
    }

    @Test
    void updateUpdatesRecordInContext() {
        final User author = generateAuthorWithInstitution();
        final PatientRecord record = Generator.generatePatientRecord(author);
        record.setKey(IdentificationUtils.generateKey());
        record.setUri(PatientRecordDao.generateRecordUriFromKey(record.getKey()));
        record.setQuestion(Generator.generateQuestionAnswerTree());
        final Descriptor descriptor = getDescriptor(record);
        transactional(() -> {
            em.persist(record, descriptor);
            new QuestionSaver(descriptor).persistIfNecessary(record.getQuestion(), em);
        });

        final String updatedName = "Updated name";
        record.setLocalName(updatedName);
        final Answer answer = record.getQuestion().getSubQuestions().iterator().next().getAnswers().iterator().next();
        final String updatedAnswer = "Updated answer";
        answer.setTextValue(updatedAnswer);

        transactional(() -> sut.update(record));

        final PatientRecord result = em.find(PatientRecord.class, record.getUri(), descriptor);
        assertEquals(updatedName, result.getLocalName());
        final Answer resultAnswer = em.find(Answer.class, answer.getUri(), descriptor);
        assertNotNull(resultAnswer);
        assertEquals(updatedAnswer, resultAnswer.getTextValue());
    }

    @Test
    void findByKeyLoadsRecordByKey() {
        final User author = generateAuthorWithInstitution();
        final PatientRecord record = Generator.generatePatientRecord(author);
        record.setKey(IdentificationUtils.generateKey());
        record.setQuestion(Generator.generateQuestionAnswerTree());
        final Descriptor descriptor = getDescriptor(record);
        transactional(() -> {
            em.persist(record, descriptor);
            new QuestionSaver(descriptor).persistIfNecessary(record.getQuestion(), em);
        });

        final PatientRecord result = sut.findByKey(record.getKey());
        assertNotNull(result);
        assertEquals(record.getUri(), result.getUri());
        assertNotNull(result.getQuestion());
    }

    private void persistRecordWithIdentification(PatientRecord record) {
        record.setKey(IdentificationUtils.generateKey());
        record.setUri(PatientRecordDao.generateRecordUriFromKey(record.getKey()));
        em.persist(record, getDescriptor(record));
    }

    @Test
    void findAllFullReturnsRecordsMatchingSpecifiedDatePeriod() {
        final User author = generateAuthorWithInstitution();
        final List<PatientRecord> allRecords = generateRecordsForAuthor(author, 5);
        for (int i = 0; i < allRecords.size(); i++) {
            allRecords.get(i).setDateCreated(new Date(System.currentTimeMillis() - i * Environment.MILLIS_PER_DAY));
        }
        transactional(() -> allRecords.forEach(this::persistRecordWithIdentification));
        final LocalDate minDate = LocalDate.now().minusDays(3);
        final LocalDate maxDate = LocalDate.now().minusDays(1);
        final List<PatientRecord> expected = allRecords.stream().filter(r -> {
            final Date modified = r.getLastModified() != null ? r.getLastModified() : r.getDateCreated();
            final LocalDate modifiedDate = modified.toInstant().atZone(ZoneOffset.UTC).toLocalDate();
            return !modifiedDate.isBefore(minDate) && !modifiedDate.isAfter(maxDate);
        }).toList();

        final Page<PatientRecord> result =
                sut.findAllRecordsFull(new RecordFilterParams(null, minDate, maxDate, Collections.emptySet()),
                                       Pageable.unpaged());
        assertFalse(result.isEmpty());
        assertThat(result.getContent(), containsSameEntities(expected));
    }

    private List<PatientRecord> generateRecordsForAuthor(User author, int count) {
        return IntStream.range(0, count).mapToObj(i -> {
                            final PatientRecord r = Generator.generatePatientRecord(author);
                            if (Generator.randomBoolean()) {
                                r.setDateCreated(new Date(System.currentTimeMillis() - 30 * i * Environment.MILLIS_PER_DAY));
                            } else {
                                r.setDateCreated(new Date(System.currentTimeMillis() - 30 * i * Environment.MILLIS_PER_DAY));
                                r.setLastModified(new Date(System.currentTimeMillis() - i * Environment.MILLIS_PER_DAY));
                            }
                            return r;
                        }).sorted(Comparator.comparing(
                                (PatientRecord r) -> r.getLastModified() != null ? r.getLastModified() : r.getDateCreated()).reversed())
                        .toList();
    }

    @Test
    void findAllFullReturnsRecordsMatchingSpecifiedDatePeriodAndInstitution() {
        final User authorOne = generateAuthorWithInstitution();
        final Institution institution = authorOne.getInstitution();
        final User authorTwo = generateAuthorWithInstitution();
        final List<PatientRecord> allRecords = new ArrayList<>(generateRecordsForAuthor(authorOne, 5));
        allRecords.addAll(generateRecordsForAuthor(authorTwo, 5));
        transactional(() -> allRecords.forEach(this::persistRecordWithIdentification));
        final LocalDate minDate = LocalDate.now().minusDays(31);
        final LocalDate maxDate = LocalDate.now().minusDays(1);
        final List<PatientRecord> expected = allRecords.stream().filter(r -> {
            final Date modified = r.getLastModified() != null ? r.getLastModified() : r.getDateCreated();
            final LocalDate modifiedDate = modified.toInstant().atZone(ZoneOffset.UTC).toLocalDate();
            return !modifiedDate.isBefore(minDate) && !modifiedDate.isAfter(maxDate) && r.getInstitution().getUri()
                                                                                         .equals(institution.getUri());
        }).toList();

        final Page<PatientRecord> result =
                sut.findAllRecordsFull(new RecordFilterParams(institution.getKey(), minDate, maxDate, Collections.emptySet()),
                                       Pageable.unpaged());
        assertFalse(result.isEmpty());
        assertThat(result.getContent(), containsSameEntities(expected));
    }

    @Test
    void findAllFullReturnsRecordsMatchingSpecifiedPhase() {
        final User author = generateAuthorWithInstitution();
        final List<PatientRecord> allRecords = generateRecordsForAuthor(author, 5);
        transactional(() -> allRecords.forEach(r -> {
            r.setPhase(RecordPhase.values()[Generator.randomInt(RecordPhase.values().length)]);
            persistRecordWithIdentification(r);
        }));
        final RecordPhase phase = allRecords.get(Generator.randomIndex(allRecords)).getPhase();
        final RecordFilterParams filterParams = new RecordFilterParams();
        filterParams.setPhaseIds(Set.of(phase.getIri()));

        final Page<PatientRecord> result = sut.findAllRecordsFull(filterParams, Pageable.unpaged());
        assertFalse(result.isEmpty());
        result.forEach(res -> assertEquals(phase, res.getPhase()));
    }

    @Test
    void findAllFullReturnsRecordsMatchingSpecifiedPage() {
        final User author = generateAuthorWithInstitution();
        final List<PatientRecord> allRecords = generateRecordsForAuthor(author, 10);
        transactional(() -> allRecords.forEach(this::persistRecordWithIdentification));
        final int pageSize = 5;

        final Page<PatientRecord> result = sut.findAllRecordsFull(new RecordFilterParams(), PageRequest.of(1, pageSize));
        assertFalse(result.isEmpty());
        assertEquals(pageSize, result.getNumberOfElements());
        assertThat(result.getContent(), containsSameEntities(allRecords.subList(pageSize, allRecords.size())));
    }

    @Test
    void findAllFullReturnsRecordsSortedAccordingToSortSpecification() {
        final User author = generateAuthorWithInstitution();
        final List<PatientRecord> allRecords = generateRecordsForAuthor(author, 5);
        transactional(() -> allRecords.forEach(this::persistRecordWithIdentification));
        final Page<PatientRecord> result =
                sut.findAllRecordsFull(new RecordFilterParams(), PageRequest.of(0, allRecords.size(), Sort.Direction.ASC,
                                                                                RecordSort.SORT_DATE_PROPERTY));
        assertFalse(result.isEmpty());
        assertEquals(allRecords.size(), result.getNumberOfElements());
        final ListIterator<PatientRecord> itExp = allRecords.listIterator(allRecords.size());
        final ListIterator<PatientRecord> itAct = result.getContent().listIterator();
        while (itExp.hasPrevious() && itAct.hasNext()) {
            assertEquals(itExp.previous().getUri(), itAct.next().getUri());
        }
    }

    @Test
    void findAllFullThrowsIllegalArgumentExceptionForUnsupportedSortProperty() {
        assertThrows(IllegalArgumentException.class,
                     () -> sut.findAllRecordsFull(new RecordFilterParams(), PageRequest.of(0, 10, Sort.Direction.ASC,
                                                                                           "unknownProperty")));
    }

    @Test
    void findAllFullReturnsPageWithTotalNumberOfMatchingRecords() {
        final User author = generateAuthorWithInstitution();
        final List<PatientRecord> allRecords = generateRecordsForAuthor(author, 10);
        transactional(() -> allRecords.forEach(this::persistRecordWithIdentification));
        final int pageSize = 5;

        final Page<PatientRecord> result = sut.findAllRecordsFull(new RecordFilterParams(), PageRequest.of(1, pageSize));
        assertFalse(result.isEmpty());
        assertEquals(pageSize, result.getNumberOfElements());
        assertEquals(allRecords.size(), result.getTotalElements());
    }

    @Test
    void findAllRecordsReturnsPageWithMatchingRecords() {
        final User author = generateAuthorWithInstitution();
        final int totalCount = 10;
        final List<PatientRecord> allRecords = generateRecordsForAuthor(author, totalCount);
        for (int i = 0; i < allRecords.size(); i++) {
            allRecords.get(i).setDateCreated(new Date(System.currentTimeMillis() - i * Environment.MILLIS_PER_DAY));
        }
        transactional(() -> allRecords.forEach(this::persistRecordWithIdentification));
        final LocalDate minDate = LocalDate.now().minusDays(3);
        final LocalDate maxDate = LocalDate.now().minusDays(1);
        final List<PatientRecord> allMatching = allRecords.stream().filter(r -> {
            final Date modified = r.getLastModified() != null ? r.getLastModified() : r.getDateCreated();
            final LocalDate modifiedDate = modified.toInstant().atZone(ZoneOffset.UTC).toLocalDate();
            return !modifiedDate.isBefore(minDate) && !modifiedDate.isAfter(maxDate);
        }).sorted(Comparator.comparing(r -> r.getLastModified() != null ? r.getLastModified() : r.getDateCreated())).toList();
        final int pageSize = 3;

        final Page<PatientRecordDto> result =
                sut.findAllRecords(new RecordFilterParams(null, minDate, maxDate, Collections.emptySet()),
                                       PageRequest.of(0, pageSize, Sort.Direction.ASC, RecordSort.SORT_DATE_PROPERTY));
        assertEquals(Math.min(pageSize, allMatching.size()), result.getNumberOfElements());
        assertEquals(allMatching.size(), result.getTotalElements());
        for (int i = 0; i < result.getNumberOfElements(); i++) {
            assertEquals(allMatching.get(i).getUri(), result.getContent().get(i).getUri());
        }
    }

    @Test
    void persistDoesNotGenerateIdentificationWhenRecordAlreadyHasIt() {
        final User author = generateAuthorWithInstitution();
        final PatientRecord record = Generator.generatePatientRecord(author);
        final String key = IdentificationUtils.generateKey();
        record.setKey(key);
        final URI uri = Generator.generateUri();
        record.setUri(uri);

        transactional(() -> sut.persist(record));

        final PatientRecord result = em.find(PatientRecord.class, uri);
        assertNotNull(result);
        assertEquals(uri, result.getUri());
        assertEquals(key, result.getKey());
    }
}
