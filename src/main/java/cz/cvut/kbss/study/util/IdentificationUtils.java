package cz.cvut.kbss.study.util;

import java.util.Random;

public class IdentificationUtils {

    private static final int RANDOM_BOUND = 10000;

    private static final Random RANDOM = new Random();

    private IdentificationUtils() {
        throw new AssertionError();
    }

    /**
     * Generates a pseudo-unique OWL key using current system time and a random generator.
     *
     * @return OWL key
     */
    public static String generateKey() {
        String key = Long.toString(System.nanoTime());
        return key.concat(Integer.toString(RANDOM.nextInt(RANDOM_BOUND)));
    }
}
