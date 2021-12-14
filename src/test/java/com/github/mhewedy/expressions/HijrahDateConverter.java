package com.github.mhewedy.expressions;

import lombok.extern.slf4j.Slf4j;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.time.chrono.HijrahDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Converter(autoApply = true)
public class HijrahDateConverter implements AttributeConverter<HijrahDate, Integer> {

    private static final DateTimeFormatter BASIC_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public Integer convertToDatabaseColumn(HijrahDate date) {
        return formatHijriAsInteger(date);
    }

    @Override
    public HijrahDate convertToEntityAttribute(Integer dbDate) {
        return parseHijriDate(dbDate);
    }

    private static Integer formatHijriAsInteger(HijrahDate hijrahDate) {
        if (hijrahDate == null) {
            return null;
        }
        return Integer.parseInt(hijrahDate.format(BASIC_DATE_FORMAT));
    }

    private static HijrahDate parseHijriDate(Integer date) {
        String d;
        if (date == null || date == 0 || (d = String.valueOf(date)).length() != 8) {
            return null;
        }
        return HijrahDate.of(
                Integer.parseInt(d.substring(0, 4)),
                Integer.parseInt(d.substring(4, 6)),
                Integer.parseInt(d.substring(6, 8))
        );
    }
}
