package cz.repaymentplan.logic.calendar

import org.junit.Test
import cz.repaymentplan.logic.enums.Country
import cz.repaymentplan.logic.calendar.rules.SaturdayRule
import cz.repaymentplan.logic.calendar.rules.SundayRule
import cz.repaymentplan.logic.calendar.rules.EasterMondayRule
import cz.repaymentplan.logic.calendar.rules.GoodFridayRule
import org.junit.Assert

/**
 *
 * @author Tomas Pinos
 */
class DynamicWorkdayCheckerTest {

    @Test
    void isWorkday() {
        def checker = new DynamicWorkdayChecker(ruleMap: [
                (Country.CZE): [new SaturdayRule(), new SundayRule(), new EasterMondayRule()],
                (Country.SVK): [new SaturdayRule(), new SundayRule(), new EasterMondayRule(), new GoodFridayRule()]
        ])

        Assert.assertFalse(checker.isWorkday(Country.CZE, StaticWorkdayChecker.dateParser.parseDateTime("25.4.2011")))
        Assert.assertFalse(checker.isWorkday(Country.CZE, StaticWorkdayChecker.dateParser.parseDateTime("22.5.2011")))
        Assert.assertTrue(checker.isWorkday(Country.CZE, StaticWorkdayChecker.dateParser.parseDateTime("23.5.2011")))

        Assert.assertFalse(checker.isWorkday(Country.SVK, StaticWorkdayChecker.dateParser.parseDateTime("22.4.2011")))
        Assert.assertFalse(checker.isWorkday(Country.SVK, StaticWorkdayChecker.dateParser.parseDateTime("25.4.2011")))
        Assert.assertFalse(checker.isWorkday(Country.SVK, StaticWorkdayChecker.dateParser.parseDateTime("22.5.2011")))
        Assert.assertTrue(checker.isWorkday(Country.SVK, StaticWorkdayChecker.dateParser.parseDateTime("23.5.2011")))
    }
}
