package cz.repaymentplan.logic.calendar;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.joda.time.DateMidnight;

/**
 * @author Tomas Pinos
 */
public class CalendarUtils {

    /**
     * Compute the day of the year that Easter falls on. Step names E1 E2 etc.,
     * are direct references to Knuth, The Art of Programming Vol 1, page 155.
     *
     * @param year input year
     * @return the easter monday
     *
     * @exception IllegalArgumentException
     *                If the year is before 1582 (since the algorithm only works
     *                on the Gregorian calendar).
     */
    public static DateMidnight getEasterMonday(int year) {
        if (year <= 1582) {
            throw new IllegalArgumentException(
                    "Algorithm invalid before April 1583");
        }
        int golden, century, x, z, d, epact, n;

        golden = (year % 19) + 1; // E1: metonic cycle
        century = (year / 100) + 1; // E2: e.g. 1984 was in 20th C
        x = (3 * century / 4) - 12; // E3: leap year correction
        z = ((8 * century + 5) / 25) - 5; // E3: sync with moon's orbit
        d = (5 * year / 4) - x - 10;
        epact = (11 * golden + 20 + z - x) % 30; // E5: epact
        if ((epact == 25 && golden > 11) || epact == 24) {
            epact++;
        }
        n = 44 - epact;
        n += 30 * (n < 21 ? 1 : 0);
        n += 7 - ((d + n) % 7);

        Calendar cal;
        if (n > 31) {
            cal = new GregorianCalendar(year, 4 - 1, n - 31); // April
        } else {
            cal = new GregorianCalendar(year, 3 - 1, n); // March
        }
        cal.add(Calendar.DATE, 1);

        return new DateMidnight(cal.getTime());
    }

    /**
     *
     * @param year
     * @return
     */
    public static DateMidnight getGoodFriday(int year) {
        return getEasterMonday(year).minusDays(3);
    }
}
