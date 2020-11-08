package cz.cvut.kbss.study.service.repository;

import cz.cvut.kbss.study.exception.EntityExistsException;
import cz.cvut.kbss.study.exception.NotFoundException;
import cz.cvut.kbss.study.exception.ValidationException;
import cz.cvut.kbss.study.model.Institution;
import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.model.Vocabulary;
import cz.cvut.kbss.study.persistence.dao.GenericDao;
import cz.cvut.kbss.study.persistence.dao.PatientRecordDao;
import cz.cvut.kbss.study.persistence.dao.UserDao;
import cz.cvut.kbss.study.service.ConfigReader;
import cz.cvut.kbss.study.service.EmailService;
import cz.cvut.kbss.study.service.UserService;
import cz.cvut.kbss.study.service.security.SecurityUtils;
import cz.cvut.kbss.study.util.IdentificationUtils;
import cz.cvut.kbss.study.util.Validator;
import cz.cvut.kbss.study.util.etemplates.*;
import org.apache.commons.lang.StringUtils;
import org.eclipse.rdf4j.http.protocol.UnauthorizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class RepositoryUserService extends BaseRepositoryService<User> implements UserService {

    private final SecurityUtils securityUtils;

    private final PasswordEncoder passwordEncoder;

    private final UserDao userDao;

    private final PatientRecordDao patientRecordDao;

    private final EmailService email;

    private final ConfigReader config;

    public RepositoryUserService(SecurityUtils securityUtils,
                                 PasswordEncoder passwordEncoder,
                                 UserDao userDao,
                                 PatientRecordDao patientRecordDao,
                                 EmailService email,
                                 ConfigReader config) {
        this.securityUtils = securityUtils;
        this.passwordEncoder = passwordEncoder;
        this.userDao = userDao;
        this.patientRecordDao = patientRecordDao;
        this.email = email;
        this.config = config;
    }

    @Override
    protected GenericDao<User> getPrimaryDao() {
        return userDao;
    }

    @Override
    public User findByUsername(String username) {
        User user = userDao.findByUsername(username);
        return user;
    }

    @Override
    public List<User> findByInstitution(Institution institution) {
        Objects.requireNonNull(institution);
        return userDao.findByInstitution(institution);
    }

    @Override
    public User findByEmail(String email) {
        return Optional.ofNullable(userDao.findByEmail(email))
            .orElseThrow(
                () -> {
                    if (LOG.isTraceEnabled()) {
                        LOG.trace(
                            String.format("Could not find user by email '%s'.", email)
                        );
                    }
                    return new NotFoundException(
                        String.format("Could not find user by email '%s'.", email)
                    );
                }
            );
    }

    @Override
    public User findByToken(String token) {
        return userDao.findByToken(token);
    }

    @Override
    public String generateUsername(String usernamePrefix) {
        return usernamePrefix + (userDao.findAll().stream()
            .filter(u -> u.getUsername().startsWith(usernamePrefix))
            .map(u -> u.getUsername().replaceFirst(usernamePrefix, ""))
            .filter(s -> StringUtils.isNotBlank(s) && StringUtils.isNumeric(s))
            .map(s -> Integer.parseInt(s))
            .max(Comparator.naturalOrder())
            .orElse(0) + 1);
    }

    @Override
    public void update(User user, boolean sendEmail, String emailType) {
        final User currentUser = securityUtils.getCurrentUser();
        this.update(user);
        BaseEmailTemplate emailTemplate;
        if (emailType.equals("passwordChange") && (currentUser.getUsername().equals(user.getUsername()) || sendEmail)) {
            emailTemplate = new PasswordChange(config, user);
            email.sendEmail(emailTemplate, user.getEmailAddress(), currentUser.getEmailAddress(), !user.getUsername().equals(currentUser.getUsername()));
        } else if (emailType.equals("profileUpdate") && !currentUser.getUsername().equals(user.getUsername()) && sendEmail) {
            emailTemplate = new ProfileUpdate(config, user);
            email.sendEmail(emailTemplate, user.getEmailAddress(), currentUser.getEmailAddress(), !user.getUsername().equals(currentUser.getUsername()));
        }
    }

    @Override
    public void changePassword(User user, String newPassword, String currentPassword, boolean sendEmail) {
        final User currentUser = securityUtils.getCurrentUser();
        if (currentUser.getUsername().equals(user.getUsername()) && !passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new ValidationException("The passed user's current password is different from the specified one.");
        }
        user.setPassword(newPassword);
        user.encodePassword(passwordEncoder);
        this.update(user, sendEmail, "passwordChange");
    }

    @Override
    public void changePasswordByToken(User user, String password) {
        Objects.requireNonNull(user);
        user.setPassword(password);
        user.encodePassword(passwordEncoder);
        user.setToken(null);
        userDao.update(user);
    }

    @Override
    public void resetPassword(User user, String recipientEmail) {
        Objects.requireNonNull(user);
        user.setToken(IdentificationUtils.generateRandomToken());
        BaseEmailTemplate emailTemplate = new PasswordReset(config, user);
        email.sendEmail(emailTemplate, recipientEmail, null, false);
        userDao.update(user);
    }

    @Override
    public void sendInvitation(User user) {
        final User currentUser = securityUtils.getCurrentUser();
        Objects.requireNonNull(user);
        user.setIsInvited(true);
        user.setToken(IdentificationUtils.generateRandomToken());
        BaseEmailTemplate emailTemplate = new UserInvite(config, user);
        email.sendEmail(emailTemplate, user.getEmailAddress(), currentUser.getEmailAddress(), true);
        userDao.update(user);
    }

    @Override
    protected void prePersist(User instance) {
        if (userDao.findByUsername(instance.getUsername()) != null) {
            throw new EntityExistsException("User with specified username already exists.");
        }
        if (userDao.findByEmail(instance.getEmailAddress()) != null) {
            throw new EntityExistsException("User with specified email already exists.");
        }
        try {
            instance.encodePassword(passwordEncoder);
            Validator.validateUsername(instance.getUsername());
            Validator.validateEmail(instance.getEmailAddress());
        } catch (IllegalStateException e) {
            throw new ValidationException(e.getMessage());
        }
        instance.setToken(null);
        if (instance.getIsInvited() == null) {
            instance.setIsInvited(false);
        }
    }

    @Override
    protected void preUpdate(User instance) {
        final User currentUser = securityUtils.getCurrentUser();
        if (!currentUser.getTypes().contains(Vocabulary.s_c_administrator)
            && (!instance.getTypes().equals(currentUser.getTypes()) || (instance.getInstitution() != null
            && !instance.getInstitution().getKey().equals(currentUser.getInstitution().getKey())))) {
            throw new UnauthorizedException("Cannot update user.");
        }
        try {
            Validator.validateEmail(instance.getEmailAddress());
        } catch (IllegalStateException e) {
            throw new ValidationException(e.getMessage());
        }
        if (!userDao.findByUsername(instance.getUsername()).getUri().equals(instance.getUri())) {
            throw new EntityExistsException("User with specified username already exists.");
        }
        User userWithExistingEmail = userDao.findByEmail(instance.getEmailAddress());
        if (userWithExistingEmail != null && !userWithExistingEmail.getUsername().equals(instance.getUsername())) {
            throw new EntityExistsException("User with specified email already exists.");
        }
        final User orig = userDao.find(instance.getUri());
        if (orig == null) {
            throw new IllegalArgumentException("Cannot update user URI.");
        }
        if (StringUtils.isBlank(instance.getPassword())) {
            instance.setPassword(orig.getPassword());
        }
    }

    @Override
    protected void preRemove(User instance) {
        if (!patientRecordDao.findByAuthor(instance).isEmpty()) {
            throw new ValidationException("User with patient records cannot be deleted.");
        }
    }
}
