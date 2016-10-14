package massif.kafka.scb.util;

import java.util.Map;

import massif.mciservice.api.MCIService;

public class ServiceInfo {
	
	private MCIService service;
	private Map<String, Object> properties;

	public MCIService getService() {
		return service;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	public ServiceInfo(MCIService service, Map<String,Object> properties){
		this.service = service;
		this.properties = properties;
	}

	/**
	 * GETTER component name of the OSGi bundle
	 * @return				The name of the component
	 */
	public String getComponentName() {
		return this.properties.get("component.name").toString();
	}
}
