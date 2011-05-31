package cz.repaymentplan.logic;

import java.math.BigDecimal;

/**
* @author Tomas Pinos
*/
interface DrawdownExpression {

    BigDecimal evaluate(Drawdown drawdown);

}
