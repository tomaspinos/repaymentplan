package cz.repaymentplan.logic;

import java.math.BigDecimal;

/**
 * @author Tomas Pinos
 */
public class BigDecimalUtils {

    static int SCALE = 10;

    public static int ceil(BigDecimal x) {
        return x.setScale(0, BigDecimal.ROUND_UP).intValue();
    }

    public static BigDecimal divide(int a, BigDecimal b) {
        return divide(new BigDecimal(a), b);
    }

    public static BigDecimal divide(int a, int b) {
        return divide(new BigDecimal(a), new BigDecimal(b));
    }

    public static BigDecimal divide(BigDecimal a, BigDecimal b) {
        return a.divide(b, SCALE, BigDecimal.ROUND_HALF_UP);
    }

    public static BigDecimal multiply(BigDecimal... a) {
        BigDecimal result = BigDecimal.ONE;
        for (BigDecimal x : a) {
            result = result.multiply(x);
        }
        return result;
    }

    public static BigDecimal power(BigDecimal x, int y) {
        return power(x, new BigDecimal(y));
    }

    public static BigDecimal power(BigDecimal x, BigDecimal y) {
        return new BigDecimal(Math.pow(x.doubleValue(), y.doubleValue()));
    }

    public static BigDecimal trunc(BigDecimal x, int scale) {
        return x.setScale(scale, BigDecimal.ROUND_DOWN);
    }
}
