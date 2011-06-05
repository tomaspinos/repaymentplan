package cz.repaymentplan.logic

import org.joda.time.DateTime
import org.junit.Test
import static org.junit.Assert.assertEquals
import cz.repaymentplan.logic.enums.Country
import cz.repaymentplan.logic.enums.PaymentPeriod
import cz.repaymentplan.logic.enums.InterestCorrectionType
import cz.repaymentplan.logic.enums.LastPaymentType

import org.springframework.beans.factory.annotation.Autowired

/**
 *
 * @author Tomas Pinos
 */
class PlanTest extends AbstractContextTest {

    @Autowired
    private LoanSimulationAlgorithm loanSimulationAlgorithm;

    @Test
    void plan1() {
        def loan = loanSimulationAlgorithm.simulateSimpleLoan(date("16.05.2011"), 17, Country.CZE, new BigDecimal(500000), BigDecimal.TEN, 12,
                PaymentPeriod.ONE_MONTH, 365, 360, BigDecimal.ZERO, InterestCorrectionType.CEIL, LastPaymentType.CALCULATED)

        assertSimulation("/plan1.xml", loan)
    }

    @Test
    void plan2() {
        def loan = loanSimulationAlgorithm.simulateSimpleLoan(date("23.05.2011"), 24, Country.CZE, new BigDecimal(500000), BigDecimal.TEN, 12,
                PaymentPeriod.ONE_MONTH, 365, 360, BigDecimal.ZERO, InterestCorrectionType.CEIL, LastPaymentType.LEAST_DIFFERENCE)

        assertSimulation("/plan2.xml", loan)
    }

    @Test
    void plan_9500000_24() {
        def loan = loanSimulationAlgorithm.simulateSimpleLoan(date("6.6.2011"), 24, Country.CZE, new BigDecimal(9500000), new BigDecimal(13), 120,
                PaymentPeriod.ONE_MONTH, 365, 360, new BigDecimal(500), InterestCorrectionType.CEIL, LastPaymentType.CALCULATED)

        assertSimulation("/plan_9500000_24.xml", loan)
    }

    def assertSimulation(String resource, LoanSimulation loan) {
        def plan = new XmlSlurper().parseText(new File(PlanTest.getResource(resource).toURI()).text)
        def expectedPayments = readPayments(plan.payments)

        assertPayments(expectedPayments, loan.payments)
        assertEquals("rpsn", readRPSN(plan.rpsn), loan.rpsn)
    }

    def assertPayments(PaymentList expected, PaymentList actual) {
        assertEquals(expected.size(), actual.size())
        def expectedPayments = expected.toArray()
        def actualPayments = actual.toArray()
        for (int i = 0; i < expected.size(); i++) {
            assertPayment(expectedPayments[i], actualPayments[i])
        }
    }

    def assertPayment(Payment expected, Payment actual) {
        assertEquals("interest from", expected.interestFrom, actual.interestFrom)
        assertEquals("interest to", expected.interestTo, actual.interestTo)
        assertEquals(expected.dueDate, actual.dueDate)
        assertEquals(expected.maturityDate, actual.maturityDate)
        assertEquals("payment", expected.payment.doubleValue(), actual.payment.doubleValue(), 0d)
        assertEquals("interest", expected.interest.doubleValue(), actual.interest.doubleValue(), 0d)
        assertEquals("principal", expected.principal.doubleValue(), actual.principal.doubleValue(), 0d)
        assertEquals("outstanding", expected.outstanding.doubleValue(), actual.outstanding.doubleValue(), 0d)
    }

    PaymentList readPayments(paymentsXml) {
        def paymentList = new PaymentList()
        for (int i = 0; i < paymentsXml.tr.size(); i++) {
            paymentList.add(readPayment(paymentsXml.tr[i]))
        }
        paymentList
    }

    Payment readPayment(paymentXml) {
        new Payment(date(paymentXml.td[1]), date(paymentXml.td[2]), date(paymentXml.td[3]), date(paymentXml.td[4]),
                number(paymentXml.td[5]), number(paymentXml.td[6]), number(paymentXml.td[7]), number(paymentXml.td[8]))
    }

    private static BigDecimal readRPSN(node) {
        number(node)
    }

    private static DateTime date(node) {
        date(node.text())
    }

    private static DateTime date(String text) {
        new DateTime(Date.parse("dd.MM.yyyy", text))
    }

    private static BigDecimal number(node) {
        number(node.text())
    }

    private static BigDecimal number(String text) {
        new BigDecimal(text.replace(" ", "").replace(",", "."))
    }
}
