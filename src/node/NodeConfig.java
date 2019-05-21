package node;

import common.NodeInfo;

public class NodeConfig {

    public static Integer bootstrapPort() {
        return Integer.parseInt(System.getProperty("bootstrap.port"));
    }

    public static String bootstrapIp() {
        return System.getProperty("bootstrap.ip");
    }

    public static NodeInfo bootstrap() {
        return new NodeInfo(bootstrapIp(), bootstrapPort());
    }

    public static Integer nodePort() {
        return Integer.parseInt(System.getProperty("node.port"));
    }

}
