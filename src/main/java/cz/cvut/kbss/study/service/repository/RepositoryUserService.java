package cz.cvut.kbss.study.service.repository;

import cz.cvut.kbss.jopa.exceptions.EntityNotFoundException;
import cz.cvut.kbss.study.dto.PatientRecordDto;
import cz.cvut.kbss.study.exception.EntityExistsException;
import cz.cvut.kbss.study.exception.NotFoundException;
import cz.cvut.kbss.study.exception.ValidationException;
import cz.cvut.kbss.study.model.Institution;
import cz.cvut.kbss.study.model.PatientRecord;
import cz.cvut.kbss.study.model.Role;
import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.persistence.dao.GenericDao;
import cz.cvut.kbss.study.persistence.dao.PatientRecordDao;
import cz.cvut.kbss.study.persistence.dao.UserDao;
import cz.cvut.kbss.study.persistence.dao.util.RecordFilterParams;
import cz.cvut.kbss.study.service.ConfigReader;
import cz.cvut.kbss.study.service.EmailService;
import cz.cvut.kbss.study.service.UserService;
import cz.cvut.kbss.study.service.security.SecurityUtils;
import cz.cvut.kbss.study.util.IdentificationUtils;
import cz.cvut.kbss.study.util.Validator;
import cz.cvut.kbss.study.util.etemplates.BaseEmailTemplate;
import cz.cvut.kbss.study.util.etemplates.PasswordChange;
import cz.cvut.kbss.study.util.etemplates.PasswordReset;
import cz.cvut.kbss.study.util.etemplates.ProfileUpdate;
import cz.cvut.kbss.study.util.etemplates.UserInvite;
import org.apache.commons.lang.StringUtils;
import org.eclipse.rdf4j.http.protocol.UnauthorizedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

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

    @Transactional(readOnly = true)
    @Override
    public User findByUsername(String username) {
        return userDao.findByUsername(username);
    }

    @Override
    public User getCurrentUser() {
        return securityUtils.getCurrentUser();
    }

    @Transactional(readOnly = true)
    @Override
    public User findCurrentUser() {
        String currentUserName = securityUtils.getCurrentUserUsername();
        return userDao.findByUsername(currentUserName);
    }

    @Transactional(readOnly = true)
    @Override
    public List<User> findByInstitution(Institution institution) {
        Objects.requireNonNull(institution);
        return userDao.findByInstitution(institution);
    }

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
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
            .map(Integer::parseInt)
            .max(Comparator.naturalOrder())
            .orElse(0) + 1);
    }

    @Transactional
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

    @Transactional
    @Override
    public void changePassword(User user, String newPassword, String currentPassword, boolean sendEmail) {
        final User currentUser = securityUtils.getCurrentUser();
        if (currentUser.getUsername().equals(user.getUsername()) && !passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new ValidationException("error.password.validation.thePassedUsersCurrentPasswordIsDifferentFromTheSpecifiedOne",
                    "The passed user's current password is different from the specified one.");
        }
        user.setPassword(newPassword);
        user.encodePassword(passwordEncoder);
        this.update(user, sendEmail, "passwordChange");
    }

    @Transactional
    @Override
    public void changePasswordByToken(User user, String password) {
        Objects.requireNonNull(user);
        user.setPassword(password);
        user.encodePassword(passwordEncoder);
        user.setToken(null);
        userDao.update(user);
    }

    @Transactional
    @Override
    public void resetPassword(User user, String recipientEmail) {
        Objects.requireNonNull(user);
        user.setToken(IdentificationUtils.generateRandomToken());
        BaseEmailTemplate emailTemplate = new PasswordReset(config, user);
        email.sendEmail(emailTemplate, recipientEmail, null, false);
        userDao.update(user);
    }

    @Transactional
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

    protected void validatePermissionToUpdate(User currentUser, User toUpdate, User original) {
        boolean differentUser = !Objects.equals(toUpdate.getUsername(), currentUser.getUsername());

        boolean hasWriteAllUsers = securityUtils.hasRole(Role.writeAllUsers);
        boolean lacksPrivilegeOfUpdatedUser = !securityUtils.hasSupersetOfRoles(currentUser, original);

        boolean sameInstitution = toUpdate.getInstitution() != null
                && currentUser.getInstitution() != null
                && toUpdate.getInstitution().getKey().equals(currentUser.getInstitution().getKey());

        boolean hasWriteOrganizationUsers = securityUtils.hasRole(Role.writeOrganizationUsers) && sameInstitution;

        boolean noWriteToUpdatedUser = !(hasWriteAllUsers || hasWriteOrganizationUsers);

        if (differentUser) {
            if (noWriteToUpdatedUser) {
                throw new UnauthorizedException(String.format("Cannot update user %s as current user %s does not have write permission to update it.",
                        original.getUsername(),
                        currentUser.getUsername()));
            }
            if (lacksPrivilegeOfUpdatedUser) {
                throw new UnauthorizedException(String.format(
                        "Cannot update user with privileges %s as they are no subset of privileges %s belonging to the current user %s.",
                        original.getRoleGroup().getRoles().toString(),
                        currentUser.getRoleGroup().getRoles().toString(),
                        currentUser.getUsername()));
            }
        }
    }

    protected void validateRecordsAgainstCollisions(User toUpdate, User original) {

        if (toUpdate.getInstitution() == null) return;

        Map<String, PatientRecordDto> existingInstitutionRecords = patientRecordDao.findByInstitution(toUpdate.getInstitution()).stream().collect(
                Collectors.toMap(PatientRecordDto::getLocalName, r -> r)
        );
        Map<String, PatientRecord> newInstitutionRecords = patientRecordDao.findByAuthor(original).stream().collect(
                Collectors.toMap(PatientRecord::getLocalName, r -> r)
        );

        Map<String, List<String>> conflictingRecords = newInstitutionRecords.keySet().stream()
                .filter(existingInstitutionRecords::containsKey)
                .collect(Collectors.toMap(
                        key -> key,
                        key -> Arrays.asList(existingInstitutionRecords.get(key).getKey(), newInstitutionRecords.get(key).getKey())
                ));


        if (!conflictingRecords.isEmpty()) {

            String conflictsFormatted = conflictingRecords.entrySet().stream()
                    .map(e -> String.format(
                            "• %s (existing record ID: %s, new record ID: %s)",
                            e.getKey(),
                            e.getValue().get(0),
                            e.getValue().get(1)
                    ))
                    .collect(Collectors.joining("\n"));

            String message = String.format(
                    "User cannot be moved to institution '%s' because there are conflicting records with the same name:\n%s.",
                    toUpdate.getInstitution().getName(),
                    conflictsFormatted
            );

            throw new ValidationException(message);
        }
    }

    protected void changePatientRecordsInstitution(User original, Institution newInstitution) {
        List<PatientRecord> recordsToUpdate = patientRecordDao.findByAuthor(original);
        for (PatientRecord record : recordsToUpdate) {
            record.setInstitution(newInstitution);
            patientRecordDao.update(record);
        }
    }

    @Override
    protected void preUpdate(User instance) {
        final User currentUser = securityUtils.getCurrentUser();
        final User original = userDao.findByUsername(instance.getUsername());

        if (original == null) {
            throw new EntityNotFoundException("User with specified username does not exist.");
        }

        validatePermissionToUpdate(currentUser, instance, original);

        if (instance.getInstitution() != original.getInstitution()) {
            validateRecordsAgainstCollisions(instance, original);
            changePatientRecordsInstitution(original, instance.getInstitution());
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
