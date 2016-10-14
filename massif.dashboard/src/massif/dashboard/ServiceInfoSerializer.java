package massif.dashboard;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class ServiceInfoSerializer  extends JsonSerializer<ServiceInfo>{

	@Override
	public void serialize(ServiceInfo serviceInfo, JsonGenerator jgen, SerializerProvider arg2)
			throws IOException, JsonProcessingException {
		jgen.writeStartObject();
		jgen.writeStringField("serviceId", serviceInfo.getServiceID());
		jgen.writeStringField("componentName", serviceInfo.getComponentName());
		jgen.writeBooleanField("adaptable", serviceInfo.isAdaptable());
		
	}
	
}
