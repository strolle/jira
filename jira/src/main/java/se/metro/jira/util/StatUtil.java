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

import se.metro.jira.UpdateJiraStats;
import se.metro.jira.communication.domain.ProjectStat;
import se.metro.jira.communication.domain.Ticket;

public class StatUtil {
    public enum StatPeriod {
        WEEK, MONTH
    };

    /**
     * This one breaks all tickets into different periods depending on resolved date
     * 
     * @param tickets
     * @param period
     * @return
     */
    public static List<List<String>> buildStatResolvedPerPeriod(List<Ticket> tickets, StatPeriod period) {
        Map<String, Integer> storysMap = new HashMap<>();
        Map<String, Integer> devRejectedMap = new HashMap<>();
        Map<String, Integer> poRejectedMap = new HashMap<>();
        Map<String, Integer> reopenedMap = new HashMap<>();
        Map<String, ProjectStat> projectStats = new HashMap<>();

        // Add current period first
        String currentPeriodDate = getPeriodString(period, new DateTime());
        storysMap.put(currentPeriodDate, 0);

        for (Ticket ticket : tickets) {
            
            if (ticket.getResolvedDate() != null) {
                String periodDate = getPeriodString(period, ticket.getResolvedDate());

                Integer count = storysMap.get(periodDate);
                count = count != null ? count : 0;
                count++;
                storysMap.put(periodDate, count);

                if(ticket.isRejectedDevTest()){
                    Integer rejectCount = devRejectedMap.get(periodDate);
                    rejectCount = rejectCount != null ? rejectCount : 0;
                    rejectCount++;
                    devRejectedMap.put(periodDate, rejectCount);                    
                }
                if(ticket.isRejectedPoTest()){
                    Integer rejectCount = poRejectedMap.get(periodDate);
                    rejectCount = rejectCount != null ? rejectCount : 0;
                    rejectCount++;
                    poRejectedMap.put(periodDate, rejectCount);                    
                }
                if(ticket.isOpenAfterResolved()){
                    Integer rejectCount = reopenedMap.get(periodDate);
                    rejectCount = rejectCount != null ? rejectCount : 0;
                    rejectCount++;
                    reopenedMap.put(periodDate, rejectCount);                    
                }
                
                
                String project = ticket.getProject();
                if (!projectStats.containsKey(project)) {
                    projectStats.put(ticket.getProject(), new ProjectStat());
                }
                ProjectStat stat = projectStats.get(ticket.getProject());
                Double time = stat.devTimeMap.get(periodDate);
                time = time != null ? time : 0.;
                time += ticket.getTimeInProgress();
                stat.devTimeMap.put(periodDate, time);

                Integer stories = stat.storyMap.get(periodDate);
                stories = stories != null ? stories : 0;
                stories++;
                stat.storyMap.put(periodDate, stories);

                projectStats.put(ticket.getProject(), stat);
            }else if (ticket.getAnalysisStartDate().isAfter(new DateTime().minusMonths(2))) {
                System.out.println(ticket.getId() + " Not resolved - " + ticket.getTimeInProgress());
                // add stats regarding dev time to current period if not yet resolved
                String project = ticket.getProject();
                if (!projectStats.containsKey(project)) {
                    projectStats.put(ticket.getProject(), new ProjectStat());
                }
                ProjectStat stat = projectStats.get(ticket.getProject());
                Double time = stat.devTimeMap.get(currentPeriodDate);
                time = time != null ? time : 0.;
                time += ticket.getTimeInProgress();
                stat.devTimeMap.put(currentPeriodDate, time);
            }
        }
        List<List<String>> result = new ArrayList<>();

        List<String> keys = new ArrayList<>(storysMap.keySet());
        Collections.sort(keys);
        Collections.reverse(keys);
        for (String dateString : keys) {
            List<String> row = new ArrayList<>();
            row.add(dateString);
            row.add("" + (storysMap.get(dateString) != null ? storysMap.get(dateString) : 0));
            for (String project : UpdateJiraStats.config.projects) {
                ProjectStat stat = projectStats.get(project);
                if (stat == null) {
                    row.add("0");
                    row.add("0");
                } else {
                    row.add("" + (stat.storyMap.get(dateString) != null ? stat.storyMap.get(dateString) : 0.));
                    row.add("" + (stat.devTimeMap.get(dateString) != null ? stat.devTimeMap.get(dateString) : 0.));
                }
            }
            
            row.add("" + (devRejectedMap.get(dateString) != null ? devRejectedMap.get(dateString) : 0));
            row.add("" + (poRejectedMap.get(dateString) != null ? poRejectedMap.get(dateString) : 0));
            row.add("" + (reopenedMap.get(dateString) != null ? reopenedMap.get(dateString) : 0));
            
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
    // [8] Time in waiting for merge
    // [9] Time in waiting for stage
    // [10] Time in waiting for release

    public static List<List<String>> buildTimeInStatusPerMonth(List<Ticket> tickets) {
        Map<String, List<List<Double>>> timeSpentInStatusPerPeriod = new HashMap<>();
        for (Ticket ticket : tickets) {
            if (ticket.getResolvedDate() != null) {
                String periodDate = new DateTime(ticket.getResolvedDate()).withDayOfMonth(1).toString("yyyy-MM-dd");

                List<List<Double>> timeSpentInStatus = timeSpentInStatusPerPeriod.get(periodDate);
                if (timeSpentInStatus == null) {
                    timeSpentInStatus = new ArrayList<>();
                    timeSpentInStatusPerPeriod.put(periodDate, timeSpentInStatus);
                    for (int i = 0; i < 11; i++) {
                        timeSpentInStatus.add(new ArrayList<Double>());
                    }
                }

                if (ticket.getReleasedDate() != null)
                    timeSpentInStatus.get(0).add(TimeUtil.getWorkdaysBetween(ticket.getAnalysisStartDate(), ticket.getReleasedDate()));
                timeSpentInStatus.get(1).add(TimeUtil.getWorkdaysBetween(ticket.getAnalysisStartDate(), ticket.getResolvedDate()));
                timeSpentInStatus.get(2).add(ticket.getTimeInAnalysis());
                timeSpentInStatus.get(3).add(ticket.getTimeInProgress());
                timeSpentInStatus.get(4).add(ticket.getTimeInDevTest() + ticket.getTimeInAcceptTest());
                timeSpentInStatus.get(5).add(ticket.getTimeInDevTest());
                timeSpentInStatus.get(6).add(ticket.getTimeInAcceptTest());
                timeSpentInStatus.get(7).add(ticket.getTimeInWaitingForMerge() + ticket.getTimeInWaitingForStage() + ticket.getTimeInWaitingForRelease());
                timeSpentInStatus.get(8).add(ticket.getTimeInWaitingForMerge());
                timeSpentInStatus.get(9).add(ticket.getTimeInWaitingForStage());
                timeSpentInStatus.get(10).add(ticket.getTimeInWaitingForRelease());

                if (ticket.getReleasedDate() != null) {
                    double total = ticket.getTimeInAnalysis() + ticket.getTimeInProgress() + ticket.getTimeInDevTest() + ticket.getTimeInAcceptTest()
                            + ticket.getTimeInWaitingForMerge() + ticket.getTimeInWaitingForStage() + ticket.getTimeInWaitingForRelease();
                    double startEnd = TimeUtil.getWorkdaysBetween(ticket.getAnalysisStartDate(), ticket.getReleasedDate());
                    double diff = Math.round(100. * (total - startEnd) / total);
                    if (diff > 5. || diff < -5.)
                        System.out.println(ticket.getId() + " - " + startEnd + " - " + total + " - " + diff);
                }
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
            for (List<Double> statList : statLists) {
                resultRow.add("" + median.evaluate(ArrayUtils.toPrimitive(statList.toArray(new Double[0]))));
                resultRow.add("" + mean.evaluate(ArrayUtils.toPrimitive(statList.toArray(new Double[0]))));
            }
            result.add(resultRow);
        }
        return result;
    }
}
