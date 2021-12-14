package com.github.mhewedy.expressions;

import java.time.chrono.HijrahDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;

class DateTimeUtil {

    private static final DateTimeFormatter HIJRAH_ISO_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    static String format(Temporal temporal) {
        if (temporal.getClass() == HijrahDate.class) {
            return ((HijrahDate) temporal).format(HIJRAH_ISO_DATE);
        }
        return temporal.toString();
    }

    static HijrahDate parseHijrah(String text) {
        if (text == null) {
            return null;
        }
        // yyyy-MM-dd
        String[] date = text.trim().split("-");
        if (date.length != 3) {
            throw new IllegalArgumentException("invalid Hijrah date: " + text);
        }
        return HijrahDate.of(Integer.parseInt(date[0]),
                Integer.parseInt(date[1]),
                Integer.parseInt(date[2]));
    }
}
