package se.metro.jira.communication.domain;

import java.util.HashMap;
import java.util.Map;

public class ProjectStat {
    public Map<String, Integer> storyMap = new HashMap<>();
    public Map<String, Double> devTimeMap = new HashMap<>();
}