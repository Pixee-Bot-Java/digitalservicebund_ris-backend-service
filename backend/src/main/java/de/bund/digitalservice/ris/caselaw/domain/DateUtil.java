package de.bund.digitalservice.ris.caselaw.domain;

import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DateUtil {

  public static Year getCurrentYear() {
    return Year.of(Calendar.getInstance().get(Calendar.YEAR));
  }

  public static String getYear(Year year, int digits) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("y".repeat(digits));
    String yearString = year.format(formatter);
    // allow 3-digit year (YYY)
    return yearString.substring(yearString.length() - digits);
  }

  public static String getYear(int digits) {
    return getYear(getCurrentYear(), digits);
  }
}
