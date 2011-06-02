package cz.repaymentplan.logic;

import java.math.BigDecimal;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.Days;

import cz.repaymentplan.logic.calendar.WorkdayChecker;
import cz.repaymentplan.logic.enums.Country;

/**
 * @author Tomas Pinos
 */
public class CalculationUtils {

    public static DateTime addMonths(DateTime date, int months) {
        return date.plusMonths(months);
    }

    public static DateTime addDays(DateTime date, int days) {
        return date.plusDays(days);
    }

    public static DateTime lastDay(DateTime date) {
        return date.dayOfMonth().withMaximumValue();
    }

    public static int toDayPlusOne(DateTime date) {
        return date.plusDays(1).dayOfMonth().get();
    }

    public static DateTime truncMM(DateTime date) {
        return new DateTime(date.toDateMidnight().dayOfMonth().withMinimumValue());
    }

    public static DateTime truncDD(DateTime date) {
        return new DateTime(date.toDateMidnight());
    }

    public static DateTime getNearestWorkingDate(DateTime date, Country country, WorkdayChecker workdayChecker) {
        while (!workdayChecker.isWorkday(country, date)) {
            date = date.plusDays(1);
        }
        return date;
    }

    public static boolean isGreater(BigDecimal a, BigDecimal b) {
        return a.doubleValue() > b.doubleValue();
    }

    public static boolean isGreaterThanZero(BigDecimal a) {
        return isGreater(a, BigDecimal.ZERO);
    }

    public static boolean isLess(BigDecimal a, BigDecimal b) {
        return a.doubleValue() < b.doubleValue();
    }

    public static boolean isLessOrEqualZero(BigDecimal a) {
        return a.doubleValue() <= 0d;
    }

    public static boolean isEqual(BigDecimal a, BigDecimal b) {
        return !(a == null || b == null) && a.doubleValue() == b.doubleValue();
    }

    public static int getDayCountInYear(DateTime date) {
        DateMidnight firstDay = new DateMidnight(date).monthOfYear().withMinimumValue().dayOfMonth().withMinimumValue();
        DateMidnight lastDay = new DateMidnight(date).monthOfYear().withMaximumValue().dayOfMonth().withMaximumValue();
        return Days.daysBetween(firstDay, lastDay).getDays() + 1;
    }

    public static int getDiffInDays(DateTime d1, DateTime d2) {
        return Math.abs(Days.daysBetween(d1, d2).getDays());
    }
}
