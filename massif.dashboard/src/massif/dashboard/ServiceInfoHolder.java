package massif.dashboard;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
@JsonSerialize(using = ServiceInfoHolderSerializer.class)
public class ServiceInfoHolder {

	Map<String,ServiceInfo> serviceInfos;
	
	public ServiceInfoHolder(){
		serviceInfos = new HashMap<String,ServiceInfo>();
	}
	
	public boolean addServiceInfo(ServiceInfo serviceInfo){
		if(!serviceInfos.containsKey(serviceInfo.getServiceID())){
			serviceInfos.put(serviceInfo.getServiceID(), serviceInfo);
			return true;
		}else{
			return false;
		}
	}
	public boolean removeServiceInfo(ServiceInfo serviceInfo){
		if(serviceInfos.containsKey(serviceInfo.getServiceID())){
			serviceInfos.remove(serviceInfo.getServiceID());
			return true;
		}else{
			return false;
		}
	}
	public boolean removeServiceInfo(String serviceID){
		if(serviceInfos.containsKey(serviceID)){
			serviceInfos.remove(serviceID);
			return true;
		}else{
			return false;
		}
	}
	public Collection<ServiceInfo> getServices(){
		return serviceInfos.values();
	}
}
