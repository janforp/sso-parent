package com.janita.sso.cache.service;

import org.springframework.data.redis.connection.SortParameters;
import org.springframework.data.redis.core.query.SortQuery;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 *
 */
public class CommonSortQuery implements SortQuery<String> {
    private final String key;
    private final int start;
    private final int count;
    private final String by;
    private final String direction;

    public CommonSortQuery(String key, int start, int count, String by, String direction) {
        this.key = key;
        this.start = start;
        this.count = count;
        this.by = by;
        this.direction = direction;
    }

    /**
     * Returns the sorting order. Can be null if nothing is specified.
     *
     * @return sorting order
     */
    @Override
    public SortParameters.Order getOrder() {
        return StringUtils.isEmpty(direction) ? null : SortParameters.Order.valueOf(direction);
    }

    /**
     * Indicates if the sorting is numeric (default) or alphabetical (lexicographical). Can be null if nothing is
     * specified.
     *
     * @return the type of sorting
     */
    @Override
    public Boolean isAlphabetic() {
        return true;
    }

    /**
     * Returns the sorting limit (range or pagination). Can be null if nothing is specified.
     *
     * @return sorting limit/range
     */
    @Override
    public SortParameters.Range getLimit() {
        return new SortParameters.Range(start, count);
    }

    /**
     * Return the target key for sorting.
     *
     * @return the target key
     */
    @Override
    public String getKey() {
        return key;
    }

    /**
     * Returns the pattern of the external key used for sorting.
     *
     * @return the external key pattern
     */
    @Override
    public String getBy() {
        return by;
    }

    /**
     * Returns the external key(s) whose values are returned by the sort.
     *
     * @return the (list of) keys used for GET
     */
    @Override
    public List<String> getGetPattern() {
        return null;
    }
}
