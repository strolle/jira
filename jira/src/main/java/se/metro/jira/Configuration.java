package se.metro.jira;

import java.util.Arrays;

public class Configuration {
    public String spreadsheetName;
    public String jql;
    public String[] projects;
    
    public String getSpreadsheetName() {
        return spreadsheetName;
    }
    public void setSpreadsheetName(String spreadsheetName) {
        this.spreadsheetName = spreadsheetName;
    }
    public String getJql() {
        return jql;
    }
    public void setJql(String jql) {
        this.jql = jql;
    }
    public String[] getProjects() {
        return projects;
    }
    public void setProjects(String[] projects) {
        this.projects = projects;
    }
    @Override
    public String toString() {
        return "Configuration [spreadsheetName=" + spreadsheetName + ", jql=" + jql + ", projects=" + Arrays.toString(projects) + "]";
    }

}
