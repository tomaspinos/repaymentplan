package cz.repaymentplan.logic.calendar

import org.junit.Test
import static cz.repaymentplan.logic.calendar.StaticWorkdayChecker.getDateParser
import static cz.repaymentplan.logic.enums.Country.CZE
import static cz.repaymentplan.logic.enums.Country.SVK
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue

/**
 *
 * @author Tomas Pinos
 */
class StaticWorkdayCheckerTest {

    @Test
    void isWorkday_CZE() {
        def checker = new StaticWorkdayChecker()

        assertFalse(checker.isWorkday(CZE, dateParser.parseDateTime("1.1.2000")))

        assertFalse(checker.isWorkday(CZE, dateParser.parseDateTime("24.12.2012")))
        assertFalse(checker.isWorkday(CZE, dateParser.parseDateTime("25.12.2012")))
        assertFalse(checker.isWorkday(CZE, dateParser.parseDateTime("26.12.2012")))

        assertTrue(checker.isWorkday(CZE, dateParser.parseDateTime("23.5.2012")))
    }

    @Test
    void isWorkday_SVK() {
        def checker = new StaticWorkdayChecker()

        assertFalse(checker.isWorkday(SVK, dateParser.parseDateTime("26.12.2000")))

        assertFalse(checker.isWorkday(CZE, dateParser.parseDateTime("24.12.2012")))
        assertFalse(checker.isWorkday(CZE, dateParser.parseDateTime("25.12.2012")))
        assertFalse(checker.isWorkday(CZE, dateParser.parseDateTime("26.12.2012")))

        assertTrue(checker.isWorkday(SVK, dateParser.parseDateTime("23.5.2012")))
    }
}
