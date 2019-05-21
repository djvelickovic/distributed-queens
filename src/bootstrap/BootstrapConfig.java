package bootstrap;

import common.NodeInfo;
import common.util.ConfigUtil;

import java.util.Properties;

public class BootstrapConfig {

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

    public static Boolean testDelay() {
        return Boolean.parseBoolean(properties.getProperty("test.delay"));
    }

    public static void readConfig(String fileName) {
        properties = ConfigUtil.readConfig(fileName);
    }

    public static void setBootstrapPort(String nodePort) {
        properties.setProperty("bootstrap.port", nodePort);
    }

    public static void setBootstrapIp(String bootstrapIp) {
        properties.setProperty("bootstrap.ip", bootstrapIp);
    }
}
