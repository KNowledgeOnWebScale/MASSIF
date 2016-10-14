package massif.parallelization.api;

import java.util.Dictionary;
import java.util.List;
import java.util.Map;

public interface LocalConfigurator {
	
	public void startServices(int number, String pid);
	public void stopServices(int number,String pid);
	public void update(int number, String clazz, String pid, String componentName, String originalID);
	/**
	 * Starts a new services and passes the properties as an update
	 * @param pid	pid of the service
	 * @param newServiceProperties	properties for the new service
	 * @return	true if successful, false otherwise
	 */
	public boolean startService(String pid, Dictionary<String, Object> newServiceProperties);

}
