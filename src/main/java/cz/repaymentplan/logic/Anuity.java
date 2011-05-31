package cz.repaymentplan.logic;

import java.math.BigDecimal;

/**
* @author Tomas Pinos
*/
class Anuity {

    public BigDecimal regular_anuity;
    public BigDecimal last_anuity;

    public void copy(Anuity other) {
        this.regular_anuity = other.regular_anuity;
        this.last_anuity = other.last_anuity;
    }
}
