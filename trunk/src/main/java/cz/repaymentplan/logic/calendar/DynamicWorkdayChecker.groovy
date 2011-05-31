package cz.repaymentplan.logic.calendar

import cz.repaymentplan.logic.enums.Country
import org.joda.time.DateTime

/**
 *
 * @author Tomas Pinos
 */
class DynamicWorkdayChecker implements WorkdayChecker {

    Map<Country, List<DynamicCalendarDayRule>> ruleMap

    @Override
    boolean isWorkday(Country country, DateTime date) {
        def rules = ruleMap[country]
        if (!rules) {
            return true
        }

        null == rules.find { rule -> rule.match(date) }
    }
}
