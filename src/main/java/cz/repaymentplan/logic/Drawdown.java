package cz.repaymentplan.logic;

import java.math.BigDecimal;
import java.sql.Timestamp;

import org.joda.time.DateTime;

/**
* @author Tomas Pinos
*/
class Drawdown {

    public DateTime drawdown_date;
    public BigDecimal amount;

    public Drawdown(DateTime drawdown_date, BigDecimal outstanding) {
        this.drawdown_date = drawdown_date;
        this.amount = outstanding;
    }

    @Override
    public String toString() {
        return "Drawdown{" +
                "drawdown_date=" + drawdown_date +
                ", amount=" + amount +
                '}';
    }
}
