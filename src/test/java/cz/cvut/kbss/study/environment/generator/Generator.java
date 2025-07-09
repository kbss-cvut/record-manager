package cz.cvut.kbss.study.environment.generator;

import cz.cvut.kbss.study.dto.PatientRecordDto;
import cz.cvut.kbss.study.model.*;
import cz.cvut.kbss.study.model.qam.Answer;
import cz.cvut.kbss.study.model.qam.Question;

import java.net.URI;
import java.util.*;

import static org.apache.commons.lang3.BooleanUtils.forEach;

public class Generator {

    private static final Random random = new Random();

    private Generator() {
        throw new AssertionError();
    }

    /**
     * Generates a (pseudo) random URI, usable for test individuals.
     *
     * @return Random URI
     */
    public static URI generateUri() {
        return URI.create(Vocabulary.ONTOLOGY_IRI_RECORD_MANAGER + "/randomInstance" + randomInt());
    }

    /**
     * Generates a (pseudo-)random integer between 0 and the specified upper bound.
     * <p>
     * <b>IMPORTANT</b>: The lower bound (0) is not included in the generator output, so the smallest number this
     * generator returns is 1.
     *
     * @param upperBound Upper bound of the generated number
     * @return Randomly generated integer
     */
    public static int randomInt(int upperBound) {
        int rand;
        do {
            rand = random.nextInt(upperBound);
        } while (rand == 0);
        return rand;
    }

    /**
     * Generates a (pseudo-)random integer between the specified lower and upper bounds.
     *
     * @param lowerBound Lower bound, inclusive
     * @param upperBound Upper bound, exclusive
     * @return Randomly generated integer
     */
    public static int randomInt(int lowerBound, int upperBound) {
        int rand;
        do {
            rand = random.nextInt(upperBound);
        } while (rand < lowerBound);
        return rand;
    }

    /**
     * Generates a (pseudo) random integer.
     * <p>
     * This version always returns number greater or equal to 0.
     *
     * @return Randomly generated integer
     * @see #randomInt(int)
     */
    public static int randomInt() {
        int number = random.nextInt();
        if (number < 0) {
            number = number * -1;
        }
        return number;
    }

    /**
     * Generates a (pseudo)random index of an element in the collection.
     * <p>
     * I.e. the returned number is in the interval <0, col.size()).
     *
     * @param col The collection
     * @return Random index
     */
    public static int randomIndex(Collection<?> col) {
        assert col != null;
        assert !col.isEmpty();
        return random.nextInt(col.size());
    }

    /**
     * Generates a (pseudo)random date.
     *
     * @return Random date
     */
    public static Date randomDate() {
        return new Date(Math.abs(System.currentTimeMillis() - randomInt()));
    }

    /**
     * Generators a (pseudo) random boolean.
     *
     * @return Random boolean
     */
    public static boolean randomBoolean() {
        return random.nextBoolean();
    }

    /**
     * Creates user based params.
     * @param username
     * @param password
     * @param firstName
     * @param lastName
     * @param email
     * @param institution
     * @return user based on params
     */
    public static User getUser(String username, String password, String firstName, String lastName, String email, Institution institution, RoleGroup roleGroup) {
        final User person = new User();
        person.setUsername(username);
        person.setPassword(password);
        person.setFirstName(firstName);
        person.setLastName(lastName);
        person.setEmailAddress(email);
        person.setInstitution(institution);
        person.setRoleGroup(roleGroup);
        return person;
    }

    /**
     * Generators a (pseudo) random user.
     *
     * @return Random user
     */
    public static User generateAdministrator(Institution institution) {
        final User person = new User();
        person.setUsername("RandomUsername" + randomInt());
        person.setPassword("RandomPassword" + randomInt());
        person.setFirstName("RandomFirstName" + randomInt());
        person.setLastName("RandomLastName" + randomInt());
        person.setEmailAddress("RandomEmail" + randomInt() + "@random.rand");
        person.setInstitution(institution);
        person.setRoleGroup(generateRoleGroupWithRoles(Role.administrator));
        person.setUri(generateUri());
        return person;
    }

