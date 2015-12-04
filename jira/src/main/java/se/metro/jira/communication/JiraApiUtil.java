package se.metro.jira.communication;

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
            " AND status changed during (%s, now()) AND status changed to ('In Progress') AND NOT (status changed from (Open) to (Closed))";

    private static JiraRestClient client = null;

    public static void initClient(String username, String password) throws Exception {
        JiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
        URI uri = new URI(JIRA_URL);
        client = factory.createWithBasicHttpAuthentication(uri, username, password);
    }

    public static Iterable<Issue> getTicketList(String startPeriod, int offset) {
        Set<String> fieldSet = new HashSet<>(
                Arrays.asList("status", "project", "created", "updated", "key", "summary", "issuetype"));
        Promise<SearchResult> result = client.getSearchClient().searchJql(String.format(UpdateJiraStats.config.jql + MAIN_JQL, startPeriod), 500, offset, fieldSet);
        return result.claim().getIssues();
    }

    public static Ticket getIssue(String id) {
        Issue issue = client.getIssueClient().getIssue(id, Arrays.asList(Expandos.CHANGELOG)).claim();
        return createTicketFromIssue(issue);
    }

    private static Ticket createTicketFromIssue(Issue issue) {
        Ticket ticket = new Ticket();
        ticket.setId(issue.getKey());

        String project = issue.getProject().getKey();
        if (!UpdateJiraStats.config.projectField.equals("project")) {
            project = issue.getField(UpdateJiraStats.config.projectField).getValue().toString();
        }
        if (project != null) {
            ticket.setProj1(project.contains(UpdateJiraStats.config.proj1String));
            ticket.setProj2(project.contains(UpdateJiraStats.config.proj2String));
        }

        double storypoint = 0.;
        if (issue.getField(UpdateJiraStats.config.storyPointField) != null && issue.getField(UpdateJiraStats.config.storyPointField).getValue() != null)
            storypoint = (double) issue.getField(UpdateJiraStats.config.storyPointField).getValue();
        ticket.setStorypoint(storypoint);

        processTransactionHistory(issue, ticket);
        return ticket;
    }

    private static void processTransactionHistory(Issue issue, Ticket ticket) {
        DateTime lastTransistion = null;
        for (ChangelogGroup clg : issue.getChangelog()) {
            for (ChangelogItem cli : clg.getItems()) {
                if (!(cli.getField().equals("Fix Version") || cli.getField().equals("status"))) {
                    continue;
                }
                System.out.println(clg.getCreated() + ": " + cli.getFromString() + "->" + cli.getToString());
                double time = 0.;
                if (lastTransistion != null) {
                    time = TimeUtil.getWorkdaysBetween(lastTransistion, clg.getCreated());
                }

                if (cli.getField().equals("Fix Version")) {
                    ticket.setTimeInWaitingForRelease(ticket.getTimeInWaitingForStage() + time);
                    ticket.setReleasedDate(clg.getCreated());
                } else if (cli.getFromString() != null && cli.getFromString().equals("Open")) {
                    ticket.setAnalysisStartDate(clg.getCreated());
                } else if (cli.getFromString() != null && cli.getFromString().equals("Analysis")) {
                    ticket.setTimeInAnalysis(ticket.getTimeInAnalysis() + time);
                } else if (cli.getFromString() != null && cli.getFromString().equals("Ready for Development")) {
                    ticket.setTimeInWaitingForDev(ticket.getTimeInWaitingForDev() + time);
                } else if (cli.getFromString() != null && cli.getFromString().equals("In Progress")) {
                    ticket.setTimeInProgress(ticket.getTimeInProgress() + time);
                } else if (cli.getFromString() != null && cli.getFromString().equals("Dev-test")) {
                    ticket.setTimeInDevTest(ticket.getTimeInDevTest() + time);
                } else if (cli.getFromString() != null && cli.getFromString().equals("DOD")) {
                    ticket.setTimeInAcceptTest(ticket.getTimeInAcceptTest() + time);
                } else if (cli.getFromString() != null && cli.getFromString().equals("Resolved")) {
                    ticket.setTimeInWaitingForStage(ticket.getTimeInWaitingForStage() + time);
                } else {
                    System.err.println("ERROR: DIDN'T RESOLVE MAP");
                }

                if (cli.getToString() != null && cli.getToString().equals("Resolved")) {
                    ticket.setResolvedDate(clg.getCreated());
                } else if (cli.getToString() != null && cli.getToString().equals("Closed")) {
                    ticket.setClosedDate(clg.getCreated());
                }
                lastTransistion = clg.getCreated();
            }
        }

    }
}
