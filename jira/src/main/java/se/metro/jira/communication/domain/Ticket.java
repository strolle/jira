package se.metro.jira.communication.domain;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import se.metro.jira.UpdateJiraStats;

/**
 * Holds info of a specific ticket.
 * 
 * @author stefantrolle
 *
 */
public class Ticket {
    int sheetRow = -1;

    String id;
    double storypoint;
    boolean isProj1;
    boolean isProj2;
    DateTime analysisStartDate;
    DateTime resolvedDate;
    DateTime closedDate;
    DateTime releasedDate;
    double timeInAnalysis;
    double timeInWaitingForDev;
    double timeInProgress;
    double timeInDevTest;
    double timeInAcceptTest;
    double timeInWaitingForStage;
    double timeInWaitingForRelease;
    boolean ignoreRow;

    public void setValue(String tag, String value) {
        if (tag.equals("id")) {
            id = value;
        } else if (tag.equals("sp")) {
            storypoint = getDouble(value);
        } else if (tag.equalsIgnoreCase(UpdateJiraStats.config.proj1String)) {
            isProj1 = getBoolean(value);
        } else if (tag.equalsIgnoreCase(UpdateJiraStats.config.proj2String)) {
            isProj2 = getBoolean(value);
        } else if (tag.equals("startdate")) {
            analysisStartDate = getDate(value);
        } else if (tag.equals("resolveddate")) {
            resolvedDate = getDate(value);
        } else if (tag.equals("closeddate")) {
            closedDate = getDate(value);
        } else if (tag.equals("releasedate")) {
            releasedDate = getDate(value);
        } else if (tag.equals("timeinanalysis")) {
            timeInAnalysis = getDouble(value);
        } else if (tag.equals("timewaitingfordev")) {
            timeInWaitingForDev = getDouble(value);
        } else if (tag.equals("timeinprogress")) {
            timeInProgress = getDouble(value);
        } else if (tag.equals("timeindevtest")) {
            timeInDevTest = getDouble(value);
        } else if (tag.equals("timeinaccepttest")) {
            timeInAcceptTest = getDouble(value);
        } else if (tag.equals("timeinwaitingforstage")) {
            timeInWaitingForStage = getDouble(value);
        } else if (tag.equals("timeinwaitingforrelease")) {
            timeInWaitingForRelease = getDouble(value);
        } else if (tag.equals("ignorerow")) {
            ignoreRow = (value != null && value.length() > 0);
        } else {
            System.err.println("unmapped: " + tag);
        }
    }

    private double getDouble(String value) {
        if (value != null)
            return Double.parseDouble(value);
        return 0.;
    }

    private DateTime getDate(String value) {
        if (value != null && !value.equalsIgnoreCase("null"))
            return DateTime.parse(value);
        return null;
    }

    private boolean getBoolean(String value) {
        return Boolean.parseBoolean(value);
    }

    public int getSheetRow() {
        return sheetRow;
    }

    public void setSheetRow(int sheetRow) {
        this.sheetRow = sheetRow;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getStorypoint() {
        return storypoint;
    }

    public void setStorypoint(double storypoint) {
        this.storypoint = storypoint;
    }

    public boolean isProj1() {
        return isProj1;
    }

    public void setProj1(boolean isProj1) {
        this.isProj1 = isProj1;
    }

    public boolean isProj2() {
        return isProj2;
    }

    public void setProj2(boolean isProj2) {
        this.isProj2 = isProj2;
    }

    public DateTime getAnalysisStartDate() {
        return analysisStartDate;
    }

    public void setAnalysisStartDate(DateTime analysisStartDate) {
        this.analysisStartDate = analysisStartDate;
    }

    public DateTime getResolvedDate() {
        return resolvedDate;
    }

    public void setResolvedDate(DateTime resolvedDate) {
        this.resolvedDate = resolvedDate;
    }

    public DateTime getClosedDate() {
        return closedDate;
    }

    public void setClosedDate(DateTime closedDate) {
        this.closedDate = closedDate;
    }

    public DateTime getReleasedDate() {
        return releasedDate;
    }

    public void setReleasedDate(DateTime releasedDate) {
        this.releasedDate = releasedDate;
    }

    public double getTimeInAnalysis() {
        return timeInAnalysis;
    }

    public void setTimeInAnalysis(double timeInAnalysis) {
        this.timeInAnalysis = timeInAnalysis;
    }

    public double getTimeInWaitingForDev() {
        return timeInWaitingForDev;
    }

    public void setTimeInWaitingForDev(double timeInWaitingForDev) {
        this.timeInWaitingForDev = timeInWaitingForDev;
    }

    public double getTimeInProgress() {
        return timeInProgress;
    }

    public void setTimeInProgress(double timeInProgress) {
        this.timeInProgress = timeInProgress;
    }

    public double getTimeInDevTest() {
        return timeInDevTest;
    }

    public void setTimeInDevTest(double timeInDevTest) {
        this.timeInDevTest = timeInDevTest;
    }

    public double getTimeInAcceptTest() {
        return timeInAcceptTest;
    }

    public void setTimeInAcceptTest(double timeInAcceptTest) {
        this.timeInAcceptTest = timeInAcceptTest;
    }

    public double getTimeInWaitingForStage() {
        return timeInWaitingForStage;
    }

    public void setTimeInWaitingForStage(double timeInWaitingForStage) {
        this.timeInWaitingForStage = timeInWaitingForStage;
    }

    public double getTimeInWaitingForRelease() {
        return timeInWaitingForRelease;
    }

    public void setTimeInWaitingForRelease(double timeInWaitingForRelease) {
        this.timeInWaitingForRelease = timeInWaitingForRelease;
    }

    public boolean isIgnoreRow() {
        return ignoreRow;
    }

    public void setIgnoreRow(boolean ignoreRow) {
        this.ignoreRow = ignoreRow;
    }

    public List<String> getValues() {
        List<String> list = new ArrayList<String>();
        list.add(id.toString());
        list.add("" + storypoint);
        list.add("" + isProj1);
        list.add("" + isProj2);
        list.add("" + analysisStartDate);
        list.add("" + resolvedDate);
        list.add("" + closedDate);
        list.add("" + releasedDate);
        list.add("" + timeInAnalysis);
        list.add("" + timeInWaitingForDev);
        list.add("" + timeInProgress);
        list.add("" + timeInDevTest);
        list.add("" + timeInAcceptTest);
        list.add("" + timeInWaitingForStage);
        list.add("" + timeInWaitingForRelease);

        return list;
    }

    @Override
    public String toString() {
        return "Ticket [id=" + id + ", storypoint=" + storypoint + ", isAs=" + isProj1 + ", isMj=" + isProj2 + ", analysisStartDate=" + analysisStartDate
                + ", resolvedDate=" + resolvedDate + ", closedDate=" + closedDate + ", timeInAnalysis=" + timeInAnalysis + ", timeInWaitingForDev="
                + timeInWaitingForDev + ", timeInProgress=" + timeInProgress + ", timeInDevTest=" + timeInDevTest + ", timeInAcceptTest=" + timeInAcceptTest
                + ", timeInWaitingForStage=" + timeInWaitingForStage + ", timeInWaitingForRelease=" + timeInWaitingForRelease + "]";
    }
}
