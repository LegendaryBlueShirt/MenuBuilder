package menubuilder;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {
	static Properties properties;
	
	public static Properties getInstance() {
		if(properties == null) {
			loadProperties();
		}
		return properties;
	}
	
	private static void loadProperties() {
		properties = new Properties();
		try {
		  properties.load(new FileInputStream("config.properties"));
		} catch (IOException e) {
			System.out.println("Properties file not found.  Generating new file.");
			properties.setProperty("locale", "english");
			saveProperties();
		}
	}
	
	public static void saveProperties() {
		FileOutputStream fos;
		try {
			fos = new FileOutputStream("config.properties");
			properties.store(fos, null);
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
