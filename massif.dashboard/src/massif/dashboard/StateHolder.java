package massif.dashboard;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = StateInfoHolderSerializer.class)
public class StateHolder {
	
	private ServiceInfoHolder serviceInfo;
	private SensorInfoHolder sensorInfo;

	public StateHolder(ServiceInfoHolder serviceInfo, SensorInfoHolder sensorInfo){
		this.serviceInfo = serviceInfo;
		this.sensorInfo = sensorInfo;
	}
	
	public ServiceInfoHolder getServiceInfo(){
		return serviceInfo;
	}
	
	public SensorInfoHolder getSensorInfo(){
		return sensorInfo;
	}

}
