package cz.repaymentplan.logic.calendar;

import org.joda.time.DateTime;

/**
 * A rule that either does match or doesn't match a date.
 * 
 * @author Tomas Pinos
 *
 */
public interface DynamicCalendarDayRule {

	/**
	 * Returns true, if the rule matches the given date.
	 * 
	 * @param date
	 * @return
	 */
	boolean match(DateTime date);
	
}
