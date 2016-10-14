package massif.dashboard;

import java.util.Map;
import java.util.Properties;

public class SensorInfo {
	
	private String type;
	private String id;
	private String value;
	private boolean online;
	private String location;
	private boolean virtual;
	

	public SensorInfo(Map<String,Object> values){
		extractValues(values);	
	}
	public SensorInfo(Properties properties){
		this.type = properties.getProperty("type");
		this.id = properties.getProperty("id");
		this.value = properties.getProperty("value");
		this.online = properties.getProperty("online", "true").equals("true")?true:false;
		this.location = properties.getProperty("location");
		this.virtual = true;
	}
	
	public String getType(){
		return type;
	}
	
	public String getSensorId(){
		return id;
	}
	
	public void update(Map<String,Object> values){
		extractValues(values);
	}
	public String getId() {
		return id;
	}

	public String getValue() {
		return value;
	}

	public boolean isOnline() {
		return online;
	}
	public boolean isVirtual(){
		return virtual;
	}
	public String getLocation() {
		return location;
	}
	private void extractValues(Map<String,Object> values){
		this.type = convertType((String)values.get("@type"));
		this.id = (String)((Map)values.get("ssn:observedBy")).get("upper:hasID");
		this.value = (String)((Map)((Map)values.get("ssn:observationResult")).get("ssn:hasValue")).get("dul:hasDataValue");
		String onlineString = (String)((Map)((Map)values.get("ssn:observationResult")).get("ssn:hasValue")).get("dul:hasParameterDataValue");
		this.online = "true".equals(onlineString);
	}
	private String convertType(String type){
		String newType = type;
		if(type.contains("Button")){
			newType = "button";
		}
		else if(type.contains("RFID")){
			newType = "person";
		}
		else if(type.contains("DoorWindow")){
			newType = "contact";
		}else if(type.contains("Temperature")){
			newType = "temperature";
		}else if(type.contains("Light")){
			newType = "lamp";
		}else if(type.contains("Lock")){
			newType = "locked";
		}else if(type.contains("Window")){
			newType = "window";
		}
		return newType;
	}
	public void setValue(String value){
		this.value = value;
	}

}
