package com.crx.kids.project.node.utils;

import com.crx.kids.project.common.NodeInfo;

public class NetUtil {


    public static String url(NodeInfo nodeInfo, String path) {
        return String.format("http://%s:%d/%s", nodeInfo.getIp(), nodeInfo.getPort(), path);
    }
}
