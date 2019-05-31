package com.crx.kids.project.node.endpoints;

public class Methods {


    // TestEndpoint
    public static final String STATUS = "test/status";
    public static String PING = "test/ping";


    // NodeEndpoint
    public static String NEWBIE_JOIN = "node/newbie-join";
    public static String NEWBIE_ACCEPTED = "node/newbie-accepted";
    public static String ALTER_NEIGHBOURS = "node/alter-neighbours";
    public static String BROADCAST_JOIN = "node/join-broadcast";
    public static String BROADCAST_LEAVE = "node/leave-broadcast";


    // CriticalSectionEndpoint
    public static String BROADCAST_CRITICAL_SECTION = "critical-section/broadcast";
    public static String CRITICAL_SECTION_TOKEN = "critical-section/token";


    // JobEndpoint
    public static final String QUEENS_JOBS = "job/queens";
    public static final String QUEENS_START = "job/queens-start";
    public static final String QUEENS_PAUSE = "job/queens-pause";
    public static final String QUEENS_STATUS = "job/queens-status";
    public static final String QUEENS_STATUS_COLLECTOR = "job/queens-status-collector";
}
