package cz.cvut.kbss.study.rest.util;

import cz.cvut.kbss.study.model.RecordPhase;
import cz.cvut.kbss.study.persistence.dao.util.RecordFilterParams;
import cz.cvut.kbss.study.rest.exception.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Maps query parameters to {@link RecordFilterParams} instances.
 */
public class RecordFilterMapper {

    private static final Logger LOG = LoggerFactory.getLogger(RecordFilterMapper.class);

    private static final String MIN_DATE_PARAM = "minDate";

    private static final String MAX_DATE_PARAM = "maxDate";

    private static final String INSTITUTION_KEY_PARAM = "institution";

    private static final String PHASE_ID_PARAM = "phase";

    /**
     * Maps the specified single parameter and value to a new {@link RecordFilterParams} instance.
     *
     * @param param Parameter name
     * @param value Parameter value
     * @return New {@code RecordFilterParams} instance
     */
    public static RecordFilterParams constructRecordFilter(String param, String value) {
        return constructRecordFilter(new LinkedMultiValueMap<>(Map.of(param, List.of(value))));
    }

    /**
     * Maps the specified parameters to a new {@link RecordFilterParams} instance.
     *
     * @param params Request parameters to map
     * @return New {@code RecordFilterParams} instance
     */
    public static RecordFilterParams constructRecordFilter(MultiValueMap<String, String> params) {
        Objects.requireNonNull(params);
        final RecordFilterParams result = new RecordFilterParams();
        getSingleValue(MIN_DATE_PARAM, params).ifPresent(s -> {
            try {
                result.setMinModifiedDate(LocalDate.parse(s));
            } catch (DateTimeParseException e) {
                throw new BadRequestException("Value '" + s + "' is not a date in the ISO format.");
            }
        });
        getSingleValue(MAX_DATE_PARAM, params).ifPresent(s -> {
            try {
                result.setMaxModifiedDate(LocalDate.parse(s));
            } catch (DateTimeParseException e) {
                throw new BadRequestException("Value '" + s + "' is not a date in the ISO format.");
            }
        });
        getSingleValue(INSTITUTION_KEY_PARAM, params).ifPresent(result::setInstitutionKey);
        result.setPhaseIds(params.getOrDefault(PHASE_ID_PARAM, Collections.emptyList()).stream()
                                 .map(s -> RecordPhase.fromIriOrName(s).getIri()).collect(Collectors.toSet()));
        return result;
    }

    private static Optional<String> getSingleValue(String param, MultiValueMap<String, String> source) {
        final List<String> values = source.getOrDefault(param, Collections.emptyList());
        if (values.isEmpty()) {
            return Optional.empty();
        }
        if (values.size() > 1) {
            LOG.warn("Found multiple values of parameter '{}'. Using the first one - '{}'.", param, values.get(0));
        }
        return Optional.of(values.get(0));
    }
}
