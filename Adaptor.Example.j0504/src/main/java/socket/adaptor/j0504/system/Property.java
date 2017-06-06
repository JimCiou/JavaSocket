package socket.adaptor.j0504.system;

import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import org.apache.log4j.Logger;  
import org.apache.log4j.PropertyConfigurator;

public class Property {

	Properties prop = new Properties();
	Logger logger = Logger.getLogger(Property.class); 

	//寫入properties
	public void writeproperties(String prop_path, String item, String content) {

		OutputStream output = null;

		try {

			output = new FileOutputStream(prop_path);
			// set the properties value
			prop.setProperty(item, content);

			// save properties to project root folder
			prop.store(output, null);

		} catch (IOException io) {
				io.printStackTrace();
		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException e) {
					e.printStackTrace();}
			}
		}
	}

	//讀取properties
	public String loadproperties(String prop_path, String item) {

		InputStream input = null;
		String context = null;

		try {

			input = new FileInputStream(prop_path);
			// load a properties file
			prop.load(input);

			// get the property value and print it out
			context = prop.getProperty(item);

		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();}
			}
		}
		return context;
	}
}
