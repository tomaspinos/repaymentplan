package cz.repaymentplan.logic;

import java.math.BigDecimal;
import java.util.LinkedList;

/**
* @author Tomas Pinos
*/
public class PaymentList extends LinkedList<Payment> {

    public BigDecimal getSumPrincipal() {
        return getSum(new PaymentExpression() {
            @Override
            public BigDecimal evaluate(Payment payment) {
                return payment.principal;
            }
        });
    }

    public BigDecimal getSum(PaymentExpression exp) {
        BigDecimal sum = BigDecimal.ZERO;
        for (Payment payment : this) {
            sum = sum.add(exp.evaluate(payment));
        }
        return sum;
    }
}
