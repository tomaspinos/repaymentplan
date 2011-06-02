package cz.repaymentplan.web;

import java.math.BigDecimal;
import java.util.Date;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import cz.repaymentplan.logic.LoanSimulation;
import cz.repaymentplan.logic.enums.Country;
import cz.repaymentplan.logic.enums.LastPaymentType;

/**
 * @author Tomas Pinos
 */
public class PlanForm {

    private BigDecimal outstanding = new BigDecimal(500000);
    private Date dropdownDate;
    private BigDecimal interestRate = BigDecimal.TEN;
    private Integer dueDay;
    private Integer payments = 12;
    private String country = Country.CZE.getCode();
    private BigDecimal fee = BigDecimal.ZERO;
    private String lastPaymentType = LastPaymentType.CALCULATED.getCode();

    private LoanSimulation loanSimulation;

    @NotNull()
    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @NotNull()
    public Date getDropdownDate() {
        return dropdownDate;
    }

    public void setDropdownDate(Date dropdownDate) {
        this.dropdownDate = dropdownDate;
    }

    @NotNull()
    public Integer getDueDay() {
        return dueDay;
    }

    public void setDueDay(Integer dueDay) {
        this.dueDay = dueDay;
    }

    @NotNull()
    @Min(0)
    @Max(1000)
    public BigDecimal getFee() {
        return fee;
    }

    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }

    @NotNull()
    @Min(0)
    @Max(30)
    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(BigDecimal interestRate) {
        this.interestRate = interestRate;
    }

    @NotNull()
    public String getLastPaymentType() {
        return lastPaymentType;
    }

    public void setLastPaymentType(String lastPaymentType) {
        this.lastPaymentType = lastPaymentType;
    }

    @NotNull()
    @Min(10000)
    @Max(10000000)
    public BigDecimal getOutstanding() {
        return outstanding;
    }

    public void setOutstanding(BigDecimal outstanding) {
        this.outstanding = outstanding;
    }

    @NotNull()
    @Min(1)
    @Max(120)
    public Integer getPayments() {
        return payments;
    }

    public void setPayments(Integer payments) {
        this.payments = payments;
    }

    public LoanSimulation getLoanSimulation() {
        return loanSimulation;
    }

    public void setLoanSimulation(LoanSimulation loanSimulation) {
        this.loanSimulation = loanSimulation;
    }
}
