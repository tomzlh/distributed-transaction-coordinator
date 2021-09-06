package com.ops.sc.common.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.Date;


public class DateUtil {
    private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";

    public static String date2String(Date date) {
        DateFormat df = new SimpleDateFormat(DATE_PATTERN);
        return df.format(date);
    }

    /**
     * 当天零点
     *
     * @return
     */
    public static Date getTodayStart() {
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        return Date.from(todayStart.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 当天结束时间
     *
     * @return
     */
    public static Date getTodayEnd() {
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        return Date.from(todayEnd.atZone(ZoneId.systemDefault()).toInstant());
    }


    public static final Duration DEFAULT_DURATION = Duration.ofMillis(-1);

    public static final String DAY_UNIT = "d";
    public static final String HOUR_UNIT = "h";
    public static final String MINUTE_UNIT = "m";
    public static final String SECOND_UNIT = "s";
    public static final String MILLIS_SECOND_UNIT = "ms";

    public static Duration parse(String str) {
        if (StringTools.isEmpty(str)) {
            return DEFAULT_DURATION;
        }

        if (str.contains(MILLIS_SECOND_UNIT)) {
            Long value = doParse(MILLIS_SECOND_UNIT, str);
            return value == null ? null : Duration.ofMillis(value);
        } else if (str.contains(DAY_UNIT)) {
            Long value = doParse(DAY_UNIT, str);
            return value == null ? null : Duration.ofDays(value);
        } else if (str.contains(HOUR_UNIT)) {
            Long value = doParse(HOUR_UNIT, str);
            return value == null ? null : Duration.ofHours(value);
        } else if (str.contains(MINUTE_UNIT)) {
            Long value = doParse(MINUTE_UNIT, str);
            return value == null ? null : Duration.ofMinutes(value);
        } else if (str.contains(SECOND_UNIT)) {
            Long value = doParse(SECOND_UNIT, str);
            return value == null ? null : Duration.ofSeconds(value);
        }
        try {
            int millis = Integer.parseInt(str);
            return Duration.ofMillis(millis);
        } catch (Exception e) {
            throw new UnsupportedOperationException(str + " can't parse to duration", e);
        }
    }

    private static Long doParse(String unit, String str) {
        str = str.replace(unit, "");
        if ("".equals(str)) {
            return null;
        }
        try {
            return Long.parseLong(str);
        } catch (NumberFormatException e) {
            throw new UnsupportedOperationException("\"" + str + "\" can't parse to Duration", e);
        }
    }


}
