/**
 * 
 */
package cz.repaymentplan.logic.calendar.rules;


import cz.repaymentplan.logic.calendar.CalendarUtils
import cz.repaymentplan.logic.calendar.DynamicCalendarDayRule
import org.joda.time.DateTime

/**
 * @author Tomas Pinos
 *
 */
class GoodFridayRule implements DynamicCalendarDayRule {

	boolean match(DateTime date) {
		date.toDateMidnight() == CalendarUtils.getGoodFriday(date.year().get())
	}
}
