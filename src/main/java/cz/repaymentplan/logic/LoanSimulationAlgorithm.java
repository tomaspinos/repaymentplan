package cz.repaymentplan.logic;

import java.math.BigDecimal;

import org.joda.time.DateTime;

import cz.repaymentplan.logic.calendar.WorkdayChecker;
import cz.repaymentplan.logic.enums.Country;
import cz.repaymentplan.logic.enums.InterestCorrectionType;
import cz.repaymentplan.logic.enums.LastPaymentType;
import cz.repaymentplan.logic.enums.PaymentPeriod;

import static cz.repaymentplan.logic.BigDecimalUtils.SCALE;
import static cz.repaymentplan.logic.BigDecimalUtils.ceil;
import static cz.repaymentplan.logic.BigDecimalUtils.divide;
import static cz.repaymentplan.logic.BigDecimalUtils.multiply;
import static cz.repaymentplan.logic.BigDecimalUtils.power;
import static cz.repaymentplan.logic.BigDecimalUtils.trunc;
import static cz.repaymentplan.logic.CalculationUtils.addDays;
import static cz.repaymentplan.logic.CalculationUtils.addMonths;
import static cz.repaymentplan.logic.CalculationUtils.getDayCountInYear;
import static cz.repaymentplan.logic.CalculationUtils.getDiffInDays;
import static cz.repaymentplan.logic.CalculationUtils.getNearestWorkingDate;
import static cz.repaymentplan.logic.CalculationUtils.isEqual;
import static cz.repaymentplan.logic.CalculationUtils.isGreater;
import static cz.repaymentplan.logic.CalculationUtils.isGreaterThanZero;
import static cz.repaymentplan.logic.CalculationUtils.isLess;
import static cz.repaymentplan.logic.CalculationUtils.isLessOrEqualZero;
import static cz.repaymentplan.logic.CalculationUtils.lastDay;
import static cz.repaymentplan.logic.CalculationUtils.toDayPlusOne;
import static cz.repaymentplan.logic.CalculationUtils.truncDD;
import static cz.repaymentplan.logic.CalculationUtils.truncMM;

/**
 * Code mostly reverse-engineered from the original PL/SQL implementation.
 * No special care was taken to increase the code quality.
 *
 * @author Tomas Pinos
 */
public class LoanSimulationAlgorithm {

    private WorkdayChecker workdayChecker;

    public void setWorkdayChecker(WorkdayChecker workdayChecker) {
        this.workdayChecker = workdayChecker;
    }

    //
    // vrati DueDate
    public DateTime getDueDate(DateTime id_date,    // rerefencny datum v mesiaci, hociktory den mesiaca
                               Integer in_due_day,  // den splacania
                               Integer in_months) {
        DateTime ld_due_date;
        if (in_due_day == 31) { // posledny den v mesiaci
            return lastDay(addMonths(id_date, in_months));
        } else {
            ld_due_date = addDays(truncMM(addMonths(id_date, in_months)), in_due_day - 1);
            if (truncMM(ld_due_date).equals(addMonths(truncMM(id_date), in_months))) {
                return ld_due_date;
            } else {
                return lastDay(addMonths(id_date, in_months));
            }
        }
    }

    //
    // pre zadany DueDate urci datum splatnosti. plati pravidlo, ze datum splatnosti je due date, ak je nepracovny, tak je datum splatnosti najblizsi predosly pracovny
    // den, okrem pripadu, kedy by predchadzajuci pracovny den bol v predchadzajucom mesiaci. v tom pripade je to najbliszi nasledujuci pracovny den
    public DateTime getMaturityDate(DateTime id_due_date,
                                    Country country) {
        DateTime ld_work_date;
        DateTime ld_maturity_date = truncDD(id_due_date);
        while (true) {
            ld_work_date = getNearestWorkingDate(ld_maturity_date, country, workdayChecker);
            if (ld_work_date.equals(ld_maturity_date)) {
                break;
            }
            ld_maturity_date = addDays(ld_maturity_date, -1);
        }
        if (truncMM(ld_maturity_date).equals(truncMM(id_due_date))) {
            return ld_maturity_date;
        } else {
            return getNearestWorkingDate(id_due_date, country, workdayChecker);
        }
    }

