package cz.cvut.kbss.study.rest.util;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
}