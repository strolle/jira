package se.metro.jira.communication.domain;

import java.util.*;

import org.joda.time.DateTime;

/**
 * Holds info of a specific ticket.
 * 
 * @author stefantrolle
 *
 */
public class Ticket {
    private int sheetRow = -1;

    private String id;
    private String project;
    private DateTime progressStartDate;
    private DateTime resolvedDate;
    private DateTime closedDate;
    private DateTime releasedDate;
    private double timeInProgress;
    private double timeInDevTest;
    private double timeInAcceptTest;
    private double timeInRC;
    private double timeInWaitingForRelease;

    private double timeInProgressAfterTest;
    private double timeForUntouchedInDevTest;

    private boolean rejectedDevTest;
    private boolean rejectedPoTest;
    private boolean openAfterResolved;
    private boolean ignoreRow;

    public void setValue(String tag, String value) {
        if (tag.equals("id")) {
            id = value;
        } else if (tag.equalsIgnoreCase("project")) {
            project = value;
        } else if (tag.equals("startdate")) {
            progressStartDate = getDate(value);
        } else if (tag.equals("resolveddate")) {
            resolvedDate = getDate(value);
        } else if (tag.equals("closeddate")) {
            closedDate = getDate(value);
        } else if (tag.equals("releasedate")) {
            releasedDate = getDate(value);
        } else if (tag.equals("timeinprogress")) {
            timeInProgress = getDouble(value);
        } else if (tag.equals("timeindevtest")) {
            timeInDevTest = getDouble(value);
        } else if (tag.equals("timeinaccepttest")) {
            timeInAcceptTest = getDouble(value);
        } else if (tag.equals("timeinrc")) {
            timeInRC = getDouble(value);
        } else if (tag.equals("timeinwaitingforrelease")) {
            timeInWaitingForRelease = getDouble(value);
        } else if (tag.equalsIgnoreCase("rejecteddevtest")) {
            rejectedDevTest = getBoolean(value);
        } else if (tag.equalsIgnoreCase("rejectedpotest")) {
            rejectedPoTest = getBoolean(value);
        } else if (tag.equalsIgnoreCase("openafterresolved")) {
            openAfterResolved = getBoolean(value);
        } else if (tag.equalsIgnoreCase("timeInProgressAfterTest")) {
            timeInProgressAfterTest = getDouble(value);
        } else if (tag.equalsIgnoreCase("timeForUntouchedInDevTest")) {
            timeForUntouchedInDevTest = getDouble(value);
        } else if (tag.equals("ignorerow")) {
            ignoreRow = (value != null && value.length() > 0);
        } else {
            System.err.println("unmapped: " + tag);
        }
    }

    private double getDouble(String value) {
        if (value != null) {
            try {
                value = value.replace(',', '.');
                return Double.parseDouble(value);
            } catch (Exception e) {
               e.printStackTrace();
            }
        }
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

    public List<String> getValues() {
        List<String> list = new ArrayList<>();
        list.add(id);
        list.add("" + project);
        list.add("" + progressStartDate);
        list.add("" + resolvedDate);
        list.add("" + closedDate);
        list.add("" + releasedDate);
        list.add("" + timeInProgress);
        list.add("" + timeInDevTest);
        list.add("" + timeInAcceptTest);
        list.add("" + timeInRC);
        list.add("" + timeInWaitingForRelease);
        list.add("" + timeInProgressAfterTest);
        list.add("" + timeForUntouchedInDevTest);

        list.add("" + rejectedDevTest);
        list.add("" + rejectedPoTest);
        list.add("" + openAfterResolved);
        return list;
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

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public DateTime getProgressStartDate() {
        return progressStartDate;
    }

    public void setProgressStartDate(DateTime progressStartDate) {
        this.progressStartDate = progressStartDate;
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

    public double getTimeInRC() {
        return timeInRC;
    }

    public void setTimeInRC(double timeInRC) {
        this.timeInRC = timeInRC;
    }

    public double getTimeInWaitingForRelease() {
        return timeInWaitingForRelease;
    }

    public void setTimeInWaitingForRelease(double timeInWaitingForRelease) {
        this.timeInWaitingForRelease = timeInWaitingForRelease;
    }

    public double getTimeInProgressAfterTest() {
        return timeInProgressAfterTest;
    }

    public void setTimeInProgressAfterTest(double timeInProgressAfterTest) {
        this.timeInProgressAfterTest = timeInProgressAfterTest;
    }

    public double getTimeForUntouchedInDevTest() {
        return timeForUntouchedInDevTest;
    }

    public void setTimeForUntouchedInDevTest(double timeForUntouchedInDevTest) {
        this.timeForUntouchedInDevTest = timeForUntouchedInDevTest;
    }

    public boolean isRejectedDevTest() {
        return rejectedDevTest;
    }

    public void setRejectedDevTest(boolean rejectedDevTest) {
        this.rejectedDevTest = rejectedDevTest;
    }

    public boolean isRejectedPoTest() {
        return rejectedPoTest;
    }

    public void setRejectedPoTest(boolean rejectedPoTest) {
        this.rejectedPoTest = rejectedPoTest;
    }

    public boolean isOpenAfterResolved() {
        return openAfterResolved;
    }

    public void setOpenAfterResolved(boolean openAfterResolved) {
        this.openAfterResolved = openAfterResolved;
    }

    public boolean isIgnoreRow() {
        return ignoreRow;
    }

    public void setIgnoreRow(boolean ignoreRow) {
        this.ignoreRow = ignoreRow;
    }

    @Override
    public String toString() {
        return "Ticket{" +
                "sheetRow=" + sheetRow +
                ", id='" + id + '\'' +
                ", project='" + project + '\'' +
                ", progressStartDate=" + progressStartDate +
                ", resolvedDate=" + resolvedDate +
                ", closedDate=" + closedDate +
                ", releasedDate=" + releasedDate +
                ", timeInProgress=" + timeInProgress +
                ", timeInDevTest=" + timeInDevTest +
                ", timeInAcceptTest=" + timeInAcceptTest +
                ", timeInRC=" + timeInRC +
                ", timeInWaitingForRelease=" + timeInWaitingForRelease +
                ", timeInProgressAfterTest=" + timeInProgressAfterTest +
                ", timeForUntouchedInDevTest=" + timeForUntouchedInDevTest +
                ", rejectedDevTest=" + rejectedDevTest +
                ", rejectedPoTest=" + rejectedPoTest +
                ", openAfterResolved=" + openAfterResolved +
                ", ignoreRow=" + ignoreRow +
                '}';
    }
}
