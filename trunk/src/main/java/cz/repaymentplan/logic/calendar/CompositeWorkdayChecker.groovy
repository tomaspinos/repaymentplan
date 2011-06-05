package cz.repaymentplan.logic.calendar

import cz.repaymentplan.logic.enums.Country
import org.joda.time.DateTime

/**
 *
 * @author Tomas Pinos
 */
class CompositeWorkdayChecker implements WorkdayChecker {

    List<WorkdayChecker> checkers
    Map<CountryDateCacheKey, Boolean> resultCache = [:]

    @Override
    boolean isWorkday(Country country, DateTime date) {
        def cacheKey = new CountryDateCacheKey(country: country, date: date)

        def result = resultCache[cacheKey]
        if (!result) {
            result = true

            checkers.each { checker -> result &= checker.isWorkday(country, date) }

            resultCache[cacheKey] = result

            result
        }

        result
    }
}
