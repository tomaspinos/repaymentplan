package cz.repaymentplan.logic;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
* @author Tomas Pinos
*/
class DrawdownList {

    private List<Drawdown> list = new ArrayList<Drawdown>();

    BigDecimal getSumAmount() {
        return getSum(new DrawdownExpression() {
            @Override
            public BigDecimal evaluate(Drawdown drawdown) {
                return drawdown.amount;
            }
        });
    }

    BigDecimal getSum(DrawdownExpression exp) {
        BigDecimal sum = BigDecimal.ZERO;
        for (Drawdown drawdown : list) {
            sum = sum.add(exp.evaluate(drawdown));
        }
        return sum;
    }

    public void add(Drawdown drawdown) {
        list.add(drawdown);
    }
}
