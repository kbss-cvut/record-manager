package cz.cvut.kbss.study.util;

import cz.cvut.kbss.study.exception.RecordManagerException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
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
            throw new RecordManagerException(
                    "Initialization exception. Query file not found in " + Constants.QUERY_DIRECTORY +
                            File.separator + queryFileName);
        }
        try (final BufferedReader in = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            return in.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new RecordManagerException("Initialization exception. Unable to load query!", e);
        }
    }

    public static URI prepareUri(String remoteUrl, Map<String, String> queryParams) {
        final StringBuilder sb = new StringBuilder(remoteUrl);
        boolean containsQueryString = remoteUrl.matches("^.+\\?.+=.+$");
        for (Map.Entry<String, String> e : queryParams.entrySet()) {
            sb.append(!containsQueryString ? '?' : '&');
            sb.append(e.getKey()).append('=').append(e.getValue());
            containsQueryString = true;
        }
        return URI.create(sb.toString());
    }

    /**
     * Returns specified URI enclosed in &lt; and &gt;.
     *
     * @param uri URI to stringify
     * @return URI in angle brackets
     */
    public static String uriToString(URI uri) {
        return "<" + uri + ">";
    }

    public static String kebabToCamel(String str) {
        StringBuilder result = new StringBuilder();
        boolean upperNext = false;
        for (char c : str.toCharArray()) {
            if (c == '-') {
                upperNext = true;
            } else {
                result.append(upperNext ? Character.toUpperCase(c) : c);
                upperNext = false;
            }
        }
        return result.toString();
    }
}
