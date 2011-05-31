/**
 * 
 */
package cz.repaymentplan.logic.calendar.rules

import org.joda.time.DateTimeConstants

/**
 * @author Tomas Pinos
 *
 */
class SaturdayRule extends DayOfWeekRule {

	SaturdayRule() {
		super(DateTimeConstants.SATURDAY)
	}
}
