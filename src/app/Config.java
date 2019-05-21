package app;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * This class contains all the global application configuration stuff.
 * @author bmilojkovic
 *
 */
public class Config {


	public static void readConfig(String configName){
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(new File(configName)));
			
		} catch (IOException e) {
			Log.stderr("Couldn't open properties file. Exiting...");
			System.exit(0);
		}

	}

	
}
