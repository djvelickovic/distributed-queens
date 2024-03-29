package com.crx.kids.project.node.endpoints;

public class Methods {


    // TestEndpoint
    public static final String STATUS = "test/status";


    public static String PING = "test/ping";


    // NodeEndpoint
    public static final String HOST_ACK = "node/host-ack";
    public static String NEWBIE_JOIN = "node/newbie-join";
    public static String NEWBIE_ACCEPTED = "node/newbie-accepted";
    public static String ALTER_NEIGHBOURS = "node/alter-neighbours";
    public static String BROADCAST_JOIN = "node/join-broadcast";
    public static String BROADCAST_LEAVE = "node/leave-broadcast";
    public static String HOST_REQUEST = "node/host-request";
    public static final String MAX_LEAVE = "node/max-leave";

    // CriticalSectionEndpoint
    public static String BROADCAST_CRITICAL_SECTION = "critical-section/broadcast";
    public static String CRITICAL_SECTION_TOKEN = "critical-section/token";


    // JobEndpoint
    public static final String QUEENS_JOBS = "job/queens";
    public static final String QUEENS_START = "job/queens-start";
    public static final String QUEENS_PAUSE = "job/queens-pause";
    public static final String QUEENS_STATUS = "job/queens-status";
    public static final String QUEENS_STATUS_COLLECTOR = "job/queens-status-collector";

    public static final String JOB_STEALING_REQUEST = "job/stealing-request";
    public static final String JOB_STEALING_COLLECTOR = "job/stealing-collector";
    public static final String QUEENS_RESULT_BROADCAST = "job/queens-result-broadcast";



}
