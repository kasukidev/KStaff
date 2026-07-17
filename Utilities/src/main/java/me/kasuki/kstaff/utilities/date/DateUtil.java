package me.kasuki.kstaff.utilities.date;

import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class DateUtil {
    private final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd");

    /**
     * Returns the current date formatted as MM/dd
     */
    public String getDate() {
        return LocalDate.now().format(DATE_FORMAT);
    }
}
