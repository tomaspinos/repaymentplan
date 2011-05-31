package cz.repaymentplan.logic;

import java.text.SimpleDateFormat;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

import cz.repaymentplan.logic.enums.Country;

/**
 * @author Tomas Pinos
 */
public class TestLoanSimulationAlgorithm {

    @Test
    public void getDueDate() throws Exception {
        LoanSimulationAlgorithm loan = new LoanSimulationAlgorithm();

        Assert.assertEquals(dt("17.06.2011"), loan.getDueDate(dt("17.5.2011"), 17, 1));
    }

    @Test
    public void getMaturityDate() throws Exception {
        LoanSimulationAlgorithm loan = new LoanSimulationAlgorithm();

        Assert.assertEquals(dt("17.06.2011"), loan.getMaturityDate(dt("17.06.2011"), Country.CZE));
    }

    private static DateTime dt(String s) throws Exception {
        return new DateTime(new SimpleDateFormat("dd.MM.yyyy").parse(s).getTime());
    }
}
