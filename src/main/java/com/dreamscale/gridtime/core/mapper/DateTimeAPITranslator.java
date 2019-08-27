package com.dreamscale.gridtime.core.mapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeAPITranslator {

    public static LocalDateTime convertToDateTime(String dateTimeStr) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        return LocalDateTime.from(dateTimeFormatter.parse(dateTimeStr));
    }

    public static String convertToString(LocalDateTime localDateTime) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        return dateTimeFormatter.format(localDateTime);
    }
}
