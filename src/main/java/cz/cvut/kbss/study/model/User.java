package cz.cvut.kbss.study.model;

import cz.cvut.kbss.jopa.model.annotations.*;
import cz.cvut.kbss.study.model.util.HasDerivableUri;
import cz.cvut.kbss.study.util.Constants;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.Serializable;
import java.net.URI;
import java.util.Date;
import java.util.Set;

@OWLClass(iri = Vocabulary.s_c_user)
public class User implements HasDerivableUri, Serializable {

    @Id
    private URI uri;

    @ParticipationConstraints(nonEmpty = true)
    @OWLDataProperty(iri = Vocabulary.s_p_firstName)
    private String firstName;

    @ParticipationConstraints(nonEmpty = true)
    @OWLDataProperty(iri = Vocabulary.s_p_lastName)
    private String lastName;

    @ParticipationConstraints(nonEmpty = true)
    @OWLDataProperty(iri = Vocabulary.s_p_accountName)
    private String username;

    @OWLDataProperty(iri = Vocabulary.s_p_password)
    private String password;

    @OWLDataProperty(iri = Vocabulary.s_p_mbox)
    private String emailAddress;

    @OWLDataProperty(iri = Vocabulary.s_p_created)
    private Date dateCreated;

    @OWLObjectProperty(iri = Vocabulary.s_p_is_member_of, fetch = FetchType.EAGER)
    private Clinic clinic;

    @Types
    private Set<String> types;

    @Override
    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    /**
     * Gets date when this user's account was created.
     */
    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Clinic getClinic() {
        return clinic;
    }

    public void setClinic(Clinic clinic) {
        this.clinic = clinic;
    }

    public Set<String> getTypes() {
        return types;
    }

    public void setTypes(Set<String> types) {
        this.types = types;
    }

    /**
     * Encodes password of this person.
     *
     * @param encoder Encoder to user to encode the password
     */
    public void encodePassword(PasswordEncoder encoder) {
        if (password == null || password.isEmpty()) {
            throw new IllegalStateException("Cannot encode an empty password.");
        }
        this.password = encoder.encode(password);
    }

    /**
     * Erases the password.
     * <p>
     * Handy for example before sending the instance outside the application.
     */
    public void erasePassword() {
        this.password = null;
    }

    @Override
    public void generateUri() {
        if (uri != null) {
            return;
        }
        if (firstName == null || firstName.isEmpty()) {
            throw new IllegalStateException("Cannot generate Person URI without first name.");
        }
        if (lastName == null || lastName.isEmpty()) {
            throw new IllegalStateException("Cannot generate Person URI without last name.");
        }
        this.uri = URI.create(Constants.BASE_URI + firstName + "-" + lastName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        return uri != null ? uri.equals(user.uri) : user.uri == null;

    }

    @Override
    public int hashCode() {
        return uri != null ? uri.hashCode() : 0;
    }

    @Override
    public String toString() {
        return firstName + ' ' + lastName + '<' + uri + '>';
    }
}
