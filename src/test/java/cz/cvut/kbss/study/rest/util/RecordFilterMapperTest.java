package cz.cvut.kbss.study.rest.util;

import cz.cvut.kbss.study.model.RecordPhase;
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
                )), new RecordFilterParams(null, null, LocalDate.now().minusYears(1), LocalDate.now(),
                                           Collections.emptySet(), Collections.emptySet())),
                Arguments.of(new LinkedMultiValueMap<>(Map.of(
                        "minDate", List.of(LocalDate.now().minusYears(1).toString()),
                        "maxDate", List.of(LocalDate.now().minusDays(1).toString())
                )), new RecordFilterParams(null, null, LocalDate.now().minusYears(1), LocalDate.now().minusDays(1),
                                           Collections.emptySet(), Collections.emptySet())),
                Arguments.of(new LinkedMultiValueMap<>(Map.of(
                        "institution", List.of("1111111")
                )), new RecordFilterParams(null,"1111111", LocalDate.EPOCH, LocalDate.now(), Collections.emptySet(), Collections.emptySet())),
                Arguments.of(new LinkedMultiValueMap<>(Map.of(
                        "institution", List.of("1111111"),
                        "phase", List.of(RecordPhase.open.getIri(), RecordPhase.completed.name())
                )), new RecordFilterParams(null, "1111111", LocalDate.EPOCH, LocalDate.now(),
                                           Set.of(RecordPhase.open.getIri(), RecordPhase.completed.getIri()), Collections.emptySet())),
                Arguments.of(new LinkedMultiValueMap<>(Map.of(
                        "minDate", List.of(LocalDate.now().minusYears(1).toString()),
                        "maxDate", List.of(LocalDate.now().minusDays(1).toString()),
                        "institution", List.of("1111111"),
                        "phase", List.of(RecordPhase.published.name())
                )), new RecordFilterParams(null,"1111111", LocalDate.now().minusYears(1), LocalDate.now().minusDays(1),
                                           Set.of(RecordPhase.published.getIri()), Collections.emptySet()))
        );
    }
}