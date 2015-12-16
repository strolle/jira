package se.metro.jira.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.rank.Median;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

import se.metro.jira.communication.domain.Ticket;

public class StatUtil {
    public enum StatPeriod{WEEK, MONTH};
    
    /**
     * This one breaks all tickets into different rovs depending on 
     * @param tickets
     * @param period
     * @return
     */
    public static List<List<String>> buildStoryPointResolvedPerPeriod(List<Ticket> tickets, StatPeriod period) {
        Map<String, Double> storyPointMap = new HashMap<>();
        Map<String, Integer> storysMap = new HashMap<>();
        Map<String, Double> proj1StoryPointMap = new HashMap<>();
        Map<String, Integer> proj1StorysMap = new HashMap<>();
        Map<String, Double> proj1DevTimeMap = new HashMap<>();
        Map<String, Double> proj2StoryPointMap = new HashMap<>();
        Map<String, Integer> proj2StorysMap = new HashMap<>();
        Map<String, Double> proj2DevTimeMap = new HashMap<>();

        //Add current period first
        String currentPeriodDate = getPeriodString(period, new DateTime());
        storyPointMap.put(currentPeriodDate, 0.);
         
        for (Ticket ticket : tickets) {
            if (ticket.getResolvedDate() != null) {
                String periodDate = getPeriodString(period, ticket.getResolvedDate());
                Double sp = storyPointMap.get(periodDate);
                sp = sp != null ? sp : 0.;
                sp += ticket.getStorypoint();
                storyPointMap.put(periodDate, sp);

                Integer count = storysMap.get(periodDate);
                count = count != null ? count : 0;
                count++;
                storysMap.put(periodDate, count);

                if (ticket.isProj2()) {
                    Double proj2Sp = proj2StoryPointMap.get(periodDate);
                    proj2Sp = proj2Sp != null ? proj2Sp : 0.;
                    proj2Sp += ticket.getStorypoint();
                    proj2StoryPointMap.put(periodDate, proj2Sp);

                    Double proj2Time = proj2DevTimeMap.get(periodDate);
                    proj2Time = proj2Time != null ? proj2Time : 0.;
                    proj2Time += ticket.getTimeInProgress();
                    proj2DevTimeMap.put(periodDate, proj2Time);

                    Integer proj2Count = proj2StorysMap.get(periodDate);
                    proj2Count = proj2Count != null ? proj2Count : 0;
                    proj2Count++;
                    proj2StorysMap.put(periodDate, proj2Count);
                }
                if (ticket.isProj1()) {
                    Double proj1Sp = proj1StoryPointMap.get(periodDate);
                    proj1Sp = proj1Sp != null ? proj1Sp : 0.;
                    proj1Sp += ticket.getStorypoint();
                    proj1StoryPointMap.put(periodDate, proj1Sp);

                    Double proj1Time = proj1DevTimeMap.get(periodDate);
                    proj1Time = proj1Time != null ? proj1Time : 0.;
                    proj1Time += ticket.getTimeInProgress();
                    proj1DevTimeMap.put(periodDate, proj1Time);

                    Integer proj1Count = proj1StorysMap.get(periodDate);
                    proj1Count = proj1Count != null ? proj1Count : 0;
                    proj1Count++;
                    proj1StorysMap.put(periodDate, proj1Count);
                }
            }
        }
        List<List<String>> result = new ArrayList<>();

        List<String> keys = new ArrayList<>(storyPointMap.keySet());
        Collections.sort(keys);
        Collections.reverse(keys);
        for (String dateString : keys) {
            List<String> row = new ArrayList<>();
            row.add(dateString);
            row.add("" + storyPointMap.get(dateString));
            row.add("" + (storysMap.get(dateString) != null ? storysMap.get(dateString) : 0));
            row.add("" + (proj2StoryPointMap.get(dateString) != null ? proj2StoryPointMap.get(dateString) : 0.));
            row.add("" + (proj2StorysMap.get(dateString) != null ? proj2StorysMap.get(dateString) : 0.));
            row.add("" + (proj1StoryPointMap.get(dateString) != null ? proj1StoryPointMap.get(dateString) : 0.));
            row.add("" + (proj1StorysMap.get(dateString) != null ? proj1StorysMap.get(dateString) : 0.));

            row.add("" + (proj2DevTimeMap.get(dateString) != null ? proj2DevTimeMap.get(dateString) : 0.));
            row.add("" + (proj1DevTimeMap.get(dateString) != null ? proj1DevTimeMap.get(dateString) : 0.));
            result.add(row);
        }

        return result;
    }
    private static String getPeriodString(StatPeriod period, DateTime dateTime) {
        String periodDate = null;
        if (period.equals(StatPeriod.WEEK)) {
            periodDate = dateTime.withDayOfWeek(DateTimeConstants.MONDAY).toString("yyyy-MM-dd");
        } else {
            periodDate = dateTime.withDayOfMonth(1).toString("yyyy-MM-dd");
        }
        return periodDate;
    }
    // [0] Time to release
    // [1] Time to resolved
    // [2] Time before progress
    // [3] Time in progress
    // [4] Time in test
    // [5] Time in dev-test
    // [6] Time in PO-test
    // [7] Time after resolved
    // [8] Time in waiting for stage
    // [9] Time in waiting for release

