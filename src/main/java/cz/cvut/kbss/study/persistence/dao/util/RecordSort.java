package cz.cvut.kbss.study.persistence.dao.util;

import org.springframework.data.domain.Sort;

import java.util.Set;

/**
 * Provides constants for sorting records.
 */
public class RecordSort {

    /**
     * Property used to sort records by date of last modification (if available) or creation.
     */
    public static final String SORT_DATE_PROPERTY = "date";

    /**
     * Supported sorting properties.
     */
    public static final Set<String> SORTING_PROPERTIES = Set.of(SORT_DATE_PROPERTY);

    private RecordSort() {
        throw new AssertionError();
    }

    /**
     * Returns the default sort for retrieving records.
     * <p>
     * By default, records are sorted by date of last modification/creation in descending order.
     *
     * @return Default sort
     */
    public static Sort defaultSort() {
        return Sort.by(Sort.Order.desc("date"));
    }
}
