package massif.dashboard;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import massif.framework.dashboard.api.AdaptableService;
import massif.mciservice.api.MCIService;
@JsonSerialize(using = ServiceInfoSerializer.class)
public class ServiceInfo {

	private MCIService service;
	private String serviceID;
	private String componentName;
	private boolean adaptable;
	
	private Map<String,Object> infoMap;
	
	public ServiceInfo(MCIService service){
		this.service = service;
		if(service instanceof AdaptableService){
			adaptable = true;
			
		}
		HashMap<String,Object> infoMap = new HashMap<String,Object>();
		infoMap.put("serviceID", serviceID);
		infoMap.put("componentName", componentName);
		infoMap.put("adaptable", adaptable);
		
		if(adaptable){
			infoMap.put("queries", ((AdaptableService)service).getQueries());
			infoMap.put("filterRules", ((AdaptableService)service).getFilterRules());
			infoMap.put("componentName", ((AdaptableService)service).getName());
			componentName =  ((AdaptableService)service).getName();
		}
		
	}
	public ServiceInfo(MCIService service, Map<String, Object> properties){
		this(service);
		serviceID = properties.get("service.id").toString();
		if(componentName == null){
			componentName = properties.get("component.name").toString();
		}

	}
	
	public Map<String,Object> convertToMap(){	
		return infoMap;
	}
	public MCIService getService() {
		return service;
	}
	public String getServiceID() {
		return serviceID;
	}
	public String getComponentName() {
		return componentName;
	}
	public boolean isAdaptable() {
		return adaptable;
	}
	public List<String> getQueries(){
		if(adaptable){
			return ((AdaptableService)service).getQueries();
		}else{
			return Collections.emptyList();
		}
		
	}
	public List<String> getFilterRules(){
		if(adaptable){
			return ((AdaptableService)service).getFilterRules();
		}else{
			return Collections.emptyList();
		}
		
	}
	
}
