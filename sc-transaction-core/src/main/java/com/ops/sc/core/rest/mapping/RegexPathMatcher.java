
package com.ops.sc.core.rest.mapping;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class RegexPathMatcher implements PathMatcher {
    
    private static final String PATH_SEPARATOR = "/";
    
    private static final Pattern PATH_PATTERN = Pattern.compile("^/(([^/{}]+|\\{[^/{}]+})(/([^/{}]+|\\{[^/{}]+}))*/?)?$");
    
    private static final Pattern TEMPLATE_PATTERN = Pattern.compile("\\{(?<template>[^/]+)}");
    
    private static final String TEMPLATE_REGEX = "(?<${template}>[^/]+)";
    
    private final Map<String, Pattern> patternCache = new ConcurrentHashMap<>();
    
    @Override
    public Map<String, String> captureVariables(final String pathPattern, final String path) {
        Pattern compiled = getCompiledPattern(pathPattern);
        String pathWithoutQuery = trimUriQuery(path);
        Matcher matcher = compiled.matcher(pathWithoutQuery);
        if (!matcher.matches() || 0 == matcher.groupCount()) {
            return Collections.emptyMap();
        }
        Map<String, String> variables = new LinkedHashMap<>();
        for (String variableName : extractTemplateNames(pathPattern)) {
            variables.put(variableName, matcher.group(variableName));
        }
        return Collections.unmodifiableMap(variables);
    }
    
    @Override
    public boolean matches(final String pathPattern, final String path) {
        return getCompiledPattern(pathPattern).matcher(trimUriQuery(path)).matches();
    }
    
    @Override
    public boolean isValidPathPattern(final String pathPattern) {
        return PATH_PATTERN.matcher(pathPattern).matches();
    }
    
    private Pattern getCompiledPattern(final String pathPattern) {
        String regexPattern = convertToRegexPattern(pathPattern);
        patternCache.computeIfAbsent(regexPattern, Pattern::compile);
        return patternCache.get(regexPattern);
    }
    
    private String convertToRegexPattern(final String pathPattern) {
        return TEMPLATE_PATTERN.matcher(pathPattern).replaceAll(TEMPLATE_REGEX);
    }
    
    private List<String> extractTemplateNames(final String pathPattern) {
        String[] pathFragments = pathPattern.split(PATH_SEPARATOR);
        List<String> result = new ArrayList<>();
        for (String fragment : pathFragments) {
            int start = fragment.indexOf('{');
            int end = fragment.lastIndexOf('}');
            if (-1 != start && -1 != end) {
                result.add(fragment.substring(start + 1, end));
            }
        }
        return result;
    }
    
    private String trimUriQuery(final String uri) {
        int index = uri.indexOf('?');
        if (-1 != index) {
            return uri.substring(0, index);
        }
        return uri;
    }
}
