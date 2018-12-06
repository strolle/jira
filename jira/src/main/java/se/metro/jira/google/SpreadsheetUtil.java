package se.metro.jira.google;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.gdata.client.spreadsheet.SpreadsheetQuery;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.CellEntry;
import com.google.gdata.data.spreadsheet.CellFeed;
import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.data.spreadsheet.ListFeed;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetFeed;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.data.spreadsheet.WorksheetFeed;

import se.metro.jira.communication.domain.Ticket;

public class SpreadsheetUtil {
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String SPREADSHEET_SERVICE_URL = "https://spreadsheets.google.com/feeds/spreadsheets/private/full";
    private static SpreadsheetService service = null;

    public static void initService() throws Exception {
        System.out.println(new File("jira-spread-0fcbed75c4ea.p12").getAbsolutePath());
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        GoogleCredential credential = new GoogleCredential.Builder().setTransport(httpTransport).setJsonFactory(JSON_FACTORY)
                .setServiceAccountId("account-1@jira-spread.iam.gserviceaccount.com")
                .setServiceAccountScopes(Arrays.asList("https://spreadsheets.google.com/feeds"))
                .setServiceAccountPrivateKeyFromP12File(new File("jira-spread-0fcbed75c4ea.p12"))
                .build();
        service = new SpreadsheetService("jira-spread");
        service.setOAuth2Credentials(credential);
    }

    public static SpreadsheetEntry getSpreadsheet(String sheetName) throws Exception {
        try {
            URL spreadSheetFeedUrl = new URL(SPREADSHEET_SERVICE_URL);

            SpreadsheetQuery spreadsheetQuery = new SpreadsheetQuery(spreadSheetFeedUrl);
            spreadsheetQuery.setTitleQuery(sheetName);
            spreadsheetQuery.setTitleExact(true);
            SpreadsheetFeed spreadsheet = service.getFeed(spreadsheetQuery, SpreadsheetFeed.class);

            if (spreadsheet.getEntries() != null && spreadsheet.getEntries().size() == 1) {
                return spreadsheet.getEntries().get(0);
            } else {
                throw new Exception();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    public static List<WorksheetEntry> getWorksheetFeed(SpreadsheetEntry spreadsheet) throws Exception {
        WorksheetFeed worksheetFeed = service.getFeed(spreadsheet.getWorksheetFeedUrl(), WorksheetFeed.class);
        return worksheetFeed.getEntries();
    }

    public static ListFeed getListFeed(WorksheetEntry worksheet) throws Exception{
        return service.getFeed(worksheet.getListFeedUrl(), ListFeed.class);
    }
    public static CellFeed getCellFeed(WorksheetEntry worksheet) throws Exception{
        return service.getFeed(worksheet.getCellFeedUrl(), CellFeed.class);
    }
    
    public static void printDataToSheet(WorksheetEntry weekSheet, List<List<String>> weekStat) throws Exception {
        CellFeed cellFeed = SpreadsheetUtil.getCellFeed(weekSheet);
        System.out.println("open cell feed");
        int rowCnt = 2;
        for (List<String> row : weekStat) {
            System.out.println("writing " + rowCnt + " - " + row.get(0));
            SpreadsheetUtil.setRow(cellFeed, rowCnt++, row);
        }
        System.out.println("Done writing to spreadsheet");
    }

    public static void setRow(CellFeed cellFeed, int row, List<String> values) throws Exception {
        for (int i = 0; i < values.size(); i++) {
            CellEntry cell = new CellEntry(row, i + 1, values.get(i));
            cellFeed.insert(cell);
        }
    }
    
    public static List<Ticket> parseTicketSheet(WorksheetEntry ticketsSheet) throws Exception{
        List<Ticket> ticketList = new ArrayList<>();
        ListFeed listFeed = SpreadsheetUtil.getListFeed(ticketsSheet);
        int i = 1;
        for(ListEntry row : listFeed.getEntries()){
            i++;
            Ticket ticket = new Ticket();
            ticket.setSheetRow(i);
            ticketList.add(ticket);
            for (String tag : row.getCustomElements().getTags()) {
                String value = row.getCustomElements().getValue(tag);
                ticket.setValue(tag, value);
            }
        }
        
        return ticketList;
    }

}
