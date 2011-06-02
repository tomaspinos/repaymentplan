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
    void getMaturityDate() throws Exception {
        Assert.assertEquals(dt("17.06.2011"), loanSimulationAlgorithm.getMaturityDate(dt("17.06.2011"), Country.CZE));
    }

    private static DateTime dt(String text) {
        new DateTime(Date.parse("dd.MM.yyyy", text))
    }
}
