package se.metro.jira.communication;

import java.net.URI;
import java.util.*;

import org.codehaus.jettison.json.*;
import org.joda.time.DateTime;

import com.atlassian.jira.rest.client.api.IssueRestClient.Expandos;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
import com.atlassian.jira.rest.client.api.domain.ChangelogGroup;
import com.atlassian.jira.rest.client.api.domain.ChangelogItem;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.atlassian.util.concurrent.Promise;

import se.metro.jira.UpdateJiraStats;
import se.metro.jira.communication.domain.Ticket;
import se.metro.jira.util.TimeUtil;

public class JiraApiUtil {
    private static final String JIRA_URL = "https://metroonline.atlassian.net";
    private static final String MAIN_JQL =
            " AND status changed during (%s, now()) AND status changed to ('In Progress') AND NOT (status changed from ('Backlog') to ('Done'))";

    private static JiraRestClient client = null;

    public static final Set<String> parentIssues = new HashSet<>();

    public static void initClient(String username, String password) throws Exception {
        JiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
        URI uri = new URI(JIRA_URL);
        client = factory.createWithBasicHttpAuthentication(uri, username, password);
    }

    public static Iterable<Issue> getTicketList(String startPeriod, int offset) {
        Set<String> fieldSet = new HashSet<>(
                Arrays.asList("status", "project", "created", "updated", "key", "summary", "issuetype"));
        Promise<SearchResult> result =
                client.getSearchClient().searchJql(String.format(UpdateJiraStats.config.getJql() + MAIN_JQL, startPeriod), 100, offset, fieldSet);
        return result.claim().getIssues();
    }

    public static Ticket getIssue(String id) {
        Issue issue = client.getIssueClient().getIssue(id, Arrays.asList(Expandos.CHANGELOG)).claim();
        return createTicketFromIssue(issue);
    }

    private static Ticket createTicketFromIssue(Issue issue) {
        Ticket ticket = new Ticket();
        ticket.setId(issue.getKey());
        ticket.setProject(issue.getProject().getKey());

        if(issue.getField("parent") != null){
            try {
                parentIssues.add(((JSONObject)issue.getField("parent").getValue()).getString("key"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        processTransactionHistory(issue, ticket);
        return ticket;
    }

    private static void processTransactionHistory(Issue issue, Ticket ticket) {
        DateTime lastTransistion = null;
        boolean openToReady = false;

        //fix jira sorting issue
        List<ChangelogGroup> groupList = new ArrayList<>();
        for (ChangelogGroup clg : issue.getChangelog()) {
            groupList.add(clg);
        }
        if (groupList.size() > 1 && groupList.get(0).getCreated().isAfter(groupList.get(groupList.size() - 1).getCreated())) {
            Collections.reverse(groupList);
        }

        String currentStatus = null;

        for (ChangelogGroup clg : groupList) {
            for (ChangelogItem cli : clg.getItems()) {
                if (cli.getField().equals("Flagged") && cli.getTo().toString().equals("[10000]")) {
                    //track flagged
                    if (currentStatus != null && currentStatus.equals("Dev Review")) {
                        ticket.setTimeForUntouchedInDevTest(ticket.getTimeForUntouchedInDevTest() + TimeUtil.getWorkdaysBetween(lastTransistion, clg.getCreated()));
                        currentStatus = "";
                    }
                    continue;
                }

                if (!(cli.getField().equals("Fix Version") || cli.getField().equals("status"))) {
                    continue;
                }
                System.out.println(clg.getCreated() + ": " + cli.getFromString() + "->" + cli.getToString());
                double time = 0.;
                if (lastTransistion != null) {
                    time = TimeUtil.getWorkdaysBetween(lastTransistion, clg.getCreated());
                }

                currentStatus = cli.getToString();
                if (cli.getField().equals("Fix Version")) {
                    ticket.setTimeInWaitingForRelease(ticket.getTimeInWaitingForRelease() + time);
                    ticket.setReleasedDate(clg.getCreated());
                } else if (cli.getFromString() != null && cli.getFromString().equals("Backlog")) {
                    if (cli.getToString() != null && cli.getToString().equals("Ready for Development")) {
                        openToReady = true;
                    }
                    ticket.setProgressStartDate(clg.getCreated());
                } else if (cli.getFromString() != null && cli.getFromString().equals("In Progress")) {
                    ticket.setTimeInProgress(ticket.getTimeInProgress() + time);
                } else if (cli.getFromString() != null && cli.getFromString().equals("Dev Review")) {
                    ticket.setTimeInDevTest(ticket.getTimeInDevTest() + time);
                    String to = cli.getToString();
                    if (to != null && (to.equals("In Progress") || to.equals("Backlog") || cli.getToString().equals("Ready for Development"))) {
                        ticket.setRejectedDevTest(true);
                    }
                } else if (cli.getFromString() != null && cli.getFromString().equals("DOD")) {
                    String to = cli.getToString();
                    if (to != null && (to.equals("In Progress") || to.equals("Backlog") || cli.getToString().equals("Ready for Development"))) {
                        ticket.setRejectedPoTest(true);
                    }
                    ticket.setTimeInAcceptTest(ticket.getTimeInAcceptTest() + time);
                } else if (cli.getFromString() != null && (cli.getFromString().equals("Build-RC"))) {
                    ticket.setTimeInRC(ticket.getTimeInRC() + time);
                    String to = cli.getToString();
                    if (to != null && to.equals("Backlog")) {
                        System.out.println("!!" + ticket.getId() + " RESOLVED TO OPEN: " + cli.getFromString());
                        ticket.setOpenAfterResolved(true);
                    }
                } else if (cli.getFromString() != null && cli.getFromString().equals("Done")) {
                    String to = cli.getToString();
                    if (to != null && to.equals("Backlog")) {
                        System.out.println("!!" + ticket.getId() + " CLOSED TO OPEN: " + cli.getFromString());
                        ticket.setOpenAfterResolved(true);
                    }
                } else if (cli.getFromString() != null && cli.getFromString().equals("Ready for Development")) {
                    if (openToReady) {
                        ticket.setProgressStartDate(clg.getCreated());
                        openToReady = false;
                    }
                } else {
                    System.out.println("!!" + ticket.getId() + " ERROR: DIDN'T RESOLVE MAP: " + cli.getFromString());
                }

                if (cli.getToString() != null && cli.getToString().equals("Build-RC")) {
                    if (ticket.getResolvedDate() != null && !ticket.isOpenAfterResolved())
                        System.out.println("!! " + ticket.getId() + " Build-RC date already set to " + ticket.getResolvedDate() + " -  Skipping");
                    else {
                        if (ticket.getResolvedDate() != null)
                            System.out.println("!! " + ticket.getId() + " Resetting resolved date from " + ticket.getResolvedDate());
                        ticket.setResolvedDate(clg.getCreated());
                    }
                } else if (cli.getToString() != null && cli.getToString().equals("Done")) {
                    if (ticket.getClosedDate() != null && !ticket.isOpenAfterResolved())
                        System.out.println("!! " + ticket.getId() + " Closed date already set to " + ticket.getClosedDate() + " -  Skipping");
                    else {
                        if (ticket.getClosedDate() != null)
                            System.out.println("!! " + ticket.getId() + " Resetting closed date from " + ticket.getClosedDate());
                        ticket.setClosedDate(clg.getCreated());
                        //set resolved as well
                        if (ticket.getResolvedDate() == null) {
                            ticket.setResolvedDate(clg.getCreated());
                        }
                    }
                }
                lastTransistion = clg.getCreated();
            }
        }
    }
}
