package com.dineshsawant.pg.cdc.util;

import com.dineshsawant.pg.cdc.exception.PgCDCException;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

public class DateTimeUtil {

    public static final DateTimeFormatter PG_X_TIMESTAMP_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd HH:mm:ss")
            .appendFraction(ChronoField.MILLI_OF_SECOND, 0, 6, true) // min 2 max 3
            .toFormatter();

    public static long convertToEpoch(String timestamp) {
        if (timestamp.endsWith("+00")) {
            final String withoutZoneOffset = timestamp.replaceFirst("\\+00", "");
            return LocalDateTime.parse(withoutZoneOffset, PG_X_TIMESTAMP_FORMATTER).toInstant(ZoneOffset.UTC).toEpochMilli();
        } else {
            throw new PgCDCException("New transaction timestamp format found [" + timestamp  +
                    "]. Possible Solution is setting UTC timezone to process");
        }
    }

    public static long convertToEpoch(LocalDateTime localDateTime) {
        return localDateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
    }
}
