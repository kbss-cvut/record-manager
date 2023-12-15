package cz.cvut.kbss.study.rest.util;

import cz.cvut.kbss.study.persistence.dao.util.RecordFilterParams;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RecordFilterMapperTest {

    @ParameterizedTest
    @MethodSource("testValues")
    void testConstructRecordFilter(MultiValueMap<String, String> params, RecordFilterParams expected) {
        assertEquals(expected, RecordFilterMapper.constructRecordFilter(params));
    }

    static Stream<Arguments> testValues() {
        return Stream.of(
                Arguments.of(new LinkedMultiValueMap<>(Map.of(
                        "minDate", List.of(LocalDate.now().minusYears(1).toString())
                )), new RecordFilterParams(null, LocalDate.now().minusYears(1), LocalDate.now(),
                                           Collections.emptySet())),
                Arguments.of(new LinkedMultiValueMap<>(Map.of(
                        "minDate", List.of(LocalDate.now().minusYears(1).toString()),
                        "maxDate", List.of(LocalDate.now().minusDays(1).toString())
                )), new RecordFilterParams(null, LocalDate.now().minusYears(1), LocalDate.now().minusDays(1),
                                           Collections.emptySet())),
                Arguments.of(new LinkedMultiValueMap<>(Map.of(
                        "institution", List.of("1111111")
                )), new RecordFilterParams("1111111", LocalDate.EPOCH, LocalDate.now(), Collections.emptySet())),
                Arguments.of(new LinkedMultiValueMap<>(Map.of(
                        "institution", List.of("1111111"),
                        "phase", List.of("http://example.org/phaseOne", "http://example.org/phaseTwo")
                )), new RecordFilterParams("1111111", LocalDate.EPOCH, LocalDate.now(),
                                           Set.of("http://example.org/phaseOne", "http://example.org/phaseTwo"))),
                Arguments.of(new LinkedMultiValueMap<>(Map.of(
                        "minDate", List.of(LocalDate.now().minusYears(1).toString()),
                        "maxDate", List.of(LocalDate.now().minusDays(1).toString()),
                        "institution", List.of("1111111"),
                        "phase", List.of("http://example.org/phaseOne")
                )), new RecordFilterParams("1111111", LocalDate.now().minusYears(1), LocalDate.now().minusDays(1),
                                           Set.of("http://example.org/phaseOne")))
        );
    }
}