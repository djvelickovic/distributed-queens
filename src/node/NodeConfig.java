package node;

import common.ConfigUtil;
import common.NodeInfo;

import java.util.Properties;

public class NodeConfig {

    private static Properties properties;


    public static Integer bootstrapPort() {
        return Integer.parseInt(properties.getProperty("bootstrap.port"));
    }

    public static String bootstrapIp() {
        return properties.getProperty("bootstrap.ip");
    }

    public static NodeInfo bootstrap() {
        return new NodeInfo(bootstrapIp(), bootstrapPort());
    }

    public static Integer nodePort() {
        return Integer.parseInt(properties.getProperty("node.port"));
    }

    public static Integer limit() {
        return Integer.parseInt(properties.getProperty("node.limit"));
    }

    public static Boolean testDelay() {
        return Boolean.parseBoolean(properties.getProperty("test.delay"));
    }

    public static void readConfig(String fileName) {
        properties = ConfigUtil.readConfig(fileName);
    }

    public static void setNodePort(String nodePort) {
        properties.setProperty("node.port", nodePort);
    }
}
