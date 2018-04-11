package cz.cvut.kbss.study.util;

import cz.cvut.kbss.study.exception.ValidationException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validator {
    public static void validateEmail(String email) {
        Pattern p = Pattern.compile("^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]" +
                "+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])" +
                "|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$");
        Matcher m = p.matcher(email);
        if (!m.find()) {
            throw new ValidationException("Email is not valid.");
        }
    }

    public static void validateUsername(String username) {
        Pattern p = Pattern.compile("[^A-Za-z0-9]");
        Matcher m = p.matcher(username);
        if (m.find()) {
            throw new ValidationException("Username cannot contain special characters.");
        }
    }
}
