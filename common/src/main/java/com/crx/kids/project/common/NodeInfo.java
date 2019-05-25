package com.crx.kids.project.common;

import java.util.Objects;

public class NodeInfo {
    private String ip;
    private int port;

    public NodeInfo(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeInfo nodeInfo = (NodeInfo) o;
        return port == nodeInfo.port &&
                ip.equals(nodeInfo.ip);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ip, port);
    }

    @Override
    public String toString() {
        return "NodeInfo{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                '}';
    }
}
