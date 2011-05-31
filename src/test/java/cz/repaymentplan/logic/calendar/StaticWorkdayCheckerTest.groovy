package cz.repaymentplan.logic.calendar

import cz.repaymentplan.logic.enums.Country
import org.junit.Assert
import org.junit.Test

/**
 *
 * @author Tomas Pinos
 */
class StaticWorkdayCheckerTest {

    @Test
    void isWorkday() {
        def checker = new StaticWorkdayChecker()

        Assert.assertFalse(checker.isWorkday(Country.CZE, StaticWorkdayChecker.dateParser.parseDateTime("1.1.2000")))
        Assert.assertTrue(checker.isWorkday(Country.CZE, StaticWorkdayChecker.dateParser.parseDateTime("23.5.2012")))

        Assert.assertFalse(checker.isWorkday(Country.SVK, StaticWorkdayChecker.dateParser.parseDateTime("26.12.2000")))
        Assert.assertTrue(checker.isWorkday(Country.SVK, StaticWorkdayChecker.dateParser.parseDateTime("23.5.2012")))
    }
}
