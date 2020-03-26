package com.dreamscale.gridtime.api.shared;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class TimeFormatter {

    public static String format(LocalDateTime time) {
        String timeStr = null;

        if (time != null) {
            ZonedDateTime zonedTime = ZonedDateTime.of(time, ZoneId.of("UTC"));
            DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
            timeStr = formatter.format(zonedTime);
        }

        return timeStr;
    }

}
