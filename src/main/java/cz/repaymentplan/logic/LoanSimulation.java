package cz.repaymentplan.logic;

import java.math.BigDecimal;

/**
 * @author Tomas Pinos
 */
public class LoanSimulation {

    private PaymentList paymentList;
    private BigDecimal rpsn;

    public LoanSimulation(PaymentList paymentList, BigDecimal rpsn) {
        this.paymentList = paymentList;
        this.rpsn = rpsn;
    }

    public PaymentList getPaymentList() {
        return paymentList;
    }

    public BigDecimal getRpsn() {
        return rpsn;
    }
}
