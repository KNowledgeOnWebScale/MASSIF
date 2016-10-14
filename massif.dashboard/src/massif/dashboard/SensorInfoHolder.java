package massif.dashboard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = SensorInfoHolderSerializer.class)
public class SensorInfoHolder {
	Map<String,SensorInfo> sensorInfos;
	List<Map<String,Object>> actuatorRequests;
	public SensorInfoHolder(){
		sensorInfos = new HashMap<String,SensorInfo>();
		actuatorRequests = new ArrayList<Map<String,Object>>();
	}
	
	public boolean addSensorInfo(SensorInfo serviceInfo){
		if(!sensorInfos.containsKey(serviceInfo.getSensorId())){
			sensorInfos.put(serviceInfo.getSensorId(), serviceInfo);
			return true;
		}else{
			return false;
		}
	}
	public boolean removeSensorInfo(SensorInfo serviceInfo){
		if(sensorInfos.containsKey(serviceInfo.getSensorId())){
			sensorInfos.remove(serviceInfo.getSensorId());
			return true;
		}else{
			return false;
		}
	}
	public boolean removeSensorInfo(String serviceID){
		if(sensorInfos.containsKey(serviceID)){
			sensorInfos.remove(serviceID);
			return true;
		}else{
			return false;
		}
	}
	public Collection<SensorInfo> getSensors(){
		return sensorInfos.values();
	}
	public void sensorUpdate(String sensorID, Map<String,Object> sensorValues){
		if(sensorInfos.containsKey(sensorID)){
			sensorInfos.get(sensorID).update(sensorValues);
		}else{
			SensorInfo newSensor = new SensorInfo(sensorValues);
			sensorInfos.put(sensorID, newSensor);
		}
	}
	public boolean isSensorVirtual(String sensorID){
		if(sensorInfos.containsKey(sensorID)){
			return sensorInfos.get(sensorID).isVirtual();
		}else{
			return false;
		}
	}
	public void actuate(String sensorID, String type){
		if(sensorInfos.containsKey(sensorID)){
			Map<String,Object> actRequest = new HashMap<String,Object>();
			actRequest.put("sensor", sensorInfos.get(sensorID));
			if(type.contains("TurnOn")){	
				actRequest.put("newValue", "true");
			}else{
				actRequest.put("newValue", "false");
			}
			actuatorRequests.add(actRequest);
		}
	}
	public List<Map<String,Object>> getActuatorRequests(){
		return actuatorRequests;
	}
	public void flushRequests(){
		actuatorRequests.clear();
	}
}