    //
    // podla periody splacania vrati pocet platieb za rok
    public static Integer getPaymentsPerAnum(PaymentPeriod paymentPeriod) {
        switch (paymentPeriod) {
            case ONE_MONTH:
                return 12;
            default:
                throw new IllegalStateException("Unknown payment period");
        }
    }

    //
    // vypocita vysku anuitnej splatky (iba podla vzorca)
    public static BigDecimal getAnuity(BigDecimal in_outstanding,   // vyska uveru
                                       BigDecimal in_interest_rate,   // urokova sadzba p.a.
                                       Integer in_num_of_payments,   // pocet splatok
                                       PaymentPeriod paymentPeriod,  // perioda splatok, default mesacne
                                       Integer in_days_in_year,  // pocet dni v roku, povolene hodnoty 365, 365.25, 366 (znamena skutocny pocet dni v roku)
                                       Integer in_interest_days,  // podla bazy urocenia, u anuit je to 30denna, tj. 12*30 = 360
                                       BigDecimal in_periodic_fee    // periodicky poplatok
    ) {
        BigDecimal lnR1;
        BigDecimal lnR2;
        switch (paymentPeriod) {
            case ONE_MONTH:
                lnR1 = multiply(divide(in_interest_rate, new BigDecimal(getPaymentsPerAnum(paymentPeriod) * 100)), divide(in_days_in_year, in_interest_days));
                lnR2 = (lnR1.add(BigDecimal.ONE)).pow(in_num_of_payments);
                return (divide(multiply(in_outstanding, lnR2, lnR1), lnR2.subtract(BigDecimal.ONE)).add(in_periodic_fee)).setScale(0, BigDecimal.ROUND_HALF_UP);
            default:
                throw new IllegalStateException("Unknown payment period");
        }
    }

