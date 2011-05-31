package cz.repaymentplan.logic;

import java.math.BigDecimal;

/**
* @author Tomas Pinos
*/
interface PaymentExpression {

    BigDecimal evaluate(Payment payment);

}
