
package com.ops.sc.core.rest.query;

import java.util.*;
import java.util.stream.Collectors;


public final class QueryParameterMap extends AbstractMap<String, List<String>> {
    
    private final Map<String, List<String>> queryMap;
    
    public QueryParameterMap() {
        queryMap = new LinkedHashMap<>();
    }
    
    public QueryParameterMap(final Map<String, List<String>> map) {
        queryMap = new LinkedHashMap<>(map);
    }
    
    /**
     * Get values by parameter name.
     *
     * @param parameterName parameter name
     * @return values
     */
    public List<String> get(final String parameterName) {
        return queryMap.get(parameterName);
    }
    
    /**
     * Get the first from values.
     *
     * @param parameterName parameter name
     * @return first value
     */
    public String getFirst(final String parameterName) {
        String firstValue = null;
        List<String> values = queryMap.get(parameterName);
        if (values != null && !values.isEmpty()) {
            firstValue = values.get(0);
        }
        return firstValue;
    }
    
    @Override
    public boolean isEmpty() {
        return queryMap.isEmpty();
    }
    
    @Override
    public Set<Entry<String, List<String>>> entrySet() {
        return queryMap.entrySet();
    }
    
    /**
     * Add value.
     *
     * @param parameterName parameter name
     * @param value value
     */
    public void add(final String parameterName, final String value) {
        List<String> values = queryMap.get(parameterName);
        if (null == values) {
            values = new LinkedList<>();
        }
        values.add(value);
        put(parameterName, values);
    }
    
    @Override
    public List<String> put(final String parameterName, final List<String> value) {
        return queryMap.put(parameterName, value);
    }
    
    /**
     * Convert to a single value map, abandon values except the first of each parameter.
     *
     * @return single value map
     */
    public Map<String, String> toSingleValueMap() {
        return queryMap.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().get(0)));
    }
}
