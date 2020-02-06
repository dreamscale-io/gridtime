package com.dreamscale.gridtime.api.shared;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class TimeFormatter {

    public static String format(LocalDateTime time) {
        ZonedDateTime zonedTime = ZonedDateTime.of(time, ZoneId.of("UTC"));

        DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        return formatter.format(zonedTime);
    }

}
