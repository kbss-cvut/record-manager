package cz.cvut.kbss.study.service;

import cz.cvut.kbss.study.model.Institution;
import cz.cvut.kbss.study.model.User;

import java.util.List;

public interface UserService extends BaseService<User> {

    User findByUsername(String username);

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

    void changePassword(User user, String newPassword, String currentPassword);

    void changePasswordByToken(User user, String password);

    void resetPassword(User user, String emailAddress);

}
