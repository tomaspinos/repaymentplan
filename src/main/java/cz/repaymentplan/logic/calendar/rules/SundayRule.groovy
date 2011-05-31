/**
 * 
 */
package cz.repaymentplan.logic.calendar.rules;

import org.joda.time.DateTimeConstants

/**
 * @author Tomas Pinos
 *
 */
class SundayRule extends DayOfWeekRule {

	SundayRule() {
		super(DateTimeConstants.SUNDAY)
	}
}
