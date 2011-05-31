package cz.repaymentplan.logic.calendar

import cz.repaymentplan.logic.enums.Country
import org.joda.time.DateTime
import org.joda.time.DateMidnight
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter

/**
 *
 * @author Tomas Pinos
 */
class StaticWorkdayChecker implements WorkdayChecker {

    static final int BASE_YEAR = 1970

    Map<Country, List<DateTime>> holidayMap

    static DateTimeFormatter dateParser = DateTimeFormat.forPattern("dd.MM.yyyy")

    StaticWorkdayChecker() {
        parseData()
    }

    @Override
    boolean isWorkday(Country country, DateTime date) {
        def dates = holidayMap[country]
        if (!dates) {
            return true
        }
        !containsDate(date, dates)
    }

    void parseData() {

        holidayMap = [:]

        def holidays = new XmlSlurper().parseText(new File(StaticWorkdayChecker.getResource("/holidays.xml").toURI()).text)
        holidays.country.each { countryDates ->
            def country = Country.valueOf(countryDates.@code.text())

            holidayMap[(country)] = []

            countryDates.date.each { ddMM ->
                def date = dateParser.parseDateTime(ddMM.text() + BASE_YEAR)
                holidayMap[(country)] << date
            }
        }
    }

    boolean containsDate(DateTime date, List<DateTime> dates) {
        def dateToCheck = new DateMidnight(date).withYear(BASE_YEAR)
        dates.contains(dateToCheck)
    }
}
