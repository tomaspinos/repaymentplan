package cz.repaymentplan.logic

import cz.repaymentplan.logic.calendar.WorkdayChecker
import cz.repaymentplan.logic.enums.Country
import org.joda.time.DateTime
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import static cz.repaymentplan.logic.CalculationUtils.getDayCountInYear
import static cz.repaymentplan.logic.CalculationUtils.getDiffInDays
import static cz.repaymentplan.logic.CalculationUtils.getNearestWorkingDate
import static org.junit.Assert.assertEquals

/**
 *
 * @author Tomas Pinos
 */
class CalculationUtilsTest extends AbstractContextTest {

    @Autowired
    WorkdayChecker workdayChecker;

    @Test
    void getDiffInDays() {
        assertEquals(1, getDiffInDays(date("1.1.2010"), date("2.1.2010")))
        assertEquals(1, getDiffInDays(date("2.1.2010"), date("1.1.2010")))
    }

    @Test
    void getDayCountInYear() {
        assertEquals(365, getDayCountInYear(date("22.5.2011")))
        assertEquals(366, getDayCountInYear(date("22.5.2012")))
        assertEquals(365, getDayCountInYear(date("22.5.2013")))
    }

    @Test
    void getNearestWorkingDate_24_12_2012() {
        assertEquals(date("27.12.2012"), getNearestWorkingDate(date("24.12.2012"), Country.CZE, workdayChecker))
    }

    static DateTime date(String text) {
        new DateTime(Date.parse("dd.MM.yyyy", text))
    }
}