    public static User generateUser(Institution institution, RoleGroup roleGroup) {
        final User person = new User();
        person.setUsername("RandomUsername" + randomInt());
        person.setPassword("RandomPassword" + randomInt());
        person.setFirstName("RandomFirstName" + randomInt());
        person.setLastName("RandomLastName" + randomInt());
        person.setEmailAddress("RandomEmail" + randomInt() + "@random.rand");
        person.setInstitution(institution);
        person.setRoleGroup(roleGroup);
        person.setUri(generateUri());
        return person;
    }

    public static RoleGroup generateRoleGroup() {
        final RoleGroup roleGroup = new RoleGroup();
        roleGroup.setName("RandomRoleGroup" + randomInt());
        roleGroup.setUri(generateUri());
        roleGroup.addRole(Role.administrator);
        return roleGroup;
    }


    public static RoleGroup generateRoleGroupWithRoles(Role ... roles) {
        final RoleGroup roleGroup = new RoleGroup();
        roleGroup.setName("RandomRoleGroup" + randomInt());
        roleGroup.setUri(generateUri());
        Arrays.stream(roles).forEach(roleGroup::addRole);
        return roleGroup;
    }

    /**
     * Generators a (pseudo) random institution.
     *
     * @return Random institution
     */
    public static Institution generateInstitution() {
        final Institution org = new Institution();
        org.setName("RandomInstitution" + randomInt());
        org.setUri(generateUri());
        return org;
    }

    /**
     * Generators a (pseudo) random patient record.
     *
     * @param author author of patient record
     * @return Random patient record
     */
    public static PatientRecord generatePatientRecord(User author) {
        final PatientRecord rec = new PatientRecord();
        rec.setAuthor(author);
        rec.setLocalName("RandomRecord" + randomInt());
        rec.setUri(generateUri());
        rec.setInstitution(author.getInstitution());
        rec.setPhase(RecordPhase.values()[Generator.randomInt(0, RecordPhase.values().length)]);
        return rec;
    }

    /**
     * Generators a (pseudo) random patient record dto.
     *
     * @param author author of patient record dto
     * @return Random patient record dto
     */
    public static PatientRecordDto generatePatientRecordDto(User author) {
        final PatientRecordDto rec = new PatientRecordDto();
        rec.setLocalName("RandomRecordDto" + randomInt());
        rec.setAuthor(author.getUri());
        rec.setUri(generateUri());
        rec.setInstitution(author.getInstitution());
        return rec;
    }

    /**
     * Generators a (pseudo) random action history.
     *
     * @return Random action history
     */
    public static ActionHistory generateActionHistory(User author) {
        final ActionHistory action = new ActionHistory();
        action.setAuthor(author);
        action.setTimestamp(randomDate());
        action.setType("RANDOM_TYPE_" + randomInt());
        action.setPayload("RandomPayload" + randomInt());
        return action;
    }

    public static Question generateQuestionAnswerTree() {
        final Question root = new Question();
        root.setOrigin(Generator.generateUri());
        final Question childOne = new Question();
        childOne.setOrigin(Generator.generateUri());
        childOne.setAnswers(Set.of(generateAnswer()));
        root.setSubQuestions(new HashSet<>(Set.of(childOne)));
        final Question childTwo = new Question();
        childTwo.setOrigin(Generator.generateUri());
        childTwo.setAnswers(Set.of(generateAnswer()));
        root.getSubQuestions().add(childTwo);
        return root;
    }

    private static Answer generateAnswer() {
        final Answer childOneAnswer = new Answer();
        if (randomBoolean()) {
            childOneAnswer.setCodeValue(Generator.generateUri());
        } else {
            childOneAnswer.setTextValue("Child one answer");
        }
        return childOneAnswer;
    }
}
