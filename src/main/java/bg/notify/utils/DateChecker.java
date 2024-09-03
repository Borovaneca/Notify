package bg.notify.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public class DateChecker {

    public static boolean checkDateIfItsAfter(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", new Locale("bg", "BG"));
        LocalDate seminarDate = null;
        try {
            seminarDate = LocalDate.parse(date, formatter);
            if (seminarDate.isBefore(LocalDate.now())) {
                System.out.println("The seminar date is before today.");
                return false;
            }
        } catch (DateTimeParseException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
