package cz.repaymentplan.logic

import org.joda.time.DateTime
import org.junit.Test
import org.junit.Assert

/**
 *
 * @author Tomas Pinos
 */
class CalculationUtilsTest {

    @Test
    void getDiffInDays() {
        Assert.assertEquals(1, CalculationUtils.getDiffInDays(date("1.1.2010"), date("2.1.2010")))
        Assert.assertEquals(1, CalculationUtils.getDiffInDays(date("2.1.2010"), date("1.1.2010")))
    }

    @Test
    void getDayCountInYear() {
        Assert.assertEquals(365, CalculationUtils.getDayCountInYear(date("22.5.2011")))
        Assert.assertEquals(366, CalculationUtils.getDayCountInYear(date("22.5.2012")))
        Assert.assertEquals(365, CalculationUtils.getDayCountInYear(date("22.5.2013")))
    }

    DateTime date(String text) {
        new DateTime(Date.parse("dd.MM.yyyy", text))
    }
}
