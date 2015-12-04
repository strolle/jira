package se.metro.jira;

public class Configuration {
    public String spreadsheetName;
    public String jql;
    public String storyPointField;
    public String projectField;
    public String proj1String;
    public String proj2String;
    
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
    public String getStoryPointField() {
        return storyPointField;
    }
    public void setStoryPointField(String storyPointField) {
        this.storyPointField = storyPointField;
    }
    public String getProjectField() {
        return projectField;
    }
    public void setProjectField(String projectField) {
        this.projectField = projectField;
    }
    public String getProj1String() {
        return proj1String;
    }
    public void setProj1String(String proj1String) {
        this.proj1String = proj1String;
    }
    public String getProj2String() {
        return proj2String;
    }
    public void setProj2String(String proj2String) {
        this.proj2String = proj2String;
    }
    @Override
    public String toString() {
        return "Configuration [spreadsheetName=" + spreadsheetName + ", jql=" + jql + ", storyPointField=" + storyPointField + ", projectField=" + projectField
                + ", proj1String=" + proj1String + ", proj2String=" + proj2String + "]";
    }
}
