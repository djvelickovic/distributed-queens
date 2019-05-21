package bootstrap;

import common.ConfigUtil;
import common.NodeInfo;

import java.util.Properties;

public class BootstrapConfig {

    private static Properties properties;


    public static Integer bootstrapPort() {
        return Integer.parseInt(properties.getProperty("bootstrap.port"));
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
}