    public static List<List<String>> buildTimeInStatusPerMonth(List<Ticket> tickets) {
        Map<String, List<List<Double>>> timeSpentInStatusPerPeriod = new HashMap<>();
        for (Ticket ticket : tickets) {
            if (ticket.getResolvedDate() != null) {
                String periodDate = new DateTime(ticket.getResolvedDate()).withDayOfMonth(1).toString("yyyy-MM-dd");

                List<List<Double>> timeSpentInStatus = timeSpentInStatusPerPeriod.get(periodDate);
                if (timeSpentInStatus == null) {
                    timeSpentInStatus = new ArrayList<>();
                    timeSpentInStatusPerPeriod.put(periodDate, timeSpentInStatus);
                    for(int i = 0; i < 10; i++){
                        timeSpentInStatus.add(new ArrayList<Double>());
                    }
                }
                
                if(ticket.getReleasedDate() != null)
                    timeSpentInStatus.get(0).add(TimeUtil.getWorkdaysBetween(ticket.getAnalysisStartDate(), ticket.getReleasedDate()));
                timeSpentInStatus.get(1).add(TimeUtil.getWorkdaysBetween(ticket.getAnalysisStartDate(), ticket.getResolvedDate()));
                timeSpentInStatus.get(2).add(ticket.getTimeInAnalysis() + ticket.getTimeInWaitingForDev());
                timeSpentInStatus.get(3).add(ticket.getTimeInProgress());
                timeSpentInStatus.get(4).add(ticket.getTimeInDevTest() + ticket.getTimeInAcceptTest());
                timeSpentInStatus.get(5).add(ticket.getTimeInDevTest());
                timeSpentInStatus.get(6).add(ticket.getTimeInAcceptTest());
                timeSpentInStatus.get(7).add(ticket.getTimeInWaitingForStage() + ticket.getTimeInWaitingForRelease());
                timeSpentInStatus.get(8).add(ticket.getTimeInWaitingForStage());
                timeSpentInStatus.get(9).add(ticket.getTimeInWaitingForRelease());
            }
        }
        Median median = new Median();
        Mean mean = new Mean();
        List<List<String>> result = new ArrayList<>();

        List<String> keys = new ArrayList<>(timeSpentInStatusPerPeriod.keySet());
        Collections.sort(keys);
        Collections.reverse(keys);
        for (String dateString : keys) {
            List<String> resultRow = new ArrayList<>();
            resultRow.add(dateString);
            
            List<List<Double>> statLists = timeSpentInStatusPerPeriod.get(dateString);
            for(List<Double> statList : statLists ){
                resultRow.add("" + median.evaluate(ArrayUtils.toPrimitive(statList.toArray(new Double[0]))));
                resultRow.add("" + mean.evaluate(ArrayUtils.toPrimitive(statList.toArray(new Double[0]))));
            }
            result.add(resultRow);
        }
        return result;
    }
}
