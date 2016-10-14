package massif.dashboard;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class StateInfoHolderSerializer  extends JsonSerializer<StateHolder>{

	@Override
	public void serialize(StateHolder stateInfos, JsonGenerator jgen, SerializerProvider arg2)
			throws IOException, JsonProcessingException {

		
			jgen.writeStartObject();
			//jgen.writeObjectField("service", serviceInfo);
			jgen.writeObjectField("sensors", stateInfos.getSensorInfo());
			jgen.writeObjectField("services", stateInfos.getServiceInfo());


			jgen.writeEndObject();
	
		
	}
	
}
