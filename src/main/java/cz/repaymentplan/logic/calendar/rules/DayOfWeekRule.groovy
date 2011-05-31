package cz.repaymentplan.logic.calendar.rules;

import cz.repaymentplan.logic.calendar.DynamicCalendarDayRule
import org.joda.time.DateTime

/**
 * 
 * @author Tomas Pinos
 *
 */
class DayOfWeekRule implements DynamicCalendarDayRule {

	private int dayOfWeek;

	DayOfWeekRule(int dayOfWeek) {
		this.dayOfWeek = dayOfWeek
	}

	boolean match(DateTime date) {
		dayOfWeek == date.dayOfWeek().get()
	}
}
