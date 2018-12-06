package se.metro.jira.communication.domain;

import java.util.HashMap;
import java.util.Map;

public class ProjectStat {
    public final Map<String, Integer> storyMap = new HashMap<>();
    public final Map<String, Double> devTimeMap = new HashMap<>();
}