package cz.repaymentplan.logic;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
* @author Tomas Pinos
*/
public class PaymentList implements Iterable<Payment> {

    private List<Payment> list = new ArrayList<Payment>();

    public void clear() {
        list.clear();
    }

    public Payment getFirst() {
        return list.get(0);
    }

    public Payment getLast() {
        return list.get(list.size() - 1);
    }

    public List<Payment> getList() {
        return list;
    }

    public void add(Payment payment) {
        list.add(payment);
    }

    public int size() {
        return list.size();
    }

    @Override
    public Iterator<Payment> iterator() {
        return list.iterator();
    }

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
        for (Payment payment : list) {
            sum = sum.add(exp.evaluate(payment));
        }
        return sum;
    }

    @Override
    public String toString() {
        return list.toString();
    }
}
