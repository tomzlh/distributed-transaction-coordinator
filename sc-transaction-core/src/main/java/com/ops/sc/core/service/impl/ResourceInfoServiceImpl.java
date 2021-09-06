package com.ops.sc.core.service.impl;


import com.ops.sc.core.service.ResourceInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;


@Service
public class ResourceInfoServiceImpl implements ResourceInfoService {
    @Autowired
    @Qualifier(value = "errorMessageResource")
    private MessageSource messageSource;

    @Override
    public String getMessage(String infoKey, Locale locale, Locale defaultLocale, String... args) {
        return messageSource.getMessage(infoKey, args, locale == null ? defaultLocale : locale);
    }

    @Override
    public String getMessage(String infoKey, Locale locale, String... args) {
        return getMessage(infoKey, locale, locale, args);
    }
}