    //
    // podla zadanych parametrov vrati splatkovy plan v XML
    public PaymentList getRepaymentPlan(DateTime id_anuity_start_date, // datum obdobia splacania anuity,
                                        Integer in_due_day, // den splacania
                                        Country country, // krajina, pre ktoru sa maju pocitat pracovne, nepracovne dni
                                        BigDecimal in_outstanding,   // vyska uveru
                                        BigDecimal in_interest_rate,   // urokova sadzba p.a.
                                        Integer in_num_of_payments,   // pocet splatok
                                        PaymentPeriod paymentPeriod,  // perioda splatok, default mesacne
                                        Integer in_days_in_year,  // pocet dni v roku, povolene hodnoty 365, 365.25, 366 (znamena skutocny pocet dni v roku)
                                        Integer in_interest_days,  // podla bazy urocenia, u anuit je to 30denna, tj. 12*30 = 360
                                        BigDecimal in_periodic_fee,    // periodicky poplatok
                                        InterestCorrectionType interestCorrectionType, // 'ROUND' - urok zaokruhlit, 'TRUNC' - urok orezat, 'CEIL' - zarovnat nahor,
                                        // 'NONE' - urok neupravovat - vyuzit celu sirku typu number v oracle
                                        LastPaymentType lastPaymentType) {
        // vyska riadnej anuity
        Anuity lr_calc_last = new Anuity();
        Anuity lr_calc_actual = new Anuity();
        Anuity lr_calc_new = new Anuity();
        BigDecimal ln_regular_anuity = getAnuity(in_outstanding, in_interest_rate, in_num_of_payments, paymentPeriod, in_days_in_year, in_interest_days, in_periodic_fee);
        BigDecimal outstanding;
        DateTime dueDate;
        DateTime interestFrom;
        BigDecimal ln_principal_sum;
        DateTime maturityDate;
        DateTime interestTo;
        BigDecimal interest;
        BigDecimal payment;
        BigDecimal principal;
        Boolean lb_exit = false;

        PaymentList paymentList = new PaymentList();

        for (int ln_step = 1; ln_step <= 1000; ln_step++) {
            ln_principal_sum = BigDecimal.ZERO;
            outstanding = in_outstanding;
            interestFrom = id_anuity_start_date;
            dueDate = getDueDate(id_anuity_start_date, in_due_day, 1);

            paymentList.clear();

            for (int i = 1; i <= in_num_of_payments; i++) {
                maturityDate = getMaturityDate(dueDate, country);
                interestTo = addDays(dueDate, -1);

                interest = divide(multiply(new BigDecimal(getDiffInDays(interestFrom, interestTo) + 1), outstanding, divide(in_interest_rate, new BigDecimal(100))),
                        new BigDecimal(in_interest_days));

                switch (interestCorrectionType) {
                    case ROUND:
                        interest = interest.setScale(2, BigDecimal.ROUND_HALF_UP);
                        break;
                    case TRUNC:
                        interest = trunc(interest, 2);
                        break;
                    case CEIL:
                        interest = divide(ceil(multiply(power(BigDecimal.TEN, 2), interest)), power(BigDecimal.TEN, 2));
                        break;
                }
                
                if (i < in_num_of_payments) {
                    payment = ln_regular_anuity;
                    principal = (payment.subtract(interest)).subtract(in_periodic_fee);
                    ln_principal_sum = ln_principal_sum.add(principal);
                } else {
                    principal = in_outstanding.subtract(ln_principal_sum);
                    payment = principal.add(interest).add(in_periodic_fee);
                }
                paymentList.add(new Payment(interestFrom, interestTo, dueDate, maturityDate, payment,
                        interest, principal, outstanding.subtract(principal)));
                interestFrom = dueDate;
                if (paymentPeriod == PaymentPeriod.ONE_MONTH) {
                    dueDate = getDueDate(dueDate, in_due_day, 1);
                }
                outstanding = (outstanding.subtract(ln_regular_anuity)).add(in_periodic_fee).add(interest);
            }

            switch (lastPaymentType) {
                case CALCULATED:
                    return paymentList;

                case LEAST_DIFFERENCE:
                    lr_calc_actual.regular_anuity = paymentList.getFirst().payment;
                    lr_calc_actual.last_anuity = paymentList.getLast().payment;

                    if (lb_exit) {
                        return paymentList;
                    }

                    if (isGreater(lr_calc_actual.last_anuity, lr_calc_actual.regular_anuity)) {
                        lr_calc_new.regular_anuity = lr_calc_actual.regular_anuity.add(BigDecimal.ONE);
                    } else {
                        lr_calc_new.regular_anuity = lr_calc_actual.regular_anuity.subtract(BigDecimal.ONE);
                    }

                    if (isEqual(lr_calc_last.regular_anuity, lr_calc_new.regular_anuity)) { // podla pravidla urcena nova anuita je taka ista ako predchadzajuca => koniec
                        if (isLess((lr_calc_last.regular_anuity.subtract(lr_calc_last.last_anuity)).abs(), (lr_calc_actual.regular_anuity.subtract(lr_calc_actual.last_anuity)).abs())) {
                            lr_calc_actual.copy(lr_calc_last);
                            lb_exit = true;
                        } else {
                            return paymentList;
                        }
                    } else {
                        lr_calc_last.copy(lr_calc_actual);
                        lr_calc_actual.copy(lr_calc_new);
                    }

                    ln_regular_anuity = lr_calc_actual.regular_anuity;

                    break;
            }
        }

        return paymentList;
    }

