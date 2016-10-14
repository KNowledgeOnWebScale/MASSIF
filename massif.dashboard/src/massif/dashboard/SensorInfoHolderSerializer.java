package massif.dashboard;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class SensorInfoHolderSerializer  extends JsonSerializer<SensorInfoHolder>{

	@Override
	public void serialize(SensorInfoHolder sensorInfos, JsonGenerator jgen, SerializerProvider arg2)
			throws IOException, JsonProcessingException {
		//jgen.writeArrayFieldStart("sensors");

		
		jgen.writeStartObject();
		jgen.writeFieldName("state");
		jgen.writeStartArray();
		for(SensorInfo serviceInfo: sensorInfos.getSensors()){
			jgen.writeStartObject();
			//jgen.writeObjectField("service", serviceInfo);
			writeSensorInfo(serviceInfo, jgen);

			
			jgen.writeEndObject();
		}
		jgen.writeEndArray();
		//jgen.writeEndObject();
		
		//jgen.writeStartObject();
		jgen.writeFieldName("requests");
		jgen.writeStartArray();
		for(Map<String,Object> actRequests:sensorInfos.getActuatorRequests()){
			String newValue = (String) actRequests.get("newValue");
			SensorInfo sensorInfo = (SensorInfo)actRequests.get("sensor");
			jgen.writeStartObject();
			jgen.writeStringField("newValue", newValue);
			writeSensorInfo(sensorInfo, jgen);

			jgen.writeEndObject();

		}
		jgen.writeEndArray();
		jgen.writeEndObject();
		sensorInfos.flushRequests();

	}
	private void writeSensorInfo(SensorInfo sensorInfo,JsonGenerator jgen) throws JsonGenerationException, IOException{
		jgen.writeStringField("sensorId", sensorInfo.getSensorId());
		jgen.writeStringField("type", sensorInfo.getType());
		jgen.writeStringField("value", sensorInfo.getValue());
		jgen.writeBooleanField("virtual", sensorInfo.isVirtual());
		jgen.writeBooleanField("online", sensorInfo.isOnline());
	}
}
