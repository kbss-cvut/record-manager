package cz.cvut.kbss.study.rest.util;

import cz.cvut.kbss.study.environment.generator.Generator;
import cz.cvut.kbss.study.persistence.dao.util.RecordSort;
import cz.cvut.kbss.study.util.Constants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RestUtilsTest {

    @Test
    void parseTimestampReturnsLocalDateParsedFromSpecifiedString() {
        final LocalDate value = LocalDate.now();
        assertEquals(value, RestUtils.parseDate(value.toString()));
    }

    @Test
    void parseTimestampThrowsResponseStatusExceptionWithStatus400ForUnparseableString() {
        final Date date = new Date();
        final ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                                                        () -> RestUtils.parseDate(date.toString()));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void parseTimestampThrowsResponseStatusExceptionWithStatus400ForNullArgument() {
        final ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                                                        () -> RestUtils.parseDate(null));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void resolvePagingCreatesPageableObjectWithSpecifiedPageSizeAndNumber() {
        final int page = Generator.randomInt(0, 10);
        final int size = Generator.randomInt(20, 50);
        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>(Map.of(
                Constants.PAGE_PARAM, List.of(Integer.toString(page)),
                Constants.PAGE_SIZE_PARAM, List.of(Integer.toString(size))));

        final Pageable result = RestUtils.resolvePaging(params);
        assertEquals(PageRequest.of(page, size), result);
    }

    @Test
    void resolvePagingReturnsUnpagedObjectWhenNoPageNumberIsSpecified() {
        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        final Pageable result = RestUtils.resolvePaging(params);
        assertTrue(result.isUnpaged());
    }

    @Test
    void resolvePagingReturnsPagedObjectWithDefaultPageSizeWhenNoPageSizeIsSpecified() {
        final int page = Generator.randomInt(0, 10);
        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>(Map.of(
                Constants.PAGE_PARAM, List.of(Integer.toString(page))));

        final Pageable result = RestUtils.resolvePaging(params);
        assertEquals(PageRequest.of(page, Constants.DEFAULT_PAGE_SIZE), result);
    }

    @ParameterizedTest
    @MethodSource("sortTestArguments")
    void resolvePagingAddsSpecifiedSortPropertyAndDirectionToResult(String sortParam, Sort.Order expectedOrder) {
        final int page = Generator.randomInt(0, 10);
        final int size = Generator.randomInt(20, 50);
        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>(Map.of(
                Constants.PAGE_PARAM, List.of(Integer.toString(page)),
                Constants.PAGE_SIZE_PARAM, List.of(Integer.toString(size)),
                Constants.SORT_PARAM, List.of(sortParam)));

        final Pageable result = RestUtils.resolvePaging(params);
        assertNotNull(result.getSort());
        assertEquals(expectedOrder, result.getSort().getOrderFor(expectedOrder.getProperty()));
    }

    protected static Stream<Arguments> sortTestArguments() {
        return Stream.of(
                Arguments.of(RestUtils.SORT_DESC + RecordSort.SORT_DATE_PROPERTY,
                             Sort.Order.desc(RecordSort.SORT_DATE_PROPERTY)),
                Arguments.of(RestUtils.SORT_ASC + RecordSort.SORT_DATE_PROPERTY,
                             Sort.Order.asc(RecordSort.SORT_DATE_PROPERTY)),
                Arguments.of(RecordSort.SORT_DATE_PROPERTY, Sort.Order.asc(RecordSort.SORT_DATE_PROPERTY))
        );
    }

    @Test
    void resolvePagingSupportsMultipleSortValues() {
        final String anotherSort = "name";
        final int page = Generator.randomInt(0, 10);
        final int size = Generator.randomInt(20, 50);
        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>(Map.of(
                Constants.PAGE_PARAM, List.of(Integer.toString(page)),
                Constants.PAGE_SIZE_PARAM, List.of(Integer.toString(size)),
                Constants.SORT_PARAM, List.of(RecordSort.SORT_DATE_PROPERTY, RestUtils.SORT_ASC + anotherSort)));

        final Pageable result = RestUtils.resolvePaging(params);
        assertNotNull(result.getSort());
        assertEquals(Sort.Order.asc(RecordSort.SORT_DATE_PROPERTY),
                     result.getSort().getOrderFor(RecordSort.SORT_DATE_PROPERTY));
        assertEquals(Sort.Order.asc(anotherSort),
                     result.getSort().getOrderFor(anotherSort));
    }
}