package cz.cvut.kbss.study.util;

import cz.cvut.kbss.study.exception.FormManagerException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class Utils {

    /**
     * Loads query from the specified file.
     * <p>
     * The query should be in the query directory specified by {@link Constants#QUERY_DIRECTORY}.
     *
     * @param queryFileName Name of the query file
     * @return Query string read from the file
     */
    public static String loadQuery(String queryFileName) {
        final InputStream is = Utils.class.getClassLoader().getResourceAsStream(
                Constants.QUERY_DIRECTORY + File.separator + queryFileName);
        if (is == null) {
            throw new FormManagerException(
                    "Initialization exception. Query file not found in " + Constants.QUERY_DIRECTORY +
                            File.separator + queryFileName);
        }
        try (final BufferedReader in = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            return in.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new FormManagerException("Initialization exception. Unable to load query!", e);
        }
    }
}
