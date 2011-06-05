package cz.repaymentplan.logic;


import cz.repaymentplan.logic.enums.Country
import org.joda.time.DateTime
import org.junit.Assert
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired

/**
 * @author Tomas Pinos
 */
class LoanSimulationAlgorithmTest extends AbstractContextTest {

    @Autowired
    private LoanSimulationAlgorithm loanSimulationAlgorithm;

    @Test
    void getDueDate() throws Exception {
        Assert.assertEquals(dt("17.06.2011"), loanSimulationAlgorithm.getDueDate(dt("17.5.2011"), 17, 1));
    }

    @Test
    void getMaturityDate_17_06_2011() throws Exception {
        Assert.assertEquals(dt("17.06.2011"), loanSimulationAlgorithm.getMaturityDate(dt("17.06.2011"), Country.CZE));
    }

    @Test
    void getMaturityDate_24_12_2011() throws Exception {
        Assert.assertEquals(dt("23.12.2011"), loanSimulationAlgorithm.getMaturityDate(dt("24.12.2011"), Country.CZE));
    }

    @Test
    void getMaturityDate_24_12_2012() throws Exception {
        Assert.assertEquals(dt("21.12.2012"), loanSimulationAlgorithm.getMaturityDate(dt("24.12.2012"), Country.CZE));
    }

    private static DateTime dt(String text) {
        new DateTime(Date.parse("dd.MM.yyyy", text))
    }
}
