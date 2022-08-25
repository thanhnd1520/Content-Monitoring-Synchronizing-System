package config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Configuration {
	static Properties prop ;
	static FileInputStream fis;
	public static String getProperties(String key) {
		try {
			prop = new Properties();
			fis = new FileInputStream("config.properties");
			prop.load(fis);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return prop.getProperty(key, "null");
	}
}
