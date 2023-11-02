package cz.cvut.kbss.study.util;

import java.math.BigInteger;
import java.net.URI;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

public class IdentificationUtils {

    private static final int RANDOM_BOUND = 10000;

    private static final Random RANDOM = new Random();

    private static final Random SECURE_RANDOM = new SecureRandom();

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

    /**
     * Generates a number for uri using  a random generator.
     *
     * @return String number
     */
    public static String generateRandomURINumber() {
        return Integer.toString(RANDOM.nextInt(RANDOM_BOUND));
    }

    /**
     * Generates a token for setting password using a secure random generator.
     *
     * @return String number
     */
    public static String generateRandomToken() {
        int length = 20;
        return String.format("%"+length+"s", new BigInteger(length*5/*base 32,2^5*/, SECURE_RANDOM)
                .toString(32)).replace('\u0020', '0');
    }

    /**
     * Generates a URI for the specified base.
     *
     * The URI consists of the base to which a generated UUID is appended.
     * @param base Base for the URI
     * @return Generated URI
     */
    public static URI generateUri(String base) {
        Objects.requireNonNull(base);
        if (base.charAt(base.length() - 1) != '/') {
            base += '/';
        }
        return URI.create(base + UUID.randomUUID());
    }
}
