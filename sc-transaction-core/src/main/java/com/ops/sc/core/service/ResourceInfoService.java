package com.ops.sc.core.service;

import java.util.Locale;



public interface ResourceInfoService {

    String getMessage(String infoKey, Locale locale, Locale defaultLocale, String... args);

    String getMessage(String infoKey, Locale locale, String... args);
}
