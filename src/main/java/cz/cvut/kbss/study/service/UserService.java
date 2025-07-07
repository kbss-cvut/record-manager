package cz.cvut.kbss.study.service;

import cz.cvut.kbss.study.model.Institution;
import cz.cvut.kbss.study.model.User;

import java.util.List;

public interface UserService extends BaseService<User> {

    User findByUsername(String username);

    /**
     * Retrieve currently authenticated user. The returned <code>User</code> entity is populated
     * with information from the security context.
     * @return User currently authenticated user or null.
     */
    User getCurrentUser();

    /**
     * Retrieve persisted user from the current authenticated user. The returned <code>User</code>
     * entity is not populated with information from the security context. See
     * {@link #getCurrentUser()} to retrieve security context as well.
     * @return User currently authenticated user or null.
     */
    User findCurrentUser();

    User findByEmail(String email);

    User findByToken(String token);

    /**
     * Gets users associated with the specified institution.
     *
     * @param institution The institution to filter by
     * @return Records of matching users
     */
    List<User> findByInstitution(Institution institution);

    String generateUsername(String usernamePrefix);

    void update(User user, boolean sendEmail, String emailType);

    void changePassword(User user, String newPassword, String currentPassword, boolean sendEmail);

    void changePasswordByToken(User user, String password);

    void resetPassword(User user, String emailAddress);

    void sendInvitation(User user);

}
