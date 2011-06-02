package cz.repaymentplan.logic;

import java.math.BigDecimal;
import java.util.Date;

import org.joda.time.DateTime;

/**
* @author Tomas Pinos
*/
public class Payment {

    public DateTime interestFrom;
    public DateTime interestTo;
    public DateTime dueDate;
    public DateTime maturityDate;
    public BigDecimal payment;
    public BigDecimal interest;
    public BigDecimal principal;
    public BigDecimal outstanding;

    public Payment(DateTime interest_from, DateTime interest_to, DateTime due_date, DateTime maturity_date, BigDecimal payment, BigDecimal interest, BigDecimal principal, BigDecimal outstanding) {
        this.interestFrom = interest_from;
        this.interestTo = interest_to;
        this.dueDate = due_date;
        this.maturityDate = maturity_date;
        this.payment = payment;
        this.interest = interest;
        this.principal = principal;
        this.outstanding = outstanding;
    }

    public DateTime getDueDate() {
        return dueDate;
    }

    public Date getDueDateAsDate() {
        return dueDate.toDate();
    }

    public BigDecimal getInterest() {
        return interest;
    }

    public DateTime getInterestFrom() {
        return interestFrom;
    }

    public Date getInterestFromAsDate() {
        return interestFrom.toDate();
    }

    public DateTime getInterestTo() {
        return interestTo;
    }

    public Date getInterestToAsDate() {
        return interestTo.toDate();
    }

    public DateTime getMaturityDate() {
        return maturityDate;
    }

    public Date getMaturityDateAsDate() {
        return maturityDate.toDate();
    }

    public BigDecimal getOutstanding() {
        return outstanding;
    }

    public BigDecimal getPayment() {
        return payment;
    }

    public BigDecimal getPrincipal() {
        return principal;
    }

    @Override
    public String toString() {
        return "Payment{" +
                "interest_from=" + interestFrom +
                ", interest_to=" + interestTo +
                ", due_date=" + dueDate +
                ", maturity_date=" + maturityDate +
                ", payment=" + payment +
                ", interest=" + interest +
                ", principal=" + principal +
                ", outstanding=" + outstanding +
                '}';
    }
}
