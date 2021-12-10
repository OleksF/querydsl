/*
 * Copyright 2015, The Querydsl Team (http://www.querydsl.com/team)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.querydsl.collections;

import com.querydsl.core.types.Ops;
import org.joda.time.LocalDate;

/**
 * Custom templates which support the Joda Time API instead of the JDK Date API
 *
 * @author tiwe
 * @author oleksf
 *
 */
public class JodaTimeTemplates extends CollQueryTemplates {

    @SuppressWarnings("FieldNameHidesFieldInSuperclass") //Intentional
    public static final JodaTimeTemplates DEFAULT = new JodaTimeTemplates();

    /**
     * Returns mapped commands corresponding to Jode Time alternatives to base
     * Java time classes (except YEAR_WEEK_MYSQL; see yearWeek_mysql_templ())
     * 
     * @return nothing, populates Ops.DateTimeOps class members via side-effects
     */
    protected JodaTimeTemplates() {
        add(Ops.DateTimeOps.YEAR,         "{0}.getYear()");
        add(Ops.DateTimeOps.MONTH,        "{0}.getMonthOfYear()");
        add(Ops.DateTimeOps.WEEK,         "{0}.getWeekOfWeekyear()");
        add(Ops.DateTimeOps.DAY_OF_WEEK,  "{0}.getDayOfWeek()");
        add(Ops.DateTimeOps.DAY_OF_MONTH, "{0}.getDayOfMonth()");
        add(Ops.DateTimeOps.DAY_OF_YEAR,  "{0}.getDayOfYear()");
        add(Ops.DateTimeOps.HOUR,         "{0}.getHourOfDay()");
        add(Ops.DateTimeOps.MINUTE,       "{0}.getMinuteOfHour()");
        add(Ops.DateTimeOps.SECOND,       "{0}.getSecondOfMinute()");
        add(Ops.DateTimeOps.MILLISECOND,  "{0}.getMillisOfSecond()");

        add(Ops.DateTimeOps.YEAR_MONTH,   "({0}.getYear() * 100 + {0}.getMonthOfYear())");
        add(Ops.DateTimeOps.YEAR_WEEK,    "({0}.getWeekyear() * 100 + {0}.getWeekOfWeekyear())");

        // YEAR_WEEK_MYSQL added by OleksF
        add(Ops.DateTimeOps.YEAR_WEEK_MYSQL,    "({0}.yearWeek_mysql_templ())");

    }

    /**
     * Checks whether last week of given year contains a Sunday.
     * 
     * @param y year as int
     * @return boolean for if Sunday is in the last week of the year
     * 
     * @implNote public b/c may be accessed from outside, given templetized nature of calls
     */
    public boolean yearHasSunday(int y){
        boolean sun = false;
        for (int i = 28; i <= 31; i++){
            LocalDate d = new LocalDate(y, 12, i);
            if (d.getDayOfWeek() == 7){
                sun = true;
                break;
            }
        }
        return sun;
    }
    
    /**
     * Given a date input, returns MySQL YEARWEEK() mode 0 (default).
     * This is not ISO8601. For ISO convention, use YEAR_WEEK.
     * Useful for MySQL derived databases as well, such as MariaDB.
     * 
     * 
     * @param d input date
     * @return
     * 
     * @implNote TODO: refactor to different location, as does not fit well
     *  in this template class
     */
    public int yearWeek_mysql_templ(LocalDate d){
        // if a Sunday, increment
        if (d.getDayOfWeek() == 7){
            d.plusDays(1);
        }
        // check previous two years' for Sundays
        //   (for details, look up MySQL and ISO8601 standards)
        String yr;
        String wk = "";
        int lastYr = d.minusYears(1).getYear();
        if (yearHasSunday(lastYr)){
            if (yearHasSunday(lastYr - 1)){
                wk = "52";
            } else {
                wk = "53";
            }
            // for first week, adjsut to previous year and use week number from above
            // and for all others, adjust week number down by 1 to match mode 0's start at 0
            if (d.getWeekOfWeekyear() > 1){
                yr = String.valueOf(d.getYear());
                wk = String.valueOf(d.getWeekOfWeekyear() - 1);
            } else {
                yr = String.valueOf(d.getYear() - 1);
            }
        }
        // if no yerar-change issues, use ISO standard
        else {
            yr = String.valueOf(d.getYear());
            wk = String.valueOf(d.getWeekOfWeekyear());
        }
        return Integer.parseInt(yr + wk);
    }

}
