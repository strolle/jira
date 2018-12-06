package se.metro.jira;

import java.util.ArrayList;
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
            boolean allCsv = false;
            if (args.length == 4 && args[3].equals("ALL")) {
                allCsv = true;
            } else if (args.length != 3) {
                System.out.println("Usage: [config] [username] [password]");
                System.exit(0);
            }
            ObjectMapper mapper = new ObjectMapper();
            String jsonConfig = FileUtil.readResourceTextFile(args[0] + ".json");
            config = mapper.readValue(jsonConfig, Configuration.class);

            String username = args[1];
            String password = args[2];
            JiraApiUtil.initClient(username, password);

            if(allCsv){
                System.out.println("Read all tickets");
                updateAllTickets();
                System.out.println("... aaaand we're done");
                System.exit(0);
            }
            
            System.out.println("Open spreadsheet");
            SpreadsheetUtil.initService();
            SpreadsheetEntry spreadsheet = SpreadsheetUtil.getSpreadsheet(config.getSpreadsheetName());
            List<WorksheetEntry> worksheets = SpreadsheetUtil.getWorksheetFeed(spreadsheet);
            WorksheetEntry ticketsSheet = worksheets.get(1);

            System.out.println("Parse tickets");
            List<Ticket> ticketsInDocument = SpreadsheetUtil.parseTicketSheet(ticketsSheet);
            System.out.println("Start parsing document");
            updateEditedTickets(ticketsSheet, ticketsInDocument, false);
            // updateAllTickets(ticketsSheet);
            System.out.println("Tickets updated");

            ticketsInDocument = SpreadsheetUtil.parseTicketSheet(ticketsSheet);
            removeIgnoredTickets(ticketsInDocument);
            System.out.println("Re-read ticket sheet");

            System.out.println("Update Week Stat");
            updateWeekStat(worksheets, ticketsInDocument);

            System.out.println("Update month Stat");
            updateMonthStat(worksheets, ticketsInDocument);

            System.out.println("Update status Stat");
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
                Ticket t = ticketsInDocument.remove(i);
                System.out.println("Ignore ticket: " + t.getId());
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
        List<List<String>> weekStat = StatUtil.buildStatResolvedPerPeriod(ticketsInDocument, StatPeriod.WEEK);
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
        List<List<String>> monthStat = StatUtil.buildStatResolvedPerPeriod(ticketsInDocument, StatPeriod.MONTH);
        System.out.println("Stats built");
        WorksheetEntry weekSheet = worksheets.get(3);
        SpreadsheetUtil.printDataToSheet(weekSheet, monthStat);
    }

    /**
     * Fetches tickets that has been changed during the last two days and updates the Tickets worksheet accordingly
     *
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

        Iterable<Issue> issueItr = JiraApiUtil.getTicketList("-9d", 0);
        System.out.println("Ticket list fetched");
        for (Issue issue : issueItr) {
            System.out.println("Ticket: " + issue.getKey());
            Ticket previousTicket = documentHash.get(issue.getKey().toUpperCase());

            if (previousTicket == null || !onlyNew) {
                System.out.println("Handle ticket: " + issue.getKey());
                Ticket ticket = JiraApiUtil.getIssue(issue.getKey());
                if (previousTicket == null) {
                    row++;
                    System.out.println("New - row " + row);
                    SpreadsheetUtil.setRow(cellFeed, row, ticket.getValues());
                } else {
                    SpreadsheetUtil.setRow(cellFeed, previousTicket.getSheetRow(), ticket.getValues());
                }
            }
        }

        //set ignore
        for(String issueId : JiraApiUtil.parentIssues){
            Ticket previousTicket = documentHash.get(issueId.toUpperCase());
            if(previousTicket != null && !previousTicket.isIgnoreRow()) {
                previousTicket.setIgnoreRow(true);
                SpreadsheetUtil.setRow(cellFeed, previousTicket.getSheetRow(), previousTicket.getValues());
            }
        }

    }

    /**
     * Not used in the main program. Used as run once the first time the board is imported (or when a bug is corrected).
     * This may take hours.
     *
     * @throws Exception
     */
    private static void updateAllTickets() {
        Iterable<Issue> issueItr = JiraApiUtil.getTicketList("-108w", 0);
        int loopCnt = 0;

        List<Ticket> tickets = new ArrayList<>();
        while (true) {
            int count = 0;
            for (Issue issue : issueItr) {
                Ticket ticket = JiraApiUtil.getIssue(issue.getKey());
                tickets.add(ticket);
                count++;
                System.out.println(loopCnt + "-" + count + " - " + ticket.getValues());
            }
            if (count > 0) {
                loopCnt++;
                issueItr = JiraApiUtil.getTicketList("-108w", loopCnt * 100);
            } else {
                break;
            }
        }

        System.out.println("CSV");

        for (Ticket ticket : tickets) {
            if(JiraApiUtil.parentIssues.contains(ticket.getId())){
                ticket.setIgnoreRow(true);
            }

            List<String> values = ticket.getValues();
            for (int i = 0; i < values.size(); i++) {
                if (i != 0)
                    System.out.print(",");
                System.out.print(values.get(i));
            }
            System.out.println();
        }
    }

}
