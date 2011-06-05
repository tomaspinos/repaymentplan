package cz.repaymentplan.logic.calendar

import cz.repaymentplan.logic.AbstractContextTest
import org.springframework.beans.factory.annotation.Autowired
import org.junit.Test
import cz.repaymentplan.logic.enums.Country
import org.joda.time.DateTime
import org.junit.Assert
import static org.junit.Assert.*
import static cz.repaymentplan.logic.enums.Country.*

/**
 *
 * @author Tomas Pinos
 */
class CompositeWorkdayCheckerTest extends AbstractContextTest {

    @Autowired
    CompositeWorkdayChecker checker

    @Test
    void test_CZE() {
        assertFalse(checker.isWorkday(CZE, date("24.12.2011")))
        assertFalse(checker.isWorkday(CZE, date("25.12.2011")))
        assertFalse(checker.isWorkday(CZE, date("26.12.2011")))

        assertFalse(checker.isWorkday(CZE, date("24.12.2012")))
        assertFalse(checker.isWorkday(CZE, date("25.12.2012")))
        assertFalse(checker.isWorkday(CZE, date("26.12.2012")))
    }

    @Test
    void test_SVK() {
        assertFalse(checker.isWorkday(SVK, date("24.12.2011")))
        assertFalse(checker.isWorkday(SVK, date("25.12.2011")))
        assertFalse(checker.isWorkday(SVK, date("26.12.2011")))

        assertFalse(checker.isWorkday(SVK, date("24.12.2012")))
        assertFalse(checker.isWorkday(SVK, date("25.12.2012")))
        assertFalse(checker.isWorkday(SVK, date("26.12.2012")))
    }

    static DateTime date(String text) {
        new DateTime(Date.parse("dd.MM.yyyy", text))
    }
}
