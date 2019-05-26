package com.crx.kids.project.node.net;

import com.crx.kids.project.common.NodeInfo;

import java.text.MessageFormat;

public class NetUtil {


    public static String url(NodeInfo nodeInfo, String path) {
        return MessageFormat.format("http://{0}:{1}/{3}", nodeInfo.getIp(), nodeInfo.getPort(), path);
    }
}
