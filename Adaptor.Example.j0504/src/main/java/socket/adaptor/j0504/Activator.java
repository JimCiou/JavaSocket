package socket.adaptor.j0504;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.apache.log4j.Logger;  
import org.apache.log4j.PropertyConfigurator;

public class Activator implements BundleActivator {

	Adaptor adaptor=new Adaptor();
	Logger logger = Logger.getLogger(Activator.class); 

	public void start(BundleContext context) {

		adaptor.start();
		logger.warn("Starting the gt350s");
	}

	public void stop(BundleContext context) {

		adaptor.gt350sStop();
		logger.warn("Stopping the gt350s");
	}

}
