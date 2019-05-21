package common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigUtil {

	public static void readConfig(String configName){
		try (FileInputStream fis = new FileInputStream(new File(configName))) {
			Properties properties = new Properties();
			properties.load(fis);

			System.setProperties(properties);

		} catch (IOException e) {
			Log.error("Couldn't open properties file. Exiting...");
			System.exit(0);
		}
	}

	
}
