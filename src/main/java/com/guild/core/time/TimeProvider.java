package com.guild.core.time;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public final class TimeProvider {

    private static final ZoneId SERVER_ZONE = ZoneId.systemDefault();

    public static final DateTimeFormatter FULL_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private TimeProvider() {}

    public static ZonedDateTime now() {
        return ZonedDateTime.now(SERVER_ZONE);
    }

    public static LocalDateTime nowLocalDateTime() {
        return LocalDateTime.now(SERVER_ZONE);
    }

    public static String nowString() {
        return nowLocalDateTime().format(FULL_FORMATTER);
    }

    public static String plusMinutesString(int minutes) {
        return nowLocalDateTime().plusMinutes(minutes).format(FULL_FORMATTER);
    }

    public static String plusDaysString(int days) {
        return nowLocalDateTime().plusDays(days).format(FULL_FORMATTER);
    }

    public static String format(LocalDateTime dateTime) {
        if (dateTime == null) return "Desconhecido";
        return dateTime.format(FULL_FORMATTER);
    }

    public static String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) return "Desconhecido";
        return dateTime.format(DATE_FORMATTER);
    }
}


