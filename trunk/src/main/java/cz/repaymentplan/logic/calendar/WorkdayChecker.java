package cz.repaymentplan.logic.calendar;

import org.joda.time.DateTime;

import cz.repaymentplan.logic.enums.Country;

/**
 * @author Tomas Pinos
 */
public interface WorkdayChecker {

    /**
     *
     * @param country
     * @param date
     * @return
     */
    boolean isWorkday(Country country, DateTime date);
}
