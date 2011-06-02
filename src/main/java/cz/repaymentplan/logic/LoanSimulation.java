package cz.repaymentplan.logic;

import java.math.BigDecimal;

/**
 * @author Tomas Pinos
 */
public class LoanSimulation {

    private PaymentList payments;
    private BigDecimal rpsn;

    public LoanSimulation(PaymentList payments, BigDecimal rpsn) {
        this.payments = payments;
        this.rpsn = rpsn;
    }

    public PaymentList getPayments() {
        return payments;
    }

    public BigDecimal getRpsn() {
        return rpsn;
    }
}
