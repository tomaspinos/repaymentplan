package cz.repaymentplan.logic;

import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests

/**
 * @author Tomas Pinos
 */
@ContextConfiguration(["classpath:repaymentplan-logic.xml"])
abstract class AbstractContextTest extends AbstractJUnit4SpringContextTests {
}
