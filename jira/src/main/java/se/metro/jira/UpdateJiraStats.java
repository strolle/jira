package se.metro.jira;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.google.gdata.data.spreadsheet.CellFeed;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.WorksheetEntry;

import se.metro.jira.communication.JiraApiUtil;
import se.metro.jira.communication.domain.Ticket;
import se.metro.jira.google.SpreadsheetUtil;
import se.metro.jira.util.FileUtil;
import se.metro.jira.util.StatUtil;
import se.metro.jira.util.StatUtil.StatPeriod;

public class UpdateJiraStats {
    public static Configuration config = new Configuration();

    public static void main(String[] args) throws Exception {
        try {
            if (args.length != 3) {
                System.out.println("Usage: [config] [username] [password]");
                System.exit(0);
            }
            ObjectMapper mapper = new ObjectMapper();
            String jsonConfig = FileUtil.readResourceTextFile(args[0] + ".json");
            config = mapper.readValue(jsonConfig, Configuration.class);

            String username = args[1];
            String password = args[2];
            JiraApiUtil.initClient(username, password);

            System.out.println("Open spreadsheet");
            SpreadsheetUtil.initService();
            SpreadsheetEntry spreadsheet = SpreadsheetUtil.getSpreadsheet(config.spreadsheetName);
            List<WorksheetEntry> worksheets = SpreadsheetUtil.getWorksheetFeed(spreadsheet);
            WorksheetEntry ticketsSheet = worksheets.get(1);

            System.out.println("Parse tickets");
            List<Ticket> ticketsInDocument = SpreadsheetUtil.parseTicketSheet(ticketsSheet);
            System.out.println("Start parsing document");
            //updateEditedTickets(ticketsSheet, ticketsInDocument, false);
            updateAllTickets(ticketsSheet);
            System.out.println("Tickets updated");

            ticketsInDocument = SpreadsheetUtil.parseTicketSheet(ticketsSheet);
            removeIgnoredTickets(ticketsInDocument);
            System.out.println("Re-read ticket sheet");

            System.out.println("Update Week Stat");
            updateWeekStat(worksheets, ticketsInDocument);

            System.out.println("Update month Stat");
            updateMonthStat(worksheets, ticketsInDocument);

            System.out.println("Update month Stat");
            updateStatusStat(worksheets, ticketsInDocument);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("... aaaand we're done");

        System.exit(0);
    }

    /**
     * Remove all tickets tagged as ignored in the document so we don't build statistics on them.
     * 
     * @param ticketsInDocument
     */
    private static void removeIgnoredTickets(List<Ticket> ticketsInDocument) {
        for (int i = 0; i < ticketsInDocument.size(); i++) {
            if (ticketsInDocument.get(i).isIgnoreRow()) {
                ticketsInDocument.remove(i);
                i--;
            }
        }

    }

    /**
     * Updates the StatusStat sheet of the document. Calculates statistics from the ticket list and dumps it to the
     * document.
     * 
     * @param worksheets
     * @param ticketsInDocument
     * @throws Exception
     */
    private static void updateStatusStat(List<WorksheetEntry> worksheets, List<Ticket> ticketsInDocument) throws Exception {
        List<List<String>> statData = StatUtil.buildTimeInStatusPerMonth(ticketsInDocument);
        System.out.println("Stats built");
        WorksheetEntry weekSheet = worksheets.get(4);
        SpreadsheetUtil.printDataToSheet(weekSheet, statData);
    }

    /**
     * Updates the WeekStat sheet of the document. Calculates statistics from the ticket list and dumps it to the
     * document. Could be a lot faster since it will update all rows while only the top row will change (and possibly be
     * added)
     * 
     * @param worksheets
     * @param ticketsInDocument
     * @throws Exception
     */
    private static void updateWeekStat(List<WorksheetEntry> worksheets, List<Ticket> ticketsInDocument) throws Exception {
        List<List<String>> weekStat = StatUtil.buildStoryPointResolvedPerPeriod(ticketsInDocument, StatPeriod.WEEK);
        System.out.println("Stats built");
        WorksheetEntry weekSheet = worksheets.get(2);
        SpreadsheetUtil.printDataToSheet(weekSheet, weekStat);
    }

    /**
     * Updates the MonthStat sheet of the document. Calculates statistics from the ticket list and dumps it to the
     * document. Could be a lot faster since it will update all rows while only the top row will change (and possibly be
     * added)
     * 
     * @param worksheets
     * @param ticketsInDocument
     * @throws Exception
     */
    private static void updateMonthStat(List<WorksheetEntry> worksheets, List<Ticket> ticketsInDocument) throws Exception {
        List<List<String>> monthStat = StatUtil.buildStoryPointResolvedPerPeriod(ticketsInDocument, StatPeriod.MONTH);
        System.out.println("Stats built");
        WorksheetEntry weekSheet = worksheets.get(3);
        SpreadsheetUtil.printDataToSheet(weekSheet, monthStat);
    }

    /**
     * Fetches tickets that has been changed during the last two days and updates the Tickets worksheet accordingly
     * 
     * @param worksheets
     * @param ticketsInDocument
     * @throws Exception
     */
    private static void updateEditedTickets(WorksheetEntry ticketsSheet, List<Ticket> ticketsInDocument, boolean onlyNew) throws Exception {
        CellFeed cellFeed = SpreadsheetUtil.getCellFeed(ticketsSheet);
        Map<String, Ticket> documentHash = new HashMap<>();
        for (Ticket loopTicket : ticketsInDocument) {
            documentHash.put(loopTicket.getId().toUpperCase(), loopTicket);
        }
        int row = SpreadsheetUtil.getListFeed(ticketsSheet).getEntries().size() + 1;

        Iterable<Issue> issueItr = JiraApiUtil.getTicketList("-2d", 0);
        System.out.println("Ticket list fetched");
        for (Issue issue : issueItr) {
            Ticket previousTicket = documentHash.get(issue.getKey().toUpperCase());

            if (previousTicket == null || !onlyNew) {
                System.out.println("Handle ticket: " + issue.getKey());
                Ticket ticket = JiraApiUtil.getIssue(issue.getKey());
                if (previousTicket == null) {
                    row++;
                    SpreadsheetUtil.setRow(cellFeed, row, ticket.getValues());
                } else {
                    SpreadsheetUtil.setRow(cellFeed, previousTicket.getSheetRow(), ticket.getValues());
                }
            }
        }
    }

    /**
     * Not used in the main program. Used as run once the first time the board is imported (or when a bug is corrected). This may take hours. 
     * 
     * @param ticketsSheet
     * @throws Exception
     */
    @SuppressWarnings("unused")
    private static void updateAllTickets(WorksheetEntry ticketsSheet) throws Exception {
        CellFeed cellFeed = SpreadsheetUtil.getCellFeed(ticketsSheet);

        Iterable<Issue> issueItr = JiraApiUtil.getTicketList("-104w", 0);
        int row = 2;
        int loopCnt = 0;
        int count = 0;
        while (true) {
            count=0;
            for (Issue issue : issueItr) {
                Ticket ticket = JiraApiUtil.getIssue(issue.getKey());
                SpreadsheetUtil.setRow(cellFeed, row++, ticket.getValues());
                count++;
            }
            if(count > 0){
                loopCnt++;
                issueItr = JiraApiUtil.getTicketList("-104w", loopCnt*500);
            }
            else{
                break;
            }
        }

    }

}
