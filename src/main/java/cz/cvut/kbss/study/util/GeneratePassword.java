package cz.cvut.kbss.study.util;

import org.apache.commons.lang.RandomStringUtils;

public class GeneratePassword {
    private static final int PASSWORD_LENGHT = 4;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    public static String generatePassword() {
        return RandomStringUtils.random( PASSWORD_LENGHT, CHARACTERS );
    }
}