    //
    // vyhlda RPSN podla splatkoveho kalendara a planu cerpani metodou plenia intervalu, predpoklad : bude medzi 0 - 1000
    public static BigDecimal getRPSN(DrawdownList it_drawdown,
                                     PaymentList it_payment,
                                     final DateTime id_start_drawdown,
                                     Integer in_days_in_year  // pocet dni v roku, povolene hodnoty 365, 365.25, 366 (znamena skutocny pocet dni v roku)
                                     ) {
        // kontrola planov
        final RPSNCalculationIteration left = new RPSNCalculationIteration();
        final RPSNCalculationIteration right = new RPSNCalculationIteration();
        final RPSNCalculationIteration mid = new RPSNCalculationIteration();

        final Integer ln_days_in_year = in_days_in_year == 366 ? getDayCountInYear(id_start_drawdown) : in_days_in_year;

        mid.drawdownSum = it_drawdown.getSumAmount();
        mid.paymentSum = it_payment.getSumPrincipal();

        if (mid.drawdownSum == null || mid.paymentSum == null || mid.drawdownSum.doubleValue() != mid.paymentSum.doubleValue()) {
            return null;
        }
        // vypocet RPSN polenim intervalu
        left.rpsn = BigDecimal.ZERO;

        left.drawdownSum = it_drawdown.getSum(new DrawdownExpression() {
            @Override
            public BigDecimal evaluate(Drawdown drawdown) {
                return divide(drawdown.amount, power(BigDecimal.ONE.add(left.rpsn), divide(getDiffInDays(drawdown.drawdown_date, id_start_drawdown), ln_days_in_year)));
            }
        });
        left.paymentSum = it_payment.getSum(new PaymentExpression() {
            @Override
            public BigDecimal evaluate(Payment payment) {
                return divide(payment.payment, power(BigDecimal.ONE.add(left.rpsn), divide(getDiffInDays(payment.maturityDate, id_start_drawdown), ln_days_in_year)));
            }
        });

        right.rpsn = new BigDecimal(1000); // pocita sa s maximalnou vyskou RPSN. pre normalne sadzby (max desiatky percent) staci startovat od takejto max vysky RPSN

        right.drawdownSum = it_drawdown.getSum(new DrawdownExpression() {
            @Override
            public BigDecimal evaluate(Drawdown drawdown) {
                return divide(drawdown.amount, power(BigDecimal.ONE.add(right.rpsn), divide(getDiffInDays(drawdown.drawdown_date, id_start_drawdown), ln_days_in_year)));
            }
        });
        right.paymentSum = it_payment.getSum(new PaymentExpression() {
            @Override
            public BigDecimal evaluate(Payment payment) {
                return divide(payment.payment, power(BigDecimal.ONE.add(right.rpsn), divide(getDiffInDays(payment.maturityDate, id_start_drawdown), ln_days_in_year)));
            }
        });

        for (int step = 1; step <= 100; step++) {
            mid.rpsn = divide(left.rpsn.add(right.rpsn), new BigDecimal(2));

            if (isEqual(mid.rpsn.setScale(SCALE, BigDecimal.ROUND_HALF_UP), left.rpsn.setScale(SCALE, BigDecimal.ROUND_HALF_UP))
                    || isEqual(mid.rpsn.setScale(SCALE, BigDecimal.ROUND_HALF_UP), right.rpsn.setScale(SCALE, BigDecimal.ROUND_HALF_UP))) {
                break;
            }

            mid.drawdownSum = it_drawdown.getSum(new DrawdownExpression() {
                @Override
                public BigDecimal evaluate(Drawdown drawdown) {
                    return divide(drawdown.amount, power(BigDecimal.ONE.add(mid.rpsn), divide(getDiffInDays(drawdown.drawdown_date, id_start_drawdown), ln_days_in_year)));
                }
            });
            mid.paymentSum = it_payment.getSum(new PaymentExpression() {
                @Override
                public BigDecimal evaluate(Payment payment) {
                    return divide(payment.payment, power(BigDecimal.ONE.add(mid.rpsn), divide(getDiffInDays(payment.maturityDate, id_start_drawdown), ln_days_in_year)));
                }
            });

            if (isGreaterThanZero(mid.drawdownSum.subtract(mid.paymentSum)) && isGreaterThanZero(left.drawdownSum.subtract(left.paymentSum)) ||
                    isLessOrEqualZero(mid.drawdownSum.subtract(mid.paymentSum)) && isLessOrEqualZero(left.drawdownSum.subtract(left.paymentSum))) {
                left.copy(mid);
            } else if (isGreaterThanZero(mid.drawdownSum.subtract(mid.paymentSum)) && isGreaterThanZero(right.drawdownSum.subtract(right.paymentSum)) ||
                    isLessOrEqualZero(mid.drawdownSum.subtract(mid.paymentSum)) && isLessOrEqualZero(right.drawdownSum.subtract(right.paymentSum))) {
                right.copy(mid);
            } else {
                return null;
            }
        }

        return multiply(new BigDecimal(100), mid.rpsn).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    //
    // pre jednoduchy uver (cerpanie naraz, splacanie anuitne, mesacne) vygeneruje splatkovy plan a spocita RPSN
    public LoanSimulation simulateSimpleLoan(DateTime id_drawdown_date,     // datum cerpania, pre single uver sa pocita, ze v den cerpania sa vycerpa vsetko naraz
                                             Integer in_due_day,   // den splacania, ak nie je vyplneny, tak je to den nasledujuci po dni cerpania
                                             Country country, // krajina, pre ktoru sa maju pocitat pracovne, nepracovne dni
                                             BigDecimal in_outstanding,   // vyska uveru
                                             BigDecimal in_interest_rate,   // urokova sadzba p.a.
                                             Integer in_num_of_payments,   // pocet splatok
                                             PaymentPeriod paymentPeriod,  // perioda splatok, default mesacne
                                             Integer in_days_in_year,  // pocet dni v roku, povolene hodnoty 365, 365.25, 366 (znamena skutocny pocet dni v roku)
                                             Integer in_interest_days,  // podla bazy urocenia, u anuit je to 30denna, tj. 12*30 = 360
                                             BigDecimal in_periodic_fee,    // periodicky poplatok
                                             InterestCorrectionType interestCorrectionType, // 'ROUND' - urok zaokruhlit, 'TRUNC' - urok orezat, 'CEIL' - zarovnat nahor,
                                             // 'NONE' - urok neupravovat - vyuzit celu sirku typu number v oracle
                                             LastPaymentType lastPaymentType  // ako vypocitat anuitu : 'CALCULATED' - iba podla vzorca, poslednu splatku nijako neupravovat,
                                             // 'LEAST_DIFFERENCE' tak, aby bol rozdiel medzi riadnymi splatkami a poslednou minimalny,
                                             // 'LOWER_THAN_REGULAR_WITH_LEAST_DIFFERENCE' - tak aby posledna splatka bola vzdy nizsia ako
                                             // riadna anuita, ale aby rozdiel riadnej anuity a poslednej splatky bol minimalny
                                             ) {
        Integer ln_due_day = (in_due_day != null) ? in_due_day : toDayPlusOne(id_drawdown_date);
        PaymentList paymentList = getRepaymentPlan(addDays(id_drawdown_date, 1),
                ln_due_day, country, in_outstanding, in_interest_rate,
                in_num_of_payments, paymentPeriod,
                in_days_in_year, in_interest_days, in_periodic_fee,
                interestCorrectionType,
                lastPaymentType);
        DrawdownList drawdownList = new DrawdownList();
        drawdownList.add(new Drawdown(id_drawdown_date, in_outstanding));
        BigDecimal rpsn = getRPSN(drawdownList, paymentList, id_drawdown_date, in_days_in_year);

        return new LoanSimulation(paymentList, rpsn);
    }

    private static class RPSNCalculationIteration {

        public BigDecimal paymentSum;
        public BigDecimal drawdownSum;
        public BigDecimal rpsn;

        void copy(RPSNCalculationIteration iteration) {
            this.paymentSum = iteration.paymentSum;
            this.drawdownSum = iteration.drawdownSum;
            this.rpsn = iteration.rpsn;
        }

        @Override
        public String toString() {
            return "RPSNCalculationIteration{" +
                    "payment_sum=" + paymentSum +
                    ", drawdown_sum=" + drawdownSum +
                    ", rpsn=" + rpsn +
                    '}';
        }
    }
}