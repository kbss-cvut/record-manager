package cz.cvut.kbss.study.rest.util;

import cz.cvut.kbss.study.util.Constants;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class RestUtils {

    /**
     * Supported export media types
     */
    public static List<String> SUPPORTED_EXPORT_MDEIA_TYPES = Arrays.asList(Constants.MEDIA_TYPE_EXCEL, MediaType.APPLICATION_JSON_VALUE);

    /**
     * Prefix indicating ascending sort order.
     */
    public static final char SORT_ASC = '+';

    /**
     * Prefix indicating descending sort order.
     */
    public static final char SORT_DESC = '-';

    private RestUtils() {
        throw new AssertionError();
    }

    /**
     * Creates HTTP headers object with a location header with the specified path appended to the current request URI.
     * <p>
     * The {@code uriVariableValues} are used to fill in possible variables specified in the {@code path}.
     *
     * @param path              Path to add to the current request URI in order to construct a resource location
     * @param uriVariableValues Values used to replace possible variables in the path
     * @return HttpHeaders with location headers set
     */
    public static HttpHeaders createLocationHeaderFromCurrentUri(String path, Object... uriVariableValues) {
        assert path != null;

        final URI location = ServletUriComponentsBuilder.fromCurrentRequestUri().path(path).buildAndExpand(
                uriVariableValues).toUri();
        final HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.LOCATION, location.toASCIIString());
        return headers;
    }

    /**
     * Creates HTTP headers object with a location header with the specified path appended to the current context path.
     * <p>
     * The {@code uriVariableValues} are used to fill in possible variables specified in the {@code path}.
     * <p>
     * This method is useful when major part of the request URI needs to be replaced and so it is easier to start from
     * base context path and append the new path to it.
     *
     * @param path              Path to add to the current request URI in order to construct a resource location
     * @param uriVariableValues Values used to replace possible variables in the path
     * @return HttpHeaders with location headers set
     */
    public static HttpHeaders createLocationHeaderFromContextPath(String path, Object... uriVariableValues) {
        assert path != null;

        final URI location = ServletUriComponentsBuilder.fromCurrentContextPath().path(path)
                                                        .buildAndExpand(uriVariableValues).toUri();
        final HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.LOCATION, location.toASCIIString());
        return headers;
    }

    /**
     * Encodes the specifies value with an URL encoder.
     *
     * @param value The value to encode
     * @return Encoded string
     */
    public static String encodeUrl(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    public static String getCookie(HttpServletRequest request, String cookieName) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals(cookieName)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * Parses the specified date string.
     * <p>
     * The parameter is expected to be in the ISO format.
     *
     * @param dateStr Date string
     * @return {@code LocalDate} object corresponding to the specified date string
     * @throws ResponseStatusException Bad request is thrown if the date string is not parseable
     */
    public static LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr);
        } catch (DateTimeParseException | NullPointerException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                              "Value '" + dateStr + "' is not a valid date in ISO format.");
        }
    }

    /**
     * Resolves paging and sorting configuration from the specified request parameters.
     * <p>
     * If no paging and filtering info is specified, an {@link Pageable#unpaged()} object is returned.
     * <p>
     * Note that for sorting, {@literal +} should be used before sorting property name to specify ascending order,
     * {@literal -} for descending order, for example, {@literal -date} indicates sorting by date in descending order.
     *
     * @param params Request parameters
     * @return {@code Pageable} containing values resolved from the params or defaults
     */
    public static Pageable resolvePaging(MultiValueMap<String, String> params) {
        if (params.getFirst(Constants.PAGE_PARAM) == null) {
            return Pageable.unpaged();
        }
        final int page = Integer.parseInt(params.getFirst(Constants.PAGE_PARAM));
        final int size = Optional.ofNullable(params.getFirst(Constants.PAGE_SIZE_PARAM)).map(Integer::parseInt)
                                 .orElse(Constants.DEFAULT_PAGE_SIZE);
        if (params.containsKey(Constants.SORT_PARAM)) {
            final Sort sort = Sort.by(params.get(Constants.SORT_PARAM).stream().map(sp -> {
                if (sp.charAt(0) == SORT_ASC || sp.charAt(0) == SORT_DESC) {
                    final String property = sp.substring(1);
                    return sp.charAt(0) == SORT_DESC ? Sort.Order.desc(property) : Sort.Order.asc(property);
                }
                return Sort.Order.asc(sp);
            }).collect(Collectors.toList()));
            return PageRequest.of(page, size, sort);
        }
        return PageRequest.of(page, size);
    }

    public static boolean isSupportedExportType(MediaType mt){
        return SUPPORTED_EXPORT_MDEIA_TYPES.stream().filter(s -> mt.toString().contains(s)).findAny().isPresent();
    }
}
