package se.metro.jira.util;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Days;
import org.joda.time.Hours;

public class TimeUtil {
    public static double getWorkdaysBetween(DateTime firstDate, DateTime lastDate) {
        long hours = Hours.hoursBetween(firstDate, lastDate).getHours();
        long days = hours/24;
        
        long workHours = hours - days*16;

        hours= hours%24;
        if(hours > 12 && firstDate.getMillisOfDay() < lastDate.getMillisOfDay())
            workHours -= 7;
        else if(hours > 10 && firstDate.getMillisOfDay() > lastDate.getMillisOfDay())
            workHours -= 15;
        
        int daysBetween = Days.daysBetween(firstDate, lastDate).getDays();
        int daysToProcess = daysBetween;
        int weekendDays = 0;
        if (daysBetween >= 7) {
            weekendDays = (daysBetween / 7) * 2;
            daysToProcess = (daysBetween % 7);
        }

        DateTime loopDate = new DateTime(lastDate);
        while (daysToProcess > 0) {
            loopDate = loopDate.minusDays(1);

            if (loopDate.getDayOfWeek() == DateTimeConstants.SATURDAY || loopDate.getDayOfWeek() == DateTimeConstants.SUNDAY)
                weekendDays++;
            daysToProcess--;
        }
        workHours -= 8*weekendDays;
        
        double value = ((double)workHours)/8.;
        if (value < 0.)
            value = 0.;
        return value;
    }
}
